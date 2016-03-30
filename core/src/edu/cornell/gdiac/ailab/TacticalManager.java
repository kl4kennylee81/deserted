package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.DecisionNode;
import edu.cornell.gdiac.ailab.DecisionNode.IndexNode;
import edu.cornell.gdiac.ailab.DecisionNode.LeafNode;
import edu.cornell.gdiac.ailab.DecisionNode.MoveList;
import edu.cornell.gdiac.ailab.DecisionNode.Specific;
import edu.cornell.gdiac.ailab.DecisionNode.Tactic;
import edu.cornell.gdiac.ailab.Effect.Type;


public class TacticalManager extends ConditionalManager{
	
	private DecisionNode tacticalTree;
	private HashMap<String, DecisionNode> nodeMap;
	private HashMap<String, LeafNode> preSelected;
	private HashMap<String, IndexNode> characterTrees;
	
	public TacticalManager(){
		preSelected = new HashMap<String, LeafNode>();
		characterTrees = new HashMap<String, IndexNode>();
		nodeMap = new HashMap<String, DecisionNode>();
	}
	
	
	public void setState(GridBoard board, List<Character> chars, ActionBar bar){
		this.chars = chars;
		this.board = board;
		this.bar = bar;
	}
	
	
	/**
	 * Update all the condition booleans in the Conditional Manager
	 */
	public void updateConditions(Character c){
		friends = new ArrayList<Character>();
		enemies = new ArrayList<Character>();
		interval  = (1f-ActionBar.castPoint) / ActionBar.getTotalSlots();
		for(Character ch: chars){
			if(ch.isAI && ch != c && ch.isAlive()){
				friends.add(ch);
			}
			else if(ch != c && ch.isAlive()){
				enemies.add(ch);
			}
		}
		update(board, chars, friends, enemies, bar, c);
	}
	
	
	/**
	 * Select and action for the character whose id is "id". First check if this
	 * character's specific actions have already been selected. If they have, 
	 * then just use those. If a general course of action has been preselected
	 * for this character, then traverse the character's tree for that general
	 * action. There is no action preselected, then traverse the main Tactical
	 * Manager tree to find a list of actions. 
	 */
	public void selectActions(Character c){
		LeafNode leaf;
		//Either get the preselected leaf or traverse the tree to find it
		if(preSelected.containsKey(c.name)){
			leaf = preSelected.get(c.name);
			preSelected.remove(c.name);
		}
		else{
			leaf = traverse(tacticalTree);
		}
		
		//If the leaf has an action for a ally, add it to the preselected info
		if(leaf.friendTactic != Tactic.NONE){
			Character friend = findNextFriend(c);
			LeafNode friendLeaf = new LeafNode(leaf.friendTactic, leaf.friendSpecific);
			preSelected.put(friend.name, friendLeaf);
		}
		
		if(leaf.myTactic == Tactic.SPECIFIC){
			c.setQueuedActions(getActionsFromSpecific(c, leaf.mySpecific));
		}
		else {
			c.setQueuedActions(getActionsFromGeneral(c, leaf.myTactic));
		}
	}
	
	
	/**
	 * Find the teammate that will reach the casting phase next.
	 */
	public Character findNextFriend(Character c){
		//IMPLEMENT
		return null;
	}
	
	
	/**
	 * Given an IndexNode which is the root of a character's individual 
	 * decision tree, find the subtree which corresponds to the chosen general
	 * tactic.
	 */
	public DecisionNode getSubtreeFromTactic(IndexNode node, Tactic tactic){
		for(String s: node.decisions){
			DecisionNode n = nodeMap.get(s); 
			if(n.branchType == tactic){
				return n;
			}
		}
		System.out.println("NO TACTIC MATCHED");
		return null;
	}
	
	
	/**
	 * Given a general course of action, traverse the character's individual 
	 * decision tree to figure our what to do.
	 */
	public List<ActionNode> getActionsFromGeneral(Character c, Tactic general){
		IndexNode node = characterTrees.get(c.name);
		DecisionNode subTree = getSubtreeFromTactic(node, general);
		LeafNode charLeaf = traverse(subTree);
		return getActionsFromSpecific(c, charLeaf.mySpecific);
	}
	
	
	/**
	 * Find the leaf node given an IndexNode n as a start point
	 */
	public LeafNode traverse(DecisionNode n){
		if(n instanceof LeafNode){
			return (LeafNode) n;
		}
		IndexNode index = (IndexNode) n;
		for(int i = 0; i < index.conditions.size(); i++){
			List<String> conds = index.conditions.get(i);
			boolean matched = true;
			for(String s: conds){
				if(map.containsKey(s) || !map.get(s)){
					matched = false;
					break;
				}
			}
			if(matched){
				return traverse(nodeMap.get(index.decisions.get(i)));
			}
		}
		System.out.println("NO CONDITION MATCHED");
		return null;
	}
	
	
	/**
	 * Given a specific list of actions, convert them into ActionNodes.
	 */
	public List<ActionNode> getActionsFromSpecific(Character c, MoveList specific){
		ArrayList<Specific> moves = specific.specificActions;
		ArrayList<ActionNode> nodes = new ArrayList<ActionNode>();
		int startSlot = 0;
		int x = c.xPosition;
		int y = c.yPosition;
		for(Specific s: moves){
			ActionNode a = nopNode(startSlot);
			switch(s){
				case SINGLE_OPTIMAL:
					a = singleOptimal(c, startSlot);
					break;
				case SINGLE_WEAKEST:
					a = singleWeakest(c, startSlot);
					break;
				case SINGLE_STRONGEST:
					a = singleStrongest(c, startSlot);
					break;
				case NORMAL_ATTACK:
					a = attackNode(c, startSlot, x, y, normalAttack(c, x, y));
					break;
				case QUICK_ATTACK:
					a = attackNode(c, startSlot, x, y, quickAttack(c, x, y));
					break;
				case POWERFUL_ATTACK:
					a = attackNode(c, startSlot, x, y, powerfulAttack(c, x, y));
					break;
				case SHIELD:
					a = shieldNode(c, startSlot, x, y);
					break; 
				case MOVE_AGGRESSIVE:
					a = moveAggressive(c, startSlot, x, y);
					break;
				case MOVE_DEFENSIVE:
					a = moveDefensive(c, startSlot, x, y);
					break;
				default:
					a = nopNode(startSlot);
					break;
			}
			startSlot+=a.action.cost;
			x = x + applyMoveX(a);
			y = y + applyMoveY(a);
			nodes.add(a);
		}
		return nodes;
	}
	
	
	/**
	 * Returns -1 if this is a move left, +1 if this is a move right, 0 otherwise
	 */
	private int applyMoveX(ActionNode a){
		if(a.action.pattern == Pattern.MOVE && a.direction == Direction.LEFT){
			return -1;
		}
		if(a.action.pattern == Pattern.MOVE && a.direction == Direction.RIGHT){
			return 1;
		}
		return 0;
	}
	
	
	/**
	 * Returns -1 if this is a move down, +1 if this is a move up, 0 otherwise
	 */
	private int applyMoveY(ActionNode a){
		if(a.action.pattern == Pattern.MOVE && a.direction == Direction.DOWN){
			return -1;
		}
		if(a.action.pattern == Pattern.MOVE && a.direction == Direction.UP){
			return 1;
		}
		return 0;
	}
	
	
	
	
	//=======================================================================//
	//    +-----------------------------------+                              //
	//	  | SINGLE-SQUARE SELECTION FUNCTIONS |							     //
	//	  +-----------------------------------+							     //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	/**
	 * Returns a single-square Action object if c has one
	 */
	public Action single(Character c){
		for(Action a: c.availableActions){
			if(a.pattern == Pattern.SINGLE){
				return a;
			}
		}
		return nop();
	}
	
	
	/**
	 * Returns a single-square attack node which attacks the character with the 
	 * greatest chance of hitting
	 */
	private ActionNode singleOptimal(Character c, int startPoint){
		Action a = single(c);
		Character opt = null;
		int framesToMove = Integer.MIN_VALUE;
		for(Character e: enemies){
			int frames = minFramesToMove(e);
			if(frames > framesToMove){
				framesToMove = frames;
				opt = e;
			}
		}
		if(opt == null){
			return nopNode(startPoint);
		}
		ActionNodes anPool = ActionNodes.getInstance();
		return anPool.newActionNode(a, getCastTime(a, startPoint), opt.xPosition, opt.yPosition);
	}
	

