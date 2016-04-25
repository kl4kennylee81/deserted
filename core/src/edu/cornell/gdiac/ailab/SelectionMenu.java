package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class SelectionMenu {
	/** Available actions to use */
	Action[] actions;
	/** Index of current action */
	int selectedAction;
	/** List of already selected actions */
	LinkedList<ActionNode> selectedActions;
	
	/** Total number of available slots */

	private static final float RELATIVE_TEXT_X_POS = 0.02f;

	private static final float RELATIVE_TEXT_Y_POS = 0.5f;

	public static final float RELATIVE_TEXT_SPACING = 0.0625f;

	private static final float ACTION_POINTER_OFFSET_X = 15;

	private static final float ACTION_POINTER_OFFSET_Y = 15;

	public static final float TEXT_ACTION_OFFSET = 30f;

	private static final float RELATIVE_DESCRIPTION_Y_POS = 0.65f;
	
	private static final float RELATIVE_DESCRIPTION_X_POS = 0.5f;

	/** Currently used up slots */
	int takenSlots;
	
	/** If player is currently choosing target for a selected action */
	private boolean choosingTarget;
	/** X position of target */
	private int selectedX;
	/** Y position of target */
	private int selectedY;
	
	/** Lerp value for highlighting */
	private float lerpVal;
	/** Lerp value increasing or decreasing */
	private boolean increasing;
	
	TexturedMesh menuMesh;
	TexturedMesh menuBar;

	private float confirmX;

	private float confirmY;
	
	private float confirmHeight;
	
	private float confirmWidth;
	
	public SelectionMenu(Action[] actions){
		this.actions = actions;
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		selectedActions = new LinkedList<ActionNode>();
		lerpVal = 0;
		increasing = true;
	}
	
	/**
	 * Adds an action node to current queue
	 */
	public void add(ActionNode actionNode,int numSlots){
		int slots_taken = this.takenSlots + actionNode.action.cost;
		if (slots_taken <= numSlots){
			selectedActions.addLast(actionNode);
			this.takenSlots+=actionNode.action.cost;
		}
	}
	
	/**
	 * Checks if character has used shield, and thus cannot move
	 */
	public boolean canMove(){
		for (ActionNode an : selectedActions){
			if (an.action.pattern == Pattern.SHIELD){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Removes last queued action;
	 */
	public ActionNode removeLast(){
		ActionNode an = selectedActions.pollLast();
		if (an != null) {
			takenSlots -= an.action.cost;
			an.free();
		}
		return an;
	}
	
	public List<ActionNode> getQueuedActions(){
		return selectedActions;
	}
	
	public Action getSelectedAction(){
		if (selectedAction < actions.length){
			return actions[selectedAction];
		}
		return null;
	}
	
	public Action[] getActions(){
		return actions;
	}
	
	public boolean getChoosingTarget(){
		return choosingTarget;
	}
	
	public int getSelectedX(){
		return selectedX;
	}
	
	public int getSelectedY(){
		return selectedY;
	}
	
	public boolean setChoosingTarget(boolean ct){
		boolean prev = choosingTarget;
		choosingTarget = ct;
		return prev;
	}
	
	public void setSelectedX(int x){
		selectedX = x;
	}
	
	public void setSelectedY(int y){
		selectedY = y;
	}
	
	/**
	 * Checks if character can do the given action
	 */
	public boolean canDoAction(Action a,int numSlots){
		return takenSlots+a.cost <= numSlots && (canMove() || a.pattern != Pattern.MOVE);
	}
	
	public boolean canDoAction(int i,int numSlots){
		if (i < actions.length) {
			return canDoAction(actions[i],numSlots);
		}
		if (i == actions.length) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if character has any available actions
	 */
	public boolean canAct(int numSlots){
		for (Action a : actions){
			if (canDoAction(a,numSlots)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if character can at least NOP
	 */
	public boolean canNop(int numSlots){
		return takenSlots < numSlots;
	}
	
	/**
	 * Change selected action to the next available either up or down
	 */
	public boolean changeSelected(boolean up,int numSlots){
		if (up){
			for (int i = 0; i <= actions.length; i++){
				selectedAction += 1;
				selectedAction %= actions.length+1;
				if (canDoAction(selectedAction,numSlots)){
					return true;
				}
			}
		} else {
			for (int i = 0; i <= actions.length; i++){
				selectedAction -= 1;
				if (selectedAction < 0){
					selectedAction += actions.length+1;
				}
				if (canDoAction(selectedAction,numSlots)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Resets selected action index and returns true if a possible action is found
	 * @return
	 */
	public boolean resetPointer(int numSlots){
		if (selectedAction < actions.length && actions[selectedAction].cost > numSlots - takenSlots){
			for (int i = 0; i <= actions.length; i++){
				selectedAction = i;
				if (canDoAction(selectedAction,numSlots)){
					return true;
				}
			}
		}
		return false;
	}
	
	public void reset(){
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		while(selectedActions.peek() != null){
			ActionNode freeAction = selectedActions.poll();
			freeAction.free();
		}
	}

	public boolean isActionInvalid(int usableNumSlots,Action action){
		return (action.cost > usableNumSlots - takenSlots || (!canMove() && action.pattern == Pattern.MOVE));
	}
	
	/** determines the color for the action names in the selection menu while selecting **/
	public Color getActionColor(int usableNumSlots,Action action){
		if (this.isActionInvalid(usableNumSlots,action)){
			Color dimColor = Color.WHITE.cpy().mul(1f,1f,1f,0.2f);
			 return dimColor;
		} 
		else if (selectedAction < actions.length && actions[selectedAction].name == action.name){
			 if (this.choosingTarget){
				return Color.CORAL.cpy();
			 }
			 else{
				 return Color.WHITE.cpy().lerp(Color.CORAL,lerpVal);
			 }
		}
		else {
			return Color.WHITE;
		}
	}
	
	//TODO: update for dazed
	public void draw(GameCanvas canvas,CharActionBar actionBar,int count, boolean charIsClicked){
		int totalNumSlots = actionBar.getTotalNumSlots();
		int usableNumSlots = actionBar.getUsableNumSlots();
		
		if (increasing){
			lerpVal+=0.02;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= 0.02;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		//Draw action names
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		float text_x = RELATIVE_TEXT_X_POS * w;
		float text_y = RELATIVE_TEXT_Y_POS * h;
		float spacing_h = RELATIVE_TEXT_SPACING * h;
		GlyphLayout g = null;
		float offset_y = 0f;
		float selectedPointerOffset = 0f;
		
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			if (g != null) {
				offset_y += spacing_h + g.height/2;
			}
			
			if (i == selectedAction){
				selectedPointerOffset = offset_y;
			}
			Color actionColor = this.getActionColor(usableNumSlots, action);
			g = canvas.drawBoardWrapText(action.name, text_x, text_y - offset_y, actionColor);
		}
		
		if (!charIsClicked){
			//Draw confirm selection
			offset_y += spacing_h + g.height/2;
			if (selectedAction == actions.length){
				selectedPointerOffset = offset_y;
				g = canvas.drawBoardWrapText("Confirm", text_x, text_y - offset_y, Color.CORAL);
			} else {
				g = canvas.drawBoardWrapText("Confirm", text_x, text_y - offset_y, Color.GREEN);
			}
		}
		
		float pointer_x = text_x - ACTION_POINTER_OFFSET_X;
		float pointer_y = text_y - selectedPointerOffset -  ACTION_POINTER_OFFSET_Y;
		//draws action name pointers
		if (!this.choosingTarget){
			canvas.drawPointer(pointer_x,pointer_y, Color.CORAL);
		}
		
		//Draw action bar with 3 black boxes to show 4 slots
		float actionSlot_x = actionBar.getBarCastPoint(canvas);
		float actionSlot_y = actionBar.getY(canvas,count);
		
		float slot_width = actionBar.getSlotWidth(canvas);
		float slot_height = actionBar.getBarHeight(canvas);
		
		int offset = 0;
		for (int i = 0; i < totalNumSlots; i++){
			float curSlot_x = actionSlot_x + ((slot_width) * i) + CharActionBar.BAR_DIVIDER_WIDTH;
			float slot_w_space = slot_width-CharActionBar.BAR_DIVIDER_WIDTH;
			if (i < takenSlots) {
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.valueOf("990033"));
			} else if (selectedAction < actions.length && i < takenSlots+actions[selectedAction].cost){
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE.cpy().lerp(Color.valueOf("990033"),lerpVal));
			} else if (i >= usableNumSlots){
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.GRAY);
			} else {
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE);
			}
		}
		
		//Write the names of selected action
		for (ActionNode an : selectedActions){
			float x_pos = actionSlot_x + offset + (slot_width*an.action.cost/2);
			float y_pos = actionSlot_y;
			canvas.drawCenteredText(an.action.name, x_pos, y_pos, Color.WHITE);
			offset+=slot_width*an.action.cost;
		}
		
		//Write description
		if (selectedAction < actions.length){
			float descript_x = RELATIVE_DESCRIPTION_X_POS *w;
			float descript_y = RELATIVE_DESCRIPTION_Y_POS * h;
			canvas.drawCenteredText(actions[selectedAction].description, descript_x,descript_y, Color.WHITE);
		}
	}
	
	public void setSelectedAction(int num){
		selectedAction = num;
	}

	public void updateActionInfo(GameCanvas canvas) {
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		float text_x = RELATIVE_TEXT_X_POS * w;
		float text_y = RELATIVE_TEXT_Y_POS * h;
		float spacing_h = RELATIVE_TEXT_SPACING * h;
		BitmapFont b = canvas.getFont();
		float width = (GridBoard.BOARD_OFFSET_X - GridBoard.EXTRA_OFFSET)*canvas.getWidth();
		GlyphLayout g = new GlyphLayout(b, actions[0].name, Color.WHITE, width, Align.left, true);
		float glyphWidth = g.width;
		float offset_y = actions[0].height;
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			g.setText(b, action.name, Color.WHITE, width, Align.left, true);
			if (i != 0){
				 offset_y += spacing_h + g.height/2;
			}
			action.setHeight(g.height);//TODO change
			action.setPosition(i);
			glyphWidth = g.width;
			action.setWidth(glyphWidth);//TODO change
			action.setX(text_x);
			action.setY(text_y-offset_y);
		}
		offset_y += spacing_h + g.height/2;
		this.confirmX = text_x;
		this.confirmY = text_y - offset_y;
		this.confirmHeight = g.height;
		this.confirmWidth = glyphWidth;
		
	}
	
	public boolean confirmContain(float x, float y){
		float xMin = this.confirmX;
		float xMax = xMin + this.confirmWidth;
		float yMin = this.confirmY;
		float yMax = yMin + this.confirmHeight;
		return (x>xMin && x<=xMax && y>yMin && y<=yMax);
	}
	
}
