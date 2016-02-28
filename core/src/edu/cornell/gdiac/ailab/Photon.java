/*
 * Photon.java
 * 
 * This is a lightweight class that just has getters and setters. Because memory 
 * allocation is handled by PhotonPool, we would prefer that anymore complicated go 
 * through that class instead, particularly methods that require the Photon to be deleted.
 *
 * This is a "passive" model.  It does not access the methods or fields of any other 
 * Model class.  It also does not store any other model object as a field. This allows us
 * to keep the models from being tightly coupled.
 *
 * This decision makes a slight complication when it comes to photon ownership.  We do 
 * not want a ship to be hit by its own photons, so each photon needs an owner.  But we 
 * do not want to store a ship object in an photon.  We solve this problem by storing 
 * the ship id in the photon instead.  Id numbers are a classic technique for decoupling 
 * classes from one another.
 *
 * Ideally, this class should have been an inner class instead of PhotonPool.  That would
 * have allowed us to hide certain methods (like deletion) while still giving PhotonPool
 * access to them.  However, there are several points where we need to loop over all
 * photons, and this is just not possible.  This is a classic example of an engineering
 * trade-off.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;

/**
 * Class to represent a photon object.
 * 
 * Photon objects are lightweight, and just have getters and setters.  All of
 * the logic (and memory management) is provided by PhotonPool.
 */
public class Photon {
	/** Factor to multiply times velocity for collisions */
	private static final float SHOT_SPEED = 10.0f; 
	/** Number of frames before deactivation */
	private static final int   MAX_AGE = 32;
	
	/** Color to tint this photon */
	private Color tint; 
	/** Photon position */
	private Vector2 position;
	/** Photon velocity */
	private Vector2 velocity;
	/** Number of animation frames left to live (-1 for dead) */
	private int life;
	/** Marks whether this photon is dead, but not deallocated */
	private boolean dirty;
	/** ID of ship that created Photon (LOOSE COUPLING) */
	private int ship;
	
	/**
	 * Creates an empty Photon.  
	 * 
	 * This constructor is used in memory allocation.
	 */
	public Photon() {
		// Allocate the subobjects
		tint = new Color();
		position = new Vector2();
		velocity = new Vector2();
		
		// Everything else is undefined/default
		life = -1;
		ship = -1;
		dirty = false;

	}
	
	/**
	 * "Allocates" a new photon with the given attributes.
	 *
	 * The color object is copied.  This method does not store a reference to the 
	 * original color object.
	 *
	 * NEVER call this method directly.  This method should only be accessed by
	 * PhotonPool.  Call the allocate() method in PhotonPool instead.	 
	 *
	 * @param ship The ship that fired the photon
	 * @param x  The initial x-coordinate of the photon 
	 * @param y  The initial y-coordinate of the photon 
	 * @param vx The x-value of the photon velocity
	 * @param vy The y-value of the photon velocity
	 * @param color The photon tint color
	 */
	public void set(int ship, float x, float y, float vx, float vy, Color color) {
		this.ship = ship;
		
		this.position.set(x,y);
		this.velocity.set(vx,vy).scl(SHOT_SPEED);
		this.tint.set(color);

		dirty = false;
		life = MAX_AGE;
	}

	/**
	 * Returns the x-coordinate of the photon position
	 *
	 * @return the x-coordinate of the photon position
	 */
	public float getX() {
		return position.x;
	}

	/**
	 * Sets the x-coordinate of the photon position
	 *
	 * @param value the x-coordinate of the photon position
	 */
	public void setX(float value) {
		position.x = value;
	}

	/**
	 * Returns the y-coordinate of the photon position
	 *
	 * @return the y-coordinate of the photon position
	 */
	public float getY() {
		return position.y;
	}

	/**
	 * Sets the y-coordinate of the photon position
	 *
	 * @param value the y-coordinate of the photon position
	 */
	public void setY(float value) {
		position.y = value;
	}
	
