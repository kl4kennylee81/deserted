package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class LevelMenu extends Menu {
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = 0.1375f;
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.5f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.15f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.35f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_X = 0.20f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_Y = 0.1f;
	
	public LevelMenu(Option[] options){
		this.options = options;
		for (int i =0;i<options.length;i++){
			
			float spacedX= (RELATIVE_X_POS + RELATIVE_MENU_SPACING_X * i);
			options[i].setBounds(spacedX, RELATIVE_Y_POS, RELATIVE_WIDTH, RELATIVE_HEIGHT);
			
			// set the color to something else
			options[i].setColor(Color.WHITE);
		}
	}
	
	public void setImage(Texture t){
		this.optionTexture = t;
		for (int i=0;i<options.length;i++){
			options[i].setImage(t);
		}
	}

	@Override
	public void draw(GameCanvas canvas) {	
		// draw the menu options
		for (int i=0;i<options.length;i++){
			float x = options[i].getX(canvas);
			float y = options[i].getY(canvas);
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			options[i].draw(canvas);
		}
	}

	@Override
	public void selectOption(String optionKey) {
		for (int i=0;i<options.length;i++){
			if (options[i].optionKey == optionKey){
				options[i].isSelected = true;
			}
			else if (options[i].optionKey == selectedOption){
				options[i].isSelected = false;
			}
		}
		selectedOption = optionKey;
	}
	
	@Override
	public int getIndexOption(String optionKey){
		for (int i=0;i<options.length;i++){
			if (options[i].optionKey == optionKey){
				return i;
			}			
		}
		return -1;
	}
	
	public int getCurIndexOption(){
		return getIndexOption(this.selectedOption);
	}

}
