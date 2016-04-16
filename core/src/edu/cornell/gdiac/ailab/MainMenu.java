package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class MainMenu extends Menu{
	/** game logo texture */
	Texture logo;
	
	/** option gradient highlighting */
	Texture optionHighlight;

	/** Lerp value for highlighting */
	private float lerpVal;

	/** Lerp value increasing or decreasing */
	private boolean increasing;
	
	/** start position of the menu's options x Position **/
	private static final float RELATIVE_X_POS = 0.40f;
	
	/** start position of the menu's options y Position going down **/
	private static final float RELATIVE_Y_POS = 0.45f;
	
	/** relative width of options **/
	private static final float RELATIVE_WIDTH = 0.35f;
	
	/** relative height of options **/
	private static final float RELATIVE_HEIGHT = 0.05f;
	
	/** relative spacing between options **/
	private static final float RELATIVE_MENU_SPACING = 0.075f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	private static final float RELATIVE_LOGO_X = 0.13f;
	
	private static final float RELATIVE_LOGO_Y = 0.11f;
	
	private static final float RELATIVE_LOGO_WIDTH = 0.225f;
	
	private static final float RELATIVE_LOGO_HEIGHT = 0.8f;

	public MainMenu(Option[] options){
		this.options = options;
		
		// after setting the options must now assign the bounds based on the main menu specs
		for (int i =0;i<options.length;i++){
			float spacedY= (RELATIVE_Y_POS - RELATIVE_MENU_SPACING * i);
			options[i].setBounds(RELATIVE_X_POS, spacedY, RELATIVE_WIDTH, RELATIVE_HEIGHT);
			options[i].setColor(Constants.MENU_COLOR);
		}
		this.lerpVal = 0;
	}
	
	public void setHighlight(Texture t){
		this.optionHighlight = t;
	}
	
	public void setLogo(Texture t){
		this.logo = t;
	}
	
	public void draw(GameCanvas canvas){
		drawLogo(canvas);
		
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
			options[i].draw(canvas);
		}
	}
	
	public void drawLogo(GameCanvas canvas){
		if (logo == null){
			return;
		}
		float x = RELATIVE_LOGO_X * canvas.getWidth();
		float y = RELATIVE_LOGO_Y * canvas.getHeight();
		float width = RELATIVE_LOGO_WIDTH * canvas.getWidth();
		float height = RELATIVE_LOGO_HEIGHT * canvas.getHeight();
		canvas.drawTexture(logo, x, y,width,height, Color.WHITE);
	}
}	
