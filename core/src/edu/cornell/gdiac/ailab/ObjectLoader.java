package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.Effect.Type;
import edu.cornell.gdiac.mesh.MeshLoader;

public class ObjectLoader {

	private static ObjectLoader instance = null;
	
	private AssetManager manager;
	private HashMap<Integer, Character> availableCharacters;
    private HashMap<Integer, Action> availableActions;
    private HashMap<Integer, Animation> availableAnimations;
	
	protected ObjectLoader() {
		AssetManager manager = new AssetManager();
		manager.setLoader(Mesh.class, new MeshLoader(new InternalFileHandleResolver()));
	}
	
	public static ObjectLoader getInstance() {
		if (instance == null) {
			instance = new ObjectLoader();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public Level createLevel(HashMap<String, Object> levelDef) throws IOException {
		availableCharacters = new HashMap<Integer, Character>();
	    availableActions = new HashMap<Integer, Action>();
	    availableAnimations = new HashMap<Integer, Animation>();
		
		
		ArrayList<HashMap<String, Object>> allies =  (ArrayList<HashMap<String, Object>>) levelDef.get("allies");
		ArrayList<HashMap<String, Object>> enemies = (ArrayList<HashMap<String, Object>>) levelDef.get("enemies");
		String nextLevel = (String) levelDef.get("nextLevel");
		
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
		
		Level loadedLevel = new Level();
		loadedLevel.setAvailableCharacters(availableCharacters);
		loadedLevel.setAvailableActions(availableActions);
		loadedLevel.setAvailableAnimations(availableAnimations);
		loadedLevel.setNextLevel(nextLevel);
		
		return loadedLevel;
	}
	
	private void loadKeysFromLevels(ArrayList<HashMap<String, Object>> levelChars) {
		for (HashMap<String, Object> character : levelChars) {
			Integer charId = (Integer) character.get("id");
			availableCharacters.put(charId, null);
		}
	}
	
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
	
	private void loadKeysFromActions(HashMap<Integer, HashMap<String, Object>> actions) {
		for (Integer actionId: availableActions.keySet()) {
			Integer animationId = (Integer) actions.get(actionId).get("animationId");
			availableAnimations.put(animationId, null);
		}
	}
	
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
			Texture charTexture = manager.get(charTextureName,Texture.class);
			Texture iconTexture = manager.get(iconTextureName,Texture.class);
			Integer animationId = (Integer) character.get("animationId");
			Animation anim = availableAnimations.get(animationId);
			AnimationNode animNode = new AnimationNode(anim);
			
			Character characterToAdd = new Character(charTexture, iconTexture, animNode,
					name, health, maxHealth, Color.valueOf(hexColor), speed, 
					castSpeed, xPosition, yPosition, leftSide, actionArray); 

			availableCharacters.put(charId, characterToAdd);
		
		}

	}
	
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
						Pattern.valueOf(pattern), new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName), 
						description, persistingNumRounds, moveSpeed);
			}else{
				actionToAdd = new Action(name, cost, damage, range, Pattern.valueOf(pattern),
						new Effect(effectNumRounds, Type.valueOf(eff), magnitude, effectName), description);
			}
			
			Integer animationId = (Integer) action.get("animationId");
			if (animationId != null){
				actionToAdd.setAnimation(availableAnimations.get(animationId));
			}
			
			
			availableActions.put(actionId, actionToAdd);
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void loadAnimations(HashMap<Integer, HashMap<String, Object>> animations) {
		for (Integer animationId: availableAnimations.keySet()) {
			HashMap<String, Object> animation = animations.get(animationId);
			String name = (String) animation.get("name");
			String textureName = (String) animation.get("texture");
			Integer rows = (Integer) animation.get("rows");
			Integer cols = (Integer) animation.get("cols");
			Integer size = (Integer) animation.get("size");
			
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
