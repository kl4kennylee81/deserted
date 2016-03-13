package edu.cornell.gdiac.ailab;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import edu.cornell.gdiac.ailab.TextMessage.Message;

public class GameplayController {
	/** Subcontroller for actions (CONTROLLER CLASS) */
    private ActionController actionController;
    /** Subcontroller for selection menu (CONTROLLER CLASS) */
    private SelectionMenuController selectionMenuController;
    /** Subcontroller for action bar (CONTROLLER CLASS) */
    private ActionBarController actionBarController;
    /** Subcontroller for persisting actions (CONTROLLER CLASS) */
    private PersistingController persistingController;
    /** Subcontroller for AI selection (CONTROLLER CLASS) */
    private AIController aiController;
    /** Subcontroller for managing effects */
    private EffectController effectController;
	
	/** Current Models */
    private GridBoard board;
    private List<Character> characters;
    private ActionBar bar;
    private TextMessage textMessages;
    
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
        
        textMessages = new TextMessage();
        
        bar = new ActionBar();
        
		// Create the subcontrollers
        actionController = new ActionController(board,characters,bar,textMessages);
        selectionMenuController = new SelectionMenuController(board,characters,bar);
        actionBarController = new ActionBarController(characters,bar);
        aiController = new AIController(board,characters,bar);
        persistingController = new PersistingController(board,characters,bar,textMessages);
        effectController = new EffectController(characters);
        
	}
    
    public void update(){
    	switch(inGameState){
    	case NORMAL:
    		effectController.update();
    		actionBarController.update();
    		persistingController.update();
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
    	updateTextMessages();
    	
    	if (gameOver()){
    		inGameState = InGameState.DONE;
    	}
    }
    
    public void drawPlay(GameCanvas canvas){
    	board.draw(canvas);
        bar.draw(canvas);
        drawCharacters(canvas);
        textMessages.draw(canvas);
    }
    
    
    //Change how i do this.
    //This needs to be done so characters below show over characters above and selection menu
    //shows over characters.
    private void drawCharacters(GameCanvas canvas){
        for (Character c : characters){
        	c.drawSelection(canvas);
        }
    	for (Character c : characters){
        	c.draw(canvas);
        }
    	for (int i = board.height-1; i >= 0; i--){
    		for (Character c : characters){
    			if (c.yPosition == i && c.isAlive()){
    				c.drawCharacter(canvas);
    			}
    			if (c.getShadowY() == i && c.needShadow() && c.isAlive()){
    				c.drawShadowCharacter(canvas);
    			}
            }
    	}
    }
    
    public void drawAfter(GameCanvas canvas){
	    if (leftsideDead() && rightsideDead()){
			canvas.drawText("A tie?", 400, 400, Color.BLACK);
		} else if (leftsideDead()){
			canvas.drawText("RED SIDE WINS", 400, 400, Color.BLACK);
		} else if (rightsideDead()){
			canvas.drawText("BLUE SIDE WINS", 400, 400, Color.BLACK);
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
    	Iterator<Message> iter = textMessages.damageMessages.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    	
    	iter = textMessages.otherMessages.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    	
    	iter = textMessages.tempSingles.iterator();
    	while (iter.hasNext()) {
    	    Message m = iter.next();
    	    m.current++;
			if (m.current > m.duration){
				iter.remove();
			}
    	}
    }
	
}