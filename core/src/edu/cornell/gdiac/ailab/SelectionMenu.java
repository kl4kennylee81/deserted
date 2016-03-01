package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class SelectionMenu {
	int height;
	int width;
	String font;
	float spacing;
	Action[] actions;
	int selectedAction;
	LinkedList<ActionNode> selectedActions;
	final int TOTAL_SLOTS = 4;
	int takenSlots;
	TexturedMesh menuMesh;
	TexturedMesh menuBar;
	private boolean choosingTarget;
	private int selectedX;
	private int selectedY;
	private float lerpVal;
	private boolean increasing;
	
	int shadowX;
	int shadowY;
	
	LinkedList<Integer> oldShadowX;
	LinkedList<Integer> oldShadowY;

	//Initialized with character
	//draw menu
	//draw action selection bar
	
	// in selection screen controller
	// interval = (ActionBar.length-ActionBar.castPoint)/SelectionMenu.totalSlots
	// actionNode.executePoint = ActionBar.castPoint + interval * (selectedAction.cost + takenSlots)
	
	//Currently under the impression that there will always be a one cost skill
	
	public SelectionMenu(Action[] actions){
		this.actions = actions;
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		selectedActions = new LinkedList<ActionNode>();
		oldShadowX = new LinkedList<Integer>();
		oldShadowY = new LinkedList<Integer>();
		lerpVal = 0;
		increasing = true;
	}
	
	public void add(ActionNode actionNode){
		selectedActions.addLast(actionNode);
		takenSlots += actionNode.action.cost;
	}
	
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
	
	public void setShadow(int shadX, int shadY){
		shadowX = shadX;
		shadowY = shadY;
		oldShadowX.push(shadX);
		oldShadowY.push(shadY);
	}
	
	public void rewindShadow(){
		oldShadowX.pop();
		oldShadowY.pop();
		shadowX = oldShadowX.peek();
		shadowY = oldShadowY.peek();
	}
	
	public boolean canDoAction(){
		return takenSlots < TOTAL_SLOTS;
	}
	
	public boolean changeSelected(boolean up){
		if (up){
			for (int i = 0; i < TOTAL_SLOTS; i++){
				selectedAction += 1;
				selectedAction %= TOTAL_SLOTS;
				if (actions[selectedAction].cost <= TOTAL_SLOTS - takenSlots){
					return true;
				}
			}
		} else {
			for (int i = 0; i < TOTAL_SLOTS; i++){
				selectedAction -= 1;
				if (selectedAction < 0){
					selectedAction += TOTAL_SLOTS;
				}
				if (actions[selectedAction].cost <= TOTAL_SLOTS - takenSlots){
					return true;
				}
			}
		}
		return false;
	}
	
	public void resetPointer(){
		if (actions[selectedAction].cost > TOTAL_SLOTS - takenSlots && takenSlots < TOTAL_SLOTS){
			for (int i = 0; i < TOTAL_SLOTS; i++){
				selectedAction = i;
				if (actions[selectedAction].cost <= TOTAL_SLOTS - takenSlots){
					return;
				}
			}
		}
	}
	
	public void reset(){
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		selectedActions.clear();
		oldShadowX.clear();
		oldShadowY.clear();
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
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			if (action.cost > TOTAL_SLOTS - takenSlots){
				canvas.drawText(action.name, 200, 630-50*i, new Color(1f, 1f, 1f, 0.5f));
			} else {
				canvas.drawText(action.name, 200, 630-50*i, Color.WHITE);
			}
		}
		
		if (choosingTarget){
			//draws grid target
			canvas.drawPointer(145+selectedX*100, 45+selectedY*100, Color.BLACK);
		} else if (canDoAction()){
			//draws action name pointers
			canvas.drawPointer(180,620-50*selectedAction, Color.CORAL);
		}
		
		//Draw action bar with 3 black boxes to show 4 slots
		int offset = 0;
		for (int i = 0; i < 4; i++){
			if (i < takenSlots) {
				canvas.drawBox(400+80*i,600,75,30,Color.CORAL);
			} else if (i < takenSlots+actions[selectedAction].cost){
				canvas.drawBox(400+80*i,600,75,30,Color.WHITE.cpy().lerp(Color.RED,lerpVal));
			} else {
				canvas.drawBox(400+80*i,600,75,30,Color.WHITE);
			}
		}
		
		//Write the names of selected action
		for (ActionNode an : selectedActions){
			canvas.drawCenteredText(an.action.name, 400+offset+75*an.action.cost/2, 580);
			offset+=75*an.action.cost;
		}
		
		//TODO: add color to used up boxes and a lighter color to highlight what the current selected action would add
		
	}
}
