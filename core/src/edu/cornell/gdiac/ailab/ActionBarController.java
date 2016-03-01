package edu.cornell.gdiac.ailab;

import java.util.List;

public class ActionBarController {
	//TEMP
	GridBoard board;
	List<Character> characters;
	ActionBar bar;
	boolean isSelection;
	boolean isAttack;
	
	public ActionBarController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		this.isSelection = false;
	}
	
	public void update(){
		this.isSelection = false;
		this.isAttack = false;
		for (Character c : characters){
			c.castPosition %= 1;
			if (c.castPosition > bar.castPoint){
				c.castPosition += c.castSpeed;
			} else {
				c.castPosition += c.speed;
			}
			if (c.castPosition >= bar.castPoint && c.castPosition-c.castSpeed < bar.castPoint) {
				c.needsSelection = true;
				this.isSelection = true;
			}
			if (c.hasAttacks() && c.castPosition >= c.getNextCast()){
				c.needsAttack = true;
				this.isAttack = true;
			}
		}
		
	}
	
	public boolean isAttack() {
		return isAttack;
	}
	
	public boolean isSelection() {
		return isSelection;
		
	}
}