	/**
	 * Returns the position of this photon.
	 *
	 * This method returns a reference to the underlying photon position vector.
	 * Changes to this object will change the position of the photon.
	 *
	 * @return the position of this photon.
	 */
	public Vector2 getPosition() {
		return position;
	}

	/**
	 * Returns the x-coordinate of the photon velocity
	 *
	 * @return the x-coordinate of the photon velocity
	 */
	public float getVX() {
		return velocity.x;
	}

	/**
	 * Sets the x-coordinate of the photon velocity
	 *
	 * @param value the x-coordinate of the photon velocity
	 */
	public void setVX(float value) {
		velocity.x = value;
	}

	/**
	 * Returns the y-coordinate of the photon velocity
	 *
	 * @return the y-coordinate of the photon velocity
	 */
	public float getVY() {
		return velocity.y;
	}

	/**
	 * Sets the y-coordinate of the photon velocity
	 *
	 * @param value the y-coordinate of the photon velocity
	 */
	public void setVY(float value) {
		velocity.y = value;
	}

	/**
	 * Returns the velocity of this photon.
	 *
	 * This method returns a reference to the underlying photon velocity vector.
	 * Changes to this object will change the velocity of the photon.
	 *
	 * @return the velocity of this photon.
	 */	
	public Vector2 getVelocity() {
		return velocity;
	}

	/**
	 * Returns the color of this photon.
	 *
	 * This method returns a reference to the underlying photon color.
	 * Changes to this object will change the color of the photon.
	 *
	 * @return the color of this photon.
	 */		
	public Color getColor() {
		return tint;
	}
	
	/** 
	 * Returns the unique identifier of ship that created photon.
	 *
	 * @return the identifier of ship that created photon.
	 */
	public int getSource() {
		return ship;
	}

	/**
	 * Returns whether or not this photon is still alive.
	 *
	 * Dead photons are not drawn, and are not processed in collision detection.
	 *
	 * @return whether or not this photon is still alive.
	 */
	public boolean isAlive() {
		return life > 0;
	}
	
	/**
	 * Returns the amount of life remaining in this photon, as a percentage.
	 * 
	 * 1 = newly created; 0 = dead.
	 *
	 * @return the amount of life remaining in this photon, as a percentage.
	 */
	public float getLifeRatio() {
		return (float)life / (float)MAX_AGE;
	}

	/**
	 * Destroy this photon immediately, removing it from the screen.
	 *
	 * This method will mark the photon as dirty, so that it can be processed
	 * properly later (e.g. its deletion violates the FIFO property of the PhotonQueue
	 * in the previous lab).
	 *
	 * NEVER call this method directly.  This method should only be accessed by
	 * PhotonPool.  Call the destroy() method in PhotonPool instead.
	 */
	public void destroy() {
		life = 0;
		dirty = true;
	}
	
	/**
	 * Age this photon normally by one frame.
	 *
	 * When the photon reaches life 0, it is dead.
	 */
	public void age() {
		life--;
	}
	
	/**
	 * This photon is dirty and was destroyed prematurely.
	 *
	 * Dirty photons are those that did not age normally and were deleted "out of order".
	 * Because we are still using a queue structure like the last lab, we have to handle
	 * them specially.
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Returns the amount to push a ship (in x-direction) upon collision.
	 *
	 * @return the amount to push a ship (in x-direction) upon collision.
	 */
	public float getPushX() {
		return (velocity.x < -SHOT_SPEED * 0.5f) ? -1.0f
				: (velocity.x > SHOT_SPEED * 0.5f) ? 1.0f : 0.0f;
	}

	/**
	 * Returns the amount to push a ship (in y-direction) upon collision.
	 *
	 * @return the amount to push a ship (in y-direction) upon collision.
	 */
	public float getPushY() {
		return (velocity.y < -SHOT_SPEED * 0.5f) ? -1.0f
				: (velocity.y > SHOT_SPEED * 0.5f) ? 1.0f : 0.0f;
	}
}