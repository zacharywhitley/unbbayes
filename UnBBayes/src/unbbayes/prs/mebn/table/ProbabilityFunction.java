package unbbayes.prs.mebn.table;

import unbbayes.prs.mebn.MultiEntityNode;

public class ProbabilityFunction {
	
	private MultiEntityNode node;
	private StateFunction[] listStateFunction;

	public ProbabilityFunction(MultiEntityNode node) {
		this.node = node;
		listStateFunction = new StateFunction[node.getStatesSize()];
	}
	
	/**
	 * This method is responsible for adding the state function in the order 
	 * it is found in the node.
	 * @param stateFunction The state function to be added.
	 */
	public void addStateFunction(StateFunction stateFunction) {
		// Set the state function to the index it is found in the node.
		int index = node.getPossibleValueIndex(stateFunction.getStateName());
		listStateFunction[index] =  stateFunction;
	}

}
