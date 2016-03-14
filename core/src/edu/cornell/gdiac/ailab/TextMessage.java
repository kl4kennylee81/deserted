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

	private static final int DAMAGE_OFFSET = 300;

	private static final int OTHER_OFFSET = 320;

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
	
	public void addSingleTemp(int xPos, int yPos){
		tempSingles.add(new Message("lol",xPos,yPos,120));
	}

	public void draw(GameCanvas canvas,GridBoard board){
		float tileW = board.getTileWidth(canvas);
		float tileH = board.getTileHeight(canvas);
		for (Message m : damageMessages){
			float messageX = tileW/2 + tileW*m.xPos;
			float messageY = tileH*m.yPos + DAMAGE_OFFSET + m.getRatio()*TIME_TRANSLATION_OFFSET;
			canvas.drawCenteredText(m.text, messageX, messageY, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 2f);
			//canvas.drawCenteredText(m.text, 75+150*m.xPos, 300+100*m.yPos+m.getRatio()*25, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 2f);
		}
		for (Message m : otherMessages){
			float messageX = tileW/2 + tileW*m.xPos;
			float messageY = tileH*m.yPos + OTHER_OFFSET + m.getRatio()*TIME_TRANSLATION_OFFSET;
			canvas.drawCenteredText(m.text, messageX, messageY, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 1.3f);
			//canvas.drawCenteredText(m.text, 75+150*m.xPos, 320+100*m.yPos+m.getRatio()*25, m.color.cpy().lerp(Color.CLEAR, m.getRatio()/2), 1.3f);
		}
		for (Message m : tempSingles){
			
			float xPos = 5+m.xPos*tileW;
			float yPos = 5+m.yPos*tileH;
			float ratio = m.getRatio();
			if (ratio < 0.25 || (ratio > 0.5 && ratio < 0.75)){
				for (int i = 0; i < 5; i++){
					if (i%2==1){
						canvas.drawBox(xPos, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+40, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+80, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+120, yPos+i*20, 10, 10, Color.RED);
					} else {
						canvas.drawBox(xPos+20, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+60, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+100, yPos+i*20, 10, 10, Color.RED);
					}
				}
			} else {
				for (int i = 0; i < 5; i++){
					if (i%2==1){
						canvas.drawBox(xPos+20, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+60, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+100, yPos+i*20, 10, 10, Color.RED);
					} else {
						canvas.drawBox(xPos, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+40, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+80, yPos+i*20, 10, 10, Color.RED);
						canvas.drawBox(xPos+120, yPos+i*20, 10, 10, Color.RED);
					}
				}
			}
			
		}
	}
}
