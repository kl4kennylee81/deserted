package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class ConditionalManager {
	public HashMap<String, Boolean> map;
	
	
	protected List<Character> chars;
	protected GridBoard board;
	protected Character selected;
	protected List<Character> friends;
	protected List<Character> enemies;
	protected Shields shields;
	
	protected HashSet<Character> firstMove;
    
    protected final String[] conditions = {
    		"default",
    		"safe", 
    		"not_safe", 
    		"can_hit",
    		"quick_attack_can_hit",
    		"adjacent_safe_square", 
    		"adjacent_attack_square",
    		"adjacent_attack_square_single",
    		"adjacent_attack_square_quick",
    		"no_adjacent_attack_square",
    		"single_in_range",
    		"ally_casting", 
    		"ally_shielding", 
    		"enemy_casting",
    		"high_interrupt_chance",
    		"medium_interrupt_chance",
    		"low_interrupt_chance", 
    		"no_interrupt_chance",
    		"low_health", 
    		"high_health", 
    		"alone",
    		"enemy_has_wall", 
    		"is_protected", 
    		"can_protect",
    		"can_protect_with_move",
    		"protected_by_move",
    		"maybe_protected_by_move",
    		"could_be_blocked",
    		"one_enemy_left", 
    		"has_shield",
    		"has_single", 
    		"ally_safe",
    		"ally_can_hit_enemy",
    		"ally_almost_done_waiting",
    		"can_interrupt_enemy",
    		"first_move",
    		"not_hastened"
    };

	
	public void update(GridBoard board, List<Character> chars, List<Character> friends, List<Character> enemies, Character c){
		this.chars = chars;
		this.board = board;
		selected = c;
		map = new HashMap<String, Boolean>();
		this.friends = friends;
		this.enemies = enemies;
		
		map.put("safe", isSafe());
		map.put("safe_from_single", isSafeFromSingle());
		map.put("not_safe", isNotSafe());
		map.put("can_hit", canHitOpponent());
		map.put("quick_attack_can_hit", canHitQuickAttack());
		map.put("adjacent_attack_square", attackSquareAdjacent());
	    map.put("adjacent_attack_square_single", attackSquareAdjacentSingle());
	    map.put("adjacent_attack_square_quick", attackSquareAdjacentQuick());
		map.put("no_adjacent_attack_square", !attackSquareAdjacent());
		map.put("adjacent_safe_square", safeSquareAdjacent());
		map.put("ally_casting", friendIsCasting());
		map.put("ally_shielding", friendIsShielding());
		map.put("enemy_casting", opponentIsCasting());
		map.put("high_interrupt_chance", highInterruptChance());
		map.put("medium_interrupt_chance", mediumInterruptChance());
		map.put("low_interrupt_chance", lowInterruptChance());
		map.put("no_interrupt_chance", noInterruptChance());
		map.put("low_health", hasLowHealth());
		map.put("high_health", isHealthy());
		map.put("alone", isAlone());
		map.put("enemy_has_wall", opponentHasWall());
		map.put("is_protected", isProtected());
		map.put("can_protect", canProtect());
		map.put("can_protect_with_move", canProtectFriendWithMove());
		map.put("protected_by_move", wouldBeProtectedWithMove());
		map.put("maybe_protected_by_move", canBeProtectedWithMove());
		map.put("could_be_blocked", projectileCouldBeBlocked());
		map.put("one_enemy_left", oneEnemyLeft());
		map.put("has_shield", hasShield());
		map.put("has_single", hasSingle());
		map.put("single_in_range", singleInRange());
		map.put("ally_safe", nextFriendSafe());
		map.put("ally_can_hit_enemy", nextFriendCanHit());
		map.put("ally_almost_done_waiting", friendWillEnterSoon());
		map.put("can_interrupt_enemy", canInterruptEnemy());
		map.put("first_move", firstMove());
		map.put("not_hastened", notHastened());
		map.put("default", true);
		
//		System.out.println("----------------------------------------------");
//		for(String s: map.keySet()){
//			System.out.println(s + ": "+ map.get(s));
//		}
	}
	
	public boolean notHastened(){
		for(Effect e: selected.getEffects()){
			if(e.name.equals("Hasten")) return false;
		}
		return true;
	}
	
	public boolean firstMove(){
		if(!firstMove.contains(selected)){
			firstMove.add(selected);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Returns true if the selected character's current square is safe from
	 * projectiles and instant attacks
	 */
	public boolean isSafe(){
		return isSafeSquare(selected.xPosition, selected.yPosition);
	}
	
	/**
	 * Returns true if the selected character's current square is safe from 
	 * all types of attacks
	 */
	public boolean isSafeFromSingle(){
		for(Character c: enemies){
			for(Action a: c.availableActions){
				if(canAttackSquare(c, selected.xPosition, selected.yPosition, a)) return false;
			}
		}
		return true;
	}

	
	/** 
	 * Returns true if the selected character's current square is not safe
	 */
	public boolean isNotSafe(){
		return !isSafeSquare(selected.xPosition, selected.yPosition);
	}
	
	/**
	 * Returns true if the next ally to enter the cast phase is currently at a 
	 * a safe square
	 */
	public boolean nextFriendSafe(){
		Character nextFriend = findNextFriend();
		if(nextFriend == null){
			return false;
		}
		return isSafeSquare(nextFriend.xPosition, nextFriend.yPosition);
	}
	
	
	/**
	 * Returns true if the next ally to enter the cast phase can hit an enemy
	 */
	public boolean nextFriendCanHit(){
		Character nextFriend = findNextFriend();
		if(nextFriend == null){
			return false;
		}
		return canHitEnemyFrom(nextFriend, nextFriend.xPosition, nextFriend.yPosition);
	}
	
	
	/**
	 * Returns true if the selected character can hit an opponent from his 
	 * current location
	 */
	public boolean canHitOpponent(){
		return canHitEnemyFrom(selected, selected.xPosition, selected.yPosition);
	}
	
	/**
	 * Returns true if the character's quickest damaging attack can hit someone
	 */
	public boolean canHitQuickAttack(){
		Action quickest = null;
		for(Action a : selected.availableActions){
			if(a.damage > 0 && (quickest == null || a.cost < quickest.cost)){
				quickest = a;
			}
		}
		if(quickest == null) return false;
		for(Character c: enemies){
			if(quickest.hitsTarget(selected.xPosition, selected.yPosition, c.xPosition, c.yPosition, selected.leftside, board)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if there is an adjacent square to the selected character's
	 * current position from which he could hit an emeny. 
	 */
	public boolean attackSquareAdjacent(){
		int x = selected.xPosition;
		int y = selected.yPosition;
		Action quickest = null;
		for(Action a : selected.availableActions){
			if(a.damage > 0 && (quickest == null || a.cost < quickest.cost)){
				quickest = a;
			}
		}
		if(quickest == null) return false;
		if(canHitEnemyFrom(selected, x+1, y) && board.canMove(selected.leftside, x+1, y)){
			return true;
		}
		if(canHitEnemyFrom(selected, x, y+1) && board.canMove(selected.leftside, x, y+1)){
			return true;
		}
		if(canHitEnemyFrom(selected, x-1, y) && board.canMove(selected.leftside, x-1, y)){
			return true;
		}
		if(canHitEnemyFrom(selected, x, y-1) && board.canMove(selected.leftside, x, y-1)){
			return true;
		}
		return false;
	}
	
	public boolean attackSquareAdjacentSingle(){
		int x = selected.xPosition;
		int y = selected.yPosition;
		if(canHitEnemyFromSingle(selected, x+1, y) && board.canMove(selected.leftside, x+1, y)){
			return true;
		}
		if(canHitEnemyFromSingle(selected, x, y+1) && board.canMove(selected.leftside, x, y+1)){
			return true;
		}
		if(canHitEnemyFromSingle(selected, x-1, y) && board.canMove(selected.leftside, x-1, y)){
			return true;
		}
		if(canHitEnemyFromSingle(selected, x, y-1) && board.canMove(selected.leftside, x, y-1)){
			return true;
		}
		return false;
	}
	
	public boolean attackSquareAdjacentQuick(){
		int x = selected.xPosition;
		int y = selected.yPosition;
		Action quick = null;
		for(Action a : selected.availableActions){
			if(a.damage > 0 && (quick == null || a.cost < quick.cost)){
				quick = a;
			}
		}
		if(quick == null) return false;
		if(canHitEnemyFromAction(selected, x+1, y, quick) && board.canMove(selected.leftside, x+1, y)){

			return true;
		}
		if(canHitEnemyFromAction(selected, x, y+1, quick) && board.canMove(selected.leftside, x, y+1)){

			return true;
		}
		if(canHitEnemyFromAction(selected, x-1, y, quick) && board.canMove(selected.leftside, x-1, y)){

			return true;
		}
		if(canHitEnemyFromAction(selected, x, y-1, quick) && board.canMove(selected.leftside, x, y-1)){

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
		if(isSafeSquare(x+1, y) && board.canMove(selected.leftside, x+1, y)){
			return true;
		}
		if(isSafeSquare(x, y+1) && board.canMove(selected.leftside, x, y+1)){
			return true;
		}
		if(isSafeSquare(x-1, y) && board.canMove(selected.leftside, x-1, y)){
			return true;
		}
		if(isSafeSquare(x, y-1) && board.canMove(selected.leftside, x, y-1)){
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
		for(Character c: enemies){
			if(c.hasShield()){
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
				if(c.castPosition > c.actionBar.getCastPoint()){
					return true;
				}
				else{
					int framesLeft = (int) ((c.actionBar.getCastPoint() - c.castPosition) / c.getSpeed());
					int slots = fastestMoveThatCanHit(c);
					int framesToCast = (int) ((selected.getInterval() * slots) / selected.getSpeed());
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
	 * True if the selected character has a shield action
	 */
	public boolean hasShield(){
		for(Action a: selected.availableActions){
			if(a.pattern == Pattern.SHIELD){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * True if the selected character has a single-square action
	 */
	public boolean hasSingle(){
		for(Action a: selected.availableActions){
			if(a.pattern == Pattern.SINGLE){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the selected character has a single square action
	 * in range of hitting an opponent
	 */
	public boolean singleInRange(){
		for(Action a: selected.availableActions){
			if(a.pattern == Pattern.SINGLE){
				for(Character c: enemies){
					if(a.hitsTarget(selected.xPosition, selected.yPosition, 
							c.xPosition, c.yPosition, selected.leftside, board)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if the selected character is currently behind another
	 * character's shield.
	 */
	public boolean isProtected(){

		List<Coordinate> list = shields.leftShieldedCoordinates;
		for(Coordinate coord: list){
			int x1 = selected.leftside? selected.xPosition : coord.x;
			int x2 = selected.leftside? coord.x : selected.xPosition;
			if(coord.y == selected.yPosition && x1 <= x2){
				return true;
			}
		}
		list = shields.rightShieldedCoordinates;
		for(Coordinate coord: list){
			int x1 = selected.leftside? selected.xPosition : coord.x;
			int x2 = selected.leftside? coord.x : selected.xPosition;
			if(coord.y == selected.yPosition && x1 <= x2){
				return true;
			}
		}
	
		return false;
		
	}
	
	
	/**
	 * Returns true if the selected character can protect an ally with a shield
	 */
	public boolean canProtect(){
		for(Character c: friends){
			int x1 = selected.leftside? c.xPosition : selected.xPosition;
			int x2 = selected.leftside? selected.xPosition : c.xPosition;
			if(x1 <= x2 && Math.abs(selected.yPosition - c.yPosition) <= 1){
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
			int x1 = selected.leftside? selected.xPosition : c.xPosition;
			int x2 = selected.leftside? c.xPosition : selected.xPosition;
			if(x1 <= x2 && Math.abs(selected.yPosition - c.yPosition) <= 1){
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
			int x1 = selected.leftside? selected.xPosition : c.xPosition;
			int x2 = selected.leftside? c.xPosition : selected.xPosition;
			if(x1 <= x2+1 && Math.abs(c.yPosition - selected.yPosition) <= 1){
				return true;
			}
			if(x1 <= x2 && Math.abs(c.yPosition - selected.yPosition) <= 2){
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
					int x1 = selected.leftside? selected.xPosition : coord.x;
					int x2 = selected.leftside? coord.x : selected.xPosition;
					if(coord.y == selected.yPosition && x1 <= x2+1){
						return true;
					}
					if(Math.abs(coord.y - selected.yPosition) <= 1 && x1 <= x2){
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
			int x1 = selected.leftside? c.xPosition : selected.xPosition;
			int x2 = selected.leftside? selected.xPosition : c.xPosition;
			if(x1 <= x2+1 && Math.abs(c.yPosition - selected.yPosition) <= 1){
				return true;
			}
			if(x1 <= x2 && Math.abs(c.yPosition - selected.yPosition) <= 2){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if there is an ally who will shortly enter their cast position
	 */
	public boolean friendWillEnterSoon(){
		Character c = findNextFriend();
		return c != null && c.castPosition < c.actionBar.getCastPoint() && c.castPosition > c.actionBar.getCastPoint() * .75f;
	}
	
	
	/**
	 * Returns true if I have an attack which can interrupt a currently casting enemy
	 */
	public boolean canInterruptEnemy(){
		for(Character c: enemies){
			if(c.castPosition > c.lastCastStart){
				int slots = quickestAction(c);
				int theirFrames = framesToAction(c, slots);
				int mySlots = fastestMoveThatCanHit(c);
				int myFrames = (int) ((c.getInterval() * mySlots)/ c.getSpeed());
				if(myFrames < theirFrames){
					return true;
				}
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
		if(c.castPosition < c.actionBar.getCastPoint()){
			int waitFrames = (int) ((c.actionBar.getCastPoint() - c.castPosition) / c.getSpeed());
			int castFrames = (int) ((c.getInterval() * slots) / c.getSpeed());
			return waitFrames + castFrames;
		}
		else {
			float distance = (c.lastCastStart + (c.getInterval() * slots)) - c.castPosition;
			//System.out.println(c.name+" --- start: "+c.lastCastStart + "     position: " + c.castPosition +  "     interval: " + c.getInterval());
			return Math.max(0, (int) (distance / c.getSpeed()));
		}
	}
	
	/**
	 * Returns the number of frames until I can cast an action that costs a certain 
	 * number of slots.
	 */
	public int framesToMyAction(int slots){
		//System.out.println("interval: "+interval + " | slots: "+slots + " | speed: "+selected.getCastSpeed());
		return (int) ((selected.getInterval() * slots) / selected.getSpeed());
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
	 * Returns the number of slots of the characters slowest interrupting move
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
				minCost = Math.min(a.cost, minCost);
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
		//System.out.println(c.name+"---theirFrames: "+theirFrames +" | myFrames: "+myFrames);
		return theirFrames < myFrames;
	}
	
	
	/**
	 * Returns true if I could possibly be interrupted by any character
	 * if I decide to cast an aciton which requires "slots" number of slots
	 */
	public boolean fastInterruptible(int slots){
		for(Character c: enemies){
			int num = fastestMoveThatCanHitMe(c);
			//System.out.println(selected.name+": "+c.name+"'s fastest is "+num + " slots");
			if(num <= c.getActionBar().getTotalNumSlots() && interruptibleBy(c, slots, num)){
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
			//System.out.println(c.name);
			int num = slowestMoveThatCanHitMe(c);
			if(num >= 0 && interruptibleBy(c, slots, num)){
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
		if(!board.isInBounds(x, y)) return false;
		for(Character c: enemies){
			if(canAttackSquareNoSingle(c, x, y)) return false;
		}
		return true;
	}
	
		
	
	/**
	 * Returns true if Action a cast from (x1,y1) can hit square (x2,y2);
	 */
	public boolean canAttackSquareFrom(int x1, int y1, int x2, int y2, Action a){
		boolean leftside = x1 < board.width / 2 ? true : false;
		return a.hitsTarget(x1, y1, x2, y2, leftside ,board);
	}
	
	
	/**
	 * Returns true if the the character can hit an enemy from square (x,y)
	 */
	public boolean canHitEnemyFrom(Character c, int x, int y){
		if(!board.isInBounds(x, y)) return false;
		for(Character e: enemies){
			if(canAttackSquareNoSingle(c, x, y, e.xPosition, e.yPosition)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the the character can hit an enemy from square (x,y), including single-square attacks
	 */
	public boolean canHitEnemyFromSingle(Character c, int x, int y){
		if(!board.isInBounds(x, y)) return false;
		for(Character e: enemies){
			if(canAttackSquareWithSingle(c, x, y, e.xPosition, e.yPosition)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the the character can hit an enemy from square (x,y) with action a
	 */
	public boolean canHitEnemyFromAction(Character c, int x, int y, Action a){
		if(!board.isInBounds(x, y)) return false;
		for(Character e: enemies){
			if(a.hitsTarget(x, y, e.xPosition, e.yPosition, c.leftside, board)){
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * Returns true if Character c can attack square (x2,y2) from (x1,y1), including using a
	 * single-square attack
	 */
	public boolean canAttackSquareWithSingle(Character c, int x1, int y1, int x2, int y2){
		for(Action a: c.availableActions){
			if(canAttackSquareFrom(x1, y1, x2, y2, a)) return true;
		}
		return false;
	}
	
	
	/**
	 * Find the teammate that will reach the casting phase next.
	 */
	public Character findNextFriend(){
		Character ally = null;
		int minFrames = Integer.MAX_VALUE;
		for(Character ch: friends){
			if(ch == selected || !ch.isAI) continue; 
			int framesLeft;
			if(ch.castPosition < ch.actionBar.getCastPoint()){
				framesLeft = (int) ((ch.actionBar.getCastPoint() - ch.castPosition) / ch.getSpeed()); 
			}
			else{
				int waitFrames = (int) (ch.actionBar.getCastPoint() / ch.getSpeed());
				int castFrames = (int) ((1f - ch.castPosition) / ch.getSpeed());
				framesLeft = waitFrames + castFrames;
			}
			if(framesLeft < minFrames){
				ally = ch;
				minFrames = framesLeft;
			}
		}
		return ally;
	}
	
	
	/**
	 * Returns true if a tile (x, _ ) is on the right side of the board
	 */
	public boolean ownSide(int x){
		if(selected.leftside){
			return x >= board.width/2 ? false : true;
		}
		else{
			return x >= board.width/2 ? true : false;
		}
	}
}
