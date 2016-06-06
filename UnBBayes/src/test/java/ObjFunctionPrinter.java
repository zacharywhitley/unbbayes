
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;


/**
 * This is just a test program (non-JUnit) that shows how we can use {@link PotentialTable}
 * in order to generate an objective function and constraints for non-linear optimizers.
 * The output of this class can be used as an input for a third-party non-linear optimizer.
 */
public class ObjFunctionPrinter {

	private static float greaterThanValue = 0;
	
	private boolean isToBreakLineOnObjectFunction = false;

	private boolean isToSubtract1WayLikelihood = true;;
	
	private static String threatName = "Threat";
	private static String[] indicatorNames = {"I1", "I2", "I3", "I4", "I5"};
	private static String[] detectorNames = {"D1", "D2", "D3", "D4", "D5"};

	private static boolean isStrictlyGreaterThan = true;
	
	
	private static final int[][][] indicatorCorrelations = {
		{
			// I2=true, I2=false
			{3,	2},			// I1=true
			{211,	4051}	// I1 = false
		},
		{
			// I3=true, I3=false
			{2,3},			// I1=true
			{213,4049},		// I1 = false
		},
		{
			// I4
			{1,4},			// I1
			{213,4049},
		},
		{
			// I3
			{1,4},			// I2
			{214,4048},
		},
		{
			{31,183},
			{184,3869},
		},
		{
			{46,168},
			{168,3885},
		},
		{
			{20,194},
			{195,3858},
		},
		{
			{31,184},
			{183,3869},
		},
		{
			{25,190},
			{190,3862},
		},
		{
			{18,196},
			{197,3856},
		},
	};
	
	private static final int[][][] detectorCorrelations = {
		{
			{3,2},
			{209,4053}
		},
		{
			{2,3},
			{213,4049}
		},
		{
			{1,4},
			{201,4061}
		},
		{
			{1,4},
			{213,4049}
		},
		{
			{31,181},
			{184,3871}
		},
		{
			{44,168},
			{158,3897}
		},
		{
			{20,192},
			{194,3861}
		},
		{
			{28,187},
			{174,3878}
		},
		{
			{25,190},
			{189,3863}
		},
		{
			{18,184},
			{196,3869},
		},
	};
	
	private static final int[][][] threatIndicatorMatrix = {
		{
			{3,	271},
			{1,	3529}
		},
		{
			{211,63},
			{1,3529}
		},
		{
			{211,63},
			{1,3529}
		},
		{
			{211,63},
			{1,3529}
		},
		{
			{211,63},
			{1,3529}
		},
	};
	
	private static final int[][][] detectorIndicatorMatrix = {
		{
			{3,1},
			{1,3799}
		},
		{
			{211,1},
			{5,3587}
		},
		{
			{211,1},
			{4,3588}
		},
		{
			{211,1},
			{8,3584}
		},
		{
			{211,1},
			{4,3588},
		},
	};


	
	/**
	 * Auto-generated default constructor
	 */
	public ObjFunctionPrinter() {}
	
	/**
	 * @param variableMap : in/out argument that will be filled with variables in common.
	 * @param vars : name of the variables to be included in table (e.g. threat, I1, I2, ..., I5)
	 * @return the table with Threat var and all indicators
	 */
	public PotentialTable getJointTable(Map<String, INode> variableMap, List<String> vars) {
		
		// reverse list of vars, because first var will be itereted most
		vars = new ArrayList<String>(vars);	// clone
		Collections.reverse(vars);
		
		if (variableMap == null) {
			variableMap = new HashMap<String, INode>();
		}
		PotentialTable table = new ProbabilisticTable();
		for (String name : vars) {
			INode var = variableMap.get(name);
			if (var == null) {
				var = new ProbabilisticNode();
				var.setName(name);
				var.appendState("TRUE");
				var.appendState("FALSE");
				variableMap.put(name,var);
			}
			table.addVariable(var);
		}
		return table;
	}
	
