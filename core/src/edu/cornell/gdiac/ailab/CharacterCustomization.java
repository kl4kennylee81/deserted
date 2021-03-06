package edu.cornell.gdiac.ailab;

import java.awt.Canvas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.GameSaveState.ActionUpgrade;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CharacterCustomization extends Menu {
	
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = 0.198f;
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.4f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.05f;
	
	private static final float RELATIVE_OPTION_WIDTH = 0.12f;
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING = 0.15f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.005f;
	
	private static final float RELATIVE_HIGHLIGHT_Y_OFFSET = 0.005f;
	
	public static final String CHARACTER_ID_STRING = "CharId:";
	
	private static final Color SELECTED_CHARACTER_COLOR = Color.GOLDENROD.cpy();
	
	private static final int BOARD_X_POS = 1;
	private static final int BOARD_Y_POS = 1;
	
	Texture optionHighlight;
	GameSaveState gameSaveState;
	CharacterData charData;
	float lerpVal;
	Integer selectedCharacterId;
	Integer selectedActionId;
	HashMap<ActionUpgrade,Action> actionMap;
	ArrayList<Option> characterOptions;
	GridBoard miniBoard;
	
	
	boolean pressingCharacter;
	
	//TODO: do drag and drop for selected characters
	
	public CharacterCustomization(GameSaveState gameSaveState){
		this.gameSaveState = gameSaveState;
		this.lerpVal = 0;
		loadCharacterInfo();
		selectedCharacterId = 0;
		setOptions();
		selectedActionId = null;
		this.miniBoard = new GridBoard(6,4);
		miniBoard.setTileTexture(new Texture(Constants.TILE_TEXTURE));
		pressingCharacter = false;
	}
	
	public void loadCharacterInfo(){
		try {
			ObjectLoader.getInstance().getCharacterInfo(gameSaveState.characters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setOptions(){
		this.charData = gameSaveState.getCharacterData(selectedCharacterId);
		
		//this.options = new Option[3 + gameSaveState.characters.size() + charData.getTotalNumActionUpgrades()];
		this.options = new Option[2 + gameSaveState.availableCharacters.size() + charData.getTotalNumActionUpgrades()-1];
		options[0] = new Option("Back","Back");
		options[0].setBounds(0.12f - RELATIVE_OPTION_WIDTH/2, 0.1f, RELATIVE_OPTION_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		options[1] = new Option("Reset Points","Reset");
		options[1].setBounds(0.88f - RELATIVE_OPTION_WIDTH*2f/3f, 0.1f, RELATIVE_OPTION_WIDTH*4f/3f,  RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		
		characterOptions = new ArrayList<Option>();
		
		List<CharacterData> availableCharacters = gameSaveState.getAvailableCharactersData();
		List<CharacterData> unlockableCharacters = gameSaveState.getUnlockableCharactersData();
		int i = 2;
		int j, k;
		float curX = 0.05f;
		float incrY = 0.1f;
		float curY = 0.68f;
		for (j = 0; j < availableCharacters.size(); j++){
			float relY = curY - incrY*j;
			CharacterData cd = availableCharacters.get(j);
			Texture toDraw = cd.bigIcon;
			Color col = Color.WHITE;
			if (cd.characterId == selectedCharacterId){
				col = SELECTED_CHARACTER_COLOR;
			}
			options[i] = new Option("",CHARACTER_ID_STRING+cd.characterId);
			options[i].setBounds(curX, relY, 0.06f, 0.06f);
			options[i].sameWidthHeight = true;
			options[i].setColor(col);;
			options[i].setImageColor(col);
			options[i].setImage(toDraw);
			characterOptions.add(options[i]);
			i++;
		}
		
		/*
		for (k = 0; k < unlockableCharacters.size(); k++){
			float relX = curX + incrX*(k+j);
			CharacterData cd = unlockableCharacters.get(k);
			Texture toDraw = cd.getIcon();
			Color col = Color.GRAY;
			options[i] = new Option("","");
			options[i].setBounds(relX, curY, 0.06f, 0.06f);
			options[i].sameWidthHeight = true;
			options[i].setColor(col);;
			options[i].setImageColor(col);
			options[i].setImage(toDraw);
			characterOptions.add(options[i]);
			i++;
		}
		*/
		try {
			actionMap = ObjectLoader.getInstance().getActions(charData.getAllActionUpgrades());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for (j = 0; j < charData.actionUpgrades.size(); j++){
			ActionUpgrade actionUpgrade = charData.actionUpgrades.get(j);
			//don't want move 
			if (actionMap.get(actionUpgrade).name.equals("Move")){
				continue;
			}
			//j is one more than it should be because we encountered useless Move case.
			float spacedX= (RELATIVE_X_POS + RELATIVE_WIDTH * (j-1) + RELATIVE_MENU_SPACING * (j-1));
			
			options[i] = new Option("", Integer.toString(actionUpgrade.actionId));
			options[i].setImage(actionMap.get(actionUpgrade).menuIcon);
			options[i].sameWidthHeight = true;
			options[i].setBounds(spacedX, RELATIVE_Y_POS, RELATIVE_WIDTH, RELATIVE_HEIGHT);
			options[i].setColor(Constants.MENU_COLOR);
			i++;
			k = 0;
			float spacedY = RELATIVE_Y_POS-0.08f;
			for (ActionUpgrade au : actionUpgrade.upgrades){
				float tempSpacedX = (spacedX-RELATIVE_WIDTH*2f/3f) + (2*RELATIVE_WIDTH*2f/3f) * (k%2);
				float tempSpacedY = spacedY - 0.08f * (k/2);
				options[i] = new Option("", Integer.toString(au.actionId));
				options[i].setImage(actionMap.get(au).menuIcon);
				options[i].sameWidthHeight = true;
				options[i].setBounds(tempSpacedX, tempSpacedY, RELATIVE_WIDTH, RELATIVE_HEIGHT);
				options[i].setColor(Constants.MENU_COLOR);
				i++;
				k++;
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
		if (this.selectedActionId!=null && this.selectedActionId == actionId){
			selectAction();
		}else{
			this.selectedActionId = actionId;
		}
	}
	
	public void selectAction(){
		if (selectedActionId != null){
			charData.setAction(selectedActionId);
		}
	}
	
	public void setCharacter(int charId){
		if (charId == selectedCharacterId){
			return;
		}
		int oldSelectedChar = this.selectedCharacterId;
		this.selectedCharacterId = charId;
		selectedActionId = null;
		setOptions();
		for (Option o : characterOptions){
			if (o.optionKey.equals(CHARACTER_ID_STRING+charId)){
				o.setImageColor(SELECTED_CHARACTER_COLOR);
			} 
			if (o.optionKey.equals(CHARACTER_ID_STRING+oldSelectedChar)){
				o.setImageColor(Color.WHITE);
			} 
		}
	}
	
	public void dropCharacter(){
		this.pressingCharacter = false;
		
		float mouseX = InputController.getMouseXRelative();
		float mouseY = InputController.getMouseYRelative();
		
		float minX0 = 0.664f;
		float maxX0 = 0.746f;
		
		float minX1 = 0.814f;
		float maxX1 = 0.896f;
		
		float minY = 0.809f;
		float maxY = 0.955f;
		
		if (mouseX > minX0 && mouseX < maxX0 && mouseY > minY && mouseY < maxY){
			replaceCharacter(0);
		}
		if (mouseX > minX1 && mouseX < maxX1 && mouseY > minY && mouseY < maxY){
			replaceCharacter(1);
		}
	}
	
	public void replaceCharacter(int selIndex){
		gameSaveState.replaceSelectedCharacter(selIndex, selectedCharacterId);
	}

	@Override
	public void draw(GameCanvas canvas) {
		//draw first tree container
		canvas.drawActionBackground(0.14f*canvas.width, 0.2f * canvas.height , 0.165f * canvas.width, 0.3f * canvas.height, Color.DARK_GRAY);
		//draw second tree container
		canvas.drawActionBackground(0.34f*canvas.width, 0.2f * canvas.height , 0.165f * canvas.width, 0.3f * canvas.height, Color.BLUE);
		//draw third tree container
		canvas.drawActionBackground(0.54f*canvas.width, 0.2f * canvas.height , 0.165f * canvas.width, 0.3f * canvas.height, Color.RED);
		
		String topTxt = "Choose a character and spend skill points to customize your character's skills!";
		canvas.drawCenteredText(topTxt, canvas.width/2, 0.96f * canvas.height, Color.WHITE, 1.2f);
		canvas.drawCenteredText("Skill Points: " + charData.getRemainingSP(), canvas.getWidth() * 0.5f, 0.11f * canvas.height, Color.BLACK);
		// draw the menu options
		for (int i = 0; i < 2; i ++){
			float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvas.getWidth();
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
		for (int i=2;i<options.length;i++){
			if (options[i].optionKey.equals("Select") && selectedActionId == null){
				continue;
			}
			
			float x = options[i].getX(canvas);
			float y = options[i].getY(canvas);
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (options[i].isSelected && options[i].image == null){
				if (optionHighlight == null){
					return;
				}
				
				x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvas.getWidth();
				y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
				// we will draw the highlighting behind the option
				canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
			}
			
			try{
				options[i].setImageColor(Color.WHITE);
				int actionId = Integer.parseInt(options[i].optionKey);
				if (charData.currentlyUsingAction(actionId)){
					options[i].setImageColor(Color.ORANGE);
				}
				else if (selectedActionId != null && actionId == selectedActionId){
					options[i].setImageColor(Color.YELLOW);
				}
			} catch (NumberFormatException e) {
				// not an integer!
			}
			boolean drawCircle = false;
			if (options[i].optionKey.contains(CHARACTER_ID_STRING)){
				String charIdString = options[i].optionKey.substring(CHARACTER_ID_STRING.length());
				int charId = Integer.parseInt(charIdString);
				if (charId == selectedCharacterId){
					drawCircle = true;
				}
			}
			options[i].drawForSkillTree(canvas,drawCircle);
		}
		
		//drawIcons(canvas);
		drawActionInfo(canvas);
		
	}
	
	public ActionUpgrade getSelectedActionUpgrade(){
		if (selectedActionId == null) return null;
		for (ActionUpgrade au : actionMap.keySet()){
			if (au.actionId == selectedActionId){
				return au;
			}
		}
		return null;
	}
	
	public void drawActionInfo(GameCanvas canvas){
		if (selectedActionId != null){
			ActionUpgrade selectedAU = getSelectedActionUpgrade();
			Action selectedAction = actionMap.get(selectedAU);
			canvas.drawAction(selectedAction, canvas.width*0.76f, canvas.height*0.35f, 0.18f * canvas.width, 0.37f * canvas.height, false);
			canvas.drawCenteredText("SP Cost: "+selectedAU.cost, canvas.width*0.85f, canvas.height*0.32f, Color.BLACK);
			miniBoard.reset();
			drawHighlights(selectedAction);
			miniBoard.drawMini(canvas);
			canvas.drawCircle(canvas.width*0.292f, canvas.height*0.64f, canvas.height * 0.06f, Color.BLACK);
			
			//TODO: draw Actions and Costs
		}
	}
	
	public void drawHighlights(Action action){
		switch (action.pattern){
		case STRAIGHT:
			drawStraight(action);
			break;
		case SINGLE:
			drawSingle(action);
			break;
		case MOVE:
			drawMove(action);
			break;
		case HORIZONTAL:
			drawHorizontal(action);
			break;
		case DIAGONAL:
			drawDiagonal(action);
			break;
		case SHIELD:
			drawShield(action);
			break;
		case INSTANT:
			drawPath(action);
			break;
		case PROJECTILE:
			drawPath(action);
			break;
		case SINGLEPATH:
			drawSinglePath(action);
			break;
		case NOP:
			break;
		default:
			break;
		}
	}
	
	public void drawStraight(Action action){
		for (int i = 1; i <= action.range; i++){
			miniBoard.setHighlighted(BOARD_X_POS+i, BOARD_Y_POS);
		}
	}
	
	public void drawSingle(Action action){
		boolean highlighted = true;
		for (int i=0;i<miniBoard.getWidth();i++){
			for (int j = 0;j<miniBoard.getHeight();j++){
				if (action.singleCanTarget(BOARD_X_POS,BOARD_Y_POS,i,j, true, miniBoard)){
					if (i >= (int)miniBoard.getWidth()/2 || action.isBuff){
						if (highlighted) {
							miniBoard.setHighlighted(i, j);
							highlighted = false;
						}
						miniBoard.setCanTarget(i,j);
					}
				}
			}
		}
	}
	
	private Coordinate[] singlePathHitPath(Action action, int selectedX, int selectedY, boolean leftside){
		Coordinates coords = Coordinates.getInstance();
		// when we pass in coordinate for the path we can go out of bounds it is checked in execution time
		if (action== null || action.path == null){
			System.out.println("line action controller 187: error input pattern projectile or instant did not have path");
			return null;
		}
		Coordinate[] relativePath = action.path;
		Coordinate[] absolutePath = new Coordinate[relativePath.length];
		for (int i = 0;i<relativePath.length;i++){
			// if on leftside we increment x in opposite direction
			int x;int y;
			if (leftside){
				x = selectedX + relativePath[i].x;
				y = selectedY + relativePath[i].y;
			}
			else{
				x = selectedX - relativePath[i].x;
				y = selectedY + relativePath[i].y;
			}
			absolutePath[i] = coords.obtain();
			absolutePath[i].set(x, y);
		}
		return absolutePath;
	}
	
	private int getHitSize(Coordinate[] hitCoords, boolean leftside, boolean isBuff){
		int i = 0;
		for (Coordinate c : hitCoords){
			int ii = c.x;
			int jj = c.y;
			if (ii < 0 || ii >= miniBoard.getWidth() || jj < 0 || jj >= miniBoard.getHeight()){
				continue;
			}
			boolean add = false;
			if (leftside && ii >= (int)miniBoard.getWidth()/2){
				add = true;
			}
			else if (!leftside && ii < (int)miniBoard.getWidth()/2){
				add = true;
			}
			else if(isBuff){
				add = true;
			}
			if (add){
				i++;
			}
		}
		return i;
	}
	
	public void drawSinglePath(Action action){
		if (action.path == null){
			return;
		}
		int maxHitSize = 0;
		int selectedX = 0;
		int selectedY = 0;
		for (int i=0;i<miniBoard.getWidth();i++){
			for (int j = 0;j<miniBoard.getHeight();j++){
				if (action.singleCanTarget(BOARD_X_POS,BOARD_Y_POS,i,j, true,miniBoard)){
					if (i >= (int)miniBoard.getWidth()/2){
						Coordinate[] hitCoords = singlePathHitPath(action,i,j,true);
						int curHitSize = getHitSize(hitCoords, true, action.isBuff);
						if (curHitSize > maxHitSize){
							selectedX = i;
							selectedY = j;
							maxHitSize = curHitSize;
						}
						for (Coordinate c : singlePathHitPath(action,i,j,true)){
							int ii = c.x;
							int jj = c.y;
							if (ii >= (int)miniBoard.getWidth()/2){
								miniBoard.setCanTarget(ii,jj);
							}
							else if(action.isBuff){
								miniBoard.setCanTarget(ii, jj);
							}
						}
						miniBoard.setSingleCanTarget(i, j);
					}
				}
			}
		}
		
		Coordinate[] path = action.path;
		for (int i = 0; i < path.length; i++){
			int x = selectedX + path[i].x;
			int y = selectedY + path[i].y;
			if (BOARD_X_POS == x && BOARD_Y_POS == y){
				continue;
			}
			else if ((!miniBoard.isInBounds(x,y))){
				continue;
			}
			if (!miniBoard.isOnSide(true, x, y)){
				miniBoard.setHighlighted(x,y);
			}
		}
	}
	
	public void drawMove(Action action){
		miniBoard.setCanMove(true, BOARD_X_POS-1, BOARD_Y_POS);
		miniBoard.setCanMove(true, BOARD_X_POS+1, BOARD_Y_POS);
		miniBoard.setCanMove(true, BOARD_X_POS, BOARD_Y_POS-1);
		miniBoard.setHighlighted(BOARD_X_POS, BOARD_Y_POS+1);
	}
	
	public void drawHorizontal(Action action){
		for (int y = 0; y < miniBoard.height; y++){
			miniBoard.setHighlighted(miniBoard.width - 1 - BOARD_X_POS, y);
		}
	}
	
	public void drawDiagonal(Action action){
		for (int i = 0; i < action.range; i++){
			miniBoard.setHighlighted(BOARD_X_POS+i+1, BOARD_Y_POS+i);
			miniBoard.setCanTarget(BOARD_X_POS+i+1, BOARD_Y_POS-i);
		}
	}
	
	public void drawShield(Action action){
		for (int i = 0; i<= action.range/2;i++){
			miniBoard.setHighlighted(BOARD_X_POS, BOARD_Y_POS+i);
			if (action.range%2!=0){
				miniBoard.setHighlighted(BOARD_X_POS, BOARD_Y_POS-i);
			}
			miniBoard.setCanTarget(BOARD_X_POS, BOARD_Y_POS-i);
		}
	}
	
	public void drawPath(Action action){
		if (action.path == null){
			return;
		}
		Coordinate[] path = action.path;
		for (int i = 0; i < path.length; i++){
			int x = BOARD_X_POS + path[i].x;
			int y1 = BOARD_Y_POS + path[i].y;
			int y2 = BOARD_Y_POS - path[i].y;
			if (BOARD_X_POS == x && BOARD_Y_POS == y1){
				continue;
			}
			if (miniBoard.isInBounds(x,y1)){
				miniBoard.setHighlighted(x, y1);
			}
			if (miniBoard.isInBounds(x,y2)){
				miniBoard.setCanTarget(x, y2);
			}
		}
	}
	
	public void drawIcons(GameCanvas canvas){

		float x = 0.05f * canvas.getWidth();
		float y = 0.82f * canvas.getHeight();
		Texture selectedIcon = charData.bigIcon;
		float circleXOffset = selectedIcon.getWidth() * 0.25f;
		float circleYOffset = selectedIcon.getHeight() * 0.25f;
		canvas.drawCircle(x-circleXOffset,y-circleYOffset,selectedIcon.getWidth()*2f, Color.GOLD);
		canvas.drawTexture(selectedIcon,x,y,selectedIcon.getWidth()*1.5f,selectedIcon.getHeight()*1.5f,Color.WHITE);
		
	}

}
