package edu.cornell.gdiac.ailab;

public class ActionNode {
	Action action;
	float executePoint;
	
	//target tile
	int xPosition;
	int yPosition;
	
	int castPoint;
	float curX;
	float curY;
	
	boolean isInterrupted;
	
	public ActionNode(Action action, float executePoint, int xPos, int yPos){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
	}
	
	public void setPersisting(int castPoint, int curX, int curY){
		this.castPoint = castPoint;
		this.curX = curX;
		this.curY = curY;
	}
	
	public int getCurrentX(){
		return Math.round(curX);
	}
	
	public int getCurrentY(){
		return Math.round(curY);
		
	}
	
}
