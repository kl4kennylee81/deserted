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
	
	/** Individual Step*/
	private class Step{
		String text;
		boolean paused;
		List<TutorialAction> actions;
		
		public Step (String text, boolean paused){
			this.text = text;
			this.paused = paused;
			this.actions = null;
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
	
	public TutorialSteps(){
		steps = new ArrayList<Step>();
		curStep = 0;
	}
	
	public void addStep(String text, boolean paused, List<TutorialAction> actions){
		Step newStep = new Step(text, paused);
		steps.add(newStep);
		if (steps.size() == 1){
			step = newStep;
		}
	}
	
	public void addStep(String text, boolean paused){
		Step newStep = new Step(text, paused);
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
		return step.actions != null;
	}
	
	public List<TutorialAction> getActions(){
		return step.actions;
	}
	
	public void drawText(GameCanvas canvas){
		if (step != null && step.text != null){
			canvas.drawText(step.text, 80, 660, Color.WHITE);
		}
	}
	
}
