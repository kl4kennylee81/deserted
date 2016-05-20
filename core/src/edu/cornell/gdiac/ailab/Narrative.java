package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.AnimationNode.CharacterState;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

public class Narrative {
	private static final Color FADED_COLOR = Color.GRAY;
	
	private static final float CHARACTER_WIDTH = 0.15f;
	private static final float CHARACTER_Y = 0.3f;
	
	private static final Texture TEXT_BACKGROUND = new Texture("models/description_background.png");
	
	ArrayList<Panel> panels = new ArrayList<Panel>();
	int index;
	
	public class Panel {
		ArrayList<Character> leftChars;
		ArrayList<Character> rightChars;
		ArrayList<Integer> highlightChars;
		
		String text;	
		Texture backgroundTexture;
	}
	
	public Narrative(String stringFileName){
		panels = new ArrayList<Panel>();
		index = 0;
		
		HashMap<Integer, HashMap<String, Object>> narrativeData = null;
		try {
			narrativeData = ObjectLoader.getInstance().loadNarrative(stringFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Integer> charIDs = new ArrayList<Integer>();
		
		for (Integer i : narrativeData.keySet()){
			HashMap<String, Object> panelData = narrativeData.get(i);
			Object leftChars = panelData.get("leftCharacters");
			if (leftChars != null){
				for (Integer j : (ArrayList<Integer>) leftChars){
					if (!charIDs.contains(j)){
						charIDs.add(j);
					}
				}
			}
			Object rightChars = panelData.get("rightCharacters");
			if (rightChars != null){
				for (Integer j : (ArrayList<Integer>) rightChars){
					if (!charIDs.contains(j)){
						charIDs.add(j);
					}
				}
			}
		}
		
		HashMap<Integer, Character> characters = null;
		try {
			characters = ObjectLoader.getInstance().getCharacterMap(charIDs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Integer i : narrativeData.keySet()){
			HashMap<String, Object> panelData = narrativeData.get(i);
			Panel p = new Panel();
			p.leftChars = new ArrayList<Character>();
			Object leftChars = panelData.get("leftCharacters");
			if (leftChars != null){
				for (Integer j : (ArrayList<Integer>) leftChars){
					p.leftChars.add(characters.get(j));
				}
			}
			p.rightChars = new ArrayList<Character>();
			Object rightChars = panelData.get("rightCharacters");
			if (rightChars != null){
				for (Integer j : (ArrayList<Integer>) rightChars){
					p.rightChars.add(characters.get(j));
				}
			}
			
			Object highlightChars = panelData.get("highlightCharacters");
			p.highlightChars = (ArrayList<Integer>) highlightChars;
			if (p.highlightChars == null){
				p.highlightChars = new ArrayList<Integer>();
			}
			
			p.text = (String) panelData.get("text");
			String backgroundTextureName = (String)panelData.get("background");
			if (backgroundTextureName != null){
				p.backgroundTexture = new Texture(backgroundTextureName);
			}
			panels.add(p);
		}
	}
	
	public void draw(GameCanvas canvas){
		if (index < 0 || index >= panels.size()){
			return;
		}
		Panel toShow = panels.get(index);
		if (toShow.backgroundTexture != null){
			canvas.setBackground(toShow.backgroundTexture);
		}
		for (int i = 0; i < toShow.leftChars.size(); i++){
			Texture toDraw = toShow.leftChars.get(i).texture;
			if (toDraw != null){
				int width = (int) (CHARACTER_WIDTH * canvas.width);
				float heightToWidthRatio = toDraw.getHeight()*1f/toDraw.getWidth();
				int height = (int) (heightToWidthRatio * width);
				float ratio = (i+1f)/(toShow.leftChars.size()+1);
				float middleX = ratio * 0.5f * canvas.width;
				float x = middleX - width/2;
				float y = CHARACTER_Y * canvas.height;
				Color col = FADED_COLOR;
				if (toShow.highlightChars.contains(toShow.leftChars.get(i).id)){
					col = Color.WHITE;
				}
				canvas.drawCharacter(toDraw, x, y, width, height, col,true);
			}
		}
		for (int i = 0; i < toShow.rightChars.size(); i++){
			Texture toDraw = toShow.rightChars.get(i).texture;
			if (toDraw != null){
				int width = (int) (CHARACTER_WIDTH * canvas.width);
				float heightToWidthRatio = toDraw.getHeight()*1f/toDraw.getWidth();
				int height = (int) (heightToWidthRatio * width);
				float ratio = (i+1f)/(toShow.rightChars.size()+1);
				float middleX = (ratio * 0.5f + 0.5f) * canvas.width;
				float x = middleX - width/2;
				float y = CHARACTER_Y * canvas.height;
				Color col = FADED_COLOR;
				if (toShow.highlightChars.contains(toShow.rightChars.get(i).id)){
					col = Color.WHITE;
				}
				canvas.drawCharacter(toDraw, x, y, width, height, col,false);
			}
		}
		
		float x = 0.05f * canvas.width;
		float y = 0.05f * canvas.height;
		canvas.draw(TEXT_BACKGROUND, Color.WHITE,x,y,canvas.width*0.9f,canvas.height*0.2f);
		if (toShow.text != null){
			x += 0.03f * canvas.width;
			y += 0.15f * canvas.height;
			float textWidth = canvas.width - x*2;
			//canvas.drawText(toShow.text, x, y, Color.WHITE);
			canvas.drawWrapText(toShow.text, x, y, textWidth, Color.WHITE);
		}
	}
	
	
}
