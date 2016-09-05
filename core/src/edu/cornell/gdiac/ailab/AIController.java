package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;

import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Effect.Type;
import edu.cornell.gdiac.ailab.GameActionNode;


public class AIController {
	public static enum Difficulty {
		EASY,
		MEDIUM,
		HARD,
		BOSS_1,
		BOSS_2,
		BOSS_3
	}
	
	GridBoard board;
	List<Character> chars;
	Character selected;
	Action nop;
	Shields shields;
	
	private int xOffset;
	private int yOffset;
	private float interval;
	private int curSlot;
	private boolean shield;
	private boolean hasSingle;
	private TacticalManager tacticalManager;
	
	public AIController(GridBoard board, List<Character> chars, TacticalManager tm, Shields shields) {
		this.board = board;
		this.chars = chars;
		this.tacticalManager = tm;
		this.shields = shields;
		tacticalManager.setState(board, chars, shields);
		nop = new Action("NOP", 1, 0, 0, 1, Pattern.NOP, false, false,false, new Effect(0, Type.REGULAR, 0, "Nope",null), "no action",null);

	}
	
	private Character selecting;

	
	public void update(){
		for (Character c : chars){
			if (c.needsSelection && c.isAI){
				//Update tactical manager
				tacticalManager.updateConditions(c);
				tacticalManager.selectActions(c);
				c.needsSelection = false;
//				System.out.print(c.name+ ": ");
//				for(ActionNode n: c.queuedActions){
//					System.out.print("("+n.action.name+") ");
//				}
//				System.out.println();
//				xOffset = 0;
//				yOffset = 0;
//				shield = false;
//				hasSingle = hasSingle();
//				interval = (1f-bar.castPoint) / ActionBar.getTotalSlots();
//				curSlot = 1;
//				switch (c.diff){
//					case EASY:
//						selected.setQueuedActions(getActions(0.8f, 0.33f, 0.2f, 0.1f));
//						break;
//					case MEDIUM:
//						selected.setQueuedActions(getActions(0.8f, 0.33f, 0.2f, 0.7f));
//						break;
//					default:
//						selected.setQueuedActions(getActions(0.8f, 0.33f, 0.2f, 1.0f));
//						break;
//				}
			}
		}
	}
	
	public void outputData(JsonArray jsonArray){
		for (Character c : chars){
			if(c.needsDataOutput){
				c.needsDataOutput = false;
				tacticalManager.updateConditions(c);
				tacticalManager.outputData(c, jsonArray);
			}
		}
	}
	
