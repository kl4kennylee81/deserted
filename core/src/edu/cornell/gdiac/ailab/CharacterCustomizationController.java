package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;

import edu.cornell.gdiac.ailab.GameSaveState.ActionUpgrade;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CharacterCustomizationController {
	CharacterCustomization characterCustomization;
	MouseOverController mouseOverController;
	private AssetManager manager;
	boolean isDone;
	public String backLevelName;
	
	public CharacterCustomizationController(GameSaveState gameSaveState, AssetManager manager, MouseOverController mouseOverController, String backLevelName){
		this.characterCustomization = new CharacterCustomization(gameSaveState);
		this.manager = manager;
		if (manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE) && characterCustomization.optionHighlight == null){
			characterCustomization.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
		}
		this.mouseOverController = mouseOverController;
		this.backLevelName = backLevelName;
		isDone = false;
	}
	
	public void update(){
		mouseOverController.update(characterCustomization.options, characterCustomization, true);
		String optionKey = characterCustomization.getCurOption();
		if (InputController.pressedLeftMouse()){
			// fixup to get cur option string from the index
			handlePress(optionKey);
		} 
		if (InputController.leftMouseClickedLast){
			handleCharacterPress(optionKey);
		} 
//		else {
//			if (characterCustomization.pressingCharacter){
//				characterCustomization.dropCharacter();
//			}
//		}
	}
	
	
	public void setCharacter(int charId){
		characterCustomization.setCharacter(charId);
	}
	
	public void handlePress(String optionKey){
		if (optionKey.equals("Back")){
			isDone = true;
			return;
		}
		if (optionKey.equals("Reset")){
			characterCustomization.resetSP();
			return;
		}
		if (optionKey.equals("Select")){
			characterCustomization.selectAction();
			return;
		}
		try{
			int actionId = Integer.parseInt(optionKey);
			characterCustomization.setAction(actionId);
		} catch (NumberFormatException e) {
			// not an integer!
		}
	}
	
	public void handleCharacterPress(String optionKey){
		if (optionKey.contains(characterCustomization.CHARACTER_ID_STRING)){
			String charIdString = optionKey.substring(characterCustomization.CHARACTER_ID_STRING.length());
			int charId = Integer.parseInt(charIdString);
			characterCustomization.setCharacter(charId);
			//characterCustomization.pressingCharacter = true;
			return;
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
	public void draw(GameCanvas canvas){
		characterCustomization.draw(canvas);
	}
}
