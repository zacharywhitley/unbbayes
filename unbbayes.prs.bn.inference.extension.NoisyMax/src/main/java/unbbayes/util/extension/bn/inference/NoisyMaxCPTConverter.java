/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import unbbayes.prs.INode;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.PotentialTable;

/**
 * It reads some columns of a CPT and automatically fills other
 * columns, so that the CPT becomes a noisy-max distribution.
 * @author Shou Matsumoto
 *
 */
public class NoisyMaxCPTConverter implements IIndependenceCausalInfluenceCPTConverter, IIndependenceCausalInfluenceChecker {
	
	public static final float DEFAULT_PROBABILITY_ERROR_MARGIN = 0.00005f;

	/** Differences in probability within this margin will be considered ignorable in comparison */
	private float probErrorMargin = DEFAULT_PROBABILITY_ERROR_MARGIN;

	/**
	 * Default constructor
	 */
	public NoisyMaxCPTConverter() {}

	/**
	 * This method transforms the CPT to a noisy-max distribution as follows:
	 * 
	 * <br/>
	 * <br/>
	 * Given network A->B<-C and table of B as follows ("=" represents some unknown value):
	 * <pre>
	 * -----------------------------------------
	 *| C ||     0     |     1     |     2     |
	 * -----------------------------------------
	 *| A || 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 |
	 * -----------------------------------------
	 *| 0 || = |p3 |p5 |p9 | = | = |p11| = | = |
	 *| 1 || = |p4 |p6 |p10| = | = |p12| = | = |
	 *| 2 || = | = | = | = | = | = | = | = | = |
	 * -----------------------------------------
	 * </pre>
	 * 
	 * This method will change the probabilities to:
	 * <br/>
	 * <br/>
	 * P(B|A=0,C=0) = [100%, 0%,  0%]; <br/>
	 * P(B|A=1,C=0) = [p3,   p4,  =]; (remains the same) <br/>
	 * P(B|A=2,C=0) = [p5,   p6,  =]; (remains the same) <br/>
	 * P(B|A=0,C=1) = [p9,   p10, =]; (remains the same) <br/>
	 * P(B|A=0,C=2) = [p11,  p12, =]; (remains the same) <br/>
	 * P(B|A=1,C=1) = [p3*p9, p4*p10-(p3*p9), 1-previous]; <br/>
	 * P(B|A=1,C=2) = [p3*p11, (p3+p4)*(p11+p12)-(p3*p11), 1-previous]; <br/>
	 * P(B|A=2,C=1) = [p5*p9, (p5+p6)*(p9+p10)-(p5*p9), 1-previous]; <br/>
	 * P(B|A=2,C=2) = [p5*p11, (p5+p6)*(p11+p12)-(p5*p11), 1-previous]; <br/>
	 * <br/>
	 * The resulting values will represent a noisy-max function.
	 * 
	 * @param localDistribution: CPT to be used. Needs to be a table, so it is assumed to be an instance of {@link PotentialTable}.
	 * @see unbbayes.util.extension.bn.inference.IIndependenceCausalInfluenceCPTConverter#forceCPTToIndependenceCausalInfluence(unbbayes.prs.bn.IProbabilityFunction)
	 */
	public void forceCPTToIndependenceCausalInfluence( IProbabilityFunction localDistribution) {
		if (!this.visitCPTCells(localDistribution, true)) {
			throw new RuntimeException("Could not update non-free parameters of noisy-max distribution accordingly to free parameters.");
		}
	}
	
