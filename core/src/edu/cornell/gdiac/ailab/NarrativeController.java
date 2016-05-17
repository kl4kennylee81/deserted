package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class NarrativeController {
	private boolean isDone;
	public boolean isPre;
	private Narrative narrative;
	
	public void reset(String narrativeFileName, boolean isPre){
		this.isPre = isPre;
		this.isDone = false;
		
		narrative = new Narrative(narrativeFileName);
	}
	
	public void update(){
		if (InputController.pressedEnter() || InputController.pressedLeftMouse()){
			narrative.index++;
			if (narrative.index >= narrative.panels.size()){
				isDone = true;
			}
		}
	}
	
	public void draw(GameCanvas canvas){
		narrative.draw(canvas);
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}
