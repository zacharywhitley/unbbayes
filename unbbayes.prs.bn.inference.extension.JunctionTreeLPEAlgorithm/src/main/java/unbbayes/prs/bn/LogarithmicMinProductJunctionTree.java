/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;

import unbbayes.prs.Node;
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

	/**
	 *  This is an extension of {@link MinProductJunctionTree}
	 * for values in logarithmic scale. That is, instead of using
	 * multiplications and divisions during the method {@link #absorb(Clique, Clique)}, 
	 * it uses addition and subtraction respectively.
	 */
	public LogarithmicMinProductJunctionTree() { }
	
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

		dummyTable.directOpTab(
			originalSeparatorTable,
			PotentialTable.MINUS_OPERATOR);

		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PLUS_OPERATOR);
	}

}
