/**
 * 
 */
package unbbayes.prs.prm.cpt.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.prm.AttributeValue;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IDependencyChain;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.cpt.AggregateFunctionMode;
import unbbayes.prs.prm.cpt.IPRMCPT;
import unbbayes.util.Debug;

/**
 * Default implementation of {@link IPRMCPTCompiler}
 * @author Shou Matsumoto
 *
 */
public class PRMCPTCompiler implements IPRMCPTCompiler {

	/**
	 * At least one constructor must be visible for subclasses
	 * to allow inheritance.
	 */
	protected PRMCPTCompiler() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * @return
	 */
	public static PRMCPTCompiler newInstance () {
		PRMCPTCompiler ret = new PRMCPTCompiler();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.cpt.compiler.IPRMCPTCompiler#compileCPT(unbbayes.prs.prm.IAttributeValue, unbbayes.prs.bn.IRandomVariable, java.util.Map)
	 */
	public IProbabilityFunction compileCPT(IAttributeValue prmNode, IRandomVariable probabilityFunctionOwner, Map<INode, IAttributeValue> parentMap) {
		
		// CPT to be filled
		PotentialTable tableToFill = null;
		
		// initial assertion
		if (probabilityFunctionOwner.getProbabilityFunction() instanceof PotentialTable) {
			tableToFill = (PotentialTable)probabilityFunctionOwner.getProbabilityFunction();
		} else {
			// probabilityFunctionOwner does not contain expected table format. We cannot handle it
			throw new IllegalArgumentException( probabilityFunctionOwner + " contains a CPT that cannot be handled by this class. Check PRM compilation algorithm and select appropriate PRM compiler.");
		}
		
		// extract the CPT to be read
		IPRMCPT prmCPT = prmNode.getAttributeDescriptor().getPRMDependency().getCPT();
		
		// start filling table
		// OBS. we assume prmNode and probabilityFunctionOwner has consistent parents 
		// (i.e. each prmNode's parents maps 1-1 to each probabilityFunctionOwner's parents)
		// we also assume probabilityFunctionOwner's CPT has sufficient size.
		// TODO implement a more fail safe procedure
		
		// how many rows do we have in tableToFill
		int targetRowSize = tableToFill.getVariableAt(0).getStatesSize();
		
		// number of rows in target table must be equal to the prmNode's CPT's number of rows
		if (targetRowSize != prmNode.getAttributeDescriptor().getStatesSize()) {
			throw new IllegalArgumentException(
					probabilityFunctionOwner
					+ " has " 
					+ targetRowSize 
					+ " rows in CPT, while "
					+ prmNode.getAttributeDescriptor()
					+ " has " 
					+ prmNode.getAttributeDescriptor().getStatesSize() 
					+ " rows. Those two must match. Check PRM compiler.");
		}
		
		// how many columns do we have in tableToFill: table size / number of rows
		int targetColumnSize = tableToFill.tableSize() / targetRowSize;
		
		// iterate over columns of the target cpt
		for (int columnNumber = 0; columnNumber < targetColumnSize; columnNumber++) {
			
			// obtain column in original prm cpt. 
			// Note that the first column is the default, because getTableValuesByColumn returns the 1st column if getParentStateMapByTargetCPTColumnNumber == null
			Map<IPRMDependency, String> parentStateMap = this.getParentStateMapByTargetCPTColumnNumber(parentMap, prmCPT, tableToFill, columnNumber);
			List<Float> columnValues = null;
			if (parentStateMap == null || parentStateMap.isEmpty()) {
				Debug.println(this.getClass(), 
						"Could not obtain parent's states map for " 
						+ probabilityFunctionOwner
						+  ". Using default...");
				if (!prmNode.getAttributeDescriptor().isMandatory()) {
					// use the table provided by user if it exists (if mandatory, usually no table is provided)
					try {
						// use a copy (just the needed part) of the original table (list) instead of using the original one directly
						columnValues = prmCPT.getTableValues().subList(0, targetRowSize);
					} catch (Exception e) {
						Debug.println(this.getClass(), "Failed to extract a copy/sublist of user-provided default probability for " 
								+ prmNode + ". Using the original one instead.", e);
						columnValues = prmCPT.getTableValues();
					}
				} else {
					// add linear distribution if no value is retrievable
					columnValues = new ArrayList<Float>();
					float val = 1.0f / targetRowSize;
					for (int i = 0; i < targetRowSize; i++) {
						columnValues.add(val);
					}
				}
			} else {
				try {
					columnValues = prmCPT.getTableValuesByColumn(parentStateMap);
				} catch (Exception e) {
					e.printStackTrace();
					// use default value, if it could not be extracted
					columnValues = prmCPT.getTableValuesByColumn(null);
				}
			}
			if (columnValues.size() != targetRowSize) {
				// usually, this code will not be executed, unless getTableValuesByColumn is ill implemented
				throw new IllegalArgumentException(
						probabilityFunctionOwner
						+ " has " 
						+ targetRowSize 
						+ " rows in CPT, while "
						+ prmNode.getAttributeDescriptor()
						+ " reports " 
						+ columnValues.size()
						+ " rows. Those two must match. Check unbbayes.prs.prm.cpt.IPRMCPT#getTableValuesByColumn(Map<IPRMDependency, String>).");
			}
			
			int targetLinearColumnIndex = (columnNumber * targetRowSize); // linearized columnIndex for target cpt
			
			// fill column of target cpt
			for (int i = 0; i < targetRowSize; i++) {
				tableToFill.setValue(targetLinearColumnIndex + i, columnValues.get(i));
			}
		}
		
		/*
		 *              ________________________________________________________________________
		 * 				|D.a      |             true             |             false            |
		 * 				|Mode(B.a)|     true      |    false     |     true      |    false     |
		 * 				|A.a      |  true | false | true | false |  true | false | true | false |
		 * 				|=======================================================================|
		 * 				|true     |   1       1       1      1       .8      .7     .3     .5   |
		 * 				|false    |   0       0       0      0       .2      .3     .7     .5   |
		 * 				------------------------------------------------------------------------
		 *__________________________________________________________________________________________________________________________________
		 *|D1.a |                            true                             |                            false                            |
		 *|B2.a |             true             |             false            |             true             |             false            |
		 *|B1.a |     true      |    false     |     true      |    false     |     true      |    false     |     true      |    false     |
		 *|A1.a |  true | false | true | false |  true | false | true | false |  true | false | true | false |  true | false | true | false |
		 *|=================================================================================================================================|
		 *|true |   1       1       1      1       1       1       1      1   |   .8     .7      .8     .7      .3      .5      .3     .5   |
		 *|false|   0       0       0      0       0       0       0      0   |   .2     .3      .2     .3      .7      .5      .7     .5   |
		 *----------------------------------------------------------------------------------------------------------------------------------
		 */
		
		// TODO Auto-generated method stub
		return tableToFill;
	}


	/**
	 * TODO this method assumes ALPHA restriction, which allows only mode as aggregate function and 1 level FK chain. Fix it!
	 * Fills Map<IPRMDependency, String> used by {@link IPRMCPT#getTableValuesByColumn(Map)}
	 * @param prmCPT
	 * @param tableToFill
	 * @param columnNumber
	 * @return
	 */
	private Map<IPRMDependency, String> getParentStateMapByTargetCPTColumnNumber(
			Map<INode, IAttributeValue> parentMap, IPRMCPT prmCPT,
			PotentialTable tableToFill, int columnNumber) {
		
		// ALPHA consistency check - allow only mode as aggregate function
		for (IDependencyChain depChain : prmCPT.getPRMDependency().getIncomingDependencyChains()) {
			if (depChain.getAggregateFunction() != null && !(depChain.getAggregateFunction() instanceof AggregateFunctionMode)) {
				throw new IllegalArgumentException("ALPHA allows only mode as aggregate function.");
			}
		}
		
		// ALPHA consistency check - allow only 1 dependency chain per parent
		Set<IPRMDependency> prmDependencySetForALPHACheck = new HashSet<IPRMDependency>();
		for (IDependencyChain depChain : prmCPT.getPRMDependency().getIncomingDependencyChains()) {
			if (prmDependencySetForALPHACheck.contains(depChain.getDependencyFrom())) {
				throw new IllegalArgumentException("ALPHA allows only one dependency chain pointing to the same attribute. "
						+ prmCPT.getPRMDependency().getAttributeDescriptor()
						+ " contains more than one dependency to "
						+ depChain.getDependencyFrom().getAttributeDescriptor());
			}
			prmDependencySetForALPHACheck.add(depChain.getDependencyFrom());
		}
		
		Map<IPRMDependency, String> ret = new HashMap<IPRMDependency, String>();
		
		// a map classifying states of parents by its original IPRMDependency. It uses sample (stub) IAttributeValue (IAttributeValue with only a value)
		// TODO fix ALPHA restriction assuming 1 FK to the same class attribute. See dependencyChainSolver
		Map<IPRMDependency, List<IAttributeValue>> possibleStatesMap = new HashMap<IPRMDependency, List<IAttributeValue>>();
		
		/*
		 * columnMultFactor is a recursive value:
		 * 		columnMultFactor(Parent0) = 1;
		 * 		columnMultFactor(Parent1) = Parent0.statesSize * columnMultFactor(Parent0);
		 * 		columnMultFactor(ParentN) = ParentN-1.statesSize * columnMultFactor(ParentN-1);
		 */
		int columnMultFactor = 1;
		
		// create possibleStatesMap
		for (int i = 1; i < tableToFill.variableCount(); i++) {
			
			List<IAttributeValue> states = possibleStatesMap.get(parentMap.get(tableToFill.getVariableAt(i)).getAttributeDescriptor().getPRMDependency());
			if (states == null) {
				states = new ArrayList<IAttributeValue>();
				possibleStatesMap.put(parentMap.get(tableToFill.getVariableAt(i)).getAttributeDescriptor().getPRMDependency(), states);
			}
			
			IAttributeValue sampleValue = AttributeValue.newInstance(null, null);	// generate sample attribute value
			// (columnNumber / columnMultFactor(ParentN)) mod ParentN.statesSize is the index of a state of a parent.
			sampleValue.setValue(tableToFill.getVariableAt(i).getStateAt((columnNumber / columnMultFactor) % tableToFill.getVariableAt(i).getStatesSize()));
			states.add(sampleValue);
			
			// update recursive value
			columnMultFactor *= tableToFill.getVariableAt(i).getStatesSize();
		}
		
		// TODO ALPHA restriction: use only mode aggregation function.
		// NOTE: mode applyed to a single value returns the value itself, so, we may apply aggregate function for direct FK as well
		for (IPRMDependency key : possibleStatesMap.keySet()) {
			ret.put(key, AggregateFunctionMode.newInstance(null).evaluate(possibleStatesMap.get(key)).getValue());
		}
		
		return ret;
	}
	

}
