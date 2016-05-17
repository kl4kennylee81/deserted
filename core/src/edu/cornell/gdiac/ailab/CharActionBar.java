package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode;

public class CharActionBar {
	
	public static String INTERRUPT_TEXT = "Interrupted";
	
	public static Texture actionBar_center;
	public static Texture actionBar_leftRed;
	public static Texture actionBar_leftBlue;
	public static Texture actionBar_rightend;
	public static Texture actionBar_fillWait;
	public static Texture actionBar_fillCast;
	public static Texture actionBar_icon;
	
	public static Texture cancel_token;
	
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
	
	public static float CENTER_MULTIPLIER = 2f;

	/** time all characters have to wait before they enter their casting period **/
	public static final float STARTING_BUFFER_TIME = 0f;
	
	public static final float CHAR_VELOCITY_SCREEN_RATIO = 0.0008f;
	
	public static final float MAX_BAR_SCREEN_RATIO = 0.8f;
	
	public static final float CAST_POINT_CENTERED = 0.5f;
	
	/** Ration of the bar height to the screen */
	private static final float BAR_HEIGHT_RATIO = 0.035f;	
	
	/** the x position of the bar should start at the top 7/8 of the screen **/
	private static final float BAR_RELATIVE_Y_POS = 1.0f;
	
	public static final float BAR_DIVIDER_WIDTH = 2f;
	
	private static final float Y_SPACING = 0.08f;
	
	private static final float RELATIVE_EFFECT_ICON_WIDTH = 0.025f;
	
	private static final int CIRCLE_ANGLE = 360;
	
	// make this in terms of the max speed after applying speed modifier
	public static float MAX_TIME = 30;
	
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
	
	ArrayList<Option> actionOptions;
	ArrayList<Action> actions;
	
	ArrayList<Option> tempActionOptions;
	ArrayList<Action> tempActions;
	
	boolean leftHighlighted;
	
	// pass in seconds in waiting, seconds in casting, and number of slots
	// generates a cast bar with a length and cast point
	CharActionBar(int numSlots,float waitTime,float castTime){
		this.numSlots = numSlots;
		float totalTime = waitTime + castTime;
		this.length = totalTime/MAX_TIME;
		this.castPoint = waitTime/totalTime;
		this.speedModifier = 0;
		
		actionBar_center = new Texture(Constants.ACTIONBAR_CENTER_TEXTURE);
		actionBar_leftRed = new Texture(Constants.ACTIONBAR_LEFTRED_TEXTURE);
		actionBar_leftBlue = new Texture(Constants.ACTIONBAR_LEFTBLUE_TEXTURE);
		actionBar_rightend = new Texture(Constants.ACTIONBAR_RIGHTEND_TEXTURE);
		actionBar_fillWait = new Texture(Constants.ACTIONBAR_FILLWAIT_TEXTURE);
		actionBar_fillCast = new Texture(Constants.ACTIONBAR_FILLCAST_TEXTURE);
		actionBar_icon = new Texture(Constants.ACTIONBAR_ICON);
		
		
		cancel_token = new Texture(Constants.CANCEL_TOKEN);
		
		actionOptions = new ArrayList<Option>();
		actions = new ArrayList<Action>();
		
		tempActionOptions = new ArrayList<Option>();
		tempActions = new ArrayList<Action>();
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
		float modifiedWaitTime = (waitTime + STARTING_BUFFER_TIME)/this.getSpeedModifier();
		
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
		
		// modify based on speed modifier
		float modifiedWaitTime = (waitTime + STARTING_BUFFER_TIME)/this.getSpeedModifier();
		
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
		float leftEndWidth = CharActionBar.actionBar_rightend.getWidth() * this.getBarHeight(canvas)/CharActionBar.actionBar_rightend.getHeight();
		float leftEndHeight = this.getBarHeight(canvas);
		
		float leftEndX = xPosBar;
		float leftEndY = yPosBar;

		float Sx = leftEndWidth/CharActionBar.actionBar_rightend.getWidth();
		float Sy = leftEndHeight/CharActionBar.actionBar_rightend.getHeight();
		
		// make this endpoint part of the curvature of the end filling
		canvas.draw(actionBar_rightend,barColor,0,0,leftEndX,leftEndY,0,-Sx,Sy);
	}
	
	public void drawBar(GameCanvas canvas,float xPosBar,float yPosBar, Color barColor){
		float barWidth = this.getWidth(canvas);
		
		float barHeight = this.getBarHeight(canvas);
		
		canvas.drawTexture(CharActionBar.actionBar_center,xPosBar,yPosBar,barWidth,barHeight,barColor);
	}
	
