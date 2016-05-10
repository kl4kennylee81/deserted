package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.GameEngine.GameState;

public class CharacterSelectController {
	private boolean isDone;
	private GameCanvas canvas;
	private AssetManager manager;
	private GameSaveStateController gameSaveStateController;
	private MouseOverController mouseOverController;
	private CharacterSelect characterSelect;
	public String nextLevelName;
	
	public CharacterSelectController(GameCanvas canvas, AssetManager manager, MouseOverController mouseOverController, GameSaveStateController gameSaveStateController){
		this.canvas = canvas;
		this.manager = manager;
		this.mouseOverController = mouseOverController;
		this.gameSaveStateController = gameSaveStateController;
		this.isDone = false;
		
		this.characterSelect = new CharacterSelect(gameSaveStateController.getAvailableCharactersData());
	}
	
	public void reset(){
		isDone = false;
		this.characterSelect = new CharacterSelect(gameSaveStateController.getAvailableCharactersData());
	}
	
	public void update(){
		mouseOverController.update(characterSelect.options, characterSelect);
		boolean mouseCondition = false;
		if (characterSelect.selectedIndex!=-1){
			Option curOption = characterSelect.options[characterSelect.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		if (InputController.pressedEnter() || mouseCondition){
			// fixup to get cur option string from the index
			String optionKey = characterSelect.getCurOption();
			handlePress(optionKey);
		}
		
	     else if (InputController.pressedRight() && !InputController.pressedLeft()){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	    	 int newSelection = characterSelect.getCurIndexOption() + 1;
	         int length = characterSelect.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
		     characterSelect.setOption(toSelect);
	     }  
	     else if (InputController.pressedLeft() && !InputController.pressedRight()){
			//Actions go from up down, so we need to flip
	    	 int newSelection = characterSelect.getCurIndexOption() - 1;
	        int length = characterSelect.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
					(Math.abs(newSelection) % length) ) 
					%length : (newSelection % 
							length);
	        characterSelect.setOption(toSelect);
		}
	}
	
	public void handlePress(String optionKey){
		switch (optionKey){
		case "Back":
			nextLevelName = "Level Select";
			isDone = true;
			break;
		case "Skill Tree":
			nextLevelName = "Skill Tree";
			isDone = true;
			break;
		case "Play":
			nextLevelName = "Start Level";
			isDone = true;
			break;
		}
		
	}
	
	public Integer getSelectedCharacterId(){
		if (characterSelect.selectedCharacterId == null){
			return 0;
		} else {
			return characterSelect.selectedCharacterId;
		}
	}
	
	public void draw(GameCanvas canvas){
		characterSelect.draw(canvas);
	}
	
	public boolean isDone(){
		return isDone;
	}
}
