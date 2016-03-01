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
	
	int i;
	 
	/** 
	 * Invokes the controller for this ship.
	 *
     * Movement actions are determined, but not committed (e.g. the velocity
	 * is updated, but not the position). New weapon firing action is processed
	 * but photon collisions are not.
	 */
	public void update() {
		//TODO EVERYTHING
		if (selected != null){
			//System.out.println("why wtf\n");
			//Do that dudes actions
			ActionNode action = selected.popCast();
			selected.needsAttack = false;
			executeAction(action);
			selected = null;
		} else {
			isDone = true;
			//Sort characters by speed then check their attacks
			//these characters should be presorted in the initial loading
			for (Character c : characters){
				if (c.needsAttack){
					//System.out.println("setting to selected");
					isDone = false;
					selected = c;
					i = 0;
					break;
				}
			}
		}
	}	
	
	private void executeAction(ActionNode a_node){
		//switch between types of actions
		switch(a_node.action.pattern){
			case MOVE:
				executeMovement(a_node);
				break;
			case SHIELD:
				executeShield(a_node);
				break;
			case STRAIGHT:
				executeStraight(a_node);
				break;
			case DIAGONAL:
				executeDiagonal(a_node);
				break;
			case SINGLE:
				executeSingle(a_node);
				break;
		}
	}
	
	private void executeMovement(ActionNode a_node){
		selected.xPosition = a_node.xPosition;
		selected.yPosition = a_node.yPosition;
	}
	
	private void executeShield(ActionNode a_node){
		
	}
	
	private void executeStraight(ActionNode a_node){
		int selected_xPos = selected.xPosition;
		//
	}
	
	private void executeDiagonal(ActionNode a_node){
		
	}
	
	private void executeSingle(ActionNode a_node){
		
	}
	
	private void processHit(Character c, ActionNode action){
		
	}
	
	// make isDone true when every character who needs to attack has attacked
	public boolean isDone() {
		return isDone;
	}
}