package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class ActionEditor {
	
	private Float OBJECT_WIDTH = 300f;
	
	private Table table;
	private SelectBox<String> editSelect;
	private Label idText;
	private TextField nameText;
	private TextField costText;
	private TextField damageText;
	private TextField rangeText;
	private SelectBox<String> patternSelect;
	private SelectBox<String> typeSelect;
	private TextField effectNameText;
	private TextField roundText;
	private TextField magnitudeText;
	private TextField descriptNameText;
	private TextField persistRoundText;
	private TextField persistSpeedText;
	private SelectBox<String> animationSelect;
	
	
	private TextButton submit;
	
	public ActionEditor (String[] opts, String[] animIds, String newId) {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		Label editLabel = new Label("Edit:", skin);
		editSelect = new SelectBox<String>(skin);
		editSelect.setItems(opts);
		
		Label idLabel = new Label("Id:", skin);
		idText = new Label(newId, skin);
		
		Label nameLabel = new Label("Name:", skin);
		nameText = new TextField("", skin);
		
		Label costLabel = new Label("Cost:", skin);
		costText = new TextField("", skin);
		
		Label damageLabel = new Label("Damage:", skin);
		damageText = new TextField("", skin);
		
		Label rangeLabel = new Label("Range:", skin);
		rangeText = new TextField("", skin);
		
		Label patternLabel = new Label("Pattern:", skin);
		patternSelect = new SelectBox<String>(skin);
		String[] patterns = {"MOVE", "SHIELD", "STRAIGHT",
						"DIAGONAL", "SINGLE", "NOP",
						"PROJECTILE", "INSTANT"};
		patternSelect.setItems(patterns);
		
		
		Label typeLabel = new Label("Effect Type:", skin);
		typeSelect = new SelectBox<String>(skin);
		String[] types = {"REGULAR", "SPEED"};
		typeSelect.setItems(types);		
		
		Label effectNameLabel = new Label("Effect Name:", skin);
		effectNameText = new TextField("", skin);
		
		Label effectRoundsLabel = new Label("Number of Rounds:", skin);
		roundText = new TextField("", skin);
		
		Label magnitudeLabel = new Label("Magnitude:", skin);
		magnitudeText = new TextField("", skin);
		
		Label descriptLabel = new Label("Description:", skin);
		descriptNameText = new TextField("", skin);
		
		
		Label persistRoundsLabel = new Label("Number of Rounds Persisting:", skin);
		persistRoundText = new TextField("", skin);

		
		Label persistSpeedLabel = new Label("Persisting Move Speed:", skin);
		persistSpeedText = new TextField("", skin);

		
		Label animationLabel = new Label("Animation Id:", skin);
		animationSelect = new SelectBox<String>(skin);
		animationSelect.setItems(animIds);

		submit = new TextButton("Submit", skin);
		
		table = new Table();
		
		table.add(editLabel);
		table.add(editSelect).width(OBJECT_WIDTH);
		table.row();
		
		table.add(idLabel);
		table.add(idText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(nameLabel);
		table.add(nameText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(costLabel);
		table.add(costText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(damageLabel);
		table.add(damageText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(rangeLabel);
		table.add(rangeText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(patternLabel);
		table.add(patternSelect).width(OBJECT_WIDTH);
		table.row();
		
		table.add(typeLabel);
		table.add(typeSelect).width(OBJECT_WIDTH);
		table.row();
		
		table.add(effectNameLabel);
		table.add(effectNameText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(effectRoundsLabel);
		table.add(roundText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(magnitudeLabel);
		table.add(magnitudeText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(descriptLabel);
		table.add(descriptNameText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(persistRoundsLabel);
		table.add(persistRoundText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(persistSpeedLabel);
		table.add(persistSpeedText).width(OBJECT_WIDTH);
		table.row();
		
		table.add(animationLabel);
		table.add(animationSelect).width(OBJECT_WIDTH);
		table.row();
		
		table.add(submit);
	}
	
	public Table getTable(){
		return table;
	}
	
	public String getSelectedId () {
		return editSelect.getSelected();
	}
	
	public Integer getId() {
		return Integer.parseInt(idText.getText().toString());
	}
	
	public String getName() {
		return nameText.getText();
	}
	
	public String getCost() {
		return costText.getText();
	}
	
	public String getDamage() {
		return damageText.getText();
	}
	
	public String getRange() {
		return rangeText.getText();
	}
	
	public String getPattern() {
		return patternSelect.getSelected();
	}
	
	public String getType() {
		return typeSelect.getSelected();
	}
	
	public String getEffectName() {
		return effectNameText.getText();
	}
	
	public String getRounds() {
		return roundText.getText();
	}
	
	public String getMagnitude() {
		return magnitudeText.getText();
	}
	
	public String getDescription() {
		return descriptNameText.getText();
	}
	
	public String getPersistRound() {
		return persistRoundText.getText();
	}
	
	public String getPersistSpeed() {
		return persistSpeedText.getText();
	}
	
	public String getAnimation() {
		String selected = animationSelect.getSelected();
		String id = selected.substring(0, selected.indexOf(" "));
		return id;
	}
	
	public boolean submitWasClicked() {
		return submit.isPressed();
	}
	
	
	public void setUpEdit(String id, String name, String cost, String damage, String range,
			String pattern, String type, String effectName, String rounds,
			String magnitude, String description, Integer persistRound, 
			Double persistSpeed, String animation) {
		idText.setText(id);
		nameText.setText(name);
		costText.setText(cost);
		damageText.setText(damage);
		rangeText.setText(range);
		patternSelect.setSelected(pattern);
		typeSelect.setSelected(type);
		effectNameText.setText(effectName);
		roundText.setText(rounds);
		magnitudeText.setText(magnitude);
		descriptNameText.setText(description);
		if (persistRound != null && persistSpeed != null){
			persistRoundText.setText(persistRound.toString());
			persistSpeedText.setText(persistSpeed.toString());
		}else{
			persistRoundText.setText("");
			persistSpeedText.setText("");
		}
		animationSelect.setSelected(animation);
		
	}
	
	public void setUpAdd(String newId) {
		idText.setText(newId);
		nameText.setText("");
		costText.setText("");
		damageText.setText("");
		rangeText.setText("");
		patternSelect.setSelectedIndex(0);
		typeSelect.setSelectedIndex(0);
		effectNameText.setText("");
		roundText.setText("");
		magnitudeText.setText("");
		descriptNameText.setText("");
		persistRoundText.setText("");
		persistSpeedText.setText("");
		animationSelect.setSelectedIndex(0);
	}
}
