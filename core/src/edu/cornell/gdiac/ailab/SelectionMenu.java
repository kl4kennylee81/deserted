package edu.cornell.gdiac.ailab;

import java.util.List;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class SelectionMenu {
	int height;
	int width;
	String font;
	float spacing;
	Action[] actions;
	int selectedAction;
	List<ActionNode> selectedActions;
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
}
