package edu.cornell.gdiac.ailab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
	
	public CharacterEditorController() throws IOException{
		setRoot();
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
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
	
	/** TAKEN FROM http://www.uofr.net/~greg/java/get-resource-listing.html*/
	String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
	      URL dirURL = clazz.getClassLoader().getResource(path);
	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
	        /* A file path: easy enough */
	        return new File(dirURL.toURI()).list();
	      } 

	      if (dirURL == null) {
	        /* 
	         * In case of a jar file, we can't actually find a directory.
	         * Have to assume the same jar as clazz.
	         */
	        String me = clazz.getName().replace(".", "/")+".class";
	        dirURL = clazz.getClassLoader().getResource(me);
	      }
	      
	      if (dirURL.getProtocol().equals("jar")) {
	        /* A JAR path */
	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	        while(entries.hasMoreElements()) {
	          String name = entries.nextElement().getName();
	          if (name.startsWith(path)) { //filter according to the path
	            String entry = name.substring(path.length());
	            int checkSubdir = entry.indexOf("/");
	            if (checkSubdir >= 0) {
	              // if it is a subdirectory, we just return the directory name
	              entry = entry.substring(0, checkSubdir);
	            }
	            result.add(entry);
	          }
	        }
	        return result.toArray(new String[result.size()]);
	      } 
	        
	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	  }
}
