package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class CharActionBar {

	public static float CHAR_VELOCITY_SCREEN_RATIO = 0.0005f;
	
	public static float MAX_BAR_SCREEN_RATIO = 1.0f;
	
	public static float CAST_POINT_CENTERED = 0.5f;
	
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.035f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 0.35f;
	
	private static final float BAR_DIVIDER_WIDTH = 4f;
	
	private static final float Y_SPACING = 0.075f;
	
	
	
	// make this in terms of the max speed after applying speed modifier
	public static float MAX_TIME = 20;
	
	// length of the cast bar in terms of the max bar length
	// actual pixel length = MAX_BAR_SCREEN_RATIO * length * canvas.getWidth()
	float length;
	
	// the cast point of the cast bar at a particular percentage for when you begin
	// casting ex. 0.5 = start casting halfway through the bar.
	float castPoint;
	
	// number of action slots
	int numSlots;
	
	// Speed Modifier when affected by Speed Up/Slows
	int speedModifier;
	
	// pass in seconds in waiting, seconds in casting, and number of slots
	// generates a cast bar with a length and cast point
	CharActionBar(int numSlots,float waitTime,float castTime){
		this.numSlots = numSlots;
		float totalTime = waitTime + castTime;
		this.length = totalTime/MAX_TIME;
		this.castPoint = waitTime/totalTime;
	}
	
	/** speed is in the percentage of that bar that is moved
	 *  the amount of pixels moved in the bar is constant between characters.
	 *  returns: the percentage of the bar moved in one frame
	 *  ex. (CHAR_VELOCITY = 0.1% of the canvas) to move a character along the bar
	 *  proportionally it will be CHAR_VELOCITY/ (ratio of bar to the canvas)
	 */
	public float getSpeed(){
		float proportionLengthBar = MAX_BAR_SCREEN_RATIO * this.length;
		return CHAR_VELOCITY_SCREEN_RATIO/proportionLengthBar;
	}
	
	float getSpeedModifier() {
		switch (speedModifier) {
		case -3:
			return 0.55f;
		case -2:
			return 0.7f;
		case -1:
			return 0.85f;
		case 0:
			return 1;
		case 1:
			return 1.15f;
		case 2:
			return 1.3f;
		case 3:
			return 1.45f;
		default:
			if (speedModifier < -3){
				return 0.4f;
			} else {
				return 1.6f;
			}
		
		}
	}
	
	// need to account for offsetting for the cast point
	public float getX(GameCanvas canvas){
		float w = canvas.getWidth();
		float waitLength = this.length * this.castPoint * 2;
		float widthProportion = MAX_BAR_SCREEN_RATIO *waitLength;
		float xOffsetProportion = (1-widthProportion)/2;
		return xOffsetProportion * w;
	}
	
	public float getY(GameCanvas canvas,int count){
		float h = canvas.getHeight();
		
		float ySpacing = Y_SPACING * h;
		return BAR_RELATIVE_Y_POS * h - count*ySpacing;
	}
	
	
	public float getWidth(GameCanvas canvas){
		return MAX_BAR_SCREEN_RATIO * this.length * canvas.getWidth();
	}
	
	public float getCastWidth(GameCanvas canvas){
		return (1-castPoint) * getWidth(canvas);
	}
	
	public float getSlotWidth(GameCanvas canvas){
		float castWidth = getCastWidth(canvas);
		return castWidth/numSlots;
	}
	
	public float getSlotWidth(){
		return (1-this.castPoint)/this.numSlots;
	}
	
	public float getBarHeight(GameCanvas canvas){
		return BAR_HEIGHT_RATIO * canvas.getHeight();
	}
	
	public float actionExecutionTime(float takenSlots,float actionCost){
		float totalSlots = takenSlots + actionCost;
		float slotWidth = getSlotWidth();
		return this.castPoint + totalSlots*slotWidth;
	}
	
	public float getBarCastPoint(GameCanvas canvas){
		float start_x = getX(canvas);
		float bar_width = getWidth(canvas);
		float cast_point = bar_width * castPoint;
		return start_x + cast_point;
	}
	
	public void draw(GameCanvas canvas,int count,Color waitColor,Color castColor){
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float xPosBar =getX(canvas);
		float yPosBar = getY(canvas,count);
		
		float widthBar = MAX_BAR_SCREEN_RATIO *this.length * w;
		float heightBar = BAR_HEIGHT_RATIO * h;
		
		// casting is red we draw red the full bar
		canvas.drawBox(xPosBar,yPosBar, widthBar, heightBar, waitColor);
		
		float nonActWidth = widthBar * castPoint;
		
		// non casting is green we draw width up to the casting point
		canvas.drawBox(xPosBar, yPosBar, nonActWidth, heightBar, castColor);
		for (int i = 0; i < this.numSlots; i++){
			float intervalSize = (widthBar*(1-this.castPoint))/this.numSlots;
			float startCastX = xPosBar + nonActWidth;
			canvas.drawBox(startCastX + i*intervalSize, yPosBar, BAR_DIVIDER_WIDTH, heightBar, Color.BLACK);
		}	
		
	}
	
}
