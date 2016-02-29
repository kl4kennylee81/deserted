package edu.cornell.gdiac.ailab;

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
	
	/** Color of a regular tile */
	private static final Color BASIC_COLOR1 = new Color(0.2f, 0.2f, 1.0f, 1.0f);
	private static final Color BASIC_COLOR2 = new Color(1.0f, 0.6f, 0.2f, 1.0f);
	/** Highlight color for power tiles */
	private static final Color POWER_COLOR = new Color( 0.0f,  1.0f,  1.0f, 0.5f);
	
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
		
	}
	
	public GridBoard(int width, int height) {
		this.width = width;
		this.height = height;
		size = 100;
		tiles = new Tile[width][height];
		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				tiles[x][y] = new Tile();
			}
		}
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
		
		float sx = size*x+100;
		float sy = size*y;

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
		Color color = x<width/2 ? BASIC_COLOR1 : BASIC_COLOR2;

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawTile(sx,sy,tileMesh,size,color);
	}
	
	public void update(){
		
	}
	
	
}
