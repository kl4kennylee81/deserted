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
			case SLOW:
				processSlow(e, c);
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
	public void processSlow(Effect e, Character c){
		if(e.isNew){
			c.castSpeed = c.castSpeed * e.magnitude;
			c.speed = c.speed * e.magnitude;
			e.isNew = false;
		}
		e.framesLeft -= 1;
		if(e.framesLeft == 0){
			c.castSpeed = c.castSpeed / e.magnitude;
			c.speed = c.speed / e.magnitude;
			e.isDone = true;
		}
	}
}
