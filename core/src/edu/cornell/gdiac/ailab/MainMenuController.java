package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuController {
	/** Current target selection */
	int selected;
	public int gameNo;
	private boolean isDone;
	private GameCanvas canvas;
	/** Background image for the menu */
	private static final String MENU_BCKGD_TEXTURE = "images/menubg.png";
	/** File storing the texture for an option tile */
	private static final String OPTION_TEXTURE = "models/Tile.png";
	private static final String WHITE_BOX = "images/white.png";
	/** The message font to use */
	private static final String FONT_FILE  = "fonts/Milonga-Regular.ttf";
	private AssetManager manager;
	private MainMenu mainMenu;
	
	public MainMenuController(GameCanvas canvas, AssetManager manager){
		this.canvas = canvas;
		this.manager = manager;
		Option[] default_options = makeDefaultOptions();
		mainMenu = new MainMenu(default_options);
	}
	
	private Option[] makeDefaultOptions() {
		Option [] default_options = new Option[1];
		default_options[0] = new Option(canvas.getWidth()/2,50,270,110,"     EASY \n  Or Press 'E'",OPTION_TEXTURE,0);
		System.out.println(canvas.getWidth()/2);
//		default_options[1] = new Option(canvas.getWidth()/2-140,180,270,110,"   MEDIUM \n  Or Press 'M'",OPTION_TEXTURE,1);
//		default_options[2] = new Option(canvas.getWidth()/2-140,310,270,110,"   HARD \n Or Press 'H'",OPTION_TEXTURE,2);
//		default_options[3] = new Option(canvas.getWidth()/2-140,440,270,110,"       PvP \n  Or Press 'P'",OPTION_TEXTURE,3);
		return default_options;
		//make a method that sizes and positions them according to the number of options
		//resize isn't affecting this
	}
	
	private Option[] spaceOptions(int numOptions){
		return null;
	}
	
	private Option[] spaceOptions(Option[] options){
		return null;
	}

	public void drawMenu() {
		initializeCanvas(MENU_BCKGD_TEXTURE);
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
		//This method shouldn't be in two places as it currently is, only one
    }
    
    /**
	 * Update when an action is not targeting yet
	 */
	private void updateSelection(){
		if (InputController.pressedA() || InputController.pressedEnter()){
			System.out.println("Current option is " + mainMenu.getOptions()[mainMenu.selectedOption].text);
			gameNo = mainMenu.selectedOption;
			isDone = true;
		}  else if ((InputController.pressedUp() && !InputController.pressedDown())){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	         int newSelection = mainMenu.selectedOption+1;
	         int length = mainMenu.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
			mainMenu.selectOption(toSelect);
		}   else if ((InputController.pressedDown() && !InputController.pressedUp())){
			//Actions go from up down, so we need to flip
			int newSelection = mainMenu.selectedOption-1;
	        int length = mainMenu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        mainMenu.selectOption(toSelect);
		}
	}
	
	public void resetMenu(){
		isDone = false;
	}

}
