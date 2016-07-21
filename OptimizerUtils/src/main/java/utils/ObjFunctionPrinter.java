package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.io.DneIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.Debug;


/**
 * This is just a test program (non-JUnit) that shows how we can use {@link PotentialTable}
 * in order to generate an objective function and constraints for non-linear optimizers.
 * The output of this class can be used as an input for a third-party non-linear optimizer.
 */
public class ObjFunctionPrinter {
	
	private String probabilityVariablePrefix = "p";
	
	private String problemID = "RCP1";

	private static float greaterThanValue = 0;
	
	private boolean isToPrintJointProbabilityDescription = true;
	
	private boolean isToBreakLineOnObjectFunction = true;

	private boolean isToSubtract1WayLikelihood = true;
	
	private boolean isToConsiderDetectors = true;
	
	private String auxiliaryTableDirectoryName = null;
	private String primaryTableDirectoryName = null;
	
	
	private String threatName = "Threat";
	
	public static final String[] DEFAULT_INDICATOR_NAMES1 = {"I1", "I2", "I3", "I4", "I5"}; //RCP1
	public static final String[] DEFAULT_INDICATOR_NAMES2 = {"I1", "I2", "I3", "I4", "I5", "I6"}; //RCP2
	public static final String[] DEFAULT_INDICATOR_NAMES3 = {"I1", "I2", "I3", "I4"}; //RCP3
	
	
	public static final  String[] DEFAULT_DETECTOR_NAMES1 = {"D1", "D2", "D3", "D4", "D5"};	//RCP1
	public static final  String[] DEFAULT_DETECTOR_NAMES2 = {"D1", "D2", "D3", "D4", "D5", "D6"};	//RCP2
	public static final  String[] DEFAULT_DETECTOR_NAMES3 = {"D1", "D2", "D3", "D4"};	//RCP3
	
	
	
	private String primaryTableWeightSymbol = "w1";
	private String auxiliaryTableWeightSymbol = "w2";
	
	private Map<String, String> primaryTableSpecialCasesWeightSymbol = new HashMap<String, String>();
	{
		primaryTableSpecialCasesWeightSymbol.put("I6", "w3");
	}
	

	private static boolean isStrictlyGreaterThan = true;

	
	private Integer[] jointProbsIndexesToConsider = null;
//	private Integer[] jointProbsIndexesToConsider = {
//		2047,
//		1915,
//		2014,
//		1783,
//		1981,
//		990,
//		891,
//		957,
//		693,
//		759,
//		627,
//		825,
//		858,
//		726,
//		827,
//		695,
//		924,
//		926,
//	};


	private String[] indicatorNames = DEFAULT_INDICATOR_NAMES1;
	private String[] detectorNames = DEFAULT_DETECTOR_NAMES1;
	
	public static int[][][] indicatorCorrelations = {};
	public static int[][][] detectorCorrelations = {};
	
	public static int[][][] threatIndicatorMatrix = {};
	public static int[][][] detectorIndicatorMatrix = {};
	
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
		
