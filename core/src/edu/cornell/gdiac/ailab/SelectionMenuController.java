package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Pattern;

public class SelectionMenuController {
	//Do a lot more
	//TEMP
	GridBoard board;
	List<Character> characters;
	Character selected;
	ActionBar bar;
	InputController controls;
	boolean isDone;
	
	int boardWidth;
	int boardHeight;
	
	private static final int SINGLE_X_LEFT = 3;
	private static final int SINGLE_X_RIGHT = 2;
	
	private static final int DIAGONAL_UP = 3;
	private static final int DIAGONAL_DOWN = 0;
	private static final int X_OFFSCREEN = -2;
	
	private static final int SINGLE_Y = 3;
	
	public SelectionMenuController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		controls = new PlayerController();
		this.isDone = false;
		this.selected = null;
		
		this.boardWidth = board.width;
		this.boardHeight = board.height;
	}
	
	
	//BIG TODO: after using move need to show some copy on board of where it would be
	
	public void update(){
		controls.getAction();
		if (selected != null){
			board.reset();
			board.occupy(characters);
			SelectionMenu menu = selected.getSelectionMenu();
			Action action = menu.getSelectedAction();
			boolean choosingTarget =  menu.getChoosingTarget();
			int curX = selected.xPosition;
			int curY = selected.yPosition;
			int selectedX = menu.getSelectedX();
			int selectedY = menu.getSelectedY();
			boolean leftside = selected.leftside;
			drawHighlights(selected, action, choosingTarget, selectedX, selectedY);
			if (!choosingTarget){
				if (controls.pressedEnter()){
					selected.setSelecting(false);
					selected = null;
				} else if (controls.pressedA()){
					if (action.pattern == Pattern.STRAIGHT){
						//Change casttime based on action.cost
						menu.add(new ActionNode(action,0.8f,0,0));
					} else if (action.pattern == Pattern.SINGLE){
						selectedX = leftside ? SINGLE_X_LEFT : SINGLE_X_RIGHT;
						selectedY = SINGLE_Y;
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.MOVE){
						if (!board.isOccupied(curX, curY+1)){
							selectedX = curX;
							selectedY = curY+1;
						} else if (!board.isOccupied(curX+1, curY)){
							selectedX = curX+1;
							selectedY = curY;
						} else if (!board.isOccupied(curX-1, curY)){
							selectedX = curX-1;
							selectedY = curY;
						} else if (!board.isOccupied(curX, curY-1)){
							selectedX = curX;
							selectedY = curY-1;
						} else {
							//do something to tell them they cant move
						}
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.DIAGONAL){
						selectedX = X_OFFSCREEN;
						if (curY < boardHeight/2){
							selectedY = DIAGONAL_UP;
						} else {
							selectedY = DIAGONAL_DOWN;
						}
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.SHIELD){
						
					}
				} else if (controls.pressedS()){
					menu.removeLast();
				} else if (controls.pressedUp() && !controls.pressedDown()){
					menu.selectedAction -= 1;
					if (menu.selectedAction < 0){
						menu.selectedAction += 4;
					}
				} else if (controls.pressedDown() && !controls.pressedUp()){
					menu.selectedAction += 1;
					menu.selectedAction %= 4;
				}
			} else {
				switch (action.pattern){
				case SINGLE:
					if (controls.pressedUp() && !controls.pressedDown()){
						selectedY+=1;
						selectedY %= boardHeight;
					} else if (controls.pressedDown() && !controls.pressedUp()){
						selectedY-=1;
						if (selectedY < 0){
							selectedY += boardHeight;
						}
					} else if (controls.pressedLeft() && !controls.pressedRight()){
						selectedX -= 1;
						if (leftside && selectedX<boardWidth/2){
							selectedX+=boardWidth/2;
						} else if (!leftside && selectedX<0){
							selectedX+=boardWidth/2;
						}
					} else if (controls.pressedRight() && !controls.pressedLeft()){
						selectedX += 1;
						if (leftside && selectedX > boardWidth-1){
							selectedX -= boardWidth/2;
						} else if (!leftside && selectedX > boardWidth/2-1){
							selectedX -= boardWidth/2;
						}
					}
					break;
				case MOVE:
					//Need to check in all of these if its a valid move;
					if (controls.pressedUp() && !controls.pressedDown()){
						if (!board.isOccupied(curX, curY+1)){
							selectedX = curX;
							selectedY = curY+1;
						}
					} else if (controls.pressedDown() && !controls.pressedUp()){
						if (!board.isOccupied(curX, curY-1)){
							selectedX = curX;
							selectedY = curY-1;
						}
					} else if (controls.pressedLeft() && !controls.pressedRight()){
						if (!board.isOccupied(curX-1, curY)){
							selectedX = curX-1;
							selectedY = curY;
						}
					} else if (controls.pressedRight() && !controls.pressedLeft()){
						if (!board.isOccupied(curX+1, curY)){
							selectedX = curX+1;
							selectedY = curY;
						}
					}
					break;
				case DIAGONAL:
					if (controls.pressedUp() && !controls.pressedDown()){
						selectedY = DIAGONAL_UP;
					} else if (controls.pressedDown() && !controls.pressedUp()){
						selectedY = DIAGONAL_DOWN;
					} 
					break;
				default:
					break;
				}
				if (controls.pressedA()){
					//Change casttime based on action.cost
					menu.add(new ActionNode(action,0.8f,selectedX,selectedY));
					menu.setChoosingTarget(false);
				} else if (controls.pressedS()){
					menu.setChoosingTarget(false);
				}
			}
			menu.setSelectedX(selectedX);
			menu.setSelectedY(selectedY);
		} else {
			isDone = true;
			for (Character c : characters){
				if (c.needsSelection){
					isDone = false;
					selected = c;
					c.needsSelection = false;
					c.isSelecting = true;
					break;
				}
			}
		}	
	}
	
	public void drawHighlights(Character selected, Action action, boolean choosingTarget, int xPos, int yPos){
		if (action.pattern == Pattern.STRAIGHT){
			drawStraight(selected.leftside, selected.xPosition, selected.yPosition);
		}
		if (action.pattern == Pattern.SINGLE){
			drawSingle(selected.leftside, choosingTarget, xPos, yPos);
		}
		if (action.pattern == Pattern.MOVE){
			drawMove(selected, selected.leftside, choosingTarget, xPos, yPos);
		}
		if (action.pattern == Pattern.DIAGONAL){
			drawDiagonal(selected,selected.leftside, choosingTarget, yPos);
		}
	}
	
	public void drawStraight(boolean leftside, int x, int y){
		if (leftside) {
			for (int i = x+1; i < boardWidth; i++){
				board.setHighlighted(i,y);
			}
		} else {
			for (int i = x-1; i >= 0; i--){
				board.setHighlighted(i,y);
			}
		}
	}
	public void drawSingle(boolean leftside, boolean choosingTarget, int x, int y){
		if (choosingTarget){
			board.setHighlighted(x, y);
		} else {
			if (leftside) {
				board.setHighlighted(SINGLE_X_LEFT,SINGLE_Y);
			} else {
				board.setHighlighted(SINGLE_X_RIGHT,SINGLE_Y);
			}
		}
	}
	
	public void drawMove(Character selected, boolean leftside, boolean choosingTarget, int x, int y){
		int curX = selected.xPosition;
		int curY = selected.yPosition;
		board.setCanTarget(curX-1, curY);
		board.setCanTarget(curX+1, curY);
		board.setCanTarget(curX, curY-1);
		board.setCanTarget(curX, curY+1);
		if (choosingTarget){
			board.setHighlighted(x, y);
		} else {
			if (!board.isOccupied(curX, curY+1)){
				board.setHighlighted(curX, curY+1);
			} else if (!board.isOccupied(curX+1, curY)){
				board.setHighlighted(curX+1, curY);
			} else if (!board.isOccupied(curX-1, curY)){
				board.setHighlighted(curX-1, curY);
			} else if (!board.isOccupied(curX, curY-1)){
				board.setHighlighted(curX, curY-1);
			} else {
				//should never get here
				//this means we let them move when every space was occupied
				System.out.println("UHOHHHH");
			}
		}
	}
	
	public void drawDiagonal(Character selected, boolean leftside, boolean choosingTarget, int yPos){
		int curY = selected.yPosition;
		if (leftside){
			int curX = selected.xPosition+1;
			if (!choosingTarget){
				if (curY < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX+i, curY+i);
						board.setCanTarget(curX+i, curY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX+i, curY-i);
						board.setCanTarget(curX+i, curY+i);
					}
				}
			} else {
				if (yPos == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX+i, curY+i);
						board.setCanTarget(curX+i, curY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX+i, curY-i);
						board.setCanTarget(curX+i, curY+i);
					}
				}
				
			}
		} else {
			int curX = selected.xPosition-1;
			if (!choosingTarget){
				if (curY < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX-i, curY+i);
						board.setCanTarget(curX-i, curY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX-i, curY-i);
						board.setCanTarget(curX-i, curY+i);
					}
				}
			} else {
				if (yPos == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX-i, curY+i);
						board.setCanTarget(curX-i, curY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(curX-i, curY-i);
						board.setCanTarget(curX-i, curY+i);
					}
				}
				
			}
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}