	public void drawRightEndpoint(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		float rightEndWidth = CharActionBar.actionBar_rightend.getWidth() * this.getBarHeight(canvas)/CharActionBar.actionBar_rightend.getHeight();
		float rightEndHeight = this.getBarHeight(canvas);
		
		float rightEndX = xPosBar + this.getWidth(canvas);
		float rightEndY = yPosBar - rightEndHeight*0.018f;
		
		canvas.drawTexture(actionBar_rightend, rightEndX, rightEndY, rightEndWidth, rightEndHeight, barColor);
	}
	
	public void drawWaitFill(GameCanvas canvas,float xPosBar,int count,Color barColor){
		float xPos = xPosBar;
		float yPos = this.getFillY(canvas, count);
		
		float waitWidth = this.getWaitWidth(canvas);
		float waitHeight = this.getBarFillHeight(canvas);
		canvas.drawTexture(CharActionBar.actionBar_fillWait, xPos, yPos, waitWidth, waitHeight, barColor);
	}
	
	public void drawCastFill(GameCanvas canvas,float xPosBar,int count,Color barColor,float castPosition){
		float xPos = xPosBar + this.getWaitWidth(canvas);
		float yPos = this.getFillY(canvas, count);
		
		float castWidth = this.getWidth(canvas)*castPosition - this.getWaitWidth(canvas);
		float castHeight = this.getBarFillHeight(canvas);
		if (castPosition > this.getCastPoint()){
			canvas.drawTexture(CharActionBar.actionBar_fillCast, xPos, yPos, castWidth, castHeight, barColor);
		}
	}
	
	public void drawCenterIcon(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		float iconHeight = this.getBarHeight(canvas) * CharActionBar.CENTER_MULTIPLIER;
		float iconWidth = actionBar_icon.getWidth() * (this.getBarHeight(canvas)*CharActionBar.CENTER_MULTIPLIER)/actionBar_icon.getHeight();
		
		float iconX = xPosBar + this.getWaitWidth(canvas) - iconWidth/4;
		float iconY = yPosBar - iconHeight/4;
		
		canvas.drawTexture(actionBar_icon,iconX, iconY, iconWidth, iconHeight, barColor);
	}
	
	public int drawSlots(GameCanvas canvas,int curSlot,
			List<ActionNode> listActions,float xPosBar,float yPosBar,Color barColor,boolean drawBoxes){
		int numberSlots = curSlot;
		for (ActionNode an:listActions){
			numberSlots = this.drawSlot(canvas,numberSlots,an,xPosBar,yPosBar,barColor,drawBoxes);
		}
		return numberSlots;
	}
	
//	float curSlot_x = actionSlot_x + ((slot_width) * i) + CharActionBar.BAR_DIVIDER_WIDTH;
//	float slot_w_space = slot_width-CharActionBar.BAR_DIVIDER_WIDTH;
//	if (i < takenSlots) {
//		canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Constants.CAST_COLOR.cpy());
//	} else if (selectedAction < actions.length && i < takenSlots+actions[selectedAction].cost){
//		canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE.cpy().lerp(Constants.CAST_COLOR.cpy(),lerpVal));
//	} else if (i >= usableNumSlots){
//		canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.GRAY);
//	} else {
//		canvas.drawBox(curSlot_x,actionSlot_y,slot_w_space,slot_height,Color.WHITE);
//	}
	
	public void drawSlotBox(GameCanvas canvas,int curSlots,
			Action curAction,float xPosBar,float yPosBar,Color slotColor,boolean addOption){
		float intervalSize = this.getSlotWidth(canvas);
		float slotX = xPosBar + this.getWaitWidth(canvas) + intervalSize*curSlots;
		float actionSlotWidth = intervalSize*curAction.cost;
		float actionSlotHeight = this.getBarFillHeight(canvas);
		
		canvas.drawBox(slotX,yPosBar,actionSlotWidth,actionSlotHeight,slotColor);
		
		if (addOption){
			tempActions.add(curAction);
			Option o = new Option("","");
			o.setBounds(slotX/canvas.getWidth(), yPosBar/canvas.getHeight(), actionSlotWidth/canvas.getWidth(), actionSlotHeight/canvas.getHeight());
			o.checkNormalBounds = true;
			tempActionOptions.add(o);
		}
	}
	
