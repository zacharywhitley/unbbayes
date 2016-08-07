/**
 * 
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
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

		if (printer == null) {
			printer = System.out;
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
		
		// tables related to correlation. 
		// We won't use the values in cells (we'll just use the instance of PotentialTable to organize the order and names of variables/states)
		List<PotentialTable> primaryTables = new ArrayList<PotentialTable>();
		if (isToUsePrimaryTables()) {
			primaryTables.addAll(this.getCorrelationTables(variableMap , null, indicatorNameList));
			primaryTables.addAll(this.getCorrelationTables(variableMap , null, detectorNameList));
		}
		
		// Similarly, tables related to Indicator X Detector. 
		// Again, we won't use the values in cells (the table is just for organizing variables and states)
		List<PotentialTable> auxiliaryTables = null;
		if (isToUseAuxiliaryTables()) {
			auxiliaryTables = new ArrayList<PotentialTable>();
			auxiliaryTables.addAll(this.getThreatTables(variableMap, null, indicatorNameList, getThreatName()));
			auxiliaryTables.addAll(this.getDetectorTables(variableMap , null, indicatorNameList, detectorNameList));
		}
		
		// actually print the objective function
		printer.println(this.getObjFunction(primaryTables, auxiliaryTables, jointTable));

		// print the meaning of p variables
		if (isToPrintJointProbabilityDescription()) {
			printer.println();
			printer.println();
			printer.println("Joint probability table:");
			printer.println(this.getJointTableDescription(jointTable));
			
			// also print the meaning of count variables n[i] and w[i]
			printer.println();
			printer.println("Counts:");
			printer.println(this.getCountTableDescription(primaryTables, auxiliaryTables));
		}
		
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
		for (int tableIndex = 0, globalCellIndex = 0; tableIndex < allTables.size(); tableIndex++) {
			PotentialTable currentTable = allTables.get(tableIndex);
			
			// w[1] * ( 
			printer.print(" " + getPrimaryTableWeightSymbol() + "[" + (tableIndex+1) +"]" + " * ( ");
			
			// (x[1] + x[2] - n[1])^2 + (x[3] + x[4] - n[2])^2 + (x[5] + x[6] - n[3])^2 + (x[7] + x[8] - n[4])^2) )
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++, globalCellIndex++) {
				// cell index is like a translation of globalCellIndex to an index in current table
				
				// (x[1] + x[2] - n[1])^2
				printer.print(" (");
				
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
					
					if (found) {
						if (!is1stP) {
							printer.print(" +");
						}
						printer.print(" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						is1stP = false;
					}
					
				}
				
				printer.print(" - " + getAuxiliaryTableWeightSymbol() + "[" + (globalCellIndex+1) + "]");
				
				printer.print(" )^2");
				
				if (cellIndex + 1 < currentTable.tableSize()) {
					printer.print(" + ");
				}
			}
			
			// end of ( (x[1] + x[2] - n[1])^2 + (x[3] + x[4] - n[2])^2 + (x[5] + x[6] - n[3])^2 + (x[7] + x[8] - n[4])^2) )
			printer.print(" )");
			
			if (isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
			// this is the "+" before next weight
			if (tableIndex + 1 < allTables.size()) {
				printer.print(" +");
			}
		}
		
		
		printer.println(";");
		
		return output.toString();
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
		
		
		
		PrintStream ps = System.out;
		if (cmd.hasOption("out")) {
			try {
				ps = new PrintStream(new File(cmd.getOptionValue("out")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				ps = System.out;
			}
		}
		
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
		
		
		try {
			printer.printAll(null, null, null, null,ps);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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


}
