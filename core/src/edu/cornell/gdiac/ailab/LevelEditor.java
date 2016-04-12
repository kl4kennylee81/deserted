package edu.cornell.gdiac.ailab;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class LevelEditor {

	private static Float OBJ_WIDTH = 400f;
	private static Float PADDING = 4f;
	private Table table;
	private SelectBox<String> editSelect;
	private TextField idText;
	private DropDownTable alliesTable;
	private DropDownTable enemiesTable;
	private SelectBox<String> nextSelect;
	private TextField widthText;
	private TextField heightText;
	private TextField textureText;
	private TextField aiText;


	private TextButton submit;
	
	public LevelEditor (String[] editOps, String[] charIds,
						String[] addLabels) {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		Label editLabel = new Label("Edit:", skin);
		editSelect = new SelectBox<String>(skin);
		editSelect.setItems(editOps);
		
		Label idLabel = new Label("Id:", skin);
		idText = new TextField("", skin);
	
		Label alliesLabel = new Label("Allies:", skin);
		alliesTable = new DropDownTable(charIds, addLabels);
		
		Label enemiesLabel = new Label("Enemies:", skin);
		enemiesTable = new DropDownTable(charIds, addLabels);
		
		Label nextLabel = new Label("Next Level:", skin);
		nextSelect = new SelectBox<String>(skin);
		nextSelect.setItems(Arrays.copyOfRange(editOps, 1, editOps.length));
		
		Label widthLabel = new Label("Board Width:", skin);
		widthText = new TextField("", skin);
		
		Label heightLabel = new Label("Board Height:", skin);
		heightText = new TextField("", skin);
	
		Label textureLabel = new Label("Board Texture:", skin);
		textureText = new TextField("", skin);
		
		Label aiLabel = new Label("AI:", skin);
		aiText = new TextField("", skin);
		
		submit = new TextButton("Submit", skin);
		
		table = new Table();
		
		table.add(editLabel);
		table.add(editSelect).width(OBJ_WIDTH).pad(PADDING);
		table.row();
	
		table.add(idLabel);
		table.add(idText).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(alliesLabel);
		table.add(alliesTable).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(enemiesLabel);
		table.add(enemiesTable).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(nextLabel);
		table.add(nextSelect).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(widthLabel);
		table.add(widthText).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(heightLabel);
		table.add(heightText).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(textureLabel);
		table.add(textureText).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(aiLabel);
		table.add(aiText).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(submit);
		
	}
	
	
	public Table getTable(){
		return table;
	}
	
	public String getSelectedId () {
		return editSelect.getSelected();
	}
	
	public String getId() {
		return idText.getText();
	}
	
	public Integer[] getAllies() {
		return alliesTable.getIdValues();
	}
	
	public Integer[] getAlliesAddIntField(int index){
		return alliesTable.getIntFieldValues(index);
	}
	
	
	public Integer[] getEnemies() {
		return enemiesTable.getIdValues();
	}
	 
	public Integer[] getEnemiesAddIntField(int index){
		return enemiesTable.getIntFieldValues(index);
	}
	
	
	public String getNext() {
		return nextSelect.getSelected();
	}
	
	public Integer getWidth() {
		return Integer.parseInt(widthText.getText());
	}
	
	public Integer getHeight() {
		return Integer.parseInt(heightText.getText());
	}
	
	public String getTexture() {
		return textureText.getText();
	}
	
	public String getAI() {
		return aiText.getText();
	}
	
	public boolean submitWasClicked() {
		return submit.isPressed();
	}
	
	public void setUpEdit(String[] allies, String[][] alliesAddtl, String[] enemies, 
						String[][] enemiesAddtl, String next, String width, 
					String height, String texture, String ai) {
		idText.setText(editSelect.getSelected());
		alliesTable.setValues(allies, alliesAddtl);
		enemiesTable.setValues(enemies, enemiesAddtl);
		nextSelect.setSelected(next);
		widthText.setText(width);
		heightText.setText(height);
		textureText.setText(texture);
		aiText.setText(ai);
	}
	
	public void setUpAdd() {
		idText.setText("");
		alliesTable.setValues(null, null);
		enemiesTable.setValues(null, null);
		nextSelect.setSelected("");
		widthText.setText("");
		heightText.setText("");
		textureText.setText("");
		aiText.setText("");
	}
	
}
