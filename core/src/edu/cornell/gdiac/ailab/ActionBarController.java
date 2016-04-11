package edu.cornell.gdiac.ailab;

import java.util.List;

public class ActionBarController {
	
	/** Models */
	List<Character> characters;
	
	/** State variables */
	boolean isPlayerSelection;
	boolean isAISelection;
	boolean isAttack;
	
	public ActionBarController(List<Character> chars) {
		this.characters = chars;
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
			// will remove castMoved and just do it by castSpeed
			c.castMoved = c.getSpeed();
			
			c.castPosition += c.castMoved;
			
			if (c.castPosition >= c.getCastPoint() && oldCastPosition < c.getCastPoint()) {
				// Let characters select their attacks
				c.needsSelection();
				c.startingCast();

				if (c.isAI){
					this.isAISelection = true;
				} else {
					this.isPlayerSelection = true;
				}
			} else if (c.hasAttacks() && c.castPosition >= c.getNextCast()){
				// Character uses action
				c.startingCast();
				c.needsAttack = true;
				this.isAttack = true;
			} else if (!c.hasAttacks() && c.castPosition >= c.getCastPoint()) {
				// cast moved accounts for NOPing and stopping going through cast preemptively
				if (c.castPosition < 1){
					c.castMoved += Math.abs(1.0f - c.castPosition);
				}
				
				// Reset once done with attacks
				c.castPosition = 0;
			}
		}
	}

}