	/**
	 * 
	 * @param variableMap
	 * @param matrix
	 * @param columnName
	 * @param rowName
	 * @return
	 */
	public PotentialTable getPotentialTable(Map<String, INode> variableMap, int[][] matrix, String columnName, String rowName) {
		if (variableMap == null) {
			variableMap = new HashMap<String, INode>();
		}
		ProbabilisticTable table = new ProbabilisticTable();
		
		INode columnVar = variableMap.get(columnName);
		if (columnVar == null) {
			columnVar = new ProbabilisticNode();
			columnVar.setName(columnName);
			columnVar.appendState("TRUE");
			columnVar.appendState("FALSE");
			variableMap.put(columnName,columnVar);
		}
		
		table.addVariable(columnVar);
		
		INode rowVar = variableMap.get(rowName);
		if (rowVar == null) {
			rowVar = new ProbabilisticNode();
			rowVar.setName(rowName);
			rowVar.appendState("TRUE");
			rowVar.appendState("FALSE");
			variableMap.put(rowName,rowVar);
		}
		
		table.addVariable(rowVar);
		
		for (int n=0,i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++,n++) {
				table.setValue(n, matrix[i][j]);
			}
		}
		
		return table;
	}
	
	
	
	
	/**
	 * 
	 * @param variableMap
	 * @param indicatorCorrelations
	 * @param indicatorNames
	 * @return
	 */
	public List<PotentialTable> getCorrelationTables(Map<String, INode> variableMap, int[][][] correlations, List<String> names) {
		
		if (combinatorial(names.size(), 2) != correlations.length) {
			throw new IllegalArgumentException("Combinations of indicatorNames is " + combinatorial(names.size(), 2) 
					+ ", but correlation matrix had size " + correlations.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int n=0,i=0; i < names.size()-1; i++) {
			String rowName = names.get(i);
			for (int j = i+1; j < names.size(); j++,n++) {
				int[][] matrix = correlations[n];
				String columnName = names.get(j);
				PotentialTable table = this.getPotentialTable(variableMap, matrix, columnName, rowName);
				ret.add(table);
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param variableMap
	 * @param threatName 
	 * @param indicatorCorrelations
	 * @param indicatorNames
	 * @return
	 */
	public List<PotentialTable> getThreatTables(Map<String, INode> variableMap, int[][][] tables, List<String> indicatorNames, String threatName) {
		
		if (tables.length != indicatorNames.size()) {
			throw new IllegalArgumentException("Number of tables of threat is expected to be: " + indicatorNames.size() + ", but was " + tables.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int i=0; i < tables.length; i++) {
			int[][] matrix = tables[i];
			String rowName = threatName;
			String columnName = indicatorNames.get(i);
			
			PotentialTable table = this.getPotentialTable(variableMap, matrix, columnName, rowName);
			
			ret.add(table);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param variableMap
	 * @param tables
	 * @param indicatorNameList
	 * @param detectorNameList
	 * @return
	 */
	public Collection<PotentialTable> getDetectorTables(Map<String, INode> variableMap, int[][][] tables,
			List<String> indicatorNameList, List<String> detectorNameList) {
		
		if (indicatorNameList.size() != detectorNameList.size()) {
			throw new IllegalArgumentException("List of detectors and indicators must be of same size: " + detectorNameList + " ; " + indicatorNameList);
		}
		
		if (tables.length != detectorNameList.size()) {
			throw new IllegalArgumentException("Number of tables of detector frequencies is expected to be: " + detectorNameList.size() + ", but was " + tables.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int i=0; i < tables.length; i++) {
			int[][] matrix = tables[i];
			String rowName = indicatorNameList.get(i);
			String columnName = detectorNameList.get(i);
			
			PotentialTable table = this.getPotentialTable(variableMap, matrix, columnName, rowName);
			
			ret.add(table);
		}
		return ret;
	}
	
	/**
	 * This will return a string like the following (but without the tabs):
	 * <pre>
	 * Threat,	I1,	I2,	I3,	I4,	I5,	D1,	D2,	D3,	D4,	D5,	P
	 * TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	p[1]
	 * TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	FALSE,	p[2]
	 * TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	FALSE,	TRUE,	p[3]
	 * TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	FALSE,	FALSE,	p[4]
	 * (...)
	 * FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	TRUE,	p[2047]
	 * FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	FALSE,	p[2048]
	 * </pre>
	 * @param jointTable
	 * @return
	 */
	public String getJointTableDescription(PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
	    // print names of vars in jointTable from last to first (because the first var has values changing more frequently)
	    for (int i = jointTable.variableCount()-1; i >= 0 ; i--) {
			printer.print(jointTable.getVariableAt(i).getName() + ",");
		}
	    printer.println("P");
	    
	    // print TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	p[1]
	    for (int rowIndex = 0; rowIndex < jointTable.tableSize(); rowIndex++) {
			int[] coord = jointTable.getMultidimensionalCoord(rowIndex);
	    	for (int varIndex = coord.length-1; varIndex >= 0; varIndex--) {
				printer.print(jointTable.getVariableAt(varIndex).getStateAt(coord[varIndex]) + ",");
			}
	    	printer.println("p[" + (rowIndex+1) + "]");
		}
		
		return output.toString();
	}
	
	/**
	 * This will return a string like the following
	 * <pre>
	 * objFun : w[1] * ( 3 * log( p[1] + p[2] +,...,p[k] ) + 9 * log(p[3] + ... + p[l]) ) + w[2] * ( 271 * log( p[1] + p[2] +,...,p[m] ) + ...
	 * </pre>
	 * @param unweightedTables
	 * @param weightedTables
	 */
	public String getObjFunction(List<PotentialTable> unweightedTables, List<PotentialTable> weightedTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
		printer.print("objFun :");
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		// weightedTables
		for (int tableIndex = 0; tableIndex < weightedTables.size(); tableIndex++) {
//			printer.print(" w[" + (tableIndex+1) + "] * ( ");
			printer.print(" w * ( ");
			PotentialTable currentTable = weightedTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				printer.print(((int)currentTable.getValue(cellIndex)) + " * log(");
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					boolean found = true;
					for (int i = 0; i < coord.length; i++) {
						INode var = currentTable.getVariableAt(i);
						int indexInJointTable = jointTable.getVariableIndex((Node) var);
						if (jointCoord[indexInJointTable] != coord[i]) {
							found = false;
							break;
						}
					}
					
					if (found) {
						if (found1stP) {
							printer.print(" +");
						}
						printer.print(" p[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				printer.print(" )");
				
				if (cellIndex + 1 < currentTable.tableSize()) {
					printer.print(" + ");
				}
				
			}
			
			printer.print(" )");
			
			if (isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
			if (tableIndex + 1 < weightedTables.size()) {
				printer.print(" +");
			}
			
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		printer.print(" + ");
		
		// unweightedTables
		for (int tableIndex = 0; tableIndex < unweightedTables.size(); tableIndex++) {
			PotentialTable currentTable = unweightedTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				printer.print(((int)currentTable.getValue(cellIndex)) + " * log(");
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					boolean found = true;
					for (int i = 0; i < coord.length; i++) {
						INode var = currentTable.getVariableAt(i);
						int indexInJointTable = jointTable.getVariableIndex((Node) var);
						if (jointCoord[indexInJointTable] != coord[i]) {
							found = false;
							break;
						}
					}
					
					if (found) {
						if (found1stP) {
							printer.print(" +");
						}
						printer.print(" p[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				printer.print(" )");
				
				if (cellIndex + 1 < currentTable.tableSize()) {
					printer.print(" + ");
				}
				
			}
			
			if (isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
			if (tableIndex + 1 < unweightedTables.size()) {
				printer.print(" + ");
			}
			
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		// make sure we only count the 1-way log likelihood once
		if (isToSubtract1WayLikelihood()) {
			printer.print(get1WayLikelihoodSubtraction(unweightedTables, weightedTables, jointTable));
		}
		
		printer.println(";");
		
		return output.toString();
		
	}
	
	/**
	 * 
	 * @param unweightedTables : correlation table
	 * @param weightedTables : table from naive bayes distribution
	 * @param jointTable : used just in order to treat the indexes of cells of a table containing all variables
	 * @return
	 */
	public String get1WayLikelihoodSubtraction(List<PotentialTable> unweightedTables, List<PotentialTable> weightedTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(output);
		
		
	    
	    // count number of occurrences of variables
	    Map<String, Integer> weightedTableCounter = new HashMap<String, Integer>();
	    for (PotentialTable table : weightedTables) {
			for (int i = 0; i < table.getVariablesSize(); i++) {
				INode var = table.getVariableAt(i);
				Integer count = weightedTableCounter.get(var.getName());
				if (count == null) {
					count = 0;
				}
				count++;
				weightedTableCounter.put(var.getName(), count);
			}
		}
	    Map<String, Integer> unweightedTableCounter = new HashMap<String, Integer>();
	    for (PotentialTable table : unweightedTables) {
	    	for (int i = 0; i < table.getVariablesSize(); i++) {
	    		INode var = table.getVariableAt(i);
	    		Integer count = unweightedTableCounter.get(var.getName());
	    		if (count == null) {
	    			count = 0;
	    		}
	    		count++;
	    		unweightedTableCounter.put(var.getName(), count);
	    	}
	    }
	    
//	    Map<String, INode> jointTableVariableMap = new HashMap<String, INode>();
//	    for (int i = 0; i < jointTable.getVariablesSize(); i++) {
//			jointTableVariableMap.put(jointTable.getVariableAt(i).getName(), jointTable.getVariableAt(i));
//		}
	    

		// check marginal consistency between tables in same category and get marginal counts (frequency) for each
	    Map<String, int[]> weightedMarginals = this.getMarginalCounts(weightedTables);
	    Map<String, int[]> unweightedMarginals = this.getMarginalCounts(unweightedTables);
		
	    // treat weighted marginals
	    for (int varIndex = jointTable.getVariablesSize()-1; varIndex >= 0; varIndex--) {
			
	    	INode var = jointTable.getVariableAt(varIndex);
	    	String varName = var.getName();
	    	
	    	if (!weightedMarginals.containsKey(varName) || !weightedTableCounter.containsKey(varName)) {
	    		continue;	// ignore marginals that are not present in weighted table
	    	}
	    	
	    	int[] marginal = weightedMarginals.get(varName);
	    	Integer numToRemove = weightedTableCounter.get(varName);
			if (!unweightedTableCounter.containsKey(varName) || unweightedTableCounter.get(varName).intValue() == 0) {
				// if there is no occurrences in the other table, we need to consider 1 less marginals
				numToRemove--;
			}
			printer.print(" - w * " + numToRemove + " * (");
			
			for (int stateIndex = 0; stateIndex < marginal.length; stateIndex++) {
				printer.print(marginal[stateIndex] + " * log(");

				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
					int indexInJointTable = jointTable.getVariableIndex((Node) var);
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					if (jointCoord[indexInJointTable] == stateIndex) {
						if (found1stP) {
							printer.print(" +");
						}
						printer.print(" p[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				printer.print(")");
				if (stateIndex + 1  < marginal.length) {
					printer.print(" + ");
				}
			}
			
			printer.print(") ");

			if (isToBreakLineOnObjectFunction()) {
				printer.println();
			}
		}
	    
		
	    if (isToBreakLineOnObjectFunction()) {
	    	printer.println();
	    }
	    
		// for unweighted marginals, 
	    for (int varIndex = jointTable.getVariablesSize()-1; varIndex >= 0; varIndex--) {
	    	
	    	INode var = jointTable.getVariableAt(varIndex);
	    	String varName = var.getName();
	    	
	    	if (!unweightedMarginals.containsKey(varName) || !unweightedTableCounter.containsKey(varName)) {
	    		continue;	// ignore marginals that are not present in unweighted table
	    	}
	    	
	    	int[] marginal = unweightedMarginals.get(varName);
	    	Integer numToRemove = unweightedTableCounter.get(varName)-1;	// always remove 1-way log-likelihood except 1 entry
	    	
	    	printer.print(" - " + numToRemove + " * (");
	    	
	    	for (int stateIndex = 0; stateIndex < marginal.length; stateIndex++) {
	    		printer.print(marginal[stateIndex] + " * log(");
	    		
	    		boolean found1stP = false;
	    		for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
	    			
	    			int indexInJointTable = jointTable.getVariableIndex((Node) var);
	    			int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
	    			
	    			if (jointCoord[indexInJointTable] == stateIndex) {
	    				if (found1stP) {
	    					printer.print(" +");
	    				}
	    				printer.print(" p[" + (jointCellIndex+1) + "]");
	    				found1stP = true;
	    			}
	    			
	    		}
	    		
	    		printer.print(")");
	    		if (stateIndex + 1  < marginal.length) {
	    			printer.print(" + ");
	    		}
	    	}
	    	
	    	printer.print(") ");
	    	
	    	if (isToBreakLineOnObjectFunction()) {
	    		printer.println();
	    	}
	    }

		
		
		return output.toString();
	}

	/**
	 * 
	 * @param tables
	 * @return
	 */
	public Map<String, int[]> getMarginalCounts(List<PotentialTable> tables) {
		if (tables == null || tables.isEmpty()) {
			return Collections.EMPTY_MAP;
		}
		
		Map<String, int[]> marginals = new HashMap<String, int[]>();
		
		for (PotentialTable originalTable : tables) {
			for (int currentVarIndex = 0; currentVarIndex < originalTable.getVariablesSize(); currentVarIndex++) {
				PotentialTable table = originalTable.getTemporaryClone();
				INode var = originalTable.getVariableAt(currentVarIndex);
				// marginalize out other variables
				for (int indexToRemove = 0; indexToRemove < originalTable.getVariablesSize(); indexToRemove++) {
					if (indexToRemove != currentVarIndex) {
						table.removeVariable(table.getVariableAt(indexToRemove), false);	// do not normalize
					}
				}
				if (table.tableSize() != var.getStatesSize()) {
					throw new RuntimeException("Could not marginalize out variables other than " + var + ". Expected table size was " + var.getStatesSize() + ", but found " + table.tableSize());
				}
				// fill marginal
				int[] marginal = new int[var.getStatesSize()];
				for (int i = 0; i < table.tableSize(); i++) {
					marginal[i] = (int) table.getValue(i);
				}
				
				// compare with previously obtained marginal
				int[] oldMarginal = marginals.get(var.getName());
				if (oldMarginal == null) {
					// 1st time we calculate marginal
					marginals.put(var.getName(), marginal);
//					// also fill node with marginal (optional)
//					if (var instanceof TreeVariable) {
//						((TreeVariable) var).initMarginalList();
//						for (int i = 0; i < var.getStatesSize(); i++) {
//							((TreeVariable) var).setMarginalAt(i, marginal[i]);
//						}
//					}
				} else {
					if (oldMarginal.length != marginal.length) {
						throw new IllegalArgumentException(var + " has size " + oldMarginal.length + " and " + marginal.length);
					}
					for (int i = 0; i < oldMarginal.length; i++) {
						if (oldMarginal[i] != marginal[i]) {
							throw new IllegalArgumentException(var + " has different marginal at state " + i + ": " + oldMarginal[i] + " != " + marginal[i]);
						}
					}
				}
				
			}
		}
		return marginals;
	}

	/**
	 * objFun : p[1] + p[2] +,...,p[k] >= 0.0000001
	 * @param correlationTables
	 * @param threatTables
	 */
	public String getNonZeroRestrictions(List<PotentialTable> correlationTables, List<PotentialTable> threatTables, 
			PotentialTable jointTable, boolean isStrictlyGreater, float value) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
		// threatIndicatorMatrix
		for (int tableIndex = 0; tableIndex < threatTables.size(); tableIndex++) {
			PotentialTable currentTable = threatTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					boolean found = true;
					for (int i = 0; i < coord.length; i++) {
						INode var = currentTable.getVariableAt(i);
						int indexInJointTable = jointTable.getVariableIndex((Node) var);
						if (jointCoord[indexInJointTable] != coord[i]) {
							found = false;
							break;
						}
					}
					
					if (found) {
						if (found1stP) {
							printer.print(" +");
						}
						printer.print(" p[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				printer.print(" >");
				if (!isStrictlyGreater) {
					printer.print("= ");
				} else {
					printer.print(" ");
				}
				
				printer.print(value);
				
				printer.println(";");
				
			}
			
		}
		
		printer.println();
		
		// indicatorCorrelations
		for (int tableIndex = 0; tableIndex < correlationTables.size(); tableIndex++) {
			PotentialTable currentTable = correlationTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					boolean found = true;
					for (int i = 0; i < coord.length; i++) {
						INode var = currentTable.getVariableAt(i);
						int indexInJointTable = jointTable.getVariableIndex((Node) var);
						if (jointCoord[indexInJointTable] != coord[i]) {
							found = false;
							break;
						}
					}
					
					if (found) {
						if (found1stP) {
							printer.print(" +");
						}
						printer.print(" p[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				

				printer.print(" > ");
				if (!isStrictlyGreater) {
					printer.print("= ");
				}
				
				printer.print(value);
				
				printer.println(";");
				
			}
			
			
		}
		
		
		return output.toString();
	}
	
	
	/**
	 * @see #printAll(String[], String[], String, int[][][], int[][][], int[][][], int[][][], PrintStream)
	 */
	public void printAll(String[] indicatorNames, String[] detectorNames, String threatName,
			int[][][] indicatorCorrelations, int[][][] detectorCorrelations,
			int[][][] threatIndicatorMatrix, int[][][] detectorIndicatorMatrix){
		this.printAll(indicatorNames, detectorNames, threatName, indicatorCorrelations, detectorCorrelations, threatIndicatorMatrix, detectorIndicatorMatrix, null);
	}
	
	/**
	 * 
	 * @param indicatorNames
	 * @param detectorNames
	 * @param threatName
	 * @param indicatorCorrelations
	 * @param detectorCorrelations
	 * @param threatIndicatorMatrix
	 * @param detectorIndicatorMatrix
	 * @param printer
	 */
	public void printAll(String[] indicatorNames, String[] detectorNames, String threatName,
			int[][][] indicatorCorrelations, int[][][] detectorCorrelations,
			int[][][] threatIndicatorMatrix, int[][][] detectorIndicatorMatrix ,
			PrintStream printer){

		if (printer == null) {
			printer = System.out;
		}
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		
		List<String> indicatorNameList = this.getNameList(indicatorNames);
		List<String> detectorNameList = this.getNameList(detectorNames);
		
		List<String> allVariableList = new ArrayList<String>();
		allVariableList.add(threatName);
		allVariableList.addAll(indicatorNameList);
		allVariableList.addAll(detectorNameList);
		
		PotentialTable jointTable = this.getJointTable(variableMap, allVariableList );
		
		List<PotentialTable> unweightedTables = this.getCorrelationTables(variableMap , indicatorCorrelations, indicatorNameList);
		unweightedTables.addAll(this.getCorrelationTables(variableMap , detectorCorrelations, detectorNameList));
		
		List<PotentialTable> weightedTables = this.getThreatTables(variableMap , threatIndicatorMatrix, indicatorNameList, threatName);
		weightedTables.addAll(this.getDetectorTables(variableMap , detectorIndicatorMatrix, indicatorNameList, detectorNameList));
		
		printer.println(this.getObjFunction(unweightedTables, weightedTables, jointTable));
		
		
		printer.println();
		printer.println();
		printer.println("Subject to: ");
		printer.println();
		
		printer.println(this.getNonZeroRestrictions(unweightedTables, weightedTables, jointTable, isStrictlyGreaterThan, greaterThanValue));
		
		
		printer.println();
		printer.println();
		printer.println("Joint probability table:");
		printer.println(this.getJointTableDescription(jointTable));
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new ObjFunctionPrinter().printAll(indicatorNames, detectorNames, threatName, indicatorCorrelations, detectorCorrelations, threatIndicatorMatrix, detectorIndicatorMatrix);
		
	}



	/**
	 * 
	 * @param indicatorNames
	 * @return
	 */
	public List<String> getNameList(String[] names)  {
		List<String> ret = new ArrayList<String>();
		
		for (String name : names) {
			ret.add(name);
		}
		return ret;
	}
	
	/**
	 * 
	 * @param n
	 * @return
	 */
	public int factorial(int n) {
		int ret = 1;
		for (int i = 2; i <= n; i++) {
			ret *= i;
		}
		return ret;
	}
	
	/**
	 * 
	 * @param n
	 * @param k
	 * @return
	 */
	public int combinatorial (int n,int k) {
		return (factorial(n)/factorial(k))/factorial(n-k);
	}

	/**
	 * @return the isToBreakLineOnObjectFunction
	 */
	public boolean isToBreakLineOnObjectFunction() {
		return isToBreakLineOnObjectFunction;
	}

	/**
	 * @param isToBreakLineOnObjectFunction the isToBreakLineOnObjectFunction to set
	 */
	public void setToBreakLineOnObjectFunction(boolean isToBreakLineOnObjectFunction) {
		this.isToBreakLineOnObjectFunction = isToBreakLineOnObjectFunction;
	}

	/**
	 * @return the isToSubtract1WayLikelihood
	 */
	public boolean isToSubtract1WayLikelihood() {
		return isToSubtract1WayLikelihood;
	}

	/**
	 * @param isToSubtract1WayLikelihood the isToSubtract1WayLikelihood to set
	 */
	public void setToSubtract1WayLikelihood(boolean isToSubtract1WayLikelihood) {
		this.isToSubtract1WayLikelihood = isToSubtract1WayLikelihood;
	}
	

}
