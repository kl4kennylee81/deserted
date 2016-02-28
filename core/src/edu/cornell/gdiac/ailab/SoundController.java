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
import com.badlogic.gdx.assets.*;

/** 
 *  Static controller class for managing sound.
 */
public class SoundController {
	// Static names to the sounds 
	/** Weapon fire sound */
	public static final String FIRE_SOUND = "fire";
	/** Falling (to death) sound */
	public static final String FALL_SOUND = "fall";
	/** Collision sound */
	public static final String BUMP_SOUND = "bump";
	/** Game over (player lost) sound */
	public static final String GAME_OVER_SOUND = "over";

	/** Hash map storing references to sound assets (after they are loaded) */
	private static HashMap<String, Sound> soundBank; 

	// Files storing the sound references
	/** File to weapon fire */
	private static final String FIRE_FILE = "sounds/Fire.mp3";
	/** File to falling sound */
	private static final String FALL_FILE = "sounds/Fall.mp3";
	/** File to collision sound */
	private static final String BUMP_FILE = "sounds/Bump.mp3";
	/** File to game over (player lost) */
	private static final String OVER_FILE = "sounds/GameOver.mp3";

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
		manager.load(FIRE_FILE,Sound.class);
		manager.load(FALL_FILE,Sound.class);
		manager.load(BUMP_FILE,Sound.class);
		manager.load(OVER_FILE,Sound.class);
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
		if (manager.isLoaded(FIRE_FILE)) {
			soundBank.put(FIRE_SOUND,manager.get(FIRE_FILE,Sound.class));
		}
		if (manager.isLoaded(FALL_FILE)) {
			soundBank.put(FALL_SOUND,manager.get(FALL_FILE,Sound.class));
		}
		if (manager.isLoaded(BUMP_FILE)) {
			soundBank.put(BUMP_SOUND,manager.get(BUMP_FILE,Sound.class));
		}
		if (manager.isLoaded(OVER_FILE)) {
			soundBank.put(GAME_OVER_SOUND,manager.get(OVER_FILE,Sound.class));
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
			manager.unload(FIRE_FILE);
			manager.unload(FALL_FILE);
			manager.unload(BUMP_FILE);
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
}