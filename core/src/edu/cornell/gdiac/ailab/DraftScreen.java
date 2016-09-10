package edu.cornell.gdiac.ailab;

import java.awt.Canvas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.cornell.gdiac.ailab.AnimationNode.CharacterState;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

public class DraftScreen extends Menu{
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.06f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.03f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	public static final String CHARACTER_ID_STRING = "CharId:";
	
	private static final Color SELECTED_CHARACTER_COLOR = Color.GOLDENROD.cpy();
	
	private static final float RELATIVE_CHARACTER_Y = 0.73f;
	
	private static final float RELATIVE_CHARACTER_WIDTH = 0.08f;
	
	private static final float TEMP_HEIGHT = 0.2f;
	
	private static final float RELATIVE_DESCRIPTION_Y_POS = 0.1f;
	
	private static final float RELATIVE_DESCRIPTION_WIDTH = 0.35f;
	
	private static final float DRAFT_SLOT_WIDTH = 0.125f;
	
	
	private static final float RELATIVE_DESCRIPTION_HEIGHT = 0.45f;
	
	private static final float RELATIVE_CHARACTERS_SIZE = 0.75f;
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");
	
	private static final float PLAYER1_CHARACTER_LIST_X_OFFSET = 0.075f;
	
	private static final float SELECT_BUTTON_WIDTH = 0.075f;
				
	private static final int NULL_ID= -1;
	
	float lerpVal = 0;
	boolean increasing = true;
	
	private static final Texture[] BAR_COLORS = new Texture [] {
		new Texture(Constants.BLUE_BAR),
		new Texture(Constants.GREEN_BAR),
		new Texture(Constants.YELLOW_BAR),
		new Texture(Constants.RED_BAR),
		new Texture(Constants.LIGHT_BLUE_BAR)
	};
	
	private static final String[] stats = new String [] {
		"Effects",
		"Recharge Speed",
		"Cast Speed",
		"Defense",
		"Attack"
	};
	
	private static HashMap<Integer, ArrayList<Integer>> map; 
	private static final Texture LOGO = new Texture(Constants.LEVEL_SELECT_REG);
	
	Characters characters;
	Character parent1;
	Character parent2;
	int selectedCharacterId;
	HashMap<Integer,ArrayList<Action>> actions;
	List<Option> characterOptions;
	HashMap<Integer,Option> charIdToOptions;
	
	int[] player1Characters;
	int[] player2Characters;
	HashSet<Integer> notAvailable;
	
	Texture optionHighlight;
		
	String from;
	String to;
	boolean isFirst;
	
	public DraftScreen(Characters characters, String from, String to, boolean isFirst){
		this.characters = characters;
		
		map = new HashMap<Integer, ArrayList<Integer>>();
		map.put(20, new ArrayList<Integer>(Arrays.asList(1,4,4,2,1)));
		map.put(21, new ArrayList<Integer>(Arrays.asList(1,1,4,1,4)));
		map.put(22, new ArrayList<Integer>(Arrays.asList(1,4,1,1,4)));
		map.put(23, new ArrayList<Integer>(Arrays.asList(2,3,1,2,3)));
		map.put(24, new ArrayList<Integer>(Arrays.asList(3,2,2,4,2)));
		map.put(25, new ArrayList<Integer>(Arrays.asList(4,1,2,4,1)));

		this.selectedCharacterId = characters.get(0).id;
		
		this.charIdToOptions = new HashMap<Integer,Option>();
		actions = new HashMap<Integer,ArrayList<Action>>();
		
		loadCharacterInfo();
		loadActionInfo();
		
		characterOptions = new ArrayList<Option>();
		this.player1Characters = new int[2];
		this.player2Characters = new int[2];
		notAvailable = new HashSet<Integer>();
		for(int i = 0; i < 2; i++){
			this.player1Characters[i] = NULL_ID;
			this.player2Characters[i] = NULL_ID;
		}
		setOptions();
		
		this.from = from;
		this.to = to;
		this.isFirst = isFirst;
	}
	
	public boolean containsCharacterId(int charId){
		for (Character cd : characters){
			if (cd.id == charId){
				return true;
			}
		}
		return false;
	}
	
