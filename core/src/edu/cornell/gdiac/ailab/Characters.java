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
			Color fillColor= c.getActionBarColor(shouldDim, Color.RED.cpy());	
			c.actionBar.draw(canvas, count, barColor,fillColor,c.castPosition);
			
			
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
	
	public void drawSelectionMenu(GameCanvas canvas,boolean shouldDim, boolean inSelection){
		boolean clickedCharExist = clickedCharExists();
		int count = 0;
        for (Character c : this){
        	count++;
        	if (inSelection){
        		c.drawSelection(canvas,count,clickedCharExist);
        	}
			c.drawToken(canvas,count,shouldDim);
        }
	}
	
	private boolean clickedCharExists() {
		for (Character c : this){
			if (c.isClicked){
				return true;
			}
		}
		return false;
	}
	
	public Characters(){
		super();
	}
	
	public void draw(GameCanvas canvas,boolean shouldDim, boolean inSelection){
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas,shouldDim, inSelection);
	}
}
