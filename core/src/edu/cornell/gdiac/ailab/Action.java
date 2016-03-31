package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class Action implements GUIElement {
	String name;
	int cost;
	int damage;
	int range;
	Pattern pattern;
	Effect effect;
	String description;
	Animation animation;
	
	TexturedMesh menuToken;
	TexturedMesh barToken;
	TexturedMesh actionEffects;
	float x;
	float y;
	float width;
	float height;
	int position;
	
	Coordinate[] path;

	public static enum Pattern {
		MOVE,
		SHIELD,
		STRAIGHT,
		DIAGONAL,
		SINGLE,
		NOP,
		PROJECTILE,
		INSTANT
	}
	
	public Action(String name, int cost, int damage, int range, Pattern pattern, Effect effect, String description){
		this.name = name;
		this.cost = cost;
		this.damage = damage;
		this.range = range;
		this.pattern = pattern;
		this.effect = effect;
		this.description = description;
	}
	
	public Action(String name, int cost, int damage, int range, Pattern pattern, Effect effect, String description, String strpath){
		this.name = name;
		this.cost = cost;
		this.damage = damage;
		this.range = range;
		this.pattern = pattern;
		this.effect = effect;
		this.description = description;
		
		if ((this.pattern == Pattern.PROJECTILE||this.pattern == Pattern.INSTANT)&&strpath!=""){
			// path string for a straight range 3 looks like this "0,0 1,0 2,0, 3,0" with 0,0 being the character current position"
			// the coordinate relative to the character will be converted to actual board coordinate when turned into an action node
			String[] stringPath = strpath.split(" ");
			this.path = new Coordinate[stringPath.length];
			Coordinates coordPool = Coordinates.getInstance();
			for (int i =0;i<stringPath.length;i++){
				String[] coord = stringPath[i].split(",");
				if (coord.length == 2){
					this.path[i] = coordPool.obtain();
					int x = Integer.parseInt(coord[0]);
					int y = Integer.parseInt(coord[1]);
					this.path[i].set(x, y);
				}
			}
		}
		else{
			this.path = new Coordinate[0];
		}
	}
	
	/** helper function you pass in the starting location of the path startX and startY
	 * returns if (targetX,targetY) is on the path trajectory
	 * @param startX: starting x position of path
	 * @param startY: starting y position of path
	 * @param targetX: target x position
	 * @param targetY: target y position
	 * @param leftside: if character is on leftside
	 * @return if (targetX,targetY) is on the path trajectory of the action
	 */
	public boolean isOnPath(int startX,int startY, int targetX,int targetY,boolean leftside){
		if (path == null||path.length <= 0){
			return false;
		}
		Coordinate[] path = this.path;
		if (leftside) {
			for (int i = 0; i < path.length; i++){
				int x = startX + path[i].x;
				int y = startX + path[i].y;
				if (x == targetX && y == targetY){
					return true;
				}
			}
			return false;
		} else {
			for (int i = 0; i < path.length; i++){
				int x = startX - path[i].x;
				int y = startX + path[i].y;
				if (x == targetX && y == targetY){
					return true;
				}
			}
			return false;
		}	
	}
	
	public void setAnimation(Animation animation){
		this.animation = animation;
	}
	
	public void setX(float x){
		this.x = x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public void setWidth(float width){
		this.width = width;
	}
	
	public void setHeight(float height){
		this.height = height;
	}

	public void setPosition(int position){
		this.position = position;
	}
	
	
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board){
//		System.out.println("x is " + x);
//		System.out.println("y is " + y);
//		System.out.println("this.x is " + this.x);
//		System.out.println("this.y is " + this.y);
//		System.out.println("this.width is " + width);
//		System.out.println("this.height is " + height);
		return (x <= this.x+this.width && x >= this.x && y <= this.y + this.height && y >= this.y);
	}
}
