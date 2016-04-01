package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Effect.Type;

public class SelectionMenuController {
	/** Models */
	GridBoard board;
	List<Character> characters;
	
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
	private String prompt;
	
	//TODO: Change to be 0 for down and anything else is up
	/** Attack direction values */
	Direction direction;
	
	public SelectionMenuController(GridBoard board, List<Character> chars) {
		this.board = board;
		this.characters = chars;
		
		isDone = false;
		selected = null;
		menu = null;
		action = null;
		choosingTarget = false;
		
		boardWidth = board.width;
		boardHeight = board.height;
		nop = new Action("NOP", 1, 0, 0, Pattern.NOP, new Effect(0, Type.REGULAR, 0, "Nope"), "no action");
	}
	
	public void update(){
		if (selected != null){
			updateVariables();
			int numSlots = selected.actionBar.getNumSlots();
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
	
	private void updateVariables(){
		menu = selected.getSelectionMenu();
		action = menu.getSelectedAction();
		choosingTarget =  menu.getChoosingTarget();
		shadowX = selected.getShadowX();
		shadowY = selected.getShadowY();
		selectedX = menu.getSelectedX();
		selectedY = menu.getSelectedY();
		leftside = selected.leftside;
		board.reset();
	}
	
	/**
	 * Update when an action is not targeting yet
	 */
	private void updateNotChoosingTarget(){
		boolean mouseCondition = InputController.pressedLeftMouse();// && 
//				action.contains(InputController.getMouseX(), InputController.getMouseX(), InputController.getCanvas(), board);
		ActionNodes anPool = ActionNodes.getInstance();
		int numSlots = selected.actionBar.numSlots;
		if ((InputController.pressedEnter() || mouseCondition)){
			if (action != null && menu.canAct(numSlots)){
				updateTargetedAction();
				prompt = "Choose a Target";
			} else {
				selected.setSelecting(false);
				selected.setQueuedActions(menu.getQueuedActions());
				selected = null;
				resetNeedsShadow();
			}
		} else if (InputController.pressedBack()){
			menu.removeLast();
		} else if (InputController.pressedD() && menu.canNop(numSlots)){
			float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,0);
			menu.add(anPool.newActionNode(nop,actionExecute,0,0,Direction.NONE),numSlots);
			menu.resetPointer(numSlots);
		} else if (InputController.pressedW() && !InputController.pressedS()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false,numSlots);
		} else if (InputController.pressedS() && !InputController.pressedW()){
			menu.changeSelected(true,numSlots);
		}
	}
	
