package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class ActionBar {
	//conversion from length to pixels
	private float CONVERSION = 2.3f;
	float castPoint = 0.7f;
	
	public void draw(GameCanvas canvas){
		canvas.drawActionBar(100,700,castPoint);
		for (int i = 0; i < 3; i++){
			canvas.drawBox(563 + i*45, 700, 4, 20, Color.BLACK);
		}
	}
	
	public void update(){
		
	}
	
}
