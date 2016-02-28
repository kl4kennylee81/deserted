/*
 * GameplayController.java
 *
 * This class processes the primary gameplay.  It reads from either the player input
 * or the AI controller to determine the move for each ship.  It then updates the
 * velocity and desired angle for each ship, as well as whether or not it will fire.
 *
 * HOWEVER, this class does not actually do anything that would change the animation
 * state of each ship.  It does not move a ship, or turn it. That is the purpose of 
 * the CollisionController.  Our reason for separating these two has to do with the 
 * sense-think-act cycle which we will learn about in class.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

import java.util.Random;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Class to process AI and player input
 *
 * As a major subcontroller, this class must have a reference to all the models.
 */
public class GameplayController {
	/** How close to the center of the tile we need to be to stop drifting */
	private static final float DRIFT_TOLER = 1.0f;
	/** How fast we drift to the tile center when paused */
	private static final float DRIFT_SPEED = 0.325f;

	/** Reference to the game board */
	public Board board; 
	/** Reference to all the ships in the game */	
	public ShipList ships; 
	/** Reference to the active photons */
	public PhotonPool photons; 
	
	/** List of all the input (both player and AI) controllers */
	protected InputController[] controls;

	/** Random number generator for state initialization */
	private Random random;

	/**
	 * Creates a GameplayController for the given models.
	 *
	 * @param board The game board 
	 * @param ships The list of ships 
	 * @param photons The active photons
	 */
	public GameplayController(Board board, ShipList ships, PhotonPool photons) {
		this.board = board;
		this.ships = ships;
		this.photons = photons;
		
		initShipPositions();
		controls = new InputController[ships.size()];
		controls[0] = new PlayerController();
		for(int ii = 1; ii < ships.size(); ii++) {
			controls[ii] = new AIController(ii,board,ships);			
		}
	}

	/**
	 * Initializes the ships to new random locations.  
	 *
	 * The player is always at the center of the board.
	 */
	private void initShipPositions() {
		// Set the player position
		float px = board.boardToScreen(board.getWidth() / 2);
		float py = board.boardToScreen(board.getHeight() / 2);
		ships.get(0).getPosition().set(px,py);

		// Create a list of available AI positions
		Vector2[] positions = new Vector2[board.getWidth() * board.getHeight() - 1];
		int ii = 0;
		for (int x = 0; x < board.getWidth(); x++) {
			for (int y = 0; y < board.getHeight(); y++) {
				// Leave the center space for the player.
				if (x != board.getWidth() / 2 || y != board.getHeight() / 2) {
					positions[ii++] = new Vector2(x, y);
				}	
			}
		}

		// Shuffle positions
		random = new Random();
		Vector2 rTemp = new Vector2();
		for (ii = 0; ii < positions.length; ii++) {
			int jj = random.nextInt(positions.length);
			rTemp.set(positions[ii]);
			positions[ii].set(positions[jj]);
			positions[jj].set(rTemp);
		}	

		// Assign positions
		for (ii = 1; ii < ships.size(); ii++) {
			Vector2 tile = positions[ii-1];
			float sx = board.boardToScreen((int)tile.x);
			float sy = board.boardToScreen((int)tile.y);
			ships.get(ii).getPosition().set(sx, sy);
		}
	}

	/** 
	 * Invokes the controller for this ship.
	 *
     * Movement actions are determined, but not committed (e.g. the velocity
	 * is updated, but not the position). New weapon firing action is processed
	 * but photon collisions are not.
	 */
	public void update() {
		// Adjust for drift and remove dead ships
		for (Ship s : ships) {
			adjustForDrift(s);
			checkForDeath(s);

			if (!s.isFalling() && controls[s.getId()] != null) {
				int action = controls[s.getId()].getAction();
				boolean firing = (action & InputController.CONTROL_FIRE) != 0;
				s.update(action);
				if (firing && s.canFire()) {
					fireWeapon(s);
				} else {
					s.coolDown(true);
				}

			} else {
				s.update(InputController.CONTROL_NO_ACTION);
			}
		}
	}	

	/** 
	 * Nudges the ship back to the center of a tile if it is not moving.
	 *
	 * @param ship The ship to adjust
	 */
	private void adjustForDrift(Ship ship) {
		// Drift to line up vertically with the grid.
		if (ship.getVX() == 0.0f) {
			float offset = board.centerOffset(ship.getX());
			if (offset < -DRIFT_TOLER) {
				ship.setX(ship.getX()+DRIFT_SPEED);
			} else if (offset > DRIFT_TOLER) {
				ship.setX(ship.getX()-DRIFT_SPEED);
			}
		}

		// Drift to line up horizontally with the grid.
		if (ship.getVY() == 0.0f) {
			float offset = board.centerOffset(ship.getY());
			if (offset < -DRIFT_TOLER) {
				ship.setY(ship.getY()+DRIFT_SPEED);
			} else if (offset > DRIFT_TOLER) {
				ship.setY(ship.getY()-DRIFT_SPEED);
			}
		}
	}
	
	/**
	 * Determines if a ship is on a destroyed tile. 
	 *
	 * If so, the ship is killed.
	 *
	 * @param ship The ship to check
	 */
	private void checkForDeath(Ship ship) {
		// Nothing to do if ship is already dead.
		if (!ship.isActive()) {
			return;
		}

		// Get the tile for the ship
		int tx = board.screenToBoard(ship.getX());
		int ty = board.screenToBoard(ship.getY());

		if (!board.inBounds(tx,ty) || board.isDestroyedAt(tx, ty)) {
			ship.play(SoundController.FALL_SOUND);
			ship.destroy();
		}
	}

	/** Firing angle for normal tiles */
	private static final float NORMAL_ANGLE = 90.0f;
	/** Firing angle for power tiles */
	private static final float POWER_ANGLE = 45.0f;
	/** Firing color for normal tiles */
	private static final Color NORMAL_COLOR = Color.CYAN;
	/** Firing color for power tiles */
	private static final Color POWER_COLOR = Color.RED;
	/** Half of a circle (for radian conversions) */
	private static final float HALF_CIRCLE = 180.0f;
	
	/**
	 * Creates photons and udates the ship's cooldown.
	 *
	 * Firing a weapon requires access to all other models, so we have factored
	 * this behavior out of the Ship into the GameplayController.
	 */
	private void fireWeapon(Ship ship) {
		// Determine the number of photons to create.
		boolean isPower = board.isPowerTileAtScreen(ship.getX(), ship.getY());
		float angPlus = isPower ? POWER_ANGLE : NORMAL_ANGLE;
		Color c = isPower ? POWER_COLOR : NORMAL_COLOR;
		for (float fireAngle = 0.0f; fireAngle < 360.0f; fireAngle += angPlus) {
			float vx = (float) Math.cos(fireAngle * Math.PI / HALF_CIRCLE);
			float vy = (float) Math.sin(fireAngle * Math.PI / HALF_CIRCLE);

			photons.allocate(ship.getId(), ship.getX(), ship.getY(), vx, vy, c);
		}

		// Manage the sound effects.
		ship.play(SoundController.FIRE_SOUND);

		// Reset the firing cooldown.
		ship.coolDown(false);
	}
}