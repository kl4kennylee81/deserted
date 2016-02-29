package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Action.Effect;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class Character {
	String name;
	int health;
	int max_health;
	float speed;
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
	
	Action[] availableActions; 
			
	List<Action> queuedActions;
	
	public Character (int i, Texture texture, Color color) {
		this.texture = texture;
		this.color = color;
		castPosition = 0;
		queuedActions = new LinkedList<Action>();
		availableActions = new Action[4];
		
		//We will preload moves in actual game, that way we don't need Pattern and Effect from Action
		Action move = new Action("Move", 1, 0, 1, Pattern.MOVE, Effect.REGULAR, "move your dude");
		Action straight = new Action("Straight",2,2,5,Pattern.STRAIGHT, Effect.REGULAR, "straight attack");
		Action diag = new Action("Diagonal",2,2,5,Pattern.DIAGONAL,Effect.REGULAR,"diagonal attack");
		Action single = new Action("Single",3,4,10,Pattern.SINGLE,Effect.REGULAR,"single target attack");
		availableActions[0]=move;
		availableActions[1]=straight;
		availableActions[2]=diag;
		availableActions[3]=single;
		selectionMenu = new SelectionMenu(availableActions);
		switch(i) {
		case 0:
			xPosition = 0;
			yPosition = 0;
			angle = 180;
			leftside = true;
			speed = 0.007f;
			break;
		case 1:
			xPosition = 0;
			yPosition = 3;
			angle = 180;
			leftside = true;
			speed = 0.004f;
			break;
		case 2:
			xPosition = 5;
			yPosition = 0;
			angle = 0;
			leftside = false;
			speed = 0.005f;
			break;
		case 3:
			xPosition = 5;
			yPosition = 3;
			angle = 0;
			leftside = false;
			speed = 0.006f;
			break;
		}
	}
	
	boolean isAlive() {
		return health > 0;
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
	
	void setCastPosition(float castPosition) {
		this.castPosition = castPosition;
	}
	
	void setMesh(Texture texture) {
		this.texture = texture;
	}
	
	
	public void draw(GameCanvas canvas){
		drawShip(canvas);
		drawHealth(canvas);
		drawToken(canvas);
		if (isSelecting){
			drawSelection(canvas);
			selectionMenu.draw(canvas);
		}
	}
	
	public void drawShip(GameCanvas canvas){
		float canvasX = 100 + 100*xPosition;
		float canvasY = 100*yPosition;
		int angle = leftside ? 270 : 90;
		canvas.drawShip(texture, canvasX,canvasY,color,angle);
	}
	
	private void drawHealth(GameCanvas canvas){
		float canvasX = 100 + 100*xPosition;
		float canvasY = 90 + 100*yPosition;
		canvas.drawHealthBars(canvasX,canvasY,0.5f);
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
