package edu.cornell.gdiac.ailab;

import java.util.AbstractSequentialList;

import com.badlogic.gdx.utils.Pool;

public class ActionNodes{
	
	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NONE
	}
	
	public class ActionNode implements Pool.Poolable {
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
		
		// boolean if it has already been freed
		boolean isFreed;
		
		// a path of the trajectory
		//TODO
		public ActionNode(){
			reset();
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
		
		public void setActionNode(Action action, float executePoint, int xPos, int yPos){
			this.action = action;
			this.executePoint = executePoint;
			this.xPosition = xPos;
			this.yPosition = yPos;
			this.isInterrupted = false;
			this.isFreed = false;
		}
		
		public void setActionNode(Action action, float executePoint, int xPos, int yPos, Direction direction){
			this.action = action;
			this.executePoint = executePoint;
			this.xPosition = xPos;
			this.yPosition = yPos;
			this.isInterrupted = false;
			this.direction = direction;
			this.isFreed = false;
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

		@Override
		public void reset() {
			action = null;
			executePoint = 0;
			isInterrupted = true;
			
			//target tile for single attacks
			xPosition = 0;
			yPosition = 0;
			
			//current info for persisting actions
			castPoint = 0;
			curX = 0;
			curY = 0;
			direction = Direction.NONE;
			this.isFreed = true;
		}
		
		public void free() {
			if (!this.isFreed){
				memory.free(this);
				System.out.println("freed size "+memory.getFree());
			}
		}
	}

	private class ActionNodePool extends Pool<ActionNode>{
		
		public ActionNodePool(){
			super();
		}
		
		// allocate
		@Override
		protected ActionNode newObject() {
			// TODO Auto-generated method stub
			return new ActionNode();
		}
	}
	
	private Pool<ActionNode> memory;
	
	private int size;
	
	public ActionNodes(){
		memory = new ActionNodePool();
		size = 0;
	}
	
	public ActionNode obtain(){
		System.out.println(memory.getFree());
		return memory.obtain();
	}
	
	public ActionNode newActionNode(Action action, float executePoint, int xPos, int yPos){
		ActionNode an = obtain();
		an.setActionNode(action, executePoint, xPos, yPos);
		return an;
	}
	
	public ActionNode newActionNode(Action action, float executePoint, int xPos, int yPos, Direction direction){
		ActionNode an = obtain();
		an.setActionNode(action, executePoint, xPos, yPos, direction);
		return an;
	}
	
	

}
