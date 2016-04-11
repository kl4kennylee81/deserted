package edu.cornell.gdiac.ailab;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CharacterEditorController implements EditorController {
	private Stage stage;
	private Yaml yaml;
	private String currentSelection;
	private Integer nextId;
	private HashMap<Integer, HashMap<String, Object>> chars;
	private HashMap<Integer, HashMap<String, Object>> animations;
	private HashMap<Integer, HashMap<String, Object>> actions;
	private CharacterEditor charEdit;
	
	public CharacterEditorController() throws IOException{
		yaml = new Yaml();
		loadChars();
		stage = new Stage();
		charEdit = new CharacterEditor(getIds(), getAnimationIds(), 
						getActions(), nextId.toString());
		
		Table table = charEdit.getTable();
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		//table.setDebug(true);
		currentSelection = "Add a new character";
	}
	
	public void update() {
		String selection = charEdit.getSelectedId();
		if ( !currentSelection.equals(selection) ){
			setUpIdEdit(selection);
			currentSelection = selection;
		}
		
		if ( charEdit.submitWasClicked() ){
			Integer id = charEdit.getId();
			addNewEntry(id);
			writeCharsToFile();
			if ( nextId.equals(id) ){
				nextId++;
			}
		}
	}
	
	public void draw() {
		stage.act();
		stage.draw();
	}
	
	
	@SuppressWarnings("unchecked")
	private void loadChars() throws IOException{
		FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		try (InputStream is = charFile.read()){
			chars = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
	}
	
	
	private String[] getIds() {
		Set<Integer> keys = chars.keySet();
		Integer num_ids = keys.size();
		String[] options = new String[num_ids + 1];
		options[0] = "Add a new character";
		int i = 1;
		for (Integer id : keys){
			options[i] = id.toString()+ " " + chars.get(id).get("name");
			i++;
		}
		nextId = num_ids;
		return options;
	}
	
	@SuppressWarnings("unchecked")
	private String[] getAnimationIds() throws IOException {
		FileHandle animationsFile = Gdx.files.internal("yaml/animations.yml"); 
		try (InputStream is = animationsFile.read()){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		Set<Integer> keys = animations.keySet();
		Integer num_ids = keys.size();
		String[] ids = new String[num_ids];
		int i = 0;
		for (Integer id : keys){
			ids[i] = id.toString()+ " " + animations.get(id).get("name");
			i++;
		}
		return ids;
		
	}
	
	private void addNewEntry(Integer id) {
		String name = charEdit.getName();
		Integer health = Integer.parseInt(charEdit.getHealth());
		Integer maxHealth = Integer.parseInt(charEdit.getMaxHealth());
		String hex = charEdit.getHex();
		Float speed = Float.parseFloat(charEdit.getSpeed());
		Float castSpeed = Float.parseFloat(charEdit.getCastSpeed());
		Integer[] actions = charEdit.getActions();
	
		
		String texture = charEdit.getTexture();
		String icon = charEdit.getIcon();
		String animation = charEdit.getAnimation();
		
		HashMap<String, Object> entry = new HashMap<String, Object>();
		entry.put("name", name);
		entry.put("health", health);
		entry.put("maxHealth", maxHealth);
		entry.put("hexColor", hex);
		entry.put("speed", speed);
		entry.put("castSpeed", castSpeed);
		entry.put("availableActions", actions);
		entry.put("texture", texture);
		entry.put("icon", icon);
		entry.put("animationId", animation);
		chars.put(id, entry);
	}
	
	private void writeCharsToFile(){
		FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		FileWriter writer = (FileWriter) charFile.writer(false);
		yaml.dump(chars, writer);
	}
	
	@SuppressWarnings("unchecked")
	private void setUpIdEdit(String id) {
		if (id.equals("Add a new character") ){
			charEdit.setUpAdd(nextId.toString());
		}else{
			id = id.substring(0, id.indexOf(" "));
			HashMap<String, Object> character = chars.get(Integer.parseInt(id));
			String name = (String) character.get("name");
			Integer health = (Integer) character.get("health");
			Integer maxHealth = (Integer) character.get("maxHealth");
			String hexColor = (String) character.get("hexColor");
			Double speed = (Double) character.get("speed");
			Double castSpeed = (Double) character.get("castSpeed");
			ArrayList<Integer> availActions = (ArrayList<Integer>) character.get("availableActions");
			
			Integer[] actionInts = availActions.toArray(new Integer[availActions.size()]);
			
			String[] actionArray = convertActionsFormat(actionInts);
			
			String charTextureName = (String) character.get("texture");
			String iconTextureName = (String) character.get("icon");

			Integer animationId = (Integer) character.get("animationId");
			String animation = animationId + " " + animations.get(animationId).get("name");
			charEdit.setUpEdit(id, name, health.toString(), maxHealth.toString(), 
					hexColor, speed.toString(), castSpeed.toString(), actionArray, charTextureName, 
					iconTextureName, animation);
		}
		
	}
	
//	private String[] getModels(){
//		FileHandle modelDir = Gdx.files.internal("models");
//		modelDir.
//		
//	}
	
	private String[] convertActionsFormat(Integer[] availActions){
		String[] options = new String[availActions.length];
		int i = 0;
		for (Integer id : availActions){
			options[i] = id.toString()+ " " + actions.get(id).get("name");
			i++;
		}
		return options;
	}
	
	@SuppressWarnings("unchecked")
	private String[] getActions() throws IOException{
		FileHandle actionsFile = Gdx.files.internal("yaml/actions.yml"); 
		try (InputStream is = actionsFile.read()){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		Set<Integer> keys = actions.keySet();
		Integer num_ids = keys.size();
		String[] options = new String[num_ids];
		int i = 0;
		for (Integer id : keys){
			options[i] = id.toString()+ " " + actions.get(id).get("name");
			i++;
		}
		return options;
	}
	
}
