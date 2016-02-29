package edu.cornell.gdiac.ailab;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class Character {
	String name;
	int health;
	float speed;
	//Highlight during selection screen
	boolean isHighlighted;
	int xPosition;
	int yPosition;
	//Cast bar position
	float castPosition;
	//Color for boxes in prototype
	Color color;
	TexturedMesh texture;
	SelectionMenu selectionMenu;
	
	Action[] availableActions; 
			
	List<Action> queuedActions;
	
	public Character (int i) {
		
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
	
	
	public void draw(GameCanvas canvas){
		
	}
	
	public void update(){
		
	}
	
	
}
