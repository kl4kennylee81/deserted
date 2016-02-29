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

import java.util.List;
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
	public GridBoard board; 
	
	public List<Character> characters;
	
	public ActionBar bar;
	
	
	/** Reference to all the ships in the game */	
	//public ShipList ships; 
	/** Reference to the active photons */
	//public PhotonPool photons; 
	
	/** List of all the input (both player and AI) controllers */
	protected InputController[] controls;

	/** Random number generator for state initialization */
	private Random random;

	/**
	 * Creates a GameplayController for the given models.
	 *
	 * @param board The game board 
	 * @param chars The list of characters
	 * @param bar The action bar
	 */
	public GameplayController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		
		controls = new InputController[4];
		controls[0] = new PlayerController();
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
		/*for (Ship s : ships) {
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
		}*/
	}	
}