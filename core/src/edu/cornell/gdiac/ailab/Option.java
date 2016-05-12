package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Option implements GUIElement{
	String optionKey;
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
	
	Color imageColor;
	
	String text;
	
	Texture image;
	
	boolean sameWidthHeight;
	
	boolean currentlyHovered;
	
	boolean isHighlighted;
	
	boolean checkNormalBounds;
	
	public void setImage(Texture t){
		image = t;
	}
	
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
	
	public Option(String text,String key){
		this.text = text;
		this.optionKey = key; 
		checkNormalBounds = false;
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
		
		if (sameWidthHeight){
			height = width;
		} 
		
		if (image != null){
			if(isHighlighted){
				canvas.drawHighlightToken(image, x, y, width, height, Color.YELLOW);
				canvas.drawLeftArrow(x + (width + 10), y + (width / 2) + 10, Color.GOLD);
			}
			Color imageColor = this.getColorImages();
			//canvas.drawTexture(image, x, y, width,height, Color.WHITE);
			canvas.drawTexture(image, x, y, width,height, imageColor);
			
		}
	}
	
	public void setColor(Color c){
		this.color = c;
	}
	
	public void setImageColor(Color c){
		this.imageColor = c;
	}
	
	public Color getColorImages(){
		if (this.isSelected){
			return Color.WHITE.cpy().add(Color.BLACK);
		}
		else if (this.imageColor != null) {
			return this.imageColor.cpy();
		} else {
			return Color.WHITE.cpy();
		}
			
	}
	
	public Color getColor(){
		if (this.isSelected){
			return Color.BLACK.cpy();
		}
		else if (this.color != null) {
			return this.color.cpy();
		} else {
			return Color.WHITE.cpy();
		}
	}

	@Override
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board) {
		// to capture within the image
		if (image != null){
			float width_m = this.width * canvas.getWidth();
			float height_m = this.height * canvas.getHeight();
			if (sameWidthHeight){
				height_m = width_m;
			}
			
			float x_m = this.xPosition*canvas.getWidth();
			float y_m = this.yPosition*canvas.getHeight();
			return (x <= x_m+width_m && x >= x_m && y <= y_m + height_m && y >= y_m);
		}
		// to capture within the text the problem with text is when i'm drawing it
		// it starts x,y at the top left coordinate i have no idea why, thus the minY
		// is actually the yPosition - height.
		else if (checkNormalBounds){
			float width_m = this.width * canvas.getWidth();
			float height_m = this.height * canvas.getHeight();	
			
			float x_m = this.xPosition*canvas.getWidth();
			float y_m = this.yPosition*canvas.getHeight();
			return (x <= x_m+width_m && x >= x_m && y <= y_m + height_m && y >= y_m);
		} else {
			float width_m = this.width * canvas.getWidth();
			float height_m = this.height * canvas.getHeight();	
			
			float x_m = this.xPosition*canvas.getWidth();
			float y_m = this.yPosition*canvas.getHeight() - height_m;
			return (x <= x_m+width_m && x >= x_m && y <= y_m + height_m && y >= y_m);
		}
	}


}
