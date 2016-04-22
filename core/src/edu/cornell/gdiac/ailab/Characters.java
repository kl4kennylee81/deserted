package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
			Color waitColor = c.getActionBarColor(shouldDim,c.color.cpy());
			Color castColor = c.getActionBarColor(shouldDim, Color.RED.cpy());
			Color bufferColor = c.getActionBarColor(shouldDim, Color.WHITE.cpy());
			
			// drawing code for the waiting area and health action bar
			//c.actionBar.draw(canvas,count,waitColor,castColor,bufferColor);
			
			c.actionBar.draw(canvas, count, waitColor, castColor);
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
	
	public void drawSelectionMenu(GameCanvas canvas,boolean shouldDim){
		// find if there is any character with isClicked == true
		// if there is pass into drawSelection a boolean that there is a clickedCharacter
		boolean clickedCharExist = false;
		int count = 0;
        for (Character c : this){
        	count++;
    		c.drawSelection(canvas,count,clickedCharExist);
			c.drawToken(canvas,count,shouldDim);
        }
	}
	
	public Characters(){
		super();
	}
	
	public void draw(GameCanvas canvas,boolean shouldDim){
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas,shouldDim);
	}
}
