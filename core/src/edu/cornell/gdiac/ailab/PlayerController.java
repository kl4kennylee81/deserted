/*
 * PlayerController.java
 * 
 * This class provides the interface of the human player.  It is essentially equivalent
 * the InputController from lab one.  The difference is that we use a single integer
 * to return the input result, instead of attaching properties to the InputController. 
 * 
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
 package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.*;

import edu.cornell.gdiac.util.*;

/**
 * An input controller for the human player
 */
public class PlayerController implements InputController {
	/** Whether to enable keyboard control (as opposed to X-Box) */
	private boolean keyboard;
	
	private boolean pressedEnter;
	private boolean lastPressedEnter;
	private boolean pressedA;
	private boolean lastPressedA;
	private boolean pressedS;
	private boolean lastPressedS;
	private boolean pressedUp;
	private boolean lastPressedUp;
	private boolean pressedDown;
	private boolean lastPressedDown;
	private boolean pressedLeft;
	private boolean lastPressedLeft;
	private boolean pressedRight;
	private boolean lastPressedRight;
	
	/** The XBox Controller hooked to this machine */
	private XBox360Controller xbox;
	
	/**
	 * Constructs a PlayerController with keyboard control.
	 * 
	 * If an XBox-controller is hooked up, it will defer to that
	 * controller instead.
	 */
    public PlayerController() {
    	keyboard = true;
    	xbox = null;
		// If we have a game-pad for id, then use it.
		if (Controllers.getControllers().size > 0) {
			Controller controller = Controllers.getControllers().get(0);
			if (controller.getName().toLowerCase().contains("xbox") &&
				controller.getName().contains("360")) {
				xbox = new XBox360Controller(0);
				keyboard = false;
			}
		}
	}

	/**
	 * Return the action of this ship (but do not process)
	 * 
	 * The value returned must be some bitmasked combination of the static ints 
	 * in the implemented interface.  For example, if the ship moves left and fires, 
	 * it returns CONTROL_MOVE_LEFT | CONTROL_FIRE
	 *
	 * @return the action of this ship
	 */
    public void getAction() {
		int enter = Input.Keys.ENTER;
		int a = Input.Keys.A;
		int s = Input.Keys.S;
		int up = Input.Keys.UP;
		int down = Input.Keys.DOWN;
		int left = Input.Keys.LEFT;
		int right = Input.Keys.RIGHT;
		
		lastPressedEnter = pressedEnter;
		pressedEnter = Gdx.input.isKeyPressed(enter);
		lastPressedA = pressedA;
		pressedA = Gdx.input.isKeyPressed(a);
		lastPressedS = pressedS;
		pressedS = Gdx.input.isKeyPressed(s);
		lastPressedUp = pressedUp;
		pressedUp = Gdx.input.isKeyPressed(up);
		lastPressedDown = pressedDown;
		pressedDown = Gdx.input.isKeyPressed(down);
		lastPressedLeft = pressedLeft;
		pressedLeft = Gdx.input.isKeyPressed(left);
		lastPressedRight = pressedRight;
		pressedRight = Gdx.input.isKeyPressed(right);
	}
    
    public boolean pressedEnter() {
    	return pressedEnter && !lastPressedEnter;
    }
    
    public boolean pressedA() {
    	return pressedA && !lastPressedA;
    }
    
    public boolean pressedS() {
    	return pressedS && !lastPressedS;
    }
    
    public boolean pressedUp() {
    	return pressedUp && !lastPressedUp;
    }
    
    public boolean pressedDown() {
    	return pressedDown && !lastPressedDown;
    }
    
    public boolean pressedLeft() {
    	return pressedLeft && !lastPressedLeft;
    }
    
    public boolean pressedRight() {
    	return pressedRight && !lastPressedRight;
    }
}
