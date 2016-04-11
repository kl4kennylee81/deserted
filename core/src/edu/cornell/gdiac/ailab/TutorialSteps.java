package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.ActionNodes.Direction;

/**
 * TutorialSteps Class
 */
public class TutorialSteps {
	/** List of all Steps */
	List<Step> steps;
	/** Current Step */
	int curStep;
	/** Step object */
	Step step;
	/** finish game or continue to next level */
	boolean finishGame;
	int textDone;
//	boolean incrTextDone;
	boolean showHighlights;
	int timeElapsed;
	int writeTime;
	boolean startTime;
	String warning;
	int warningTime;
	Color levelColor;
	
	/** Individual Step*/
	class Step{
		String text;
		boolean paused;
		List<TutorialAction> actions;
		List<CurrentHighlight> highlights;
		boolean confirm;
		int waitTime;
		boolean spaceToContinue;
		boolean dontWriteText;
		int timeToPause;
		
		public Step (String text, boolean paused, boolean confirm, int waitTime, 
				boolean spaceToContinue, boolean dontWriteText, int timeToPause){
			this.text = text;
			this.paused = paused;
			this.confirm = confirm;
			this.actions = null;
			this.highlights = null;
			this.waitTime = waitTime;
			this.spaceToContinue = spaceToContinue;
			this.dontWriteText = dontWriteText;
			this.timeToPause = timeToPause;
		}
		
	}
	
	public class TutorialAction{
		int actionId;
		int xPos;
		int yPos;
		Direction direction;
		
		public TutorialAction (int actionId, int xPos, int yPos, String direction){
			this.actionId = actionId;
			this.xPos = xPos;
			this.yPos = yPos;
			this.direction = Direction.valueOf(direction);
		}
	}
	
	public class CurrentHighlight{
		double xPos;
		double yPos;
		double width;
		double height;
		String arrow;
		
		public CurrentHighlight (double xPos, double yPos, double width, double height, String arrow){
			this.xPos = xPos;
			this.yPos = yPos;
			this.width = width;
			this.height = height;
			this.arrow = arrow;
		}
	}
	
	public TutorialSteps(){
		steps = new ArrayList<Step>();
		curStep = 0;
		finishGame = false;
		this.textDone = 0;
//		this.incrTextDone = true;
		this.showHighlights = false;
		this.timeElapsed = 0;
		this.writeTime = 0;
		this.warning = "";
		this.warningTime = 0;
	}
	
	public void addStep(String text, boolean paused, boolean confirm, int waitTime, 
			boolean spaceToContinue, boolean dontWriteText, int timeToPause){
		Step newStep = new Step(text, paused, confirm, waitTime, spaceToContinue, dontWriteText, timeToPause);
		steps.add(newStep);
		if (steps.size() == 1){
			step = newStep;
		}
	}
	
	public void addAction(int actionId, int xPos, int yPos, String direction){
		TutorialAction ta = new TutorialAction(actionId,xPos,yPos,direction);
		Step latestStep = steps.get(steps.size()-1);
		
		if (latestStep.actions == null){
			latestStep.actions = new ArrayList<TutorialAction>();
		}
		latestStep.actions.add(ta);
	}
	
	public void addHighlight(double xPos, double yPos, double width, double height, String arrow){
		CurrentHighlight ch = new CurrentHighlight(xPos, yPos, width, height, arrow);
		Step latestStep = steps.get(steps.size()-1);
		
		if (latestStep.highlights == null){
			latestStep.highlights = new ArrayList<CurrentHighlight>();
		}
		latestStep.highlights.add(ch);		
	}
	
	public void setFinishGame(boolean finishGame){
		this.finishGame = finishGame;
	}
	
	public void setLevelColor(Color levelColor){
		this.levelColor = levelColor;
	}
	
	public void nextStep(){
		startTime = false;
		timeElapsed = 0;
		writeTime = 0;
		showHighlights = false;
		textDone = 0;
		curStep += 1;
		if (!isDone()){
			step = steps.get(curStep);
		} else {
			step = null;
		}
	}
	
	public boolean isDone(){
		return curStep >= steps.size();
	}
	
	public boolean isPaused(){
		return step != null && step.paused;
	}
	
	public boolean needsAction(){
		return step != null && step.actions != null;
	}
	
	public List<TutorialAction> getActions(){
		return step.actions;
	}
	
	public List<CurrentHighlight> getHighlights(){
		if (step != null) return step.highlights;
		else return null;
	}
	
	public boolean needsConfirm(){
		return step != null && step.confirm;
	}
	
	public void drawText(GameCanvas canvas){
		if (step!= null && !step.dontWriteText){
			if (writeTime % 4 == 0){
				if (textDone < step.text.length()){
					textDone++;
				} else{
					showHighlights = true;
					startTime = true;
				}
//				incrTextDone = false;
			}
		} else {
//			incrTextDone = true;
			startTime = true;
		}
		String toWrite = "";
		if (step != null && step.text != null){
			if (step.dontWriteText){
				toWrite = step.text;//canvas.drawTutorialText(step.text, Color.WHITE);
				showHighlights = true;
			} else {
				toWrite = step.text.substring(0, textDone);//canvas.drawTutorialText(step.text.substring(0, textDone), Color.WHITE);
			}
		}
		if (step != null && timeElapsed == step.waitTime && step.spaceToContinue){
			//canvas.drawTutorialText("\n\n\nPress Spacebar to Continue", Color.WHITE);
			toWrite +=  "\n\nPress Spacebar to Continue";
		}
		canvas.drawTutorialText(toWrite, levelColor == null? Color.WHITE: levelColor);
		if (!warning.equals("")){
			canvas.drawWarningText(warning);
			warningTime++;
			if (warningTime == 40){
				warningTime = 0;
				warning = "";
			}
		}
	}
	
	public Step currStep(){
		return step;
	}

	public void setWarning(String warning) {
		this.warning = warning;
		warningTime = 0;
		
	}
	
}
