package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Texture;

public abstract class Menu {
	/** Available options to use */
	Option[] options;

	/** Index of current option */
	public int selectedOption;
	
	/** option texture **/
	Texture optionTexture;
	
	/** main menu background */
	Texture menuBackground;
	
	public abstract void draw(GameCanvas canvas);
	
	public Option[] getOptions() {
		return options;
	}
	
	public Texture getImage(){
		return optionTexture;
	}
	
	public abstract void selectOption(int optionNo);
	
	/** return the index if found otehrwise return -1 **/
	public abstract int getIndexOption(int optionNo);
	
	/** return the current index of the selected option otherwise return -1 **/
	public abstract int getCurIndexOption();
}
