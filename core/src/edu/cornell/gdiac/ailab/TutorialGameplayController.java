package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.TutorialSteps.CurrentHighlight;

public class TutorialGameplayController extends GameplayController{
	TutorialSteps tutorialSteps;
	boolean curPaused;
	
	/** State when tutorial is not paused */
	InGameState regGameState;
	
	/** Subcontroller for selection menu (CONTROLLER CLASS) */
    protected TutorialSelectionMenuController selectionMenuController;
    
    /** Subcontroller for AI selection (CONTROLLER CLASS) */
    protected TutorialAIController aiController;

	public TutorialGameplayController(MouseOverController moc, FileHandle file, int fileNum) {
		super(moc, file, fileNum);
		// TODO Auto-generated constructor stub
	}

	public void resetGame(Level level){
		super.resetGame(level);
		tutorialSteps = level.getTutorialSteps();
		selectionMenuController = new TutorialSelectionMenuController(board,characters,tutorialSteps);
        aiController = new TutorialAIController(board,characters,tutorialSteps);
		curPaused = false;
		// update the character models
		characters.update();
		effectController.update();
		actionBarController.update();
		persistingController.update();
		mouseOverController.update(selectionMenuController.getMenu(),characters);
	}
	
	public void update(){
		if (tutorialSteps.isDone()){
			super.update();
			return;
		}
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
    		// update the character models
    		characters.update();
    		effectController.update();
    		actionBarController.update();
    		persistingController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		if (actionBarController.isAISelection) {
    			tutorialSteps.nextStep();
    			aiController.update();
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			tutorialSteps.nextStep();
    			selectionMenuController.update();
    			inGameState = InGameState.SELECTION;
    		}
    		//updateTutorial();
    		break;
    	case SELECTION:
    		screen.setJustScreen();
    		mouseOverController.clearAll();
    		selectionMenuController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		prompt = "Choose an Action";
    		selectionMenuController.setPrompt(prompt);
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			prompt = null;
    			board.reset();
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
    		//updateTutorial();
    		break;
    	case PAUSED:
    		if (regGameState == InGameState.SELECTION){
            	screen.setJustScreen();
            }
    		//updateTutorial();
    		break;
		default:
			//updateTutorial();
			break;	
    	}
    	updateTutorial();
    	updateTextMessages();
    	removeDead();
    	if (gameOver()){
    		inGameState = InGameState.DONE;
    		if(GameEngine.dataGen){
    			dataFile.writeString(jsonArray.toString(), false);
        		fileNumFile.writeString(""+fileNum, false);
    		}
    		ObjectLoader.getInstance().unloadCurrentLevel();
    	}
    }
	
	public boolean isDone(){
		return tutorialSteps.finishGame && tutorialSteps.isDone() && super.isDone();
	}
	
	public void drawPlay(GameCanvas canvas){
		List<CurrentHighlight> highlights = tutorialSteps.getHighlights();
		if (highlights == null){
			screen.setJustScreen();
		} else {
	    	for (CurrentHighlight highlight:highlights){
	    		screen.addCurrentHighlight(highlight.xPos*canvas.getWidth(), highlight.yPos*canvas.getHeight(), 
	    				highlight.width*canvas.getWidth(), highlight.height*canvas.getHeight());
//	    		System.out.println(highlight.xPos*canvas.getWidth());
//	    		System.out.println(highlight.yPos*canvas.getHeight());
//	    		System.out.println(highlight.width*canvas.getWidth());
//	    		System.out.println(highlight.height*canvas.getHeight());	
	    	}
	    	screen.noScreen();	
		}
        screen.draw(canvas);
    	board.draw(canvas);
    	drawCharacters(canvas);
        animations.draw(canvas,board);
        
        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        //screen should be drawn after greyed out characters
        //but before selected characters
        tutorialSteps.drawText(canvas);
        //screen should be drawn after greyed out characters
        //but before selected characters
    }
	
	private void updateTutorial() {
		if (InputController.pressedSpace() && tutorialSteps.isPaused()){
			tutorialSteps.nextStep();
		}
		if (tutorialSteps.isPaused()){
			if (!curPaused){
				curPaused = true;
				regGameState = inGameState;
			}
			inGameState = InGameState.PAUSED;
		} else {
			if (curPaused){
				curPaused = false;
				inGameState = regGameState;
			}
		}
		
	}
	
	
	
}
