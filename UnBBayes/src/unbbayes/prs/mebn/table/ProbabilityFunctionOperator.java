package unbbayes.prs.mebn.table;

import unbbayes.prs.mebn.table.exception.InvalidProbabilityFunctionOperandException;
import unbbayes.prs.mebn.table.exception.ProbabilityFunctionEvalException;
import unbbayes.prs.mebn.table.exception.StateFunctionNotFoundException;

public enum ProbabilityFunctionOperator {
	
	MIN,
	MAX,
	PLUS,
	MINUS,
	TIMES,
	DIVIDE,
	CARDINALITY,
	REFERENCE;
	
	private ProbabilityFunction probabilityFunction;
	
	private String uniqueOperand;
	private float firstNumber = Float.NaN;
	private float secondNumber = Float.NaN;
	private ProbabilityFunctionOperator firstProbabilityFunctionOperator;
	private ProbabilityFunctionOperator secondProbabilityFunctionOperator;
		
	public float eval() throws ProbabilityFunctionEvalException {
		
		if (uniqueOperand != null) {
			
			// Define the first number.
			if (firstProbabilityFunctionOperator != null) {
				firstNumber = firstProbabilityFunctionOperator.eval();
			} else if (Float.isNaN(firstNumber)) {
				throw new ProbabilityFunctionEvalException("First operand is missing!");
			}
			
			// Define the second number.
			if (secondProbabilityFunctionOperator != null) {
				secondNumber = secondProbabilityFunctionOperator.eval();
			} else if (Float.isNaN(secondNumber)) {
				throw new ProbabilityFunctionEvalException("Second operand is missing!");
			}
			
		}
		
		// The evaluation itself.
		switch(this) {
	        case MIN:   return firstNumber < secondNumber ? firstNumber : secondNumber;
	        case MAX:   return firstNumber > secondNumber ? firstNumber : secondNumber;
	        case PLUS:  return firstNumber + secondNumber;
	        case MINUS: return firstNumber - secondNumber;
	        case TIMES:  return firstNumber * secondNumber;
	        case DIVIDE: return firstNumber / secondNumber;
	        // TODO Calcular a cardinalidade!!!
	        case CARDINALITY: return -1;
	        case REFERENCE: try {
								probabilityFunction.evalStateFunction(uniqueOperand);
							} catch (StateFunctionNotFoundException e) {
								throw new ProbabilityFunctionEvalException("Unable to eval the expression!", e);
							};
	    }
	    throw new ProbabilityFunctionEvalException("Unknown operator: " + this + "!");

	}
	
	/**
	 * Method responsible for setting the probability function this operator 
	 * belongs to.
	 * @param probabilityFunction The probability function this operator 
	 * belongs to.
	 */
	public void setProbabilityFunction(ProbabilityFunction probabilityFunction) {
		this.probabilityFunction = probabilityFunction;
	}
	
	public void setFirstOperand(ProbabilityFunctionOperator firstOperand) throws InvalidProbabilityFunctionOperandException {
		isTowOperandsValid();
		this.firstProbabilityFunctionOperator = firstOperand;
		firstNumber = Float.NaN;
		uniqueOperand = null;
	}
	
	public void setSecondOperand(ProbabilityFunctionOperator secondOperand) throws InvalidProbabilityFunctionOperandException {
		isTowOperandsValid();
		this.secondProbabilityFunctionOperator = secondOperand;
		secondNumber = Float.NaN;
		uniqueOperand = null;
	}
	
	public void setFirstOperand(float firstOperand) throws InvalidProbabilityFunctionOperandException {
		isTowOperandsValid();
		this.firstNumber = firstOperand;
		firstProbabilityFunctionOperator = null;
		uniqueOperand = null;
	}
	
	public void setSecondOperand(float secondOperand) throws InvalidProbabilityFunctionOperandException {
		isTowOperandsValid();
		this.secondNumber = secondOperand;
		secondProbabilityFunctionOperator = null;
		uniqueOperand = null;
	}
		
	private void isTowOperandsValid() throws InvalidProbabilityFunctionOperandException {
		if (this == ProbabilityFunctionOperator.CARDINALITY || this == ProbabilityFunctionOperator.REFERENCE) {
			throw new InvalidProbabilityFunctionOperandException(this + " is only valid with unique operand!");
		}
	}
	
	public void setUniqueOperand(String uniqueOperand) throws InvalidProbabilityFunctionOperandException {
		if (this != ProbabilityFunctionOperator.CARDINALITY && this != ProbabilityFunctionOperator.REFERENCE) {
			throw new InvalidProbabilityFunctionOperandException("Unique operand is only valid for " + ProbabilityFunctionOperator.CARDINALITY + " and " + ProbabilityFunctionOperator.REFERENCE + "!");
		}
		this.uniqueOperand = uniqueOperand;
	}
	
	public ProbabilityFunctionOperator getFirstOperand() {
		return firstProbabilityFunctionOperator;
	}
	
	public ProbabilityFunctionOperator getSecondOperand() {
		return secondProbabilityFunctionOperator;
	}

}
