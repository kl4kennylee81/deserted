package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
			Color waitColor = c.getActionBarColor(shouldDim,Color.RED.cpy());
			Color castColor = c.getActionBarColor(shouldDim, Color.WHITE.cpy());
			
			c.actionBar.draw(canvas,count,waitColor,castColor);
			c.drawHealth(canvas, count, shouldDim);
			c.drawToken(canvas,count,shouldDim);
			
			// handle all character drawing logic here
		}
	}
	
	public void drawSelectionMenu(GameCanvas canvas){
		int count = 0;
        for (Character c : this){
        	count++;
        	c.drawSelection(canvas,count);
        }
	}
	
	public Characters(){
		super();
	}
	
	public void draw(GameCanvas canvas,boolean shouldDim){
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas);
		
	}
}
