package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import edu.cornell.gdiac.ailab.AIController.Difficulty;
import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Effect.Type;
import edu.cornell.gdiac.mesh.MeshLoader;
import edu.cornell.gdiac.ailab.DecisionNode.*;

public class ObjectLoader {
	
	private static ObjectLoader instance = null;
	
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Container to track the assets loaded so far */
	private Array<String> assets;
	
	//hashmap used to load characters for level from yaml
	private HashMap<Integer, Character> availableCharacters;
	//hashmap used to load actions for level from yaml
    private HashMap<Integer, Action> availableActions;
    //hashmap used to load animations for level from yaml
    private HashMap<Integer, Animation> availableAnimations;
    //TacticalManager to be loaded from yaml
    private TacticalManager tacticalManager;
    
    //singleton pattern constructor
    //Instantiates assets array and asset manager
	protected ObjectLoader() {
		assets = new Array<String>();
		manager = new AssetManager();
		manager.setLoader(Mesh.class, new MeshLoader(new InternalFileHandleResolver()));
	}
	
	/**
	 * Singleton pattern getInstance. Creates new instance if instance is null.
	 * @return
	 */
	public static ObjectLoader getInstance() {
		if (instance == null) {
			instance = new ObjectLoader();
		}
		return instance;
	}
	
	
	public void unloadCurrentLevel() {
		for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
		assets.clear();
		availableCharacters=null;
		availableActions = null;
		availableAnimations = null;
		tacticalManager = null;
	}
	
	/**
	 * Main method used to construct a level.
	 * level definition hashmap passed in as argument.
	 * Assumed that yaml definitions have been filled out correctly.
	 * @param levelDef
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public Level createLevel(HashMap<String, Object> levelDef) throws IOException {
		availableCharacters = new HashMap<Integer, Character>();
	    availableActions = new HashMap<Integer, Action>();
	    availableAnimations = new HashMap<Integer, Animation>();
	    tacticalManager = new TacticalManager();
		
		
		ArrayList<HashMap<String, Object>> allies =  (ArrayList<HashMap<String, Object>>) levelDef.get("allies");
		ArrayList<HashMap<String, Object>> enemies = (ArrayList<HashMap<String, Object>>) levelDef.get("enemies");
		String nextLevel = (String) levelDef.get("nextLevel");	
		Integer boardWidth = (Integer) levelDef.get("boardWidth");
		Integer boardHeight = (Integer) levelDef.get("boardHeight");
		String boardTexture = (String) levelDef.get("boardTexture");
		ArrayList<String> ai = (ArrayList<String>) levelDef.get("AI");
		
		Yaml yaml = new Yaml();
		FileHandle animationFile = Gdx.files.internal("yaml/animations.yml");
		HashMap<Integer, HashMap<String, Object>> animations;
		try (InputStream is = animationFile.read()){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		FileHandle actionFile = Gdx.files.internal("yaml/actions.yml");
		HashMap<Integer, HashMap<String, Object>> actions;
		try (InputStream is = actionFile.read()){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		HashMap<Integer, HashMap<String, Object>> characters;
		try (InputStream is = charFile.read()){
			characters = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		
		loadKeysFromLevels(allies);
		loadKeysFromLevels(enemies);
		loadKeysFromCharacters(characters);
		loadKeysFromActions(actions);
		
		loadAnimations(animations);
		loadActions(actions);
		loadCharacters(allies, characters, true);
		loadCharacters(enemies, characters, false);
		loadAI(ai);
		
		Level loadedLevel = new Level();
		
		Characters chars = new Characters();
		chars.addAll(availableCharacters.values());
		loadedLevel.setCharacters(chars);
		loadedLevel.setNextLevel(nextLevel);
		loadedLevel.setBoardHeight(boardHeight);
		loadedLevel.setBoardWidth(boardWidth);
		loadedLevel.setTacticalManager(tacticalManager);
		
		manager.load(boardTexture,Texture.class);
		assets.add(boardTexture);
		manager.finishLoading();
		loadedLevel.setBoardTexture(manager.get(boardTexture,Texture.class));
		return loadedLevel;
	}
	
	/**Looks at characters specified in level definition
	 * and adds the character ids to availableCharacters.
	 * @param levelChars
	 */
	private void loadKeysFromLevels(ArrayList<HashMap<String, Object>> levelChars) {
		for (HashMap<String, Object> character : levelChars) {
			Integer charId = (Integer) character.get("id");
			availableCharacters.put(charId, null);
		}
	}
	
	/** Looks at actions and animations specified in target character
	 * definitions and adds ids as keys to the appropriate hashmap
	 * @param characters
	 */
	@SuppressWarnings("unchecked")
	private void loadKeysFromCharacters(HashMap<Integer, HashMap<String, Object>> characters) {
		for (Integer charId: availableCharacters.keySet()) {
			Integer animationId = (Integer) characters.get(charId).get("animationId");
			availableAnimations.put(animationId, null);
			
			ArrayList<Integer> actionList = (ArrayList<Integer>) characters.get(charId).get("availableActions");
			for (Integer actionId : actionList) {
				availableActions.put(actionId, null);
			}
		}
	}
	
