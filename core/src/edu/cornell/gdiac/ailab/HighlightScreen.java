package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.CurrentHighlight;
import edu.cornell.gdiac.ailab.TutorialSteps.TutorialAction;

public class HighlightScreen {
	
	Texture screen;
	/** image for the highlight screen  */
	private static final String HIGHLIGHT_TEXTURE = "images/white.png";
	private static final String HIGHLIGHT_CIRCLE_TEXTURE = "images/white_circle_2.png";
	private static final String HIGHLIGHT_RECTANGLE_TEXTURE = "images/rounded_rectangle.png";
	private static Color color;
	private static List<CurrentHighlight> currentHighlights;
	private static Color highlightColor;
	private static boolean justScreen;
	private static float SCREEN_OPACITY = 0.60f;
	float lerpVal;
	boolean increasing;
	int maxY;
	int minY;
	ArrayList<Integer> x_s;
	Texture highlight_circle;
	Texture highlight_rect;
	
	public HighlightScreen(){
		screen = new Texture(HIGHLIGHT_TEXTURE);
		highlight_circle = new Texture(HIGHLIGHT_CIRCLE_TEXTURE);
		highlight_rect = new Texture(HIGHLIGHT_RECTANGLE_TEXTURE);
		color = Color.BLACK.cpy();
		color.mul(1,1,1,SCREEN_OPACITY);
		highlightColor = new Color(255f/255f, 221f/255f, 153f/255f, 1f);
		highlightColor.set(highlightColor.r, highlightColor.g, highlightColor.b, 0.4f);
		currentHighlights = new ArrayList<CurrentHighlight>();
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
		for (CurrentHighlight currentHighlight:currentHighlights){
			canvas.draw(currentHighlight.isSquare? highlight_rect : highlight_circle, toColor, (float)currentHighlight.xPos, 
					(float)currentHighlight.yPos, (float)currentHighlight.width, 
					(float)currentHighlight.height);
			
		}
	}
	
	public void addCurrentHighlight(CurrentHighlight current){
		currentHighlights.add(current);
		
	}
	
	public void addCurrentHighlight(CurrentHighlight current, GameCanvas canvas, GridBoard board){
		TextureRegion highlightTexture = new TextureRegion(screen,(int)current.xPos,(int)current.yPos,(int)current.width,(int)current.height);
		float charScale = Character.getCharScale(canvas, highlightTexture, board);
		current.width = (int)(current.width * charScale);
		current.height = (int)(current.height * charScale);
		currentHighlights.add(current);
	}
	
	public void removeHighlight(){
		currentHighlights.clear();
	}

}

//In selection dim the other characters
//When hovering leave everything as is - but make the current character shine plus his position on 
//the cast bar shine also
//also could we do 
