/**
 * 
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;

/**
 * This is an extension of {@link ObjFunctionPrinter} which instead of printing an
 * objective function for maximum log-likelihood, it prints an objective function for
 * euclidian distance.
 * @author Shou Matsumoto
 *
 */
public class EuclideanDistanceObjFunctionPrinter extends ObjFunctionPrinter {
	
	private boolean isToUseAuxiliaryTable = true;
	private boolean isToUsePrimaryTables = true;
	private String outputPath;
	private Collection<Integer> jointIndexesToIgnore = new HashSet<Integer>();
	
	public static final String DEFAULT_JOINT_INDEX_VARIABLE_NAME = "k";
	
	private String jointIndexVariableName = DEFAULT_JOINT_INDEX_VARIABLE_NAME;

	public EuclideanDistanceObjFunctionPrinter() {
		setIndicatorNames(null);
		setDetectorNames(null);
		setThreatName(null);
		setAlertName(null);
		this.setPrimaryTableWeightSymbol("w");		// for weights
		this.setAuxiliaryTableWeightSymbol("n");	// for counts
		this.setProbabilityVariablePrefix("x");		// for probabilities to be optimized
	}


	/* (non-Javadoc)
	 * @see utils.ObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][], java.io.PrintStream)
	 */
	public void printAll(int[][][] indicatorCorrelationsMatrix,
			int[][][] detectorCorrelationsMatrix,
			int[][][] threatIndicatorTableMatrix,
			int[][][] detectorIndicatorTableMatrix, PrintStream printer)
			throws IOException {

		PrintStream jointProbPrinter = printer;
		PrintStream weightPrinter = printer;
		
		if (printer == null) {
			File file = new File(getOutputPath());
			if (!file.exists()) {
				file.mkdirs();
			}
			if (file.isDirectory()) {
				printer = new PrintStream(new File(file,"objFun.txt"));
				jointProbPrinter = new PrintStream(new File(file,"meaning_prob.csv"));
				weightPrinter = new PrintStream(new File(file,"meaning_count_weight.csv"));
			} else if (file.isFile()) {
				printer = new PrintStream(file);
				jointProbPrinter = printer;
				weightPrinter = printer;
			} else {
				printer = System.out;
				jointProbPrinter = printer;
				weightPrinter = printer;
			}
		}
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		
		List<String> indicatorNameList = this.getNameList(getIndicatorNames());
		List<String> detectorNameList = this.getNameList(getDetectorNames());
		
		List<String> allVariableList = new ArrayList<String>();
		if (getThreatName() != null && !getThreatName().trim().isEmpty()) {
			allVariableList.add(getThreatName());
		}
		allVariableList.addAll(indicatorNameList);
		allVariableList.addAll(detectorNameList);
		if (getAlertName() != null && !getAlertName().trim().isEmpty()) {
			allVariableList.add(getAlertName());
			
		}
		
		PotentialTable jointTable = this.getJointTable(variableMap, allVariableList );
		fillJointStatesToIgnore(jointTable);
		
		// tables related to correlation. 
		// We won't use the values in cells (we'll just use the instance of PotentialTable to organize the order and names of variables/states)
		List<PotentialTable> primaryTables = new ArrayList<PotentialTable>();
		if (isToUsePrimaryTables()) {
			if (getPrimaryTableVarNames() != null && !getPrimaryTableVarNames().isEmpty()) {
				primaryTables.addAll(getEmptyPotentialTables(variableMap, getPrimaryTableVarNames()));
			} else {
				primaryTables.addAll(this.getCorrelationTables(variableMap , null, indicatorNameList));
				primaryTables.addAll(this.getCorrelationTables(variableMap , null, detectorNameList));
			}
		}
		
		// Similarly, tables related to Indicator X Detector. 
		// Again, we won't use the values in cells (the table is just for organizing variables and states)
		List<PotentialTable> auxiliaryTables = null;
		if (isToUseAuxiliaryTables()) {
			auxiliaryTables = new ArrayList<PotentialTable>();
			if (getAuxiliaryTableVarNames() != null && !getAuxiliaryTableVarNames().isEmpty()) {
				auxiliaryTables.addAll(getEmptyPotentialTables(variableMap, getAuxiliaryTableVarNames()));
			} else {
				auxiliaryTables.addAll(this.getThreatTables(variableMap, null, indicatorNameList, getThreatName()));
				auxiliaryTables.addAll(this.getDetectorTables(variableMap , null, indicatorNameList, detectorNameList));
			}
		}
		
		// actually print the objective function
		printer.println(this.getObjFunction(primaryTables, auxiliaryTables, jointTable));
		

		this.printJointIndexesToConsider(printer, jointTable, getJointIndexesToIgnore(), primaryTables, auxiliaryTables);

		// print the meaning of p variables
		if (isToPrintJointProbabilityDescription()) {
			printer.println();
//			printer.println();
//			printer.println("Joint probability table:");
			jointProbPrinter.println(this.getJointTableDescription(jointTable));
			if (jointProbPrinter != weightPrinter
					&& jointProbPrinter != printer) {
				jointProbPrinter.close();
			}
			
			// also print the meaning of count variables n[i] and w[i]
			printer.println();
//			printer.println("Counts:");
			weightPrinter.println(this.getCountTableDescription(primaryTables, auxiliaryTables));
			if (weightPrinter != printer) {
				weightPrinter.close();
			}
		}
		
	}

