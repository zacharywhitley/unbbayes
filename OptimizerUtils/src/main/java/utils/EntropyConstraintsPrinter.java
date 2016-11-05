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
public class EntropyConstraintsPrinter extends
		EuclideanDistanceObjFunctionPrinter {

	/**
	 * Default constructor
	 */
	public EntropyConstraintsPrinter() {
		super();
		this.setPrimaryTableWeightSymbol("total");	// for total number of entries
//		this.setAuxiliaryTableWeightSymbol("n");	// for counts
//		this.setProbabilityVariablePrefix("x");		// for probabilities to be optimized
	}
	
	/**
	 * Prints a set of constraints instead of an objective function. <br/><br/>
	 * The constraints to print are something like:
	 * <pre/>
	 * Subject to :	
	 * x[1] + x[2]  = n[1]/total ;
	 * x[3] + x[4]  = n[2]/total ;
	 * x[1] + x[3]  = n[3]/total ;
	 * x[2] + x[4]  = n[4]/total ;
	 * </pre>
	 * Note: x = {@link #getProbabilityVariablePrefix()}, n = {@link #getAuxiliaryTableWeightSymbol()}, total = {@link #getPrimaryTableWeightSymbol()}.
	 * @see utils.EuclideanDistanceObjFunctionPrinter#getObjFunction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String getObjFunction(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
	    PrintStream printer = new PrintStream(output);
		
		
		// use all tables equally (instead of distinguishing them in auxiliary and primary tables)
		List<PotentialTable> allTables = new ArrayList<PotentialTable>();	// create a new list containing all of them
		if (primaryTables != null) {
			allTables.addAll(primaryTables);
		}
		// auxiliary tables comes after primary tables
		if (isToUseAuxiliaryTables() && auxiliaryTables != null) {
			allTables.addAll(auxiliaryTables);	
		}
		
		
		if (allTables.isEmpty()) {
			return "";
		}

	    printer.print("Subject to :	");
	    if (isToBreakLineOnObjectFunction()) {
	    	printer.println();
	    }
		
		// tableIndex is the index for w; globalCellIndex is the index for n (i.e. the index of a cell in a table, but it does not reset from table to table)
		for (int tableIndex = 0, globalCellIndex = 0; tableIndex < allTables.size(); tableIndex++) {
			PotentialTable currentTable = allTables.get(tableIndex);
			
			
			// x[1] + x[2]  = n[1]/total
			// Note: x = getProbabilityVariablePrefix(), n = getAuxiliaryTableWeightSymbol(), total = getPrimaryTableWeightSymbol()
			for (int cellIndex = 0; cellIndex < currentTable.tableSize() ; cellIndex++, globalCellIndex++) {
				// cell index is like a translation of globalCellIndex to an index in current table
				
				String currentConstraint = "";
				
				// coord represents the states (of the variables) associated with current cell in current table
				int[] coord = currentTable.getMultidimensionalCoord(cellIndex);
				
				// is1stP is whether this is the 1st probability in row (it's going to be used to decide whether to append a "+" before p)
				boolean is1stP = true;	// this is also used to check if there was at least one x[i] in current constraint
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
					
					// x[1] + x[2]
					if (found && !getJointIndexesToIgnore().contains(jointCellIndex)) {
						if (!is1stP) {
							currentConstraint += (" +");
						}
						currentConstraint += (" " + getProbabilityVariablePrefix() + "[" + (jointCellIndex+1) + "]");
						is1stP = false;
					}
					
				}
				
				// = n[1]/total
				currentConstraint += (" = " + getAuxiliaryTableWeightSymbol() + "[" + (globalCellIndex+1) + "] / " + getPrimaryTableWeightSymbol() + " ;");
				
				if (!is1stP) {
					printer.print(currentConstraint);
					if (isToBreakLineOnObjectFunction()) {
						printer.println();
					}
				}
			}
		}
		
		return output.toString();
	}
	

	/**
	 * Performs the same of superclass' method, but it does not print weights.
	 * @see utils.EuclideanDistanceObjFunctionPrinter#getCountTableDescription(java.util.List, java.util.List)
	 */
	public String getCountTableDescription(List<PotentialTable> primaryTables, List<PotentialTable> auxiliaryTables) {
		String backup = getPrimaryTableWeightSymbol();
		String ret = "";
		try {
			setPrimaryTableWeightSymbol("");
			ret = super.getCountTableDescription(primaryTables, auxiliaryTables);
		} catch (RuntimeException e) {
			setPrimaryTableWeightSymbol(backup);
			throw e;
		}
		setPrimaryTableWeightSymbol(backup);
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
		options.addOption("count","count-alert", true, "How many categories to be active in order to trigger alert.");
		options.addOption("category","detector-category", true, "Comma-separated list of detectors to be included in a single category. "
				+ "Detectors in a single category will only contribute 1 time to the trigger of alert "
				+ "(i.e. regardless how many detectors there are, if they are in the same category they will be counted as 1 for the alert trigger)");
		
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
		
		EntropyConstraintsPrinter printer = new EntropyConstraintsPrinter();
		
		
		
		
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
		if (cmd.hasOption("count")) {
			printer.setCountAlert(Integer.parseInt(cmd.getOptionValue("count")));
		}
		if (cmd.hasOption("category")) {
			printer.parseCategories(cmd.getOptionValues("category"));
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
