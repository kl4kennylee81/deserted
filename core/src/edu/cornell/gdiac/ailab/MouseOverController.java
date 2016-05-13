package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.ActionNode;

public class MouseOverController {
	
	Character highlighted;
	GameCanvas canvas;
	HighlightScreen screen;
	GridBoard board;
	Action hAction;
	SelectionMenu currMenu;
	
	private static boolean DISABLE_MOUSE = false;

	public void update(Option[] options, Menu Menu){
		if (!InputController.mouseJustMoved()) return;
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for (int i =0;i<options.length;i++){
			Option o = options[i];
			if (o.contains(x,y,canvas,board)){
				Menu.setOption(i);
			}
		}
		
	}
	
	public void update(Option[] options, Menu Menu, boolean reset){
		if (!InputController.mouseJustMoved()) return;
		if (reset){
			Menu.reset();
		}
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for (int i =0;i<options.length;i++){
			Option o = options[i];
			if (o.contains(x,y,canvas,board)){
				Menu.setOption(i);
			}
		}
	}

	public void update(SelectionMenu currMenu1,Characters characters){
		if (DISABLE_MOUSE){
			return;
		}
		hAction = null;
		currMenu = null;
		if (currMenu1 != null){
			currMenu1.updateActionInfo(canvas);
		}
		// remove last frames highlight
		if (highlighted != null){
			highlighted.removeHovering();
			highlighted = null;
		}
		// find this frames highlight
		screen.removeHighlight();
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		

		Character clickedChar = null;
		Character actionChar = null;
		
		boolean isHovering = false;
		
		for(Character c: characters){
			SelectionMenu menu = c.getSelectionMenu();
			
			// this will check if mouse is hovering over any action
			if (!menu.getChoosingTarget() && (c.isSelecting || c.isClicked)){
				Option[] options = menu.getOptions();
				for (int i =0;i<options.length;i++){
					Option o = options[i];
					o.currentlyHovered = false;
					if (o.contains(x,y,canvas,board)){
						isHovering = true;
					}
				}
			}
			
			if (InputController.mouseJustMoved()){
				
				if (!menu.getChoosingTarget() && (c.isClicked || (c.isSelecting && characters.clickedCharExists() == null))){
					Option[] options = menu.getOptions();
					for (int i =0;i<options.length;i++){
						Option o = options[i];
						o.currentlyHovered = false;
						if (o.contains(x,y,canvas,board)){
							menu.trySelectingAction(c.getActionBar(),i,c.isSelecting);
							actionChar = c;
							o.currentlyHovered = true;
							//menu.setChoosingTarget(false);
							//menu.selectedAction = i;
						}
					}
				}
				
				
				/*if (c.getSelectionMenu().confirmContain(InputController.getMouseX(), InputController.getMouseY())){
					
					menu.setChoosingTarget(false);
					menu.selectedAction=menu.getActions().length;
				}*/
			}
		}
		for (Character c : characters){
			if (c.contains(x,y,canvas,board)){
				if (highlighted != null){
					highlighted.removeHovering();
				}
				highlighted = c;
				highlighted.setHovering();
				
				if (InputController.leftMouseClicked && !isHovering && currMenu1 != null
						&& !currMenu1.getChoosingTarget() && actionChar == null) {
					clickedChar = c;
					c.selectionMenu.selectedAction = -1;
				}
			}
		}

		if (clickedChar != null){
			for (Character c : characters) {
				c.isClicked = false;
			}	
			clickedChar.isClicked = true;
			if (!clickedChar.selectionMenu.equals(currMenu1) && currMenu1 != null && !currMenu1.getChoosingTarget()){
				currMenu1.selectedAction = -1;
			}
		}
		
		if (hAction != null 
				&& ((currMenu.getSelectedAction() == null && !currMenu.isActionInvalid(actionChar.getActionBar().getUsableNumSlots(), hAction))
						||currMenu.getActions().length > currMenu.selectedAction && hAction!= currMenu.getSelectedAction())){
			currMenu.setChoosingTarget(false);
			currMenu.setSelectedAction(hAction.position);
		}
		
		for (Character c : characters){
			for (Option o : c.actionBar.actionOptions){
				o.currentlyHovered = false;
				if (o.contains(x, y, canvas, board)){
					o.currentlyHovered = true;
				}
			}
		}
		
	}
	
	
	public void init(HighlightScreen screen, GridBoard board){
		this.screen = screen;
		this.board = board;
	}
	
	public MouseOverController(GameCanvas canvas){
		this.canvas = canvas;
	}
	
	public void updateScreen(){
		int x = (int)highlighted.getXMin(canvas, board);
		int y = (int)highlighted.getYMin(canvas, board);
		int x_width = (int)(highlighted.getXMax(canvas, board) - highlighted.getXMin(canvas, board));
		int y_width = (int)(highlighted.getYMax(canvas, board) - highlighted.getYMin(canvas, board));
		CurrentHighlight current = new CurrentHighlight(x, y, x_width, y_width, "down", false, false);
		screen.addCurrentHighlight(current);
	}

	public void clearAll() {
		if (highlighted != null){
			highlighted.removeHovering();
			highlighted = null;
		}
		screen.removeHighlight();
	}
	
	public boolean isCharacterHighlighted(){
		return highlighted != null;
	}
	
	public static void setCanvas(GameCanvas canvas) {
		InputController.canvas = canvas;
		
	}

}
