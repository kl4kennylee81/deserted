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
		
		for(Character c: characters){
			SelectionMenu menu = c.getSelectionMenu();
			if (InputController.mouseJustMoved()){
				for (Action a: menu.getActions()){
					int usableNumSlots = c.getActionBar().getUsableNumSlots();
					boolean actionInvalid = menu.isActionInvalid(usableNumSlots, a);
					if (a.contains(x,y,canvas,board) && !actionInvalid){
						hAction = a;
						currMenu = currMenu1;
						actionChar = c;
					}
				}
				
				if (c.getSelectionMenu().confirmContain(InputController.getMouseX(), InputController.getMouseY())){
					menu.setChoosingTarget(false);
					menu.selectedAction=menu.getActions().length;
				}
			}
			
			if (c.contains(x,y,canvas,board)){
				if (highlighted != null){
					highlighted.removeHovering();
				}
				highlighted = c;
				highlighted.setHovering();

				if (InputController.leftMouseClicked) {
					clickedChar = c;
				}
			}
		}
		

		if (clickedChar != null){
			for (Character c : characters) {
				c.isClicked = false;
			}	
			clickedChar.isClicked = true;
		}
		
		if (hAction != null 
				&& ((currMenu.getSelectedAction() == null && !currMenu.isActionInvalid(actionChar.getActionBar().getUsableNumSlots(), hAction))
						||currMenu.getActions().length > currMenu.selectedAction && hAction!= currMenu.getSelectedAction())){
			currMenu.setChoosingTarget(false);
			currMenu.setSelectedAction(hAction.position);
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
		screen.addCurrentHighlight(x, y, x_width, y_width);
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