	/**
	 * Returns a single square attack aimed at the enemy with the lowest health
	 */
	public ActionNode singleWeakest(Character attacker, int startPoint){
		int minHealth = Integer.MAX_VALUE;
		Character weakest = null;
		for(Character c: enemies){
			if(c.health < minHealth){
				minHealth = c.health;
				weakest = c;
			}
		}
		return singleNode(attacker, startPoint, weakest.xPosition, weakest.yPosition);	
	}
	
	
	/**
	 * Returns a single square attack aimed at the enemy with highest health
	 */
	public ActionNode singleStrongest(Character attacker, int startPoint){
		int minHealth = Integer.MIN_VALUE;
		Character strongest = null;
		for(Character c: enemies){
			if(c.health > minHealth){
				minHealth = c.health;
				strongest = c;
			}
		}
		return singleNode(attacker, startPoint, strongest.xPosition, strongest.yPosition);	
	}
	
	
	/**
	 * Returns an ActionNode representing a single square attack by character c
	 * targeted at (x,y), starting from slot "startPoint"
	 */
	public ActionNode singleNode(Character c, int startPoint, int xTarget, int yTarget){
		ActionNodes anPool = ActionNodes.getInstance();
		Action a = single(c);
		return anPool.newActionNode(a, getCastTime(a, startPoint), xTarget, yTarget, Direction.NONE);
	}
	
	
	

