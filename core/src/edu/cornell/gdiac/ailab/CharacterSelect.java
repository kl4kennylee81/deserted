package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

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
	
	private static final float RELATIVE_CHARACTER_WIDTH = 0.12f;
	
	private static final float TEMP_HEIGHT = 0.2f;
	
	private static final float RELATIVE_DESCRIPTION_Y_POS = 0.25f;
	
	private static final float RELATIVE_DESCRIPTION_WIDTH = 0.15f;
	
	private static final float RELATIVE_DESCRIPTION_HEIGHT = 0.25f;
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");
	
	List<CharacterData> characters;
	int selectedCharacterId;
	List<ArrayList<Action>> actions;
	List<Option> characterOptions;
	
	Texture optionHighlight;
	
	public CharacterSelect(List<CharacterData> characters){
		this.characters = characters;
		this.selectedCharacterId = 0;
		actions = new ArrayList<ArrayList<Action>>();
		
		loadCharacterInfo();
		loadActionInfo();
		
		characterOptions = new ArrayList<Option>();
		
		setOptions();
	}
	
	public void setOptions(){
		Option[] options = new Option[3+characters.size()];
		
		options[0] = new Option("Play","Play");
		options[0].setBounds(0.88f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		
		options[1] = new Option("Skill Tree","Skill Tree");
		options[1].setBounds(0.48f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		options[2] = new Option("Back","Back");
		options[2].setBounds(0.08f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[2].setColor(Constants.MENU_COLOR);
		
		int i = 3;
		int j, k;
		float curX = 0.6f;
		float incrX = 0.1f;
		float curY = 0.68f;
		for (j = 0; j < characters.size(); j++){
			float relX = curX + incrX*j;
			CharacterData cd = characters.get(j);
			options[i] = new Option("",CHARACTER_ID_STRING+cd.characterId);
			
			float ratio = (j+1f) / (characters.size()+1);
			
			options[i].setBounds(ratio-RELATIVE_CHARACTER_WIDTH/2, RELATIVE_CHARACTER_Y, RELATIVE_CHARACTER_WIDTH,TEMP_HEIGHT);
			
			options[i].setImage(cd.getIcon());
			characterOptions.add(options[i]);
			i++;
		}
		
		this.options = options;
	}
	
	public void setHighlight(Texture t){
		this.optionHighlight = t;
	}
	
	public void setCharacter(int charId){
		if (charId == selectedCharacterId){
			return;
		}
		this.selectedCharacterId = charId;
		//redo option for selecting character;
		//show action descriptions
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
				actions.add(ObjectLoader.getInstance().getSelectedActionList(cd.currentActions));
			}
		} catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	@Override
	public void draw(GameCanvas canvas) {
		int canvasW = canvas.getWidth();
		int canvasH = canvas.getHeight();
		
		// TODO Auto-generated method stub
		for (int i=0;i<3;i++){
			float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvasW;
			float y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (options[i].isSelected){
				if (optionHighlight != null){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
				}
			}
			options[i].draw(canvas);
		}
		for (int i=0; i < characters.size(); i++){
			FilmStrip toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			Option charOption = characterOptions.get(i);
			if (toDraw == null){
				toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			}
			if (toDraw != null){
				float heightToWidthRatio = toDraw.getRegionHeight()*1f/toDraw.getRegionWidth();
				float relativeHeight = charOption.getWidth()*heightToWidthRatio;
				charOption.height = relativeHeight;
				
				float width = characterOptions.get(i).getWidth()*canvas.width;
				float height = width * toDraw.getRegionHeight() / toDraw.getRegionHeight();
				float ratio = (i+1f) / (characters.size()+1);
				float x = ratio*canvas.width - width/2;
				float y = RELATIVE_CHARACTER_Y * canvas.height;
				if (i == selectedCharacterId){
					canvas.draw(toDraw, Color.WHITE,x,y,width,height);
				} else {
					canvas.draw(toDraw, Color.GRAY,x,y,width,height);
				}
			}
		}
		
		ArrayList<Action> actionsToDraw = actions.get(selectedCharacterId);
		
		float relative_offset = 0.065f;
		
		float i = 1;
		
		for (Action action : actionsToDraw){
			float middle_x = canvasW * i / (actionsToDraw.size()+1);
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
	}

}
