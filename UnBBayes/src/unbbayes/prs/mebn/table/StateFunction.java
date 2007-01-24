package unbbayes.prs.mebn.table;

import java.util.List;

import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;

public class StateFunction {
	
	private MultiEntityNode node;
	private String stateName;
	private List<Object> function;

	public StateFunction(MultiEntityNode node, String stateName) throws EntityNotPossibleValueOfNodeException {
		this.node = node;
		this.stateName = stateName;
		if (!node.hasPossibleValue(stateName)) {
			throw new EntityNotPossibleValueOfNodeException("The entity " + stateName + " is not present in the node " + node.getName());
		}
	}
	
	public String getStateName() {
		return stateName;
	}
	
	/**
	 * Method responsible for adding elements in the function.
	 * @param element The element to be added.
	 */
	public void addFunctionElement(Object element) {
		function.add(element);
	}

}
