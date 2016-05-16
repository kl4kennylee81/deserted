package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class Shields {
	
	private static final float SHIELD_WIDTH = 0.0125f;
	private static final float SHIELD_Y_OFFSET = 0.02f;
	
	private static final int EXPECTED_TILE_1_WIDTH = 200;
	private static final int EXPECTED_TILE_1_HEIGHT = 80;
	private static final int EXPECTED_TILE_2_WIDTH = 210;
	private static final int EXPECTED_TILE_2_HEIGHT = 80;
	private static final int EXPECTED_TILE_3_WIDTH = 200;
	private static final int EXPECTED_TILE_3_HEIGHT = 77;
	private static final float SHIELD_X_OFFSET = 0.03f;
	
	private static final float TILE_1_X_OFFSET = 0.015f;
	private static final float TILE_2_X_OFFSET = 0.03f;
	private static final float TILE_3_X_OFFSET = 0.03f;
	
	private static final Texture SHIELD_1_BOTTOM = new Texture("models/shield_1bottom.png");
	private static final Texture SHIELD_1_TOP = new Texture("models/shield_1top.png");
	private static final Texture SHIELD_2_BOTTOM = new Texture("models/shield_2bottom.png");
	private static final Texture SHIELD_2_TOP = new Texture("models/shield_2top.png");
	private static final Texture SHIELD_3_BOTTOM = new Texture("models/shield_3bottom.png");
	private static final Texture SHIELD_3_TOP = new Texture("models/shield_3top.png");
	
	private List<ActionNode> leftShields;
	private List<ActionNode> rightShields;
	
	List<Coordinate> leftShieldedCoordinates;
	List<Coordinate> rightShieldedCoordinates;
	
	GridBoard board;
	
	public Shields(GridBoard board){
		this.board = board;
		this.leftShields = new ArrayList<ActionNode>();
		this.rightShields = new ArrayList<ActionNode>();
		this.leftShieldedCoordinates = new ArrayList<Coordinate>();
		this.rightShieldedCoordinates = new ArrayList<Coordinate>();
	}
	
	public void addShield(Character character, ActionNode shield, Coordinate[] path){
		shield.setPersisting(0, character.xPosition, character.yPosition, path);
		
		if (character.leftside){
			removeOverlappingShields(leftShields,path);
			leftShields.add(shield);
		} else {
			removeOverlappingShields(rightShields,path);
			rightShields.add(shield);
		}
		resetShieldedCoordinates();
	}
	
	public void removeOverlappingShields(List<ActionNode> shields, Coordinate[] path){
		for (Iterator<ActionNode> iterator = shields.iterator(); iterator.hasNext();) {
		    ActionNode an = iterator.next();
		    Coordinate[] otherPath = an.path;
		    if (overlapPaths(path,otherPath)){
				iterator.remove();
			}
		}
	}
	
	public boolean overlapPaths(Coordinate[] path1, Coordinate[] path2){
		Coordinate pt1;
		Coordinate pt2;
		for (int i = 0; i < path1.length; i++){
			pt1 = path1[i];
			for (int j = 0; j < path2.length; j++){
				pt2 = path2[j];
				if (pt1.x == pt2.x && pt1.y == pt2.y){
					return true;
				}
			}
		}
		return false;
	}
	
	
	public void hitShield(int coordX, int coordY, boolean leftside, TextMessage textMessages){
		List<ActionNode> shieldsToCheck = leftside ? rightShields : leftShields;

		//Character attacks from leftside, hits rightside
		for (Iterator<ActionNode> iterator = shieldsToCheck.iterator(); iterator.hasNext();) {
		    ActionNode an = iterator.next();
		    if (!an.hitThisRound && shieldContains(an,coordX,coordY)){
				an.shieldHitsLeft-=1;
				an.hitThisRound = true;
				textMessages.addOtherMessage("SHIELDED", coordX, coordY, 2*TextMessage.SECOND, Color.BROWN);
			}
		}
		resetShieldedCoordinates();
	}
	
	public void removeShields(){
		for (Iterator<ActionNode> iterator = leftShields.iterator(); iterator.hasNext();) {
		    ActionNode an = iterator.next();
			if (an.shieldHitsLeft <= 0){
				iterator.remove();
			}
		}
		
		for (Iterator<ActionNode> iterator = rightShields.iterator(); iterator.hasNext();) {
		    ActionNode an = iterator.next();
			if (an.shieldHitsLeft <= 0){
				iterator.remove();
			}
		}
		
		resetShieldedCoordinates();
	}
	
	public boolean shieldContains(ActionNode shield, int coordX, int coordY){
		for (Coordinate c : shield.path){
			if (c.x == coordX && c.y == coordY){
				return true;
			}
		}
		return false;
	}
	
	private void resetShieldedCoordinates(){
		// add coordinates back to the pool
		leftShieldedCoordinates.clear();

		for (ActionNode an : leftShields){
			for (Coordinate c:an.path){
				leftShieldedCoordinates.add(c);
			}
		}
		
		rightShieldedCoordinates.clear();

		for (ActionNode an : rightShields){
			for (Coordinate c:an.path){
				rightShieldedCoordinates.add(c);
			}
		}
	}
	
	public void resetShieldsHitThisRound(){
		for (ActionNode an : leftShields){
			an.hitThisRound = false;
		}
		for (ActionNode an : rightShields){
			an.hitThisRound = false;
		}
	}
	
	public void draw(GameCanvas canvas, boolean top, boolean fade){
		for (ActionNode s : leftShields){
			drawShield(canvas,s,true,fade,top);
		}
		for (ActionNode s : rightShields){
			drawShield(canvas,s,false,fade,top);
		}
	}
	
	private void drawShield(GameCanvas canvas,ActionNode an,boolean leftside,boolean fade,boolean top){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		Coordinate c;	
		int botY = Coordinates.minYCoordinate(an.path);
		int numWithin = Coordinates.numWithinBounds(an.path, board);
		int shieldW = (int)(SHIELD_WIDTH * canvas.getWidth());
		int shieldH = (int)(tileH * numWithin);
		// since we draw from the lower left corner. for the left side you draw 1 tile up
		// so it looks like its covering the back of the 2nd tile aka the front of the 1st.
		//int shieldX = (int)(leftside ?tileW*an.curX + tileW:tileW*an.curX);
		int shieldX = (int)(tileW*an.curX);
		shieldX += (int) (SHIELD_X_OFFSET * canvas.getWidth() * (botY));
		int shieldY = (int)(tileH *botY);
		// since the shield is being sheared it just needs to be offset by the X and not by the shearing amount which
		// board offset does. thus we just do it manually by adding on the amount we offset rather than offset board
		// which also takes into the x displacement from being sheared.
		shieldX = (int) (shieldX + board.getBoardOffsetX(canvas));
		shieldY = (int) (shieldY + board.getBoardOffsetY(canvas));
		//canvas.drawTileArrow(shieldX, shieldY, shieldW, shieldH, Color.GRAY)
		float widthRatio = 1;
		float heightRatio = 1;
		if (numWithin == 1){
			shieldX += canvas.getWidth() * TILE_1_X_OFFSET;
			widthRatio = tileW/EXPECTED_TILE_1_WIDTH;
			heightRatio = tileH/EXPECTED_TILE_1_HEIGHT;
		} else if (numWithin == 2){
			shieldX += canvas.getWidth() * TILE_2_X_OFFSET;
			widthRatio = tileW/EXPECTED_TILE_2_WIDTH;
			heightRatio = tileH/EXPECTED_TILE_2_HEIGHT;
		} else if (numWithin == 3){
			shieldX += canvas.getWidth() * TILE_3_X_OFFSET;
			widthRatio = tileW/EXPECTED_TILE_3_WIDTH;
			heightRatio = tileH/EXPECTED_TILE_3_HEIGHT;
		}
		
		Texture toDraw = null;
		if (top){
			if (numWithin == 1){
				toDraw = SHIELD_1_TOP;
			} else if (numWithin == 2){
				toDraw = SHIELD_2_TOP;
			} else if (numWithin == 3){
				toDraw = SHIELD_3_TOP;
			}
		} else {
			if (numWithin == 1){
				toDraw = SHIELD_1_BOTTOM;
			} else if (numWithin == 2){
				toDraw = SHIELD_2_BOTTOM;
			} else if (numWithin == 3){
				toDraw = SHIELD_3_BOTTOM;
			}
		}
		
		Color col = Color.WHITE;
		if (an.shieldHitsLeft == an.action.shieldNumberHits){
			if (an.action.shieldColor0 != null){
				col = an.action.shieldColor0;
			} else {
				col = Color.CYAN;
			}
		} else {
			if (an.action.shieldColor1 != null){
				col = an.action.shieldColor1;
			} else if (an.action.shieldColor0 != null){
				col = an.action.shieldColor0;
			} else {
				col = Color.CYAN;
			}
		}
		if (fade){
			col = col.cpy().mul(1,1,1,0.3f);
		}
		
		canvas.drawTexture(toDraw, shieldX, shieldY, toDraw.getWidth()*widthRatio, toDraw.getHeight()*heightRatio, col);
	}
	
}
