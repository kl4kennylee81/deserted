package edu.cornell.gdiac.ailab;

import java.util.List;

public class MouseOverController {
	
	public List<Character> characters;
	Character highlighted;
	GameCanvas canvas;
	HighlightScreen screen;
	GridBoard board;
	
	public void update(){
		if (highlighted != null){
			highlighted.removeHovering();
			highlighted = null;
		}
		screen.removeHighlight();
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for(Character c: characters){
			float x_min = c.getXMin(canvas, board);
			float x_max = c.getXMax(canvas, board);
			float y_min = c.getYMin(canvas, board);
			float y_max = c.getYMax(canvas, board);
			float x_token_min = c.getTokenXMin(canvas, board);
			float x_token_max = c.getTokenXMax(canvas, board);
			float y_token_min = c.getTokenYMin(canvas, board);
			float y_token_max = c.getTokenYMax(canvas, board);
			
			if (x <= x_max && x >= x_min && y <= y_max && y >= y_min
					|| x <= x_token_max && x >= x_token_min && y <= y_token_max && y >= y_token_min){
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
		if (highlighted == null){
//			System.out.println("Mouse is at " + x + "," + y);
			return;
		} else{
//			System.out.println(highlighted.name + " is highlighted");
//			updateScreen();
			highlighted.setHovering();
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
