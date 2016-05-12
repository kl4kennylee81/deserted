/*
 * GameCanvas.cs
 *
 * To properly follow the model-view-controller separation, we should not have
 * any specific drawing code in GameMode. All of that code goes here.  As
 * with GameEngine, this is a class that you are going to want to copy for
 * your own projects.
 *
 * An important part of this canvas design is that it is loosely coupled with
 * the model classes. All of the drawing methods are abstracted enough that
 * it does not require knowledge of the interfaces of the model classes.  This
 * important, as the model classes are likely to change often.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.ailab;

import javax.swing.Action;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.Character;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;

/**
 * Primary view class for the game, abstracting the basic graphics calls.
 * 
 *  This version of GameCanvas only supports (rectangular) Sprite drawing.
 *  support for polygonal textures and drawing primitives will be present
 *  in future labs.
 */
public class GameCanvas {
	/** Drawing context to handle textures as sprites */
	public SpriteBatch spriteBatch;
	/** Canvas background image. */
	private Texture background;
	/** Font object for displaying images */
	private BitmapFont displayFont;
	
	private GlyphLayout layout;
	/** White texture */
	private Texture white;
	
	/** white texture region that can be transformed **/
	private TextureRegion whiteRegion;
	
	/** Value to cache window width (if we are currently full screen) */
	int width;
	/** Value to cache window height (if we are currently full screen) */
	int height;
	
	/** Track whether or not we are active (for error checking) */
	private boolean active;
	
	/** The current color blending mode */
	private BlendState blend;
	
	// CACHE OBJECTS
	/** Affine cache for current sprite to draw */
	private Affine2 local;
	/** Affine cache for all sprites this drawing pass */
	private Affine2 global;
	/** Cache object to unify everything under a master draw method */
	private TextureRegion holder;
	
	/** Orthographic camera for the SpriteBatch layer */
	private OrthographicCamera spriteCam;
	
	/**to draw lines for tutorial */
	ShapeRenderer shapeRenderer;
	
	/** Arrows */
	Texture upArrow = new Texture("models/upArrow.png");
	Texture downArrow = new Texture("models/downArrow.png");
	Texture leftArrow = new Texture("models/leftArrow.png");
	Texture whiteCircle = new Texture("images/white_circle.png");
	Texture roundedRect = new Texture("images/rounded_rectangle.png");
	
	/** Tutorial font */
	BitmapFont tutorialFont;
	/**
	 * Creates a new GameCanvas determined by the application configuration.
	 * 
	 * Width, height, and fullscreen are taken from the LWGJApplicationConfig
	 * object used to start the application.
	 */
	public GameCanvas() {
		this.width  = Gdx.graphics.getWidth();
		this.height = Gdx.graphics.getHeight();

		active = false;
		spriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		
		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		
		layout = new GlyphLayout();
		
		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Affine2();
		spriteCam = new OrthographicCamera(getWidth(),getHeight());
		spriteCam.setToOrtho(false);
		spriteBatch.setProjectionMatrix(spriteCam.combined);
		shapeRenderer.setProjectionMatrix(spriteCam.combined);
	}
	
	/**
	 * Creates a new GameCanvas of the given size.
	 *
	 * The canvas will be displayed in a window, not fullscreen.
	 *
	 * @param width The width of the canvas window
	 * @param height The height of the canvas window
	 */
	public GameCanvas(int width, int height) {
		this(width,height,false);
	}

	/**
	 * Creates a new GameCanvas with the giving parameters.
	 *
	 * This constructor will completely override the settings in the
	 * LWGJApplicationConfig object used to start the application.
	 *
	 * @param width The width of the canvas window
	 * @param height The height of the canvas window
	 * @param fullscreen Whether or not the window should be full screen.
	 */	 
	protected GameCanvas(int width, int height, boolean fullscreen) {
		// Create a new graphics manager.
		this.width  = width;
		this.height = height;
		if (fullscreen) {
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		} else {
			Gdx.graphics.setWindowedMode(width, height);
			
		}
		
		// Continue as normal
		active = false;
		spriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		// Set the projection matrix (for proper scaling)
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		
		layout = new GlyphLayout();
		
		// Initialize the cache objects
		holder = new TextureRegion();
		local  = new Affine2();
		global = new Affine2();
	}
		
