package utils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;
import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 */

/**
 * @author Shou Matsumoto
 *
 */
public class ExpectationPrinter extends ObjFunctionPrinter {

	private String defaultJointProbabilityInputFileName = "weights";
	
	public static final int DEFAULT_NUM_POPULATION_CORRELATION_TABLE = 4267;

	public static final int DEFAULT_NUM_POPULATION_RCP_TABLE = 3804;
	
	private String probabilityColumnName = "NLP OUT";
	
	private String[] columnsToIgnore = {"P"};

	private boolean isToPrintExpectationTable = true;

	private boolean is1stLineForNames = false;

	private int defaultIndexOfProbability = 0;

	private boolean isToUseAverageAsExpected = true;

	/**
	 * 
	 */
	public ExpectationPrinter() {
		this.setToPrintJointProbabilityDescription(false);
	}
	
	

	/**
	 * 
	 * @param jointTable
	 * @param file
	 * @throws IOException 
	 */
	public PotentialTable getJointProbabilityFromFile(File file) throws IOException {
		
		CSVReader reader = new CSVReader(new FileReader(file));
		
		
		// read csv file line-by-line
		
		// prepare index of columns to be ignored
		List<String> namesToIgnore = Collections.EMPTY_LIST;
		String[] namesToIgnoreArray = getColumnsToIgnore();
		if (namesToIgnoreArray != null && namesToIgnoreArray.length > 0) {
			namesToIgnore = Arrays.asList(namesToIgnoreArray);
		}
		List<Integer> columnIndexesToIgnore = new ArrayList<Integer>();
		
		
		List<String> varNames = null;
		
		// read the 1st line of csv (name of the columns)
		String[] csvLine = null;
		int indexOfProbability = getDefaultIndexOfProbability();
		
		if (is1stLineForNames()) {
			varNames = new ArrayList<String>();
			csvLine = reader.readNext();
			indexOfProbability = csvLine.length-1;	// assuming by default to be the last column
			for (int column = 0; column < csvLine.length; column++) {
				String name = csvLine[column];
				if (namesToIgnore.contains(name)) {
					columnIndexesToIgnore.add(column);
					continue;
				}
				
				if (name.equals(getProbabilityColumnName())) {
					indexOfProbability = column;
					continue;
				}
				
				varNames.add(name);
			}
		}
		
		if (varNames ==null) {
			varNames = getNameList(getIndicatorNames());
			varNames.add(0, getThreatName());
			if (isToConsiderDetectors()) {
				varNames.addAll(getNameList(getDetectorNames()));
			}
		}
		
		PotentialTable jointTable = super.getJointTable(null, varNames);
		
		// read the remaining file
		int cellsRead = 0;
		for (csvLine = reader.readNext(); csvLine != null; csvLine = reader.readNext()) {
			if (cellsRead >= jointTable.tableSize()) {
				break;
			}
			float value = 0;
			if (csvLine.length > indexOfProbability) {
				try {
					value = Float.parseFloat(csvLine[(indexOfProbability>=0)?indexOfProbability:(csvLine.length-1)]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			jointTable.setValue(cellsRead, value);
			cellsRead++;
		}
		if (cellsRead != jointTable.tableSize()) {
			reader.close();
			throw new RuntimeException("Cells read = " + cellsRead + ", expected = " + jointTable.tableSize());
		}
		
		reader.close();
		
		
		return jointTable;
	}

	/**
	 * 
	 * @param file
	 * @param numPopulationCorrelation
	 * @param numPopulationRCP
	 * @param isToPrintChiSquareHeader
	 * @throws IOException
	 */
	public void printExpectationFromFile(File file, 
			int numPopulationCorrelation, int numPopulationRCP, boolean isToPrintChiSquareHeader) throws IOException {
		
		// if it is a directory, read all files in the directory
		if (file.isDirectory()) {
			List<File> files = new ArrayList<File>(Arrays.asList(file.listFiles()));
			Collections.sort(files, new Comparator<File>() {
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (File internalFile : files) {
				this.printExpectationFromFile(internalFile, numPopulationCorrelation, numPopulationRCP, isToPrintChiSquareHeader);
				isToPrintChiSquareHeader = false;	// don't print the header next time
			}
			return;
		}
		
		PotentialTable jointTable = this.getJointProbabilityFromFile(file);
		
		Map<String, INode> variableMap = new HashMap<String, INode>();
		for (int i = 0; i < jointTable.getVariablesSize(); i++) {
			variableMap.put(jointTable.getVariableAt(i).getName(), jointTable.getVariableAt(i));
		}
		
		List<String> indicatorNameList = this.getNameList(getIndicatorNames());
		List<String> detectorNameList = this.getNameList(getDetectorNames());
		
		List<PotentialTable> correlationTables = null;
		if (getPrimaryTableDirectoryName() != null &&  !getPrimaryTableDirectoryName().trim().isEmpty()) {
			correlationTables = this.getTablesFromNetFile(variableMap , new File(getPrimaryTableDirectoryName()));
		} else {
			correlationTables = this.getCorrelationTables(variableMap , null, indicatorNameList);
			correlationTables.addAll(this.getCorrelationTables(variableMap , null, detectorNameList));
		}
		
		List<PotentialTable> rcpTables = null;
		if (getAuxiliaryTableDirectoryName() != null &&  !getAuxiliaryTableDirectoryName().trim().isEmpty()) {
			rcpTables = this.getTablesFromNetFile(variableMap , new File(getAuxiliaryTableDirectoryName()));
		} else {
			rcpTables = this.getThreatTables(variableMap , null, indicatorNameList, getThreatName());
			rcpTables.addAll(this.getDetectorTables(variableMap, null, indicatorNameList, detectorNameList));
		}
		
		List<PotentialTable> tables = new ArrayList<PotentialTable>(correlationTables) ;
		tables.addAll(rcpTables);
		
		// get probabilities of each smaller tables
		this.fillTablesFromJointProbability(tables, jointTable);
		
		if (isToPrintExpectationTable() || isToPrintJointProbabilityDescription()) {
			System.out.println("File:" + file.getName());
			System.out.println();
		}
		
		if (isToPrintJointProbabilityDescription()) {
			System.out.println("Probabilities:");
			// print the probabilities in proper format
			this.printFormattedTables(tables);
			System.out.println();
			System.out.println();
		}
		
		
		// get expectation
		for (PotentialTable table : correlationTables) {
			for (int i = 0; i < table.tableSize(); i++) {
				table.setValue(i, table.getValue(i) * numPopulationCorrelation);
			}
		}
		for (PotentialTable table : rcpTables) {
			for (int i = 0; i < table.tableSize(); i++) {
				table.setValue(i, table.getValue(i) * numPopulationRCP);
			}
		}
		
		if (isToPrintExpectationTable()) {
			System.out.println("Expected:");
			// print the expectation table
			this.printFormattedTables(tables);
			System.out.println();
		}
		
		
		List<PotentialTable> originalCorrelationTables = null;
		if (getPrimaryTableDirectoryName() != null &&  !getPrimaryTableDirectoryName().trim().isEmpty()) {
			originalCorrelationTables = this.getTablesFromNetFile(variableMap , new File(getPrimaryTableDirectoryName()));
		} else {
			originalCorrelationTables = this.getCorrelationTables(variableMap, indicatorCorrelations, indicatorNameList);
			originalCorrelationTables.addAll(this.getCorrelationTables(variableMap, detectorCorrelations, detectorNameList));
		}
		
		List<PotentialTable> originalRCPTables = null;
		if (getAuxiliaryTableDirectoryName() != null &&  !getAuxiliaryTableDirectoryName().trim().isEmpty()) {
			originalRCPTables = this.getTablesFromNetFile(variableMap , new File(getAuxiliaryTableDirectoryName()));
		} else {
			originalRCPTables = this.getThreatTables(variableMap , threatIndicatorMatrix, indicatorNameList, getThreatName());
			originalRCPTables.addAll(this.getDetectorTables(variableMap, detectorIndicatorMatrix, indicatorNameList, detectorNameList));
		}
		
		if (correlationTables.size() != originalCorrelationTables.size()) {
			throw new IllegalArgumentException("Number of correlation tables did not match.");
		}
		if (rcpTables.size() != originalRCPTables.size()) {
			throw new IllegalArgumentException("Total number of threat-indicator and indicator-detector tables did not match.");
		}
		
		if (isToPrintChiSquareHeader) {
			System.out.println("File,Var1,Var2,ChiSquare,sumChiSqure");
		}
		
		List<List<String>> temp = new ArrayList<List<String>>();
		double sumChiSquares = 0;
		for (int i = 0; i < correlationTables.size(); i++) {
			List<String> line = new ArrayList<String>();
			PotentialTable expected = originalCorrelationTables.get(i);
			PotentialTable observed = correlationTables.get(i);
			
			if (expected.variableCount() != observed.variableCount()) {
				throw new IllegalArgumentException("Number of variables in table did not match: " + observed + " ; " + expected);
			}
			if (expected.tableSize() != observed.tableSize()) {
				throw new IllegalArgumentException("Size of table did not match: " + observed + " = " + observed.tableSize() 
						+ "; " + expected + " = " + expected.tableSize());
			}
			
			line.add(file.getName());
			
			for (int j = expected.variableCount()-1; j >= 0; j--) {
				if (observed.getVariableIndex((Node) expected.getVariableAt(j)) < 0) {
					throw new IllegalArgumentException(expected.getVariableAt(j) + " not found in observed table: " + observed + " ; " + expected);
				}
				line.add(expected.getVariableAt(j).getName());
			}
			double chiSquare = this.getChiSqure(observed,expected, isToUseAverageAsExpected());
			sumChiSquares += chiSquare;
			line.add(""+chiSquare);
			temp.add(line);
		}
		for (List<String> list : temp) {
			for (String string : list) {
				System.out.print(string + ",");
			}
			System.out.println(sumChiSquares + ",");
		}
		
		temp = new ArrayList<List<String>>();
		sumChiSquares = 0;
		for (int i = 0; i < rcpTables.size(); i++) {
			List<String> line = new ArrayList<String>();
			PotentialTable expected = originalRCPTables.get(i);
			PotentialTable observed = rcpTables.get(i);
			
			if (expected.variableCount() != observed.variableCount()) {
				throw new IllegalArgumentException("Number of variables in table did not match: " + observed + " ; " + expected);
			}
			if (expected.tableSize() != observed.tableSize()) {
				throw new IllegalArgumentException("Size of table did not match: " + observed + " = " + observed.tableSize() 
						+ "; " + expected + " = " + expected.tableSize());
			}
			line.add(file.getName());
			for (int j = expected.variableCount()-1; j >= 0; j--) {
				if (observed.getVariableIndex((Node) expected.getVariableAt(j)) < 0) {
					throw new IllegalArgumentException(expected.getVariableAt(j) + " not found in observed table: " + observed + " ; " + expected);
				}
				line.add(expected.getVariableAt(j).getName());
			}
			double chiSquare = this.getChiSqure(observed,expected, isToUseAverageAsExpected());
			sumChiSquares += chiSquare;
			line.add(""+chiSquare);
			temp.add(line);
		}
		for (List<String> list : temp) {
			for (String string : list) {
				System.out.print(string + ",");
			}
			System.out.println(sumChiSquares + ",");
		}
		
	}
	
	/**
	 * 
	 * @param tables
	 */
	public void printFormattedTables(List<PotentialTable> tables) {
		for (PotentialTable table : tables) {
			this.printFormattedTable(table);
			System.out.println();
		}
	}



	public void printFormattedTable(PotentialTable table) {
		if (table.getVariablesSize() > 2) {
			throw new IllegalArgumentException("Current version of this method cannot print table with more than 2 variables");
		}
		if (table.tableSize() > 4) {
			throw new IllegalArgumentException("Current version of this method cannot print tables larger than 4 cells");
		}
		
		System.out.println(" , ," + table.getVariableAt(0).getName() + "," + table.getVariableAt(0).getName());
		System.out.println(" , ," + table.getVariableAt(0).getStateAt(0) + "," + table.getVariableAt(0).getStateAt(1));
		System.out.println(table.getVariableAt(1).getName() + "," + table.getVariableAt(1).getStateAt(0) + "," + table.getValue(0) + "," + table.getValue(1));
		System.out.println(table.getVariableAt(1).getName() + "," + table.getVariableAt(1).getStateAt(1) + "," + table.getValue(2) + "," + table.getValue(3));
	}



	/**
	 * 
	 * @param observed
	 * @param expected
	 * @param isToUseAverageAsExpectedValue
	 * @return
	 */
	public double getChiSqure(PotentialTable observed, PotentialTable expected, boolean isToUseAverageAsExpectedValue) {
		if (observed.tableSize() != expected.tableSize()) {
			throw new IllegalArgumentException("Tables differ in size: " + observed.tableSize() + " != " + expected.tableSize());
		}
		for (int i = 0; i < expected.variableCount(); i++) {
			if (observed.getVariableIndex((Node) expected.getVariableAt(i)) < 0) {
				throw new IllegalArgumentException(expected.getVariableAt(i) + " not found observedn table.");
			}
		}
		
		double sum = 0;
		
		for (int cell = 0; cell < expected.tableSize(); cell++) {
			// extract coordinates (configuration of states at current cell) of expected table
			int[] expectedCoord = expected.getMultidimensionalCoord(cell);
			
			// convert coordinates of expected table to coordinates of observed table
			int[] observedCoord = observed.getMultidimensionalCoord(0);
			if (expectedCoord.length != observedCoord.length) {
				throw new RuntimeException();
			}
			for (int stateIndex = 0; stateIndex < observedCoord.length; stateIndex++) {
				observedCoord[stateIndex] = expectedCoord[expected.getVariableIndex((Node) observed.getVariableAt(stateIndex))];
			}
			
			double temp = Double.NaN;
			if (isToUseAverageAsExpectedValue) {
				double average = (observed.getValue(observedCoord) + expected.getValue(expectedCoord)) / 2;
				temp = (observed.getValue(observedCoord) - average);
				temp /= average;	// divide first, to avoid overflow
				temp *= (observed.getValue(observedCoord) - average);
				double temp2 = (expected.getValue(observedCoord) - average);
				temp2 /= average;	// divide first, to avoid overflow
				temp2 *= (expected.getValue(observedCoord) - average);
				temp += temp2;
			} else {
				temp = (observed.getValue(observedCoord) - expected.getValue(expectedCoord));
				temp /= expected.getValue(expectedCoord);	// divide first, to avoid overflow
				temp *= (observed.getValue(observedCoord) - expected.getValue(expectedCoord));
			}
			
			sum += temp;
			
			
		}
		
		return sum;
	}



	/**
	 * 
	 * @param tables
	 * @param jointTable
	 */
	public void fillTablesFromJointProbability(List<PotentialTable> tables, PotentialTable jointTable) {
		for (PotentialTable currentTable : tables) {
			// marginalize joint table
			PotentialTable marginalTable = jointTable.getTemporaryClone();
			for (int varIndex = 0; varIndex < marginalTable.variableCount(); varIndex++) {
				if (currentTable.getVariableIndex((Node) marginalTable.getVariableAt(varIndex)) < 0) {
					// remove var which is in joint table and not in current table
					marginalTable.removeVariable(marginalTable.getVariableAt(varIndex), false);
					varIndex--;
				}
			}
			for (int i = 0; i < currentTable.tableSize(); i++) {
				int[] currentCoord = currentTable.getMultidimensionalCoord(i);
				int[] marginalTableCoord = marginalTable.getMultidimensionalCoord(0);
				if (currentCoord.length != marginalTableCoord.length) {
					throw new RuntimeException();
				}
				for (int marginalTableStateIndex = 0; marginalTableStateIndex < marginalTableCoord.length; marginalTableStateIndex++) {
					marginalTableCoord[marginalTableStateIndex] = currentCoord[currentTable.getVariableIndex((Node) marginalTable.getVariableAt(marginalTableStateIndex))];
				}
				currentTable.setValue(i, marginalTable.getValue(marginalTableCoord));
			}
		}
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("id","problem-id", true, "Name or identification of the current problem (this will be used as suffixes of output file names).");
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("i","input", true, "File or directory to get joint probabilities from.");
//		options.addOption("o","output", true, "File or directory to write results.");
		options.addOption("c","correlation-num", true, "Size of population in correlation table.");
		options.addOption("r","rcp-num", true, "Size of population in tables in RCP problem document.");
		options.addOption("h","help", false, "Help.");
		options.addOption("f","full", false, "Use full domain size (includes detector) instead of using a subset (only indicators).");
		options.addOption("e","expectation", false, "Print expectation table as well.");
		options.addOption("primary","primary-tables", true, "Where to read primary tables from.");
		options.addOption("aux","auxiliary-tables", true, "Where to read primary tables from.");
		options.addOption("inames","indicator-names", true, "Comma-separated names of indicators.");
		options.addOption("dnames","detector-names", true, "Comma-separated names of detectors.");
		options.addOption("threat","threat-name", true, "Name of threat variable.");
		
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
			System.out.println("-i <FILE NAME> : file or directory containing csv files of joint probabilities.");
			System.out.println("-id <SOME NAME> : Name or identification of the current problem (e.g. \"Users_RCP1\", \"Users_RCP2\", or \"Users_RCP3\"). "
					+ "This will be used as suffixes of output file names");
			System.out.println("-d : Enables debug mode.");
			System.out.println("-c : size of population in correlation table.");
			System.out.println("-r : size of population in tables in RCP problem document.");
			System.out.println("-f : use full domain size (includes detector) instead of using a subset (only indicators).");
			System.out.println("-e : print expectation table as well.");
			System.out.println("-primary : where to read primary tables from.");
			System.out.println("-aux : where to read auxiliary tables from.");
			System.out.println("-inames : Comma-separated names of indicators.");
			System.out.println("-dnames : Comma-separated names of detectors.");
			System.out.println("-threat : Name of threat variable.");
			System.out.println("-h: Help.");
			return;
		}
		
		if (cmd.hasOption("d")) {
			Debug.setDebug(true);
		} else {
			Debug.setDebug(false);
		}
		
		
		
		ExpectationPrinter printer = new ExpectationPrinter();
		if (cmd.hasOption("f")) {
			printer.setToConsiderDetectors(true);
		}
		if (cmd.hasOption("id")) {
			printer.setProblemID(cmd.getOptionValue("id"));
		}
		if (cmd.hasOption("i")) {
			printer.setDefaultJointProbabilityInputFileName(cmd.getOptionValue("i"));
		}
		
		int numPopulationCorrelationTable = DEFAULT_NUM_POPULATION_CORRELATION_TABLE;
		if (cmd.hasOption("c")) {
			numPopulationCorrelationTable = Integer.parseInt(cmd.getOptionValue("c"));
		}
		int numPopulationRCPTable = DEFAULT_NUM_POPULATION_RCP_TABLE;
		if (cmd.hasOption("r")) {
			numPopulationCorrelationTable = Integer.parseInt(cmd.getOptionValue("r"));
		}
		
		printer.setToPrintExpectationTable(cmd.hasOption("e"));
		

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
			printer.printExpectationFromFile(new File(printer.getDefaultJointProbabilityInputFileName()), 
					numPopulationCorrelationTable, numPopulationRCPTable, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	

	/**
	 * @return the defaultJointProbabilityInputFileName
	 */
	public String getDefaultJointProbabilityInputFileName() {
		return defaultJointProbabilityInputFileName;
	}

	/**
	 * @param defaultJointProbabilityInputFileName the defaultJointProbabilityInputFileName to set
	 */
	public void setDefaultJointProbabilityInputFileName(
			String defaultJointProbabilityInputFileName) {
		this.defaultJointProbabilityInputFileName = defaultJointProbabilityInputFileName;
	}





	/**
	 * @return the probabilityColumnName
	 */
	public String getProbabilityColumnName() {
		return probabilityColumnName;
	}



	/**
	 * @param probabilityColumnName the probabilityColumnName to set
	 */
	public void setProbabilityColumnName(String probabilityColumnName) {
		this.probabilityColumnName = probabilityColumnName;
	}



	/**
	 * @return the columnsToIgnore
	 */
	public String[] getColumnsToIgnore() {
		return columnsToIgnore;
	}



	/**
	 * @param columnsToIgnore the columnsToIgnore to set
	 */
	public void setColumnsToIgnore(String[] columnsToIgnore) {
		this.columnsToIgnore = columnsToIgnore;
	}



	/**
	 * @return the isToPrintExpectationTable
	 */
	public boolean isToPrintExpectationTable() {
		return isToPrintExpectationTable;
	}



	/**
	 * @param isToPrintExpectationTable the isToPrintExpectationTable to set
	 */
	public void setToPrintExpectationTable(boolean isToPrintExpectationTable) {
		this.isToPrintExpectationTable = isToPrintExpectationTable;
	}



	/**
	 * @return the is1stLineForNames
	 */
	public boolean is1stLineForNames() {
		return is1stLineForNames;
	}



	/**
	 * @param is1stLineForNames the is1stLineForNames to set
	 */
	public void set1stLineForNames(boolean is1stLineForNames) {
		this.is1stLineForNames = is1stLineForNames;
	}



	/**
	 * @return the defaultIndexOfProbability
	 */
	public int getDefaultIndexOfProbability() {
		return defaultIndexOfProbability;
	}



	/**
	 * @param defaultIndexOfProbability the defaultIndexOfProbability to set
	 */
	public void setDefaultIndexOfProbability(int defaultIndexOfProbability) {
		this.defaultIndexOfProbability = defaultIndexOfProbability;
	}



	/**
	 * @return the isToUseAverageAsExpected
	 */
	public boolean isToUseAverageAsExpected() {
		return isToUseAverageAsExpected;
	}



	/**
	 * @param isToUseAverageAsExpected the isToUseAverageAsExpected to set
	 */
	public void setToUseAverageAsExpected(boolean isToUseAverageAsExpected) {
		this.isToUseAverageAsExpected = isToUseAverageAsExpected;
	}

}
