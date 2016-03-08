package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuController {
	/** Current target selection */
	int selected;
	public int gameNo;
	private boolean isDone;
	private GameCanvas canvas;
	/** Background image for the canvas */
	private static final String BCKGD_TEXTURE = "images/bg.png";
	/** File storing the texture for an option tile */
	private static final String OPTION_TEXTURE = "images/white.png";
	private static final String WHITE_BOX = "images/white.png";
	private AssetManager manager;
	private MainMenu mainMenu;
	
	public MainMenuController(GameCanvas canvas, AssetManager manager){
		this.canvas = canvas;
		this.manager = manager;
		Option[] default_options = makeDefaultOptions();
		mainMenu = new MainMenu(default_options);
	}
	
	private Option[] makeDefaultOptions() {
		// TODO Auto-generated method stub
		Option [] default_options = new Option[4];
		
		default_options[0] = new Option(canvas.getWidth()/4,canvas.getHeight()/4,100,200,"HARD",OPTION_TEXTURE);
		default_options[1] = new Option(canvas.getWidth()/4,3*canvas.getHeight()/4,100,200,"EASY",OPTION_TEXTURE);
		default_options[2] = new Option(3*canvas.getWidth()/4,canvas.getHeight()/4,100,200,"MEDIUM",OPTION_TEXTURE);
		default_options[3] = new Option(3*canvas.getWidth()/4,3*canvas.getHeight()/4,100,200,"PvP",OPTION_TEXTURE);
		return default_options;
	}

	public void drawMenu() {
		initializeCanvas(BCKGD_TEXTURE);
		mainMenu.draw(canvas);
	}
	
	public void update(){
		drawMenu();
//		if (InputController.pressedE()){
//		startGame(0);
//	} else if (InputController.pressedM()){
//		startGame(1);
//	} else if (InputController.pressedH()){
//		startGame(2);
//	} else if (InputController.pressedP()){
//		startGame(3);
//	}
	}
	
	public boolean isDone(){
		return isDone;
	}
		
	/**
	 * Loads the assets used by the game canvas.
	 *
	 * This method loads the background and font for the canvas.  As these are
	 * needed to draw anything at all, we block until the assets have finished
	 * loading.
	 */
    private void initializeCanvas(String texture_msg) { 
    	canvas.setFont(new BitmapFont());
		Texture texture = manager.get(texture_msg, Texture.class);
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		canvas.setBackground(texture);
		canvas.setWhite(manager.get(WHITE_BOX, Texture.class));
    }

}
