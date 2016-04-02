package edu.cornell.gdiac.ailab;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedList;
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
import org.json.simple.*;


public class TacticalManager extends ConditionalManager{
	
	private DecisionNode tacticalTree;
	private HashMap<String, DecisionNode> nodeMap;
	private HashMap<String, LeafNode> preSelected;
	
	public TacticalManager(){
		preSelected = new HashMap<String, LeafNode>();
		nodeMap = new HashMap<String, DecisionNode>();
	}
	
	public void setRoot(DecisionNode n){
		tacticalTree = n;
	}
	
	public void addToMap(String s, DecisionNode n){
		nodeMap.put(s, n);
	}
	
	
	public void setState(GridBoard board, List<Character> chars){
		this.chars = chars;
		this.board = board;
	}
	
	
	/**
	 * Update all the condition booleans in the Conditional Manager
	 */
	public void updateConditions(Character c){
		selected = c;
		friends = new ArrayList<Character>();
		enemies = new ArrayList<Character>();
		interval  = (1f-ActionBar.castPoint) / ActionBar.getTotalSlots();
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
//		System.out.print(c.name+": ");
//		System.out.print(map.get("NO_INT_CHANCE")+" ");
//		System.out.print(map.get("LOW_INT_CHANCE")+" ");
//		System.out.print(map.get("MED_INT_CHANCE")+" ");
//		System.out.println(map.get("HIGH_INT_CHANCE")+" ");

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
			return (LeafNode) n;
		}
		IndexNode index = (IndexNode) n;
		for(int i = 0; i < index.conditions.size(); i++){
			List<String> conds = index.conditions.get(i);
			boolean matched = true;
			for(String s: conds){
				if(!map.containsKey(s) || !map.get(s)){
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
		LinkedList<ActionNode> nodes = new LinkedList<ActionNode>();
		int startSlot = 0;
		int x = c.xPosition;
		int y = c.yPosition;
		//System.out.print(c.name+ "moves: ");
		for(Specific s: moves){
			ActionNode a = nopNode(startSlot);
			//System.out.print(s.toString()+" ");
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
					System.out.println("nopnode");
					a = nopNode(startSlot);
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
		int maxCost = Integer.MIN_VALUE;
		for(Action a: c.availableActions){
			if(a.damage > 0 && a.pattern != Pattern.SINGLE){
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
		int x1;
		int x2;
		for(Character f: friends){
			x1 = c.leftside? xPos : f.xPosition;
			x2 = c.leftside? f.xPosition : xPos;
			if(x1 + 1 >= xPos && x2 - yPos == 1){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.UP);
			}
			if(x1 + 1 >= x2 && f.yPosition - yPos == -1){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.DOWN);
			}
		}
		for(Character f: friends){
			x1 = c.leftside? xPos : f.xPosition;
			x2 = c.leftside? f.xPosition : xPos;
			if(x1 >= x2 && f.yPosition - yPos == 2){
				return anPool.newActionNode(a, getCastTime(a, startPoint), xPos, yPos, Direction.UP);
			}
			if(x1 >= x2 && f.yPosition - yPos == -2){
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
		ArrayList<Direction> list = new ArrayList<Direction>();
		if(canHitEnemyFrom(c, xPos-1, yPos) && isSafeSquare(xPos-1, yPos) && !board.isOccupied(xPos-1, yPos) && ownSide(xPos - 1)){
			list.add(Direction.LEFT);
		}
		if(canHitEnemyFrom(c, xPos, yPos+1) && isSafeSquare(xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(canHitEnemyFrom(c, xPos, yPos-1) && isSafeSquare(xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(canHitEnemyFrom(c, xPos+1, yPos) && isSafeSquare(xPos+1, yPos) && !board.isOccupied(xPos+1, yPos) && ownSide(xPos + 1)){
			list.add(Direction.RIGHT);
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
		if(canHitEnemyFrom(c, xPos-1, yPos) && !board.isOccupied(xPos-1, yPos) && ownSide(xPos-1)){
			list.add(Direction.LEFT);
		}
		if(canHitEnemyFrom(c, xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(canHitEnemyFrom(c, xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(canHitEnemyFrom(c, xPos+1, yPos) && !board.isOccupied(xPos+1, yPos) && ownSide(xPos + 1)){
			list.add(Direction.RIGHT);
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
		if(isSafeSquare(xPos+1, yPos) && !board.isOccupied(xPos+1, yPos) && ownSide(xPos + 1)){
			list.add(Direction.RIGHT);
		}
		if(isSafeSquare(xPos, yPos+1) && !board.isOccupied(xPos, yPos+1)){
			list.add(Direction.UP);
		}
		if(isSafeSquare(xPos, yPos-1) && !board.isOccupied(xPos, yPos-1)){
			list.add(Direction.DOWN);
		}
		if(isSafeSquare(xPos-1, yPos) && !board.isOccupied(xPos-1, yPos) && ownSide(xPos-1)){
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
	private Direction randomDirection(int xPos, int yPos){
		ArrayList<Direction> directions = new ArrayList<Direction>();
		if(board.isInBounds(xPos + 1, yPos)  && ownSide(xPos + 1)){
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
		if(c.hasShield() && c.castPosition > ActionBar.castPoint){
			int waitFrames = (int) ((ActionBar.castPoint) / c.getBarSpeed());
			int castFrames = (int) ((1f - c.castPosition) / c.getCastSpeed());
			return waitFrames + castFrames;
		}
		
		else if(c.castPosition <= ActionBar.castPoint){
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
	public void outputData(Character c){
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
			ArrayList<Specific> moves = getPossibleMoves(c, a, xPos, yPos, startSlot);
			JSONArray possibleMoves = new JSONArray();
			for(Specific move: moves){
				possibleMoves.add(move.toString());
			}
			moveMap.put(a.action.name, possibleMoves);
			array.add(moveMap);
			startSlot += a.action.cost;
			xPos += applyMoveX(a);
			yPos += applyMoveY(a);
		}
		
		json.put("vector", vector);
		json.put("actions", array);
		System.out.println(json.toString());
	}
	
	
	/**
	 * Return a lost of possible specific moves that could have led to this ActionNode
	 */
	public ArrayList<Specific> getPossibleMoves(Character c, ActionNode a, int xPos, int yPos, int startSlot){
		ArrayList<Specific> moves = new ArrayList<Specific>();
		addSinglePossibilities(c, a, moves, startSlot);
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
	
	private void addSinglePossibilities(Character c, ActionNode a, ArrayList<Specific> moves, int startSlot){
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
		ActionNode node = singleOptimal(c, startSlot);
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
 