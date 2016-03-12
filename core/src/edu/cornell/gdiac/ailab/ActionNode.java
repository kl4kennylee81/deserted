package edu.cornell.gdiac.ailab;

public class ActionNode {
	Action action;
	float executePoint;
	boolean isInterrupted;
	
	//target tile for single attacks
	int xPosition;
	int yPosition;
	
	//current info for persisting actions
	int castPoint;
	float curX;
	float curY;
	
	//direction for move, diagonal, shield
	Direction direction;
	
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}
	
	//Get rid of this and use the bottom constructor.
	public ActionNode(Action action, float executePoint, int xPos, int yPos){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
	}
	
	public ActionNode(Action action, float executePoint, int xPos, int yPos, Direction direction){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
		this.direction = direction;
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
