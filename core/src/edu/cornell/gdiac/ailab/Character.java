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

public class Character implements GUIElement {
	private static final float TOKEN_OFFSET_UP = 20;
	private static final float TOKEN_OFFSET_DOWN = 30;
	private static final float SHADOW_ARROW_WIDTH = 0.0075f; // 6 pixel of 800
	private static final float SHIELD_OFFSET = 5;
	private static final float SHIELD_WIDTH = 0.0125f;
	private static final int DIAGONAL_SIZE = 20;
	
	private static final float ACTIONBAR_TICK_SIZE = 8f;
	
	// character width is 120 and at tile size 150 proportion of current tile size
	//
	float CHARACTER_PROPORTION = 0.7f;

	/** Name of character */
	String name;
	/** Current health of character */
	int health;
	/** Maximum health */
	int maxHealth;
	/** Speed when moving through normal part of bar */
	private float speed;
	/** Speed when moving through cast par of bar */
	private float castSpeed;
	
	/** Current x position */
	int xPosition;
	/** Current y position */
	int yPosition;
	/** Current cast position */
	float castPosition;
	/** How much cast moved in last frame  */
	float castMoved;
	Color color;
	Texture texture;
	Texture icon;
	AnimationNode animation;
	SelectionMenu selectionMenu;
	CharActionBar actionBar;
	
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
	private boolean isHovering;
	
	/** Cast bar position of last cast (Used for animating) */
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

		this.color = color;
		
		/* Randomize so that its not always the same thing */
		this.speed = (float) (Math.random()*0.003 + 0.003)/3;
		this.castSpeed = (float) (Math.random()*0.003 + 0.0025)/3;
		
		this.startingXPosition = this.xPosition = xPosition;
		this.startingYPosition = this.yPosition = yPosition;
		this.leftside = leftSide;
		lastCastStart = 0;
		castPosition = 0;
		castMoved = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		effects = new ArrayList<Effect>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		
		this.availableActions = actions;
		selectionMenu = new SelectionMenu(availableActions);
		
		float waitTime = (float) (Math.random()*3 + 2);
		float castTime = (float) (Math.random()*4 + 4);
		actionBar = new CharActionBar(4,waitTime,castTime);
		
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
		
		lastCastStart = 0;
		castPosition = 0;
		castMoved = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		effects = new ArrayList<Effect>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		
		this.availableActions = c.availableActions;
		selectionMenu = new SelectionMenu(availableActions);
	}
	
	/** update the state of the character 
	 *  currently just updates his actionBar with his current health
	 * **/
	public void update(){
		float healthProportion = ((float)health/(float)maxHealth);
		this.actionBar.update(healthProportion);
	}
	
	public float getXMin(GameCanvas canvas, GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float canvasX = board.offsetBoard(canvas,tileW*xPosition,0).x;
		return canvasX;
	}
	
	public float getYMin(GameCanvas canvas, GridBoard board){
		float tileH = board.getTileHeight(canvas);
		float canvasY = board.offsetBoard(canvas,0,tileH*yPosition).y;
		return canvasY;
	}
	
	public float getXMax(GameCanvas canvas, GridBoard board){
		float charScale = getCharScale(canvas,texture,board);
		return getXMin(canvas, board) + texture.getWidth()*charScale;
//		return CANVAS_X_MULTIPLIER*xPosition + texture.getWidth();
	}
	
	public float getYMax(GameCanvas canvas, GridBoard board){
		float charScale = getCharScale(canvas,texture,board);
		return getYMin(canvas, board) + texture.getHeight()*charScale;
	}
	
//	public float getTokenX(GameCanvas canvas){
//		return ActionBar.getBarX(canvas) + ActionBar.getBarWidth(canvas)*castPosition - icon.getWidth()/2;
//	}
//	
//	public float getTokenY(GameCanvas canvas){
//		float upBar = ActionBar.getBarY(canvas) + TOKEN_OFFSET_UP;
//		float downBar = ActionBar.getBarY(canvas) - TOKEN_OFFSET_DOWN;
//		return leftside ? upBar : downBar;
//		
//	}
	
