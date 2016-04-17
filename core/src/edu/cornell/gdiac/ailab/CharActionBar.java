package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class CharActionBar {

	/** time all characters have to wait before they enter their casting period **/
	public static final float STARTING_BUFFER_TIME = 2f;
	
	public static final float CHAR_VELOCITY_SCREEN_RATIO = 0.002f;
	
	public static final float MAX_BAR_SCREEN_RATIO = 0.9f;
	
	public static final float CAST_POINT_CENTERED = 0.5f;
	
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.035f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 0.975f;
	
	public static final float BAR_DIVIDER_WIDTH = 4.5f;
	
	private static final float Y_SPACING = 0.065f;
	
	// make this in terms of the max speed after applying speed modifier
	public static float MAX_TIME = 30;
	
	// length of the cast bar in terms of the max bar length
	// actual pixel length = MAX_BAR_SCREEN_RATIO * length * canvas.getWidth()
	float length;
	
	// the cast point of the cast bar at a particular percentage for when you begin
	// casting ex. 0.5 = start casting halfway through the bar.
	float castPoint;
	
	// number of action slots
	private int numSlots;
	
	// Speed Modifier when affected by Speed Up/Slows
	int speedModifier;
	
	// slots affected by daze
	int dazedSlots;
	
	float healthProportion;
	
	// pass in seconds in waiting, seconds in casting, and number of slots
	// generates a cast bar with a length and cast point
	CharActionBar(int numSlots,float waitTime,float castTime){
		this.numSlots = numSlots;
		float totalTime = waitTime + castTime;
		this.length = totalTime/MAX_TIME;
		this.castPoint = waitTime/totalTime;
		this.speedModifier = 0;
	}
	
	public int getTotalNumSlots(){
		return numSlots;
	}
	
	public int getUsableNumSlots(){
		return Math.max(numSlots - getEffectedDazedSlots(),0);
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
	
	float getSpeedModifier() {
		switch (speedModifier) {
		case -2:
			// gets you 2x more time
			return 1/2f;
		case -1:
			// gets you 1.5x more time
			return 2/3f;
		case 0:
			return 1;
		case 1:
			return 3/2f;
		case 2:
			return 2f;
		default:
			if (speedModifier < -2){
				return 1/2f;
			} else if (speedModifier > 2){
				return 2f;
			}
			else{
				return 1;
			}
		
		}
	}
	
	/** clamp to be 0 or 1. */
	public int getEffectedDazedSlots(){
		return Math.min(dazedSlots, 1);
	}
	
	/** length as a proportion of the max_length **/
	public float getLength(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		
		// modify based on speed modifier
		float modifiedWaitTime = waitTime/this.getSpeedModifier();
		
		// modify based on current health left
		modifiedWaitTime = modifiedWaitTime * this.healthProportion  + STARTING_BUFFER_TIME;
		
		float newTotalTime = modifiedWaitTime + castTime;
		return newTotalTime/MAX_TIME;
	}
	
	public float getCastPointX(GameCanvas canvas){
		float xBar = this.getX(canvas);
		float waitWidth = this.getWaitWidth(canvas);
		return xBar + waitWidth;
	}
	
	/** given a castPosition ex. 0.7 returns the x pixel of it relative to the start of the bar **/
	public float getXInBar(GameCanvas canvas,float castposition){
		return this.getWidth(canvas) * castposition;
	}
	
	/** given a cast position and a cost of an action returns the x pixel 
	 * center of the action on the bar relative to the start of the bar **/
	public float getCenteredActionX(GameCanvas canvas, float executePoint, int cost){
		float executeX = getXInBar(canvas,executePoint);
		
		// rewind based on cost * slot width to get starting position
		float startingX = executeX - cost*this.getSlotWidth(canvas);
		float average = (executeX + startingX)/2;
		return average;
		
	}
	
	/** used to draw the width of the energy bar without the buffering period **/
	public float getWaitWidthNoBuffer(GameCanvas canvas){
		float widthWait = this.getWaitWidth(canvas);
		return widthWait - getBufferWidth(canvas);
	}
	
	public float getBufferWidth(){
		float timeProp = STARTING_BUFFER_TIME/MAX_TIME;
		return timeProp * MAX_BAR_SCREEN_RATIO;
	}
	
	public float getBufferWidth(GameCanvas canvas){
		return getBufferWidth() * canvas.getWidth();
	}
	
	
	public float getCastPoint(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		float modifiedWaitTime = waitTime/this.getSpeedModifier();
		// modify based on current health left
		modifiedWaitTime = modifiedWaitTime * this.healthProportion + STARTING_BUFFER_TIME;
		
		float newTotalTime = modifiedWaitTime + castTime;
		return modifiedWaitTime/newTotalTime;
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
		return castWidth/getTotalNumSlots();
	}
	
	public float getSlotWidth(){
		return (1-this.getCastPoint())/this.getTotalNumSlots();
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
	
	public void update(float healthProp){
		this.healthProportion = healthProp;
	}
	
	/** Section for handling drawing a normal bar to represent total initial energy 
	 *  total length unmodified by the current health he has left
	 * **/
	public float getTotalLength(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		
		// modify based on speed modifier
		float modifiedWaitTime = waitTime/this.getSpeedModifier() + STARTING_BUFFER_TIME;
		
		float newTotalTime = modifiedWaitTime + castTime;
		return newTotalTime/MAX_TIME;	
	}
	
	/** total width in pixels of the actionbar unmodified by amount of hp **/
	public float getTotalWidth(GameCanvas canvas){
		return MAX_BAR_SCREEN_RATIO * this.getTotalLength() * canvas.getWidth();
	}
	
	/** used to offset the buffer area of the bar from the x position **/
	public float getWaitWidthTotalNoBuffer(GameCanvas canvas){
		float widthWait = this.getTotalWaitWidth(canvas);
		return widthWait - getBufferWidth(canvas);
	}
	
	public float getTotalWaitWidth(GameCanvas canvas){
		return this.getTotalCastPoint() * getTotalWidth(canvas);
	}
	
	public float getTotalX(GameCanvas canvas){
		float w = canvas.getWidth();
		float waitLength = this.getTotalLength() * this.getTotalCastPoint() * 2;
		float widthProportion = MAX_BAR_SCREEN_RATIO *waitLength;
		float xOffsetProportion = (1-widthProportion)/2;
		return xOffsetProportion * w;
	}
	
	public float getTotalCastPoint(){
		float totalTime = this.length * MAX_TIME;
		float waitTime = totalTime * this.castPoint;
		float castTime = totalTime - waitTime;
		float modifiedWaitTime = waitTime/this.getSpeedModifier() + STARTING_BUFFER_TIME;
		// modify based on current health left
		
		float newTotalTime = modifiedWaitTime + castTime;
		return modifiedWaitTime/newTotalTime;
	}
	
	/** original draw code to draw the action bar constant and a waiting time area **/
	public void draw(GameCanvas canvas,int count,Color waitColor,Color castColor,Color bufferColor){
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float xPosBar = getTotalX(canvas);
		float yPosBar = getY(canvas,count);
		
		// width of the bar unmodified by hp
		float widthTotalBar = this.getTotalWidth(canvas);
		float heightBar = BAR_HEIGHT_RATIO * h;
		
		// waiting is red we draw red the full bar
		canvas.drawBox(xPosBar,yPosBar, widthTotalBar, heightBar, castColor);
		
		// non casting is green we draw width up to the casting point
		float waitWidth = widthTotalBar * this.getTotalCastPoint();
		canvas.drawBox(xPosBar, yPosBar, waitWidth, heightBar, waitColor);
		
		//buffering period is coral color for now
		float bufferWidth = this.getBufferWidth(canvas);
		float xPosBuffer = xPosBar + this.getWaitWidthTotalNoBuffer(canvas);
		canvas.drawBox(xPosBuffer, yPosBar, bufferWidth, heightBar, bufferColor);
	
		for (int i = 0; i < this.getTotalNumSlots(); i++){
			float intervalSize = this.getSlotWidth(canvas);
			float startCastX = xPosBar + waitWidth;
			canvas.drawBox(startCastX + i*intervalSize, yPosBar, BAR_DIVIDER_WIDTH, heightBar, Color.BLACK);
		}	
		
	}
	
	public void draw(GameCanvas canvas,int count,Color waitColor,Color castColor){
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float xPosBar = getX(canvas);
		float yPosBar = getY(canvas,count);
		
		// width of the bar unmodified by hp
		float widthTotalBar = this.getWidth(canvas);
		float heightBar = BAR_HEIGHT_RATIO * h;
		
		// waiting is red we draw red the full bar
		canvas.drawBox(xPosBar,yPosBar, widthTotalBar, heightBar, castColor);		
		
		// non casting is green we draw width up to the casting point
		float waitWidth = widthTotalBar * this.getCastPoint();
		canvas.drawBox(xPosBar, yPosBar, waitWidth, heightBar, waitColor);
		
		//draw dazed slots as gray
		float castTotalWidth = widthTotalBar - waitWidth;
		float dazedWidth = (dazedSlots*1f/numSlots)*castTotalWidth;
		float dazedxPos = xPosBar + widthTotalBar - dazedWidth;
		canvas.drawBox(dazedxPos, yPosBar, dazedWidth, heightBar, Color.GRAY);
	
		for (int i = 0; i < this.getTotalNumSlots(); i++){
			float intervalSize = this.getSlotWidth(canvas);
			float startCastX = xPosBar + waitWidth;
			canvas.drawBox(startCastX + i*intervalSize, yPosBar, BAR_DIVIDER_WIDTH, heightBar, Color.BLACK);
		}	
		
	}
	
}
