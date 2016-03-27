package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.List;

public class DecisionNode {
	
	/** 
	 * Used to determine which branch do go down if we are in an individual character AI
	 */
	public Tactic branchType;
		
	
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
		/** Launch a normal attack that can hit an opponent */
		NORMAL_ATTACK,
		/** Use attack with lowest cast time */
		QUICK_ATTACK,
		/** Intelligently put up a shield - will protect teammate if possible */
		SHIELD,
		/** Try to move to an attacking goal tile */
		MOVE_AGGRESSIVE,
		/** Try to move to a safety goal tile */
		MOVE_DEFENSIVE,
		/** Try to move in front of a teammate block attacks */
		MOVE_PROTECT,		
	}
	
	public static enum Tactic {
		/** No tactical information */
		NONE,
		/** General attack mode */
		ATTACK,
		/** Attempt high-risk, high-reward actions */
		GAMBLE,
		/** Do whatever it takes to stay alive */
		PRESERVATION,
		/** This means that specific actions have been chosen */
		SPECIFIC
	}
	
	
	/**
	 * Represents an intermediate node in the decision tree. Each branch
	 * is matched with a list of strings which represent conditions that
	 * must be met.
	 */
	public static class IndexNode extends DecisionNode{
		ArrayList<List<String>> conditions;
		ArrayList<DecisionNode> decisions;
		
		public IndexNode(){
			conditions = new ArrayList<List<String>>();
			decisions = new ArrayList<DecisionNode>();
		}
		
		public void addRule(List<String> conds, DecisionNode decision){
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
		
		Tactic friendTactic;
		MoveList friendSpecific;
		
		public LeafNode(Tactic t, MoveList m){
			friendTactic = Tactic.NONE;
			myTactic = t;
			mySpecific = m;
		}
		
		public LeafNode(){
			myTactic = Tactic.NONE;
			friendTactic = Tactic.NONE;
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
