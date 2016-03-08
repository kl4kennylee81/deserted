package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuController {
	
	public int gameNo;
	private boolean isDone;
	private GameCanvas canvas;
	/** Background image for the canvas */
	private static final String BCKGD_TEXTURE = "images/bg.png";
	/** File storing the texture for an option tile */
	private static final String OPTION_TEXTURE = "images/white.png";
	private static final String WHITE_BOX = "images/white.png";
	private AssetManager manager;
	
	public MainMenuController(GameCanvas canvas, AssetManager manager){
		this.canvas = canvas;
		this.manager = manager;
	}
	
	public void drawMenu() {
		initializeCanvas(BCKGD_TEXTURE);
		drawOption(canvas.getWidth()/4,canvas.getHeight()/4,100,"HARD MODE","(or press 'h')");
		drawOption(canvas.getWidth()/4,3*canvas.getHeight()/4,100,"EASY MODE","(or press 'e')");
		drawOption(3*canvas.getWidth()/4,canvas.getHeight()/4,100,"MEDIUM MODE","(or press 'm')");
		drawOption(3*canvas.getWidth()/4,3*canvas.getHeight()/4,100,"PvP MODE","(or press 'p')");
	}
	
	public void update(){
		drawMenu();
//		if (InputController.pressedE()){
//		startGame(0);
//	} else if (InputController.pressedM()){
//		startGame(1);
//	} else if (InputController.pressedH()){
//		startGame(2);
//	} else if (InputController.pressedP()){
//		startGame(3);
//	}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
    /**
	 * Draws an option for the start screen at position (x,y). 
	 *
	 *
	 * @param x The x index for the Option cell
	 * @param y The y index for the Option cell
	 */
	private void drawOption(float sx, float sy, int size, String msg1, String msg2) {
		// Compute drawing coordinates
		//Option option = new Option();
		//System.out.println("" + sx + " " + sy);

		// You can modify the following to change a tile's highlight color.
		// BASIC_COLOR corresponds to no highlight.
		///////////////////////////////////////////////////////
		Color color = Color.WHITE;
		//if (option.isHighlighted){
			//System.out.println("dude");
			//color.lerp(HIGHLIGHT_COLOR,lerpVal);
		//} 

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawOption(sx,sy,new Texture(OPTION_TEXTURE),size,color, msg1, msg2);
	}
	
	/**
	 * Loads the assets used by the game canvas.
	 *
	 * This method loads the background and font for the canvas.  As these are
	 * needed to draw anything at all, we block until the assets have finished
	 * loading.
	 */
    private void initializeCanvas(String texture_msg) { 
    	canvas.setFont(new BitmapFont());
		Texture texture = manager.get(texture_msg, Texture.class);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		canvas.setBackground(texture);
		canvas.setWhite(manager.get(WHITE_BOX, Texture.class));
    }

}

package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.ailab.Action.Effect;
import edu.cornell.gdiac.ailab.Action.Pattern;

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
	public static final int DIAGONAL_UP = 3;
	public static final int DIAGONAL_DOWN = 0;
	private static final int X_OFFSCREEN = -2;
	
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
		nop = new Action("NOP", 1, 0, 0, Pattern.NOP, Effect.REGULAR, "no action");
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
					c.isSelecting = true;
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
		shadowX = selected.shadowX;
		shadowY = selected.shadowY;
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
			ActionNode old = menu.removeLast();
			if (old != null && old.action.pattern == Pattern.MOVE){
				selected.rewindShadow();
			}
		} else if (InputController.pressedD() && menu.canNop()){
			menu.add(new ActionNode(nop,bar.castPoint+(action.cost+menu.takenSlots)*0.075f,0,0));
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
			selectedX = X_OFFSCREEN;
			if (shadowY < boardHeight/2){
				selectedY = DIAGONAL_UP;
			} else {
				selectedY = DIAGONAL_DOWN;
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
				selectedY = DIAGONAL_UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				selectedY = DIAGONAL_DOWN;
			} 
			break;
		case SHIELD:
			if (InputController.pressedUp() && !InputController.pressedDown()){
				selectedY = DIAGONAL_UP;
			} else if (InputController.pressedDown() && !InputController.pressedUp()){
				selectedY = DIAGONAL_DOWN;
			} 
			break;
		default:
			break;
		}
		if (InputController.pressedEnter()){
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
		if (InputController.pressedA()){
			menu.add(new ActionNode(action,bar.castPoint+(action.cost+menu.takenSlots)*((1-bar.castPoint)/menu.TOTAL_SLOTS),selectedX,selectedY));
			menu.setChoosingTarget(false);
			menu.resetPointer();
			if (action.pattern == Pattern.MOVE){
				selected.setShadow(selectedX,selectedY);
			}
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
				selectedX = shadowX;
				selectedY = shadowY+1;
			}
			
		} else if (InputController.pressedDown() && !InputController.pressedUp()){
			if (!board.isOccupied(shadowX, shadowY-1)){
				selectedX = shadowX;
				selectedY = shadowY-1;
			}
		} else if (InputController.pressedLeft() && !InputController.pressedRight()){
			//if (not occupied) and (not rightside at x=3)
			if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowX == boardWidth/2)){
				selectedX = shadowX-1;
				selectedY = shadowY;
			}
		} else if (InputController.pressedRight() && !InputController.pressedLeft()){
			if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
				selectedX = shadowX+1;
				selectedY = shadowY;
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
				board.setHighlighted(i,shadowY);
			}
		} else {
			for (int i = shadowX-1; i >= 0; i--){
				board.setHighlighted(i,shadowY);
			}
		}
	}
	public void drawSingle(){
		if (choosingTarget){
			board.setHighlighted(selectedX, selectedY);
		} else {
			if (leftside) {
				board.setHighlighted(SINGLE_X_LEFT,SINGLE_Y);
			} else {
				board.setHighlighted(SINGLE_X_RIGHT,SINGLE_Y);
			}
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
			board.setHighlighted(selectedX, selectedY);
		} else {
			if (!board.isOccupied(shadowX, shadowY+1)){
				board.setHighlighted(shadowX, shadowY+1);
			} else if (!board.isOccupied(shadowX+1, shadowY) && !(leftside && shadowX == boardWidth/2-1)){
				board.setHighlighted(shadowX+1, shadowY);
			} else if (!board.isOccupied(shadowX-1, shadowY) && !(!leftside && shadowX == boardWidth/2)){
				board.setHighlighted(shadowX-1, shadowY);
			} else if (!board.isOccupied(shadowX, shadowY-1)){
				board.setHighlighted(shadowX, shadowY-1);
			} else {
				//should never get here
				//this means we let them move when every space was occupied
				System.out.println("UHOHHHH");
			}
		}
	}
	
	public void drawDiagonal(){
		if (leftside){
			shadowX++;
			if (!choosingTarget){
				if (shadowY < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX+i, shadowY+i);
						board.setCanTarget(shadowX+i, shadowY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX+i, shadowY-i);
						board.setCanTarget(shadowX+i, shadowY+i);
					}
				}
			} else {
				if (selectedY == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX+i, shadowY+i);
						board.setCanTarget(shadowX+i, shadowY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX+i, shadowY-i);
						board.setCanTarget(shadowX+i, shadowY+i);
					}
				}
			}
		} else {
			shadowX--;
			if (!choosingTarget){
				if (shadowY < boardHeight/2){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX-i, shadowY+i);
						board.setCanTarget(shadowX-i, shadowY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX-i, shadowY-i);
						board.setCanTarget(shadowX-i, shadowY+i);
					}
				}
			} else {
				if (selectedY == DIAGONAL_UP){
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX-i, shadowY+i);
						board.setCanTarget(shadowX-i, shadowY-i);
					}
				} else {
					for (int i = 0; i < boardHeight; i++){
						board.setHighlighted(shadowX-i, shadowY-i);
						board.setCanTarget(shadowX-i, shadowY+i);
					}
				}
				
			}
		}
	}
	
	public void drawShield(){
		if (!choosingTarget){
			if (shadowY < boardHeight/2){
				selectedY = 3;
			} else {
				selectedY = 0;
			}
		}
		if (selectedY == 3){
			board.setHighlighted(shadowX, shadowY+1);
			board.setHighlighted(shadowX, shadowY);
			board.setCanTarget(shadowX, shadowY-1);
		} else {
			board.setCanTarget(shadowX, shadowY+1);
			board.setHighlighted(shadowX, shadowY);
			board.setHighlighted(shadowX, shadowY-1);
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
}

