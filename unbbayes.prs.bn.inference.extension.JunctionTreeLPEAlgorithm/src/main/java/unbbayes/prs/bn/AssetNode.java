/**
 * 
 */
package unbbayes.prs.bn;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable.ISumOperation;
import unbbayes.prs.bn.inference.extension.MinProductJunctionTree.MinOperation;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 */
public class AssetNode extends DecisionNode {
	
	private static final long serialVersionUID = -1926227466217847395L;
	private boolean isToCalculateMarginal = false;


	/**
	 * Default constructor is protected only to allow inheritance.
	 * Use {@link #getInstance()} to instantiate.
	 */
	protected AssetNode() {
		super();
	}
	
	/**
	 * Default constructor method.
	 * @return
	 */
	public static AssetNode getInstance() {
		return new AssetNode();
	}

	/**
	 * Although this class extends decision node for convenience,
	 * it will return {@link Node#UTILITY_NODE_TYPE} as its type
	 * @see unbbayes.prs.id.DecisionNode#getType()
	 */
	public int getType() {
		return this.UTILITY_NODE_TYPE;
	}


   /*
    * (non-Javadoc)
    * @see unbbayes.prs.id.DecisionNode#marginal()
    */
    protected void marginal() {
    	initMarginalList();	// at least make sure marginal list is not null
    	if (!isToCalculateMarginal()) {
    		return;
    	}
//    	return;	// asset nodes has no semantics for marginals
//    	for (int i = 0; i < marginalList.length; i++) {
//    		marginalList[i] = 1;
//    	}
//    	return;
        PotentialTable auxTab = (PotentialTable) ((PotentialTable)getAssociatedClique().getProbabilityFunction()).clone();
        int index = auxTab.indexOfVariable(this);
        int size = getAssociatedClique().getProbabilityFunction().variableCount();
        for (int i = 0; i < size; i++) {
            if (i != index) {
                auxTab.removeVariable(getAssociatedClique().getProbabilityFunction().getVariableAt(i));
            }
        }

        int tableSize = auxTab.tableSize();
        if (tableSize > marginalList.length) {
        	Debug.println(getClass(), "There is some inconsistency. Maybe there is some node with no state at all.");
        } else {
        	for (int i = 0; i < tableSize; i++) {
        		marginalList[i] = auxTab.getValue(i);
        	}
        }
    }

	/**
	 *  If false, {@link #marginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true,  {@link #marginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation ({@link ISumOperation})
	 * (e.g. sum-out, min-out, max-out).
	 * @param isToCalculateMarginal the isToCalculateMarginal to set
	 * @see ISumOperation
	 * @see MinOperation
	 */
	public void setToCalculateMarginal(boolean isToCalculateMarginal) {
		this.isToCalculateMarginal = isToCalculateMarginal;
	}

	/**
	 *  If false, {@link AssetNode#updateMarginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true, {@link AssetNode#updateMarginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation ({@link ISumOperation})
	 * (e.g. sum-out, min-out, max-out).
	 * @return the isToCalculateMarginal
	 * @see ISumOperation
	 * @see MinOperation
	 */
	public boolean isToCalculateMarginal() {
		return isToCalculateMarginal;
	}


}