	/** Looks at animations specified in target action definitions
	 * and adds ids as keys to the appropriate hashmap
	 * @param actions
	 */
	private void loadKeysFromActions(HashMap<Integer, HashMap<String, Object>> actions) {
		for (Integer actionId: availableActions.keySet()) {
			Integer animationId = (Integer) (actions.get(actionId).get("animationId"));
			availableAnimations.put(animationId, null);
		}
	}
	
	/**Loads all target characters from their yaml specifications
	 * @param levelChars
	 * @param characters
	 * @param leftSide
	 */
	@SuppressWarnings("unchecked")
	private void loadCharacters(ArrayList<HashMap<String, Object>> levelChars,  
			HashMap<Integer, HashMap<String, Object>> characters, boolean leftSide){
		for (HashMap<String, Object> levelChar : levelChars) {
			Integer charId = (Integer) levelChar.get("id");
			Integer xPosition = (Integer) levelChar.get("xPosition");
			Integer yPosition = (Integer) levelChar.get("yPosition");
			
			HashMap<String, Object> character = characters.get(charId);

			String name = (String) character.get("name");
			Integer health = (Integer) character.get("health");
			Integer maxHealth = (Integer) character.get("maxHealth");
			String hexColor = (String) character.get("hexColor");
			Float speed = (Float) ((Double) character.get("speed")).floatValue();
			Float castSpeed = (Float) ((Double) character.get("castSpeed")).floatValue();
			ArrayList<Integer> actions = (ArrayList<Integer>) character.get("availableActions");
			Action[] actionArray = new Action[actions.size()];
			int i=0;
			for (Integer actionId : actions){
				actionArray[i] = availableActions.get(actionId);
				i++;
			}
			String charTextureName = (String) character.get("texture");
			String iconTextureName = (String) character.get("icon");
			
			manager.load(charTextureName,Texture.class);
			assets.add(charTextureName);
			manager.load(iconTextureName, Texture.class);
			assets.add(iconTextureName);
			manager.finishLoading();
			Texture charTexture = manager.get(charTextureName,Texture.class);
			Texture iconTexture = manager.get(iconTextureName,Texture.class);
			Integer animationId = (Integer) character.get("animationId");
			Animation anim = availableAnimations.get(animationId);
			AnimationNode animNode = new AnimationNode(anim);
			
			Character characterToAdd = new Character(charTexture, iconTexture, animNode,
					name, health, maxHealth, Color.valueOf(hexColor), speed, 
					castSpeed, xPosition, yPosition, leftSide, actionArray); 
			
			//temporary difficulty ai code!!!
			if (leftSide == false && levelChar.containsKey("difficulty")){
				String difficulty = (String) levelChar.get("difficulty");
				characterToAdd.setAI(Difficulty.valueOf(difficulty));
			}
			
			availableCharacters.put(charId, characterToAdd);
		}

	}
	
	/**Loads all target actions from their yaml specifications
	 * @param actions
	 */
	@SuppressWarnings("unchecked")
	private void loadActions(HashMap<Integer, HashMap<String, Object>> actions) {
		for (Integer actionId: availableActions.keySet()) {
			HashMap<String, Object> action = actions.get(actionId);
			
			String name = (String) action.get("name");
			Integer cost = (Integer) action.get("cost");
			Integer damage = (Integer) action.get("damage");
			Integer range = (Integer) action.get("range");
			String pattern = (String) action.get("pattern");
			String description = (String) action.get("description");
			String path = (String) action.get("path");
			HashMap<String,Object> persisting = 
						(HashMap<String, Object>) action.get("persisting_action"); 
			HashMap<String, Object> effect = 
					(HashMap<String, Object>) action.get("effect");
			String eff = (String) effect.get("type");
			String effectName = (String) effect.get("name");
			Integer effectNumRounds = (Integer) effect.get("numRounds");
			Integer magnitude = (Integer) effect.get("magnitude");
			
			Action actionToAdd;
			if (persisting != null){
				Integer persistingNumRounds = (Integer) persisting.get("numRounds");
				Float moveSpeed = (Float) ((Double) persisting.get("moveSpeed")).floatValue();
					actionToAdd = new PersistingAction(name, cost, damage, range, 
							Pattern.valueOf(pattern), path,new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName), 
							description, persistingNumRounds, moveSpeed);
			}else{
				actionToAdd = new Action(name, cost, damage, range, Pattern.valueOf(pattern),
						new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName), description,path);
			}
			