	/**
	 * CURRENT BASIC AI ALGORIGHM:
	 * - Iterate through action slots
	 * - At each slot, pick either movement or attack based on aggression factor
	 * 		- If there is only one slot left, always pick movement
	 * - If attack, choose between projectile and single square based on risk factor
	 * 		- If projectile, pick a random attack among all straight or diagonal attacks that 
	 * 		  could hit an enemy
	 * 		- If single square, pick a random enemy to target
	 * - If movement, try to move to a safe square if the character does not have enough slots 
	 * 	 left to attack afterwards. If there are enough slots left to attack after the movement, 
	 * 	 move to a square from which an enemy can be hit. 
	 * 
	 * random ideas for improvement:
	 *  - Move more than one square to get into an attacking position
	 * 	- Take into account opponents position in cast bar 
	 * 		- be more aggressive if they are in their waiting phase, more
	 * 		  defensive if they are close to/ in their cast phase
	 *  - Dont always target directly at an opponent (in case they move)
	 *  - Consider all opponents positions at once (pick the attack that is most likely to hit)
	 *  - Work with teammate somehow
	 *  - Dodge incoming projectiles
	 */
	private List<ActionNode> getActions(float aggression, float risk, float defensiveness, float intelligence){
		LinkedList<ActionNode> actions = new LinkedList<ActionNode>();
		while(curSlot <= 4){
			ActionNode a = getAction(aggression, risk, defensiveness, intelligence);
			// kyle this is a hotfix for the showcase im not sure what you want
			// check if it fails to return i just have it as a NOP;
			if (a == null){
				curSlot+=1;
				continue;
			}
			else{
				curSlot += a.getAction().cost;
			}
			if(a.getAction().pattern == Pattern.MOVE){
				xOffset = a.xPosition - selected.xPosition;
				yOffset = a.yPosition - selected.yPosition;
			}
			actions.add(a);
		}
		return actions;
	}
	/**
	 * Returns true if the selected character has a single square attack
	 */
	private boolean hasSingle(){
		for(Action a: selected.availableActions){
			if(a.pattern == Pattern.SINGLE){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns true if the c can hit the square (enemyX, enemyY) with a projectile
	 *  attack launched from (cX, cY).
	 */
	private boolean canHit(Character c, int cX, int cY, int enemyX, int enemyY){
		boolean hit = false;
		for(Action a: c.availableActions){
			switch(a.pattern){
				case STRAIGHT:
					hit = cY == enemyY;
				case DIAGONAL:
					hit = canHitDiagonal(cX, cY, enemyX, enemyY);
				default:
					break;
			}
		}
		return hit;
	}
	
	
	/**
	 * returns true if the square (enemyX, enemyY) can be hit from a diagonal
	 * attack launched from (cX, cY).
	 */
	private boolean canHitDiagonal(int cX, int cY, int enemyX, int enemyY){
		if(enemyY == cY){
			return Math.abs(enemyX - cX) == 1 ? true : false;
		}
		int xDir = (enemyX < cX) ? -1 : 1;
		int yDir = (enemyY < cY) ? -1 : 1;
		int x = cX + xDir;
		int y = cY;
		while(board.isInBounds(x,y)){
			if(x == enemyX && y == enemyY){
				return true;
			}
			x += xDir;
			y += yDir;
		}
		return false;
	}
	
	/**
	 * Returns true if the selected character can hit any enemy with a projectile
	 * from square (x,y).
	 */
	public boolean canHitSomeone(int x, int y){
		for(Character c: chars){
			if(!c.isAI && canHit(selected, x, y, c.xPosition, c.yPosition) && c.isAlive()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the currently selected character would be able to 
	 * get hit by a projectile at (x,y), given the current state of the board.
	 */
	public boolean isSafe(int x, int y){
		if(!board.isInBounds(x, y) || !ownSide(x)){
			return false;
		}
		for(Character c: chars){
			if(!c.isAI && canHit(c, c.xPosition, c.yPosition, x, y) && c.isAlive()){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if a tile (x, _ ) would be on the selected character's own side
	 * of the board.
	 */
	public boolean ownSide(int x){
		if(selected.xPosition <= board.width/2){
			return (x <= board.width/2) ? true : false;
		}
		else{
			return (x > board.width/2) ? true : false;
		}
	}
	
	/**
	 * Returns a list of movement actions that the selected character could perform such
	 * that he would not be able to be attacked by a projectile, given the current
	 * state of the board.
	 */
	public ArrayList<ActionNode> findSafeSquares(Action movement){
		ArrayList<ActionNode> nodes = new ArrayList<ActionNode>();
		int curX = selected.xPosition + xOffset;
		int curY = selected.yPosition + yOffset;
		if(isSafe(curX + 1, curY)){
			nodes.add(new GameActionNode(movement, curSlot, curX + 1, curY, Direction.RIGHT));
		}
		if(isSafe(curX, curY + 1)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY + 1, Direction.UP));
		}
		if(isSafe(curX - 1, curY)){
			nodes.add(new GameActionNode(movement, curSlot, curX - 1, curY, Direction.LEFT));
		}
		if(isSafe(curX, curY - 1)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY - 1, Direction.DOWN));
		}
		if(isSafe(curX, curY)){			
			nodes.add(new GameActionNode(nop, curSlot, 0, 0, Direction.NONE));		
		}
		return nodes;
	}
	
	/**
	 * Returns a list of movement actions that the selected character could perform
	 * such that he would be able to hit the opponent with a projectile from his new
	 * square.
	 */
	public ArrayList<ActionNode> findAttackSquares(Action movement){
		ArrayList<ActionNode> nodes = new ArrayList<ActionNode>();
		int curX = selected.xPosition + xOffset;
		int curY = selected.yPosition + yOffset;
		if(canHitSomeone(curX + 1, curY) && ownSide(curX + 1)){
			nodes.add(new GameActionNode(movement, curSlot, curX + 1, curY, Direction.RIGHT));
		}
		if(canHitSomeone(curX, curY + 1) && ownSide(curX)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY + 1, Direction.UP));
		}
		if(canHitSomeone(curX, curY - 1) && ownSide(curX)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY - 1, Direction.DOWN));
		}
		if(canHitSomeone(curX - 1, curY) && ownSide(curX - 1)){
			nodes.add(new GameActionNode(movement, curSlot, curX - 1, curY, Direction.LEFT));
		}
		if(canHitSomeone(curX, curY)){	
			nodes.add(new GameActionNode(nop, curSlot, 0, 0, Direction.NONE));
		}
		return nodes;
	}
	
	/**
	 * Returns a single random movement action
	 */
	public ActionNode randomMovement(Action movement){
		ArrayList<ActionNode> nodes = new ArrayList<ActionNode>();
		int curX = selected.xPosition + xOffset;
		int curY = selected.yPosition + yOffset;
		if(board.isInBounds(curX + 1, curY) && ownSide(curX + 1) ){
			nodes.add(new GameActionNode(movement, curSlot, curX + 1, curY, Direction.RIGHT));
		}
		if(board.isInBounds(curX, curY + 1) && ownSide(curX)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY + 1, Direction.UP));
		}
		if(board.isInBounds(curX - 1, curY) && ownSide(curX - 1)){
			nodes.add(new GameActionNode(movement, curSlot, curX - 1, curY, Direction.LEFT));
		}
		if(board.isInBounds(curX, curY - 1) && ownSide(curX)){
			nodes.add(new GameActionNode(movement, curSlot, curX, curY - 1, Direction.DOWN));
		}
		Random r = new Random();
		return nodes.get(r.nextInt(nodes.size()));	
	}
	
	/**
	 * If the selected character has enough time to move and then attack,
	 * this function returns a movement to a square on which the character
	 * could hit an enemy with a projectile. If there is not enough time to
	 * move and then attack, then this function returns a movement to a square 
	 * that is save, given the current state of the board.
	 */
	public ActionNode getMovement(){
		Action movement = nop;
		for(Action a: selected.availableActions){
			if(a.pattern == Pattern.MOVE){
				movement = a;
			}
		}
		ArrayList<ActionNode> moves = new ArrayList<ActionNode>();
		if(curSlot >= 3){
			moves = findSafeSquares(movement);
		}
		else {
			moves = findAttackSquares(movement);
		}
		if(moves.size() == 0){
			moves.add(randomMovement(movement));
		}
		Random r = new Random();
		return moves.get(r.nextInt(moves.size()));
	}
	
	/**
	 * Adds a straight ActionNode to attacks for every enemy that can be hit by 
	 * a straight attack from the AI's current position.
	 */
	public void addStraight(Action a, ArrayList<ActionNode> attacks){
		//attacks.add(new GameActionNode(a, c.getActionBar().getCastPoint() + (interval * (curSlot + a.cost - 1)), 0, 0));
		for(Character c: chars){
			if(!c.isAI && c.yPosition == selected.yPosition + yOffset && c.isAlive()){
				attacks.add(new GameActionNode(a,curSlot + a.cost - 1, 0, 0, Direction.NONE));
			}
		}
	}
	
	/**
	 * Adds a diagonal ActionNode to attacks for every enemy that can be hit by 
	 * a diagonal attack from the AI's current position.
	 */
	public void addDiagonal(Action a, ArrayList<ActionNode> attacks){
		for(Character c: chars){
			if(!c.isAI & c.isAlive() && canHitDiagonal(
					selected.xPosition + xOffset, 
					selected.yPosition + yOffset, 
					c.xPosition, 
					c.yPosition)){
				Direction dir = c.yPosition < (selected.yPosition + yOffset) ? Direction.DOWN : Direction.UP;
				attacks.add(new GameActionNode(a,curSlot + a.cost -1, 0, 0, dir));
			}
		}
	}
	
	/**
	 * Picks a random projectile attack out of the selected characters
	 * available projectile attacks
	 */
	public ActionNode randomProjectile(){
		for(Action a: selected.availableActions){
			switch (a.pattern){
				case STRAIGHT:
					return new GameActionNode(a,curSlot + a.cost - 1, 0, 0, Direction.NONE);
				case DIAGONAL:
					Direction dir = (selected.yPosition + yOffset < board.height / 2) ? Direction.UP : Direction.DOWN;
					return new GameActionNode(a,curSlot + a.cost - 1, 0, 0, dir);
				default:
					break;
			}
		}
		return new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);			
	}
	
	/**
	 * Figures out all potential projectiles that could hit an enemy based on the
	 * current board position, and then returns a random one.
	 */
	public ActionNode getProjectile(){
		ArrayList<ActionNode> attacks = new ArrayList<ActionNode>();
		for(Action a: selected.availableActions){
			switch (a.pattern){
				case STRAIGHT:
					addStraight(a, attacks);
					break;
				case DIAGONAL:
					addDiagonal(a, attacks);
					break;
				default:
					break;
			}
		}
		if(attacks.size() == 0){
			attacks.add(randomProjectile());
		}
		Random r = new Random();
		return attacks.get(r.nextInt(attacks.size())); 
	}
	
	/**
	 * Adds a single attack ActionNode to attacks for every enemy.
	 */
	public void addSingle(Action a, ArrayList<ActionNode> attacks){
		for(Character c: chars){
			if(!c.isAI && c.isAlive()){
				attacks.add(new GameActionNode(a,curSlot + a.cost -1, c.xPosition, c.yPosition, Direction.NONE));
			}
		}
	}
	
	/**
	 * Returns an ActionNode which does a single square attack on a randomly selected enemy.
	 */
	public ActionNode getSingle(){
		ArrayList<ActionNode> attacks = new ArrayList<ActionNode>();
		for(Action a: selected.availableActions){
			switch (a.pattern){
				case SINGLE:
					addSingle(a, attacks);
					break;
				default:
					break;
			}
		}
		if(attacks.size() == 0){
			return new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);
		}
		else{
			Random r = new Random();
			return attacks.get(r.nextInt(attacks.size())); 
		}
	}
	
	/**
	 * Returns either a single square attack or a projectile based on the risk parameter
	 */
	public ActionNode getAttack(float risk){
		float f = (float) Math.random();
		if(f < risk && hasSingle){
			return getSingle();
		}
		else if(canHitSomeone(selected.xPosition + xOffset, selected.yPosition + yOffset)) {
			return getProjectile();
		}
		if(shield){
			return new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);
		}
		return getMovement();
	}
	
	/**
	 * Returns a shield action node if available. Otherwise, returns a movement.
	 */
	public ActionNode getShield(){
		for(Action a: selected.availableActions){
			switch (a.pattern){
				case SHIELD:
					shield = true;
					Direction dir = (selected.yPosition + yOffset < board.height / 2) ? Direction.UP : Direction.DOWN;
					return new GameActionNode(a,curSlot + a.cost -1, 0, 0, dir);
				default:
					break;
			}
		}
		if(shield){
			return new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);
		}
		return getMovement();
	}
	
	public ActionNode randomAction(){
		ArrayList<ActionNode> actions = new ArrayList<ActionNode>();
		for(Action a: selected.availableActions){
			switch (a.pattern){
				case MOVE:
					if(!shield){
						actions.add(randomMovement(a));
						actions.add(randomMovement(a));
					}
					break;
				case STRAIGHT:
					if(curSlot <= 3)
						actions.add(new GameActionNode(a, curSlot + a.cost - 1, 0, 0, Direction.NONE));
					break;
				case DIAGONAL:
					Direction dir = (selected.yPosition + yOffset < board.height / 2) ? Direction.UP : Direction.DOWN;
					if(curSlot <= 3)
						actions.add(new GameActionNode(a,curSlot + a.cost - 1, 0, 0, dir));
					break;
				default:
					break;
			}
		}
		Random r = new Random();
		// nextInt can only work with positive numbers (aka size 0 doesn't work)
		// this is the case when number of actions is equal to 4 and he tries to select
		// some move.
		if (actions.size() > 0 ){
			return actions.get(r.nextInt(actions.size())); 
		}
		else{
			return new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);
		}
	}
	
	
	/**
	 * Returns either a movement, projectile, or single square attack based on the 
	 * aggression and risk parameters.
	 */
	public ActionNode getAction(float aggression, float risk, float defensiveness, float intelligence){
		ActionNode action;
		if(curSlot == 4){
			float r = (float) Math.random();
			if(r < defensiveness){
				action = getShield();
			}
			else if(shield){
				action = new GameActionNode(nop, curSlot, 0, 0, Direction.NONE);
			}
			else{
				action = getMovement();
			}
		}
		else if(curSlot == 3){
			float r = (float) Math.random();
			if((r < aggression && canHitSomeone(selected.xPosition + xOffset, selected.yPosition + yOffset)) || shield){
				action = getProjectile();
			}
			else{
				action = getMovement();
			}
		}
		else{
			float r = (float) Math.random();
			if(r < defensiveness && !isSafe(selected.xPosition + xOffset, selected.yPosition + yOffset)){
				action = getShield();
			}
			else if(r < aggression || shield){
				action = getAttack(risk);
			}
			else{
				action = getMovement();
			}
		}
		float dumb = (float) Math.random();
		if(dumb > intelligence){
			return randomAction();
		}
		else{
			return action;
		}
	}
}
