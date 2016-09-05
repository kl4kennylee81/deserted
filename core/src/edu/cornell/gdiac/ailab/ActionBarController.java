package edu.cornell.gdiac.ailab;

import java.util.List;

public class ActionBarController {
	
	/** Models */
	List<Character> characters;
	
	/** State variables */
	boolean isPlayerSelection;
	boolean isNetworkingOpponentSelection;
	boolean isAISelection;
	boolean isAttack;
	boolean characterSelectionNeeded;
	boolean selectingFirst;
	
	int turnsCompleted;
	
	public ActionBarController(List<Character> chars) {
		this.characters = chars;
		turnsCompleted = 0;
	}
	
	public void update(){
		this.isPlayerSelection = false;
		this.isNetworkingOpponentSelection = false;
		this.isAISelection = false;
		this.isAttack = false;
		this.characterSelectionNeeded = false;
		this.selectingFirst = false;
		for (Character c : characters){
			if (!c.isAlive()){
				continue;
			}
			if (c.castPosition > 1){
				c.resetCastPosition();
				if (c.leftside){
					turnsCompleted++;
				}
			}
			float oldCastPosition = c.castPosition;
			
			// Increase characters cast position by their normal speed or cast speed
			// will remove castMoved and just do it by castSpeed
			c.castMoved = c.getSpeed();
			
			c.castPosition += c.castMoved;
			
			if (c.castPosition >= c.getCastPoint() && oldCastPosition < c.getCastPoint()) {
				// Let characters select their attacks
				if (c.getQueuedActions().isEmpty()){
					c.needsSelection();
					c.startingCast();
					if (c.isAI){
						this.isAISelection = true;
					} else if (c.isNetworkingOpponent){
						if (!this.characterSelectionNeeded) { 
							this.selectingFirst = false;
							this.characterSelectionNeeded = true;
						}
						this.isNetworkingOpponentSelection = true;
					} else {
						c.needsDataOutput = true;
						if (!this.characterSelectionNeeded) { 
							this.selectingFirst = true;
							this.characterSelectionNeeded = true;
						}
						this.isPlayerSelection = true;
					}
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
//					System.out.println("SKIP ACTION BAR Controller "+c.castMoved);
				}
				
				// Reset once done with attacks
				c.resetCastPosition();
				if (c.leftside){
					turnsCompleted++;
				}
			}
		}
	}

}
