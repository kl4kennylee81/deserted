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
	List<textMessage> textMessages;
	
	
	public PersistingController(GridBoard board, List<Character> chars, ActionBar bar,List<textMessage> textMsgs) {
		this.board = board;
		this.characters = chars;
		this.bar = bar;
		
		this.selected = null;
		this.selectedActionNode = null;
		this.shieldedPaths = new LinkedList<Coordinate>();
		this.textMessages = textMsgs;
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
	
	private boolean isHit(Character c, int curX, int curY){
		return c.isAlive() && !c.equals(selected) && c.leftside != selected.leftside && c.xPosition == curX && c.yPosition == curY;
	}
	
	public void executeStraight(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		
		int prevIntX = selectedActionNode.getCurrentX();
		int prevIntY = selectedActionNode.getCurrentY();
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
			if (isHit(c,curIntX,curIntY) || isHit(c,prevIntX,prevIntY)){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
	
	public void executeDiagonal(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		
		int prevIntX = selectedActionNode.getCurrentX();
		int prevIntY = selectedActionNode.getCurrentY();
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
			if (isHit(c,curIntX,curIntY) || isHit(c,prevIntX,prevIntY)){
				processHit(selectedActionNode,c);
				selected.popPersistingCast(selectedActionNode);
				break;
			}
		}
	}
	
	private void processHit(ActionNode a_node,Character target){
		applyDamage(a_node,target);
		
		String attack_damage = Integer.toString(a_node.action.damage);
		textMessages.add(new textMessage(attack_damage,5*textMessage.SECOND,target));
		
		//handle interruption
		if (target.queuedActions.peek() != null &&!target.queuedActions.peek().isInterrupted){
			target.queuedActions.peek().isInterrupted = true;
		}
	}
	
	private void applyDamage(ActionNode a_node,Character target){
		target.health = Math.max(target.health-a_node.action.damage, 0);
	}
}