	//=======================================================================//
	//    +----------------------------+                                     //
	//	  | ATTACK SELECTION FUNCTIONS |							         //
	//	  +----------------------------+							         //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	
	/**
	 * Launch a normal, non-single-target attack, assuming character is at (xPos,yPos)
	 */
	public ActionNode attackNode(Character c, int startPoint, int xPos, int yPos, Action a){
		if(a.pattern == Pattern.NOP){
			return anyAttack(c, startPoint);
		}
		ActionNodes anPool = ActionNodes.getInstance();
		if(a.pattern == Pattern.DIAGONAL){
			Direction d = findDiagonalDirection(c, a, xPos, yPos);
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, d);
		}
		else{
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, Direction.NONE);
		}
	}
	
	
	
	/**
	 * Find any normal attack from the character that can hit an opponent, assuming
	 * the character is at (xPos,yPos)
	 */
	public Action normalAttack(Character c, int xPos, int yPos){
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
				for(Character e: enemies){
					if(canAttackSquareFrom(xPos, yPos, e.xPosition, e.yPosition, a)){
						return a;
					}
				}
			}
		}
		return nop();
	}
	
	
	/**
	 * Find the quickest normal attack from the character that can hit an opponent, 
	 * assuming the character is at (xPos,yPos)
	 */
	public Action quickAttack(Character c, int xPos, int yPos){
		Action quickest = nop();
		int minCost = Integer.MAX_VALUE;
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
				for(Character e: enemies){
					if(a.cost < minCost && canAttackSquareFrom(xPos, yPos, e.xPosition, e.yPosition, a)){
						quickest = a;
						minCost = a.cost;
					}
				}
			}
		}
		return quickest;
	}
	
	
	/**
	 * Find the most powerful normal attack from the character that can hit an 
	 * opponent, assuming the character is at (xPos,yPos)
	 */
	public Action powerfulAttack(Character c, int xPos, int yPos){
		Action powerful = nop();
		int maxDamage = Integer.MIN_VALUE;
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
				for(Character e: enemies){
					if(a.damage > maxDamage && canAttackSquareFrom(xPos, yPos, e.xPosition, e.yPosition, a)){
						powerful = a;
						maxDamage = a.damage;
					}
				}
			}
		}
		return powerful;
	}
	
	
	/**
	 * Figures out what direction to send a diagonal projectile from (xPos, yPos)
	 */
	private Direction findDiagonalDirection(Character c, Action a, int xPos, int yPos){
		for(Character e: enemies){
			if(canAttackSquareFrom(xPos, yPos, e.xPosition, e.yPosition, a)){
				return e.yPosition < yPos ? Direction.DOWN : Direction.UP;
			}
		}
		return Direction.NONE;
	}
	
	
	/**
	 * Returns an attack node for any non-single-square attack
	 */
	private ActionNode anyAttack(Character c, int startPoint){
		ActionNodes anPool = ActionNodes.getInstance();
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
				return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, Direction.NONE);
			}
		}
		return nopNode(startPoint);
	}
	
	
	
	//=======================================================================//
	//    +----------------------------+                                     //
	//	  | SHIELD SELECTION FUNCTIONS |							         //
	//	  +----------------------------+							         //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	/**
	 * Return a shield action if this character has one
	 */
	private Action shield(Character c){
		for(Action a: c.availableActions){
			if(a.pattern == Pattern.SHIELD){
				return a;
			}
		}
		return nop();
	}
	
	
	/**
	 * Intelligently put up a shield action node, aiming to protect an ally
	 */
	private ActionNode shieldNode(Character c, int startPoint, int xPos, int yPos){
		Action a = shield(c);
		ActionNodes anPool = ActionNodes.getInstance();
		for(Character f: friends){
			if(f.xPosition + 1 >= xPos && f.yPosition - yPos == 1){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.UP);
			}
			if(f.xPosition + 1 >= xPos && f.yPosition - yPos == -1){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.DOWN);
			}
		}
		for(Character f: friends){
			if(f.xPosition >= xPos && f.yPosition - yPos == 2){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.UP);
			}
			if(f.xPosition >= xPos && f.yPosition - yPos == -2){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.DOWN);
			}
		}
		if(yPos >= board.height - 1){
			return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.DOWN);
		}
		else{
			return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.UP);
		}
	}
	
	
	
	//=======================================================================//
	//    +------------------------------+                                   //
	//	  | MOVEMENT SELECTION FUNCTIONS |							         //
	//	  +------------------------------+							         //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	/** 
	 * Return the movement action from a character 
	 */
	private Action move(Character c){
		for(Action a: c.availableActions){
			if(a.pattern == Pattern.MOVE){
				return a;
			}
		}
		return nop();
	}
	
	
	/**
	 * Right now, only checks for an adjacent safe square and moves there. Prioritize tiles that 
	 * would also lead to potential offensive positions.
	 * Future improvement: mark ideal defensive goal tiles and try to move there if 
	 * performing a sequence of more than one move
	 */
	private ActionNode moveDefensive(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);
		ActionNodes anPool = ActionNodes.getInstance();

		Direction d = optimalDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, d);
		}
		d = defensiveDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, d);
		}
		
		return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, randomDirection(xPos, yPos));
	}
	
	
	/**
	 * Right now, only checks for an adjacent safe square and moves there. Prioritize tiles 
	 * that would also be save to move to.
	 * Future improvement: mark ideal aggressive goal tiles and try to move there if 
	 * performing a sequence of more than one move
	 */
	private ActionNode moveAggressive(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);
		ActionNodes anPool = ActionNodes.getInstance();
		Direction d = optimalDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, d);
		}
		d = attackingDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, d);
		}
		
		return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, randomDirection(xPos, yPos));
	}
	
	
	/**
	 * Try to find a single movement that is both offensive and defensive: move to
	 * a square where you can attack the enemy but they cant attack you. Return NONE if
	 * no such move exists.
	 */
	private Direction optimalDirection(Character c, int xPos, int yPos){
		if(canHitEnemyFrom(xPos-1, yPos) && isSafeSquare(xPos-1, yPos) && !board.isOccupied(xPos-1, yPos)){
			return Direction.LEFT;
		}
		if(canHitEnemyFrom(xPos, yPos+1) && isSafeSquare(xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			return Direction.UP;
		}
		if(canHitEnemyFrom(xPos, yPos-1) && isSafeSquare(xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			return Direction.DOWN;
		}
		if(canHitEnemyFrom(xPos+1, yPos) && isSafeSquare(xPos+1, yPos) && !board.isOccupied(xPos+1, yPos)){
			return Direction.RIGHT;
		}
		return Direction.NONE;
	}
	
	
	/**
	 * Try to find a single movement that will put character in position to attack
	 */
	private Direction attackingDirection(Character c, int xPos, int yPos){
		if(canHitEnemyFrom(xPos-1, yPos) && !board.isOccupied(xPos-1, yPos)){
			return Direction.LEFT;
		}
		if(canHitEnemyFrom(xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			return Direction.UP;
		}
		if(canHitEnemyFrom(xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			return Direction.DOWN;
		}
		if(canHitEnemyFrom(xPos+1, yPos) && !board.isOccupied(xPos+1, yPos)){
			return Direction.RIGHT;
		}
		return Direction.NONE;
	}
	
	
	/**
	 * Try to find a single movement that will put the character at a safe square
	 */
	private Direction defensiveDirection(Character c, int xPos, int yPos){
		if(canHitEnemyFrom(xPos+1, yPos) && !board.isOccupied(xPos+1, yPos)){
			return Direction.RIGHT;
		}
		if(canHitEnemyFrom(xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			return Direction.UP;
		}
		if(canHitEnemyFrom(xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			return Direction.DOWN;
		}
		if(canHitEnemyFrom(xPos-1, yPos) && !board.isOccupied(xPos-1, yPos)){
			return Direction.LEFT;
		}
		return Direction.NONE;
	}
	
	
	/**
	 * Returns a single random movement action from (xPos, yPos)
	 */
	private Direction randomDirection(int xPos, int yPos){
		ArrayList<Direction> directions = new ArrayList<Direction>();
		if(board.isInBounds(xPos + 1, yPos)){
			directions.add(Direction.RIGHT);
		}
		if(board.isInBounds(xPos, yPos + 1)){
			directions.add(Direction.UP);
		}
		if(board.isInBounds(xPos - 1, yPos) && ownSide(xPos - 1)){
			directions.add(Direction.LEFT);
		}
		if(board.isInBounds(xPos, yPos - 1)){
			directions.add(Direction.DOWN);
		}
		Random r = new Random();
		return directions.get(r.nextInt(directions.size()));	
	}
	
	
	/**
	 * Returns true if a tile (x, _ ) would be on the selected character's own side
	 * of the board.
	 */
	private boolean ownSide(int x){
		if(selected.xPosition <= board.width/2){
			return (x <= board.width/2) ? true : false;
		}
		else{
			return (x > board.width/2) ? true : false;
		}
	}
	
	
	
	//=======================================================================//
	//    +-------------------------+                                        //
	//	  | EXTRA UTILITY FUNCTIONS |							             //
	//	  +-------------------------+							             //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	
	/**
	 * Return the value in the cast bar where this action will go off
	 */
	private float getCastTime(Action a, int startPoint){
		return ActionBar.castPoint + (interval * (startPoint + a.cost));
	}
		
	
	/**
	 * Returns the number of frames until this character could potentially move
	 */
	private int minFramesToMove(Character c){
		if(c.castPosition < ActionBar.castPoint){
			int waitFrames = (int) ((ActionBar.castPoint - c.castPosition) / c.getBarSpeed());
			int castFrames = (int) (interval / c.getCastSpeed());
			return waitFrames + castFrames;
		}
		else{
			float nextCastPoint = ActionBar.castPoint;
			while(nextCastPoint < c.castPosition){
				nextCastPoint += interval;
			}
			float castDistance =  nextCastPoint - c.castPosition;
//			if(c.castPosition - c.lastCastStart > interval){
//				castDistance += interval;
//			}
			return (int) (castDistance / c.getCastSpeed());
		}
	}
	
	
	/**
	 * Returns a nop action object
	 */
	public Action nop(){
		return new Action("NOP", 1, 0, 0, Pattern.NOP, new Effect(0, Type.REGULAR, 0, "Nope"), "no action");
	}
	
	
	/**
	 * Returns an action node representing a nop action object
	 */
	public ActionNode nopNode(int startPoint){
		ActionNodes anPool = ActionNodes.getInstance();
		Action a = nop();
		return anPool.newActionNode(a, getCastTime(a, startPoint), 0, 0, Direction.NONE);
	}
}
 