/*
 * GameEngine.java
 * 
 * This class works like GameMode from last lab.  It is slightly different from the
 * class in Lab 1 in that it must extend the built in class Screen (which works like
 * a combination of a GameCanvas and a root controller).  We had to use this class 
 * because for some unknown reason, alpha blending was not working properly when
 * we combined sprites with 3D models in ApplicationAdapter.
 *
 * We do not have a separate game mode loading this time.  We handle loading directly
 * in this class, making it part of the camera pan.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

//import static com.badlogic.gdx.Gdx.gl20;
//import static com.badlogic.gdx.graphics.GL20.GL_BLEND;

//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.yaml.snakeyaml.Yaml;

import com.badlogic.gdx.*;
//import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.audio.*;
//import com.badlogic.gdx.controllers.Controller;
//import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
//import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;

import edu.cornell.gdiac.ailab.Effect.Type;
import edu.cornell.gdiac.ailab.AIController.Difficulty;
import edu.cornell.gdiac.ailab.Action.Pattern;
//import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.mesh.*;
import edu.cornell.gdiac.ailab.GameCanvas;
//import edu.cornell.gdiac.util.*;

/**
 * Primary class for controlling the game.
 * 
 * This class is slightly different from the class GameMode in Lab 1; it extends the 
 * built in class Screen (which works like a combination of a GameCanvas and a root 
 * controller).  We had to use this class because for some unknown reason, alpha 
 * blending was not working properly when we combined sprites with 3D models in 
 * ApplicationAdapter.
 *
 */
public class GameEngine implements Screen {
	/** 
	 * Enumeration defining the game state
	 */
	public static enum GameState {
		/** While we are still loading assets */
		LOAD,
		/** After loading, but before we start the game */
		MENU,
		/** While we are playing the game */
		PLAY,
		/** When the game has ended, but we are still waiting on animation */
		FINISH,
		/** When the game is over */
		AFTER
	}

	/** Background image for the canvas */
	private static final String BCKGD_TEXTURE = "images/bg.png";
	/** Background image for the menu */
	private static final String MENU_BCKGD_TEXTURE = "images/menubg.png";
	
	/** Background image for the canvas */
	private static final String LOADING_TEXTURE = "images/loading1.png";
	
	/** image for the loading bar  */
	private static final String PRGRSBR_TEXTURE = "images/progressbar.png";
	/** Texture for loading status bar  */
	private static Texture statusBar;
	// statusBar is a "texture atlas." Break it up into parts.
	/** Left cap to the status background (grey region) */
	private TextureRegion statusBkgLeft;
	/** Middle portion of the status background (grey region) */
	private TextureRegion statusBkgMiddle;
	/** Right cap to the status background (grey region) */
	private TextureRegion statusBkgRight;
	/** Left cap to the status forground (colored region) */
	private TextureRegion statusFrgLeft;
	/** Middle portion of the status forground (colored region) */
	private TextureRegion statusFrgMiddle;
	/** Right cap to the status forground (colored region) */
	private TextureRegion statusFrgRight;	
	
	/** File storing the texture for a board tile */
	public static final String TILE_TEXTURE = "models/Tile.png";

	/** File storing the enemy texture for a ship */
	private static final String CHAR_1_TEXTURE = "models/CHARA_1.png";
	/** File storing the player texture for a ship */
	private static final String CHAR_2_TEXTURE = "models/CHARA_2.png";
	/** File storing the enemy texture for a ship */
	private static final String CHAR_3_TEXTURE = "models/CHARA_3.png";
	/** File storing the player texture for a ship */
	private static final String CHAR_4_TEXTURE = "models/CHARA_4.png";
	/** File storing the enemy texture for a ship */
	private static final String CHAR_5_TEXTURE = "models/CHARA_5.png";
	
