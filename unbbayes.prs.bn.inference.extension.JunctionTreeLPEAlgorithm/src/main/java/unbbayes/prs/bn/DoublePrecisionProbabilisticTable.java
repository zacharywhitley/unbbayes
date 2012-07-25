/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.Debug;

/**
 * This is a {@link ProbabilisticTable} which uses
 * double instead of double.
 * @author Shou Matsumoto
 *
 */
public class DoublePrecisionProbabilisticTable extends ProbabilisticTable {

	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName(),
  			Locale.getDefault(),
  			DoublePrecisionProbabilisticTable.class.getClassLoader());
	
//  	private List<Double> dataPTDouble = new ArrayList<Double>(0);
  	private DoubleCollection dataPTDouble = new DoubleCollection();
  	
	//	private List<Double> dataCopyDouble = null;
	private DoubleCollection dataCopyDouble = new DoubleCollection();

//	private boolean modified;
  	

	/**
	 * Default constructor is not private, in order to allow inheritance
	 */
	protected DoublePrecisionProbabilisticTable() {
		super();
		dataPT = null;
//		dataMarginal = null;	// probably we won't use this value
		dataCopy = null;
	}
	
	/** Default constructor method */
	public static DoublePrecisionProbabilisticTable getInstance() {
		return new DoublePrecisionProbabilisticTable();
	}

	/**
	 *  Remove variable from table. This method can also be used for general marginalization.
	 *
	 *@param  variavel : variable to be removed from table.
	 */
	public void removeVariable(unbbayes.prs.INode variavel) {
		computeFactors();
		int index = variableList.indexOf(variavel);
		if (variavel.getType() == Node.DECISION_NODE_TYPE) {
			DecisionNode decision = (DecisionNode) variavel;
			int statesSize = variavel.getStatesSize();
			if (decision.hasEvidence()) {
				finding(variableList.size()-1, index, new int[variableList.size()], decision.getEvidence());
			} else {
				sum(index);
				for (int i = dataPTDouble.size-1; i >= 0; i--) {
					dataPTDouble.data[i] = dataPTDouble.data[i] / statesSize;
				}
			}
		} else {
		  sum(index);
		}
		notifyModification();
		variableList.remove(index);
	}

	/**
	 * Remove the variable of the table. 
	 * 
	 * Note: 
	 * Substitute the previous method removeVariable(Node variable)
	 *
	 * @param variable  Variable to be removed
	 * @param normalize True if is to normalize the cpt after the node remotion
	 */	
	public void removeVariable(unbbayes.prs.INode variable, boolean normalize){
		int index = variableList.indexOf(variable);
		if (index < 0) {
			// variable not found. Ignore it.
			return;
		}
		computeFactors();
		
		if (variable.getType() == Node.DECISION_NODE_TYPE) {
			DecisionNode decision = (DecisionNode) variable;
			int statesSize = variable.getStatesSize();
			if (decision.hasEvidence()) {
				finding(variableList.size()-1, index, new int[variableList.size()], decision.getEvidence());
			} else {
				sum(index);
				if(normalize){
					for (int i = dataPTDouble.size-1; i >= 0; i--) {
						dataPTDouble.data[i] = dataPTDouble.data[i] / statesSize;
					}
				}
			}
		} else if (variableList.size() <= 1) {
			// we are removing the only probabilistic node in this potential table, so we need neither to sum-out nor to normalize.
			dataPTDouble.size = 0;
		} else {
			sum(index);
			if(normalize){
				int statesSize = variable.getStatesSize();
				for (int i = dataPTDouble.size-1; i >= 0; i--) {
					dataPTDouble.data[i] = dataPTDouble.data[i] / statesSize;
				}
			}
		}
		notifyModification();
		variableList.remove(index);
	}
	
	/**
	 *  Check the consistency of the property of this table.
	 *
	 * @throws Exception if table sums up to 100% for the states
	 *				   given parent's states.
	 */
	public void verifyConsistency() throws Exception {
		Node auxNo = variableList.get(0);
		int noLin = auxNo.getStatesSize();

		/* Check if the node represents a numeric attribute */
		if (noLin == 0) {
			/* 
			 * The node represents a numeric attribute which has no potential
			 * table. Just Return.
			 */
			return;
		}
		
		int noCol = 1;
		int sizeVariaveis = variableList.size();
		for (int k = 1; k < sizeVariaveis; k++) {
			auxNo = variableList.get(k);
			noCol *= auxNo.getStatesSize();
		}

		double soma;
		for (int j = 0; j < noCol; j++) {
			soma = 0;
			for (int i = 0; i < noLin; i++) {
				soma += dataPTDouble.data[j * noLin + i] * 100;
			}

			if (Math.abs(soma - 100.0) > 0.01) {
				throw new Exception(resource.getString("variableTableName") + variableList.get(0) + resource.getString("inconsistencyName") + soma + "%\n");
			}
		}
	}
	

	/**
	 * Returns a new instance of a ProbabilisticTable. Implements the abstract method from PotentialTable.
	 * @return a new instance of a ProbabilisticTable.
	 */
	public PotentialTable newInstance() {
		return DoublePrecisionProbabilisticTable.getInstance();
	}
	
	
	/**
	 * Creates a copy of the data from the table.
	 */
	public void copyData() {
		dataCopyDouble.size = dataPTDouble.size;
		dataCopyDouble.data = new double[dataPTDouble.size];
		System.arraycopy(dataPTDouble.data, 0, dataCopyDouble.data, 0, dataPTDouble.size);
	}
	
	/**
	 * Restores the data from the table using its stored copy.
	 */
	public void restoreData() {
		int dataSize = dataPTDouble.size;
		for (int i = 0; i < dataSize; i++) {
			dataPTDouble.data[i] = dataCopyDouble.data[i];
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#addValueAt(int, float)
	 */
	public void addValueAt(int index, float value) {
		throw new UnsupportedOperationException();
//		this.addValueAt(index, (double)value);
	}
	
	public void addValueAt(int index, double value) {
		dataPTDouble.add(index, value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#removeValueAt(int)
	 */
	public  void removeValueAt(int index) {
		dataPTDouble.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#tableSize()
	 */
	public int tableSize() {
	   return dataPTDouble.size;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#setTableSize(int)
	 */
	@Deprecated
	public void setTableSize(int size) {
		dataPTDouble.size = size;
	}

	/**
	 * Returns a copy of the data from the table.
	 * 
	 * @return A copy of the data from the table.
	 */
	public Object clone() {
		DoublePrecisionProbabilisticTable auxTab = (DoublePrecisionProbabilisticTable) newInstance();
		// perform fast arraylist copy
		auxTab.variableList = new ArrayList<Node>(variableList);
		// perform fast array copy
		auxTab.dataPTDouble.size = dataPTDouble.size;
		auxTab.dataPTDouble.data = new double[dataPTDouble.size];
		System.arraycopy(dataPTDouble.data, 0, auxTab.dataPTDouble.data, 0, dataPTDouble.size);
		auxTab.setSumOperation(this.getSumOperation());
		return auxTab;
	}
	
	/**
	 * Returns a copy of the data from the table associated with the new 
	 * node, instead of the original node. Useful when cloning a node.
	 * This method assumes that: <br/><br/>
	 *   1. The size of the new node table is the same as this node table being copied. <br/><br/>
	 *   2. If you want an exact copy, the parents in the new node have to be in the same
	 *      order they were in this node being copied, i.e., the variableList order is the same.<br/><br/>
	 * 
	 * @return A copy of the data from the table associated with the new node.
	 */
	public Object clone(ProbabilisticNode newNode) {
		DoublePrecisionProbabilisticTable auxTab = (DoublePrecisionProbabilisticTable) newInstance();
		auxTab.addVariable(newNode);
		for (Node node : newNode.getParents()) {
			auxTab.addVariable(node);
		}
		int sizeDados = dataPTDouble.size;
		for (int c = 0; c < sizeDados; c++) {
			auxTab.dataPTDouble.add(dataPTDouble.data[c]);
		}
		return auxTab;
	}
	
	/**
	 * @deprecated use {@link #setValue(int[], double)
	 * @see unbbayes.prs.bn.PotentialTable#setValue(int[], float)
	 */
	public void setValue(int[] coord, float value) {
		throw new UnsupportedOperationException();
//		this.setValue(coord, (double)value);
	}

	/**
	 * Set a value in the table using the multidimensional coordinate, 
	 * which is a list containing the state of each variable in the table.
	 * 
	 * @param coord
	 *            The multidimensional coordinate, which is a list containing 
	 *            the state of each variable in the table.
	 * @param value
	 *            The value to be set in the table.
	 */
	public void setValue(int[] coord, double value) {
		dataPTDouble.data[getLinearCoord(coord)] = value;
	}

	/**
	 * @deprecated use {@link #setValue(int, double)}
	 * @see unbbayes.prs.bn.PotentialTable#setValue(int, float)
	 */
	public  void setValue(int index, float value) {
		throw new UnsupportedOperationException();
//		this.setValue(index, (double)value);
	}
	
	/**
	 * Set a value in the table using the linear coordinate, 
	 * which corresponds to the state of each variable in the table.
	 * 
	 * @param index
	 *            The linear coordinate, which corresponds to the state 
	 *            of each variable in the table.
	 * @param value
	 *            The value to be set in the table.
	 */
	public  void setValue(int index, double value) {
		dataPTDouble.data[index] = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#setValues(float[])
	 */
	public  void setValues(float values[]) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Equivalent to {@link #setValue(int, double)}, but for all
	 * possible indexes.
	 * @param index
	 * @param values
	 */
	public  void setValues(double values[]) {
		dataPTDouble.data = new double[values.length];
		System.arraycopy(values, 0, dataPTDouble.data, 0, values.length);
	}

	/**
	 * @deprecated use {@link #getDoubleValue(int)} instead
	 */
	public float getValue(int index) {
		throw new UnsupportedOperationException();
//		return (float) this.getDoubleValue(index);
	}
	
	/**
	 * It returns the value of a cell identified by an index
	 * 
	 * @param index
	 *            linear index of a cell
	 * @return a value found in the specified cell .
	 */
	public double getDoubleValue(int index) {
		return dataPTDouble.data[index];
	}
	
	/**
	 * @deprecated use {@link #getDoubleValues()} instead
	 */
	@Deprecated
	public float[] getValues() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * It returns all values you can get from {@link #getValue(int)}.
	 * Be careful when you modify these values, because it will modify the 
	 * original values as well.
	 * @return
	 */
	public double[] getDoubleValues() {
		return dataPTDouble.data;
	}

	/**
	 * @deprecated use {@link #getDoubleValue(int[])} instead
	 */
	public  float getValue(int[] coordinate) {
		throw new UnsupportedOperationException();
//		return (float) this.getDoubleValue(coordinate);
	}
	
	/**
	 * It returns a value in this table from a vector of coordinates.
	 * 
	 * @param coordinate
	 * 			the coordinates of the value to be returned.
	 * @return a value of a cell specified by coordinate.
	 */
	public  double getDoubleValue(int[] coordinate) {
		return dataPTDouble.data[getLinearCoord(coordinate)];
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#addVariable(unbbayes.prs.Node)
	 */
	public void addVariable(INode newVariable) {
		/** @TODO reimplement it using correct format. */
		notifyModification();
		int numStatesOfNewVar = newVariable.getStatesSize();
//		int previousTableSize = this.dataPTDouble.size;
		if (variableList.size() == 0) {
			// this is the first time we add a variable to this CPT. Initialize with zeros
			dataPTDouble.size = numStatesOfNewVar;
			dataPTDouble.data = new double[numStatesOfNewVar];
			for (int i = 0; i < numStatesOfNewVar; i++) {
				dataPTDouble.data[i] = 0;	
			}
			// the above code substutes the following code, because dataPTDouble.add(0) was quite slow...
//			for (int i = 0; i < noEstados; i++) {
//				dataPTDouble.add(0);
//			}
		} else {
			// the table will be expanded to this size
			int newTableSize = numStatesOfNewVar * dataPTDouble.size;
			// remember old values, because we are going to copy them into newer cells
			int oldSize = dataPTDouble.size;	// Unfortunately, dataPTDouble.size can be different from dataPTDouble.data.length
			double[] oldValues = dataPTDouble.data;
			dataPTDouble.size = newTableSize;
			dataPTDouble.data = new double[newTableSize];
			// duplicate the cells
			for (int i = 0; i < numStatesOfNewVar; i++) {
				System.arraycopy(oldValues, 0, dataPTDouble.data, i*oldSize, oldSize);
			}
			
			// the above code substitutes the following code, because dataPTDouble.add is quite slow...
			
//			while (numStatesOfNewVar > 1) {
//				numStatesOfNewVar--;
//				for (int i = 0; i < previousTableSize; i++) {
//					dataPTDouble.add(dataPTDouble.data[i]);
//				}
//			}
		}
		variableList.add((Node)newVariable);
	}
	
	/**
	 * Move a variable of position. Only the variables are moved... the data of
	 * the table aren't changed. 
	 * 
	 * ATENTION: Use this method only if you need to fill the table manually and want choice
	 * the order of the variables. Use before to fill the data. 
	 * 
	 * Pre-requisites: 
	 *     - The table has size > initialPosition and size > destinationPosition
	 * 
	 * @param initialPosition
	 * @param destinationPosition
	 */
	public void moveVariableWithoutMoveData(int initialPosition, int destinationPosition){
		
		Node nodeToMove = variableList.remove(initialPosition); 
		variableList.add(destinationPosition, nodeToMove); 
		
	}
	
	public int getVariablesSize(){
		return variableList.size(); 
	}

	
	
	protected void sum(int index) {
		boolean marked[]  = new boolean[dataPTDouble.size];	
		if ( getSumOperation() == null) {
			// ensure the operation exists
			setSumOperation(DEFAULT_MARGINALIZATION_OP);
		}
		sumAux(variableList.size() - 1, index, 0, 0, marked);
		
		int j = 0;
		for (int i = 0; i < dataPTDouble.size; i++) {
			if (marked[i]) {
				continue;				
			}
			dataPTDouble.data[j++] = dataPTDouble.data[i];
		}
		
		dataPTDouble.size = j;
	}
	
	
	/**
	 * Auxiliary method for sum().
	 * Recursively sums all the values of the variable with the index specified. 
	 * @param control
	 *            Control index for the recursion. Call with the value
	 *            'variaveis.size - 1'
	 * @param index
	 *            Index of the variable to delete from the table
	 * @param coord
	 *            The current iterated linear coordinate of the table. 
	 *            Call with 0
	 * @param base
	 * 			  Represents the number that needs to be subtracted from coord to reach the first coordinate of the variable.
	 *            Call with 0.
	 * @param marked
	 * 			  The removed cells will be marked as 'true'.
	 *            Call with an array of falses.
	 * TODO convert this to a non-recursive fast method.
	 */
	private void sumAux(int control, int index, int coord, int base, boolean[] marked) {
		if (control == -1) {
			// concentrate the sum on the first cell. 
			int linearCoordDestination = coord - base;
			
			double value = (double) getSumOperation().operate(dataPTDouble.data[linearCoordDestination], dataPTDouble.data[coord]);
			dataPTDouble.data[linearCoordDestination] = value;
			marked[coord] = true;
			return;
		}
		
		Node node = variableList.get(control);
		if (control == index) {
			// if the current iterated variable is the one we want to delete, then iterate only until 1,
			// because the position 0 will hold the sum. 
			for (int i = node.getStatesSize()-1; i >= 1; i--) {
				sumAux(control-1, index, coord + i*factorsPT[control], i*factorsPT[index], marked);
			}	
		} else {
			for (int i = node.getStatesSize()-1; i >= 0; i--) {
				sumAux(control-1, index, coord + i*factorsPT[control], base, marked);
			}
		}
	}
	
	

	protected void finding(int control, int index, int coord[], int state) {
		if (control == -1) {
			int linearCoordToKill = getLinearCoord(coord);
			if (coord[index] == state) {
				int linearCoordDestination = linearCoordToKill - coord[index]*factorsPT[index];
				double value = dataPTDouble.data[linearCoordToKill];
				dataPTDouble.data[linearCoordDestination] = value;
			}
			dataPTDouble.remove(linearCoordToKill);
			return;
		}

		int fim = (index == control) ? 1 : 0;
		Node node = variableList.get(control);
		for (int i = node.getStatesSize()-1; i >= fim; i--) {
			coord[control] = i;
			finding(control-1, index, coord, state);
		}
	}





	/**
	 * Get the multidimensional coordinate from the linear one.
	 * <br/>
	 * A multidimensional coordinate is an array representing 
	 * the states of the variable {@link #getVariableAt(int)}.
	 * for example, the coordinate [3,4,1] represents the cell in the table
	 * in which {@link #getVariableAt(0)} is in state 3,
	 * {@link #getVariableAt(1)} is in state 4,
	 * and {@link #getVariableAt(2)} is in state 1.
	 * @param linearCoord The linear coordinate.
	 * @return The corresponding multidimensional coordinate.
	 */
	public  int[] getMultidimensionalCoord(int linearCoord) {
		computeFactors();
		int fatorI;
		int sizeVariaveis = variableList.size();
		int coord[] = new int[sizeVariaveis];
		int i = sizeVariaveis - 1;
		while (linearCoord != 0) {
			fatorI = factorsPT[i];
			coord[i--] = linearCoord / fatorI;
			linearCoord %= fatorI;
		}
		return coord;
	}
	
	/**
	 * @deprecated use {@link #directOpTab(DoublePrecisionProbabilisticTable, int)} instead
	 * @see unbbayes.prs.bn.PotentialTable#directOpTab(unbbayes.prs.bn.PotentialTable, int)
	 */
	public  void directOpTab(PotentialTable tab, int operator) {
		if (!(tab instanceof DoublePrecisionProbabilisticTable)) {
			throw new IllegalArgumentException("Only instances of " + DoublePrecisionProbabilisticTable.class.getName() + " allowed.");
		}
		directOpTab((DoublePrecisionProbabilisticTable)tab, operator);
	}

	/**
	 * Operates with the argument table directly.
	 * 
	 * @param table
	 *            table to operate.
	 * @param operator
	 *            operator to use, defined in this class constants.
	 */
	public  void directOpTab(DoublePrecisionProbabilisticTable tab, int operator) {
		if (tableSize() != tab.tableSize()) {
			throw new RuntimeException(resource.getString("TableSizeException") + ": " + tableSize() + " " + tab.tableSize());
		}
		
		switch (operator) {
			case PRODUCT_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dataPTDouble.data[k] *= tab.dataPTDouble.data[k];
				}
				break;
			 
			case DIVISION_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					if (tab.dataPTDouble.data[k] != 0) {
						dataPTDouble.data[k] /= tab.dataPTDouble.data[k];
					} else {
						dataPTDouble.data[k] = 0;						
					}
				}
				break;
			
			case MINUS_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dataPTDouble.data[k] -= tab.dataPTDouble.data[k];
				}
				break;
		}
	}

	public  void opTab(PotentialTable tab, int operator) {
		opTab((DoublePrecisionProbabilisticTable)tab, operator);
	}

	/**
	 * Performs a operation between this table and another
	 * 
	 * @param tab
	 *            the table to be operated with this one.
	 * @param operator
	 *            an identifier of the operation to be used. 
	 *            		E.g.
	 *            {@link PotentialTable#PRODUCT_OPERATOR},
	 *            {@link PotentialTable#PLUS_OPERATOR},
	 *            {@link PotentialTable#DIVISION_OPERATOR}
	 *  @see PotentialTable#PRODUCT_OPERATOR
	 *  @see PotentialTable#PLUS_OPERATOR
	 *  @see PotentialTable#DIVISION_OPERATOR
	 *  
	 */
	public  void opTab(DoublePrecisionProbabilisticTable tab, int operator) {		
		int[] index = new int[variableList.size()];
		for (int c = variableList.size()-1; c >= 0; c--) {
			index[c] = tab.variableList.indexOf(variableList.get(c));
		}
		computeFactors();
		tab.computeFactors();
		
		switch (operator) {
			case PRODUCT_OPERATOR:
				fastOpTabProd(0, 0, 0, index, tab);
				break;
				
			case PLUS_OPERATOR:
				fastOpTabPlus(0, 0, 0, index, tab);
				break;
			
			case DIVISION_OPERATOR:
				fastOpTabDiv(0, 0, 0, index, tab);
				break;
				
			default:
		}
	}
	
	
	private void fastOpTabPlus(int c, int linearA, int linearB, int index[], DoublePrecisionProbabilisticTable tab) {
		if (c >= variableList.size()) {			
			dataPTDouble.data[linearA] += tab.dataPTDouble.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*factorsPT[c] , linearB, index, tab);
			}
		} else {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*factorsPT[c] , linearB + i*tab.factorsPT[index[c]], index, tab);
			}
		}
	}

	private void fastOpTabProd(int c, int linearA, int linearB, int index[], DoublePrecisionProbabilisticTable tab) {
		// TODO stop using slow, recursive method
		if (c >= variableList.size()) {
			dataPTDouble.data[linearA] *= tab.dataPTDouble.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabProd(c+1, linearA + i*factorsPT[c] , linearB, index, tab);
			}
		} else {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabProd(c+1, linearA + i*factorsPT[c] , linearB + i*tab.factorsPT[index[c]], index, tab);
			}
		}
	}
	
	private void fastOpTabDiv(int c, int linearA, int linearB, int index[], DoublePrecisionProbabilisticTable tab) {
		if (c >= variableList.size()) {
			dataPTDouble.data[linearA] /= tab.dataPTDouble.data[linearB];
			return;						
		}
		if (index[c] == -1) {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabDiv(c+1, linearA + i*factorsPT[c] , linearB, index, tab);
			}
		} else {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabDiv(c+1, linearA + i*factorsPT[c] , linearB + i*tab.factorsPT[index[c]], index, tab);
			}
		}
	}

