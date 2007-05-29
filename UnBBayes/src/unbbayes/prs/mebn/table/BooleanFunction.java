package unbbayes.prs.mebn.table;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.exception.NodeNotPresentInMTheoryException;
import unbbayes.prs.mebn.table.exception.InvalidConditionantException;

public class BooleanFunction {
	
	private MultiEntityBayesianNetwork mebn;
	private String nodeName;
	private String stateName;
	
	public BooleanFunction(MultiEntityBayesianNetwork mebn, String nodeName, String stateName) 
			throws NodeNotPresentInMTheoryException, 
			EntityNotPossibleValueOfNodeException { 
		this.mebn = mebn;
		this.nodeName = nodeName;
		this.stateName = stateName;
		if (!isNodeInMTheory()) {
			throw new NodeNotPresentInMTheoryException("The node " + nodeName + " is not present in this MTheory."); 
		}
		
		if (!isStateInNode()) {
			throw new EntityNotPossibleValueOfNodeException("The entity " + stateName + " is not present in the node " + nodeName);
		}
	}
	
	//TODO IMPLEMENT THIS EVALUATION OR CUT IT OUT
	public boolean eval() {
		//if ()
		return false;
	}
	
	private boolean isNodeInMTheory() {
		if (mebn.getNode(nodeName) != null) {
			
			return true;
		}
		return false;
	}
	
	
	private boolean isStateInNode() {
		MultiEntityNode node = (MultiEntityNode)mebn.getNode(nodeName); 
		if (node != null) {
			return (node.hasPossibleValue(stateName));
		}
		return false;
	}

}
