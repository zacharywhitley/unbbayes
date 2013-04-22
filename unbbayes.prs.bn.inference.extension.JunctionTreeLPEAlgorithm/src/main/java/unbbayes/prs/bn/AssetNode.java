/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collections;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable.ISumOperation;
import unbbayes.prs.bn.inference.extension.MinProductJunctionTree.MinOperation;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.Debug;
import unbbayes.util.SerializablePoint2D;

/**
 * @author Shou Matsumoto
 */
public class AssetNode extends DecisionNode implements IRandomVariable {
	
	private static final long serialVersionUID = -1926227466217847395L;
	private boolean isToCalculateMarginal = true;
	
	private int internalIdentificator = Integer.MIN_VALUE;

	
	
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

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.TreeVariable#addFinding(int, boolean)
	 */
	@Override
	public void addFinding(int stateIndex, boolean isNegative) {
		this.addFinding(stateIndex, isNegative, false);
	}
	
	/**
	 * @param isToUseQValues : if false, impossible states will be marked as {@link Float#POSITIVE_INFINITY}
	 * @see TreeVariable#addFinding(int, boolean)
	 */
	public void addFinding(int stateIndex, boolean isNegative, boolean isToUseQValues) {
		super.addFinding(stateIndex, isNegative);
        for (int i = 0; i < getStatesSize(); i++) {
        	// if not isNegative, set marginal to 1 if stateindex == i; 0 otherwise.
        	// if isNegative, set marginal to 0 if stateindex == i; 1 otherwise.
        	if (isToUseQValues) {
        		setMarginalAt(i, ((i==stateIndex)?(isNegative?0:1):(isNegative?1:0)) );
        	} else {
        		setMarginalAt(i, ((i==stateIndex)?(isNegative?Float.POSITIVE_INFINITY:1):(isNegative?1:Float.POSITIVE_INFINITY)) );
        	}
		}
	}



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
		this.parents = null;
		this.label = null;
		this.nodeNameChangedListenerList = Collections.EMPTY_LIST;
		this.setAdjacents(null);
		this.setChildren(null);
		states = new ArrayList<String>(0);
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
    	initMarginalList();	// at least make sure marginal list is not null
    	if (!isToCalculateMarginal()) {
    		return;
    	}

////    	for (int i = 0; i < marginalList.length; i++) {
////    		marginalList[i] = 1;
////    	}
////    	return;
    	
    	PotentialTable auxTab = (PotentialTable) ((PotentialTable)getAssociatedClique().getProbabilityFunction()).getTemporaryClone();
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
    
//    /**
//     * If {@link #isToCalculateMarginal()}, then it calculates the marginals
//     * using double precision.
//     * @return array with marginal
//     * @see #marginal()
//     */
//    public double[] calculateDoublePrecisionMarginal() {
//    	initMarginalList();	// at least make sure marginal list is not null
//    	if (!isToCalculateMarginal()) {
//    		return null;
//    	}
//    	DoublePrecisionProbabilisticTable auxTab = (DoublePrecisionProbabilisticTable) ((DoublePrecisionProbabilisticTable)getAssociatedClique().getProbabilityFunction()).clone();
//        int index = auxTab.indexOfVariable(this);
//        int size = getAssociatedClique().getProbabilityFunction().variableCount();
//        for (int i = 0; i < size; i++) {
//            if (i != index) {
//                auxTab.removeVariable(getAssociatedClique().getProbabilityFunction().getVariableAt(i));
//            }
//        }
//        
//        double[] ret = new double[getStatesSize()];
//
//        int tableSize = auxTab.tableSize();
//        if (tableSize > marginalList.length) {
//        	Debug.println(getClass(), "There is some inconsistency. Maybe there is some node with no state at all.");
//        	return null;
//        } else {
//        	for (int i = 0; i < tableSize; i++) {
//        		ret[i] = auxTab.getDoubleValue(i);
//        	}
//        }
//        
//        return ret;
//    }
    

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

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPosition(double, double)
	 */
	@Override
	public void setPosition(double x, double y) {
		if (this.position == null) {
			this.position = new SerializablePoint2D(DEFAULT_SIZE.getX(), DEFAULT_SIZE.getY());
		}
		super.setPosition(x, y);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setSize(double, double)
	 */
	@Override
	public void setSize(double width, double height) {
		if (this.size == null) {
			this.size = new SerializablePoint2D(DEFAULT_SIZE.getX(), DEFAULT_SIZE.getY());
		}
		super.setSize(width, height);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRandomVariable) {
			return this.internalIdentificator == ((IRandomVariable)obj).getInternalIdentificator();
		}
		return false;
	}

	/**
	 * @return the internalIdentificator
	 */
	public int getInternalIdentificator() {
		return internalIdentificator;
	}

	/**
	 * @param internalIdentificator the internalIdentificator to set
	 */
	public void setInternalIdentificator(int internalIdentificator) {
		this.internalIdentificator = internalIdentificator;
	}

	/**
	 * @see unbbayes.prs.bn.IRandomVariable#getProbabilityFunction()
	 * @deprecated
	 */
	public IProbabilityFunction getProbabilityFunction() {
		return null;
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

	/**
	 * Inserts a state with the specified name at the end of the list,
	 * but it does not update {@link #getProbabilityFunction()},
	 * because it is supposedly null or empty.
	 * @param state
	 *            Name of the state to be added.
	 */
	public void appendState(String state) {
		states.add(state);
	}
	
   
}
