/*
 * Board.java
 * 
 * This class keeps track of all the tiles in the game. If a photon hits 
 * a ship on a Tile, then that Tile falls away.
 *
 * Because of this gameplay, there clearly has to be a lot of interaction
 * between the Board, Ships, and Photons.  However, this way leads to 
 * cyclical references.  As we will discover later in the class, cyclic
 * references are bad, because they lead to components that are too
 * tightly coupled.
 *
 * To address this problem, this project uses a philosophy of "passive"
 * models.  Models do not access the methods or fields of any other
 * Model class.  If we need for two Model objects to interact with
 * one another, this is handled in a controller class. This can get 
 * cumbersome at times (particularly in the coordinate transformation
 * methods in this class), but it makes it easier to modify our
 * code in the future.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;

import edu.cornell.gdiac.mesh.*;

/**
 * Class represents a 2D grid of tiles.
 *
 * Most of the work is done by the internal Tile class.  The outer class is
 * really just a container.
 */
public class Board {

	/**
	 * Each tile on the board has a set of attributes associated with it.
	 * However, no class other than board needs to access them directly.
	 * Therefore, we make this an inner class.
	 */
	private static class TileState {
		/** Is this a power tile? */
		public boolean power = false;
		/** Is this a goal tiles */
		public boolean goal = false;
		/** Has this tile been visited (used for pathfinding)? */
		public boolean visited = false;
		/** Is this tile falling */
		public boolean falling = false;
		/** How far the tile has fallen */
		public float fallAmount = 0;
	}
	
	// Constants
	/** How far a doomed ship will fall (in z-coords) each turn */
	private static final float FALL_RATE = 0.5f;
	/** The minimal z-coordinate before a ship will fall to death */
	private static final float MIN_FALL_AMOUNT = 1.0f;
	/** The z-coordinate at which the ship is removed from the screen */
	private static final float MAX_FALL_AMOUNT = 200.0f;
	/** Space to leave open between tiles */
	private static final float TILE_SPACE = 12.0f;
	/** The dimensions of a single tile */
	private static final int   TILE_WIDTH = 64; // MUST BE 2X VALUE IN GAMECANVAS
	/** The number of normal tiles before a power tile */
	private static final int   POWER_SPACE = 4;
	/** Color of a regular tile */
	private static final Color BASIC_COLOR = new Color(0.25f, 0.25f, 0.25f, 0.5f);
	/** Highlight color for power tiles */
	private static final Color POWER_COLOR = new Color( 0.0f,  1.0f,  1.0f, 0.5f);

	// Instance attributes
	/** The board width (in number of tiles) */
	private int width;
	/** The board height (in number of tiles) */
	private int height;
	/** The tile grid (with above dimensions) */
	private TileState[] tiles;

	/** Texture+Mesh for tile. Only need one, since all have same geometry */
	private TexturedMesh tileMesh;

