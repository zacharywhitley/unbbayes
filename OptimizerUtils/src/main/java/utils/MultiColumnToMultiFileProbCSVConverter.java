/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This is an utility class which converts a single csv with multiple columns (rows represent joint state and columns are the probabilities)
 * to multiple files with single column.
 * @author Shou Matsumoto
 *
 */
public class MultiColumnToMultiFileProbCSVConverter extends ExpectationPrinter {

	private ExpectationPrinter delegator = new ExpectationPrinter();
	private String indicatorPrefix = "I";
	private String detectorPrefix = "D";
	
	/**
	 * Default constructor is protected to enable inheritance.
	 * @see #getInstance()
	 * @see #getInstance(ExpectationPrinter)
	 */
	protected MultiColumnToMultiFileProbCSVConverter() {
		setPrimaryTableDirectoryName("./output/");	// default output location
		setDefaultJointProbabilityInputFileName("./input.csv");;	// default input location
		set1stLineForNames(true);
		setProbabilityColumnName("");
		setProblemID("Probability");
		setThreatName("Threat");
		setAlertName("Alert");
	}
	
	/**
	 * Default constructor method
	 * @return new instance of MultiColumnToMultiFileProbCSVConverter
	 */
	public static MultiColumnToMultiFileProbCSVConverter getInstance() {
		return new MultiColumnToMultiFileProbCSVConverter();
	}
	
	/**
	 * Constructor method initializing fields
	 * @param delegator : calls to methods of {@link ExpectationPrinter} except {@link #printExpectationFromFile(File, int, int, boolean)} 
	 * will be delegated to this object. This allows changes in behaviors of this class in runtime.
	 * @return new instance of MultiColumnToMultiFileProbCSVConverter
	 * @see #setDelegator(ExpectationPrinter)
	 */
	public static MultiColumnToMultiFileProbCSVConverter getInstance(ExpectationPrinter delegator) {
		MultiColumnToMultiFileProbCSVConverter ret = new MultiColumnToMultiFileProbCSVConverter();
		ret.setDelegator(delegator);
		return ret;
	}
	

