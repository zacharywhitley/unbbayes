/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This is a junction tree which does max-value search in {@link #absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)}
 * instead of marginalization.
 * @author Shou Matsumoto
 *
 */
public class MaxProductJunctionTree extends JunctionTree implements IPropagationOperationHolder, IExplanationJunctionTree {

	private PotentialTable.ISumOperation maxOperation;

	private Comparator tableExplanationComparator;
	
	private boolean isToNormalize = true;
	
	/**
	 * Default constructor
	 */
	public MaxProductJunctionTree() {
		setMaxOperation(new ProbabilisticTable().new MaxOperation());	// init default Max operation
		try {
			this.setTableExplanationComparator(new Comparator() {
				public int compare(Object o1, Object o2) {
					return Float.compare((Float)o1, (Float)o2);
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#absorb(unbbayes.prs.bn.Clique, unbbayes.prs.bn.Clique)
	 */
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

//	/**
//	 * This method was overridden because
//	 * we need to use {@link PotentialTable.SumOperation} as {@link #getMaxOperation()}
//	 * in {@link #absorb(Clique, Clique)} (which is called
//	 * indirectly by this method) during belief initialization.
//	 * @see unbbayes.prs.bn.JunctionTree#initBeliefs()
//	 */
//	public void initBeliefs() throws Exception {
//		// backup previous one
//		PotentialTable.ISumOperation backup = this.getMaxOperation();
//		// use PotentialTable.SumOperation instead of the backup
//		this.setMaxOperation(new ProbabilisticTable().new SumOperation());
//		super.initBeliefs();
//		// restore pervious one
//		this.setMaxOperation(backup);
//	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IPropagationOperationHolder#setMaxOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	public void setMaxOperation(PotentialTable.ISumOperation maxOperation) {
		this.maxOperation = maxOperation;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IPropagationOperationHolder#getMaxOperation()
	 */
	public PotentialTable.ISumOperation getMaxOperation() {
		return maxOperation;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IExplanationJunctionTree#calculateExplanation(unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	public Map<INode, Integer> calculateExplanation(Graph graph,
			IInferenceAlgorithm algorithm) {
		Map<INode, Integer> ret = new HashMap<INode, Integer>();
		for (Clique clique : this.getCliques()) {
			PotentialTable table = clique.getProbabilityFunction();
			if (table.tableSize() <= 0) {
				throw new IllegalArgumentException(clique + "- table size: " + table.tableSize());
			}
			// find index of the maximum value in clique
			int indexOfMaximumInClique = 0;
			for (int i = 1; i < table.tableSize(); i++) {
				if (this.getTableExplanationComparator().compare(table.getValue(i), table.getValue(indexOfMaximumInClique)) > 0) {
					indexOfMaximumInClique = i;
				}
			}
			// the indexes of the states can be obtained from the index of the linearized table by doing the following operation:
			// indexOfMPEOfNthNode = mod(indexOfMaximumInClique / prodNumberOfStatesPrevNodes, numberOfStates). 
			// prodNumberOfStatesPrevNodes is the product of the number of states for all previous nodes. If this is the first node, then prodNumberOfStatesPrevNodes = 1.
			// e.g. suppose there are 2 nodes A(w/ 4 states), B(w/ 3 states) and C (w/ 2 states). If the maximum probability occurs at index 5, then
			// the state of A is mod(5/1 , 4) = 1, and the state of B is mod(5/4, 3) = 1, and state of C is mod(5/(4*3),2) = 0.
			// I.e.
			//      |                             c0                            |                             c1                            |
			//      |         b0        |         b1        |         b2        |         b0        |         b1        |         b2        |
			//      | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 |
			//index:| 0  |  1 |  2 |  3 | 4  | 5  | 6  | 7  |  8 | 9  | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
			int prodNumberOfStatesPrevNodes = 1;
			for (int i = 0; i < table.getVariablesSize(); i++) {
				INode node = table.getVariableAt(i);
				int numberOfStates = node.getStatesSize();
				// number of states must be strictly positive
				if (numberOfStates <= 0) {
					try {
						Debug.println(getClass(), "[Warning] Size of " + table.getVariableAt(i) + " is " + numberOfStates);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				// calculate most probable state
				int indexOfMPEOfNthNode = (indexOfMaximumInClique / prodNumberOfStatesPrevNodes) % numberOfStates;
//				String state = node.getStateAt(indexOfMPEOfNthNode);
				// add to states if it was not already added
				if (!ret.containsKey(node)) {
					// this is the first time we add this entry. Add it
					ret.put(node, indexOfMPEOfNthNode);
				} else {
					// check consistency (max state must be unique between cliques)
					if (!ret.get(node).equals(indexOfMPEOfNthNode)) {
						try {
							Debug.println(getClass(), "Obtained states differ between cliques (clique inconsistency)... The current clique is: " 
									+ clique + "; previous state: " + ret.get(node) + "; index of state found in current clique: " + indexOfMPEOfNthNode);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
				prodNumberOfStatesPrevNodes *= numberOfStates;
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IExplanationJunctionTree#calculateJointProbability(java.util.Map, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	public double calculateJointProbability(Map<INode, Integer> states,
			Graph graph, IInferenceAlgorithm algorithm) {
		// TODO implement it
		throw new RuntimeException("Not implemented yet");
	}

	/**
	 * This comparator is used by {@link #calculateExplanation(Graph, IInferenceAlgorithm)} in order
	 * to obtain the maximum value in a clique table.
	 * @return the tableExplanationComparator
	 */
	public Comparator getTableExplanationComparator() {
		return tableExplanationComparator;
	}

	/**
	 * This comparator is used by {@link #calculateExplanation(Graph, IInferenceAlgorithm)} in order
	 * to obtain the maximum value in a clique table.
	 * @param tableExplanationComparator the tableExplanationComparator to set
	 */
	public void setTableExplanationComparator(Comparator tableExplanationComparator) {
		this.tableExplanationComparator = tableExplanationComparator;
	}



	/**
	 * If set to true, {@link #coleteEvidencia(Clique)} will normalize clique potential table.
	 * If false, {@link #coleteEvidencia(Clique)} will not normalize clique potential table.
	 * @return the isToNormalize
	 */
	public boolean isToNormalize() {
		return isToNormalize;
	}



	/**
	 * If set to true, {@link #coleteEvidencia(Clique)} will normalize clique potential table.
	 * If false, {@link #coleteEvidencia(Clique)} will not normalize clique potential table.
	 * @param isToNormalize the isToNormalize to set
	 */
	public void setToNormalize(boolean isToNormalize) {
		this.isToNormalize = isToNormalize;
	}



	/**
	 * This is the same of the superclass's method. However,
	 * if {@link #isToNormalize()} is false, it will not normalize the clique table.
	 * @see unbbayes.prs.bn.JunctionTree#coleteEvidencia(unbbayes.prs.bn.Clique)
	 */
	protected void coleteEvidencia(Clique clique) throws Exception {
		if (isToNormalize()) {
			super.coleteEvidencia(clique);
			return;
		} else {
			// this is the same of super.coleteEvidencia(clique), but without normalizing clique pot
			Clique auxClique;
			int sizeFilhos = clique.getChildrenSize();
			for (int c = 0; c < sizeFilhos; c++) {
				auxClique = clique.getChildAt(c);
				if (auxClique.getChildrenSize() != 0) {
					this.coleteEvidencia(auxClique);
				}
				
				absorb(clique, auxClique);
			}
		}
	}

	
	

}
