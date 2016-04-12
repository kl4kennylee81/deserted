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
	private Menu menu;
	private MouseOverController mouseOverController;
	
	private int LEVEL_SELECT_CODE = 0;
	
	
	public MainMenuController(GameCanvas canvas, AssetManager manager, MouseOverController mouseOverController){
		this.canvas = canvas;
		this.manager = manager;
		Option[] default_options = makeDefaultOptions();
		menu = new MainMenu(default_options);
		if (default_options.length > 0){
			menu.selectOption(0);
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
		menu.draw(canvas);
	}
	
	public void update(){
		updateMenuAssets();
		
		mouseOverController.update(menu.options, menu);
		updateSelection();
		drawMenu();
	}
	
	public void done(int doneCode){
		if (menu instanceof MainMenu){
			if (doneCode == LEVEL_SELECT_CODE){
				this.menu = createLevelMenu();
			}
			else {
				gameNo = doneCode;
				isDone = true;
			}
		}
		else if (menu instanceof LevelMenu){
			if (doneCode == LEVEL_SELECT_CODE){
				this.menu = createMainMenu();
			}
			else{
				gameNo = doneCode;
				isDone = true;
			}
		}
	}
	
	public Menu createLevelMenu(){
		Option [] default_options = new Option[4];
		default_options[0] = new Option("Back",0);
		default_options[1] = new Option("TUTORIAL 1",4);
		default_options[2] = new Option("TUTORIAL 2",5);
		default_options[3] = new Option("LEVEL 1",2);
		
		Menu levelMenu = new LevelMenu(default_options);
		if (default_options.length > 0){
			menu.selectOption(0);
		}
		return levelMenu;
	}
	
	public Menu createMainMenu(){
		Option[] default_options = makeDefaultOptions();
		MainMenu menu = new MainMenu(default_options);
		if (default_options.length > 0){
			menu.selectOption(0);
		}
		return menu;
	}
	
	public boolean isDone(){
		return isDone;
	}
	
	public void updateMenuAssets(){
		
		if (menu instanceof MainMenu){
			MainMenu mainMenu = (MainMenu) menu;
		
			if (manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE) && mainMenu.optionHighlight == null){
				mainMenu.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
			}
			
			if (mainMenu.logo == null && manager.isLoaded(Constants.MENU_LOGO)){
				mainMenu.setLogo(manager.get(Constants.MENU_LOGO,Texture.class));
			}
		}
		if (menu instanceof LevelMenu){
			LevelMenu levelMenu = (LevelMenu) menu;
			if (manager.isLoaded(Constants.LEVEL_SELECT_REG) && levelMenu.getImage() == null){
				levelMenu.setImage(manager.get(Constants.LEVEL_SELECT_REG,Texture.class));
			}
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
		if (menu instanceof MainMenu){
			MainMenu mainMenu = (MainMenu) menu;
			updateSelectionMainMenu(mainMenu);
		}
		else if (menu instanceof LevelMenu){
			LevelMenu levelMenu = (LevelMenu) menu;
			if (InputController.pressedEnter() || InputController.pressedLeftMouse()){
				done(levelMenu.selectedOption);
			}
			
		     else if ((InputController.pressedD() && !InputController.pressedA())){
		         //newSelection % length
		         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
		         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
		    	 int newSelection = levelMenu.getCurIndexOption() + 1;
		         int length = levelMenu.getOptions().length;
		         int toSelect = (newSelection < 0) ? (length - 
							(Math.abs(newSelection) % length) ) 
							%length : (newSelection % 
									length);
			     int optionSrNo = levelMenu.getOptions()[toSelect].srNo;
			     levelMenu.selectOption(optionSrNo);
		     }  
		     else if ((InputController.pressedA() && !InputController.pressedD())){
				//Actions go from up down, so we need to flip
		    	 int newSelection = levelMenu.getCurIndexOption() - 1;
		        int length = levelMenu.getOptions().length;
		        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
		        int optionSrNo = levelMenu.getOptions()[toSelect].srNo;
		        levelMenu.selectOption(optionSrNo);
			}
		}
	}
	
	private void updateSelectionMainMenu(MainMenu mainMenu){
		if (InputController.pressedEnter() || InputController.pressedLeftMouse()){
			done(mainMenu.selectedOption);
		}  else if ((InputController.pressedS() && !InputController.pressedW())){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	    	 int newSelection = mainMenu.getCurIndexOption() + 1;
	         int length = mainMenu.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
		     int optionSrNo = mainMenu.getOptions()[toSelect].srNo;
		     mainMenu.selectOption(optionSrNo);
		}   else if ((InputController.pressedW() && !InputController.pressedS())){
			//Actions go from up down, so we need to flip
	    	 int newSelection = mainMenu.getCurIndexOption() - 1;
	        int length = mainMenu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        // at teh moment we are storing the srNo NOT THE INDEX!
	        int optionSrNo = mainMenu.getOptions()[toSelect].srNo;
	        mainMenu.selectOption(optionSrNo);
		}
	}
	public void resetMenu(){
		isDone = false;
	}

}
