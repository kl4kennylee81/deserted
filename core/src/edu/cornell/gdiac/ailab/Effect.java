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
	/** Number of frames left for this effect */
	int framesLeft;
	
	/** "size" of the effect. For example, a effect with magnitude
	 * 0.5 will cut the affected character's speed in half. */
	float magnitude;
	
	/** Determines what the effect will actually do */
	Type type;
	
	/** Flag so effect gets applied when it is added to a character */
	boolean isNew;
	
	/** Flag so effect gets removed when it is finished */
	boolean isDone;
	
	public static enum Type {
		SLOW,
		REGULAR
	}
	
	public Effect(int d, Type t, float mag){
		framesLeft = d;
		type = t;
		magnitude = mag;
		isNew = true;
		isDone = false;
	}
	
	/** Always use this when adding an effect to a character from an actionNode */
	public Effect clone(){
		return new Effect(framesLeft, type, magnitude);
	}
	
}
