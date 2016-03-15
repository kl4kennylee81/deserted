package edu.cornell.gdiac.ailab;

import java.util.List;

public class MouseOverController {
	
	public List<Character> characters;
	Character highlighted;
	GameCanvas canvas;
	HighlightScreen screen;
	
	public void update(){
		if (highlighted != null){
			highlighted.removeHovering();
			highlighted = null;
		}
		screen.removeHighlight();
		float x = InputController.getMouseX();
		float y = InputController.getMouseY();
		for(Character c: characters){
			float x_min = c.getXMin();
			float x_max = c.getXMax();
			float y_min = c.getYMin();
			float y_max = c.getYMax();
			if (x <= x_max && x >= x_min && y <= y_max && y >= y_min){
				highlighted = c;
			}
		}
		draw(x, y);
	}
	
	public MouseOverController(List<Character> chars, HighlightScreen screen){
		this.characters = chars;
		this.screen = screen;
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
		int x = (int)highlighted.getXMin();
		int y = (int)highlighted.getYMin();
		int x_width = (int)(highlighted.getXMax() - highlighted.getXMin());
		int y_width = (int)(highlighted.getYMax() - highlighted.getYMin());
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

}
