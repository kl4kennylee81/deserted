package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import edu.cornell.gdiac.ailab.ActionNode.ActionNodeType;

public class CharacterActions {
	
	public class CharacterAction {
		int charId;
		LinkedList<MessageActionNode> queuedActions;
		
		public CharacterAction(int charId, LinkedList<ActionNode> queuedActions) {
			this.charId = charId;
			this.queuedActions = new LinkedList<MessageActionNode>();
			for (ActionNode an: queuedActions){
				this.queuedActions.add(new MessageActionNode(an));
			}
		}
		
		public LinkedList<ActionNode> convertToActionGameNodes(Action[] char_actions){
			LinkedList<ActionNode> gameNodeList = new LinkedList<ActionNode>();
			
			for (MessageActionNode msg_an: this.queuedActions){
				gameNodeList.add(new GameActionNode(msg_an,char_actions));
			}
			return gameNodeList;
			
		}
	}
	
	public class MessageActionNode extends ActionNode{
		
		String actionName;
		
		public MessageActionNode(ActionNode an) {
			super(an);
			actionName = an.getAction().getName();
			this.an_type = ActionNodeType.MESSAGE;
		}

		@Override
		public Action getAction() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getActionName(){
			return this.actionName;
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
