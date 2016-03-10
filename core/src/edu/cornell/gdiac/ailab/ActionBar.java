package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class ActionBar {
	public float castPoint;
	
	public ActionBar(){
		this.castPoint = 0.7f;
	}
	
	public void draw(GameCanvas canvas){
		canvas.drawBox(50, 700, 800, 20, Color.RED);
		canvas.drawBox(50, 700, 800*castPoint, 20, Color.GREEN);
		for (int i = 0; i < 4; i++){
			canvas.drawBox(668 + i*60, 700, 4, 20, Color.BLACK);
		}
	}
}