//	public float getTokenXMin(GameCanvas canvas, GridBoard board){
//		float tokenX = getTokenX(canvas);
//		return tokenX;
//	}
//	
//	public float getTokenYMin(GameCanvas canvas, GridBoard board){
//		float tokenY = getTokenY(canvas);
//		return tokenY;
//	}
//	
//	public float getTokenXMax(GameCanvas canvas, GridBoard board){
//		float tokenX = getTokenX(canvas);
//		float charScale = getCharScale(canvas,texture,board);
//		return tokenX + icon.getWidth()*charScale;
//	}
//	
//	public float getTokenYMax(GameCanvas canvas, GridBoard board){
//		float tokenY = getTokenY(canvas);
//		float charScale = getCharScale(canvas,texture,board);
//		return tokenY + icon.getHeight()*charScale;
//	}
	
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
	
	/** if the character hasAttacks leftover that are not interrupted (moves can still execute when interrupted)
	 *  returns true if there are attacks left returns false if its empty or last action is interrupted
	 *  sideEffect: pops the last action if its interrupted
	 * @return
	 */
	public boolean hasAttacks() {
		ActionNode anode = queuedActions.peek();
		if (anode!= null){
			if ((anode.isInterrupted && anode.action.pattern != Pattern.MOVE) && queuedActions.size() == 1){
				queuedActions.poll();
				return false;
			}
			else{
				return true;
			}
		}
		else{
			return false;
		}
	}
	
	public boolean hasPersisting() {
		return persistingActions.peek() != null;
	}
	
	public float getSpeed() {
		return this.actionBar.getSpeed();
	}
	
	public float getCastPoint(){
		return this.actionBar.getCastPoint();
	}
	
	public void setSpeedModifier(int val){
		//TODO if we have more modifier on stats have a bitmap of modifiers
		this.actionBar.setSpeedModifier(val);
	}
	
	public int getSpeedModifier(){
		return this.actionBar.speedModifier;
	}
	
	/**
	 * Reset coordinates that are shielded by this character 
	 */
	private void resetShieldedCoordinates(){
		// add coordinates back to the pool
		for (Coordinate c: shieldedCoordinates){
			c.free();
		}
		shieldedCoordinates.clear();

		for (ActionNode an : persistingActions){
			if (an.action.pattern == Pattern.SHIELD){
				for (Coordinate c:an.path){
					shieldedCoordinates.add(c);
				}
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
			an.setPersisting(castPosition, xPosition, yPosition);
			persistingActions.add(an);
			resetShieldedCoordinates();
		} else if (an.action.pattern == Pattern.DIAGONAL || an.action.pattern == Pattern.STRAIGHT){
			if (leftside){
				an.setPersisting(castPosition, xPosition+1, yPosition);
			} else {
				an.setPersisting(castPosition, xPosition-1, yPosition);
			}
			persistingActions.add(an);
		}
	}
	
	void addPersisting(ActionNode an,Coordinate[] path){
		if (an.action.pattern == Pattern.SHIELD){
			an.setPersisting(castPosition, xPosition, yPosition,path);
			persistingActions.add(an);
			resetShieldedCoordinates();
		} else if (an.action.pattern == Pattern.DIAGONAL || an.action.pattern == Pattern.STRAIGHT){
			if (leftside){
				an.setPersisting(castPosition, xPosition+1, yPosition,path);
			} else {
				an.setPersisting(castPosition, xPosition-1, yPosition,path);
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
	
	CharActionBar getActionBar(){
		return this.actionBar;
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
	
	public void updateRoundLengths(){
		
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
		if(hasPersisting()){
			drawPersisting(canvas,board);
		}
	}
	
	/** temporary while menu is blocked by characters */
	public void drawSelection(GameCanvas canvas,int count){
		if (isSelecting && isAlive()){
			selectionMenu.draw(canvas,this.actionBar,count);
		}
	}
	
	/** draw the queued actions **/
	public void drawQueuedActions(GameCanvas canvas,int count){
		// don't draw queued actions unless he is hovering or is an AI
		if (this.isAI||!this.getHovering()){
			return;
		}
		
		float actionSlot_x = this.actionBar.getX(canvas);
		float actionSlot_y = this.actionBar.getY(canvas, count);

		List<ActionNode> queuedActions = this.queuedActions;
		for (ActionNode a: queuedActions){
			// length relative 
			float centeredCast = this.actionBar.getCenteredActionX(canvas, a.executePoint, a.action.cost);
			float x_pos = actionSlot_x + centeredCast;
			
			float y_pos = actionSlot_y;
			String text = a.action.name;
			canvas.drawCenteredText(text,x_pos,y_pos,Color.BLACK);
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
			FilmStrip fs = animation.getTexture(CharacterState.EXECUTE);
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
			if (castPosition - lastCastStart >= ((1-this.getActionBar().getCastPoint())/this.getActionBar().getNumSlots())){
				return animation.getTexture(CharacterState.CAST);
			} else {
				return animation.getTexture(CharacterState.ACTIVE);
			}
		}
	}
	
	public void drawCharacter(GameCanvas canvas,GridBoard board, boolean shouldDim){
		if (increasing){
			lerpVal+=0.02;
			if (lerpVal >= 0.5){
				increasing = false;
			}
		} else {
			lerpVal -= 0.02;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Coordinate c = board.offsetBoard(canvas,tileW*xPosition,tileH*yPosition);
		float canvasX = c.x;
		float canvasY = c.y;
		c.free();
		
		Color color = getColor(shouldDim);
		Color highlightColor = getHighlightColor(shouldDim);
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
			// draw once character normally then draw character again with tint
			canvas.drawCharacter(toDraw, canvasX, canvasY, color, leftside,charScale);
			canvas.drawCharacter(toDraw, canvasX, canvasY, highlightColor, leftside,charScale);
		} else {
			float charScale = getCharScale(canvas,texture,board);
			canvas.drawCharacter(texture, canvasX, canvasY, color, leftside,charScale);
			canvas.drawCharacter(texture, canvasX, canvasY, highlightColor, leftside,charScale);
		}
		//use Jons logic for getting textures and then continue doing the same thing with the textures
	}
	
	
	/**
	 * Draws future position of ship with lines depicting path
	 */
	public void drawShadowCharacter(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Coordinate canvasC = board.offsetBoard(canvas, tileW*getShadowX(), tileH*getShadowY());
		float canvasX = canvasC.x;
		float canvasY = canvasC.y;
		canvasC.free();
		
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
				float arrowX = arrowOffX + (tileW *minX);
				float arrowY = arrowOffY + (tileH *nowY);
				Coordinate c = board.offsetBoard(canvas, arrowX, arrowY);
				arrowX = c.x;
				arrowY = c.y;
				c.free();
				float arrowWidth = tileW + SHADOW_ARROW_WIDTH*canvas.getWidth();
				float arrowHeight = SHADOW_ARROW_WIDTH*canvas.getWidth();
				canvas.drawBox(arrowX,arrowY,arrowWidth, arrowHeight, Color.BLACK);
				//canvas.drawBox(72 + 150*minX, 47 + 100*nowY, 156, 6, Color.BLACK);
			} else if (nowY != tempY && nowX == tempX){
				int minY = Math.min(nowY, tempY);
				float arrowX = arrowOffX + (tileW *nowX);
				float arrowY = arrowOffY + (tileH *minY);

				Coordinate c = board.offsetBoard(canvas, arrowX, arrowY);
				arrowX = c.x;
				arrowY = c.y;
				c.free();
				float arrowWidth = SHADOW_ARROW_WIDTH*canvas.getWidth();
				float arrowHeight = tileH + SHADOW_ARROW_WIDTH*canvas.getWidth();
				canvas.drawBox(arrowX,arrowY,arrowWidth, arrowHeight, Color.BLACK);
				//canvas.drawBox(72+150*nowX, 47+100*minY, 6, 106, Color.BLACK);
			} else {
//				System.out.println("PLEASE CHECK Character");
			}
			tempX = nowX;
			tempY = nowY;
		}
	}
	
	private void drawShield(GameCanvas canvas,GridBoard board,ActionNode an){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Coordinate c;	
		int botY = Coordinates.minYCoordinate(an.path);
		int numWithin = Coordinates.numWithinBounds(an.path, board);
		int shieldW = (int)(SHIELD_WIDTH * canvas.getWidth());
		int shieldH = (int)(tileH * numWithin);
		int shieldX = (int)(leftside ?(tileW + tileW*an.curX- SHIELD_OFFSET) :tileW*an.curX - SHIELD_OFFSET);
		int shieldY = (int)(tileH *botY);
		c = board.offsetBoard(canvas, shieldX, shieldY);
		shieldX = c.x;
		shieldY = c.y;
		c.free();
		canvas.drawBox(shieldX, shieldY, shieldW, shieldH, Color.GRAY);
	}
	
	/**
	 * Draws persisting objects
	 */
	private void drawPersisting(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Coordinate c;
		for (ActionNode an : persistingActions){
			switch (an.action.pattern){
			case SHIELD:
				drawShield(canvas,board,an);
				break;
			case STRAIGHT:
			case DIAGONAL:
				float diagX = (tileW/2 - DIAGONAL_SIZE/2 + (board.getTileWidth(canvas)*an.curX));
				float diagY = tileH/2 - DIAGONAL_SIZE/2 + (board.getTileHeight(canvas)*an.curY);
				c = board.offsetBoard(canvas, diagX, diagY);
				diagX = c.x;
				diagY = c.y;
				c.free();
				canvas.drawBox(diagX,diagY, DIAGONAL_SIZE, DIAGONAL_SIZE, color);
				break;
			default:
				break;
			}
		}
	}
	
	
	public void drawHealth(GameCanvas canvas,int count,boolean shouldDim){
		Color iconColor = this.getColor(shouldDim);
		Color waitColor = this.getActionBarColor(shouldDim, this.color.cpy());
		
		float tokenX = this.actionBar.getTotalX(canvas) - this.icon.getWidth();
		float tokenY = this.actionBar.getY(canvas, count);
		
		canvas.drawTexture(this.icon, tokenX, tokenY, this.icon.getWidth(),this.icon.getHeight(),iconColor);
		
		/** the wait width is modified by the hp already **/
		float healthW = this.actionBar.getWaitWidthNoBuffer(canvas);
		float healthH = this.actionBar.getBarHeight(canvas);
		
		float healthX = this.actionBar.getX(canvas);
		float healthY = tokenY;
		
		canvas.drawBox(healthX, healthY, healthW, healthH, waitColor);
	}
	
	public Color getActionBarColor(boolean shouldDim,Color c){
		if (isHovering){
			return c;
		}
		else if (isSelecting){
			c.mul(Color.LIGHT_GRAY);
		}
		else if (shouldDim){
			c.mul(Color.LIGHT_GRAY);
		}
		return c;
	}
	
	private Color getColor(boolean shouldDim){
		Color chosenColor = Color.WHITE.cpy();
		if (isHovering){
			return chosenColor;
		}
		else if (isSelecting){
			chosenColor = chosenColor.lerp(this.color.cpy(), lerpVal);
		}
		else if (shouldDim){
			chosenColor = Color.LIGHT_GRAY.cpy().mul(1,1,1,0.8f);
		}
		return chosenColor;
	}
	
	private Color getHighlightColor(boolean shouldDim){
		Color chosenColor = Color.DARK_GRAY.cpy().mul(1,1,1,0.0f);
		if (isHovering){
			chosenColor = this.color.cpy().mul(1,1,1,0.6f);
		}
		else if (isSelecting){
			//
		}
		else if (shouldDim){
			//
		}
		return chosenColor;
	}
	
	private Color actionBarTickColor(boolean shouldDim){
		Color chosenColor = Color.DARK_GRAY.cpy();
		if (isHovering){
			chosenColor = chosenColor.lerp(Color.WHITE, lerpVal);
		}
		else if (isSelecting){
			chosenColor = chosenColor.lerp(Color.WHITE, lerpVal);
		}
		else if (shouldDim){
			chosenColor = Color.DARK_GRAY.cpy().mul(1,1,1,0.8f);
		}
		return chosenColor;	
	}
	
	public void drawToken(GameCanvas canvas, int count,boolean shouldDim){
		float tokenX = this.actionBar.getX(canvas) + this.actionBar.getWidth(canvas)*this.castPosition - ACTIONBAR_TICK_SIZE/2;
		float tokenY = this.actionBar.getY(canvas, count);
		
		float actionBarHeight = this.actionBar.getBarHeight(canvas);
		Color c = actionBarTickColor(shouldDim);
		canvas.drawBox(tokenX, tokenY, ACTIONBAR_TICK_SIZE, actionBarHeight, c);
		
//		float tokenX = this.actionBar.getX(canvas) + this.actionBar.getWidth(canvas)*this.castPosition - icon.getWidth()/2;
//		float tokenY = this.actionBar.getY(canvas, count) - TOKEN_OFFSET_DOWN;
//		Color c = getColor(shouldDim);
//		canvas.drawTexture(icon,tokenX,tokenY,c,false);
	}

	public boolean getHovering(){
		return isHovering;
	}
	
	public void removeHovering() {
		isHovering = false;
		
	}

	public void setHovering() {
		isHovering = true;
		
	}

	@Override
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board) {
		float x_min = getXMin(canvas, board);
		float x_max = getXMax(canvas, board);
		float y_min = getYMin(canvas, board);
		float y_max = getYMax(canvas, board);
		
		return (x <= x_max && x >= x_min && y <= y_max && y >= y_min);
//		float x_token_min = getTokenXMin(canvas, board);
//		float x_token_max = getTokenXMax(canvas, board);
//		float y_token_min = getTokenYMin(canvas, board);
//		float y_token_max = getTokenYMax(canvas, board);
		
//		return (x <= x_max && x >= x_min && y <= y_max && y >= y_min
//				|| x <= x_token_max && x >= x_token_min && y <= y_token_max && y >= y_token_min);
	}
}
