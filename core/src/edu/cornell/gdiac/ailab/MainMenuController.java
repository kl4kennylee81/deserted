package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import edu.cornell.gdiac.ailab.Action.Pattern;

public class MainMenuController {
	/** Current target selection */
	int selected;
	public int gameNo;
	private boolean isDone;
	private GameCanvas canvas;
	/** Background image for the canvas */
	private static final String BCKGD_TEXTURE = "images/bg.png";
	/** File storing the texture for an option tile */
	private static final String OPTION_TEXTURE = "models/Tile.png";
	private static final String WHITE_BOX = "images/white.png";
	/** The message font to use */
	private static final String FONT_FILE  = "fonts/Amyn.ttf";
	/** The size of the messages */
	private static final int FONT_SIZE = 70;
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
		default_options[0] = new Option(canvas.getWidth()/4-150,3*canvas.getHeight()/4-150,300,150,"     EASY \n  Or Press 'E'",OPTION_TEXTURE,0);
		default_options[1] = new Option(3*canvas.getWidth()/4-150,3*canvas.getHeight()/4-150,300,150,"       PvP \n  Or Press 'P'",OPTION_TEXTURE,1);
		default_options[2] = new Option(canvas.getWidth()/4-150,canvas.getHeight()/4-50,300,150,"   MEDIUM \n  Or Press 'M'",OPTION_TEXTURE,2);
		default_options[3] = new Option(3*canvas.getWidth()/4-150,canvas.getHeight()/4-50,300,150,"   HARD \n Or Press 'H'",OPTION_TEXTURE,3);
//		default_options[0] = new Option(canvas.getWidth()/2-150,3*canvas.getHeight()/4+50,280,130,"     EASY \n  Or Press 'E'",OPTION_TEXTURE,0);
//		default_options[1] = new Option(canvas.getWidth()/2-150,canvas.getHeight()/2+50,280,130,"       PvP \n  Or Press 'P'",OPTION_TEXTURE,1);
//		default_options[2] = new Option(canvas.getWidth()/2-150,canvas.getHeight()/4+50,280,130,"   MEDIUM \n  Or Press 'M'",OPTION_TEXTURE,2);
//		default_options[3] = new Option(canvas.getWidth()/2-150,50,280,130,"   HARD \n Or Press 'H'",OPTION_TEXTURE,3);
		return default_options;
	}

	public void drawMenu() {
		initializeCanvas(BCKGD_TEXTURE);
		mainMenu.draw(canvas);
	}
	
	public void update(){
		updateSelection();
		drawMenu();
		if (InputController.pressedE()){
			gameNo = 0;
			isDone = true;
		} else if (InputController.pressedM()){
			gameNo = 1;
			isDone = true;
		} else if (InputController.pressedH()){
			gameNo = 2;
			isDone = true;
		} else if (InputController.pressedP()){
			gameNo = 3;
			isDone = true;
		}
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
		
		if (manager.isLoaded(FONT_FILE)) {
			canvas.setFont(manager.get(FONT_FILE,BitmapFont.class));
		}
    }
    
    /**
	 * Update when an action is not targeting yet
	 */
	private void updateSelection(){
		if (InputController.pressedA() || InputController.pressedEnter()){
			gameNo = mainMenu.selectedOption;
			isDone = true;
		}  else if ((InputController.pressedUp() && !InputController.pressedDown()) || 
				(InputController.pressedDown() && !InputController.pressedUp())){
			//Actions go from up down, so we need to flip
			switch(mainMenu.selectedOption){
			case 0: mainMenu.selectOption(2); break;
			case 1: mainMenu.selectOption(3); break;
			case 2: mainMenu.selectOption(0); break;
			case 3: mainMenu.selectOption(1); break;
			}
		}  else if ((InputController.pressedLeft() && !InputController.pressedRight()) || 
				(InputController.pressedRight() && !InputController.pressedLeft())){
			switch(mainMenu.selectedOption){
			case 0: mainMenu.selectOption(1); break;
			case 1: mainMenu.selectOption(0); break;
			case 2: mainMenu.selectOption(3); break;
			case 3: mainMenu.selectOption(2); break;
			}
		}
	}
	
	public void resetMenu(){
		isDone = false;
	}

}
