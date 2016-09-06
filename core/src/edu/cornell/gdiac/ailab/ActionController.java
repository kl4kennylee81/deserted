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
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Effect.Type;
import edu.cornell.gdiac.ailab.Action;
import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.AnimationNode.CharacterState;
import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.ActionNode.Direction;

import com.badlogic.gdx.graphics.*;

/**
 * As a major subcontroller, this class must have a reference to all the models.
 */
public class ActionController {

	/** Models */
	public GridBoard board;
	public List<Character> characters;
	public TextMessage textMessages;
	public AnimationPool animations;
	
	public Shields shields;

	/** Done executing actions */
	boolean isDone;
	/** Is animating a character's attack execution */
	boolean isAnimating;
	/** Current character that is executing */
	Character selected;
	/** Shielded coordinates against the selected character */
	List<Coordinate> shieldedPaths;

	ActionNode curAction;
	
	boolean curActionExecuted;

	/**
	 * Creates a GameplayController for the given models.
	 *
	 * @param board The game board
	 * @param chars The list of characters
	 * @param bar The action bar
	 */
	public ActionController(GridBoard board, List<Character> chars,
			TextMessage textMsgs, AnimationPool animations, Shields shields) {
		this.board = board;
		this.characters = chars;
		this.textMessages = textMsgs;
		this.animations = animations;
		
		this.shields = shields;

		isDone = false;
		isAnimating = false;
		selected = null;
		shieldedPaths = new LinkedList<Coordinate>();
		curAction = null;
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
			if (selected.needsAttack){
				curActionExecuted = false;
				// set the character state to start the animation
				curAction = selected.popCast();
				selected.needsAttack = false;
				if (curAction.getAction().pattern == Pattern.MOVE){
					curActionExecuted = true;
					executeAction(curAction);
					selected = null;
				} else if (!curAction.isInterrupted){
					selected.setCast();
					if (!Constants.PAUSE_ATTACK_ANIMATION){
						executeAction(curAction);
						selected = null;
					}
				} else{
					selected = null;
				}
			}
			if (selected != null && !curActionExecuted && selected.charState == CharacterState.EXECUTE){
				curActionExecuted = true;
				executeAction(curAction);
			}
			if (selected!= null && curActionExecuted && animations.pool.isEmpty() && selected.getPersistingActions().isEmpty()){
				shields.removeShields();
				selected = null;
			}
		} else {
			isDone = true;
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
		if (a_node.getAction() instanceof PersistingAction){
			if (a_node.getAction().pattern == Pattern.SHIELD){
				Coordinate[] path = getPath(a_node);
				shields.addShield(selected,a_node,path);
			} else {
				Coordinate[] path = getPath(a_node);
				if (path != null){
					selected.addPersisting(a_node,path);
				}
				else{
					selected.addPersisting(a_node);
				}
			}
			return;
		}
		//switch between types of actions
		TutorialGameplayController.highlight_action = 0;
		switch(a_node.getAction().pattern){
			case MOVE:
				executeMovement(a_node);
				break;
			case STRAIGHT:
				executeStraight(a_node);
				break;
			case HORIZONTAL:
				executeHorizontal(a_node);
				break;
			case DIAGONAL:
				executeDiagonal(a_node);
				break;
			case SINGLE:
				executeSingle(a_node);
				break;
			case SINGLEPATH:
				executeSinglePath(a_node);
				break;
			case INSTANT:
				executeInstant(a_node);
				break;
			case PROJECTILE:
				break;
			case NOP:
				break;
			default:
				break;
		}
	}

	private Coordinate[] getPath(ActionNode a_node){
		Coordinate[] path = null;
		switch(a_node.getAction().pattern){
			case MOVE:
				break;
			case STRAIGHT:
				path = straightHitPath(a_node);
				break;
			case DIAGONAL:
				path = diagonalHitPath(a_node);
				break;
			case SHIELD:
				path = shieldedPath(a_node);
				break;
			case PROJECTILE:
				path = convertRelativePath(a_node);
				break;
			case INSTANT:
				path = convertRelativePath(a_node);
				break;
			case SINGLEPATH:
				path = singlePathHitPath(a_node);
			default:
				break;
		}
		return path;
	}
	