	/** File storing the enemy texture for a ship */
	private static final String ICON_1_TEXTURE = "models/CASTICON_1.png";
	/** File storing the player texture for a ship */
	private static final String ICON_2_TEXTURE = "models/CASTICON_2.png";
	/** File storing the enemy texture for a ship */
	private static final String ICON_3_TEXTURE = "models/CASTICON_3.png";
	/** File storing the player texture for a ship */
	private static final String ICON_4_TEXTURE = "models/CASTICON_4.png";
	/** File storing the enemy texture for a ship */
	private static final String ICON_5_TEXTURE = "models/CASTICON_5.png";
	
	/** Temprorary Character Animation */
	private static final String CAST_ANIMATION_SHEET = "models/castanimation_spritesheet.png";
	
	/** File storing the single lightning film strip */
	private static final String LIGHTNING_TEXTURE = "actions/lightningstrip.png";
	
	private static final String WHITE_BOX = "images/white.png";
	/** The message font to use */
	private static final String MENU_FONT_FILE  = "fonts/Milonga-Regular.ttf";
	/** The size of the messages */
	private static final int MENU_FONT_SIZE = 20;
	
	private static final String SELECT_FONT_FILE  = "fonts/LondrinaSolid-Regular.ttf";
	/** The size of the messages */
	private static final int SELECT_FONT_SIZE = 18;
	
	/** The width of the progress bar */	
	private int width;
	/** The y-coordinate of the center of the progress bar */
	private int centerY;
	/** The x-coordinate of the center of the progress bar */
	private int centerX;
	/** Scaling factor for when the student changes the resolution. */
	private float scale;

	// We keep sound information in the sound controller, as it belongs there
	
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Container to track the assets loaded so far */
	private Array<String> assets;
    
	/**Subcontroller for gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;
    /** Used to draw the game onto the screen (VIEW CLASS) */
    private GameCanvas canvas;
    /** Subcontroller for main menu (CONTROLLER CLASS) */
    private MainMenuController mainMenuController;
    
//	/** Default budget for asset loader (do nothing but load 60 fps) */
//	private static int DEFAULT_BUDGET = 15;
	/** Standard window size (for scaling) */
	private static int STANDARD_WIDTH  = 800;
	/** Standard window height (for scaling) */
	private static int STANDARD_HEIGHT = 700;
	/** Ratio of the bar width to the screen */
	private static float BAR_WIDTH_RATIO  = 0.66f;
	/** Ration of the bar height to the screen */
	private static float BAR_HEIGHT_RATIO = 0.25f;	
	/** Height of the progress bar */
	private static int PROGRESS_HEIGHT = 30;
	/** Width of the rounded cap on left or right */
	private static int PROGRESS_CAP    = 15;
	/** Width of the middle portion in texture atlas */
	private static int PROGRESS_MIDDLE = 200;
//	private static float BUTTON_SCALE  = 0.75f;
    
    //Current Models
    private HashMap<Integer, Character> availableCharacters;
    private HashMap<Integer, Action> availableActions;
    private HashMap<Integer, Animation> availableAnimations;
    private HashMap<String, HashMap<String, Object>> levelDefs;
    
    /** The current game state (SIMPLE FIELD) */
    private GameState gameState;
    /** How far along (0 to 1) we are in loading process */
	private float  gameLoad;
	
	/** 
	 * Constructs a new game engine
	 *
	 * We can only assign simple fields at this point, as there is no OpenGL context
	 */
    public GameEngine() {
    	gameState = GameState.LOAD;
    	gameLoad  = 0.0f;
		canvas = new GameCanvas();
		gameplayController = new GameplayController();

		availableActions = new HashMap<Integer, Action>();
		availableCharacters = new HashMap<Integer, Character>();
		availableAnimations = new HashMap<Integer, Animation>();
		
		updateMeasures();

	}
    