    /**
     * Eliminate any resources that should be garbage collected manually.
     */
    public void dispose() {
		if (active) {
			Gdx.app.error("GameCanvas", "Cannot dispose while drawing active", new IllegalStateException());
			return;
		}
		spriteBatch.dispose();
		shapeRenderer.dispose();
    	spriteBatch = null;
    	shapeRenderer = null;
    	global = null;
    	local  = null;
    	holder = null;
    }
    
    /**
	 * Sets the font used to display messages.
	 *
	 * @param font the font used to display messages.
	 */
	public void setFont(BitmapFont font) {
		displayFont = font;
	}
	
	public BitmapFont getFont() {
		return displayFont;
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
	
	public void setWhite(Texture white) {
		this.white = white;
		this.whiteRegion = new TextureRegion(white);
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
	 * Resets the SpriteBatch camera when this canvas is resized.
	 *
	 * If you do not call this when the window is resized, you will get
	 * weird scaling issues.
	 */
	 public void resize() {
		// Resizing screws up the spriteBatch projection matrix
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, getWidth(), getHeight());
		spriteCam.setToOrtho(false,getWidth(),getHeight());
		spriteBatch.setProjectionMatrix(spriteCam.combined);
		shapeRenderer.setProjectionMatrix(spriteCam.combined);
		this.width = Gdx.graphics.getWidth();
		this.height = Gdx.graphics.getHeight();
	}
	
	/**
	 * Returns the current color blending state for this canvas.
	 *
	 * Textures draw to this canvas will be composited according
	 * to the rules of this blend state.
	 *
	 * @return the current color blending state for this canvas
	 */
	public BlendState getBlendState() {
		return blend;
	}
	
	/**
	 * Sets the color blending state for this canvas.
	 *
	 * Any texture draw subsequent to this call will use the rules of this blend 
	 * state to composite with other textures.  Unlike the other setters, if it is 
	 * perfectly safe to use this setter while  drawing is active (e.g. in-between 
	 * a begin-end pair).  
	 *
	 * @param state the color blending rule
	 */
	public void setBlendState(BlendState state) {
		if (state == blend) {
			return;
		}
		switch (state) {
		case NO_PREMULT:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ALPHA_BLEND:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ONE_MINUS_SRC_ALPHA);
			break;
		case ADDITIVE:
			spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA,GL20.GL_ONE);
			break;
		case OPAQUE:
			spriteBatch.setBlendFunction(GL20.GL_ONE,GL20.GL_ZERO);
			break;
		}
		blend = state;
	}

	/**
	 * Start and active drawing sequence with the identity transform.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 */
    public void begin() {
    	spriteBatch.begin();
    	shapeRenderer.begin(ShapeType.Filled);
    	active = true;
    	drawBackground();
    }

	/**
	 * Start and active drawing sequence with the given transform.
	 *
	 * All textures drawn will have the given transform applied before any
	 * any other subsequent transforms.
	 *
	 * Nothing is flushed to the graphics card until the method end() is called.
	 *
	 * @param transform The drawing transform.
	 */
    public void begin(Affine2 transform) {
    	global.set(transform);
    	spriteBatch.begin();
    	shapeRenderer.begin(ShapeType.Line);
    	drawBackground();
    }

	/**
	 * Ends a drawing sequence, flushing textures to the graphics card.
	 */
    public void end() {
    	spriteBatch.end();
    	shapeRenderer.end();
    	active = false;
    }

    public void drawBackground() {
    	drawOverlay(background, Color.WHITE, true);
    }
    
	/**
	 * Draws the texture at the given position.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(image,Color.WHITE,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture at the given position.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(Texture image, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(image,tint,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture with the given transformations
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the screen location
	 * @param y 	The y-coordinate of the screen location
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(Texture image, Color tint, float ox, float oy, 
					float x, float y, float angle, float sx, float sy) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		holder.setRegion(image);
		draw(holder,tint,ox,oy,x,y,angle,sx,sy);
	}
	
	/**
	 * Draws the texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method	
		draw(region,Color.WHITE,0,0,x,y,0,1.0f,1.0f);
	}

	/**
	 * Draws the tinted texture region (filmstrip) at the given position.
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * Unless otherwise transformed by the global transform (@see begin(Affine2)),
	 * the texture will be unscaled.  The bottom left of the texture will be positioned
	 * at the given coordinates.
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void draw(TextureRegion region, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		// Call the master drawing method
		draw(region,tint,0,0,x,y,0,1.0f,1.0f);
	}
	
	/**
	 * Draws the tinted texture region (filmstrip) with the given transformations
	 *
	 * THIS IS THE MASTER DRAW METHOD (Read this for exercise 4)
	 *
	 * A texture region is a single texture file that can hold one or more textures.
	 * It is used for filmstrip animation.
	 *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
	 * The transformations are BEFORE after the global transform (@see begin(Affine2)).  
	 * As a result, the specified texture origin will be applied to all transforms 
	 * (both the local and global).
	 *
	 * The local transformations in this method are applied in the following order: 
	 * scaling, then rotation, then translation (e.g. placement at (sx,sy)).
	 *
	 * @param image The texture to draw
	 * @param tint  The color tint
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */	
	public void draw(TextureRegion region, Color tint, float ox, float oy, 
					 float x, float y, float angle, float sx, float sy) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		for (float tx = x - this.width; tx < this.width + ox/2  ; tx += this.width){
			for (float ty = y - this.height; ty < this.height + oy/2 ; ty += this.height){
				if (tx + ox/2 >= 0 && ty + oy/2 >= 0){
					computeTransform(ox,oy,tx,ty,angle,sx,sy);
					spriteBatch.setColor(tint);
					spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
				}
			}
		}
	}
	
	/**
	 * Compute the affine transform (and store it in local) for this image.
	 * 
	 * This helper is meant to simplify all of the math in the above draw method
	 * so that you do not need to worry about it when working on Exercise 4.
	 *
	 * @param ox 	The x-coordinate of texture origin (in pixels)
	 * @param oy 	The y-coordinate of texture origin (in pixels)
	 * @param x 	The x-coordinate of the texture origin
	 * @param y 	The y-coordinate of the texture origin
	 * @param angle The rotation angle (in degrees) about the origin.
	 * @param sx 	The x-axis scaling factor
	 * @param sy 	The y-axis scaling factor
	 */
	private void computeTransform(float ox, float oy, float x, float y, float angle, float sx, float sy) {
		local.set(global);
		local.translate(x,y);
		local.rotate(angle);
		local.scale(sx,sy);
		local.translate(-ox,-oy);
	}

	/**
     * Draw an unscaled overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * @param image Texture to draw as an overlay
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
    public void drawOverlay(Texture image, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,x,y);
    }
    
	/**
     * Draw an unscaled overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
	 *
     * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param x 	The x-coordinate of the bottom left corner
	 * @param y 	The y-coordinate of the bottom left corner
	 */
	public void drawOverlay(Texture image, Color tint, float x, float y) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, x, y);
    }

	/**
     * Draw an stretched overlay image.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
     * @param image Texture to draw as an overlay
	 * @param fill	Whether to stretch the image to fill the screen
	 */
    public void drawOverlay(Texture image, boolean fill) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		drawOverlay(image,Color.WHITE,fill);
    }
    
	/**
     * Draw an stretched overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
     *
     * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param fill	Whether to stretch the image to fill the screen
	 */
	public void drawOverlay(Texture image, Color tint, boolean fill) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		float w, h;
		if (fill) {
			w = getWidth();
			h = getHeight();
		} else {
			w = image.getWidth();
			h = image.getHeight();
		}
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, 0, 0, w, h);
    }
	
	/**
     * Draw an stretched overlay image tinted by the given color.
     *
     * An overlay image is one that is not scaled by the global transform
     * This is ideal for backgrounds, foregrounds and uniform HUDs that do not
     * track the camera.
     * 
     * The image will be drawn starting at the bottom right corner, and will
     * be stretched to fill the whole screen if appropriate.
     *
	 * The texture colors will be multiplied by the given color.  This will turn
	 * any white into the given color.  Other colors will be similarly affected.
     *
     * Rotates image by the given rotation factor.
     *
     * @param image Texture to draw as an overlay
	 * @param tint  The color tint
	 * @param fill	Whether to stretch the image to fill the screen
	 * @param rot   Rotation factor of image
	 */
	public void drawOverlay(Texture image, Color tint, boolean fill, float rot) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		float w, h;
		int iw, ih;
		w = getWidth();
		h = getHeight();
		iw = image.getWidth();
		ih = image.getHeight();
		spriteBatch.setColor(tint);
		spriteBatch.draw(image, -w/2, -h/2, w, h, w*2, h*2, 1.1f, 1.1f, rot, 0, 0, iw, ih, false, false);
    }
	
	public void drawMessage(String msg){
		displayFont.draw(spriteBatch, msg, 400,400);
	}
	
	public void draw(TextureRegion region, Color tint, float x, float y, float width, float height) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(region, x,  y, width, height);
	}
	
	public void draw(Texture image, Color tint, float x, float y, float width, float height) {
		if (!active) {
			Gdx.app.error("GameCanvas", "Cannot draw without active begin()", new IllegalStateException());
			return;
		}
		
		// Unlike Lab 1, we can shortcut without a master drawing method
    	spriteBatch.setColor(tint);
		spriteBatch.draw(image, x,  y, width, height);
	}
	
	public void drawMessage(String msg1, String msg2){
		displayFont.draw(spriteBatch, msg1, 400,300);
		displayFont.draw(spriteBatch, msg2, 400,500);
	}
	
	public void drawActionBar(float x, float y, float ratio){
		spriteBatch.setColor(Color.RED);
		spriteBatch.draw(white, x, y, 600, 20);
		spriteBatch.setColor(Color.GREEN);
		spriteBatch.draw(white, x, y, 600*ratio, 20);
	}
	
	// TODO this is hacky
	public void setShearBoard(float x,float y,float shearX,float shearY){
		local.setToTranslation(x, y);
		local.shear(shearX,shearY);
		float xTranslate = y*shearX;
		local.translate(xTranslate,0);
		
//		float translateBack = -this.getWidth()*0.055f;
//		local.translate(translateBack,0);

	}
	
	public void drawTile(float x, float y, TextureRegion mesh, float width, float height, Color tint){	
		setShearBoard(x,y,Constants.TILE_SHEAR,0);
		spriteBatch.setColor(tint);
		spriteBatch.draw(mesh,width,height,local);
	}
	
	public void drawOption(float sx, float sy, Texture button,float width, float height, 
			Color tint, String text){
		spriteBatch.setColor(tint);
		spriteBatch.draw(button,sx,sy,width,height);
			
		displayFont.draw(spriteBatch, text, sx + width/2,sy + height/2);
		//make positions in Option just multipliers of canvas.getWidth and Height for now
		//TODO: we should get rid of any hardcoding going on with this what exactly are you trying
		// to position it because it has to be extensible not just for one menu type.
	}	
	
	public void drawScreen(float sx, float sy, Texture screen,float x_size, float y_size, 
			Color tint){
		spriteBatch.setColor(tint);
		spriteBatch.draw(screen,sx,sy,x_size,y_size);
	}
	
	public void drawTexture(Texture texture, float x, float y, float width, float height, Color color){
		spriteBatch.setColor(color);
		spriteBatch.draw(texture, x, y, width, height);
	}
	
	public void drawBoardRim(TextureRegion region,float x,float y,float sx,float sy,float shearX,float shearY,Color color){
		spriteBatch.setColor(color);
		computeTransform(0,0,x,y,0,sx,sy);
		local.shear(shearX,shearY);
		spriteBatch.draw(region,region.getRegionWidth(),region.getRegionHeight(),local);
	}
	
	
	public void drawCharacter(Texture texture, float x, float y, Color color, boolean leftside, float scale){
		spriteBatch.setColor(color);
		int tWidth = texture.getWidth();
		int tHeight = texture.getHeight();
		spriteBatch.draw(texture,x,y,0,0,tWidth,tHeight,scale,scale,0,0,0,tWidth,tHeight, leftside, false);
	}
	
	public void drawCharacter(TextureRegion texture, float x, float y, Color color, boolean leftside,float scale){
		spriteBatch.setColor(color);
		if (leftside){
			texture.flip(true, false);
		}
		int tWidth = texture.getRegionWidth();
		int tHeight = texture.getRegionHeight();
		spriteBatch.draw(texture, x, y, 0, 0, tWidth, tHeight, scale, scale, 0);
		if (leftside){
			texture.flip(true, false);
		}
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
	public void drawShip(Texture model, float x, float y, Color color, int angle) {
		spriteBatch.setColor(color);
		spriteBatch.draw(model, x, y, 50, 50, 100, 100, 1, 1, angle, 0, 0, model.getWidth(),model.getHeight(),false,false);
	}
	
	//Generalize all these to a draw box function
	
	public void drawHealthBars(float x, float y, float ratio){
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.draw(white, x, y, 100,10);
		spriteBatch.setColor(Color.GREEN);
		spriteBatch.draw(white, x, y, 100*ratio,10);
	}
	
	public void drawPointer(float x, float y, Color color){
		spriteBatch.setColor(color);
		spriteBatch.draw(white,x,y,10,10);
	}
	
	public void drawBox(float x, float y, float width, float height, Color color){
		spriteBatch.setColor(color);
		spriteBatch.draw(white,x,y,width,height);
	}
	
	/** we might change the guide to reflect the game properly **/
	public void drawTileArrow(float x,float y,float width,float height,Color color){
		this.setShearBoard(x,y,Constants.TILE_SHEAR,0);
		spriteBatch.setColor(color);
		spriteBatch.draw(whiteRegion,width,height,local);
	}
	
	public void drawUpArrow(float x1, float y1, float x2, float y2, Color color){
		shapeRenderer.setColor(color);
		shapeRenderer.rectLine(x1, y1, x2, y2, 7f);
		shapeRenderer.rectLine(x2-25, y2-16, x2-3, y2-3, 6f);
		shapeRenderer.rectLine(x2+11, y2-21, x2-2, y2-2, 6f);
	}
	
	public void drawUpArrow(float x, float y, Color color) {
		spriteBatch.setColor(color);
		spriteBatch.draw(upArrow, x-100, y-100, 135, 135);
	}
	
	public void drawDownArrow(float x, float y, Color color){
		spriteBatch.setColor(color);
		spriteBatch.draw(downArrow, x-37, y+10, 135, 135);
	}
	
	public void drawCharArrow(Texture screen, float x, float y, float x_width, float y_width, Color color, GridBoard board){
		TextureRegion arrowTexture = new TextureRegion(screen,(int)x,(int)y,(int)x_width,(int)y_width);
		float charScale = Character.getCharScale(this, arrowTexture, board);
		spriteBatch.setColor(color);
		spriteBatch.draw(downArrow, (int)(x), (int)(y + y_width), 135, 135);
	}
	
	public void drawDownTextArrow(float x, float y, Color color, String text){
		spriteBatch.setColor(color);
		spriteBatch.draw(downArrow, x-37, y+10, 75, 75);
		displayFont.getData().setScale(1);
		displayFont.setColor(color);
		displayFont.draw(spriteBatch, text, x+25, y+70, getWidth()-(x+60), Align.left, true);
		
	}
	
	public void drawLeftArrow(float x, float y, Color color){
		spriteBatch.setColor(color);
		spriteBatch.draw(leftArrow, x-35, y-80, 135, 135);
	}
	
	public GlyphLayout drawBoardWrapText(String msg, float x, float y, Color color) {
		displayFont.getData().setScale(1);
		displayFont.setColor(color);
		float width = (GridBoard.BOARD_OFFSET_X - GridBoard.EXTRA_OFFSET)*getWidth();
		GlyphLayout g = displayFont.draw(spriteBatch, msg, x,y, width, Align.left, true);
		return g;
	}
	
	
	public GlyphLayout drawText(String msg, float x, float y, Color color) {
		displayFont.getData().setScale(1);
		displayFont.setColor(color);
		GlyphLayout g = displayFont.draw(spriteBatch, msg, x,y);
		return g;
	}
	
	public GlyphLayout drawTutorialText(String msg, Color color, int alignment) {
		BitmapFont currFont = getFont();
		displayFont.getData().setScale(1);
		if (tutorialFont != null){
			setFont(tutorialFont);
		}
		if (color != null){
			displayFont.setColor(color);
		}
		float width = ((float)getWidth())/1.3f;
		float x = getWidth()/2-width/2;
		float y = ((float)getHeight())/2f;
//		GlyphLayout g = displayFont.draw(spriteBatch, msg, x,y, width, alignment, true);
		GlyphLayout g = displayFont.draw(spriteBatch, msg, x,y, width, Align.center, true);
		setFont(currFont);
		return g;
	}
	
	public void drawCenteredText(String msg, float x, float y, Color color) {
		displayFont.getData().setScale(1);
		layout.setText(displayFont, msg);
		float width = layout.width;
		displayFont.setColor(color);
		displayFont.draw(spriteBatch, msg, x-width/2, y);
	}
	
	public void drawCenteredText(String msg, float x, float y, Color color, float scale) {
		displayFont.getData().setScale(scale);
		layout.setText(displayFont, msg);
		float width = layout.width;
		displayFont.setColor(color);
		displayFont.draw(spriteBatch, msg, x-width/2, y);
	}
	
	public void drawCenteredTexture(Texture texture, float x, float y, float width, float height, Color color){
		this.drawTexture(texture, x-width/2, y-height/2, width, height, color);
	}
	
	public void drawCircle(float x, float y, float diam, Color color){
		this.drawTexture(whiteCircle, x, y, diam, diam, color);
	}
	
	public void drawHighlightCharacter(Texture texture, float x, float y, Color color, float scale){
		spriteBatch.setColor(color.r, color.g, color.b, 0.8f);
		float width = texture.getWidth() * scale;
		float height = texture.getHeight() * scale;
		width += 34;
		spriteBatch.draw(whiteCircle, x - 17, y - 13, width, height);
	}
	
	public void drawHighlightToken(Texture texture, float x, float y, float width, float height, Color color){
		spriteBatch.setColor(color.r, color.g, color.b, 0.8f);
		spriteBatch.draw(whiteCircle, x-15, y-15,width + 30, height+30);
	}
	
	public void drawBarHighlight(float x, float y, float width, float height, Color color){
		spriteBatch.setColor(color.r, color.g, color.b, 0.8f);
		spriteBatch.draw(roundedRect, x-20, y-20,width + 40, height+40);
	}

	/**
	 * Enumeration of supported BlendStates.
	 *
	 * For reasons of convenience, we do not allow user-defined blend functions.
	 * 99% of the time, we find that the following blend modes are sufficient
	 * (particularly with 2D games).
	 */
	public enum BlendState {
		/** Alpha blending on, assuming the colors have pre-multipled alpha (DEFAULT) */
		ALPHA_BLEND,
		/** Alpha blending on, assuming the colors have no pre-multipled alpha */
		NO_PREMULT,
		/** Color values are added together, causing a white-out effect */
		ADDITIVE,
		/** Color values are draw on top of one another with no transparency support */
		OPAQUE
	}

	public void drawWarningText(String warning, boolean green) {
		displayFont.getData().setScale(2);
		displayFont.setColor(green? Color.GREEN : Color.SCARLET);
		float width = ((float)getWidth())/2f;
		float x = getWidth()/2-width/2;
		float y = ((float)getHeight())/2f;
		displayFont.draw(spriteBatch, warning, x,y, width, Align.center, true);
	}

	public void setTutorialFont(BitmapFont font) {
		tutorialFont = font;
	}
	
}
//TODO: make draw message take positions rather than hardcoding
//TODO: in the long run use GlyphLabels to put text in 