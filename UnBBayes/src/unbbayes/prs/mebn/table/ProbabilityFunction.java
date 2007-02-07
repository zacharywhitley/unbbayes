package unbbayes.prs.mebn.table;

import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.table.exception.ProbabilityFunctionEvalException;
import unbbayes.prs.mebn.table.exception.StateFunctionNotFoundException;

public class ProbabilityFunction {
	
	private MultiEntityNode node;
	private Map<String, StateFunction> mapStateFunction;

	public ProbabilityFunction(MultiEntityNode node) {
		this.node = node;
		mapStateFunction = new HashMap<String, StateFunction>(node.getStatesSize());
	}
	
	/**
	 * This method is responsible for adding the state function in the order 
	 * it is found in the node.
	 * @param stateFunction The state function to be added.
	 */
	public void addStateFunction(StateFunction stateFunction) {
		mapStateFunction.put(stateFunction.getStateName(), stateFunction);
	}
	
	/**
	 * Method responsible for returning the evaluation of the given state's 
	 * function.
	 * @param stateName The name of the state's function
	 * @return The evaluation of the given state's function.
	 * @throws ProbabilityFunctionEvalException
	 * @throws StateFunctionNotFoundException 
	 */
	public float evalStateFunction(String stateName) throws ProbabilityFunctionEvalException, StateFunctionNotFoundException {
		StateFunction function = mapStateFunction.get(stateName);
		if (function == null) {
			throw new StateFunctionNotFoundException("The state's function " + stateName + " was not found!");
		}
		return function.eval();
	}

}
