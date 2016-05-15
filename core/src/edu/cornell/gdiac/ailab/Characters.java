package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
//			Color waitColor = c.getActionBarColor(shouldDim,Color.valueOf("336699"));
//			Color castColor = c.getActionBarColor(shouldDim, Color.valueOf("990033"));
//			Color bufferColor = c.getActionBarColor(shouldDim, Color.WHITE.cpy());
////			drawing code for 2 color bar
//			c.actionBar.draw(canvas, count, waitColor, castColor);
			
			// drawing code for the waiting area and health action bar
			//c.actionBar.draw(canvas,count,waitColor,castColor,bufferColor);
			
//			 drawing for gauge style bar
			Color barColor = c.getActionBarColor(shouldDim, Color.WHITE.cpy());
			Color fillColor= c.getActionBarColor(shouldDim, Color.WHITE.cpy());	
			c.actionBar.draw(canvas, count, barColor,fillColor,c.castPosition,c.leftside,c.isSelecting(),
					c.selectionMenu.getQueuedActions(),c.selectionMenu.getSelectedAction(),c.getQueuedActions(),
					c.castActions,c.selectionMenu.getLerpVal(), c.effects);
			
			c.drawHealth(canvas, count, shouldDim);
			c.drawQueuedActions(canvas,count);
			
			// handle all character drawing logic here
		}
	}
	
	public void update(){
		for (Character c: this){
			c.update();
		}
	}
	
	public void drawSelectionMenu(GameCanvas canvas,GridBoard board,boolean shouldDim, boolean inSelection,boolean charActionHovered){
		Character clickedChar = clickedCharExists();
		boolean clickedCharExist = clickedChar != null;
		int count = 0;
        for (Character c : this){
        	count++;
        	if (inSelection){
        		// if no character is clicked we let the drawSelection determine if the character can draw.
        		// if there is a clicked char only the character equal to the clicked one will pass through
        		if (clickedChar == null||c == clickedChar){
        			c.drawSelection(canvas,board,count,clickedCharExist,charActionHovered);
        		}
        	}
			c.drawToken(canvas,count,shouldDim);
        }
	}
	
	public boolean isActionHovered(){
		for (Character c1 : this){
			for (int i = 0; i < c1.actionBar.actionOptions.size(); i++){
				Option o1 = c1.actionBar.actionOptions.get(i);
				if (o1.currentlyHovered){
					return true;
				}
			}
		}
		return false;
	}
	
	public Character clickedCharExists() {
		for (Character c : this){
			if (c.isClicked){
				return c;
			}
		}
		return null;
	}
	
	public Characters(){
		super();
	}
	
	public void draw(GameCanvas canvas,GridBoard board,boolean shouldDim, boolean inSelection,boolean charActionHovered){
		if (charActionHovered){
			drawActionHoveredDescription(canvas);
		}
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas,board,shouldDim, inSelection, charActionHovered);
	}
	
	public void drawActionHoveredDescription(GameCanvas canvas){
		Character c = null;
		Option o = null;
		Action a = null;
		for (Character c1 : this){
			for (int i = 0; i < c1.actionBar.actionOptions.size(); i++){
				Option o1 = c1.actionBar.actionOptions.get(i);
				if (o1.currentlyHovered){
					c = c1;
					o = o1;
					a = c1.actionBar.actions.get(i);
				}
			}
		}
		if (c == null || o == null || a == null){
			return;
		}
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float descript_x;
		if (c.leftside){
			descript_x = SelectionMenu.RELATIVE_DESCRIPTION_X_LEFT_POS *w;
		} else {
			descript_x = SelectionMenu.RELATIVE_DESCRIPTION_X_RIGHT_POS * w;
		}
		float descript_y = SelectionMenu.RELATIVE_DESCRIPTION_Y_POS * h;
		float descript_width = SelectionMenu.RELATIVE_DESCRIPTION_WIDTH *w;
		float descript_height = SelectionMenu.RELATIVE_DESCRIPTION_HEIGHT * h;
		canvas.drawAction(a, descript_x, descript_y, descript_width, descript_height);
	}
}
