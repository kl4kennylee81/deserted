package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class Action implements GUIElement {
	String name;
	int cost;
	private int damage;
	int range;
	/** horizontal width of action, used for projectiles*/
	int size;
	boolean oneHit;
	boolean canBlock;
	boolean needsToggle;
	boolean isBuff;
	boolean notSymmetric;
	Pattern pattern;
	Effect effect;
	String description;
	Animation animation;
	Animation projectileAnimation;
	Integer shieldNumberHits;
	
	Texture menuIcon;
	Texture barIcon;
	Texture actionEffects;
	float x;
	float y;
	float width;
	float height;
	int position;
	
	Coordinate[] path;
	
	Color shieldColor0;
	Color shieldColor1;

	public static enum Pattern {
		MOVE,
		SHIELD,
		STRAIGHT,
		HORIZONTAL,
		DIAGONAL,
		SINGLE,
		NOP,
		PROJECTILE,
		INSTANT,
		SINGLEPATH
	}
	
	public Action(String name, int cost, int damage, int range, int size, 
			Pattern pattern, boolean oneHit, boolean canBlock,boolean needsToggle, 
			Effect effect, String description,Texture icon){
		this.name = name;
		this.cost = cost;
		this.damage = damage;
		this.range = range;
		this.size = size;
		this.pattern = pattern;
		this.oneHit = oneHit;
		this.canBlock = canBlock;
		this.needsToggle = needsToggle;
		this.effect = effect;
		this.description = description;
		this.isBuff = false;
		this.barIcon = icon;
		this.menuIcon = icon;
	}
	
	public Action(String name, int cost, int damage, int range, int size, 
			Pattern pattern, boolean oneHit, boolean canBlock,boolean needsToggle,
			Effect effect, String description, String strpath,Texture icon){
		this(name, cost, damage, range, size, pattern, oneHit, canBlock,needsToggle, effect, description,icon);
		
		if ((this.pattern == Pattern.PROJECTILE||this.pattern == Pattern.INSTANT||this.pattern == Pattern.SINGLEPATH)&&strpath!=""){
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
	
	public Action() {
		this("N/A", 0, 0, 0, 0, Pattern.NOP, false, false, false, null, "", null);
	}

	public String getName(){
		return this.name;
	}
	
	
	/**
	 * helper function that tells if this action would hit (targetX, targetY) starting
	 * from (startX, startY).
	 */
	public boolean hitsTarget(int startX, int startY, int targetX, int targetY, boolean leftside, GridBoard board){
		if(pattern == Pattern.MOVE || pattern == pattern.NOP || pattern == pattern.SHIELD){
			return false;
		}
		switch(pattern){
		case SINGLE:
			return this.singleCanTarget(startX, startY, targetX, targetY, leftside, board);
		case STRAIGHT:
			return startY == targetY && (Math.abs(startX - targetX) <= range);
		case HORIZONTAL:
			return targetX == board.width - 1 - startX;
		case DIAGONAL:
			if(Math.abs(startX - targetX) > range){
				return false;
			}
			if(leftside){
				return Math.abs(startX + 1 - targetX) == Math.abs(startY - targetY);
			}
			else{
				return Math.abs(startX - 1 - targetX) == Math.abs(startY - targetY);
			}
		case SINGLEPATH:
			return this.singlePathCanTarget(startX,startY,targetX,targetY,leftside,board);
		default:
			return isOnPath(startX, startY, targetX, targetY, leftside);
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
				int y = startY + path[i].y;
				if (x == targetX && y == targetY){
					return true;
				}
			}
			if(needsToggle){
				for (int i = 0; i < path.length; i++){
					int x = startX + path[i].x;
					int y = startY - path[i].y;
					if (x == targetX && y == targetY){
						return true;
					}
				}
			}
			return false;
		} else {
			for (int i = 0; i < path.length; i++){
				int x = startX - path[i].x;
				int y = startY + path[i].y;
				if (x == targetX && y == targetY){
					return true;
				}
			}
			if(needsToggle){
				for (int i = 0; i < path.length; i++){
					int x = startX - path[i].x;
					int y = startY - path[i].y;
					if (x == targetX && y == targetY){
						return true;
					}
				}
			}
			return false;
		}	
	}
	
	public boolean getNeedsToggle(){
		return this.needsToggle;
	}
	
	public boolean singleCanTarget(int startX,int startY, int targetX,int targetY, boolean leftside, GridBoard board){
		// check with range from epicenter
		if(isBuff && leftside && targetX >= board.width / 2){
//			if(isBuff){
//				System.out.println("can hit "+ targetX+" "+startX+" "+false);
//			}
			return false;
		}
		if(isBuff && !leftside && targetX < board.width / 2){
//			if(isBuff){
//				System.out.println("can hit "+ targetX+" "+startX+" "+false);
//			}
			return false;
		}
		
		int diffX = Math.abs(targetX - startX);
		int diffY = Math.abs(targetY - startY);
		return (diffX+diffY) < this.range;
	}
	
	public boolean singlePathCanTarget(int startX,int startY, int targetX,int targetY, boolean leftside, GridBoard board){
		if (notSymmetric){
			return singleCanTarget(startX,startY,targetX,targetY,leftside,board) || singleCanTarget(startX,startY-1,targetX,targetY,leftside,board); 
		} else {
			return singleCanTarget(startX,startY,targetX,targetY,leftside,board);
		}
		
	}
	
	public void setAnimation(Animation animation){
		this.animation = animation;
	}
	
	public void setProjectileAnimation(Animation projectileAnimation){
		//System.out.println(name);
		//System.out.println(projectileAnimation);
		this.projectileAnimation = projectileAnimation;
	}
	
	public void setShieldNumberHits(int num){
		this.shieldNumberHits = num;
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
		float xMin = this.x;
		float xMax = this.x + this.width;
		float yMin = this.y;
		float yMax = this.y + this.height;
		return x <= xMax && x >= xMin && y <= yMax && y >= yMin;
	}
	
	public int getDamage(Character hitChar){
		if (hitChar != null){
			return hitChar.damageTaken(this.getDamage(null));
		} else {
			return damage*10;
		}
	}
}
