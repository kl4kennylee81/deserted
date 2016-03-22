package edu.cornell.gdiac.ailab;

public class PersistingAction extends Action {
	// How many rounds to cast action 
	int totalNumRounds;
	// Move speed across screen
	float moveSpeed;
	
	public PersistingAction(String name, int cost, int damage, int range, Pattern pattern, Effect effect,
			String description, int totalNumRounds, float moveSpeed) {
		
		super(name, cost, damage, range, pattern, effect, description);
		this.totalNumRounds = totalNumRounds;
		this.moveSpeed = moveSpeed;
	}

}
