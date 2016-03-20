package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Characters extends LinkedList<Character>{

	private final float Y_START_POS = 0.95f;
	
	//ex. X_START_POS = 0.25 means left starts at position 25% of the canvasWidth
	// right starts at position 75% of the canvas (1-X_START_POS)*canvasWidth
	private final float X_START_POS = 0.020f;
	
	private final float Y_SPACING = 0.05f;
	
	private final float HEALTH_WIDTH = 0.175f;
	
	private final float HEALTH_HEIGHT = 0.03f;

	public void drawHealth(GameCanvas canvas,Character c,int count){
		Color col = c.isSelecting ? Color.WHITE.cpy().lerp(c.color, 0) : Color.WHITE;
		col = c.getHovering() ? c.color : col;
		
		float tokenX,tokenY;
		if (c.leftside){
			tokenX = X_START_POS*canvas.getWidth();
			tokenY = Y_START_POS*canvas.getHeight() - (Y_SPACING*canvas.getHeight()*count);
		}
		else{
			tokenX = ((1-X_START_POS)*canvas.getWidth()) - c.icon.getWidth();
			tokenY = Y_START_POS*canvas.getHeight() - (Y_SPACING*canvas.getHeight()*count);
		}
		canvas.drawTexture(c.icon, tokenX, tokenY, c.icon.getWidth(),c.icon.getHeight(),col);
		
		float healthW = HEALTH_WIDTH*canvas.getWidth();
		float healthH = HEALTH_HEIGHT*canvas.getHeight();
		
		float healthX,healthY;
		if (c.leftside){
			healthX = tokenX + c.icon.getWidth();
			healthY = tokenY;
		}
		else{
			healthX = tokenX - healthW;
			healthY = tokenY;
		}
		
		canvas.drawBox(healthX, healthY, healthW, healthH,col);
		canvas.drawBox(healthX, healthY, (int) (healthW*c.health/c.maxHealth), healthH, c.color);
	}
	
	public void drawHealths(GameCanvas canvas,Characters cList){
		int leftSideCount = 0;
		int rightSideCount = 0;
		for (Character c:cList){
			if (c.leftside){
				leftSideCount++;
				drawHealth(canvas,c,leftSideCount);
			}
			else{
				rightSideCount++;
				drawHealth(canvas,c,rightSideCount);
			}
		}
		
	}
	
	public Characters(){
		super();
	}
	
	public void drawHealthBars(GameCanvas canvas){
		drawHealths(canvas,this);
	}
}
