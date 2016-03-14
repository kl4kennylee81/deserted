package edu.cornell.gdiac.ailab;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class GridBoard {
	float space;
	Texture tileMesh;
	Tile[][] tiles;
	// In number of tiles
	int width;
	int height;
	int size;
	float lerpVal = 0;
	boolean increasing;
	
	int xwidth;
	int yheight;
	
	/** Color of a regular tile */
	private static final Color BASIC_COLOR1 = new Color(0.2f, 0.2f, 1.0f, 1.0f);
	private static final Color BASIC_COLOR2 = new Color(237f/255f, 92f/255f, 92f/255f, 1.0f);//new Color(1.0f, 0.6f, 0.2f, 1.0f);
	/** Highlight color for power tiles */
	private static final Color CAN_TARGET_COLOR = new Color( 1f,  1.0f,  0f, 1.0f);
	private static final Color HIGHLIGHT_COLOR = new Color( 0.0f,  1.0f,  1.0f, 1.0f);
	private static final Color ATTACK_COLOR = new Color( 1f, 0f, 0f, 1f);
	
	private class Tile {
		//Currently targeting
		boolean isHighlighted;
		
		//Available to target
		boolean canTarget;
		
		//Tile is attacked
		boolean isAttacked;
		
		//Currently has a character
		boolean isOccupied;
		
		public Tile() {
			isHighlighted = canTarget = isAttacked = isOccupied = false;
		}
		
		public void reset(){
			isHighlighted = canTarget = isAttacked = isOccupied = false;
		}
		
	}
	
	public GridBoard(int width, int height) {
		this.width = width;
		this.height = height;
		lerpVal = 0;
		size = 100;
		tiles = new Tile[width][height];
		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				tiles[x][y] = new Tile();
			}
		}
		
		//change to 150 later
		xwidth = 150;
		yheight = 100;
	}
	
	public void setTileTexture(Texture mesh) {
		tileMesh = mesh;
	}
	
	/**
	 * Draws the board to the given canvas.
	 *
	 * This method draws all of the tiles in this board. It should be the first drawing
	 * pass in the GameEngine.
	 *
	 * @param canvas the drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (increasing){
			lerpVal+=0.03;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= 0.03;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				drawTile(x,y, canvas);
			}
		}
	}
	
	/**
	 * Draws the individual tile at position (x,y). 
	 *
	 * Fallen tiles are not drawn.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 */
	private void drawTile(int x, int y, GameCanvas canvas) {
		Tile tile = tiles[x][y];

		// Compute drawing coordinates
		
		float sx = xwidth*x;
		float sy = yheight*y;

		// You can modify the following to change a tile's highlight color.
		// BASIC_COLOR corresponds to no highlight.
		///////////////////////////////////////////////////////
		/*
		if (x<width/2){
			((TexturedMesh) tileMesh).setColor(BASIC_COLOR1);
		} else {
			tileMesh.setColor(BASIC_COLOR2);
		}
		if (tile.isHighlighted) {
			tileMesh.setColor(POWER_COLOR);
		}*/
		Color color = x<width/2 ? BASIC_COLOR1.cpy() : BASIC_COLOR2.cpy();
		if (tile.isHighlighted){
			//System.out.println("dude");
			color.lerp(HIGHLIGHT_COLOR,lerpVal);
		} else if (tile.canTarget){
			color = CAN_TARGET_COLOR;
		} else if (tile.isAttacked){
			color = ATTACK_COLOR;
		}

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawTile(sx, sy, tileMesh, xwidth, yheight,color);
	}
	
	/**
	 * Reset all tiles
	 */
	public void reset(){
		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
				tiles[i][j].reset();
			}
		}
	}
	
	public void setHighlighted(int x, int y){
		if (x>=0 && x<width && y>=0 && y<height){
			tiles[x][y].isHighlighted = true;
		}
	}
	
	public void setCanTarget(int x, int y){
		if (x>=0 && x<width && y>=0 && y<height){
			tiles[x][y].canTarget = true;
		}
	}
	
	public void setCanTargetSide(boolean leftside){
		int addX = leftside ? width/2 : 0;
		for (int i = 0; i < width/2; i++){
			for (int j = 0; j < height; j++){
				tiles[i+addX][j].canTarget = true;
			}
		}
	}
	
	/**
	 * Occupy the board with active characters.
	 */
	public void occupy(List<Character> chars){
		reset();
		for (Character c : chars){
			if (c.isAlive()){
				tiles[c.xPosition][c.yPosition].isOccupied = true;
			}
		}
	}
	
	/**
	 * Occupy the board with active characters. If a character is 
	 * selected, occupy his shadow's position instead of his own.
	 */
	
	//TODO: What do we occupy now? do we let characters try to move wherever?
	public void occupy(List<Character> chars, Character selected){
		reset();
		for (Character c : chars){
			if (c.isAlive()){
				if (c.equals(selected)){
					//tiles[c.shadowX][c.shadowY].isOccupied = true;
				} else {
					tiles[c.xPosition][c.yPosition].isOccupied = true;
				}
			}
		}
	}
	
	public boolean isOccupied(int x, int y){
		if (x>=0 && x<width && y>=0 && y<height){
			return tiles[x][y].isOccupied;
		}
		return true;
	}
	
	public boolean isInBounds(int x,int y){
		return x<width && x>=0 && y<height && y>=0;
	}
	
	public boolean isOnSide(boolean leftside, int x,int y){
		boolean isLeft = leftside && (this.width/2 > x) && isInBounds(x, y);
		boolean isRight = !leftside && (this.width/2 <= x) && isInBounds(x,y);
		return isLeft||isRight;
	}
	
}
