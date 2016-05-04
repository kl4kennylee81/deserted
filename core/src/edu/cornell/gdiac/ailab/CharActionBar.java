package edu.cornell.gdiac.ailab;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;

public class CharActionBar {
	
	public static Texture actionBar_center;
	public static Texture actionBar_right;
	public static Texture actionBar_left;
	public static Texture actionBar_leftRed;
	public static Texture actionBar_leftBlue;
	public static Texture actionBar_rightend;
	public static Texture actionBar_fill;
	
	public static TextureRegion actionBar_fill_left;
	public static TextureRegion actionBar_fill_right;
	public static TextureRegion actionBar_fill_middleWait;
	public static TextureRegion actionBar_fill_middleCast;
	public static Texture actionBar_centerpotrait;
	
	public static final int ACTIONBAR_MIDDLE_WAIT_WIDTH = 144;
	
	public static final int ACTIONBAR_MIDDLE_CAST_WIDTH = 121;
	
	public static final int ACTIONBAR_MIDDLE_WIDTH = 265;
	
	public static final int ACTIONBAR_LEFT_FILL = 433;
	
	public static final int ACTIONBAR_RIGHT_FILL = 430;
	
	public static final int ACTIONBAR_FILL_Y_OFFSET = 39;
	
	public static final int ACTIONBAR_FILL_X_OFFSET = 5;
	
	public static final int ACTIONBAR_FILL_HEIGHT = 50;
	
	// height ratio bar container/bar fill
	public static final float ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO = 1.8f;
	
	public static float CENTER_MULTIPLIER = 165f/76f;

	/** time all characters have to wait before they enter their casting period **/
	public static final float STARTING_BUFFER_TIME = 3f;
	
	public static final float CHAR_VELOCITY_SCREEN_RATIO = 0.0011f;
	
	public static final float MAX_BAR_SCREEN_RATIO = 0.9f;
	
	public static final float CAST_POINT_CENTERED = 0.5f;
	
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.032f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 1.0f;
	
	public static final float BAR_DIVIDER_WIDTH = 2f;
	
	private static final float Y_SPACING = 0.08f;
	
	// make this in terms of the max speed after applying speed modifier
	public static float MAX_TIME = 24;
	
	public static float HEALTH_TIME_PROPORTION = 1.0f;
	
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
		
		actionBar_center = new Texture(Constants.ACTIONBAR_CENTER_TEXTURE);
		actionBar_right = new Texture(Constants.ACTIONBAR_RIGHT_TEXTURE);
		actionBar_left = new Texture(Constants.ACTIONBAR_LEFT_TEXTURE);
		actionBar_leftRed = new Texture(Constants.ACTIONBAR_LEFTRED_TEXTURE);
		actionBar_leftBlue = new Texture(Constants.ACTIONBAR_LEFTBLUE_TEXTURE);
		actionBar_rightend = new Texture(Constants.ACTIONBAR_RIGHTEND_TEXTURE);
		actionBar_centerpotrait = new Texture(Constants.ACTIONBAR_CENTER_POTRAIT);
		
		actionBar_fill = new Texture(Constants.ACTIONBAR_FILL_TEXTURE);
		actionBar_fill_left = new TextureRegion(actionBar_fill,ACTIONBAR_FILL_X_OFFSET,ACTIONBAR_FILL_Y_OFFSET,ACTIONBAR_LEFT_FILL,ACTIONBAR_FILL_HEIGHT);	
		int fill_midX = ACTIONBAR_LEFT_FILL;
		actionBar_fill_middleWait = new TextureRegion(actionBar_fill,fill_midX,0,ACTIONBAR_MIDDLE_WAIT_WIDTH,actionBar_fill.getHeight());
		
		int fill_midCastX = ACTIONBAR_LEFT_FILL + ACTIONBAR_MIDDLE_WAIT_WIDTH;
		actionBar_fill_middleCast = new TextureRegion(actionBar_fill,fill_midCastX,0,ACTIONBAR_MIDDLE_CAST_WIDTH,actionBar_fill.getHeight());
		
		int fill_rightX = fill_midX + ACTIONBAR_MIDDLE_WIDTH;
		actionBar_fill_right = new TextureRegion(actionBar_fill,fill_rightX,ACTIONBAR_FILL_Y_OFFSET,ACTIONBAR_RIGHT_FILL,ACTIONBAR_FILL_HEIGHT);
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
				return 3f;
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
		
		// modify based on current health left
		float healthTime = waitTime*HEALTH_TIME_PROPORTION;
		float unmovedTime = waitTime*(1-HEALTH_TIME_PROPORTION);
		float modifiedWaitTime = (healthTime* this.healthProportion) +unmovedTime;
		
		// modify based on speed modifier
		modifiedWaitTime = (modifiedWaitTime + STARTING_BUFFER_TIME)/this.getSpeedModifier();
		
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
		
