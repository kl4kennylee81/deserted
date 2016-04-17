package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Texture;

public class Level {

	private Characters characters;
    
    private String nextLevel;
    private GridBoard board;
    private TacticalManager tacticalManager;
    
    private TutorialSteps tutorialSteps;
    
    public Level(){
    	
    }

    public GridBoard getBoard() {
    	return board;
    }
    
    public void setBoard(GridBoard board) {
    	this.board = board;
    }

	public String getNextLevel() {
		return nextLevel;
	}

	public void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}



	public Characters getCharacters() {
		return characters;
	}



	public void setCharacters(Characters characters) {
		this.characters = characters;
	}

	
	public TacticalManager getTacticalManager(){
		return tacticalManager;
	}
	
	public void setTacticalManager(TacticalManager tm){
		this.tacticalManager = tm;
	}



	public TutorialSteps getTutorialSteps() {
		return tutorialSteps;
	}



	public void setTutorialSteps(TutorialSteps tutorialSteps) {
		this.tutorialSteps = tutorialSteps;
	}
    
}
