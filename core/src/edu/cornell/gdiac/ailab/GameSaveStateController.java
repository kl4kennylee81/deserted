package edu.cornell.gdiac.ailab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import edu.cornell.gdiac.ailab.GameSaveState.LevelData;

public class GameSaveStateController {
	private static  GameSaveStateController instance = null;
	private static File ROOT;
	private Yaml yaml;
	private GameSaveState gameSaveState;
	private HashMap<String, HashMap<String, Object>> gameSaveStateData;
	
	private final String CURRENT_SAVE_FILE = "yaml/gameSaveState.yml";
	private final String BASIC_SAVE_FILE = "yaml/basicSaveState.yml";
	
	public GameSaveStateController() throws IOException{
		setRoot();
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);
		
		gameSaveState = new GameSaveState();
		loadGameSaveState();
	}
	
	public static GameSaveStateController getInstance(){
		if (instance == null) {
			try {
				instance = new GameSaveStateController();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public List<LevelData> getLevelData(){
		return gameSaveState.levels;
	}
	
	public GameSaveState getGameSaveState(){
		return gameSaveState;
	}
	
	public boolean containsLevel(String levelName){
		for (LevelData ld : gameSaveState.levels){
			if (ld.levelName.equals(levelName)){
				return true;
			}
		}
		return false;
	}
	
	public void saveGameSaveState(){
		gameSaveState.save(gameSaveStateData);
		
		File saveFile = new File(ROOT, CURRENT_SAVE_FILE);
		FileWriter writer = null;
		try {
			writer = new FileWriter(saveFile, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		yaml.dump(gameSaveStateData, writer);
	}
	
	@SuppressWarnings("resource")
	private void resetGameSaveState() throws IOException {
		FileChannel src = new FileInputStream(new File(ROOT, BASIC_SAVE_FILE)).getChannel();
		FileChannel dest = new FileOutputStream(new File(ROOT, CURRENT_SAVE_FILE)).getChannel();
		dest.transferFrom(src, 0, src.size());
		loadGameSaveState();
	}
	
	@SuppressWarnings("unchecked")
	private void loadGameSaveState() throws IOException{
		File stateFile = new File(ROOT, CURRENT_SAVE_FILE);
		try (InputStream is = new FileInputStream(stateFile)){
			gameSaveStateData = (HashMap<String, HashMap<String, Object>>) yaml.load(is);
		}
		setGameSaveState();
	}
	
	private void setGameSaveState(){
		gameSaveState.setState(gameSaveStateData);
	}
	
	public void beatLevel(String levelName){
		gameSaveState.beatLevel(levelName);
		saveGameSaveState();
	}
	
	public int getLevelSP(String levelName){
		LevelData ld = gameSaveState.getLevelData(levelName);
		if (!ld.beaten){
			return gameSaveState.getLevelData(levelName).gainedSP;
		} else {
			return 0;
		}
	}
	
	public List<CharacterData> getLevelUnlockedChars(String levelName){
		LevelData ld = gameSaveState.getLevelData(levelName);
		if (!ld.beaten){
			return gameSaveState.getLevelUnlockedChars(levelName);
		} else {
			return new ArrayList<CharacterData>();
		}
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
}
