package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.cornell.gdiac.ailab.Action.Effect;
import edu.cornell.gdiac.ailab.Action.Pattern;

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
	ActionBar bar;
	Character selected;
	Action nop;
	
	private int xOffset;
	private int yOffset;
	private float interval;
	private int curSlot;
	private boolean shield;
	
	public AIController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.chars = chars;
		this.bar = bar;
		nop = new Action("NOP", 1, 0, 0, Pattern.NOP, Effect.REGULAR, "no action");
	}
	
	public void update(){
		for (Character c : chars){
			if (c.needsSelection && c.isAI){
				c.needsSelection = false;
				selected = c;
				xOffset = 0;
				yOffset = 0;
				shield = false;
				interval = (1f-bar.castPoint) / selected.selectionMenu.TOTAL_SLOTS;
				curSlot = 1;
				selected.setQueuedActions(getActions(0.8f, 0.33f, 0.2f));
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
	 *  - Add shield logic
	 *  - Move more than one square to get into an attacking position
	 * 	- Take into account opponents position in cast bar 
	 * 		- be more aggressive if they are in their waiting phase, more
	 * 		  defensive if they are close to/ in their cast phase
	 *  - Dont always target directly at an opponent (in case they move)
	 *  - Consider all opponents positions at once (pick the attack that is most likely to hit)
	 *  - Work with teammate somehow
	 *  - Dodge incoming projectiles
	 */
	private List<ActionNode> getActions(float aggression, float risk, float defensiveness){
		LinkedList<ActionNode> actions = new LinkedList<ActionNode>();
		while(curSlot <= 4){
			ActionNode a = getAction(aggression, risk, defensiveness);
			curSlot += a.action.cost;
			if(a.action.pattern == Pattern.MOVE){
				xOffset = a.xPosition - selected.xPosition;
				yOffset = a.yPosition - selected.yPosition;
			}
			actions.add(a);
		}
		return actions;
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
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX + 1, curY));
		}
		if(isSafe(curX, curY + 1)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY + 1));
		}
		if(isSafe(curX - 1, curY)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX - 1, curY));
		}
		if(isSafe(curX, curY - 1)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY - 1));
		}
		if(isSafe(curX, curY)){
			nodes.add(new ActionNode(nop, bar.castPoint + (interval * (curSlot)), 0, 0));
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
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX + 1, curY));
		}
		if(canHitSomeone(curX, curY + 1) && ownSide(curX)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY + 1));
		}
		if(canHitSomeone(curX, curY - 1) && ownSide(curX)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY - 1));
		}
		if(canHitSomeone(curX - 1, curY) && ownSide(curX - 1)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX - 1, curY));
		}
		if(canHitSomeone(curX, curY)){
			nodes.add(new ActionNode(nop, bar.castPoint + (interval * (curSlot)), 0, 0));
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
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX + 1, curY));
		}
		if(board.isInBounds(curX, curY + 1) && ownSide(curX)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY + 1));
		}
		if(board.isInBounds(curX - 1, curY) && ownSide(curX - 1)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX - 1, curY));
		}
		if(board.isInBounds(curX, curY - 1) && ownSide(curX)){
			nodes.add(new ActionNode(movement, bar.castPoint + (interval * (curSlot)), curX, curY - 1));
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
		//attacks.add(new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost - 1)), 0, 0));
		for(Character c: chars){
			if(!c.isAI && c.yPosition == selected.yPosition + yOffset && c.isAlive()){
				attacks.add(new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost - 1)), 0, 0));
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
				int y = c.yPosition < (selected.yPosition + yOffset) ? 0 : 3;
				attacks.add(new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost -1)), 0, y));
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
					return new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost - 1)), 0, 0);
				case DIAGONAL:
					int dir = (selected.yPosition + yOffset < board.height / 2) ? 3 : 0;
					return new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost - 1)), 0, dir);
				default:
					break;
			}
		}
		return new ActionNode(nop, bar.castPoint + (interval * (curSlot)), 0, 0);			
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
				attacks.add(new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost -1)), c.xPosition, c.yPosition));
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
			return new ActionNode(nop, bar.castPoint + (interval * (curSlot)), 0, 0);
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
//		if(f < risk ){
//			return getSingle();
//		}
//		else if(canHitSomeone(selected.xPosition + xOffset, selected.yPosition + yOffset)) {
//			return getProjectile();
//		}
		if(canHitSomeone(selected.xPosition + xOffset, selected.yPosition + yOffset)) {
			return getProjectile();
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
					int dir = (selected.yPosition + yOffset < board.height / 2) ? 3 : 0;
					return new ActionNode(a, bar.castPoint + (interval * (curSlot + a.cost -1)), 0, dir);
				default:
					break;
			}
		}
		return getMovement();
	}
	
	/**
	 * Returns either a movement, projectile, or single square attack based on the 
	 * aggression and risk parameters.
	 */
	public ActionNode getAction(float aggression, float risk, float defensiveness){
		if(curSlot == 4){
			float r = (float) Math.random();
			if(r < defensiveness){
				return getShield();
			}
			return getMovement();
		}
		if(curSlot == 3){
			float r = (float) Math.random();
			if(r < aggression && canHitSomeone(selected.xPosition + xOffset, selected.yPosition + yOffset)){
				return getProjectile();
			}
			return getMovement();
		}
		else{
			float r = (float) Math.random();
			if(r < defensiveness && !isSafe(selected.xPosition + xOffset, selected.yPosition + yOffset)){
				return getShield();
			}
			if(r < aggression){
				return getAttack(risk);
			}
			return getMovement();
		}
	}
}