	/**
	 * Reads noisy-max free parameters (those in columns which only 1 parent is at non-zero state --i.e. those specified by user) from a cpt,
	 * and either updates other cells (non-free parameters) accordingly, or checks if the values in the other cells matches
	 * with the values that can be derived from the free parameters.
	 * @param localDistribution : cpt to be verified
	 * @param isToOverwrite : if true, will update (write) non-free parameters. If false, will check if non-free parameters are consistent with
	 * free parameters.
	 * @return true if non-free parameters are consistent with free parameters. False otherwise.
	 * @see #isICI(IProbabilityFunction)
	 * @see #forceCPTToIndependenceCausalInfluence(IProbabilityFunction)
	 */
	protected boolean visitCPTCells (IProbabilityFunction localDistribution, boolean isToOverwrite) {

		// check inputs
		if (localDistribution == null) {
			return false;
		}
		if (!(localDistribution instanceof PotentialTable)) {
			throw new IllegalArgumentException("This version expects the local probability distribution to be an instance of " + PotentialTable.class.getName());
		}
		
		PotentialTable table = (PotentialTable) localDistribution;
		
		// extract the owner of the table
		INode tableOwner = table.getVariableAt(0);
		
		// extract the number of states of the owner
		int numStatesOwner = tableOwner.getStatesSize();
		
		// check if first column is [100%, 0%, 0% ,..., 0%]
		if (isToOverwrite) {
			table.setValue(0, 1f);
		} else  if (Math.abs(1f-table.getValue(0) ) > getProbErrorMargin()) { // check if first row is 100%
			return false;
		}
		// check other rows (cells starting at index 1)
		for (int i = 1; i < numStatesOwner; i++) {
			if (isToOverwrite) {
				table.setValue(i, 0f);
			} else if (Math.abs(table.getValue(i) ) > getProbErrorMargin()) { // check if other rows are 0%
				return false;
			}
		}
		
		// extract the size of the table, because we'll iterate on it
		int tableSize = table.tableSize();
		
		/*
		 * Prepare a vector of coordinates (states of each variable in CPT), but with all states of all nodes at 0
		 * This will be used in next loop in order to extract parameter of noisy max, 
		 * by setting one variable at non-zero state, read CPT, reset to zero again, and repeat for another variable.
		 * 
		 * Only one parent must be at non-zero state (the table owner --index 0-- can be non-zero). E.g.: 
		 * 
		 * [2,3,0,0] is the parameter of noisy max for state 2 of owner of the CPT, and state 3 of 1st parent.
		 * [3,0,1,0] is the parameter of noisy max for state 3 of owner of the CPT, and state 1 of 2nd parent.
		 * [0,0,0,5] is the parameter of noisy max for state 0 of owner of the CPT, and state 5 of 3rd parent.
		 * 
		 * We can extract all parameters by iterating on all possible combinations, subject to the fact that only 1 parent is non-zero.
		 */
		int[] coordNoisyMax = table.getMultidimensionalCoord(0);	// argument 0 guarantees that all states are zero (i.e. it's [0,0,0,...,0])
		
		// iterate on columns. Start from 2nd column, because 1st column was already changed to [100%, 0%, 0% ,..., 0%]
		for (int cellIndex = numStatesOwner; cellIndex < tableSize; ) {
			
			// the index is for cell, but we are actually iterating column by column, because at each iteration the cellIndex is incremented by numStatesOwner
			// therefore, cellIndex should be at the 1st cell column
			if (cellIndex % numStatesOwner != 0) {
				throw new IllegalStateException("If we are looking at each cell one-by-one, the 1st time we find a column not to change is supposedly when we reached the 1st cell in a column. "
						+ "The index of such cell shall be divisible by number of states of the owner of the table, but index " + cellIndex + " was not divisible by " + numStatesOwner
						+ ". This is probably a bug. Please, check required plug-in/core versions.");
			}
			
			// check if current is free parameter. If so, column does not need to be changed or checked
			if (isCPTColumnWithNoisyMaxParameter(cellIndex, table)) {
				// go directly to next column
				cellIndex += numStatesOwner;
			} else {
				// change or check the probabilities
				
				// convert the index of cell to array which indicates what are the states of each variable
				int[] coordCurrentColumn = table.getMultidimensionalCoord(cellIndex);	// this will be used to extract parameters of noisy max
				
				// prepare a vector which will store what are the parameters of noisy-max for current current state of owner (row)
				float[] paramNoisyMax = new float[table.getVariablesSize()-1];	// For a single row, we need one parameter for each parent
				// make sure they all start with zero, because we need to add previous value to new value.
				for (int i = 0; i < paramNoisyMax.length; i++) {
					paramNoisyMax[i] = 0f;
				}
				
				/*
				 * Iterate on states of the owner of the CPT in order to fill lines of the current column.
				 * The content of CPT for each state (row) of owner of the table and for each of its parents will be:
				 * 
				 *  Product( paramNoisyMax(row, parent) + paramNoisyMaxx(row-1, parent) + ... +  paramNoisyMaxx(0, parent) ) - SUM(previousRows)
				 * 
				 *  => Product is a product for each parent; 
				 *  
				 *  => SUM(previousRows) is the sum of all probabilities filled so far for the current column of CPT;
				 *  
				 *  => paramNoisyMax(row, parent) is the value user entered in the noisy-max table at the row and parent. 
				 *     This parameter can be extracted from the CPT by accessing columns where only 1 of the parents has non-zero state.
				 */
				for (int row = 0; row < numStatesOwner; row++, cellIndex++) {	// i.e. row is an index for the state of the owner (node) of CPT
					
					// coordNoisyMax needs to point to the same row we are currently inspecting
					coordNoisyMax[0] = row;	// the row is the state of owner of the table.
					
					// iterate on parents of table owner in order to fill the vector of parameters of noisy max for current parents and state (row)
					for (int parentIndex = 0; parentIndex < paramNoisyMax.length; parentIndex++) {
						// We can extract parameter of current parent by accessing the cell with all states in zero except for current parent.
						// note: coordNoisyMax was initialized with state 0 for all nodes, 
						// so changing only the current parent will guarantee that all other parents are at state zero
						coordNoisyMax[parentIndex+1] = coordCurrentColumn[parentIndex+1];	// need +1 because index 0 represents the owner of the table, and we want to consider parents only
						
						// this is the paramNoisyMax(row, parent) for the current row. 
						// By adding with old values, we are getting paramNoisyMax(row, parent) + paramNoisyMaxx(row-1, parent) + ... +  paramNoisyMaxx(0, parent)
						paramNoisyMax[parentIndex] += table.getValue(coordNoisyMax);
						
						coordNoisyMax[parentIndex+1] = 0;	// reset, for next iteration. Again, need +1 because index 0 represents the owner of the table, and we want to consider parents only
					}
					
					// get product of paramNoisyMax
					float product = 1f;
					for (int i = 0; i < paramNoisyMax.length; i++) {
						product *= paramNoisyMax[i];
					}
					
					// now, subtract prob of each row of same column, because we need to subtract SUM(previousRows) from product of paramNoisyMax
					for (int i = 1; i <= row; i++) {	// if it's the 1st row, this loop won't start
						// Note: cellIndex and row are supposedly incremented synchronously, 
						// so we can use cellIndex and subtract row to get 1st cell of current column
						product -= table.getValue(cellIndex-i);
					}
					
					// set the current cell to the value we calculated
					if (isToOverwrite) {
						table.setValue(cellIndex, product);
					} else if (Math.abs(table.getValue(cellIndex) - product) > getProbErrorMargin()) { // if we are simply checking consistency with noisy max, then check if content of table is equal to the value we want
						return false;
					}
				}
				
			}
		}
		
		return true;
	
	}
	

