package edu.cornell.gdiac.ailab;

public class ActionNode {
	Action action;
	float executePoint;
	
	//target tile
	int xPosition;
	int yPosition;
	
	boolean isInterrupted;
	
	public ActionNode(Action action, float executePoint, int xPos, int yPos){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
	}
	
}
