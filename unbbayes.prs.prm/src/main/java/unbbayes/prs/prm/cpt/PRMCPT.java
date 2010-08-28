/**
 * 
 */
package unbbayes.prs.prm.cpt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import unbbayes.prs.prm.IDependencyChain;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.cpt.compiler.IPRMCPTCompiler;
import unbbayes.prs.prm.cpt.compiler.PRMCPTCompiler;
import unbbayes.util.Debug;

/**
 * Default implementation of {@link IPRMCPT} using
 * a linearized float table.
 * @author Shou Matsumoto
 *
 */
public class PRMCPT implements IPRMCPT {

	private List<Float> tableValues;
	private IPRMDependency prmDependency;
	private IPRMCPTCompiler cptCompiler;

	/**
	 * At least one constructor must be visible for subclasses
	 * to allow inheritance.
	 */
	protected PRMCPT() {
		super();
		// initialize
		this.tableValues = new ArrayList<Float>();
		this.tableValues.add(1.0f);
		this.cptCompiler = PRMCPTCompiler.newInstance();
	}
	
	/**
	 * Default construction method.
	 * @param prmDependency
	 * @return
	 */
	public static PRMCPT newInstance(IPRMDependency prmDependency) {
		PRMCPT ret = new PRMCPT();
		ret.prmDependency = prmDependency;
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IPRMCPT#getCPTCompiler()
	 */
	public IPRMCPTCompiler getCPTCompiler() {
		return this.cptCompiler;
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.prm.cpt.IPRMCPT#getDependencyChains()
//	 */
//	public List<IDependencyChain> getDependencyChains() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IPRMCPT#getPRMDependency()
	 */
	public IPRMDependency getPRMDependency() {
		return this.prmDependency;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IPRMCPT#setCPTCompiler(unbbayes.prs.prm.cpt.compiler.IPRMCPTCompiler)
	 */
	public void setCPTCompiler(IPRMCPTCompiler cptCompiler) {
		this.cptCompiler = cptCompiler;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IPRMCPT#setPRMDependency(unbbayes.prs.prm.IPRMDependency)
	 */
	public void setPRMDependency(IPRMDependency hostNode) {
		this.prmDependency = hostNode;
		if (this.prmDependency != null && !this.equals(this.prmDependency.getCPT())) {
			this.prmDependency.setCPT(this);
		}
	}

	/**
	 * @return the tableValues
	 */
	public List<Float> getTableValues() {
		// Let's auto ajust size, depending on the parents' states' size
		try {
			// calculate the expected size. 
			int totalSize = this.getPRMDependency().getAttributeDescriptor().getStatesSize();
			for (IDependencyChain parentChain : this.getPRMDependency().getIncomingDependencyChains()) {
				totalSize *= parentChain.getDependencyFrom().getAttributeDescriptor().getStatesSize();
			}
			// If it differs from the current size, then re-build tableValues adding "0.0"s
			if (this.tableValues.size() < totalSize) {
				// actually, the hanging problem (ArrayIndexOutOfBoundException) 
				// would only happen if totalSize (i.e. expected size) >  tableValues.size() (i.e. actual size)
				for (int i = 0; i < totalSize - this.tableValues.size(); i++) {
					this.tableValues.add(0.0f);	// add default value (0.0) to the unfilled columns
				}
			}
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failed to auto ajust table values", e);
		}
		
		return tableValues;
	}

	/**
	 * @param tableValues the tableValues to set
	 */
	public void setTableValues(List<Float> tableValues) {
		this.tableValues = tableValues;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.IPRMCPT#getTableValuesByColumn(java.util.Map)
	 */
	public List<Float> getTableValuesByColumn(Map<IPRMDependency, String> parentStateMap) {
		
		// the size of the column is the number of states it can have.
		int columnSize = this.getPRMDependency().getAttributeDescriptor().getStatesSize();
		
		// this is the return
		List<Float> ret = new ArrayList<Float>();
		
		// if parentStateMap == null or empty, just return the first column. This is useful if a node has no parent at all (1st column is the table itself)
		if (parentStateMap == null || parentStateMap.isEmpty()) {
			// fill first column
			for (int i = 0; i < columnSize; i++) {
				ret.add(this.getTableValues().get(i));
			}
			return ret;
		}
		
		// lets calculate the index of the column described by parentStateMap
		int columnIndex = 0; // this is going to be the index of the selected column
		
		/*
		 * The following columnJumpFactor helps us calculate how many columns we shall jump to go from one "block" to other.
		 * That is, if we have the following header:
		 *   _____________________________________________________________________________________________________
		 *   |P2 |           0           |           1           |           2           |           3           |
		 *   |P1 |     0     |     1     |     0     |     1     |     0     |     1     |     0     |     1     |
		 * 	 |P0 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 | 0 | 1 | 2 |
		 *   ====================================================================================================
		 *  
		 *  ...Then, we must jump columnJumpFactor(P0) == 1 column  to go from P0 == 0 to P0 == 1, (columnJumpFactor(P0) = 1, by default)
		 *           we must jump columnJumpFactor(P1) == 3 columns to go from P1 == 0 to P1 == 1, (columnJumpFactor(P1) = P0.stateSize() * columnJumpFactor(P0))
		 *       and we must jump columnJumpFactor(P2) == 6 columns to go from P2 == 0 to P2 == 1, (columnJumpFactor(P2) = P1.stateSize() * columnJumpFactor(P1))
		 *  
		 *  Note that the index of column (P0 = 0, P1 = 1, P2 = 2) can be evaluated by the following math:
		 *  
		 *  index = (P0 * columnJumpFactor(P0)) + (P1 * columnJumpFactor(P1)) + (P2 * columnJumpFactor(P2))
		 *        = (0 * 1) + (1 * (1 * stateSize(P0)))  + (2 * (1 * stateSize(P0)* stateSize(P1)))
		 *        = (0) + (1 * (1 * 3))  + (2 * (1 * 3* 2))
		 *        = 0 + 3 + 12
		 *        = 15
		 */
		int columnJumpFactor = 1; 
		
		// calculate columnIndex by iterating over parents. 
		// getPRMDependency().getIncomingDependencyChains() retrieves edges coming to this node (so, we can find out who are the parents)
		for (IDependencyChain parentChain : this.getPRMDependency().getIncomingDependencyChains()) {
			
			// extract the state of current parent from parentStateMap
			// parentChain.getDependencyFrom() retrieves the origin or the edge (i.e. the parent)
			String parentState = parentStateMap.get(parentChain.getDependencyFrom());
			
			// the parent was not declared. It means that parentStateMap was incomplete.
			if (parentState == null) {
				throw new IllegalArgumentException(
						"The input map must specify one state for all parents of " 
						+ this.getPRMDependency().getAttributeDescriptor()
						+ " in order to obtain the column's values, but the argument was: " 
						+ parentStateMap);
			}
			
			// extract the position of the parentState TODO avoid linear search
			int statePosition;	// position of parentState in parentChain.getDependencyFrom()
			for (statePosition = 0; statePosition < parentChain.getDependencyFrom().getAttributeDescriptor().getStatesSize(); statePosition++) {
				if (parentState.equals(parentChain.getDependencyFrom().getAttributeDescriptor().getStateAt(statePosition))) {
					// found state
					break;
				}
			}
			
			// check state consistency (if state was not found, parentStateMap was specifying a wrong state for that parent)
			if (statePosition >= parentChain.getDependencyFrom().getAttributeDescriptor().getStatesSize()) {
				throw new IllegalArgumentException(
						"The state " 
						+ parentState
						+ " was not found in attribute " 
						+ parentChain.getDependencyFrom().getAttributeDescriptor()
						+ ", a parent of "
						+ this.getPRMDependency().getAttributeDescriptor());
			}
			
			// Update column index
			columnIndex += (columnJumpFactor * statePosition);
			
			// Update columnJumpFactor
			columnJumpFactor *= parentChain.getDependencyFrom().getAttributeDescriptor().getStatesSize();
		}
		
		// since #getTableValues() is a linearized table, the index of the 1st cell of a column is:
		int cellIndex = columnIndex * columnSize;
		
		// lets now fill the ret using the column pointed by columnIndex (thus, iterating from cellIndex)
		for (int i = cellIndex; i < (cellIndex + columnSize); i++) {
			ret.add(this.getTableValues().get(i));
		}
		
		return ret;
	}

	
	
}
