package edu.cornell.gdiac.ailab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CharacterEditorController implements EditorController {
	private static File ROOT;
	private Stage stage;
	private Yaml yaml;
	private String currentSelection;
	private Integer nextId;
	private HashMap<Integer, HashMap<String, Object>> chars;
	private HashMap<Integer, HashMap<String, Object>> animations;
	private HashMap<Integer, HashMap<String, Object>> actions;
	private CharacterEditor charEdit;
	private boolean isDone;
	
	public CharacterEditorController() throws IOException{
		setRoot();
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
		loadChars();
		stage = new Stage();
		String[] models = listFiles("models", ".png");
		charEdit = new CharacterEditor(getIds(), getAnimationIds(), 
						getActions(), models, nextId.toString());
		
		Table table = charEdit.getTable();
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		//table.setDebug(true);
		currentSelection = "Add a new character";
		isDone = false;
	}
	
//	http://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file
	private String[] listFiles(String dir, String ext) throws IOException {
		String uri = CharacterEditor.class.getResource("CharacterEditor.class").toString();
		LinkedList<String> result = new LinkedList<String>();
		if (!uri.substring(0, 3).equals("jar")) {
			File directory = new File(ROOT,"core/assets/"+dir);
			for (String item :directory.list()) {
				result.add(dir+ "/" +item);
			}
			return result.toArray(new String[result.size()]);
		}else{
			CodeSource src = CharacterEditor.class.getProtectionDomain().getCodeSource();
			if (src != null) {
			  URL jar = src.getLocation();
			  ZipInputStream zip = new ZipInputStream(jar.openStream());
			  ZipEntry e = zip.getNextEntry();
			  result = new LinkedList<String>();
			  while(e !=null) {
			    String name = e.getName();
			    if (name.contains(dir) && name.contains(ext)) {
			    	result.add(name);
			      System.out.println(name);
			    }
			    e = zip.getNextEntry();
			  }
			  return result.toArray(new String[result.size()]);
			} 
		}
		return null;
		
	}
	
	
	/**Code taken from http://stackoverflow.com/questions/5527744/java-jar-writing-to-a-file 
	 * @throws URISyntaxException */
	public void setRoot() {
		// Find out where the JAR is:
		String path = null;
		try {
			path = CharacterEditor.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//add to make work for eclipse
		String uri = CharacterEditor.class.getResource("CharacterEditor.class").toString();
		if (!uri.substring(0, 3).equals("jar")) {
			path = path.substring(0, path.lastIndexOf('/'));
			path = path.substring(0, path.lastIndexOf('/'));
		}
		path = path.substring(0, path.lastIndexOf('/')+1);
		
		// Create the project-folder-file:
		ROOT = new File(path);
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
			reset();
		}
		if ( charEdit.backWasClicked() ){
			stage.dispose();
			isDone=true;
		}
	}
	
	public void draw() {
		stage.act();
		stage.draw();
	}
	
	public boolean isDone() {
		return isDone;
	}
	
	private void reset() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
		try {
			loadChars();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stage = new Stage();
		try {
			String[] models = listFiles("models", ".png");
			charEdit = new CharacterEditor(getIds(), getAnimationIds(), 
							getActions(), models, nextId.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Table table = charEdit.getTable();
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		//table.setDebug(true);
		currentSelection = "Add a new character";
	}
	
	@SuppressWarnings("unchecked")
	private void loadChars() throws IOException{
		//FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		File charFile = new File(ROOT, "yaml/characters.yml");
		
		try (FileInputStream is = new FileInputStream(charFile)){
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
		//FileHandle animationsFile = Gdx.files.internal("yaml/animations.yml"); 
		File animationsFile = new File(ROOT, "yaml/animations.yml");
		try (FileInputStream is = new FileInputStream(animationsFile)){
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
		Integer animation = Integer.parseInt(charEdit.getAnimation());
		
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
	
	private void writeCharsToFile() {
		//FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		File charFile = new File(ROOT, "yaml/characters.yml");
		FileWriter writer = null;;
		try {
			writer = new FileWriter(charFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		//FileHandle actionsFile = Gdx.files.internal("yaml/actions.yml");
		File actionsFile = new File(ROOT, "yaml/actions.yml");
		try (FileInputStream is = new FileInputStream(actionsFile)){
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
