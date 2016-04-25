package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class LevelMenu extends Menu {
	/** relative width of row of options **/
	private static final float RELATIVE_WIDTH_ROW = 0.8f;
	
	private static final float RELATIVE_HEIGHT_COLUMN = 0.75f;
	
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = ((1-RELATIVE_WIDTH_ROW)/2);
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.7f;
	
	private static final int NUM_ROWS_TOTAL = 3;
	
	private static final int NUM_COLUMNS_TOTAL = 5; 
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_X = 0.025f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_Y = 0.05f;
	
	// the menu options are set in their proper position by the constructor
	public LevelMenu(Option[] options){
		this.options = options;

		float width = (RELATIVE_WIDTH_ROW - NUM_COLUMNS_TOTAL*RELATIVE_MENU_SPACING_X)/NUM_COLUMNS_TOTAL;
		
		float height = (RELATIVE_HEIGHT_COLUMN - NUM_ROWS_TOTAL*RELATIVE_MENU_SPACING_Y)/NUM_ROWS_TOTAL;
		
		int numRows = (int) Math.ceil((width * options.length)/(RELATIVE_WIDTH_ROW - NUM_COLUMNS_TOTAL*RELATIVE_MENU_SPACING_X));
		
		for (int i =0;i<numRows;i++){
			int itemsLeft = options.length - i*NUM_COLUMNS_TOTAL;
			for (int j =0;j<itemsLeft;j++){
				int index = i*NUM_COLUMNS_TOTAL + j;
				float spacedX= (RELATIVE_X_POS + (RELATIVE_MENU_SPACING_X +width)*j);
				float spacedY = (RELATIVE_Y_POS - (RELATIVE_MENU_SPACING_Y+height)*i);
				options[index].setBounds(spacedX, spacedY, width, height);
				// set the color to something else
				options[index].setColor(Color.WHITE);
			}
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
}