	/**
	 * Converts a CSV with multiple columns for probabilities to a set of CSVs
	 * with single column for probabilities.
	 * The output will be placed into {@link #getPrimaryTableDirectoryName()} directory.
	 * @param input : csv file where to read probabilities from. 1st row must be variable names, 
	 * initial columns after 1st row are variable states, and columns without var names
	 * are probabilities. If it is a directory, files in it will be read.
	 * @param directoryIndex : number to be used as suffix of output files.
	 * @param fileIndex : another number to be used as suffix of output files.
	 * @see utils.ExpectationPrinter#printExpectationFromFile(java.io.File, int, int, boolean)
	 * @see #getDefaultIndexOfProbability()
	 * @see #getJointProbabilityFromFile(File)
	 */
	public void printExpectationFromFile(File input, int directoryIndex, int fileIndex, boolean flag) throws IOException {
		if (input == null || !input.exists()) {
			throw new IllegalArgumentException("Invalid input file: " + input);
		}
		
		directoryIndex++;
		
		// if it is a directory, read all files in the directory
		if (input.isDirectory()) {
			List<File> files = new ArrayList<File>(Arrays.asList(input.listFiles()));
			for (File internalFile : files) {
				this.printExpectationFromFile(internalFile, directoryIndex, fileIndex++, flag);
			}
			return;
		}
		
		
		String directoryName = getPrimaryTableDirectoryName();
		if (directoryName == null || directoryName.trim().isEmpty()) {
			throw new IllegalArgumentException("Output directory was not specified");
		}
		
		File outputDir = new File(directoryName);
		if (!outputDir.exists() && !outputDir.mkdirs()) {
			throw new IOException("Unable to create directory " + directoryName);
		}
		
		if (!outputDir.isDirectory()) {
			throw new IOException(directoryName + " is not a valid directory.");
		}
		

//		BufferedReader reader = new BufferedReader(new FileReader(input));
//		CSVReader csvReader = new CSVReader(reader);
		CSVReader csvReader = new CSVReader(new FileReader(input));
		
		List<String> varNames = new ArrayList<String>();	// var to be filled by next method
		
		// read the header of csv
		if (readJointProbabilityHeader(csvReader, Collections.EMPTY_LIST, varNames) < 0) {
			throw new IllegalArgumentException("Unable to identify column for probability in file: " + input.getName());
		}
		// probabilities are declared in next rows after the last non-empty column of 1st row
		int indexOfProbability = varNames.size();	
		
		// reorder varNames so that the order is {Threat, Indicators, Detectors, Alert}. This will be used later when we write files
		List<String> varNamesSorted = new ArrayList<String>(varNames);
		Collections.sort(varNamesSorted, new Comparator<String>() {
			public int compare(String o1, String o2) {
				if (getThreatName().equalsIgnoreCase(o1)) {
					if (getThreatName().equalsIgnoreCase(o2)) {
						// o1 and o2 are threat
						return 0;
					} else {
						// o1 is threat and o2 is something else
						return -1;	// threats shall come to beginning
					}
				} else if (getThreatName().equalsIgnoreCase(o2)) {
					// o2 is a threat and o1 is something else
					return 1;	// threats shall come to beginning
				} else if (getAlertName().equalsIgnoreCase(o1)) {
					if (getAlertName().equalsIgnoreCase(o2)) {
						// o1 and o2 are alert
						return 0;
					} else {
						// o1 is alert and o2 is something else
						return 1;	// alerts shall go to the end
					}
				} else if (getAlertName().equalsIgnoreCase(o2)) {
					// o2 is alert and o1 is something else
					return -1;	// alerts shall go to the end
				} else if (o1.startsWith(getIndicatorPrefix())) {
					// o1 is an indicator
					if (o2.startsWith(getIndicatorPrefix())) {
						// o2 is also an indicator. Check number.
						int o1Number = Integer.parseInt(o1.substring(getIndicatorPrefix().length()));
						int o2Number = Integer.parseInt(o2.substring(getIndicatorPrefix().length()));
						return o1Number - o2Number;
					} else if (o2.startsWith(getDetectorPrefix())) {
						// o1 is indicator, o2 is detector
						return -1;	// indicators comes before detector
					}
				} else if (o2.startsWith(getIndicatorPrefix())) {
					// o2 is an indicator and o1 is something else
					if (o1.startsWith(getDetectorPrefix())) {
						// o2 is indicator, o1 is detector
						return 1;	// indicators comes before detector
					}
				} else if (o1.startsWith(getDetectorPrefix())) {
					// o1 is detector
					if (o2.startsWith(getDetectorPrefix())) {
						// o1 is detector, o2 is detector. Check number.
						int o1Number = Integer.parseInt(o1.substring(getIndicatorPrefix().length()));
						int o2Number = Integer.parseInt(o2.substring(getIndicatorPrefix().length()));
						return o1Number - o2Number;
					}
				}
				
				// all other cases are unknown
				
				throw new IllegalArgumentException(o1 + " or " + o2 + " are not valid variable names.");
			}
		});
		

		Map<String, INode> variableMap = new HashMap<String, INode>();	// keep track of what variables were created
		for (int numColumnsRead = 0; numColumnsRead < Integer.MAX_VALUE; numColumnsRead++) {
			
			PotentialTable potentialTable = null;
			try {
				potentialTable = readJointProbabilityBody(csvReader, indexOfProbability, varNames, is1stLineForNames(), variableMap);
			} catch (IndexOutOfBoundsException e) {
				Debug.println(getClass(), numColumnsRead + " probabilities read. Finished reading csv body in column: " + indexOfProbability , e);
				break;
			}
			indexOfProbability++;	// attempt to read next row in next iteration
			// return to 1st row in body in next iteration
			
			// TODO find a more efficient method for reseting file reader. I tried Reader#mark and Reader#reset but they are not efficient for buffered readers
			csvReader.close();
			csvReader = new CSVReader(new FileReader(input));
			// read the header of csv
			if (readJointProbabilityHeader(csvReader, Collections.EMPTY_LIST, null) < 0) {
				throw new IllegalArgumentException("Unable to identify column for probability in file: " + input.getName());
			}
			
			
			// write table to a file;
			
			// reorder variables so that it fits specification;
			PotentialTable reorderedTable = getJointTable(variableMap, varNamesSorted);
			if (reorderedTable.tableSize() != potentialTable.tableSize()) {
				throw new RuntimeException("Failed to create a table with sorted variables. Sorted table has size " 
						+ reorderedTable.tableSize() + ", table read from CSV has size " + potentialTable.tableSize());
			}
			// setting reorderedTable to zero and then adding potentialTable will copy values of potentialTable in proper cells of reorderedTable
			reorderedTable.fillTable(0f);
			reorderedTable.opTab(potentialTable, potentialTable.PLUS_OPERATOR);
			reorderedTable.normalize();
			
			// create a new file to print reordered table
			File output = new File(outputDir, getProblemID() + "_" + directoryIndex + "_" + fileIndex + "_" + numColumnsRead + ".csv");

			// prepare the stream to print results to
			PrintStream printer = new PrintStream(new FileOutputStream(output, false));	// do not append if file exists
			
			for (int tableIndex = 0; tableIndex < reorderedTable.tableSize(); tableIndex++) {
				printer.println(reorderedTable.getValue(tableIndex));
			}
			
			printer.close();
		}
		
		csvReader.close();
		
	}
	
	

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 * @see utils.ExpectationPrinter#getJointProbabilityFromFile(java.io.File)
	 */
	public PotentialTable getJointProbabilityFromFile(File file)
			throws IOException {
		return this.delegator.getJointProbabilityFromFile(file);
	}