	private Coordinate[] singlePathHitPath(ActionNode a_node){
		Coordinates coords = Coordinates.getInstance();
		// when we pass in coordinate for the path we can go out of bounds it is checked in execution time
		if (a_node.getAction()== null || a_node.getAction().path == null){
			System.out.println("line action controller 187: error input pattern projectile or instant did not have path");
			return null;
		}
		Coordinate[] relativePath = a_node.getAction().path;
		Coordinate[] absolutePath = new Coordinate[relativePath.length];
		for (int i = 0;i<relativePath.length;i++){
			// if on leftside we increment x in opposite direction
			int x;int y;
			if (selected.leftside){
				x = a_node.xPosition + relativePath[i].x;
				if (a_node.direction!= null && a_node.direction==Direction.DOWN){
					y = a_node.yPosition - relativePath[i].y;
				}
				else{
					y = a_node.yPosition + relativePath[i].y;
				}
			}
			else{
				x = a_node.xPosition - relativePath[i].x;
				if (a_node.direction!= null && a_node.direction==Direction.DOWN){
					y = a_node.yPosition - relativePath[i].y;
				}
				else{
					y = a_node.yPosition + relativePath[i].y;
				}
			}
			absolutePath[i] = coords.obtain();
			absolutePath[i].set(x, y);
		}
		return absolutePath;
	}

	/** converts path from relative coordinates to absolute coordinates on the board **/
	private Coordinate[] convertRelativePath(ActionNode a_node){
		Coordinates coords = Coordinates.getInstance();
		// when we pass in coordinate for the path we can go out of bounds it is checked in execution time
		if (a_node.getAction()== null || a_node.getAction().path == null){
			System.out.println("line action controller 187: error input pattern projectile or instant did not have path");
			return null;
		}
		Coordinate[] relativePath = a_node.getAction().path;
		Coordinate[] absolutePath = new Coordinate[relativePath.length];
		for (int i = 0;i<relativePath.length;i++){
			// if on leftside we increment x in opposite direction
			int x;int y;
			if (selected.leftside){
				x = selected.xPosition + relativePath[i].x;
				if (a_node.direction!= null && a_node.direction==Direction.DOWN){
					y = selected.yPosition - relativePath[i].y;
				}
				else{
					y = selected.yPosition + relativePath[i].y;
				}
			}
			else{
				x = selected.xPosition - relativePath[i].x;
				if (a_node.direction!= null && a_node.direction==Direction.DOWN){
					y = selected.yPosition - relativePath[i].y;
				}
				else{
					y = selected.yPosition + relativePath[i].y;
				}
			}
			absolutePath[i] = coords.obtain();
			absolutePath[i].set(x, y);
		}
		return absolutePath;
	}

	private Coordinate[] shieldedPath(ActionNode a_node){
		Coordinates coords = Coordinates.getInstance();
		int range = a_node.getAction().range;
		Direction direction = a_node.direction;
		Coordinate[] shieldedPath = new Coordinate[range];
		// if odd center shield on person
		if (range%2 == 1){
			int tempX = selected.xPosition;
			int tempY = selected.yPosition;
			shieldedPath[range/2] = coords.newCoordinate(tempX, tempY);
			for (int i =1;i<=range/2;i++){
				shieldedPath[range/2+i] = coords.newCoordinate(tempX, tempY+i);
				shieldedPath[range/2-i] = coords.newCoordinate(tempX,tempY-i);
			}
		}
		// choose by direction
		else{
			int tempX = selected.xPosition;
			int tempY = selected.yPosition;
			for (int i =0;i<range;i++){
				Coordinate c = coords.newCoordinate(tempX, tempY);
				shieldedPath[i] = c;
				tempY = (direction == Direction.DOWN) ? tempY-1:tempY+1;
			}
		}
		return shieldedPath;
	}

	private int actionWidthEndpoint(ActionNode a_node,int xPosition){
		if (selected.leftside){
			return Math.min(board.width-1, xPosition+a_node.getAction().range);
		}
		else{
			return Math.max(0, xPosition-a_node.getAction().range);
		}
	}

	private int actionWidthRange(ActionNode a_node,int xPosition){
		int endpoint = actionWidthEndpoint(a_node,xPosition);
		return Math.abs(endpoint - xPosition);
	}

	private int actionHeightEndpoint(ActionNode a_node,int yPosition){
		if (isDiagonalUp(a_node)){
			return Math.min(board.height-1,yPosition+a_node.getAction().range);
		}
		else if (isDiagonalDown(a_node)){
			return Math.max(0,yPosition - a_node.getAction().range);
		}
		return 0;
	}

	private int actionHeightRange(ActionNode a_node,int yPosition){
		int endpoint = actionHeightEndpoint(a_node,yPosition);
		return Math.abs(endpoint - yPosition);
	}