	public void setOptions(){
		Option[] options = new Option[1+characters.size()];
		
		String text;
	    text = "Select";
		
		int index = getSelectedCharacterIndex();
		
		if (index < 0 || index > characters.size()){
			System.out.println("FIX THIS - CharacterSelect");
		}
		
		float leftRatio = PLAYER1_CHARACTER_LIST_X_OFFSET + DRAFT_SLOT_WIDTH + 0.025f;
		float curCharacterRatio = (index+1f) * RELATIVE_CHARACTERS_SIZE / (characters.size()+1);
		
		options[0] = new Option(text,"Select");
		options[0].setBounds(0.5f - (SELECT_BUTTON_WIDTH/2), RELATIVE_DESCRIPTION_Y_POS + 0.04f, SELECT_BUTTON_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Color.WHITE);

		
		characterOptions.clear();
		
		int i = 1;
		int j;
		float current_x_pos = leftRatio;
		float widthToUse = 1 - leftRatio - leftRatio;
		float characterWidth = widthToUse / characters.size();
		float margin = characterWidth * 0.12f;
		characterWidth -= (margin * 2);
		
		for (j = 0; j < characters.size(); j++){
			Character cd = characters.get(j);
			options[i] = new Option("",CHARACTER_ID_STRING+cd.id);
			current_x_pos += margin;
			options[i].setBounds(current_x_pos, RELATIVE_CHARACTER_Y, characterWidth, TEMP_HEIGHT);
			options[i].setImage(cd.icon);
			characterOptions.add(options[i]);
			
			charIdToOptions.put(cd.id,options[i]);
			
			i++;
			current_x_pos += (characterWidth + margin);
		}
		
		this.options = options;
	}
	
