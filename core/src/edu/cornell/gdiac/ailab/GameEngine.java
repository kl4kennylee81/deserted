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
	private static final String BCKGD_TEXTURE = "images/stars.jpg";
	
	/** File storing the 3D model for a board tile */
	private static final String TILE_MODEL   = "models/Tile.obj";
	/** File storing the texture for a board tile */
	private static final String TILE_TEXTURE = "models/Tile.png";

	/** File storing the 3D model for the ship */
	private static final String SHIP_MODEL = "models/Ship.obj";
	/** File storing the enemy texture for a ship */
	private static final String ENEMY_TEXTURE  = "models/Ship.png";
	/** File storing the player texture for a ship */
	private static final String PLAYER_TEXTURE = "models/ShipPlayer.png";
	/** File storing the 3D model for the afterburner */
	private static final String FIRE_MODEL   = "models/Fire.obj";
	/** File storing the fire texture for the afterburner  */
	private static final String FIRE_TEXTURE = "models/Fire.png";

	/** File storing information about the 3D model */
	private static final String PHOTON_MODEL   = "models/Photon.obj";
	/** File storing the texture for the 3D model */
	private static final String PHOTON_TEXTURE = "models/Photon.png";

	// We keep sound information in the sound controller, as it belongs there
	
	/** AssetManager to load game assets (textures, sounds, etc.) */
	private AssetManager manager;
	/** Container to track the assets loaded so far */
	private Array<String> assets;
	
	/** Subcontroller for physics (CONTROLLER CLASS) */
    private CollisionController physicsController;
	/** Subcontroller for gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;
    /** Used to draw the game onto the screen (VIEW CLASS) */
    private GameCanvas canvas;
    
    /** XBox 360 controller, IF one is connected */
    private XBox360Controller xbox;
    
    // Location and animation information for game objects (MODEL CLASSES) 
    /** The grid of tiles (MODEL CLASS) */
    private Board board; 
    /** The ship objects (MODEL CLASS) */   
    private ShipList ships; 
    /** Collection of photons on screen. (MODEL CLASS) */ 
    private PhotonPool photons; 

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
		
		// Attach an XBox controller if one exists
		if (Controllers.getControllers().size > 0) {
			Controller controller = Controllers.getControllers().get(0);
			if (controller.getName().toLowerCase().contains("xbox") &&
				controller.getName().contains("360")) {
				xbox = new XBox360Controller(0);
			}
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
        int BOARD_WIDTH  = 40; 
        int BOARD_HEIGHT = 40;
        int MAX_SHIPS    = 20;
        int MAX_PHOTONS  = 1024;

        gameState = GameState.PLAY;

        // Create the models.
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
		board.setTileMesh(createMesh(TILE_MODEL,TILE_TEXTURE));
		        
        ships = new ShipList(MAX_SHIPS);
        ships.setPlayerMesh(createMesh(SHIP_MODEL,PLAYER_TEXTURE));
        ships.setEnemyMesh(createMesh(SHIP_MODEL,ENEMY_TEXTURE));
        ships.setFireMesh(createMesh(FIRE_MODEL,FIRE_TEXTURE));

        photons = new PhotonPool(MAX_PHOTONS);
        photons.setPhotonMesh(createMesh(PHOTON_MODEL,PHOTON_TEXTURE));
        
		// Create the two subcontroller
        gameplayController = new GameplayController(board,ships,photons);
        physicsController = new CollisionController(board, ships, photons);
	}
		
	/** 
	 * Returns true if the user reset the game.
	 *
	 * It also exits the game if the player chose to exit.
	 *
	 * @return true if the user reset the game.
	 */
	private boolean didReset() {
		if (xbox != null && xbox.getGuide()) {
        	Gdx.app.exit();			
		}
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
        	Gdx.app.exit();
        }

        if (gameState == GameState.BEFORE) {
            // start the game when the player hits 'the any key' 
        	if (xbox != null && xbox.getStart()) {
    			gameState = GameState.PLAY;
    			return true;
        	} 
        		
        	for(int i = 0;i < 255;i++) {
        		if (Gdx.input.isKeyPressed(i)) {
        			gameState = GameState.PLAY;
        			return true;
        		}
        	}
        }

        if (gameState == GameState.PLAY || gameState == GameState.FINISH || gameState == GameState.AFTER) {
            // If the player presses 'R', reset the game.
        	if (xbox != null && xbox.getBack()) {
                gameState = GameState.PLAY;
                return true;        		
        	}
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
        canvas.setEyePan(gameLoad);
        
        if (ships != null) {
	        canvas.begin(ships.getPlayer().getX(), ships.getPlayer().getY());
    	    // Perform the three required passes
			gl20.glEnable(GL_BLEND);
	        board.draw(canvas);
	        ships.draw(canvas);
	        photons.draw(canvas);
	    } else {
	        canvas.begin();
	    }	

		canvas.drawMessage(MESSG_LOAD, Color.WHITE);
        canvas.end();
    }
    
    /**
     * The primary update loop of the game; called while it is running.
     */
    public void updateGame() {
    	// Update the ships
		gameplayController.update();

		// Update the other elements
		board.update();
		photons.update();

		// Resolve any collisions
		physicsController.update();

        // if the player ship is dead, end the game with a Game Over:
		if (gameState == GameState.PLAY) {
			if (!ships.getPlayer().isActive()) {
				gameState = GameState.FINISH;
				Sound s = SoundController.get(SoundController.GAME_OVER_SOUND);
				s.play(1); // LibGDX Bug: This really needs a priority system
			} else if (ships.numActive() <= 1) {
				gameState = GameState.FINISH;
			}
		} else if (gameState == GameState.FINISH) {
			if (!ships.getPlayer().isAlive() || ships.numAlive() <= 1) {
				gameState = GameState.AFTER;
			}
        }
    }
    
    /**
     * Called to draw when we are playing the game.
     */
    public void drawGame() {
        canvas.setEyePan(1.0f);
        canvas.begin(ships.getPlayer().getX(), ships.getPlayer().getY());

        // Perform the three required passes
		gl20.glEnable(GL_BLEND);
        board.draw(canvas);
        ships.draw(canvas);
        photons.draw(canvas);

        // Optional message pass
        switch (gameState) {
        case BEFORE:
    		canvas.drawMessage(MESSG_BEFORE_1, MESSG_BEFORE_2, Color.WHITE);
    		break;
        case FINISH:
        case AFTER:
        	if (!ships.getPlayer().isActive()) {
        		canvas.drawMessage(MESSG_LOST, MESSG_RESTART, Color.WHITE);
        	} else {
        		canvas.drawMessage(MESSG_WON, MESSG_RESTART, Color.WHITE);
        	}
        	break;
        case LOAD:
    		canvas.drawMessage(MESSG_LOAD, Color.WHITE);
    		break;
        case PLAY:
        	// No message.
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
		
		// We have to force the canvas to fully load (so we can draw something)
		initializeCanvas();
		
		MeshLoader.MeshParameter parameter = new MeshLoader.MeshParameter();
		parameter.attribs = new VertexAttribute[2];
		parameter.attribs[0] = new VertexAttribute(Usage.Position, 3, "vPosition");
		parameter.attribs[1] = new VertexAttribute(Usage.TextureCoordinates, 2, "vUV");

		// Board tile
		manager.load(TILE_MODEL,Mesh.class,parameter);
		assets.add(TILE_MODEL);
		manager.load(TILE_TEXTURE,Texture.class);
		assets.add(TILE_TEXTURE);

		// Ship information
		manager.load(SHIP_MODEL,Mesh.class,parameter);
		assets.add(SHIP_MODEL);
		manager.load(FIRE_MODEL,Mesh.class,parameter);
		assets.add(FIRE_MODEL);
		manager.load(ENEMY_TEXTURE,Texture.class);
		assets.add(ENEMY_TEXTURE);
		manager.load(PLAYER_TEXTURE,Texture.class);
		assets.add(PLAYER_TEXTURE);
		manager.load(FIRE_TEXTURE,Texture.class);
		assets.add(FIRE_TEXTURE);

		// Photon information
		manager.load(PHOTON_MODEL,Mesh.class,parameter);
		assets.add(PHOTON_MODEL);
		manager.load(PHOTON_TEXTURE,Texture.class);
		assets.add(PHOTON_TEXTURE);

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
		// Load the background.
		manager.load(BCKGD_TEXTURE,Texture.class);
		assets.add(BCKGD_TEXTURE);
		
		// Provide basic font support
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
		
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
		manager.finishLoading();
		
		if (manager.isLoaded(BCKGD_TEXTURE)) {
			Texture texture = manager.get(BCKGD_TEXTURE, Texture.class);
			texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			canvas.setBackground(texture);
		}
		if (manager.isLoaded(FONT_FILE)) {
			canvas.setFont(manager.get(FONT_FILE,BitmapFont.class));
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
    
    /**
     * Returns a new textured mesh from the given components.
     *
     * If either model or texture has not yet been loaded by the asset manager,
     * this method returns null.
     *
     * @param model the filename of the mesh model
     * @param texture the filename the texture to wrap the model
     *
     * @return a new textured mesh from the given components.
     */
	private TexturedMesh createMesh(String model, String texture) {
		if (manager.isLoaded(model) && manager.isLoaded(texture)) {
			TexturedMesh mesh = new TexturedMesh(manager.get(model, Mesh.class));
			Texture mtext = manager.get(texture,Texture.class);
			mesh.setTexture(mtext);
			return mesh;
		}
		return null;
	}

}

