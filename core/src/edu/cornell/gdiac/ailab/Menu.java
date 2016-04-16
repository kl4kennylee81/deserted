package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Texture;

public abstract class Menu {
	/** Available options to use */
	Option[] options;

	/** Name of current option */
	
	/** will change to index again don't worry lads the calvary is here **/
	public String selectedOption;
	
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
	
	public String getOption(int index){
		if (this.options.length > index){
			return options[index].optionKey;
		}
		else{
			
			// we will have empty string always be catched and just default to do nothing
			return "";
		}
	}
	
	public abstract void selectOption(String optionKey);
	
	/** return the index if found otherwise return -1 **/
	public abstract int getIndexOption(String optionKey);
	
	/** return the current index of the selected option otherwise return -1 **/
	public abstract int getCurIndexOption();
	
	public void setOption(int index){
		if (this.options.length > index){
			String selectedOptionKey = this.options[index].optionKey;
			// then call select action
			selectOption(selectedOptionKey);
		}
	}
}
