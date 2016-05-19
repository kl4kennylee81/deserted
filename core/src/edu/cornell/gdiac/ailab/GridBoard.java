package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Tile.TileState;

public class GridBoard {
	float space;
	TextureRegion tileMesh;
	TextureRegion bottomRimMesh;
	
	Tile[][] tiles;
	// In number of tiles
	int width;
	int height;
	float lerpVal = 0;
	boolean increasing;
	
	// tile effects
	private HashMap<String,Effect> tileEffects;
	
	/** Color of a regular tile */
	private static final Color BASIC_COLOR1 = new Color(0.2f, 0.2f, 1.0f, 1.0f);
	private static final Color BASIC_COLOR2 = new Color(237f/255f, 92f/255f, 92f/255f, 1.0f);//new Color(1.0f, 0.6f, 0.2f, 1.0f);
	/** Highlight color for power tiles */
	private static final Color CAN_TARGET_COLOR = new Color( 1f,  1.0f,  0f, 1.0f);
	private static final Color HIGHLIGHT_COLOR = new Color( 0.0f,  1.0f,  1.0f, 1.0f);
	private static final Color ATTACK_COLOR = new Color( 1f, 0f, 0f, 1f);
	private static final Color BROKEN_COLOR = Color.BLACK.cpy();
	
	
	public static final float BOARD_WIDTH = 0.6f;
	
	public static final float BOARD_HEIGHT = 0.35f;

	public static final float BOARD_OFFSET_X = (1-BOARD_WIDTH - BOARD_HEIGHT*Constants.TILE_SHEAR)/2;
	
	public static final float EXTRA_OFFSET = 0.02f;
	
	public static final float BOARD_OFFSET_Y = 0.075f;
	
	public HashMap<String,Effect> getTileEffects(){
		return this.tileEffects;
	}
	
	public float getWidth(){
		return this.width;
	}
	
	public float getHeight(){
		return this.height;
	}
	
	public float getTileWidth(GameCanvas canvas){
		return (canvas.getWidth() * BOARD_WIDTH)/width;
	}
	
	public float getTileHeight(GameCanvas canvas){
		return (canvas.getHeight() * BOARD_HEIGHT)/height;
	}
	
	public float getBoardOffsetX(GameCanvas canvas){
		return BOARD_OFFSET_X * canvas.getWidth();
	}
	
	public float getBoardOffsetY(GameCanvas canvas){
		return BOARD_OFFSET_Y * canvas.getHeight();
	}
	
	public boolean getIsHighlighted(int x,int y){
		if (this.isInBounds(x, y)){
			return tiles[x][y].isHighlighted;
		}
		else{
			return false;
		}
	}
	
	public boolean getcanTarget(int x,int y){
		if (this.isInBounds(x, y)){
			return tiles[x][y].canTarget;
		}
		else{
			return false;
		}
	}
	
	
	/** returns true if tile (x,y) is broken **/
	public boolean IsBroken(int x,int y){
		if (this.isInBounds(x, y)){
			return tiles[x][y].state == TileState.BROKEN;
		}
		else{
			return false;
		}
	}
	
	/** currently you can't move onto a tile if its broken not on your side or if its occupied **/
	public boolean canMove(boolean leftside,int x,int y){
		return this.isOnSide(leftside,x,y) &&
		(!this.isOccupied(x,y)) && (!this.IsBroken(x,y));
	}
	
