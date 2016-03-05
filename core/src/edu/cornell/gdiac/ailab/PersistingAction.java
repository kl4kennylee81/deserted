package edu.cornell.gdiac.ailab;

public class PersistingAction extends Action {
	
	int castLength; //maybe an float? we'll see
	int moveSpeed; //maybe a float? lol
	
	

	public PersistingAction(String name, int cost, int damage, int range, Pattern pattern, Effect effect,
			String description, int castLength, int moveSpeed) {
		super(name, cost, damage, range, pattern, effect, description);
		
		this.castLength = castLength;
		this.moveSpeed = moveSpeed;
		// TODO Auto-generated constructor stub
	}

}
