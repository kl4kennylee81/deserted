package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import edu.cornell.gdiac.ailab.ActionNode.Direction;
import edu.cornell.gdiac.ailab.Action.Pattern;
import edu.cornell.gdiac.ailab.ActionNode;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class PersistingController extends ActionController{
	
	/** Controller Variables */
	ActionNode selectedActionNode;
	
	
	public PersistingController(GridBoard board, List<Character> chars,TextMessage textMsgs,AnimationPool animations,
			Shields shields) {
		super(board, chars, textMsgs, animations, shields);
		
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
				List<ActionNode> toDelete = new LinkedList<ActionNode>();
				for (ActionNode an:actionNodes){
					if (an.getAction().pattern != Pattern.SHIELD){
						continue;
					}
					an.curRound+= c.castMoved;
					if (an.curRound >= ((PersistingAction) an.getAction()).totalNumRounds){
						toDelete.add(an);
					} else {
						selectedActionNode = an;
						executeAction();
					}
				}
				for (ActionNode an:toDelete){
					c.popPersistingCast(an);
				}
			}
		}
	}
	
	public void updateProjs(){
		isDone = true;
		//TODO: Sort by cast times
		for (Character c : characters){
			// Choose a character and execute his persisting actions
			if (c.hasPersisting()){
				selected = c;
				updateShieldedPath();
				List<ActionNode> actionNodes = c.getPersistingActions();
				List<ActionNode> toDelete = new LinkedList<ActionNode>();
				for (ActionNode an:actionNodes){
					if (an.getAction().pattern == Pattern.SHIELD){
						continue;
					}
					isDone = false;
					an.curRound+= c.castMoved;
					if (an.curRound >= ((PersistingAction) an.getAction()).totalNumRounds){
						toDelete.add(an);
					} else {
						selectedActionNode = an;
						executeAction();
					}
				}
				for (ActionNode an:toDelete){
					System.out.println("DOES THIS EVER HAPPEN?? PersistingController updateProjs");
					c.popPersistingCast(an);
				}
			}
		}
		
	}
	
	public void executeAction(){
		switch (selectedActionNode.getAction().pattern){
		case STRAIGHT:
			executePath();
			break;
		case DIAGONAL:
			executePath();
			break;
		case PROJECTILE:
			executePath();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Returns true if character is hit by the attack on the given coordinate
	 */
	private boolean isHit(Character c, int curX, int curY){
		return c.isAlive() && !c.equals(selected) && c.leftside != selected.leftside && c.xPosition == curX && c.yPosition == curY;
	}
	
	public void moveAlongPath(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.getAction();
		
		Coordinate cur_pos = selectedActionNode.getCurInPath();
		Coordinate next_pos = selectedActionNode.getNextInPath();
		
		float angleTo = cur_pos.angleTo(next_pos);
		
		double cosA = Math.cos(Math.toRadians(angleTo));
		double sinA = Math.sin(Math.toRadians(angleTo));

		selectedActionNode.curX += selectedAction.moveSpeed * cosA;
		selectedActionNode.curY += selectedAction.moveSpeed * sinA;

		// compute current distance from middle
		float diffX = Math.abs(next_pos.x - selectedActionNode.curX);
		float diffY = Math.abs(next_pos.y - selectedActionNode.curY);
		float dist = diffX + diffY;
		
		// when its past the middle you increment to next path
		if (dist <= selectedAction.moveSpeed){
			selectedActionNode.pathIndex+=1;
		}
	}
	
	public void executePath(){
		if (selectedActionNode.getNextInPath() == null){
			selected.popPersistingCast(selectedActionNode);
			return;
		}
		else {
			moveAlongPath();
			
			shields.resetShieldsHitThisRound();
			
			// check based on a rounding of the current X and current Y
			int curIntX = selectedActionNode.getCurInPath().x;
			int curIntY = selectedActionNode.getCurInPath().y;
			
			// Check if next position is out of bounds or blocked
			if (!board.isInBounds(curIntX,curIntY)){
				selected.popPersistingCast(selectedActionNode);
				return;
			}
			
			if (!board.isInBounds((int)selectedActionNode.getCurrentX(),(int)selectedActionNode.getCurrentY())){
				selected.popPersistingCast(selectedActionNode);
				return;
			}
			
			if (isBlocked(curIntX, curIntY)){
				shields.hitShield(curIntX, curIntY, selected.leftside,textMessages);
				selected.popPersistingCast(selectedActionNode);
				return;
			}
			
			// Check if attack hit any characters
			for (Character c:characters){
				if (isHit(c,curIntX,curIntY)){
					processHit(selectedActionNode,c);
					selected.popPersistingCast(selectedActionNode);
					break;
				}
			}
		}
	}
}
