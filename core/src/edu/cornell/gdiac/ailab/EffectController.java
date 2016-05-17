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
		HashMap<Character,List<Character>> toUpdate = new HashMap<Character,List<Character>>();
		for (Character c: characters){
			if (c instanceof BossCharacter){
				Character parent = ((BossCharacter) c).getParent();
				if (((BossCharacter) c).sharedStatus 
						&& !toUpdate.containsKey(parent)){
					List<Character> childList = new LinkedList<Character>();
					childList.add(c);
					toUpdate.put(parent,childList);
				}
				else if (toUpdate.containsKey(parent)){
					toUpdate.get(parent).add(c);
				}
				else if (!(((BossCharacter) c).sharedStatus )){
					toUpdate.put(c,null);
				}
			}
			else{
				toUpdate.put(c,null);
			}
		}
		
		// update the character position for the children characters after
		// removing the effect
		for(Character c: toUpdate.keySet()){
			if (toUpdate.get(c) == null){
				removeFinishedEffects(c);
			}
			else {
				List<Effect> effectRemoved = removeFinishedEffects(c);
				List<Character> childList = toUpdate.get(c);
				for (Character child:childList){
					for (Effect e: effectRemoved){
						child.translateCastPosition(e, false);
					}
				}
			}
		}
		// update board tiles
		processTileEffects(board);
	}
	
	
	public List<Effect> removeFinishedEffects(Character c){
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
		return toRemove;
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
