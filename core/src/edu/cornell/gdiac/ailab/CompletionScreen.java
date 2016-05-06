package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class CompletionScreen {
	
	private static CompletionScreen instance = null;

	private static final float RELATIVE_X_POS = 0.15f;
	
	private static final float RELATIVE_Y_POS = 0.25f;
	
	private static final float RELATIVE_WIDTH = 0.7f;
	
	private static final float RELATIVE_HEIGHT = 0.5f;
	
	Texture image;
	
	private boolean isWin;
	
	protected CompletionScreen(){

	}
	
	public static CompletionScreen getInstance(){
		if (instance == null) {
			instance = new CompletionScreen();
		}
		return instance;
	}
	
	public void setImage(Texture im){
		this.image = im;
	}
	
	public void setIsWin(boolean newVal){
		this.isWin = newVal;
	}
	
	
	public void draw(GameCanvas canvas) {
		float x = RELATIVE_X_POS * canvas.getWidth();
		float y = RELATIVE_Y_POS * canvas.getHeight();
		float width = RELATIVE_WIDTH * canvas.getWidth();
		float height = RELATIVE_HEIGHT * canvas.getHeight();
		
		canvas.drawTexture(image, x, y, width, height, Color.WHITE);
	}
	
}
