package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.TutorialSteps.TutorialAction;

public class TutorialSelectionMenuController extends SelectionMenuController{
	TutorialSteps tutorialSteps;
	
	public TutorialSelectionMenuController(GridBoard board, List<Character> chars, TutorialSteps tutorialSteps) {
		super(board, chars);
		// TODO Auto-generated constructor stub
		this.tutorialSteps = tutorialSteps;
	}
	
	public void update(){
		if (selected != null){
			updateVariables();
			int numSlots = selected.getActionBar().getUsableNumSlots();
			if (menu.canAct(numSlots) && action != null){
				drawHighlights();
			}
			if (!choosingTarget){
				updateNotChoosingTarget();
			} else {
				updateChoosingTarget();
			}
			menu.setSelectedX(selectedX);
			menu.setSelectedY(selectedY);
		} else {
			isDone = true;
			for (Character c : characters){
				if (c.needsSelection && c.isAlive() && !c.isAI){
					isDone = false;
					selected = c;
					SelectionMenu menu = c.getSelectionMenu();
					menu.reset();
					c.needsSelection = false;
					c.setSelecting(true);
					setNeedsShadow();
					break;
				}
			}
		}	
	}
	
	private void updateNotChoosingTarget(){
		boolean mouseCondition = InputController.pressedLeftMouse();// && 
//				action.contains(InputController.getMouseX(), InputController.getMouseX(), InputController.getCanvas(), board);
		ActionNodes anPool = ActionNodes.getInstance();
		int numSlots = selected.getActionBar().getUsableNumSlots();
		if ((InputController.pressedEnter() || mouseCondition)){
			if (action != null && menu.canAct(numSlots)){
				updateTargetedAction();
				prompt = "Choose a Target";
			} else {
				if (correctActions()){
					selected.setSelecting(false);
					selected.setQueuedActions(menu.getQueuedActions());
					selected = null;
					resetNeedsShadow();
					tutorialSteps.nextStep();
				} else {
					System.out.println("prompt them to choose different attack");
				}
			}
		} else if (InputController.pressedBack()){
			menu.removeLast();
		} else if (InputController.pressedD() && menu.canNop(numSlots)){
			/*float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,0);
			menu.add(anPool.newActionNode(nop,actionExecute,0,0,Direction.NONE),numSlots);
			menu.resetPointer(numSlots);*/
		} else if (InputController.pressedW() && !InputController.pressedS()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false,numSlots);
		} else if (InputController.pressedS() && !InputController.pressedW()){
			menu.changeSelected(true,numSlots);
		}
	}
	
	public boolean correctActions(){
		List<ActionNode> selectedActions = menu.getQueuedActions();
		List<TutorialAction> tas = tutorialSteps.getActions();
		if (selectedActions.size() != tas.size()){
			return false;
		}
		for (int i = 0; i < tas.size(); i++){
			ActionNode an = selectedActions.get(i);
			TutorialAction ta = tas.get(i);
			if (an.action != selected.availableActions[ta.actionId]){
				return false;
			}
			if (an.direction != ta.direction){
				return false;
			}
		}
		return true;
	}
}