//	/**
//	 * @deprecated use {@link #updateRecursive(double[], int, int, int, int)} instead
//	 */
//	protected void updateRecursive(float[] marginalList, int c, int linear, int index, int state) {
//		throw new UnsupportedOperationException();
//	}
	
	/**
	 * Recursively accesses the cells of the CPT and updates its values using
	 * the values in marginalList.
	 * This is used by {@link #updateEvidences(double[], int)}.
	 * Overwrite this method if you need {@link #updateEvidences(double[], int)} to change
	 * its behavior.
	 * It assumes {@link #computeFactors()} was run prior to this method.
	 * @param marginalList
	 * @param c
	 * @param linear
	 * @param index
	 * @param state
	 */
//	protected void updateRecursive(double[] marginalList, int c, int linear, int index, int state) {
	protected void updateRecursive(float[] marginalList, int c, int linear, int index, int state) {
		if (index < 0) {
			return;
		}
    	if (c >= this.variableList.size()) {
    		this.dataPTDouble.data[linear] *= marginalList[state];
    		return;    		    		
    	}
    	
    	if (index == c) {
    		for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(marginalList, c+1, linear + i*this.factorsPT[c] , index, i);
    		}
    	} else {
	    	for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(marginalList, c+1, linear + i*this.factorsPT[c] , index, state);
    		}
    	}
    }
	
