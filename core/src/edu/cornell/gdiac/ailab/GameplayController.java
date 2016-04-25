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
    
    protected boolean isTutorial;
    
    
    protected HighlightScreen screen;
    
    protected String prompt;
    
    public int warningTime;
    public final static int WARNING_DONE_TIME = 80;
    
    /** Current state of game */
    protected InGameState inGameState;
    protected FileHandle fileNumFile;
    protected int fileNum;
    protected FileHandle dataFile;
    protected JSONArray jsonArray;
    
    public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK,
		PAUSED,
		DONE,
		WARNING
	}
    
    public GameplayController(MouseOverController moc, FileHandle file, int fileNum){
    	mouseOverController = moc;
    	fileNumFile = file;
    	this.fileNum = fileNum;
    }
    
    public void resetGame(Level level){
    	inGameState = InGameState.NORMAL;
    	fileNum++;
		dataFile = GameEngine.dataGen ? new FileHandle(Constants.DATA_PATH+"data/data"+fileNum) : null;
		jsonArray = new JSONArray();
    	
        // Create the models.
        board = level.getBoard();
        this.characters = level.getCharacters();
        screen = new HighlightScreen();
        
        textMessages = new TextMessage();
        animations = new AnimationPool();
        this.isTutorial = level.isTutorial();
        
		// Create the subcontrollers
        actionController = new ActionController(board,characters,textMessages,animations);
        selectionMenuController = new SelectionMenuController(board,characters);
        actionBarController = new ActionBarController(characters);
        aiController = new AIController(board,characters,level.getTacticalManager());
        persistingController = new PersistingController(board,characters,textMessages,animations);
        effectController = new EffectController();
        mouseOverController.init(screen, board);
        warningTime = 0;
    }
    
    public void update(){
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
    		// update the character models
    		characters.update();
    		effectController.update(characters,board);
    		actionBarController.update();
    		persistingController.update();
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
    		selectionMenuController.update();;
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			prompt = null;
    			board.reset();
    			aiController.outputData(jsonArray);
    		}
    		break;
    	case ATTACK:
    		actionController.update();
    		if (actionController.isDone()){
    			if (actionBarController.isPlayerSelection){
    				inGameState = InGameState.SELECTION;
    			} else {
    				inGameState = InGameState.NORMAL;
    			}
    		}
    		break;
    	case WARNING:
    		warningTime++;
    		if (warningTime == WARNING_DONE_TIME){
    			warningTime = 0;
    			inGameState = InGameState.DONE;
    		}
    		return;
		default:
			break;	
    	}
    	updateTextMessages();
    	removeDead();
    	if (gameOver()){
    		inGameState = InGameState.DONE;
    		if (this.isTutorial){
    		// @ishaan shouldn't you only be doing this if your in a tutorial level
    		// we shouldnt be checking this everytime. i'm getting a null pointer from this stuff
	    		
    			if (!TutorialSteps.levelName.equals("") && leftsideDead()){
	    			GameEngine.nextLevel = TutorialSteps.levelName;
	    			TutorialSteps.setWarning((TutorialSteps.wrongText.equals("")? "Try again!" : TutorialSteps.wrongText), false);
	    			System.out.println("TEST TEST TEST TEST TEST TEST2");
	    			inGameState = InGameState.WARNING;
	    		} else {
	    			TutorialSteps.setWarning((TutorialSteps.rightText.equals("")? "Well Done!" : TutorialSteps.rightText), true);
	    			inGameState = InGameState.WARNING;
	    		}
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
    	if (inGameState == InGameState.WARNING) {
    		TutorialSteps.drawWarningText(canvas);
    		return;
    	}
        screen.draw(canvas);
    	board.draw(canvas);
    	drawCharacters(canvas);
        animations.draw(canvas,board,inGameState);
        
        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        //screen should be drawn after greyed out characters
        //but before selected characters
    }
    
    
    //Change how i do this.
    //This needs to be done so characters below show over characters above and selection menu
    //shows over characters.
    protected void drawCharacters(GameCanvas canvas){
    	boolean inSelection = inGameState == InGameState.SELECTION;
		boolean shouldDim = inGameState == InGameState.SELECTION || 
				mouseOverController.isCharacterHighlighted();
    	characters.draw(canvas,shouldDim, inSelection);
    	for (int i = board.height-1; i >= 0; i--){
    		for (Character c : characters){
    			
    			if (c.yPosition == i && c.isAlive()){
    				c.draw(canvas,board,shouldDim,this.inGameState);
    			}
    			if (c.getShadowY() == i && c.needShadow() && c.isAlive()){
    				c.drawShadowCharacter(canvas,board,this.inGameState);
    			}
            }
    	}
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
    
    public boolean gameOver(){
    	return leftsideDead() || rightsideDead();
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