	/**
	 * This will fill the content of {@link #getJointIndexesToIgnore()}
	 * @param jointTable
	 * @see #getAlertName()
	 */
	public void fillJointStatesToIgnore(PotentialTable jointTable) {
		
		Collection<Integer> toIgnore = getJointIndexesToIgnore();
		if (toIgnore == null) {
			toIgnore = new HashSet<Integer>();
		}
		toIgnore.clear();
		
		if (getAlertName() == null || getAlertName().trim().isEmpty()) {
			return;
		}
		
		if (jointTable == null) {
			throw new IllegalArgumentException("Joint table is null.");
		}
		
		List<String> detectorNames = Collections.EMPTY_LIST;
		if (getDetectorNames() != null) {
			detectorNames = getNameList(getDetectorNames());
		}
		
		for (int jointIndex = 0; jointIndex < jointTable.tableSize(); jointIndex++) {
			
			int[] coord = jointTable.getMultidimensionalCoord(jointIndex);
			
			// count the number of active detectors in current coordinate
			int sumDetectors = 0;
			boolean isAlertTrue = false;
			for (int varIndex = 0; varIndex < coord.length; varIndex++) {
				// check if current var is a detector
				if (detectorNames.contains(jointTable.getVariableAt(varIndex).getName())) {  
					// check if state is ON
					String state = jointTable.getVariableAt(varIndex).getStateAt(coord[varIndex]);
					if (parseBoolean(state) == true) {
						sumDetectors++;
					}
				} 
				// check if current var is alert
				if (jointTable.getVariableAt(varIndex).getName().equals(getAlertName())) { 
					// check if state is ON
					String state = jointTable.getVariableAt(varIndex).getStateAt(coord[varIndex]);
					if (parseBoolean(state) == true) {
						isAlertTrue = true;
					}
				}
				
			}
			
			// alert should be true only if the number of active detectors is larger than a threshold
			if ( isAlertTrue != (sumDetectors >= getCountAlert()) ) {
				toIgnore.add(jointIndex);
			}
			
		}
		
		
		setJointIndexesToIgnore(toIgnore);
	}