	private boolean isDiagonalUp(ActionNode a_node){
		return a_node.direction == Direction.UP;
	}

	private boolean isDiagonalDown(ActionNode a_node){
		return a_node.direction == Direction.DOWN;
	}
	
	private Coordinate[] diagonalHitPath(ActionNode a_node){
		Coordinates coordPool = Coordinates.getInstance();
		int projectileX = (selected.leftside) ? selected.xPosition+1:selected.xPosition-1;
		if (!board.isInBounds(projectileX, selected.yPosition)){
			return null;
		}
		int heightRange = actionHeightRange(a_node,selected.yPosition);
		int widthRange = actionWidthRange(a_node,projectileX);
		int trueRange = Math.min(heightRange,widthRange);
		Coordinate[] path = new Coordinate[trueRange+2];
		path[0] = coordPool.newCoordinate(selected.xPosition,selected.yPosition);
		path[1] = coordPool.newCoordinate(projectileX,selected.yPosition);
		if (selected.leftside && isDiagonalUp(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+2] = coordPool.newCoordinate(projectileX+i+1,selected.yPosition+i+1);
			}
		}
		else if (selected.leftside && isDiagonalDown(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+2] = coordPool.newCoordinate(projectileX+i+1,selected.yPosition-i-1);
			}
		}
		else if (!selected.leftside && isDiagonalUp(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+2] = coordPool.newCoordinate(projectileX-i-1,selected.yPosition+i+1);
			}
		}
		else if (!selected.leftside && isDiagonalDown(a_node)){
			for (int i=0;i<trueRange;i++){
				path[i+2] = coordPool.newCoordinate(projectileX-i-1,selected.yPosition-i-1);
			}
		}
		return path;
	}

	private Coordinate[] straightHitPath(ActionNode a_node){
		Coordinates coordPool = Coordinates.getInstance();
		int j = selected.yPosition;
		int numTiles;
		if (selected.leftside){
			numTiles = Math.min(board.width-1,selected.xPosition + a_node.getAction().range) - selected.xPosition;
		}
		else{
			numTiles = Math.max(0, Math.min(selected.xPosition,a_node.getAction().range));
		}

		Coordinate[] path = new Coordinate[numTiles];
		for (int i=0;i<numTiles;i++){
			if (selected.leftside){
				path[i] = coordPool.newCoordinate(selected.xPosition+i+1,j);
			}
			else{
				path[i] = coordPool.newCoordinate(selected.xPosition-i-1,j);
			}
		}
		return path;
	}

	private Coordinate[] horizontalHitPath(ActionNode a_node){
		Coordinates coordPool = Coordinates.getInstance();
		int numTiles = board.height;
		Coordinate[] path = new Coordinate[numTiles];

		int targetX = board.width - 1 - selected.xPosition;
		for (int i = 0; i < numTiles; i++){
			path[i] = coordPool.newCoordinate(targetX, i);
		}
		return path;
	}

	protected void updateShieldedPath(){
		shieldedPaths.clear();
		if (selected.leftside){
			shieldedPaths.addAll(shields.rightShieldedCoordinates);
		} else {
			shieldedPaths.addAll(shields.leftShieldedCoordinates);
		}
		shields.resetShieldsHitThisRound();
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
		int nextX = selected.xPosition;
		int nextY = selected.yPosition;
		switch (a_node.direction){
		case UP:
			nextY++;
			break;
		case DOWN:
			nextY--;
			break;
		case LEFT:
			nextX--;
			break;
		case RIGHT:
			nextX++;
			break;
		default:
			break;
		}
		// can't move onto tile that is broken/occupied or not on your side
		if (board.canMove(selected.leftside,nextX,nextY)){
			selected.xPosition = nextX;
			selected.yPosition = nextY;
		}
	}
	
	private void executeSinglePath(ActionNode a_node){
		Coordinate[] path = singlePathHitPath(a_node);
		processHitPath(a_node,path,a_node.getAction().oneHit,a_node.getAction().canBlock);
	}

	private void executeStraight(ActionNode a_node){
		Coordinate[] path = straightHitPath(a_node);
		// execute the hit interrupt and do damage to closest enemy
		processHitPath(a_node,path,a_node.getAction().oneHit,a_node.getAction().canBlock);
	}

	private void executeHorizontal(ActionNode a_node){
		Coordinate[] path = horizontalHitPath(a_node);
		// execute the hit interrupt and do damage to every enemy in path
		processHitPath(a_node,path,a_node.getAction().oneHit,a_node.getAction().canBlock);
	}

	private void executeDiagonal(ActionNode a_node){
		Coordinate[] path = diagonalHitPath(a_node);
		// check along path and apply damage to first person hit
		processHitPath(a_node,path,a_node.getAction().oneHit,a_node.getAction().canBlock);
	}

	private void executeInstant(ActionNode a_node){
		Coordinate[] path = convertRelativePath(a_node);
		processHitPath(a_node,path,a_node.getAction().oneHit,a_node.getAction().canBlock);
	}

	private void processHitPath(ActionNode a_node, Coordinate[] path, boolean oneHit, boolean canBlock){
		boolean hasHit = false;
		for (int i=0;i<path.length;i++){
			// if you have already hit one character and you only can hit one break out of checking path
			if (oneHit && hasHit){
				break;
			}
			
			// if it had hit anything in a prior coordinate
			boolean hitThisRound = false;
			
			if (board.isInBounds(path[i].x,path[i].y) && !(path[i].x == selected.xPosition && path[i].y == selected.yPosition && !a_node.getAction().isBuff)){
				animations.add(a_node.getAction().animation,path[i].x,path[i].y);
			}
			
			if (isBlocked(path[i].x, path[i].y)){
				if (a_node.getAction().getDamage(null) > 0){
					shields.hitShield(path[i].x, path[i].y, selected.leftside,textMessages);
				}
				hitThisRound = true;
			} else {
				for (Character c:characters){
					// if same side stop checking
					if (selected.leftside ==c.leftside && a_node.getAction().pattern != Pattern.SINGLE){
						continue;
					}
					if (c.xPosition == path[i].x && c.yPosition == path[i].y){
						processHit(a_node,c);
						hitThisRound = true;
						break;
					}
				}
			}
			// set hasHit to true if it has hit someone this round
			if (hitThisRound && !hasHit){
				hasHit = true;
			}
			// if it has hit someone at that coordinate we don't break that tile
			if (!hitThisRound && !board.isOnSide(selected.leftside, path[i].x, path[i].y)){
				applyTileEffect(a_node.getAction().effect,path[i].x,path[i].y);
			}
		}
		// free Coordinates back into the Pool
		for (int j = 0;j<path.length;j++){
			path[j].free();
		}
	}

	private void applyTileEffect(Effect e,int x,int y){
		if (e==null){
			return;
		}
		else{
			switch (e.type){
			case BROKEN:
				String keyCoordinate = String.format("%s:%s",Integer.toString(x),Integer.toString(y));
				board.addTileEffect(keyCoordinate, e.clone());
				break;
			default:
				break;
			}
		}
	}

	private void executeSingle(ActionNode a_node){
		Coordinate c = Coordinates.getInstance().obtain();
		c.set(a_node.xPosition, a_node.yPosition);
		Coordinate[] path = new Coordinate[1];
		path[0] = c;
		processHitPath(a_node,path,true,false);
	}

	protected void processHit(ActionNode a_node,Character target){
		applyDamage(a_node,target);
		applyEffect(a_node,target);

		//add text bubble for amount of damage in front of target
		// only add the text damage if any damage has been done
		if (a_node.getAction().getDamage(target) > 0){
			String attack_damage = Integer.toString(a_node.getAction().getDamage(target));
			textMessages.addDamageMessage(attack_damage, target.xPosition, target.yPosition, 2*TextMessage.SECOND, Color.WHITE);
			target.setHurt();
			ActionNode nextAttack = target.queuedActions.peek();

			//handle interruption
			// if an attack does 0 damage it doesn't interrupt for example slows
			if (nextAttack != null && !nextAttack.isInterrupted){
				nextAttack.isInterrupted = true;
				if (nextAttack.getAction().pattern != Pattern.MOVE){
					textMessages.addOtherMessage("INTERRUPTED", target.xPosition, target.yPosition, 2*TextMessage.SECOND, Color.RED);
				}
			}
		}
	}

	protected void applyDamage(ActionNode a_node,Character target){
		target.setHealth(Math.max(target.getHealth()-a_node.getAction().getDamage(target), 0));
	}

	protected void applyEffect(ActionNode a_node, Character target){
		Effect eff = a_node.getAction().effect;
		if(eff.type == Type.REGULAR){
			return;
		}
		textMessages.addOtherMessage(eff.toString(),target.xPosition,target.yPosition,2*TextMessage.SECOND, Color.RED);
		target.addEffect(a_node.getAction().effect.clone());
	}

	// make isDone true when every character who needs to attack has attacked
	public boolean isDone() {
		return isDone;
	}
}