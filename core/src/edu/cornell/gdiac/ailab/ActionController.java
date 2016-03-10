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

import java.util.LinkedList;
import java.util.List;
//import java.util.Random;
//import com.badlogic.gdx.math.*;

import edu.cornell.gdiac.ailab.Character;
import edu.cornell.gdiac.ailab.Action.Pattern;

import com.badlogic.gdx.graphics.*;

/**
 * As a major subcontroller, this class must have a reference to all the models.
 */
public class ActionController {

	/** Models */
	public GridBoard board; 
	public List<Character> characters;
	public ActionBar bar;
	public TextMessage textMessages;
	
	/** Done executing actions */
	boolean isDone;
	/** Current character that is executing */
	Character selected;
	/** Shielded coordinates against the selected character */
	List<Coordinate> shieldedPaths;

	/**
	 * Creates a GameplayController for the given models.
	 *
	 * @param board The game board 
	 * @param chars The list of characters
	 * @param bar The action bar
	 */
	public ActionController(GridBoard board, List<Character> chars, ActionBar bar, TextMessage textMsgs) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		this.textMessages = textMsgs;
		
		isDone = false;
		selected = null;
		shieldedPaths = new LinkedList<Coordinate>();
	}
	 
	/** 
	 * Invokes the controller for this ship.
	 *
     * Movement actions are determined, but not committed (e.g. the velocity
	 * is updated, but not the position). New weapon firing action is processed
	 * but photon collisions are not.
	 */
	public void update() {
		board.occupy(characters);
		if (selected != null){
			// Execute character's action;
			ActionNode action = selected.popCast();
			selected.needsAttack = false;
			if (!action.isInterrupted || action.action.pattern == Pattern.MOVE){
				executeAction(action);
			}
			selected = null;
		} else {
			isDone = true;
			//Sort characters by speed then check their attacks
			//these characters should be presorted in the initial loading
			//But things like slows should affect it right? -jon
			for (Character c : characters){
				if (c.needsAttack && c.isAlive()){
					isDone = false;
					selected = c;
					updateShieldedPath();
					break;
				}
			}
		}
	}	
	
	private void executeAction(ActionNode a_node){
		// If persisting, then add to character 
		if (a_node.action instanceof PersistingAction){
			selected.addPersisting(a_node);
			return;
		}
		//switch between types of actions
		switch(a_node.action.pattern){
			case MOVE:
				executeMovement(a_node);
				break;
			case STRAIGHT:
				System.out.println("straignt");
				executeStraight(a_node);
				break;
			case DIAGONAL:
				executeDiagonal(a_node);
				break;
			case SINGLE:
				executeSingle(a_node);
				break;
			case NOP:
				break;
			default:
				break;
		}
	}
	
	private int actionWidthEndpoint(ActionNode a_node,int xPosition){
		if (selected.leftside){
			return Math.min(board.width-1, xPosition+a_node.action.range);			
		}
		else{
			return Math.max(0, xPosition-a_node.action.range);
		}
	}
	
	private int actionWidthRange(ActionNode a_node,int xPosition){
		int endpoint = actionWidthEndpoint(a_node,xPosition);
		return Math.abs(endpoint - xPosition);
	}
	
	private int actionHeightEndpoint(ActionNode a_node,int yPosition){
		if (isDiagonalUp(a_node)){
			return Math.min(board.height-1,yPosition+a_node.action.range);			
		}
		else if (isDiagonalDown(a_node)){
			return Math.max(0,yPosition - a_node.action.range);
		}
		return 0;
	}
	
	private int actionHeightRange(ActionNode a_node,int yPosition){
		int endpoint = actionHeightEndpoint(a_node,yPosition);
		return Math.abs(endpoint - yPosition);
	}
	
	private boolean isDiagonalUp(ActionNode a_node){
		return a_node.yPosition == SelectionMenuController.DIAGONAL_UP;
	}
	
	private boolean isDiagonalDown(ActionNode a_node){
		return a_node.yPosition == SelectionMenuController.DIAGONAL_DOWN;		
	}
	
	private Coordinate[] diagonalHitPath(ActionNode a_node){
		int projectileX = (selected.leftside) ? selected.xPosition+1:selected.xPosition-1;
		if (!board.isInBounds(projectileX, selected.yPosition)){
			return null;
		}
		int heightRange = actionHeightRange(a_node,selected.yPosition);
		int widthRange = actionWidthRange(a_node,projectileX);
		int trueRange = Math.min(heightRange,widthRange);
		Coordinate[] path = new Coordinate[trueRange+1];
		path[0] = new Coordinate(projectileX,selected.yPosition);
		if (selected.leftside && isDiagonalUp(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+1] = new Coordinate(projectileX+i+1,selected.yPosition+i+1);
			}
		}
		else if (selected.leftside && isDiagonalDown(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+1] = new Coordinate(projectileX+i+1,selected.yPosition-i-1);
			}			
		}
		else if (!selected.leftside && isDiagonalUp(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+1] = new Coordinate(projectileX-i-1,selected.yPosition+i+1);
			}				
		}
		else if (!selected.leftside && isDiagonalDown(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+1] = new Coordinate(projectileX-i-1,selected.yPosition-i-1);
			}
		}
		return path;
	}
	
	private Coordinate[] straightHitPath(ActionNode a_node){
		int j = selected.yPosition;
		int numTiles;
		if (selected.leftside){
			numTiles = Math.min(board.width-1,selected.xPosition + a_node.action.range) - selected.xPosition;
		}
		else{
			numTiles = Math.max(0, Math.min(selected.xPosition,a_node.action.range));
		}
		
		Coordinate[] path = new Coordinate[numTiles];
		for (int i=0;i<numTiles;i++){
			if (selected.leftside){
				path[i] = new Coordinate(selected.xPosition+i+1,j);
			}
			else{
				path[i] = new Coordinate(selected.xPosition-i-1,j);
			}
		}
		return path;
	}
	
	protected void updateShieldedPath(){
		shieldedPaths.clear();
		for (Character c : characters){
			if (c.leftside != selected.leftside && c.hasShield()) {
				shieldedPaths.addAll(c.getShieldedCoords());
			}
		}
	}
	
	protected boolean isBlocked(int coordX, int coordY){
		for (Coordinate c : shieldedPaths){
			if (c.x==coordX && c.y==coordY){
				return true;
			}
		}
		return false;
	}
	
	private void executeMovement(ActionNode a_node){
		selected.popLastShadow();
		int total_moves = (Math.abs(selected.xPosition-a_node.xPosition)
				+Math.abs(selected.yPosition-a_node.yPosition));
		if (total_moves==1 && !board.isOccupied(a_node.xPosition, a_node.yPosition)){
			selected.xPosition = a_node.xPosition;
			selected.yPosition = a_node.yPosition;			
		}
	}
	
	private void executeStraight(ActionNode a_node){
		System.out.println("execute straight");
		Coordinate[] path = straightHitPath(a_node);
		// execute the hit interrupt and do damage to closest enemy
		processHitPath(a_node,path);
	}
	
	private void executeDiagonal(ActionNode a_node){
		Coordinate[] path = diagonalHitPath(a_node);
		// check along path and apply damage to first person hit
		processHitPath(a_node,path);
	}
	
	private void processHitPath(ActionNode a_node, Coordinate[] path){
		boolean hasHit = false;
		for (int i=0;i<path.length;i++){
			if (isBlocked(path[i].x, path[i].y)){
				break;
			}
			for (Character c:characters){
				if (selected.leftside ==c.leftside){
					continue;
				}
				if (c.xPosition == path[i].x && c.yPosition == path[i].y){
					processHit(a_node,c);
					hasHit = true;
					break;
				}
				if (hasHit){
					break;
				}
			}
		}
	}
	
	private void executeSingle(ActionNode a_node){
		textMessages.addSingleTemp(a_node.xPosition,a_node.yPosition);
		for (Character c:characters){
			if (c.xPosition == a_node.xPosition && c.yPosition == a_node.yPosition){
				processHit(a_node,c);
				break;
			}
		}
	}
	
	protected void processHit(ActionNode a_node,Character target){
		applyDamage(a_node,target);
		
		//add text bubble for amount of damage in front of target
		String attack_damage = Integer.toString(a_node.action.damage);
		textMessages.addDamageMessage(attack_damage, target.xPosition, target.yPosition, 2*TextMessage.SECOND, Color.WHITE);

		ActionNode nextAttack = target.queuedActions.peek();
		//handle interruption
		if (nextAttack != null && !nextAttack.isInterrupted){
			nextAttack.isInterrupted = true;
			if (nextAttack.action.pattern != Pattern.MOVE){
				textMessages.addOtherMessage("INTERRUPTED", target.xPosition, target.yPosition, 2*TextMessage.SECOND, Color.RED);
			}
		}
	}
	
	protected void applyDamage(ActionNode a_node,Character target){
		target.health = Math.max(target.health-a_node.action.damage, 0);
	}
	
	// make isDone true when every character who needs to attack has attacked
	public boolean isDone() {
		return isDone;
	}
}