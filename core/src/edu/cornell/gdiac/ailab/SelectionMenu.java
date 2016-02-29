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
	}
	
	public void add(ActionNode actionNode){
		selectedActions.addLast(actionNode);
	}
	
	public void draw(GameCanvas canvas){
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			canvas.drawText(action.name, 200, 630-50*i);
		}
		canvas.drawPointer(180,620-50*selectedAction);
		int offset = 0;
		canvas.drawBox(400,600,300,30,Color.WHITE);
		for (ActionNode an : selectedActions){
			
			canvas.drawText(an.action.name, 400+offset+an.action.cost*30, 580);
			offset+=75*an.action.cost;
			canvas.drawBox(395+offset,600,10,30,Color.BLACK);
		}
		
	}
}
