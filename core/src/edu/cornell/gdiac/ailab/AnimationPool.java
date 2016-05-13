package edu.cornell.gdiac.ailab;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

public class AnimationPool{
	//List of AnimationNodes to draw
	List<AnimationNode> pool;
	
	private static final float ANIMATION_BOARD_SCALE = 0.85f;
	
	public AnimationPool(){
		pool = new LinkedList<AnimationNode>();
	}
	
	public void add(Animation an, int xPos, int yPos){
		pool.add(new AnimationNode(an,xPos,yPos));
	}
	
	public float getBoardScale(GameCanvas canvas,float textureWidth,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		return (tileW*ANIMATION_BOARD_SCALE)/textureWidth;
	}
	
	@SuppressWarnings("unchecked")
	public void sort(){
		Collections.sort(this.pool);
	}
	
	/**
	 * Draws each animation on the board, and removes if the animation is done
	 */
	public void draw(GameCanvas canvas, GridBoard board, InGameState inGameState){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Iterator<AnimationNode> iter = pool.iterator();
		boolean paused = inGameState == InGameState.PAUSED ? true : false;
    	while (iter.hasNext()) {
    	    AnimationNode animNode = iter.next();
    	    Coordinate c =board.offsetBoard(canvas, tileW*animNode.xPos,tileH*animNode.yPos);
    	    float messageX = c.x;
			float messageY = c.y;
			c.free();
    	    FilmStrip toDraw = animNode.getTexture(paused);
    	    if (toDraw != null){
    	    	// temporary to scale down for now to size of tile we will do something more clever 
    	    	// later if we attempt to have multi-tile spanning particles.
    	    	float boardScale =  this.getBoardScale(canvas, toDraw.getRegionWidth(), board);
    			float widthTexture = toDraw.getRegionWidth()*boardScale;
    			float heightTexture = toDraw.getRegionHeight()*boardScale;
    	    	canvas.draw(toDraw, Color.WHITE.cpy(), messageX,messageY,widthTexture,heightTexture);
    	    } else {
    	    	iter.remove();
    	    }
    	}
	}

}
