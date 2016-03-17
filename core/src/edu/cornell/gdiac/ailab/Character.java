package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
//import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.AnimationNode.CharacterState;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.AIController.Difficulty;

public class Character {
	float TOKEN_OFFSET_UP = 20;
	float TOKEN_OFFSET_DOWN = 30;
	float SHADOW_ARROW_WIDTH = 0.0075f; // 6 pixel of 800
	float SHIELD_OFFSET = 5;
	float SHIELD_WIDTH = 0.0125f;
	int DIAGONAL_SIZE = 20;
	int HEALTH_HEIGHT = 10;
	
	// currently it takes 2 squares not definite might be utilizing range
	float SHIELD_LENGTH = 2;
	
	// character width is 120 and at tile size 150 proportion of current tile size
	//
	float CHARACTER_PROPORTION = 0.8f;

	/** Name of character */
	String name;
	/** Current health of character */
	int health;
	/** Maximum health */
	int maxHealth;
	/** Speed when moving through normal part of bar */
	float speed;
	/** Speed when moving through cast par of bar */
	float castSpeed;
	
	/** Current x position */
	int xPosition;
	/** Current y position */
	int yPosition;
	/** Current cast position */
	float castPosition;
	Color color;
	Texture texture;
	Texture icon;
	AnimationNode animation;
	SelectionMenu selectionMenu;
	boolean leftside;
	
	/** Do I need to select my actions */
	boolean needsSelection;
	/** Do I need to use an attack on my queue */
	boolean needsAttack;
	/** Do I need to draw my shadow */
	boolean needsShadow;
	/** Am I currently selecting my actions */
	boolean isSelecting;
	/** Do I have a persisting action currently in play */
	boolean isPersisting;
	/** Am I currently being affected by an effect? */
	boolean isAffected;
	/** Did I just execute an attack? */
	boolean isExecuting;
	/** Did I just get hit? */
	boolean isHurt;
	
	//TODO: Change to pass this in from GameEngine
	/** Starting x and y positions */
	int startingXPosition;
	int startingYPosition;
	
	/** movement is block afterwards if bump into */
	boolean isBlocked;
	
	/** AI info */
	boolean isAI;
	Difficulty diff;
	
	/** List of available actions */
	Action[] availableActions; 
		
	/** Lists of queued and persisting actions */
	LinkedList<ActionNode> queuedActions;
	LinkedList<ActionNode> persistingActions;
	ArrayList<Effect> effects;
	
	/** List of coordinates blocked by my shield */
	List<Coordinate> shieldedCoordinates;
	
	/** For highlighting character */
	float lerpVal = 0;
	boolean increasing;
	
	float lastCastStart;
	
	/**Constructor used by GameEngine to create characters from yaml input. */
	public Character (Texture texture, Texture icon, AnimationNode animation, String name, 
						int health, int maxHealth, Color color, 
						float speed, float castSpeed, int xPosition, int yPosition,
						boolean leftSide, Action[] actions){
		this.texture = texture;
		this.icon = icon;
		this.animation = animation;
		this.name = name;
		this.health = health;
		this.maxHealth = maxHealth;
		this.speed = speed;
		this.castSpeed = castSpeed;

		this.color = color;
		
		/* Randomize so that its not always the same thing */
		//this.speed = (float) (Math.random()*0.003 + 0.003);
		//this.castSpeed = (float) (Math.random()*0.004 + 0.005);
		
		this.startingXPosition = this.xPosition = xPosition;
		this.startingYPosition = this.yPosition = yPosition;
		this.leftside = leftSide;
		
		lastCastStart = 0;
		castPosition = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		effects = new ArrayList<Effect>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		
		this.availableActions = actions;
		selectionMenu = new SelectionMenu(availableActions);
		
	}
	
	public Character(Character c){
		this.texture = c.texture;
		this.icon = c.icon;
		this.name = c.name;
		this.health = c.health;
		this.maxHealth = c.maxHealth;
		this.speed = c.speed;
		this.castSpeed = c.castSpeed;

		this.color = c.color;	
		
		this.startingXPosition = this.xPosition = c.xPosition;
		this.startingYPosition = this.yPosition = c.yPosition;
		this.leftside = c.leftside;
		
		castPosition = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		effects = new ArrayList<Effect>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		
		this.availableActions = c.availableActions;
		selectionMenu = new SelectionMenu(availableActions);
	}
	
