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
	
	private AssetManager manager;
	private MainMenu mainMenu;
	private MouseOverController mouseOverController;
	
	
	public MainMenuController(GameCanvas canvas, AssetManager manager, MouseOverController mouseOverController){
		this.canvas = canvas;
		this.manager = manager;
		Option[] default_options = makeDefaultOptions();
		mainMenu = new MainMenu(default_options);
		if (default_options.length > 0){
			mainMenu.selectOption(0);
		}
		this.mouseOverController = mouseOverController; 
	}
	
	private Option[] makeDefaultOptions() {
		// we will rearrange when i merge to master
		Option [] default_options = new Option[3];
		default_options[0] = new Option("LEVEL SELECT",0);
		default_options[1] = new Option("TUTORIAL",4);
		default_options[2] = new Option("LEVEL EDITOR",2);
		return default_options;
		//make a method that sizes and positions them according to the number of options
		//resize isn't affecting this
	}

	public void drawMenu() {
		initializeCanvas(Constants.MENU_BCKGD_TEXTURE);
		mainMenu.draw(canvas);
	}
	
	public void update(){
		updateMenuAssets();
		
		mouseOverController.update(mainMenu.options, mainMenu);
		updateSelection();
		drawMenu();
		if (InputController.pressedE()){
			done(0);
		} else if (InputController.pressedM()){
			done(1);
		} else if (InputController.pressedH()){
			done(2);
		} else if (InputController.pressedP()){
			done(3);
		} else if (InputController.pressedT()){
			done(4);
		}
//		}  else if (InputController.pressedW()){
//			done(5);
//		}
	}
	
	public void done(int doneCode){
		gameNo = doneCode;
		isDone = true;
	}
	
	public boolean isDone(){
		return isDone;
	}
	
	public void updateMenuAssets(){
		if (manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE) && this.mainMenu.optionHighlight == null){
			this.mainMenu.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
		}
		
		if (this.mainMenu.logo == null && manager.isLoaded(Constants.MENU_LOGO)){
			this.mainMenu.setLogo(manager.get(Constants.MENU_LOGO,Texture.class));
		}
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
		canvas.setWhite(manager.get(Constants.WHITE_BOX, Texture.class));
		
		if (manager.isLoaded(Constants.MENU_FONT_FILE)) {
			canvas.setFont(manager.get(Constants.MENU_FONT_FILE,BitmapFont.class));
		}
		//This method shouldn't be in two places as it currently is, only one
    }
    
    /**
	 * Update when an action is not targeting yet
	 */
	private void updateSelection(){
		if (InputController.pressedA() || InputController.pressedEnter() || InputController.pressedLeftMouse()){
			done(mainMenu.selectedOption);
		}  else if ((InputController.pressedS() && !InputController.pressedW())){
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
		}   else if ((InputController.pressedW() && !InputController.pressedS())){
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
