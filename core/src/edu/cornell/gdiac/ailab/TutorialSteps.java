package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import edu.cornell.gdiac.ailab.ActionNode.Direction;

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
	public static String nextLevel;
	public static String levelName;
	public static String rightText;
	public static String wrongText;
	int prevTextDone;
	int textDone;
	boolean showHighlights;
	int timeElapsed;
	int writeTime;
	boolean startTime;
	static String warning;
	static boolean warningGreen;
	static int warningTime;
	Color levelColor;
	boolean stepOnSelection;

	/** Individual Step */
	class Step {
		String text;
		boolean paused;
		List<TutorialAction> actions;
		List<CurrentHighlight> highlights;
		List<String> highlightChars;
		List<String> highlightTokens;
		boolean confirm;
		boolean spaceToContinue;
		boolean dontWriteText;
		int timeToPause;
		boolean ignoreTextDone;

		public Step(String text, boolean paused, boolean confirm, boolean spaceToContinue, boolean dontWriteText,
				int timeToPause, boolean ignoreTextDone) {
			this.text = text;
			this.paused = paused;
			this.confirm = confirm;
			this.actions = null;
			this.highlights = null;
			this.highlightChars = null;
			this.highlightTokens = null;
			this.spaceToContinue = spaceToContinue;
			this.dontWriteText = dontWriteText;
			this.timeToPause = timeToPause;
			this.ignoreTextDone = ignoreTextDone;
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
		warning = "";
		warningTime = 0;
		warningGreen = true;
	}

	public void addStep(String text, boolean paused, boolean confirm, boolean spaceToContinue, boolean dontWriteText,
			int timeToPause, boolean ignoreTextDone) {
		Step newStep = new Step(text, paused, confirm, spaceToContinue, dontWriteText, timeToPause, ignoreTextDone);
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

	public void addHighlight(double xPos, double yPos, double width, double height, String arrow, boolean isChar, boolean isSquare) {
		CurrentHighlight ch = new CurrentHighlight(xPos, yPos, width, height, arrow, isChar, isSquare);
		Step latestStep = steps.get(steps.size() - 1);

		if (latestStep.highlights == null) {
			latestStep.highlights = new ArrayList<CurrentHighlight>();
		}
		latestStep.highlights.add(ch);
	}
	
	public void addHighlightChars(List<String> highlightChars){
		Step latestStep = steps.get(steps.size()-1);
		latestStep.highlightChars = new ArrayList<String>(highlightChars);
	}
	
	public void addHighlightTokens(List<String> highlightTokens){
		Step latestStep = steps.get(steps.size()-1);
		latestStep.highlightTokens = new ArrayList<String>(highlightTokens);
	}

	public void setFinishGame(boolean finishGame) {
		this.finishGame = finishGame;
	}
	
	public void setNextLevel(String nextLevel) {
		TutorialSteps.nextLevel = nextLevel;
	}
	
	public void setLevelName(String levelName) {
		TutorialSteps.levelName = levelName;
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
		return curStep >= steps.size();
	}

	public boolean isPaused() {
		return step != null && step.paused;
	}

	public boolean needsAction() {
		return step != null && step.actions != null;
	}

	public List<TutorialAction> getActions() {
		if(step == null){
			return new ArrayList<TutorialAction>();
		}
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
			toWrite += "\n\nPress Enter or Click anywhere to continue";
		}
		if (step != null) {
			canvas.drawTutorialText(toWrite, levelColor == null ? Color.WHITE : levelColor,
					step.text.length() > 10 ? Align.left : Align.center);
		} else {
			canvas.drawTutorialText(toWrite, levelColor == null ? Color.WHITE : levelColor, Align.left);
		}

		drawWarningText(canvas);
	}
	
	public static void drawWarningText(GameCanvas canvas){
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

	public static void setWarning(String warning, boolean green) {
		TutorialSteps.warning = warning;
		warningTime = 0;
		warningGreen = green;

	}

	public void setRightText(String rightText) {
		TutorialSteps.rightText = rightText;
	}

	public void setWrongText(String wrongText) {
		TutorialSteps.wrongText = wrongText;
	}

	public void setStepOnSelection(boolean stepOnSelection) {
		this.stepOnSelection = stepOnSelection;
		
	}

}
