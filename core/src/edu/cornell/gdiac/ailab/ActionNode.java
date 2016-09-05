package edu.cornell.gdiac.ailab;

import com.google.gson.annotations.SerializedName;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.CharacterActions.MessageActionNode;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import com.google.gson.annotations.SerializedName;

public abstract class ActionNode {
	
	public enum ActionNodeType {
		@SerializedName("0")
		MESSAGE (0),
		@SerializedName("1")
		GAME (1);
		
	    private final int value;
	    public int getValue() {
	        return value;
	    }

	    private ActionNodeType(int value) {
	        this.value = value;
	    }
	}
	
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}
	
	ActionNodeType an_type;
	int executeSlot;
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
	
	public ActionNode(ActionNode an){
		this.executeSlot = an.executeSlot;
		this.isInterrupted = an.isInterrupted;
		
		this.xPosition = an.xPosition;
		this.yPosition = an.yPosition;
		
		//current info for persisting actions
		this.castPosition = an.castPosition;
		this.curX = an.curX;
		this.curY = an.curY;
		this.curRound = an.curRound;
		
		this.direction = an.direction;
		
		this.path = an.path;
		this.pathIndex = an.pathIndex;
		
		this.animation = an.animation;
		
		this.shieldHitsLeft = an.shieldHitsLeft;
	    this.hitThisRound = an.hitThisRound;
	}

	public ActionNode(Action action, int executeSlot, int xPos, int yPos){
		this.executeSlot = executeSlot;
		this.xPosition = xPos;
		this.yPosition = yPos;
		this.isInterrupted = false;
		this.pathIndex = 0;
		
		if (action.pattern == Pattern.SHIELD && action.shieldNumberHits != null){
			this.shieldHitsLeft = action.shieldNumberHits;
		}
	}
	
	public ActionNode(Action action, int executeSlot, int xPos, int yPos, Direction direction){
		this.executeSlot = executeSlot;
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
	
	public abstract Action getAction();
	
	public ActionNodeType getType(){
		return this.an_type;
	}
	
	
	public void setAnimation(){
		this.animation = new AnimationNode(this.getAction().projectileAnimation);
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
