/*
 * InputController.java
 * 
 * This class provides a uniform interface for the player and the AI.  The player
 * controls with an input device, while and AI player controls with AI algorithms.
 * This is a very standard way to set up AI control in a game that can have either
 * AI or human players.
 * 
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

/**
 * Interface for either player or AI control
 */
public interface InputController {

	/**
	 * Returns true if button is pressed, false otherwise
	 */
	public boolean pressedEnter();
	public boolean pressedA();
	public boolean pressedS();
	public boolean pressedD();
	public boolean pressedUp();
	public boolean pressedDown();
	public boolean pressedLeft();
	public boolean pressedRight();
}

