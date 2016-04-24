package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.Tile.TileState;


public class EffectController {
	
	// constant a global effects one round = 8 seconds
	private static final float ROUNDS_TO_SECONDS = 8f;
	
	private static final float FPS = 60f;
	
	public EffectController(){
	}
	
	public void update(List<Character> characters,GridBoard board){
		for(Character c: characters){
			processEffects(c);
			removeFinishedEffects(c);
		}
		// update board tiles
		processTileEffects(board);
	}
	
	public void processEffects(Character c){
		for(Effect e: c.getEffects()){
			processEffect(e, c);
		}
	}
	
	public void processEffect(Effect e, Character c){
		switch(e.type){
			case SPEED:
				processSpeed(e, c);
				break;
			case DAZED:
				processDazed(e, c);
				break;
			default:
				break;
		}
	}
	
	/**
	 * Applies the slow effect to the character if it is new, reduces the "framesLeft" 
	 * counter, and sets a flag, and reverses the effect, if the effect is done.
	 */
	public void processSpeed(Effect e, Character c){
		if(e.isNew){
			c.setSpeedModifier(c.getSpeedModifier()+e.magnitude);
			e.isNew = false;
		}
		e.roundsLeft -= c.castMoved;
		if (e.roundsLeft <= 0){
			c.setSpeedModifier(c.getSpeedModifier()-e.magnitude);
			e.isDone = true;
		}
	}
	
	/**
	 * Applies the dazed effect to the character if it is new, reduces the "framesLeft" 
	 * counter, and sets a flag, and reverses the effect, if the effect is done.
	 */
	public void processDazed(Effect e, Character c){
		if(e.isNew){
			c.setDazedSlots(c.getDazedSlots()+e.magnitude);
			e.isNew = false;
		}
		e.roundsLeft -= c.castMoved;
		if (e.roundsLeft <= 0){
			c.setDazedSlots(c.getDazedSlots()-e.magnitude);
			e.isDone = true;
		}
	}
	
	
	public void removeFinishedEffects(Character c){
		int i = 0;
		while(i < c.getEffects().size()){
			if(c.getEffects().get(i).isDone){
				c.removeEffect(i);
			}
			else{
				i++;
			}
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
