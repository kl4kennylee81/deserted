package edu.cornell.gdiac.ailab;

import java.util.List;

public class PersistingController {
	GridBoard board;
	List<Character> characters;
	Character selected;
	ActionBar bar;
	
	public PersistingController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		
		this.selected = null;
	}
	
	public void update(){
		//should first sort characters by cast time
		for (Character c : characters){
			if (c.hasPersisting()){
				List<ActionNode> actionNodes = c.getPersistingActions();
				for (ActionNode an : actionNodes){
					//execute specific action node
					an.castPoint += 1;
					if (an.castPoint >= ((PersistingAction) an.action).castLength){
						c.popPersistingCast(an);
					}
				}
			}
		}
	}
}