    public void updateMeasures(){
		width = (int)(BAR_WIDTH_RATIO*canvas.getWidth());
		centerY = (int)(BAR_HEIGHT_RATIO*canvas.getHeight());
		centerX = canvas.getWidth()/2;
		float sx = ((float)canvas.getWidth())/STANDARD_WIDTH;
		float sy = ((float)canvas.getHeight())/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
    }
    
    /**
     * Probably want a separate Level class later
     * 
     * Types:
     * 0 - Easy
     * 1 - Medium
     * 2 - Hard
     * 3 - PVP
     * 
     */
    public void startGame(int type) {
    	initializeCanvas(BCKGD_TEXTURE, SELECT_FONT_FILE);
    	List<Character> chars = new LinkedList<Character>();
    	
    	availableCharacters.get(0).reset();
		availableCharacters.get(1).reset();
    	
		Character enemy1 = availableCharacters.get(2);
		Character enemy2 = availableCharacters.get(3);
		enemy1.reset();
		enemy2.reset();
		
    	switch (type) {
    	case 0:
    		enemy1.setAI(Difficulty.EASY);
    		enemy2.setAI(Difficulty.EASY);
    		break;
    	case 1:
    		enemy1.setAI(Difficulty.MEDIUM);
    		enemy2.setAI(Difficulty.MEDIUM);
    		break;
    	case 2:
    		enemy1.setAI(Difficulty.HARD);
    		enemy2.setAI(Difficulty.HARD);
    		break;
    	default:
    		enemy1.setHuman();
    		enemy2.setHuman();
    		break;
    	}
    	chars.add(availableCharacters.get(0));
    	chars.add(availableCharacters.get(1));
    	chars.add(availableCharacters.get(2));
    	chars.add(availableCharacters.get(3));
    	gameplayController.resetGame(chars,6,4,manager.get(TILE_TEXTURE,Texture.class));
    	gameState = GameState.PLAY;
    	//font support
    	//here load up the font for the selection menu
    	
    }

