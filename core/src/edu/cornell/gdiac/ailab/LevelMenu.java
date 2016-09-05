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
	
	private static final int NUM_ROWS_TOTAL = 7;
	
	private static final int NUM_COLUMNS_TOTAL = 2; 
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_X = 0.025f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING_Y = 0.05f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.33f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.1f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	Texture optionHighlight;
	
	Texture logoTexture;
	
	// TODO change when free
	// the menu options are set in their proper position by the constructor
	public LevelMenu(Option[] options){
		
		this.logoTexture = new Texture(Constants.LEVEL_SELECT_LOGO);
		
		this.options = options;
		
		for (int i =0; i< 2;i++){
			for (int j =0;j<7;j++){
				int index= i*7+ j;
				if (index >= options.length){
					break;
				}
				float spacedX = RELATIVE_X_POS + i*(RELATIVE_WIDTH + 0.14f);
				float spacedY = RELATIVE_Y_POS - j*(RELATIVE_HEIGHT);
				options[index].setBounds(spacedX, spacedY, RELATIVE_WIDTH,RELATIVE_HEIGHT);
				// set the color to something else
				options[index].setColor(Color.BLACK.cpy());
			}
		}
		
		options[options.length-1].setBounds(0.1f - RELATIVE_WIDTH/2, 0f, RELATIVE_WIDTH,  RELATIVE_HEIGHT);
		options[options.length-1].setColor(Constants.MENU_COLOR.cpy());
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
		for (int i=0;i<options.length-1;i++){
			if (options[i].equals(null) || options[i].text == null){
				break;
			}
			float x = options[i].getX(canvas);
			float y = options[i].getY(canvas);
			float width = options[i].getWidth(canvas);
			float height = options[i].getHeight(canvas);
			Color imageColor = options[i].getColorImages();
			//canvas.drawTexture(image, x, y, width,height, Color.WHITE);
			if (options[i].image != null){
				canvas.drawTexture(options[i].image, x, y, width,height, imageColor);
			}
			
			Color textColor = options[i].getColor();
			canvas.drawCenteredText(options[i].text, x+width/2, y+3*height/5, textColor);
				
		}
		int i = options.length-1;
		float x = options[i].getX(canvas) - RELATIVE_HIGHLIGHT_X_OFFSET*canvas.getWidth();
		float y = options[i].getY(canvas);
		float width = options[i].getWidth(canvas);
		float height = options[i].getHeight(canvas);
		
		Color col = options[i].getColorImages();
		
		if (options[i].isSelected){
			if (optionHighlight != null){
				canvas.drawTexture(optionHighlight,x,y,width,height,col);
			}
		}
		Color textColor = options[i].getColor();
		if (options[i].isSelected){
			textColor = Color.BLACK;
		}
		canvas.drawCenteredText(options[i].text, x+width/2, y+height*3/4f,textColor);
		
		float logoX = 0.2f*canvas.getWidth();
		float logoY = 0.87f*canvas.getHeight();
		float logoW = 0.6f*canvas.getWidth();
		float logoH = 0.1f*canvas.getHeight();
		
		canvas.drawTexture(this.logoTexture,logoX,logoY,logoW,logoH,Color.WHITE.cpy());
	}
}