	/**
	 * Resets a character back to starting data
	 */
	public void reset(){
		this.health = this.maxHealth;
		this.xPosition = this.startingXPosition;
		this.yPosition = this.startingYPosition;
		
		/* Randomize for now so that its not always the same thing */
		this.speed = (float) (Math.random()*0.003 + 0.003);
		this.castSpeed = (float) (Math.random()*0.004 + 0.002);
		
		lastCastStart = 0;
		castPosition = 0;
		queuedActions.clear();
		persistingActions.clear();

		for (Coordinate c:shieldedCoordinates){
			c.free();
		}
		shieldedCoordinates.clear();

		selectionMenu.reset();
		
		needsSelection = needsAttack = needsShadow = isSelecting = isPersisting = false;
		isExecuting = isHurt = false;
	}
	
	/**  copy the static attributes of the character into a new object **/
	public Character copy(){
		return new Character(this);
	}
	
	public boolean isAlive() {
		return health > 0;
	}
	
	public SelectionMenu getSelectionMenu() {
		return selectionMenu;
	}
	
	/**
	 * Is currently selecting a move in the selection menu
	 */
	public boolean isSelecting() {
		return isSelecting;
	}
	
	public void setSelecting(boolean isSelecting) {
		this.isSelecting = isSelecting;
		lerpVal = 0;
		increasing = true;
	}
	
	public void setQueuedActions(List<ActionNode> actions){
		this.queuedActions = (LinkedList<ActionNode>) actions;
	}
	
	/** 
	 * prompts the players selection screen to pop up next frame
	 * resets any variables used in the last round selection screen
	 * this is for when a unit tries to move into another units square
	 * we have this flag which stops all future movements (temporary)	
	 * when he starts his next round this flag is switched to false.
	 */
	public void needsSelection(){
		this.needsSelection = true;
		this.isBlocked = false;
	}
	
	/**
	 * Make an AI with the given difficulty
	 */
	public void setAI(Difficulty diff){
		this.diff = diff;
		this.isAI = true;
	}
	
	/**
	 * Make a human
	 */
	public void setHuman(){
		this.isAI = false;
	}
	
	/**
	 * Check if character needs to display shadow
	 */
	boolean needShadow() {
		List<ActionNode> actions = isSelecting ? selectionMenu.selectedActions : queuedActions;
		for (ActionNode an : actions){
			if (an.action!= null && an.action.pattern == Pattern.MOVE){
				return needsShadow;
			}
		}
		return false;
	}
	
	boolean hasAttacks() {
		return queuedActions.peek() != null;
	}
	
	boolean hasPersisting() {
		return persistingActions.peek() != null;
	}
	
	/**
	 * Reset coordinates that are shielded by this character 
	 */
	private void resetShieldedCoordinates(){
		Coordinates coordPool = Coordinates.getInstance();
		// add coordinates back to the pool
		for (Coordinate c: shieldedCoordinates){
			c.free();
		}
		shieldedCoordinates.clear();

		for (ActionNode an : persistingActions){
			if (an.action.pattern == Pattern.SHIELD){
				int tempX = an.getCurrentX();
				int tempY = an.getCurrentY();
				shieldedCoordinates.add(coordPool.newCoordinate(tempX,tempY));
				shieldedCoordinates.add(coordPool.newCoordinate(tempX, an.direction == Direction.DOWN ? tempY-1 : tempY+1));
			}
		}
	}
	
	void addEffect(Effect e){
		effects.add(e);
	}
	
	ArrayList<Effect> getEffects(){
		return effects;
	}
	
	void removeEffect(int i){
		effects.remove(i);
	}
	
	/**
	 * Add a persisting action to draw/be checked in the future
	 */
	void addPersisting(ActionNode an){
		if (an.action.pattern == Pattern.SHIELD){
			an.setPersisting(0, xPosition, yPosition);
			persistingActions.add(an);
			resetShieldedCoordinates();
		} else if (an.action.pattern == Pattern.DIAGONAL || an.action.pattern == Pattern.STRAIGHT){
			if (leftside){
				an.setPersisting(0, xPosition+1, yPosition);
			} else {
				an.setPersisting(0, xPosition-1, yPosition);
			}
			persistingActions.add(an);
		}
	}
	
	void popPersistingCast(ActionNode an){
		persistingActions.remove(an);
		if (an.action != null && an.action.pattern == Pattern.SHIELD){
			resetShieldedCoordinates();
		}
		an.free();
	}
	
	List<ActionNode> getPersistingActions(){
		return persistingActions;
	}
	
	boolean hasShield() {
		return !shieldedCoordinates.isEmpty();
	}
	
	List<Coordinate> getShieldedCoords(){
		return shieldedCoordinates;
	}
	
	float getNextCast() {
		ActionNode an = queuedActions.peek();
		if (an != null){
			return an.executePoint;
		} else {
			return 2f;
		}
	}
	
	public ActionNode popCast(){
		return queuedActions.poll();
	}
	
	public void startingCast(){
		lastCastStart = castPosition;
	}
	
