package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import java.util.List;

public class ConditionalManager extends TacticalManager {
	public HashMap<String, Boolean> map;
	
	public static final String[] strings = {
		"IsSafe"
	};

	
	private List<Character> chars;
	private ActionBar bar;
	private GridBoard board;
	private Character selected;
	
	public void update(GridBoard board, List<Character> chars, ActionBar bar, Character c){
		this.chars = chars;
		this.bar = bar;
		this.board = board;
		selected = c;
		map = new HashMap<String, Boolean>();
	}
	
	public void isSafe(){
		
	}
	
	public void isNotSafe(){
		
	}
	
	
	public void canHitOpponent(){
		
	}
	
	public void attackSquareAdjacent(){
		
	}
	
	public void safeSquareAdjacent(){
		
	}
	
	public void canHitMultipleOpponents(){
		
	}
	
	public void friendIsCasting(){
		
	}
	
	public void friendIsShielding(){
		
	}
	
	public void opponentIsCasting(){
		
	}
	
	public void opponentIsAttackingMe(){
		
	}
	
	public void opponentIsAttackingFriend(){
		
	}
	
	public void highInterruptChance(){
		
	}
	
	public void mediumInterruptChance(){
		
	}
	
	public void lowInterruptChance(){
		
	}
	
	public void noInterruptChance(){
		
	}
	
	public void hasLowHealth(){
		
	}
	
	public void isHealthy(){
		
	}
	
	public void isAlone(){
		
	}
	
	public void opponentHasWall(){
		
	}
	
	public void opponentIsWeak(){
		
	}
	
	public void opponentIsSlowed(){
		
	}
	
	public void isProtected(){
		
	}
	
	public void canProtect(){
		
	}
	
	public void canBeProtected(){
		
	}
	
	public void canBeProtectedWithMove(){
		
	}
	
	public void canProtectFriendWithMove(){
		
	}
	
	
	

}
