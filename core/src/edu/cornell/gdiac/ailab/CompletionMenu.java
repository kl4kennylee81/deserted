package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class CompletionMenu extends Menu {
	
	Texture background;
	
	Texture optionHighlight;
	
	private static final float VSELECT_X_POS = 0.4f;
	
	private static final float VSELECT_Y_POS = 0.16f;
	
	private static final float VSELECT_WIDTH = 0.2f;
	
	private static final float VSELECT_HEIGHT = 0.15f;
	
	private static final float RELATIVE_HIGHLIGHT_X_OFFSET = 0.02f;
	
	public CompletionMenu(){

		Option[] ops = new Option[2];
		Option nl = new Option("Next Level", "Next Level");
		nl.setBounds(0.43f, 0.28f, 0.17f, 0.05f);
		Option mm = new Option("Main Menu", "Main Menu");
		mm.setBounds(0.43f, 0.23f, 0.17f, 0.05f);
		ops[0] = nl;
		ops[1] = mm;
		
		this.options = ops;
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
	}
	
	public void draw(GameCanvas canvas, float w, float h) {
		float bgX = VSELECT_X_POS * w;
		float bgY = VSELECT_Y_POS * h;
		float bgWidth = VSELECT_WIDTH * w;
		float bgHeight = VSELECT_HEIGHT * h;
		
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
	
	public void setWin(boolean won){
		Option topOption = this.options[0];
		if (won){
			topOption.setText("Next Level");
		}else{
			topOption.setText("Retry");
		}
	}
	
	public void setTopText(String s){
		Option topOption = this.options[0];
		topOption.setText(s);
	}

	@Override
	public void draw(GameCanvas canvas) {
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		draw(canvas, w, h);
		
	}

}
