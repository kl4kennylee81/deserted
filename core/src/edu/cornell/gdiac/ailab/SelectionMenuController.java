package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;

public class SelectionMenuController {
	//Do a lot more
	//TEMP
	GridBoard board;
	List<Character> characters;
	Character selected;
	ActionBar bar;
	InputController controls;
	boolean isDone;
	boolean choosingTarget;
	
	public SelectionMenuController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		controls = new PlayerController();
		this.isDone = false;
		this.selected = null;
		this.choosingTarget = false;
	}
	
	
	/** need SelectionMenu helper functions */
	public void update(){
		controls.getAction();
		if (selected != null){
			SelectionMenu menu = selected.selectionMenu;
			if (!choosingTarget){
				if (controls.pressedEnter()){
					selected.isSelecting = false;
					selected = null;
				} else if (controls.pressedA()){
					Action action = menu.actions[menu.selectedAction];
					if (action.pattern == Pattern.STRAIGHT){
						menu.add(new ActionNode(action,0.8f,0,0));
					} else {
						//TODO: DEAL WITH BITCH ASS PATTERNS
						
						
					}
				} else if (controls.pressedS()){
					menu.selectedActions.pollLast();
				} else if (controls.pressedUp() && !controls.pressedDown()){
					menu.selectedAction -= 1;
					if (menu.selectedAction < 0){
						menu.selectedAction += 4;
					}
				} else if (controls.pressedDown() && !controls.pressedUp()){
					menu.selectedAction += 1;
					menu.selectedAction %= 4;
				}
			} else {
			}
			
		} else {
			isDone = true;
			for (Character c : characters){
				if (c.needsSelection){
					isDone = false;
					selected = c;
					c.needsSelection = false;
					c.isSelecting = true;
					break;
				}
			}
		}
		
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}
