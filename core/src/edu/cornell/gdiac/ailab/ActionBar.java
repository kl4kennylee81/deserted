package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class ActionBar {
	public float castPoint;
	
	public ActionBar(){
		this.castPoint = 0.7f;
	}
	
	public void draw(GameCanvas canvas){
		canvas.drawActionBar(100,700,castPoint);
		for (int i = 0; i < 3; i++){
			canvas.drawBox(563 + i*45, 700, 4, 20, Color.BLACK);
		}
	}
}
