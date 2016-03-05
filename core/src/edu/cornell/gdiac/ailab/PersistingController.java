package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

public class PersistingController {
	GridBoard board;
	List<Character> characters;
	Character selected;
	ActionBar bar;
	ActionNode selectedActionNode;
	List<Coordinate> shieldedPaths;
	
	public PersistingController(GridBoard board, List<Character> chars, ActionBar bar) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		
		this.selected = null;
		this.selectedActionNode = null;
		this.shieldedPaths = new LinkedList<Coordinate>();
	}
	
	public void update(){
		//should first sort characters by cast time
		for (Character c : characters){
			if (c.hasPersisting()){
				selected = c;
				updateShieldedPath();
				List<ActionNode> actionNodes = c.getPersistingActions();
				for (ActionNode an : actionNodes){
					selectedActionNode = an;
					executeAction();
					an.castPoint += 1;
					if (an.castPoint >= ((PersistingAction) an.action).castLength){
						c.popPersistingCast(an);
					}
				}
			}
		}
	}
	
	private void updateShieldedPath(){
		shieldedPaths.clear();
		for (Character c : characters){
			if (c.leftside != selected.leftside && c.hasShield()) {
				shieldedPaths.addAll(c.getShieldedCoords());
			}
		}
	}
	
	private boolean isBlocked(int coordX, int coordY){
		for (Coordinate c : shieldedPaths){
			if (c.x==coordX && c.y==coordY){
				return true;
			}
		}
		return false;
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
	
	public void executeStraight(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		if (selected.leftside){
			selectedActionNode.curX += selectedAction.moveSpeed;
		} else {
			selectedActionNode.curX -= selectedAction.moveSpeed;
		}
		int curIntX = selectedActionNode.getCurrentX();
		int curIntY = selectedActionNode.getCurrentY();
		
		if (!board.isInBounds(curIntX, curIntY) || isBlocked(curIntX, curIntY)){
			selected.popPersistingCast(selectedActionNode);
			return;
		}
		
		for (Character c:characters){
			if (!c.equals(selected) && c.xPosition == curIntX && c.yPosition == curIntY){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
	
	public void executeDiagonal(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		if (selected.leftside){
			selectedActionNode.curX += selectedAction.moveSpeed;
		} else {
			selectedActionNode.curX -= selectedAction.moveSpeed;
		}
		if (selectedActionNode.yPosition == 0){
			selectedActionNode.curY -= selectedAction.moveSpeed;
		} else {
			selectedActionNode.curY += selectedAction.moveSpeed;
		}
		
		int curIntX = selectedActionNode.getCurrentX();
		int curIntY = selectedActionNode.getCurrentY();
		
		if (!board.isInBounds(curIntX, curIntY) || isBlocked(curIntX, curIntY)){
			selected.popPersistingCast(selectedActionNode);
			return;
		}
		
		for (Character c:characters){
			if (!c.equals(selected) && c.xPosition == curIntX && c.yPosition == curIntY){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
	
	private void processHit(ActionNode a_node,Character target){
		applyDamage(a_node,target);
		
		//handle interruption
		if (target.queuedActions.peek() != null &&!target.queuedActions.peek().isInterrupted){
			target.queuedActions.peek().isInterrupted = true;
		}
	}
	
	private void applyDamage(ActionNode a_node,Character target){
		target.health = Math.max(target.health-a_node.action.damage, 0);
	}
}