	public int getShadowX(){
		int shadX = xPosition;
		List<ActionNode> actions = isSelecting ? selectionMenu.selectedActions : queuedActions;
		for (ActionNode an : actions){
			if (an.action.pattern == Pattern.MOVE){
				if (an.direction == Direction.LEFT){
					shadX--;
				} else if (an.direction == Direction.RIGHT){
					shadX++;
				}
			}
		}
		return shadX;
	}
	
	public int getShadowY(){
		int shadY = yPosition;
		List<ActionNode> actions = isSelecting ? selectionMenu.selectedActions : queuedActions;
		for (ActionNode an : actions){
			if (an.action != null && an.action.pattern == Pattern.MOVE){
				if (an.direction == Direction.UP){
					shadY++;
				} else if (an.direction == Direction.DOWN){
					shadY--;
				}
			}
		}
		return shadY;
	}
	
	public void draw(GameCanvas canvas,GridBoard board){
		if (!isAlive()){
			return;
		}
		drawHealth(canvas,board);
		drawToken(canvas);
		if(hasPersisting()){
			drawPersisting(canvas,board);
		}
	}
	
	/** temporary while menu is blocked by characters */
	public void drawSelection(GameCanvas canvas){
		if (isSelecting && isAlive()){
			selectionMenu.draw(canvas);
		}
	}
	
	public float getCharScale(GameCanvas canvas, Texture texture,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		return (tileW*CHARACTER_PROPORTION)/texture.getWidth();
	}
	
	public float getCharScale(GameCanvas canvas, TextureRegion region,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		return (tileW*CHARACTER_PROPORTION)/region.getRegionWidth();
	}
	
	public void setExecuting(){
		isExecuting = true;
		isHurt = false;
	}
	
	public void setHurt(){
		isHurt = true;
		isExecuting = false;
	}
	
	/**
	 * Return FilmStrip with animation set as frame
	 * @return
	 */
	public FilmStrip getFilmStrip(){
		if (isHurt){
			FilmStrip fs = animation.getTexture(CharacterState.HURT);
			if (fs == null){
				isHurt = false;
			}
			return fs;
		} 
		if (isExecuting){
			FilmStrip fs = animation.getTexture(CharacterState.EXECUTING);
			if (fs == null){
				isExecuting = false;
			}
			return fs;
		}
		if (queuedActions.isEmpty()){
			return animation.getTexture(CharacterState.IDLE);
		} else {
			if (queuedActions.peek().isInterrupted){
				//change to interrupted later maybe?
				return animation.getTexture(CharacterState.IDLE);
			}
			if (castPosition - lastCastStart >= ((1-ActionBar.castPoint)/ActionBar.getTotalSlots())){
				return animation.getTexture(CharacterState.CASTING);
			} else {
				return animation.getTexture(CharacterState.ACTIVE);
			}
		}
	}
	