	/**
	 * @param file
	 * @param isToConsiderAlert
	 * @return
	 * @throws IOException
	 * @see utils.ExpectationPrinter#getJointProbabilityFromFile(java.io.File, boolean)
	 */
	public PotentialTable getJointProbabilityFromFile(File file,
			boolean isToConsiderAlert) throws IOException {
		return this.delegator.getJointProbabilityFromFile(file,
				isToConsiderAlert);
	}


	/**
	 * @param variableMap
	 * @param vars
	 * @return
	 * @see utils.ObjFunctionPrinter#getJointTable(java.util.Map, java.util.List)
	 */
	public PotentialTable getJointTable(Map<String, INode> variableMap,
			List<String> vars) {
		return this.delegator.getJointTable(variableMap, vars);
	}


	/**
	 * @param variableMap
	 * @param matrix
	 * @param columnName
	 * @param rowName
	 * @return
	 * @see utils.ObjFunctionPrinter#getPotentialTable(java.util.Map, int[][], java.lang.String, java.lang.String)
	 */
	public PotentialTable getPotentialTable(Map<String, INode> variableMap,
			int[][] matrix, String columnName, String rowName) {
		return this.delegator.getPotentialTable(variableMap, matrix,
				columnName, rowName);
	}

	/**
	 * @param variableMap
	 * @param varNames
	 * @return
	 * @see utils.ObjFunctionPrinter#getEmptyPotentialTable(java.util.Map, java.util.List)
	 */
	public PotentialTable getEmptyPotentialTable(
			Map<String, INode> variableMap, List<String> varNames) {
		return this.delegator.getEmptyPotentialTable(variableMap, varNames);
	}

	/**
	 * @param variableMap
	 * @param correlations
	 * @param names
	 * @return
	 * @see utils.ObjFunctionPrinter#getCorrelationTables(java.util.Map, int[][][], java.util.List)
	 */
	public List<PotentialTable> getCorrelationTables(
			Map<String, INode> variableMap, int[][][] correlations,
			List<String> names) {
		return this.delegator.getCorrelationTables(variableMap, correlations,
				names);
	}

	/**
	 * @param variableMap
	 * @param file
	 * @return
	 * @throws IOException
	 * @see utils.ObjFunctionPrinter#getTablesFromNetFile(java.util.Map, java.io.File)
	 */
	public List<PotentialTable> getTablesFromNetFile(
			Map<String, INode> variableMap, File file) throws IOException {
		return this.delegator.getTablesFromNetFile(variableMap, file);
	}

	/**
	 * @param tables
	 * @see utils.ExpectationPrinter#printFormattedTables(java.util.List)
	 */
	public void printFormattedTables(List<PotentialTable> tables) {
		this.delegator.printFormattedTables(tables);
	}

	/**
	 * @param table
	 * @see utils.ExpectationPrinter#printFormattedTable(unbbayes.prs.bn.PotentialTable)
	 */
	public void printFormattedTable(PotentialTable table) {
		this.delegator.printFormattedTable(table);
	}

	/**
	 * @param variableMap
	 * @param tables
	 * @param indicatorNames
	 * @param threatName
	 * @return
	 * @see utils.ObjFunctionPrinter#getThreatTables(java.util.Map, int[][][], java.util.List, java.lang.String)
	 */
	public List<PotentialTable> getThreatTables(Map<String, INode> variableMap,
			int[][][] tables, List<String> indicatorNames, String threatName) {
		return this.delegator.getThreatTables(variableMap, tables,
				indicatorNames, threatName);
	}

