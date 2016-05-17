package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode;
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
	
	public static final float RELATIVE_ICON_LENGTH = 0.04f;

	private static final float ACTION_POINTER_OFFSET_X = 15;

	private static final float ACTION_POINTER_OFFSET_Y = 15;

	public static final float TEXT_ACTION_OFFSET = 30f;

	public static final float RELATIVE_DESCRIPTION_Y_POS = 0.33f;
	
	public static final float RELATIVE_DESCRIPTION_X_LEFT_POS = 0.035f;
	
	public static final float RELATIVE_DESCRIPTION_X_RIGHT_POS = 0.845f;
	
	public static final float RELATIVE_DESCRIPTION_WIDTH = 0.12f;
	
	public static final float RELATIVE_DESCRIPTION_HEIGHT = 0.25f;
	
	private static final float RADIUS_CONSTANT = 1.3f;
	
	private static final float CONFIRM_WIDTH = 0.05f;
	
	private static final float CONFIRM_HEIGHT = 0.04f;
	
	private static final Texture CONFIRM_BUTTON_UNPRESSED = new Texture("models/confirmbutton_unpressed.png");
	
	private static final Texture CONFIRM_BUTTON_PRESSED = new Texture("models/confirmbutton_pressed.png");
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");

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
	
	private Option[] options;
	
	public boolean highlightBox;
	
	public SelectionMenu(Action[] actions){
		this.actions = actions;
		this.options = new Option[actions.length+1];
		setOptions();
		selectedAction = 0;
		takenSlots = 0;
		choosingTarget = false;
		selectedActions = new LinkedList<ActionNode>();
		lerpVal = 0;
		increasing = true;
		highlightBox = false;
	}
	
	public void setOptions(){
		for (int i = 0; i < actions.length; i++){
			options[i] = new Option("",String.valueOf(i));
			options[i].sameWidthHeight = true;
			options[i].setImage(actions[i].menuIcon);
		}
		Option confirm = new Option("","Confirm");
		confirm.setImage(CONFIRM_BUTTON_UNPRESSED);
		confirm.setImageColor(Color.WHITE);
		options[actions.length] = confirm;
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
		}
		return an;
	}
	
	public Option[] getOptions(){
		return options;
	}
	
	public float getLerpVal(){
		return this.lerpVal;
	}
	
	public List<ActionNode> getQueuedActions(){
		return selectedActions;
	}
	
	public Action getSelectedAction(){
		if (selectedAction >= 0 && selectedAction < actions.length){
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
	
	public void trySelectingAction(CharActionBar actionBar, int i, boolean isSelecting){
		int usableNumSlots = actionBar.getUsableNumSlots();
		if (i == actions.length){
			if (isSelecting){
				selectedAction = i;
			}
		}
		else if (i >= 0 && i < actions.length){
			if (canDoAction(actions[i], usableNumSlots) || !isSelecting){
				setChoosingTarget(false);
				selectedAction = i;
			} 
		}
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
	public boolean changeSelected(boolean up,int numSlots, boolean isSelecting){
		int modVar = isSelecting ? actions.length+1 : actions.length;
		if (up){
			for (int i = 0; i <= actions.length; i++){
				selectedAction += 1;
				selectedAction %= modVar;
				if (canDoAction(selectedAction,numSlots)){
					if (this.getSelectedAction() != null) TutorialGameplayController.highlight_action = this.getSelectedAction().cost;
					return true;
				}
			}
		} else {
			for (int i = 0; i <= actions.length; i++){
				selectedAction -= 1;
				if (selectedAction < 0){
					selectedAction += modVar;
				}
				if (canDoAction(selectedAction,numSlots)){
					if (this.getSelectedAction() != null) TutorialGameplayController.highlight_action = this.getSelectedAction().cost;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean changeSelected(boolean up,int numSlots){
		if (up){
			for (int i = 0; i <= actions.length; i++){
				selectedAction += 1;
				selectedAction %= actions.length+1;
				if (canDoAction(selectedAction,numSlots)){
					if (this.getSelectedAction() != null) TutorialGameplayController.highlight_action = takenSlots + this.getSelectedAction().cost;
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
					if (this.getSelectedAction() != null) TutorialGameplayController.highlight_action = takenSlots + this.getSelectedAction().cost;
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
		if (selectedAction >= 0 && selectedAction < actions.length && actions[selectedAction].cost > numSlots - takenSlots){
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
			selectedActions.poll();
		}
	}

	public boolean isActionInvalid(int usableNumSlots,Action action){
		return (action.cost > usableNumSlots - takenSlots || (!canMove() && action.pattern == Pattern.MOVE));
	}
	
	/** determines the color for the action names in the selection menu while selecting **/
	public Color getActionColor(int usableNumSlots,Action action, boolean isSelecting){
		if (this.isActionInvalid(usableNumSlots,action) && isSelecting){
			Color dimColor = Color.WHITE.cpy().mul(1f,1f,1f,0.2f);
			return dimColor;
		} 
		else if (selectedAction < actions.length &&selectedAction >= 0 && actions[selectedAction].name == action.name){
			 /*if (this.choosingTarget){
				return Color.CORAL.cpy();
			 }
			 else{
				 return Color.WHITE.cpy().lerp(Color.CORAL,lerpVal);
			 }*/
			 return Color.CORAL.cpy();
		}
		else {
			return Color.WHITE;
		}
	}
	
	//TODO: update for dazed
	public void draw(GameCanvas canvas,CharActionBar actionBar,int count, boolean charIsClicked, float centerX, float centerY,
			float radius, boolean leftside, boolean writeDescription, boolean isSelecting){
		int totalNumSlots = actionBar.getTotalNumSlots();
		int usableNumSlots = actionBar.getUsableNumSlots();
		
		if (increasing){
			lerpVal+=0.012;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= 0.012;
			if (lerpVal <= 0.4){
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
		radius *= RADIUS_CONSTANT;
		
		if (isSelecting){
			if (!this.choosingTarget){
				for (int i = 0; i < actions.length; i++){
					float frac = (i+1f)/(actions.length+1);
					Action action = actions[i];
					Option option = options[i];
					float x;
					if (leftside){
						x = (float) (centerX + radius*Math.sin(frac*Math.PI));
					} else {
						x = (float) (centerX - radius*Math.sin(frac*Math.PI));
					}
					float y = (float) (centerY + radius*Math.cos(frac*Math.PI));
					x /= canvas.width;
					y /= canvas.height;
					
					option.setBounds(x-RELATIVE_ICON_LENGTH/2, y-RELATIVE_ICON_LENGTH/2, RELATIVE_ICON_LENGTH, RELATIVE_ICON_LENGTH);
					option.setImageColor(getActionColor(usableNumSlots,action,isSelecting));
					option.draw(canvas);
				}

				Option confirm = options[actions.length];
				float x = centerX/canvas.width;
				float y = (centerY - radius)/canvas.height;
				confirm.setBounds(x-CONFIRM_WIDTH/2, y-CONFIRM_HEIGHT/2, CONFIRM_WIDTH, CONFIRM_HEIGHT);
				
				if (confirm.currentlyHovered || selectedAction == actions.length){
					confirm.setImage(CONFIRM_BUTTON_PRESSED);
				} else {
					confirm.setImage(CONFIRM_BUTTON_UNPRESSED);
				}
				
				if (canAct(usableNumSlots)){
					confirm.setImageColor(Color.WHITE);
				} else {
					confirm.setImageColor(Color.WHITE.cpy().lerp(Color.GOLD,lerpVal));
				}
				
				confirm.draw(canvas);
			}
		} else {
			for (int i = 0; i < actions.length; i++){
				float frac = (i+1f)/(actions.length+1);
				Action action = actions[i];
				Option option = options[i];
				float x;
				if (leftside){
					x = (float) (centerX + radius*Math.sin(frac*Math.PI));
				} else {
					x = (float) (centerX - radius*Math.sin(frac*Math.PI));
				}
				float y = (float) (centerY + radius*Math.cos(frac*Math.PI));
				x /= canvas.width;
				y /= canvas.height;
				
				option.setBounds(x-RELATIVE_ICON_LENGTH/2, y-RELATIVE_ICON_LENGTH/2, RELATIVE_ICON_LENGTH, RELATIVE_ICON_LENGTH);
				/*if (charIsClicked){
					option.setImageColor(Color.WHITE);
				} else {
					option.setImageColor(getActionColor(usableNumSlots,action));
				}*/
				option.setImageColor(getActionColor(usableNumSlots,action,isSelecting));
				option.draw(canvas);
			}
		}
		
		/*
		 * 
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
				g = canvas.drawBoardWrapText("Done", text_x, text_y - offset_y, Color.CORAL);
			} else {
				g = canvas.drawBoardWrapText("Done", text_x, text_y - offset_y, Color.GREEN);
			}
		}
		
		float pointer_x = text_x - ACTION_POINTER_OFFSET_X;
		float pointer_y = text_y - selectedPointerOffset -  ACTION_POINTER_OFFSET_Y;
		//draws action name pointers
		if (!this.choosingTarget){
			canvas.drawPointer(pointer_x,pointer_y, Color.CORAL);
		}
		
		*/
		
		//Draw action bar with 3 black boxes to show 4 slots
//		float actionSlot_x = actionBar.getBarCastPoint(canvas);
//		float actionSlot_y = actionBar.getFillY(canvas,count);
//		
//		float slot_width = actionBar.getSlotWidth(canvas);
//		float slot_height = actionBar.getBarFillHeight(canvas);
//		
//		int offset = 0;
//		for (int i = 0; i < totalNumSlots; i++){
//			float curSlot_x = actionSlot_x + ((slot_width) * i) + CharActionBar.BAR_DIVIDER_WIDTH;
//			float slot_w_space = slot_width-CharActionBar.BAR_DIVIDER_WIDTH;
//			if (i < takenSlots) {
//				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Constants.CAST_COLOR.cpy());
//			} else if (selectedAction < actions.length && i < takenSlots+actions[selectedAction].cost){
//				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE.cpy().lerp(Constants.CAST_COLOR.cpy(),lerpVal));
//			} else if (i >= usableNumSlots){
//				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.GRAY);
//			} else {
//				canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE);
//			}
//		}
		
		//Write the names of selected action
//		for (ActionNode an : selectedActions){
//			float x_pos = actionSlot_x + offset + (slot_width*an.action.cost/2);
//			float y_pos = actionSlot_y;
//			canvas.drawCenteredText(an.action.name, x_pos, y_pos, Color.WHITE);
//			offset+=slot_width*an.action.cost;
//		}
		
		float relative_offset = 0.065f;
		
		//Write description
		if (writeDescription){
			if (selectedAction < actions.length && selectedAction >= 0){
				Action action = actions[selectedAction];
				float descript_x;
				if (leftside){
					descript_x = RELATIVE_DESCRIPTION_X_LEFT_POS *w;
				} else {
					descript_x = RELATIVE_DESCRIPTION_X_RIGHT_POS * w;
				}
				float descript_y = RELATIVE_DESCRIPTION_Y_POS * h;
				float descript_width = RELATIVE_DESCRIPTION_WIDTH *w;
				float descript_height = RELATIVE_DESCRIPTION_HEIGHT * h;
				canvas.drawAction(action, descript_x, descript_y, descript_width, descript_height, highlightBox);
			}
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
	
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board){
		for (Option o : options){
			if (o.contains(x, y, canvas, board)){
				return true;
			}
		}
		return false;
	}
	
}
