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
	int totalSlots;
	int takenSlots;
	TexturedMesh menuMesh;
	TexturedMesh menuBar;
	private boolean choosingTarget;
	private int selectedX;
	private int selectedY;
	
	//Initialized with character
	//draw menu
	//draw action selection bar
	
	// in selection screen controller
	// interval = (ActionBar.length-ActionBar.castPoint)/SelectionMenu.totalSlots
	// actionNode.executePoint = ActionBar.castPoint + interval * (selectedAction.cost + takenSlots)
	
	public SelectionMenu(Action[] actions){
		this.actions = actions;
		selectedAction = 0;
		totalSlots = 4;
		takenSlots = 0;
		selectedActions = new LinkedList<ActionNode>();
		choosingTarget = false;
	}
	
	public void add(ActionNode actionNode){
		selectedActions.addLast(actionNode);
		takenSlots += actionNode.action.cost;
	}
	
	public void removeLast(){
		ActionNode an = selectedActions.pollLast();
		if (an != null) {
			takenSlots -= an.action.cost;
		}
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
	
	public void draw(GameCanvas canvas){
		//Draw action names
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			if (action.cost > totalSlots - takenSlots){
				canvas.drawText(action.name, 200, 630-50*i, new Color(1f, 1f, 1f, 0.5f));
			} else {
				canvas.drawText(action.name, 200, 630-50*i, Color.WHITE);
			}
		}
		
		if (choosingTarget){
			//draws grid target
			canvas.drawPointer(145+selectedX*100, 45+selectedY*100, Color.BLACK);
		} else {
			//draws action name pointers
			canvas.drawPointer(180,620-50*selectedAction, Color.CORAL);
		}
		
		//Draw action bar with 3 black boxes to show 4 slots
		int offset = 0;
		for (int i = 0; i < 4; i++){
			if (i < takenSlots) {
				canvas.drawBox(400+80*i,600,75,30,Color.CORAL);
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
