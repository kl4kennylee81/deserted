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

import java.io.File;
import java.io.FileInputStream;

//import static com.badlogic.gdx.Gdx.gl20;
//import static com.badlogic.gdx.graphics.GL20.GL_BLEND;

//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
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

//import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.mesh.*;
import edu.cornell.gdiac.ailab.GameCanvas;
//import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.ailab.GameplayController.InGameState;

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
	
	public static final boolean dataGen = true;
	
	/** 
	 * Enumeration defining the game state
	 */
	public static enum GameState {
		/** While we are still loading assets */
		LOAD,
		/** After loading, but before we start the game */
		MENU,
		LEVEL_MENU,
		/** While we are playing the game */
		PLAY,
		/** When the game is currently paused */
		PAUSED,
		/** When the game has ended, but we are still waiting on animation */
		FINISH,
		/** When the game is over */
		AFTER,
		/** When we are using one of the editors */
		EDITOR
	}


	private static File ROOT;
	/** Background image for the canvas */
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
    
	/**Currently used gameplay subcontroller */
	private GameplayController curGameplayController;
	/**Subcontroller for tutorial (CONTROLLER CLASS) */
	private TutorialGameplayController tutorialGameplayController;
	/**Subcontroller for gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;
    /** Used to draw the game onto the screen (VIEW CLASS) */
    private GameCanvas canvas;
    /** Subcontroller for main menu (CONTROLLER CLASS) */
    private MainMenuController mainMenuController;
    /** Subcontroller for mouse controls (CONTROLLER CLASS) */
    private MouseOverController mouseOverController;
    
    private EditorController editorController;
    
    
    
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
    private HashMap<String, HashMap<String, Object>> levelDefs;
    
    /** The current game state (SIMPLE FIELD) */
    private GameState gameState;
    /** How far along (0 to 1) we are in loading process */
	private float  gameLoad;
	
	/** What data file number we are on */
	private int fileNum;
	
	/** 
	 * Constructs a new game engine
	 *
	 * We can only assign simple fields at this point, as there is no OpenGL context
	 */
    public GameEngine() {
    	setRoot();
    	gameState = GameState.LOAD;
    	gameLoad  = 0.0f;
		canvas = new GameCanvas();
		FileHandle file = Gdx.files.internal("data/fileinfo.txt");
		fileNum = Integer.parseInt(file.readString());
		
		mouseOverController = new MouseOverController(canvas);

		editorController = null;
		gameplayController = new GameplayController(mouseOverController, file, fileNum);
		tutorialGameplayController = new TutorialGameplayController(mouseOverController, file, fileNum);
		
		updateMeasures();
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
    
    public void updateMeasures(){
		width = (int)(BAR_WIDTH_RATIO*canvas.getWidth());
		centerY = (int)(BAR_HEIGHT_RATIO*canvas.getHeight());
		centerX = canvas.getWidth()/2;
		float sx = ((float)canvas.getWidth())/STANDARD_WIDTH;
		float sy = ((float)canvas.getHeight())/STANDARD_HEIGHT;
		scale = (sx < sy ? sx : sy);
    }
    
    public void startGame(String levelName) throws IOException {
    	if (this.levelDefs.containsKey(levelName)){
        	initializeCanvas(Constants.BCKGD_TEXTURE, Constants.SELECT_FONT_FILE);
        	Level level = null;
    		level = this.getLevel(levelName);
    		
        	if (level.getTutorialSteps() == null){
        		gameplayController.resetGame(level);
        		curGameplayController = gameplayController;
            	gameState = GameState.PLAY;
        	} else {
        		tutorialGameplayController.resetGame(level);
        		curGameplayController = tutorialGameplayController;
        		gameState = GameState.PLAY;
        	}
    	}
    	// start matching with keywords to get to levels, options, etc. atm its just editors
    	else {
    		this.startKeyword(levelName);
    	}
    	
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
		try {
			load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
        
        //update the input controller
        InputController.update();
		
		// What we do depends on the game state
		switch (gameState) {
		case LOAD:
			updateLoad();
			drawLoad();
			canvas.end();
			break;
		case MENU:
			try {
				updateMenu();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			canvas.end();
			break;
		case LEVEL_MENU:
			updateLevelMenu();
			break;
		case PLAY:
			updatePlay();
			drawPlay();
			canvas.end();
			break;
		case FINISH:
			//Graphics after play is over?
			canvas.end();
			break;
		case PAUSED:
			updatePaused();
			drawPlay();
			drawPaused();
			canvas.end();
			break;
		case AFTER:
			//updateAfter();
			drawAfter();
			canvas.end();
			break;
		case EDITOR:
			canvas.end();
			updateEditor();
			break;
		}
		
	}
		
	private void updateLevelMenu() {
		// TODO Auto-generated method stub
		
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
        if (gameState != GameState.LOAD && gameState != GameState.EDITOR
        		&& InputController.pressedR()) {
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
	 * @throws IOException 
	 */
	private void updateMenu() throws IOException {
		mainMenuController.update();
		if (mainMenuController.isDone()){
			loadNextMenu(mainMenuController.levelName);
		}
	}
	
	private void startKeyword(String keyword) {
		if (keyword == "Action Editor") {
			try {
				editorController = new ActionEditorController();
				gameState = gameState.EDITOR;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (keyword == "Character Editor") {
			try {
				editorController = new CharacterEditorController();
				gameState = gameState.EDITOR;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (keyword == "Level Editor") {
			try {
				editorController = new LevelEditorController();
				gameState = gameState.EDITOR;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadNextMenu(String levelName) throws IOException {
		startGame(levelName);
	}

	/**
     * The primary update loop of the game; called while it is running.
     */
    public void updatePlay() {
    	curGameplayController.update();
    	if (curGameplayController.isDone()){
			gameState = GameState.AFTER;
    	}
    	if (InputController.pressedP()){
    		gameState = GameState.PAUSED;
    	}
    }
	
    
    /**
     * Update loop if we are in the paused state
     */
    public void updatePaused(){
    	if(InputController.pressedP()){
    		gameState = GameState.PLAY;
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
	
	public void drawPaused(){
		canvas.drawCenteredText("PAUSED", canvas.getWidth()/2, canvas.getHeight()/2, Color.BLACK);
	}
	
	public void drawPlay() {
		curGameplayController.drawPlay(canvas); 
	}
	
	public void drawAfter() {
		curGameplayController.drawAfter(canvas);
	}
	
	public void updateEditor() {
		editorController.update();
		editorController.draw();
    	if (editorController.isDone()) {
    		editorController = null;
    		mainMenuController.resetMenu();
    		gameState = gameState.MENU;
    	}
	}
    
	
	/**
	 * Called when the Application is resized. 
	 * 
	 * This can happen at any point during a non-paused state
	 */
	public void resize(int width, int height) {
		canvas.resize();
		updateMeasures();
		InputController.setCanvas(canvas);
		MouseOverController.setCanvas(canvas);
		//do we need all this?^
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
		File levelFile = new File(ROOT,"yaml/levels.yml");
		HashMap<String, Object> targetLevelDef;
		try (InputStream iS = new FileInputStream(levelFile)){
			levelDefs = (HashMap<String, HashMap<String, Object>>) yaml.load(iS);
			targetLevelDef= levelDefs.get(levelId);	
			
		}
		return ObjectLoader.getInstance().createLevel(targetLevelDef);
	}

	@SuppressWarnings("unchecked")
	private void loadLevels() throws IOException{
		Yaml yaml = new Yaml();
		File levelFile = new File(ROOT,"yaml/levels.yml");
		try (InputStream iS = new FileInputStream(levelFile)){
			this.levelDefs = (HashMap<String, HashMap<String, Object>>) yaml.load(iS);
		}	
	}
	
	// HELPER FUNCTIONS
	
	/**
	 * (Pre)loads all assets for this level
	 *
	 * This method add all of the important assets to the asset manager.  However,
	 * it does not block until the assets are loaded.  This allows us to draw a 
	 * loading screen while we still wait.
	 * @throws IOException 
	 */
    private void load() throws IOException {
		manager = new AssetManager();
		manager.setLoader(Mesh.class, new MeshLoader(new InternalFileHandleResolver()));
		assets = new Array<String>();
		
		manager.load(Constants.BCKGD_TEXTURE,Texture.class);
		assets.add(Constants.BCKGD_TEXTURE);
		
		manager.load(Constants.MENU_BCKGD_TEXTURE,Texture.class);
		assets.add(Constants.MENU_BCKGD_TEXTURE);
		
		manager.load(Constants.LOADING_TEXTURE,Texture.class);
		assets.add(Constants.LOADING_TEXTURE);
		
		manager.load(Constants.MENU_LOGO,Texture.class);
		assets.add(Constants.MENU_LOGO);
		
		manager.load(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
		assets.add(Constants.MENU_HIGHLIGHT_TEXTURE);
		
		manager.load(Constants.LEVEL_SELECT_BOSS,Texture.class);
		assets.add(Constants.LEVEL_SELECT_BOSS);
		
		manager.load(Constants.LEVEL_SELECT_REG,Texture.class);
		assets.add(Constants.LEVEL_SELECT_REG);
		
		manager.load(Constants.PRGRSBR_TEXTURE, Texture.class);
		assets.add(Constants.PRGRSBR_TEXTURE);
		statusBar = new Texture(Constants.PRGRSBR_TEXTURE);
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
		
		manager.load(Constants.WHITE_BOX,Texture.class);
		assets.add(Constants.WHITE_BOX);
		
		manager.finishLoading();
		
		// load all the level defs
		loadLevels();
		mainMenuController = new MainMenuController(canvas, manager, mouseOverController,levelDefs);
		
		//for some reason the font loading has to be after the call to manager.finishLoading();
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = Constants.MENU_FONT_FILE;
		size2Params.fontParameters.size = Constants.MENU_FONT_SIZE;
		size2Params.fontParameters.color = Color.WHITE;
		manager.load(Constants.MENU_FONT_FILE, BitmapFont.class, size2Params);
		assets.add(Constants.MENU_FONT_FILE);

		size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = Constants.SELECT_FONT_FILE;
		size2Params.fontParameters.size = Constants.SELECT_FONT_SIZE;
		size2Params.fontParameters.color = Color.WHITE;
		manager.load(Constants.SELECT_FONT_FILE, BitmapFont.class, size2Params);
		assets.add(Constants.SELECT_FONT_FILE);
		size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = Constants.TUTORIAL_FONT_FILE;
		size2Params.fontParameters.size = Constants.TUTORIAL_FONT_SIZE;
		size2Params.fontParameters.color = Constants.TUTORIAL_FONT_COLOR;
		manager.load(Constants.TUTORIAL_FONT_FILE, BitmapFont.class, size2Params);
		assets.add(Constants.TUTORIAL_FONT_FILE);
		
		// We have to force the canvas to fully load (so we can draw something)
		initializeCanvas(Constants.LOADING_TEXTURE, Constants.MENU_FONT_FILE);
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
		canvas.setWhite(manager.get(Constants.WHITE_BOX, Texture.class));
		
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
