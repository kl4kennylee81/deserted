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
			float oldCastPosition = c.castPosition;
			
			// Increase characters cast position by their normal speed or cast speed
			if (c.castPosition > ActionBar.castPoint){
				c.castMoved = c.getCastSpeed();
			} else {
				c.castMoved = c.getBarSpeed();
			}
			
			c.castPosition += c.castMoved;
			
			if (c.castPosition >= ActionBar.castPoint && oldCastPosition < ActionBar.castPoint) {
				// Let characters select their attacks
				c.needsSelection();
				c.startingCast();
				if (c.isAI){
					this.isAISelection = true;
				} else {
					c.needsDataOutput = true;
					this.isPlayerSelection = true;
				}
			} else if (c.hasAttacks() && c.castPosition >= c.getNextCast()){
				// Character uses action
				c.startingCast();
				c.needsAttack = true;
				this.isAttack = true;
			} else if (!c.hasAttacks() && c.castPosition >= ActionBar.castPoint) {
				// Reset once done with attacks
				c.castPosition = 0;
			}
		}
	}
}
