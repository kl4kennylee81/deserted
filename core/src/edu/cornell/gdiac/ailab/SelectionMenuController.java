package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Effect;
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
	
	Action nop;
	
	int boardWidth;
	int boardHeight;
	
	private static final int SINGLE_X_LEFT = 3;
	private static final int SINGLE_X_RIGHT = 2;
	
	public static final int DIAGONAL_UP = 3;
	public static final int DIAGONAL_DOWN = 0;
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
		nop = new Action("NOP", 1, 0, 0, Pattern.NOP, Effect.REGULAR, "no action");
	}
	
	public void update(){
		controls.getAction();
		if (selected != null){
			SelectionMenu menu = selected.getSelectionMenu();
			Action action = menu.getSelectedAction();
			boolean choosingTarget =  menu.getChoosingTarget();
			int shadowX = selected.shadowX;
			int shadowY = selected.shadowY;
			int selectedX = menu.getSelectedX();
			int selectedY = menu.getSelectedY();
			boolean leftside = selected.leftside;
			board.reset();
			//TODO: change it to just send characters selected and then find shadows from selected
			board.occupy(characters, selected, selected.shadowX, selected.shadowY);
			if (menu.canDoAction()){
				drawHighlights(selected, action, choosingTarget, selectedX, selectedY, shadowX, shadowY);
			}
			if (!choosingTarget){
				if (controls.pressedEnter()){
					selected.setSelecting(false);
					selected.setQueuedActions(menu.getQueuedActions());
					selected = null;
					resetNeedsShadow();
				} else if (controls.pressedA() && menu.canDoAction()){
					if (action.pattern == Pattern.STRAIGHT){
						menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*0.075f,0,0));
						menu.resetPointer();
					} else if (action.pattern == Pattern.SINGLE){
						selectedX = leftside ? SINGLE_X_LEFT : SINGLE_X_RIGHT;
						selectedY = SINGLE_Y;
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.MOVE){
						if (!board.isOccupied(shadowX, shadowY+1)){
							selectedX = shadowX;
							selectedY = shadowY+1;
						} else if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
							selectedX = shadowX+1;
							selectedY = shadowY;
						} else if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowY == boardWidth/2)){
							selectedX = shadowX-1;
							selectedY = shadowY;
						} else if (!board.isOccupied(shadowX, shadowY-1)){
							selectedX = shadowX;
							selectedY = shadowY-1;
						} else {
							//do something to tell them they cant move
						}
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.DIAGONAL){
						selectedX = X_OFFSCREEN;
						if (shadowY < boardHeight/2){
							selectedY = DIAGONAL_UP;
						} else {
							selectedY = DIAGONAL_DOWN;
						}
						menu.setChoosingTarget(true);
					} else if (action.pattern == Pattern.SHIELD){
						//FILL IN SHIELD
					}
				} else if (controls.pressedS()){
					ActionNode old = menu.removeLast();
					if (old != null && old.action.pattern == Pattern.MOVE){
						selected.rewindShadow();
					}
				} else if (controls.pressedD() && menu.canDoAction()){
					menu.add(new ActionNode(nop,bar.castPoint+(action.cost+menu.takenSlots)*0.075f,0,0));
					menu.resetPointer();
				} else if (controls.pressedUp() && !controls.pressedDown()){
					//Actions go from up down, so we need to flip
					menu.changeSelected(false);
				} else if (controls.pressedDown() && !controls.pressedUp()){
					menu.changeSelected(true);
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
						if (!board.isOccupied(shadowX, shadowY+1)){
							selectedX = shadowX;
							selectedY = shadowY+1;
						}
						
					} else if (controls.pressedDown() && !controls.pressedUp()){
						if (!board.isOccupied(shadowX, shadowY-1)){
							selectedX = shadowX;
							selectedY = shadowY-1;
						}
					} else if (controls.pressedLeft() && !controls.pressedRight()){
						//if (not occupied) and (not rightside at x=3)
						if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowX == boardWidth/2)){
							selectedX = shadowX-1;
							selectedY = shadowY;
						}
					} else if (controls.pressedRight() && !controls.pressedLeft()){
						if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
							selectedX = shadowX+1;
							selectedY = shadowY;
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
				if (controls.pressedEnter()){
					menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*((1-bar.castPoint)/menu.TOTAL_SLOTS),selectedX,selectedY));
					menu.setChoosingTarget(false);
					if (action.pattern == Pattern.MOVE){
						selected.setShadow(selectedX,selectedY);
					}
					selected.setSelecting(false);
					selected.setQueuedActions(menu.getQueuedActions());
					selected = null;
					resetNeedsShadow();
				}
				if (controls.pressedA()){
					menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*((1-bar.castPoint)/menu.TOTAL_SLOTS),selectedX,selectedY));
					menu.setChoosingTarget(false);
					menu.resetPointer();
					if (action.pattern == Pattern.MOVE){
						selected.setShadow(selectedX,selectedY);
					}
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
					SelectionMenu menu = c.getSelectionMenu();
					menu.reset();
					c.needsSelection = false;
					c.isSelecting = true;
					setNeedsShadow();
					break;
				}
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
	
	public void drawHighlights(Character selected, Action action, boolean choosingTarget, int xPos, int yPos, int shadX, int shadY){
		if (action.pattern == Pattern.STRAIGHT){
			drawStraight(selected.leftside, shadX, shadY);
		}
		if (action.pattern == Pattern.SINGLE){
			drawSingle(selected.leftside, choosingTarget, xPos, yPos);
		}
		if (action.pattern == Pattern.MOVE){
			drawMove(shadX, shadY, selected.leftside, choosingTarget, xPos, yPos);
		}
		if (action.pattern == Pattern.DIAGONAL){
			drawDiagonal(shadX, shadY,selected.leftside, choosingTarget, yPos);
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
	
	public void drawMove(int xPos, int yPos, boolean leftside, boolean choosingTarget, int x, int y){
		//if not leftside and at x=2 then draw
		if (!(leftside && xPos == boardWidth/2-1)){
			board.setCanTarget(xPos+1, yPos);
		}
		if (!(!leftside && xPos == boardWidth/2)){
			board.setCanTarget(xPos-1, yPos);
		}
		board.setCanTarget(xPos, yPos-1);
		board.setCanTarget(xPos, yPos+1);
		if (choosingTarget){
			board.setHighlighted(x, y);
		} else {
			if (!board.isOccupied(xPos, yPos+1)){
				board.setHighlighted(xPos, yPos+1);
			} else if (!board.isOccupied(xPos+1, yPos) && !(leftside && xPos == boardWidth/2-1)){
				board.setHighlighted(xPos+1, yPos);
			} else if (!board.isOccupied(xPos-1, yPos) && !(!leftside && xPos == boardWidth/2)){
				board.setHighlighted(xPos-1, yPos);
			} else if (!board.isOccupied(xPos, yPos-1)){
				board.setHighlighted(xPos, yPos-1);
			} else {
				//should never get here
				//this means we let them move when every space was occupied
				System.out.println("UHOHHHH");
			}
		}
	}
	
	public void drawDiagonal(int xPos, int yPos, boolean leftside, boolean choosingTarget, int yTarget){
		if (leftside){
			xPos++;
			if (!choosingTarget){
				if (yPos < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos+i, yPos+i);
						board.setCanTarget(xPos+i, yPos-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos+i, yPos-i);
						board.setCanTarget(xPos+i, yPos+i);
					}
				}
			} else {
				if (yTarget == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos+i, yPos+i);
						board.setCanTarget(xPos+i, yPos-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos+i, yPos-i);
						board.setCanTarget(xPos+i, yPos+i);
					}
				}
			}
		} else {
			xPos--;
			if (!choosingTarget){
				if (yPos < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos-i, yPos+i);
						board.setCanTarget(xPos-i, yPos-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos-i, yPos-i);
						board.setCanTarget(xPos-i, yPos+i);
					}
				}
			} else {
				if (yTarget == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos-i, yPos+i);
						board.setCanTarget(xPos-i, yPos-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(xPos-i, yPos-i);
						board.setCanTarget(xPos-i, yPos+i);
					}
				}
				
			}
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}
