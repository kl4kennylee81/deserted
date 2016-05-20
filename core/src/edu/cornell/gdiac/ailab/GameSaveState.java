package edu.cornell.gdiac.ailab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;

/**
 * In yaml file gameSaveState.yaml
 * Also have basic state file basicSaveState.yaml
 * @author JonathanChen
 *
 */
public class GameSaveState {
	public class ActionUpgrade{
		int actionId;
		int cost;
		List<ActionUpgrade> upgrades;
		
		public ActionUpgrade(){
			this.upgrades = new ArrayList<ActionUpgrade>();
		}
		
		public void addActionUpgrade(ActionUpgrade au){
			this.upgrades.add(au);
		}
		
		public int getSize(){
			int size = 1;
			for (ActionUpgrade au : upgrades){
				size += au.getSize();
			}
			return size;
		}
		
		/** Includes itself and its upgrades */
		public List<ActionUpgrade> getActionUpgrades(){
			ArrayList<ActionUpgrade> ids = new ArrayList<ActionUpgrade>();
			ids.add(this);
			for (ActionUpgrade au : upgrades){
				ids.addAll(au.getActionUpgrades());
			}
			return ids;
		}
		
		public boolean hasAction(int actionId){
			if (this.actionId == actionId){
				return true;
			}
			
			for (ActionUpgrade au : upgrades){
				if (au.hasAction(actionId)){
					return true;
				}
			}
			
			return false;
		}
	}
	
	public class CharacterData {
		int characterId;
		// Skill points
		int totalSP;
		List<ActionUpgrade> actionUpgrades;
		ArrayList<Integer> currentActions;
		private Texture icon;
		Texture bigIcon;
		AnimationNode animation;
		String name;
		
		Texture fullCharTexture;
		
		public CharacterData(){
			this.actionUpgrades = new ArrayList<ActionUpgrade>();
			this.currentActions = new ArrayList<Integer>();
			getUsedSP();
		}
		
		public void setIconTexture(Texture icon){
			this.icon = icon;
		}
		
		public Texture getIcon(){
			return this.icon;
		}
		
		public Texture getTexture(){
			return this.fullCharTexture;
		}
		
		public void setTexture(Texture t){
			this.fullCharTexture = t;
		}
		
		public void setAnimation(AnimationNode animation){
			this.animation = animation;
		}
		
		public AnimationNode getAnimation(){
			return this.animation;
		}
		
		public void setAction(int actionId){
			int oldAction = 0;
			int index = 0;
			for (int i = 0; i < actionUpgrades.size(); i++){
				if (actionUpgrades.get(i).hasAction(actionId)){
					index = i;
					oldAction = currentActions.get(i);
					currentActions.set(i, actionId);
				}
			}
			if (getRemainingSP() < 0){
				currentActions.set(index,oldAction);
			}
		}
		
		public void resetSP(){
			this.currentActions.clear();
			for (ActionUpgrade au : actionUpgrades){
				currentActions.add(au.actionId);
			}
		}
		
		public boolean currentlyUsingAction(int actionId){
			return currentActions.contains(actionId);
		}
		
		public List<ActionUpgrade> getAllActionUpgrades(){
			ArrayList<ActionUpgrade> aus = new ArrayList<ActionUpgrade>();
			for (ActionUpgrade au : actionUpgrades){
				aus.addAll(au.getActionUpgrades());
			}
			return aus;
		}
		
		public int getTotalNumActionUpgrades(){
			int sum = 0;
			for (ActionUpgrade au : actionUpgrades){
				sum += au.getSize();
			}
			return sum;
		}
		
		public Integer getCostHelper(ActionUpgrade au, int i){
			if (au.actionId == i){
				return au.cost;
			}
			if (au.upgrades.isEmpty()){
				return null;
			}
			for (ActionUpgrade a : au.upgrades){
				Integer cost = getCostHelper(a,i);
				if (cost != null){
					return au.cost + cost;
				}
			}
			return null;
		}
		
		public Integer getCost(int i){
			for (ActionUpgrade au : actionUpgrades){
				Integer cost = getCostHelper(au,i);
				if (cost != null){
					return cost;
				}
			}
			return null;
		}
		
		public int getUsedSP(){
			int costs = 0;
			for (Integer i : currentActions){
				Integer curCost = getCost(i);
				if (curCost != null){
					costs += getCost(i);
				} else {
					System.out.println("cost is null, check gamesavestate");
				}
			}
			
			return costs;
		}
		
		public int getRemainingSP(){
			return totalSP - getUsedSP();
		}
		
