package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	public void drawHealths(GameCanvas canvas,Characters cList,boolean shouldDim){
		int leftSideCount = 0;
		int rightSideCount = 0;
		for (Character c:cList){
			if (c.leftside){
				leftSideCount++;
				c.drawHealth(canvas,leftSideCount,shouldDim);
			}
			else{
				rightSideCount++;
				c.drawHealth(canvas,rightSideCount,shouldDim);
			}
		}
		
	}
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
			Color waitColor = c.getActionBarColor(shouldDim,Color.RED.cpy());
			Color castColor = c.getActionBarColor(shouldDim, Color.GREEN.cpy());
			
			c.actionBar.draw(canvas,count,waitColor,castColor);
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
	
	public void drawHealthBars(GameCanvas canvas,boolean shouldDim){
		drawHealths(canvas,this,shouldDim);
	}
	
	public void draw(GameCanvas canvas,boolean shouldDim){
		drawHealthBars(canvas,shouldDim);
		drawActionBars(canvas,shouldDim);
		drawSelectionMenu(canvas);
		
	}
}
