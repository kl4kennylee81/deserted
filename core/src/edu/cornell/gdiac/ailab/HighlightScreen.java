package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HighlightScreen {
	
	Texture screen;
	/** image for the highlight screen  */
	private static final String HIGHLIGHT_TEXTURE = "images/white.png";
	private static Color color = Color.BLACK.cpy();
	private static TextureRegion currentHighlight;
	private static Color highlightColor = new Color(Color.WHITE);
	private static boolean justScreen;
	private static float SCREEN_OPACITY = 0.4f;
	
	public HighlightScreen(){
		screen = new Texture(HIGHLIGHT_TEXTURE);
		color.mul(1,1,1,SCREEN_OPACITY);
		highlightColor.set(highlightColor.r, highlightColor.g, highlightColor.b, 0.4f);
	}
	
	public void setJustScreen(){
		justScreen = true;
	}
	
	public void noScreen(){
		justScreen = false;
	}
	
	public void draw(GameCanvas canvas){
		if (justScreen){
			canvas.drawScreen(0, 0, screen, canvas.getWidth(), canvas.getHeight(), color);
			return;
		}
		if (currentHighlight == null){
			return;
		}
		canvas.drawScreen(0, 0, screen, canvas.getWidth(), canvas.getHeight(), color);
		canvas.draw(currentHighlight, highlightColor, currentHighlight.getRegionX(), 
				currentHighlight.getRegionY(), currentHighlight.getRegionWidth(), 
				currentHighlight.getRegionHeight());
	}
	
	public void setCurrentHighlight(int x, int y, int x_width, int y_width){
		currentHighlight = new TextureRegion(screen,x,y,x_width,y_width);
		
	}
	
	public void removeHighlight(){
		currentHighlight = null;
	}

}

//In selection dim the other characters
//When hovering leave everything as is - but make the current character shine plus his position on 
//the cast bar shine also
//also could we do 
