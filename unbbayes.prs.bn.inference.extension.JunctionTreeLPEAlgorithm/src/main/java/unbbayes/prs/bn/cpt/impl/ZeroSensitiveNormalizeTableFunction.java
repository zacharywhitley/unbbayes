/**
 * 
 */
package unbbayes.prs.bn.cpt.impl;

import unbbayes.prs.bn.ProbabilisticTable;

/**
 * Performs the same of {@link NormalizeTableFunction},
 * but it does not change zero probabilities.
 * @author Shou Matsumoto
 *
 */
public class ZeroSensitiveNormalizeTableFunction extends NormalizeTableFunction {

	/**
	 * Default constructor
	 * @see NormalizeTableFunction#NormalizeTableFunction()
	 */
	public ZeroSensitiveNormalizeTableFunction() {
		super();
	}
	

	/**
	 * Normalizes the cpt, but do not touch probabilities that are 0.
	 */
	public void applyFunction(ProbabilisticTable table) {
		float[] totalSum = new float[table.tableSize()];
		float value;
		int[] coord = new int[table.variableCount()];
		for (int i = 0; i < table.tableSize(); i++) {
			value = table.getValue(i);
			coord = table.getMultidimensionalCoord(i);
			coord[0] = 0;
			totalSum[table.getLinearCoord(coord)] += value;
		}
		
		for (int i = 0; i < table.tableSize(); i++) {
			coord = table.getMultidimensionalCoord(i);
			coord[0] = 0;
			if (table.getValue(i) == 0.0f) {
				// keep zeros untouched.
				continue;
			}
			value = table.getValue(i) / totalSum[table.getLinearCoord(coord)];
			table.setValue(i, value);
		}
	}



}
