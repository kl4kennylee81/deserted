package edu.cornell.gdiac.ailab;

public class Tile {

	public static enum TileEffect {
		NORMAL
	}
	
	//tile's effect
	TileEffect effect;
	
	//Currently targeting
	boolean isHighlighted;
	
	//Available to target
	boolean canTarget;
	
	//Tile is attacked
	boolean isAttacked;
	
	//Currently has a character
	boolean isOccupied;
	
	public Tile(TileEffect effect) {
		this.effect = effect;
		isHighlighted = canTarget = isAttacked = isOccupied = false;
	}
	
	public void reset(){
		isHighlighted = canTarget = isAttacked = isOccupied = false;
	}
	
	public void setEffect(TileEffect effect) {
		this.effect = effect;
	}
}
