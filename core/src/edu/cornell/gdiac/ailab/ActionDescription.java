package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class ActionDescription {
	
	private static final float NAME_Y_RATIO = 0.8f;
	
	private static final float CASTTIME_Y_RATIO = 0.6f;
	
	private static final float DESCRIPTION1_Y_RATIO = 0.4f;
	
	private static final float DESCRIPTION2_Y_RATIO = 0.2f;
	
	private static final float DESCRIPTION_Y_RATIO = 0.4f;
	
	private static final Texture DESCRIPTION_BACKGROUND = new Texture("models/description_background.png");
	
	public void draw(GameCanvas canvas, Action action, float x, float y, float width, float height){
		float h = canvas.getHeight();
		float middle_x = x + width/2;
		canvas.drawTexture(DESCRIPTION_BACKGROUND, x, y, width, height, Color.WHITE);
		float iconSize = width/3;
		canvas.drawCenteredTexture(action.menuIcon, middle_x, y+height,iconSize,iconSize, Color.WHITE);
		
		float name_y = y+NAME_Y_RATIO*height;
		canvas.drawCenteredText(action.name, middle_x,name_y, Color.WHITE);
		
		float cost_y = y+CASTTIME_Y_RATIO*height;
		canvas.drawCenteredText("CASTTIME: "+action.cost, middle_x,cost_y, Color.WHITE);
		
		float damage_y = y+DESCRIPTION1_Y_RATIO*height;
		canvas.drawCenteredText("DAMAGE: "+action.damage, middle_x,damage_y, Color.WHITE);
		float range_y = y+DESCRIPTION2_Y_RATIO*height;
		canvas.drawCenteredText("RANGE: "+action.range, middle_x,range_y, Color.WHITE);
		
		//figure out what each attack should say
		
		/*
		float dmg_y = (RELATIVE_DESCRIPTION_Y_POS+relative_offset*2)*h;
		canvas.drawCenteredText("DMG: "+action.damage, middle_x,dmg_y, Color.WHITE);*/
		//figure out why its offset wrong in characterselect menu
	}
	

}