	/**
	 * @param observed
	 * @param expected
	 * @param isToUseAverageAsExpectedValue
	 * @return
	 * @see utils.ExpectationPrinter#getChiSqure(unbbayes.prs.bn.PotentialTable, unbbayes.prs.bn.PotentialTable, boolean)
	 */
	public double getChiSqure(PotentialTable observed, PotentialTable expected,
			boolean isToUseAverageAsExpectedValue) {
		return this.delegator.getChiSqure(observed, expected,
				isToUseAverageAsExpectedValue);
	}

	/**
	 * @param variableMap
	 * @param tables
	 * @param indicatorNameList
	 * @param detectorNameList
	 * @return
	 * @see utils.ObjFunctionPrinter#getDetectorTables(java.util.Map, int[][][], java.util.List, java.util.List)
	 */
	public List<PotentialTable> getDetectorTables(
			Map<String, INode> variableMap, int[][][] tables,
			List<String> indicatorNameList, List<String> detectorNameList) {
		return this.delegator.getDetectorTables(variableMap, tables,
				indicatorNameList, detectorNameList);
	}

	/**
	 * @param tables
	 * @param jointTable
	 * @see utils.ExpectationPrinter#fillTablesFromJointProbability(java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public void fillTablesFromJointProbability(List<PotentialTable> tables,
			PotentialTable jointTable) {
		this.delegator.fillTablesFromJointProbability(tables, jointTable);
	}

	/**
	 * @param jointTable
	 * @return
	 * @see utils.ObjFunctionPrinter#getJointTableDescription(unbbayes.prs.bn.PotentialTable)
	 */
	public String getJointTableDescription(PotentialTable jointTable) {
		return this.delegator.getJointTableDescription(jointTable);
	}

	/**
	 * @param primaryTables
	 * @param auxiliaryTables
	 * @param jointTable
	 * @return
	 * @see utils.ObjFunctionPrinter#getObjFunction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String getObjFunction(List<PotentialTable> primaryTables,
			List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		return this.delegator.getObjFunction(primaryTables, auxiliaryTables,
				jointTable);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#getDefaultJointProbabilityInputFileName()
	 */
	public String getDefaultJointProbabilityInputFileName() {
		return this.delegator.getDefaultJointProbabilityInputFileName();
	}

