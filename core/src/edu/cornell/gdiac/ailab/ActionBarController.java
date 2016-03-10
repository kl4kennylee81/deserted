package edu.cornell.gdiac.ailab;

import java.util.List;

public class ActionBarController {
	
	/** Models */
	List<Character> characters;
	ActionBar bar;
	
	/** State variables */
	boolean isPlayerSelection;
	boolean isAISelection;
	boolean isAttack;
	
	public ActionBarController(List<Character> chars, ActionBar bar) {
		this.characters = chars;
		this.bar = bar;
	}
	
	public void update(){
		this.isPlayerSelection = false;
		this.isAISelection = false;
		this.isAttack = false;
		for (Character c : characters){
			if (!c.isAlive()){
				continue;
			}
			c.castPosition %= 1;
			float oldCast = c.castPosition;
			
			// Increase characters cast position by their normal speed or cast speed
			if (c.castPosition > bar.castPoint){
				c.castPosition += c.castSpeed;
			} else {
				c.castPosition += c.speed;
			}
			
			if (c.castPosition >= bar.castPoint && oldCast < bar.castPoint) {
				// Let characters select their attacks
				c.needsSelection = true;
				
				// this is for when a unit tries to move into another units square
				// we have this flag which stops all future movements (temporary)	
				// when he starts his next round this flag is switched to false.
				c.isBlocked = false;
				
				if (c.isAI){
					this.isAISelection = true;
				} else {
					this.isPlayerSelection = true;
				}
			} else if (c.hasAttacks() && c.castPosition >= c.getNextCast()){
				// Character uses action
				c.needsAttack = true;
				this.isAttack = true;
			} else if (!c.hasAttacks() && c.castPosition >= bar.castPoint) {
				// Reset once done with attacks
				c.castPosition = 0;
			}
		}
	}
}
