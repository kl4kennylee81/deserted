package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

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
		
		this.characterSelect = new CharacterSelect(gameSaveStateController.getAvailableCharactersData(),gameSaveStateController.getSelectedCharactersId());
		
		manager.load(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
		manager.finishLoading();
		
		characterSelect.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
	}
	
	public void reset(){
		isDone = false;
		this.characterSelect = new CharacterSelect(gameSaveStateController.getAvailableCharactersData(),gameSaveStateController.getSelectedCharactersId());
		characterSelect.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
	}
	
	public void update(){
		mouseOverController.update(characterSelect.options, characterSelect, true);
		boolean mouseCondition = false;
		if (characterSelect.selectedIndex!=-1){
			Option curOption = characterSelect.options[characterSelect.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		String optionKey = characterSelect.getCurOption();
		if (InputController.pressedEnter() || mouseCondition){
			// fixup to get cur option string from the index
			handlePress(optionKey);
		}
	}
	
	public void handlePress(String optionKey){
		switch (optionKey){
		case "Back":
			handleNextLevel("Level Select");
			break;
		case "Skill Tree":
			handleNextLevel("Skill Tree");
			break;
		case "Play":
			handleNextLevel("Start Level");
			break;
		case "Select":
			characterSelect.selectCurrentCharacter();
		default:
			if (optionKey.contains(characterSelect.CHARACTER_ID_STRING)){
				String charIdString = optionKey.substring(characterSelect.CHARACTER_ID_STRING.length());
				int charId = Integer.parseInt(charIdString);
				characterSelect.setCharacter(charId);
				return;
			}
		}
	}
	
	public void handleNextLevel(String levelName){
		if (characterSelect.canLeave()){
			gameSaveStateController.setSelectedCharactersId(characterSelect.charactersInPlay);
			gameSaveStateController.saveGameSaveState();
			nextLevelName = levelName;
			isDone = true;
		} else {
			
		}
	}
	
	public Integer getSelectedCharacterId(){
		return characterSelect.selectedCharacterId;
	}
	
	public void draw(GameCanvas canvas){
		characterSelect.draw(canvas);
	}
	
	public boolean isDone(){
		return isDone;
	}
}
