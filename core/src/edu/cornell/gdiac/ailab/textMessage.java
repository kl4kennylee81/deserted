package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;

public class textMessage {
	
	public final static int SECOND = 60;
	
	float x_pos;
	float y_pos;
	Character character;

	String text;
	
	// duration is in frames that is (assuming 60 fps)
	int duration;
	
	public textMessage(String text,int d){
		this.text = text;
		this.duration = d;
		this.x_pos = -1;
		this.y_pos = -1;
		this.character = null;
	}
	
	public textMessage(String text,int d,Character c){
		this.text = text;
		this.duration = d;
		this.character = c;
		this.x_pos = -1;
		this.y_pos = -1;
	}
	
	public textMessage(String text,int d,float x,float y,Character c){
		this.text = text;
		this.duration = d;	
		this.x_pos = x;
		this.y_pos =y;
		this.character = c;

	}
	
	public void draw(GameCanvas canvas){
		if (duration <= 0){
			return;
		}
		if (this.x_pos!=-1 && this.y_pos!=-1){
			canvas.drawText(text, this.x_pos, this.y_pos, Color.BLACK);			
		}
		if (this.character != null){
			drawTextAboveUnit(canvas);
		}
	}
	
	private void drawTextAboveUnit(GameCanvas canvas){
		this.x_pos = 150 + 100*this.character.xPosition;
		this.y_pos = 120 + 100*this.character.yPosition;
		
		canvas.drawText(text,this.x_pos,this.y_pos,Color.BLACK);
	}
}
