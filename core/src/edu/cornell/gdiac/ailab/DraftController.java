package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class DraftController {
	private boolean isDone;
	private GameCanvas canvas;
	private AssetManager manager;
	private Level level;
	private MouseOverController mouseOverController;
	private DraftScreen draftScreen;
	private boolean isFirst;
	public String nextLevelName;
	public int playerNum = 1;
	
	public DraftController(GameCanvas canvas, MouseOverController mouseOverController){
		this.canvas = canvas;
		this.mouseOverController = mouseOverController;
		this.isDone = false;
	}
	
	public void setLevel(AssetManager manager, Level level) {
		this.level = level;
		this.manager = manager;
		this.draftScreen = new DraftScreen(level.getCharacters());
		
		manager.load(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
		manager.finishLoading();
		
		draftScreen.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
	}
	
	public void setIsFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}
	
	public void reset(){
		isDone = false;
		this.draftScreen = new DraftScreen(level.getCharacters());
		draftScreen.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
	}
	
	public void update(){
		mouseOverController.update(draftScreen.options, draftScreen, true);
		boolean mouseCondition = false;
		if (draftScreen.selectedIndex!=-1){
			Option curOption = draftScreen.options[draftScreen.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		String optionKey = draftScreen.getCurOption();
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
		case "Play":
			handleNextLevel("pvp");
			break;
		case "Select":
			if(draftScreen.selectCurrentCharacter(playerNum)){
				playerNum = playerNum == 1 ? 2 : 1;
			}
			break;
		default:
			if (optionKey.contains(draftScreen.CHARACTER_ID_STRING)){
				String charIdString = optionKey.substring(draftScreen.CHARACTER_ID_STRING.length());
				int charId = Integer.parseInt(charIdString);
				draftScreen.setCharacter(charId);
				return;
			}
		}
	}
	
	public void handleNextLevel(String levelName){
		if (draftScreen.canLeave(1) && draftScreen.canLeave(2)){
			nextLevelName = levelName;
			isDone = true;
		} 
	}
	
	public Characters getSelectedChars(){
		Characters list = new Characters();
		BossCharacter c1 = (BossCharacter) draftScreen.getCharacter(draftScreen.player1Characters[0]);
		BossCharacter c2 = (BossCharacter) draftScreen.getCharacter(draftScreen.player2Characters[0]);
		Character parent1 = new Character(2000, c1.texture, c1.icon, c1.animation, "Ishaan", 10, 10, Color.BLUE, 0, 0, new Action[]{},0);
		Character parent2 = new Character(2001, c2.texture, c2.icon, c2.animation, "Ishaan", 10, 10, Color.YELLOW, 0, 0, new Action[]{},0);
		int health1 = 0;
		for(int i = 0; i < draftScreen.player1Characters.length; i++){
			BossCharacter c = (BossCharacter) draftScreen.getCharacter(draftScreen.player1Characters[i]);
			c.xPosition = 0;
			c.yPosition = i * 3;
			c.setParent(parent1);
			c.setLeftSide(true);
			list.add(c);
			health1 += c.health;
		}
		parent1.setHealth(health1);
		parent1.setMaxHealth(health1);
		int health2 = 0;
		for(int i = 0; i < draftScreen.player2Characters.length; i++){
			BossCharacter c = (BossCharacter) draftScreen.getCharacter(draftScreen.player2Characters[i]);
			c.xPosition = 5;
			c.yPosition = i * 3;
			c.setParent(parent2);
			c.setLeftSide(false);
			list.add(c);
			health2 += c.health;
		}
		parent2.setHealth(health2);
		parent2.setMaxHealth(health2);
		return list;
	}
	
	public Integer getSelectedCharacterId(){
		return draftScreen.selectedCharacterId;
	}
	
	public void draw(GameCanvas canvas){
		draftScreen.draw(canvas);
	}
	
	public boolean isDone(){
		return isDone;
	}
}
