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
			return Math.max(0,((float) current)/duration/2);
		}
	}
	
	/** List of messages */
	List<Message> damageMessages;
	/** List of interrupt messages (for now) */
	List<Message> otherMessages; 
	
	public final static int SECOND = 60;
	
	public TextMessage(){
		this.damageMessages = new LinkedList<Message>();
		this.otherMessages = new LinkedList<Message>();
	}
	
	public void addDamageMessage(String text, int xPos, int yPos, int duration, Color color){
		damageMessages.add(new Message(text,xPos,yPos,duration,color));
	}
	
	public void addOtherMessage(String text, int xPos, int yPos, int duration, Color color){
		otherMessages.add(new Message(text,xPos,yPos,duration,color));
	}

	public void draw(GameCanvas canvas){
		for (Message m : damageMessages){
			canvas.drawCenteredText(m.text, 150+100*m.xPos, 120+100*m.yPos+m.getRatio()*50, m.color.cpy().lerp(Color.CLEAR, m.getRatio()), 1.3f);
		}
		for (Message m : otherMessages){
			canvas.drawCenteredText(m.text, 150+100*m.xPos, 135+100*m.yPos+m.getRatio()*50, m.color.cpy().lerp(Color.CLEAR, m.getRatio()), 1.3f);
		}
	}
}