	public void drawCharacter(GameCanvas canvas,GridBoard board){
		if (increasing){
			lerpVal+=0.01;
			if (lerpVal >= 0.5){
				increasing = false;
			}
		} else {
			lerpVal -= 0.01;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		float canvasX = board.offsetBoard(canvas,tileW*xPosition);
		float canvasY = tileH*yPosition;
		
		/** maybe highlight character? */
		Color col = isSelecting ? Color.WHITE.cpy().lerp(Color.GREEN, lerpVal) : Color.WHITE;
		
		//Decide what animation to draw
		//Will sometimes be null when current animation is done, we just need to call again
		FilmStrip toDraw = getFilmStrip();
		if (toDraw == null) {
			toDraw = getFilmStrip();
		}
		
		//For now, if still not found (shouldnt happen when animation sheet is full) 
		//go back to initial texture (current idle texture)
		if (toDraw != null){
			float charScale = getCharScale(canvas,toDraw,board);
			canvas.drawCharacter(toDraw, canvasX, canvasY, col, leftside,charScale);
		} else {
			float charScale = getCharScale(canvas,texture,board);
			canvas.drawCharacter(texture, canvasX, canvasY, col, leftside,charScale);
		}
	}
	
	/**
	 * Draws future position of ship with lines depicting path
	 */
	public void drawShadowCharacter(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		float canvasX = board.offsetBoard(canvas,tileW*getShadowX());
		float canvasY = tileH*getShadowY();
		
		float charScale = getCharScale(canvas,texture,board);
		canvas.drawCharacter(texture, canvasX, canvasY, Color.WHITE.cpy().lerp(Color.CLEAR, 0.3f), leftside,charScale);
		int tempX = xPosition;
		int tempY = yPosition;
		int nowX = tempX;
		int nowY = tempY;
		List<ActionNode> actions = isSelecting ? selectionMenu.selectedActions : queuedActions;
		for (ActionNode an : actions){
			if (an.action.pattern == Action.Pattern.MOVE){
				switch (an.direction){
				case UP:
					nowY++;
					break;
				case DOWN:
					nowY--;
					break;
				case LEFT:
					nowX--;
					break;
				case RIGHT:
					nowX++;
					break;
				default:
					break;
				}
			}
			//72
			float arrowOffX = (tileW - SHADOW_ARROW_WIDTH)/2;
			float arrowOffY = (tileH - SHADOW_ARROW_WIDTH)/2;
			if (nowX != tempX && nowY == tempY){
				int minX = Math.min(nowX, tempX);
				float arrowX = board.offsetBoard(canvas,arrowOffX + (tileW *minX));
				float arrowY = arrowOffY + (tileH *nowY);
				float arrowWidth = tileW + SHADOW_ARROW_WIDTH;
				float arrowHeight = SHADOW_ARROW_WIDTH;
				canvas.drawBox(arrowX,arrowY,arrowWidth, arrowHeight, Color.BLACK);
				//canvas.drawBox(72 + 150*minX, 47 + 100*nowY, 156, 6, Color.BLACK);
			} else if (nowY != tempY && nowX == tempX){
				int minY = Math.min(nowY, tempY);
				float arrowX = board.offsetBoard(canvas,arrowOffX + (tileW *nowX));
				float arrowY = arrowOffY + (tileH *minY);
				float arrowWidth = SHADOW_ARROW_WIDTH;
				float arrowHeight = tileH + SHADOW_ARROW_WIDTH;
				canvas.drawBox(arrowX,arrowY,arrowWidth, arrowHeight, Color.BLACK);
				//canvas.drawBox(72+150*nowX, 47+100*minY, 6, 106, Color.BLACK);
			} else {
//				System.out.println("PLEASE CHECK Character");
			}
			tempX = nowX;
			tempY = nowY;
		}
	}
	
	/**
	 * Draws persisting objects
	 */
	private void drawPersisting(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		for (ActionNode an : persistingActions){
			switch (an.action.pattern){
			case SHIELD:
				int shieldW = (int)(SHIELD_WIDTH * canvas.getWidth());
				int shieldH = (int)(tileH * SHIELD_LENGTH);
				if (leftside){
					int shieldX = (int)board.offsetBoard(canvas,tileW - SHIELD_OFFSET + (tileW*an.curX));
					int shieldY = (int)(tileH *an.curY);
					if (an.direction == Direction.UP){
						canvas.drawBox(shieldX, shieldY, shieldW, shieldH, Color.GRAY);
					} else {
						canvas.drawBox(shieldX,shieldY - tileH, shieldW, shieldH, Color.GRAY);
						//canvas.drawBox(145+150*an.curX, 100*an.curY-100, 10, 200, Color.GRAY);
					}
				} else {
					int shieldX = (int)board.offsetBoard(canvas,-SHIELD_OFFSET + (tileW*an.curX));
					int shieldY = (int)(tileH *an.curY);
					if (an.direction == Direction.UP){
						canvas.drawBox(shieldX, shieldY,shieldW,shieldH, Color.GRAY);
					} else {
						canvas.drawBox(shieldX, shieldY - tileH,shieldW,shieldH, Color.GRAY);
					}
				}
				break;
			case STRAIGHT:
			case DIAGONAL:
				float diagX = board.offsetBoard(canvas,(tileW/2 - DIAGONAL_SIZE/2 + (board.getTileWidth(canvas)*an.curX)));;
				float diagY = tileH/2 - DIAGONAL_SIZE/2 + (board.getTileHeight(canvas)*an.curY);
				canvas.drawBox(diagX,diagY, DIAGONAL_SIZE, DIAGONAL_SIZE, color);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Draws health bar 
	 */
	private void drawHealth(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		
		float charScale = getCharScale(canvas,texture,board);
		
		float canvasX = board.offsetBoard(canvas,tileW*xPosition);
		float canvasY = texture.getHeight()*charScale + tileH*yPosition;
		canvas.drawBox(canvasX, canvasY, tileW, 10, Color.WHITE);
		canvas.drawBox(canvasX, canvasY, (int) (tileW*health/maxHealth), HEALTH_HEIGHT, color);
	}
	
	/**
	 * Draws token on action bar
	 * @param canvas
	 */
	private void drawToken(GameCanvas canvas){
		float canvasX = ActionBar.getBarX(canvas) + ActionBar.getBarWidth(canvas)*castPosition - icon.getWidth()/2;
		float upBar = ActionBar.getBarY(canvas) + TOKEN_OFFSET_UP;
		float downBar = ActionBar.getBarY(canvas) - TOKEN_OFFSET_DOWN;
		float canvasY = leftside ? upBar : downBar;
		canvas.drawTexture(icon, canvasX, canvasY, color);
	}
}
