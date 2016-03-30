package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class CharActionBar {

	public static final float CHAR_VELOCITY_SCREEN_RATIO = 0.001f;
	
	public static final float MAX_BAR_SCREEN_RATIO = 0.9f;
	
	public static final float CAST_POINT_CENTERED = 0.5f;
	
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.035f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 0.975f;
	
	private static final float BAR_DIVIDER_WIDTH = 4f;
	
	private static final float Y_SPACING = 0.065f;
	
	
	
	// make this in terms of the max speed after applying speed modifier
	public static float MAX_TIME = 24;
	
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
		this.speedModifier = 0;
	}
	
	public void setSpeedModifier(int val){
		speedModifier = val;
	}
	
	/** speed is in the percentage of that bar that is moved
	 *  the amount of pixels moved in the bar is constant between characters.
	 *  returns: the percentage of the bar moved in one frame
	 *  ex. (CHAR_VELOCITY = 0.1% of the canvas) to move a character along the bar
	 *  proportionally it will be CHAR_VELOCITY/ (ratio of bar to the canvas)
	 */
	public float getSpeed(){
		float proportionLengthBar = MAX_BAR_SCREEN_RATIO * this.getLength();
		return CHAR_VELOCITY_SCREEN_RATIO/proportionLengthBar;
	}
	
	public int getNumSlots(){
		return this.numSlots;
	}
	
	float getSpeedModifier() {
		switch (speedModifier) {
		case -3:
			return 0.6f;
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
				return 0.5f;
			} else {
				return 1.6f;
			}
		
		}
	}
	
	/** length as a proportion of the max_length **/
	public float getLength(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		float modifiedWaitTime = waitTime/this.getSpeedModifier();
		
		float newTotalTime = modifiedWaitTime + castTime;
		System.out.println(newTotalTime);
		return newTotalTime/MAX_TIME;
	}
	
	public float getCastPoint(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		float modifiedWaitTime = waitTime/this.getSpeedModifier();
		float newTotalTime = modifiedWaitTime + castTime;
		return modifiedWaitTime/newTotalTime;
	}
	
	public float getHurtLength(float hurtPercent){
		float trueLength = this.getLength();
		float waitLength = trueLength * this.getCastPoint();
		return 0;
//		
//		
//		float healthW = getWaitWidth(canvas);
//		float healthH = this.actionBar.getBarHeight(canvas);
//		
//		float damagedWidth = (healthW*this.health/this.maxHealth);
//		float leftoverWidth = healthW - damagedWidth;
//		return 0;
	}
	
	
	// need to account for offsetting for the cast point
	public float getX(GameCanvas canvas){
		float w = canvas.getWidth();
		float waitLength = this.getLength() * this.getCastPoint() * 2;
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
		return MAX_BAR_SCREEN_RATIO * this.getLength() * canvas.getWidth();
	}
	
	public float getCastWidth(GameCanvas canvas){
		return (1-this.getCastPoint()) * getWidth(canvas);
	}
	
	public float getSlotWidth(GameCanvas canvas){
		float castWidth = getCastWidth(canvas);
		return castWidth/getNumSlots();
	}
	
	public float getSlotWidth(){
		return (1-this.getCastPoint())/this.getNumSlots();
	}
	
	public float getBarHeight(GameCanvas canvas){
		return BAR_HEIGHT_RATIO * canvas.getHeight();
	}
	
	public float getWaitWidth(GameCanvas canvas){
		return this.getCastPoint() * getWidth(canvas);
	}
	
	public float actionExecutionTime(float takenSlots,float actionCost){
		float totalSlots = takenSlots + actionCost;
		float slotWidth = getSlotWidth();
		return this.getCastPoint() + totalSlots*slotWidth;
	}
	
	public float getBarCastPoint(GameCanvas canvas){
		float start_x = getX(canvas);
		float bar_width = getWidth(canvas);
		float cast_point = bar_width * this.getCastPoint();
		return start_x + cast_point;
	}
	
	public void draw(GameCanvas canvas,int count,Color waitColor,Color castColor){
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float xPosBar =getX(canvas);
		float yPosBar = getY(canvas,count);
		
		float widthBar = MAX_BAR_SCREEN_RATIO *this.getLength() * w;
		float heightBar = BAR_HEIGHT_RATIO * h;
		
		// casting is red we draw red the full bar
		canvas.drawBox(xPosBar,yPosBar, widthBar, heightBar, waitColor);
		
		float nonActWidth = widthBar * this.getCastPoint();
		
		// non casting is green we draw width up to the casting point
		canvas.drawBox(xPosBar, yPosBar, nonActWidth, heightBar, castColor);
		for (int i = 0; i < this.getNumSlots(); i++){
			float intervalSize = (widthBar*(1-this.getCastPoint()))/this.getNumSlots();
			float startCastX = xPosBar + nonActWidth;
			canvas.drawBox(startCastX + i*intervalSize, yPosBar, BAR_DIVIDER_WIDTH, heightBar, Color.BLACK);
		}	
		
	}
	
}