	/**
	 * This is used in {@link #forceCPTToNoisyMax(PotentialTable)} in order
	 * to check whether the current column must remain the same in a noisy-max distribution.
	 * Columns are kept the same if only one parent is at a non-zero state.
	 * @param tableIndex : index in {@link PotentialTable#getValue(int)}
	 * @param localDistribution: CPT being processed.
	 * @return true if the noisy-max distribution shall not touch this column. False otherwise.
	 */
	public boolean isCPTColumnWithNoisyMaxParameter(int tableIndex, IProbabilityFunction localDistribution) {
		
		PotentialTable table = (PotentialTable) localDistribution;
		
		// extract what are the states represented by the cell of table at index tableIndex 
		int[] states = table.getMultidimensionalCoord(tableIndex);
		
		// ignore the state of the owner of the table, because it is related to row, rather than column
		states[0] = 0;	// setting it to zero will make the following loop to ignore it
		
		// condition for true: all states are zero except for 1 variable
		boolean foundNonZero = false;
		for (int state : states) {
			if (state != 0) {
				if (foundNonZero) {
					// this is the 2nd occurrence of non-zero state
					return false;
				}
				foundNonZero = true;
			}
		}
		
		// note: if foundNonZero == false, then all values were zero. 
		// In noisy max, this column must be changed to [100%,0%,...,0%], so must not remain the same
		return foundNonZero;
	}
	
	/**
	 * Checks if content of given cpt follows the pattern of noisy-max distribution
	 * @param localDistribution : the distribution to check
	 * @return: true if provided local distribution can be considered to represent a noisy-max distribution
	 */
	public boolean isICI(IProbabilityFunction localDistribution) {
		return this.visitCPTCells(localDistribution, false);
	}
	
	/**
	 * @return  Differences in probability within this margin will be considered ignorable in comparison.
	 * @see #isICI(IProbabilityFunction)
	 */
	public float getProbErrorMargin() {
		return probErrorMargin;
	}

	/**
	 * @param probErrorMargin :  Differences in probability within this margin will be considered ignorable in comparison.
	 * @see #isICI(IProbabilityFunction)
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		this.probErrorMargin = probErrorMargin;
	}

}
