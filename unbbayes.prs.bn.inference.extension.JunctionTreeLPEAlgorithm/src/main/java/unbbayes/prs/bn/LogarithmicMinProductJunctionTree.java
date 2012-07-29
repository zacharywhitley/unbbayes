/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable.ISumOperation;
import unbbayes.prs.bn.inference.extension.MinProductJunctionTree;
import unbbayes.util.SetToolkit;

/**
 * This is an extension of {@link MinProductJunctionTree}
 * for values in logarithmic scale. That is, instead of using
 * multiplications and divisions during the method {@link #absorb(Clique, Clique)}, 
 * it uses addition and subtraction respectively.
 * @author Shou Matsumoto
 *
 */
public class LogarithmicMinProductJunctionTree extends MinProductJunctionTree {
	
	/** Instance called when doing marginalization. This instance will min-out values in {@link PotentialTable#removeVariable(INode)} */
	public static final ISumOperation DEFAULT_MIN_OUT_OPERATION = new LogarithmicMinProductJunctionTree().new MinOperation();
	
	
	/**
	 *  This is an extension of {@link MinProductJunctionTree}
	 * for values in logarithmic scale. That is, instead of using
	 * multiplications and divisions during the method {@link #absorb(Clique, Clique)}, 
	 * it uses addition and subtraction respectively.
	 */
	public LogarithmicMinProductJunctionTree() {
		setMaxOperation(DEFAULT_MIN_OUT_OPERATION);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	protected void absorb(Clique clique1, Clique clique2) {
		Separator sep = getSeparator(clique1, clique2);
		if (sep == null) {
			// cliques are disconnected (they are separated subnets of a disconnected network)
			return;
		}
		// table of separator
		PotentialTable sepTab = sep.getProbabilityFunction();
		// who are going to be removed 
		ArrayList<Node> minOut = SetToolkit.clone(clique2.getNodes());
		if (sepTab.tableSize() <= 0) {
			// cliques are disconnected (they are separated subnets of a disconnected network)
			return;	// there is nothing to propagate
		}
		
		// variables in the separator are not going to be removed, so remove from maxOut
		for (int i = 0; i < sepTab.variableCount(); i++) {
			minOut.remove(sepTab.getVariableAt(i));			
		}

		// temporary table for ratio calculation
		PotentialTable dummyTable =
			(PotentialTable) clique2.getProbabilityFunction().clone();
		
		for (int i = 0; i < minOut.size(); i++) {
			PotentialTable.ISumOperation backupOp = dummyTable.getSumOperation();	// backup real op
			// TODO store maximum values so that we can update content of dummyTable with max instead of marginal
			dummyTable.setSumOperation(getMaxOperation());	// substitute op w/ operator for comparison (max) instead of sum (marginal)
			// remove maxout (this will automatically marginalize)
			dummyTable.removeVariable(minOut.get(i));	// no normalization, 
			// TODO the removal did a marginalization. Do max right now
			dummyTable.setSumOperation(backupOp);	// restore previous op
		}
		

		PotentialTable originalSeparatorTable =
			(PotentialTable) sepTab.clone();

		for (int i = sepTab.tableSize() - 1; i >= 0; i--) {
			sepTab.setValue(i, dummyTable.getValue(i));
		}

//		dummyTable.directOpTab(
//			originalSeparatorTable,
//			PotentialTable.MINUS_OPERATOR);
		// the following code performs the above, but it forces that Float.POSITIVE_INFINITY
		// is treated specially: any operation with Float.POSITIVE_INFINITY will result in Float.POSITIVE_INFINITY.
		for (int k = dummyTable.tableSize()-1; k >= 0; k--) {
			if (originalSeparatorTable.dataPT.data[k] == Float.POSITIVE_INFINITY) {
				// force a subtraction with infinity to result in positive infinity
				dummyTable.dataPT.data[k] = Float.POSITIVE_INFINITY;
			} else {
				dummyTable.dataPT.data[k] -= originalSeparatorTable.dataPT.data[k];
			}
		}
		
		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PLUS_OPERATOR);
	}
	
	/**
	 * {@link #operate(float, float)} returns the minimum,
	 * without ignoring zeros or negatives.
	 * @author Shou Matsumoto
	 */
	public class MinOperation extends MinProductJunctionTree.MinOperation {
		/**
		 * Return the minimum, but ignores values less than or equals to 0.0f (except when both values are 0.0f).
		 * @return (arg1 < arg2)?arg1:arg2
		 */
		public float operate(float arg1, float arg2) {
			return (arg1 < arg2)?(arg1):(arg2);
		}
		
	}

}
