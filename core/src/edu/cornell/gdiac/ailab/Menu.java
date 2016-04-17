package edu.cornell.gdiac.ailab;

import com.badlogic.gdx.graphics.Texture;

public abstract class Menu {
	/** Available options to use */
	Option[] options;

	/** Name of current option */
	
	/** will change to index again don't worry lads the calvary is here **/
	public int selectedIndex;
	
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
	
	/** return the current index of the selected option otherwise return -1 **/
	public int getCurIndexOption(){
		return this.selectedIndex;
	}
	
	public String getCurOption(){
		return this.getOption(this.selectedIndex);
	}
	
	public void setOption(int newIndex){
		if (newIndex < options.length){
			options[selectedIndex].isSelected = false;
			options[newIndex].isSelected = true;
			this.selectedIndex = newIndex;
		}
	}
	
	public void reset(){
		if (this.options.length > 0){
			selectedIndex = 0;
		}
	}
}
