package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;
//import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.AIController.Difficulty;

public class Character {
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
	
	//TODO: Change to pass this in from GameEngine
	/** Starting x and y positions */
	int startingXPosition;
	int startingYPosition;
	
	/** Position of shadow for depicting future move path */
	int shadowX;
	int shadowY;
	
	//TODO: Use Coordinates
	/** 
	 * List of path, in the front is current position and at the end
	 * is shadowX and shadowY
	 */
	LinkedList<Integer> oldShadowX;
	LinkedList<Integer> oldShadowY;
	
	/** AI info */
	boolean isAI;
	Difficulty diff;
	
	/** List of available actions */
	Action[] availableActions; 
		
	/** Lists of queued and persisting actions */
	LinkedList<ActionNode> queuedActions;
	LinkedList<ActionNode> persistingActions;
	
	/** List of coordinates blocked by my shield */
	List<Coordinate> shieldedCoordinates;
	
	/**Constructor used by GameEngine to create characters from yaml input. */
	public Character (Texture texture, Texture icon, String name, int health, int maxHealth, Color color, 
						float speed, float castSpeed, int xPosition, int yPosition,
						boolean leftSide, Action[] actions){
		this.texture = texture;
		this.icon = icon;
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
		
		castPosition = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		
		oldShadowX = new LinkedList<Integer>();
		oldShadowY = new LinkedList<Integer>();
		setShadow(xPosition,yPosition);
		
		this.availableActions = actions;
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
		
		this.castPosition = 0;
		queuedActions.clear();
		persistingActions.clear();
		shieldedCoordinates.clear();
		
		oldShadowX.clear();
		oldShadowY.clear();
		setShadow(this.xPosition,this.yPosition);
		
		selectionMenu.reset();
		
		needsSelection = needsAttack = needsShadow = isSelecting = isPersisting = false;
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
	}
	
	public void setQueuedActions(List<ActionNode> actions){
		this.queuedActions = (LinkedList<ActionNode>) actions;
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
		return (oldShadowX.size()>1) && needsShadow;
	}
	
	public void popLastShadow(){
		oldShadowX.pollLast();
		oldShadowY.pollLast();
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
		shieldedCoordinates.clear();
		for (ActionNode an : persistingActions){
			if (an.action.pattern == Pattern.SHIELD){
				int tempX = an.getCurrentX();
				int tempY = an.getCurrentY();
				shieldedCoordinates.add(new Coordinate(tempX,tempY));
				shieldedCoordinates.add(new Coordinate(tempX, an.yPosition == 0 ? tempY-1 : tempY+1));
			}
		}
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
		if (an.action.pattern == Pattern.SHIELD){
			resetShieldedCoordinates();
		}
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
	
	public void setShadow(int shadX, int shadY){
		shadowX = shadX;
		shadowY = shadY;
		oldShadowX.push(shadX);
		oldShadowY.push(shadY);
	}
	
	public void rewindShadow(){
		oldShadowX.pop();
		oldShadowY.pop();
		shadowX = oldShadowX.peek();
		shadowY = oldShadowY.peek();
	}
	
	public void draw(GameCanvas canvas){
		if (!isAlive()){
			return;
		}
		drawHealth(canvas);
		drawToken(canvas);
		if(hasPersisting()){
			drawPersisting(canvas);
		}
	}
	
	/** temporary while menu is blocked by characters */
	public void drawSelection(GameCanvas canvas){
		if (isSelecting){
			selectionMenu.draw(canvas);
		}
	}
	
	public void drawCharacter(GameCanvas canvas){
		float canvasX = 150*xPosition;
		float canvasY = 100*yPosition;
		
		/** maybe highlight character? */
		//Color col = isSelecting ? Color.WHITE.cpy().lerp(Color.BLUE, 0.5f) : Color.WHITE;
		canvas.drawCharacter(texture, canvasX, canvasY, Color.WHITE, leftside);
	}
	
	/**
	 * Draws future position of ship with lines depicting path
	 */
	public void drawShadowCharacter(GameCanvas canvas){
		float canvasX = 150*shadowX;
		float canvasY = 100*shadowY;
		canvas.drawCharacter(texture, canvasX, canvasY, Color.WHITE.cpy().lerp(Color.CLEAR, 0.3f), leftside);
		int tempX = shadowX;
		int tempY = shadowY;
		int nowX;
		int nowY;
		for (int i = 1; i < oldShadowX.size(); i++){
			nowX = oldShadowX.get(i);
			nowY = oldShadowY.get(i);
			if (nowX != tempX && nowY == tempY){
				int minX = Math.min(nowX, tempX);
				canvas.drawBox(72 + 150*minX, 47 + 100*nowY, 156, 6, Color.BLACK);
			} else if (nowY != tempY && nowX == tempX){
				int minY = Math.min(nowY, tempY);
				canvas.drawBox(72+150*nowX, 47+100*minY, 6, 106, Color.BLACK);
			} else {
				System.out.println("PLEASE CHECK Character");
			}
			
			tempX = nowX;
			tempY = nowY;
		}
	}
	
	/**
	 * Draws persisting objects
	 */
	private void drawPersisting(GameCanvas canvas){
		for (ActionNode an : persistingActions){
			switch (an.action.pattern){
			case SHIELD:
				if (leftside){
					if (an.yPosition==3){
						canvas.drawBox(145+150*an.curX, 100*yPosition, 10, 200, Color.GRAY);
					} else {
						canvas.drawBox(145+150*an.curX, 100*yPosition-100, 10, 200, Color.GRAY);
					}
				} else {
					if (an.yPosition==3){
						canvas.drawBox(-5+150*an.curX, 100*yPosition, 10, 200, Color.GRAY);
					} else {
						canvas.drawBox(-5+150*an.curX, 100*yPosition-100, 10, 200, Color.GRAY);
					}
				}
				break;
			case STRAIGHT:
			case DIAGONAL:
				canvas.drawBox(65+150*an.curX, 40+100*an.curY, 20, 20, Color.GRAY);
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * Draws health bar 
	 */
	private void drawHealth(GameCanvas canvas){
		float canvasX = 150*xPosition;
		float canvasY = 270 + 100*yPosition;
		canvas.drawBox(canvasX, canvasY, 150, 10, Color.WHITE);
		canvas.drawBox(canvasX, canvasY, (int) (150.*health/maxHealth), 10, Color.GREEN);
	}
	
	/**
	 * Draws token on action bar
	 * @param canvas
	 */
	private void drawToken(GameCanvas canvas){
		float canvasX = 35 + 800*castPosition;
		float canvasY = leftside ? 720 : 670;
		canvas.drawTexture(icon, canvasX, canvasY, Color.WHITE);
	}
}
