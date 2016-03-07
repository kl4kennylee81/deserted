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
	
	/**
	 * Constructs a PlayerController with keyboard control.
	 */
    public PlayerController() {
	}
    
    public boolean pressedEnter() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }
    
    public boolean pressedA() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.A);
    }
    
    public boolean pressedS() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.S);
    }
    
    public boolean pressedD() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.D);
    }
    
    public boolean pressedUp() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.UP);
    }
    
    public boolean pressedDown() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.DOWN);
    }
    
    public boolean pressedLeft() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.LEFT);
    }
    
    public boolean pressedRight() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT);
    }
}
