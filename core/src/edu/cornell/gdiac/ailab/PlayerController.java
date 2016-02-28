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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.*;

import edu.cornell.gdiac.util.*;

/**
 * An input controller for the human player
 */
public class PlayerController implements InputController {
	/** Whether to enable keyboard control (as opposed to X-Box) */
	private boolean keyboard;
	
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
    public int getAction() {
		int code = CONTROL_NO_ACTION;
		
		if (keyboard) {
			if (Gdx.input.isKeyPressed(Keys.UP))    code |= CONTROL_MOVE_UP;
			if (Gdx.input.isKeyPressed(Keys.LEFT))  code |= CONTROL_MOVE_LEFT;
			if (Gdx.input.isKeyPressed(Keys.DOWN))  code |= CONTROL_MOVE_DOWN;
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) code |= CONTROL_MOVE_RIGHT;
			if (Gdx.input.isKeyPressed(Keys.SPACE)) code |= CONTROL_FIRE;
		} else {
			double ANALOG_THRESH  = 0.3;
			double TRIGGER_THRESH = -0.75;
			if (xbox.getLeftY() < -ANALOG_THRESH)	code |= CONTROL_MOVE_UP;
			if (xbox.getLeftX() < -ANALOG_THRESH)  	code |= CONTROL_MOVE_LEFT;
			if (xbox.getLeftY() > ANALOG_THRESH)   code |= CONTROL_MOVE_DOWN;
			if (xbox.getLeftX() > ANALOG_THRESH) 	code |= CONTROL_MOVE_RIGHT;
			if (xbox.getRightTrigger() > TRIGGER_THRESH) code |= CONTROL_FIRE;
		}

		// Prevent diagonal movement.
        if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_LEFT) != 0) {
            code ^= CONTROL_MOVE_UP;
        }

		if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
			code ^= CONTROL_MOVE_RIGHT;
        }

		if ((code & CONTROL_MOVE_DOWN) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
			code ^= CONTROL_MOVE_DOWN;
        }

		if ((code & CONTROL_MOVE_DOWN) != 0 && (code & CONTROL_MOVE_LEFT) != 0) {
			code ^= CONTROL_MOVE_LEFT;
        }

		// Cancel out conflicting movements.
		if ((code & CONTROL_MOVE_LEFT) != 0 && (code & CONTROL_MOVE_RIGHT) != 0) {
			code ^= (CONTROL_MOVE_LEFT | CONTROL_MOVE_RIGHT);
        }

		if ((code & CONTROL_MOVE_UP) != 0 && (code & CONTROL_MOVE_DOWN) != 0) {
			code ^= (CONTROL_MOVE_UP | CONTROL_MOVE_DOWN);
        }

		return code;
	}
}
