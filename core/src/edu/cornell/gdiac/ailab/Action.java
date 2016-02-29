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
		SINGLE
	}
	
	/* TODO: Make separate effect class for values */
	public static enum Effect {
		REGULAR,
		SLOW
	}
	
	//TODO Selection menu draw for menu and bar tokens, pass in offsets
	
	
}
