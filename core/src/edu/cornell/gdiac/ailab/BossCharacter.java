package edu.cornell.gdiac.ailab;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class BossCharacter extends Character{
	/** pointer to a parent character **/
	Character parentChar;
	
	// if share health is true we use the parents health otherwise subtract from own health
	boolean shareHealth;
	
	// this is for drawing if its true you draw it on the parents bar
	boolean shareBar;
	
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
		
		// for now set the action bar of this character to the action bar of the parent.
		// this is not a copy it will thus be shared between units of the same boss.
		this.actionBar = this.parentChar.actionBar;
		this.shareBar = true;
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
		this.parentChar.getEffects().add(e);
	}
	
	@Override
	ArrayList<Effect> getEffects(){
		return this.parentChar.getEffects();
	}
	
	@Override
	void removeEffect(int i){
		this.parentChar.getEffects().remove(i);
	}
	
}
