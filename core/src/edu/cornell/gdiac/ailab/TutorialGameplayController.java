package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.GameplayController.InGameState;
import edu.cornell.gdiac.ailab.TutorialSteps.CurrentHighlight;

public class TutorialGameplayController extends GameplayController{
	TutorialSteps tutorialSteps;
	boolean curPaused;
	public static int pauseTimer;
	public static int targetPauseTime;


	/** State when tutorial is not paused */
	InGameState regGameState;

    /** Subcontroller for AI selection (CONTROLLER CLASS) */
    protected TutorialAIController aiController;

    public static int highlight_action = 0;

	public TutorialGameplayController(MouseOverController moc, FileHandle file, int fileNum) {
		super(moc, file, fileNum, true);
		// TODO Auto-generated constructor stub
	}

	public void resetGame(Level level){
		super.resetGame(level);
		tutorialSteps = level.getTutorialSteps();
		this.selectionMenuController = new TutorialSelectionMenuController(board,characters,tutorialSteps);
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
        GameEngine.nextLevel = TutorialSteps.nextLevel;
	}

	public void update(){
		tutorialSteps.writeTime += 2;
		if (tutorialSteps.currStep() != null){
			if (tutorialSteps.startTime){
				tutorialSteps.timeElapsed += 1;
			}
		}
		if (tutorialSteps.isDone()){
			//if problem persists, have conditions for if SELECTION mode
			super.update();
			return;
		}
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
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
    			if (tutorialSteps.currStep().actions.size() == 0) super.aiController.update();
    			else aiController.update();
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			//System.out.println("next step 2");
    			tutorialSteps.nextStep();
    			if (tutorialSteps.currStep() != null) targetPauseTime = tutorialSteps.currStep().timeToPause;
    			pauseTimer = 0;
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
    		prompt = "";//prompt = "Choose an Action";
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
    	case WARNING:
    		if (InputController.pressedEnter()){
    			inGameState = InGameState.DONE;
	    		if (this.leftsideDead()){
	    			GameEngine.nextLevel = TutorialSteps.levelName;
	    		}
	    		else if (this.rightsideDead()){
	    			GameEngine.nextLevel = TutorialSteps.nextLevel;
	    		}
    			
    		}
    		return;
		default:
			//updateTutorial();
			break;
    	}
    	updateTutorial();
    	updateTextMessages();
    	removeDead();
    	if (gameOver()){
    		// you are set to done state after the warning time has elapsed
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

	public boolean isDone(){
		boolean endNow = false;//take this from the yaml file eventually
		return (tutorialSteps.finishGame && tutorialSteps.isDone() && inGameState != InGameState.PAUSED || super.isDone()) || endNow;
	}

	public void drawPlay(GameCanvas canvas){

    	// temporary hacky code to show that you have won without destroying the canvas
    	// will definately need to rewrite this portion
		if (this.gameOver() && inGameState == InGameState.WARNING){
			super.drawPlay(canvas);
			return;
		}
    	else if (this.isTutorial && inGameState == InGameState.WARNING) {
    		TutorialSteps.drawWarningText(canvas);
    		return;
    	}
		if (isDone()){
			return;
		}
		List<CurrentHighlight> highlights = tutorialSteps.getHighlights();
		if(tutorialSteps.step == null || !tutorialSteps.step.text.equals("")) screen.setJustScreen();
		if (highlights != null && tutorialSteps.showHighlights){
	    	for (CurrentHighlight highlight:highlights){
	    		if (!highlight.isChar) {
	    			screen.addCurrentHighlight(highlight.xPos*canvas.getWidth(), highlight.yPos*canvas.getHeight(),
	    					highlight.width*canvas.getWidth(), highlight.height*canvas.getHeight());
	    		} else {
	    			screen.addCurrentHighlight(highlight.xPos*canvas.getWidth(), highlight.yPos*canvas.getHeight(),
	    					highlight.width*canvas.getWidth(), highlight.height*canvas.getHeight(), canvas, board);
	    		}
	    	}
	    	screen.noScreen();
		}
        screen.draw(canvas);
    	board.draw(canvas);
    	drawCharacters(canvas);
        animations.draw(canvas,board,inGameState);
		if (highlight_action > 0){
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
    			float highlightX = selectedChar.actionBar.getBarCastPoint(canvas) + (highlight_action)*selectedChar.actionBar.getSlotWidth(canvas);
    			float highlightY = selectedChar.actionBar.getY(canvas, count) - selectedChar.actionBar.getBarHeight(canvas);//characters.indexOf(selectedChar));
    			canvas.drawDownTextArrow(highlightX, highlightY, Color.GOLD, "This is where this move will go off");
    		}
			//getY: iterate over characters, and when character matches selected character thats the number to pass to getY
		}

		// draw the description box
    	this.drawDescriptionBox(canvas);

        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        if (highlights != null && tutorialSteps.showHighlights){
	    	for (CurrentHighlight highlight:highlights){
	    		if (highlight.isChar){
	    			canvas.drawCharArrow(screen.screen, 
	    					(float)highlight.xPos*canvas.getWidth(),
	    					(float)highlight.width*canvas.getWidth(), 
	    					(float)highlight.yPos*canvas.getHeight(),
	    					(float)highlight.height*canvas.getHeight(),
	    					Color.GOLD, board);
	    			continue;
	    		}
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
		pauseTimer++;
		if (targetPauseTime != -1) {
//			System.out.println("target pause time is " + targetPauseTime);
//			System.out.println("pause timer is " + pauseTimer);
		}
		if (pauseTimer == targetPauseTime){
//			System.out.println("pause timer reached");
			if (!curPaused){
				curPaused = true;
				regGameState = inGameState;
			}
//			inGameState = InGameState.PAUSED;
			//System.out.println("reality");
			//System.out.println(targetPauseTime);
			//System.out.println("next step 3");
			tutorialSteps.nextStep();
			pauseTimer = 0;
			if (tutorialSteps.currStep() != null) targetPauseTime = tutorialSteps.currStep().timeToPause;
			return;
		}
		if ((InputController.pressedEnter() || InputController.pressedLeftMouse()) && inGameState == InGameState.PAUSED){
			if (tutorialSteps.textDone == tutorialSteps.step.text.length()){
				//System.out.println("next step 4");
				tutorialSteps.nextStep();
				if (tutorialSteps.currStep() != null) targetPauseTime = tutorialSteps.currStep().timeToPause;
				pauseTimer = 0;

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
	    canvas.drawTutorialText("“Congratulations on completing this Tutorial segment. "
	    		+ "\n\nPress \'R\' to return to the main menu where you can "
	    		+ "select another level \n\nor Press Enter to move on to the next Tutorial segment", Color.BLACK, Align.center);
    }
}

//TODO bugs: multiple highlihgts when resizing
//TODO: spacebar to continue slightly flawed
