package edu.cornell.gdiac.ailab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cornell.gdiac.ailab.ActionNodes.ActionNode;
import edu.cornell.gdiac.ailab.DecisionNode;
import edu.cornell.gdiac.ailab.DecisionNode.IndexNode;
import edu.cornell.gdiac.ailab.DecisionNode.LeafNode;
import edu.cornell.gdiac.ailab.DecisionNode.MoveList;
import edu.cornell.gdiac.ailab.DecisionNode.Specific;
import edu.cornell.gdiac.ailab.DecisionNode.Tactic;


public class TacticalManager {
	
    private ConditionalManager conditions;
	private List<Character> chars;
	private ActionBar bar;
	private GridBoard board;
	private DecisionNode tacticalTree;
	private HashMap<Character, LeafNode> preSelected;
	private HashMap<Character, IndexNode> characterTrees;
		
	
	public TacticalManager(){
		preSelected = new HashMap<Character, LeafNode>();
		characterTrees = new HashMap<Character, IndexNode>();
	}
	
	
	public void setState(GridBoard board, List<Character> chars, ActionBar bar){
		this.chars = chars;
		this.board = board;
		this.bar = bar;
	}
	
	
	/**
	 * Update all the condition booleans in the Conditional Manager
	 */
	public void updateConditions(Character c){
		conditions.update(board, chars, bar, c);
	}
	
	
	/**
	 * Select and action for the character whose id is "id". First check if this
	 * character's specific actions have already been selected. If they have, 
	 * then just use those. If a general course of action has been preselected
	 * for this character, then traverse the character's tree for that general
	 * action. There is no action preselected, then traverse the main Tactical
	 * Manager tree to find a list of actions. 
	 */
	public void selectActions(Character c){
		LeafNode leaf;
		//Either get the preselected leaf or traverse the tree to find it
		if(preSelected.containsKey(c)){
			leaf = preSelected.get(c);
			preSelected.remove(c);
		}
		else{
			leaf = traverse(tacticalTree);
		}
		
		//If the leaf has an action for a teammate, add it to the preselected info
		if(leaf.friendTactic != Tactic.NONE){
			Character friend = findNextFriend(c);
			LeafNode friendLeaf = new LeafNode(leaf.friendTactic, leaf.friendSpecific);
			preSelected.put(friend, friendLeaf);
		}
		
		if(leaf.myTactic == Tactic.SPECIFIC){
			c.setQueuedActions(getActionsFromSpecific(c, leaf.mySpecific));
		}
		else {
			c.setQueuedActions(getActionsFromGeneral(c, leaf.myTactic));
		}
	}
	
	
	/**
	 * Find the teammate that will reach the casting phase next.
	 */
	public Character findNextFriend(Character c){
		//IMPLEMENT
		return null;
	}
	
	
	/**
	 * Given an IndexNode which is the root of a character's individual 
	 * decision tree, find the subtree which corresponds to the chosen general
	 * tactic.
	 */
	public DecisionNode getSubtreeFromTactic(IndexNode node, Tactic tactic){
		for(DecisionNode n: node.decisions){
			if(n.branchType == tactic){
				return n;
			}
		}
		System.out.println("NO TACTIC MATCHED");
		return null;
	}
	
	
	/**
	 * Given a general course of action, traverse the character's individual 
	 * decision tree to figure our what to do.
	 */
	public List<ActionNode> getActionsFromGeneral(Character c, Tactic general){
		IndexNode node = characterTrees.get(c);
		DecisionNode subTree = getSubtreeFromTactic(node, general);
		LeafNode charLeaf = traverse(subTree);
		return getActionsFromSpecific(c, charLeaf.mySpecific);
	}
	
	
	/**
	 * Given a specific list of actions, convert them into ActionNodes.
	 */
	public List<ActionNode> getActionsFromSpecific(Character c, MoveList specific){
		ArrayList<Specific> moves = specific.specificActions;
		ArrayList<ActionNode> nodes = new ArrayList<ActionNode>();
		for(Specific s: moves){
			switch(s){
				//IMPLEMENT
				case SINGLE_OPTIMAL:
					break;
				case SINGLE_WEAKEST:
					break;
				case SINGLE_STRONGEST:
					break;
				case NORMAL_ATTACK:
					break;
				case QUICK_ATTACK:
					break;
				case SHIELD:
					break; 
				case MOVE_AGGRESSIVE:
					break;
				case MOVE_DEFENSIVE:
					break;
				case MOVE_PROTECT:
					break;
				default:
					break;
			}
		}
		return nodes;
	}
	
	
	/**
	 * Find the leaf node given an IndexNode n as a start point
	 */
	public LeafNode traverse(DecisionNode n){
		if(n instanceof LeafNode){
			return (LeafNode) n;
		}
		IndexNode index = (IndexNode) n;
		for(int i = 0; i < index.conditions.size(); i++){
			List<String> conds = index.conditions.get(i);
			boolean matched = true;
			for(String s: conds){
				if(!conditions.map.containsKey(s) || !conditions.map.get(s)){
					matched = false;
					break;
				}
			}
			if(matched){
				return traverse(index.decisions.get(i));
			}
		}
		System.out.println("NO CONDITION MATCHED");
		return null;
	}

}
 