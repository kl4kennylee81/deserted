package edu.cornell.gdiac.ailab;



import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class CharacterEditor {
	
	private Float OBJ_WIDTH = 400f;
	private Float OBJECT_HEIGHT = 20f;
	private Float PADDING = 4f;
	
	private Table table;
	private SelectBox<String> editSelect;
	private Label idText;
	private TextField nameText;
	private TextField healthText;
	private TextField maxHealthText;
	private TextField hexText;
	private TextField speedText;
	private TextField castSpeedText;
	private DropDownTable actionsTable;
	private SelectBox<String> textureSelect;
	private SelectBox<String> iconSelect;
	private SelectBox<String> animationSelect;
	
	
	private TextButton submit;
	private TextButton back;
	
	private boolean submitClicked;
	private boolean backClicked;
	
	public CharacterEditor (String[] opts, String[] animIds, 
							String[] actions, 
							String[] models, String newId) {
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		Label editLabel = new Label("Edit:", skin);
		editSelect = new SelectBox<String>(skin);
		editSelect.setItems(opts);
		
		
		Label idLabel = new Label("Id:", skin);
		idText = new Label(newId, skin);
		
		Label nameLabel = new Label("Name:", skin);
		nameText = new TextField("", skin);
		
		Label healthLabel = new Label("Health:", skin);
		healthText = new TextField("", skin);
		
		Label maxHealthLabel = new Label("Maximum Health:", skin);
		maxHealthText = new TextField("", skin);
			
		Label hexLabel = new Label("Hex Color:", skin);
		hexText = new TextField("", skin);
		
		Label speedLabel = new Label("Speed:", skin);
		speedText = new TextField("", skin);
		
		
		Label castSpeedLabel = new Label("Cast Speed:", skin);
		castSpeedText = new TextField("", skin);	
		
		Label actionsLabel = new Label("Actions:", skin);
		actionsTable = new DropDownTable(actions, null);
		
		Label textureLabel = new Label("Texture:", skin);
		textureSelect = new SelectBox<String>(skin);
		textureSelect.setItems(models);
		
		Label iconLabel = new Label("Icon:", skin);
		iconSelect = new SelectBox<String>(skin);
		iconSelect.setItems(models);
		
		Label animationLabel = new Label("Animation Id:", skin);
		animationSelect = new SelectBox<String>(skin);
		animationSelect.setItems(animIds);

		submit = new TextButton("Submit", skin);
		back = new TextButton("Back", skin);
		
		table = new Table();
		
		table.add(editLabel);
		table.add(editSelect).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(idLabel);
		table.add(idText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(nameLabel);
		table.add(nameText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(healthLabel);
		table.add(healthText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(maxHealthLabel);
		table.add(maxHealthText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(hexLabel);
		table.add(hexText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(speedLabel);
		table.add(speedText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(castSpeedLabel);
		table.add(castSpeedText).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		
		table.add(actionsLabel);
		table.add(actionsTable).width(OBJ_WIDTH).pad(PADDING);
		table.row();
		
		table.add(textureLabel);
		table.add(textureSelect).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(iconLabel);
		table.add(iconSelect).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(animationLabel);
		table.add(animationSelect).width(OBJ_WIDTH).height(OBJECT_HEIGHT).pad(PADDING);
		table.row();
		
		table.add(submit);
		table.add(back);
	}
	
	public Table getTable() {
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
	
	public String getHealth() {
		return healthText.getText();
	}
	
	public String getMaxHealth() {
		return maxHealthText.getText();
	}
	
	public String getHex() {
		return hexText.getText();
	}
	
	public String getSpeed() {
		return speedText.getText();
	}
	
	public String getCastSpeed() {
		return castSpeedText.getText();
	}
	
	public Integer[] getActions() {
		return actionsTable.getIdValues();
	}
	
	public String getTexture() {
		return textureSelect.getSelected();
	}
	
	public String getIcon() {
		return iconSelect.getSelected();
	}
	
	public String getAnimation() {
		String selected = animationSelect.getSelected();
		String id = selected.substring(0, selected.indexOf(" "));
		return id;
	}
	
	public boolean submitWasClicked() {
		if (submit.isPressed()){
			submitClicked = submit.isPressed();
		}
		if (InputController.pressedLeftMouse() &&submitClicked){
			submitClicked = false;
			return true;
		}
		return false;
	}
	
	public boolean backWasClicked() {
		if (back.isPressed()){
			backClicked = back.isPressed();
		}
		if (InputController.pressedLeftMouse() && backClicked){
			backClicked = false;
			return true;
		}
		return false;
	}
	
	public void setUpEdit(String id, String name, String health, String maxHealth, String hex, 
					String speed, String castSpeed, String[] actions, 
					String texture, String icon, String animation) {
		idText.setText(id);
		nameText.setText(name);
		healthText.setText(health);
		maxHealthText.setText(maxHealth);
		hexText.setText(hex);
		speedText.setText(speed);
		castSpeedText.setText(castSpeed);
		actionsTable.setValues(actions, null);
		textureSelect.setSelected(texture);
		iconSelect.setSelected(icon);
		animationSelect.setSelected(animation);
	}
	
	public void setUpAdd(String newId) {
		idText.setText(newId);
		nameText.setText("");
		healthText.setText("");
		maxHealthText.setText("");
		hexText.setText("");
		speedText.setText("");
		castSpeedText.setText("");
		actionsTable.setValues(null, null);
		textureSelect.setSelectedIndex(0);
		iconSelect.setSelectedIndex(0);
		animationSelect.setSelectedIndex(0);
	}
	
	
}
