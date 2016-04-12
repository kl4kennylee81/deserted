package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.TutorialSteps.TutorialAction;

public class TutorialSelectionMenuController extends SelectionMenuController{
	TutorialSteps tutorialSteps;
	
	public TutorialSelectionMenuController(GridBoard board, List<Character> chars, TutorialSteps tutorialSteps) {
		super(board, chars);
		// TODO Auto-generated constructor stub
		this.tutorialSteps = tutorialSteps;
	}
	
	public void update(){
		if (InputController.pressedSpace()){
			if (tutorialSteps.timeElapsed <= tutorialSteps.step.waitTime){
				tutorialSteps.timeElapsed = tutorialSteps.step.waitTime;
				tutorialSteps.textDone = tutorialSteps.step.text.length();
			}
		}
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
				if (correctAction()){
					updateTargetedAction();
					prompt = "Choose a Target";
					tutorialSteps.nextStep();
				} else {
					System.out.println("wrong attack");
					tutorialSteps.setWarning("Please follow the instructions!");
				}
			} else {
				if (correctActions()){
					selected.setSelecting(false);
					selected.setQueuedActions(menu.getQueuedActions());
					selected = null;
					resetNeedsShadow();
					tutorialSteps.nextStep();
				} else {
					System.out.println("can't confirm");
					tutorialSteps.setWarning("You can\'t confirm that action just yet!");
				}
			}
		} else if (InputController.pressedBack()){
			//menu.removeLast();
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
	
	protected void updateChoosingTarget(){
		ActionNodes anPool = ActionNodes.getInstance();
		switch (action.pattern){
		case SINGLE:
			updateChoosingSingle();
			break;
		case MOVE:
			updateChoosingMove();
			break;
		case DIAGONAL:
			if (InputController.pressedW() && !InputController.pressedS()){
				direction = Direction.UP;
			} else if (InputController.pressedS() && !InputController.pressedW()){
				direction = Direction.DOWN;
			} 
			break;
		case SHIELD:
			if (InputController.pressedW() && !InputController.pressedS()){
				direction = Direction.UP;
			} else if (InputController.pressedS() && !InputController.pressedW()){
				direction = Direction.DOWN;
			} 
			break;
		case INSTANT:
			break;
		case PROJECTILE:
			break;
		case NOP:
			break;
		default:
			break;
		}
		if (InputController.pressedEnter()){
			if (correctDirection()){
				float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
				int numSlots = selected.actionBar.numSlots;
				menu.add(anPool.newActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
				menu.setChoosingTarget(false);
				menu.resetPointer(numSlots);
				tutorialSteps.nextStep();
			} else {
				System.out.println("wrong target");
				tutorialSteps.setWarning("Please follow the instructions!");
			}
		} else if (InputController.pressedBack()){
			//menu.setChoosingTarget(false);
		}
	}
	
	public boolean correctDirection(){
		if (tutorialSteps.needsConfirm()){
			return false;
		}
		List<TutorialAction> tas = tutorialSteps.getActions();
		TutorialAction ta = tas.get(0);
		if (ta.direction != Direction.NONE){
			return ta.direction == direction;
		}
		if (ta.xPos != 0 || ta.yPos != 0){
			return ta.xPos == selected.xPosition && ta.yPos == selected.yPosition;
		}
		return true;
	}
	
	public boolean correctAction(){
		if (tutorialSteps.needsConfirm()){
			return false;
		}
		List<TutorialAction> tas = tutorialSteps.getActions();
		TutorialAction ta = tas.get(0);
		return action == selected.availableActions[ta.actionId];
	}
	
	public boolean correctActions(){
		if (!tutorialSteps.needsConfirm()){
			return false;
		}
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
			if (ta.direction != Direction.NONE && ta.direction != an.direction){
				return false;
			}
			if ((ta.xPos != 0 || ta.yPos != 0) && (ta.xPos != an.xPosition || ta.yPos != an.yPosition)){
				return false;
			}
		}
		return true;
	}
}
