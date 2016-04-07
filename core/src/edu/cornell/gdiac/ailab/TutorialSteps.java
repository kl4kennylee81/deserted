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
	
	/** Individual Step*/
	private class Step{
		String text;
		boolean paused;
		List<TutorialAction> actions;
		List<CurrentHighlight> highlights;
		boolean confirm;
		
		public Step (String text, boolean paused, boolean confirm){
			this.text = text;
			this.paused = paused;
			this.confirm = confirm;
			this.actions = null;
			this.highlights = null;
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
		
		public CurrentHighlight (double xPos, double yPos, double width, double height){
			this.xPos = xPos;
			this.yPos = yPos;
			this.width = width;
			this.height = height;
		}
	}
	
	public TutorialSteps(){
		steps = new ArrayList<Step>();
		curStep = 0;
		finishGame = false;
	}
	
	public void addStep(String text, boolean paused, boolean confirm){
		Step newStep = new Step(text, paused, confirm);
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
	
	public void addHighlight(double xPos, double yPos, double width, double height){
		CurrentHighlight ch = new CurrentHighlight(xPos, yPos, width, height);
		Step latestStep = steps.get(steps.size()-1);
		
		if (latestStep.highlights == null){
			latestStep.highlights = new ArrayList<CurrentHighlight>();
		}
		latestStep.highlights.add(ch);		
	}
	
	public void setFinishGame(boolean finishGame){
		this.finishGame = finishGame;
	}
	
	public void nextStep(){
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
		return step.highlights;
	}
	
	public boolean needsConfirm(){
		return step != null && step.confirm;
	}
	
	public void drawText(GameCanvas canvas){
		if (step != null && step.text != null){
			canvas.drawTutorialText(step.text, Color.WHITE);
		}
	}
	
}
