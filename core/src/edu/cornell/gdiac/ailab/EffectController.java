package edu.cornell.gdiac.ailab;

import java.util.List;


public class EffectController {
	
	public List<Character> characters;
	
	public EffectController(List<Character> chars){
		this.characters = chars;
	}
	
	public void update(){
		for(Character c: characters){
			processEffects(c);
			removeFinishedEffects(c);
		}
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
			default:
				break;
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
}
