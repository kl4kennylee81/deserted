package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class ConditionalManager extends TacticalManager {
	public HashMap<String, Boolean> map;
	
	public static final String[] strings = {
		"IsSafe"
	};

	
	private List<Character> chars;
	private ActionBar bar;
	private GridBoard board;
	private Character selected;
	public List<Character> friends;
	public List<Character> enemies;

	
    private float interval;

	
	public void update(GridBoard board, List<Character> chars, List<Character> friends, List<Character> enemies, ActionBar bar, Character c){
		this.chars = chars;
		this.bar = bar;
		this.board = board;
		selected = c;
		interval  = (1f-ActionBar.castPoint) / ActionBar.getTotalSlots();
		map = new HashMap<String, Boolean>();
		this.friends = friends;
		this.enemies = enemies;
	}
	
	
	/**
	 * Returns true if the selected character's current square is safe
	 */
	public boolean isSafe(){
		return isSafeSquare(selected.xPosition, selected.yPosition);
		
	}
	
	
	/** 
	 * Returns true if the selected character's current square is not safe
	 */
	public boolean isNotSafe(){
		return !isSafeSquare(selected.xPosition, selected.yPosition);
	}
	
	
	/**
	 * Returns true if the selected character can hit an opponent from his 
	 * current location
	 */
	public boolean canHitOpponent(){
		return canHitEnemyFrom(selected.xPosition, selected.yPosition);
	}
	
	
	/**
	 * Returns true if there is an adjacent square to the selected character's
	 * current position from which he could hit an emeny. 
	 */
	public boolean attackSquareAdjacent(){
		int x = selected.xPosition;
		int y = selected.yPosition;
		if(canHitEnemyFrom(x+1, y)){
			return true;
		}
		if(canHitEnemyFrom(x, y+1)){
			return true;
		}
		if(canHitEnemyFrom(x-1, y)){
			return true;
		}
		if(canHitEnemyFrom(x, y-1)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true if there is an adjacent square to the selected character's 
	 * current position from which he could move to and not be in the range of
	 * an enemy attack.
	 */
	public boolean safeSquareAdjacent(){
		int x = selected.xPosition;
		int y = selected.yPosition;
		if(isSafeSquare(x+1, y)){
			return true;
		}
		if(isSafeSquare(x, y+1)){
			return true;
		}
		if(isSafeSquare(x-1, y)){
			return true;
		}
		if(isSafeSquare(x, y-1)){
			return true;
		}
		return false;
	}
	
	

	/**
	 * Returns true if one of my allies is in their casting phase
	 */
	public boolean friendIsCasting(){
		for(Character c: friends){
			if(c.castPosition >= c.lastCastStart){
				return true;
			}
		}
		return false;
		
	}
	
	
	/** 
	 * Returns true if one of my allies is currently putting up a shield
	 */
	public boolean friendIsShielding(){
		for(Character c: friends){
			if(c.hasShield()){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if there is an opponent that is currently casting move
	 */
	public boolean opponentIsCasting(){
		for(Character c: enemies){
			if(c.castPosition >= c.lastCastStart){
				return true;
			}
		}
		return false;
		
	}
	
	
	
	public void opponentIsAttackingMe(){
		
	}
	
	
	public void opponentIsAttackingFriend(){
		
	}
	
	
	/**
	 * Returns true if I could be interrupted even if I use my fastest action and 
	 * my enemies use their slowest actions.
	 */
	public boolean highInterruptChance(){
		return slowInterruptible(quickestAction(selected));
	}
	
	/** 
	 * Returns true if the selected character could be interrupted if an enemy 
	 * decides to use a quick action, but would likely be safe if the enemy 
	 * decides to use a slow action
	 */
	public boolean mediumInterruptChance(){
		int mySlots = quickestAction(selected);
		return fastInterruptible(mySlots) && !slowInterruptible(mySlots);
		
	}
	
	/**
	 * Returns true if the selected character cannot possilby be interrupted
	 * if he decides to use a quick action
	 */
	public boolean lowInterruptChance(){
		return !fastInterruptible(quickestAction(selected));
	}
	
	/**
	 * Returns true if the selected character cannot possibly be interrupted
	 * even if he decides to use his longest action.
	 */
	public boolean noInterruptChance(){
		return !fastInterruptible(longestAction(selected));
	}
	
	
	public boolean hasLowHealth(){
		if(selected.health <= 4){
			return true;
		}
		return false;
	}
	
	public boolean isHealthy(){
		if(selected.health >= (selected.maxHealth - (selected.maxHealth/4))){
			return true;
		}
		return false;
	}
	
	public boolean isAlone(){
		for(Character c: friends){
			if(c.isAlive()){
				return false;
			}
		}
		return true;
		
	}
	
	public boolean opponentHasWall(){
		for(Character c: chars){
			if(!c.isAI && c.hasShield()){
				return true;
			}
		}
		return false;
		
	}
	
	
	/**
	 * Returns true if a projectile cast from the selected character's current
	 * position could be blocked by a current shield on the board.
	 */
	public boolean projectileCouldBeBlocked(){
		for(Character c: enemies){
			if(c.hasShield()){
				if(c.castPosition > ActionBar.castPoint){
					return true;
				}
				else{
					int framesLeft = (int) ((ActionBar.castPoint - c.castPosition) / c.getBarSpeed());
					int slots = fastestMoveThatCanHit(c);
					int framesToCast = (int) ((interval * slots) / selected.getCastSpeed());
					if(framesLeft > framesToCast){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if there is only one enemy left
	 */
	public boolean oneEnemyLeft(){
		return enemies.size() == 1;
	}
	
	/**
	 * Returns true if the selected character is currently behind another
	 * character's shield.
	 */
	public boolean isProtected(){
		for(Character c: friends){
			if(c.hasShield()){
				List<Coordinate> list = c.getShieldedCoords();
				for(Coordinate coord: list){
					if(coord.y == selected.yPosition && coord.x <= selected.xPosition){
						return true;
					}
				}
			}
		}
		return false;
		
	}
	
	
	/**
	 * Returns true if the selected character can protect an ally with a shield
	 */
	public boolean canProtect(){
		for(Character c: friends){
			if(selected.xPosition <= c.xPosition && 
					Math.abs(selected.yPosition - c.yPosition) <= 1){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if there is ally which can protect the selected character 
	 * with a shield
	 */
	public boolean canBeProtected(){
		for(Character c: friends){
			if(c.xPosition <= selected.xPosition && 
					Math.abs(selected.yPosition - c.yPosition) <= 1){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the selected character can be protected by an ally after a 
	 * single move
	 */
	public boolean canBeProtectedWithMove(){
		for(Character c: friends){
			if(c.xPosition <= selected.xPosition+1 && Math.abs(c.yPosition - selected.yPosition) <= 1){
				return true;
			}
			if(c.xPosition <= selected.xPosition && Math.abs(c.yPosition - selected.yPosition) <= 2){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if an ally has a shield up such that the selected character
	 * would be protected with a single move action
	 */
	public boolean wouldBeProtectedWithMove(){
		for(Character c: friends){
			if(c.hasShield()){
				List<Coordinate> list = c.getShieldedCoords();
				for(Coordinate coord: list){
					if(coord.y == selected.yPosition && coord.x <= selected.xPosition+1){
						return true;
					}
					if(Math.abs(coord.y - selected.yPosition) <= 1 && coord.x <= selected.xPosition){
						return true;
					}
				}
					
			}
		}
		return false;
		
	}
	
	
	/**
	 * Returns true if a friend could move into a protected spot if I put a shield up
	 */
	public boolean canProtectFriendWithMove(){
		for(Character c: friends){
			if(selected.xPosition <= c.xPosition+1 && Math.abs(c.yPosition - selected.yPosition) <= 1){
				return true;
			}
			if(selected.xPosition <= c.xPosition && Math.abs(c.yPosition - selected.yPosition) <= 2){
				return true;
			}
		}
		return false;
	}
	
	
	
	//=========================================================================//
	//  	        +----------------+                                         //
	//  	        | HELPER METHODS |                                         // 
	//  	        +----------------+                                         //
	//      	           \   ^__^                                            //
	//          	        \  (OO)\_______                                    // 
	//             	           (__)\       )\/\                                //
	//                 	           ||----w |                                   //
	//                             ||     ||                                   //
	//=========================================================================//

	
	/**
	 * Returns the number of frames until character c can cast an action that costs
	 * a certain number of slots. 
	 */
	public int framesToAction(Character c, int slots){
		if(c.castPosition < ActionBar.castPoint){
			int waitFrames = (int) ((ActionBar.castPoint - c.castPosition) / c.getBarSpeed());
			int castFrames = (int) ((interval * slots) / c.getCastSpeed());
			return waitFrames + castFrames;
		}
		else {
			float distance = (c.lastCastStart + (interval * slots)) - c.castPosition;
			return Math.max(0, (int) (distance / c.getCastSpeed()));
		}
	}
	
	/**
	 * Returns the number of frames until I can cast an action that costs a certain 
	 * number of slots.
	 */
	public int framesToMyAction(int slots){
		return (int) ((interval * slots) / selected.getCastSpeed());
	}
	
	/**
	 * Returns the number of slots of the characters fastest interrupting move
	 */
	public int fastestMoveThatCanHitMe(Character c){
		int minCost = Integer.MAX_VALUE;
		for(Action a: c.availableActions){
			if(a.pattern != Pattern.MOVE && inRangeOfAction(c, selected, a)){
				minCost = Math.min(minCost, a.cost);
			}
		}
		return minCost;
	}
	
	
	/**
	 * Returns the number of slots of the selected character's fastest move that could
	 * hit character c.
	 */
	public int fastestMoveThatCanHit(Character c){
		int minCost = Integer.MAX_VALUE;
		for(Action a: selected.availableActions){
			if(a.pattern != Pattern.MOVE && inRangeOfAction(selected, c, a)){
				minCost = Math.min(minCost, a.cost);
			}
		}
		return minCost;
	}
	
	
	/**
	 * Returns the number of slots of the characters fastest interrupting move
	 */
	public int slowestMoveThatCanHitMe(Character c){
		int maxCost = Integer.MIN_VALUE;
		for(Action a: c.availableActions){
			if(a.pattern != Pattern.MOVE && inRangeOfAction(c, selected, a)){
				maxCost = Math.max(maxCost, a.cost);
			}
		}
		return maxCost;
	}
	
	
	/**
	 * Returns the number of slots required by the character's longest move
	 */
	public int longestAction(Character c){
		int maxCost = Integer.MIN_VALUE;
		for(Action a: c.availableActions){
			maxCost = Math.max(a.cost, maxCost);
		}
		return maxCost;
	}
	
	/**
	 * Returns the number of slots required by the character's quickest move 
	 * (excluding movement)
	 */
	public int quickestAction(Character c){
		int minCost = Integer.MAX_VALUE;
		for(Action a: c.availableActions){
			if(a.pattern != Pattern.MOVE){
				minCost = Math.max(a.cost, minCost);
			}
		}
		return minCost;
	}
	
	
	/**
	 * Returns the number of slots required by the character's quickest attack 
	 * (excluding single-square)
	 */
	public int quickestAttack(Character c){
		int minCost = Integer.MAX_VALUE;
		for(Action a: c.availableActions){
			if(a.damage >= 0 && a.pattern != Pattern.SINGLE){
				minCost = Math.max(a.cost, minCost);
			}
		}
		return minCost;
	}
	
	
	/**
	 * Returns true if I could potentially be interrupted by a character if I 
	 * decided to cast an action which requires "slots" number of slots.
	 */
	public boolean interruptibleBy(Character c, int mySlots, int theirSlots){
		int theirFrames = framesToAction(c, theirSlots);
		int myFrames = framesToMyAction(mySlots);
		return theirFrames < myFrames;
	}
	
	
	/**
	 * Returns true if I could possibly be interrupted by any character
	 * if I decide to cast an aciton which requires "slots" number of slots
	 */
	public boolean fastInterruptible(int slots){
		for(Character c: enemies){
			if(interruptibleBy(c, slots, fastestMoveThatCanHitMe(c))){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if I could be interrupted by any character if I decide
	 * to cast an action which requires "slots" slots, and they decide to 
	 * cast their slowest action.
	 */
	public boolean slowInterruptible(int slots){
		for(Character c: enemies){
			if(interruptibleBy(c, slots, slowestMoveThatCanHitMe(c))){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if the action cast by character c1 could hit character c2.
	 */
	public boolean inRangeOfAction(Character c1, Character c2, Action a){
		return canAttackSquare(c1, c2.xPosition, c2.yPosition, a);
	}
	
	
	/** 
	 * Returns true if Character c can attack square (x,y) without using a
	 * single-square attack.
	 */
	public boolean canAttackSquareNoSingle(Character c, int x, int y){
		return canAttackSquareNoSingle(c, c.xPosition, c.yPosition, x, y);
	}
	
	/** 
	 * Returns true if Character c can attack square (x2,y2) from (x1,y1) without using a
	 * single-square attack
	 */
	public boolean canAttackSquareNoSingle(Character c, int x1, int y1, int x2, int y2){
		for(Action a: c.availableActions){
			if(a.pattern != Pattern.SINGLE){
				if(canAttackSquareFrom(x1, y1, x2, y2, a)) return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if Character c can attack square (x,y) with Action a.
	 */
	public boolean canAttackSquare(Character c, int x, int y, Action a){
		return canAttackSquareFrom(c.xPosition, c.yPosition, x, y, a);
	}
	
	
	/**
	 * Returns true if the square specified by (x,y) is a safe square
	 */
	public boolean isSafeSquare(int x, int y){
		for(Character c: enemies){
			if(canAttackSquareNoSingle(c, x, y)) return false;
		}
		return true;
	}
	
	/**
	 * Returns true if Action a cast from (x1,y1) can hit square (x2,y2);
	 */
	public boolean canAttackSquareFrom(int x1, int y1, int x2, int y2, Action a){
		return true;
	}
	
	
	/**
	 * Returns true if the selected character can hit an enemy from square (x,y)
	 */
	public boolean canHitEnemyFrom(int x, int y){
		for(Character c: enemies){
			if(canAttackSquareNoSingle(selected, x, y, c.xPosition, c.yPosition)){
				return true;
			}
		}
		return false;
	}
}
