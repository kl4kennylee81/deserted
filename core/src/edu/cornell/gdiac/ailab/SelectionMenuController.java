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
	protected String prompt;
	
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
		nop = new Action("NOP", 1, 0, 0, 1, Pattern.NOP, false, false,false, new Effect(0, Type.REGULAR, 0, "Nope"), "no action");
	}
	
	public void update(){
		if (selected != null){
			updateVariables();
			int numSlots = selected.getActionBar().getUsableNumSlots();
			if (menu.canAct(numSlots) && action != null){
				drawHighlights();
			}
			if (!choosingTarget){
				// prompt choose an action when not choosing target
	    		prompt = "";//prompt = "Choose an Action";
	    		this.setPrompt(prompt);
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
	
	protected void updateVariables(){
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
		int numSlots = selected.getActionBar().getUsableNumSlots();
		if ((InputController.pressedEnter() || mouseCondition)){
			if (action != null && menu.canAct(numSlots)){
				
				// allows for bypassing the targetting phase
				if (action.getNeedsToggle()){
					updateTargetedAction();
					prompt = "Choose a Target";
				} else {
					float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
					menu.add(anPool.newActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
					menu.resetPointer(numSlots);
				}
			} else {
				selected.setSelecting(false);
				selected.setQueuedActions(menu.getQueuedActions());
				selected = null;
				resetNeedsShadow();
			}
		} else if (InputController.pressedBack()){
			menu.removeLast();
		// this is the noping command
//		} else if (InputController.pressedRight() && menu.canNop(numSlots)){
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
	
	/** 
	 * Select an action to start targeting
	 */
	protected void updateTargetedAction(){
		switch (action.pattern){
		case STRAIGHT:
			menu.setChoosingTarget(true);
			break;
		case SINGLE:
			this.singleUpdateTargetedAction();
			menu.setChoosingTarget(true);
			break;
		case HORIZONTAL:
			selectedX = board.width - 1 - shadowX;
			selectedY = 0;
			menu.setChoosingTarget(true);
		case MOVE:
			if (board.canMove(selected.leftside,shadowX, shadowY+1)){
				direction = Direction.UP;
			} else if (board.canMove(selected.leftside,shadowX+1, shadowY)){
				direction = Direction.RIGHT;
			} else if (board.canMove(selected.leftside,shadowX-1, shadowY)){
				direction = Direction.LEFT;
			} else if (board.canMove(selected.leftside,shadowX, shadowY-1)){
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
	
	protected void singleUpdateTargetedAction(){
		boolean hasFound = false;
		for (int i =0;i<board.getWidth();i++){
			for (int j=0;j<board.getHeight();j++){
				if ((this.selected.leftside && i >= board.getWidth()/2)||(!this.selected.leftside && i < board.getWidth()/2)){
					boolean canHit = this.action.hitsTarget(this.selected.getShadowX(),this.selected.getShadowY(),i,j,this.selected.leftside,board);
					if (canHit){
						this.selectedX = i;
						this.selectedY = j;
						hasFound = true;
						break;
					}
				}
			}
			if (hasFound){
				break;
			}
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
			int numSlots = selected.getActionBar().getUsableNumSlots();
			menu.add(anPool.newActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
			menu.setChoosingTarget(false);
			menu.resetPointer(numSlots);
		} else if (InputController.pressedBack()){
			menu.setChoosingTarget(false);
		}
	}
	
	protected void updateChoosingSingle(){
		direction = Direction.NONE;
		int updateX = selectedX;
		int updateY = selectedY;
		if (InputController.pressedUp() && !InputController.pressedDown()){
			updateX = selectedX;
			updateY = selectedY + 1;
			updateY %= boardHeight;
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			updateX = selectedX;
			updateY = selectedY - 1;
			if (updateY < 0){
				updateY += boardHeight;
			}
		} else if (InputController.pressedLeft() && !InputController.pressedRight()){
			updateX = selectedX - 1;
			updateY = selectedY;
			if (leftside && updateX<boardWidth/2){
				updateX+=boardWidth/2;
			} else if (!leftside && selectedX<0){
				updateX+=boardWidth/2;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			updateX = selectedX + 1;
			updateY = selectedY;
			if (leftside && updateX> boardWidth-1){
				updateX -= boardWidth/2;
			} else if (!leftside && updateX > boardWidth/2-1){
				updateX -= boardWidth/2;
			}
		}
		
		if (action.singleCanTarget(selected.getShadowX(), selected.getShadowY(), updateX,updateY)){
			selectedX = updateX;
			selectedY = updateY;
		}
	}
	
	protected void updateChoosingMove(){
		
		//Find out why moving is weird on right side
		//System.out.println("choosing");
		
		//Need to check in all of these if its a valid move;
		if (InputController.pressedUp() && !InputController.pressedDown()){
			if (board.canMove(selected.leftside,shadowX, shadowY+1)){
				direction = Direction.UP;
			}
			
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			if (board.canMove(selected.leftside,shadowX, shadowY-1)){
				direction = Direction.DOWN;
			}
		} else if (InputController.pressedLeft() && !InputController.pressedRight()){
			//if (not occupied) and (not rightside at x=3)
			if (board.canMove(selected.leftside,shadowX-1, shadowY)){
				direction = Direction.LEFT;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			if (board.canMove(selected.leftside,shadowX+1, shadowY)){
				direction = Direction.RIGHT;
			}
		}
	}
	
	protected void setNeedsShadow(){
		for (Character c : characters){
			if (c.leftside == selected.leftside){
				c.needsShadow = true;
			}
		}
	}
	
	protected void resetNeedsShadow(){
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
		case HORIZONTAL:
			drawHorizontal();
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
	
	public void drawHorizontal(){
		if (choosingTarget){
			for (int y = 0; y < board.height; y++){
				board.setHighlighted(selectedX, y);
			}
		} else {
			for (int y = 0; y < board.height; y++){
				board.setCanTarget(board.width - 1 - shadowX, y);
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
				for (int i=0;i<board.getWidth();i++){
					for (int j = 0;j<board.getHeight();j++){
						if (this.action.singleCanTarget(selected.getShadowX(),selected.getShadowY(),i,j)){
							if (selected.leftside && i >= (int)board.getWidth()/2){
								board.setCanTarget(i,j);
							}
							else if (!selected.leftside && i < (int)board.getWidth()/2){
								board.setCanTarget(i,j);
							}
						}
					}
				}
			board.setHighlighted(selectedX, selectedY);
		} else {
			
			// we have to limit it by the range so for example he can only single target with
			// range 3 around a radius.
			for (int i = 0;i<board.getWidth();i++){
				for (int j = 0;j<board.getHeight();j++){
					if (this.action.singleCanTarget(selected.getShadowX(),selected.getShadowY(),i,j)){
						if (selected.leftside && i >= (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
						else if (!selected.leftside && i < (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
					}
				}
			}
		}
	}
	
	public void drawMove(){
		//if not leftside and at x=2 then draw
		if (!(leftside && shadowX == boardWidth/2-1)){
			board.setCanMove(selected.leftside,shadowX+1, shadowY);
		}
		if (!(!leftside && shadowX == boardWidth/2)){
			board.setCanMove(selected.leftside,shadowX-1, shadowY);
		}
		board.setCanMove(selected.leftside,shadowX, shadowY-1);
		board.setCanMove(selected.leftside,shadowX, shadowY+1);
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
