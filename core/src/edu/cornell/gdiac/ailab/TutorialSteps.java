package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

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
	String nextLevel;
	String levelName;
	String rightText;
	String wrongText;
	int prevTextDone;
	int textDone;
	boolean showHighlights;
	int timeElapsed;
	int writeTime;
	boolean startTime;
	String warning;
	boolean warningGreen;
	int warningTime;
	Color levelColor;
	boolean stepOnSelection;

	/** Individual Step */
	class Step {
		String text;
		boolean paused;
		List<TutorialAction> actions;
		List<CurrentHighlight> highlights;
		boolean confirm;
		boolean spaceToContinue;
		boolean dontWriteText;
		int timeToPause;

		public Step(String text, boolean paused, boolean confirm, boolean spaceToContinue, boolean dontWriteText,
				int timeToPause) {
			this.text = text;
			this.paused = paused;
			this.confirm = confirm;
			this.actions = null;
			this.highlights = null;
			this.spaceToContinue = spaceToContinue;
			this.dontWriteText = dontWriteText;
			this.timeToPause = timeToPause;
		}

	}

	public class TutorialAction {
		int actionId;
		int xPos;
		int yPos;
		Direction direction;

		public TutorialAction(int actionId, int xPos, int yPos, String direction) {
			this.actionId = actionId;
			this.xPos = xPos;
			this.yPos = yPos;
			this.direction = Direction.valueOf(direction);
		}
	}

	public class CurrentHighlight {
		double xPos;
		double yPos;
		double width;
		double height;
		String arrow;

		public CurrentHighlight(double xPos, double yPos, double width, double height, String arrow) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.width = width;
			this.height = height;
			this.arrow = arrow;
		}
	}

	public TutorialSteps() {
		steps = new ArrayList<Step>();
		curStep = 0;
		finishGame = false;
		stepOnSelection = false;
		nextLevel = "";
		levelName = "";
		rightText = "";
		wrongText = "";
		this.textDone = 0;
		this.prevTextDone = 0;
		this.showHighlights = false;
		this.timeElapsed = 0;
		this.writeTime = 0;
		this.warning = "";
		this.warningTime = 0;
		this.warningGreen = true;
	}

	public void addStep(String text, boolean paused, boolean confirm, boolean spaceToContinue, boolean dontWriteText,
			int timeToPause) {
		Step newStep = new Step(text, paused, confirm, spaceToContinue, dontWriteText, timeToPause);
		steps.add(newStep);
		if (steps.size() == 1) {
			step = newStep;
		}
	}

	public void addAction(int actionId, int xPos, int yPos, String direction) {
		TutorialAction ta = new TutorialAction(actionId, xPos, yPos, direction);
		Step latestStep = steps.get(steps.size() - 1);

		if (latestStep.actions == null) {
			latestStep.actions = new ArrayList<TutorialAction>();
		}
		latestStep.actions.add(ta);
	}

	public void anyAction() {
		Step latestStep = steps.get(steps.size() - 1);

		if (latestStep.actions == null) {
			latestStep.actions = new ArrayList<TutorialAction>();
		}
	}

	public void addHighlight(double xPos, double yPos, double width, double height, String arrow) {
		CurrentHighlight ch = new CurrentHighlight(xPos, yPos, width, height, arrow);
		Step latestStep = steps.get(steps.size() - 1);

		if (latestStep.highlights == null) {
			latestStep.highlights = new ArrayList<CurrentHighlight>();
		}
		latestStep.highlights.add(ch);
	}

	public void setFinishGame(boolean finishGame) {
		this.finishGame = finishGame;
	}
	
	public void setNextLevel(String nextLevel) {
		this.nextLevel = nextLevel;
	}
	
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public void setLevelColor(Color levelColor) {
		this.levelColor = levelColor;
	}

	public void nextStep() {
		startTime = false;
		timeElapsed = 0;
		writeTime = 0;
		showHighlights = false;
		textDone = 0;
		prevTextDone = 0;
		curStep += 1;
		if (!isDone()) {
			step = steps.get(curStep);
		} else {
			step = null;
		}
	}
	
	public void prevStep() {
		startTime = false;
		timeElapsed = 0;
		writeTime = 0;
		showHighlights = false;
		textDone = 0;
		prevTextDone = 0;
		curStep -= 1;
		if (!isDone()) {
			step = steps.get(curStep);
		} else {
			step = null;
		}
	}

	public boolean isDone() {
		System.out.println("curStep: " + curStep + "steps size: " + steps.size());
		return curStep >= steps.size();
	}

	public boolean isPaused() {
		return step != null && step.paused;
	}

	public boolean needsAction() {
		return step != null && step.actions != null;
	}

	public List<TutorialAction> getActions() {
		return step.actions;
	}

	public List<CurrentHighlight> getHighlights() {
		if (step != null)
			return step.highlights;
		else
			return null;
	}

	public boolean needsConfirm() {
		return step != null && step.confirm;
	}

	public void drawText(GameCanvas canvas) {
		if (step != null && !step.dontWriteText) {
			if (writeTime % 4 == 0) {
				if (textDone < step.text.length()) {
					if (step.text.charAt(textDone) != '\n') {
						textDone++;
					}
				} else {
					showHighlights = true;
					startTime = true;
				}
			}
		} else {
			startTime = true;
		}
		String toWrite = "";
		if (step != null && step.text != null) {
			if (step.dontWriteText) {
				toWrite = step.text;// canvas.drawTutorialText(step.text,
									// Color.WHITE);
				showHighlights = true;
			} else {
				toWrite = step.text.substring(prevTextDone, textDone);// canvas.drawTutorialText(step.text.substring(0,
																		// textDone),
																		// Color.WHITE);
			}
		}
		if (step != null && curStep == 0 && step.spaceToContinue
				&& (textDone == step.text.length() || step.text.charAt(textDone) == '\n')) {
			// canvas.drawTutorialText("\n\n\nPress Spacebar to Continue",
			// Color.WHITE);
			toWrite += "\n\nPress Spacebar or Click anywhere to continue";
		}
		if (step != null) {
			canvas.drawTutorialText(toWrite, levelColor == null ? Color.WHITE : levelColor,
					step.text.length() > 10 ? Align.left : Align.center);
		} else {
			canvas.drawTutorialText(toWrite, levelColor == null ? Color.WHITE : levelColor, Align.left);
		}

		if (!warning.equals("")) {
			canvas.drawWarningText(warning, warningGreen);
			warningTime++;
			if (warningTime == GameplayController.WARNING_DONE_TIME) {
				warningTime = 0;
				warning = "";
			}
		}
	}

	public Step currStep() {
		return step;
	}

	public void setWarning(String warning, boolean green) {
		this.warning = warning;
		warningTime = 0;
		warningGreen = green;

	}

	public void setRightText(String rightText) {
		this.rightText = rightText;
	}

	public void setWrongText(String wrongText) {
		this.wrongText = wrongText;
	}

	public void setStepOnSelection(boolean stepOnSelection) {
		this.stepOnSelection = stepOnSelection;
		
	}

}
