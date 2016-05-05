package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Texture;

public class PersistingAction extends Action {
	// How many rounds to cast action 
	int totalNumRounds;
	// Move speed across screen
	float moveSpeed;
	
	public PersistingAction(String name, int cost, int damage, int range, int size, Pattern pattern, boolean oneHit, 
			boolean canBlock,boolean needsToggle, Effect effect, String description, int totalNumRounds, float moveSpeed,Texture icon) {
		
		super(name, cost, damage, range, size, pattern, oneHit, canBlock,needsToggle, effect, description,icon);
		this.totalNumRounds = totalNumRounds;
		this.moveSpeed = moveSpeed;
	}
	
	public PersistingAction(String name, int cost, int damage, int range, int size, Pattern pattern, String path, boolean oneHit,
			boolean canBlock,boolean needsToggle, Effect effect, String description, int totalNumRounds, float moveSpeed,Texture icon) {
		
		super(name, cost, damage, range, size, pattern, oneHit, canBlock,needsToggle, effect, description, path,icon);
		this.totalNumRounds = totalNumRounds;
		this.moveSpeed = moveSpeed;
	}

}
