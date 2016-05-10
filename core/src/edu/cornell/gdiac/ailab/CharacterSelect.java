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
	
	public static final String CHARACTER_ID_STRING = "CharId:";
	
	private static final Color SELECTED_CHARACTER_COLOR = Color.GOLDENROD.cpy();
	
	private static final float RELATIVE_CHARACTER_Y = 0.7f;
	
	private static final float RELATIVE_CHARACTER_WIDTH = 0.12f;
	
	List<CharacterData> characters;
	Integer selectedCharacterId;
	List<ArrayList<Action>> actions;
	List<Option> characterOptions;
	
	public CharacterSelect(List<CharacterData> characters){
		this.characters = characters;
		this.selectedCharacterId = null;
		actions = new ArrayList<ArrayList<Action>>();
		for (CharacterData cd : characters){
			actions.add(new ArrayList<Action>());
		}
		
		loadCharacterInfo();
		
		characterOptions = new ArrayList<Option>();
		
		Option[] options = new Option[3+characters.size()];
		options[0] = new Option("Back","Back");
		options[0].setBounds(0.08f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		
		options[1] = new Option("Skill Tree","Skill Tree");
		options[1].setBounds(0.48f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		options[2] = new Option("Play","Play");
		options[2].setBounds(0.88f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[2].setColor(Constants.MENU_COLOR);
		
		int i = 3;
		int j, k;
		float curX = 0.6f;
		float incrX = 0.1f;
		float curY = 0.68f;
		for (j = 0; j < characters.size(); j++){
			float relX = curX + incrX*j;
			CharacterData cd = characters.get(j);
			Texture toDraw = cd.getIcon();
			Color col = Color.WHITE;
			if (selectedCharacterId != null && cd.characterId == selectedCharacterId){
				col = SELECTED_CHARACTER_COLOR;
			}
			options[i] = new Option("",CHARACTER_ID_STRING+cd.characterId);
			options[i].setBounds(relX, curY, 0.06f, 0.06f);
			options[i].sameWidthHeight = true;
			options[i].setColor(col);;
			options[i].setImageColor(col);
			options[i].setImage(toDraw);
			characterOptions.add(options[i]);
			i++;
		}
		
		this.options = options;
	}
	
	public void loadCharacterInfo(){
		try {
			ObjectLoader.getInstance().getCharacterInfo(characters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void draw(GameCanvas canvas) {
		// TODO Auto-generated method stub
		for (int i=0;i<options.length;i++){
			options[i].draw(canvas);
		}
		for (int i=0; i < characters.size(); i++){
			FilmStrip toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			if (toDraw == null){
				toDraw = characters.get(i).animation.getTexture(CharacterState.ACTIVE, InGameState.NORMAL);
			}
			if (toDraw != null){
				float width = RELATIVE_CHARACTER_WIDTH*canvas.width;
				float height = width * toDraw.getRegionHeight() / toDraw.getRegionHeight();
				float ratio = (i+1f) / (characters.size()+1);
				float x = ratio*canvas.width - width/2;
				float y = RELATIVE_CHARACTER_Y * canvas.height;
				canvas.draw(toDraw, Color.WHITE,x,y,width,height);
			}
		}
	}

}
