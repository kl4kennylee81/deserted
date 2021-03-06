package edu.cornell.gdiac.ailab;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.JsonArray;

import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.TextMessage.Message;
import edu.cornell.gdiac.ailab.CurrentHighlight;

public class GameplayController {
	/** Subcontroller for actions (CONTROLLER CLASS) */
    protected ActionController actionController;
    /** Subcontroller for selection menu (CONTROLLER CLASS) */
    protected SelectionMenuController selectionMenuController;
    /** Subcontroller for action bar (CONTROLLER CLASS) */
    protected ActionBarController actionBarController;
    /** Subcontroller for persisting actions (CONTROLLER CLASS) */
    protected PersistingController persistingController;
    /** Subcontroller for AI selection (CONTROLLER CLASS) */
    protected AIController aiController;
    /** Subcontroller for managing effects */
    protected EffectController effectController;
    /** Subcontroller for managing mouse over highlighting */
    protected MouseOverController mouseOverController;
	
    protected CompletionMenuController compMenuController;
    
    protected PauseMenuController pauseMenuController;
    
	/** Current Models */
    protected GridBoard board;
    protected Characters characters;
    protected TextMessage textMessages;
    protected AnimationPool animations;
    protected Shields shields;
    
    protected boolean isTutorial;
    protected boolean canPause;
    
    protected HighlightScreen screen;
    
    protected String prompt;
    
    public int warningTime;
    public final static int WARNING_DONE_TIME = 120;
    
    /** Current state of game */
    protected InGameState inGameState;
    protected FileHandle fileNumFile;
    protected int fileNum;
    protected FileHandle dataFile;
    protected JsonArray jsonArray;
    
    protected String levelName;
    private static Texture DESCRIPTION_BOX_TEXTURE;
    
    Integer winIn;
    Integer surviveFor;
    
    boolean temp;
    
    InGameState prePauseState;
    
    boolean reset;
    