	/**
	 * This will return a string like the following:
	 * <pre>
	 * Var1,	StateVar1,	Var2,	StateVar2,	n,	w
	 * I1,	TRUE,		I2,	TRUE,		n[1],	w[1]
	 * I1,	TRUE,		I2,	FALSE,		n[2],	w[1]
	 * I1,	FALSE,		I2,	TRUE,		n[3],	w[1]
	 * I1,	FALSE,		I2,	FALSE,		n[4],	w[1]
	 * D1,	TRUE,		D2,	TRUE,		n[5],	w[2]
	 * D1,	TRUE,		D2,	FALSE,		n[6],	w[2]
	 * D1,	FALSE,		D2,	TRUE,		n[7],	w[2]
	 * D1,	FALSE,		D2,	FALSE,		n[8],	w[2]
	 * I1,	TRUE,		D1,	TRUE,		n[9],	w[3]
	 * I1,	TRUE,		D1,	FALSE,		n[10],	w[3]
	 * I1,	FALSE,		D1,	TRUE,		n[11],	w[3]
	 * I1,	FALSE,		D1,	FALSE,		n[12],	w[3]
	 * I2,	TRUE,		D2,	TRUE,		n[13],	w[4]
	 * I2,	TRUE,		D2,	FALSE,		n[14],	w[4]
	 * I2,	FALSE,		D2,	TRUE,		n[15],	w[4]
	 * I2,	FALSE,		D2,	FALSE,		n[16],	w[4]
	 * </pre>
	 * @param tables
	 * @param auxiliaryTables 
	 * @return
	 */
	public String getCountTableDescription(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables) {
		// assertion
		if (primaryTables == null && auxiliaryTables == null) {
			return "";
		}
		
		// create a table containing all tables
		List<PotentialTable> tables = new ArrayList<PotentialTable>();
		if (primaryTables != null) {
			tables.addAll(primaryTables);
		}
		if (isToConsiderDetectors() && auxiliaryTables != null) {
			tables.addAll(auxiliaryTables);
		}
		
		
		// check that all tables have same number of variables
		int numVars = tables.get(0).variableCount();
		for (int i = 1; i < tables.size(); i++) {
			if (tables.get(i).variableCount() != numVars) {
				throw new RuntimeException("Tables must have the same number of variables, but found: " + tables.get(i).variableCount() + ", and " +  numVars);
			}
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
	    // print 1st line : Var1,	StateVar1,	Var2,	StateVar2,	n,	w
	    for (int i = 0; i < numVars; i++) {
	    	// Var1,	StateVar1,	Var2,	StateVar2, ...
			printer.print("Var" + (i+1) + ",StateVar" + (i+1) + ",") ;
		}
	    // n,w
	    printer.print(getAuxiliaryTableWeightSymbol());	
	    if (getPrimaryTableWeightSymbol() != null && !getPrimaryTableWeightSymbol().isEmpty()) {
	    	printer.println("," + getPrimaryTableWeightSymbol());	// n,w
	    } else {
	    	printer.println();
	    }
	    
	    
	    // I1,	TRUE,		I2,	TRUE,		n[1],	w[1]
	    for (int tableIndex = 0, rowIndex = 0; tableIndex < tables.size(); tableIndex++) {
			PotentialTable table = tables.get(tableIndex);
			
			for (int cellIndex = 0; cellIndex < table.tableSize(); cellIndex++, rowIndex++) {
				int[] coord = table.getMultidimensionalCoord(cellIndex);
				for (int varIndex = coord.length-1; varIndex >= 0; varIndex--) {
					printer.print(table.getVariableAt(varIndex).getName() + ",");
					printer.print(table.getVariableAt(varIndex).getStateAt(coord[varIndex]) + ",");
				}
				printer.print(getAuxiliaryTableWeightSymbol()+"[" + (rowIndex+1) + "],");
				if (getPrimaryTableWeightSymbol() != null && !getPrimaryTableWeightSymbol().isEmpty()) {
					printer.println(getPrimaryTableWeightSymbol() + "[" + (tableIndex+1) +"]");
				} else {
					printer.println();
				}
			}
		}
		
		return output.toString();
	}


	/**
	 * Print something like the following:
	 * <pre>
	 * w[1] * ( (x[1] + x[2] - n[1])^2 + (x[3] + x[4] - n[2])^2 + (x[5] + x[6] - n[3])^2 + (x[7] + x[8] - n[4])^2) )
	 * + w[2] * ( (x[1] + x[3] - n[5])^2 + (x[2] + x[4] - n[6])^2 + (x[5] + x[7] - n[7])^2 + (x[6] + x[8] - n[8])^2) )
	 * + w[3] * ( (x[1] + x[5] - n[9])^2 + (x[2] + x[4] - n[10])^2 + (x[5] + x[7] - n[11])^2 + (x[6] + x[8] - n[12])^2 ) 
	 * ...
	 * set k := {1,2,5,6,9,10};
	 * </pre>
	 * @see utils.ObjFunctionPrinter#getObjFunction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String getObjFunction(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
		printer.print("objFun :");
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		// use all tables equally (instead of distinguishing them in auxiliary and primary tables)
		List<PotentialTable> allTables = new ArrayList<PotentialTable>();	// create a new list containing all of them
		if (primaryTables != null) {
			allTables.addAll(primaryTables);
		}
		// auxiliary tables comes after primary tables
		if (isToUseAuxiliaryTables() && auxiliaryTables != null) {
			allTables.addAll(auxiliaryTables);	
		}
		
		// tableIndex is the index for w; globalCellIndex is the index for n (i.e. the index of a cell in a table, but it does not reset from table to table)
		boolean hasWeightFactor = false;
		for (int tableIndex = 0, globalCellIndex = 0; tableIndex < allTables.size(); tableIndex++) {
			PotentialTable currentTable = allTables.get(tableIndex);
			
			// w[1] * ( 
			String weightFactor = "";
			if (hasWeightFactor) {
				weightFactor += " +";
			}
			
			weightFactor += (" " + getPrimaryTableWeightSymbol() + "[" + (tableIndex+1) +"]" + " * (");
			
			// (x[1] + x[2] - n[1])^2 + (x[3] + x[4] - n[2])^2 + (x[5] + x[6] - n[3])^2 + (x[7] + x[8] - n[4])^2) )
			boolean hasSquareFactor = false;	// this will become true if we ever wrote (x[5] + x[6] - n[3])^2
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++, globalCellIndex++) {
				// cell index is like a translation of globalCellIndex to an index in current table
				
				String squareFactor = "";
				
				// + (x[1] + x[2] - n[1])^2
				if (hasSquareFactor) {
					squareFactor += (" +");
				}
				squareFactor += (" (");
				
				// coord represents the states (of the variables) associated with current cell in current table
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				// is1stP is whether this is the 1st probability in parenthesis (it's going to be used to decide whether to append a "+" before p)
				boolean is1stP = true;	
				// find what cells in joint table are associated with the states in coord
				for (Integer jointCellIndex = 0; jointCellIndex < jointTable.tableSize(); jointCellIndex++) {
					
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
					
					if (found && !getJointIndexesToIgnore().contains(jointCellIndex)) {
						if (!is1stP) {
							squareFactor += (" +");
						}
						squareFactor += (" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						is1stP = false;
					}
					
				}
				
				squareFactor += (" - " + getAuxiliaryTableWeightSymbol() + "[" + (globalCellIndex+1) + "]");
				
				squareFactor += (" )^2");
				
				if (!is1stP) {
					weightFactor += (squareFactor);
					hasSquareFactor = true;
				}
				
			}
			
			// end of ( (x[1] + x[2] - n[1])^2 + (x[3] + x[4] - n[2])^2 + (x[5] + x[6] - n[3])^2 + (x[7] + x[8] - n[4])^2) )
			weightFactor += (" )");
			
			if (hasSquareFactor) {
				printer.print(weightFactor);
				hasWeightFactor = true;
			}
			
			if (hasWeightFactor && isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
		}
		
		
		printer.println(";");
		
		
		
		return output.toString();
	}


	/**
	 * Prints something like the following:
	 * <pre>
	 * set k := {1,2,3,5,7,8,9};
	 * </pre>
	 * @param printer 
	 * @param jointTable
	 * @param jointIndexesToIgnore
	 * @param auxiliaryTables 
	 * @param primaryTables 
	 */
	public void printJointIndexesToConsider(PrintStream printer, PotentialTable jointTable, Collection<Integer> jointIndexesToIgnore, List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables) {
		if (jointTable == null || jointTable.tableSize() <= 0) {
			return;
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		if (jointIndexesToIgnore == null) {
			jointIndexesToIgnore = Collections.EMPTY_LIST;
		}
		
		printer.print("set " + getJointIndexVariableName());
		printer.print(" := { ");
		int numEntries = 0;	// counts how many entries were written
		for (int i = 0; i < jointTable.tableSize(); i++) {
			if (!jointIndexesToIgnore.contains(i)) {
				if (numEntries > 0) {
					printer.print(" , ");
				}
				printer.print(""+(i+1));
				numEntries++;
			}
		}
		printer.println("};");
		
		printer.println();
		printer.println();
		
		printer.println("size_" + getJointIndexVariableName() + " = " + numEntries);
		
		
	}


	/**
	 * @param optionValues : list of comma-separated list of names of variables.
	 * @return : a list whose each element is an entry in optionValues (comma-separated option values will be split to a list).
	 * Wildcards will be properly handled.
	 * @see #getThreatName()
	 * @see #getAlertName()
	 * @see #getIndicatorNames()
	 * @see #getDetectorNames()
	 */
	public List<List<String>> parseVarNames(String[] optionValues) {
		
		List<List<String>> ret = new ArrayList<List<String>>();
		
		if (optionValues == null) {
			return ret;
		}
		
		// prepare the collection of all valid names of variables to consider
		List<String> allKnownVarNames = new ArrayList<String>();
		if (getThreatName() != null && !getThreatName().trim().isEmpty()) {
			allKnownVarNames.add(getThreatName());
		}
		if (getIndicatorNames() != null) {
			for (String name : getIndicatorNames()) {
				if (name != null && !name.trim().isEmpty()) {
					allKnownVarNames.add(name);
				}
			}
		}
		if (getDetectorNames() != null) {
			for (String name : getDetectorNames()) {
				if (name != null && !name.trim().isEmpty()) {
					allKnownVarNames.add(name);
				}
			}
		}
		if (getAlertName() != null && !getAlertName().trim().isEmpty()) {
			allKnownVarNames.add(getAlertName());
		}
		
		// handle all option values
		for (String regex : optionValues) {
			int numVars = regex.split(",").length;
			
			// create a matrix which is simply allKnownVarNames copied the number of comma-separated blocks we have.
			List<List<String>> allKnownVarsMatrix = new ArrayList<List<String>>(numVars);
			for (int i = 0; i < numVars; i++) {
				allKnownVarsMatrix.add(allKnownVarNames);
			}
			

			// create index (multi-dimensional) to iterate on matrix
			int[] multiIndex = new int[allKnownVarsMatrix.size()];
			for (int i = 0; i < multiIndex.length; i++) {
				multiIndex[i] = 0;	// begin at 1st element (index 0)
			}
			

			// iterate on matrix
			while(!allKnownVarsMatrix.isEmpty()) {	// the condition to end iteration is not here (see last if-clause)
				
				// fill the name with current combination in matrix
				List<String> names = new ArrayList<String>();
				for (int i = 0; i < multiIndex.length; i++) {
					names.add(allKnownVarsMatrix.get(i).get(multiIndex[i]));
				}
				
				if (!names.isEmpty()) {
					// convert names to a comma-separated string (so that we can see if it matches regex)
					String stringToMatch = names.get(0);
					for (int i = 1; i < names.size(); i++) {
						stringToMatch += "," + names.get(i);
					}
					
					if (stringToMatch.matches(regex)) {	// return it if current combination of nodes matches regex
						ret.add(names);
					}
				}
				
				// increment index
				boolean hasNext = false; // this will tell if any row in matrix had a next column
				for (int i =  multiIndex.length-1; i >= 0; i--) {
					if (multiIndex[i]+1 < allKnownVarsMatrix.get(i).size()) {
						multiIndex[i]++;
						hasNext = true;
						break;	// break the for
					} else {
						multiIndex[i] = 0;
					}
				}
				
				if (!hasNext) { // this is the only condition to end iteration
					break;	// break the while(!matrix.isEmpty())
				}
				
			}	// end of while(!matrix.isEmpty())
			
		}	// end of for
		
		return ret;
	}
	


	/**
	 * 
	 * @return
	 */
	public boolean isToUseAuxiliaryTables() {
		return this.isToUseAuxiliaryTable;
	}

	/**
	 * 
	 * @param isToUseAuxiliaryTable
	 */
	public void setToUseAuxiliaryTables(boolean isToUseAuxiliaryTable) {
		this.isToUseAuxiliaryTable  = isToUseAuxiliaryTable;
	}


	/**
	 * @return the isToUsePrimaryTables
	 */
	public boolean isToUsePrimaryTables() {
		return isToUsePrimaryTables;
	}


	/**
	 * @param isToUsePrimaryTables the isToUsePrimaryTables to set
	 */
	public void setToUsePrimaryTables(boolean isToUsePrimaryTables) {
		this.isToUsePrimaryTables = isToUsePrimaryTables;
	}


	/**
	 * @return the outputPath
	 */
	public String getOutputPath() {
		return outputPath;
	}


	/**
	 * @param outputPath the outputPath to set
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("out","output", true, "Output file. If null, then the result will be printed to console.");
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("inames","indicator-names", true, "Comma-separated names of indicators.");
		options.addOption("dnames","detector-names", true, "Comma-separated names of detectors.");
		options.addOption("threat","threat-name", true, "Name of threat variable.");
		options.addOption("alert","alert-name", true, "Name of alert variable.");
		options.addOption("h","help", false, "Help.");
		options.addOption("aux","use-auxiliary-table", false, "Use auxiliary tables: tables of Indicator X Detector or Threat X Indicator joint states.");
		options.addOption("aonly","use-auxiliary-table-only", false, "Use auxiliary tables (e.g. tables of Indicator X Detector or Threat X Indicator joint states) "
				+ "and do not use primary tables (e.g. correlation tables).");
		options.addOption("pnames","primary-table-var-names", true, "Names (comma-separated) of variables to be included in primary table. "
				+ "Wildcard \"*\" can be used to indicate all names that matches.");
		
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
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		if (cmd.hasOption("d")) {
			Debug.setDebug(true);
		} else {
			Debug.setDebug(false);
		}
		
		EuclideanDistanceObjFunctionPrinter printer = new EuclideanDistanceObjFunctionPrinter();
		
		
		
		
		printer.setToUseAuxiliaryTables(cmd.hasOption("aux"));
		
		if (cmd.hasOption("aonly")) {
			printer.setToUseAuxiliaryTables(true);
			printer.setToUsePrimaryTables(false);
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
		if (cmd.hasOption("alert")) {
			printer.setAlertName(cmd.getOptionValue("alert"));
		}
		
		if (cmd.hasOption("pnames")) {
			printer.setPrimaryTableVarNames(printer.parseVarNames(cmd.getOptionValues("pnames")));
		}

		if (cmd.hasOption("out")) {
			File out = new File(cmd.getOptionValue("out"));
			if (!out.exists()) {
				out.mkdirs();
			}
			if (out.isDirectory()) {
				printer.setOutputPath(out.getAbsolutePath());
				try {
					printer.printAll(null, null, null, null,null);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					printer.printAll(null, null, null, null,new PrintStream(out));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			try {
				printer.printAll(null, null, null, null,System.out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}


	/**
	 * @return the jointIndexesToIgnore : joint probabilities with indexes specified here will be ignored in {@link #getObjFunction(List, List, PotentialTable)}
	 * 
	 */
	public Collection<Integer> getJointIndexesToIgnore() {
		return jointIndexesToIgnore;
	}


	/**
	 * @param jointIndexesToIgnore : joint probabilities with indexes specified here will be ignored in {@link #getObjFunction(List, List, PotentialTable)}
	 */
	public void setJointIndexesToIgnore(Collection<Integer> indexesToIgnore) {
		this.jointIndexesToIgnore = indexesToIgnore;
	}


	/**
	 * @return the jointIndexVariableName
	 */
	public String getJointIndexVariableName() {
		return jointIndexVariableName;
	}


	/**
	 * @param jointIndexVariableName the jointIndexVariableName to set
	 */
	public void setJointIndexVariableName(String jointIndexVariableName) {
		this.jointIndexVariableName = jointIndexVariableName;
	}
	

}
