package edu.cornell.gdiac.ailab;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class LevelEditor {

private Table table;
private TextButton submit;
	
	public LevelEditor () {
		Skin skin = new Skin();	
		Label nameLabel = new Label("Name:", skin);
		TextField nameText = new TextField("", skin);
	
		Label alliesLabel = new Label("Allies (as csv of tuples of (char_id, x_position, y_position)):", skin);
		TextField alliesText = new TextField("", skin);
		
		Label enemiesLabel = new Label("Enemies (as csv of tuples of (char_id, x_position, y_position, difficulty)):", skin);
		TextField enemiesText = new TextField("", skin);
		
		Label nextLabel = new Label("Next Level:", skin);
		TextField nextText = new TextField("", skin);
		
		Label widthLabel = new Label("Board Width:", skin);
		Slider widthSlider = new Slider(0f, 20f, 1f, false, skin);
		
		Label heightLabel = new Label("Board Height:", skin);
		Slider heightSlider = new Slider(0f, 20f, 1f, false, skin);
		
		
				
		
		Label textureLabel = new Label("Board Texture:", skin);
		TextField textureText = new TextField("", skin);
		
		submit = new TextButton("Submit", skin);
		
		table = new Table();
		
		table.add(nameLabel);
		table.add(nameText).width(100);
		table.row();
		
		table.add(alliesLabel);
		table.add(alliesText).width(300);
		table.row();
		
		table.add(enemiesLabel);
		table.add(enemiesText).width(300);
		table.row();
		
		table.add(nextLabel);
		table.add(nextText).width(100);
		table.row();
		
		table.add(widthLabel);
		table.add(widthSlider).width(100);
		table.row();
		
		table.add(heightLabel);
		table.add(heightSlider).width(100);
		table.row();
		
		table.add(textureLabel);
		table.add(textureText).width(100);
		table.row();
		
		table.add(submit);
	}
	
	//NOT USING THIS FOR NOW. 
	/**Set up allies and enemies for table. Assumed that table is not null.*/
	private void setUpAlliesAndEnemies (Skin skin, HashMap<Integer, HashMap<String, Object>> characters) {
		for (HashMap<String, Object> character : characters.values()) {
			String name = (String) character.get("name");
			CheckBox charBox = new CheckBox(name, skin);
			
			Label xPosLabel = new Label("X Position:", skin);
			Slider xPosSlider = new Slider(0f, 5f, 1f, false, skin);
					
			Label yPosLabel = new Label("Y Position:", skin);
			Slider yPosSlider = new Slider(0f, 5f, 1f, false, skin);
			
			table.add(charBox);
			table.row();
			
			table.add(xPosLabel);
			table.add(xPosSlider);
			table.row();
			
			table.add(yPosLabel);
			table.add(yPosSlider);
			table.row();
			
			
		}
		
		
	}
	
	
}
