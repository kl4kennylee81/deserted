package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

public class DecisionNode {
	
	/**  Used to determine which branch do go down if we are in an individual character AI */
	public Tactic branchType;
	
	/** Label of this node, used for traversal */
	public String label;
		
	public DecisionNode(Tactic b){
		branchType = b;
	}
	
	/**
	 * Enumerates possible specific actions that an AI can take;
	 */
	public static enum Specific {
		/** Pick the single target with highest hit chance */
		SINGLE_OPTIMAL,
		/** Target the opponent with the lowest health */
		SINGLE_WEAKEST,
		/** Target the opponent with the most health */
		SINGLE_STRONGEST,
		/** Target yourself */
		SINGLE_SELF,
		/** Launch a normal attack that can hit an opponent */
		NORMAL_ATTACK,
		/** Use attack with lowest cast time */
		QUICK_ATTACK,
		/** Use most powerful normal attack that can hit an opponent */
		POWERFUL_ATTACK,
		/** Intelligently put up a shield - will protect teammate if possible */
		SHIELD,
		/** Try to move to an attacking goal tile */
		MOVE_AGGRESSIVE,
		/** Try to move to a safety goal tile */
		MOVE_DEFENSIVE,
		/** Try to move in front of a teammate block attacks */
		MOVE_PROTECT, 
		/** Try to move towards an optimal goal tile */
		MOVE_GOAL,		
		/** Try to pick a random decent action */
		RANDOM_DECENT
		
	}
	
	public static enum Tactic {
		NONE, ATTACK, POSITION, DEFENSE, SPECIFIC
	}
	
	
	/**
	 * Represents an intermediate node in the decision tree. Each branch
	 * is matched with a list of strings which represent conditions that
	 * must be met.
	 */
	public static class IndexNode extends DecisionNode{
		ArrayList<List<String>> conditions;
		ArrayList<String> decisions;
		
		public IndexNode(Tactic b){
			super(b);
			conditions = new ArrayList<List<String>>();
			decisions = new ArrayList<String>();
		}
		
		public void addRule(List<String> conds, String decision){
			conditions.add(conds);
			decisions.add(decision);
		}
	}
	
	
	/**
	 * Represents a leaf node in the decision tree. The result of this leaf
	 * node is either a general course of action or a list of specific actions.
	 */
	public static class LeafNode extends DecisionNode{
		Tactic myTactic;
		MoveList mySpecific;
		
		Tactic allyTactic;
		MoveList allySpecific;
		
		public LeafNode(Tactic b, Tactic t, MoveList m){
			super(b);
			allyTactic = Tactic.NONE;
			myTactic = t;
			mySpecific = m;
		}
		
		public LeafNode(Tactic b){
			super(b);
			myTactic = Tactic.NONE;
			allyTactic = Tactic.NONE;
		}
	}
	
	/**
	 * Wrapper for a list of specific moves for a single character
	 */
	public static class MoveList {
		ArrayList<Specific> specificActions;
		
		public MoveList(){
			specificActions = new ArrayList<Specific>();
		}
		
		public MoveList(ArrayList<Specific> list){
			specificActions = list;
		}
		
		public void addAction(Specific s){
			specificActions.add(s);
		}
	}
}
