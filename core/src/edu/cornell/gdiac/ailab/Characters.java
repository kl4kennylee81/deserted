package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
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
	
	public void drawSelectionMenu(GameCanvas canvas,GridBoard board,boolean shouldDim, boolean inSelection){
		Character clickedChar = clickedCharExists();
		boolean clickedCharExist = clickedChar != null;
		int count = 0;
        for (Character c : this){
        	count++;
        	if (inSelection){
        		// if no character is clicked we let the drawSelection determine if the character can draw.
        		// if there is a clicked char only the character equal to the clicked one will pass through
        		if (clickedChar == null||c == clickedChar){
        			c.drawSelection(canvas,board,count,clickedCharExist);
        		}
        	}
			c.drawToken(canvas,count,shouldDim);
        }
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
	
	public void draw(GameCanvas canvas,GridBoard board,boolean shouldDim, boolean inSelection){
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas,board,shouldDim, inSelection);
	}
}