		public void setCurrentActions(ArrayList<Integer> currentActionsData){
			currentActions.clear();
			for (Integer actionId : currentActionsData){
				currentActions.add(actionId);
			}
		}
		
		public void addActionUpgrade(HashMap<String,Object> actionUpgradeData){
			this.actionUpgrades.add(getActionUpgrade(actionUpgradeData));
		}
		
		public ActionUpgrade getActionUpgrade(HashMap<String,Object> actionUpgradeData){
			ActionUpgrade au = new ActionUpgrade();
			au.actionId = (int) actionUpgradeData.get("actionId");
			Integer cost = (Integer) actionUpgradeData.get("cost");
			au.cost = cost == null? 0 : cost;
			ArrayList<HashMap<String,Object>> nextActions = (ArrayList<HashMap<String,Object>>) actionUpgradeData.get("nextAction");
			if (nextActions != null){
				for (HashMap<String,Object> nextActionData : nextActions){
					au.addActionUpgrade(getActionUpgrade(nextActionData));
				}
			}
			return au;
		}
	}
	
	public class LevelData {
		String levelName;
		String nextLevelName;
		boolean beaten;
		boolean boss;
		int gainedSP;
		Integer unlockableCharacter;
		boolean needsSelect;
		Integer winIn;
		Integer surviveFor;
		String backgroundTexture;
		String preNarrative;
		Boolean seenPre;
		String postNarrative;
		Boolean seenPost;
		String displayName;
		
		public boolean needsSelect(){
			boolean ns = needsSelect;
			//needsSelect = false;
			return ns;
		}
		
		public String getNextLevelName(){
			return nextLevelName;
		}
	}
	
	List<LevelData> levels;
	List<CharacterData> characters;
	List<Integer> availableCharacters;
	List<Integer> unlockableCharacters;
	ArrayList<Integer> selectedCharacters;
	
	public GameSaveState(){
		this.levels = new ArrayList<LevelData>();
		this.characters = new ArrayList<CharacterData>();
		this.availableCharacters = new ArrayList<Integer>();
		this.unlockableCharacters = new ArrayList<Integer>();
		this.selectedCharacters = new ArrayList<Integer>();
	}
	
	public void save(HashMap<String, HashMap<String, Object>> gameSaveStateData){
		HashMap<String, Object> characterData = gameSaveStateData.get("characters");
		for (Object ch : characterData.values()){
			HashMap<String,Object> charData = (HashMap<String, Object>) ch;
			int charId = (int) charData.get("characterId");
			CharacterData cd = getCharacterData(charId);
			charData.put("totalSP", cd.totalSP);
			charData.put("currentActions", cd.currentActions);
		}
		
		HashMap<String, Object> levelData = gameSaveStateData.get("levels");
		for (Object l : levelData.values()){
			HashMap<String,Object> levData = (HashMap<String, Object>) l;
			String levelName = (String) levData.get("levelName");
			LevelData ld = getLevelData(levelName);
			levData.put("beaten", ld.beaten);
			if (ld.preNarrative != null && ld.seenPre != null){
				levData.put("seenPre", ld.seenPre);
			}
			if (ld.postNarrative != null && ld.seenPost != null){
				levData.put("seenPost", ld.seenPost);
			}
		}
		
		Object selChars = gameSaveStateData.get("selectedCharacters");
		selChars = selectedCharacters;
	}
	
