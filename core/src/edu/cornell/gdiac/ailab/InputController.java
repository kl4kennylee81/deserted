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
	
	public static boolean leftMouseClickedLast = false;
	
	public static boolean leftMouseClicked = false;
	
	public static void update(){
		if (leftMouseClickedLast){
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
				leftMouseClicked = false;
			}
			else{
				leftMouseClicked = true;
				leftMouseClickedLast = false;
			}
		}
		else{
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
				leftMouseClickedLast = true;
				leftMouseClicked = false;
			}
			else{
				leftMouseClickedLast = false;
				leftMouseClicked = false;
			}
		}
	}
    
    public static boolean pressedEnter() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.ENTER);
    }
    
    public static GameCanvas getCanvas() {
    	return canvas;
    }
    
    public static boolean pressedESC(){
    	return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);    	
    }
    
    public static boolean pressedBack() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE);
    }
    
    public static boolean pressedP() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.P);
    }
    
    //Reset
    public static boolean pressedR() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.R);
    }
    
    public static boolean pressedUp() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.UP)||Gdx.input.isKeyJustPressed(Input.Keys.W);
    }
    
    public static boolean pressedDown() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.DOWN)||Gdx.input.isKeyJustPressed(Input.Keys.S);
    }
    
    public static boolean pressedLeft() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.LEFT)||Gdx.input.isKeyJustPressed(Input.Keys.A);
    }
    
    public static boolean pressedRight() {
    	return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)||Gdx.input.isKeyJustPressed(Input.Keys.D);
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
		return leftMouseClicked;
	}
	
	public static boolean mouseJustMoved(){
		//TODO: Make this better
		return Gdx.input.getDeltaY() > 0f || Gdx.input.getDeltaX() > 0f;
	}

	public static boolean pressedSpace() {
		return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
	}
}
