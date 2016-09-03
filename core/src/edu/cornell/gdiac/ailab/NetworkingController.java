package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.badlogic.gdx.graphics.Color;

import networkUtils.ChallengeMessage;
import networkUtils.Connection;
import networkUtils.Message;
import networkUtils.UsernameMessage;

public class NetworkingController {
	
	private static enum NetworkingState {
		SET_NAME,
		GAMEROOM,
		CHARACTER_SELECTION,
		PLAYING,
	}
	
	private NetworkingState networkingState;
	private NetworkingMenu networkingMenu;
	private NetworkingGameplayController gameplayController;
	Level level;
	
	private Connection connection;
	
	private boolean isFirst;
	
	boolean connected;
	boolean isDone;
	
	public NetworkingController(NetworkingGameplayController ngc) {
		networkingMenu = new NetworkingMenu();
		setState(NetworkingState.SET_NAME);
		isDone = false;
		gameplayController = ngc;
		try {
			connection = new Connection();
			connection.connect("localhost", 8989);
			connected = true;
		} catch (IOException e) {
			connected = false;
		}
		gameplayController.setConnection(connection);
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
				ChallengeMessage cm = (ChallengeMessage) m;
				isFirst = cm.getIsFirst();
				gameplayController.resetGame(level);
				gameplayController.setupGame(isFirst, cm.getFrom(), cm.getTo());
				setState(NetworkingState.PLAYING);
			}
			break;
		case CHARACTER_SELECTION:
			break;
		case PLAYING:
			gameplayController.update();
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
			break;
		case PLAYING:
			gameplayController.drawPlay(canvas);
			canvas.end();
			break;
		}
		
	}
	
	public void setState(NetworkingState state) {
		networkingState = state;
		switch (networkingState) {
		case SET_NAME:
			break;
		case GAMEROOM:
			break;
		case CHARACTER_SELECTION:
			break;
		case PLAYING:
			break;
		}
	}
	
	public void reset() {
		isDone = false;
		networkingMenu = new NetworkingMenu();
		setState(NetworkingState.SET_NAME);
	}
	
	public boolean isDone() {
		return isDone;
	}
}