package edu.cornell.gdiac.ailab;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.DecisionNode;
import edu.cornell.gdiac.ailab.DecisionNode.IndexNode;
import edu.cornell.gdiac.ailab.DecisionNode.LeafNode;
import edu.cornell.gdiac.ailab.DecisionNode.MoveList;
import edu.cornell.gdiac.ailab.DecisionNode.Specific;
import edu.cornell.gdiac.ailab.DecisionNode.Tactic;
import edu.cornell.gdiac.ailab.Effect.Type;
import org.json.simple.*;


public class TacticalManager extends ConditionalManager{
	
	private static boolean PRINT_MODE = false;
	
	private DecisionNode tacticalTree;
	private HashMap<String, DecisionNode> nodeMap;
	private HashMap<String, LeafNode> preSelected;
	private ArrayList<Coordinate> goalTiles;
	private Coordinate goal;
	
	public TacticalManager(){
		firstMove = new HashSet<Character>();
		preSelected = new HashMap<String, LeafNode>();
		nodeMap = new HashMap<String, DecisionNode>();
	}
	
	public void setRoot(DecisionNode n){
		tacticalTree = n;
	}
	
	public void addToMap(String s, DecisionNode n){
		nodeMap.put(s, n);
	}
	
	
	public void setState(GridBoard board, List<Character> chars, Shields shields){
		this.chars = chars;
		this.board = board;
		this.shields = shields;
	}
	
	
	/**
	 * Update all the condition booleans in the Conditional Manager
	 */
	public void updateConditions(Character c){
		selected = c;
		friends = new ArrayList<Character>();
		enemies = new ArrayList<Character>();
		for(Character ch: chars){
			if(c.leftside == ch.leftside && ch != c && ch.isAlive()){
				friends.add(ch);
			}
			else if(c.leftside != ch.leftside && ch != c && ch.isAlive()){
				enemies.add(ch);
			}
		}
		update(board, chars, friends, enemies, c);
	}
	
	
	/**
	 * Select an action for the character whose id is "id". First check if this
	 * character's specific actions have already been selected. If they have, 
	 * then just use those. If a general course of action has been preselected
	 * for this character, then traverse the character's tree for that general
	 * action. There is no action preselected, then traverse the main Tactical
	 * Manager tree to find a list of actions. 
	 */
	public void selectActions(Character c){
		System.out.print(c.name+": ");
		System.out.println(map.get("is_protected"));
		// System.out.println(map.get("single_in_range")+" ");
		//System.out.print(map.get("MED_INT_CHANCE")+" ");
		//System.out.println(map.get("HIGH_INT_CHANCE")+" ");

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
		if(leaf.allyTactic != Tactic.NONE){
			Character friend = findNextFriend();
			LeafNode friendLeaf = new LeafNode(Tactic.NONE, leaf.allyTactic, leaf.allySpecific);
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
		IndexNode node = (IndexNode) nodeMap.get(c.name);
		DecisionNode subTree = getSubtreeFromTactic(node, general);
		LeafNode charLeaf = traverse(subTree);
		return getActionsFromSpecific(c, charLeaf.mySpecific);
	}
	
	
	/**
	 * Find the leaf node given an IndexNode n as a start point
	 */
	public LeafNode traverse(DecisionNode n){
		if(n instanceof LeafNode){
			//System.out.println(selected.name + " " + n.label + " ");
			return (LeafNode) n;
		}
		IndexNode index = (IndexNode) n;
		//System.out.println(index);
		//System.out.println(index.conditions);
		for(int i = 0; i < index.conditions.size(); i++){
			List<String> conds = index.conditions.get(i);
			System.out.println(conds.toString());
			boolean matched = true;
			for(String s: conds){
				if(!map.containsKey(s) || !map.get(s)){
					matched = false;
					break;
				}
			}
			if(matched){
//				System.out.println(conds.toString());
//				System.out.println(index.decisions.get(i).toString());
//				DecisionNode x = nodeMap.get(index.decisions.get(i));
//				if(x instanceof LeafNode){
//					LeafNode l = (LeafNode) x;
//					if(l.myTactic == Tactic.SPECIFIC){
//						System.out.println(selected.name+ ": " + conds.toString());
//						System.out.println(l.mySpecific.specificActions.toString());
//						System.out.println("-----------------------------------------------");
//					}
//				}
//				if(nodeMap.get(index.decisions.get(i)) == null){
//					System.out.println(conds.toString());
//				}
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
		LinkedList<ActionNode> nodes = new LinkedList<ActionNode>();
		int startSlot = 0;
		int x = c.xPosition;
		int y = c.yPosition;
		//System.out.print(c.name+ "moves: ");
		goal = null;
		for(Specific s: moves){
			if(startSlot >= c.actionBar.getUsableNumSlots()) break;
			ActionNode a = nopNode(c, startSlot);
			//System.out.print(s.toString()+" ");
			switch(s){
				case SINGLE_OPTIMAL:
					a = singleOptimal(c, startSlot, x, y);
					break;
				case SINGLE_WEAKEST:
					a = singleWeakest(c, startSlot, x, y);
					break;
				case SINGLE_STRONGEST:
					a = singleStrongest(c, startSlot, x, y);
					break;
				case SINGLE_SELF:
					a = singleSelf(c, startSlot, x, y);
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
				case MOVE_GOAL:
					a = moveGoal(c, startSlot, x, y);
					break;
				case MOVE_PROTECT:
					a = moveProtect(c, startSlot, x, y);
				case RANDOM_DECENT:
					a = randomDecentMove(c, startSlot, x, y);
					break;
				default:
					//System.out.println("nopnode");
					a = nopNode(c, startSlot);
					break;
			}
			startSlot+=a.action.cost;
			x = x + applyMoveX(a);
			y = y + applyMoveY(a);
			nodes.add(a);
		}
		//System.out.println();
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
	private ActionNode singleOptimal(Character c, int startPoint, int xPos, int yPos){
		Action a = single(c);
		Character opt = null;
		int framesToMove = Integer.MIN_VALUE;
		for(Character e: enemies){
			if(!a.hitsTarget(xPos, yPos, e.xPosition, e.yPosition, c.leftside, board)) continue;
			int frames = minFramesToMove(e);
			if(frames > framesToMove){
				framesToMove = frames;
				opt = e;
			}
		}
		if(opt == null){
			return nopNode(c, startPoint);
		}
		return new ActionNode(a, getCastTime(c, a, startPoint), opt.xPosition, opt.yPosition);
	}
	

	/**
	 * Returns a single square attack aimed at the enemy with the lowest health
	 */
	public ActionNode singleWeakest(Character attacker, int startPoint, int xPos, int yPos){
		Action a = single(attacker);
		int minHealth = Integer.MAX_VALUE;
		Character weakest = null;
		for(Character c: enemies){
			if(!a.hitsTarget(xPos, yPos, c.xPosition, c.yPosition, c.leftside, board)) continue;
			if(c.health < minHealth){
				minHealth = c.health;
				weakest = c;
			}
		}
		if(weakest == null){
			return nopNode(attacker, startPoint);
		}
		return singleNode(attacker, startPoint, weakest.xPosition, weakest.yPosition);	
	}
	
	/**
	 * Returns a single square attack on the current square
	 */
	public ActionNode singleSelf(Character attacker, int startPoint, int xPos, int yPos){
		return singleNode(attacker, startPoint, xPos, yPos);
	}
	
	
	/**
	 * Returns a single square attack aimed at the enemy with highest health
	 */
	public ActionNode singleStrongest(Character attacker, int startPoint, int xPos, int yPos){
		Action a = single(attacker);
		int minHealth = Integer.MIN_VALUE;
		Character strongest = null;
		for(Character c: enemies){
			if(!a.hitsTarget(xPos, yPos, c.xPosition, c.yPosition, c.leftside, board)) continue;
			if(c.health > minHealth){
				minHealth = c.health;
				strongest = c;
			}
		}
		if(strongest == null){
			return nopNode(attacker, startPoint);
		}
		return singleNode(attacker, startPoint, strongest.xPosition, strongest.yPosition);	
	}
	
	
	/**
	 * Returns an ActionNode representing a single square attack by character c
	 * targeted at (x,y), starting from slot "startPoint"
	 */
	public ActionNode singleNode(Character c, int startPoint, int xTarget, int yTarget){
		Action a = single(c);
		return new ActionNode(a, getCastTime(c, a, startPoint), xTarget, yTarget, Direction.NONE);
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
	 * Launch an attack, assuming character is at (xPos,yPos)
	 */
	public ActionNode attackNode(Character c, int startPoint, int xPos, int yPos, Action a){
		if(a.pattern == Pattern.NOP){
			return anyAttack(c, startPoint);
		}
		if(a.pattern == Pattern.SINGLE){
			return singleOptimal(c, startPoint, xPos, yPos);
		}
		else if(a.pattern == Pattern.DIAGONAL){
			Direction d = findToggleDirection(c, a, xPos, yPos);
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		else{
			Direction d = findToggleDirection(c, a, xPos, yPos);
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
	}
	
	
	private Direction findToggleDirection(Character c, Action a, int xPos, int yPos){
		for(Character e: enemies){
			//System.out.println("myPos: ("+xPos+","+yPos+")"+"   theirPos: ("+e.xPosition+","+e.yPosition+")");
			if(a.hitsTarget(xPos, yPos, e.xPosition, e.yPosition, c.leftside, board)){
				//System.out.println("my y: " + c.yPosition + "   their y: "+ e.yPosition);
				if(yPos == e.yPosition){
					return yPos == board.height - 1 ? Direction.DOWN : Direction.UP;
				}
				return e.yPosition < yPos ? Direction.DOWN : Direction.UP;
			}
		}
		//System.out.println("none: "+a.name+" "+xPos+" "+yPos+"   "+a.getNeedsToggle());
		return Direction.NONE;
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
			if(a.damage > 0){
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
		int maxCost = Integer.MIN_VALUE;
		for(Action a: c.availableActions){
			if(a.damage > 0){
				for(Character e: enemies){
					if(a.cost > maxCost && canAttackSquareFrom(xPos, yPos, e.xPosition, e.yPosition, a)){
						powerful = a;
						maxCost = a.cost;
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
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
				return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.NONE);
			}
		}
		return nopNode(c, startPoint);
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
		int x1;
		int x2;
		for(Character f: friends){
			x1 = c.leftside? xPos : f.xPosition;
			x2 = c.leftside? f.xPosition : xPos;
			if(x1 + 1 >= xPos && x2 - yPos == 1){
				return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.UP);
			}
			if(x1 + 1 >= x2 && f.yPosition - yPos == -1){
				return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.DOWN);
			}
		}
		for(Character f: friends){
			x1 = c.leftside? xPos : f.xPosition;
			x2 = c.leftside? f.xPosition : xPos;
			if(x1 >= x2 && f.yPosition - yPos == 2){
				return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.UP);
			}
			if(x1 >= x2 && f.yPosition - yPos == -2){
				return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.DOWN);
			}
		}
		if(yPos >= board.height - 1){
			return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.DOWN);
		}
		else{
			return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, Direction.UP);
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
	 * Move towards goal tile
	 */
	private ActionNode moveGoal(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);
		if(goal == null){
			setGoalTile(c, 3, 4);
		}
		for(Coordinate coord: goalTiles){
			int prevDist = Math.abs(goal.x - xPos) + Math.abs(goal.y - yPos);
			int dist = Math.abs(goal.x - coord.x) + Math.abs(goal.y - coord.y);
			boolean adjacent = Math.abs(coord.x - xPos) + Math.abs(coord.y - yPos) == 1;
        	//System.out.println(coord.toString() + " prev: "+ prevDist + " dist: "+ dist+ "adjacent: "+adjacent);
			if(dist < prevDist && adjacent && board.canMove(c.leftside, coord.x, coord.y)){
				if(coord.x < xPos){
					return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.LEFT);
				}
				if(coord.x > xPos){
					return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.RIGHT);
				}
				if(coord.y < yPos){
					return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.DOWN);
				}
				if(coord.y > yPos){
					return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.UP);
				}
			}
		}
		return moveDefensive(c, startPoint, xPos, yPos);
	}
	
