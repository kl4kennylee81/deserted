package edu.cornell.gdiac.ailab;

public class Tile {

	public static enum TileState {
		NORMAL,
		BROKEN
	}
	
	//tile's state
	TileState state;
	
	//Currently targeting
	boolean isHighlighted;
	
	//Available to target
	boolean canTarget;
	
	//single path can hit
	boolean singleCanTarget;
	
	//Tile is attacked
	boolean isAttacked;
	
	//Currently has a character
	boolean isOccupied;
	
	public Tile(TileState effect) {
		this.state = effect;
		isHighlighted = canTarget = isAttacked = isOccupied = singleCanTarget = false;
	}
	
	public void reset(){
		isHighlighted = canTarget = isAttacked = isOccupied = singleCanTarget = false;
	}
	
	public void setEffect(TileState effect) {
		this.state = effect;
	}
}
