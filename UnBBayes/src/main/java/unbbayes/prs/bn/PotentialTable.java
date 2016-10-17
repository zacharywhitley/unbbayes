/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.bn;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.util.FloatCollection;
import unbbayes.util.SetToolkit;


/**
 * This is a conditional potential table
 * 
 * @author Michael, Rommel
 * @version September, 21th, 2001
 */
public abstract class PotentialTable implements Cloneable, java.io.Serializable, IProbabilityFunction {
	public static final int PRODUCT_OPERATOR = 0;
	public static final int DIVISION_OPERATOR = 1;
	public static final int PLUS_OPERATOR = 2;
	public static final int MINUS_OPERATOR = 3;
	
	/** Instance called when doing marginalization. This instance sum-out values in {@link PotentialTable#removeVariable(INode)} */
	public static final ISumOperation DEFAULT_MARGINALIZATION_OP = new PotentialTable() {
			// this is a stub implementation of PotentialTable created  just in order to instantiate SumOperation
			public void removeVariable(INode variable) {}
			public void removeVariable(INode variable, boolean normalize) {}
			public void purgeVariable(INode variable, boolean normalize) {}
			public PotentialTable newInstance() { return null; }
			public PotentialTable getTemporaryClone() { return null; }
		}.new SumOperation();	// created an anonymous extension of PotentialTable just to instantiate inner class SumOperation

	private boolean modified;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());

	/**
	 * RVs that are in the table, including the variable that owns the table.
	 */
	protected List<Node> variableList;

	/**
	 * The data from the table as a list of floats. The use of this data 
	 * is done by using coordinates and linear coordinates.
	 */
	protected FloatCollection dataPT;
	
	/**
	 * Copy of the data from the data.
	 */
	protected FloatCollection dataCopy;
	
