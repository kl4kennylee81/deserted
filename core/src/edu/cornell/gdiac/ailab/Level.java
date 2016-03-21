package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

public class Level {

	private LinkedList<Character> characters;
    
	
    private String nextLevel;
    
    public Level(){
    	
    }



	public String getNextLevel() {
		return nextLevel;
	}

	public void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}



	public LinkedList<Character> getCharacters() {
		return characters;
	}



	public void setCharacters(LinkedList<Character> characters) {
		this.characters = characters;
	}



    
}