	public Coordinate offsetBoard(GameCanvas canvas,float xPos,float yPos){
		// board offset accounts for the shearing as well as centering to the tile
		int newxPos = (int)(getBoardOffsetX(canvas) + xPos + Constants.TILE_SHEAR*yPos + this.getTileWidth(canvas)/3);
		int newyPos = (int)(getBoardOffsetY(canvas) + yPos);
		Coordinates coords = Coordinates.getInstance();
		Coordinate c = coords.obtain();
		c.set(newxPos, newyPos);
		return c;
	}
	
	
	public GridBoard(int width, int height) {
		this.width = width;
		this.height = height;
		lerpVal = 0;
		tiles = new Tile[width][height];
		this.tileEffects = new HashMap<String,Effect>();
		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				tiles[x][y] = new Tile(TileState.NORMAL);
			}
		}
	}
	
	public void setTileTexture(Texture mesh) {
		tileMesh = new TextureRegion(mesh);
	}
	
	public void setTileRimTexture(Texture mesh){
		bottomRimMesh = new TextureRegion(mesh);
	}
	
	public void setTileEffect(int x, int y, TileState effect){
		if (this.isInBounds(x, y)){
			tiles[x][y].setEffect(effect);
		}
	}
	
	public void addTileEffect(String c,Effect e){
		this.tileEffects.put(c,e);
	}
	
	
	/**
	 * Draws the board to the given canvas.
	 *
	 * This method draws all of the tiles in this board. It should be the first drawing
	 * pass in the GameEngine.
	 *
	 * @param canvas the drawing context
	 */
	public void draw(GameCanvas canvas, SelectionMenuController controller) {
		if (increasing){
			lerpVal += Constants.LERP_RATE;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= Constants.LERP_RATE;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				drawTile(x,y, canvas, controller);
			}
		}
	}
	
	public void drawMini(GameCanvas canvas) {
		if (increasing){
			lerpVal += Constants.LERP_RATE;
			if (lerpVal >= 1){
				increasing = false;
			}
		} else {
			lerpVal -= Constants.LERP_RATE;
			if (lerpVal <= 0){
				increasing = true;
			}
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				drawMiniTile(x,y, canvas);
			}
		}
	}
	
	public void drawMiniTile(int x, int y, GameCanvas canvas){
		Tile tile = tiles[x][y];
		float tileW = 0.06f*canvas.width;
		float tileH = 0.06f*canvas.height;
		float tileX = 0.22f*canvas.width+tileW*x;
		float tileY = 0.66f*canvas.height+tileH*y;
		
		Color color = x<width/2 ? BASIC_COLOR1.cpy() : BASIC_COLOR2.cpy();
		if (tile.isHighlighted){
			color.lerp(HIGHLIGHT_COLOR,lerpVal);
		} else if (tile.canTarget){
			color = CAN_TARGET_COLOR;
		} 
		
		canvas.draw(tileMesh, color, tileX, tileY, tileW, tileH);
	}
	
	/**
	 * Draws the individual tile at position (x,y). 
	 *
	 * Fallen tiles are not drawn.
	 *
	 * @param x The x index for the Tile cell
	 * @param y The y index for the Tile cell
	 */
	private void drawTile(int x, int y, GameCanvas canvas, SelectionMenuController controller) {
		Tile tile = tiles[x][y];
		
		// Compute drawing coordinates
		int tileW = (int) getTileWidth(canvas);
		int tileH = (int) getTileHeight(canvas);
		
		float tileX = tileW*x + getBoardOffsetX(canvas);
		float tileY = tileH*y + getBoardOffsetY(canvas);

		Color color = x<width/2 ? BASIC_COLOR1.cpy() : BASIC_COLOR2.cpy();
		if (tile.isHighlighted){
			if (!controller.choosingTarget){
				color = CAN_TARGET_COLOR;
			} else {
				color.lerp(HIGHLIGHT_COLOR,lerpVal);
			}
		} else if (tile.canTarget){
			color = CAN_TARGET_COLOR;
		} else if (tile.isAttacked){
			color = ATTACK_COLOR;
		} else if (tile.state == TileState.BROKEN){
			color = BROKEN_COLOR;
		}

		///////////////////////////////////////////////////////

		// Draw
		canvas.drawTile(tileX, tileY, tileMesh, tileW, tileH,color);
		
		
		// for the bottom row draw the tile rim
		if (y == 0){
			float rimSx = this.getTileWidth(canvas)/this.bottomRimMesh.getRegionWidth();
			float rimSy = 1;
			float rimY = tileY - this.bottomRimMesh.getRegionHeight();
			float rimX = tileX + this.bottomRimMesh.getRegionHeight();
			//FIXUP this hacky thing to get it to the right position figure out why its not aligned
			canvas.drawBoardRim(bottomRimMesh, rimX, rimY, rimSx, rimSy, 0, 0, color);
		}
		// for the rightmost row draw rim
		if (x == this.getWidth()-1){
			float rimSx = (this.getTileHeight(canvas)*Constants.TILE_SHEAR)/this.bottomRimMesh.getRegionWidth();
			float rimSy = 1;
			float rimY = tileY - this.bottomRimMesh.getRegionHeight();
			float rimX = tileX + this.getTileWidth(canvas) + this.bottomRimMesh.getRegionHeight() + (this.getTileHeight(canvas)*Constants.TILE_SHEAR*y);
			
			float shearX = 0;
			float shearY = getTileHeight(canvas)/this.bottomRimMesh.getRegionWidth();

			canvas.drawBoardRim(bottomRimMesh, rimX,rimY,rimSx,rimSy,shearX,shearY,color);
		}
	}
	
	public float bottomOffset(GameCanvas canvas) {
		return canvas.getHeight()*((1-BOARD_WIDTH)/8);
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
	
	/** sets if you can move to tile x,y **/
	public void setCanMove(boolean leftside,int x,int y){
		if (this.canMove(leftside, x, y)){
			this.setCanTarget(x,y);
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
	
	public Coordinate contains(float x, float y, GameCanvas canvas){
		for (int i =0;i<this.getWidth();i++){
			for (int j=0;j<this.getHeight();j++){
				int tileW = (int) getTileWidth(canvas);
				int tileH = (int) getTileHeight(canvas);
				
				int tileX =tileW*i;
				int tileY = tileH*j;
				Coordinate tilePos = this.offsetBoard(canvas, tileX, tileY);
				
				float tileXMax = tilePos.x + tileW;
				float tileYMax = tilePos.y + tileH;
				if (x>tilePos.x && x<=tileXMax && y>tilePos.y && y<=tileYMax){
					tilePos.set(i, j);
					return tilePos;
				}
			}
		}
		return null;
	}
	
}
