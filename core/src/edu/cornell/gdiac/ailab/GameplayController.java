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
 * As a major subcontroller, this class must have a reference to all the models.
 */
public class GameplayController {

	/** Reference to the game board */
	public GridBoard board; 
	
	public List<Character> characters;
	
	public ActionBar bar;
	
	boolean isDone;
	
	Character selected;

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
		this.isDone = false;
		
		selected = null;
	}
	
	/** 
	 * Invokes the controller for this ship.
	 *
     * Movement actions are determined, but not committed (e.g. the velocity
	 * is updated, but not the position). New weapon firing action is processed
	 * but photon collisions are not.
	 */
	public void update() {
		//TODO EVERYTHING
		/*
		if (selected != null){
			//Do that dudes actions
		} else {
			isDone = true;
			//Sort characters by speed then check their attacks
			for (Character c : characters){
				if (c.needsAttack){
					isDone = false;
					selected = c;
					break;
				}
			}
		}*/
		isDone = true;
		
	}	
	
	// make isDone true when every character who needs to attack has attacked
	public boolean isDone() {
		return isDone;
	}
}