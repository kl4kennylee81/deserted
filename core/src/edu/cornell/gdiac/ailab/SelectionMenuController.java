package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.Effect.Type;

public class SelectionMenuController {
	/** Models */
	GridBoard board;
	List<Character> characters;
	ActionBar bar;
	
	//TODO: Change how I handle this
	/** NOP action that is available for every character */
	Action nop;
	
	/** Controller variables */
	Character selected;
	boolean isDone;
	SelectionMenu menu;
	Action action;
	boolean choosingTarget;
	boolean leftside;
	
	/** Current shadow position */
	int shadowX;
	int shadowY;
	/** Current target selection */
	int selectedX;
	int selectedY;
	
	/** Board height and width in number of tiles */
	int boardWidth;
	int boardHeight;
	
	/** Starting positions */
	private static final int SINGLE_X_LEFT = 3;
	private static final int SINGLE_X_RIGHT = 2;
	private static final int SINGLE_Y = 3;
	
	//TODO: Change to be 0 for down and anything else is up
	/** Attack direction values */
	Direction direction;
	
	public SelectionMenuController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		
		isDone = false;
		selected = null;
		menu = null;
		action = null;
		choosingTarget = false;
		
		boardWidth = board.width;
		boardHeight = board.height;
		nop = new Action("NOP", 1, 0, 0, Pattern.NOP, new Effect(0, Type.REGULAR, 0), "no action");
	}
	
	public void update(){
		if (selected != null){
			updateVariables();
			if (menu.canAct()){
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
	
	private void updateVariables(){
		menu = selected.getSelectionMenu();
		action = menu.getSelectedAction();
		choosingTarget =  menu.getChoosingTarget();
		shadowX = selected.getShadowX();
		shadowY = selected.getShadowY();
		selectedX = menu.getSelectedX();
		selectedY = menu.getSelectedY();
		leftside = selected.leftside;
		board.occupy(characters, selected);
	}
	
	/**
	 * Update when an action is not targeting yet
	 */
	private void updateNotChoosingTarget(){
		if (InputController.pressedEnter()){
			selected.setSelecting(false);
			selected.setQueuedActions(menu.getQueuedActions());
			selected = null;
			resetNeedsShadow();
		} else if (InputController.pressedA() && menu.canAct()){
			updateTargetedAction();
		} else if (InputController.pressedS()){
			menu.removeLast();
		} else if (InputController.pressedD() && menu.canNop()){
			menu.add(new ActionNode(nop,bar.castPoint+(action.cost+menu.takenSlots)*0.075f,0,0,Direction.NONE));
			menu.resetPointer();
		} else if (InputController.pressedUp() && !InputController.pressedDown()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false);
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			menu.changeSelected(true);
		}
	}
	
	/** 
	 * Select an action to start targeting
	 */
	private void updateTargetedAction(){
		if (action.pattern == Pattern.STRAIGHT){
			menu.setChoosingTarget(true);
		} else if (action.pattern == Pattern.SINGLE){
			selectedX = leftside ? SINGLE_X_LEFT : SINGLE_X_RIGHT;
			selectedY = SINGLE_Y;
			menu.setChoosingTarget(true);
		} else if (action.pattern == Pattern.MOVE){
			if (!board.isOccupied(shadowX, shadowY+1)){
				direction = Direction.UP;
			} else if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
				direction = Direction.RIGHT;
			} else if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowY == boardWidth/2)){
				direction = Direction.LEFT;
			} else if (!board.isOccupied(shadowX, shadowY-1)){
				direction = Direction.DOWN;
			} else {
				System.out.println("do something to tell them they cant move");
			}
			menu.setChoosingTarget(true);
		} else if (action.pattern == Pattern.DIAGONAL){
			if (shadowY < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			menu.setChoosingTarget(true);
		} else if (action.pattern == Pattern.SHIELD){
			if (shadowY < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			menu.setChoosingTarget(true);
		}
	}
	
	private void updateChoosingTarget(){
		switch (action.pattern){
		case SINGLE:
			updateChoosingSingle();
			break;
		case MOVE:
			updateChoosingMove();
			break;
		case DIAGONAL:
			if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			} 
			break;
		case SHIELD:
			if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			} 
			break;
		default:
			break;
		}
		if (InputController.pressedEnter()){
			menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*((1-bar.castPoint)/menu.TOTAL_SLOTS),selectedX,selectedY,direction));
			menu.setChoosingTarget(false);
			selected.setSelecting(false);
			selected.setQueuedActions(menu.getQueuedActions());
			selected = null;
			resetNeedsShadow();
		}
		if (InputController.pressedA()){
			menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*((1-bar.castPoint)/menu.TOTAL_SLOTS),selectedX,selectedY,direction));
			menu.setChoosingTarget(false);
			menu.resetPointer();
		} else if (InputController.pressedS()){
			menu.setChoosingTarget(false);
		}
	}
	
	private void updateChoosingSingle(){
		if (InputController.pressedUp() && !InputController.pressedDown()){
			selectedY+=1;
			selectedY %= boardHeight;
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			selectedY-=1;
			if (selectedY < 0){
				selectedY += boardHeight;
			}
		} else if (InputController.pressedLeft() && !InputController.pressedRight()){
			selectedX -= 1;
			if (leftside && selectedX<boardWidth/2){
				selectedX+=boardWidth/2;
			} else if (!leftside && selectedX<0){
				selectedX+=boardWidth/2;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			selectedX += 1;
			if (leftside && selectedX > boardWidth-1){
				selectedX -= boardWidth/2;
			} else if (!leftside && selectedX > boardWidth/2-1){
				selectedX -= boardWidth/2;
			}
		}
	}
	
	private void updateChoosingMove(){
		
		//Find out why moving is weird on right side
		//System.out.println("choosing");
		
		//Need to check in all of these if its a valid move;
		if (InputController.pressedUp() && !InputController.pressedDown()){
			if (!board.isOccupied(shadowX, shadowY+1)){
				direction = Direction.UP;
			}
			
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			if (!board.isOccupied(shadowX, shadowY-1)){
				direction = Direction.DOWN;
			}
		} else if (InputController.pressedLeft() && !InputController.pressedRight()){
			//if (not occupied) and (not rightside at x=3)
			if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowX == boardWidth/2)){
				direction = Direction.LEFT;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
				direction = Direction.RIGHT;
			}
		}
	}
	
	private void setNeedsShadow(){
		for (Character c : characters){
			if (c.leftside == selected.leftside){
				c.needsShadow = true;
			}
		}
	}
	
	private void resetNeedsShadow(){
		for (Character c : characters){
			c.needsShadow = false;
		}
	}
	
	public void drawHighlights(){
		if (action.pattern == Pattern.STRAIGHT){
			drawStraight();
		}
		if (action.pattern == Pattern.SINGLE){
			drawSingle();
		}
		if (action.pattern == Pattern.MOVE){
			drawMove();
		}
		if (action.pattern == Pattern.DIAGONAL){
			drawDiagonal();
		}
		if (action.pattern == Pattern.SHIELD){
			drawShield();
		}
	}
	
	public void drawStraight(){
		if (leftside) {
			for (int i = shadowX+1; i < boardWidth; i++){
				if (choosingTarget){
					board.setHighlighted(i,shadowY);
				} else {
					board.setCanTarget(i, shadowY);
				}
			}
		} else {
			for (int i = shadowX-1; i >= 0; i--){
				if (choosingTarget){
					board.setHighlighted(i,shadowY);
				} else {
					board.setCanTarget(i, shadowY);
				}
			}
		}
	}
	public void drawSingle(){
		if (choosingTarget){
			board.setHighlighted(selectedX, selectedY);
		} else {
			board.setCanTargetSide(leftside);
		}
	}
	
	public void drawMove(){
		//if not leftside and at x=2 then draw
		if (!(leftside && shadowX == boardWidth/2-1)){
			board.setCanTarget(shadowX+1, shadowY);
		}
		if (!(!leftside && shadowX == boardWidth/2)){
			board.setCanTarget(shadowX-1, shadowY);
		}
		board.setCanTarget(shadowX, shadowY-1);
		board.setCanTarget(shadowX, shadowY+1);
		if (choosingTarget){
			switch (direction){
			case UP:
				board.setHighlighted(shadowX, shadowY+1);
				break;
			case DOWN:
				board.setHighlighted(shadowX, shadowY-1);
				break;
			case LEFT:
				board.setHighlighted(shadowX-1, shadowY);
				break;
			case RIGHT:
				board.setHighlighted(shadowX+1, shadowY);
				break;
			default:
				break;
			}
		}
	}
	
	public void drawDiagonal(){
		if (leftside){
			shadowX++;
			for (int i = 0; i < boardHeight; i++){
				board.setCanTarget(shadowX+i, shadowY+i);
				board.setCanTarget(shadowX+i, shadowY-i);
			}
			if (choosingTarget){
				for (int i = 0; i < boardHeight; i++){
					if (direction == Direction.UP){
						board.setHighlighted(shadowX+i, shadowY+i);
					} else {
						board.setHighlighted(shadowX+i, shadowY-i);
					}
				}
			}
		} else {
			shadowX--;
			for (int i = 0; i < boardHeight; i++){
				board.setCanTarget(shadowX-i, shadowY+i);
				board.setCanTarget(shadowX-i, shadowY-i);
			}
			if (choosingTarget){
				for (int i = 0; i < boardHeight; i++){
					if (direction == Direction.UP){
						board.setHighlighted(shadowX-i, shadowY+i);
					} else {
						board.setHighlighted(shadowX-i, shadowY-i);
					}
				}
			}
		}
	}
	
	public void drawShield(){
		board.setCanTarget(shadowX, shadowY+1);
		board.setCanTarget(shadowX, shadowY);
		board.setCanTarget(shadowX, shadowY-1);
		if (choosingTarget){
			if (direction == Direction.UP){
				board.setHighlighted(shadowX, shadowY+1);
				board.setHighlighted(shadowX, shadowY);
			} else {
				board.setHighlighted(shadowX, shadowY);
				board.setHighlighted(shadowX, shadowY-1);
			}
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}
