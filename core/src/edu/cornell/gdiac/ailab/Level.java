package edu.cornell.gdiac.ailab;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.Texture;

public class Level {

	private Characters characters;
    
    private String nextLevel;
    private Integer boardWidth;
    private Integer boardHeight;
    private Texture boardTexture;
    private TacticalManager tacticalManager;
    
    public Level(){
    	
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



	public int getBoardHeight() {
		return boardHeight;
	}



	public void setBoardHeight(int boardHeight) {
		this.boardHeight = boardHeight;
	}



	public int getBoardWidth() {
		return boardWidth;
	}



	public void setBoardWidth(int boardWidth) {
		this.boardWidth = boardWidth;
	}



	public Texture getBoardTexture() {
		return boardTexture;
	}



	public void setBoardTexture(Texture boardTexture) {
		this.boardTexture = boardTexture;
	}
	
	public TacticalManager getTacticalManager(){
		return tacticalManager;
	}
	
	public void setTacticalManager(TacticalManager tm){
		this.tacticalManager = tm;
	}
    
}
