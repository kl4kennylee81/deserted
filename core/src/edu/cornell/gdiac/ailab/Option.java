package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option {
	int srNo;
	boolean isHighlighted;
	int x_size;
	int y_size;
	float x_min;
	float y_min;
	Color regularColor = Color.CHARTREUSE;
	String text;
	Color highlightedColor = Color.BLUE;
	String image;
	
	public boolean isHighlighted(){
		return isHighlighted;
	}
	
	public void setHighlighted(){
		isHighlighted = true;
	}
	
	public Option(float sx, float sy, int x_size, int y_size, String text, String image){
		x_min = sx;
		y_min = sy;
		this.x_size = x_size;
		this.y_size = y_size;
		this.text = text;
		this.image = image;
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
