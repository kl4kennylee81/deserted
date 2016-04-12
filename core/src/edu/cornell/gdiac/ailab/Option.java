package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option implements GUIElement{
	int srNo;
	boolean isSelected;
	
	/** relative width ex. 0.3f of the canvas's width **/
	float width;
	
	/** relative height ex. 0.3f of the canvas's height **/
	float height;
	
	/** relative x position on the canvas **/
	float xPosition;
	
	/** relative y position on the canvas **/
	float yPosition;
	
	Color color;
	
	String text;
	
	public float getX(){
		return xPosition;
	}
	
	public float getX(GameCanvas gc){
		return xPosition * gc.getWidth();
	}
	
	public float getY(){
		return yPosition;
	}
	
	public float getY(GameCanvas gc){
		return yPosition * gc.getHeight();
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getWidth(GameCanvas gc){
		return this.width * gc.getWidth();
	}
	
	public float getHeight(GameCanvas gc){
		return this.height * gc.getHeight();
	}
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public void setHighlighted(){
		isSelected = true;
	}
	
	public Option(String text, int srNo){
		this.text = text;
		this.srNo = srNo;
	}
	
	public void setBounds(float x,float y,float width,float height){
		this.xPosition= x;
		this.yPosition = y;
		this.width = width;
		this.height = height;
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
		float x = this.xPosition * canvas.getWidth();
		float y = this.yPosition * canvas.getHeight();
		float height = this.height * canvas.getHeight();
		float width = this.width * canvas.getWidth();
		Color textColor = this.getColor();
		canvas.drawText(this.text, x, y, textColor);
	}
	
	public void setColor(Color c){
		this.color = c;
	}
	
	public Color getColor(){
		if (this.isSelected){
			return Color.BLACK;
		}
		else{
			return this.color;
		}
	}

	@Override
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board) {
		float x_m = this.xPosition*canvas.getWidth()-this.width/2;
		float y_m = this.yPosition*canvas.getHeight()-this.height/2;
		return (x <= x_m+this.width && x >= x_m && y <= y_m + this.height && y >= y_m);
	}


}