//	/**
//	 * @deprecated use {@link #updateEvidences(double[], int)}
//	 * @see unbbayes.prs.bn.PotentialTable#updateEvidences(float[], int)
//	 */
//	public void updateEvidences(float[] marginalList, int index) {
//		throw new UnsupportedOperationException();
//	}
//
//	/**
//	 * Given a node identified by an index, updates its values
//	 * using an array of double.
//	 * For a probabilistic CPT, this method shall multiply the cells
//	 * of the CPT related to the variable in index using the 
//	 * values in marginalList. This can be used for implementing
//	 * hard evidence.
//	 * @param marginalList : values for updating
//	 * @param index : index of the node related to the cells to update.
//	 * The node can be obtained by calling {@link #getVariableAt(int)}
//	 */
//	public void updateEvidences(double[] marginalList, int index) {
//		this.computeFactors();
//		this.updateRecursive(marginalList, 0, 0, index, 0);
//	}
	
	/**
	 * @deprecated use {@link #normalizeDouble()} instead
	 * @see unbbayes.prs.bn.PotentialTable#normalize()
	 */
	public float normalize()  {
		Debug.println(getClass(), "Deprecated operation \"normalize\" called. Recommended \"normalizeDouble\"");
		return (float) this.normalizeDouble();
	}
	
	/**
	 * Default implementation of normalization.
	 * It normalizes current content of table (the sum is going to be 1).
	 * @return normalization factor (sum of the cells of this table); if table is already normalized, this is 1.
	 * @throws IllegalStateException : when an inconsistency or underflow is found
	 * TODO migrate this method to ProbabilisticTable
	 */
	public double normalizeDouble()  {
        double n = 0;
        double valor;

        int sizeDados = this.tableSize();
        for (int c = 0; c < sizeDados; c++) {
            n += this.getValue(c);
        }
        if (Math.abs(n - 1.0) > 0.001) {
            for (int c = 0; c < sizeDados; c++) {
                valor = this.getValue(c);
                if (valor == 0.0) {
                	// zeros will remain always zero.
                	continue;
                }
                if (n == 0.0) {
                    throw new IllegalStateException(resource.getString("InconsistencyUnderflowException"));
                }
                valor /= n;
                this.setValue(c, valor);
            }
        }
        return n;
    }

	/**
	 * @return the dataPTDouble
	 */
	protected DoubleCollection getDataPTDouble() {
		return dataPTDouble;
	}

	/**
	 * @param dataPTDouble the dataPTDouble to set
	 */
	protected void setDataPTDouble(DoubleCollection dataPTDouble) {
		this.dataPTDouble = dataPTDouble;
	}

	/**
	 * @return the dataCopyDouble
	 */
	protected DoubleCollection getDataCopyDouble() {
		return dataCopyDouble;
	}

	/**
	 * @param dataCopyDouble the dataCopyDouble to set
	 */
	protected void setDataCopyDouble(DoubleCollection dataCopyDouble) {
		this.dataCopyDouble = dataCopyDouble;
	}
	
}
