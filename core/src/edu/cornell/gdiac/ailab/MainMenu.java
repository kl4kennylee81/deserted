package edu.cornell.gdiac.ailab;

public class MainMenu {
	/** Available options to use */
	Option[] options;

	/** Index of current option */
	int selectedOption;

	/** Lerp value for highlighting */
	private float lerpVal;

	/** Lerp value increasing or decreasing */
	private boolean increasing;

	public MainMenu(Option[] options){
		this.options = options;
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		selectedActions = new LinkedList<ActionNode>();
		lerpVal = 0;
		increasing = true;
	}

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
			for (int i = 0; i < TOTAL_SLOTS; i++){
				selectedAction += 1;
				selectedAction %= TOTAL_SLOTS;
				if (canDoAction(actions[selectedAction])){
					return true;
				}
			}
		} else {
			for (int i = 0; i < TOTAL_SLOTS; i++){
				selectedAction -= 1;
				if (selectedAction < 0){
					selectedAction += TOTAL_SLOTS;
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
			for (int i = 0; i < TOTAL_SLOTS; i++){
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
		for (int i = 0; i < actions.length; i++){
			Action action = actions[i];
			if (action.cost > TOTAL_SLOTS - takenSlots || (!canMove() && action.pattern == Pattern.MOVE)){
				canvas.drawText(action.name, 200, 630-50*i, new Color(1f, 1f, 1f, 0.5f));
			} else {
				canvas.drawText(action.name, 200, 630-50*i, Color.BLACK);
			}
		}
		
		if (choosingTarget){
			//draws grid target
			canvas.drawPointer(145+selectedX*100, 45+selectedY*100, Color.BLACK);
		} else if (canAct()){
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
			canvas.drawCenteredText(an.action.name, 400+offset+75*an.action.cost/2, 580, Color.BLACK);
			offset+=75*an.action.cost;
		}
		
		//Write description
		canvas.drawCenteredText(actions[selectedAction].description, 550, 520, Color.BLACK);
	}
}
