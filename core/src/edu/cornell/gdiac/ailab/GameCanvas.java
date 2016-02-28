/*
 * GameCanvas.cs
 *
 * This class is similar to the GameCanvas for the previous lab, except that now
 * we have support for 3D models.  Because we are trying to hide as much of the
 * 3D specific code as possible, we internalize many features that do not belong
 * in this class as part of a proper model-view-controller separation.  For example,
 * we have specific methods for drawing Tiles or Ships, instead of a general method
 * for drawing 3D models.  
 *
 * Needless to say, you should not emulate this GameCanvas at all; you should have
 * much better separation in your code.  As your games will (likely) be 2D, this
 * should not be a problem.
 *
 * Author: Walker M. White, Cristian Zaloj
 * Based on original AI Game Lab by Yi Xu and Don Holden, 2007
 * LibGDX version, 1/24/2015
 */
package edu.cornell.gdiac.ailab;

import static com.badlogic.gdx.Gdx.gl20;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.*;

import edu.cornell.gdiac.mesh.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 * This version of GameCanvas combines both 3D and 2D drawing.  As this combination
 * is complicated, and we want to hide the details, we make a lot of design decisions 
 * in this canvas that are not ideal.  Do not use this canvas as an example of good
 * architecture design.
 */
public class GameCanvas {

	/** Canvas background image. */
	private Texture background;
	/** Font object for displaying images */
	private BitmapFont displayFont;
	/** Glyph layout to compute the size */
	private GlyphLayout displayLayout;
		
	// Constants only needed locally.
	/** Reverse the y-direction so that it is consistent with SpriteBatch */
	private static final Vector3 UP_REVERSED = new Vector3(0,-1,0);
	/** For managing the camera pan interpolation at the start of the game */
	private static final Interpolation.SwingIn SWING_IN = new Interpolation.SwingIn(0.1f);
	/** Distance from the eye to the target */
	private static final float EYE_DIST  = 400.0f;
	/** Field of view for the perspective */
	private static final float FOV = 0.7f;
	/** Near distance for perspective clipping */
	private static final float NEAR_DIST = 10.0f;
	/** Far distance for perspective clipping */
	private static final float FAR_DIST  = 500.0f;
	/** Horizontal clipping width */
	private static final int   CLIP_X = 500;
	/** Vertical clipping width */
	private static final int   CLIP_Y = 450;
	/** Multiplicative factors for initial camera pan */
	private static final float INIT_TARGET_PAN = 0.1f;
	private static final float INIT_EYE_PAN = 0.05f;
	/** Tile drawing constants */
	private static final float TILE_SIZE = 32.0f;
	private static final float TILE_DEPTH = 3.0f;
	/** Ship drawing constants */
	private static final float SHIP_SIZE = 30.0f;
	private static final float SHIP_FALL_TRANS = -16f;
	private static final float SHIP_FALL_X_SKEW = 0.04f;
	private static final float SHIP_FALL_Z_SKEW = 0.03f;
	/** Photon drawing constants */
	private static final float PHOTON_TRANS = -15f;
	private static final float PHOTON_SIZE  = 12f;
	private static final float PHOTON_DECAY = 8f;
	/** Constants for shader program locations */
	private static final String SHADER_VERTEX = "shaders/Tinted.vert";
	private static final String SHADER_FRAGMT = "shaders/Tinted.frag";
	private static final String SHADER_U_TEXT = "unTexture";
	private static final String SHADER_U_VIEWP = "unVP";
	private static final String SHADER_U_WORLD = "unWorld";
	private static final String SHADER_U_TINT = "unTint";
	
	// Instance attributes	
	/** Value to cache window width (if we are currently full screen) */
	int width;
	/** Value to cache window height (if we are currently full screen) */
	int height;
	
