package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.math.MathUtils;
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
			}
		}
		/** angle needed to move from this coordinate to pos **/
		public float angleTo(Coordinate pos){
			float x = pos.x - this.x;
			float y = pos.y - this.y;
			float angle = (float)Math.atan2(y, x) * MathUtils.radiansToDegrees;
			if (angle < 0) angle += 360;
			return angle;
		}
		
		public float dist(float x,float y){
				final float x_d = x - this.x;
				final float y_d = y - this.y;
				return x_d * x_d + y_d * y_d;
		}
		
		/** point (x,y) is distance to this coordinate is within this radius **/
		public boolean inRadius(float radius,float x,float y){
			float dist = this.dist(x, y);
			if (dist <= radius){
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	
	public static int minYCoordinate(Coordinate[] coords){
		if (coords.length != 0){
			int min = coords[0].y;
			for (int i =0;i<coords.length;i++){
				if (coords[i].y<min){
					min = coords[i].y;
				}
			}
			return Math.max(min,0);
		}
		return 0;
	}
	
	public static int numWithinBounds(Coordinate[] coords,GridBoard board){
		int num = 0;
		for (int i =0;i<coords.length;i++){
			if (board.isInBounds(coords[i].x, coords[i].y)){
					num++;
			}
		}	
		return num;
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
