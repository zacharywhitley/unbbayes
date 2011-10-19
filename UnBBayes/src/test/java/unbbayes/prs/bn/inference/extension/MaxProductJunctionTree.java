/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;

import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.SetToolkit;

/**
 * This is a junction tree which does max-value search in {@link #absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)}
 * instead of marginalization.
 * @author Shou Matsumoto
 *
 */
public class MaxProductJunctionTree extends JunctionTree {

	private PotentialTable.ISumOperation maxOperation;

	/**
	 * Default constructor
	 */
	public MaxProductJunctionTree() {
		setMaxOperation(new ProbabilisticTable().new MaxOperation());	// init default Max operation
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
	@Override
	protected void absorb(Clique clique1, Clique clique2) {
		// table of separator
		PotentialTable sepTab = getSeparator(clique1, clique2).getProbabilityFunction();
		// who are going to be removed 
		ArrayList<Node> maxOut = SetToolkit.clone(clique2.getNodes());
		
		// variables in the separator are not going to be removed, so remove from maxOut
		for (int i = 0; i < sepTab.variableCount(); i++) {
			maxOut.remove(sepTab.getVariableAt(i));			
		}

		// temporary table for ratio calculation
		PotentialTable dummyTable =
			(PotentialTable) clique2.getProbabilityFunction().clone();
		
		for (int i = 0; i < maxOut.size(); i++) {
			PotentialTable.ISumOperation backupOp = dummyTable.getSumOperation();	// backup real op
			// TODO store maximum values so that we can update content of dummyTable with max instead of marginal
			dummyTable.setSumOperation(getMaxOperation());	// substitute op w/ operator for comparison (max) instead of sum (marginal)
			// remove maxout (this will automatically marginalize)
			dummyTable.removeVariable(maxOut.get(i));	// no normalization, 
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
			PotentialTable.DIVISION_OPERATOR);

		clique1.getProbabilityFunction().opTab(dummyTable, PotentialTable.PRODUCT_OPERATOR);
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.JunctionTree#coleteEvidencia(unbbayes.prs.bn.Clique)
//	 */
//	@Override
//	protected void coleteEvidencia(Clique clique) throws Exception {
//		Clique auxClique;
//		int sizeFilhos = clique.getChildrenSize();
//		for (int c = 0; c < sizeFilhos; c++) {
//			auxClique = clique.getChildAt(c);
//			if (auxClique.getChildrenSize() != 0) {
//				this.coleteEvidencia(auxClique);
//			}
//			
//			absorb(clique, auxClique);
//		}
//		// do not normalize
//	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.JunctionTree#distributeEvidences(unbbayes.prs.bn.Clique)
//	 */
//	@Override
//	protected void distributeEvidences(Clique clique) {
//		// TODO Auto-generated method stub
//		super.distributeEvidences(clique);
//	}

	/**
	 * This method was overridden because
	 * we need to use {@link PotentialTable.SumOperation} as {@link #getMaxOperation()}
	 * in {@link #absorb(Clique, Clique)} (which is called
	 * indirectly by this method) during belief initialization.
	 * @see unbbayes.prs.bn.JunctionTree#initBeliefs()
	 */
	@Override
	public void initBeliefs() throws Exception {
		// backup previous one
		PotentialTable.ISumOperation backup = this.getMaxOperation();
		// use PotentialTable.SumOperation instead of the backup
		this.setMaxOperation(new ProbabilisticTable().new SumOperation());
		super.initBeliefs();
		// restore pervious one
		this.setMaxOperation(backup);
	}

	/**
	 * This operator is used in {@link #absorb(Clique, Clique)}
	 * to substitute the sum (marginal) operation. The
	 * sum (marginal) operation is used by {@link PotentialTable#getSumOperation()}.
	 * @param maxOperation the maxOperation to set
	 * @see PotentialTable#getSumOperation()
	 * @see PotentialTable#setSumOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	protected void setMaxOperation(PotentialTable.ISumOperation maxOperation) {
		this.maxOperation = maxOperation;
	}

	/**
	 * This operator is used in {@link #absorb(Clique, Clique)}
	 * to substitute the sum (marginal) operation. The
	 * sum (marginal) operation is used by {@link PotentialTable#getSumOperation()}.
	 * @return the maxOperation
	 * @see PotentialTable#getSumOperation()
	 * @see PotentialTable#setSumOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	protected PotentialTable.ISumOperation getMaxOperation() {
		return maxOperation;
	}

	
	

}
