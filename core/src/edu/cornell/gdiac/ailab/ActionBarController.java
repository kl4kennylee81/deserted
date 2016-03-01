package edu.cornell.gdiac.ailab;

import java.util.List;

public class ActionBarController {
	//TEMP
	GridBoard board;
	List<Character> characters;
	ActionBar bar;
	boolean isPlayerSelection;
	boolean isAISelection;
	boolean isAttack;
	
	public ActionBarController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
	}
	
	public void update(){
		this.isPlayerSelection = false;
		this.isAISelection = false;
		this.isAttack = false;
		for (Character c : characters){
			c.castPosition %= 1;
			float oldCast = c.castPosition;
			if (c.castPosition > bar.castPoint){
				c.castPosition += c.castSpeed;
			} else {
				c.castPosition += c.speed;
			}
			
			if (c.castPosition >= bar.castPoint && oldCast < bar.castPoint) {
				c.needsSelection = true;
				if (c.isAI){
					this.isAISelection = true;
				} else {
					this.isPlayerSelection = true;
				}
			} else if (c.hasAttacks() && c.castPosition >= c.getNextCast()){
				c.needsAttack = true;
				this.isAttack = true;
			} else if (!c.hasAttacks() && c.castPosition >= bar.castPoint) {
				//maybe reset once done with attacks
				c.castPosition %= bar.castPoint;
			}
		}
	}
}
