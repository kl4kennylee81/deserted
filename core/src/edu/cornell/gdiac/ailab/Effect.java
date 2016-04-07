package edu.cornell.gdiac.ailab;

/** How to create effects:
 * 1: Create a new value in the Type enum for the effect
 * 2: Create a new case in processEffect() in EffectController
 * 3: Modify actions.yml to add the effect to an action
 * 
 * Note: effects can currently be tweaked in actions.yml by changing
 * the magnitude and framesLeft paramters. 
 */
public class Effect {
	/** "size" of the effect. */
	int magnitude;
	
	/** Determines what the effect will actually do */
	Type type;
	
	/** Flag so effect gets applied when it is added to a character */
	boolean isNew;
	
	/** Flag so effect gets removed when it is finished */
	boolean isDone;
	
	/** Number of rounds left for this effect */
	float roundsLeft;
	
	/** Cast bar position when it was first applied */
	float castPosition;
	
	/** Name of what happened */
	String name;
	
	public static enum Type {
		BROKEN,
		SPEED,
		REGULAR
	}
	
	public static float FRAMES_PER_SECOND = 60;
	
	public Effect(float d, Type t, int mag, String n){
		roundsLeft = d;
		type = t;
		magnitude = mag;
		name = n;
		isNew = true;
		isDone = false;
	}
	
	/** Always use this when adding an effect to a character from an actionNode */
	public Effect clone(){
		return new Effect(roundsLeft, type, magnitude, name);
	}
	
	public static float getSecondtoFrames(float sec){
		return sec*FRAMES_PER_SECOND;
	}
	
	public String toString(){
		return name;
	}
	
}
