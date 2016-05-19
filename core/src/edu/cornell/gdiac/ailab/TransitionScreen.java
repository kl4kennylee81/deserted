package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.ailab.GameEngine.GameState;

public class TransitionScreen extends HighlightScreen{
	
	public static enum TransitionState {
		NOTHING,
		FADEOUT,
		FADEIN
	}
	
	private float opacityStart;
	
	private float opacityEnd;
	
	private float transitionFrames;
	
	private float transitionFramesCount;
	
	private GameState nextState;
	
	private String levelName;
	
	private boolean needsSelect;
	
	private TransitionState transitionState;
	
	// 60 fps so fade in 1 second
	// will let this be tuneable
	private static final float DIM_END = 0.9f;
	
	private static final float FPS = 60f;
	
	public TransitionScreen(){
		super();
		this.opacityStart = 1f;
		this.opacityEnd = 1f;;
		this.transitionFrames = 0;
		this.transitionFramesCount = 0;
		this.transitionState = TransitionState.NOTHING;
		this.nextState = null;
		this.levelName = "";
		this.needsSelect = false;
	}
	
	public TransitionState getTransitionState(){
		return this.transitionState;
	}
	
	@Override
	public float getOpacity(){
		float diff = (this.opacityEnd - this.opacityStart);
		float transitionRatio = (this.transitionFramesCount/this.transitionFrames);
		float curOpacity = diff * transitionRatio + this.opacityStart;
		return curOpacity;
	}
	
	public boolean getNeedsSelect(){
		return this.needsSelect;
	}
	
	public float getTransitionFrames(){
		return this.transitionFrames;
	}
	
	public void setTime(float seconds){
		this.transitionFrames = seconds*FPS;
		this.transitionFramesCount = 0;
	}
	
	public float getFadeRate(){
		return 1/this.getTransitionFrames();
	}
	
	public boolean isDone(){
		return this.transitionFramesCount > this.transitionFrames;
	}
	
	public boolean isActive(){
		return this.transitionState != TransitionState.NOTHING;
	}
	
	
	public void updateScreen(){
		this.transitionFramesCount++;
		if (this.isDone() && this.transitionState == TransitionState.FADEOUT){
			// hardcode fade in is always 1 second for now
			this.setFadeIn(this.getTransitionFrames()/FPS);
		}
		else if (this.isDone() && this.transitionState == TransitionState.FADEIN){
			this.reset();
		}
	}
	
	
	// only call reset after an appropriate fade out and fade in is done then reset
	public void reset(){
		this.opacityStart = 1f;
		this.opacityEnd = 1f;;
		this.transitionFrames = 0;
		this.transitionFramesCount = 0;
		this.transitionState = TransitionState.NOTHING;
		this.nextState = null;
		this.levelName = "";
		this.needsSelect = false;
		super.noScreen();
	}
	
	public void setFadeOut(float seconds){
		super.setJustScreen();
		this.setTime(seconds);
		this.opacityStart = 0f;
		this.opacityEnd = DIM_END;
		this.transitionState = TransitionState.FADEOUT;
	}
	
	public GameState getNextState(){
		return this.nextState;
	}
	
	public String getNextLevel(){
		return this.levelName;
	}
	
	public void setFadeOut(float seconds,GameState nextState){
		this.nextState = nextState;
		this.setFadeOut(seconds);
	}
	
	public void setFadeOut(float seconds,String levelName,boolean needsSelect){
		this.levelName = levelName;
		this.needsSelect = needsSelect;
		this.setFadeOut(seconds);
	}
	
	public void setFadeIn(float seconds){
		this.setTime(seconds);
		this.opacityStart = DIM_END;
		this.opacityEnd = 0f;
		this.transitionState = TransitionState.FADEIN;
	}
}
