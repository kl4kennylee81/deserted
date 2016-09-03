package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

public class CharacterActions {
	public class CharacterAction {
		int charId;
		LinkedList<ActionNode> queuedActions;
		
		public CharacterAction(int charId, LinkedList<ActionNode> queuedActions) {
			this.charId = charId;
			this.queuedActions = queuedActions;
		}
	}
	
	LinkedList<CharacterAction> charActions;
	
	public CharacterActions () {
		charActions = new LinkedList<CharacterAction>();
	}
	
	public void addCharacterAction(int charId, LinkedList<ActionNode> queuedActions) {
		charActions.add(new CharacterAction(charId, queuedActions));
	}
}
