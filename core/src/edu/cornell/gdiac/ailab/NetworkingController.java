package edu.cornell.gdiac.ailab;

public class NetworkingController {
	
	private static enum NetworkingState {
		SET_NAME,
		GAMEROOM,
		CHARACTER_SELECTION,
		PLAYING,
	}
	
	private NetworkingState networkingState;
	private NetworkingMenu networkingMenu;
	
	public NetworkingController() {
		networkingMenu = new NetworkingMenu();
		setState(NetworkingState.SET_NAME);
	}
	
	public void update() {
		switch (networkingState) {
		case SET_NAME:
			networkingMenu.update();
			if (networkingMenu.isDone()) {
				setState(NetworkingState.GAMEROOM);
			}
			break;
		case GAMEROOM:
			break;
		case CHARACTER_SELECTION:
			break;
		case PLAYING:
			break;
		}
	}
	
	public void draw(GameCanvas canvas) {
		switch (networkingState) {
		case SET_NAME:
			networkingMenu.draw(canvas);
			break;
		case GAMEROOM:
			break;
		case CHARACTER_SELECTION:
			break;
		case PLAYING:
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
		if (networkingMenu.username == null || networkingMenu.username.length() == 0) {
			setState(NetworkingState.SET_NAME);
		} else {
			setState(NetworkingState.GAMEROOM);
		}
	}
	
	public boolean isDone() {
		return false;
	}
}
