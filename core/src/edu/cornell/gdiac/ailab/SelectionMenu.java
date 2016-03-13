package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.mesh.TexturedMesh;

public class SelectionMenu {
	/** Available actions to use */
	Action[] actions;
	/** Index of current action */
	int selectedAction;
	/** List of already selected actions */
	LinkedList<ActionNode> selectedActions;
	
	/** Total number of available slots */
	final int TOTAL_SLOTS = 4;

	private static float RELATIVE_TEXT_X_POS = 0.2f;

	private static float RELATIVE_TEXT_Y_POS = 0.80f;

	private static float RELATIVE_TEXT_SPACING = 0.0625f;

	private static float ACTION_POINTER_OFFSET_X = 20;

	private static float ACTION_POINTER_OFFSET_Y = 15;

	private static float TEXT_ACTION_OFFSET = 30f;

	private static float RELATIVE_DESCRIPTION_Y_POS = 0.80f;

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
	public void add(ActionNode actionNode){
		selectedActions.addLast(actionNode);
		takenSlots += actionNode.action.cost;
		if (takenSlots > TOTAL_SLOTS){
			System.out.println("Please check SelectionMenu");
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
		}
		return an;
	}
	
	public List<ActionNode> getQueuedActions(){
		return selectedActions;
	}
	
	public Action getSelectedAction(){
		return actions[selectedAction];
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
	
	public void setChoosingTarget(boolean ct){
		choosingTarget = ct;
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
	public boolean canDoAction(Action a){
		return takenSlots+a.cost <= TOTAL_SLOTS && (canMove() || a.pattern != Pattern.MOVE);
	}
	
	/**
	 * Checks if character has any available actions
	 */
	public boolean canAct(){
		for (Action a : actions){
			if (canDoAction(a)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if character can at least NOP
	 */
	public boolean canNop(){
		return takenSlots < TOTAL_SLOTS;
	}
	
	/**
	 * Change selected action to the next available either up or down
	 */
	public boolean changeSelected(boolean up){
		if (up){
			for (int i = 0; i < actions.length; i++){
				selectedAction += 1;
				selectedAction %= actions.length;
				if (canDoAction(actions[selectedAction])){
					return true;
				}
			}
		} else {
			for (int i = 0; i < actions.length; i++){
				selectedAction -= 1;
				if (selectedAction < 0){
					selectedAction += actions.length;
				}
				if (canDoAction(actions[selectedAction])){
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
	public boolean resetPointer(){
		if (actions[selectedAction].cost > TOTAL_SLOTS - takenSlots && takenSlots < TOTAL_SLOTS){
			for (int i = 0; i < actions.length; i++){
				selectedAction = i;
				if (canDoAction(actions[selectedAction])){
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
		selectedActions.clear();
	}
	
	public void draw(GameCanvas canvas){
		if (increasing){
			lerpVal+=0.03;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= 0.03;
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
		
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			float offset_y = spacing_h * i;
			if (action.cost > TOTAL_SLOTS - takenSlots || (!canMove() && action.pattern == Pattern.MOVE)){
				canvas.drawText(action.name, text_x, text_y - offset_y, new Color(1f, 1f, 1f, 0.5f));
			} else {
				canvas.drawText(action.name, text_x, text_y - offset_y, Color.BLACK);
			}
		}
		
		if (choosingTarget){
			//draws grid target
			//canvas.drawPointer(145+selectedX*100, 45+selectedY*100, Color.BLACK);
		} else if (canAct()){
			float pointer_x = text_x - ACTION_POINTER_OFFSET_X;
			float pointer_y = text_y - spacing_h*selectedAction - ACTION_POINTER_OFFSET_Y;
			//draws action name pointers
			canvas.drawPointer(pointer_x,pointer_y, Color.CORAL);
		}
		
		//Draw action bar with 3 black boxes to show 4 slots
		float actionSlot_x = ActionBar.getBarCastPoint(canvas);
		float actionSlot_y = ActionBar.getBarY(canvas);
		
		float slot_width = ActionBar.getSlotWidth(canvas);
		float slot_height = ActionBar.getBarHeight(canvas);
		
		int offset = 0;
		for (int i = 0; i < 4; i++){
			float curSlot_x = actionSlot_x + ((slot_width) * i) + ActionBar.getSpacing();
			float slot_w_space = slot_width-ActionBar.getSpacing();
			if (i < takenSlots) {
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.RED);
			} else if (i < takenSlots+actions[selectedAction].cost){
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE.cpy().lerp(Color.RED,lerpVal));
			} else {
				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE);
			}
		}
		
		//Write the names of selected action
		for (ActionNode an : selectedActions){
			float x_pos = actionSlot_x + offset + (slot_width*an.action.cost/2);
			float y_pos = actionSlot_y - TEXT_ACTION_OFFSET;
			canvas.drawCenteredText(an.action.name, x_pos, y_pos, Color.BLACK);
			offset+=slot_width*an.action.cost;
		}
		
		//Write description
		float descript_x = ActionBar.getBarCastPoint(canvas);
		float descript_y = RELATIVE_DESCRIPTION_Y_POS * h;
		canvas.drawText(actions[selectedAction].description, descript_x,descript_y, Color.BLACK);
	}
}
