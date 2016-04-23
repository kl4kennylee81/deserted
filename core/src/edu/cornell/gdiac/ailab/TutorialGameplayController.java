package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.TutorialSteps.CurrentHighlight;

public class TutorialGameplayController extends GameplayController{
	TutorialSteps tutorialSteps;
	boolean curPaused;
	int pauseTimer;
	int targetPauseTime;

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
		effectController.update(characters, board);
		actionBarController.update();
		persistingController.update();
		mouseOverController.update(selectionMenuController.getMenu(),characters);
		pauseTimer = 0;
		targetPauseTime = -1;
        GameEngine.nextLevel = tutorialSteps.nextLevel;
	}

	public void update(){
		System.out.println("heyooo");
		tutorialSteps.writeTime += 2;
		if (tutorialSteps.currStep() != null){
			if (tutorialSteps.startTime){
				tutorialSteps.timeElapsed += 1;
			}
		}
		System.out.println(tutorialSteps.curStep);
		if (tutorialSteps.isDone()){
			System.out.println("yoyoyoyoyo");
			if (gameOver() && !tutorialSteps.levelName.equals("") && leftsideDead()){
    			GameEngine.nextLevel = tutorialSteps.levelName;
    			tutorialSteps.setWarning("Your enemy is quicker than you! Dodge him!");
    		}//code duplication :(
			super.update();
			return;
		}
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
    		System.out.println("nononononon");
    		// update the character models
    		characters.update();
    		effectController.update(characters, board);
    		actionBarController.update();
    		persistingController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		if (actionBarController.isAISelection) {
    			pauseTimer = 0;
    			targetPauseTime = tutorialSteps.currStep().timeToPause;
    			//System.out.println("next step 1");
    			//tutorialSteps.nextStep();
//    			tutorialSteps.nextStep();
    			if (!curPaused){
    				curPaused = true;
    				regGameState = inGameState;
    			}
    			inGameState = InGameState.PAUSED;
    			//System.out.println(inGameState);
    			aiController.update();
    			System.out.println(tutorialSteps.curStep);
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			pauseTimer = 0;
    			targetPauseTime = tutorialSteps.currStep().timeToPause;
    			//System.out.println("next step 2");
    			tutorialSteps.nextStep();
    			selectionMenuController.update();
    			inGameState = InGameState.SELECTION;
    		}
    		//updateTutorial();
    		break;
    	case SELECTION:
    		System.out.println("zeusss");
    		screen.setJustScreen();
    		mouseOverController.clearAll();
    		selectionMenuController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		prompt = "";//prompt = "Choose an Action";
    		selectionMenuController.setPrompt(prompt);
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			prompt = null;
    			board.reset();
    		}
    		break;
    	case ATTACK:
    		System.out.println("kickball");
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
    		System.out.println("austin");
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
    		System.out.println("godzilla");
    		inGameState = InGameState.DONE;
    		if (!tutorialSteps.levelName.equals("") && leftsideDead()){
    			GameEngine.nextLevel = tutorialSteps.levelName;
    			System.out.println("stfu");
    		}
    		if(GameEngine.dataGen){
    			dataFile.writeString(jsonArray.toString(), false);
        		fileNumFile.writeString(""+fileNum, false);
    		}
    		ObjectLoader.getInstance().unloadCurrentLevel();
    	}
    }

	public boolean isDone(){
		return tutorialSteps.finishGame && tutorialSteps.isDone() && inGameState != InGameState.PAUSED || super.isDone();
	}

	public void drawPlay(GameCanvas canvas){
		if (isDone()){
			return;
		}
		List<CurrentHighlight> highlights = tutorialSteps.getHighlights();
		if(tutorialSteps.step == null || !tutorialSteps.step.text.equals("")) screen.setJustScreen();
		if (highlights != null && tutorialSteps.showHighlights){
	    	for (CurrentHighlight highlight:highlights){
	    		screen.addCurrentHighlight(highlight.xPos*canvas.getWidth(), highlight.yPos*canvas.getHeight(),
	    				highlight.width*canvas.getWidth(), highlight.height*canvas.getHeight());
	    	}
	    	screen.noScreen();
		}
        screen.draw(canvas);
    	board.draw(canvas);
    	drawCharacters(canvas);
        animations.draw(canvas,board,inGameState);

        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        if (highlights != null && tutorialSteps.showHighlights){
	    	for (CurrentHighlight highlight:highlights){
	    		if (highlight.arrow.equals("up")){
		    		canvas.drawUpArrow((float)(highlight.xPos*canvas.getWidth() + (highlight.width*canvas.getWidth())/2f),
		    				(float)highlight.yPos*canvas.getHeight(),
		    				Color.GOLD);

	    		} else if (highlight.arrow.equals("down")) {
		    		canvas.drawDownArrow((float)(highlight.xPos*canvas.getWidth() + highlight.width*canvas.getWidth()),
		    				(float)(highlight.yPos*canvas.getHeight() + (highlight.height*canvas.getHeight())/2f),
		    				Color.GOLD);
	    		} else {
		    		canvas.drawLeftArrow((float)(highlight.xPos*canvas.getWidth() + highlight.width*canvas.getWidth()),
		    				(float)(highlight.yPos*canvas.getHeight() + (highlight.height*canvas.getHeight())/2f),
		    				Color.GOLD);
	    		}
	    	}
		}
        tutorialSteps.drawText(canvas);
    }

	private void updateTutorial() {
		System.out.println("stuff is japening");
		pauseTimer++;
		if (targetPauseTime != -1) {
			//System.out.println(targetPauseTime);
			//System.out.println(pauseTimer);
		}
		if (pauseTimer == targetPauseTime){
			pauseTimer = 0;
			if (!curPaused){
				curPaused = true;
				regGameState = inGameState;
			}
			inGameState = InGameState.PAUSED;
			//System.out.println("reality");
			targetPauseTime = tutorialSteps.currStep().timeToPause;
			//System.out.println(targetPauseTime);
			//System.out.println("next step 3");
			tutorialSteps.nextStep();
			return;
		}
		if ((InputController.pressedSpace() || InputController.pressedLeftMouse()) && inGameState == InGameState.PAUSED){
			if (tutorialSteps.textDone == tutorialSteps.step.text.length()){
				pauseTimer = 0;
				targetPauseTime = tutorialSteps.currStep().timeToPause;
				//System.out.println("next step 4");
				tutorialSteps.nextStep();

			} else {
				if (tutorialSteps.step.text.charAt(tutorialSteps.textDone) == '\n'){
					tutorialSteps.prevTextDone = tutorialSteps.textDone;
					tutorialSteps.textDone++;
				} else {
					int pt = tutorialSteps.step.text.indexOf('\n', tutorialSteps.prevTextDone);
					int t = tutorialSteps.step.text.indexOf('\n', tutorialSteps.textDone);
//					System.out.println("prevTextDone:"+tutorialSteps.prevTextDone + " textDone:" + tutorialSteps.textDone + " pt:" + pt + " t:" + t);
					if (pt != -1) tutorialSteps.prevTextDone = pt == t? tutorialSteps.prevTextDone: pt;
					tutorialSteps.textDone = t == -1? tutorialSteps.step.text.length(): t;
				}
			}
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

    public void drawAfter(GameCanvas canvas){
	    canvas.drawTutorialText("“Congratulations on completing Tutorial Level 1. "
	    		+ "\n\nPress \'R\' to return to the main menu where you can "
	    		+ "select another level \n\nor press Spacebar to move on to Tutorial Level 2", Color.BLACK, Align.center);
    }
}

//TODO bugs: multiple highlihgts when resizing
//TODO: spacebar to continue slightly flawed
