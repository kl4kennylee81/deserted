package edu.cornell.gdiac.ailab;

public class CharActionBar {

	public static float CHAR_VELOCITY_SCREEN_RATIO = 0.00125f;
	
	public static float MAX_BAR_SCREEN_RATIO = 0.7f;
	
	public static float CHAR_VELOCITY;
	
	public static float MAX_BAR_LENGTH;
	
	// length of the cast bar in terms of the max bar length
	// actual length = MAX_BAR_LENGTH * length
	float length;
	
	// the cast point of the cast bar
	float castPoint;
	
	// number of action slots
	int numSlots;
	
	// pass in seconds in waiting, seconds in casting, and number of slots
	// generates a cast bar with a length and cast point
	CharActionBar(int numSlots,float waitTime,float castTime){
		
	}
	
}