	/** 
	 * Select an action to start targeting
	 */
	private void updateTargetedAction(){
		switch (action.pattern){
		case STRAIGHT:
			menu.setChoosingTarget(true);
			break;
		case SINGLE:
			selectedX = leftside ? SINGLE_X_LEFT : SINGLE_X_RIGHT;
			selectedY = SINGLE_Y;
			menu.setChoosingTarget(true);
			break;
		case MOVE:
			if (board.isInBounds(shadowX, shadowY+1)){
				direction = Direction.UP;
			} else if (board.isInBounds(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
				direction = Direction.RIGHT;
			} else if (board.isInBounds(shadowX-1, shadowY) && !(!leftside && shadowY == boardWidth/2)){
				direction = Direction.LEFT;
			} else if (board.isInBounds(shadowX, shadowY-1)){
				direction = Direction.DOWN;
			} else {
				System.out.println("do something to tell them they cant move");
			}
			menu.setChoosingTarget(true);
			break;
		case DIAGONAL:
			if (shadowY < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			menu.setChoosingTarget(true);
			break;
		case SHIELD:
			if (shadowY < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			menu.setChoosingTarget(true);
			break;
		case INSTANT:
			menu.setChoosingTarget(true);
			break;
		case PROJECTILE:
			menu.setChoosingTarget(true);
			break;
		case NOP:
			break;
		default:
			break;
		}
	}
	
	private void updateChoosingTarget(){
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
			float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
			int numSlots = selected.actionBar.numSlots;
			menu.add(anPool.newActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
			menu.setChoosingTarget(false);
			menu.resetPointer(numSlots);
		} else if (InputController.pressedBack()){
			menu.setChoosingTarget(false);
		}
	}
	
	private void updateChoosingSingle(){
		direction = Direction.NONE;
		if (InputController.pressedW() && !InputController.pressedS()){
			selectedY+=1;
			selectedY %= boardHeight;
		} else if (InputController.pressedS() && !InputController.pressedW()){
			selectedY-=1;
			if (selectedY < 0){
				selectedY += boardHeight;
			}
		} else if (InputController.pressedA() && !InputController.pressedD()){
			selectedX -= 1;
			if (leftside && selectedX<boardWidth/2){
				selectedX+=boardWidth/2;
			} else if (!leftside && selectedX<0){
				selectedX+=boardWidth/2;
			}
		} else if (InputController.pressedD() && !InputController.pressedA()){
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
		if (InputController.pressedW() && !InputController.pressedS()){
			if (board.isInBounds(shadowX, shadowY+1)){
				direction = Direction.UP;
			}
			
		} else if (InputController.pressedS() && !InputController.pressedW()){
			if (board.isInBounds(shadowX, shadowY-1)){
				direction = Direction.DOWN;
			}
		} else if (InputController.pressedA() && !InputController.pressedD()){
			//if (not occupied) and (not rightside at x=3)
			if (board.isInBounds(shadowX-1, shadowY) && !(!leftside && shadowX == boardWidth/2)){
				direction = Direction.LEFT;
			}
		} else if (InputController.pressedD() && !InputController.pressedA()){
			if (board.isInBounds(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
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
		switch (action.pattern){
		case STRAIGHT:
			drawStraight();
			break;
		case SINGLE:
			drawSingle();
			break;
		case MOVE:
			drawMove();
			break;
		case DIAGONAL:
			drawDiagonal();
			break;
		case SHIELD:
			drawShield();
			break;
		case INSTANT:
			drawPath(false);
			break;
		case PROJECTILE:
			drawPath(true);
			break;
		case NOP:
			break;
		default:
			break;
		}
	}
	
	public void drawStraight(){
		if (leftside) {
			for (int i = 1; i <= action.range; i++){
				if (choosingTarget){
					board.setHighlighted(shadowX+i,shadowY);
				} else {
					board.setCanTarget(shadowX+i, shadowY);
				}
			}
		} else {
			for (int i = 1; i <= action.range; i++){
				if (choosingTarget){
					board.setHighlighted(shadowX-i,shadowY);
				} else {
					board.setCanTarget(shadowX-i, shadowY);
				}
			}
		}
	}
	
	public void drawPath(boolean isProjectile){
		if (action.path == null){
			return;
		}
		Coordinate[] path = action.path;
		if (leftside) {
			for (int i = 0; i < path.length; i++){
				int x = shadowX + path[i].x;
				int y = shadowY + path[i].y;
				if (shadowX == x && shadowY == y){
					continue;
				}
				else if ((!board.isInBounds(x, y)) && isProjectile){
					break;
				}
				else if (choosingTarget){
					board.setHighlighted(x,y);
				} else {
					board.setCanTarget(x, y);
				}
			}
		} else {
			for (int i = 0; i < action.path.length; i++){
				int x = shadowX - path[i].x;
				int y = shadowY + path[i].y;
				if (shadowX == x && shadowY == y){
					continue;
				}
				else if ((!board.isInBounds(x, y)) && isProjectile){
					break;
				}
				if (choosingTarget){
					board.setHighlighted(x,y);
				} else {
					board.setCanTarget(x,y);
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
			for (int i = 0; i < action.range; i++){
				board.setCanTarget(shadowX+i, shadowY+i);
				board.setCanTarget(shadowX+i, shadowY-i);
			}
			if (choosingTarget){
				for (int i = 0; i < action.range; i++){
					if (direction == Direction.UP){
						board.setHighlighted(shadowX+i, shadowY+i);
					} else {
						board.setHighlighted(shadowX+i, shadowY-i);
					}
				}
			}
		} else {
			shadowX--;
			for (int i = 0; i < action.range; i++){
				board.setCanTarget(shadowX-i, shadowY+i);
				board.setCanTarget(shadowX-i, shadowY-i);
			}
			if (choosingTarget){
				for (int i = 0; i < action.range; i++){
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
		// for even total target is dependent on up or down.
		if (action.range % 2 == 0){
			for (int i = 0; i< action.range;i++){
				board.setCanTarget(shadowX, shadowY-i);
				board.setCanTarget(shadowX, shadowY+i);
			}
		}
		else{
			for (int i =0; i<= action.range/2;i++){
				board.setCanTarget(shadowX, shadowY+i);
				board.setCanTarget(shadowX, shadowY-i);
			}
		}
		if (choosingTarget){
			// for even you can choose where to position the shield
			if (action.range % 2 == 0){
				if (direction == Direction.UP){
					board.setHighlighted(shadowX, shadowY+1);
					board.setHighlighted(shadowX, shadowY);
				} else {
					board.setHighlighted(shadowX, shadowY);
					board.setHighlighted(shadowX, shadowY-1);
				}
			}
			else{
				for (int i =0; i<= action.range/2;i++){
					board.setHighlighted(shadowX, shadowY+i);
					board.setHighlighted(shadowX, shadowY-i);
				}				
			}
		}
	}
	
	public boolean isDone(){
		return isDone;
	}

	public SelectionMenu getMenu() {
		return menu;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	
}
