package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class TextMessage {
	class Message {
		String text;
		int xPos;
		int yPos;
		int current;
		int duration;
		Color color;
		Action action;
		
		public Message(String text, int xPos, int yPos, int duration, Action action){
			this.text = text;
			this.xPos = xPos;
			this.yPos = yPos;
			this.current = 0;
			this.duration = duration;
			this.color = Color.BLACK;
			this.action = action;
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

	private static final int DAMAGE_OFFSET = 180;

	private static final int OTHER_OFFSET = 200;

	private static final int TIME_TRANSLATION_OFFSET = 25;
	
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

	public void draw(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		for (Message m : damageMessages){
			// FIXUP temp fix to get in relatively close to correct location
			float messageX = tileW*m.xPos - board.getTileWidth(canvas)/3;
			float messageY = tileH*m.yPos + DAMAGE_OFFSET + m.getRatio()*TIME_TRANSLATION_OFFSET;
			Coordinate c = board.offsetBoard(canvas, messageX, messageY);
			messageX = c.x;
			messageY = c.y;
			c.free();
			canvas.drawCenteredText(m.text, messageX, messageY, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 2f);
		}
		for (Message m : otherMessages){
			float messageX = tileW*m.xPos;
			float messageY = tileH*m.yPos + OTHER_OFFSET + m.getRatio()*TIME_TRANSLATION_OFFSET;
			Coordinate c = board.offsetBoard(canvas, messageX, messageY);
			messageX = c.x;
			messageY = c.y;
			c.free();
			canvas.drawCenteredText(m.text, messageX, messageY, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 1.3f);
		}
	}
}
