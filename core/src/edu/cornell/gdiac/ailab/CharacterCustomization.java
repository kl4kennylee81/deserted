package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.GameSaveState.ActionUpgrade;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CharacterCustomization extends Menu {
	
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = 0.40f;
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.95f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.35f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING = 0.075f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	Texture optionHighlight;
	GameSaveState gameSaveState;
	CharacterData charData;
	float lerpVal;
	
	public CharacterCustomization(GameSaveState gameSaveState){
		this.gameSaveState = gameSaveState;
		this.charData = gameSaveState.getCharacterData(0);
		this.lerpVal = 0;
		setOptions();
	}
	
	public void setOptions(){
		this.options = new Option[2 + charData.getTotalNumActionUpgrades()];
		options[0] = new Option("Back","Back");
		options[0].setBounds(0.1f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		options[1] = new Option("Reset","Reset");
		options[1].setBounds(0.9f, 0.1f, RELATIVE_WIDTH, RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		HashMap<ActionUpgrade,Action> actionMap = null;
		
		try {
			actionMap = ObjectLoader.getInstance().getActions(charData.getAllActionUpgrades());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int i = 2;
		for (int j = 0; j < charData.actionUpgrades.size(); j++){
			ActionUpgrade actionUpgrade = charData.actionUpgrades.get(j);
			for (ActionUpgrade au : actionUpgrade.getActionUpgrades()){
				float spacedY= (RELATIVE_Y_POS - RELATIVE_MENU_SPACING * i);
				options[i] = new Option(actionMap.get(au).name+" ("+au.cost+")", Integer.toString(au.actionId));
				options[i].setBounds(RELATIVE_X_POS, spacedY, RELATIVE_WIDTH, RELATIVE_HEIGHT);
				options[i].setColor(Constants.MENU_COLOR);
				i++;
			}
		}
	}
	
	public void setHighlight(Texture t){
		this.optionHighlight = t;
	}
	
	public void resetSP(){
		charData.resetSP();
	}
	
	public void setAction(int actionId){
		charData.setAction(actionId);
	}

	@Override
	public void draw(GameCanvas canvas) {
		canvas.drawText("Skill points: " + charData.getRemainingSP(), 450, 50, Color.BLACK);
		// draw the menu options
		for (int i=0;i<options.length;i++){
			float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvas.getWidth();
			float y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (options[i].isSelected){
				if (optionHighlight == null){
					return;
				}
				// we will draw the highlighting behind the option
				canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
			}
			
			try{
				int actionId = Integer.parseInt(options[i].optionKey);
				if (charData.currentlyUsingAction(actionId)){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE.cpy().lerp(Color.CLEAR, 0.5f));
				}
			} catch (NumberFormatException e) {
				// not an integer!
			}
			options[i].draw(canvas);
		}
	}

}