	/**
	 * @param defaultJointProbabilityInputFileName
	 * @see utils.ExpectationPrinter#setDefaultJointProbabilityInputFileName(java.lang.String)
	 */
	public void setDefaultJointProbabilityInputFileName(
			String defaultJointProbabilityInputFileName) {
		this.delegator
				.setDefaultJointProbabilityInputFileName(defaultJointProbabilityInputFileName);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#getProbabilityColumnName()
	 */
	public String getProbabilityColumnName() {
		return this.delegator.getProbabilityColumnName();
	}

	/**
	 * @param probabilityColumnName
	 * @see utils.ExpectationPrinter#setProbabilityColumnName(java.lang.String)
	 */
	public void setProbabilityColumnName(String probabilityColumnName) {
		this.delegator.setProbabilityColumnName(probabilityColumnName);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#getColumnsToIgnore()
	 */
	public String[] getColumnsToIgnore() {
		return this.delegator.getColumnsToIgnore();
	}

	/**
	 * @param columnsToIgnore
	 * @see utils.ExpectationPrinter#setColumnsToIgnore(java.lang.String[])
	 */
	public void setColumnsToIgnore(String[] columnsToIgnore) {
		this.delegator.setColumnsToIgnore(columnsToIgnore);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#isToPrintExpectationTable()
	 */
	public boolean isToPrintExpectationTable() {
		return this.delegator.isToPrintExpectationTable();
	}

	/**
	 * @param isToPrintExpectationTable
	 * @see utils.ExpectationPrinter#setToPrintExpectationTable(boolean)
	 */
	public void setToPrintExpectationTable(boolean isToPrintExpectationTable) {
		this.delegator.setToPrintExpectationTable(isToPrintExpectationTable);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#is1stLineForNames()
	 */
	public boolean is1stLineForNames() {
		return this.delegator.is1stLineForNames();
	}

	/**
	 * @param is1stLineForNames
	 * @see utils.ExpectationPrinter#set1stLineForNames(boolean)
	 */
	public void set1stLineForNames(boolean is1stLineForNames) {
		this.delegator.set1stLineForNames(is1stLineForNames);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#getDefaultIndexOfProbability()
	 */
	public int getDefaultIndexOfProbability() {
		return this.delegator.getDefaultIndexOfProbability();
	}

	/**
	 * @param defaultIndexOfProbability
	 * @see utils.ExpectationPrinter#setDefaultIndexOfProbability(int)
	 */
	public void setDefaultIndexOfProbability(int defaultIndexOfProbability) {
		this.delegator.setDefaultIndexOfProbability(defaultIndexOfProbability);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#isToUseAverageAsExpected()
	 */
	public boolean isToUseAverageAsExpected() {
		return this.delegator.isToUseAverageAsExpected();
	}

	/**
	 * @param isToUseAverageAsExpected
	 * @see utils.ExpectationPrinter#setToUseAverageAsExpected(boolean)
	 */
	public void setToUseAverageAsExpected(boolean isToUseAverageAsExpected) {
		this.delegator.setToUseAverageAsExpected(isToUseAverageAsExpected);
	}

	/**
	 * @return
	 * @see utils.ExpectationPrinter#isToPrintChiSquare()
	 */
	public boolean isToPrintChiSquare() {
		return this.delegator.isToPrintChiSquare();
	}

	/**
	 * @param isToPrintChiSquare
	 * @see utils.ExpectationPrinter#setToPrintChiSquare(boolean)
	 */
	public void setToPrintChiSquare(boolean isToPrintChiSquare) {
		this.delegator.setToPrintChiSquare(isToPrintChiSquare);
	}

	/**
	 * @param primaryTables
	 * @param auxiliaryTables
	 * @param jointTable
	 * @return
	 * @see utils.ObjFunctionPrinter#get1WayLikelihoodSubtraction(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable)
	 */
	public String get1WayLikelihoodSubtraction(
			List<PotentialTable> primaryTables,
			List<PotentialTable> auxiliaryTables, PotentialTable jointTable) {
		return this.delegator.get1WayLikelihoodSubtraction(primaryTables,
				auxiliaryTables, jointTable);
	}

	/**
	 * @param tables
	 * @return
	 * @see utils.ObjFunctionPrinter#getMarginalCounts(java.util.List)
	 */
	public Map<String, int[]> getMarginalCounts(List<PotentialTable> tables) {
		return this.delegator.getMarginalCounts(tables);
	}

	/**
	 * @param correlationTables
	 * @param threatTables
	 * @param jointTable
	 * @param isStrictlyGreater
	 * @param value
	 * @return
	 * @see utils.ObjFunctionPrinter#getNonZeroRestrictions(java.util.List, java.util.List, unbbayes.prs.bn.PotentialTable, boolean, float)
	 */
	public String getNonZeroRestrictions(
			List<PotentialTable> correlationTables,
			List<PotentialTable> threatTables, PotentialTable jointTable,
			boolean isStrictlyGreater, float value) {
		return this.delegator.getNonZeroRestrictions(correlationTables,
				threatTables, jointTable, isStrictlyGreater, value);
	}

	/**
	 * @param indicatorCorrelations
	 * @param detectorCorrelations
	 * @param threatIndicatorMatrix
	 * @param detectorIndicatorMatrix
	 * @throws IOException
	 * @see utils.ObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][])
	 */
	public void printAll(int[][][] indicatorCorrelations,
			int[][][] detectorCorrelations, int[][][] threatIndicatorMatrix,
			int[][][] detectorIndicatorMatrix) throws IOException {
		this.delegator.printAll(indicatorCorrelations, detectorCorrelations,
				threatIndicatorMatrix, detectorIndicatorMatrix);
	}

	/**
	 * @param indicatorCorrelationsMatrix
	 * @param detectorCorrelationsMatrix
	 * @param threatIndicatorTableMatrix
	 * @param detectorIndicatorTableMatrix
	 * @param printer
	 * @throws IOException
	 * @see utils.ObjFunctionPrinter#printAll(int[][][], int[][][], int[][][], int[][][], java.io.PrintStream)
	 */
	public void printAll(int[][][] indicatorCorrelationsMatrix,
			int[][][] detectorCorrelationsMatrix,
			int[][][] threatIndicatorTableMatrix,
			int[][][] detectorIndicatorTableMatrix, PrintStream printer)
			throws IOException {
		this.delegator.printAll(indicatorCorrelationsMatrix,
				detectorCorrelationsMatrix, threatIndicatorTableMatrix,
				detectorIndicatorTableMatrix, printer);
	}

	/**
	 * @param variableMap
	 * @param varNames
	 * @return
	 * @see utils.ObjFunctionPrinter#getEmptyPotentialTables(java.util.Map, java.util.List)
	 */
	public List<PotentialTable> getEmptyPotentialTables(
			Map<String, INode> variableMap, List<List<String>> varNames) {
		return this.delegator.getEmptyPotentialTables(variableMap, varNames);
	}

	/**
	 * @param names
	 * @return
	 * @see utils.ObjFunctionPrinter#getNameList(java.lang.String[])
	 */
	public List<String> getNameList(String[] names) {
		return this.delegator.getNameList(names);
	}

	/**
	 * @param n
	 * @return
	 * @see utils.ObjFunctionPrinter#factorial(int)
	 */
	public int factorial(int n) {
		return this.delegator.factorial(n);
	}

	/**
	 * @param n
	 * @param k
	 * @return
	 * @see utils.ObjFunctionPrinter#combinatorial(int, int)
	 */
	public int combinatorial(int n, int k) {
		return this.delegator.combinatorial(n, k);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#isToBreakLineOnObjectFunction()
	 */
	public boolean isToBreakLineOnObjectFunction() {
		return this.delegator.isToBreakLineOnObjectFunction();
	}

	/**
	 * @param isToBreakLineOnObjectFunction
	 * @see utils.ObjFunctionPrinter#setToBreakLineOnObjectFunction(boolean)
	 */
	public void setToBreakLineOnObjectFunction(
			boolean isToBreakLineOnObjectFunction) {
		this.delegator
				.setToBreakLineOnObjectFunction(isToBreakLineOnObjectFunction);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#isToSubtract1WayLikelihood()
	 */
	public boolean isToSubtract1WayLikelihood() {
		return this.delegator.isToSubtract1WayLikelihood();
	}

	/**
	 * @param isToSubtract1WayLikelihood
	 * @see utils.ObjFunctionPrinter#setToSubtract1WayLikelihood(boolean)
	 */
	public void setToSubtract1WayLikelihood(boolean isToSubtract1WayLikelihood) {
		this.delegator.setToSubtract1WayLikelihood(isToSubtract1WayLikelihood);
	}

	/**
	 * @param jointTable
	 * @return
	 * @see utils.ObjFunctionPrinter#getJointProbsIndexesToIgnore(unbbayes.prs.bn.PotentialTable)
	 */
	public Collection<Integer> getJointProbsIndexesToIgnore(
			PotentialTable jointTable) {
		return this.delegator.getJointProbsIndexesToIgnore(jointTable);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getJointProbsIndexesToConsider()
	 */
	public Integer[] getJointProbsIndexesToConsider() {
		return this.delegator.getJointProbsIndexesToConsider();
	}

	/**
	 * @param jointProbsToConsider
	 * @see utils.ObjFunctionPrinter#setJointProbsIndexesToConsider(java.lang.Integer[])
	 */
	public void setJointProbsIndexesToConsider(Integer[] jointProbsToConsider) {
		this.delegator.setJointProbsIndexesToConsider(jointProbsToConsider);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#isToPrintJointProbabilityDescription()
	 */
	public boolean isToPrintJointProbabilityDescription() {
		return this.delegator.isToPrintJointProbabilityDescription();
	}

	/**
	 * @param isToPrintJointProbabilitySpecification
	 * @see utils.ObjFunctionPrinter#setToPrintJointProbabilityDescription(boolean)
	 */
	public void setToPrintJointProbabilityDescription(
			boolean isToPrintJointProbabilitySpecification) {
		if (delegator == null) {
			return;
		}
		this.delegator.setToPrintJointProbabilityDescription(isToPrintJointProbabilitySpecification);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getAuxiliaryTableWeightSymbol()
	 */
	public String getAuxiliaryTableWeightSymbol() {
		return this.delegator.getAuxiliaryTableWeightSymbol();
	}

	/**
	 * @param auxiliaryTableWeightSymbol
	 * @see utils.ObjFunctionPrinter#setAuxiliaryTableWeightSymbol(java.lang.String)
	 */
	public void setAuxiliaryTableWeightSymbol(String auxiliaryTableWeightSymbol) {
		this.delegator
				.setAuxiliaryTableWeightSymbol(auxiliaryTableWeightSymbol);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getPrimaryTableWeightSymbol()
	 */
	public String getPrimaryTableWeightSymbol() {
		return this.delegator.getPrimaryTableWeightSymbol();
	}

	/**
	 * @param primaryTableWeightSymbol
	 * @see utils.ObjFunctionPrinter#setPrimaryTableWeightSymbol(java.lang.String)
	 */
	public void setPrimaryTableWeightSymbol(String primaryTableWeightSymbol) {
		this.delegator.setPrimaryTableWeightSymbol(primaryTableWeightSymbol);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getProblemID()
	 */
	public String getProblemID() {
		return this.delegator.getProblemID();
	}

	/**
	 * @param problemID
	 * @see utils.ObjFunctionPrinter#setProblemID(java.lang.String)
	 */
	public void setProblemID(String problemID) {
		this.delegator.setProblemID(problemID);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#isToConsiderDetectors()
	 */
	public boolean isToConsiderDetectors() {
		return this.delegator.isToConsiderDetectors();
	}

	/**
	 * @param isToConsiderDetectors
	 * @see utils.ObjFunctionPrinter#setToConsiderDetectors(boolean)
	 */
	public void setToConsiderDetectors(boolean isToConsiderDetectors) {
		this.delegator.setToConsiderDetectors(isToConsiderDetectors);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getPrimaryTableSpecialCasesWeightSymbol()
	 */
	public Map<String, String> getPrimaryTableSpecialCasesWeightSymbol() {
		return this.delegator.getPrimaryTableSpecialCasesWeightSymbol();
	}

	/**
	 * @param primaryTableSpecialCasesWeightSymbol
	 * @see utils.ObjFunctionPrinter#setPrimaryTableSpecialCasesWeightSymbol(java.util.Map)
	 */
	public void setPrimaryTableSpecialCasesWeightSymbol(
			Map<String, String> primaryTableSpecialCasesWeightSymbol) {
		this.delegator
				.setPrimaryTableSpecialCasesWeightSymbol(primaryTableSpecialCasesWeightSymbol);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getAuxiliaryTableDirectoryName()
	 */
	public String getAuxiliaryTableDirectoryName() {
		return this.delegator.getAuxiliaryTableDirectoryName();
	}

	/**
	 * @param auxiliaryTableDirectoryName
	 * @see utils.ObjFunctionPrinter#setAuxiliaryTableDirectoryName(java.lang.String)
	 */
	public void setAuxiliaryTableDirectoryName(
			String auxiliaryTableDirectoryName) {
		this.delegator
				.setAuxiliaryTableDirectoryName(auxiliaryTableDirectoryName);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getPrimaryTableDirectoryName()
	 */
	public String getPrimaryTableDirectoryName() {
		return this.delegator.getPrimaryTableDirectoryName();
	}

	/**
	 * @param primaryTableDirectoryName
	 * @see utils.ObjFunctionPrinter#setPrimaryTableDirectoryName(java.lang.String)
	 */
	public void setPrimaryTableDirectoryName(String primaryTableDirectoryName) {
		this.delegator.setPrimaryTableDirectoryName(primaryTableDirectoryName);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getIndicatorNames()
	 */
	public String[] getIndicatorNames() {
		return this.delegator.getIndicatorNames();
	}

	/**
	 * @param indicatorNames
	 * @see utils.ObjFunctionPrinter#setIndicatorNames(java.lang.String[])
	 */
	public void setIndicatorNames(String[] indicatorNames) {
		this.delegator.setIndicatorNames(indicatorNames);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getDetectorNames()
	 */
	public String[] getDetectorNames() {
		return this.delegator.getDetectorNames();
	}

	/**
	 * @param detectorNames
	 * @see utils.ObjFunctionPrinter#setDetectorNames(java.lang.String[])
	 */
	public void setDetectorNames(String[] detectorNames) {
		this.delegator.setDetectorNames(detectorNames);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getThreatName()
	 */
	public String getThreatName() {
		return this.delegator.getThreatName();
	}

	/**
	 * @param threatName
	 * @see utils.ObjFunctionPrinter#setThreatName(java.lang.String)
	 */
	public void setThreatName(String threatName) {
		this.delegator.setThreatName(threatName);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getProbabilityVariablePrefix()
	 */
	public String getProbabilityVariablePrefix() {
		return this.delegator.getProbabilityVariablePrefix();
	}

	/**
	 * @param probabilityVariablePrefix
	 * @see utils.ObjFunctionPrinter#setProbabilityVariablePrefix(java.lang.String)
	 */
	public void setProbabilityVariablePrefix(String probabilityVariablePrefix) {
		this.delegator.setProbabilityVariablePrefix(probabilityVariablePrefix);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getAlertName()
	 */
	public String getAlertName() {
		return this.delegator.getAlertName();
	}

	/**
	 * @param alertName
	 * @see utils.ObjFunctionPrinter#setAlertName(java.lang.String)
	 */
	public void setAlertName(String alertName) {
		this.delegator.setAlertName(alertName);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getPrimaryTableVarNames()
	 */
	public List<List<String>> getPrimaryTableVarNames() {
		return this.delegator.getPrimaryTableVarNames();
	}

	/**
	 * @param primaryTableVarNames
	 * @see utils.ObjFunctionPrinter#setPrimaryTableVarNames(java.util.List)
	 */
	public void setPrimaryTableVarNames(List<List<String>> primaryTableVarNames) {
		this.delegator.setPrimaryTableVarNames(primaryTableVarNames);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getAuxiliaryTableVarNames()
	 */
	public List<List<String>> getAuxiliaryTableVarNames() {
		return this.delegator.getAuxiliaryTableVarNames();
	}

	/**
	 * @param auxiliaryTableVarNames
	 * @see utils.ObjFunctionPrinter#setAuxiliaryTableVarNames(java.util.List)
	 */
	public void setAuxiliaryTableVarNames(
			List<List<String>> auxiliaryTableVarNames) {
		this.delegator.setAuxiliaryTableVarNames(auxiliaryTableVarNames);
	}

	/**
	 * @return
	 * @see utils.ObjFunctionPrinter#getCountAlert()
	 */
	public int getCountAlert() {
		return this.delegator.getCountAlert();
	}

	/**
	 * @param countAlert
	 * @see utils.ObjFunctionPrinter#setCountAlert(int)
	 */
	public void setCountAlert(int countAlert) {
		this.delegator.setCountAlert(countAlert);
	}

	/**
	 * @return the delegator : methods of {@link ExpectationPrinter} except {@link #printExpectationFromFile(File, int, int, boolean)}
	 * will be delegated to this object.
	 */
	public ExpectationPrinter getDelegator() {
		return delegator;
	}

	/**
	 * @param delegator : methods of {@link ExpectationPrinter} except {@link #printExpectationFromFile(File, int, int, boolean)}
	 * will be delegated to this object.
	 */
	public void setDelegator(ExpectationPrinter delegator) {
		this.delegator = delegator;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("i","input", true, "File or directory to get joint probabilities from.");
		options.addOption("o","output", true, "Directory to write results.");
		options.addOption("id","problem-id", true, "Name or identification of the current problem (this will be used as prefixes of output file names).");
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
		
		MultiColumnToMultiFileProbCSVConverter converter = new MultiColumnToMultiFileProbCSVConverter();
		if (cmd.hasOption("i")) {
			converter.setDefaultJointProbabilityInputFileName(cmd.getOptionValue("i"));
		}
		if (cmd.hasOption("o")) {
			// use these attributes as directory where to output tables
			converter.setPrimaryTableDirectoryName(cmd.getOptionValue("o"));
//			converter.setAuxiliaryTableDirectoryName(cmd.getOptionValue("o"));
		}
		if (cmd.hasOption("id")) {
			converter.setProblemID(cmd.getOptionValue("id"));
		}
		
		try {
			converter.printExpectationFromFile(new File(converter.getDefaultJointProbabilityInputFileName()), 0, 0, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @return the indicatorPrefix
	 */
	public String getIndicatorPrefix() {
		return indicatorPrefix;
	}

	/**
	 * @param indicatorPrefix the indicatorPrefix to set
	 */
	public void setIndicatorPrefix(String indicatorPrefix) {
		this.indicatorPrefix = indicatorPrefix;
	}

	/**
	 * @return the detectorPrefix
	 */
	public String getDetectorPrefix() {
		return detectorPrefix;
	}

	/**
	 * @param detectorPrefix the detectorPrefix to set
	 */
	public void setDetectorPrefix(String detectorPrefix) {
		this.detectorPrefix = detectorPrefix;
	}

}
