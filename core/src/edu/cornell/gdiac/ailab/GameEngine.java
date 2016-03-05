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

import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.graphics.GL20.GL_BLEND;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.*;
import com.badlogic.gdx.utils.*;

import edu.cornell.gdiac.ailab.AIController.Difficulty;
import edu.cornell.gdiac.mesh.*;
import edu.cornell.gdiac.util.*;

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
		BEFORE,
		/** While we are playing the game */
		PLAY,
		/** When the game has ended, but we are still waiting on animation */
		FINISH,
		/** When the game is over */
		AFTER
	}
	
	public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK
	}
	
	// ASSET LOADING INFORMATION
	// Messages to display to the player
	/** The message font to use */
	private static final String FONT_FILE  = "fonts/Amyn.ttf";
	/** The size of the messages */
	private static final int FONT_SIZE = 70;
	/** Message while assets are loading */
	private static final String MESSG_LOAD = "Loading...";
	/** Message before the game has started */
	private static final String MESSG_BEFORE_1 = "Press any Key";
	private static final String MESSG_BEFORE_2 = "To Begin";
	/** Message when the player has lost */
	private static final String MESSG_LOST = "Game Over";
	/** Message when the player has won */
	private static final String MESSG_WON = "You Won!";
	/** Message telling the user how to restart */
	private static final String MESSG_RESTART = "Press \"R\" to Restart";

	/** Background image for the canvas */
	private static final String BCKGD_TEXTURE = "images/desert_sand.jpg";
	
	/** File storing the texture for a board tile */
	private static final String TILE_TEXTURE = "models/Tile.png";

	/** File storing the enemy texture for a ship */
	private static final String PLAYER_TEXTURE  = "models/Ship.png";
	/** File storing the player texture for a ship */
	private static final String ENEMY_TEXTURE = "models/ShipPlayer.png";
	
	private static final String WHITE_BOX = "images/white.png";

	// We keep sound information in the sound controller, as it belongs there
	
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Container to track the assets loaded so far */
	private Array<String> assets;
	
	//OLD
	/** Subcontroller for physics (CONTROLLER CLASS) */
    //private CollisionController physicsController;
    //OLD
    
    
	/** Subcontroller for gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;
    /** Subcontroller for selection menu (CONTROLLER CLASS) */
    private SelectionMenuController selectionMenuController;
    /** Subcontroller for action bar (CONTROLLER CLASS) */
    private ActionBarController actionBarController;
    /** Subcontroller for persisting actions (CONTROLLER CLASS) */
    private PersistingController persistingController;
    /** Subcontroller for ai selection (CONTROLLER CLASS) */
    private AIController aiController;
    /** Used to draw the game onto the screen (VIEW CLASS) */
    private GameCanvas canvas;
    
    //Current Models
    private GridBoard board;
    private List<Character> characters;
    private ActionBar bar;

    /** The current game state (SIMPLE FIELD) */
    private GameState gameState;
    /** The current in game state */
    private InGameState inGameState;
    /** How far along (0 to 1) we are in loading process */
	private float  gameLoad;
	
	/** 
	 * Constructs a new game engine
	 *
	 * We can only assign simple fields at this point, as there is no OpenGL context
	 */
    public GameEngine() {
    	gameState = GameState.LOAD;
    	inGameState = inGameState.NORMAL;
    	gameLoad  = 0.0f;
		canvas = new GameCanvas();
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
		if (didReset()) {
			resetGame();
		}
		
		// What we do depends on the game state
		switch (gameState) {
		case LOAD:
			updateLoad();
			drawLoad();
			break;
		case PLAY:
		case FINISH:
			updateGame();
		case BEFORE:
		case AFTER:
			drawGame();
			break;
		}
	}
	
	/**
	 * Restart the game, laying out all the ships and tiles
	 */
	public void resetGame() {
		// Local constants
        int BOARD_WIDTH  = 6; 
        int BOARD_HEIGHT = 4;
        int MAX_SHIPS    = 20;
        int MAX_PHOTONS  = 1024;

        gameState = GameState.PLAY;

        // Create the models.
        board = new GridBoard(BOARD_WIDTH,BOARD_HEIGHT);
        board.setTileTexture(manager.get(TILE_TEXTURE,Texture.class));
        characters = new LinkedList<Character>();
        //Texture playerTexture = manager.get(PLAYER_TEXTURE,Texture.class);
        Texture enemyTexture = manager.get(ENEMY_TEXTURE,Texture.class);
        
        characters.add(new Character(0,enemyTexture,Color.GREEN));
        characters.add(new Character(1,enemyTexture,Color.YELLOW));
        characters.add(new Character(2,enemyTexture,Color.RED));
//        characters.get(2).setAI(Difficulty.EASY);
        characters.add(new Character(3,enemyTexture,Color.BROWN));
//        characters.get(3).setAI(Difficulty.EASY);
        
        bar = new ActionBar();
        
		// Create the three subcontrollers
        gameplayController = new GameplayController(board,characters,bar);
        selectionMenuController = new SelectionMenuController(board,characters,bar);
        actionBarController = new ActionBarController(board,characters,bar);
        aiController = new AIController(board,characters,bar);
        persistingController = new PersistingController(board,characters,bar);
        
        //physicsController = new CollisionController(board, ships, photons);
	}
		
	/** 
	 * Returns true if the user reset the game.
	 *
	 * It also exits the game if the player chose to exit.
	 *
	 * @return true if the user reset the game.
	 */
	private boolean didReset() {
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
        	Gdx.app.exit();
        }

        if (gameState == GameState.BEFORE) {
            // start the game when the player hits 'the any key' 
        	for(int i = 0;i < 255;i++) {
        		if (Gdx.input.isKeyPressed(i)) {
        			gameState = GameState.PLAY;
        			return true;
        		}
        	}
        }

        if (gameState == GameState.PLAY || gameState == GameState.FINISH || gameState == GameState.AFTER) {
            // If the player presses 'R', reset the game.
            if (Gdx.input.isKeyPressed(Keys.R)) {
                gameState = GameState.PLAY;
                return true;
            }
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
        	if (board == null) {
                SoundController.LoadContent(manager);
                // Reset but still loading
        		resetGame();
        		gameState = GameState.LOAD;
        	}
        	if (gameLoad < 1.0f) {
        		gameLoad += 0.01f;
        	} else {
        		gameState = GameState.BEFORE;
        	}
      	}
	}
	
	/**
	 * Draws the game board while we are still loading
	 */
	public void drawLoad() {
		// Draw the game
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        canvas.begin();
		canvas.drawMessage(MESSG_LOAD);
        canvas.end();
    }
	
    /**
     * The primary update loop of the game; called while it is running.
     */
    public void updateGame() {
    	switch(inGameState){
    	case NORMAL:
    		actionBarController.update();
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			inGameState = InGameState.SELECTION;
    		} else if (actionBarController.isAISelection) {
    			aiController.update();
    		}
    		break;
    	case SELECTION:
    		selectionMenuController.update();
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			board.reset();
    		}
    		break;
    	case ATTACK:
    		gameplayController.update();
    		if (gameplayController.isDone()){
    			if (actionBarController.isPlayerSelection){
    				inGameState = InGameState.SELECTION;
    			} else {
    				inGameState = InGameState.NORMAL;
    			}
    		}
    		break;	
    	}
    	
    	persistingController.update();
    	
    	if (gameOver()){
    		inGameState = InGameState.NORMAL;
    		gameState = GameState.AFTER;
    	}
    }
    
    public boolean leftsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	return dead;
    }
    
    public boolean rightsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (!c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	return dead;
    }
    
    public boolean gameOver(){
    	return leftsideDead() || rightsideDead();
    }
    
    /**
     * Called to draw when we are playing the game.
     */
    public void drawGame() {
    	// Draw the game
		Gdx.gl.glClearColor(0.39f, 0.58f, 0.93f, 1.0f);  // Homage to the XNA years
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		canvas.begin();
		
		switch (gameState) {
        case BEFORE:
    		canvas.drawMessage(MESSG_BEFORE_1, MESSG_BEFORE_2);
    		break;
        case FINISH:
        case AFTER:
        	if (leftsideDead() && rightsideDead()){
        		canvas.drawText("A tie?", 400, 400, Color.BLACK);
        	} else if (leftsideDead()){
        		canvas.drawText("Hah you lost", 400, 400, Color.BLACK);
        	} else if (rightsideDead()){
        		canvas.drawText("Yay you beat an easy bot", 400, 400, Color.BLACK);
        	} else {
        		System.out.println("SHOULD NEVER GET HERE");
        	}
        	break;
        case LOAD:
    		canvas.drawMessage(MESSG_LOAD);
    		break;
        case PLAY:
        	board.draw(canvas);
            for (Character c : characters){
            	c.draw(canvas);
            }
            bar.draw(canvas);
        	break;
        }
        canvas.end();
	}
	
	/**
	 * Called when the Application is resized. 
	 * 
	 * This can happen at any point during a non-paused state
	 */
	public void resize(int width, int height) {
		canvas.resize();
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
		
		MeshLoader.MeshParameter parameter = new MeshLoader.MeshParameter();
		parameter.attribs = new VertexAttribute[2];
		parameter.attribs[0] = new VertexAttribute(Usage.Position, 3, "vPosition");
		parameter.attribs[1] = new VertexAttribute(Usage.TextureCoordinates, 2, "vUV");

		manager.load(TILE_TEXTURE,Texture.class);
		assets.add(TILE_TEXTURE);
		
		manager.load(ENEMY_TEXTURE,Texture.class);
		assets.add(ENEMY_TEXTURE);
		manager.load(PLAYER_TEXTURE,Texture.class);
		assets.add(PLAYER_TEXTURE);
		
		manager.load(WHITE_BOX,Texture.class);
		assets.add(WHITE_BOX);
		
		manager.finishLoading();
		
		// We have to force the canvas to fully load (so we can draw something)
		initializeCanvas();
		
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
    private void initializeCanvas() {    	
    	canvas.setFont(new BitmapFont());
		Texture texture = manager.get(BCKGD_TEXTURE, Texture.class);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		canvas.setBackground(texture);
		canvas.setWhite(manager.get(WHITE_BOX, Texture.class));
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

