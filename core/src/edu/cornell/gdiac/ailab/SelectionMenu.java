package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

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

	private static final float RELATIVE_TEXT_X_POS = 0.0125f;

	private static final float RELATIVE_TEXT_Y_POS = 0.45f;

	private static final float RELATIVE_TEXT_SPACING = 0.0625f;

	private static final float ACTION_POINTER_OFFSET_X = 15;

	private static final float ACTION_POINTER_OFFSET_Y = 15;

	private static final float TEXT_ACTION_OFFSET = 30f;

	private static final float RELATIVE_DESCRIPTION_Y_POS = ActionBar.getRelativeY() - 0.1f;
	
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
	
	public SelectionMenu(Action[] actions){
		this.actions = actions;
//		System.out.println("Selection action set to 0");
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
		if (takenSlots > ActionBar.getTotalSlots()){
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
			an.free();
		}
		return an;
	}
	
	public List<ActionNode> getQueuedActions(){
		return selectedActions;
	}
	
	public Action getSelectedAction(){
		return actions[selectedAction];
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
		return takenSlots+a.cost <= ActionBar.getTotalSlots() && (canMove() || a.pattern != Pattern.MOVE);
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
		return takenSlots < ActionBar.getTotalSlots();
	}
	
	/**
	 * Change selected action to the next available either up or down
	 */
	public boolean changeSelected(boolean up){
		if (up){
			for (int i = 0; i < actions.length; i++){
//				System.out.println("Selection action set to " + selectedAction);
				selectedAction += 1;
				selectedAction %= actions.length;
//				System.out.println("Selection action set to " + selectedAction);
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
//				System.out.println("Selection action set to " + selectedAction);
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
		if (actions[selectedAction].cost > ActionBar.getTotalSlots() - takenSlots && takenSlots < ActionBar.getTotalSlots()){
			for (int i = 0; i < actions.length; i++){
				selectedAction = i;
//				System.out.println("Selection action set to " + selectedAction);
				if (canDoAction(actions[selectedAction])){
					return true;
				}
			}
		}
		return false;
	}
	
	public void reset(){
		selectedAction = 0;
//		System.out.println("Selection action set to " + selectedAction);
		takenSlots = 0;
		choosingTarget = false;
		while(selectedActions.peek() != null){
			ActionNode freeAction = selectedActions.poll();
			freeAction.free();
		}
	}
	
	public void draw(GameCanvas canvas){
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
		
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			float offset_y = spacing_h * i;
//			System.out.println(action.name + " is at " + i);
			if (action.cost > ActionBar.getTotalSlots() - takenSlots || (!canMove() && action.pattern == Pattern.MOVE)){
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
//			System.out.println("Pointer is at " + selectedAction);
			canvas.drawPointer(pointer_x,pointer_y, Color.CORAL);
		}
		
		//Draw action bar with 3 black boxes to show 4 slots
		float actionSlot_x = ActionBar.getBarCastPoint(canvas);
		float actionSlot_y = ActionBar.getBarY(canvas);
		
		float slot_width = ActionBar.getSlotWidth(canvas);
		float slot_height = ActionBar.getBarHeight(canvas);
		
		int offset = 0;
		for (int i = 0; i < ActionBar.getTotalSlots(); i++){
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
			float y_pos = actionSlot_y;
			canvas.drawCenteredText(an.action.name, x_pos, y_pos, Color.BLACK);
			offset+=slot_width*an.action.cost;
		}
		
		//Write description
		float descript_x = RELATIVE_DESCRIPTION_X_POS *w;
		float descript_y = RELATIVE_DESCRIPTION_Y_POS * h;
		canvas.drawCenteredText(actions[selectedAction].description, descript_x,descript_y, Color.BLACK);
	}
	
	public void setSelectedAction(int num){
//		System.out.println("Selected action set to " + num);
		selectedAction = num;
	}

	public void updateActionInfo(GameCanvas canvas) {
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		float text_x = RELATIVE_TEXT_X_POS * w;
		float text_y = RELATIVE_TEXT_Y_POS * h;
		float spacing_h = RELATIVE_TEXT_SPACING * h;
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			float offset_y = spacing_h * i;
			action.setHeight(15);//TODO change
			action.setPosition(i);
			action.setWidth(80);//TODO change
			action.setX(text_x);
			action.setY(text_y-offset_y);
		}
	}
	
}
