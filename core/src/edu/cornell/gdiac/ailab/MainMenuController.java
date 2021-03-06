package edu.cornell.gdiac.ailab;

import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import edu.cornell.gdiac.ailab.GameSaveState.LevelData;

public class MainMenuController {
	/** Current target selection */
	int selected;
	public String levelName;
	
	private List<LevelData> levelDefs;
	
	private boolean isDone;
	private GameCanvas canvas;

	private AssetManager manager;
	private Menu menu;
	private MouseOverController mouseOverController;
	
	private static final String LEVEL_SELECT_NAME = "Level Select";
	
	private static final String MAIN_MENU_NAME = "Main Menu";
	
	public MainMenuController(GameCanvas canvas, AssetManager manager, MouseOverController mouseOverController,
			List<LevelData> levelDefs){
		this.canvas = canvas;
		this.manager = manager;
		
		// the levelDef is needed to create the options
		this.levelDefs = levelDefs;
		// initially create the main menu options
		this.menu = this.createStartingMenu();
		this.mouseOverController = mouseOverController; 
	}
	
	public void setLevelSelect(){
		this.menu = this.createLevelMenu();
	}
	
	private Option[] makeDefaultOptions() {
		if (this.levelDefs == null){
			Option [] default_options = new Option[0];
			return default_options;
		}
		Option [] default_options = new Option[this.levelDefs.size()];
		
		int i = 0;
		for (LevelData ld : levelDefs){
			default_options[i] = new Option(ld.levelName,ld.levelName);
			i++;
		}
		return default_options;
	}
	
	private Option[] makeMainMenuOptions(){
		Option[] options = new Option[3];
		options[0] = new Option("Play","Play");
		options[1] = new Option(LEVEL_SELECT_NAME,LEVEL_SELECT_NAME);
		options[2] = new Option("Play Online", "Networking");
		return options;
	}
	
	private Option[] makeLevelSelectOptions(){
		Option[] options;
		if (this.levelDefs == null){
			options = new Option[0];
			return options;
		}
		int num_levels = 0;
		for (LevelData ld : levelDefs){
			if (ld.displayName != null){
				num_levels+=1;	
			}
		}
		options = new Option[num_levels+1];
		
		int i = 0;
		for (LevelData ld : levelDefs){
			if (ld.displayName != null){
				options[i] = new Option(ld.displayName,ld.levelName);
			}
			i++;
		}
		options[num_levels] = new Option("Back",MAIN_MENU_NAME);
		return options;
	}

	public void drawMenu() {
		if (menu instanceof StartingMenu){
			initializeCanvas(Constants.MENU_BCKGD_TEXTURE);
		} else if (menu instanceof MainMenu){
			initializeCanvas(Constants.MENU_BCKGD_TEXTURE);
		} else if (menu instanceof LevelMenu){
			initializeCanvas(Constants.MENU_BCKGD_TEXTURE);
		}
		menu.draw(canvas);
	}
	
	public void update(){
		this.isDone = false;
		updateMenuAssets();
		mouseOverController.update(menu.options, menu);
		updateSelection();
		drawMenu();
		
		// this is technically cheating this wont be allowed just used for fast testing
		if (InputController.pressedP()){
			done("pvp_local");
		}
	}
	
