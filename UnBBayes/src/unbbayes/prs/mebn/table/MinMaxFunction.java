package unbbayes.prs.mebn.table;

import java.util.List;

public class MinMaxFunction {
	
	private List<Object> firstFunction;
	private List<Object> secondFunction;
	private MinMaxOperation operation;

	public MinMaxFunction(MinMaxOperation operation) {
		this.operation = operation;
	}
	
	/**
	 * Method responsible for adding elements in the first function.
	 * @param element The element to be added.
	 */
	public void addFirstFunctionElement(Object element) {
		firstFunction.add(element);
	}
	
	/**
	 * Method responsible for adding elements in the second function.
	 * @param element The element to be added.
	 */
	public void addSecondFunctionElement(Object element) {
		secondFunction.add(element);
	}

	public MinMaxOperation getOperation() {
		return operation;
	}

}
