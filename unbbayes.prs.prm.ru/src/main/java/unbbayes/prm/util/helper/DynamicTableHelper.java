package unbbayes.prm.util.helper;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;

/**
 * 
 * @author David Saldaña.
 * 
 */
public class DynamicTableHelper {

	/**
	 * The the order of the states in a CPT. This is to identify how to clone a
	 * state.
	 * 
	 * @param numColumns
	 * @param numStates
	 * @param numUpperStates
	 * @return
	 */
	public static int[] statesOrderInCpt(int level, PotentialTable cpt) {
		int numColumns = getNumColumns(cpt);
		int numStates = cpt.getVariableAt(level + 1).getStatesSize();
		int numSubStates = getNumSubStates(level, cpt);

		numSubStates = numSubStates == 0 ? 1 : numSubStates;

		// Identify the columns related with every state of this
		// variable.
		List<Integer> statesOrder = new ArrayList<Integer>(numColumns);
		int nextVal = 0;

		// number of columns for a block;
		int colsForState = numSubStates * numStates;
		colsForState = colsForState == numColumns ? colsForState / numStates
				: colsForState;

		int iters = numColumns / numSubStates;

		int varIndex = 0;
		int blockIndex = 0;
		// Each block
		for (int j = 0; j < iters; j++) {

			// Each substate
			for (int i = 0; i < numSubStates; i++) {
				statesOrder.add(nextVal);

				if (i + 1 < numSubStates) {
					nextVal++;
				}
			}
			if(statesOrder.size()==numColumns){
				break;
			}

			blockIndex++;
			nextVal = colsForState * (blockIndex) + varIndex;

			boolean initial = true;
			// find a hole
			while (nextVal >= numColumns || statesOrder.contains(nextVal)) {
				if (initial) {
					blockIndex = 0;
					initial = false;
				}
				varIndex++;
				nextVal = colsForState * (blockIndex) + varIndex;

				if (varIndex > numStates) {
					varIndex = 0;
					blockIndex++;
				}
			}
		}

		// Convert Integer to int.
		int result[] = new int[numColumns];

		for (int i = 0; i < result.length; i++) {
			result[i] = statesOrder.get(i);
		}

		return result;
	}

	/**
	 * Get number of sub states.
	 * 
	 * @param level
	 * @param numCptParents
	 * @param rightCptWithValues
	 * 
	 * @return number of sub states.
	 */
	public static int getNumSubStates(int level,
			PotentialTable rightCptWithValues) {
		int numCptParents = rightCptWithValues.getVariablesSize();

		// If there is no sub-states.
		if (level + 2 >= numCptParents) {
			return 0;
		}

		int numSubStates = 1;
		for (int k = level + 2; k < numCptParents; k++) {
			numSubStates *= rightCptWithValues.getVariableAt(k).getStatesSize();
		}

		return numSubStates;
	}

	/**
	 * Total number of columns in the CPT.
	 * 
	 * @param rightCptWithValues
	 * @return
	 */
	public static int getNumColumns(PotentialTable rightCptWithValues) {
		int numColumns = 1;
		int numCptParents = rightCptWithValues.getVariablesSize();
		for (int i = 1; i < numCptParents; i++) {
			numColumns *= rightCptWithValues.getVariableAt(i).getStatesSize();
		}
		return numColumns;
	}

	public static int getNumUpperStates(int level,
			PotentialTable rightCptWithValues) {

		if (level < 1) {
			return 0;
		}

		int numUpperStates = 1;
		for (int k = 0; k < level; k++) {
			numUpperStates *= rightCptWithValues.getVariableAt(k + 1)
					.getStatesSize();
		}
		return numUpperStates;
	}

	/**
	 * Create a new level based in a organized array of columns.
	 * 
	 * @param level
	 * @param cpt
	 * @param childNode
	 * @param numColumns
	 * @return
	 */
	public static int[] addLevel(int level, PotentialTable cpt,
			INode childNode, int numColumns, int[] order) {
		int numSubStates = getNumSubStates(level, cpt);
		numSubStates = numSubStates == 0 ? 1 : numSubStates;

		int numStates = childNode.getStatesSize();

		int numResultCols = numColumns * numStates;
		int[] result = new int[numResultCols];
		int currentVal = 0;

		int index = 0;
		int tmpStates = 0;
		for (int s = 0; s < numColumns; s += numSubStates) {
			tmpStates = s;
			for (int j = 0; j < numStates; j++) {
				currentVal = tmpStates;
				for (int i = 0; i < numSubStates; i++) {
					result[index++] = order[currentVal];
					currentVal++;
				}
			}
		}
		return result;
	}
}
