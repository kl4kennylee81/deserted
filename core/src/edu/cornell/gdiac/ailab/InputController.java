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

/**
 * An input controller for the human player
 */
public class InputController {
	
	public static GameCanvas canvas;
    
    public static boolean pressedEnter() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }
    
    public static GameCanvas getCanvas() {
    	return canvas;
    }
    
    public static boolean pressedESC() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }
    
    public static boolean pressedBack() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE);
    }
    
    //Easy mode
    public static boolean pressedE() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.E);
    }
    
    //Medium mode
    public static boolean pressedM() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.M);
    }
    
    //Hard mode
    public static boolean pressedH() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.H);
    }
    
    public static boolean pressedT() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.T);
    }
    
    public static boolean pressedQ() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.Q);
    }
    
    //PVP mode
    public static boolean pressedP() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.P);
    }
    
    public static boolean pressedW() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.W);
    }
    
    public static boolean pressedA() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.A);
    }
    
    public static boolean pressedS() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.S);
    }
    
    public static boolean pressedD() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.D);
    }
    
    //Reset
    public static boolean pressedR() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.R);
    }
    
    public static boolean pressedUp() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.UP);
    }
    
    public static boolean pressedDown() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.DOWN);
    }
    
    public static boolean pressedLeft() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.LEFT);
    }
    
    public static boolean pressedRight() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT);
    }

	public static void setCanvas(GameCanvas canvas) {
		InputController.canvas = canvas;
		
	}

	public static float getMouseX() {
		return Gdx.input.getX();
	}

	public static float getMouseY() {
		return canvas.getHeight() - Gdx.input.getY();
	}
	
	public static boolean pressedLeftMouse(){
		return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
	}
	
	public static boolean mouseJustMoved(){
		//TODO: Make this better
		return Gdx.input.getDeltaY() > 0f || Gdx.input.getDeltaX() > 0f;
	}

	public static boolean pressedSpace() {
		return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
	}
}
