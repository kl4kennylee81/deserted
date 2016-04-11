package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.TutorialSteps.TutorialAction;

public class TutorialAIController {
	GridBoard board;
	List<Character> chars;
	TutorialSteps tutorialSteps;
	
	Character selected;
	
	public TutorialAIController(GridBoard board, List<Character> chars, TutorialSteps tutorialSteps){
		this.board = board;
		this.chars = chars;
		this.tutorialSteps = tutorialSteps;
		
		selected = null;
	}
	
	public void update(){
		for (Character c : chars){
			if (c.needsSelection && c.isAI){
				selected = c;
				setActions();
				c.needsSelection = false;
				tutorialSteps.nextStep();
			}
		}
	}
	
	public void setActions(){
		System.out.println(tutorialSteps.getActions());
		System.out.println(tutorialSteps.step.text);
		if (tutorialSteps.getActions() == null){
			return;
		}
		
		List<ActionNode> actions = new LinkedList<ActionNode>();
		ActionNodes anPool = ActionNodes.getInstance();
		int startPoint = 0;
	
		for (TutorialAction ta : tutorialSteps.getActions()){
			Action action = selected.availableActions[ta.actionId];
			float executePoint = selected.actionBar.castPoint + (selected.getInterval() * (startPoint + action.cost));
			ActionNode a = anPool.newActionNode(action, executePoint, ta.xPos, ta.yPos, ta.direction);
			startPoint += action.cost;
			actions.add(a);
		}
		
		selected.setQueuedActions(actions);
	}
}
