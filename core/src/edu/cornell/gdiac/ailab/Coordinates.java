package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.utils.Pool;

public class Coordinates {
	public class Coordinate implements Pool.Poolable{
		//Make a static array of coordinates
		boolean isFreed;
		int x;
		int y;
		
		public Coordinate(int x,int y){
			isFreed = false;
			this.x = x;
			this.y = y;
		}
		
		public Coordinate(){
			reset();
		}
		
		public void set(int x,int y){
			isFreed = false;
			this.x = x;
			this.y = y;			
		}
		
		@Override
		public String toString(){
			return String.format("%d,%d",x,y);
		}

		@Override
		public void reset() {
			isFreed = true;
			this.x = 0;
			this.y = 0;
			
		}
		
		public void free(){
			if (!this.isFreed){
				memory.free(this);
				System.out.println("free"+ memory.getFree());
			}
		}
	}
	
	public class CoordinatePool extends Pool<Coordinate>{
		
		public CoordinatePool(){
			super();
		}
		
		@Override
		protected Coordinate newObject() {
			return new Coordinate();
		}
	}
	
	/** singleton Pool of Coordinates **/
	private static Coordinates PoolCoordinates = null;
	
	private Pool<Coordinate> memory;
	
	/** constructor for coordinates **/
	public Coordinates(){
		memory = new CoordinatePool();
	}
	
	public static Coordinates getInstance(){
		if (PoolCoordinates == null){
			PoolCoordinates = new Coordinates();
		}
		return PoolCoordinates;
	}
	
	public Coordinate obtain(){
		return memory.obtain();
	}
	
	public Coordinate newCoordinate(int x,int y){
		Coordinate c = this.obtain();
		c.set(x, y);
		return c;
	}
}
