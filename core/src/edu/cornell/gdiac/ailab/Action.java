package edu.cornell.gdiac.ailab;


import edu.cornell.gdiac.mesh.TexturedMesh;

public class Action implements GUIElement {
	String name;
	int cost;
	int damage;
	int range;
	Pattern pattern;
	Effect effect;
	String description;
	TexturedMesh menuToken;
	TexturedMesh barToken;
	TexturedMesh actionEffects;
	float x;
	float y;
	float width;
	float height;
	int position;

	public static enum Pattern {
		MOVE,
		SHIELD,
		STRAIGHT,
		DIAGONAL,
		SINGLE,
		NOP
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