	public void setState(HashMap<String, HashMap<String, Object>> gameSaveStateData){
		this.characters.clear();
		this.levels.clear();
		this.availableCharacters.clear();
		this.selectedCharacters.clear();
		
		HashMap<String, Object> characterData = gameSaveStateData.get("characters");
		for (Object ch : characterData.values()){
			HashMap<String,Object> charData = (HashMap<String, Object>) ch;
			CharacterData cd = new CharacterData();
			cd.characterId = (int) charData.get("characterId");
			cd.totalSP = (int) charData.get("totalSP");
			ArrayList<HashMap<String,Object>> actionTree = (ArrayList<HashMap<String,Object>>) charData.get("actionTree");
			for (HashMap<String,Object> actionUpgradeData : actionTree){
				cd.addActionUpgrade(actionUpgradeData);
			}
			cd.setCurrentActions((ArrayList<Integer>) charData.get("currentActions"));
			this.characters.add(cd);
		}
		
		HashMap<String, Object> levelData = gameSaveStateData.get("levels");
		LevelData prevLevelData = null;
		for (Object l : levelData.values()){
			HashMap<String,Object> levData = (HashMap<String, Object>) l;
			LevelData ld = new LevelData();
			ld.levelName = (String) levData.get("levelName");
			ld.beaten = (boolean) levData.get("beaten");
			ld.boss = (boolean) levData.get("boss");
			ld.gainedSP = levData.get("gainedSP") == null ? 0 : (int) levData.get("gainedSP");
			ld.unlockableCharacter = (Integer) levData.get("unlockableCharacter");
			ld.needsSelect = levData.get("needsSelect") == null ? false : (boolean) levData.get("needsSelect");
			ld.winIn = (Integer) levData.get("winIn");
			ld.surviveFor = (Integer) levData.get("surviveFor");
			ld.backgroundTexture = (String) levData.get("background");
			
			ld.preNarrative = (String) levData.get("preNarrative");
			ld.seenPre = (Boolean) levData.get("seenPre");
			ld.postNarrative = (String) levData.get("postNarrative");
			ld.seenPost = (Boolean) levData.get("seenPost");
			ld.displayName = (String) levData.get("displayName");
			
			this.levels.add(ld);
			
			if (prevLevelData != null){
				prevLevelData.nextLevelName = ld.levelName;
			}
			prevLevelData = ld;
		}
		
		Object availChars = gameSaveStateData.get("availableCharacters");
		availableCharacters = (ArrayList<Integer>) availChars;

		Object unlockChars = gameSaveStateData.get("unlockableCharacters");
		unlockableCharacters = (ArrayList<Integer>) unlockChars;
		
		Object selChars = gameSaveStateData.get("selectedCharacters");
		selectedCharacters = (ArrayList<Integer>) selChars;
	}
	
	public void beatLevel(String levelName){
		for (LevelData ld : levels){
			if (ld.levelName.equals(levelName)){
				if (!ld.beaten){
					ld.beaten = true;
					for (CharacterData cd : characters){
						cd.totalSP += ld.gainedSP;
					}
					if (ld.unlockableCharacter != null){
						unlockableCharacters.remove(ld.unlockableCharacter);
						availableCharacters.add(ld.unlockableCharacter);
					}
				}	
				break;
			}
		}
	}
	
	public ArrayList<CharacterData> getAvailableCharactersData(){
		ArrayList<CharacterData> availChars = new ArrayList<CharacterData>();
		for (int i : availableCharacters){
			for (CharacterData cd : characters){
				if (cd.characterId == i){
					availChars.add(cd);
					break;
				}
			}
		}
		return availChars;
	}
	
	public ArrayList<CharacterData> getUnlockableCharactersData(){
		ArrayList<CharacterData> unlockChars = new ArrayList<CharacterData>();
		for (int i : unlockableCharacters){
			for (CharacterData cd : characters){
				if (cd.characterId == i){
					unlockChars.add(cd);
					break;
				}
			}
		}
		return unlockChars;
	}
	
	public ArrayList<CharacterData> getSelectedCharacters(){
		ArrayList<CharacterData> selChars = new ArrayList<CharacterData>();
		for (int i : selectedCharacters){
			for (CharacterData cd : characters){
				if (cd.characterId == i){
					selChars.add(cd);
					break;
				}
			}
		}
		/*if (selChars.size() == 1){
			selChars.add(null);
		}*/
		return selChars;
	}
	
	public void replaceSelectedCharacter(int selIndex, int charId){
		if (selectedCharacters.contains(charId)){
			//switch if not in right place
			if (selectedCharacters.get(selIndex) != charId){
				int oldIndex = selectedCharacters.indexOf(charId);
				int otherCharId = selectedCharacters.get(selIndex);
				selectedCharacters.set(oldIndex,otherCharId);
				selectedCharacters.set(selIndex,charId);
			}
		} else {
			selectedCharacters.set(selIndex, charId);
		}
	}
	
	public LevelData getLevelData (String levelName){
		for (LevelData ld : levels){
			if (ld.levelName.equals(levelName)){
				return ld;
			}
		}
		return null;
	}
	
	public CharacterData getCharacterData (int characterId){
		for (CharacterData cd : characters){
			if (cd.characterId == characterId){
				return cd;
			}
		}
		return null;
	}
	
	public ArrayList<Integer> getActionIds(int characterId){
		CharacterData cd = getCharacterData(characterId);
		return cd == null ? null : cd.currentActions;
	}
	
	public List<CharacterData> getLevelUnlockedChars(String levelName){
		List<CharacterData> unlockChars = new ArrayList<CharacterData>();
		LevelData ld = getLevelData(levelName);
		if (ld.unlockableCharacter != null){
			unlockChars.add(getCharacterData(ld.unlockableCharacter));
		}
		try {
			ObjectLoader.getInstance().getCharacterInfo(unlockChars);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return unlockChars;
	}
	
}
