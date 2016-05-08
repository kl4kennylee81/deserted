package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class CompletionScreen {
	
	private static CompletionScreen instance = null;

	private static final float RELATIVE_X_POS = 0.3f;
	
	private static final float RELATIVE_Y_POS = 0.3f;
	
	private static final float RELATIVE_WIDTH = 0.4f;
	
	private static final float RELATIVE_HEIGHT = 0.4f;
	
	private static final float TOP_X_POS = 0.5f;
	
	private static final float TOP_Y_POS = 0.75f;
	
	private static final float BOTTOM_X_POS = 0.5f;
	
	private static final float BOTTOM_Y_POS = 0.3f;
	
	Texture image;
	
	
	private String topText;
	
	private String bottomText;
	
	protected CompletionScreen(){
		bottomText = "Press Enter to Continue or R to Return";
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
		if (newVal){
			topText = "Victory!";
		}else{
			topText = "Try Again!";
		}
	}
	
	
	public void draw(GameCanvas canvas) {
		drawTopText(canvas);
		drawImage(canvas);
		drawBottomText(canvas);
	}
	
	public void drawImage(GameCanvas canvas){
		float x = RELATIVE_X_POS * canvas.getWidth();
		float y = RELATIVE_Y_POS * canvas.getHeight();
		float width = RELATIVE_WIDTH * canvas.getWidth();
		float height = RELATIVE_HEIGHT * canvas.getHeight();
		
		canvas.drawTexture(image, x, y, width, height, Color.WHITE);
	}
	
	public void drawTopText(GameCanvas canvas) {
		float x = TOP_X_POS * canvas.getWidth();
		float y = TOP_Y_POS * canvas.getHeight();
		canvas.drawCenteredText(topText, x, y, Color.WHITE, 1.2f);
	}
	
	public void drawBottomText(GameCanvas canvas) {
		float x = BOTTOM_X_POS * canvas.getWidth();
		float y = BOTTOM_Y_POS * canvas.getHeight();
		canvas.drawCenteredText(bottomText, x, y, Color.WHITE, 1.2f);
	}
	
}
