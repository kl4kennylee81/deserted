package edu.cornell.gdiac.ailab;

import java.util.Iterator;
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
    /** Subcontroller for managing mouse over highlighting */
    private MouseOverController mouseOverController;
	
	/** Current Models */
    private GridBoard board;
    private Characters characters;
    private TextMessage textMessages;
    private AnimationPool animations;
    
    private HighlightScreen screen;
    
    private String prompt;
    
    /** Current state of game */
    private InGameState inGameState;
    
    public static enum InGameState {
		NORMAL,
		SELECTION,
		ATTACK,
		DONE
	}
    
    public GameplayController(MouseOverController moc){
    	mouseOverController = moc;
    }
    
    public void resetGame(Level level){
    	inGameState = InGameState.NORMAL;
		
    	int boardWidth = level.getBoardWidth();
    	int boardHeight = level.getBoardHeight();
    	Texture boardMesh = level.getBoardTexture();
    	
        // Create the models.
        board = new GridBoard(boardWidth,boardHeight);
        board.setTileTexture(boardMesh);
        this.characters = level.getCharacters();
        screen = new HighlightScreen();
        
        textMessages = new TextMessage();
        animations = new AnimationPool();
        
		// Create the subcontrollers
        actionController = new ActionController(board,characters,textMessages,animations);
        selectionMenuController = new SelectionMenuController(board,characters);
        actionBarController = new ActionBarController(characters);
        aiController = new AIController(board,characters);
        persistingController = new PersistingController(board,characters,textMessages,animations);
        effectController = new EffectController(characters);
        mouseOverController.init(screen, board);
    }
    
    public void update(){
    	screen.noScreen();
    	switch(inGameState){
    	case NORMAL:
    		// update the character models
    		characters.update();
    		effectController.update();
    		actionBarController.update();
    		persistingController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
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
    		screen.setJustScreen();
    		mouseOverController.clearAll();
    		selectionMenuController.update();
    		mouseOverController.update(selectionMenuController.getMenu(),characters);
    		prompt = "Choose an Action";
    		selectionMenuController.setPrompt(prompt);
    		if (selectionMenuController.isDone()){
    			inGameState = InGameState.NORMAL;
    			prompt = null;
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
    	removeDead();
    	if (gameOver()){
    		inGameState = InGameState.DONE;
    		ObjectLoader.getInstance().unloadCurrentLevel();
    	}
    }
    
    public void removeDead(){
    	Iterator<Character> iter = characters.iterator();
    	while (iter.hasNext()) {
    	    Character c = iter.next();
			if (!c.isAlive()){
				iter.remove();
			}
    	}
    }
    
    public void drawPlay(GameCanvas canvas){
        screen.draw(canvas);
    	board.draw(canvas);
    	drawCharacters(canvas);
        animations.draw(canvas,board);
        
        textMessages.draw(canvas,board);
        if (prompt != null){
        	canvas.drawText(prompt, 18, 530, Color.BLACK);
        }
        //screen should be drawn after greyed out characters
        //but before selected characters
    }
    
    
    //Change how i do this.
    //This needs to be done so characters below show over characters above and selection menu
    //shows over characters.
    private void drawCharacters(GameCanvas canvas){
		boolean shouldDim = inGameState == InGameState.SELECTION || 
				mouseOverController.isCharacterHighlighted();
    	characters.draw(canvas,shouldDim);
    	for (int i = board.height-1; i >= 0; i--){
    		for (Character c : characters){
    			
    			if (c.yPosition == i && c.isAlive()){
    				c.drawCharacter(canvas,board,shouldDim,this.inGameState);
    			}
    			if (c.getShadowY() == i && c.needShadow() && c.isAlive()){
    				c.drawShadowCharacter(canvas,board,this.inGameState);
    			}
            }
    	}
        for (Character c : characters){
        	c.draw(canvas,board);
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