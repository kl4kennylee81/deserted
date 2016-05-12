package edu.cornell.gdiac.ailab;

import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

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
	
	/** Current Models */
    protected GridBoard board;
    protected Characters characters;
    protected TextMessage textMessages;
    protected AnimationPool animations;
    protected Shields shields;
    
    protected boolean isTutorial;
    
    
    protected HighlightScreen screen;
    
    protected String prompt;
    
    public int warningTime;
    public final static int WARNING_DONE_TIME = 120;
    
    /** Current state of game */
    protected InGameState inGameState;
    protected FileHandle fileNumFile;
    protected int fileNum;
    protected FileHandle dataFile;
    protected JSONArray jsonArray;
    
    protected String levelName;
    private static Texture DESCRIPTION_BOX_TEXTURE;
    
    Integer winIn;
    Integer surviveFor;
    
    boolean temp;
    
    public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK,
		PAUSED,
		DONE,
		WARNING
	}
    
    public GameplayController(MouseOverController moc, FileHandle file, int fileNum, boolean isTutorial){
    	mouseOverController = moc;
    	fileNumFile = file;
    	this.fileNum = fileNum;
    	
    	// create the description box texture
    	if (DESCRIPTION_BOX_TEXTURE == null){
    		DESCRIPTION_BOX_TEXTURE = new Texture(Constants.DESCRIPTION_BOX_TEXTURE);
    	}
    }
    
    public void resetGame(Level level){
    	inGameState = InGameState.NORMAL;
    	fileNum++;
		dataFile = GameEngine.dataGen ? new FileHandle(Constants.DATA_PATH+"data/data"+fileNum) : null;
		jsonArray = new JSONArray();
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
        aiController = new AIController(board,characters,level.getTacticalManager());
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
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		if (actionBarController.isAISelection) {
    			aiController.update();
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			inGameState = InGameState.SELECTION;
    		}
    		break;
    	case SELECTION:
    		screen.setJustScreen();
    		mouseOverController.clearAll();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		selectionMenuController.update();
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			prompt = null;
    			board.reset();
    			aiController.outputData(jsonArray);
    		}
    		break;
    	case ATTACK:
    		actionController.update();
    		persistingController.updateProjs();
    		if (actionController.isDone() && persistingController.isDone()){
    			if (actionBarController.isPlayerSelection){
    				inGameState = InGameState.SELECTION;
    			} else {
    				inGameState = InGameState.NORMAL;
    			}
    		}
    		this.animations.sort();
    		break;
    	case WARNING:
    		
    		if (InputController.pressedEnter()){
    			System.out.println("ENTER 1");
    			temp = true;
    			inGameState = InGameState.DONE;
    			if (this.isTutorial){
	    			if (this.leftsideDead()){
	    				GameEngine.nextLevel = TutorialSteps.levelName;
	    			}
	    			else if (this.rightsideDead()){
	    				GameEngine.nextLevel = TutorialSteps.nextLevel;
	    			}
    			}
    		}
    		return;
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
    	// not sure why this is needed
//    	if (this.isTutorial && inGameState == InGameState.WARNING) {
//    		TutorialSteps.drawWarningText(canvas);
//    		return;
//    	}
    	// temporary hacky code to show that you have won without destroying the canvas
    	// will definately need to rewrite this portion
//		if (this.gameOver() && this.rightsideDead() && inGameState == InGameState.WARNING){
//    		//canvas.drawCenteredText("You have Won", canvas.getWidth()/2, canvas.getHeight()/2, Color.WHITE);
//    		CompletionScreen cs = CompletionScreen.getInstance();
//    		cs.setIsWin(true);
//    		cs.draw(canvas);
//    	}
//		else if (this.gameOver() && this.leftsideDead() && inGameState == InGameState.WARNING){
//    		//canvas.drawCenteredText("Try Again!", canvas.getWidth()/2, canvas.getHeight()/2, Color.WHITE);	
//			CompletionScreen cs = CompletionScreen.getInstance();
//			cs.setIsWin(false);
//			cs.draw(canvas);
//		}
		
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
    	board.draw(canvas);
    	
    	
    	if (inGameState == InGameState.SELECTION){
    		shields.draw(canvas,false,true);
        	
        	shields.draw(canvas,true,true);
        	
        	drawCharacters(canvas);
        	
        	animations.draw(canvas,board,inGameState);
    	} else {
    		shields.draw(canvas,false,false);
    		
    		animations.draw(canvas,board,inGameState);
    		
    		drawCharacters(canvas);
    		
    		shields.draw(canvas,true,false);
    	}
        
        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        //screen should be drawn after greyed out characters
        //but before selected characters
        float turnX = canvas.getWidth() * 0.9f;
    	float turnY1 = canvas.getHeight() * 0.9f;
    	float turnY2 = canvas.getHeight() * 0.85f;
        if (winIn != null){
        	canvas.drawCenteredText("Win in", turnX, turnY1, Color.BLACK);
        	canvas.drawCenteredText(winIn - turnsCompleted() + " turns", turnX, turnY2, Color.BLACK);
        } else if (surviveFor != null){
        	canvas.drawCenteredText("Survive for", turnX, turnY1, Color.BLACK);
        	canvas.drawCenteredText(surviveFor - turnsCompleted() + " turns", turnX, turnY2, Color.BLACK);
        }
        
        if (temp){
        	System.out.println("check here");
        }
        
        
		if (this.gameOver() && this.playerWon() && inGameState == InGameState.WARNING){
    		//canvas.drawCenteredText("You have Won", canvas.getWidth()/2, canvas.getHeight()/2, Color.WHITE);
    		CompletionScreen cs = CompletionScreen.getInstance();
    		GameSaveStateController gss = GameSaveStateController.getInstance();
    		gss.beatLevel(levelName);
    		cs.skill_point = gss.getLevelSP(levelName);
    		cs.characters_unlocked = gss.getLevelUnlockedChars(levelName);
    		cs.setIsWin(true);
    		cs.draw(canvas);
    	}
		else if (this.gameOver() && this.playerLost() && inGameState == InGameState.WARNING){
    		//canvas.drawCenteredText("Try Again!", canvas.getWidth()/2, canvas.getHeight()/2, Color.WHITE);	
			CompletionScreen cs = CompletionScreen.getInstance();
			cs.setIsWin(false);
			cs.draw(canvas);
		}
		else if (this.gameOver() && this.tieGame() && inGameState == InGameState.WARNING){
    		//canvas.drawCenteredText("Try Again!", canvas.getWidth()/2, canvas.getHeight()/2, Color.WHITE);	
			System.out.println("gameplaycontroller tie game make a completion screen for it");
			CompletionScreen cs = CompletionScreen.getInstance();
			cs.setIsWin(false);
			cs.draw(canvas);
		}
    }
    
    private boolean isHitByAnimation(Character c){
    	for (AnimationNode an : animations.pool){
    		if (an.xPos == c.xPosition && an.yPos == c.yPosition){
    			return true;
    		}
    	}
    	return false;
    }
    
    //Change how i do this.
    //This needs to be done so characters below show over characters above and selection menu
    //shows over characters.
    protected void drawCharacters(GameCanvas canvas){
    	boolean inSelection = inGameState == InGameState.SELECTION;
		boolean shouldDim = inGameState == InGameState.SELECTION || 
				mouseOverController.isCharacterHighlighted();
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
    	characters.draw(canvas,board,shouldDim, inSelection);
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