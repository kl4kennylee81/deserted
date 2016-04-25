package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HighlightScreen {
	
	Texture screen;
	/** image for the highlight screen  */
	private static final String HIGHLIGHT_TEXTURE = "images/white.png";
	private static Color color;
	private static List<TextureRegion> currentHighlights;
	private static Color highlightColor;
	private static boolean justScreen;
	private static float SCREEN_OPACITY = 0.55f;
	float lerpVal;
	boolean increasing;
	int maxY;
	int minY;
	ArrayList<Integer> x_s;
	
	public HighlightScreen(){
		screen = new Texture(HIGHLIGHT_TEXTURE);
		color = Color.BLACK.cpy();
		color.mul(1,1,1,SCREEN_OPACITY);
		highlightColor = new Color(255f/255f, 221f/255f, 153f/255f, 1f);
		highlightColor.set(highlightColor.r, highlightColor.g, highlightColor.b, 0.4f);
		currentHighlights = new ArrayList<TextureRegion>();
		lerpVal = 0f;
		increasing = true;
		maxY = Integer.MAX_VALUE;
		minY = Integer.MIN_VALUE;
		x_s = new ArrayList<Integer>();
	}
	
	public void setJustScreen(){
		justScreen = true;
		currentHighlights.clear();
	}
	
	public void noScreen(){
		justScreen = false;
	}
	public void draw(GameCanvas canvas){
		if (justScreen){
			canvas.drawScreen(0, 0, screen, canvas.getWidth(), canvas.getHeight(), color);
			return;
		}
		if (currentHighlights.size() == 0){
			return;
		}
		if (increasing){
			lerpVal+=0.02f;
			if (lerpVal >= 0.5f){
				increasing = false;
			}
		} else {
			lerpVal -= 0.02f;
			if (lerpVal <= 0f){
				increasing = true;
			}
		}
		Color toColor = Color.ORANGE.cpy();
		toColor = toColor.lerp(Color.WHITE.cpy(), lerpVal);
		canvas.drawScreen(0, 0, screen, canvas.getWidth(), canvas.getHeight(), color);
		for (TextureRegion currentHighlight:currentHighlights){
			canvas.draw(currentHighlight, toColor, currentHighlight.getRegionX(), 
					currentHighlight.getRegionY(), currentHighlight.getRegionWidth(), 
					currentHighlight.getRegionHeight());	
		}
	}
	
	public void addCurrentHighlight(double x, double y, double x_width, double y_width){
		currentHighlights.add(new TextureRegion(screen,(int)x,(int)y,(int)x_width,(int)y_width));
		if ((int)y < minY){
			minY = (int)y;
		}
		if ((int)y > maxY){
			maxY = (int)y;
		}
		x_s.add((int)x);
		x_s.add((int)x+(int)x_width);
		
		
	}
	
	public void addCurrentHighlight(double x, double y, double x_width, double y_width, GameCanvas canvas, GridBoard board){
		TextureRegion highlightTexture = new TextureRegion(screen,(int)x,(int)y,(int)x_width,(int)y_width);
		float charScale = Character.getCharScale(canvas, highlightTexture, board);
		currentHighlights.add(new TextureRegion(screen,(int)(x*charScale),(int)(y*charScale),
				(int)(x_width*charScale),(int)(y_width*charScale)));
	}
	
	public void removeHighlight(){
		currentHighlights.clear();
	}

}

//In selection dim the other characters
//When hovering leave everything as is - but make the current character shine plus his position on 
//the cast bar shine also
//also could we do 
