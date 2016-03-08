package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option {
	int srNo;
	int isSelected;
	int x_size;
	int y_size;
	float x_min;
	float y_min;
	Color regularColor;
	String text;
	Color highlightedColor;
	String image;
	
	public boolean isHighlighted(){
		return false;
	}
	
	public void setHighlighted(){
		
	}
	
	
	/**
	 * Draws an option for the start screen at position (x,y). 
	 *
	 *
	 * @param x The x index for the Option cell
	 * @param y The y index for the Option cell
	 */
	public void draw(GameCanvas canvas) {
		canvas.drawOption(x_min,y_min,new Texture(image),x_size, 
				y_size,isHighlighted() ? highlightedColor : regularColor, text);
	}


}
