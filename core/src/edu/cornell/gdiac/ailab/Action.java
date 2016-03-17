package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class Action {
	String name;
	int cost;
	int damage;
	int range;
	Pattern pattern;
	Effect effect;
	String description;
	Animation animation;
	
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
	
	public void setAnimation(Animation animation){
		this.animation = animation;
	}
}
