package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;

public class MouseOverController {
	
	Character highlighted;
	GameCanvas canvas;
	HighlightScreen screen;
	GridBoard board;
	Action hAction;
	SelectionMenu currMenu;
	
	public void update(SelectionMenu currMenu1,Characters characters){
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
		for(Character c: characters){
			for (Action a: c.getSelectionMenu().getActions()){
				if (a.contains(x,y,canvas,board)){
//					hAction = a;
//					currMenu = currMenu1;
//					currMenu.setSelectedAction(hAction.position);
				}
			}
			if (c.contains(x,y,canvas,board)){
				highlighted = c;
				highlighted.setHovering();
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
		screen.setCurrentHighlight(x, y, x_width, y_width);
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
