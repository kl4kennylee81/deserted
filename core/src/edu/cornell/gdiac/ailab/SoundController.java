/* 
 * SoundController.java
 *
 * Sound is one of the trickiest things to manage in a game. While playing
 * sounds is easy, it is always difficult to figure out what classes the
 * the sounds should go into.  This is compounded by the fact that sounds
 * DO NOT align with frame boundaries and run across multiple frames.
 *
 * For this application, we have solved this problem by using a static 
 * class for the sound controller.  Static classes benefit from the fact
 * that they can be used anywhere and we never need to worry about storing
 * a reference to them.  Just use the classname and you are done.
 *
 * To be a candidate for a static class, the class should never need to
 * change state (after initialization) and should never need to reference
 * any other class in the application.  Both of these are true here.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import com.badlogic.gdx.audio.*;

import edu.cornell.gdiac.ailab.GameEngine.GameState;

import com.badlogic.gdx.assets.*;

/** 
 *  Static controller class for managing sound.
 */
public class SoundController {
	// Static names to the sounds 
	/** Game over (player lost) sound */
	public static final String GAME_OVER_SOUND = "over";

	/** Hash map storing references to sound assets (after they are loaded) */
	private static HashMap<String, Sound> soundBank;
	
	/** menu sound player */
	private static Music menuSound;
	
	/** battle sound player */
	private static Music battleSound;

	// Files storing the sound references
	/** File to game over (player lost) */
	private static final String OVER_FILE = "sounds/GameOver.mp3";

	/** Menu music file*/
	private static final String MENU_MUSIC_FILE  = "sounds/menu_theme.mp3";
	
	/** Battle music file*/
	private static final String BATTLE_MUSIC_FILE  = "sounds/battle_theme_1.mp3";
	
	/** 
	 * Preloads the assets for this Sound controller.
	 * 
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the first step: telling it what to load.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void PreLoadContent(AssetManager manager) {
		manager.load(OVER_FILE,Sound.class);
		manager.load(MENU_MUSIC_FILE,Music.class);
		manager.load(BATTLE_MUSIC_FILE,Music.class);
	}

	/** 
	 * Loads the assets for this Sound controller.
	 * 
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the second step: extracting assets from the manager after it has
	 * finished loading them.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void LoadContent(AssetManager manager) {
		soundBank = new HashMap<String, Sound>();
		if (manager.isLoaded(OVER_FILE)) {
			soundBank.put(GAME_OVER_SOUND,manager.get(OVER_FILE,Sound.class));
		}
		if (manager.isLoaded(MENU_MUSIC_FILE)) {
			menuSound = manager.get(MENU_MUSIC_FILE,  Music.class);
		}
		if (manager.isLoaded(BATTLE_MUSIC_FILE)) {
			battleSound = manager.get(BATTLE_MUSIC_FILE,  Music.class);
		}
	}

	/** 
	 * Unloads the assets for this GameCanvas
	 * 
	 * This method erases the static variables.  It also deletes the
	 * associated textures from the assert manager.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void UnloadContent(AssetManager manager) {
		if (soundBank != null) {
			soundBank.clear();
			soundBank = null;
			manager.unload(OVER_FILE);
		}
	}
	
	/**
	 * Returns the sound for the given name
	 *
	 * @return the sound for the given name
	 */
	public static Sound get(String key) {
        return soundBank.get(key);
    }
	
	public static void update(GameState gs){
		switch (gs) {
		case LOAD:
		case LEVEL_MENU:
		case PAUSED:
		case SELECT:
			break;
		case MENU:
		case EDITOR:
		case CUSTOMIZE:
			if (menuSound.isPlaying()) break;
			else {
				battleSound.stop();
				menuSound.play();
			}
			break;
		case PLAY:			
			if (battleSound.isPlaying()) break;
			else {
				menuSound.stop();
				battleSound.play();
			}
			break;
		case FINISH:
		case AFTER:
			battleSound.stop();
			menuSound.stop();
			((Sound)soundBank.get(GAME_OVER_SOUND)).play();
			break;
		}
	}
}
