/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.Comparator;

import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.PotentialTable.ISumOperation;

/**
 * This is the junction tree to be used by algorithms implementing least probable explanation.
 * This class behaves the same way of {@link MaxProductJunctionTree},
 * but uses {@link MinOperation} instead of {@link PotentialTable.MaxOperation},
 * so that minimum values are propagated instead of maximum values.
 * @author Shou Matsumoto
 * @see JunctionTreeLPEAlgorithm
 * @see MinOperation
 */
public class MinProductJunctionTree extends MaxProductJunctionTree {

	/** Instance called when doing marginalization. This instance will min-out values in {@link PotentialTable#removeVariable(INode)} */
	public static final ISumOperation DEFAULT_MIN_OUT_OPERATION = new MinProductJunctionTree().new MinOperation();
	
	/** Default value of {@link #getTableExplanationComparator()} */
	public static final Comparator DEFAULT_TABLE_EXPLANATION_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			// ignore zeros
			if (Double.compare((Float)o1, 0.0f) == 0) {
				return -1;
			}
			if (Double.compare((Float)o2, 0.0f) == 0) {
				return 1;
			}
			// compare inverting the order (so that it returns the inverse of "normal" compare)
			return Double.compare((Double)o2, (Double)o1);
		}
	};


	/**
	 * This is the junction tree to be used by algorithms implementing least probable explanation.
	 * This class behaves the same way of {@link MaxProductJunctionTree},
	 * but uses {@link MinOperation} instead of {@link PotentialTable.MaxOperation},
	 * so that minimum values are propagated instead of maximum values.
	 * @see JunctionTreeLPEAlgorithm
	 * @see MinOperation
	 * @see #setMaxOperation(unbbayes.prs.bn.PotentialTable.ISumOperation)
	 */
	public MinProductJunctionTree() {
		setMaxOperation(DEFAULT_MIN_OUT_OPERATION);
		try {
			// add the comparator (it is the inverse of the superclass' comparator - i.e. instead of max, return min)
			this.setTableExplanationComparator(DEFAULT_TABLE_EXPLANATION_COMPARATOR);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
//	
//	
//	/**
//	 * This method is called by {@link #coleteEvidencia(Clique)}
//	 * in order to substitute {@link Clique#normalize()}
//	 * @param clique
//	 * @return
//	 * @throws Exception
//	 */
//	protected float normalizeClique(Clique clique) throws Exception {
//        float n = 0;
//        float valor;
//        PotentialTable potentialTable = clique.getProbabilityFunction();
//        int sizeDados = potentialTable.tableSize();
//        for (int c = 0; c < sizeDados; c++) {
//            n += potentialTable.getValue(c);
//        }
//        if ((n != 0.0) && (Math.abs(n - 1.0) > 0.001)) {
//            for (int c = 0; c < sizeDados; c++) {
//                valor = potentialTable.getValue(c);
//                // this is the changed part. In the original, if n==0.0, it throws exception.
//                valor /= n;
//                potentialTable.setValue(c, valor);
//            }
//        }
//        return n;
//    }



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
//
////		this.normalizeClique(clique);
//	}
	
	/**
	 * {@link #operate(float, float)} returns the minimum.
	 * @author Shou Matsumoto
	 *
	 */
	public class MinOperation implements PotentialTable.ISumOperation {
		/**
		 * Return the minimum, but ignores values less than or equals to 0.0f (except when both values are 0.0f).
		 * @return (arg1 < arg2)?arg1:arg2
		 */
		public double operate(double arg1, double arg2) {
			if (arg2 <= 0.0f) {
				return arg1;
			} else if (arg1 <= 0.0f) {
				return arg2;
			} else {
				return (arg1 < arg2)?(arg1):(arg2);
			}
		}
		
	}
}
