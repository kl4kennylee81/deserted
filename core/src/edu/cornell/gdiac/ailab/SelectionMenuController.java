package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Effect.Type;

public class SelectionMenuController {

	public static enum MenuState {
		//choosing an action on the selection menu
		SELECTING,
//		//choosing an action's target path
//		TARGETING,
		//waiting for a character to need selecting
		WAITING,
		//looking at an enemy's selection menu
		PEEKING
	}

	protected MenuState menuState;
	/** Models */
	GridBoard board;
	List<Character> characters;

	//TODO: Change how I handle this
	/** NOP action that is available for every character */
	Action nop;

	/** Controller variables */
	Character clickedChar;
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

	/** Attack direction values */
	Direction direction;




	public SelectionMenuController(GridBoard board, List<Character> chars) {
		clickedChar = null;
		this.board = board;
		this.characters = chars;
		this.menuState = MenuState.WAITING;
		isDone = false;
		selected = null;
		menu = null;
		action = null;
		choosingTarget = false;

		boardWidth = board.width;
		boardHeight = board.height;
		nop = new Action("NOP", 1, 0, 0, 1, Pattern.NOP, false, false,false, new Effect(0, Type.REGULAR, 0, "Nope"), "no action",null);
	}

	private void setChoosingTarget(boolean isChoosingTarget){
		this.choosingTarget = isChoosingTarget;
		this.menu.setChoosingTarget(isChoosingTarget);
	}

