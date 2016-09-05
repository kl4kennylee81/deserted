package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.CharacterActions.MessageActionNode;

public class GameActionNode extends ActionNode {

	Action action;
		
	public GameActionNode(Action action, int executeSlot, int xPos, int yPos) {
		super(action, executeSlot, xPos, yPos);
		this.an_type = ActionNodeType.GAME;
		this.action = action;
	}
	
	public GameActionNode(Action action, int executeSlot, int xPos, int yPos, Direction direction){
		super(action,executeSlot,xPos,yPos,direction);
		this.action = action;
		this.an_type = ActionNodeType.GAME;
	}
	
	public GameActionNode(MessageActionNode msg_an,Action[] char_actions){
		super(msg_an);
		this.an_type = ActionNodeType.GAME;
		this.action = new Action();
		for (Action a:char_actions){
			if (msg_an.getActionName().equals(a.getName())){
				System.out.println(a.getName());
				this.action = a;
			}
		}
	}

	@Override
	public Action getAction() {
		return action;
	}
	
}
