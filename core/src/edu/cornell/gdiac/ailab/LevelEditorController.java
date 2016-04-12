package edu.cornell.gdiac.ailab;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class LevelEditorController implements EditorController {

	private Stage stage;
	private Yaml yaml;
	private String currentSelection;
	private HashMap<String, HashMap<String, Object>> levels;
	private HashMap<Integer, HashMap<String, Object>> characters;
	private LevelEditor levelEdit;
	
	public LevelEditorController() throws IOException{
		yaml = new Yaml();
		loadLevelsAndChars();
		stage = new Stage();
		String[] labels = new String[2];
		labels[0] = "X Pos:";
		labels[1] = "Y Pos:";
		levelEdit = new LevelEditor(getIds(), getCharIds(), labels);
		
		Table table = levelEdit.getTable();
		table.setFillParent(true);
		stage.addActor(table);
		Gdx.input.setInputProcessor(stage);
		//table.setDebug(true);
		currentSelection = "Add a new level";
	}
	
	public void update() {
		String selection = levelEdit.getSelectedId();
		if ( !currentSelection.equals(selection) ){
			setUpIdEdit(selection);
			currentSelection = selection;
		}
		
		if ( levelEdit.submitWasClicked() ){
			String id = levelEdit.getId();
			addNewEntry(id);
			writeLevelsToFile();
		}
	}
	
	public void draw() {
		stage.act();
		stage.draw();
	}
	
	
	@SuppressWarnings("unchecked")
	private void loadLevelsAndChars() throws IOException{
		FileHandle levelFile = Gdx.files.internal("yaml/levels.yml");
		try (InputStream is = levelFile.read()){
			levels = (HashMap<String, HashMap<String, Object>>) yaml.load(is);
		}
		
		FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		try (InputStream is = charFile.read()){
			characters = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
	}
	
	
	private String[] getIds() {
		Set<String> keys = levels.keySet();
		Integer num_ids = keys.size();
		String[] options = new String[num_ids + 1];
		options[0] = "Add a new level";
		int i = 1;
		for (String id : keys){
			options[i] = id;
			i++;
		}
		return options;
	}
	
	private String[] getCharIds() {
		Set<Integer> keys = characters.keySet();
		Integer num_ids = keys.size();
		String[] options = new String[num_ids];
		int i = 0;
		for (Integer id : keys){
			options[i] = id.toString()+ " " + characters.get(id).get("name");
			i++;
		}
		return options;
	}
	
	private ArrayList<HashMap<String, Object>> generateAlliesEnemyAL(Integer[] allies,
												Integer[] xPos, Integer[] yPos) {
		ArrayList<HashMap<String, Object>> aL = new ArrayList<HashMap<String, Object>>();
		for (int i=0; i < allies.length; i++) {
			HashMap<String, Object> entry = new HashMap<String, Object>();
			entry.put("id", allies[i]);
			entry.put("xPosition", xPos[i]);
			entry.put("yPosition", yPos[i]);
			aL.add(entry);
		}
		
		return aL;
	}
	
	private void addNewEntry(String id) {
		Integer[] allies = levelEdit.getAllies();
		Integer[] xPos = levelEdit.getAlliesAddIntField(0);
		Integer[] yPos = levelEdit.getAlliesAddIntField(1);
		ArrayList<HashMap<String, Object>> allyList = generateAlliesEnemyAL(allies, xPos, yPos);
		
		Integer[] enemies = levelEdit.getAllies();
		Integer[] enemyXPos = levelEdit.getEnemiesAddIntField(0);
		Integer[] enemyYPos = levelEdit.getEnemiesAddIntField(1);
		ArrayList<HashMap<String, Object>> enemyList = generateAlliesEnemyAL(enemies, 
								enemyXPos, enemyYPos);
		
		String next = levelEdit.getNext();
		Integer width = levelEdit.getWidth();
		Integer height = levelEdit.getHeight();
		String texture = levelEdit.getTexture();
		String ai = levelEdit.getAI();
		
		HashMap<String, Object> entry = new HashMap<String, Object>();
		entry.put("allies", allyList);
		entry.put("enemies", enemyList);
		entry.put("nextLevel", next);
		entry.put("boardWidth", width);
		entry.put("boardHeight", height);
		entry.put("boardTexture", texture);
		entry.put("AI", ai);
		levels.put(id, entry);
	}
	
	private void writeLevelsToFile(){
		FileHandle levelFile = Gdx.files.internal("yaml/levels.yml");
		FileWriter writer = (FileWriter) levelFile.writer(false);
		yaml.dump(levels, writer);
	}
	
	@SuppressWarnings("unchecked")
	private void setUpIdEdit(String id) {
		if (id.equals("Add a new character") ){
			levelEdit.setUpAdd();
		}else{
			HashMap<String, Object> level = levels.get(id);
			
			ArrayList<HashMap<String, Object>> allyList = (ArrayList<HashMap<String, Object>>) level.get("allies");
			ArrayList<HashMap<String, Object>> enemyList = (ArrayList<HashMap<String, Object>>) level.get("enemies");
			
			String[][] completeAlly = getCharInfo(allyList);
			String[] allies = completeAlly[0];
			String[][] allyInfo = Arrays.copyOfRange(completeAlly, 1, 3);
			
			String[][] completeEnemy = getCharInfo(enemyList);
			String[] enemies = completeAlly[0];
			String[][] enemyInfo = Arrays.copyOfRange(completeAlly, 1, 3);
			
			String next = (String) level.get("nextLevel");
			String width = ((Integer) level.get("boardWidth")).toString();
			String height = ((Integer) level.get("boardWidth")).toString();
			String texture = (String) level.get("boardTexture");
			String ai = (String) level.get("AI");
			

			levelEdit.setUpEdit(allies, allyInfo, enemies, enemyInfo, next, width, height, texture, ai);
		}
		
	}
	
	private String[][] getCharInfo(ArrayList<HashMap<String, Object>> list) {
		String[][] array = new String[3][list.size()];
		int i=0;
		for (HashMap<String,Object> map : list) {
			array[0][i] = ((Integer) map.get("id")).toString();
			array[1][i] = ((Integer) map.get("xPosition")).toString();
			array[2][i] = ((Integer) map.get("yPosition")).toString();
		}
		return array;
	}
	
	
//	private String[] getModels(){
//		FileHandle modelDir = Gdx.files.internal("models");
//		modelDir.
//		
//	}
	
//	private String[] convertActionsFormat(Integer[] availActions){
//		String[] options = new String[availActions.length];
//		int i = 0;
//		for (Integer id : availActions){
//			options[i] = id.toString()+ " " + actions.get(id).get("name");
//			i++;
//		}
//		return options;
//	}
//	
//	@SuppressWarnings("unchecked")
//	private String[] getActions() throws IOException{
//		FileHandle actionsFile = Gdx.files.internal("yaml/actions.yml"); 
//		try (InputStream is = actionsFile.read()){
//			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
//		}
//		Set<Integer> keys = actions.keySet();
//		Integer num_ids = keys.size();
//		String[] options = new String[num_ids];
//		int i = 0;
//		for (Integer id : keys){
//			options[i] = id.toString()+ " " + actions.get(id).get("name");
//			i++;
//		}
//		return options;
//	}

	
	
}