	public void done(String levelName){
		if (menu instanceof StartingMenu){
			((StartingMenu) menu).updateLevel(levelName);
			if (((StartingMenu) menu).isDone){
				if (((StartingMenu) menu).isNew){
					//todo new game starting at tutorial1
					this.levelName = Constants.LEVEL_BEGIN;
					isDone = true;
				} else {
					this.menu = createMainMenu();
				}
			}
		}
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
			if (levelName == MAIN_MENU_NAME){
				this.menu = createMainMenu();
			}
			else{
				this.levelName = levelName;
				isDone = true;
			}
		}
	}
	
	public Menu createLevelMenu(){
		Option [] default_options = this.makeLevelSelectOptions();
		
		Menu levelMenu = new LevelMenu(default_options);
		if (default_options.length > 0){
			// DEFAULT FIXUP
			levelMenu.setOption(0);
		}
		return levelMenu;
	}
	
	public Menu createMainMenu(){
		Option[] default_options = this.makeMainMenuOptions();
		MainMenu mainmenu = new MainMenu(default_options);
		if (default_options.length > 0){
			mainmenu.setOption(0);
		}
		return mainmenu;
	}
	
	public Menu createStartingMenu(){
		StartingMenu startmenu = new StartingMenu();
		return startmenu;
	}
	
	public boolean isDone(){
		return isDone;
	}
	
	public void updateMenuAssets(){
		if (menu instanceof StartingMenu){
			StartingMenu startMenu = (StartingMenu) menu;
			if (startMenu.optionHighlight == null && manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE)){
				startMenu.setHighlight(manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class));
			}
		}

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
			if (levelMenu.optionHighlight == null && manager.isLoaded(Constants.MENU_HIGHLIGHT_TEXTURE)){
				levelMenu.optionHighlight = manager.get(Constants.MENU_HIGHLIGHT_TEXTURE,Texture.class);
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
		if (menu instanceof StartingMenu){
			StartingMenu startMenu = (StartingMenu) menu;
			updateStartingMainMenu(startMenu);
		} 
		else if (menu instanceof MainMenu){
			MainMenu mainMenu = (MainMenu) menu;
			updateSelectionMainMenu(mainMenu);
		}
		else if (menu instanceof LevelMenu){
			LevelMenu levelMenu = (LevelMenu) menu;
			boolean mouseCondition = false;
			if (levelMenu.selectedIndex!=-1){
				Option curOption = levelMenu.options[levelMenu.selectedIndex];
				mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
					&& (InputController.pressedLeftMouse());
			}
			if (InputController.pressedEnter() || mouseCondition){
				// fixup to get cur option string from the index
				String levelKey = levelMenu.getCurOption();
				done(levelKey);
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
			     levelMenu.setOption(toSelect);
		     }  
		     else if (InputController.pressedLeft() && !InputController.pressedRight()){
				//Actions go from up down, so we need to flip
		    	 int newSelection = levelMenu.getCurIndexOption() - 1;
		        int length = levelMenu.getOptions().length;
		        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
		        levelMenu.setOption(toSelect);
			}
		}
	}
	
	private void updateSelectionMainMenu(MainMenu mainMenu){
		if (mainMenu.getOptions().length <= 0){
			return;
		}
		
		boolean mouseCondition = false;
		if (mainMenu.selectedIndex!=-1){
			Option curOption = mainMenu.options[mainMenu.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		
		if (InputController.pressedEnter() || mouseCondition){
			// fixup to get cur option string from the index
			String levelKey = mainMenu.getCurOption();
			done(levelKey);
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

		     mainMenu.setOption(toSelect);
		     
		}   else if (InputController.pressedUp() && !InputController.pressedDown()){
	    	 int newSelection = mainMenu.getCurIndexOption() - 1;
	        int length = mainMenu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        mainMenu.setOption(toSelect);
		}
	}
	
	private void updateStartingMainMenu(StartingMenu startingMenu){
		boolean mouseCondition = false;
		if (startingMenu.selectedIndex!=-1){
			Option curOption = startingMenu.options[startingMenu.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		
		if (InputController.pressedEnter() || mouseCondition){
			// fixup to get cur option string from the index
			String levelKey = startingMenu.getCurOption();
			done(levelKey);
		}  else if (InputController.pressedDown() && !InputController.pressedUp()){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	    	 int newSelection = startingMenu.getCurIndexOption() + 1;
	         int length = startingMenu.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);

		     startingMenu.setOption(toSelect);
		     
		}   else if (InputController.pressedUp() && !InputController.pressedDown()){
	    	 int newSelection = startingMenu.getCurIndexOption() - 1;
	        int length = startingMenu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        startingMenu.setOption(toSelect);
		}
	}
	
	public void resetMenu(){
		this.levelName = "";
		this.menu = createMainMenu();
		this.isDone = false;
	}

}
