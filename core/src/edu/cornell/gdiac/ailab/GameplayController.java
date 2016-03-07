package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class GameplayController {
	/** Subcontroller for actions (CONTROLLER CLASS) */
    private ActionController actionController;
    /** Subcontroller for selection menu (CONTROLLER CLASS) */
    private SelectionMenuController selectionMenuController;
    /** Subcontroller for action bar (CONTROLLER CLASS) */
    private ActionBarController actionBarController;
    /** Subcontroller for persisting actions (CONTROLLER CLASS) */
    private PersistingController persistingController;
    /** Subcontroller for ai selection (CONTROLLER CLASS) */
    private AIController aiController;
	
	/** Current Models */
    private GridBoard board;
    private List<Character> characters;
    private ActionBar bar;
    private List<textMessage> textMessages;
    
    /** Current state of game */
    private InGameState inGameState;
    
    public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK,
		DONE
	}
    
    public GameplayController(){
    	
    }
    
    /**
	 * Restart the game, laying out all the ships and tiles
	 */
	public void resetGame(List<Character> characters, int boardWidth, int boardHeight, Texture boardMesh) {
		inGameState = InGameState.NORMAL;
		
        // Create the models.
        board = new GridBoard(boardWidth,boardHeight);
        board.setTileTexture(boardMesh);
        this.characters = characters;
        
        textMessages = new LinkedList<textMessage>();
        
        bar = new ActionBar();
        
		// Create the subcontrollers
        actionController = new ActionController(board,characters,bar,textMessages);
        selectionMenuController = new SelectionMenuController(board,characters,bar);
        actionBarController = new ActionBarController(characters,bar);
        aiController = new AIController(board,characters,bar);
        persistingController = new PersistingController(board,characters,bar,textMessages);
        
	}
    
    public void update(){
    	switch(inGameState){
    	case NORMAL:
    		actionBarController.update();
    		persistingController.update();
    		updateTextMessages();
    		if (actionBarController.isAISelection) {
    			aiController.update();
    		}
    		if (actionBarController.isAttack){
    			inGameState = InGameState.ATTACK;
    		} else if (actionBarController.isPlayerSelection) {
    			inGameState = InGameState.SELECTION;
    		}
    		break;
    	case SELECTION:
    		selectionMenuController.update();
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			board.reset();
    		}
    		break;
    	case ATTACK:
    		actionController.update();
    		if (actionController.isDone()){
    			if (actionBarController.isPlayerSelection){
    				inGameState = InGameState.SELECTION;
    			} else {
    				inGameState = InGameState.NORMAL;
    			}
    		}
    		break;
		default:
			break;	
    	}
    	
    	if (gameOver()){
    		inGameState = InGameState.DONE;
    	}
    }
    
    public void drawPlay(GameCanvas canvas){
    	board.draw(canvas);
        for (Character c : characters){
        	c.draw(canvas);
        }
        bar.draw(canvas);
        
        for (textMessage m : textMessages){
        	m.draw(canvas);
        }
    }
    
    public void drawAfter(GameCanvas canvas){
	    if (leftsideDead() && rightsideDead()){
			canvas.drawText("A tie?", 400, 400, Color.BLACK);
		} else if (leftsideDead()){
			canvas.drawText("Hah you lost", 400, 400, Color.BLACK);
		} else if (rightsideDead()){
			canvas.drawText("Yay you beat an easy bot", 400, 400, Color.BLACK);
		} else {
			System.out.println("SHOULD NEVER GET HERE");
		}
	    canvas.drawText("Press R to return", 400, 360, Color.BLACK);
    }
    
    
    public boolean leftsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	return dead;
    }
    
    public boolean rightsideDead(){
    	boolean dead = true;
    	for (Character c : characters){
    		if (!c.leftside && c.isAlive()){
    			dead = false;
    		}
    	}
    	return dead;
    }
    
    public boolean gameOver(){
    	return leftsideDead() || rightsideDead();
    }
    
    public boolean isDone(){
    	return inGameState == InGameState.DONE;
    }
    
    public void updateTextMessages(){
//		List<textMessage> tempMsg = new LinkedList<textMessage>();
		for (textMessage m: textMessages){
			if (m.duration > 0){
				m.duration--;
				m.y_pos+=0.4;
//				tempMsg.add(m);
			}
		}
//		textMessages = tempMsg;
    }
	
}