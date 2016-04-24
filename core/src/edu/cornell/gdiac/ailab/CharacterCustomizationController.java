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
	
	public CharacterCustomizationController(GameSaveState gameSaveState, AssetManager manager, MouseOverController mouseOverController){
		this.characterCustomization = new CharacterCustomization(gameSaveState);
		this.manager = manager;
		if (manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE) && characterCustomization.optionHighlight == null){
			characterCustomization.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
		}
		this.mouseOverController = mouseOverController;
		isDone = false;
	}
	
	public void update(){
		mouseOverController.update(characterCustomization.options, characterCustomization);
		if (InputController.pressedLeftMouse()){
			// fixup to get cur option string from the index
			String optionKey = characterCustomization.getCurOption();
			handlePress(optionKey);
		}
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
		try{
			int actionId = Integer.parseInt(optionKey);
			characterCustomization.setAction(actionId);
		} catch (NumberFormatException e) {
			// not an integer!
		}
	}
	
	public boolean isDone(){
		return isDone;
	}
	
	public void draw(GameCanvas canvas){
		characterCustomization.draw(canvas);
	}
}
