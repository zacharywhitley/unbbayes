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
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class ConditionalLogLikelihoodObjFunctionPrinter extends LogLikelihoodObjFunctionPrinter {
	
	private boolean isToUseJointProbabilities = true;

	/**
	 * 
	 */
	public ConditionalLogLikelihoodObjFunctionPrinter() {}
	

	

	/**
	 * This will print nothing. The conditional probabilities will be printed by {@link #getCountTableDescription(List, List)}.
	 * @see utils.ObjFunctionPrinter#getJointTableDescription(unbbayes.prs.bn.PotentialTable)
	 */
	public String getJointTableDescription(PotentialTable jointTable) {
		return "";
	}
	

	/**
	 *  This will return a string like the following
	 * <pre>
	 * objFun : 
	 * 		w[1] * ( n[1] * log( p[1] ) + n[2] * log(p[2]) ) + ... 
	 * </pre>
	 * @see utils.LogLikelihoodObjFunctionPrinter#getObjFunction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
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
		
		// prepare name of threat variable, because we might need to find it later
		String threatName = getThreatName();
		
		// tableIndex is the index for w; globalCellIndex is the index for n (i.e. the index of a cell in a table, but it does not reset from table to table)
		boolean hasWeightFactor = false;
		for (int tableIndex = 0, /*globalCellIndex = 0,*/ offset = 0; tableIndex < allTables.size(); tableIndex++) {
			PotentialTable currentTable = allTables.get(tableIndex);
			
			// find threat variable in current table
			int threatIndex = -1;		// position of threat variable in current table
			int numStatesThreat = -1;	// number of states of the threat variable (in current table)
			if (isToUseJointProbabilities() && (threatName != null)) {
				for (int i = 0; i < currentTable.getVariablesSize(); i++) {
					if (currentTable.getVariableAt(i).getName().equals(threatName)) {
						threatIndex = i;
						numStatesThreat = currentTable.getVariableAt(i).getStatesSize();
						break;
					}
				}
			}
			
			// w[1] * ( 
			String weightFactor = "";
			if (hasWeightFactor) {
				weightFactor += " +";
			}
			
			weightFactor += (" " + getPrimaryTableWeightSymbol() + "[" + (tableIndex+1) +"]" + " * (");
			
			// w[1] * ( n[1] * log(p[1]) + n[2] * log(p[2])) + w[2] *  ...
			boolean hasLogFactor = false;	// this will become true if we ever wrote the content of log
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++/*, globalCellIndex++*/) {
				
				String logFactor = "";
				
				// +  n[1] * 
				if (hasLogFactor) {
					logFactor += (" + ");
				}
//				logFactor += getAuxiliaryTableWeightSymbol() + "[" + (globalCellIndex+1) + "] * ";
				logFactor += getAuxiliaryTableWeightSymbol() + "[" + (offset + cellIndex + 1) + "] * ";
				
				if (isToUseJointProbabilities()) {
					// fill with something like "( log(p[1]) - log(p[1] + p[3]) )"
					if (threatIndex < 0) {
						throw new IllegalArgumentException("Variable " + threatName + " not found in table " + currentTable);
					}
					if (currentTable.getVariablesSize() != 2) {
						throw new IllegalArgumentException("Current version does not support tuples of variables other than pairs: " + currentTable);
					}
					// find the variable other than threat
//					int otherVarIndex = -1;
//					for (int i = 0; i < currentTable.getVariablesSize(); i++) {
//						if (i != threatIndex) {
//							otherVarIndex = i;
//							break;
//						}
//					}
//					if (otherVarIndex < 0) {
//						throw new IllegalArgumentException("Could not find variable other than threat: " + currentTable);
//					}
					
					// ( log(p[1]) - log(p[1] + p[3]) )
//					logFactor += "( log(" + getProbabilityVariablePrefix() + "[" + (globalCellIndex+1) + "]) - log(";
					logFactor += "( log(" + getProbabilityVariablePrefix() + "[" + (offset + cellIndex + 1) + "]) - log(";
					
					// convert global cell index to a coordinate (<state of var1>, <state of var2>)
					int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
					
					// p[1] + p[3]
					for (int i = 0; i < numStatesThreat ; i++) {
						coord[threatIndex] = i;
						logFactor += getProbabilityVariablePrefix() + "[" + (offset + currentTable.getLinearCoord(coord) + 1) + "]";
						if (i+1 < numStatesThreat) {
							logFactor += " + ";
						}
					}
					logFactor += ") )";
				} else {
					// fill with something like log( p[1] )
					logFactor += "log(" + getProbabilityVariablePrefix() + "[" + (offset + cellIndex + 1) + "])";
				}
				
				weightFactor += logFactor;
				hasLogFactor = true;
				
			}
			
			// end of ( n[1] * log(p[1]) + n[2] * log(p[2]) )
			weightFactor += (" )");
			
			if (hasLogFactor) {
				printer.print(weightFactor);
				hasWeightFactor = true;
			}
			
			if (hasWeightFactor && isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
			// next iteration shall start with this index
			offset += currentTable.tableSize();
		}
		
		
		printer.println(";");
		
		
		
		return output.toString();
	}
	

	/**
	 * Prints the following constraints 
	 * which indicate that conditional probabilities should sum up to 1.
	 * <pre>
	 * x[1] + x[3] = 1;		(this means P(Threat=true|I1=true) + P(Threat=false|I1=true) = 1)
	 * x[2] + x[4] = 1;		(this means P(Threat=true|I1=false) + P(Threat=false|I1=false) = 1)
	 * x[5] + x[7] = 1;		(this means P(Threat=true|I2=true) + P(Threat=false|I2=true) = 1)
	 * x[6] + x[8] = 1;		(this means P(Threat=true|I2=false) + P(Threat=false|I2=false) = 1)
	 * </pre>
	 * Or if {@link #isToUseJointProbabilities()} is on, then:
	 * <pre>
	 * x[1] + x[2] + x[3] + x[4] = 1;		(this means sum of P(Threat,I1) = 1)
	 * x[5] + x[6] + x[7] + x[8] = 1;		(this means sum of P(Threat,I2) = 1)
	 * </pre>
	 * @see utils.EuclideanDistanceObjFunctionPrinter#printJointIndexesToConsider(java.io.PrintStream, unbbayes.prs.bn.PotentialTable, java.util.Collection, java.util.List, java.util.List)
	 */
	public void printJointIndexesToConsider(PrintStream printer, PotentialTable jointTable, Collection<Integer> jointIndexesToIgnore,  List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables) {
//		super.printJointIndexesToConsider(printer, jointTable, jointIndexesToIgnore);
		if (jointTable == null || jointTable.tableSize() <= 0) {
			return;
		}
		
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		printer.println("Subject to:");
		if (isToBreakLineOnObjectFunction()) {
			printer.println();
		}
		
		if (isToUseJointProbabilities()) {

			// use all tables equally (instead of distinguishing them in auxiliary and primary tables)
			List<PotentialTable> allTables = new ArrayList<PotentialTable>();	// create a new list containing all of them
			if (primaryTables != null) {
				allTables.addAll(primaryTables);
			}
			// auxiliary tables comes after primary tables
			if (isToUseAuxiliaryTables() && auxiliaryTables != null) {
				allTables.addAll(auxiliaryTables);	
			}
			
			int offset = 0;
			for (PotentialTable table : allTables) {
				for (int cellIndex = 0; cellIndex < table.tableSize(); cellIndex++) {
					printer.print(getProbabilityVariablePrefix() + "[" + (offset + cellIndex + 1) + "]");
					if (cellIndex + 1 < table.tableSize()) {
						printer.print(" + ");
					}
				}
				printer.println(" = 1;");
				offset += table.tableSize();
			}
		} else {
			// TODO use primary and auxiliary tables instead
			Debug.println(getClass(), "Warning: using joint tables instead of primary and auxiliary tables to print constraints about conditionals probabilities...");
			// find threat variable
			int threatVarIndex = -1;
			INode threatVar = null;
			for (int i = 0; i < jointTable.getVariablesSize(); i++) {
				if (getThreatName().equals(jointTable.getVariableAt(i).getName())) {
					// check if threat has only 2 states
//					if (jointTable.getVariableAt(i).getStatesSize() != 2) {
//						throw new IllegalArgumentException("Current version only supports threat variable with 2 states, but found " + jointTable.getVariableAt(i).getStatesSize());
//					}
					threatVarIndex = i;
					threatVar = jointTable.getVariableAt(i);
					break;
				};
			}
			
			if (threatVarIndex <= 0) {
				Debug.println(getClass(), "No threat variable found. Threat name: " + getThreatName());
				return;
			}
			
			// for each pair of variables (i.e. threat and something), print constraints
			for (int varIndex = 0, offset = 0; varIndex < jointTable.getVariablesSize(); varIndex++) {
				if (varIndex == threatVarIndex) {
					continue;
				}
				INode currentVar = jointTable.getVariableAt(varIndex);
				// check if var has only 2 states
//				if (currentVar.getStatesSize() != 2) {
//					throw new IllegalArgumentException("Current version only supports variables with 2 states, but found "
//							+ currentVar + " with " + currentVar.getStatesSize() + " states.");
//				}
				
				// this is just an object to convert multi-dimensional coordinates (var1=true, var2=false) to a linear index.
				PotentialTable indexConverter = new ProbabilisticTable();
				indexConverter.addVariable(currentVar);
				indexConverter.addVariable(threatVar);
				int[] coord = indexConverter.getMultidimensionalCoord(0);	// this array represents a multi-dimensional coordinate.
				
				// print x[1] + x[3] = 1; for each pair of variables
				for (int currentVarStateIndex = 0; currentVarStateIndex < currentVar.getStatesSize(); currentVarStateIndex++) {
					
					coord[indexConverter.getVariableIndex((Node) currentVar)] = currentVarStateIndex;
					
					// print x[1] + x[3]
					for (int threatVarStateIndex = 0; threatVarStateIndex < threatVar.getStatesSize(); threatVarStateIndex++) {
						
						coord[indexConverter.getVariableIndex((Node) threatVar)] = threatVarStateIndex;
						
						printer.print(getProbabilityVariablePrefix() + "[" + (offset + indexConverter.getLinearCoord(coord) + 1) + "]");
						
						if (threatVarStateIndex + 1 < threatVar.getStatesSize()) {
							printer.print(" + ");
						}
						
					}	// end of x[1] + x[3]
					
					printer.println(" = 1;");
					
				}	// end of x[1] + x[3] = 1; x[2] + x[4] = 1;
				
				// adjust offset of next iteration, because next iteration shall start with larger index. 
				// E.g. first iteration shall be x[1] + x[3] = 1; x[2] + x[4]; 
				// next iteration shall be x[5] + x[7] = 1; x[6] + x[8]; 
				offset += currentVar.getStatesSize() * threatVar.getStatesSize();
				
			}	// end of for each pair of variables (i.e. threat and something), print constraints
			
		}
		
		
	}
	

	/**
	 * This will return a string like the following (without the tabs and white spaces):
	 * <pre>
	 * Var,	State,	Cond_Var,	Cond_State,	n,	w,	p
	 * Threat,	TRUE,	I1,		TRUE,		n[1],	w[1],	p[1]
	 * Threat,	TRUE,	I1,		FALSE,		n[2],	w[1],	p[2]
	 * Threat,	FALSE,	I1,		TRUE,		n[3],	w[1],	p[3]
	 * Threat,	FALSE,	I1,		FALSE,		n[4],	w[1],	p[4]
	 * Threat,	TRUE,	I2,		TRUE,		n[5],	w[2],	p[5]
	 * Threat,	TRUE,	I2,		FALSE,		n[6],	w[2],	p[6]
	 * Threat,	FALSE,	I2,		TRUE,		n[7],	w[2],	p[7]
	 * Threat,	FALSE,	I2,		FALSE,		n[8],	w[2],	p[8]
	 * </pre>
	 * (non-Javadoc)
	 * @see utils.EuclideanDistanceObjFunctionPrinter#getCountTableDescription(java.util.List, java.util.List)
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
		
	    // print 1st line : Var,	State,	Cond_Var,	Cond_State,	n,	w,	p
	    if (isToUseJointProbabilities()) {
	    	printer.print("Var1,State1,Var2,State2,") ;
	    } else {
	    	printer.print("Var,State,Cond_Var,Cond_State,") ;
	    }
	    printer.print(getAuxiliaryTableWeightSymbol());			// n
	    if (getPrimaryTableWeightSymbol() != null && !getPrimaryTableWeightSymbol().isEmpty()) {
	    	printer.print("," + getPrimaryTableWeightSymbol());	// w
	    }
	    printer.println("," + getProbabilityVariablePrefix());	// p
	    
	    // Threat,	TRUE,	I1,		TRUE,		n[1],	w[1],	p[1]
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
					printer.print(getPrimaryTableWeightSymbol() + "[" + (tableIndex+1) +"],");
				}
				printer.println(getProbabilityVariablePrefix()+"[" + (rowIndex+1) + "],");
			}
		}
		
		return output.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see utils.EuclideanDistanceObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][], java.io.PrintStream)
	 */
	public void printAll(int[][][] indicatorCorrelationsMatrix, int[][][] detectorCorrelationsMatrix, int[][][] threatIndicatorTableMatrix, int[][][] detectorIndicatorTableMatrix, 
			PrintStream printer) throws IOException {
		
		super.printAll(indicatorCorrelationsMatrix, detectorCorrelationsMatrix, threatIndicatorTableMatrix, detectorIndicatorTableMatrix, printer);
		
		// delete joint probability table
		if (printer == null) {
			File file = new File(getOutputPath());
			if (file.exists() && file.isDirectory()) {
				File jointProbTableFile = new File(file,"meaning_prob.csv");
				if (jointProbTableFile.exists()) {
					jointProbTableFile.delete();
				}
				File meaningFile = new File(file,"meaning_count_weight.csv");
				if (meaningFile.exists()) {
					meaningFile.renameTo(new File(file,"meaning_count_weight_prob.csv"));
				}
			}
		}
		
	}
	


	/**
	 * @return the isToUseJointProbabilities : if true, conditional probabilities will be considered
	 * as a sum and division of joint probabilities:
	 * <pre>
	 * P(X|Y) = P(X,Y) / (P(X=true,Y) + P(X=false,Y))
	 * </pre>
	 * @see #getCountTableDescription(List, List)
	 * @see #getObjFunction(List, List, PotentialTable)
	 */
	public boolean isToUseJointProbabilities() {
		return isToUseJointProbabilities;
	}




	/**
	 * @param isToUseJointProbabilities : : if true, conditional probabilities will be considered
	 * as a sum and division of joint probabilities:
	 * <pre>
	 * P(X|Y) = P(X,Y) / (P(X=true,Y) + P(X=false,Y))
	 * </pre>
	 * @see #getCountTableDescription(List, List)
	 * @see #getObjFunction(List, List, PotentialTable)
	 */
	public void setToUseJointProbabilities(boolean isToUseJointProbabilities) {
		this.isToUseJointProbabilities = isToUseJointProbabilities;
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
		options.addOption("c","conditiona;", false, "Variables will represent conditional probabilities P(X|Y) directly, instead of joint probabilities of subset of variables.");
		
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
		
		if (cmd.hasOption("h") || args.length <= 0) {
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
		
		ConditionalLogLikelihoodObjFunctionPrinter printer = new ConditionalLogLikelihoodObjFunctionPrinter();
		
		
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
		printer.setToUseJointProbabilities(!cmd.hasOption("c"));
		

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




}