    public void startGame(Level level) {
    	initializeCanvas(BCKGD_TEXTURE, SELECT_FONT_FILE);
    	
    }

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose() {
		unload();
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 * 
	 * This is the equivalent of create() in Lab 1
	 */
	public void show() {
		load();
	}
	
	/**
	 * Called when this screen is no longer the current screen for a Game.
	 *
	 * When this happens, we should also dispose of all resources.
	 */
	public void hide() {
		unload();
	}

	/**
	 * Called when the screen should render itself.
	 *
	 * This is the primary game loop.  We break it up into a bunch of helpers to
	 * make it readable.
	 *
	 * @param delta The time in seconds since the last render.
	 */
	public void render(float delta) {
		// Allow the user to reset by pressing "R"
		checkReset();
        canvas.begin();
		
		// What we do depends on the game state
		switch (gameState) {
		case LOAD:
			updateLoad();
			drawLoad();
			break;
		case MENU:
			updateMenu();
			break;
		case PLAY:
			updatePlay();
			drawPlay();
			break;
		case FINISH:
			//Graphics after play is over?
			break;
		case AFTER:
			//updateAfter();
			drawAfter();
			break;
		}
		
		canvas.end();
	}
		
	/** 
	 * Returns true if the user reset the game.
	 *
	 * It also exits the game if the player chose to exit.
	 *
	 * @return true if the user reset the game.
	 */
	private boolean checkReset() {
		if (InputController.pressedESC()) {
        	Gdx.app.exit();
        }
		
		// If the player presses 'R', reset the game.
        if (gameState != GameState.LOAD && InputController.pressedR()) {
        	mainMenuController.resetMenu();
            gameState = GameState.MENU;
            return true;
        }
        return false;
    }

	/** 
	 * Updates the state of the loading screen.
	 *
	 * Loading is done when the asset manager is finished and gameLoad == 1.
	 */
	public void updateLoad() {
		if (manager.update()) {
        	// we are done loading, let's move to another screen!
        	if (gameLoad < 1.0f) {
        		gameLoad += 0.01f;
        	} else {
        		gameState = GameState.MENU;
        	}
      	}
	}
	
	/**
	 * Updates the state of the menu screen.
	 */
	private void updateMenu() {
		mainMenuController.update();
		if (mainMenuController.isDone()){
			startGame(mainMenuController.gameNo);
		}
	}
	
	/**
     * The primary update loop of the game; called while it is running.
     */
    public void updatePlay() {
    	gameplayController.update();
    	if (gameplayController.isDone()){
    		gameState = GameState.AFTER;
    	}
    }
	
	/**
	 * Draws the game board while we are still loading
	 */
	public void drawLoad() {
		//canvas.drawMessage(MESSG_LOAD);
		canvas.draw(statusBkgLeft, Color.GOLD, centerX-width/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgRight,  Color.GOLD, centerX+width/2-scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		canvas.draw(statusBkgMiddle, Color.GOLD, centerX-width/2+scale*PROGRESS_CAP, centerY, width-2*scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);

		canvas.draw(statusFrgLeft,   Color.RED, centerX-width/2, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		if (gameLoad > 0) {
			float span = gameLoad*(width-2*scale*PROGRESS_CAP)/1.0f;
			canvas.draw(statusFrgRight,  Color.RED, centerX-width/2+scale*PROGRESS_CAP+span, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
			canvas.draw(statusFrgMiddle, Color.RED, centerX-width/2+scale*PROGRESS_CAP, centerY, span, scale*PROGRESS_HEIGHT);
		} else {
			canvas.draw(statusFrgRight, Color.RED, centerX-width/2+scale*PROGRESS_CAP, centerY, scale*PROGRESS_CAP, scale*PROGRESS_HEIGHT);
		}
    }
	
	public void drawPlay() {
		gameplayController.drawPlay(canvas); 
	}
	
	public void drawAfter() {
		gameplayController.drawAfter(canvas);
	}
    
	
	/**
	 * Called when the Application is resized. 
	 * 
	 * This can happen at any point during a non-paused state
	 */
	public void resize(int width, int height) {
		canvas.resize();
		updateMeasures();
	}
	
	/** 
	 * Called when the Application is paused.
	 * 
	 * This is usually when it's not active or visible on screen.
	 */
	public void pause() { }
	
	/**
	 * Called when the Application is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {}

	@SuppressWarnings("unchecked")
	private Level getLevel(String levelId) throws IOException{
		Yaml yaml = new Yaml();
		FileHandle levelFile = Gdx.files.internal("yaml/levels.yml");
		HashMap<String, Object> targetLevelDef;
		try (InputStream iS = levelFile.read()){
			levelDefs = (HashMap<String, HashMap<String, Object>>) yaml.load(iS);
			targetLevelDef= levelDefs.get(levelId);	
			
		}
		return ObjectLoader.getInstance().createLevel(targetLevelDef);
	}
	
	@SuppressWarnings("unchecked")
	private void loadFromYaml() throws IOException{
		Yaml yaml = new Yaml();
		//Import Animations
		FileHandle animationFile = Gdx.files.internal("yaml/animations.yml");
		try (InputStream is = animationFile.read()){
			ArrayList<HashMap<String, Object>> animations = (ArrayList<HashMap<String, Object>>) yaml.load(is);
			for (HashMap<String, Object> animation : animations){
				Integer id = (Integer) animation.get("id");
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
				availableAnimations.put(id, animationToAdd);
			}	
		} 
		//Import Actions
		FileHandle actionFile = Gdx.files.internal("yaml/actions.yml");
		try (InputStream is = actionFile.read()){
			ArrayList<HashMap<String, Object>> actions = (ArrayList<HashMap<String, Object>>) yaml.load(is);
			for (HashMap<String, Object> action : actions){
				Integer id = (Integer) action.get("id");
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
				Integer duration = Math.round(Effect.getSecondtoFrames((((Double) effect.get("duration")).floatValue())));
				Float magnitude = ((Double) effect.get("magnitude")).floatValue();
				
				Action actionToAdd;
				if (persisting != null){
					Integer castLength =  Math.round(Effect.getSecondtoFrames((((Double) persisting.get("castLength")).floatValue())));
					Float moveSpeed = (Float) ((Double) persisting.get("moveSpeed")).floatValue();
					actionToAdd = new PersistingAction(name, cost, damage, range, 
							Pattern.valueOf(pattern), new Effect(duration, Type.valueOf(eff), magnitude), 
							description, castLength, moveSpeed);
				}else{
					actionToAdd = new Action(name, cost, damage, range, Pattern.valueOf(pattern),
							new Effect(duration, Type.valueOf(eff), magnitude), description);
				}
				
				Integer animationId = (Integer) action.get("animationId");
				if (animationId != null){
					actionToAdd.setAnimation(availableAnimations.get(animationId));
				}
				
				availableActions.put(id, actionToAdd);
			}	
		} 
		//Import Characters
		FileHandle charFile = Gdx.files.internal("yaml/characters.yml");
		try (InputStream is = charFile.read()){
			ArrayList<HashMap<String, Object>> characters = (ArrayList<HashMap<String, Object>>) yaml.load(is);
			for (HashMap<String, Object> character : characters){
				Integer id = (Integer) character.get("id");
				String name = (String) character.get("name");
				Integer health = (Integer) character.get("health");
				Integer maxHealth = (Integer) character.get("maxHealth");
				String hexColor = (String) character.get("hexColor");
				Float speed = (Float) ((Double) character.get("speed")).floatValue();
				Float castSpeed = (Float) ((Double) character.get("castSpeed")).floatValue();
				Integer xPosition = (Integer) character.get("xPosition");
				Integer yPosition = (Integer) character.get("yPosition");
				Boolean leftSide = (Boolean) character.get("leftSide");
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
									
				availableCharacters.put(id, characterToAdd);
			}
		}
		
		
	}
	
	
	// HELPER FUNCTIONS
	
	/**
	 * (Pre)loads all assets for this level
	 *
	 * This method add all of the important assets to the asset manager.  However,
	 * it does not block until the assets are loaded.  This allows us to draw a 
	 * loading screen while we still wait.
	 */
    private void load() {
		manager = new AssetManager();
		manager.setLoader(Mesh.class, new MeshLoader(new InternalFileHandleResolver()));
		assets = new Array<String>();
		
		manager.load(BCKGD_TEXTURE,Texture.class);
		assets.add(BCKGD_TEXTURE);
		
		manager.load(MENU_BCKGD_TEXTURE,Texture.class);
		assets.add(MENU_BCKGD_TEXTURE);
		
		manager.load(LOADING_TEXTURE,Texture.class);
		assets.add(LOADING_TEXTURE);
		
		manager.load(PRGRSBR_TEXTURE, Texture.class);
		assets.add(PRGRSBR_TEXTURE);
		statusBar = new Texture(PRGRSBR_TEXTURE);
		statusBkgLeft   = new TextureRegion(statusBar,0,0,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusBkgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,0,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusBkgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,0,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

		int offset = statusBar.getHeight()-PROGRESS_HEIGHT;
		statusFrgLeft   = new TextureRegion(statusBar,0,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusFrgRight  = new TextureRegion(statusBar,statusBar.getWidth()-PROGRESS_CAP,offset,PROGRESS_CAP,PROGRESS_HEIGHT);
		statusFrgMiddle = new TextureRegion(statusBar,PROGRESS_CAP,offset,PROGRESS_MIDDLE,PROGRESS_HEIGHT);

		
		MeshLoader.MeshParameter parameter = new MeshLoader.MeshParameter();
		parameter.attribs = new VertexAttribute[2];
		parameter.attribs[0] = new VertexAttribute(Usage.Position, 3, "vPosition");
		parameter.attribs[1] = new VertexAttribute(Usage.TextureCoordinates, 2, "vUV");

		manager.load(TILE_TEXTURE,Texture.class);
		assets.add(TILE_TEXTURE);
		
		manager.load(CHAR_1_TEXTURE,Texture.class);
		assets.add(CHAR_1_TEXTURE);
		manager.load(CHAR_2_TEXTURE,Texture.class);
		assets.add(CHAR_2_TEXTURE);
		manager.load(CHAR_3_TEXTURE,Texture.class);
		assets.add(CHAR_3_TEXTURE);
		manager.load(CHAR_4_TEXTURE,Texture.class);
		assets.add(CHAR_4_TEXTURE);
		manager.load(CHAR_5_TEXTURE,Texture.class);
		assets.add(CHAR_5_TEXTURE);
		
		manager.load(ICON_1_TEXTURE,Texture.class);
		assets.add(ICON_1_TEXTURE);
		manager.load(ICON_2_TEXTURE,Texture.class);
		assets.add(ICON_2_TEXTURE);
		manager.load(ICON_3_TEXTURE,Texture.class);
		assets.add(ICON_3_TEXTURE);
		manager.load(ICON_4_TEXTURE,Texture.class);
		assets.add(ICON_4_TEXTURE);
		manager.load(ICON_5_TEXTURE,Texture.class);
		assets.add(ICON_5_TEXTURE);
		
		manager.load(LIGHTNING_TEXTURE,Texture.class);
		assets.add(LIGHTNING_TEXTURE);
		manager.load(CAST_ANIMATION_SHEET,Texture.class);
		assets.add(CAST_ANIMATION_SHEET);
		
		manager.load(WHITE_BOX,Texture.class);
		assets.add(WHITE_BOX);
		
		mainMenuController = new MainMenuController(canvas, manager);
		
		
		manager.finishLoading();
		//for some reason the font loading has to be after the call to manager.finishLoading();
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = MENU_FONT_FILE;
		size2Params.fontParameters.size = MENU_FONT_SIZE;
		size2Params.fontParameters.color = Color.BLACK;
		manager.load(MENU_FONT_FILE, BitmapFont.class, size2Params);
		assets.add(MENU_FONT_FILE);
		size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = SELECT_FONT_FILE;
		size2Params.fontParameters.size = SELECT_FONT_SIZE;
		size2Params.fontParameters.color = Color.BLACK;
		manager.load(SELECT_FONT_FILE, BitmapFont.class, size2Params);
		assets.add(SELECT_FONT_FILE);
		
		try {
			loadFromYaml();
		} catch (IOException e) {
			System.out.println("ERROR LOADING FROM YAML");
			e.printStackTrace();
		}
		
		// We have to force the canvas to fully load (so we can draw something)
		initializeCanvas(LOADING_TEXTURE, MENU_FONT_FILE);
		
        // Sound controller manages its own material
        SoundController.PreLoadContent(manager);
    }
    
	/**
	 * Loads the assets used by the game canvas.
	 *
	 * This method loads the background and font for the canvas.  As these are
	 * needed to draw anything at all, we block until the assets have finished
	 * loading.
	 */
    private void initializeCanvas(String texture_msg, String fontFile) { 
    	canvas.setFont(new BitmapFont());
		Texture texture = manager.get(texture_msg, Texture.class);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		canvas.setBackground(texture);
		canvas.setWhite(manager.get(WHITE_BOX, Texture.class));
		
		//Font support
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
		
		if (manager.isLoaded(fontFile)) {
			canvas.setFont(manager.get(fontFile,BitmapFont.class));
		}
    }
    
    /**
     * Unloads all assets previously loaded.
     */
    private void unload() {
    	canvas.setBackground(null);
    	canvas.setFont(null);
    	
    	for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
		
    	// Unload sound separately
		SoundController.UnloadContent(manager);
    }
}

//TODO: Resize for statusBar