			Integer animationId = (Integer) action.get("animationId");
			if (animationId != null){
				actionToAdd.setAnimation(availableAnimations.get(animationId));
			}
			
			
			availableActions.put(actionId, actionToAdd);
			
		}
		
	}
	
	
	/**
	 * Loads all the AI's from their yaml specifications
	 */
	@SuppressWarnings("unchecked") 
	private void loadAI(ArrayList<String> ai) throws IOException{
		for(String s: ai){
			HashMap<String , HashMap<String, Object>> nodes;
			FileHandle aiFile = Gdx.files.internal("yaml/"+s);
			Yaml yaml = new Yaml();
			try (InputStream is = aiFile.read()){
				nodes = (HashMap<String, HashMap<String, Object>>) yaml.load(is);
				processAIFile(nodes);
			}
		}
	}
	
	
	/**
	 * Loads a specific AI file from the yaml HashMap
	 */
	@SuppressWarnings("unchecked")
	private void processAIFile(HashMap<String, HashMap<String, Object>> nodes){
		for(String s: nodes.keySet()){
			HashMap<String, Object> map = nodes.get(s);
			String type = (String) map.get("type");
			map.remove("type");

			Tactic branchType = Tactic.NONE;
			if(map.containsKey("branch_type")){
				branchType = Tactic.valueOf((String) map.get("branch_type"));
				map.remove("branch_type");
			}
			
			DecisionNode node;
			if(type.equals("index")){
				node = new IndexNode(branchType);
				for(String cond: map.keySet()){
					String[] conds = cond.split("/");
					String other = (String) map.get(cond);
					((IndexNode) node).addRule(Arrays.asList(conds), other);
				}
			}
			
			else if(type.equals("leaf")){
				node = new LeafNode(branchType);
				Tactic myTactic = Tactic.valueOf((String) map.get("my_tactic"));
				((LeafNode) node).myTactic = myTactic;
				if(myTactic == Tactic.SPECIFIC){
					ArrayList<String> s1 = (ArrayList<String>) map.get("my_actions");
					((LeafNode) node).mySpecific = new MoveList(stringsToSpecific(s1));
				}
				
				if(map.containsKey("ally_tactic")){
					Tactic allyTactic = Tactic.valueOf((String) map.get("ally_tactic"));
					((LeafNode) node).allyTactic = allyTactic;
					if(allyTactic == Tactic.SPECIFIC){
						ArrayList<String> s2 = (ArrayList<String>) map.get("ally_actions");
						((LeafNode) node).allySpecific = new MoveList(stringsToSpecific(s2));
					}
				}
			} 
			else if(type.equals("character")){
				node = new IndexNode(branchType);
				ArrayList<String> s1 = (ArrayList<String>) map.get("branches");
				for(String branch: s1){
					((IndexNode) node).addRule(new ArrayList<String>(), branch);
				}
			}
			else {
				System.out.println("MUST SPECIFY INDEX OR LEAF");
				return;
			}
			
			if(s.equals("ROOT")){
				tacticalManager.setRoot(node);
			}
			tacticalManager.addToMap(s, node);
		}
	}
	
	
	/**
	 * Convert a list of strings into a list of specific actions
	 */
	private ArrayList<Specific> stringsToSpecific(ArrayList<String> strings){
		ArrayList<Specific> moves = new ArrayList<Specific>();
		for(String s: strings){
			moves.add(Specific.valueOf(s));
		}
		return moves;
	}
	
	
	/**
	 * Returns true if this string is the name of a character
	 */
	private boolean isCharacterName(String s){
		for(Character c: availableCharacters.values()){
			if(c.name.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	/**Loads all target animations from their yaml specifications
	 * @param animations
	 */
	@SuppressWarnings("unchecked")
	private void loadAnimations(HashMap<Integer, HashMap<String, Object>> animations) {
		for (Integer animationId: availableAnimations.keySet()) {
			HashMap<String, Object> animation = animations.get(animationId);
			String name = (String) animation.get("name");
			String textureName = (String) animation.get("texture");
			Integer rows = (Integer) animation.get("rows");
			Integer cols = (Integer) animation.get("cols");
			Integer size = (Integer) animation.get("size");
			manager.load(textureName, Texture.class);
			assets.add(textureName);
			manager.finishLoading();
			Texture animationTexture = manager.get(textureName,Texture.class);
			Animation animationToAdd = new Animation(name,animationTexture,rows,cols,size);
			
			ArrayList<HashMap<String, Object>> segments = (ArrayList<HashMap<String, Object>>) animation.get("segments");
			for (HashMap<String, Object> segmentData : segments){
				Integer segmentId = (Integer) segmentData.get("segmentId");
				Integer startingIndex = (Integer) segmentData.get("startingIndex");
				List<Integer> frameLengths = (List<Integer>) segmentData.get("frameData");
				
				animationToAdd.addSegment(segmentId,startingIndex,frameLengths);
			}
			availableAnimations.put(animationId, animationToAdd);
		}
		
	}
	
	
}
