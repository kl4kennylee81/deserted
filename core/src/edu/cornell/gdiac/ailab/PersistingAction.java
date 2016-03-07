package edu.cornell.gdiac.ailab;

public class PersistingAction extends Action {
	// How many frames to cast action 
	int castLength;
	// Move speed across screen
	float moveSpeed;
	
	public PersistingAction(String name, int cost, int damage, int range, Pattern pattern, Effect effect,
			String description, int castLength, float moveSpeed) {
		
		super(name, cost, damage, range, pattern, effect, description);
		this.castLength = castLength;
		this.moveSpeed = moveSpeed;
	}

}