    public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK,
		PAUSED,
		PAUSEMENU,
		DONE,
		WARNING,
		WAITING,
	}
    
    public GameplayController(MouseOverController moc, CompletionMenuController cmc, 
    				PauseMenuController pmc, FileHandle file, int fileNum, boolean isTutorial){
    	mouseOverController = moc;
    	compMenuController = cmc;
    	pauseMenuController = pmc;
    	fileNumFile = file;
    	this.fileNum = fileNum;
    	
    	// create the description box texture
    	if (DESCRIPTION_BOX_TEXTURE == null){
    		DESCRIPTION_BOX_TEXTURE = new Texture(Constants.DESCRIPTION_BOX_TEXTURE);
    	}
    }
    
    public void resetGame(Level level){
    	reset = false;
    	inGameState = InGameState.NORMAL;
    	fileNum++;
		dataFile = GameEngine.dataGen ? new FileHandle(Constants.DATA_PATH+"data/data"+fileNum) : null;
		jsonArray = new JsonArray();
		levelName = level.getName();
    	
        // Create the models.
        board = level.getBoard();
        this.characters = level.getCharacters();
        screen = new HighlightScreen();
        
        textMessages = new TextMessage();
        animations = new AnimationPool();
        this.isTutorial = level.isTutorial();
        
        shields = new Shields(board);
        
        temp = false;
        
		// Create the subcontrollers
        actionController = new ActionController(board,characters,textMessages,animations,shields);
        selectionMenuController = new SelectionMenuController(board,characters);
        actionBarController = new ActionBarController(characters);
        aiController = new AIController(board,characters,level.getTacticalManager(),shields);
        persistingController = new PersistingController(board,characters,textMessages,animations,shields);
        effectController = new EffectController();
        mouseOverController.init(screen, board);
        warningTime = 0;
        
    }
    
    public void setWinCondition(Integer winIn, Integer surviveFor){
    	this.winIn = winIn;
    	this.surviveFor = surviveFor;
    }
    
    public void update(){ 
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
    		// update the character models
    		characters.update();
    		effectController.update(characters,board);
    		actionBarController.update();
    		//persistingController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters,selectionMenuController.choosingTarget);
    		if (actionBarController.isAISelection) {
    			aiController.update();
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection && actionBarController.selectingFirst){
  				inGameState = InGameState.SELECTION;
  			} else if (actionBarController.isNetworkingOpponentSelection){
  				inGameState = InGameState.WAITING;
  			}
    		break;
    	case SELECTION:
    		screen.setJustScreen();
    		mouseOverController.clearAll();
    		mouseOverController.update(selectionMenuController.getMenu(),characters,selectionMenuController.choosingTarget);
    		selectionMenuController.update();
    		if (selectionMenuController.isDone()){
    			if (handleSelectionDone()) {
	    			prompt = null;
	    			board.reset();
	    			aiController.outputData(jsonArray);
	    			if (actionBarController.isNetworkingOpponentSelection) {
	    				inGameState = InGameState.WAITING;
	    			} else {
	    				inGameState = InGameState.NORMAL;
	    			}
    			}
    		}
    		break;
    	case ATTACK:
    		actionController.update();
    		persistingController.updateProjs();
    		if (actionController.isDone() && persistingController.isDone()){
    			if (actionBarController.isPlayerSelection && actionBarController.selectingFirst){
    				inGameState = InGameState.SELECTION;
    			} else if (actionBarController.isNetworkingOpponentSelection){
    				inGameState = InGameState.WAITING;
    			} else {
    				inGameState = InGameState.NORMAL;
    			}
    		}
    		this.animations.sort();
    		break;
    	case PAUSEMENU:
    		screen.setJustScreen();
    		pauseMenuController.update();
    		if (pauseMenuController.doneSelecting){
    			String selected = pauseMenuController.selected;
    			if (selected.equals("Resume Game")){
    				inGameState = prePauseState;
    			}
    			else if (selected.equals("Retry")){
    				inGameState = InGameState.DONE;
    				reset = true;
    			}else if (selected.equals("Main Menu")){
    				this.signalQuit();
    			}
    			pauseMenuController.reset();
    		}
    		break;
    	case WARNING:
    		screen.setJustScreen();
    		//notifications processed first
    		if (CompletionScreen.getInstance().notifications.size() > 0){
    			if (InputController.pressedEnter() || InputController.leftMouseClicked){
    				CompletionScreen.getInstance().notifications.remove(0);
    			}
    		}else{
    			//all notifications have been processed
    			updateCompletionMenu();
    		}
    		
    		return;
    	case WAITING: 
    		handleWaiting();
    		break;
			default:
				break;	
    	}
    	
    	updateTextMessages();
    	removeDead();
    	if (gameOver()){
    		// you are set to done after the warning
    		if (inGameState != InGameState.DONE){
    			inGameState = InGameState.WARNING;
    			return;
    		}
    		
    		if(GameEngine.dataGen){
    			dataFile.writeString(jsonArray.toString(), false);
        		fileNumFile.writeString(""+fileNum, false);
    		}
    		ObjectLoader.getInstance().unloadCurrentLevel();
    	}
    	
       	if ((InputController.pressedESC() || InputController.pressedP()) && canPause){
       		if (inGameState != InGameState.PAUSEMENU){
       			prePauseState = inGameState;
        		inGameState = InGameState.PAUSEMENU;
       		}
       		else{
       			inGameState = prePauseState;
       			pauseMenuController.reset();
       		}
       	}
       	
    }
    
    public void signalQuit(){
    	InputController.artificialRPressed = true;
    }
    
    public void updateCompletionMenu(){
    	compMenuController.update();
			//have made a selection on the completion menu
    		if (compMenuController.doneSelecting){
    			if (compMenuController.selected.equals("Next Level")){
	    			System.out.println("ENTER 1");
	    			temp = true;
	    			if (this.isTutorial){
		    			if (this.leftsideDead()){
		    				GameEngine.nextLevel = TutorialSteps.levelName;
		    			}
		    			else if (this.rightsideDead()){
		    				GameEngine.nextLevel = TutorialSteps.nextLevel;
		    			}
	    			}
    			}else{
    				InputController.artificialRPressed = true;
    			}
    			inGameState = InGameState.DONE;
    			compMenuController.reset();
    			CompletionScreen.getInstance().reset();
    		}
    }
    
    public boolean handleSelectionDone(){
    	actionBarController.isPlayerSelection = false;
    	return true;
    }
    
    public void handleWaiting(){
    	actionBarController.isNetworkingOpponentSelection = false;
    	return;
    }
    
    public void removeDead(){
    	Iterator<Character> iter = characters.iterator();
    	while (iter.hasNext()) {
    	    Character c = iter.next();
			if (!c.isAlive()){
				iter.remove();
			}
    	}
    }
    
    public void drawPlay(GameCanvas canvas){
    	
		if (TutorialGameplayController.highlight_action > 0){
			//make a custom highlight and shift it by highlight_action
			 
    		Character selectedChar = selectionMenuController.selected;
    		if (selectedChar != null){
    			int count = 0;
    			for (int i=0; i< characters.size();i++){
    				Character c = characters.get(i);
    				if (c == selectedChar){
    					count = i+1;
    					break;
    				}
    			}
    			float highlightX = selectedChar.actionBar.getBarCastPoint(canvas) + selectedChar.actionBar.getSlotWidth(canvas);
    			float highlightY = selectedChar.actionBar.getY(canvas, count) - selectedChar.actionBar.getBarHeight(canvas);//characters.indexOf(selectedChar));
    			CurrentHighlight current = new CurrentHighlight(highlightX, highlightY, 0.01f*canvas.getWidth(), 0.1f*canvas.getHeight(), "down", true, false);
    			screen.addCurrentHighlight(current);
    		}
			//getY: iterate over characters, and when character matches selected character thats the number to pass to getY
		}
        screen.draw(canvas);
        board.draw(canvas, selectionMenuController);
       

    	if (inGameState == InGameState.WAITING) {
    		canvas.drawCenteredText("Waiting for opponent", canvas.width/2, canvas.height*.6f, Color.BLACK);
    	}
    	
    	if (inGameState == InGameState.SELECTION){
    		shields.draw(canvas,false,true);
        	
        	shields.draw(canvas,true,true);
        	
        	drawCharacters(canvas);
        	
        	animations.draw(canvas,board,inGameState);
    	} else {
    		shields.draw(canvas,false,false);
    		
    		animations.draw(canvas,board,inGameState);
    		
    		
    		//interleave drawCharacters and shields
    		boolean inSelection = inGameState == InGameState.SELECTION;
    		boolean shouldDim = inGameState == InGameState.SELECTION || 
    				mouseOverController.isCharacterHighlighted();
    		boolean charActionHovered = characters.isActionHovered();
        	for (int i = board.height-1; i >= 0; i--){
        		for (Character c : characters){
        			
        			if (c.yPosition == i && c.isAlive()){
        				c.draw(canvas,board,shouldDim,this.inGameState,isHitByAnimation(c));
        			}
        			if (c.getShadowY() == i && c.needShadow() && c.isAlive()){
        				c.drawShadowCharacter(canvas,board,this.inGameState);
        			}
                }
        		shields.draw(canvas,true,false,i);
        	}
        	characters.draw(canvas,board,shouldDim, inSelection, charActionHovered);
    		
    	}
        
        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        //screen should be drawn after greyed out characters
        //but before selected characters
        float turnX = canvas.getWidth() * 0.1f;
    	float turnY1 = canvas.getHeight() * 0.75f;
    	float turnY2 = canvas.getHeight() * 0.72f;
        if (winIn != null){
        	canvas.drawCenteredText("Win in", turnX, turnY1, Color.WHITE);
        	canvas.drawCenteredText(winIn - turnsCompleted() + " turns", turnX, turnY2, Color.WHITE);
        } else if (surviveFor != null){
        	canvas.drawCenteredText("Survive for", turnX, turnY1, Color.WHITE);
        	canvas.drawCenteredText(surviveFor - turnsCompleted() + " turns", turnX, turnY2, Color.WHITE);
        }
        
        if (temp){
        	System.out.println("check here");
        }
        
        drawGameOver(canvas);
        drawPlayerNames(canvas);
		if (inGameState == InGameState.PAUSEMENU){
    		PauseMenu.getInstance().draw(canvas);
    	}
    }
    
    public void drawPlayerNames(GameCanvas canvas){
    	return;
    }
    
    private boolean isHitByAnimation(Character c){
    	for (AnimationNode an : animations.pool){
    		if (an.xPos == c.xPosition && an.yPos == c.yPosition){
    			return true;
    		}
    	}
    	return false;
    }
    
    protected void drawGameOver(GameCanvas canvas){
    	CompletionScreen cs = CompletionScreen.getInstance();
    	if (this.gameOver() && this.playerWon() && inGameState == InGameState.WARNING){
			drawCompletedGame(canvas, cs);
    	}
		else if (this.gameOver() && this.playerLost() && inGameState == InGameState.WARNING){
			cs.setIsWin(false);
			cs.draw(canvas);
		}
		else if (this.gameOver() && this.tieGame() && inGameState == InGameState.WARNING){
			cs.setIsWin(false);
			cs.draw(canvas);
		}
    }
    
    protected void drawCompletedGame(GameCanvas canvas, CompletionScreen cs){
		GameSaveStateController gss = GameSaveStateController.getInstance();
		cs.skill_point = gss.getLevelSP(levelName);
		cs.characters_unlocked = gss.getLevelUnlockedChars(levelName);
		gss.beatLevel(levelName);
		cs.setIsWin(true);
		cs.draw(canvas);
    }
    
    //Change how i do this.
    //This needs to be done so characters below show over characters above and selection menu
    //shows over characters.
    protected void drawCharacters(GameCanvas canvas){
    	boolean inSelection = inGameState == InGameState.SELECTION;
		boolean shouldDim = inGameState == InGameState.SELECTION || 
				mouseOverController.isCharacterHighlighted();
		boolean charActionHovered = characters.isActionHovered();
    	for (int i = board.height-1; i >= 0; i--){
    		for (Character c : characters){
    			
    			if (c.yPosition == i && c.isAlive()){
    				c.draw(canvas,board,shouldDim,this.inGameState,isHitByAnimation(c));
    			}
    			if (c.getShadowY() == i && c.needShadow() && c.isAlive()){
    				c.drawShadowCharacter(canvas,board,this.inGameState);
    			}
            }
    	}
    	characters.draw(canvas,board,shouldDim, inSelection, charActionHovered);
    }
    
    public void drawAfter(GameCanvas canvas){
	    if (leftsideDead() && rightsideDead()){
			canvas.drawCenteredText("A tie Press R to Return", canvas.getWidth()/2, canvas.getHeight()/2, Color.BLACK);
		} else if (leftsideDead()){
			canvas.drawCenteredText("RED SIDE WINS Press R to Return", canvas.getWidth()/2, canvas.getHeight()/2, Color.BLACK);
		} else if (rightsideDead()){
			canvas.drawCenteredText("BLUE SIDE WINS Press R to Return", canvas.getWidth()/2, canvas.getHeight()/2, Color.BLACK);
		} else 
			
	    canvas.drawCenteredText("Press R to return", canvas.getWidth()/2, canvas.getHeight()/2, Color.BLACK);
    }
    
    
    public boolean leftsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	return dead;
    }
    
    public boolean rightsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (!c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	//dead = true;
    	return dead;
    }
    public boolean turnGameOver(){
    	if (winIn != null){
    		return turnsCompleted() >= winIn;
    	}
    	if (surviveFor != null){
    		return turnsCompleted() >= surviveFor;
    	}
    	return false;
    }
    
    public int turnsCompleted(){
    	return actionBarController.turnsCompleted;
    }
    
    public boolean gameOver(){
    	return (leftsideDead() || rightsideDead() || turnGameOver());
    }
    
    public boolean playerWon(){
    	if (surviveFor != null){
    		return !leftsideDead();
    	}
    	return rightsideDead() && !leftsideDead();
    }
    
    public boolean tieGame(){
    	System.out.println("make tieGame check more rigorous gameplaycontroller");
    	return rightsideDead() && leftsideDead();
    }
    
    public boolean playerLost(){
    	if (surviveFor != null){
    		return leftsideDead();
    	}
    	if (winIn != null){
    		return !rightsideDead();
    	}
    	return !rightsideDead() && leftsideDead();
    }
    
    public boolean isDone(){
    	return inGameState == InGameState.DONE;
    }
    
    public void updateTextMessages(){
    	Iterator<Message> iter = textMessages.damageMessages.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    	
    	iter = textMessages.otherMessages.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    	
    	iter = textMessages.tempSingles.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    }
	
}