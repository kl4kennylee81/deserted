package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Tile.TileState;


public class EffectController {
	
	// constant a global effects one round = 8 seconds
	private static final float ROUNDS_TO_SECONDS = 7.5f;
	
	private static final float FPS = 60f;
	
	public EffectController(){
	}
	
	public void update(List<Character> characters,GridBoard board){	
		HashSet<Character> toUpdate = new HashSet<Character>();
		for (Character c: characters){
			if (c instanceof BossCharacter){
				Character parent = ((BossCharacter) c).getParent();
				if (((BossCharacter) c).sharedStatus 
						&& !toUpdate.contains(parent)){
					toUpdate.add(parent);
				}
				else if (!(((BossCharacter) c).sharedStatus )){
					toUpdate.add(c);
				}
			}
			else{
				toUpdate.add(c);
			}
		}
		
		for(Character c: toUpdate){
			removeFinishedEffects(c);
		}
		// update board tiles
		processTileEffects(board);
	}
	
	
	public void removeFinishedEffects(Character c){
		LinkedList<Effect> toRemove = new LinkedList<Effect>();
		
		for (Effect e:c.getEffects()){
			e.roundsLeft -= c.castMoved;
			if (e.roundsLeft <= 0){
				e.isDone = true;
			}
			
			if(e.isDone){
				toRemove.add(e);
			}
		}
		for (Effect e: toRemove){
			c.removeEffect(e);
		}
	}
	
	public void processTileEffects(GridBoard board){
		HashMap<String,Effect> tileEffects = board.getTileEffects();
		LinkedList<String> toRemove = new LinkedList<String>();
		for (String c:tileEffects.keySet()){
			Effect e = tileEffects.get(c);
			processTileEffect(e,c,board);
			if (e.isDone){
				toRemove.add(c);
			}
		}
		// remove the tile effects outside of the iteration
		for (String c:toRemove){
			tileEffects.remove(c);
		}
	}
	
	public void processTileEffect(Effect e,String c,GridBoard board){
		// each frame take away 1/(FPS * ROUNDS_TO_SECONDS) of an effects rounds
		// for all tile effects need to subtract some part of a rounds
		e.roundsLeft -= 1/(FPS*ROUNDS_TO_SECONDS);
		if (e.roundsLeft <= 0){
			e.isDone = true;
		}
		String[] coordString = c.split(":");
		int x = 0; int y = 0;
		if (coordString.length > 1){
			x = Integer.parseInt(coordString[0]);
			y = Integer.parseInt(coordString[1]);
		}
		switch (e.type){
		case BROKEN:
			processBroken(e,x,y,board);
			break;
		default:
			break;
		}
	}
	
	public void processBroken(Effect e,int x,int y,GridBoard board){
		if (e.isNew){
			// set the tile state to broken
			board.setTileEffect(x,y, TileState.BROKEN);
			e.isNew = false;
		}
		else if (e.isDone){
			// might have to save the previous tile state and return to its original state
			board.setTileEffect(x,y, TileState.NORMAL);
		}
	}
}
