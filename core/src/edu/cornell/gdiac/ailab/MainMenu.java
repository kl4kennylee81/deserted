package edu.cornell.gdiac.ailab;

public class MainMenu {
	/** Available options to use */
	Option[] options;

	/** Index of current option */
	public int selectedOption;

	/** Lerp value for highlighting */
	private float lerpVal;

	/** Lerp value increasing or decreasing */
	private boolean increasing;

	public MainMenu(Option[] options){
		this.options = options;
		selectOption(0);
		lerpVal = 0;
	}
	
	public void selectOption(int optionNo){
		options[selectedOption].isSelected = false;
		options[optionNo].isSelected = true;
		selectedOption = optionNo;
	}
	
	public void draw(GameCanvas canvas){
		for (int i=0;i<options.length;i++){
			options[i].draw(canvas);
		}
	}

}	
