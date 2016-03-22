package edu.cornell.gdiac.ailab;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class AnimationPool {
	//List of AnimationNodes to draw
	List<AnimationNode> pool;
	
	public AnimationPool(){
		pool = new LinkedList<AnimationNode>();
	}
	
	public void add(Animation an, int xPos, int yPos){
		pool.add(new AnimationNode(an,xPos,yPos));
	}
	
	/**
	 * Draws each animation on the board, and removes if the animation is done
	 */
	public void draw(GameCanvas canvas, GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Iterator<AnimationNode> iter = pool.iterator();
    	while (iter.hasNext()) {
    	    AnimationNode animNode = iter.next();
    	    Coordinate c =board.offsetBoard(canvas, tileW*animNode.xPos,tileH*animNode.yPos);
    	    float messageX = c.x;
			float messageY = c.y;
			c.free();
    	    FilmStrip toDraw = animNode.getTexture();
    	    if (toDraw != null){
    	    	canvas.draw(toDraw, messageX,messageY);
    	    } else {
    	    	iter.remove();
    	    }
    	}
	}
}