		// modify based on current health left
		float healthTime = waitTime*HEALTH_TIME_PROPORTION;
		float unmovedTime = waitTime*(1-HEALTH_TIME_PROPORTION);
		float modifiedWaitTime = (healthTime* this.healthProportion) +unmovedTime;
		
		// modify based on speed modifier
		modifiedWaitTime = (modifiedWaitTime + STARTING_BUFFER_TIME)/this.getSpeedModifier();
		
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
	
	public float getBarFillHeight(GameCanvas canvas){
		return getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
	}
	
	public float getFillY(GameCanvas canvas, int count){
		return getY(canvas,count) + (getBarHeight(canvas) - getBarFillHeight(canvas))/2;
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
	
	public void drawLeftEndpoint(GameCanvas canvas, float xPosBar, float yPosBar, boolean leftside,Color barColor){		
		if (leftside){
			// draw end point left
			float leftEndWidth = (CharActionBar.actionBar_leftBlue.getWidth()* this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER)/ CharActionBar.actionBar_leftBlue.getHeight();
			float leftEndHeight = this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER;
			
			float leftEndX = xPosBar - 0.9f*leftEndWidth;
			float leftEndY = yPosBar - 0.22f*leftEndHeight;
			
			// make this endpoint part of the curvature of the end filling
			canvas.drawTexture(actionBar_leftBlue,leftEndX,leftEndY,leftEndWidth,leftEndHeight,barColor);
		}
		else {
			// draw end point left
			float leftEndWidth = 0.7f*(CharActionBar.actionBar_leftRed.getWidth()* this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER)/ CharActionBar.actionBar_leftRed.getHeight();
			float leftEndHeight = 0.7f*this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER;
			
			float leftEndX = xPosBar - 0.9f*leftEndWidth;
			float leftEndY = yPosBar - leftEndHeight/6;
			
			// make this endpoint part of the curvature of the end filling
			canvas.drawTexture(actionBar_leftRed,leftEndX,leftEndY,leftEndWidth,leftEndHeight,barColor);
		}
	}
	
	public void drawLeftBar(GameCanvas canvas,float xPosBar,float yPosBar, Color barColor){
		float leftsideWidth = this.getWaitWidth(canvas);
		
		float leftsideHeight = this.getBarHeight(canvas);
		
		canvas.drawTexture(CharActionBar.actionBar_left,xPosBar,yPosBar,leftsideWidth,leftsideHeight,barColor);
	}
	
	public void drawRightBar(GameCanvas canvas, float xPosBar,float yPosBar, Color barColor){
		float rightsideWidth = this.getCastWidth(canvas);
		// literally no idea why its x 10
		float rightsideHeight = this.getBarHeight(canvas);
		
		float leftsideWidth = this.getWaitWidth(canvas);
		float rightside_x = xPosBar + leftsideWidth;
		float rightside_y = yPosBar;
		
		canvas.drawTexture(CharActionBar.actionBar_right,rightside_x,rightside_y,rightsideWidth,rightsideHeight,barColor);
	}
	
	public void drawCenterRing(GameCanvas canvas,float xPosBar,float yPosBar, Color barColor){
		float centerRingWidth = (CharActionBar.actionBar_center.getWidth()* this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER)/ CharActionBar.actionBar_center.getHeight();
		float centerRingHeight = this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER;
		
		float centerX = this.getCastPointX(canvas) - centerRingWidth/2;
		float centerY = yPosBar - centerRingHeight/3.7f;
		
		canvas.drawTexture(CharActionBar.actionBar_center, centerX, centerY,centerRingWidth,centerRingHeight,barColor);
	}
	
	public void drawRightEndpoint(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		float rightEndWidth = (CharActionBar.actionBar_center.getWidth()* this.getBarHeight(canvas)/ CharActionBar.actionBar_center.getHeight());
		float rightEndHeight = this.getBarHeight(canvas);
		
		float rightEndX = xPosBar + this.getWidth(canvas) - rightEndWidth/1.25f;
		float rightEndY = yPosBar;
		
		canvas.drawTexture(actionBar_rightend, rightEndX, rightEndY, rightEndWidth, rightEndHeight, barColor);
	}
	
	public void drawFill(GameCanvas canvas,float castPosition,float xPosBar, float yPosBar, Color barColor){
		float leftsideHeight = this.getBarHeight(canvas);
		float rightsideHeight = this.getBarHeight(canvas);
		float centerRingWidth = (CharActionBar.actionBar_center.getWidth()* this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER)/ CharActionBar.actionBar_center.getHeight();
		float centerRingHeight = this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER;
		// draw the fill based on a percentage
		// constant amount of pixels for center
			float left_proportion = ((float) (this.getWaitWidth(canvas) - centerRingWidth/2)/(this.getWaitWidth(canvas)));
			float wait_proportion = castPosition/this.getCastPoint();
			// entered the middle portion
			float waitFillPercent;
			float waitFillPercentMiddle;
			if (wait_proportion > left_proportion){
				waitFillPercent = 1;
				
				//TODO fixup this hack
				float proportionLeft = Math.min(wait_proportion - left_proportion,1-left_proportion+0.2f);
				waitFillPercentMiddle = proportionLeft/(1-left_proportion);
			}
			// just the left portion
			else {
				waitFillPercent = wait_proportion/left_proportion;
				waitFillPercentMiddle = 0;
			}

			int leftWidth = (int)(waitFillPercent * left_proportion * this.getWaitWidth(canvas));
			float leftHeight = (leftsideHeight/(ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO-0.25f));
			
			float leftFillY = (yPosBar + ((leftsideHeight- leftHeight))/1.48f);
			// draw the wait fill
			canvas.draw(actionBar_fill_left,barColor,xPosBar,leftFillY,leftWidth,leftHeight);
	
			// draw middle fill
			// create the middle waiting period on the fly
			float centerX = this.getCastPointX(canvas) - centerRingWidth/2;
			float centerY = yPosBar - centerRingHeight/3.7f;
			
			int middleWidth = (int)(waitFillPercentMiddle * actionBar_fill_middleWait.getRegionWidth());
			TextureRegion curMiddleWait = new TextureRegion(actionBar_fill_middleWait,0,0,middleWidth,actionBar_fill_middleWait.getRegionHeight());
			
			float middleFillWidth = (int)(waitFillPercentMiddle* (1-left_proportion)*this.getWaitWidth(canvas));
			float middleHeightFill =centerRingHeight/(ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO-0.55f);
			
			float middleWaitX = xPosBar + leftWidth;
			float middleFillY = centerY + (centerRingHeight -middleHeightFill)/2;
			
			canvas.draw(curMiddleWait,barColor,middleWaitX,middleFillY,middleFillWidth,middleHeightFill);
			
			if (castPosition >= this.getCastPoint()){
				float rightFillX = middleWaitX + centerRingWidth/2;			
				float cast_width_fill_prop = (castPosition - this.getCastPoint())/(1-this.getCastPoint());
					
				float rightFillY = (yPosBar + ((leftsideHeight- leftHeight))/1.48f);
					
				float cast_width_fill = cast_width_fill_prop * (this.getCastWidth(canvas));
				float cast_height_fill = (rightsideHeight/(ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO-0.25f));
					
				// draw the wait fill
				canvas.draw(actionBar_fill_right,barColor,rightFillX,rightFillY,cast_width_fill,cast_height_fill);
			}
	}
	
	public void drawCenterPotrait(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		// draw center potrait 
		float potraitWidth = (1.07f*CharActionBar.actionBar_center.getWidth()* this.getBarHeight(canvas))/ CharActionBar.actionBar_center.getHeight();
		float potraitHeight = this.getBarHeight(canvas)*CENTER_MULTIPLIER*0.72f;
		
		float potraitX = xPosBar + this.getWaitWidth(canvas) - potraitWidth/2.15f;
		float potraitY = yPosBar - potraitHeight/5;
		
		canvas.drawTexture(actionBar_centerpotrait,potraitX,potraitY,potraitWidth,potraitHeight,barColor);
	}
	
	public void drawSlots(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		//draw dazed slots as gray
		float castTotalWidth = this.getWidth(canvas) - this.getWaitWidth(canvas);
		float dazedWidth = (dazedSlots*1f/numSlots)*castTotalWidth;
		float dazedxPos = xPosBar + this.getWidth(canvas) - dazedWidth;
		
		float tickHeight = this.getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
		float tickY = yPosBar + tickHeight/2;
		
		canvas.drawBox(dazedxPos, tickY, dazedWidth, tickHeight,barColor);
		
	
		for (int i = 0; i < this.getTotalNumSlots(); i++){
			float intervalSize = this.getSlotWidth(canvas);
			float startCastX = xPosBar + this.getWaitWidth(canvas);
			canvas.drawBox(startCastX + i*intervalSize, tickY, BAR_DIVIDER_WIDTH,tickHeight, Color.DARK_GRAY);
		}
	}
	
	
	// draw gauge style 
	public void draw(GameCanvas canvas,int count,Color barColor,Color fillColor, 
			float castPosition,boolean leftside,List<ActionNode> selectingActions, 
			Action curSelectingAction, List<ActionNode> queuedActions){
		
		float xPosBar = getX(canvas);
		float yPosBar = getY(canvas,count);
			
		this.drawLeftEndpoint(canvas, xPosBar, yPosBar, leftside, barColor);
		this.drawLeftBar(canvas, xPosBar, yPosBar, barColor);
		this.drawRightBar(canvas, xPosBar, yPosBar, barColor);
		this.drawCenterRing(canvas, xPosBar, yPosBar, barColor);
		this.drawRightEndpoint(canvas, xPosBar, yPosBar, barColor);
		
		this.drawFill(canvas, castPosition, xPosBar, yPosBar, barColor);
		this.drawCenterPotrait(canvas, xPosBar, yPosBar, barColor);
		this.drawSlots(canvas, xPosBar, yPosBar, barColor);
	
	}
	
}
