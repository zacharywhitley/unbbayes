package unbbayes.prs.mebn.table;

import java.util.List;

import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.exception.EntityNotPossibleValueOfNodeException;
import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.ProbabilityFunctionEvalException;

public class StateFunction {
	
	private IfClause ifClause;
	private MultiEntityNode node;
	private String stateName;
	private ProbabilityFunctionOperator function;

	public StateFunction(MultiEntityNode node, String stateName, IfClause ifClause) throws EntityNotPossibleValueOfNodeException {
		this.node = node;
		this.stateName = stateName;
		this.ifClause = ifClause;
		if (!node.hasPossibleValue(stateName)) {
			throw new EntityNotPossibleValueOfNodeException("The entity " + stateName + " is not present in the node " + node.getName());
		}
	}
	
	public String getStateName() {
		return stateName;
	}
	
	/**
	 * Method responsible for setting the function.
	 * @param function The function to be set.
	 * @throws InvalidProbabilityFunctionOperandException 
	 */
	public void setFunction(ProbabilityFunctionOperator function) throws InvalidProbabilityFunctionOperandException {
		if (this.function != null) {
			ProbabilityFunctionOperator tempFunction = function;
			while(tempFunction.getSecondOperand() != null) {
				tempFunction = tempFunction.getSecondOperand(); 
			}
			tempFunction.setSecondOperand(function);
		} else {
			this.function = function;
		}
	}
	
	/**
	 * Method responsible for setting a default function when the function is 
	 * actually just a number. So it is created a 
	 * ProbabilityFunctionOperator.PLUS with the second operand being 0.
	 * @param number The number to be added to zero. The function itself.
	 * @throws InvalidProbabilityFunctionOperandException 
	 */
	public void setFunction(float number) throws InvalidProbabilityFunctionOperandException {
		if (this.function != null) {
			ProbabilityFunctionOperator tempFunction = function;
			while(tempFunction.getSecondOperand() != null) {
				tempFunction = tempFunction.getSecondOperand(); 
			}
			tempFunction.setSecondOperand(number);
		} else {
			this.function = ProbabilityFunctionOperator.PLUS;
			function.setFirstOperand(number);
			function.setSecondOperand(0);
		}
	}
	
	/**
	 * Method responsible for evaluating this states function and returning a 
	 * number.
	 * @return The resulting number of the function's evaluation.
	 * @throws ProbabilityFunctionEvalException
	 */
	public float eval() throws ProbabilityFunctionEvalException {
		return function.eval();
	}

	public IfClause getIfClause() {
		return ifClause;
	}

}
