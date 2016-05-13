package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.AnimationNode.CharacterState;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

public class CharacterSelect extends Menu{
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.12f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	public static final String CHARACTER_ID_STRING = "CharId:";
	
	private static final Color SELECTED_CHARACTER_COLOR = Color.GOLDENROD.cpy();
	
	private static final float RELATIVE_CHARACTER_Y = 0.7f;
	
	private static final float RELATIVE_CHARACTER_WIDTH = 0.08f;
	
	private static final float TEMP_HEIGHT = 0.2f;
	
	private static final float RELATIVE_DESCRIPTION_Y_POS = 0.25f;
	
	private static final float RELATIVE_DESCRIPTION_WIDTH = 0.12f;
	
	private static final float RELATIVE_DESCRIPTION_HEIGHT = 0.25f;
	
	private static final float RELATIVE_CHARACTERS_SIZE = 0.75f;
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");
	
	private static final int NULL_ID= -1;
	
	List<CharacterData> characters;
	int selectedCharacterId;
	HashMap<Integer,ArrayList<Action>> actions;
	List<Option> characterOptions;
	
	int[] charactersInPlay;
	
	Texture optionHighlight;
	
	public CharacterSelect(List<CharacterData> characters, List<Integer> charactersInPlay){
		this.characters = characters;
		this.selectedCharacterId = 0;
		actions = new HashMap<Integer,ArrayList<Action>>();
		
		loadCharacterInfo();
		loadActionInfo();
		
		characterOptions = new ArrayList<Option>();
		setCharactersInPlay(charactersInPlay);
		setOptions();
	}
	
	public boolean containsCharacterId(int charId){
		for (CharacterData cd : characters){
			if (cd.characterId == charId){
				return true;
			}
		}
		return false;
	}
	
	public void setCharactersInPlay(List<Integer> charactersInPlay){
		this.charactersInPlay = new int[charactersInPlay.size()];
		for (int i = 0; i < charactersInPlay.size(); i++){
			if (containsCharacterId(charactersInPlay.get(i))){
				this.charactersInPlay[i] = charactersInPlay.get(i);
			} else {
				this.charactersInPlay[i] = NULL_ID;
			}
		}
	}
	
