package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.AIController.Difficulty;
import edu.cornell.gdiac.ailab.Action.Effect;
import edu.cornell.gdiac.mesh.TexturedMesh;


//TODO remove last element in oldShadowXY when move is done

/*

oldShadowX.clear();
oldShadowY.clear();

*/


public class Character {
	String name;
	int health;
	int maxHealth;
	float speed;
	float castSpeed;
	//Highlight during selection screen
	boolean isHighlighted;
	int xPosition;
	int yPosition;
	//Cast bar position
	float castPosition;
	//Color for boxes in prototype
	Color color;
	Texture texture;
	SelectionMenu selectionMenu;
	int angle;
	boolean leftside;
	boolean needsSelection;
	boolean isSelecting;
	boolean needsAttack;
	
	boolean needsShadow;
	
	int shadowX;
	int shadowY;
	
	LinkedList<Integer> oldShadowX;
	LinkedList<Integer> oldShadowY;
	
	boolean isPersisting;
	boolean isAI;
	Difficulty diff;
	
	Action[] availableActions; 
	
	//PersistingActions: add persisting actions for things like shield or projectiles
			
	LinkedList<ActionNode> queuedActions;
	LinkedList<ActionNode> persistingActions;
	List<Coordinate> shieldedCoordinates;
	
	/**Constructor used by GameEngine to create characters from yaml input. */
	public Character (Texture texture, String name, int health, int maxHealth, Color color, 
						float speed, float castSpeed, int xPosition, int yPosition,
						boolean leftSide, Action[] actions){
		this.texture = texture;
		this.name = name;
		this.health = health;
		this.maxHealth = maxHealth;
		this.color = color;
		this.speed = speed;
		this.castSpeed = castSpeed;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
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
	
	
	
	public Character (int i, Texture texture, Color color) {
		// this is temporary we will pass in hp afterwards
		this.health = 10;
		this.maxHealth = 10;
		this.texture = texture;
		this.color = color;
		castPosition = 0;
		queuedActions = new LinkedList<ActionNode>();
		persistingActions = new LinkedList<ActionNode>();
		shieldedCoordinates = new LinkedList<Coordinate>();
		availableActions = new Action[4];
		
		oldShadowX = new LinkedList<Integer>();
		oldShadowY = new LinkedList<Integer>();
		
		maxHealth = health = 6;
		
		//We will preload moves in actual game, that way we don't need Pattern and Effect from Action
		/*
		availableActions[0]=move;
		availableActions[1]=straight;
		availableActions[2]=diag;
		availableActions[3]=single;
		/if (i % 2 == 0){
			PersistingAction shield = new PersistingAction("Shield",2,0,0,Pattern.SHIELD,Effect.REGULAR,"shield",100,0);
			availableActions[2] = shield;
			PersistingAction diagProj = new PersistingAction("Projectile Diagonal",2,2,5,Pattern.DIAGONAL,Effect.REGULAR,"projectile diagonal",100,0.06f);
			availableActions[1] = diagProj;
		}
		if (i%2 == 1){
			PersistingAction straightProj = new PersistingAction("Projectile Straight",2,2,5,Pattern.STRAIGHT,Effect.REGULAR,"projectile straight",100,0.1f);
			availableActions[3] = straightProj;
		}*/
		
		Action move = new Action("Move", 1, 0, 1, Pattern.MOVE, Effect.REGULAR, "move your dude");
		PersistingAction straightProj = new PersistingAction("Projectile Straight",2,2,5,Pattern.STRAIGHT,Effect.REGULAR,"projectile straight",100,0.1f);
		PersistingAction diagProj = new PersistingAction("Projectile Diagonal",2,2,5,Pattern.DIAGONAL,Effect.REGULAR,"projectile diagonal",100,0.06f);
		PersistingAction shield = new PersistingAction("Shield",2,0,0,Pattern.SHIELD,Effect.REGULAR,"shield",100,0);
		//Action straight = new Action("Straight",2,2,5,Pattern.STRAIGHT, Effect.REGULAR, "straight attack");
		//Action diag = new Action("Diagonal",2,2,5,Pattern.DIAGONAL,Effect.REGULAR,"diagonal attack");
		//Action single = new Action("Single",3,4,10,Pattern.SINGLE,Effect.REGULAR,"single target attack");
		
		availableActions[0]=move;
		availableActions[1]=straightProj;
		availableActions[2]=diagProj;
		availableActions[3]=shield;
		
		selectionMenu = new SelectionMenu(availableActions);
		switch(i) {
		case 0:
			name = "kyle";
			xPosition = 0;
			yPosition = 0;
			angle = 180;
			leftside = true;
			speed = 0.007f;
			castSpeed = 0.003f;
			break;
		case 1:
			name = "jon";
			xPosition = 0;
			yPosition = 3;
			angle = 180;
			leftside = true;
			speed = 0.004f;
			castSpeed = 0.006f;
			break;
		case 2:
			name = "cameron";
			xPosition = 5;
			yPosition = 0;
			angle = 0;
			leftside = false;
			speed = 0.005f;
			castSpeed = 0.005f;
			break;
		case 3:
			name = "ishaan";
			xPosition = 5;
			yPosition = 3;
			angle = 0;
			leftside = false;
			speed = 0.006f;
			castSpeed = 0.004f;
			break;
		}
		setShadow(xPosition,yPosition);
	}
	
	boolean isAlive() {
		return health > 0;
	}
	
	boolean isSelecting() {
		return isSelecting;
	}
	
	float getCastPosition() {
		return castPosition;
	}
	
	boolean isHighlighted() {
		return isHighlighted;
	}
	
	float getSpeed() {
		return speed;
	}
	
	String getName() {
		return name;
	}
	
	SelectionMenu getSelectionMenu() {
		return selectionMenu;
	}
	
	void setSelecting(boolean isSelecting) {
		this.isSelecting = isSelecting;
	}
	
	void setCastPosition(float castPosition) {
		this.castPosition = castPosition;
	}
	
	void setMesh(Texture texture) {
		this.texture = texture;
	}
	
	public void setQueuedActions(List<ActionNode> actions){
		this.queuedActions = (LinkedList<ActionNode>) actions;
	}
	
	public void setAI(Difficulty diff){
		this.diff = diff;
		this.isAI = true;
	}
	
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
	
	void addPersisting(ActionNode an){
		if (an.action.pattern == Pattern.SHIELD){
			an.setPersisting(0, xPosition, yPosition);
			resetShieldedCoordinates();
		} else if (an.action.pattern == Pattern.DIAGONAL || an.action.pattern == Pattern.STRAIGHT){
			if (leftside){
				an.setPersisting(0, xPosition+1, yPosition);
			} else {
				an.setPersisting(0, xPosition-1, yPosition);
			}
		}
		persistingActions.add(an);
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
		drawShip(canvas);
		drawHealth(canvas);
		drawToken(canvas);
		if (isSelecting){
			drawSelection(canvas);
			selectionMenu.draw(canvas);
		}
		if(needShadow()){
			drawShadowShip(canvas);
		}
		if(hasPersisting()){
			drawPersisting(canvas);
		}
	}
	
	public void drawShip(GameCanvas canvas){
		float canvasX = 100 + 100*xPosition;
		float canvasY = 100*yPosition;
		int angle = leftside ? 270 : 90;
		canvas.drawShip(texture, canvasX,canvasY,color,angle);
	}
	
	public void drawShadowShip(GameCanvas canvas){
		//DRAW LINES TO OUTLINE SHIP MOVEMENT
		float canvasX = 100 + 100*shadowX;
		float canvasY = 100*shadowY;
		int angle = leftside ? 270 : 90;
		canvas.drawShip(texture, canvasX,canvasY,color.cpy().lerp(Color.CLEAR, 0.3f),angle);
		int tempX = shadowX;
		int tempY = shadowY;
		int nowX;
		int nowY;
		for (int i = 1; i < oldShadowX.size(); i++){
			nowX = oldShadowX.get(i);
			nowY = oldShadowY.get(i);
			if (nowX != tempX && nowY == tempY){
				int minX = Math.min(nowX, tempX);
				canvas.drawBox(147 + 100*minX, 47 + 100*nowY, 106, 6, Color.BLACK);
			} else if (nowY != tempY && nowX == tempX){
				int minY = Math.min(nowY, tempY);
				canvas.drawBox(147+100*nowX, 47+100*minY, 6, 106, Color.BLACK);
			} else {
				System.out.println("UNEXPECTED BEHAVIOR AS OF 3/1");
			}
			
			tempX = nowX;
			tempY = nowY;
		}
	}
	
	private void drawPersisting(GameCanvas canvas){
		for (ActionNode an : persistingActions){
			switch (an.action.pattern){
			case SHIELD:
				if (leftside){
					if (an.yPosition==3){
						canvas.drawBox(195+100*an.curX, 100*yPosition, 10, 200, Color.BROWN);
					} else {
						canvas.drawBox(195+100*an.curX, 100*yPosition-100, 10, 200, Color.BROWN);
					}
				} else {
					if (an.yPosition==3){
						canvas.drawBox(95+100*an.curX, 100*yPosition, 10, 200, Color.BROWN);
					} else {
						canvas.drawBox(95+100*an.curX, 100*yPosition-100, 10, 200, Color.BROWN);
					}
				}
				break;
			case STRAIGHT:
			case DIAGONAL:
				canvas.drawBox(140+100*an.curX, 40+100*an.curY, 20, 20, Color.GRAY);
				break;
			default:
				break;
			}
		}
	}
	
	private void drawHealth(GameCanvas canvas){
		float canvasX = 100 + 100*xPosition;
		float canvasY = 90 + 100*yPosition;
		canvas.drawHealthBars(canvasX,canvasY,((float)health)/maxHealth);
	}
	
	private void drawToken(GameCanvas canvas){
		float canvasX = 100+600*castPosition;
		float canvasY = leftside ? 720 : 680;
		canvas.drawToken(canvasX,canvasY, color);
	}
	
	private void drawSelection(GameCanvas canvas){
		float canvasX = 150 + 100*xPosition;
		float canvasY = 100 + 100*yPosition;
		canvas.drawSelection(canvasX,canvasY);
	}
	
	public void update(){
		
	}
	
	
}