	/** Draws Sprite objects to the background and foreground (e.g. font) */
	protected SpriteBatch spriteBatch;
	/** Draws 3D objects in the intermediate levels between background and foreground */
	protected ShaderProgram program;
	/** Track whether or not we are actively drawing (for error checking) */
	private boolean active;
	/** Track whether or not we are actively 3D drawing (for error checking) */
	private boolean shading;
	/** The panning factor for the eye, used when the game first loads */
	private float eyepan;
	/** 3D Models for the various passes. */
	private TexturedMesh model;

	// For managing the camera and perspective
	/** Orthographic camera for the SpriteBatch layer */
	private OrthographicCamera spriteCam;
	/** Target for Perspective FOV */
	private Vector3 target;
	/** Eye for Perspective FOV */
	private Vector3 eye;

	// CACHE OBJECTS
	/** Projection Matrix */
	private Matrix4 proj;
	/** View Matrix */
	private Matrix4 view;
	/** World Matrix */
	private Matrix4 world;
	/** Temporary Matrix (for Calculations) */
	private Matrix4 tmpMat;
	
	/** Temporary Vectors */
	private Vector3 tmp0;
	private Vector3 tmp1;
	private Vector2 tmp2d;

	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.  This constructor initializes all
	 * of the necessary graphics objects.
	 */
	public GameCanvas() {
		// Initialize instance attributes
		active  = false;
		shading = false;
		eyepan  = 0.0f;
		
		
		// Compile shader and assign texture slot		
		program = new ShaderProgram(Gdx.files.internal(SHADER_VERTEX),Gdx.files.internal(SHADER_FRAGMT));
		program.begin();
		//gl20.glEnable(GL20.GL_BLEND);
		program.setUniformi(SHADER_U_TEXT,0);
		program.end();

		// Create and initialize the sprite batch
		spriteBatch = new SpriteBatch();
		//spriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
		spriteCam = new OrthographicCamera(getWidth(),getHeight());
		spriteCam.setToOrtho(false);
		spriteBatch.setProjectionMatrix(spriteCam.combined);
		
		// Initialize the perspective camera objects
		eye = new Vector3();
		target = new Vector3();
		world = new Matrix4();
		view  = new Matrix4();
		proj  = new Matrix4();
		
		// Initialize the cache objects
		tmpMat = new Matrix4();
		tmp0  = new Vector3();
		tmp1  = new Vector3();
		tmp2d = new Vector2();
		model = null;
	}
	
	/**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		
		// Dispose what requires a manual deletion.
		spriteBatch.dispose();
    	spriteBatch = null;
    	program.dispose();
    	program = null;
    	
    	// Everything else is just garbage collected.
    }

	/**
	 * Returns the width of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getWidth()
	 *
	 * @return the width of this canvas
	 */
	public int getWidth() {
		return Gdx.graphics.getWidth();
	}
	
