package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;

import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.Message;
import networkUtils.UsernameMessage;

public class NetworkingController {
	
	public enum NetworkingState {
		SET_NAME,
		GAMEROOM,
		CHARACTER_SELECTION,
		PLAYING,
	}
	
	private final String PVP_LEVEL_NAME = "pvp";
	private final String SET_NAME_BACKGROUND = "images/mainmenu/backgroundLogo.png";
	private final String CHARACTER_SELECTION_BACKGROUND = "images/mainmenu/backgroundLogo.png";
	private final String PLAYING_BACKGROUND = "backgrounds/inTheSkybg.png";
	
	private NetworkingState networkingState;
	private NetworkingMenu networkingMenu;
	private NetworkingGameplayController gameplayController;
	private DraftController draftController;
	private Level level;
	
	private Connection connection;
	
	private boolean isFirst;
	private String from;
	private String to;
	
	boolean isDone;
	
  GameEngine ge;
	
	public NetworkingController(NetworkingGameplayController ngc, DraftController dc, GameEngine ge) {
		networkingMenu = new NetworkingMenu();
		isDone = false;
		gameplayController = ngc;
		draftController = dc;
		this.ge = ge;
	}
	
	public NetworkingState getNetworkingState(){
		return this.networkingState;
	}
	
	public void update() {
		switch (networkingState) {
		case SET_NAME:
			networkingMenu.update();
			if (networkingMenu.goBack()) {
				isDone = true;
			}
			if (networkingMenu.goNext()) {
				Message msg = new UsernameMessage(networkingMenu.username);
				Integer bytes_written = null;
				try {
					bytes_written = connection.write(msg);
				} catch (Exception e) {
					//hi
				}
				if (bytes_written != null) {
					setState(NetworkingState.GAMEROOM);
				}
			}
			break;
		case GAMEROOM:
			//Check connection message for game confirmation
			String s = null;
			try {
				s = connection.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (s != null) {
				Message m = Message.jsonToMsg(s);
				if (!(m instanceof ChallengeMessage)) {
					isDone = true;
					return;
				}
				ChallengeMessage cm = (ChallengeMessage) m;
				if (cm.getSuccess() == true){
					isFirst = cm.getIsFirst();
					from = cm.getFrom();
					to = cm.getTo();
					setupDraftController();
					setState(NetworkingState.CHARACTER_SELECTION);
				} else {
					ChallengeMessage server_cm_request = new ChallengeMessage();
					try {
						this.connection.write(server_cm_request);
					} catch (Exception e){
						System.out.println("problem writing out the command message");
					}
				}
			}
			break;
		case CHARACTER_SELECTION:
			draftController.update();
			if (draftController.broken){
				isDone = true;
				return;
			}
			if (draftController.isDone()){
				//get game info and start game
				Level level = draftController.getLevel();
				level.setCharacters(draftController.getSelectedChars());
				gameplayController.resetGame(level);
				gameplayController.setupGame(isFirst, from, to);
				setState(NetworkingState.PLAYING);
			}
			break;
		case PLAYING:
			gameplayController.update();
			if (gameplayController.broken){
				isDone = true;
				return;
			}
			if (gameplayController.isDone()) {
				if (gameplayController.playAgain) {
					isFirst = !isFirst;
					setupDraftController();
					setState(NetworkingState.CHARACTER_SELECTION);
				} else {
					isDone = true;
				}
			}
			break;
		}
	}
	
	public void draw(GameCanvas canvas) {
		switch (networkingState) {
		case SET_NAME:
			canvas.end();
			networkingMenu.draw();
			break;
		case GAMEROOM:
			canvas.drawCenteredText("Waiting for other player to connect", 
					canvas.width/2, canvas.height/2, Color.WHITE);
			canvas.end();
			break;
		case CHARACTER_SELECTION:
			draftController.draw(canvas);
			canvas.end();
			break;
		case PLAYING:
			gameplayController.drawPlay(canvas);
			canvas.end();
			break;
		}
		
	}
	
	private void setupDraftController(){ 
		try {
			draftController.setLevel(ge.getLevel(PVP_LEVEL_NAME));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		draftController.reset(connection, isFirst, from, to);
	}
	
	public void setState(NetworkingState state) {
		networkingState = state;
		switch (networkingState) {
		case SET_NAME:
			ge.initializeCanvas(SET_NAME_BACKGROUND, Constants.SELECT_FONT_FILE);
			break;
		case GAMEROOM:
			break;
		case CHARACTER_SELECTION:
			ge.initializeCanvas(CHARACTER_SELECTION_BACKGROUND, Constants.SELECT_FONT_FILE);
			break;
		case PLAYING:
			ge.initializeCanvas(PLAYING_BACKGROUND, Constants.SELECT_FONT_FILE);
			break;
		}
	}
	
	public void setAssetManager(AssetManager manager) {
		draftController.setAssets(manager);
	}
	
	public void reset() {
		isDone = false;
		networkingMenu = new NetworkingMenu();
		try {
			connection = new Connection();
			connection.connect("localhost", 8989);
		} catch (IOException e) {
		}
		gameplayController.setConnection(connection);
		setState(NetworkingState.SET_NAME);
	}
	
	public boolean isDone() {
		return isDone;
	}
}
