package edu.cornell.gdiac.ailab;

import java.util.List;

import org.omg.CORBA.SystemException;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.ActionNode.Direction;
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
        switch (menuState) {
            case SELECTING:
                checkForClicked();
                // FIXUP will fix this conditions
                if (clickedChar != null && !this.choosingTarget &&
                    this.menu != null && !this.menu.getChoosingTarget()){

                    // if the clicked character is the selected don't switch
                    if (clickedChar == selected){
                        clickedChar.isClicked = false;
                        clickedChar = null;
                    }
                    else{
                        menuState = MenuState.PEEKING;
                        this.setTargetedAction();
                        break;
                    }
                }
                else{
                    if (clickedChar!=null){
                        clickedChar.isClicked = false;
                        clickedChar = null;
                    }
                }

                updateVariables();
                int numSlots = selected.getActionBar().getUsableNumSlots();
                if (menu.canAct(numSlots) && action != null){
                    drawHighlights(true);
                }
                if (!choosingTarget){
                    // prompt choose an action when not choosing target
                    prompt = "Choose an Action";
                    this.setPrompt(prompt);
                    updateNotChoosingTarget();
                    if (selected == null) {
                        menuState = MenuState.WAITING;
                    }
                } else {
                    updateChoosingTarget();
                }
                menu.setSelectedX(selectedX);
                menu.setSelectedY(selectedY);
                break;

            case WAITING:
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
                        menuState = MenuState.SELECTING;
                        break;
                    }
                }
                break;
            case PEEKING:
                checkForClicked();
                updatePeekingVariables();
                if (action != null){
                    drawHighlights(false);
                }
                updatePeeking();

                // when you click on your original character it goes back to his selection menu
                if (InputController.pressedBack()||clickedChar == selected){
                    clickedChar.isClicked = false;
                    clickedChar = null;
                    menuState = MenuState.SELECTING;
                }
                break;

        }

    }



    private void updateNotChoosingTarget(){
		boolean mouseCondition = InputController.pressedLeftMouse() &&
				menu.contains(InputController.getMouseX(),InputController.getMouseY(), InputController.getCanvas(), board);
		int numSlots = selected.getActionBar().getUsableNumSlots();
		if ((InputController.pressedEnter() || mouseCondition)){
			if (action != null && menu.canAct(numSlots)){
				if (correctAction()){
					updateTargetedAction();
					prompt = "Choose a Target";
					if (tutorialSteps.stepOnSelection) {
						if (tutorialSteps.currStep() != null) prevText = tutorialSteps.currStep().text;
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
					if (tutorialSteps.stepOnSelection && tutorialSteps.currStep() != null) {
						if (tutorialSteps.currStep() != null) prevText = tutorialSteps.currStep().text;
						tutorialSteps.nextStep();
		
					}
					if (tutorialSteps.currStep() != null) TutorialGameplayController.targetPauseTime = tutorialSteps.currStep().timeToPause;
					TutorialGameplayController.pauseTimer = 0;
				} else {
					System.out.println("can't confirm");
					tutorialSteps.setWarning("You can\'t confirm that action just yet!", false);
				}
			}

//            if (selected != null) {
//				TutorialGameplayController.highlight_action = menu.takenSlots;
//        		System.out.println("highlight action is set to " + TutorialGameplayController.highlight_action);
//        	}
        } else if (InputController.pressedBack()){
        	// peek at it if its a move go back twice hotfix
        	ActionNode removed = menu.removeLast();
            if (removed != null){
                if (tutorialSteps.stepOnSelection) {
                	if (removed.action.pattern == Pattern.MOVE) {
                		tutorialSteps.prevStep();
                		tutorialSteps.prevStep();
                	} else tutorialSteps.prevStep();
                }
            }
            //		} else if (InputController.pressedD() && menu.canNop(numSlots)){
            /*float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,0);
             menu.add(new ActionNode(nop,actionExecute,0,0,Direction.NONE),numSlots);
             menu.resetPointer(numSlots);*/
        } else if (InputController.pressedUp() && !InputController.pressedDown()){
            //Actions go from up down, so we need to flip
            menu.changeSelected(false,numSlots);
        } else if (InputController.pressedDown() && !InputController.pressedUp()){
            menu.changeSelected(true,numSlots);
        }
    }

    protected void updateChoosingTarget(){
      boolean mouseCondition = this.mouseHighlight();
      // null check
      if (this.action == null){
        return;
      }
        switch (action.pattern){
            case SINGLE:
                TutorialGameplayController.highlight_action = menu.takenSlots + 2;
                updateChoosingSingle();
                break;
            case MOVE:
                TutorialGameplayController.highlight_action = menu.takenSlots + 1;
                updateChoosingMove();
                break;
            case DIAGONAL:
                TutorialGameplayController.highlight_action = menu.takenSlots + 2;
                if (InputController.pressedUp() && !InputController.pressedDown()){
                    direction = Direction.UP;
                } else if (InputController.pressedDown() && !InputController.pressedUp()){
                    direction = Direction.DOWN;
                }
                break;
            case SHIELD:
                TutorialGameplayController.highlight_action = menu.takenSlots + 2;
                if (InputController.pressedUp() && !InputController.pressedDown()){
                    direction = Direction.UP;
                } else if (InputController.pressedDown() && !InputController.pressedUp()){
                    direction = Direction.DOWN;
                }
                break;
            case INSTANT:
                TutorialGameplayController.highlight_action = menu.takenSlots + 3;
                break;
            case PROJECTILE:
                TutorialGameplayController.highlight_action = menu.takenSlots + 2;
                break;
            case NOP:
                break;
            default:
                break;
        }
        if (InputController.pressedEnter() || mouseCondition){
            if (correctDirection()){
                int actionExecute = menu.takenSlots + action.cost;
                int numSlots = selected.actionBar.getUsableNumSlots();
                menu.add(new ActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
                menu.setChoosingTarget(false);
                menu.resetPointer(numSlots);
                if (tutorialSteps.stepOnSelection) {
                    if (tutorialSteps.currStep() != null ) prevText = tutorialSteps.currStep().text;
                    tutorialSteps.nextStep();
                }
                if (tutorialSteps.currStep() != null) TutorialGameplayController.targetPauseTime = tutorialSteps.currStep().timeToPause;
                TutorialGameplayController.pauseTimer = 0;
            } else {
                System.out.println("wrong target");
                tutorialSteps.setWarning("Please follow the instructions!", false);
            }
//        	if (selected != null) {
//				TutorialGameplayController.highlight_action = menu.takenSlots;
//        		System.out.println("highlight action is set to " + TutorialGameplayController.highlight_action);
//        	}
        } else if (InputController.pressedBack()){
            boolean choosingTarget = menu.setChoosingTarget(false);
            if (choosingTarget){
                if (tutorialSteps.stepOnSelection) tutorialSteps.prevStep();
            }
        }
    }

    public boolean correctDirection(){
    			if (tutorialSteps.needsConfirm()){
        			return false;
        		}
        		List<TutorialAction> tas = tutorialSteps.getActions();
        		if(tas.size() > 0){
        			TutorialAction ta = tas.get(0);
            		if (ta.direction != Direction.NONE){
            			return ta.direction == direction;
            		}
            		if (ta.xPos != 0 || ta.yPos != 0){
            			return ta.xPos == selected.xPosition && ta.yPos == selected.yPosition;
            		}
        		}
        		return true;
    }

    public boolean correctAction(){
        		if (tutorialSteps.needsConfirm()){
        			return false;
        		}
        		List<TutorialAction> tas = tutorialSteps.getActions();
        		if (tas.size() > 0){
        			TutorialAction ta = tas.get(0);
        			if (ta == null){
        				System.out.println("check why null tutorialselectionmenu line 168");
        				return false;
        			}
        			return action == selected.availableActions[ta.actionId];
        		}
        		else {
        			System.out.println("check why no tutorial step on line 165");
        			return true;//if tas.size() == 0 this means the user is choosing any action of their choice
        		}
    }

    public boolean correctActions(){
    			if(tutorialSteps.getActions().size() == 0){
    				return true;
    			}
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
        		System.out.println("we went into here line 309");
        		return true;
    }
}
