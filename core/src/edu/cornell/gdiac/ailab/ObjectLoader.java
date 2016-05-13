package edu.cornell.gdiac.ailab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
import edu.cornell.gdiac.ailab.GameSaveState.ActionUpgrade;
import edu.cornell.gdiac.ailab.GameSaveState.CharacterData;
import edu.cornell.gdiac.ailab.Tile.TileState;
import edu.cornell.gdiac.mesh.MeshLoader;
import edu.cornell.gdiac.ailab.DecisionNode.*;

public class ObjectLoader {

	private static ObjectLoader instance = null;
	
	private static File ROOT;

	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Container to track the assets loaded so far */
	private Array<String> assets;

	//Instances of characters being used for the level
	private LinkedList<Character> characterList;
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
		setRoot();
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
	
	public void unloadCurrentLevel() {
		for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
		assets.clear();
		characterList.clear();
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
	public Level createLevel(String levelName, HashMap<String, Object> levelDef, GameSaveState gameSaveState) throws IOException {
		availableCharacters = new HashMap<Integer, Character>();
	    availableActions = new HashMap<Integer, Action>();
	    availableAnimations = new HashMap<Integer, Animation>();
	    tacticalManager = new TacticalManager();
	    characterList = new LinkedList<Character>();

		ArrayList<HashMap<String, Object>> allies =  (ArrayList<HashMap<String, Object>>) levelDef.get("allies");
		ArrayList<HashMap<String, Object>> enemies = (ArrayList<HashMap<String, Object>>) levelDef.get("enemies");
		String nextLevel = (String) levelDef.get("nextLevel");
		Integer boardWidth = (Integer) levelDef.get("boardWidth");
		Integer boardHeight = (Integer) levelDef.get("boardHeight");
		String boardTexture = (String) levelDef.get("boardTexture");
		String rimTexture = (String) levelDef.get("rimTexture");

		HashMap<String, String> tiles = (HashMap<String, String>) levelDef.get("tiles");

		ArrayList<String> ai = (ArrayList<String>) levelDef.get("AI");
		String tutorialFileName = (String) levelDef.get("tutorialFileName");

		Yaml yaml = new Yaml();
		File animationFile = new File(ROOT, "yaml/animations.yml");
		HashMap<Integer, HashMap<String, Object>> animations;
		try (InputStream is = new FileInputStream(animationFile)){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		File actionFile = new File(ROOT, "yaml/actions.yml");
		HashMap<Integer, HashMap<String, Object>> actions;
		try (InputStream is = new FileInputStream(actionFile)){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
	
		File charFile = new File(ROOT, "yaml/characters.yml");
		HashMap<Integer, HashMap<String, Object>> characters;
		try (InputStream is = new FileInputStream(charFile)){
			characters = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}


		loadKeysFromLevels(allies, gameSaveState);
		loadKeysFromLevels(enemies, gameSaveState);
		loadKeysFromCharacters(characters, gameSaveState);
		loadKeysFromActions(actions);

		loadAnimations(animations);
		loadActions(actions);
		
		boolean levelHasAI = false;
		if (ai.size() > 0){
			levelHasAI = true;
		}
		loadChars(characters);
		loadLevelChars(allies, true, gameSaveState, levelHasAI);
		loadLevelChars(enemies, false, gameSaveState, levelHasAI);
		
		loadAI(ai);

		Level loadedLevel = new Level();

		loadedLevel.setName(levelName);
		
		Characters chars = new Characters();
		chars.addAll(characterList);
		loadedLevel.setCharacters(chars);
		loadedLevel.setNextLevel(nextLevel);
		loadedLevel.setTacticalManager(tacticalManager);


		if (tutorialFileName != null){
			FileHandle tutorialFile = Gdx.files.internal(tutorialFileName);
			HashMap<Integer, HashMap<String, Object>> steps;
			TutorialSteps tutorialSteps = new TutorialSteps();
			try (InputStream is = tutorialFile.read()){
				steps = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
				loadTutorialSteps(tutorialSteps,steps);
			}
			loadedLevel.setTutorialSteps(tutorialSteps);
		}

		manager.load(boardTexture,Texture.class);
		assets.add(boardTexture);
		
		manager.load(rimTexture,Texture.class);
		assets.add(rimTexture);
		manager.finishLoading();

		GridBoard board = new GridBoard(boardWidth, boardHeight);
		board.setTileTexture(manager.get(boardTexture, Texture.class));
		board.setTileRimTexture(manager.get(rimTexture,Texture.class));
		if (tiles != null) {
			setUpTileEffects(tiles, board);
		}
		loadedLevel.setBoard(board);

		return loadedLevel;
	}


	private void setUpTileEffects(HashMap<String, String> tiles, GridBoard board) {
		for (String coord : tiles.keySet()) {
			String effect = tiles.get(coord);
			String[] coordSplit = coord.split("-");
			int x = Integer.parseInt(coordSplit[0]);
			int y = Integer.parseInt(coordSplit[1]);
			board.setTileEffect(x, y, TileState.valueOf(effect));
		}

	}

	/**Looks at characters specified in level definition
	 * and adds the character ids to availableCharacters.
	 * @param levelChars
	 */
	private void loadKeysFromLevels(ArrayList<HashMap<String, Object>> levelChars, GameSaveState gameSaveState) {
		for (HashMap<String, Object> character : levelChars) {
			Integer charId = (Integer) character.get("id");
			Integer selectedId = (Integer) character.get("selectedId");
			if (charId != null){
				availableCharacters.put(charId, null);
			} else {
				availableCharacters.put(gameSaveState.selectedCharacters.get(selectedId), null);
			} 
		}
	}

	/** Looks at actions and animations specified in target character
	 * definitions and adds ids as keys to the appropriate hashmap
	 * @param characters
	 */
	@SuppressWarnings("unchecked")
	private void loadKeysFromCharacters(HashMap<Integer, HashMap<String, Object>> characters, GameSaveState gameSaveState) {
		for (Integer charId: availableCharacters.keySet()) {
			Integer animationId = (Integer) characters.get(charId).get("animationId");
			availableAnimations.put(animationId, null);

			ArrayList<Integer> actionList = (ArrayList<Integer>) characters.get(charId).get("availableActions");
			for (Integer actionId : actionList) {
				availableActions.put(actionId, null);
			}
			
			ArrayList<Integer> actionList2 = (ArrayList<Integer>) gameSaveState.getActionIds(charId);
			if (actionList2 != null){
				for (Integer actionId : actionList2) {
					availableActions.put(actionId, null);
				}
			}
		}
	}

	/** Looks at animations specified in target action definitions
	 * and adds ids as keys to the appropriate hashmap
	 * @param actions
	 */
	private void loadKeysFromActions(HashMap<Integer, HashMap<String, Object>> actions) {
		for (Integer actionId: availableActions.keySet()) {
			System.out.println(actionId);
			Integer animationId = (Integer) (actions.get(actionId).get("animationId"));
			availableAnimations.put(animationId, null);
			Integer projectileAnimationId = (Integer) (actions.get(actionId).get("projectileAnimationId"));
			if (projectileAnimationId != null){
				availableAnimations.put(projectileAnimationId, null);
			}
		}
	}


	private void loadLevelChars(ArrayList<HashMap<String, Object>> levelChars, 
									boolean leftSide, GameSaveState gameSaveState,
									boolean levelHasAI) {
		
		for (HashMap<String, Object> levelChar : levelChars) {
			Integer normalId = (Integer) levelChar.get("id");
			Integer selectedId = (Integer) levelChar.get("selectedId");
			Integer xPosition = (Integer) levelChar.get("xPosition");
			Integer yPosition = (Integer) levelChar.get("yPosition");

			Integer charId;
			Action[] actionArray = null;
			if (normalId != null){
				charId = normalId;
			} else {
				charId = gameSaveState.selectedCharacters.get(selectedId);
				ArrayList<Integer> actions = gameSaveState.getActionIds(charId);
				actionArray = new Action[actions.size()];
				int i=0;
				for (Integer actionId : actions){
					actionArray[i] = availableActions.get(actionId);
					i++;
				}
			}
			
			Character modelChar = availableCharacters.get(charId);
			Character charToAdd = null;
			if (actionArray != null){
				charToAdd = new Character(modelChar, actionArray);
			}else{
				charToAdd = new Character(modelChar);
			}
			charToAdd.setStartPos(xPosition, yPosition);
			charToAdd.setLeftSide(leftSide);
			charToAdd.animation = new AnimationNode(modelChar.animation);

			//temporary difficulty ai code!!!
			if (leftSide == false && levelHasAI){
				charToAdd.setAI();
			}
	
			characterList.add(charToAdd);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadChars(HashMap<Integer, HashMap<String, Object>> characters) {
		for (Integer charId: availableCharacters.keySet()) {
			HashMap<String, Object> character = characters.get(charId);;
			Integer numSlots = (Integer) character.get("slots");
			String name = (String) character.get("name");
			Integer health = (Integer) character.get("health");
			Integer maxHealth = (Integer) character.get("maxHealth");
			String hexColor = (String) character.get("hexColor");
			Float speed = (Float) ((Double) character.get("speed")).floatValue();
			Float castSpeed = (Float) ((Double) character.get("castSpeed")).floatValue();
			ArrayList<Integer> actions;
			actions = (ArrayList<Integer>) character.get("availableActions");
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
					castSpeed, actionArray,numSlots);
			characterToAdd.id = charId;
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
			Integer size = (Integer) action.get("size");
			String pattern = (String) action.get("pattern");
			Boolean oneHit = (Boolean) action.get("oneHit");
			Boolean canBlock = (Boolean) action.get("canBlock");
			Boolean needsToggle = (Boolean) action.get("needsToggle");
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
			String effectIconTextureName = (String) effect.get("effectIcon");
			Texture effectIconTexture = null;
			if (effectIconTextureName != null){
				manager.load(effectIconTextureName, Texture.class);
				assets.add(effectIconTextureName);
				manager.finishLoading();
				effectIconTexture = manager.get(effectIconTextureName,Texture.class);
			}
			
			String iconTextureName = (String) action.get("icon");
			manager.load(iconTextureName, Texture.class);
			assets.add(iconTextureName);
			manager.finishLoading();
			Texture iconTexture = manager.get(iconTextureName,Texture.class);
			

			Action actionToAdd;
			if (persisting != null){
				Integer persistingNumRounds = (Integer) persisting.get("numRounds");
				Float moveSpeed = (Float) ((Double) persisting.get("moveSpeed")).floatValue();
					actionToAdd = new PersistingAction(name, cost, damage, range, size,
							Pattern.valueOf(pattern), path, oneHit, canBlock,needsToggle, new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName, effectIconTexture),
							description, persistingNumRounds, moveSpeed,iconTexture);
			}else{
				actionToAdd = new Action(name, cost, damage, range, size, Pattern.valueOf(pattern), oneHit, canBlock,needsToggle,
						new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName, effectIconTexture), description,path,iconTexture);
			}
			
			Integer shieldNumberHits = (Integer) action.get("shieldNumberHits");
			if (shieldNumberHits != null && pattern.equals("SHIELD")){
				actionToAdd.setShieldNumberHits(shieldNumberHits);
				
				if (range == 1){
					String shieldTextureTile1State1NameBottom = (String) action.get("shieldTextureTile1State1Bottom");
					manager.load(shieldTextureTile1State1NameBottom,Texture.class);
					assets.add(shieldTextureTile1State1NameBottom);
					manager.finishLoading();
					Texture shieldTextureTile1State1Bottom = manager.get(shieldTextureTile1State1NameBottom,Texture.class);
					actionToAdd.shieldTextureTile1State1Bottom = shieldTextureTile1State1Bottom;
					
					String shieldTextureTile1State1NameTop = (String) action.get("shieldTextureTile1State1Top");
					manager.load(shieldTextureTile1State1NameTop,Texture.class);
					assets.add(shieldTextureTile1State1NameTop);
					manager.finishLoading();
					Texture shieldTextureTile1State1Top = manager.get(shieldTextureTile1State1NameTop,Texture.class);
					actionToAdd.shieldTextureTile1State1Top = shieldTextureTile1State1Top;
					if (shieldNumberHits > 1){
						String shieldTextureTile1State2NameBottom = (String) action.get("shieldTextureTile1State2Bottom");
						manager.load(shieldTextureTile1State2NameBottom,Texture.class);
						assets.add(shieldTextureTile1State2NameBottom);
						manager.finishLoading();
						Texture shieldTextureTile1State2Bottom = manager.get(shieldTextureTile1State2NameBottom,Texture.class);
						actionToAdd.shieldTextureTile1State2Bottom = shieldTextureTile1State2Bottom;
						
						String shieldTextureTile1State2NameTop = (String) action.get("shieldTextureTile1State2Top");
						manager.load(shieldTextureTile1State2NameTop,Texture.class);
						assets.add(shieldTextureTile1State2NameTop);
						manager.finishLoading();
						Texture shieldTextureTile1State2Top = manager.get(shieldTextureTile1State2NameTop,Texture.class);
						actionToAdd.shieldTextureTile1State2Top = shieldTextureTile1State2Top;
					}
				}
				if (range == 2 || range == 3){
					String shieldTextureTile2State1NameBottom = (String) action.get("shieldTextureTile2State1Bottom");
					manager.load(shieldTextureTile2State1NameBottom,Texture.class);
					assets.add(shieldTextureTile2State1NameBottom);
					manager.finishLoading();
					Texture shieldTextureTile2State1Bottom = manager.get(shieldTextureTile2State1NameBottom,Texture.class);
					actionToAdd.shieldTextureTile2State1Bottom = shieldTextureTile2State1Bottom;
					
					String shieldTextureTile2State1NameTop = (String) action.get("shieldTextureTile2State1Top");
					manager.load(shieldTextureTile2State1NameTop,Texture.class);
					assets.add(shieldTextureTile2State1NameTop);
					manager.finishLoading();
					Texture shieldTextureTile2State1Top = manager.get(shieldTextureTile2State1NameTop,Texture.class);
					actionToAdd.shieldTextureTile2State1Top = shieldTextureTile2State1Top;
					if (shieldNumberHits > 1){
						String shieldTextureTile2State2NameBottom = (String) action.get("shieldTextureTile2State2Bottom");
						manager.load(shieldTextureTile2State2NameBottom,Texture.class);
						assets.add(shieldTextureTile2State2NameBottom);
						manager.finishLoading();
						Texture shieldTextureTile2State2Bottom = manager.get(shieldTextureTile2State2NameBottom,Texture.class);
						actionToAdd.shieldTextureTile2State2Bottom = shieldTextureTile2State2Bottom;
						
						String shieldTextureTile2State2NameTop = (String) action.get("shieldTextureTile2State2Top");
						manager.load(shieldTextureTile2State2NameTop,Texture.class);
						assets.add(shieldTextureTile2State2NameTop);
						manager.finishLoading();
						Texture shieldTextureTile2State2Top = manager.get(shieldTextureTile2State2NameTop,Texture.class);
						actionToAdd.shieldTextureTile2State2Top = shieldTextureTile2State2Top;
					}
				}
				if (range == 3){
					String shieldTextureTile3State1NameBottom = (String) action.get("shieldTextureTile3State1Bottom");
					manager.load(shieldTextureTile3State1NameBottom,Texture.class);
					assets.add(shieldTextureTile3State1NameBottom);
					manager.finishLoading();
					Texture shieldTextureTile3State1Bottom = manager.get(shieldTextureTile3State1NameBottom,Texture.class);
					actionToAdd.shieldTextureTile3State1Bottom = shieldTextureTile3State1Bottom;
					
					String shieldTextureTile3State1NameTop = (String) action.get("shieldTextureTile3State1Top");
					manager.load(shieldTextureTile3State1NameTop,Texture.class);
					assets.add(shieldTextureTile3State1NameTop);
					manager.finishLoading();
					Texture shieldTextureTile3State1Top = manager.get(shieldTextureTile3State1NameTop,Texture.class);
					actionToAdd.shieldTextureTile3State1Top = shieldTextureTile3State1Top;
					if (shieldNumberHits > 1){
						String shieldTextureTile3State2NameBottom = (String) action.get("shieldTextureTile3State2Bottom");
						manager.load(shieldTextureTile3State2NameBottom,Texture.class);
						assets.add(shieldTextureTile3State2NameBottom);
						manager.finishLoading();
						Texture shieldTextureTile3State2Bottom = manager.get(shieldTextureTile3State2NameBottom,Texture.class);
						actionToAdd.shieldTextureTile3State2Bottom = shieldTextureTile3State2Bottom;
						
						String shieldTextureTile3State2NameTop = (String) action.get("shieldTextureTile3State2Top");
						manager.load(shieldTextureTile3State2NameTop,Texture.class);
						assets.add(shieldTextureTile3State2NameTop);
						manager.finishLoading();
						Texture shieldTextureTile3State2Top = manager.get(shieldTextureTile3State2NameTop,Texture.class);
						actionToAdd.shieldTextureTile3State2Top = shieldTextureTile3State2Top;
					}
				}
			}
			
			Integer animationId = (Integer) action.get("animationId");
			if (animationId != null){
				actionToAdd.setAnimation(availableAnimations.get(animationId));
			}
			
			Integer projectileAnimationId = (Integer) action.get("projectileAnimationId");
			if (projectileAnimationId != null){
				actionToAdd.setProjectileAnimation(availableAnimations.get(projectileAnimationId));
			}
			
			if(action.containsKey("isBuff")){
				actionToAdd.isBuff = ((Boolean) action.get("isBuff"));
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
			FileHandle aiFile = Gdx.files.internal(s);
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
					//System.out.println(cond);
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
			node.label = s;
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
	
	public HashMap<ActionUpgrade,Action> getActions(List<ActionUpgrade> aus) throws IOException{
		HashMap<ActionUpgrade,Action> actionMap = new HashMap<ActionUpgrade,Action>();
		availableActions = new HashMap<Integer, Action>();
		availableAnimations = new HashMap<Integer, Animation>();
		for (ActionUpgrade au : aus){
			availableActions.put(au.actionId, null);
		}
		
		Yaml yaml = new Yaml();
		File animationFile = new File(ROOT, "yaml/animations.yml");
		HashMap<Integer, HashMap<String, Object>> animations;
		try (InputStream is = new FileInputStream(animationFile)){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		File actionFile = new File(ROOT, "yaml/actions.yml");
		HashMap<Integer, HashMap<String, Object>> actions;
		try (InputStream is = new FileInputStream(actionFile)){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		loadKeysFromActions(actions);
		loadAnimations(animations);
		loadActions(actions);
		
		for (ActionUpgrade au : aus){
			actionMap.put(au, availableActions.get(au.actionId));
		}
		
		return actionMap;
	}
	
	public ArrayList<Action> getSelectedActionList(List<Integer> actionIds) throws IOException{
		ArrayList<Action> actionList = new ArrayList<Action>();
		availableActions = new HashMap<Integer, Action>();
		availableAnimations = new HashMap<Integer, Animation>();
		for (Integer id : actionIds){
			availableActions.put(id, null);
		}
		
		Yaml yaml = new Yaml();
		File animationFile = new File(ROOT, "yaml/animations.yml");
		HashMap<Integer, HashMap<String, Object>> animations;
		try (InputStream is = new FileInputStream(animationFile)){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		File actionFile = new File(ROOT, "yaml/actions.yml");
		HashMap<Integer, HashMap<String, Object>> actions;
		try (InputStream is = new FileInputStream(actionFile)){
			actions = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		loadKeysFromActions(actions);
		loadAnimations(animations);
		loadActions(actions);
		
		for (Integer id : actionIds){
			actionList.add(availableActions.get(id));
		}
		
		return actionList;
	}
	
	public void getCharacterInfo(List<CharacterData> charDatas) throws IOException{
		Yaml yaml = new Yaml();
		File charFile = new File(ROOT, "yaml/characters.yml");
		HashMap<Integer, HashMap<String, Object>> characters;
		try (InputStream is = new FileInputStream(charFile)){
			characters = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		File animationFile = new File(ROOT, "yaml/animations.yml");
		HashMap<Integer, HashMap<String, Object>> animations;
		try (InputStream is = new FileInputStream(animationFile)){
			animations = (HashMap<Integer, HashMap<String, Object>>) yaml.load(is);
		}
		
		for (CharacterData cd : charDatas){
			HashMap<String, Object> character = characters.get(cd.characterId);
			if (cd.getIcon() == null){
				String iconTextureName = (String) character.get("icon");
				manager.load(iconTextureName, Texture.class);
				assets.add(iconTextureName);
				manager.finishLoading();
				Texture iconTexture = manager.get(iconTextureName,Texture.class);
				cd.setIconTexture(iconTexture);
			}
			if (cd.getAnimation() == null){
				Integer animationId = (Integer) character.get("animationId");
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
				Animation anim = new Animation(name,animationTexture,rows,cols,size);

				ArrayList<HashMap<String, Object>> segments = (ArrayList<HashMap<String, Object>>) animation.get("segments");
				for (HashMap<String, Object> segmentData : segments){
					Integer segmentId = (Integer) segmentData.get("segmentId");
					Integer startingIndex = (Integer) segmentData.get("startingIndex");
					List<Integer> frameLengths = (List<Integer>) segmentData.get("frameData");

					anim.addSegment(segmentId,startingIndex,frameLengths);
				}
				AnimationNode animNode = new AnimationNode(anim);
				cd.setAnimation(animNode);
			}
		}
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

	@SuppressWarnings("unchecked")
	private void loadTutorialSteps(TutorialSteps ts, HashMap<Integer, HashMap<String, Object>> steps) {
		for (HashMap<String, Object> step : steps.values()){
			String text = (String) step.get("text");
			Boolean paused = (Boolean) step.get("paused");
			Boolean spaceToContinue = (Boolean) step.get("spaceToContinue");
			Boolean ignoreTextDone = (Boolean) step.get("ignoreTextDone");
			Boolean dontWriteText = (Boolean) step.get("dontWriteText");
			Integer timeToPause = (Integer) step.get("timeToPause");
			if (dontWriteText == null){
				dontWriteText = false;
			}
			if (timeToPause == null){
				timeToPause = -1;
			}
			if (ignoreTextDone == null){
				ignoreTextDone = false;
			}


			Boolean confirm = (Boolean) step.get("confirm");
			if (confirm == null) confirm = false;

			Boolean finishGame = (Boolean) step.get("finishGame");
			if (finishGame != null){
				ts.setFinishGame(finishGame);
			}
			
			Boolean stepOnSelection = (Boolean) step.get("stepOnSelection");
			if (stepOnSelection != null){
				ts.setStepOnSelection(stepOnSelection);
			}
			
			String nextLevel = (String) step.get("nextLevel");
			if (nextLevel != null){
				ts.setNextLevel(nextLevel);
			}
			
			String levelName = (String) step.get("levelName");
			if (levelName != null){
				ts.setLevelName(levelName);	
			}
			
			String rightText = (String) step.get("rightText");
			if (rightText != null){
				ts.setRightText(rightText);	
			}
			
			String wrongText = (String) step.get("wrongText");
			if (wrongText != null){
				ts.setWrongText(wrongText);	
			}
			
			String levelColor = (String) step.get("levelColor");
			if (levelColor != null){
				if (levelColor.equals("WHITE")){
					ts.setLevelColor(Color.WHITE);
				} else if (levelColor.equals("BLACK")) {
					ts.setLevelColor(Color.BLACK);
				}
			}

			ts.addStep(text, paused, confirm, spaceToContinue, dontWriteText, timeToPause, ignoreTextDone);

			ArrayList<HashMap<String, Object>> actions = (ArrayList<HashMap<String, Object>>) step.get("actions");

			ArrayList<HashMap<String, Object>> highlights = (ArrayList<HashMap<String, Object>>) step.get("highlightRegions");
			
			ArrayList<String> highlightChars = (ArrayList<String>) step.get("highlightCharacters");
			ArrayList<String> highlightTokens = (ArrayList<String>) step.get("highlightTokens");
			
			if(highlightChars != null){
				ts.addHighlightChars(highlightChars);
			}
			if(highlightTokens != null){
				ts.addHighlightTokens(highlightTokens);
			}

			if (actions != null){
				for (HashMap<String, Object> actionData : actions){
					Integer actionId = (Integer) actionData.get("actionId");
					Integer xPos = (Integer) actionData.get("xPos");
					Integer yPos = (Integer) actionData.get("yPos");
					String direction = (String)actionData.get("direction");
					ts.addAction(actionId,xPos,yPos,direction);
				}
			} else {
				ts.anyAction();
			}

			if (highlights != null){
				for (HashMap<String, Object> highlightData : highlights){
					Double xPos = (Double) highlightData.get("xPos");
					Double yPos = (Double) highlightData.get("yPos");
					Double width = (Double) highlightData.get("width");
					Double height = (Double) highlightData.get("height");
					String arrow = (String) highlightData.get("arrow");
					Boolean isChar = (Boolean) highlightData.get("isChar");
					Boolean isSquare = (Boolean) highlightData.get("isSquare");
					if (isChar == null) isChar = false;
					if (isSquare == null) isSquare = false;
					ts.addHighlight(xPos,yPos,width, height, arrow, isChar, isSquare);
				}
			}
		}
	}
}
