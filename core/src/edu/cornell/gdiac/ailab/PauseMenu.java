package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class PauseMenu extends Menu {

	Texture background;
	
	Texture optionHighlight;
	
	private static final float BACKGROUND_X_POS = 0.35f;
	
	private static final float BACKGROUND_Y_POS = 0.5f;
	
	private static final float BACKGROUND_WIDTH = 0.3f;
	
	private static final float BACKGROUND_HEIGHT = 0.2f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	private static PauseMenu instance = null;
	
	protected PauseMenu(){
		Option[] ops = new Option[3];
		Option rg = new Option("Resume Game", "Resume Game");
		rg.setBounds(0.4f, 0.67f, 0.23f, 0.05f);
		Option retry = new Option("Retry", "Retry");
		retry.setBounds(0.4f, 0.62f, 0.23f, 0.05f);
		Option mm = new Option("Main Menu", "Main Menu");
		mm.setBounds(0.4f, 0.57f, 0.23f, 0.05f);
		ops[0] = rg;
		ops[1] = retry;
		ops[2] = mm;
		
		this.options = ops;
	}

	public static PauseMenu getInstance(){
		if (instance == null) {
			instance = new PauseMenu();
		}
		return instance;
	}
	
	public void setBackground(Texture bg){
		background = bg;
	}
	
	public void setHighlight(Texture h){
		optionHighlight = h;
	}
	
	public void reset(){
		for (Option opt : this.options){
			opt.isSelected = false;
		}
		setOption(0);
	}
	
	public void draw(GameCanvas canvas, float w, float h) {
		float bgX = BACKGROUND_X_POS * w;
		float bgY = BACKGROUND_Y_POS * h;
		float bgWidth = BACKGROUND_WIDTH * w;
		float bgHeight = BACKGROUND_HEIGHT * h;
		
		canvas.drawTexture(background, bgX, bgY, bgWidth, bgHeight, Color.WHITE);
		
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
	
	
	@Override
	public void draw(GameCanvas canvas) {
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		draw(canvas, w, h);
		
	}
	
}
