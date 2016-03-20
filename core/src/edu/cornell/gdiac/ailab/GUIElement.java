package edu.cornell.gdiac.ailab;

public interface GUIElement {
	
	/** Returns true if this GUIElement contains the on-screen coordinates x and y, false otherwise*/
	public boolean contains(float x, float y, GameCanvas canvas, GridBoard board);
}