	public void setOptions(){
		Option[] options = new Option[4+characters.size()];
		
		options[0] = new Option("Play","Play");
		options[0].setBounds(0.88f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		
		options[1] = new Option("Skill Tree","Skill Tree");
		options[1].setBounds(0.48f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		options[2] = new Option("Back","Back");
		options[2].setBounds(0.08f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[2].setColor(Constants.MENU_COLOR);
		
		String text;
		if (isCurrentlyInPlay()){
			text = "Unselect";
		} else {
			text = "Select";
		}
		
		int index = getSelectedCharacterIndex();
		
		if (index < 0 || index > characters.size()){
			System.out.println("FIX THIS - CharacterSelect");
		}
		
		float curCharacterRatio = (index+1f) * RELATIVE_CHARACTERS_SIZE / (characters.size()+1);
		
		options[3] = new Option(text,"Select");
		options[3].setBounds(curCharacterRatio-0.024f, 0.67f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[3].setColor(Constants.MENU_COLOR);
		
		characterOptions.clear();
		
		int i = 4;
		int j, k;
		float curX = 0.6f;
		float incrX = 0.1f;
		float curY = 0.68f;
		for (j = 0; j < characters.size(); j++){
			float relX = curX + incrX*j;
			CharacterData cd = characters.get(j);
			options[i] = new Option("",CHARACTER_ID_STRING+cd.characterId);
			
			float ratio = (j+1f) * RELATIVE_CHARACTERS_SIZE / (characters.size()+1);
			
			options[i].setBounds(ratio-RELATIVE_CHARACTER_WIDTH/2, RELATIVE_CHARACTER_Y, RELATIVE_CHARACTER_WIDTH,TEMP_HEIGHT);
			
			options[i].setImage(cd.getIcon());
			characterOptions.add(options[i]);
			i++;
		}
		
		this.options = options;
	}
	
	public int getSelectedCharacterIndex(){
		for (int i = 0; i < characters.size(); i++){
			CharacterData cd = characters.get(i);
			if (cd.characterId == selectedCharacterId){
				return i;
			}
		}
		return -1;
	}
	
	public void setHighlight(Texture t){
		this.optionHighlight = t;
	}
	
	public void setCharacter(int charId){
		if (charId == selectedCharacterId){
			return;
		}
		this.selectedCharacterId = charId;
		setOptions();
	}
	
	public boolean canLeave(){
		for (int n : charactersInPlay){
			if (n == NULL_ID){
				return false;
			}
		}
		return true;
	}

	public boolean isCurrentlyInPlay(){
		for (int i : charactersInPlay){
			if (i == selectedCharacterId){
				return true;
			}
		}
		return false;
	}
	
	public void selectCurrentCharacter(){
		if (isCurrentlyInPlay()){
			removeSelectedFromPlay();
		} else {
			addUnselectedToPlay();
		}
		setOptions();
	}
	
	public void removeSelectedFromPlay(){
		for (int i = 0; i < charactersInPlay.length; i++){
			int inPlayId = charactersInPlay[i];
			if (inPlayId == selectedCharacterId){
				charactersInPlay[i] = NULL_ID;
			}
		}
	}
	
	public void addUnselectedToPlay(){
		for (int i = 0; i < charactersInPlay.length; i++){
			int inPlayId = charactersInPlay[i];
			if (inPlayId == NULL_ID){
				charactersInPlay[i] = selectedCharacterId;
				break;
			}
		}
	}
	
	public void loadCharacterInfo(){
		try {
			ObjectLoader.getInstance().getCharacterInfo(characters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadActionInfo(){
		try {
			for (CharacterData cd : characters){
				actions.put(cd.characterId,ObjectLoader.getInstance().getSelectedActionList(cd.currentActions));
			}
		} catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CharacterData getCharacter(int id){
		for (CharacterData cd : characters){
			if (cd.characterId == id){
				return cd;
			} 
		}
		return null;
	}
	

	@Override
	public void draw(GameCanvas canvas) {
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		
		// TODO Auto-generated method stub
		for (int i=0;i<4;i++){
			float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvasW;
			float y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (i == 3){
				x -= canvas.width * 0.015f;
			}
			
			if (options[i].isSelected){
				if (optionHighlight != null){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
				}
			}
			options[i].draw(canvas);
		}
		
		TextureRegion temp = null;
		
		for (int i=0; i < characters.size(); i++){
			FilmStrip toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			Option charOption = characterOptions.get(i);
			if (toDraw == null){
				toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			}
			if (toDraw != null){
				temp = toDraw;
				//float heightToWidthRatio = toDraw.getRegionHeight()*1f/toDraw.getRegionWidth();
				//float relativeHeight = charOption.getWidth()*heightToWidthRatio;
				//charOption.height = relativeHeight;
				
				float width = charOption.getWidth()*canvas.width;
				float heightToWidthRatio = toDraw.getRegionHeight()*1f/toDraw.getRegionWidth();
				float height = heightToWidthRatio * width;
				charOption.height = height/canvas.height;
				height = charOption.height * canvas.height;
				float ratio = (i+1f) / (characters.size()+1);
				float x = charOption.xPosition * canvas.width;
				float y = charOption.yPosition * canvas.height;
				
				String optionKey = charOption.optionKey;
				if (charOption.optionKey.contains(CHARACTER_ID_STRING)){
					String charIdString = optionKey.substring(CHARACTER_ID_STRING.length());
					int charId = Integer.parseInt(charIdString);
					if (charId == selectedCharacterId){
						canvas.draw(toDraw, Color.WHITE,x,y,width,height);
					} else {
						canvas.draw(toDraw, Color.GRAY,x,y,width,height);
					}
				}
			}
		}

		ArrayList<Action> actionsToDraw = actions.get(selectedCharacterId);
		
		float relative_offset = 0.065f;
		
		float i = 1;
		
		for (Action action : actionsToDraw){
			float middle_x = canvasW * i * RELATIVE_CHARACTERS_SIZE / (actionsToDraw.size()+1);
			float descript_y = RELATIVE_DESCRIPTION_Y_POS *canvasH;
			float descript_width = RELATIVE_DESCRIPTION_WIDTH *canvasW;
			float descript_height = RELATIVE_DESCRIPTION_HEIGHT * canvasH;
			float descript_x = middle_x - descript_width/2;
			canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y, descript_width, descript_height, Color.WHITE);
			canvas.drawCenteredTexture(action.menuIcon, middle_x, descript_y+descript_height,50,50, Color.WHITE);
			//canvas.drawCenteredText(action, descript_x, descript_y, Color.WHITE);
			float name_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*3)*canvasH;
			canvas.drawCenteredText(action.name, middle_x,name_y, Color.WHITE);
			float dmg_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*2)*canvasH;
			canvas.drawCenteredText("DMG: "+action.damage, middle_x,dmg_y, Color.WHITE);
			float cost_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*1)*canvasH;
			canvas.drawCenteredText("COST: "+action.cost, middle_x,cost_y, Color.WHITE);
			i++;
		}
		
		float ratio = 0.5f;
		
		float descript_x = canvasW * (RELATIVE_CHARACTERS_SIZE + ((1-ratio) /2) * (1-RELATIVE_CHARACTERS_SIZE) - 0.05f);
		float descript_y1 = canvasH * (0.3f);
		float descript_y2 = canvasH * (0.6f);
		float descript_width = canvasW * (1 - RELATIVE_CHARACTERS_SIZE) * ratio;
		float descript_height = canvasH * (0.2f);
		
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y1, descript_width, descript_height, Color.GOLD);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y2, descript_width, descript_height, Color.GOLD);
		
		CharacterData cd0 = getCharacter(charactersInPlay[0]);
		CharacterData cd1 = getCharacter(charactersInPlay[1]);
		
		float iconX = canvasW * (RELATIVE_CHARACTERS_SIZE + ((1-ratio) /2) * (1-RELATIVE_CHARACTERS_SIZE) - 0.035f);
		float iconWidth = canvasW * (1 - RELATIVE_CHARACTERS_SIZE) * ratio* 0.7f;
		float iconHeight = canvasH * (0.15f);
		if (cd0 != null){
			float iconY = canvasH * (0.625f);
			canvas.drawTexture(cd0.getIcon(), iconX, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd1 != null){
			float iconY = canvasH * (0.325f);
			canvas.drawTexture(cd1.getIcon(), iconX, iconY, iconWidth, iconHeight, Color.WHITE);
		}
	}

}
