package edu.cornell.gdiac.ailab;

import edu.cornell.gdiac.mesh.TexturedMesh;

public class GridBoard {
	float space;
	TexturedMesh tileMesh;
	Tile[][] tiles;
	// In number of tiles
	int width;
	int height;
	
	//CHANGE
	final int TILE_WIDTH = 5;
	final int TILE_HEIGHT = 5;
	
	private class Tile {
		//Currently targeting
		boolean isHighlighted;
		
		//Available to target
		boolean canTarget;
		
		//Tile is attacked
		boolean isAttacked;
		
		//Currently has a character
		boolean isOccupied;
		
	}
	
	public GridBoard(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new Tile[width][height];
	}
	
	public void setTileMesh(TexturedMesh mesh) {
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
		float sx = x*100;
		float sy = y*100;

		// You can modify the following to change a tile's highlight color.
		// BASIC_COLOR corresponds to no highlight.
		///////////////////////////////////////////////////////
		/*
		tileMesh.setColor(BASIC_COLOR);
		if (tile.power) {
			tileMesh.setColor(POWER_COLOR);
		}*/

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawTile(tileMesh, sx, sy, 0.5f, 0.5f);
	}
	
	public void update(){
		
	}
	
	
}
