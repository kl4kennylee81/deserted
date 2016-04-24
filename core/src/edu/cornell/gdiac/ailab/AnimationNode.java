package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.ailab.Animation.Segment;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

public class AnimationNode {
	/** Animation to draw */
	Animation animation;
	/** Current frame information */
	int curSegment;
	int curFrameIndex;
	int curFrameDuration;
	/** x and y positions for actions */
	int xPos;
	int yPos;
	
	public enum CharacterState {
		IDLE (0), ACTIVE (1), CAST (2), EXECUTE (3), HURT (4);
		
		public final int id;
		CharacterState(int id) { 
			this.id = id; 
		}
	}
	
	public AnimationNode (Animation animation){
		this.animation = animation;
		xPos = yPos = 0;
		curSegment = curFrameIndex = curFrameDuration = 0;
	}
	
	public AnimationNode (Animation animation, int xPos, int yPos){
		this.animation = animation;
		this.xPos = xPos;
		this.yPos = yPos;
		curSegment = curFrameIndex = curFrameDuration = 0;
	}
	
	/**
	 * Returns the next FilmStrip of the animation
	 */
	public FilmStrip getTexture(boolean paused){
		return getTextureHelper(0,paused);
	}
	
	/**
	 * Returns the next FilmStrip with the given CharacterState
	 */
	public FilmStrip getTexture(CharacterState charState,InGameState gameState){
		// if the game state is selection freeze the frame on the current frame
		if (gameState == InGameState.PAUSED){
			return animation.filmStrip;
		}
		
//		if (gameState == InGameState.ATTACK && charState != CharacterState.EXECUTE){
//			return animation.filmStrip;
//		}
		
		if (charState.id != curSegment){
			curSegment = charState.id;
			curFrameIndex = 0;
			curFrameDuration = 0;
		}
		// executes the only action that doesn't loop around
		else if (charState.id == curSegment && charState != CharacterState.EXECUTE
				&& curFrameIndex>=animation.segments.get(curSegment).length){
			curFrameIndex = 0;
			curFrameDuration = 0;			
		}
		return getTextureHelper(charState.id, false);
	}
	
	/**
	 * Returns the next FilmStrip with the given segment id
	 */
	public FilmStrip getTextureHelper(int segmentNum, boolean paused){
		Segment s = animation.segments.get(segmentNum);
		if (s == null || s.frameLengths == null){
			return null;
		}
		if (paused){
			return animation.filmStrip;
		}
		curFrameDuration++;
		if(curFrameDuration >= s.frameLengths[curFrameIndex]){
			curFrameDuration = 0;
			curFrameIndex++;
		}
		if (curFrameIndex >= s.length){
			curFrameIndex = 0;
			return null;
		}
		animation.filmStrip.setFrame(s.startingIndex+curFrameIndex);
		return animation.filmStrip;
		
	}
}
