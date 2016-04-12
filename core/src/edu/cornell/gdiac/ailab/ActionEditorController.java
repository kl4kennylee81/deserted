package edu.cornell.gdiac.ailab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class ActionEditorController implements EditorController {
	private static File ROOT;
	private Stage stage;
	private Yaml yaml;
	
	private String currentSelection;
	private Integer nextId;
	private HashMap<Integer, HashMap<String, Object>> actions;
	private HashMap<Integer, HashMap<String, Object>> animations;
	private ActionEditor actionEdit;
	
	public ActionEditorController() throws IOException{
		setRoot();
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
		loadActions();
		stage = new Stage();
		actionEdit = new ActionEditor(getIds(), getAnimationIds(), nextId.toString());
		
		Table table = actionEdit.getTable();
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		currentSelection = "Add a new action";
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
		String selection = actionEdit.getSelectedId();
		if ( !currentSelection.equals(selection) ){
			setUpIdEdit(selection);
			currentSelection = selection;
		}
		
		if ( actionEdit.submitWasClicked() ){
			Integer id = actionEdit.getId();
			addNewEntry(id);
			writeActionsToFile();
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
	private void loadActions() throws IOException{
		File actionFile = new File(ROOT, "yaml/actions.yml");
		try (InputStream is = new FileInputStream(actionFile)){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
	}
	
	
	private String[] getIds() {
		Set<Integer> keys = actions.keySet();
		Integer num_ids = keys.size();
		String[] options = new String[num_ids + 1];
		options[0] = "Add a new action";
		int i = 1;
		for (Integer id : keys){
			options[i] = id.toString()+ " " + actions.get(id).get("name");
			i++;
		}
		nextId = num_ids;
		return options;
	}
	
	@SuppressWarnings("unchecked")
	private String[] getAnimationIds() throws IOException {
		File animationsFile = new File(ROOT, "yaml/animations.yml"); 
		try (InputStream is = new FileInputStream(animationsFile)){
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
		String name = actionEdit.getName();
		Integer cost = Integer.parseInt(actionEdit.getCost());
		Integer damage = Integer.parseInt(actionEdit.getDamage());
		Integer range = Integer.parseInt(actionEdit.getRange());
		String pattern = actionEdit.getPattern();
		String type = actionEdit.getType();
		String effectName = actionEdit.getEffectName();
		Integer rounds = Integer.parseInt(actionEdit.getRounds());
		Integer magnitude = Integer.parseInt(actionEdit.getMagnitude());
		String description = actionEdit.getDescription();
		String animation = actionEdit.getAnimation();
		Integer persistingRounds = Integer.parseInt(actionEdit.getPersistRound());
		Float persistingSpeed = Float.parseFloat(actionEdit.getPersistSpeed());
		
		HashMap<String, Object> entry = new HashMap<String, Object>();
		entry.put("name", name);
		entry.put("cost", cost);
		entry.put("damage", damage);
		entry.put("range", range);
		entry.put("pattern", pattern);
		
		HashMap<String, Object> effect = new HashMap<String, Object>();
		effect.put("type", type);
		effect.put("name", effectName);
		effect.put("numRounds", rounds);
		effect.put("magnitude", magnitude);
		entry.put("effect", effect);
		entry.put("description", description);
		
		HashMap<String, Object> persisting = new HashMap<String, Object>();
		persisting.put("numRounds", persistingRounds);
		persisting.put("moveSpeed", persistingSpeed);
		entry.put("persisting_action", persisting);
		entry.put("animationId", animation);
		actions.put(id, entry);
	}
	
	private void writeActionsToFile(){
		File actionFile = new File(ROOT, "yaml/actions.yml");
		FileWriter writer = null;
		try {
			writer = new FileWriter(actionFile, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		yaml.dump(actions, writer);
	}
	
	@SuppressWarnings("unchecked")
	private void setUpIdEdit(String id) {
		if (id.equals("Add a new action") ){
			actionEdit.setUpAdd(nextId.toString());
		}else{
			id = id.substring(0, id.indexOf(" "));
			HashMap<String, Object> action = actions.get(Integer.parseInt(id));

			String name = (String) action.get("name");
			Integer cost = (Integer) action.get("cost");
			Integer damage = (Integer) action.get("damage");
			Integer range = (Integer) action.get("range");
			String pattern = (String) action.get("pattern");
			
			HashMap<String, Object> effect = (HashMap<String, Object>) action.get("effect");
			String type = (String) effect.get("type");
			String effectName = (String) effect.get("name");
			Integer numRounds = (Integer) effect.get("numRounds");
			Integer magnitude = (Integer) effect.get("magnitude");
			
			String description = (String) action.get("description");
			
			HashMap<String, Object> persisting = (HashMap<String, Object>) action.get("persisting_action");
			Integer persistingRounds = null;
			Double persistingSpeed = null;
			
			if (persisting != null){
				persistingRounds = (Integer) persisting.get("numRounds");
				persistingSpeed = (Double) persisting.get("moveSpeed");
			}
			
			Integer animationId = (Integer) action.get("animationId");
			String animation = animationId + " " + animations.get(animationId).get("name");
			actionEdit.setUpEdit(id, name, cost.toString(), damage.toString(), 
					range.toString(), pattern, type, effectName, numRounds.toString(), 
					magnitude.toString(), description, persistingRounds, 
					persistingSpeed, animation);
		}
		
	}
}
