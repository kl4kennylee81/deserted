package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;
import edu.cornell.gdiac.ailab.GameSaveState.ActionUpgrade;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;

public class CharacterCustomization extends Menu {
	
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = 0.15f;
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.83f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.12f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING = 0.17f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
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
		
		this.options = new Option[3 + gameSaveState.characters.size() + charData.getTotalNumActionUpgrades()];
		options[0] = new Option("Back","Back");
		options[0].setBounds(0.08f, 0.1f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		options[1] = new Option("Reset","Reset");
		options[1].setBounds(0.88f, 0.1f, RELATIVE_WIDTH, RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		options[2] = new Option("Select","Select");
		options[2].setBounds(0.8f, 0.2f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[2].setColor(Constants.MENU_COLOR);
		
		characterOptions = new ArrayList<Option>();
		
		int i = 3;
		float curX = 0.6f;
		float incrX = 0.1f;
		float curY = 0.68f;
		for (int j = 0; j < gameSaveState.characters.size(); j++){
			float relX = curX + incrX*j;
			CharacterData cd = gameSaveState.characters.get(j);
			Texture toDraw = cd.getIcon();
			Color col = Color.WHITE;
			if (cd.characterId == selectedCharacterId){
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
		
		try {
			actionMap = ObjectLoader.getInstance().getActions(charData.getAllActionUpgrades());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int k;
		for (int j = 0; j < charData.actionUpgrades.size(); j++){
			float spacedY= (RELATIVE_Y_POS - RELATIVE_MENU_SPACING * j);
			ActionUpgrade actionUpgrade = charData.actionUpgrades.get(j);
			options[i] = new Option(actionMap.get(actionUpgrade).name, Integer.toString(actionUpgrade.actionId));
			options[i].setBounds(RELATIVE_X_POS, spacedY, RELATIVE_WIDTH, RELATIVE_HEIGHT);
			options[i].setColor(Constants.MENU_COLOR);
			i++;
			k = 0;
			for (ActionUpgrade au : actionUpgrade.upgrades){
				float tempSpacedY = spacedY + RELATIVE_MENU_SPACING/6 - RELATIVE_MENU_SPACING*k/3;
				options[i] = new Option(actionMap.get(au).name, Integer.toString(au.actionId));
				options[i].setBounds(RELATIVE_X_POS+0.13f, tempSpacedY, RELATIVE_WIDTH, RELATIVE_HEIGHT);
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
		this.selectedActionId = actionId;
	}
	
	public void selectAction(){
		if (selectedActionId != null){
			charData.setAction(selectedActionId);
		}
	}
	
	public void setCharacter(int charId){
		this.selectedCharacterId = charId;
		selectedActionId = null;
		setOptions();
		for (Option o : characterOptions){
			if (o.optionKey.equals(CHARACTER_ID_STRING+charId)){
				o.setImageColor(SELECTED_CHARACTER_COLOR);
			} else {
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
		canvas.drawCenteredText("Skill Points: " + charData.getRemainingSP(), canvas.getWidth()/2, 50, Color.BLACK);
		// draw the menu options
		for (int i=0;i<options.length;i++){
			if (options[i].optionKey.equals("Select") && selectedActionId == null){
				continue;
			}
			float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvas.getWidth();
			float y = options[i].getY(canvas) - 3*options[i].getHeight(canvas)/4;
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			
			if (options[i].isSelected && options[i].image == null){
				if (optionHighlight == null){
					return;
				}
				// we will draw the highlighting behind the option
				canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE);
			}
			
			try{
				int actionId = Integer.parseInt(options[i].optionKey);
				if (charData.currentlyUsingAction(actionId)){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE.cpy().lerp(Color.CLEAR, 0.3f));
				}
				if (selectedActionId != null && actionId == selectedActionId){
					canvas.drawTexture(optionHighlight,x,y,width,height,Color.WHITE.cpy().lerp(Color.CLEAR, 0.2f));
				}
			} catch (NumberFormatException e) {
				// not an integer!
			}
			options[i].draw(canvas);
		}
		
		drawIcons(canvas);
		drawActionInfo(canvas);
		
		if (pressingCharacter){
			Texture icon = charData.getIcon();
			float mouseX = InputController.getMouseX();
			float mouseY = InputController.getMouseY();
			float width = canvas.width*0.06f;
			canvas.drawTexture(icon, mouseX-width/2, mouseY-width/2, width, width, Color.WHITE);
		}
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
			canvas.drawCenteredText(selectedAction.description, canvas.width*0.75f, canvas.height*0.28f, Color.BLACK);
			canvas.drawCenteredText("SP Cost: "+selectedAU.cost, canvas.width*0.7f, canvas.height*0.2f, Color.BLACK);
			miniBoard.reset();
			drawHighlights(selectedAction);
			miniBoard.drawMini(canvas);
			canvas.drawCircle(canvas.width*0.642f, canvas.height*0.41f, canvas.height * 0.06f, Color.BLACK);
			
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
		ArrayList<CharacterData> selChars = gameSaveState.getSelectedCharacters();
		
		float curX = 0.67f;
		float incrX = 0.15f;
		float curY = 0.82f;
		for (int i = 0; i < selChars.size(); i++){
			float relX = curX + incrX*i;
			float xx = relX * canvas.width;
			float yy = curY * canvas.height;
			Texture toDraw = selChars.get(i).getIcon();
			float circleXOffset = toDraw.getWidth() * 0.25f;
			float circleYOffset = toDraw.getHeight() * 0.25f;
			canvas.drawCircle(xx-circleXOffset,yy-circleYOffset,toDraw.getWidth()*3.5f, Color.GOLD);
			canvas.drawTexture(toDraw,xx,yy,toDraw.getWidth()*3,toDraw.getHeight()*3,Color.WHITE);
			
			//print statements to help figure out where to drop
			//System.out.println((yy-circleYOffset)/canvas.height);
			//System.out.println((yy-circleYOffset+toDraw.getWidth()*3.5f)/canvas.height);
			
		}
	}

}
