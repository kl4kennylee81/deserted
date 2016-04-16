package edu.cornell.gdiac.ailab;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class MainMenuController {
	/** Current target selection */
	int selected;
	public String levelName;
	
	private HashMap<String, HashMap<String, Object>> levelDefs;
	
	private boolean isDone;
	private GameCanvas canvas;

	private AssetManager manager;
	private Menu menu;
	private MouseOverController mouseOverController;
	
	private static final String LEVEL_SELECT_NAME = "level select";
	
	
	public MainMenuController(GameCanvas canvas, AssetManager manager, MouseOverController mouseOverController,
			HashMap<String, HashMap<String,Object>> levelDefs){
		this.canvas = canvas;
		this.manager = manager;
		
		// the levelDef is needed to create the options
		this.levelDefs = levelDefs;
		Option[] default_options = makeDefaultOptions();
		this.menu = new MainMenu(default_options);
		if (default_options.length > 0){
			// FIXUP DEFAULT
			menu.setOption(0);
		}
		this.mouseOverController = mouseOverController; 
	}
	
	private Option[] makeDefaultOptions() {
		// we will rearrange when i merge to master
		if (this.levelDefs == null){
			Option [] default_options = new Option[0];
			return default_options;
		}
		Option [] default_options = new Option[this.levelDefs.size()];
		int i = 0;
		for (String levelName:levelDefs.keySet()){
			default_options[i] = new Option(levelName,levelName);
			i++;
		}
		return default_options;
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
		if (InputController.pressedP()){
			done("pvp");
		}
	}
	
	public void done(String levelName){
		if (menu instanceof MainMenu){
			if (levelName == LEVEL_SELECT_NAME){
				this.menu = createLevelMenu();
			}
			else {
				this.levelName = levelName;
				isDone = true;
			}
		}
		else if (menu instanceof LevelMenu){
			if (levelName == LEVEL_SELECT_NAME){
				this.menu = createMainMenu();
			}
			else{
				this.levelName = levelName;
				isDone = true;
			}
		}
	}
	
	public Menu createLevelMenu(){
		Option [] default_options = this.makeDefaultOptions();
		
		Menu levelMenu = new LevelMenu(default_options);
		if (default_options.length > 0){
			// DEFAULT FIXUP
			menu.setOption(0);
		}
		return levelMenu;
	}
	
	public Menu createMainMenu(){
		Option[] default_options = makeDefaultOptions();
		MainMenu menu = new MainMenu(default_options);
		if (default_options.length > 0){
			menu.setOption(0);
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
			
		     else if (InputController.pressedRight() && !InputController.pressedLeft()){
		         //newSelection % length
		         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
		         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
		    	 int newSelection = levelMenu.getCurIndexOption() + 1;
		         int length = levelMenu.getOptions().length;
		         int toSelect = (newSelection < 0) ? (length - 
							(Math.abs(newSelection) % length) ) 
							%length : (newSelection % 
									length);

			     String optionKey = levelMenu.getOption(toSelect);
			     levelMenu.selectOption(optionKey);
		     }  
		     else if (InputController.pressedLeft() && !InputController.pressedRight()){
				//Actions go from up down, so we need to flip
		    	 int newSelection = levelMenu.getCurIndexOption() - 1;
		        int length = levelMenu.getOptions().length;
		        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
			    String optionKey = levelMenu.getOption(toSelect);
		        levelMenu.selectOption(optionKey);
			}
		}
	}
	
	private void updateSelectionMainMenu(MainMenu mainMenu){
		if (mainMenu.getOptions().length <= 0){
			return;
		}
		
		if (InputController.pressedEnter() || InputController.pressedLeftMouse()){
			done(mainMenu.selectedOption);
		}  else if (InputController.pressedDown() && !InputController.pressedUp()){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	    	 int newSelection = mainMenu.getCurIndexOption() + 1;
	         int length = mainMenu.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	         // FIXUP this nonsense
		     String optionSrNo = mainMenu.getOptions()[toSelect].optionKey;
		     mainMenu.selectOption(optionSrNo);
		}   else if (InputController.pressedUp() && !InputController.pressedDown()){
	    	 int newSelection = mainMenu.getCurIndexOption() - 1;
	        int length = mainMenu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        // at the moment we are storing the srNo NOT THE INDEX!
	        String optionSrNo = mainMenu.getOptions()[toSelect].optionKey;
	        mainMenu.selectOption(optionSrNo);
		}
	}
	public void resetMenu(){
		isDone = false;
	}

}