	public int drawSlot(GameCanvas canvas,int curSlots,
			ActionNode actionNode,float xPosBar,float yPosBar,Color slotColor,boolean drawBoxes){
		if (actionNode != null && curSlots + actionNode.action.cost <= this.getTotalNumSlots()){
			float tickHeight = this.getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
			float tickY = yPosBar;
			float intervalSize = this.getSlotWidth(canvas);
			float startCastX = xPosBar + this.getWaitWidth(canvas) + intervalSize*curSlots;

			if (drawBoxes){
				this.drawSlotBox(canvas,curSlots,actionNode.action,xPosBar,yPosBar,slotColor,true);
			}
			
			canvas.drawBox(startCastX, tickY, BAR_DIVIDER_WIDTH,tickHeight, Color.DARK_GRAY);		
			
			return curSlots + actionNode.action.cost;
		}
		else{
			return curSlots;
		}
	}
	
	public int drawSelectingActionSlot(GameCanvas canvas,int curSlots,
			Action curAction,float xPosBar,float yPosBar,Color slotColor,boolean drawBoxes,float lerpVal){
		
		if (curAction != null && curSlots + curAction.cost <= this.getTotalNumSlots()){
			float tickHeight = this.getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
			float tickY = yPosBar;
			float intervalSize = this.getSlotWidth(canvas);
			
			for (int i=0;i<curAction.cost;i++){
				float startCastX = xPosBar + this.getWaitWidth(canvas) + intervalSize*(curSlots+i);
				
				if (drawBoxes){
					Color selectingColor = Color.WHITE.cpy().lerp(slotColor,lerpVal);
					this.drawSlotBox(canvas,curSlots,curAction,xPosBar,yPosBar,selectingColor,false);
				}
				
				canvas.drawBox(startCastX, tickY, BAR_DIVIDER_WIDTH,tickHeight, Color.DARK_GRAY);		
			}
			return curSlots + curAction.cost;
		}
		else{
			return curSlots;
		}
	}
	
	public void drawDazedSlots(GameCanvas canvas,float xPosBar,float yPosBar,Color barColor){
		//draw dazed slots as gray
		float castTotalWidth = this.getWidth(canvas) - this.getWaitWidth(canvas);
		float dazedWidth = (dazedSlots*1f/numSlots)*castTotalWidth;
		float dazedxPos = xPosBar + this.getWidth(canvas) - dazedWidth;
		
		float tickHeight = this.getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
		float tickY = yPosBar + tickHeight/2;
		
		canvas.drawBox(dazedxPos, tickY, dazedWidth, tickHeight,barColor);
	}
	
	public void drawRemainingSlots(GameCanvas canvas,int curSlots, float xPosBar, float yPosBar, Color barColor){
		float tickHeight = this.getBarHeight(canvas)/CharActionBar.ACTIONBAR_HEIGHT_CONTAINER_FILL_RATIO;
		float tickY = yPosBar;
		
		for (int i = curSlots; i < this.getTotalNumSlots(); i++){
			float intervalSize = this.getSlotWidth(canvas);
			float startCastX = xPosBar + this.getWaitWidth(canvas);
			canvas.drawBox(startCastX + i*intervalSize, tickY, BAR_DIVIDER_WIDTH,tickHeight, Color.DARK_GRAY);
		}	
	}
	
	public void drawSlots(GameCanvas canvas,List<ActionNode> castActions,
			List<ActionNode> queuedActions,List<ActionNode> selectingActions,Action curAction,
			int count,Color barColor,boolean isSelecting,boolean drawBoxes,float lerpVal){
		float xPosBar = getX(canvas);
		float yPosBar = getFillY(canvas,count);
		
		this.drawDazedSlots(canvas,xPosBar,yPosBar,barColor);
		
		int curSlots = 0;
		curSlots = drawSlots(canvas,curSlots,castActions,xPosBar,yPosBar,Constants.CAST_COLOR.cpy(),drawBoxes);
		curSlots = drawSlots(canvas,curSlots,queuedActions,xPosBar,yPosBar,Color.WHITE.cpy(),drawBoxes);
		if (isSelecting && curSlots < this.getTotalNumSlots()){
			curSlots = drawSlots(canvas,curSlots,selectingActions,xPosBar,yPosBar,Constants.CAST_COLOR.cpy(),drawBoxes);
			curSlots = this.drawSelectingActionSlot(canvas, curSlots, curAction, xPosBar, yPosBar, Constants.CAST_COLOR.cpy(), drawBoxes,lerpVal);
		}
		this.drawRemainingSlots(canvas, curSlots, xPosBar, yPosBar, barColor);
		
	}
	
