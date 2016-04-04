package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;

public class MouseOverController {
	
	public List<Character> characters;
	Character highlighted;
	GameCanvas canvas;
	HighlightScreen screen;
	GridBoard board;
	Action hAction;
	SelectionMenu currMenu;
	
	private static final float TEXT_ACTION_OFFSET = 30f;
	
	public void update(Option[] options, MainMenu mainMenu){
		if (!InputController.mouseJustMoved()){
			return;
		}
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for(Option o: options){
			if (o.contains(x,y,canvas,board)){
				mainMenu.selectOption(o.srNo);
			}
		}
		
	}
	public void update(SelectionMenu currMenu1){
		if (!InputController.mouseJustMoved()){
			return;
		}
		hAction = null;
		currMenu = null;
		if (currMenu1 != null){
			currMenu1.updateActionInfo(canvas);
		}
		if (highlighted != null){
			highlighted.removeHovering();
			highlighted = null;
		}
		screen.removeHighlight();
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for(Character c: characters){
			for (Action a: c.getSelectionMenu().getActions()){
				if (a.contains(x,y,canvas,board)){
					hAction = a;
					currMenu = currMenu1;
				}
			}
			if (c.contains(x,y,canvas,board)){
				highlighted = c;
			}
		}
		draw(x, y);
	}
	
	public void init(List<Character> chars, HighlightScreen screen, GridBoard board){
		this.characters = chars;
		this.screen = screen;
		this.board = board;
	}
	
	public MouseOverController(GameCanvas canvas){
		this.canvas = canvas;
	}
	
	public void draw(float x, float y){
		if (highlighted == null && hAction == null){
//			System.out.println("Mouse is at " + x + "," + y);
			return;
		} else { 
			if (highlighted != null){
//				System.out.println(highlighted.name + " is highlighted");
//				updateScreen();
				highlighted.setHovering();
				ArrayList<ActionNode> toDisplay = new ArrayList<ActionNode>(highlighted.getSelectionMenu().getQueuedActions());
				float actionSlot_x = ActionBar.getBarCastPoint(canvas);
				float actionSlot_y = ActionBar.getBarY(canvas);
				
				float slot_width = ActionBar.getSlotWidth(canvas);
				int offset = 0;
				for (ActionNode a: toDisplay){
					float x_pos = actionSlot_x + offset + (slot_width*a.action.cost/2);
					float y_pos = actionSlot_y - TEXT_ACTION_OFFSET;
					canvas.drawCenteredText(highlighted.isAI ? "?" : a.action.name, x_pos, y_pos, Color.BLACK);
					offset+=slot_width*a.action.cost;
				}
			}
			if (hAction != null){
//				System.out.println(hAction.name);
				currMenu.setSelectedAction(hAction.position);
			}
		}
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