//	/**
//	 * The data from the table as a list of floats. Usually used to compute
//	 * the marginal probabilities "manually", i.e. by removing each and every
//	 * parent from the potential table. The use of this data is done by using  
//	 * coordinates and linear coordinates for the marginal data.
//	 */
//	protected FloatCollection dataMarginal;

	/**
	 * Factors used to convert linear coordinates in multidimensional ones. 
	 * It stores the position that starts the table of each variable, for the
	 * potential table data.
	 */
	protected int[] factorsPT;
	
	/**
	 * Factors used to convert linear coordinates in multidimensional ones. 
	 * It stores the position that starts the table of each variable, for the
	 * marginal data.
	 */
	protected int[] factorsMarginal;
	
	private ISumOperation sumOperation;
	private boolean isRemovedCellInDataPT[] = null;
	
	/** This is the singleton value enabled when {@link #isToUseSingletonArrayOfRemovedCellInDataPT()} is true */
	private static boolean[] singletonArrayOfRemovedCellInDataPT;
	
	private static boolean isToUseSingletonArrayOfRemovedCellInDataPT = true;
	
	/**
	 * Initialize data and variables.
	 */
	public PotentialTable() {
		modified = true;
		dataPT = new FloatCollection();
		dataCopy = new FloatCollection();
//		dataMarginal = new FloatCollection();
		variableList = new ArrayList<Node>();
		
		this.setSumOperation(DEFAULT_MARGINALIZATION_OP);
	}
	
	/**
	 * Creates a copy of the data from the table.
	 */
	public void copyData() {
		dataCopy.size = dataPT.size;
		if (dataCopy.data.length < dataPT.size) {
			dataCopy.data = new float[dataPT.size];
		}
		System.arraycopy(dataPT.data, 0, dataCopy.data, 0, dataPT.size);
//		int dataSize = dataPT.size;
//		dataCopy.ensureCapacity(dataSize);
//		dataCopy.size = dataSize;
//		for (int i = 0; i < dataSize; i++) {
//			dataCopy.data[i] = dataPT.data[i];
//		}
	}
	
	/**
	 * Accesses the content of data copied by
	 * {@link #copyData()}
	 * @param index : the index to be accessed.
	 * @return the value at the provided index.
	 * @see #getValue(int)
	 * @see {@link #getCopiedValue(int[])}
	 */
	public float getCopiedValue(int index) {
		return dataCopy.data[index];
	}

	/**
	 * Accesses the content of data copied by
	 * {@link #copyData()}
	 * @param coordinate : multi-dimensional coordinate.
	 * @return the value at the provided coordinate
	 * @see #getMultidimensionalCoord(int)
	 * @see #getValue(int[])
	 * @see #getCopiedValue(int)
	 */
	public float getCopiedValue(int[] coordinate) {
		return dataCopy.data[getLinearCoord(coordinate)];
	};
	
	/**
	 * Restores the data from the table using its stored copy.
	 */
	public void restoreData() {
//		int dataSize = dataPT.size;
//		for (int i = 0; i < dataSize; i++) {
//			dataPT.data[i] = dataCopy.data[i];
//		}
		// the following is a faster version of the above code
		System.arraycopy(dataCopy.data, 0, dataPT.data, 0, dataPT.size);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#notifyModification()
	 */
	public void notifyModification() {
	   modified = true;
	}

	/**
	 * Returns a copy of the variables in this table.
	 * 
	 * @return A copy of the variables in this table.
	 */
	public List<Node> cloneVariables() {
		return SetToolkit.clone(variableList);
	}

	public  int indexOfVariable(Node node) {
		return variableList.indexOf(node);
	}
	
	//by young2010
	public  int indexOfVariable(String nodeName){
		for(int i = 0; i < variableList.size(); i++){
			if(variableList.get(i).getName().equals(nodeName) ){
				return i; 
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#variableCount()
	 */
	public int variableCount() {
		return variableList.size();
	}

	public void setVariableAt(int index, INode node) {
		notifyModification();
		variableList.set(index, (Node)node);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#getVariableAt(int)
	 */
	public INode getVariableAt(int index) {
		return variableList.get(index);
	}
	
	public  int getVariableIndex(Node variable){
		for(int i = 0; i < variableList.size(); i++){
			if(variableList.get(i).equals(variable)){
				return i; 
			}
		}
		return -1; 
	}

	public void addValueAt(int index, float value) {
		dataPT.add(index, value);
	}
	
	public  void removeValueAt(int index) {
		dataPT.remove(index);
	}

	public int tableSize() {
	   return dataPT.size;
	}
	
	// FIXME ROMMEL - THIS SHOULD NOT BE HERE!! FIX BUG REPORTED ABOUT ERROR WHEN REMOVING STATE FROM NODE
	@Deprecated
	public void setTableSize(int size) {
		dataPT.size = size;
	}

	/**
	 * Returns a copy of the data from the table.
	 * 
	 * @return A copy of the data from the table.
	 */
	public Object clone() {
		PotentialTable auxTab = newInstance();
		// perform fast arraylist copy
		auxTab.variableList = new ArrayList<Node>(variableList);
		// perform fast array copy
		auxTab.dataPT.size = dataPT.size;
		auxTab.dataPT.data = new float[dataPT.size];
		System.arraycopy(dataPT.data, 0, auxTab.dataPT.data, 0, dataPT.size);
//		int sizeDados = dataPT.size;
//		for (int c = 0; c < sizeDados; c++) {
//			auxTab.dataPT.add(dataPT.data[c]);
//		}
		auxTab.setSumOperation(this.getSumOperation());
		return auxTab;
	}
	
	/**
	 * Returns a copy of the data from the table,
	 * but some optimizations may be performed by implementations (subclasses), assuming that
	 * the copy will only be alive temporary.
	 * @return A copy of the data from the table.
	 * @see unbbayes.prs.bn.JunctionTree
	 */
	public abstract PotentialTable getTemporaryClone();
	
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
		PotentialTable auxTab = newInstance();
		auxTab.addVariable(newNode);
		for (Node node : newNode.getParents()) {
			auxTab.addVariable(node);
		}
		int sizeDados = dataPT.size;
		for (int c = 0; c < sizeDados; c++) {
			auxTab.dataPT.add(dataPT.data[c]);
		}
		return auxTab;
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
	public void setValue(int[] coord, float value) {
		dataPT.data[getLinearCoord(coord)] = value;
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
	public  void setValue(int index, float value) {
		dataPT.data[index] = value;
	}
	
	/**
	 * Equivalent to {@link #setValue(int, float)}, but for all
	 * possible indexes.
	 * @param index
	 * @param values
	 */
	public  void setValues(float values[]) {
		dataPT.data = new float[values.length];
		System.arraycopy(values, 0, dataPT.data, 0, values.length);
	}

	/**
	 * It returns the value of a cell identified by an index
	 * 
	 * @param index
	 *            linear index of a cell
	 * @return a value found in the specified cell .
	 */
	public  float getValue(int index) {
		return dataPT.data[index];
	}
	
	/**
	 * It returns all values you can get from {@link #getValue(int)}.
	 * Be careful when you modify these values, because it will modify the 
	 * original values as well.
	 * @return
	 * @deprecated use {@link #getValue(int)} instead;
	 */
	@Deprecated
	public float[] getValues() {
		return dataPT.data;
	}

	/**
	 * It returns a value in this table from a vector of coordinates.
	 * 
	 * @param coordinate
	 * 			the coordinates of the value to be returned.
	 * @return a value of a cell specified by coordinate.
	 */
	public  float getValue(int[] coordinate) {
		return dataPT.data[getLinearCoord(coordinate)];
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#addVariable(unbbayes.prs.Node)
	 */
	public void addVariable(INode newVariable) {
		/** @TODO reimplement it using correct format. */
		notifyModification();
		int numStatesOfNewVar = newVariable.getStatesSize();
//		int previousTableSize = this.dataPT.size;
		if (variableList.size() == 0) {
			// this is the first time we add a variable to this CPT. Initialize with zeros
			dataPT.size = numStatesOfNewVar;
			dataPT.data = new float[numStatesOfNewVar];
			for (int i = 0; i < numStatesOfNewVar; i++) {
				dataPT.data[i] = 0;	
			}
			// the above code substutes the following code, because dataPT.add(0) was quite slow...
//			for (int i = 0; i < noEstados; i++) {
//				dataPT.add(0);
//			}
		} else {
			// the table will be expanded to this size
			int newTableSize = numStatesOfNewVar * dataPT.size;
			// remember old values, because we are going to copy them into newer cells
			int oldSize = dataPT.size;	// Unfortunately, dataPT.size can be different from dataPT.data.length
			float[] oldValues = dataPT.data;
			dataPT.size = newTableSize;
			dataPT.data = new float[newTableSize];
			// duplicate the cells
			for (int i = 0; i < numStatesOfNewVar; i++) {
				System.arraycopy(oldValues, 0, dataPT.data, i*oldSize, oldSize);
			}
			
			// the above code substitutes the following code, because dataPT.add is quite slow...
			
//			while (numStatesOfNewVar > 1) {
//				numStatesOfNewVar--;
//				for (int i = 0; i < previousTableSize; i++) {
//					dataPT.add(dataPT.data[i]);
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

	
	
	/**
	 * Remove the variable of the table. 
	 * For optimization, this can be implemented as a logical removal.
	 * 
	 * Note: 
	 * Substitute the previous method removeVariable(Node variable)
	 *
	 * @param variable  Variable to be removed
	 * @param normalize True if is to normalize the cpt after the node remotion
	 * @see #purgeVariable(INode, boolean)
	 */	
	public abstract void removeVariable(INode variable, boolean normalize); 
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#purgeVariable(unbbayes.prs.INode, boolean)
	 */
	public abstract void purgeVariable(INode variable, boolean normalize); 
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.IProbabilityFunction#removeVariable(unbbayes.prs.INode)
	 */
	public abstract void removeVariable(INode variable);
	
	/**
	 * Returns a new instance of a PotentialTable of the current implemented
	 * sub-class.
	 * 
	 * @return a new instance of a PotentialTable of the current implemented
	 *         sub-class.
	 */
	public abstract PotentialTable newInstance();

	/**
	 * Sum outs a variable from {@link #dataPT} (i.e. marginalizes out a variable from the CPT).
	 * @param index : index of the variable to sum out
	 * @see #variableList
	 * @see #isRemovedCellInDataPT()
	 * @see #getSumOperation()
	 * @see #sumAux(int, int, int, int, boolean[])
	 * @see #dataPT
	 */
	protected void sum(int index) {
		if (isToUseSingletonArrayOfRemovedCellInDataPT) {
			// use singleton instance of isRemovedCellInDataPT to avoid garbage
			if (singletonArrayOfRemovedCellInDataPT == null) {
				// instantiate new object
				synchronized(PotentialTable.class) {	// double checking avoids race condition
					if (singletonArrayOfRemovedCellInDataPT == null) {
						singletonArrayOfRemovedCellInDataPT = new boolean[dataPT.size];
					}
				}
			} 
			// check size (doble check - do not include in else clause of previous if - in case of race condition)
			if (singletonArrayOfRemovedCellInDataPT.length < dataPT.size) {
				singletonArrayOfRemovedCellInDataPT = new boolean[dataPT.size];	
			} else {
				// guarantee that all values starts from false
				for (int i = 0; i < singletonArrayOfRemovedCellInDataPT.length; i++) {
					singletonArrayOfRemovedCellInDataPT[i] = false;
				}
			}
			isRemovedCellInDataPT = singletonArrayOfRemovedCellInDataPT;
		} else {
			// do not use singleton instance of isRemovedCellInDataPT
			if (isRemovedCellInDataPT == null || isRemovedCellInDataPT.length < dataPT.size) {
				isRemovedCellInDataPT = new boolean[dataPT.size];	
			} else {
				// guarantee that all values starts from false
				for (int i = 0; i < isRemovedCellInDataPT.length; i++) {
					isRemovedCellInDataPT[i] = false;
				}
			}
		}
		if ( sumOperation == null) {
			// ensure the operation exists
			sumOperation = DEFAULT_MARGINALIZATION_OP;
		}
		sumAux(variableList.size() - 1, index, 0, 0, isRemovedCellInDataPT);
		
		int j = 0;
		for (int i = 0; i < dataPT.size; i++) {
			if (isRemovedCellInDataPT[i]) {
				continue;				
			}
			dataPT.data[j++] = dataPT.data[i];
		}
		
		dataPT.size = j;
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
		// TODO stop using slow, recursive methods
		if (control == -1) {
			// concentrate the sum on the first cell. 
			int linearCoordDestination = coord - base;
			
			float value = sumOperation.operate(dataPT.data[linearCoordDestination], dataPT.data[coord]);
			dataPT.data[linearCoordDestination] = value;
			marked[coord] = true;
			return;
		}
		
		int controlNodeStateSize = variableList.get(control).getStatesSize();
		int factorPTControl = factorsPT[control];
		if (control == index) {
			// if the current iterated variable is the one we want to delete, then iterate only until 1,
			// because the position 0 will hold the sum. 
			int factorPTIndex = factorsPT[index];
			for (int i = controlNodeStateSize-1; i >= 1; i--) {
				sumAux(control-1, index, coord + i*factorPTControl, i*factorPTIndex, marked);
			}	
		} else {
			for (int i = controlNodeStateSize-1; i >= 0; i--) {
				sumAux(control-1, index, coord + i*factorPTControl, base, marked);
			}
		}
	}
	
	/**
	 * Objects of this interface will be used by an internal method of {@link PotentialTable#sum(int)} 
	 * ({@link PotentialTable#sumAux(int, int, int, int, boolean[])}) as the main operation.
	 * The default must be + (the "add") for {@link PotentialTable#sum(int)} to implement
	 * literally the sum operation.
	 * 
	 * Creating new implementations of this object may be useful for changing the {@link PotentialTable#sum(int)}
	 * to perform a comparison (max or min) instead of a sum.
	 * @author Shou Matsumoto
	 *
	 */
	public interface ISumOperation {
		/**
		 * Perform an operation between 2 arguments
		 * @param arg1
		 * @param arg2
		 * @return
		 */
		float operate(float arg1, float arg2);
	}
	
	/**
	 * This is the default implementation of {@link ISumOperation}.
	 * By using this, {@link PotentialTable#sum(int)} behaves in the same
	 * manner it was behaving before Oct. 14, 2011.
	 * @author Shou Matsumoto
	 *
	 */
	public class SumOperation implements ISumOperation {
		/*
		 * (non-Javadoc)
		 * @see unbbayes.prs.bn.PotentialTable.ISumOperation#operate(float, float)
		 */
		public float operate(float arg1, float arg2) {
			return arg1 + arg2;
		}
		
	}
	
	/**
	 * Implementation of {@link ISumOperation} whose {@link #operate(float, float)}
	 * returns the grater of its arguments.
	 * This is useful for implementing MPE inference algorithm on junction trees.
	 * @author Shou Matsumoto
	 *
	 */
	public class MaxOperation implements ISumOperation {
		/**
		 * Returns the value which is grater
		 */
		public float operate(float arg1, float arg2) {
			return ((arg1>arg2)?arg1:arg2);
		}
	}
	

	protected void finding(int control, int index, int coord[], int state) {
		if (control == -1) {
			int linearCoordToKill = getLinearCoord(coord);
			if (coord[index] == state) {
				int linearCoordDestination = linearCoordToKill - coord[index]*factorsPT[index];
				float value = dataPT.data[linearCoordToKill];
				dataPT.data[linearCoordDestination] = value;
			}
			dataPT.remove(linearCoordToKill);
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
	 * Get the linear coordinate from the multidimensional one.
	 * LinearCoord = SumOf(StateOf[i] * FactorOf[i]), for all 
	 * possible nodes (i), which are the nodes in the table.
	 * <br/>
	 * A multidimensional coordinate is an array representing 
	 * the states of the variable {@link #getVariableAt(int)}.
	 * for example, the coordinate [3,4,1] represents the cell in the table
	 * in which {@link #getVariableAt(0)} is in state 3,
	 * {@link #getVariableAt(1)} is in state 4,
	 * and {@link #getVariableAt(2)} is in state 1.

	 * @param multidimensionalCoord Multidimensional coordinate (represented by the state for
	 * each node).
	 * @return The corresponding linear coordinate.
	 */
	public  int getLinearCoord(int multidimensionalCoord[]) {
		computeFactors();
		int coordLinear = 0;
		int sizeVariaveis = variableList.size();
		for (int v = 0; v < sizeVariaveis; v++) {
			coordLinear += multidimensionalCoord[v] * factorsPT[v];
		}
		return coordLinear;
	}


	/**
	 * Calculate the factors necessary to transform the linear coordinate into a multidimensional 
	 * one, which is a list containing the state of each variable in the table.
	 */
	protected void computeFactors() {
		if (! modified) {
			return;
		}
		modified = false;
		int sizeVariaveis = variableList.size();
		if (factorsPT == null || factorsPT.length < sizeVariaveis) {
		   factorsPT = new int[sizeVariaveis];
		}
		factorsPT[0] = 1;
		Node auxNo;
		for (int i = 1; i < sizeVariaveis; i++) {
			auxNo = variableList.get(i-1);
			factorsPT[i] = factorsPT[i-1] * auxNo.getStatesSize();
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
		if (modified) {
			// checking "modified" causes a redundant check (because computeFactors also does the same check), 
			// but in most of cases it's faster to check prior to calling methods than checking after, due to context change overhead
			computeFactors();
		}
		int fatorI;
		int sizeVariaveis = variableList.size();
		int coord[] = new int[sizeVariaveis];
		int i = sizeVariaveis - 1;
		// this assumes coord starts filled with 0
		while (linearCoord != 0) {
			fatorI = factorsPT[i];
			coord[i--] = linearCoord / fatorI;
			linearCoord %= fatorI;
		}
		return coord;
	}
	/**
	 * This is basically the same of {@link #getMultidimensionalCoord(int)},
	 * but it does not allocate new array (reuses array passed in its argument).
	 * Get the multidimensional coordinate from the linear one.
	 * <br/>
	 * A multidimensional coordinate is an array representing 
	 * the states of the variable {@link #getVariableAt(int)}.
	 * for example, the coordinate [3,4,1] represents the cell in the table
	 * in which {@link #getVariableAt(0)} is in state 3,
	 * {@link #getVariableAt(1)} is in state 4,
	 * and {@link #getVariableAt(2)} is in state 1.
	 * @param linearCoord The linear coordinate.
	 * @param coord : array of the corresponding multidimensional coordinate to be filled
	 * @return coord
	 */
	public  int[] getMultidimensionalCoord(int linearCoord, int coord[]) {
		if (modified) {
			// avoid context changes if we can detect it soon
			computeFactors();
		}
		int fatorI;
		int sizeVariaveis = variableList.size();
		// found out that simply calling a "for" loop is generally faster than most of array copy methods if coord is not too big 
		// (coord won't be too big anyway, because such a big coord means exponentially huge CPT, which means the propagation algorithm
		// itself will be too slow and we won't notice the difference in this method anyway)
		for (int i = 0; i < coord.length; i++) { coord[i] = 0; }
		int i = sizeVariaveis - 1;
		// the following loop assumes that coord starts filled with zeros
		while (linearCoord != 0) {
			fatorI = factorsPT[i];
			coord[i--] = linearCoord / fatorI;
			linearCoord %= fatorI;
		}
		return coord;
	}
	
	/**
	 * Get the linear coordinate from the multidimensional one.
	 * LinearCoord = SumOf(StateOf[i] * FactorOf[i]), for all 
	 * possible nodes (i), which are the nodes in the table.
	 * @param multidimensionalCoord Multidimensional coordinate (represented by the state for
	 * each node).
	 * @return The corresponding linear coordinate.
	 * @deprecated use {@link #getLinearCoord(int[])} instead.
	 */
	@Deprecated
	public  int getLinearCoordMarginal(int multidimensionalCoord[]) {
		computeFactorsMarginal();
		int coordLinear = 0;
		int sizeVariaveis = variableList.size();
		for (int v = 0; v < sizeVariaveis; v++) {
			coordLinear += multidimensionalCoord[v] * factorsPT[v];
		}
		return coordLinear;
	}


	/**
	 * Calculate the factors necessary to transform the linear coordinate into a multidimensional 
	 * one, which is a list containing the state of each variable in the table.
	 */
	protected void computeFactorsMarginal() {
		if (! modified) {
			return;
		}
		modified = false;
		int sizeVariaveis = variableList.size();
		if (factorsPT == null || factorsPT.length < sizeVariaveis) {
		   factorsPT = new int[sizeVariaveis];
		}
		factorsPT[0] = 1;
		Node auxNo;
		for (int i = 1; i < sizeVariaveis; i++) {
			auxNo = variableList.get(i-1);
			factorsPT[i] = factorsPT[i-1] * auxNo.getStatesSize();
		}
	}



	/**
	 * Get the multidimensional coordinate from the linear one.
	 * 
	 * @param linearCoord The linear coordinate.
	 * @return The corresponding multidimensional coordinate.
	 * @deprecated use {@link #getMultidimensionalCoord(int)} instead.
	 */
	@Deprecated
	public  int[] getMultidimensionalCoordMarginal(int linearCoord) {
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
	 * Operates with the argument table directly.
	 * 
	 * @param tab
	 *            table to operate.
	 * @param operator
	 *            operator to use, defined in this class constants.
	 *            The possible values are:
	 *            <br/>
	 *            <br/> #PLUS_OPERATOR
	 * 			  <br/> #MINUS_OPERATOR
	 * 			  <br/> #PRODUCT_OPERATOR
	 * 			  <br/> #DIVISION_OPERATOR
	 */
	public  void directOpTab(PotentialTable tab, int operator) {
		if (tableSize() != tab.tableSize()) {
			throw new RuntimeException(resource.getString("TableSizeException") + ": " + tableSize() + " " + tab.tableSize());
		}
		
		switch (operator) {
			case PRODUCT_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dataPT.data[k] *= tab.dataPT.data[k];
				}
				break;
			 
			case DIVISION_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					if (tab.dataPT.data[k] != 0) {
						dataPT.data[k] /= tab.dataPT.data[k];
					} else {
						dataPT.data[k] = 0;						
					}
				}
				break;
			
			case MINUS_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dataPT.data[k] -= tab.dataPT.data[k];
				}
				break;

			case PLUS_OPERATOR:
				for (int k = tableSize()-1; k >= 0; k--) {
					dataPT.data[k] += tab.dataPT.data[k];
				}
				break;
//			default:
//				throw new IllegalArgumentException(resource.getString("OperatorException") + " [" + operator + "]");
		}
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
	public  void opTab(PotentialTable tab, int operator) {		
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
				
//			default:
//				throw new IllegalArgumentException(resource.getString("OperatorException") + " [" + operator + "]");
		}
	}
	
	
	private void fastOpTabPlus(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		if (c >= variableList.size()) {			
			dataPT.data[linearA] += tab.dataPT.data[linearB];
			return;						
		}
		int currentFactor = factorsPT[c];
		if (index[c] == -1) {
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*currentFactor , linearB, index, tab);
			}
		} else {
			int currentTableFactor = tab.factorsPT[index[c]];
			for (int i = variableList.get(c).getStatesSize() - 1; i >= 0; i--) {						
				fastOpTabPlus(c+1, linearA + i*currentFactor , linearB + i*currentTableFactor, index, tab);
			}
		}
	}

	private void fastOpTabProd(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		// TODO stop using slow, recursive method
		if (c >= variableList.size()) {
			dataPT.data[linearA] *= tab.dataPT.data[linearB];
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
	
	private void fastOpTabDiv(int c, int linearA, int linearB, int index[], PotentialTable tab) {
		if (c >= variableList.size()) {
			dataPT.data[linearA] /= tab.dataPT.data[linearB];
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

	/**
	 * This is the operation performed in {@link #sum(int)}.
	 * By changing this, you can dynamically customize the behavior of
	 * {@link #sum(int)} (actually, {@link #sumAux(int, int, int, int, boolean[])}
	 * is the impacted method).
	 * @param sumOperation the sumOperation to set
	 * @see ISumOperation
	 */
	public void setSumOperation(ISumOperation sumOperation) {
		this.sumOperation = sumOperation;
	}

	/**
	 * This is the operation performed in {@link #sum(int)}.
	 * By changing this, you can dynamically customize the behavior of
	 * {@link #sum(int)} (actually, {@link #sumAux(int, int, int, int, boolean[])}
	 * is the impacted method).
	 * @return the sumOperation
	 */
	public ISumOperation getSumOperation() {
		return sumOperation;
	}
	
	/**
	 * Recursively accesses the cells of the CPT and updates its values using
	 * the values in marginalList.
	 * This is used by {@link #updateEvidences(float[], int)}.
	 * Overwrite this method if you need {@link #updateEvidences(float[], int)} to change
	 * its behavior.
	 * It assumes {@link #computeFactors()} was run prior to this method.
	 * @param marginalList
	 * @param c : an internal counter which traces the call depth
	 * @param linear : linear index of table being handled by the current recursive call
	 * @param index : index of variable in table
	 * @param state : state of variable identified by index, currently being treated.
	 */
	protected void updateRecursive(float[] marginalList, int c, int linear, int index, int state) {
		if (index < 0) {
			return;
		}
    	if (c >= this.variableList.size()) {
    		this.dataPT.data[linear] *= marginalList[state];
    		return;    		    		
    	}
    	
    	if (c == 0 && linear == 0 && state == 0) {
    		// this is a method critical for performance in huge tables. Calculate without using recursive calls, in order to make execution faster
    		state = this.variableList.get(index).getStatesSize();
    		c = factorsPT[index];
    		for (linear = 0; linear < this.dataPT.size; linear++) {
    			this.dataPT.data[linear] *= marginalList[(linear/c) % state];
			}
    	} else {
    		// in this case, the caller may be really attempting to do things recursively. Call recursive, just for backward compatibility
    		int currentFactor = this.factorsPT[c];
    		if (index == c) {
    			for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
    				updateRecursive(marginalList, c+1, linear + i* currentFactor, index, i);
    			}
    		} else {
    			for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
    				updateRecursive(marginalList, c+1, linear + i*currentFactor , index, state);
    			}
    		}
    	}
    	
    }

	/**
	 * Given a node identified by an index, updates its values
	 * using an array of float.
	 * For a probabilistic CPT, this method shall multiply the cells
	 * of the CPT related to the variable in index using the 
	 * values in marginalList. This can be used for implementing
	 * hard evidence.
	 * @param marginalList : values for updating
	 * @param index : index of the node related to the cells to update.
	 * The node can be obtained by calling {@link #getVariableAt(int)}
	 */
	public void updateEvidences(float[] marginalList, int index) {
		this.computeFactors();
//		this.updateRecursive(marginalList, 0, 0, index, 0);
		// this is a method critical for performance in huge tables. 
		// Instead of calling the method recursively, just do it locally so that execution is faster.
		int numStates = this.variableList.get(index).getStatesSize();
		int factor = factorsPT[index];
		for (int linear = 0; linear < this.dataPT.size; linear++) {
			this.dataPT.data[linear] *= marginalList[(linear/factor) % numStates];
		}
	}
	
	/**
	 * Default implementation of normalization.
	 * It normalizes current content of table (the sum is going to be 1).
	 * @return normalization factor (sum of the cells of this table); if table is already normalized, this is 1.
	 * @throws IllegalStateException : when an inconsistency or underflow is found
	 * TODO migrate this method to ProbabilisticTable
	 */
	public float normalize()  {
        float n = 0;
        float valor;

        int dataSize = this.tableSize();
        for (int c = 0; c < dataSize; c++) {
            n += this.getValue(c);
        }
        if (Math.abs(n - 1.0) > 0.00005) {	// if precision is 4 digits, error margin is half of precision (i.e. 0.00005)
        	for (int c = 0; c < dataSize; c++) {
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
	 * This method simply fills all entries in {@link #dataPT}
	 * with the provided value, which is the null value in multiplication and division
	 * (these two operations are the ones used in junction tree propagation
	 * for global consistency between tables in cliques and separators)
	 * @param value : the value to fill
	 * @see JunctionTree#initBelief(Separator)
	 * @see JunctionTree#initConsistency()
	 * @see #setValue(int, float)
	 */
	public void fillTable(float value) {
		Arrays.fill(dataPT.data, 0, dataPT.size,  value); // Arrays#fill seems to be faster than calling PotentialTable#setValue multiple times
//		int tableSize = tableSize();
//		for (int i = 0; i < tableSize; i++) {
//			setValue(i, 1f);
//		}
	}
	
	/**
	 * This var is used in {@link #notifyModification()}, {@link #computeFactors()}
	 * and {@link #computeFactorsMarginal()} to check/notify that the cpt was modified
	 * previously.
	 * @return the modified
	 */
	protected boolean isModified() {
		return modified;
	}

	/**
	 * This var is used in {@link #notifyModification()}, {@link #computeFactors()}
	 * and {@link #computeFactorsMarginal()} to check/notify that the cpt was modified
	 * previously.
	 * @param modified the modified to set
	 */
	protected void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * This array is used in {@link #sum(int)} and {@link #sumAux(int, int, int, int, boolean[])} in order
	 * to trace which cells in {@link #dataPT} were removed during the sum-out operations of
	 * {@link #removeVariable(INode)}. This was modeled as an attribute instead of local variable 
	 * in order to reduce garbage.
	 * @return the isRemovedCellInDataPT
	 */
	protected boolean[] isRemovedCellInDataPT() {
		return this.isRemovedCellInDataPT;
	}

	/**
	 * This array is used in {@link #sum(int)} and {@link #sumAux(int, int, int, int, boolean[])} in order
	 * to trace which cells in {@link #dataPT} were removed during the sum-out operations of
	 * {@link #removeVariable(INode)}. This was modeled as an attribute instead of local variable 
	 * in order to reduce garbage.
	 * @param isRemovedCellInDataPT the isRemovedCellInDataPT to set
	 */
	protected void setRemovedCellInDataPT(boolean[] isRemovedCellInDataPT) {
		this.isRemovedCellInDataPT = isRemovedCellInDataPT;
	}

	/**
	 * If true, {@link #sum(int)} will use a singleton boolean array in order to trace
	 * the cells in {@link #dataPT} removed during the marginalize-out operation.
	 * This value is true, by default, so set this to false if parallel sum-out is necessary.
	 * @return the isToUseSingletonArrayOfRemovedCellInDataPT
	 * @see #isRemovedCellInDataPT()
	 */
	public static boolean isToUseSingletonArrayOfRemovedCellInDataPT() {
		return isToUseSingletonArrayOfRemovedCellInDataPT;
	}

	/**
	 * If true, {@link #sum(int)} will use a singleton boolean array in order to trace
	 * the cells in {@link #dataPT} removed during the marginalize-out operation.
	 * This value is true, by default, so set this to false if parallel sum-out is necessary.
	 * @param isToUseSingletonArrayOfRemovedCellInDataPT the isToUseSingletonArrayOfRemovedCellInDataPT to set
	 * @see #isRemovedCellInDataPT()
	 */
	public static void setToUseSingletonArrayOfRemovedCellInDataPT(
			boolean isToUseSingletonArrayOfRemovedCellInDataPT) {
		PotentialTable.isToUseSingletonArrayOfRemovedCellInDataPT = isToUseSingletonArrayOfRemovedCellInDataPT;
	}
	

	/**
	 * Obtains the mutual information between two nodes in this table.
	 * It is assumed that {@link #getValue(int)} of this table are joint distribution (not necessarily normalized) of variables in this table.
	 * <br/>
	 * <br/>
	 * In probability theory and information theory, 
	 * the mutual information (MI) or (formerly) "trans" information of two random variables is a measure of the variables' mutual dependence.
	 * <br/>
	 * <br/>
	 * The mutual information for nodes X and Y is the expected value of log(P(X,Y) / P(X)P(Y)). In other words,
	 * it is the SUM [ P(X,Y) * log(P(X,Y) / P(X)P(Y)) ] for all states of X and Y.
	 * <br/>
	 * <br/>
	 * It uses natural logarithm.
	 * @param indexNode1 : index of one of the node to estimate mutual informatioan. 
	 * @param indexNode2 : index of the other node to estimate mutual information. 
	 * @return : the mutual information. In other words, the expected log(P(X,Y) / P(X)P(Y)).
	 * @see #getEntropy(int)
	 */
	public double getMutualInformation(int indexNode1, int indexNode2) {
		// basic input assertion
		if (indexNode1 < 0 || indexNode1 >= getVariablesSize()) {
			throw new ArrayIndexOutOfBoundsException(indexNode1);
		}
		if (indexNode2 < 0 || indexNode2 >= getVariablesSize()) {
			throw new ArrayIndexOutOfBoundsException(indexNode2);
		}
		
		// the mutual information of same node is its entropy
		if (indexNode1 == indexNode2) {
			return this.getEntropy(indexNode1);
		}
		
		// extract the nodes
		INode node1 = getVariableAt(indexNode1);
		if (node1 == null) {
			throw new NullPointerException("Found null node at index " + indexNode1);	// there is no way to calculate mutual information of null
		}
		INode node2 = getVariableAt(indexNode2);
		if (node2 == null) {
			throw new NullPointerException("Found null node at index " + indexNode2);	// there is no way to calculate mutual information of null
		}
		
		
		// prepare the value to return
		double ret = 0;		// this will be a sum, so initialize with 0.
		
		// extract the marginals of node1 and node2
		PotentialTable marginal1 = (PotentialTable) clone();				// use a clone, so that original is kept unchanged
		marginal1.retainVariables(Collections.singletonList(node1));	// marginalize out all vars except node1.
		PotentialTable marginal2 = (PotentialTable) clone();				// use a clone, so that original is kept unchanged
		marginal2.retainVariables(Collections.singletonList(node2));	// marginalize out all vars except node2. 
		
		// extract the joint of node1 and node2
		PotentialTable joint = (PotentialTable) clone();	// use a clone, so that original is kept unchanged
		{
			// marginalize out all vars except node1 and node2. 
			Collection<INode> nodes = new ArrayList<INode>(2);
			nodes.add(node1);
			nodes.add(node2);
			joint.retainVariables(nodes);
			if (joint.getVariablesSize() != nodes.size()) {
				throw new RuntimeException("Retaining nodes " + nodes + "from table " + this + " resulted in a table with size " + joint.getVariablesSize());
			}
		}
		
		// mutual information is calculated on probabilities, so make sure its normalized to 1
		marginal1.normalize();	
		marginal2.normalize();
		joint.normalize();
		
		// prepare an array which represents states of node1 and node2
		int[] coord = joint.getMultidimensionalCoord(0);
		// iterate on all state space
		for (int stateNode1 = 0; stateNode1 < marginal1.tableSize(); stateNode1++) {
			
			coord[joint.indexOfVariable((Node) node1)] = stateNode1;	// set current state of node1
			
			for (int stateNode2 = 0; stateNode2 < marginal2.tableSize(); stateNode2++) {
				
				coord[joint.indexOfVariable((Node) node2)] = stateNode2;	// set current state of node2
				
				// extract the joint probability of current combination of states
				float jointProb = joint.getValue(coord);
				
				// ret will be the expected (across joint probabilities) of log2(joint/productOfMarginals).
				// which is equal to sum of joint* log2((joint/marginal1)/marginal2)) = log2(joint/marginal1) -log2(marginal2) = log2(joint)-log2(marginal1)-log2(marginal2) 
				double factorCurrentState =  jointProb * (Math.log(jointProb) - Math.log(marginal1.getValue(stateNode1)) - Math.log(marginal2.getValue(stateNode2)) );
				if (!Double.isNaN(factorCurrentState)) {
					// ignore zero probabilities which will cause log to be NaN
					ret += factorCurrentState;
				}
			}
		}
		
		return Math.abs(ret);
	}
	

	/**
	 * This method calculates the entropy of a node, which is -SUM[P(x)*log(P(x))] for each state x of the variable.
	 * It is assumed that {@link #getValue(int)} of this table are joint distribution (not necessarily normalized) of variables in this table.
	 * @param nodeIndex : index of the node to calculate entropy. 
	 * @return the entropy of the node
	 * @see #getTemporaryClone()
	 */
	public double getEntropy(int nodeIndex) {
		// basic assertion of arguments
		if (nodeIndex < 0 || nodeIndex >= getVariablesSize()) {
			throw new ArrayIndexOutOfBoundsException(nodeIndex);
		}
		
		// this will be the variable to be returned
		double sum = 0f;
		
		// extract the node
		INode node = this.getVariableAt(nodeIndex);
		if (node == null) {
			throw new IllegalArgumentException("Found null at index " + nodeIndex);
		}
		
		// calculate the marginal of this node
		PotentialTable marginalTable = getTemporaryClone();
		marginalTable.retainVariables(Collections.singletonList(node));	// marginalize out other variables. 
		marginalTable.normalize();	// entropy must be calculated over probability, so make sure table is normalized to 1
		
		// basic assertion
		if (marginalTable.tableSize() != node.getStatesSize()) {
			throw new RuntimeException("Marginalized table " + marginalTable + " has size " + marginalTable.tableSize() + ", but number of states of node " + node + " was " + node.getStatesSize());
		}
		
		// calculate the SUM[P(x)*log(P(x))] for each state
		for (int state = 0; state < marginalTable.tableSize(); state++) {
			float marginal = marginalTable.getValue(state);	// extract the marginal probability of current state
			if (marginal > 0f ) {	// ignore impossible states
				sum += marginal * Math.log(marginal);		// P(x)*log(P(x))
			}
		}
		
		// entropy is -SUM[P(x)*log(P(x))]
		return -sum;
	}

	/**
	 * Remove all variables except the node in argument.
	 * Values in the table will be summed out based on {@link #removeVariable(INode, boolean)}, but
	 * without normalization.
	 * @param nodes : nodes/vars to keep in this table
	 * @see #removeVariable(INode, boolean)
	 */
	public void retainVariables(Collection<INode> nodes) {
		if (nodes == null) {
			nodes = Collections.EMPTY_LIST;
		}
		
		// check which vars we need to remove
		Collection<INode> nodesToRemove = new ArrayList<INode>(getVariablesSize());
		for (int i = 0; i < getVariablesSize(); i++) {
			INode nodeToRemove = getVariableAt(i);
			if (!nodes.contains(nodeToRemove)) {
				nodesToRemove.add(nodeToRemove);
			}
		}
		
		// marginalize out vars
		for (INode nodeToRemove : nodesToRemove) {
			removeVariable(nodeToRemove, false);
		}
	}

	
}