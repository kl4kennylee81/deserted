package edu.cornell.gdiac.ailab;

import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class StartingMenu extends Menu {
	
	private static final String START_GAME_NAME = "Start Game";
	private static final String CONTINUE_NAME = "Continue";
	private static final String NEW_GAME_NAME = "New Game";

	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	private static final float RELATIVE_X_POS = 0.47f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.12f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	Texture optionHighlight;
	
	boolean isDone;
	boolean gameStart;
	
	public StartingMenu(){
		isDone = false;
		Option[] options = new Option[1];
		options[0] = new Option(START_GAME_NAME,START_GAME_NAME);
		options[0].setBounds(RELATIVE_X_POS, 0.5f, RELATIVE_WIDTH, RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		this.options = options;
	}

	public void setHighlight(Texture t){
		this.optionHighlight = t;
	}
	
	public void updateLevel(String levelName){
		switch(levelName){
		case START_GAME_NAME:
			showNewContinue();
			break;
		case NEW_GAME_NAME:
			try {
				GameSaveStateController.getInstance().resetGameSaveState();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		case CONTINUE_NAME:
			isDone = true;
			break;
		default:
			break;
		}
		
	}
	
	public void showNewContinue(){
		Option[] options = new Option[2];
		options[0] = new Option(NEW_GAME_NAME,NEW_GAME_NAME);
		options[0].setBounds(RELATIVE_X_POS, 0.5f, RELATIVE_WIDTH, RELATIVE_HEIGHT);
		options[0].setColor(Constants.MENU_COLOR);
		options[1] = new Option(CONTINUE_NAME,CONTINUE_NAME);
		options[1].setBounds(RELATIVE_X_POS, 0.43f, RELATIVE_WIDTH, RELATIVE_HEIGHT);
		options[1].setColor(Constants.MENU_COLOR);
		this.options = options;
	}
	
	@Override
	public void draw(GameCanvas canvas) {
		for (int i = 0; i < options.length; i++){
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
			options[i].draw(canvas);
		}
		
	}

}
