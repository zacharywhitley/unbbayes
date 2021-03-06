/**
 * 
 */
package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class LogLikelihoodObjFunctionPrinter extends
		ChiSquareObjFunctionPrinter {

	/**
	 * 
	 */
	public LogLikelihoodObjFunctionPrinter() {
		// TODO Auto-generated constructor stub
	}
	
	

	/**
	 * This will return a string like the following
	 * <pre>
	 * objFun : 
	 * 		w[1] * ( n[1] * log( p[1] + p[2] +,...,p[k] ) + n[2] * log(p[3] + ... + p[l]) ) + w[2] * ( 271 * log( p[1] + p[2] +,...,p[m] ) + ...
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
		boolean isFirstLine = true;
		for (int tableIndex = 0, offset = 0; tableIndex < allTables.size(); tableIndex++) {
			PotentialTable currentTable = allTables.get(tableIndex);
			
			
			String weightFactor = this.getObjFunctionLine(currentTable, isFirstLine, tableIndex, offset, jointTable);
			offset += currentTable.tableSize();
			
//			if (hasLogFactor) {
			if (weightFactor != null) {
				printer.print(weightFactor);
				isFirstLine = false;
			}
			
			if (!isFirstLine && isToBreakLineOnObjectFunction()) {
				printer.println();
			}
			
		}
		
		
		printer.println(";");
		
		
		
		return output.toString();
	}

	/**
	 * 
	 * @param currentTable
	 * @param isFirstLine
	 * @param lineIndex
	 * @param offset
	 * @param jointTable
	 * @return
	 */
	protected String getObjFunctionLine(PotentialTable currentTable, boolean isFirstLine, int lineIndex, int offset, PotentialTable jointTable) {

		// w[1] * ( 
		String ret = "";
		if (!isFirstLine) {
			ret += " +";
		}
		
		ret += (" " + getPrimaryTableWeightSymbol() + "[" + (lineIndex+1) +"]" + " * (");
		
		// w[1] * ( n[1] * log( p[1] + p[2] +,...,p[k] ) + n[2] * log(p[3] + ... + p[l]) ) + w[2] * ( 271 * log( p[1] + p[2] +,...,p[m] ) + ...
		boolean hasLogFactor = false;	// this will become true if we ever wrote content of log
		for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++) {
			// cell index is like a translation of globalCellIndex to an index in current table
			
			String logFactor = "";
			
			
			// +  n[1] * log( p[1] + p[2] +,...,p[k] )
			if (hasLogFactor) {
				logFactor += (" + ");
			}
			logFactor += (getAuxiliaryTableWeightSymbol() + "[" + (offset + cellIndex + 1) + "] * log(");
			
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
						logFactor += (" +");
					}
					logFactor += (" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
					is1stP = false;
				}
				
			}
			
			logFactor += (" )");
			
			if (!is1stP) {
				ret += (logFactor);
				hasLogFactor = true;
			}
			
		}
		// end of ( n[1] * log( p[1] + p[2] +,...,p[k] ) + n[2] * log(p[3] + ... + p[l]) )
		
		if (!hasLogFactor) {
			return null;
		}
		
		ret += (" )");
		
		return ret;
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
		
		LogLikelihoodObjFunctionPrinter printer = new LogLikelihoodObjFunctionPrinter();
		
		
		
		
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

}
