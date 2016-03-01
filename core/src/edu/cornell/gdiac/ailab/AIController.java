package edu.cornell.gdiac.ailab;

import java.util.List;

public class AIController {
	public static enum Difficulty {
		EASY,
		MEDIUM,
		HARD,
		BOSS_1,
		BOSS_2,
		BOSS_3
	}
	
	GridBoard board;
	List<Character> chars;
	ActionBar bar;
	Character selected;
	
	public AIController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.chars = chars;
		this.bar = bar;
	}
	
	public void update(){
		for (Character c : chars){
			if (c.needsSelection && c.isAI){
				c.needsSelection = false;
				//TODO: some function to set c's queuedActions
				//break it up into whatever module/functions you want
			}
		}
	}
}