	/**
	 * Right now, only checks for an adjacent safe square and moves there. Prioritize tiles that 
	 * would also lead to potential offensive positions.
	 * Future improvement: mark ideal defensive goal tiles and try to move there if 
	 * performing a sequence of more than one move
	 */
	private ActionNode moveDefensive(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);

		Direction d = optimalDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		d = defensiveDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		
		return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, randomDirection(c, xPos, yPos));
	}
	
	
	/**
	 * Right now, only checks for an adjacent safe square and moves there. Prioritize tiles 
	 * that would also be save to move to.
	 * Future improvement: mark ideal aggressive goal tiles and try to move there if 
	 * performing a sequence of more than one move
	 */
	private ActionNode moveAggressive(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);
		Direction d = optimalDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		d = attackingDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, randomDirection(c, xPos, yPos));
	}
	
	/** 
	 * Try to move to a square where you could protect an ally with a shield
	 */
	private ActionNode moveProtect(Character c, int startPoint, int xPos, int yPos){
		Action a = move(c);
		Direction d = protectDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d);
		}
		d = attackingDirection(c, xPos, yPos);
		if(d != Direction.NONE){
			d = xPos < board.width/2 ? Direction.RIGHT : Direction.LEFT;
		}
		return new ActionNode(a, getCastTime(c, a, startPoint), xPos, yPos, d);
	}
	
	
	private Direction protectDirection(Character c, int xPos, int yPos){
		for(Character ally: friends){
			if(c.leftside){
				if(ally.xPosition == xPos+1 && Math.abs(ally.yPosition - yPos) == 1 && board.canMove(c.leftside,xPos+1, yPos)){
					return Direction.RIGHT;
				}
				else if(ally.xPosition <= xPos && ally.yPosition - yPos == 2 && board.canMove(c.leftside, xPos, yPos+1)){
					return Direction.UP;
				}
				else if(ally.xPosition <= xPos && yPos - ally.yPosition == 2 && board.canMove(c.leftside,xPos, yPos-1)){
					return Direction.DOWN;
				}
			}
			else{
				if(ally.xPosition == xPos -1 && Math.abs(ally.yPosition - yPos) == 1 && board.canMove(c.leftside,xPos - 1, yPos)){
					return Direction.LEFT;
				}
				else if(ally.xPosition >= xPos && ally.yPosition - yPos == 2 && board.canMove(c.leftside, xPos, yPos+1)){
					return Direction.UP;
				}
				else if(ally.xPosition >= xPos && yPos - ally.yPosition == 2 && board.canMove(c.leftside,xPos, yPos-1)){
					return Direction.DOWN;
				}
			}
		}
		return Direction.NONE;
	}
	
	
	/**
	 * Try to find a single movement that is both offensive and defensive: move to
	 * a square where you can attack the enemy but they cant attack you. Return NONE if
	 * no such move exists.
	 */
	private Direction optimalDirection(Character c, int xPos, int yPos){
		ArrayList<Direction> list = new ArrayList<Direction>();
		if(canHitEnemyFrom(c, xPos-1, yPos) && isSafeSquare(xPos-1, yPos) && board.canMove(c.leftside, xPos-1, yPos)){
			list.add(Direction.LEFT);
		}
		if(canHitEnemyFrom(c, xPos, yPos+1) && isSafeSquare(xPos, yPos+1) && board.canMove(c.leftside, xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(canHitEnemyFrom(c, xPos, yPos-1) && isSafeSquare(xPos, yPos-1) && board.canMove(c.leftside, xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(canHitEnemyFrom(c, xPos+1, yPos) && isSafeSquare(xPos+1, yPos) && board.canMove(c.leftside, xPos+1, yPos)){
			list.add(Direction.RIGHT);
		}
		if(list.size() == 0){
			if(canHitEnemyFromSingle(c, xPos-1, yPos) && isSafeSquare(xPos-1, yPos) && board.canMove(c.leftside, xPos-1, yPos)){
				list.add(Direction.LEFT);
			}
			if(canHitEnemyFromSingle(c, xPos, yPos+1) && isSafeSquare(xPos, yPos+1) && board.canMove(c.leftside, xPos, yPos+1)){
				list.add(Direction.UP);
			}
			if(canHitEnemyFromSingle(c, xPos, yPos-1) && isSafeSquare(xPos, yPos-1) && board.canMove(c.leftside, xPos, yPos-1)){
				list.add(Direction.DOWN);
			}
			if(canHitEnemyFromSingle(c, xPos+1, yPos) && isSafeSquare(xPos+1, yPos) && board.canMove(c.leftside, xPos+1, yPos)){
				list.add(Direction.RIGHT);
			}
		}
		if(list.size() == 0){
			return Direction.NONE;
		}
		Random r = new Random();
		return list.get(r.nextInt(list.size()));
	}
	
	
	/**
	 * Try to find a single movement that will put character in position to attack
	 */
	private Direction attackingDirection(Character c, int xPos, int yPos){
		ArrayList<Direction> list = new ArrayList<Direction>();
		if(canHitEnemyFrom(c, xPos-1, yPos) && board.canMove(c.leftside, xPos-1, yPos)){
			list.add(Direction.LEFT);
		}
		if(canHitEnemyFrom(c, xPos, yPos+1) && board.canMove(c.leftside, xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(canHitEnemyFrom(c, xPos, yPos-1) && board.canMove(c.leftside, xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(canHitEnemyFrom(c, xPos+1, yPos) && board.canMove(c.leftside, xPos+1, yPos)){
			list.add(Direction.RIGHT);
		}
		if(list.size() == 0){
			if(canHitEnemyFromSingle(c, xPos-1, yPos) && board.canMove(c.leftside, xPos-1, yPos)){
				list.add(Direction.LEFT);
			}
			if(canHitEnemyFromSingle(c, xPos, yPos+1) && board.canMove(c.leftside, xPos, yPos+1)){
				list.add(Direction.UP);
			}
			if(canHitEnemyFromSingle(c, xPos, yPos-1) && board.canMove(c.leftside, xPos, yPos-1)){
				list.add(Direction.DOWN);
			}
			if(canHitEnemyFromSingle(c, xPos+1, yPos) && board.canMove(c.leftside, xPos+1, yPos)){
				list.add(Direction.RIGHT);
			}		
		}
		if(list.size() == 0){
			return Direction.NONE;
		}
		Random r = new Random();
		return list.get(r.nextInt(list.size()));	
	}
	
	
	/**
	 * Try to find a single movement that will put the character at a safe square
	 */
	private Direction defensiveDirection(Character c, int xPos, int yPos){
		ArrayList<Direction> list = new ArrayList<Direction>();
		if(isSafeSquare(xPos+1, yPos) && board.canMove(c.leftside, xPos+1, yPos)){
			list.add(Direction.RIGHT);
		}
		if(isSafeSquare(xPos, yPos+1) && board.canMove(c.leftside, xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(isSafeSquare(xPos, yPos-1) && board.canMove(c.leftside, xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(isSafeSquare(xPos-1, yPos) && board.canMove(c.leftside, xPos-1, yPos)){
			list.add(Direction.LEFT);
		}
		if(list.size() == 0){
			return Direction.NONE;
		}
		Random r = new Random();
		return list.get(r.nextInt(list.size()));	
	}
	
	
	/**
	 * Returns a single random movement action from (xPos, yPos)
	 */
	private Direction randomDirection(Character c, int xPos, int yPos){
		ArrayList<Direction> directions = new ArrayList<Direction>();
		if(board.canMove(c.leftside, xPos + 1, yPos)  && ownSide(xPos + 1)){
			directions.add(Direction.RIGHT);
		}
		if(board.canMove(c.leftside, xPos, yPos + 1)){
			directions.add(Direction.UP);
		}
		if(board.canMove(c.leftside, xPos - 1, yPos) && ownSide(xPos - 1)){
			directions.add(Direction.LEFT);
		}
		if(board.canMove(c.leftside, xPos, yPos - 1)){
			directions.add(Direction.DOWN);
		}
		Random r = new Random();
		//next int requires a positive size
		if (directions.size() > 0){
			return directions.get(r.nextInt(directions.size()));	
		}
		else{
			return Direction.UP;
		}
	}
	
	/**
	 * Return the tile that would be optimal for this character to be on
	 */
	public void setGoalTile(Character c, int aWeight, int dWeight){
		Coordinates cPool = Coordinates.getInstance();
		int xStart = c.leftside? 0 : board.width / 2;
		int xEnd = c.leftside? (board.width / 2) : board.width;
		ArrayList<Coordinate> goals = new ArrayList<Coordinate>();
		ArrayList<Integer> values = new ArrayList<Integer>();
     	System.out.println();

		for(int x = xStart; x < xEnd; x++){
			for(int y = 0; y < board.height; y++){
				int curValue = 0;
				//System.out.println("--------STARTING "+x+" "+y+" "+c.name+"-------------");
				//calculate offensive value
				for(Action a: c.availableActions){
					for(Character e: enemies){
						if(a.hitsTarget(x, y, e.xPosition, e.yPosition, c.leftside, board)){
					//		System.out.println("Can hit "+e.name+ " with "+a.name);
							curValue += aWeight;
						}
					}
				}
				//calculate defensive value
				for(Character e: enemies){
					for(Action a: e.availableActions){
						if(a.hitsTarget(e.xPosition, e.yPosition, x, y, e.leftside, board)){
						//	System.out.println("hit by "+e.name+ " with "+a.name);
							curValue -= dWeight;
						}
					}
				}
				//insert in sorted order
				boolean added = false;
				for(int i = 0; i < values.size(); i++){
					if(curValue > values.get(i)){
						goals.add(i, cPool.newCoordinate(x, y));
						values.add(i, curValue);
						added = true;
						break;
					}
				}
				if(!added){
					goals.add(cPool.newCoordinate(x, y));
					values.add(curValue);
				}
			}
		}
		goalTiles = goals;
		int i = 0;
		for(Coordinate coord: goals){
			if(board.canMove(c.leftside, coord.x, coord.y)){
				goal = coord;
				//System.out.println(c.name+": "+coord.x+" "+coord.y+" "+"value: "+values.get(i));
				return;
			}
			i++;
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
	private float getCastTime(Character c, Action a, int startPoint){
		return c.actionBar.getCastPoint() + (c.getInterval() * (startPoint + a.cost));
	}
		
	
	/**
	 * Returns the number of frames until this character could potentially move
	 */
	private int minFramesToMove(Character c){
		if(c.hasShield() && c.castPosition > c.actionBar.getCastPoint()){
			int waitFrames = (int) ((c.actionBar.getCastPoint()) / c.getSpeed());
			int castFrames = (int) ((1f - c.castPosition) / c.getSpeed());
			return waitFrames + castFrames;
		}
		
		else if(c.castPosition <= c.actionBar.getCastPoint()){
			int waitFrames = (int) ((c.actionBar.getCastPoint() - c.castPosition) / c.getSpeed());
			int castFrames = (int) (c.getInterval() / c.getSpeed());
			return waitFrames + castFrames;
		}
		else{
			float nextCastPoint = c.actionBar.getCastPoint();
			while(nextCastPoint < c.castPosition){
				nextCastPoint += c.getInterval();
			}
			float castDistance =  nextCastPoint - c.castPosition;
//			if(c.castPosition - c.lastCastStart > interval){
//				castDistance += interval;
//			}
			return (int) (castDistance / c.getSpeed());
		}
	}
	
	
	/**
	 * Returns a nop action object
	 */
	public Action nop(){
		return new Action("NOP", 1, 0, 0, 0, Pattern.NOP, false, false,false, new Effect(0, Type.REGULAR, 0, "Nope",null), "no action",null);
	}
	
	
	/**
	 * Returns an action node representing a nop action object
	 */
	public ActionNode nopNode(Character c, int startPoint){
		Action a = nop();
		return new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, Direction.NONE);
	}
	
	//=======================================================================//
	//             +-----------------------+                                 //
	//	           | Random Move Selection |						         //
	//	           +-----------------------+						         //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	public ActionNode randomDecentMove(Character c, int startPoint, int xPos, int yPos){
		ArrayList<ActionNode> possibleActions = new ArrayList<ActionNode>();
		possibleActions.addAll(attacksThatCanHit(c, startPoint, xPos, yPos));
		int size = possibleActions.size();
		
		Random r = new Random();
		for(int i = 0; i <= size; i++){
			int j = r.nextInt(3);
			if(j == 0){
				possibleActions.add(moveGoal(c,startPoint,xPos,yPos));
			}
			if(j == 1){
				possibleActions.add(moveAggressive(c,startPoint,xPos,yPos));
			}
			if(j == 2){
				possibleActions.add(moveDefensive(c,startPoint,xPos,yPos));
			}
		}
		return possibleActions.get((r.nextInt(possibleActions.size())));
	}
	
	public List<ActionNode> attacksThatCanHit(Character c, int startPoint, int xPos, int yPos){
		ArrayList<ActionNode> attacks = new ArrayList<ActionNode>();
		for(Action a: c.availableActions){
			if(a.cost <= c.actionBar.getUsableNumSlots() - startPoint){
				for(Character e: enemies){
					if(a.hitsTarget(xPos, yPos, e.xPosition, e.yPosition, c.leftside, board)){
						if(a.pattern == Pattern.SINGLE){
							attacks.add(singleOptimal(c, startPoint, xPos, yPos));
						}
						else{
							Direction d = xPos < yPos ? Direction.DOWN : Direction.UP;
							attacks.add(new ActionNode(a, getCastTime(c, a, startPoint), 0, 0, d));
						}
					}
				}
			}
		}
		return attacks;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
	
	
	
	
	//=======================================================================//
	//             +------------+                                            //
	//	           | JSON Stuff |							                 //
	//	           +------------+							                 //
	//			             \   ^__^										 //
	//			              \  (00)\_______							     //	
	//			                 (__)\       )\/\							 //
	//			                     ||----w |								 //
	//			                     ||     ||								 //
    //=======================================================================//
	
	
	/**
	 * Output data to a file
	 */
	public void outputData(Character c, JSONArray jsonArray){
		JSONObject json = new JSONObject();
		StringBuilder vectorBuilder = new StringBuilder();
		for(String s: conditions){
			if(map.get(s)){
				vectorBuilder.append('1');
			}
			else{
				vectorBuilder.append('0');
			}
		}
		String vector = vectorBuilder.toString();

		JSONArray array = new JSONArray();
		int startSlot = 0;
		int xPos = c.xPosition;
		int yPos = c.yPosition;
		for(ActionNode a: c.queuedActions){
			JSONObject moveMap = new JSONObject();
			xPos += applyMoveX(a);
			yPos += applyMoveY(a);
			ArrayList<Specific> moves = getPossibleMoves(c, a, xPos, yPos, startSlot);
			JSONArray possibleMoves = new JSONArray();
			for(Specific move: moves){
				possibleMoves.add(move.toString());
			}
			moveMap.put(a.action.name, possibleMoves);
			array.add(moveMap);
			startSlot += a.action.cost;
		}
		
		json.put("vector", vector);
		json.put("actions", array);
		jsonArray.add(json);
		if (PRINT_MODE){
			System.out.println(json.toString());
		}
	}
	
	
	/**
	 * Return a lost of possible specific moves that could have led to this ActionNode
	 */
	public ArrayList<Specific> getPossibleMoves(Character c, ActionNode a, int xPos, int yPos, int startSlot){
		ArrayList<Specific> moves = new ArrayList<Specific>();
		addSinglePossibilities(c, a, moves, startSlot, xPos, yPos);
		if(a.action.pattern == Pattern.SHIELD){
			moves.add(Specific.SHIELD);
		}
		addAttackPossibilities(c, a, moves, xPos, yPos);
		addMovePossibilities(c, a, moves, xPos, yPos);
		return moves;
	}
	
	
	private void addAttackPossibilities(Character c, ActionNode a, ArrayList<Specific> moves, int xPos, int yPos){
		if(a.action.pattern == Pattern.SINGLE) return;
		if(a.action.pattern == Pattern.MOVE) return;
		if(a.action.pattern == Pattern.SHIELD) return;
		Action power = powerfulAttack(c, xPos, yPos);
		if(a.action.damage == power.cost){
			moves.add(Specific.POWERFUL_ATTACK);
		}
		Action quick = quickAttack(c, xPos, yPos);
		if(a.action.cost == quick.cost){
			moves.add(Specific.QUICK_ATTACK);
		}
		moves.add(Specific.NORMAL_ATTACK);
	}
	
	private void addSinglePossibilities(Character c, ActionNode a, ArrayList<Specific> moves, int startSlot, int xPos, int yPos){
		if(a.action.pattern != Pattern.SINGLE) return;
		ArrayList<Character> weakest = new ArrayList<Character>();
		ArrayList<Character> strongest = new ArrayList<Character>();
		int minHealth = Integer.MAX_VALUE;
		int maxHealth = Integer.MIN_VALUE;
		for(Character e: enemies){
			if(e.health == minHealth){
				weakest.add(e);
			}
			if(e.health < minHealth){
				weakest = new ArrayList<Character>();
				weakest.add(e);
				minHealth = e.health;
			}
			if(e.health == maxHealth){
				strongest.add(e);
			}
			if(e.health > maxHealth){
				strongest = new ArrayList<Character>();
				strongest.add(e);
				maxHealth = e.health;
			}

		}
		for(Character weak: weakest){
			if(a.xPosition == weak.xPosition && a.yPosition == weak.yPosition){
				moves.add(Specific.SINGLE_WEAKEST);
			}
		}
		for(Character strong: strongest){
			if(a.xPosition == strong.xPosition && a.yPosition == strong.yPosition){
				moves.add(Specific.SINGLE_STRONGEST);
			}
		}
		ActionNode node = singleOptimal(c, startSlot, xPos, yPos);
		if(node.xPosition == a.xPosition && node.yPosition == a.yPosition || moves.size() == 0){
			moves.add(Specific.SINGLE_OPTIMAL);
		}
	}
	
	private void addMovePossibilities(Character c, ActionNode a, ArrayList<Specific> moves, int xPos, int yPos){
		if(a.action.pattern != Pattern.MOVE){
			return;
		}
		boolean added = false;
		int x = xPos;
		int y = yPos;
		if(a.direction == Direction.LEFT) x--;
		if(a.direction == Direction.RIGHT) x++;
		if(a.direction == Direction.DOWN) y--;
		if(a.direction == Direction.UP) y++;
		if(isSafeSquare(x,y)){
			added = true;
			moves.add(Specific.MOVE_DEFENSIVE);
		}
		//System.out.println(canHitEnemyFrom(c,x,y));
		if(canHitEnemyFrom(c, x, y)){
			added = true;
			moves.add(Specific.MOVE_AGGRESSIVE);
		}
		if(!added){
			moves.add(Specific.MOVE_AGGRESSIVE);
			moves.add(Specific.MOVE_DEFENSIVE);
		}
	}
}
 