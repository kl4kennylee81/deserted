package edu.cornell.gdiac.ailab;

import java.util.List;

import org.omg.CORBA.SystemException;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.SelectionMenuController.MenuState;
import edu.cornell.gdiac.ailab.TutorialSteps.TutorialAction;

public class TutorialSelectionMenuController extends SelectionMenuController{
	private static String prevText = "";
	TutorialSteps tutorialSteps;

	public TutorialSelectionMenuController(GridBoard board, List<Character> chars, TutorialSteps tutorialSteps) {
		super(board, chars);
		// TODO Auto-generated constructor stub
		this.tutorialSteps = tutorialSteps;
	}

	public void update(){
		if (this.menuState == MenuState.PEEKING){
			super.update();
			return;
		}
		switch (super.menuState) {
		case SELECTING:
			if (!choosingTarget){
				if (selected == null) {
					super.menuState = MenuState.WAITING;
				}
			}
			break;
		case WAITING:
			for (Character c : characters){
				if (c.needsSelection && c.isAlive() && !c.isAI){
					super.menuState = MenuState.SELECTING;
					break;
				}
			}
			break;
		case PEEKING:
			// when you click on your original character it goes back to his selection menu
			if (InputController.pressedBack()||clickedChar == selected){
				super.menuState = MenuState.SELECTING;
			}
			break;
		}
		checkForClicked();
		// FIXUP will fix this conditions just need it for the playtest
		if (clickedChar != null){

			// if the clicked character is the selected don't switch
			if (clickedChar == selected){
				clickedChar.isClicked = false;
				clickedChar = null;
			}
			else{
				this.menuState = MenuState.PEEKING;
				return;
			}
		}
		else{
			if (clickedChar!=null){
				clickedChar.isClicked = false;
				clickedChar = null;
			}
		}

//		if (!tutorialSteps.currStep().text.equals(prevText) && InputController.pressedEnter()){
//			if (tutorialSteps.textDone < tutorialSteps.step.text.length()){
//				if (tutorialSteps.step.text.charAt(tutorialSteps.textDone) == '\n'){
//					tutorialSteps.prevTextDone = tutorialSteps.textDone;
//					tutorialSteps.textDone++;
//				} else {
//					int pt = tutorialSteps.step.text.indexOf('\n', tutorialSteps.prevTextDone);
//					int t = tutorialSteps.step.text.indexOf('\n', tutorialSteps.textDone);
////					System.out.println("prevTextDone:"+tutorialSteps.prevTextDone + " textDone:" + tutorialSteps.textDone + " pt:" + pt + " t:" + t);
//					if (pt != -1) tutorialSteps.prevTextDone = pt == t? tutorialSteps.prevTextDone: pt;
//					tutorialSteps.textDone = t == -1? tutorialSteps.step.text.length(): t;
//				}
//			}
//			return;
//		}
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
					if (tutorialSteps.stepOnSelection) {
						prevText = tutorialSteps.currStep().text;
						tutorialSteps.nextStep();
					}
					if (tutorialSteps.currStep() != null) TutorialGameplayController.targetPauseTime = tutorialSteps.currStep().timeToPause;
					TutorialGameplayController.pauseTimer = 0;
				} else {
					System.out.println("wrong attack");
					tutorialSteps.setWarning("Please follow the instructions!", false);
				}
			} else {
				if (correctActions()){
					selected.setSelecting(false);
					selected.setQueuedActions(menu.getQueuedActions());
					selected = null;
					resetNeedsShadow();
					if (tutorialSteps.stepOnSelection) {
						prevText = tutorialSteps.currStep().text;
						tutorialSteps.nextStep();

					}
					if (tutorialSteps.currStep() != null) TutorialGameplayController.targetPauseTime = tutorialSteps.currStep().timeToPause;
					TutorialGameplayController.pauseTimer = 0;
				} else {
					System.out.println("can't confirm");
					tutorialSteps.setWarning("You can\'t confirm that action just yet!", false);
				}
			}
		} else if (InputController.pressedBack()){
			if (menu.removeLast() != null){
				if (tutorialSteps.stepOnSelection) tutorialSteps.prevStep();
			}
//		} else if (InputController.pressedD() && menu.canNop(numSlots)){
			/*float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,0);
			menu.add(anPool.newActionNode(nop,actionExecute,0,0,Direction.NONE),numSlots);
			menu.resetPointer(numSlots);*/
		} else if (InputController.pressedUp() && !InputController.pressedDown()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false,numSlots);
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			menu.changeSelected(true,numSlots);
		}
	}

	protected void updateChoosingTarget(){
		ActionNodes anPool = ActionNodes.getInstance();
		switch (action.pattern){
		case SINGLE:
			TutorialGameplayController.highlight_action = 2;
			updateChoosingSingle();
			break;
		case MOVE:
			TutorialGameplayController.highlight_action = 1;
			updateChoosingMove();
			break;
		case DIAGONAL:
			TutorialGameplayController.highlight_action = 2;
			if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			}
			break;
		case SHIELD:
			TutorialGameplayController.highlight_action = 2;
			if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			}
			break;
		case INSTANT:
			TutorialGameplayController.highlight_action = 3;
			break;
		case PROJECTILE:
			TutorialGameplayController.highlight_action = 2;
			break;
		case NOP:
			TutorialGameplayController.highlight_action = 0;
			break;
		default:
			break;
		}
		if (InputController.pressedEnter()){
			if (correctDirection()){
				float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
				int numSlots = selected.actionBar.getUsableNumSlots();
				menu.add(anPool.newActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
				menu.setChoosingTarget(false);
				menu.resetPointer(numSlots);
				if (tutorialSteps.stepOnSelection) {
					prevText = tutorialSteps.currStep().text;
					tutorialSteps.nextStep();
				}
				if (tutorialSteps.currStep() != null) TutorialGameplayController.targetPauseTime = tutorialSteps.currStep().timeToPause;
				TutorialGameplayController.pauseTimer = 0;
			} else {
				System.out.println("wrong target");
				tutorialSteps.setWarning("Please follow the instructions!", false);
			}
		} else if (InputController.pressedBack()){
			boolean choosingTarget = menu.setChoosingTarget(false);
			if (choosingTarget){
				if (tutorialSteps.stepOnSelection) tutorialSteps.prevStep();
			}
		}
	}

	public boolean correctDirection(){
		return true;
//		if (tutorialSteps.needsConfirm()){
//			return false;
//		}
//		List<TutorialAction> tas = tutorialSteps.getActions();
//		TutorialAction ta = tas.get(0);
//		if (ta.direction != Direction.NONE){
//			return ta.direction == direction;
//		}
//		if (ta.xPos != 0 || ta.yPos != 0){
//			return ta.xPos == selected.xPosition && ta.yPos == selected.yPosition;
//		}
//		return true;
	}

	public boolean correctAction(){
		return true;
//		if (tutorialSteps.needsConfirm()){
//			return false;
//		}
//		List<TutorialAction> tas = tutorialSteps.getActions();
//		if (tas.size() > 0){
//			TutorialAction ta = tas.get(0);
//			if (ta == null){
//				System.out.println("check why null tutorialselectionmenu line 168");
//				return false;
//			}
//			return action == selected.availableActions[ta.actionId];
//		}
//		else {
//			System.out.println("check why no tutorial step on line 165");
//			return true;//if tas.size() == 0 this means the user is choosing any action of their choice
//		}
	}

	public boolean correctActions(){
		return true;
//		if (!tutorialSteps.needsConfirm()){
//			return false;
//		}
//		List<ActionNode> selectedActions = menu.getQueuedActions();
//		List<TutorialAction> tas = tutorialSteps.getActions();
//		if (selectedActions.size() != tas.size()){
//			return false;
//		}
//		for (int i = 0; i < tas.size(); i++){
//			ActionNode an = selectedActions.get(i);
//			TutorialAction ta = tas.get(i);
//			if (an.action != selected.availableActions[ta.actionId]){
//				return false;
//			}
//			if (ta.direction != Direction.NONE && ta.direction != an.direction){
//				return false;
//			}
//			if ((ta.xPos != 0 || ta.yPos != 0) && (ta.xPos != an.xPosition || ta.yPos != an.yPosition)){
//				return false;
//			}
//		}
//		return true;
	}
}
