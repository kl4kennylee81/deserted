package edu.cornell.gdiac.ailab;

public class PersistingAction extends Action {
	// How many rounds to cast action 
	int totalNumRounds;
	// Move speed across screen
	float moveSpeed;
	
	public PersistingAction(String name, int cost, int damage, int range, int size, Pattern pattern, boolean oneHit, 
			boolean canBlock,boolean needsToggle, Effect effect, String description, int totalNumRounds, float moveSpeed) {
		
		super(name, cost, damage, range, size, pattern, oneHit, canBlock,needsToggle, effect, description);
		this.totalNumRounds = totalNumRounds;
		this.moveSpeed = moveSpeed;
	}
	
	public PersistingAction(String name, int cost, int damage, int range, int size, Pattern pattern, String path, boolean oneHit,
			boolean canBlock,boolean needsToggle, Effect effect, String description, int totalNumRounds, float moveSpeed) {
		
		super(name, cost, damage, range, size, pattern, oneHit, canBlock,needsToggle, effect, description, path);
		this.totalNumRounds = totalNumRounds;
		this.moveSpeed = moveSpeed;
	}

}
