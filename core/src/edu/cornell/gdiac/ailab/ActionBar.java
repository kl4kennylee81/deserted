package edu.cornell.gdiac.ailab;

public class ActionBar {
	//conversion from length to pixels
	private float CONVERSION = 2.3f;
	float castPoint = 0.7f;
	
	public void draw(GameCanvas canvas){
		canvas.drawActionBar(100,700,castPoint);
	}
	
	public void update(){
		
	}
	
}
