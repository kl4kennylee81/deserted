package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CompletionScreen {
	
	private static CompletionScreen instance = null;

	private static final float RELATIVE_X_POS = 0.2f;
	
	private static final float RELATIVE_Y_POS = 0.35f;
	
	private static final float RELATIVE_WIDTH = 0.6f;
	
	private static final float RELATIVE_HEIGHT = 0.6f;

	
	Texture victory;
	
	Texture defeat;
	
	Texture unlock;
	
	CompletionMenu cm;
	
	boolean isWin;
	
	boolean isActive;
	
	List<CharacterData> characters_unlocked;
	
	int skill_point;
	
	List<UnlockNotification> notifications;
	
	protected CompletionScreen(){
		cm = new CompletionMenu();
		
		notifications = new LinkedList<UnlockNotification>();
		characters_unlocked = new LinkedList<CharacterData>();
		int skill_point = 0;
	}
	
	public static CompletionScreen getInstance(){
		if (instance == null) {
			instance = new CompletionScreen();
		}
		return instance;
	}
	
	public void setVictory(Texture v){
		this.victory = v;
	}
	
	public void setDefeat(Texture d){
		this.defeat = d;
	}
	
	public void setVSelect(Texture vs){
		cm.setBackground(vs);
	}
	
	public void setUnlock(Texture un){
		unlock = un;
	}
	
	public void setHighlight(Texture h){
		cm.setHighlight(h);
	}
	
	public void setIsWin(boolean won){
		generateNotifications();
		isWin = won;
	}
	
	private void generateNotifications(){
		if (skill_point>0){
			if (skill_point == 1){
				String txt = "You have unlocked 1 skill point!";
				UnlockNotification un = new UnlockNotification(txt, unlock, null);
				notifications.add(un);
			}else{
				String txt = "You have unlocked " + skill_point + " skill points!";
				UnlockNotification un = new UnlockNotification(txt, unlock, null);
				notifications.add(un);
			}
		}
		
		for (CharacterData cd : characters_unlocked){
			Texture t = cd.getTexture();
			String txt = "You have unlocked a spirit!";
			UnlockNotification un = new UnlockNotification(txt, unlock, t);
			notifications.add(un);
		}
	}
	
	public CompletionMenu getMenu(){
		return cm;
	}
	
	public void draw(GameCanvas canvas) {
		isActive = true;
		float cWidth = canvas.getWidth();
		float cHeight = canvas.getHeight();
		
		drawImage(canvas, cWidth, cHeight);
		cm.draw(canvas, cWidth, cHeight);
		
		if (notifications.size() > 0){
			notifications.get(0).draw(canvas, cWidth, cHeight);
		}
		
	}
	
	public void drawImage(GameCanvas canvas, float w, float h){
		float x = RELATIVE_X_POS * w;
		float y = RELATIVE_Y_POS * h;
		float width = RELATIVE_WIDTH * w;
		float height = RELATIVE_HEIGHT * h;
		if (isWin){
			canvas.drawTexture(victory, x, y, width, height, Color.WHITE);
		}else{
			canvas.drawTexture(defeat, x, y, width, height, Color.WHITE);
		}
	}
	
	
	public void reset(){
		characters_unlocked.clear();
		skill_point = 0;
		notifications.clear();
	}
	
}
