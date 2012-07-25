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
	private boolean isToCalculateMarginal = true;

	
	
//	/**
//	 * @return the marginalList
//	 */
//	protected double[] getMarginalList() {
//		return marginalList;
//	}
//
//	/**
//	 * @param marginalList the marginalList to set
//	 */
//	protected void setMarginalList(double[] marginalList) {
//		this.marginalList = marginalList;
//	}

	/**
	 * Default constructor is protected only to allow inheritance.
	 * Use {@link #getInstance()} to instantiate.
	 */
	protected AssetNode() {
		super();
		this.position = null;
		this.backColor = null;
		this.mean= null;
		this.size = null;
		this.sizeVariable = null;
		this.nodeNameChangedListenerList.clear();
	}
	
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#copyMarginal()
	 */
	@Override
	public void copyMarginal() {
		// never copy marginal
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
    	return;
//    	initMarginalList();	// at least make sure marginal list is not null
//    	if (!isToCalculateMarginal()) {
//    		return;
//    	}

//    	//    	return;	// asset nodes has no semantics for marginals
////    	for (int i = 0; i < marginalList.length; i++) {
////    		marginalList[i] = 1;
////    	}
////    	return;
    	
//    	DoublePrecisionProbabilisticTable auxTab = (DoublePrecisionProbabilisticTable) ((DoublePrecisionProbabilisticTable)getAssociatedClique().getProbabilityFunction()).clone();
//        int index = auxTab.indexOfVariable(this);
//        int size = getAssociatedClique().getProbabilityFunction().variableCount();
//        for (int i = 0; i < size; i++) {
//            if (i != index) {
//                auxTab.removeVariable(getAssociatedClique().getProbabilityFunction().getVariableAt(i));
//            }
//        }
//
//        int tableSize = auxTab.tableSize();
//        if (tableSize > marginalList.length) {
//        	Debug.println(getClass(), "There is some inconsistency. Maybe there is some node with no state at all.");
//        } else {
//        	for (int i = 0; i < tableSize; i++) {
//        		marginalList[i] = auxTab.getDoubleValue(i);
//        	}
//        }
    }
    
    /**
     * If {@link #isToCalculateMarginal()}, then it calculates the marginals
     * using double precision.
     * @return array with marginal
     * @see #marginal()
     */
    public double[] calculateDoublePrecisionMarginal() {
    	initMarginalList();	// at least make sure marginal list is not null
    	if (!isToCalculateMarginal()) {
    		return null;
    	}
    	DoublePrecisionProbabilisticTable auxTab = (DoublePrecisionProbabilisticTable) ((DoublePrecisionProbabilisticTable)getAssociatedClique().getProbabilityFunction()).clone();
        int index = auxTab.indexOfVariable(this);
        int size = getAssociatedClique().getProbabilityFunction().variableCount();
        for (int i = 0; i < size; i++) {
            if (i != index) {
                auxTab.removeVariable(getAssociatedClique().getProbabilityFunction().getVariableAt(i));
            }
        }
        
        double[] ret = new double[getStatesSize()];

        int tableSize = auxTab.tableSize();
        if (tableSize > marginalList.length) {
        	Debug.println(getClass(), "There is some inconsistency. Maybe there is some node with no state at all.");
        	return null;
        } else {
        	for (int i = 0; i < tableSize; i++) {
        		ret[i] = auxTab.getDoubleValue(i);
        	}
        }
        
        return ret;
    }
    

//	/**
//	 * @deprecated use {@link #getDoubleMarginalAt(int)}
//	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
//	 */
//	@Deprecated
//	public float getMarginalAt(int index) {
//		throw new UnsupportedOperationException();
//	}
//	/**
//	 * @see unbbayes.prs.bn.TreeVariable#getMarginalAt(int)
//	 */
//	public double getDoubleMarginalAt(int index) {
//		return this.marginalList[index];
//	}
//
//	/**
//	 * @deprecated use {@link #setMarginalAt(int, double)} instead
//	 * @see unbbayes.prs.bn.TreeVariable#setMarginalAt(int, float)
//	 */
//	public void setMarginalAt(int index, float value) {
//		this.marginalList[index] = value;
//	}
//	/**
//	 * @see unbbayes.prs.bn.TreeVariable#setMarginalAt(int, float)
//	 */
//	public void setMarginalAt(int index, double value) {
//		this.marginalList[index] = value;
//	}

//	/**
//	 * @deprecated use {@link #setMarginalProbabilities(double[])}
//	 * @see unbbayes.prs.bn.TreeVariable#setMarginalProbabilities(float[])
//	 */
//	@Deprecated
//	public void setMarginalProbabilities(float[] marginalProbabilities) {
//		throw new UnsupportedOperationException();
//	}
//	/**
//	 * @see unbbayes.prs.bn.TreeVariable#setMarginalProbabilities(float[])
//	 */
//	public void setMarginalProbabilities(double[] marginalProbabilities) {
//		this.marginalList = marginalProbabilities;
//	}



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

//	/**
//	 * Instantiates the array of marginals and initializes its values to 1.
//	 * The initial value is set to 1 because it is the identity value
//	 * in any multiplication, and it represents 0 assets 
//	 * (q and assets are related by a logarithm relationship assets = constant * log(q)).
//	 * @see unbbayes.prs.bn.TreeVariable#initMarginalList()
//	 */
//	public void initMarginalList() {
//		super.initMarginalList();
//		this.marginalList = new double[getStatesSize()];
//		for (int i = 0; i < marginalList.length; i++) {
//			marginalList[i] = 1;	
//		}
//	}

	

   
}
