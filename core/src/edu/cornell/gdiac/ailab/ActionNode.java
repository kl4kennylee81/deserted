package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class ActionNode {
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}
	
	Action action;
	float executePoint;
	boolean isInterrupted;
	
	//target tile for single attacks
	int xPosition;
	int yPosition;
	
	//current info for persisting actions
	float castPosition;
	float curX;
	float curY;
	float curRound;
	
	//direction for move, diagonal, shield
	Direction direction;
	
	// a path of the trajectory
	Coordinate[] path;
	int pathIndex;
	
	//projectile animation
	AnimationNode animation;
	
	int shieldHitsLeft;
	boolean hitThisRound;
	
	public ActionNode(Action action, float executePoint, int xPos, int yPos){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
		this.pathIndex = 0;
		
		if (action.pattern == Pattern.SHIELD && action.shieldNumberHits != null){
			this.shieldHitsLeft = action.shieldNumberHits;
		}
	}
	
	public ActionNode(Action action, float executePoint, int xPos, int yPos, Direction direction){
		this.action = action;
		this.executePoint = executePoint;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
		this.direction = direction;
		this.pathIndex = 0;
		
		if (action.pattern == Pattern.SHIELD && action.shieldNumberHits != null){
			this.shieldHitsLeft = action.shieldNumberHits;
		}
	}
	
	public void setPersisting(float castPosition, int curX, int curY){
		this.castPosition = castPosition;
		this.curX = curX;
		this.curY = curY;
		curRound = 0;
	}
	
	public void setPersisting(float castPosition,int curX,int curY,Coordinate[] path){
		this.castPosition = castPosition;
		this.curX = curX;
		this.curY = curY;
		this.path = path;
		curRound = 0;
	}
	
	public void setAnimation(){
		this.animation = new AnimationNode(this.action.projectileAnimation);
	}

	public int getCurrentX(){
		return Math.round(curX);
	}
	
	public int getCurrentY(){
		return Math.round(curY);
		
	}
	
	public Coordinate getLastInPath(){
		if (this.path != null&&this.path.length > pathIndex -1 && pathIndex -1 >= 0){
			return this.path[pathIndex-1];
		}
		else{
			return null;
		}			
	}
	
	public Coordinate getCurInPath(){
		if (this.path != null&&this.path.length > pathIndex){
			return this.path[pathIndex];
		}
		else{
			return null;
		}
	}
	
	public Coordinate getNextInPath(){
		if (this.path != null && this.path.length > pathIndex+1){
			return this.path[pathIndex+1];
		}
		else{
			return null;
		}
	}
}