	public void update(){
		switch (menuState) {
			case SELECTING:
				checkForClicked();
				// FIXUP will fix this conditions
				if (clickedChar != null && !this.choosingTarget &&
						this.menu != null && !this.menu.getChoosingTarget()){
							System.out.println("this is never true");

					// if the clicked character is the selected don't switch
					if (clickedChar == selected){
						clickedChar.isClicked = false;
						clickedChar = null;
					}
					else{
						System.out.println("entering peeking now 2");
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
					drawHighlights();
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
					drawHighlights();
				}
				updatePeeking();

				// when you click on your original character it goes back to his selection menu
				if (InputController.pressedBack()||InputController.pressedRightMouse()||clickedChar == selected){
					clickedChar.isClicked = false;
					clickedChar = null;
					menuState = MenuState.SELECTING;
				}
				break;

		}
	}

	protected void checkForClicked(){
		for (Character c : characters){
			if (c.isClicked){
				if (!this.choosingTarget){
					clickedChar = c;
				}
				break;
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

		// reset/updates the highlighted flashing square on the board to the proper action
		// only do this when not choosing a target so we don't keep resetting the selected squares back to default
		if (!this.choosingTarget){
			this.setTargetedAction();
		}
	}

	protected void updatePeekingVariables(){
		menu = clickedChar.getSelectionMenu();
		action = menu.getSelectedAction();
		choosingTarget =  menu.getChoosingTarget();

		//use current positions for shadow x and y so the player doesn't get info
		//on planned enemy moves
		shadowX = clickedChar.xPosition;
		shadowY = clickedChar.yPosition;
		leftside = clickedChar.leftside;
		board.reset();

		// reset the highlighted tile
		this.setTargetedAction();
	}

	protected void updatePeeking() {
		int numSlots = clickedChar.getActionBar().getUsableNumSlots();
		if (InputController.pressedUp() && !InputController.pressedDown()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false,numSlots);
			this.setTargetedAction();
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			menu.changeSelected(true,numSlots);
			this.setTargetedAction();
		}
	}

	/** returns if the action can toggle between meaningful options
	 *  checking if on the edges thus cannot effectively toggle */
	private boolean isActionToggleable(){
		if (action == null){
			return false;
		}
		switch(action.pattern){
		case MOVE:
			if (board.canMove(selected.leftside,shadowX, shadowY+1)){
				return true;
			} else if (board.canMove(selected.leftside,shadowX+1, shadowY)){
				return true;
			} else if (board.canMove(selected.leftside,shadowX-1, shadowY)){
				return true;
			} else if (board.canMove(selected.leftside,shadowX, shadowY-1)){
				return true;
			} else {
				return false;
			}
		case PROJECTILE:
		case INSTANT:
		case DIAGONAL:
		case SHIELD:
			if (this.selected.getShadowY() == 0||this.selected.getShadowY() == this.board.getHeight()-1){
				return false;
			}
			else{
				return true;
			}
		case SINGLE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Update when an action is not targeting yet
	 */
	private void updateNotChoosingTarget(){
		// only allow mouse click to select if hovering over the action
		boolean mouseCondition = InputController.pressedLeftMouse() &&
				menu.contains(InputController.getMouseX(),InputController.getMouseY(), InputController.getCanvas(), board);
		int numSlots = selected.getActionBar().getUsableNumSlots();
		if ((InputController.pressedEnter() || mouseCondition)){
			if (this.menu.confirmContain(InputController.getMouseX(), InputController.getMouseY())){
				action = null;
			}
			if (action != null && menu.canAct(numSlots)){

				// allows for bypassing the targetting phase
				if (action.getNeedsToggle() && this.isActionToggleable()){
					updateTargetedAction();
					prompt = "Choose a Target";
				} else {
					float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
					menu.add(new ActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
					menu.resetPointer(numSlots);
				}
			} else {
				selected.setSelecting(false);
				selected.setQueuedActions(menu.getQueuedActions());
				selected = null;
				resetNeedsShadow();
			}
		} else if (InputController.pressedBack() || InputController.pressedRightMouse()){
			menu.removeLast();
			menu.resetPointer(this.selected.getActionBar().getUsableNumSlots());
			this.setTargetedAction();
		} else if (InputController.pressedUp() && !InputController.pressedDown()){
			//Actions go from up down, so we need to flip
			menu.changeSelected(false,numSlots);
			this.setTargetedAction();
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			menu.changeSelected(true,numSlots);
			this.setTargetedAction();
		}
	}

	/**
	 * set one of the targetable squares to targeting so you can
	 * differentiate between an AOE vs singles
	 */
	protected void setTargetedAction(){
		Action selectedAction = menu.getSelectedAction();
		if (selectedAction == null|| !selectedAction.needsToggle){
			return;
		}
		Character curChar = (menuState == MenuState.PEEKING) ? this.clickedChar : this.selected;
		switch (selectedAction.pattern){
		case STRAIGHT:
			break;
		case SINGLE:
			this.singleUpdateTargetedAction(curChar);
			break;
		case HORIZONTAL:
			break;
		case MOVE:
			if (board.canMove(curChar.leftside,curChar.getShadowX(), curChar.getShadowY()+1)){
				direction = Direction.UP;
			} else if (board.canMove(curChar.leftside,curChar.getShadowX()+1, curChar.getShadowY())){
				direction = Direction.RIGHT;
			} else if (board.canMove(curChar.leftside,curChar.getShadowX()-1, curChar.getShadowY())){
				direction = Direction.LEFT;
			} else if (board.canMove(curChar.leftside,curChar.getShadowX(),  curChar.getShadowY()-1)){
				direction = Direction.DOWN;
			} else {
				System.out.println("do something to tell them they cant move");
			}
			break;
		case DIAGONAL:
			if (curChar.getShadowY() < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			break;
		case SHIELD:
			if (curChar.getShadowY() < boardHeight/2){
				direction = Direction.UP;
			} else {
				direction = Direction.DOWN;
			}
			break;
		case INSTANT:
		case PROJECTILE:
			this.pathSetChoosingTarget(curChar);
			break;
		case NOP:
			break;
		default:
			break;
		}
	}

	/**
	 * Select an action to start targeting
	 */
	protected void updateTargetedAction(){
		this.setTargetedAction();
		this.setChoosingTarget(true);
	}

	private void pathSetChoosingTarget(Character curChar){
		if (curChar.getShadowY() >= board.getHeight()/2){
			this.direction = Direction.DOWN;
		}
		else{
			this.direction = Direction.UP;
		}
	}

	protected void singleUpdateTargetedAction(Character curChar){
		Action selectedAction = this.menu.getSelectedAction();
		if (curChar == null||selectedAction == null){
			return;
		}
		boolean hasFound = false;
		for (int i =0;i<board.getWidth();i++){
			for (int j=0;j<board.getHeight();j++){
				if ((curChar.leftside && i >= board.getWidth()/2)||(!curChar.leftside && i < board.getWidth()/2) || selectedAction.isBuff){
					boolean canHit = selectedAction.hitsTarget(curChar.getShadowX(),curChar.getShadowY(),i,j,curChar.leftside,board);
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

	protected void mouseHighlight(){
		// mouse controls for single
		float mouseX = InputController.getMouseX();
		float mouseY = InputController.getMouseY();
		Coordinate chosenTile = null;
		if (InputController.mouseJustMoved()||InputController.pressedLeftMouse()){
			chosenTile = this.board.contains(mouseX, mouseY, InputController.getCanvas());
		}
		if (chosenTile!= null){
			int chosenX = chosenTile.x;
			int chosenY = chosenTile.y;
			chosenTile.free();
			//System.out.println(chosenX+" "+chosenY);
			boolean canHit = this.board.getcanTarget(chosenX,chosenY);
			//System.out.println(canHit);
			if (canHit){
				//System.out.println("haha");
				this.selectedX = chosenX;
				this.selectedY = chosenY;
				if (InputController.pressedLeftMouse()){
					//System.out.println("PRESSED");
					confirmedAction();
				}
				return;
			}
		}
	}

	protected void updateChoosingTarget(){
		this.mouseHighlight();
		// null check
		if (this.action == null){
			return;
		}
		switch (action.pattern){
		case SINGLE:
			updateChoosingSingle();
			break;
		case MOVE:
			updateChoosingMove();
			break;
		case DIAGONAL:
			if (this.selectedY > this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.UP;
				this.selectedY = -1;
			}
			else if (this.selectedY <= this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.DOWN;
				this.selectedY = -1;
			}
			else if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			}
			break;
		case SHIELD:
			if (this.selectedY > this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.UP;
				this.selectedY = -1;
			}
			else if (this.selectedY <= this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.DOWN;
				this.selectedY = -1;
			}

			else if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			}
			break;
		case INSTANT:
		case PROJECTILE:
			if (this.selectedY > this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.UP;
				this.selectedY = -1;
			}
			else if (this.selectedY <= this.selected.getShadowY() && this.selectedY> 0){
				this.direction = Direction.DOWN;
				this.selectedY = -1;
			}
			else if (InputController.pressedUp() && !InputController.pressedDown()){
				direction = Direction.UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				direction = Direction.DOWN;
			}
			break;
		case NOP:
			break;
		default:
			break;
		}
		if (InputController.pressedEnter()){
			confirmedAction();
		} else if (InputController.pressedBack() || InputController.pressedRightMouse()){
			this.setChoosingTarget(false);
		}
	}

	private void confirmedAction(){
		float actionExecute = selected.actionBar.actionExecutionTime(menu.takenSlots,action.cost);
		int numSlots = selected.getActionBar().getUsableNumSlots();
		menu.add(new ActionNode(action,actionExecute,selectedX,selectedY,direction),numSlots);
		this.setChoosingTarget(false);
		menu.resetPointer(numSlots);

		// reset the highlighted flashing tile on the board
		this.setTargetedAction();
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
			if (updateX > boardWidth-1){
				updateX -= boardWidth/2;
			}
			else if (updateX < 0){
				updateX+=boardWidth/2;
			}
			if (((!this.action.isBuff && leftside)||(this.action.isBuff && !leftside)) && updateX<boardWidth/2){
				updateX+=boardWidth/2;
			} else if (((!this.action.isBuff && !leftside)||(this.action.isBuff && leftside)) && selectedX<0){
				updateX+=boardWidth/2;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			updateX = selectedX + 1;
			updateY = selectedY;
			if (updateX > boardWidth-1){
				updateX -= boardWidth/2;
			}
			else if (updateX < 0){
				updateX+=boardWidth/2;
			}
			if (((!this.action.isBuff && leftside)||(this.action.isBuff && !leftside)) && updateX> boardWidth-1){
				updateX -= boardWidth/2;
			} else if (((!this.action.isBuff && !leftside)||(this.action.isBuff && leftside)) && updateX > boardWidth/2-1){
				updateX -= boardWidth/2;
			}
		}

		if (action.singleCanTarget(selected.getShadowX(), selected.getShadowY(), updateX,updateY, selected.leftside, board)){
			selectedX = updateX;
			selectedY = updateY;
		}
	}

	protected void updateChoosingMove(){
		// allow for mouse controls for movement
		// mouse controls for single
		float mouseX = InputController.getMouseX();
		float mouseY = InputController.getMouseY();
		Coordinate chosenTile = null;
		if (InputController.mouseJustMoved()){
			chosenTile = this.board.contains(mouseX, mouseY, InputController.getCanvas());
		}
		if (chosenTile!= null){
			int startX = this.selected.getShadowX();
			int startY = this.selected.getShadowY();
			int targetX = chosenTile.x;
			int targetY = chosenTile.y;
			chosenTile.free();
			boolean oneAway = (Math.abs(startX - targetX) + Math.abs(startY - targetY)) == 1;
			boolean canMove = this.board.canMove(this.leftside, targetX, targetY);
			if (oneAway && canMove){
				if (targetX - startX == 1){
					this.direction = Direction.RIGHT;
				}
				else if (targetX - startX == -1){
					this.direction = Direction.LEFT;
				}
				else if (targetY - startY == 1){
					this.direction = Direction.UP;
				}
				else if (targetY - startY == -1){
					this.direction = Direction.DOWN;
				}
				this.selectedX = targetX;
				this.selectedY = targetY;
				if (InputController.pressedLeftMouse()){
					this.confirmedAction();

					// this will allow movement after locking in to go back to movement
					if (this.menu.canAct(this.selected.getActionBar().getUsableNumSlots())){
						this.updateTargetedAction();
					}
				}
				return;
			}
		}

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
				else if ((!board.isInBounds(x,y)) && !isProjectile){
					continue;
				}
				else if (this.action.needsToggle && choosingTarget && this.direction == Direction.UP){
					board.setHighlighted(x,y);
				}
				else if (this.action.needsToggle && !choosingTarget && this.direction == Direction.UP){
					board.setHighlighted(x, y);
				}
				else{
					board.setCanTarget(x, y);
				}


			}

			for (int i = 0; i < path.length; i++){
				int x = shadowX + path[i].x;
				int y = shadowY - path[i].y;
				if (shadowX == x && shadowY == y){
					continue;
				}
				else if ((!board.isInBounds(x, y)) && isProjectile){
					break;
				}
				else if ((!board.isInBounds(x,y)) && !isProjectile){
					continue;
				}
				else if (this.action.needsToggle && choosingTarget && this.direction == Direction.DOWN){
					board.setHighlighted(x,y);
				}
				else if (this.action.needsToggle && !choosingTarget && this.direction == Direction.DOWN){
					board.setHighlighted(x, y);
				}
				else{
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
				else if ((!board.isInBounds(x,y)) && !isProjectile){
					continue;
				}

				else if (this.action.needsToggle && choosingTarget && this.direction == Direction.UP){
					board.setHighlighted(x,y);
				}
				else if (this.action.needsToggle && !choosingTarget && this.direction == Direction.UP){
					board.setHighlighted(x, y);
				}
				else{
					board.setCanTarget(x, y);
				}
			}
			for (int i = 0; i < action.path.length; i++){
				int x = shadowX - path[i].x;
				int y = shadowY - path[i].y;
				if (shadowX == x && shadowY == y){
					continue;
				}
				else if ((!board.isInBounds(x, y)) && isProjectile){
					break;
				}
				else if ((!board.isInBounds(x,y)) && !isProjectile){
					continue;
				}

				else if (this.action.needsToggle && choosingTarget && this.direction == Direction.DOWN){
					board.setHighlighted(x,y);
				}
				else if (this.action.needsToggle && !choosingTarget && this.direction == Direction.DOWN){
					board.setHighlighted(x, y);
				}
				else{
					board.setCanTarget(x, y);
				}
			}
		}
	}
	public void drawSingle(){
		if (this.menuState != MenuState.PEEKING){
			board.setHighlighted(selectedX, selectedY);
			for (int i=0;i<board.getWidth();i++){
				for (int j = 0;j<board.getHeight();j++){
					if (this.action.singleCanTarget(selected.getShadowX(),selected.getShadowY(),i,j, selected.leftside, board)){
						if (selected.leftside && i >= (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
						else if (!selected.leftside && i < (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
						else if(this.action.isBuff){
							board.setCanTarget(i, j);
						}
					}
				}
			}
		}
		// when peeking at the enemy character movesets there moves are drawn from their current location
		// not the shadows location because the player does not have the information of where the shadow is
		else{
			board.setHighlighted(selectedX, selectedY);
			for (int i=0;i<board.getWidth();i++){
				for (int j = 0;j<board.getHeight();j++){
					if (this.action.singleCanTarget((int)clickedChar.getX(),(int)clickedChar.getY(),i,j, clickedChar.leftside, board)){
						if (clickedChar.leftside && i >= (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
						else if (!clickedChar.leftside && i < (int)board.getWidth()/2){
							board.setCanTarget(i,j);
						}
						else if(this.action.isBuff){
							board.setCanTarget(i, j);
						}
					}
				}
			}
		}

	}

	public void drawMove(){
		Character character = null;
		if (menuState == MenuState.PEEKING){
			character = clickedChar;
		}else{
			character = selected;
		}

		//if not leftside and at x=2 then draw
		if (!(leftside && shadowX == boardWidth/2-1)){
			board.setCanMove(character.leftside,shadowX+1, shadowY);
		}
		if (!(!leftside && shadowX == boardWidth/2)){
			board.setCanMove(character.leftside,shadowX-1, shadowY);
		}
		board.setCanMove(character.leftside,shadowX, shadowY-1);
		board.setCanMove(character.leftside,shadowX, shadowY+1);
		if (direction == null){
			return;
		}
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

	public void drawDiagonal(){
		if (leftside){
			shadowX++;
			for (int i = 0; i < action.range; i++){
				board.setCanTarget(shadowX+i, shadowY+i);
				board.setCanTarget(shadowX+i, shadowY-i);
			}
			if (direction == null){
				return;
			}
			for (int i = 0; i < action.range; i++){
				if (direction == Direction.UP){
					board.setHighlighted(shadowX+i, shadowY+i);
				} else {
					board.setHighlighted(shadowX+i, shadowY-i);
				}
			}

		} else {
			shadowX--;
			for (int i = 0; i < action.range; i++){
				board.setCanTarget(shadowX-i, shadowY+i);
				board.setCanTarget(shadowX-i, shadowY-i);
			}
			if (direction == null){
				return;
			}
			for (int i = 0; i < action.range; i++){
				if (direction == Direction.UP){
					board.setHighlighted(shadowX-i, shadowY+i);
				} else {
					board.setHighlighted(shadowX-i, shadowY-i);
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
			if (direction == null){
				return;
			}
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
