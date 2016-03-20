package edu.cornell.gdiac.ailab;

import java.util.HashMap;

public class Level {

	private HashMap<Integer, Character> availableCharacters;
    private HashMap<Integer, Action> availableActions;
    private HashMap<Integer, Animation> availableAnimations;
	
    private String nextLevel;
    
    public Level(){
    	
    }

	public HashMap<Integer, Character> getAvailableCharacters() {
		return availableCharacters;
	}

	public void setAvailableCharacters(HashMap<Integer, Character> availableCharacters) {
		this.availableCharacters = availableCharacters;
	}

	public HashMap<Integer, Action> getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(HashMap<Integer, Action> availableActions) {
		this.availableActions = availableActions;
	}

	public HashMap<Integer, Animation> getAvailableAnimations() {
		return availableAnimations;
	}

	public void setAvailableAnimations(HashMap<Integer, Animation> availableAnimations) {
		this.availableAnimations = availableAnimations;
	}

	public String getNextLevel() {
		return nextLevel;
	}

	public void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}
    
    
}