	public void drawQueuedActions(GameCanvas canvas,int count,List<ActionNode> queuedActions){
		// draw queuedActions
		float actionSlot_x = this.getX(canvas);
		float actionSlot_y = this.getY(canvas, count);

		for (ActionNode a: queuedActions){
			// length relative 
			float centeredCast = this.getCenteredActionX(canvas, a.executePoint, a.action.cost);
			
			float x_pos = actionSlot_x + centeredCast;
			float y_pos = actionSlot_y;
			float y_icon = this.getFillY(canvas, count);
			
			if (a.isInterrupted && a.action.pattern != Pattern.MOVE){
				canvas.drawCenteredText(INTERRUPT_TEXT,x_pos,y_pos,Color.WHITE);
				
				float scale =this.getBarFillHeight(canvas)/cancel_token.getHeight();
				float width_icon = cancel_token.getWidth() * scale;
				float height_icon = cancel_token.getHeight() * scale;
				canvas.drawTexture(cancel_token, x_pos, y_icon, width_icon,height_icon, Color.WHITE);
			}
			else{
				String text = a.action.name;
				canvas.drawCenteredText(text,x_pos,y_pos,Color.WHITE);
				
				float scale = this.getBarFillHeight(canvas)/a.action.barIcon.getHeight();
				float width_icon = a.action.barIcon.getWidth() * scale;
				float height_icon = a.action.barIcon.getHeight() * scale;
				canvas.drawTexture(a.action.barIcon, x_pos, y_icon, width_icon,height_icon, Color.WHITE);
			}
		}
	}
	
	public void drawHighlightedOption(GameCanvas canvas){
		for (Option o : actionOptions){
			if (o.currentlyHovered){
				float x = o.xPosition*canvas.width;
				float y = o.yPosition*canvas.height;
				float width = o.width*canvas.width;
				float height = o.height*canvas.height;
				//canvas.drawBox(x, y, width, height, Color.GOLD.cpy().mul(1,1,1,0.9f));
			}
		}
	}
	
	
	// draw gauge style 
	public void draw(GameCanvas canvas,int count,Color barColor,Color fillColor, 
			float castPosition,boolean leftside,boolean isSelecting,List<ActionNode> selectingActions, 
			Action curSelectedAction, List<ActionNode> queuedActions,
			List<ActionNode> castActions, float lerpVal, List<Effect> effects){
		
		float xPosBar = getX(canvas);
		float yPosBar = getY(canvas,count);
			
		this.drawLeftEndpoint(canvas, xPosBar, yPosBar, leftside, barColor);
		this.drawBar(canvas, xPosBar, yPosBar, barColor);
		this.drawRightEndpoint(canvas, xPosBar, yPosBar, barColor);
		
		//draw fill
		this.drawWaitFill(canvas,xPosBar,count,barColor);
		this.drawCastFill(canvas, xPosBar, count, barColor,castPosition);
		
		tempActionOptions.clear();
		tempActions.clear();
		
		// draw action queuing
		this.drawSlots(canvas, castActions,queuedActions,
				selectingActions,curSelectedAction,count, barColor,isSelecting,true,lerpVal);
		
		// draw the fill on the cast bar
		this.drawCastFill(canvas, xPosBar, count, barColor,castPosition);
		
		// draw icon
		this.drawCenterIcon(canvas, xPosBar,yPosBar,barColor);
		
		this.drawSlots(canvas, castActions,queuedActions,
				selectingActions,curSelectedAction,count, barColor,isSelecting,false,lerpVal);
		
		if (!isSelecting){
			this.drawHighlightedOption(canvas);
		}
		
		actionOptions.clear();
		actionOptions.addAll(tempActionOptions);
		actions.clear();
		actions.addAll(tempActions);
		
		this.drawQueuedActions(canvas, count, selectingActions);
		this.drawQueuedActions(canvas,count,queuedActions);
		this.drawQueuedActions(canvas, count, castActions);
		
		if (effects!= null && !effects.isEmpty()){
			float x = xPosBar + this.getWidth(canvas) + 0.015f * canvas.getWidth();
			float y = yPosBar - 0.005f * canvas.getHeight();
			float width = RELATIVE_EFFECT_ICON_WIDTH * canvas.getWidth();
			float height = width;
			for (Effect e : effects){
				if (e.icon == null){
					continue;
				}
				TextureRegion textureRegion = new TextureRegion(e.icon);
				float angle = e.ratioGone() * CIRCLE_ANGLE;
				canvas.drawRadial(textureRegion, x, y, width, height, angle);
				x += 0.03f * canvas.getWidth();
			}
		}
	}
	
}
