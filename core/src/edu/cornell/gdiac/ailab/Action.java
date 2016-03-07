package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class Action {
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

	public static enum Pattern {
		MOVE,
		SHIELD,
		STRAIGHT,
		DIAGONAL,
		SINGLE,
		NOP
	}
	
	/* TODO: Make separate effect class for values */
	public static enum Effect {
		REGULAR,
		SLOW
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
}
