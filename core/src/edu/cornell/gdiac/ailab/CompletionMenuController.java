package edu.cornell.gdiac.ailab;

public class CompletionMenuController {

	CompletionMenu menu;
	GameCanvas canvas;
	
	boolean doneSelecting;
	
	String selected;
	
	public CompletionMenuController(GameCanvas canvas){
		doneSelecting = false;
		selected = "";
		menu = CompletionScreen.getInstance().getMenu();
		this.canvas = canvas;
	}
	
	
	public void update(){
		boolean mouseCondition = false;
		if (menu.selectedIndex!=-1){
			Option curOption = menu.options[menu.selectedIndex];
			mouseCondition = curOption.contains(InputController.getMouseX(),InputController.getMouseY(),canvas,null)
				&& (InputController.pressedLeftMouse());
		}
		
		
		if (InputController.pressedEnter() || mouseCondition){
			// fixup to get cur option string from the index
			selected = menu.getCurOption();
			doneSelecting = true;
		}  else if (InputController.pressedDown() && !InputController.pressedUp()){
	         //newSelection % length
	         //(n < 0) ? (m - (abs(n) % m) ) %m : (n % m);
	         //taken from http://stackoverflow.com/questions/5385024/mod-in-java-produces-negative-numbers
	    	 int newSelection = menu.getCurIndexOption() + 1;
	         int length = menu.getOptions().length;
	         int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);

		     menu.setOption(toSelect);
		     
		}   else if (InputController.pressedUp() && !InputController.pressedDown()){
	    	 int newSelection =menu.getCurIndexOption() - 1;
	        int length = menu.getOptions().length;
	        int toSelect = (newSelection < 0) ? (length - 
						(Math.abs(newSelection) % length) ) 
						%length : (newSelection % 
								length);
	        menu.setOption(toSelect);
		}
		
		for (int i=0; i<menu.options.length; i++){
			Option opt = menu.options[i];
			if (opt.contains(InputController.getMouseX(), InputController.getMouseY(), canvas, null)){
				menu.setOption(i);
			}
		}
	}
	
	public void reset(){
		selected = "";
		doneSelecting = false;
		menu.reset();
	}
}