		if (matrix != null) {
			for (int n=0,i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix.length; j++,n++) {
					table.setValue(n, matrix[i][j]);
				}
			}
		} else {
			// just fill everything with zeros
			for (int i = 0; i < table.tableSize(); i++) {
				table.setValue(i, 0);
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
		
		if (correlations != null 
				&& combinatorial(names.size(), 2) != correlations.length) {
			throw new IllegalArgumentException("Combinations of indicatorNames is " + combinatorial(names.size(), 2) 
					+ ", but correlation matrix had size " + correlations.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int n=0,i=0; i < names.size()-1; i++) {
			String rowName = names.get(i);
			for (int j = i+1; j < names.size(); j++,n++) {
				int[][] matrix = null;
				if (correlations != null) {
					matrix = correlations[n];
				}
				String columnName = names.get(j);
				PotentialTable table = this.getPotentialTable(variableMap, matrix, columnName, rowName);
				ret.add(table);
			}
		}
		return ret;
	}
	
	/**
	 * @param variableMap : this is used to reuse node variables by name. If empty, nothing will be returned.
	 * Any variable not contained in this map will be considered as invalid input.
	 * If negative numbers is found in tables, it will be ignored.
	 * @param file : it may be either a network file (.net, .xml, .dne), or a directory containing network files.
	 * @return : non empty list of tables
	 * @throws IOException 
	 */
	public List<PotentialTable> getTablesFromNetFile( Map<String, INode> variableMap, File file) throws IOException {
		if (variableMap == null || variableMap.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		if (file == null || !file.exists()) {
			return Collections.EMPTY_LIST;
		}
		
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>();
		
		if (file.isDirectory()) {
			// read files in directory
			for (File input : file.listFiles()) {
				// recursively read files
				ret.addAll(this.getTablesFromNetFile(variableMap, input));
			}
		} else {
			// this is a single file

			// prepare an I/O class which delegates to proper I/O class accordingly to file extension
			FileExtensionIODelegator fileReader = FileExtensionIODelegator.newInstance();
			fileReader.getDelegators().add(new NetIO());
			fileReader.getDelegators().add(new XMLBIFIO());
			fileReader.getDelegators().add(new DneIO());
			
			// load the network file
			Graph graph = fileReader.load(file);
			
			for (Node node : graph.getNodes()) {
				PotentialTable inputTable = null;
				if (node instanceof ProbabilisticNode) {
					inputTable = ((ProbabilisticNode) node).getProbabilityFunction();
				} else if (node instanceof UtilityNode) {
					inputTable = ((UtilityNode) node).getProbabilityFunction();
				}
				
				// fill output table
				PotentialTable outputTable = new ProbabilisticTable();
				
				// fill variables, but use nodes in variableMap
				for (int varIndex = 0; varIndex < inputTable.getVariablesSize(); varIndex++) {
					// get node with same name from map
					INode mappedNode = variableMap.get(inputTable.getVariableAt(varIndex).getName());
					// checking existence
					if (mappedNode == null) {
						throw new IOException(inputTable.getVariableAt(varIndex).getName() + " is an unknown variable.");
					}
					// check number of states
					if (mappedNode.getStatesSize() != inputTable.getVariableAt(varIndex).getStatesSize()) {
						throw new IOException("Expected size of " + mappedNode.getName() + " is " + mappedNode.getStatesSize() 
								+ ", but was " + inputTable.getVariableAt(varIndex).getStatesSize());
					}
					// check that i-th state of mapped node is equal to i-th state of node in file
					for (int stateIndex = 0; stateIndex < mappedNode.getStatesSize(); stateIndex++) {
						if (mappedNode.getStateAt(stateIndex).equalsIgnoreCase("true")
								|| mappedNode.getStateAt(stateIndex).equalsIgnoreCase("yes")
								|| mappedNode.getStateAt(stateIndex).equalsIgnoreCase("1")) {
							// check if respective state in file is also true/yes/1
							if (!(inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("true")
									|| inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("yes")
									|| inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("1"))) {
								throw new IOException("State " + stateIndex + " of " + mappedNode.getName() + " is expected to be " + mappedNode.getStateAt(stateIndex)
										+ ", but found " + inputTable.getVariableAt(varIndex).getStateAt(stateIndex));
							}
						} else if (mappedNode.getStateAt(stateIndex).equalsIgnoreCase("false")
								|| mappedNode.getStateAt(stateIndex).equalsIgnoreCase("no")
								|| mappedNode.getStateAt(stateIndex).equalsIgnoreCase("0")) {
							// check if respective state in file is also false/no/0
							if (!(inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("false")
									|| inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("no")
									|| inputTable.getVariableAt(varIndex).getStateAt(stateIndex).equalsIgnoreCase("0"))) {
								throw new IOException("State " + stateIndex + " of " + mappedNode.getName() + " is expected to be " + mappedNode.getStateAt(stateIndex)
										+ ", but found " + inputTable.getVariableAt(varIndex).getStateAt(stateIndex));
							}
						}
					}
					
					// if everything is file, add variable to new table
					outputTable.addVariable(mappedNode);
				}
				
				// check consistency of table size
				if (inputTable.tableSize() != outputTable.tableSize()) {
					throw new RuntimeException("Table sizes are diverging... Input = " + inputTable.tableSize() + "; output = " + outputTable.tableSize());
				}
				
				// fill values, but check if there is invalid values
				boolean hasInvalid = false;
				// at this point, table sizes are supposedly equal
				for (int i = 0; i < inputTable.tableSize(); i++) {
					int value = (int) inputTable.getValue(i);
					if (value < 0) {
						hasInvalid = true;
						break;
					}
					outputTable.setValue(i, value);
				}
				
				if (!hasInvalid) {
					ret.add(outputTable);
				}
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
		
		if (tables != null
				&& tables.length != indicatorNames.size()) {
			throw new IllegalArgumentException("Number of tables of threat is expected to be: " + indicatorNames.size() + ", but was " + tables.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int i=0; i < indicatorNames.size(); i++) {
			int[][] matrix = null;
			if (tables != null) {
				matrix = tables[i];
			}
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
	public List<PotentialTable> getDetectorTables(Map<String, INode> variableMap, int[][][] tables,
			List<String> indicatorNameList, List<String> detectorNameList) {
		if (detectorNameList == null || detectorNameList.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		if (indicatorNameList.size() != detectorNameList.size()) {
			throw new IllegalArgumentException("List of detectors and indicators must be of same size: " + detectorNameList + " ; " + indicatorNameList);
		}
		
		if (tables != null
				&& tables.length != detectorNameList.size()) {
			throw new IllegalArgumentException("Number of tables of detector frequencies is expected to be: " + detectorNameList.size() + ", but was " + tables.length);
		}
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(); 
		
		for (int i=0; i < detectorNameList.size(); i++) {
			int[][] matrix = null;
			if (tables != null) {
				matrix = tables[i];
			}
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
	    
	    Collection<Integer> jointProbsToIgnore = (getJointProbsIndexesToIgnore(jointTable));
	    
	    // print TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	TRUE,	p[1]
	    for (int rowIndex = 0; rowIndex < jointTable.tableSize(); rowIndex++) {
			if (jointProbsToIgnore.contains(rowIndex)) {
				continue;
			}
			int[] coord = jointTable.getMultidimensionalCoord(rowIndex);
	    	for (int varIndex = coord.length-1; varIndex >= 0; varIndex--) {
				printer.print(jointTable.getVariableAt(varIndex).getStateAt(coord[varIndex]) + ",");
			}
	    	printer.println(getProbabilityVariablePrefix() + "[" + (rowIndex+1) + "]");
		}
		
		return output.toString();
	}
	
	/**
	 * This will return a string like the following
	 * <pre>
	 * objFun : w1 * ( 3 * log( p[1] + p[2] +,...,p[k] ) + 9 * log(p[3] + ... + p[l]) ) + w2 * ( 271 * log( p[1] + p[2] +,...,p[m] ) + ...
	 * </pre>
	 * @param primaryTables
	 * @param auxiliaryTables
	 */
	public String getObjFunction(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
		printer.print("objFun :");
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		Collection<Integer> jointProbsToIgnore = (getJointProbsIndexesToIgnore(jointTable));
		
		
		// auxiliaryTables
		for (int tableIndex = 0; tableIndex < auxiliaryTables.size(); tableIndex++) {
//			printer.print(" " + getAuxiliaryTableWeightSymbol() + "[" + (tableIndex+1) + "] * ( ");
			
			boolean foundLogFactor = false;
//			printer.print(" " + getAuxiliaryTableWeightSymbol() + " * ( ");
			String tempString = " " + getAuxiliaryTableWeightSymbol() + " * ( ";
			PotentialTable currentTable = auxiliaryTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
//				printer.print(((int)currentTable.getValue(cellIndex)) + " * log(");
				String tempStringLog = ((int)currentTable.getValue(cellIndex)) + " * log(";
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (Integer jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					if (jointProbsToIgnore.contains(jointCellIndex)) {
						continue;
					}
					
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
							tempStringLog += " +";
						}
						tempStringLog += " " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]";
						found1stP = true;
					}
					
				}
				
//				printer.print(" )");
				tempStringLog += " )";
				
				if (cellIndex + 1 < currentTable.tableSize()) {
//					printer.print(" + ");
					tempStringLog += " + ";
				}
				
				if (found1stP) {
					foundLogFactor = true;
					tempString += tempStringLog;
				}
			}
			
			tempString += " )";
			
			if (isToBreakLineOnObjectFunction()) {
				tempString += "\n";
			}
			
			if (tableIndex + 1 < auxiliaryTables.size()) {
				tempString += " +";
			}
			
			if (foundLogFactor) {
				printer.print(tempString);
			}
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
//		printer.print(" + ");
		
		// primaryTables
		for (int tableIndex = 0; tableIndex < primaryTables.size(); tableIndex++) {
			PotentialTable currentTable = primaryTables.get(tableIndex);
			String tempString = "";
			
			// check if we can find variables to be treated in special way
			List<String> variablesFound = new ArrayList<String>();
			for (int i = 0; i < currentTable.getVariablesSize(); i++) {
				if (getPrimaryTableSpecialCasesWeightSymbol().containsKey(currentTable.getVariableAt(i).getName())) {
					variablesFound.add(currentTable.getVariableAt(i).getName());
				}
			}
			if (variablesFound.isEmpty()) {
				tempString += " + " + getPrimaryTableWeightSymbol() + " * ( ";
			} else {
				tempString += " + ";
				if (variablesFound.size() > 1) {
					tempString += "( ";
				}
				for (int i = 0; i < variablesFound.size(); i++) {
					tempString += getPrimaryTableSpecialCasesWeightSymbol().get(variablesFound.get(i));
					if (i+1 < variablesFound.size()) {
						tempString += " * ";
					}
				}
				if (variablesFound.size() > 1) {
					tempString += " )";
				}
				tempString += " * ( ";
			}
			boolean foundLogFactor = false;
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
//				printer.print(" + " + ((int)currentTable.getValue(cellIndex)) + " * log(");
				
				String tempStringLog = ((int)currentTable.getValue(cellIndex)) + " * log(";
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					if (jointProbsToIgnore.contains(jointCellIndex)) {
						continue;
					}
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
							tempStringLog += " +";
						}
						tempStringLog += " " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]";
						found1stP = true;
					}
					
				}
				
				tempStringLog += " )";
				
				if (cellIndex + 1 < currentTable.tableSize()) {
					tempStringLog += (" + ");
				}
				if (found1stP) {
					tempString += tempStringLog;
					foundLogFactor = true;
				}
				
			}
			
			tempString += " )";
			
			if (isToBreakLineOnObjectFunction()) {
				tempString += "\n";
			}
			
//			if (tableIndex + 1 < primaryTables.size()) {
//				printer.print(" + ");
//			}
			
			if (foundLogFactor) {
				printer.print(tempString);
			}
			
			
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		// make sure we only count the 1-way log likelihood once
		if (isToSubtract1WayLikelihood()) {
			printer.print(get1WayLikelihoodSubtraction(primaryTables, auxiliaryTables, jointTable));
		}
		
		printer.println(";");
		
		return output.toString();
		
	}
	
	/**
	 * 
	 * @param primaryTables : correlation table
	 * @param auxiliaryTables : table from naive bayes distribution
	 * @param jointTable : used just in order to treat the indexes of cells of a table containing all variables
	 * @return
	 */
	public String get1WayLikelihoodSubtraction(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(output);
		
		
	    
	    // count number of occurrences of variables
	    Map<String, Integer> auxiliaryTableCounter = new HashMap<String, Integer>();
	    for (PotentialTable table : auxiliaryTables) {
			for (int i = 0; i < table.getVariablesSize(); i++) {
				INode var = table.getVariableAt(i);
				Integer count = auxiliaryTableCounter.get(var.getName());
				if (count == null) {
					count = 0;
				}
				count++;
				auxiliaryTableCounter.put(var.getName(), count);
			}
		}
	    Map<String, Integer> primaryTableCounter = new HashMap<String, Integer>();
	    for (PotentialTable table : primaryTables) {
	    	for (int i = 0; i < table.getVariablesSize(); i++) {
	    		INode var = table.getVariableAt(i);
	    		Integer count = primaryTableCounter.get(var.getName());
	    		if (count == null) {
	    			count = 0;
	    		}
	    		count++;
	    		primaryTableCounter.put(var.getName(), count);
	    	}
	    }
	    
//	    Map<String, INode> jointTableVariableMap = new HashMap<String, INode>();
//	    for (int i = 0; i < jointTable.getVariablesSize(); i++) {
//			jointTableVariableMap.put(jointTable.getVariableAt(i).getName(), jointTable.getVariableAt(i));
//		}
	    

		// check marginal consistency between tables in same category and get marginal counts (frequency) for each
	    Map<String, int[]> auxiliaryMarginals = this.getMarginalCounts(auxiliaryTables);
	    Map<String, int[]> primaryMarginals = this.getMarginalCounts(primaryTables);
	    
	    Collection<Integer> jointProbsToIgnore = (getJointProbsIndexesToIgnore(jointTable));
		
	    // treat auxiliary marginals
	    for (int varIndex = jointTable.getVariablesSize()-1; varIndex >= 0; varIndex--) {
			
	    	INode var = jointTable.getVariableAt(varIndex);
	    	String varName = var.getName();
	    	
	    	if (!auxiliaryMarginals.containsKey(varName) || !auxiliaryTableCounter.containsKey(varName)) {
	    		continue;	// ignore marginals that are not present in auxiliary table
	    	}
	    	
	    	int[] marginal = auxiliaryMarginals.get(varName);
	    	Integer numToRemove = auxiliaryTableCounter.get(varName);
			if (!primaryTableCounter.containsKey(varName) || primaryTableCounter.get(varName).intValue() == 0) {
				// if there is no occurrences in the other table, we need to consider 1 less marginals
				numToRemove--;
			}
			
			String tempString = (" - " + getAuxiliaryTableWeightSymbol() + " * " + numToRemove + " * (");
			boolean isToPrintWeightFactor = false;
			
			for (int stateIndex = 0; stateIndex < marginal.length; stateIndex++) {
				String tempStringLog = (marginal[stateIndex] + " * log(");
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					if (jointProbsToIgnore.contains(jointCellIndex)) {
						continue;
					}
					int indexInJointTable = jointTable.getVariableIndex((Node) var);
					int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
					
					if (jointCoord[indexInJointTable] == stateIndex) {
						if (found1stP) {
							tempStringLog += (" +");
						}
						tempStringLog += (" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				tempStringLog += (")");
				if (stateIndex + 1  < marginal.length) {
					tempStringLog += (" + ");
				}
				
				if (found1stP) {
					isToPrintWeightFactor = true;
					tempString += tempStringLog;
				}
			}
			
			tempString += (") ");

			if (isToBreakLineOnObjectFunction()) {
				tempString += "\n";
			}
			
			if (isToPrintWeightFactor) {
				printer.print(tempString);
			}
		}
	    
		
	    if (isToBreakLineOnObjectFunction()) {
	    	printer.println();
	    }
	    
		// for primary marginals, 
	    for (int varIndex = jointTable.getVariablesSize()-1; varIndex >= 0; varIndex--) {
	    	
	    	INode var = jointTable.getVariableAt(varIndex);
	    	String varName = var.getName();
	    	
	    	if (!primaryMarginals.containsKey(varName) || !primaryTableCounter.containsKey(varName)) {
	    		continue;	// ignore marginals that are not present in primary table
	    	}
	    	
	    	int[] marginal = primaryMarginals.get(varName);
	    	Integer numToRemove = primaryTableCounter.get(varName)-1;	// always remove 1-way log-likelihood except 1 entry
	    	
	    	String tempString = (" - " + getPrimaryTableWeightSymbol() + " * " + numToRemove + " * (");
	    	boolean isToPrintFactor = false;
	    	
	    	for (int stateIndex = 0; stateIndex < marginal.length; stateIndex++) {
	    		String tempStringLog = (marginal[stateIndex] + " * log(");
	    		
	    		boolean found1stP = false;
	    		for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
	    			if (jointProbsToIgnore.contains(jointCellIndex)) {
	    				continue;
	    			}
	    			int indexInJointTable = jointTable.getVariableIndex((Node) var);
	    			int[] jointCoord = jointTable.getMultidimensionalCoord(jointCellIndex);
	    			
	    			if (jointCoord[indexInJointTable] == stateIndex) {
	    				if (found1stP) {
	    					tempStringLog += (" +");
	    				}
	    				tempStringLog += ( " " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
	    				found1stP = true;
	    			}
	    			
	    		}
	    		
	    		tempStringLog += (")");
	    		if (stateIndex + 1  < marginal.length) {
	    			tempStringLog += (" + ");
	    		}
	    		
	    		if (found1stP) {
	    			isToPrintFactor = true;
					tempString += tempStringLog;
	    		}
	    	}
	    	
	    	tempString += (") ");
	    	
	    	if (isToBreakLineOnObjectFunction()) {
	    		tempString += "\n";
	    	}
	    	
	    	if (isToPrintFactor) {
	    		printer.print(tempString);
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
	    
	    Collection<Integer> jointProbsToIgnore = (getJointProbsIndexesToIgnore(jointTable));
		
		// threatIndicatorMatrix
		for (int tableIndex = 0; tableIndex < threatTables.size(); tableIndex++) {
			PotentialTable currentTable = threatTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					if (jointProbsToIgnore.contains(jointCellIndex)) {
						continue;
					}
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
						printer.print(" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				if (found1stP) {
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
			
		}
		
		printer.println();
		
		
		// indicatorCorrelations
		for (int tableIndex = 0; tableIndex < correlationTables.size(); tableIndex++) {
			PotentialTable currentTable = correlationTables.get(tableIndex);
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
				
				
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				boolean found1stP = false;
				for (int jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					if (jointProbsToIgnore.contains(jointCellIndex)) {
						continue;
					}
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
						printer.print(" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						found1stP = true;
					}
					
				}
				
				if (found1stP) {
					printer.print(" > ");
					if (!isStrictlyGreater) {
						printer.print("= ");
					}
					
					printer.print(value);
					
					printer.println(";");
				}
				
			}
			
			
		}
		
		
		return output.toString();
	}
	
	
	/**
	 * @throws FileNotFoundException 
	 * @see #printAll(String[], String[], String, int[][][], int[][][], int[][][], int[][][], PrintStream)
	 */
	public void printAll(
			int[][][] indicatorCorrelations, int[][][] detectorCorrelations,
			int[][][] threatIndicatorMatrix, int[][][] detectorIndicatorMatrix) throws IOException{
		this.printAll(indicatorCorrelations, detectorCorrelations, threatIndicatorMatrix, detectorIndicatorMatrix, null);
	}
	
	/**
	 * 
	 * @param indicatorNames
	 * @param detectorNames
	 * @param threatName
	 * @param indicatorCorrelationsMatrix
	 * @param detectorCorrelationsMatrix
	 * @param threatIndicatorTableMatrix
	 * @param detectorIndicatorTableMatrix
	 * @param printer
	 * @throws FileNotFoundException 
	 */
	public void printAll(int[][][] indicatorCorrelationsMatrix, int[][][] detectorCorrelationsMatrix,
			int[][][] threatIndicatorTableMatrix, int[][][] detectorIndicatorTableMatrix ,
			PrintStream printer) throws IOException{

		if (printer == null) {
			printer = System.out;
		}
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		
		List<String> indicatorNameList = this.getNameList(getIndicatorNames());
		List<String> detectorNameList = this.getNameList(getDetectorNames());
		
		List<String> allVariableList = new ArrayList<String>();
		allVariableList.add(getThreatName());
		allVariableList.addAll(indicatorNameList);
		allVariableList.addAll(detectorNameList);
		
		PotentialTable jointTable = this.getJointTable(variableMap, allVariableList );
		
		List<PotentialTable> primaryTables = null;
		if (getPrimaryTableDirectoryName() != null &&  !getPrimaryTableDirectoryName().trim().isEmpty()) {
			primaryTables = this.getTablesFromNetFile(variableMap , new File(getPrimaryTableDirectoryName()));
		} else {
			primaryTables = this.getCorrelationTables(variableMap , indicatorCorrelationsMatrix, indicatorNameList);
			primaryTables.addAll(this.getCorrelationTables(variableMap , detectorCorrelationsMatrix, detectorNameList));
		}
		
		
		List<PotentialTable> auxiliaryTables = null;
		if (getAuxiliaryTableDirectoryName() != null &&  !getAuxiliaryTableDirectoryName().trim().isEmpty()) {
			auxiliaryTables = this.getTablesFromNetFile(variableMap , new File(getAuxiliaryTableDirectoryName()));
		} else {
			auxiliaryTables = this.getThreatTables(variableMap , threatIndicatorTableMatrix, indicatorNameList, getThreatName());
			auxiliaryTables.addAll(this.getDetectorTables(variableMap , detectorIndicatorTableMatrix, indicatorNameList, detectorNameList));
		}
		
		
		
		printer.println(this.getObjFunction(primaryTables, auxiliaryTables, jointTable));
		
		
		printer.println();
		printer.println();
		printer.println("Subject to: ");
		printer.println();
		
		printer.println(this.getNonZeroRestrictions(primaryTables, auxiliaryTables, jointTable, isStrictlyGreaterThan, greaterThanValue));
		
		if (isToPrintJointProbabilityDescription()) {
			printer.println();
			printer.println();
			printer.println("Joint probability table:");
			printer.println(this.getJointTableDescription(jointTable));
		}
		
	}
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("id","problem-id", true, "Name or identification of the current problem (this will be used as suffixes of output file names).");
		options.addOption("i","indicator-only", false, "Use indicator tables, and do not use detectors.");
		options.addOption("out","output", true, "Output file.");
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("primary","primary-tables", true, "Where to read primary tables from.");
		options.addOption("aux","auxiliary-tables", true, "Where to read primary tables from.");
		options.addOption("inames","indicator-names", true, "Comma-separated names of indicators.");
		options.addOption("dnames","detector-names", true, "Comma-separated names of detectors.");
		options.addOption("threat","threat-name", true, "Name of threat variable.");
		options.addOption("h","help", false, "Help.");
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		
		if (cmd == null) {
			System.err.println("Invalid command line");
			return;
		}
		
		if (cmd.hasOption("h")) {
			System.out.println("-id <SOME NAME> : Name or identification of the current problem (e.g. \"RCP1\", \"RCP2\", or \"RCP3\").");
			System.out.println("-out <SOME NAME> : file to print output.");
			System.out.println("-i : Use indicator tables, and do not use detectors.");
			System.out.println("-d : Enables debug mode.");
			System.out.println("-h: Help.");
			System.out.println("-primary : where to read primary tables from.");
			System.out.println("-aux : where to read auxiliary tables from.");
			System.out.println("-inames : Comma-separated names of indicators.");
			System.out.println("-dnames : Comma-separated names of detectors.");
			System.out.println("-threat : Name of threat variable.");
			return;
		}
		
		if (cmd.hasOption("d")) {
			Debug.setDebug(true);
		} else {
			Debug.setDebug(false);
		}
		
		ObjFunctionPrinter printer = new ObjFunctionPrinter();
		
//		if (cmd.hasOption("data")) {
//			printer.setToPrintDataOnly(true);
//		}
		
		printer.setToConsiderDetectors(!cmd.hasOption("i"));
		
		if (cmd.hasOption("id")) {
			printer.setProblemID(cmd.getOptionValue("id"));
		}
		
		PrintStream ps = System.out;
		if (cmd.hasOption("out")) {
			try {
				ps = new PrintStream(new File(cmd.getOptionValue("out")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				ps = System.out;
			}
		}
		
		if (cmd.hasOption("primary")) {
			printer.setPrimaryTableDirectoryName(cmd.getOptionValue("primary"));
		}
		if (cmd.hasOption("aux")) {
			printer.setAuxiliaryTableDirectoryName(cmd.getOptionValue("aux"));
		}
		
		if (cmd.hasOption("inames")) {
			printer.setIndicatorNames(cmd.getOptionValue("inames").split("[,:]"));
		}
		if (cmd.hasOption("dnames")) {
			printer.setDetectorNames(cmd.getOptionValue("dnames").split("[,:]"));
		}
		
		if (cmd.hasOption("threat")) {
			printer.setThreatName(cmd.getOptionValue("threat"));
		}
		
		try {
			printer.printAll(indicatorCorrelations, detectorCorrelations, threatIndicatorMatrix, detectorIndicatorMatrix, 
					ps);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}




	/**
	 * 
	 * @param indicatorNames
	 * @return
	 */
	public List<String> getNameList(String[] names)  {
		List<String> ret = new ArrayList<String>();
		
		for (String name : names) {
			if (name.trim().isEmpty()) {
				continue;	//ignore blank names
			}
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

	/**
	 * @return the complement of {@link #getJointProbsIndexesToConsider()}
	 */
	public Collection<Integer> getJointProbsIndexesToIgnore(PotentialTable jointTable) {
		if (jointTable == null || jointTable.getVariablesSize() <= 0) {
			return Collections.EMPTY_LIST;
		}
		Integer[] toConsider = getJointProbsIndexesToConsider();
		if (toConsider == null || toConsider.length <= 0) {
			return Collections.EMPTY_LIST;
		}
		List<Integer> jointProbsToConsider = Arrays.asList(toConsider);
		Set<Integer> ret = new HashSet<Integer>();
		for (Integer i = 0; i < jointTable.tableSize(); i++) {
			if (!jointProbsToConsider.contains(i)) {
				ret.add(i);
			}
		}
		
		if (ret.size() + jointProbsToConsider.size() != jointTable.tableSize()) {
			throw new RuntimeException("Size of joint prob indexes to ignore is expected to be " 
					+ (jointTable.tableSize() - jointProbsToConsider.size()) + ", but was " + ret.size());
		}
		
		return ret;
	}


	/**
	 * @return the jointProbsIndexesToConsider
	 * @see #getJointProbsToIgnore()
	 */
	public Integer[] getJointProbsIndexesToConsider() {
		return jointProbsIndexesToConsider;
	}

	/**
	 * @param jointProbsIndexesToConsider the jointProbsIndexesToConsider to set
	 * @see #getJointProbsToIgnore()
	 */
	public void setJointProbsIndexesToConsider(Integer[] jointProbsToConsider) {
		this.jointProbsIndexesToConsider = jointProbsToConsider;
	}

	/**
	 * @return the isToPrintJointProbabilityDescription
	 */
	public boolean isToPrintJointProbabilityDescription() {
		return isToPrintJointProbabilityDescription;
	}

	/**
	 * @param isToPrintJointProbabilityDescription the isToPrintJointProbabilityDescription to set
	 */
	public void setToPrintJointProbabilityDescription(
			boolean isToPrintJointProbabilitySpecification) {
		this.isToPrintJointProbabilityDescription = isToPrintJointProbabilitySpecification;
	}

	/**
	 * @return the auxiliaryTableWeightSymbol
	 */
	public String getAuxiliaryTableWeightSymbol() {
		return auxiliaryTableWeightSymbol;
	}

	/**
	 * @param auxiliaryTableWeightSymbol the auxiliaryTableWeightSymbol to set
	 */
	public void setAuxiliaryTableWeightSymbol(
			String auxiliaryTableWeightSymbol) {
		this.auxiliaryTableWeightSymbol = auxiliaryTableWeightSymbol;
	}

	/**
	 * @return the primaryTableWeightSymbol
	 */
	public String getPrimaryTableWeightSymbol() {
		return primaryTableWeightSymbol;
	}

	/**
	 * @param primaryTableWeightSymbol the primaryTableWeightSymbol to set
	 */
	public void setPrimaryTableWeightSymbol(
			String primaryTableWeightSymbol) {
		this.primaryTableWeightSymbol = primaryTableWeightSymbol;
	}
	

	/**
	 * @return the problemID
	 */
	public String getProblemID() {
		return problemID;
	}

	

	/**
	 * @param problemID the problemID to set
	 */
	public void setProblemID(String problemID) {
		this.problemID = problemID;
		
		String upperCase = problemID.toUpperCase();
		if (upperCase.contains("RCP2")) {
			Debug.println("Using variable sets of RCP2");
			this.setIndicatorNames(DEFAULT_INDICATOR_NAMES2);
			this.setDetectorNames(DEFAULT_DETECTOR_NAMES2);
		} else if (upperCase.contains("RCP3")) {
			Debug.println("Using variable sets of RCP3");
			this.setIndicatorNames(DEFAULT_INDICATOR_NAMES3);
			this.setDetectorNames(DEFAULT_DETECTOR_NAMES3);
		} else if (upperCase.contains("RCP1")) {
			Debug.println("Using variable sets of RCP1");
			this.setIndicatorNames(DEFAULT_INDICATOR_NAMES1);
			this.setDetectorNames(DEFAULT_DETECTOR_NAMES1);
		} else {
			Debug.println("Resetting RCP variable sets");
			this.indicatorCorrelations = null;
			this.detectorCorrelations = null;
			this.threatIndicatorMatrix = null;
			this.detectorIndicatorMatrix = null;
		}
		
		if (!isToConsiderDetectors()){
			this.detectorCorrelations = new int[0][0][0];
			this.detectorIndicatorMatrix = new int[0][0][0];
			this.setDetectorNames(new String[0]);
		}
	}

	/**
	 * @return the isToConsiderDetectors
	 */
	public boolean isToConsiderDetectors() {
		return isToConsiderDetectors;
	}

	/**
	 * @param isToConsiderDetectors the isToConsiderDetectors to set
	 */
	public void setToConsiderDetectors(boolean isToConsiderDetectors) {
		this.isToConsiderDetectors = isToConsiderDetectors;
		if (!isToConsiderDetectors) {
			this.detectorCorrelations = new int[0][0][0];
			this.detectorIndicatorMatrix = new int[0][0][0];
			this.setDetectorNames(new String[0]);
		}
	}

	/**
	 * @return the primaryTableSpecialCasesWeightSymbol
	 */
	public Map<String, String> getPrimaryTableSpecialCasesWeightSymbol() {
		return primaryTableSpecialCasesWeightSymbol;
	}

	/**
	 * @param primaryTableSpecialCasesWeightSymbol the primaryTableSpecialCasesWeightSymbol to set
	 */
	public void setPrimaryTableSpecialCasesWeightSymbol(
			Map<String, String> primaryTableSpecialCasesWeightSymbol) {
		this.primaryTableSpecialCasesWeightSymbol = primaryTableSpecialCasesWeightSymbol;
	}

	/**
	 * @return the auxiliaryTableDirectoryName
	 */
	public String getAuxiliaryTableDirectoryName() {
		return auxiliaryTableDirectoryName;
	}

	/**
	 * @param auxiliaryTableDirectoryName the auxiliaryTableDirectoryName to set
	 */
	public void setAuxiliaryTableDirectoryName(
			String auxiliaryTableDirectoryName) {
		this.auxiliaryTableDirectoryName = auxiliaryTableDirectoryName;
	}

	/**
	 * @return the primaryTableDirectoryName
	 */
	public String getPrimaryTableDirectoryName() {
		return primaryTableDirectoryName;
	}

	/**
	 * @param primaryTableDirectoryName the primaryTableDirectoryName to set
	 */
	public void setPrimaryTableDirectoryName(String primaryTableDirectoryName) {
		this.primaryTableDirectoryName = primaryTableDirectoryName;
	}

	/**
	 * @return the indicatorNames
	 */
	public String[] getIndicatorNames() {
		return indicatorNames;
	}

	/**
	 * @param indicatorNames the indicatorNames to set
	 */
	public void setIndicatorNames(String[] indicatorNames) {
		this.indicatorNames = indicatorNames;
	}

	/**
	 * @return the detectorNames
	 */
	public String[] getDetectorNames() {
		return detectorNames;
	}

	/**
	 * @param detectorNames the detectorNames to set
	 */
	public void setDetectorNames(String[] detectorNames) {
		this.detectorNames = detectorNames;
	}

	/**
	 * @return the threatName
	 */
	public String getThreatName() {
		return threatName;
	}

	/**
	 * @param threatName the threatName to set
	 */
	public void setThreatName(String threatName) {
		this.threatName = threatName;
	}

	/**
	 * @return the probabilityVariablePrefix
	 */
	public String getProbabilityVariablePrefix() {
		return probabilityVariablePrefix;
	}

	/**
	 * @param probabilityVariablePrefix the probabilityVariablePrefix to set
	 */
	public void setProbabilityVariablePrefix(String probabilityVariablePrefix) {
		this.probabilityVariablePrefix = probabilityVariablePrefix;
	}


	

}
