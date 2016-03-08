package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

public class TextMessage {
	class Message {
		String text;
		int xPos;
		int yPos;
		int current;
		int duration;
		Color color;
		
		public Message(String text, int xPos, int yPos, int duration){
			this.text = text;
			this.xPos = xPos;
			this.yPos = yPos;
			this.current = 0;
			this.duration = duration;
			this.color = Color.BLACK;
		}
		
		public Message(String text, int xPos, int yPos, int duration, Color color){
			this.text = text;
			this.xPos = xPos;
			this.yPos = yPos;
			this.current = 0;
			this.duration = duration;
			this.color = color;
		}
		
		public float getRatio(){
			return ((float) current)/duration;
		}
	}
	
	/** List of messages */
	List<Message> damageMessages;
	/** List of interrupt messages (for now) */
	List<Message> otherMessages; 
	
	List<Message> tempSingles;
	
	public final static int SECOND = 60;
	
	public TextMessage(){
		this.damageMessages = new LinkedList<Message>();
		this.otherMessages = new LinkedList<Message>();
		this.tempSingles = new LinkedList<Message>();
	}
	
	public void addDamageMessage(String text, int xPos, int yPos, int duration, Color color){
		damageMessages.add(new Message(text,xPos,yPos,duration,color));
	}
	
	public void addOtherMessage(String text, int xPos, int yPos, int duration, Color color){
		otherMessages.add(new Message(text,xPos,yPos,duration,color));
	}
	
	public void addSingleTemp(int xPos, int yPos){
		tempSingles.add(new Message("lol",xPos,yPos,60));
	}

	public void draw(GameCanvas canvas){
		for (Message m : damageMessages){
			canvas.drawCenteredText(m.text, 150+100*m.xPos, 120+100*m.yPos+m.getRatio()*25, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 1.3f);
		}
		for (Message m : otherMessages){
			canvas.drawCenteredText(m.text, 150+100*m.xPos, 135+100*m.yPos+m.getRatio()*25, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 1.3f);
		}
		for (Message m : tempSingles){
			
			float xPos = 105+100*m.xPos;
			float yPos = 5+m.yPos*100;
			float ratio = m.getRatio();
			if (ratio < 0.25 || (ratio > 0.5 && ratio < 0.75)){
				for (int i = 0; i < 5; i++){
					if (i%2==1){
						canvas.drawBox(xPos, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+40, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+80, yPos+i*20, 10, 10, Color.RED);
					} else {
						canvas.drawBox(xPos+20, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+60, yPos+i*20, 10, 10, Color.RED);
					}
				}
			} else {
				for (int i = 0; i < 5; i++){
					if (i%2==1){
						canvas.drawBox(xPos+20, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+60, yPos+i*20, 10, 10, Color.RED);
					} else {
						canvas.drawBox(xPos, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+40, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+80, yPos+i*20, 10, 10, Color.RED);
					}
				}
			}
			
		}
	}
}
