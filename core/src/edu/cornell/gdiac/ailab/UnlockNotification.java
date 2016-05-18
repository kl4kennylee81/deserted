package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class UnlockNotification {

	String text;
	Texture background;
	Texture unlockedCharTexture;
	
	private static final float UNLOCK_X_POS = 0.33f;
	
	private static final float UNLOCK_Y_POS = 0.4f;
	
	private static final float UNLOCK_WIDTH = 0.34f;
	
	private static final float UNLOCK_HEIGHT = 0.34f;
	
	private static final float TEXT_X_POS = 0.5f;
	
	private static final float TEXT_Y_POS = 0.7f;
	
	private static final float CHAR_X_POS = 0.45f;
	
	private static final float CHAR_Y_POS = 0.45f;
	
	private static final float CHAR_WIDTH = 0.1f;
	
	private static final float CHAR_HEIGHT = 0.2f;
	
	
	public UnlockNotification(String msg, Texture bg, Texture texture){
		this.text = msg;
		this.background = bg;
		this.unlockedCharTexture = texture;
	}
	
	public void draw(GameCanvas canvas, float w, float h){
		
		drawBackground(canvas, w, h);
		drawText(canvas, w, h);
		if (unlockedCharTexture != null){
			drawTexture(canvas, w, h);
		}
	}
	
	public void drawBackground(GameCanvas canvas, float w, float h){
		float x = UNLOCK_X_POS * w;
		float y = UNLOCK_Y_POS * h;
		float width = UNLOCK_WIDTH * w;
		float height = UNLOCK_HEIGHT * h;
		canvas.drawTexture(background, x, y, width, height, Color.WHITE);
	}
	
	public void drawText(GameCanvas canvas, float w, float h){
		float x = TEXT_X_POS * w;
		float y = TEXT_Y_POS * h;
		canvas.drawCenteredText(text, x, y, Color.WHITE, 1.2f);
	}
	
	public void drawTexture(GameCanvas canvas, float w, float h){
		float x = CHAR_X_POS * w;
		float y = CHAR_Y_POS * h;
		float width = CHAR_WIDTH * w;
		float height = CHAR_HEIGHT * h;
		canvas.drawTexture(unlockedCharTexture, x, y, width, height, Color.WHITE);
	}
	
	
}
