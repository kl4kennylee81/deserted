package edu.cornell.gdiac.ailab;


import com.badlogic.gdx.utils.Pool;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

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
		float castPosition;
		float curX;
		float curY;
		float curRound;
		
		//direction for move, diagonal, shield
		Direction direction;
		
		// boolean if it has already been freed
		boolean isFreed;
		
		// a path of the trajectory
		Coordinate[] path;
		int pathIndex = 0;
		
		public ActionNode(){
			reset();
		}
		
		public void setActionNode(Action action, float executePoint, int xPos, int yPos){
			this.action = action;
			this.executePoint = executePoint;
			this.xPosition = xPos;
			this.yPosition = yPos;
			this.isInterrupted = false;
			this.isFreed = false;
			this.pathIndex = 0;
		}
		
		public void setActionNode(Action action, float executePoint, int xPos, int yPos, Direction direction){
			this.action = action;
			this.executePoint = executePoint;
			this.xPosition = xPos;
			this.yPosition = yPos;
			this.isInterrupted = false;
			this.direction = direction;
			this.isFreed = false;
			this.pathIndex = 0;
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
			
			for (int i =0;i<path.length;i++){
				System.out.println(path[i]);
			}
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
			castPosition = 0;
			curX = 0;
			curY = 0;
			curRound = 0;
			direction = Direction.NONE;
			this.isFreed = true;
			this.path = null;
			this.pathIndex = 0;
		}
		
		public void free() {
			if (!this.isFreed){
				memory.free(this);
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
	
	/** this makes the actionNodes into a singleton **/
	private static ActionNodes PoolActionNodes = null;
	
	private Pool<ActionNode> memory;
	
	public static ActionNodes getInstance() {
		if (PoolActionNodes == null) {
			PoolActionNodes = new ActionNodes();
		}
		return PoolActionNodes;
	}
	
	public ActionNodes(){
		memory = new ActionNodePool();
	}
	
	public ActionNode obtain(){
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
