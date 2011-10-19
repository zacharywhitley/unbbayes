/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;

/**
 * This class behaves the same way of {@link MaxProductJunctionTree},
 * but uses min product operation instead of {@link PotentialTable.MaxOperation}
 * @author Shou Matsumoto
 *
 */
public class MinProductJunctionTree extends MaxProductJunctionTree {

	public MinProductJunctionTree() {
		setMaxOperation(new MinOperation());
	}
	
	
	
	
	/**
	 * This method is called by {@link #coleteEvidencia(Clique)}
	 * in order to substitute {@link Clique#normalize()}
	 * @param clique
	 * @return
	 * @throws Exception
	 */
	protected float normalizeClique(Clique clique) throws Exception {
        float n = 0;
        float valor;
        PotentialTable potentialTable = clique.getProbabilityFunction();
        int sizeDados = potentialTable.tableSize();
        for (int c = 0; c < sizeDados; c++) {
            n += potentialTable.getValue(c);
        }
        if ((n != 0.0) && (Math.abs(n - 1.0) > 0.001)) {
            for (int c = 0; c < sizeDados; c++) {
                valor = potentialTable.getValue(c);
                // this is the changed part. In the original, if n==0.0, it throws exception.
                valor /= n;
                potentialTable.setValue(c, valor);
            }
        }
        return n;
    }



	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTree#coleteEvidencia(unbbayes.prs.bn.Clique)
	 */
	protected void coleteEvidencia(Clique clique) throws Exception {
		Clique auxClique;
		int sizeFilhos = clique.getChildrenSize();
		for (int c = 0; c < sizeFilhos; c++) {
			auxClique = clique.getChildAt(c);
			if (auxClique.getChildrenSize() != 0) {
				this.coleteEvidencia(auxClique);
			}
			
			absorb(clique, auxClique);
		}

//		this.normalizeClique(clique);
	}
	
	/**
	 * {@link #operate(float, float)} returns the minimum.
	 * @author Shou Matsumoto
	 *
	 */
	public class MinOperation implements PotentialTable.ISumOperation {
		/**
		 * @return (arg1 < arg2)?arg1:arg2
		 */
		public float operate(float arg1, float arg2) {
			return (arg1 < arg2)?arg1:arg2;
		}
		
	}
}
