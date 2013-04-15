/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;

/**
 * This is the clique table for assets.
 * @author Shou Matsumoto
 */
public class AssetTable extends ProbabilisticTable {

	private static final long serialVersionUID = 1397503323198098512L;

	/**
	 * Default constructor is protected in order
	 * to allow inheritance.
	 * @see #getInstance()
	 */
	protected AssetTable() {
		super();
		variableList = new ArrayList<Node>(0);
	}
	
	/** Default constructor method */
	public static PotentialTable getInstance() {
		return new AssetTable();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.PotentialTable#updateRecursive(float[], int, int, int, int)
	 */
	protected void updateRecursive(float[] marginalList, int c, int linear, int index, int state) {
		if (index < 0) {
			return;
		}
    	if (c >= this.variableList.size()) {
    		if ((marginalList[state] == Float.POSITIVE_INFINITY)) {
    			// Float.POSITIVE_INFINITY means impossible value.
    			// However, Float.POSITIVE_INFINITY may result in Float.NaN if operated with 0, 
    			// or Float.NEGATIVE_INFINITY if operated with negative number.
    			// So, instead of multiplying, just set to Float.POSITIVE_INFINITY whatever values are originally in this.dataPT.data[linear].
    			this.dataPT.data[linear] = Float.POSITIVE_INFINITY;
    			// Note: posteriorly, the min propagation will ignore positive infinities, because it is greater than any float value.
    		} else {
    			this.dataPT.data[linear] *= marginalList[state];
    		}
    		return;    		    		
    	}
    	int currentFactor = this.factorsPT[c];
    	if (index == c) {
    		for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(marginalList, c+1, linear + i*currentFactor , index, i);
    		}
    	} else {
	    	for (int i = this.variableList.get(c).getStatesSize() - 1; i >= 0; i--) {    		    		
	    		updateRecursive(marginalList, c+1, linear + i*currentFactor , index, state);
    		}
    	}
    }
	
	public PotentialTable newInstance() {
		return getInstance();
	}

}