	/**
	 * Creates a new board of the given size
	 *
	 * @param width Board width in tiles
	 * @param height Board height in tiles
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new TileState[width * height];
		for (int ii = 0; ii < tiles.length; ii++) {
			tiles[ii] = new TileState();
		}
		resetTiles();
	}
	
	/**
	 * Resets the values of all the tiles on screen.
	 */
	public void resetTiles() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				TileState tile = getTileState(x, y);
				tile.power = (x % POWER_SPACE == 0) || (y % POWER_SPACE == 0);
				tile.goal = false;
				tile.visited = false;
				tile.fallAmount = 0.0f;
				tile.falling = false;
			}
		}
	}
	
	/**
	 * Returns the tile state for the given position (INTERNAL USE ONLY)
	 *
	 * Returns null if that position is out of bounds.
	 *
	 * @return the tile state for the given position 
	 */
	private TileState getTileState(int x, int y) {
		if (!inBounds(x, y)) {
			return null;
		}
		return tiles[x * height + y];
	}

	/** 
	 * Returns the number of tiles horizontally across the board.
	 *
	 * @return the number of tiles horizontally across the board.
	 */
	public int getWidth() {
		return width;
	}

	/** 
	 * Returns the number of tiles vertically across the board.
	 *
	 * @return the number of tiles vertically across the board.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the size of the tile texture.
	 *
	 * @return the size of the tile texture.
	 */
	public int getTileSize() {
		return TILE_WIDTH;
	}

	/**
	 * Returns the amount of spacing between tiles.
	 *
	 * @return the amount of spacing between tiles.
	 */
	public float getTileSpacing() {
		return TILE_SPACE;
	}

	
	// Drawing information	
	/**
	 * Returns the textured mesh for each tile.
	 *
	 * We only need one mesh, as all tiles look (mostly) the same.
	 *
	 * @return the textured mesh for each tile.
	 */
	public TexturedMesh getTileMesh() {
		return tileMesh;
	}

	/**
	 * Sets the textured mesh for each tile.
	 *
	 * We only need one mesh, as all tiles look (mostly) the same.
	 *
	 * @param mesh the textured mesh for each tile.
	 */
	public void setTileMesh(TexturedMesh mesh) {
		tileMesh = mesh;
	}


	// COORDINATE TRANSFORMS
	// The methods are used by the physics engine to coordinate the
	// Ships and Photons with the board. You should not need them.
	
	/**
	 * Returns true if a screen location is safe (i.e. there is a tile there)
	 *
	 * @param x The x value in screen coordinates
	 * @param y The y value in screen coordinates
	 *
	 * @return true if a screen location is safe
	 */
	public boolean isSafeAtScreen(float x, float y) {
		int bx = screenToBoard(x);
		int by = screenToBoard(y);
		return x >= 0 && y >= 0
				&& x < width * (getTileSize() + getTileSpacing()) - getTileSpacing()
				&& y < height * (getTileSize() + getTileSpacing()) - getTileSpacing() 
				&& !getTileState(bx, by).falling;
	}

	/**
	 * Returns true if a tile location is safe (i.e. there is a tile there)
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if a screen location is safe
	 */
	public boolean isSafeAt(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height
				&& !getTileState(x, y).falling;
	}

	/**
 	 * Destroys a tile at the given cell location.
 	 *
 	 * Destruction only causes the tile to begin to fall. It is not
	 * destroyed until it reaches MIN_FATAL_AMOUNT.  This allows any
	 * ships on it a little bit of time to escape.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 */
	public void destroyTileAt(int x, int y) {
		if (!inBounds(x, y)) {
			return;
		}

		getTileState(x, y).falling = true;
	}

	/** 
	 * Returns true if a tile is completely destroyed yet.
	 *
	 * Destruction only causes the tile to begin to fall. It is not
	 * actually destroyed until it reaches MIN_FATAL_AMOUNT.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if a tile is completely destroyed
	 */
	public boolean isDestroyedAt(int x, int y) {
		if (!inBounds(x, y)) {
			return true;
		}

		return getTileState(x, y).fallAmount >= MIN_FALL_AMOUNT;
	}

	/**
	 * Returns true if a tile is a power tile (for weapon firing).
	 *
	 * @param x The x value in screen coordinates
	 * @param y The y value in screen coordinates
	 *
	 * @return true if a tile is a power tile
	 */
	public boolean isPowerTileAtScreen(float x, float y) {
		int tx = screenToBoard(x);
		int ty = screenToBoard(y);
		if (!inBounds(tx, ty)) {
			return false;
		}
		
		return getTileState(tx, ty).power;
	}
	
	/**
	 * Returns true if a tile is a power tile (for weapon firing).
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if a tile is a power tile
	 */
	public boolean isPowerTileAt(int x, int y) {
		if (!inBounds(x, y)) {
			return false;
		}
		
		return getTileState(x, y).power;
	}

	// GAME LOOP
	// This performs any updates local to the board (e.g. animation)

	/**
	 * Updates the state of all of the tiles.
	 *
	 * All we do is animate falling tiles.
	 */
	public void update() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				TileState tile = getTileState(x, y);
				if (tile.falling && tile.fallAmount <= MAX_FALL_AMOUNT) {
					tile.fallAmount += FALL_RATE;
				}
			}
		}
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
				drawTile(x, y, canvas);
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
		TileState tile = getTileState(x, y);
		
		// Don't draw tile if it's fallen off the screen
		if (tile.fallAmount >= 0.95f * MAX_FALL_AMOUNT) {
			return;
		}

		// Compute drawing coordinates
		float sx = boardToScreen(x);
		float sy = boardToScreen(y);
		float sz = tile.fallAmount;
		float a = 0.1f * tile.fallAmount;

		// You can modify the following to change a tile's highlight color.
		// BASIC_COLOR corresponds to no highlight.
		///////////////////////////////////////////////////////

		tileMesh.setColor(BASIC_COLOR);
		if (tile.power) {
			tileMesh.setColor(POWER_COLOR);
		}

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawTile(tileMesh, sx, sy, sz, a);
	}
	
	// METHODS FOR LAB 2

	// CONVERSION METHODS (OPTIONAL)
	// Use these methods to convert between tile coordinates (int) and
	// world coordinates (float).

	/**
	 * Returns the board cell index for a screen position.
	 *
	 * While all positions are 2-dimensional, the dimensions to
 	 * the board are symmetric. This allows us to use the same
	 * method to convert an x coordinate or a y coordinate to
	 * a cell index.
	 *
	 * @param f Screen position coordinate
	 *
	 * @return the board cell index for a screen position.
	 */
	public int screenToBoard(float f) {
		return (int)(f / (getTileSize() + getTileSpacing()));
	}

	/**
	 * Returns the screen position coordinate for a board cell index.
	 *
	 * While all positions are 2-dimensional, the dimensions to
 	 * the board are symmetric. This allows us to use the same
	 * method to convert an x coordinate or a y coordinate to
	 * a cell index.
	 *
	 * @param n Tile cell index
	 *
	 * @return the screen position coordinate for a board cell index.
	 */
	public float boardToScreen(int n) {
		return (float) (n + 0.5f) * (getTileSize() + getTileSpacing());
	}
	
	/**
	 * Returns the distance to the tile center in screen coordinates.
	 *
	 * This method is an implicit coordinate transform. It takes a position (either 
	 * x or y, as the dimensions are symmetric) in screen coordinates, and determines
	 * the distance to the nearest tile center.
	 *
	 * @param f Screen position coordinate
	 *
	 * @return the distance to the tile center
	 */
	public float centerOffset(float f) {
		float paddedTileSize = getTileSize() + getTileSpacing();
		int cell = screenToBoard(f);
		float nearestCenter = (cell + 0.5f) * paddedTileSize;
		return f - nearestCenter;
	}

	// PATHFINDING METHODS (REQUIRED)	
	// Use these methods to implement pathfinding on the board.
	
	/**
	 * Returns true if the given position is a valid tile
	 *
	 * It does not check whether the tile is live or not.  Dead tiles are still valid.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if the given position is a valid tile
	 */
	public boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}

	/**
	 * Returns true if the tile has been visited.
	 *
	 * A tile position that is not on the board will always evaluate to false.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if the tile has been visited.
	 */
	public boolean isVisited(int x, int y) {
		if (!inBounds(x, y)) {
			return false;
		}

		return getTileState(x, y).visited;
	}
	
	/**
	 * Marks a tile as visited.
	 *
	 * A marked tile will return true for isVisited(), until a call to clearMarks().
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 */
	public void setVisited(int x, int y) {
		if (!inBounds(x,y)) {
			Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
			return;
		}
		getTileState(x, y).visited = true;
	}

	/**
	 * Returns true if the tile is a goal.
	 *
	 * A tile position that is not on the board will always evaluate to false.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 *
	 * @return true if the tile is a goal.
	 */
	public boolean isGoal(int x, int y) {
		if (!inBounds(x, y)) {
			return false;
		}

		return getTileState(x, y).goal;
	}

	/**
	 * Marks a tile as a goal.
	 *
	 * A marked tile will return true for isGoal(), until a call to clearMarks().
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 */
	public void setGoal(int x, int y) {
		if (!inBounds(x,y)) {
			Gdx.app.error("Board", "Illegal tile "+x+","+y, new IndexOutOfBoundsException());
			return;
		}
		getTileState(x, y).goal = true;
	}

	/**
	 * Clears all marks on the board.
	 *
	 * This method should be done at the beginning of any pathfinding round.
	 */
	public void clearMarks() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				TileState state = getTileState(x, y);
				state.visited = false;
				state.goal = false;
			}
		}
	}
}