	public int getSelectedCharacterIndex(){
		for (int i = 0; i < characters.size(); i++){
			Character cd = characters.get(i);
			if (cd.id == selectedCharacterId){
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
	
	public void setSelectedToFirstAvailable(Integer justSelected){
		if (justSelected == this.selectedCharacterId){
			for (Character c : this.characters){
				if (!this.notAvailable.contains(c.id)){
					this.selectedCharacterId = c.id;
				}
			}
		}
	}
	
	public boolean doneDrafting(){
		return canLeave(1) && canLeave(2);
	}
	
	public boolean canLeave(int playerNum){
		int[] array = playerNum == 1? player1Characters : player2Characters;
		for (int n : array){
			if (n == NULL_ID){
				return false;
			}
		}
		return true;
	}

	public boolean isCurrentlyInPlay(int playerNum){
		int[] array = playerNum == 1? player1Characters : player2Characters;
		for (int i : array){
			if (i == selectedCharacterId){
				return true;
			}
		}
		return false;
	}
	
	public boolean selectCurrentCharacter(int playerNum){
		boolean toReturn = addUnselectedToPlay(playerNum);
		setOptions();		
		return toReturn;
	}
	
	public void setPlayerCharacter(int playernum, int charId){
		int prevSelectedCharacter = selectedCharacterId;
		selectedCharacterId = charId;
		selectCurrentCharacter(playernum);
		selectedCharacterId = prevSelectedCharacter;
	}
	
	public void removeSelectedFromPlay(int playerNum){
		int[] array = playerNum == 1? player1Characters : player2Characters;
		for (int i = 0; i < array.length; i++){
			int inPlayId = array[i];
			if (inPlayId == selectedCharacterId){
				array[i] = NULL_ID;
			}
		}
	}
	
	public boolean addUnselectedToPlay(int playerNum){
		
		if(notAvailable.contains(selectedCharacterId)){
			return false;
		}
		int[] array = playerNum == 1? player1Characters : player2Characters;
		for (int i = 0; i < array.length; i++){
			int inPlayId = array[i];
			if (inPlayId == NULL_ID){
				array[i] = selectedCharacterId;
				System.out.println(selectedCharacterId);
				notAvailable.add(selectedCharacterId);
				return true;
			}
		}
		return false;
	}
	
	public List<GameSaveState.CharacterData> translate(Characters characters){
		List<GameSaveState.CharacterData> datas = new LinkedList<GameSaveState.CharacterData>();
		for(Character ch: characters){
			GameSaveState.CharacterData data = new GameSaveState.CharacterData();
			data.setAnimation(ch.animation);
			data.setTexture(ch.texture);
			data.setIconTexture(ch.icon);
			data.bigIcon = ch.bigIcon;
			data.name = ch.name;
			data.characterId = ch.id;
			datas.add(data);
		}
		return datas;
	}
	
	public void loadCharacterInfo(){
		try {
			ObjectLoader.getInstance().getCharacterInfo(translate(characters));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadActionInfo(){
//		try {
//			for (Character cd : characters){
//				actions.put(cd.id,ObjectLoader.getInstance().getSelectedActionList(cd.availableActions));
//			}
//		} catch (IOException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public Character getCharacter(int id){
		for (Character cd : characters){
			if (cd.id == id){
				return cd;
			} 
		}
		return null;
	}
	
	public void drawWaitMessage(GameCanvas canvas, boolean turn){
		if(turn) canvas.drawCenteredText("Wait for opponent to select!", canvas.width/2f, canvas.height*(RELATIVE_CHARACTER_Y-0.01f), Color.WHITE);
		else canvas.drawCenteredText("Select a character!", canvas.width/2f, canvas.height*(RELATIVE_CHARACTER_Y- 0.01f), Color.WHITE);
	}

	public void draw(GameCanvas canvas) {
		
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		
		// TODO Auto-generated method stub
	
		
		TextureRegion temp = null;
		

		ArrayList<Action> actionsToDraw = actions.get(selectedCharacterId);
		
		float relative_offset = 0.065f;
		

		float ratio = 0.5f;
		
		float descript1_x = canvasW * PLAYER1_CHARACTER_LIST_X_OFFSET;
		float descript_y1 = canvasH * (0.1f);
		float descript_y2 = canvasH * (0.35f);
		float descript_width = DRAFT_SLOT_WIDTH * canvas.width;
		float descript2_x = canvasW - descript1_x - descript_width;
		float descript_height = canvasH * (0.2f);
		
		float label_height = descript_height / 2;
		float label_y = canvasH * (0.575f);
		
		String s1 = isFirst ? to : from;
		String s2 = isFirst ? from : to;
		canvas.drawTexture(LOGO, descript1_x - (0.02f * canvas.width), label_y, descript_width + (0.04f * canvas.width), label_height, Color.WHITE);
		canvas.drawText(s1, descript1_x + (0.033f * canvas.width), label_y + (0.035f * canvas.width), Color.BLACK);
		canvas.drawTexture(LOGO, descript2_x - (0.02f * canvas.width), label_y, descript_width + (0.04f * canvas.width), label_height, Color.WHITE);
		canvas.drawText(s2, descript2_x + (0.033f * canvas.width), label_y + (0.035f * canvas.width), Color.BLACK);

		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript1_x, descript_y1, descript_width, descript_height, Color.GRAY);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript1_x, descript_y2, descript_width, descript_height, Color.GRAY);
		
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript2_x, descript_y1, descript_width, descript_height, Color.GRAY);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript2_x, descript_y2, descript_width, descript_height, Color.GRAY);
		
		Character cd0 = getCharacter(player1Characters[0]);
		Character cd1 = getCharacter(player1Characters[1]);
		
		Character cd2 = getCharacter(player2Characters[0]);
		Character cd3 = getCharacter(player2Characters[1]);
				
		float iconX1 = canvasW * (PLAYER1_CHARACTER_LIST_X_OFFSET + 0.015f);
		float iconWidth = canvasW * (1 - RELATIVE_CHARACTERS_SIZE) * ratio* 0.7f;
		float iconX2 = canvasW - iconX1 - iconWidth;
		float iconHeight = canvasH * (0.15f);
		if (cd0 != null){
			float iconY = canvasH * (0.375f);
			canvas.drawTexture(cd0.bigIcon, iconX1, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd1 != null){
			float iconY = canvasH * (0.125f);
			canvas.drawTexture(cd1.bigIcon, iconX1, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd2 != null){
			float iconY = canvasH * (0.375f);
			canvas.drawTexture(cd2.bigIcon, iconX2, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd3 != null){
			float iconY = canvasH * (0.125f);
			canvas.drawTexture(cd3.bigIcon, iconX2, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		
				
		float middle_x = canvas.width / 2;
		float descript_y = RELATIVE_DESCRIPTION_Y_POS * canvas.height;
		descript_width = RELATIVE_DESCRIPTION_WIDTH * canvas.width;
		descript_height = RELATIVE_DESCRIPTION_HEIGHT * canvas.height;
		float descript_x = middle_x - (descript_width/2);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y, descript_width, descript_height, Color.GRAY);
				
		Character selected_char = this.getCharacter(selectedCharacterId);
		FilmStrip characterImg = this.getCharacter(selectedCharacterId).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
		Option option = this.charIdToOptions.get(this.selectedCharacterId);
		if (characterImg == null){
			characterImg = this.getCharacter(selectedCharacterId).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
		}
		if (characterImg != null){
			temp = characterImg;
			//float heightToWidthRatio = toDraw.getRegionHeight()*1f/toDraw.getRegionWidth();
			//float relativeHeight = charOption.getWidth()*heightToWidthRatio;
			//charOption.height = relativeHeight;
			
			float width = option.getWidth()*canvas.width;
			float heightToWidthRatio = characterImg.getRegionHeight()*1f/characterImg.getRegionWidth();
			float height = heightToWidthRatio * width;
			option.height = height/canvas.height;
			height = option.height * canvas.height;
			height *= 0.8f;
			width *= 0.8f;
			float x = middle_x - (width / 2);
			float y = (RELATIVE_DESCRIPTION_Y_POS + 0.4f) * canvas.height;
			canvas.draw(characterImg, Color.WHITE,x,y,width,height);

			canvas.drawCenteredText(selected_char.name, x+width/2, (RELATIVE_DESCRIPTION_Y_POS + 0.4f)*canvas.height, Color.WHITE);
			
			float increment = 0.32f/5f;
			float ypos = RELATIVE_DESCRIPTION_Y_POS + 0.05f;
			ArrayList<Integer> vals = map.get(selectedCharacterId);
			for(int i = 0; i < 5; i++){
				float currenty = (ypos + (increment * (0.23f))) * canvas.height;
				float heighty = (canvas.height * (0.54f * increment));
				float widthx = descript_width  / 7.4f;
				float posx = middle_x - (canvas.width * 0.05f);
				canvas.drawText(stats[i], descript_x + (0.02f * canvas.width), currenty+(0.03f * canvas.height), Color.WHITE);
				int numBars = vals.get(i);
				for(int j = 0; j < numBars; j++){
					canvas.drawTexture(BAR_COLORS[i], posx, currenty, widthx, heighty, Color.WHITE);
					posx += (widthx + 3);
				}
				ypos += increment;				
			}
			
		}
			/*canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y, descript_width, descript_height, Color.WHITE);
			canvas.drawCenteredTexture(action.menuIcon, middle_x, descript_y+descript_height,50,50, Color.WHITE);
			//canvas.drawCenteredText(action, descript_x, descript_y, Color.WHITE);
			float name_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*3)*canvasH;
			canvas.drawCenteredText(action.name, middle_x,name_y, Color.WHITE);
			float dmg_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*2)*canvasH;
			canvas.drawCenteredText("DMG: "+action.damage, middle_x,dmg_y, Color.WHITE);
			float cost_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*1)*canvasH;
			canvas.drawCenteredText("COST: "+action.cost, middle_x,cost_y, Color.WHITE);*/

		
		for (int j=0; j < characters.size(); j++){
			Texture toDraw = characters.get(j).bigIcon;
			Option charOption = characterOptions.get(j);
			if (toDraw == null){
				toDraw = characters.get(j).bigIcon;
			}
			if (toDraw != null){
				//float heightToWidthRatio = toDraw.getRegionHeight()*1f/toDraw.getRegionWidth();
				//float relativeHeight = charOption.getWidth()*heightToWidthRatio;
				//charOption.height = relativeHeight;
				
				float width = charOption.getWidth()*canvas.width;
//				float height = charOption.height * canvas.height;
			
				float heightToWidthRatio = toDraw.getHeight()*1f/toDraw.getWidth();
				float height = heightToWidthRatio * width;
				charOption.height = height/canvas.height;
				height = charOption.height * canvas.height;
				float x = charOption.xPosition * canvas.width;
				float y = charOption.yPosition * canvas.height;
				
				//canvas.drawCenteredText(characters.get(j).name, x+width/2, 0.9f*canvas.height, Color.WHITE);
				
				String optionKey = charOption.optionKey;
				if (charOption.optionKey.contains(CHARACTER_ID_STRING)){
					String charIdString = optionKey.substring(CHARACTER_ID_STRING.length());
					int charId = Integer.parseInt(charIdString);
					if (!notAvailable.contains(charId)){
						canvas.drawTexture(toDraw,x,y,width,height, Color.WHITE);
					} else {
						canvas.drawTexture(toDraw,x,y,width,height, Color.GRAY);
					}
				}
			}
		}
		for (int i=0;i<1;i++){
			float x = options[i].getX(canvas);
			float y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (options[i].isSelected){
				if (optionHighlight != null){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
				}
			}
			Color col = options[i].color;
			if (options[i].isSelected) {
				col = Color.BLACK;
			}
//			canvas.drawTexture(new Texture(Constants.DESCRIPTION_BOX_TEXTURE), x, y - height/2, width, height * 1.5f,Color.WHITE);
			if (increasing){
				lerpVal += Constants.LERP_RATE/1.5;
				if (lerpVal >= 0.5){
					increasing = false;
				}
			} else {
				lerpVal -= Constants.LERP_RATE/1.5;
				if (lerpVal <= 0){
					increasing = true;
				}
			}
			Color chosenColor = Color.DARK_GRAY.cpy();
			chosenColor = chosenColor.lerp(Color.WHITE, lerpVal);
		    canvas.drawCenteredText(options[i].text, x+width/2, y+height*3/4, chosenColor);
		    
		}
	}

}