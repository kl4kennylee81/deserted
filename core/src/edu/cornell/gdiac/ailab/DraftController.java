package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import networkUtils.BackMessage;
import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.DraftMessage;
import networkUtils.Message;

public class DraftController {
	
	private static String PLAYER1_TEXTURE = "models/portraits/owner_dara.png";
	private static String PLAYER2_TEXTURE = "models/portraits/owner_arash.png";
	
	private boolean isDone;
	private GameCanvas canvas;
	private AssetManager manager;
	private Level level;
	private MouseOverController mouseOverController;
	private DraftScreen draftScreen;
	private boolean isFirst;
	private String from;
	private String to;
	private DraftState draftState;
	private Connection connection;
	public String nextLevelName;
	public int playerNum = 1;
	
	private Texture draftScreenHighlight;
	
	private int charIdToWrite;
	
	boolean broken;
	
	private static enum DraftState {
		PICKING,
		WAITING,
		WRITING,
	}
	
	public DraftController(GameCanvas canvas, MouseOverController mouseOverController){
		this.canvas = canvas;
		this.mouseOverController = mouseOverController;
		this.isDone = false;
		this.broken = false;
	}
	
	public void setAssets(AssetManager manager) {
		if (this.manager != null) {
			return;
		}
		this.manager = manager;
		
		manager.load(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
		manager.load(PLAYER1_TEXTURE, Texture.class);
		manager.load(PLAYER2_TEXTURE, Texture.class);
		manager.finishLoading();
		
		draftScreenHighlight = manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
	}
	
	public void setLevel(Level level) {
		this.level = level;
		this.draftScreen = new DraftScreen(level.getCharacters());
		
		draftScreen.setHighlight(draftScreenHighlight);
	}
	
	public Level getLevel(){ 
		return level;
	}
	
	public void reset(Connection connection, boolean isFirst, String from, String to) {
		this.connection = connection;
		this.isFirst = isFirst;
		this.from = from;
		this.to = to;
		this.draftState = isFirst ? DraftState.PICKING : DraftState.WAITING;
		
		isDone = false;
		broken = false;
		this.draftScreen = new DraftScreen(level.getCharacters());
		draftScreen.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
	}
	
	public void update(){
		if (draftState !=  DraftState.WRITING && draftScreen.doneDrafting()) {
			handleNextLevel("pvp");
			return;
		}
		switch (draftState) {
		case PICKING:
			updatePicking();
			break;
		case WAITING:
			updateWaiting();
			break;
		case WRITING:
			updateWriting();
			break;
		}
	}
	
	public void updatePicking(){
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
	
	public void updateWriting(){
		Message msg = new DraftMessage(charIdToWrite);
		Integer bytes_written = null;
		try {
			bytes_written = connection.write(msg);
		} catch (Exception e) {
			//hi
		}
		if (bytes_written != null) {
			draftState = DraftState.WAITING;
		}
	}
	
	public void updateWaiting(){
		String s = null;
		try {
			s = connection.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (s != null) {
			Message m = Message.jsonToMsg(s);
			if (!(m instanceof DraftMessage)) {
				broken = true;
				return;
			}
			DraftMessage dm = (DraftMessage) m;
			int charId = dm.getId();
			draftScreen.setPlayerCharacter(playerNum, charId);
			draftScreen.setSelectedToFirstAvailable(charId);
			playerNum = playerNum == 1 ? 2 : 1;
			draftState = DraftState.PICKING;
		}
	}
	
	public void handlePress(String optionKey){
		switch (optionKey){
		case "Back":
			Message bm = new BackMessage();
			try {
				connection.blockingWrite(bm);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			handleNextLevel("Main Menu");
			break;
		case "Play":
			handleNextLevel("pvp");
			break;
		case "Select":
			if(draftScreen.selectCurrentCharacter(playerNum)){
				playerNum = playerNum == 1 ? 2 : 1;
				charIdToWrite = draftScreen.selectedCharacterId;
				draftState = DraftState.WRITING;
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

		Texture t1 = manager.get(PLAYER1_TEXTURE, Texture.class);
		Texture t2 = manager.get(PLAYER2_TEXTURE, Texture.class);
		Character parent1 = new Character(2000, t1, t1, c1.animation, "Ishaan", 10, 10, Color.BLUE, 0, 0, 0,new Action[]{},0);
		Character parent2 = new Character(2001, t2, t2, c2.animation, "Ishaan", 10, 10, Color.YELLOW, 0, 0, 0,new Action[]{},0);

		int health1 = 0;
		for(int i = 0; i < draftScreen.player1Characters.length; i++){
			BossCharacter c = (BossCharacter) draftScreen.getCharacter(draftScreen.player1Characters[i]);
			c.xPosition = 0;
			c.yPosition = i * 3;
			c.setParent(parent1);
			c.setLeftSide(true);
			list.add(c);
			health1 += c.health;
			c.isNetworkingOpponent = !isFirst;
			c.isAI = false;
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
			c.isNetworkingOpponent = isFirst;
			c.isAI = false;
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
		draftScreen.drawWaitMessage(canvas, draftState == DraftState.WAITING);
	}
	
	public boolean isDone(){
		return isDone;
	}
}
