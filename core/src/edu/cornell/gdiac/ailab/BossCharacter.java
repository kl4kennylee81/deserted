package edu.cornell.gdiac.ailab;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class BossCharacter extends Character{
	/** pointer to a parent character **/
	Character parentChar;
	
	boolean sharedStatus;
	
	boolean sharedBar;
	
	public boolean isBoss(){
		return this.parentChar != null;
	}
	
	public Character getParent(){
		return this.parentChar;
	}
	
	public void setParent(Character parent){
		this.parentChar = parent;
	}
	
	public BossCharacter (int id,Texture texture, Texture icon, AnimationNode animation, String name, 
			int health, int maxHealth, Color color,float speed, float castSpeed, 
			Action[] actions,int numSlots,Character parent){
		super(id,texture,icon,animation,name,health,maxHealth,color,speed,castSpeed,actions,numSlots);
		this.parentChar = parent;
	}
	
	public BossCharacter(Character c,Character parent){
		super(c);
		this.parentChar = parent;
		this.sharedStatus = true;
		this.sharedBar = true;
	}
	
	@Override
	public boolean isAlive() {
		return this.parentChar.health > 0;
	}
	
	@Override
	public void setHealth(int val){
		this.parentChar.setHealth(val);
	}
	
	@Override
	public int getHealth(){
		return this.parentChar.getHealth();
	}
	
	@Override
	void addEffect(Effect e){
		if (sharedStatus){
			this.parentChar.getEffects().add(e);
		}
		else{
			this.getEffects().add(e);
		}
	}
	
	@Override
	public ArrayList<Effect> getEffects(){
		if (sharedStatus){
			return this.parentChar.getEffects();
		}
		else{
			return this.getEffects();
		}
	}
	
	@Override
	void removeEffect(Effect e){
		if (sharedStatus){
			this.parentChar.getEffects().remove(e);
		}
		else{
			this.getEffects().remove(e);
		}
	}
	
	@Override
	/** update the state of the character and the bosses meta information
	 *  currently updates its cast moved
	 * **/
	public void update(){
		updateParentCastMoved();
		super.update();
	}
	
	public void updateParentCastMoved(){
		if (this.parentChar.castMoved == 0){
			this.parentChar.castMoved = this.castMoved;
		}
		else {
			this.parentChar.castMoved = Math.min(this.castMoved, this.parentChar.castMoved);
		}
	}
}
