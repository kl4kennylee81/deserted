package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
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
	
	private static final float RELATIVE_CHARACTER_Y = 0.63f;
	
	private static final float RELATIVE_CHARACTER_WIDTH = 0.08f;
	
	private static final float TEMP_HEIGHT = 0.2f;
	
	private static final float RELATIVE_DESCRIPTION_Y_POS = 0.25f;
	
	private static final float RELATIVE_DESCRIPTION_WIDTH = 0.12f;
	
	private static final float RELATIVE_DESCRIPTION_HEIGHT = 0.25f;
	
	private static final float RELATIVE_CHARACTERS_SIZE = 0.75f;
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");
	
	private static final float PLAYER1_CHARACTER_LIST_X_OFFSET = 0.05f;
			
	private static final int NULL_ID= -1;
	
	Characters characters;
	Character parent1;
	Character parent2;
	int selectedCharacterId;
	HashMap<Integer,ArrayList<Action>> actions;
	List<Option> characterOptions;
	
	int[] player1Characters;
	int[] player2Characters;
	HashSet<Integer> notAvailable;
	
	Texture optionHighlight;
	
	public DraftScreen(Characters characters){
		this.characters = characters;
		this.selectedCharacterId = 0;
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
		Option[] options = new Option[4+characters.size()];
		
		options[0] = new Option("Play","Play");
		options[0].setBounds(0.88f - RELATIVE_WIDTH/2, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		
		options[1] = new Option("Confirm","Confirm");
		options[1].setBounds(0.5f-RELATIVE_WIDTH*0.7f, 0.1f, RELATIVE_WIDTH*1.4f,  RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		options[2] = new Option("Nah","Nah");
		options[2].setBounds(0.12f - RELATIVE_WIDTH/2, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[2].setColor(Constants.MENU_COLOR);
		
		String text;
	    text = "Select";
		
		int index = getSelectedCharacterIndex();
		
		if (index < 0 || index > characters.size()){
			System.out.println("FIX THIS - CharacterSelect");
		}
		
		float leftRatio = PLAYER1_CHARACTER_LIST_X_OFFSET + ((1 - RELATIVE_CHARACTERS_SIZE) * 0.5f) - 0.05f;
		float curCharacterRatio = (index+1f) * RELATIVE_CHARACTERS_SIZE / (characters.size()+1);
		
		options[3] = new Option(text,"Select");
		options[3].setBounds(curCharacterRatio-(RELATIVE_CHARACTER_WIDTH/2)+leftRatio, 0.6f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[3].setColor(Constants.MENU_COLOR);

		
		characterOptions.clear();
		
		int i = 4;
		int j;
		for (j = 0; j < characters.size(); j++){
			Character cd = characters.get(j);
			options[i] = new Option("",CHARACTER_ID_STRING+cd.id);
			
			float ratio = (j+1f) * RELATIVE_CHARACTERS_SIZE / (characters.size()+1);
			
			options[i].setBounds(ratio-(RELATIVE_CHARACTER_WIDTH/2), RELATIVE_CHARACTER_Y, RELATIVE_CHARACTER_WIDTH,TEMP_HEIGHT);
			options[i].xPosition = options[i].xPosition + leftRatio;			
			options[i].setImage(cd.icon);
			characterOptions.add(options[i]);
			i++;
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
	

	@Override
	public void draw(GameCanvas canvas) {
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		
		// TODO Auto-generated method stub
		for (int i=0;i<4;i++){
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
		    canvas.drawCenteredText(options[i].text, x+width/2, y+height*3/4, col);
		}
		
		TextureRegion temp = null;
		


		ArrayList<Action> actionsToDraw = actions.get(selectedCharacterId);
		
		float relative_offset = 0.065f;
		

		float ratio = 0.5f;
		
		float descript1_x = canvasW * PLAYER1_CHARACTER_LIST_X_OFFSET;
		float descript_y1 = canvasH * (0.3f);
		float descript_y2 = canvasH * (0.6f);
		float descript_width = canvasW * (1 - RELATIVE_CHARACTERS_SIZE) * ratio;
		float descript2_x = canvasW - descript1_x - descript_width;
		float descript_height = canvasH * (0.2f);
		
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript1_x, descript_y1, descript_width, descript_height, Color.GOLD);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript1_x, descript_y2, descript_width, descript_height, Color.GOLD);
		
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript2_x, descript_y1, descript_width, descript_height, Color.GOLD);
		canvas.drawTexture(DESCRIPTION_BACKGROUND, descript2_x, descript_y2, descript_width, descript_height, Color.GOLD);
		
		Character cd0 = getCharacter(player1Characters[0]);
		Character cd1 = getCharacter(player1Characters[1]);
		
		Character cd2 = getCharacter(player2Characters[0]);
		Character cd3 = getCharacter(player2Characters[1]);
				
		float iconX1 = canvasW * (PLAYER1_CHARACTER_LIST_X_OFFSET + 0.015f);
		float iconWidth = canvasW * (1 - RELATIVE_CHARACTERS_SIZE) * ratio* 0.7f;
		float iconX2 = canvasW - iconX1 - iconWidth;
		float iconHeight = canvasH * (0.15f);
		if (cd0 != null){
			float iconY = canvasH * (0.625f);
			canvas.drawTexture(cd0.bigIcon, iconX1, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd1 != null){
			float iconY = canvasH * (0.325f);
			canvas.drawTexture(cd1.bigIcon, iconX1, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd2 != null){
			float iconY = canvasH * (0.625f);
			canvas.drawTexture(cd2.bigIcon, iconX2, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		if (cd3 != null){
			float iconY = canvasH * (0.325f);
			canvas.drawTexture(cd3.bigIcon, iconX2, iconY, iconWidth, iconHeight, Color.WHITE);
		}
		
		
		float i = 1;
		
//		for (Action action : actionsToDraw){
//			float middle_x = canvasW * i * RELATIVE_CHARACTERS_SIZE / (actionsToDraw.size()+1);
//			float descript_y = RELATIVE_DESCRIPTION_Y_POS *canvasH;
//			descript_width = RELATIVE_DESCRIPTION_WIDTH *canvasW;
//			descript_height = RELATIVE_DESCRIPTION_HEIGHT * canvasH;
//			float descript_x = middle_x - (descript_width/2) + descript1_x + descript_width;
//			canvas.drawAction(action, descript_x, descript_y, descript_width, descript_height, false);
//			/*canvas.drawTexture(DESCRIPTION_BACKGROUND, descript_x, descript_y, descript_width, descript_height, Color.WHITE);
//			canvas.drawCenteredTexture(action.menuIcon, middle_x, descript_y+descript_height,50,50, Color.WHITE);
//			//canvas.drawCenteredText(action, descript_x, descript_y, Color.WHITE);
//			float name_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*3)*canvasH;
//			canvas.drawCenteredText(action.name, middle_x,name_y, Color.WHITE);
//			float dmg_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*2)*canvasH;
//			canvas.drawCenteredText("DMG: "+action.damage, middle_x,dmg_y, Color.WHITE);
//			float cost_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*1)*canvasH;
//			canvas.drawCenteredText("COST: "+action.cost, middle_x,cost_y, Color.WHITE);*/
//			i++;
//		}
		
		for (int j=0; j < characters.size(); j++){
			FilmStrip toDraw = characters.get(j).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			Option charOption = characterOptions.get(j);
			if (toDraw == null){
				toDraw = characters.get(j).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
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
				float x = charOption.xPosition * canvas.width;
				float y = charOption.yPosition * canvas.height;
				
				//canvas.drawCenteredText(characters.get(j).name, x+width/2, 0.9f*canvas.height, Color.WHITE);
				
				String optionKey = charOption.optionKey;
				if (charOption.optionKey.contains(CHARACTER_ID_STRING)){
					String charIdString = optionKey.substring(CHARACTER_ID_STRING.length());
					int charId = Integer.parseInt(charIdString);
					if (!notAvailable.contains(charId)){
						canvas.draw(toDraw, Color.WHITE,x,y,width,height);
					} else {
						canvas.draw(toDraw, Color.GRAY,x,y,width,height);
					}
				}
			}
		}
	}

}