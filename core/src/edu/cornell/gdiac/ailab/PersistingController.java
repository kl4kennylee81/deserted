package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class PersistingController extends ActionController{
	
	/** Controller Variables */
	ActionNode selectedActionNode;
	
	
	public PersistingController(GridBoard board, List<Character> chars, ActionBar bar,TextMessage textMsgs) {
		super(board, chars, bar, textMsgs);
		
		this.selected = null;
		this.selectedActionNode = null;
		this.shieldedPaths = new LinkedList<Coordinate>();
	}
	
	@Override
	public void update(){
		//TODO: Sort by cast times
		for (Character c : characters){
			// Choose a character and execute his persisting actions
			if (c.hasPersisting()){
				selected = c;
				updateShieldedPath();
				List<ActionNode> actionNodes = c.getPersistingActions();
				for (int i=0;i<actionNodes.size();i++){
					ActionNode an = actionNodes.get(i);
					selectedActionNode = an;
					executeAction();
					an.castPoint += 1;
					if (an.action != null && an.castPoint >= ((PersistingAction) an.action).castLength){
						c.popPersistingCast(an);
					}
				}
			}
		}
	}
	
	public void executeAction(){
		switch (selectedActionNode.action.pattern){
		case STRAIGHT:
			executeStraight();
			break;
		case DIAGONAL:
			executeDiagonal();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Returns ture if character is hit by the attack on the given coordinate
	 */
	private boolean isHit(Character c, int curX, int curY){
		return c.isAlive() && !c.equals(selected) && c.leftside != selected.leftside && c.xPosition == curX && c.yPosition == curY;
	}
	
	public void executeStraight(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		
		// Keep track of current and next x/y positions
		int prevIntX = selectedActionNode.getCurrentX();
		int prevIntY = selectedActionNode.getCurrentY();
		if (selected.leftside){
			selectedActionNode.curX += selectedAction.moveSpeed;
		} else {
			selectedActionNode.curX -= selectedAction.moveSpeed;
		}
		int curIntX = selectedActionNode.getCurrentX();
		int curIntY = selectedActionNode.getCurrentY();
		
		// Check if next position is out of bounds or blocked
		if (!board.isInBounds(curIntX, curIntY) || isBlocked(curIntX, curIntY)){
			selected.popPersistingCast(selectedActionNode);
			return;
		}
		
		// Check if attack hit any characters
		for (Character c:characters){
			if (isHit(c,curIntX,curIntY) || isHit(c,prevIntX,prevIntY)){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
	
	public void executeDiagonal(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		
		// Keep track of current and next x/y positions
		int prevIntX = selectedActionNode.getCurrentX();
		int prevIntY = selectedActionNode.getCurrentY();
		if (selected.leftside){
			selectedActionNode.curX += selectedAction.moveSpeed;
		} else {
			selectedActionNode.curX -= selectedAction.moveSpeed;
		}
		if (selectedActionNode.direction == Direction.DOWN){
			selectedActionNode.curY -= selectedAction.moveSpeed;
		} else {
			selectedActionNode.curY += selectedAction.moveSpeed;
		}
		int curIntX = selectedActionNode.getCurrentX();
		int curIntY = selectedActionNode.getCurrentY();
		
		// Check if next position is out of bounds or blocked
		if (!board.isInBounds(curIntX, curIntY) || isBlocked(curIntX, curIntY)){
			selected.popPersistingCast(selectedActionNode);
			return;
		}
		
		// Check if attack hit any characters
		for (Character c:characters){
			if (isHit(c,curIntX,curIntY) || isHit(c,prevIntX,prevIntY)){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
}
