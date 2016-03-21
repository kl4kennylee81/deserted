package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class ActionBar {
	/** Ratio of the bar width to the screen */
	private static final float BAR_WIDTH_RATIO  = 0.5f;
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.035f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 0.075f;
	
	private static final float BAR_RELATIVE_X_POS = (1-BAR_WIDTH_RATIO)/2;
	
	private static final float BAR_DIVIDER_WIDTH = 4f;
	
	private static final float CAST_POINT = 0.5f;

	private static final int TOTAL_SLOTS = 4;
	
	public static float castPoint;
	
	public ActionBar(){
		ActionBar.castPoint = CAST_POINT;
	}

	public static int getTotalSlots(){
		return TOTAL_SLOTS;
	}
	
	public static float getBarWidthRatio(){
		return BAR_WIDTH_RATIO;
	}

	public static float getBarWidth(GameCanvas canvas){
		return BAR_WIDTH_RATIO * canvas.getWidth();
	}

	public static float getCastWidth(GameCanvas canvas){
		return (1-CAST_POINT) * getBarWidth(canvas);
	}

	public static float getBarX(GameCanvas canvas){
		return BAR_RELATIVE_X_POS * canvas.getWidth();
	}

	public static float getBarCastPoint(GameCanvas canvas){
		float start_x = getBarX(canvas);
		float bar_width = getBarWidth(canvas);
		float cast_point = bar_width * CAST_POINT;
		return start_x + cast_point;
	}
	
	public static float getRelativeY(){
		return BAR_RELATIVE_Y_POS;
	}

	public static float getBarY(GameCanvas canvas){
		return BAR_RELATIVE_Y_POS * canvas.getHeight();
	}
	public static float getBarHeight(GameCanvas canvas){
		return BAR_HEIGHT_RATIO * canvas.getHeight();
	}

	public static float getSlotWidth(GameCanvas canvas){
		float castWidth = getCastWidth(canvas);
		return castWidth/TOTAL_SLOTS;
	}

	public static float getSpacing(){
		return BAR_DIVIDER_WIDTH;
	}
	
	public void draw(GameCanvas canvas){
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float xPosBar = BAR_RELATIVE_X_POS *w;
		float yPosBar = BAR_RELATIVE_Y_POS * h;
		
		float widthBar = BAR_WIDTH_RATIO * w;
		float heightBar = BAR_HEIGHT_RATIO * h;
		
		// casting is red we draw red the full bar
		canvas.drawBox(xPosBar,yPosBar, widthBar, heightBar, Color.RED);
		
		float nonActWidth = widthBar * castPoint;
		
		// non casting is green we draw width up to the casting point
		canvas.drawBox(xPosBar, yPosBar, nonActWidth, heightBar, Color.GREEN);
		for (int i = 0; i < TOTAL_SLOTS; i++){
			float intervalSize = (widthBar*(1-castPoint))/TOTAL_SLOTS;
			float startCastX = xPosBar + nonActWidth;
			canvas.drawBox(startCastX + i*intervalSize, yPosBar, BAR_DIVIDER_WIDTH, heightBar, Color.BLACK);
		}	
		
	}
}
