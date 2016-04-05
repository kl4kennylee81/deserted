package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option implements GUIElement{
	int srNo;
	boolean isSelected;
	float x_size;
	float y_size;
	/** ratio of the options left x-limit to the screen's width*/
	float x_min;
	/** ratio of the options top y-limit to the and screen's height*/
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
	
	public Option(float sx, float sy, float x_size, float y_size, String text, String image, int srNo){
		x_min = sx;
		y_min = sy;
		this.x_size = x_size;
		this.y_size = y_size;
		this.text = text;
		this.image = image;
		this.srNo = srNo;
	}
	
	public Option(float sx, float sy, float x_size, float y_size, int srNo){
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
		canvas.drawOption(x_min*canvas.getWidth()-x_size/2,y_min*canvas.getHeight()-y_size/2,new Texture(image),
				x_size, 
				y_size,isSelected() ? highlightedColor : regularColor, text);
	}

	@Override
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board) {
		float x_m = x_min*canvas.getWidth()-x_size/2;
		float y_m = y_min*canvas.getHeight()-y_size/2;
		return (x <= x_m+this.x_size && x >= x_m && y <= y_m + this.y_size && y >= y_m);
	}


}
