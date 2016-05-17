package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{
	
	//the parent char and unaffiliated chars that we draw the health UI for
	HashMap<Character,List<Character>> healthChars;
	
	public void drawActionBars(GameCanvas canvas,boolean shouldDim){
		int count = 0;
		for (Character c:this){
			count++;
			
			Color barColor = c.getActionBarColor(shouldDim, Color.WHITE.cpy());
			Color fillColor= c.getActionBarColor(shouldDim, Color.WHITE.cpy());	
			c.actionBar.draw(canvas, count, barColor,fillColor,c.castPosition,c.leftside,c.isSelecting(),
					c.selectionMenu.getQueuedActions(),c.selectionMenu.getSelectedAction(),c.getQueuedActions(),
					c.castActions,c.selectionMenu.getLerpVal(), c.getEffects());
			
			c.drawQueuedActions(canvas,count);
		}
	}
	
	public void drawHealth(GameCanvas canvas,boolean shouldDim){
		int leftCount = 0;
		int rightCount = 0;
		for (Character c: healthChars.keySet()){
			List<Character> childrens = healthChars.get(c);
			if (c.leftside){
				leftCount++;
				c.drawHealth(canvas, leftCount, shouldDim,childrens);
			}
			else{
				rightCount++;
				c.drawHealth(canvas, rightCount, shouldDim,childrens);
			}
		}
	}
	
	public void update(){
		// accumulate list of ally and enemy to show health ui
		this.updateHealthUIList();
		
		for (Character c: this){
			c.update();
		}
	}
	
	public void updateHealthUIList(){
		healthChars.clear();
		for (Character c: this){
			if (c instanceof BossCharacter){
				Character parent = ((BossCharacter) c).getParent();
				if (!healthChars.containsKey(parent)){
					List<Character> children = new LinkedList<Character>();
					children.add(c);
					healthChars.put(parent, children);
				}
				else{
					healthChars.get(parent).add(c);
				}
			}
			else{
				healthChars.put(c, null);
			}
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
		this.healthChars = new HashMap<Character,List<Character>>();
	}
	
	public void draw(GameCanvas canvas,GridBoard board,boolean shouldDim, boolean inSelection,boolean charActionHovered){
		if (charActionHovered){
			drawActionHoveredDescription(canvas);
		}
		drawActionBars(canvas,shouldDim);

		drawSelectionMenu(canvas,board,shouldDim, inSelection, charActionHovered);
		drawHealth(canvas,shouldDim);
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