	/**
	 * Changes the width of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 * 
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param width the canvas width
	 */
	public void setWidth(int width) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, getHeight());
		}
		resize();
	}
	
	/**
	 * Returns the height of this canvas
	 *
	 * This currently gets its value from Gdx.graphics.getHeight()
	 *
	 * @return the height of this canvas
	 */
	public int getHeight() {
		return Gdx.graphics.getHeight();
	}
	
	/**
	 * Changes the height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param height the canvas height
	 */
	public void setHeight(int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(getWidth(), height);	
		}
		resize();
	}
	
	/**
	 * Returns the dimensions of this canvas
	 *
	 * @return the dimensions of this canvas
	 */
	public Vector2 getSize() {
		return new Vector2(width,height);
	}
	
	/**
	 * Changes the width and height of this canvas
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * This method has no effect if the resolution is full screen.  In that case, the
	 * resolution was fixed at application startup.  However, the value is cached, should
	 * we later switch to windowed mode.
	 *
	 * @param width the canvas width
	 * @param height the canvas height
	 */
	public void setSize(int width, int height) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		this.width = width;
		this.height = height;
		if (!isFullscreen()) {
			Gdx.graphics.setWindowedMode(width, height);
		}
		resize();
	}
	
	/**
	 * Returns whether this canvas is currently fullscreen.
	 *
	 * @return whether this canvas is currently fullscreen.
	 */	 
	public boolean isFullscreen() {
		return Gdx.graphics.isFullscreen(); 
	}
	
	/**
	 * Sets whether or not this canvas should change to fullscreen.
	 *
	 * Changing to fullscreen will use the resolution of the application at startup.
	 * It will NOT use the dimension settings of this canvas (which are for window
	 * display only).
	 *
	 * This method raises an IllegalStateException if called while drawing is
	 * active (e.g. in-between a begin-end pair).
	 *
	 * @param fullscreen Whether this canvas should change to fullscreen.
	 * @param desktop 	 Whether to use the current desktop resolution
	 */	 
	public void setFullscreen(boolean value) {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot alter property while drawing active", new IllegalStateException());
			return;
		}
		if (value) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
		}
	}

	/**
	 * Returns the panning factor for the eye value.
	 *
	 * This provides the zoom-in effect at the start of the game.  The eyepan is a
	 * value between 0 and 1.  When it is 1, the eye is locked into the correct place
	 * to start a game.
	 *
	 * @return The eyepan value in [0,1]
	 */
	public float getEyePan() {
		return eyepan;
	}
	
	/**
	 * Returns the font used to display messages.
	 *
	 * @return the font used to display messages.
	 */
	public BitmapFont getFont() {
		return displayFont;
	}
	
	/**
	 * Sets the font used to display messages.
	 *
	 * @param font the font used to display messages.
	 */
	public void setFont(BitmapFont font) {
		displayFont = font;
		displayLayout = (font != null ? new GlyphLayout() : null);
	}
	
	/**
	 * Returns the background texture for this canvas.
	 *
	 * The canvas fills the screen, and everything is drawn on top of the canvas.
	 *
	 * @return the background texture for this canvas.
	 */
	public Texture getBackground() {
		return background;
	}
	
	/**
	 * Sets the background texture for this canvas.
	 *
	 * The canvas fills the screen, and everything is drawn on top of the canvas.
	 *
	 * @param background the background texture for this canvas.
	 */
	public void setBackground(Texture background) {
		this.background = background;
	}

	/**
	 * Sets the panning factor for the eye value.
	 *
	 * This provides the zoom-in effect at the start of the game.  The eyepan is a
	 * value between 0 and 1.  When it is 1, the eye is locked into the correct place
	 * to start a game.
	 *
	 * @param value The eyepan value in [0,1]
	 */
	public void setEyePan(float value) {
		eyepan = value;
	}
	
	/**
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteCam.setToOrtho(false,getWidth(),getHeight());
		spriteBatch.setProjectionMatrix(spriteCam.combined);
	}
	
	/**
	 * Begins a drawing pass with no set camera.
	 *
	 * This method is used only during the loading screen.  You cannot draw
	 * any 3D models after this method.
	 */
	public void begin() {
		// We are drawing
		active = true;
		shading = false;
		
		// Clear the screen and depth buffer
		setDepthState(DepthState.DEFAULT);
		gl20.glClearColor(0, 0, 0, 0);
		gl20.glClearDepthf(1.0f);
		gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Draw the background
		drawBackground();

	}
	
	/**
	 * Begins a drawing pass with the camera focused at postion (x,y)
	 *
	 * If eyepan is not 1, the camera will interpolate between the goal position
	 * and (x,y).  This command will draw the background, no matter what else
	 * is drawn this pass.
	 *
	 * @param x The x-coordinate of the player's ship
	 * @param y The y-coordinate of the player's ship
	 */
	public void begin(float x, float y) {
		// We are drawing
		active = true;
		
		// Clear the screen and depth buffer
		setDepthState(DepthState.DEFAULT);
		gl20.glClearColor(0, 0, 0, 0);
		gl20.glClearDepthf(1.0f);
		gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Draw the background
		drawBackground();

		// Set eye and target positions.
		if (eyepan < 1.0f) {
			tmp0.set(x,y,0);
			tmp1.set(tmp0).scl(INIT_TARGET_PAN);
			target.set(tmp1).interpolate(tmp0,eyepan,SWING_IN);

			tmp0.add(0, NEAR_DIST, -EYE_DIST);
			tmp1.set(tmp0).scl(INIT_EYE_PAN);
			eye.set(tmp1).interpolate(tmp0,eyepan,SWING_IN);
		} else {
			target.set(x, y, 0);
			eye.set(target).add(0, NEAR_DIST, -EYE_DIST);
		}
		
		// Position the camera
		view.setToLookAt(eye,target,UP_REVERSED);
		setToPerspectiveFOV(proj, FOV, (float)getWidth() / (float)getHeight(), NEAR_DIST, FAR_DIST);
		tmpMat.set(view).mulLeft(proj);

		setDepthState(DepthState.DEFAULT);
		setBlendState(BlendState.ALPHA_BLEND);
		setCullState(CullState.CLOCKWISE);

		// Time to start drawing 3D objects
		shading = true;
		program.begin();
		program.setUniformMatrix(SHADER_U_VIEWP, tmpMat);
	}
	
	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
	public void end() {
		if (shading) {
			program.end();
			shading = false;
		}
		active = false;
	}

	/**
	 * Draws the background image to the screen.
	 *
	 * This image will not move, no matter what the camera does.  It is just "space".
	 */
	private void drawBackground() {
		if (background == null) {
			return;
		}
		setDepthState(DepthState.NONE);
		setBlendState(BlendState.OPAQUE);
		setCullState(CullState.COUNTER_CLOCKWISE);
		
		// Only use of spritebatch in game.
		spriteBatch.begin();
		spriteBatch.draw(background, 0, 0, getWidth(), getHeight());
		spriteBatch.end();
	}

	/**
	 * Draws a board tile to the screen.
	 *
	 * @param model The textured mesh object (with color) for the tile
	 * @param x The tile x-coordinate in world coordinates
	 * @param y The tile y-coordinate in world coordinates
	 * @param z The tile z-coordinate in world coordinates
	 * @param angle The tile z-rotation (for falling animation)
	 */
	public void drawTile(TexturedMesh model, float x, float y, float z, float angle) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (!shading) {
			Gdx.app.error("GameCanvas", "Cannot draw after a message is displayed", new IllegalStateException());
			return;
		} else if (this.model != model) {
			this.model = model;
			model.getTexture().bind(0);
		}
		
		// Very primitive culling code
		tmp0.set(target).sub(x, y, 0);
		if(Math.abs(tmp0.x) > CLIP_X || Math.abs(tmp0.y) > CLIP_Y) return;
		
		// World transform components
		world.setToTranslation(x,y,z);
		world.rotateRad(0, 0, 1, angle); // z-rotation
		world.scale(TILE_SIZE, TILE_SIZE, TILE_DEPTH);
		
		// Set world transform
		program.setUniformMatrix(SHADER_U_WORLD, world);
		program.setUniformf(SHADER_U_TINT, model.getColor());
		model.getMesh().render(program, GL20.GL_TRIANGLES);
	}
	
	/**
	 * Draws a ship model to the screen.
	 *
	 * @param model The textured mesh object (with color) for the ship
	 * @param x The ship x-coordinate in world coordinates
	 * @param y The ship y-coordinate in world coordinates
	 * @param z The ship z-coordinate (for falling animation)
	 * @param angle The ship angle for rotation in plane
	 */	 
	public void drawShip(TexturedMesh model, float x, float y, float z, float angle) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (!shading) {
			Gdx.app.error("GameCanvas", "Cannot draw after a message is displayed", new IllegalStateException());
			return;
		} else if (model == null) {
			Gdx.app.error("GameCanvas", "Attempt to draw ship without a model", new IllegalStateException());
			return;
		} else if (this.model != model) {
			this.model = model;
			model.getTexture().bind(0);
		}
		
		// World transform components
		world.setToTranslation(x, y, SHIP_FALL_TRANS + z);
		world.rotateRad(1,0,0,SHIP_FALL_X_SKEW*z);
		world.rotateRad(0,0,1,(float)Math.toRadians(angle) + SHIP_FALL_Z_SKEW*z);
		world.rotateRad(0,1,0,(float)Math.PI);
		world.rotateRad(1.0f,0.0f,0.0f,(float)(Math.PI/2));
		world.scale(SHIP_SIZE,SHIP_SIZE,SHIP_SIZE);

		// Very primitive culling code
		tmp0.set(target).sub(world.getTranslation(tmp1));
		if(Math.abs(tmp0.x) > CLIP_X || Math.abs(tmp0.y) > CLIP_Y) return;
		
		// Set world transform
		program.setUniformMatrix(SHADER_U_WORLD, world);
		program.setUniformf(SHADER_U_TINT, model.getColor());
		model.getMesh().render(program, GL20.GL_TRIANGLES);
	}
	
	/**
	 * Draws the ship exhaust to the screen.
	 *
	 * This method has to be different from drawShip because the blending mode is
	 * different.
	 *
	 * @param model The textured mesh object (with color) for the ship
	 * @param x The ship x-coordinate in world coordinates
	 * @param y The ship y-coordinate in world coordinates
	 * @param z The ship z-coordinate (for falling animation)
	 * @param angle The ship angle for rotation in plane
	 */	 
	public void drawFire(TexturedMesh model, float x, float y, float z, float angle) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (!shading) {
			Gdx.app.error("GameCanvas", "Cannot draw after a message is displayed", new IllegalStateException());
			return;
		} else if (model == null) {
			Gdx.app.error("GameCanvas", "Attempt to draw afterburner without a model", new IllegalStateException());
			return;
		} else if (this.model != model) {
			setDepthState(DepthState.READ);
			setBlendState(BlendState.ADDITIVE);
			setCullState(CullState.CLOCKWISE);
			
			this.model = model;
			model.getTexture().bind(0);
		}
		
		// World transform components
		world.setToTranslation(x, y, SHIP_FALL_TRANS + z);
		world.rotateRad(1,0,0,SHIP_FALL_X_SKEW*z);
		world.rotateRad(0,0,1,(float)Math.toRadians(angle) + SHIP_FALL_Z_SKEW*z);
		world.rotateRad(0,1,0,(float)Math.PI);
		world.rotateRad(1.0f,0.0f,0.0f,(float)(Math.PI/2));
		world.scale(SHIP_SIZE,SHIP_SIZE,SHIP_SIZE);

		// Very primitive culling code
		tmp0.set(target).sub(world.getTranslation(tmp1));
		if(Math.abs(tmp0.x) > CLIP_X || Math.abs(tmp0.y) > CLIP_Y) return;
		
		// Set world transform
		program.setUniformMatrix(SHADER_U_WORLD, world);
		program.setUniformf(SHADER_U_TINT, model.getColor());
		model.getMesh().render(program, GL20.GL_TRIANGLES);
	}
	
	/**
	 * Draws a photon to the screen.
	 *
	 * @param model The textured mesh object (with color) for the photon
	 * @param x  The photon x-coordinate in world coordinates
	 * @param y  The photon y-coordinate in world coordinates
	 * @param vx The photon x-velocity
	 * @param vy The photon y-velocity
	 * @param r  The distance from the photon to its source (for decay)
	 */	 
	public void drawPhoton(TexturedMesh model, float x, float y, float vx, float vy, float r) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (!shading) {
			Gdx.app.error("GameCanvas", "Cannot draw after a message is displayed", new IllegalStateException());
			return;
		} else if (model == null) {
			Gdx.app.error("GameCanvas", "Attempt to draw photon without a model", new IllegalStateException());
			return;
		} else if (this.model != model) {
			setDepthState(DepthState.READ);
			setBlendState(BlendState.ADDITIVE);
			setCullState(CullState.CLOCKWISE);
			
			this.model = model;
			model.getTexture().bind(0);
		}

		tmp2d.set(vx,vy).nor();
		tmpMat.idt();
		tmpMat.val[0] = tmp2d.x;
		tmpMat.val[1] = tmp2d.y;
		tmpMat.val[4] = -tmp2d.y;
		tmpMat.val[5] = tmp2d.x;

		// Compute world transform
		float scale = PHOTON_SIZE+PHOTON_DECAY*r;
		world.setToTranslation(x, y, PHOTON_TRANS);
		world.mul(tmpMat);
		world.scale(scale,scale,scale);

		// Draw a photon instance
		program.setUniformMatrix(SHADER_U_WORLD, world);
		program.setUniformf(SHADER_U_TINT, model.getColor());
		model.getMesh().render(program, GL20.GL_TRIANGLES);
	}
	
	/**
	 * Draws a message at the center of the screen
	 *
	 * The message is an overlay (like the background) and is unaffected by the
	 * camera position.
	 *
	 * Once a message is drawn, the canvas is unable to draw any more 3D objects.
	 * The user must call end().
	 *
	 * @param message The text to draw
	 * @param color   The color to tint the font
	 */
	public void drawMessage(String message, Color color) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (displayFont == null) {
			Gdx.app.error("GameCanvas", "Cannot create a message without a font", new IllegalStateException());
			return;
		} else if (shading) {
			program.end();
			shading = false;
		}
		
		setDepthState(DepthState.NONE);
		setBlendState(BlendState.ALPHA_BLEND);
		setCullState(CullState.COUNTER_CLOCKWISE);
		
		displayLayout.setText(displayFont,message);
		spriteBatch.begin();
		float x = (getWidth()  - displayLayout.width) / 2.0f;
		float y = (getHeight() + displayLayout.height) / 2.0f;
		displayFont.setColor(color);
		displayFont.draw(spriteBatch, displayLayout, x, y);
		spriteBatch.end();
	}

	/**
	 * Draws a two-line message at the center of the screen
	 *
	 * The message is an overlay (like the background) and is unaffected by the
	 * camera position.
	 *
	 * Once a message is drawn, the canvas is unable to draw any more 3D objects.
	 * The user must call end().
	 *
	 * @param mess1 The top text to draw
	 * @param mess2 The bottom text to draw
	 * @param color The color to tint the font
	 */
	public void drawMessage(String mess1, String mess2, Color color) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		} else if (displayFont == null) {
			Gdx.app.error("GameCanvas", "Cannot create a message without a font", new IllegalStateException());
			return;
		} else if (shading) {
			program.end();
			shading = false;
		}
		
		setDepthState(DepthState.NONE);
		setBlendState(BlendState.ALPHA_BLEND);
		setCullState(CullState.COUNTER_CLOCKWISE);
		
		float x, y;
		
		spriteBatch.begin();
		displayFont.setColor(color);

		displayLayout.setText(displayFont,mess1);
		x = (getWidth()  - displayLayout.width) / 2.0f;
		y = displayLayout.height+(getHeight() + displayLayout.height) / 2.0f;
		displayFont.draw(spriteBatch, displayLayout, x, y);
		
		displayLayout.setText(displayFont,mess2);
		x = (getWidth() - displayLayout.width) / 2.0f;
		y = -displayLayout.height+(getHeight() + displayLayout.height) / 2.0f;
		displayFont.draw(spriteBatch, displayLayout, x, y);		
		spriteBatch.end();
	}

	
	/**
	 * Sets the given matrix to a FOV perspective.
	 *
	 * The field of view matrix is computed as follows:
	 *
	 *        /
	 *       /_
	 *      /  \  <-  FOV 
	 * EYE /____|_____
     *
	 * Let ys = cot(fov)
	 * Let xs = ys / aspect
	 * Let a = zfar / (znear - zfar)
	 * The matrix is
	 * | xs  0   0      0     |
	 * | 0   ys  0      0     |
	 * | 0   0   a  znear * a |
	 * | 0   0  -1      0     |
	 *
	 * @param out Non-null matrix to store result
	 * @param fov field of view y-direction in radians from center plane
	 * @param aspect Width / Height
	 * @param znear Near clip distance
	 * @param zfar Far clip distance
	 *
	 * @returns Newly created matrix stored in out
	 */
	private Matrix4 setToPerspectiveFOV(Matrix4 out, float fov, float aspect, float znear, float zfar) {
		float ys = (float)(1.0 / Math.tan(fov));
		float xs = ys / aspect;
		float a  = zfar / (znear - zfar);

		out.val[0 ] = xs;
		out.val[4 ] = 0.0f;
		out.val[8 ] = 0.0f;
		out.val[12] = 0.0f;

		out.val[1 ] = 0.0f;
		out.val[5 ] = ys;
		out.val[9 ] = 0.0f;
		out.val[13] = 0.0f;

		out.val[2 ] = 0.0f;
		out.val[6 ] = 0.0f;
		out.val[10] = a;
		out.val[14] = znear * a;

		out.val[3 ] = 0.0f;
		out.val[7 ] = 0.0f;
		out.val[11] = -1.0f;
		out.val[15] = 0.0f;

		return out;
	}
	
	/**
	 * Sets the mode for blending colors on-screen.
	 *
	 * @param state The blending mode
	 */
	private void setBlendState(BlendState state) {
		int blendMod = 0;
		int blendSrc = 0;
		int blendDst = 0;
		int blendModAlpha = 0;
		int blendSrcAlpha = 0;
		int blendDstAlpha = 0;	
		
		switch (state) {
		case ALPHA_BLEND:
			blendMod = GL20.GL_FUNC_ADD;
			blendSrc = GL20.GL_ONE;
			blendDst = GL20.GL_ONE_MINUS_SRC_ALPHA;
			blendModAlpha = GL20.GL_FUNC_ADD;
			blendSrcAlpha = GL20.GL_ONE;
			blendDstAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;
			break;
		case NO_PREMULT:
			blendMod = GL20.GL_FUNC_ADD;
			blendSrc = GL20.GL_SRC_ALPHA;
			blendDst = GL20.GL_ONE_MINUS_SRC_ALPHA;
			blendModAlpha = GL20.GL_FUNC_ADD;
			blendSrcAlpha = GL20.GL_ONE;
			blendDstAlpha = GL20.GL_ZERO;
			break;
		case ADDITIVE:
			blendMod = GL20.GL_FUNC_ADD;
			blendSrc = GL20.GL_SRC_ALPHA;
			blendDst = GL20.GL_ONE;
			blendModAlpha = GL20.GL_FUNC_ADD;
			blendSrcAlpha = GL20.GL_ONE;
			blendDstAlpha = GL20.GL_ZERO;
			break;
		case OPAQUE:
			blendMod = GL20.GL_FUNC_ADD;
			blendSrc = GL20.GL_ONE;
			blendDst = GL20.GL_ZERO;
			blendModAlpha = GL20.GL_FUNC_ADD;
			blendSrcAlpha = GL20.GL_ONE;
			blendDstAlpha = GL20.GL_ZERO;
			break;
		}
		
		gl20.glBlendEquationSeparate(blendMod, blendModAlpha);
		gl20.glBlendFuncSeparate(blendSrc, blendDst, blendSrcAlpha, blendDstAlpha);
	}
	
	/**
	 * Sets the mode for culling unwanted polygons based on depth.
	 *
	 * @param state The depth mode
	 */
	private void setDepthState(DepthState state) {
		boolean shouldRead  = true;
		boolean shouldWrite = true;
		int depthFunc = 0;
		
		switch (state) {
		case NONE:
			shouldRead  = false;
			shouldWrite = false;
			depthFunc = GL20.GL_ALWAYS;
			break;
		case READ:
			shouldRead  = false;
			shouldWrite = true;
			depthFunc = GL20.GL_LEQUAL;
			break;
		case WRITE:
			shouldRead  = false;
			shouldWrite = true;
			depthFunc = GL20.GL_ALWAYS;
			break;
		case DEFAULT:
			shouldRead  = true;
			shouldWrite = true;
			depthFunc = GL20.GL_LEQUAL;
			break;
		}
		
        if (shouldRead || shouldWrite) {
        	gl20.glEnable(GL20.GL_DEPTH_TEST);
        	gl20.glDepthMask(shouldWrite);
        	gl20.glDepthFunc(depthFunc);
        } else {
        	gl20.glDisable(GL20.GL_DEPTH_TEST);
        }
	}
	
	/**
	 * Sets the mode for culling unwanted polygons based on facing.
	 *
	 * @param state The culling mode
	 */
	private void setCullState(CullState state) {
		boolean cull = true;
    	int mode = 0;
    	int face = 0;
    	
    	switch (state) {
    	case NONE:
            cull = false;
            mode = GL20.GL_BACK;
            face = GL20.GL_CCW;
			break;
    	case CLOCKWISE:
            cull = true;
            mode = GL20.GL_BACK;
            face = GL20.GL_CCW;
			break;
    	case COUNTER_CLOCKWISE:
            cull = true;
            mode = GL20.GL_BACK;
            face = GL20.GL_CW;
			break;
    	}
        if (cull) {
        	gl20.glEnable(GL20.GL_CULL_FACE);
        	gl20.glFrontFace(face);
        	gl20.glCullFace(mode);
        } else {
        	gl20.glDisable(GL20.GL_CULL_FACE);
        }

	}
		
	/**
	 * Enumeration of supported blend states.
	 *
	 * For reasons of convenience, we do not allow user-defined blend functions.
	 * 99% of the time, we find that the following blend modes are sufficient
	 * (particularly with 2D games).
	 */
	private static enum BlendState {
		/** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
		ALPHA_BLEND,
		/** Alpha blending on, assuming the colors have no pre-multipled alpha */
		NO_PREMULT,
		/** Color values are added together, causing a white-out effect */
		ADDITIVE,
		/** Color values are draw on top of one another with no transparency support */
		OPAQUE
	}

	/**
	 * Enumeration of supported depth states.
	 *
	 * For reasons of convenience, we do not allow user-defined depth functions.
	 * 99% of the time, we find that the following depth modes are sufficient
	 * (particularly with 2D games).
	 */
	private static enum DepthState {
		/** Do not enable depth masking at all. */
		NONE,
		/** Read from the depth value, but do not write to it */
		READ,
		/** Write to the depth value, but do not read from it */
		WRITE,
		/** Read and write to the depth value, providing normal masking */
		DEFAULT
	}

	/**
	 * Enumeration of supported culling states.
	 *
	 * For reasons of convenience, we do not allow user-defined culling operations.
	 * 99% of the time, we find that the following culling modes are sufficient
	 * (particularly with 2D games).
	 */
	private static enum CullState {
		/** Do not remove the backsides of any polygons; show both sides */
		NONE,
		/** Remove polygon backsides, using clockwise motion to define the front */
		CLOCKWISE,
		/** Remove polygon backsides, using counter-clockwise motion to define the front */
		COUNTER_CLOCKWISE
	}

}