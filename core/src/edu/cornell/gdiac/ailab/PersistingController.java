package edu.cornell.gdiac.ailab;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

import edu.cornell.gdiac.ailab.ActionNodes.Direction;
import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.Coordinates.Coordinate;

public class PersistingController extends ActionController{
	
	/** Controller Variables */
	ActionNode selectedActionNode;
	
	
	public PersistingController(GridBoard board, List<Character> chars, ActionBar bar,TextMessage textMsgs,AnimationPool animations) {
		super(board, chars, bar, textMsgs, animations);
		
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
					an.curRound+= c.castMoved;
					if (an.curRound >= ((PersistingAction) an.action).totalNumRounds){
						c.popPersistingCast(an);
					} else {
						selectedActionNode = an;
						executeAction();
					}
				}
			}
		}
	}
	
	public void executeAction(){
		switch (selectedActionNode.action.pattern){
		case STRAIGHT:
			executePath();
			break;
		case DIAGONAL:
			executePath();
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
	
	public void moveAlongPath(){
		PersistingAction selectedAction = (PersistingAction) selectedActionNode.action;
		
		Coordinate cur_pos = selectedActionNode.getCurInPath();
		Coordinate next_pos = selectedActionNode.getNextInPath();
		
		float angleTo = cur_pos.angleTo(next_pos);
		
		double cosA = Math.cos(Math.toRadians(angleTo));
		double sinA = Math.sin(Math.toRadians(angleTo));
		selectedActionNode.curX += selectedAction.moveSpeed * cosA;
		selectedActionNode.curY += selectedAction.moveSpeed * sinA;
		
		// get the middle of the board tile
		float midNextX,midNextY;
		if (cur_pos.y == cur_pos.y){
			midNextX = (float) ((float) Math.signum(cosA) * 0.5 + next_pos.x);
			midNextY = cur_pos.y;
		}
		else{
			midNextX = (float) ((float) Math.signum(cosA) * 0.5 + next_pos.x);
			midNextY = (float) ((float) Math.signum(sinA) * 0.5 + next_pos.y);
		}

		// compute current distance from middle
		float diffX = (midNextX - selectedActionNode.curX);
		float diffY = (midNextY - selectedActionNode.curY);
		float dist = diffX*diffX + diffY*diffY;
	
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
			
			// check based on a rounding of the current X and current Y
			int curIntX = selectedActionNode.getCurrentX();
			int curIntY = selectedActionNode.getCurrentY();
			
			// Check if next position is out of bounds or blocked
			if (!board.isInBounds(curIntX, curIntY) || isBlocked(curIntX, curIntY)){
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
