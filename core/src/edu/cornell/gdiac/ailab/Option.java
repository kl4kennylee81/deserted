package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option {
	int srNo;
	boolean isSelected;
	int x_size;
	int y_size;
	float x_min;
	float y_min;
	Color regularColor = Color.FIREBRICK;
	String text;
	Color highlightedColor = new Color(1.0f, 0.7f, 0.0f, 1.0f);
	String image;
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public void setHighlighted(){
		isSelected = true;
	}
	
	public Option(float sx, float sy, int x_size, int y_size, String text, String image, int srNo){
		x_min = sx;
		y_min = sy;
		this.x_size = x_size;
		this.y_size = y_size;
		this.text = text;
		this.image = image;
		this.srNo = srNo;
	}
	
	public Option(float sx, float sy, int x_size, int y_size, int srNo){
		x_min = sx;
		y_min = sy;
		this.x_size = x_size;
		this.y_size = y_size;
		this.srNo = srNo;
	}
	
	public void setImage(String image){
		this.image = image;
	}
	
	public void setText(String text){
		this.text = text;
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
				y_size,isSelected() ? highlightedColor : regularColor, text);
	}


}