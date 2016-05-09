package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CompletionScreen {
	
	private static CompletionScreen instance = null;

	private static final float RELATIVE_X_POS = 0.3f;
	
	private static final float RELATIVE_Y_POS = 0.3f;
	
	private static final float RELATIVE_WIDTH = 0.4f;
	
	private static final float RELATIVE_HEIGHT = 0.4f;
	
	private static final float TOP_X_POS = 0.5f;
	
	private static final float TOP_Y_POS = 0.75f;
	
	private static final float BOTTOM_X_POS = 0.5f;
	
	private static final float BOTTOM_Y_POS = 0.3f;
	
	private static final float CHEST_X_POS = 0.75f;
	
	private static final float CHEST_Y_POS = 0.5f;
	
	private static final float CHEST_WIDTH = 0.2f;
	
	private static final float CHEST_HEIGHT = 0.2f;
	
	private static final float STAR_X_POS = 0.1f;
	
	private static final float STAR_Y_POS = 0.5f;
	
	private static final float STAR_WIDTH = 0.2f;
	
	private static final float STAR_HEIGHT = 0.2f;
	
	Texture image;
	
	Texture star;
	
	Texture chest;
	
	List<CharacterData> characters_unlocked;
	
	int skill_point;
	
	private String topText;
	
	private String bottomText;
	
	protected CompletionScreen(){
		bottomText = "Press Enter to Continue or R to Return";
		characters_unlocked = new LinkedList<CharacterData>();
		int skill_point = 0;
	}
	
	public static CompletionScreen getInstance(){
		if (instance == null) {
			instance = new CompletionScreen();
		}
		return instance;
	}
	
	public void setImage(Texture im){
		this.image = im;
	}
	
	public void setStar(Texture st){
		star = st;
	}
	
	public void setChest(Texture ch){
		chest = ch;
	}
	
	public void setIsWin(boolean newVal){
		if (newVal){
			topText = "Victory!";
		}else{
			topText = "Try Again!";
		}
	}
	
	
	public void draw(GameCanvas canvas) {
		drawTopText(canvas);
		drawImage(canvas);
		drawBottomText(canvas);
		
		if (skill_point > 0){
			drawStar(canvas);
		}
		if (characters_unlocked.size() > 0){
			drawChest(canvas);
		}
	}
	
	public void drawImage(GameCanvas canvas){
		float x = RELATIVE_X_POS * canvas.getWidth();
		float y = RELATIVE_Y_POS * canvas.getHeight();
		float width = RELATIVE_WIDTH * canvas.getWidth();
		float height = RELATIVE_HEIGHT * canvas.getHeight();
		
		canvas.drawTexture(image, x, y, width, height, Color.WHITE);
	}
	
	public void drawTopText(GameCanvas canvas) {
		float x = TOP_X_POS * canvas.getWidth();
		float y = TOP_Y_POS * canvas.getHeight();
		canvas.drawCenteredText(topText, x, y, Color.WHITE, 1.2f);
	}
	
	public void drawBottomText(GameCanvas canvas) {
		float x = BOTTOM_X_POS * canvas.getWidth();
		float y = BOTTOM_Y_POS * canvas.getHeight();
		canvas.drawCenteredText(bottomText, x, y, Color.WHITE, 1.2f);
	}
	
	private void drawStar(GameCanvas canvas){
		float x = STAR_X_POS * canvas.getWidth();
		float y = STAR_Y_POS * canvas.getHeight();
		float width = STAR_WIDTH * canvas.getWidth();
		float height = STAR_HEIGHT * canvas.getHeight();
		
		canvas.drawTexture(star, x, y, width, height, Color.WHITE);
		canvas.drawText(String.valueOf(skill_point), x, y, Color.WHITE);
	}
	
	private void drawChest(GameCanvas canvas){
		float x = CHEST_X_POS * canvas.getWidth();
		float y = CHEST_Y_POS * canvas.getHeight();
		float width = CHEST_WIDTH * canvas.getWidth();
		float height = CHEST_HEIGHT * canvas.getHeight();
		
		canvas.drawTexture(chest, x, y, width, height, Color.WHITE);
		CharacterData cd = characters_unlocked.get(0);
		canvas.drawTexture(cd.getIcon(), x, y, width, height, Color.WHITE);
	}
	
}
