/**
 * 
 */
package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;
import utils.ExpectationPrinter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiColumnCSVToMultiFileConverterImpl extends ExpectationPrinter implements IMultiColumnCSVToMultiFileConverter {

	private Comparator<? super String> varNameComparator;

	/**
	 * 
	 */
	protected MultiColumnCSVToMultiFileConverterImpl() {
		setPrimaryTableDirectoryName("./output/");	// default output location
		setDefaultJointProbabilityInputFileName("./input.csv");;	// default input location
		set1stLineForNames(true);
		setProbabilityColumnName("");
		setProblemID("Probability");
		setThreatName("Threat");
		setAlertName("Alert");
	}

	/**
	 * @param varNameComparator
	 */
	public MultiColumnCSVToMultiFileConverterImpl(Comparator<? super String> varNameComparator) {
		this();
		this.varNameComparator = varNameComparator;
	}

	/*
	 * (non-Javadoc)
	 * @see io.IMultiColumnCSVToMultiFileConverter#convert(java.io.File, java.io.File, java.lang.String)
	 */
	public void convert(File input, File outputDirectory, String prefix) throws IOException {
		if (input == null || !input.exists()) {
			throw new IllegalArgumentException("Invalid input file: " + input);
		}
		
		// if it is a directory, read all files in the directory
		if (input.isDirectory()) {
			throw new IllegalArgumentException("Directory is not supported as input: " + input);
		}
		
		
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IOException("Unable to create directory " + outputDirectory);
		}
		
		if (!outputDirectory.isDirectory()) {
			throw new IOException(outputDirectory + " is not a valid directory.");
		}
		
		CSVReader csvReader = new CSVReader(new FileReader(input));
		
		List<String> varNames = new ArrayList<String>();	// var to be filled by next method
		
		// read the header of csv
		if (readJointProbabilityHeader(csvReader, Collections.EMPTY_LIST, varNames) < 0) {
			throw new IllegalArgumentException("Unable to identify column for probability in file: " + input.getName());
		}
		// probabilities are declared in next rows after the last non-empty column of 1st row
		int indexOfProbability = varNames.size();	
		
		// reorder varNames so that the order is {Threat, Indicators, Detectors, Alert}. This will be used later when we write files
		List<String> varNamesSorted = null;
		Comparator<? super String> comparator = getVarNameComparator();
		if (comparator != null) {
			varNamesSorted = new ArrayList<String>(varNames);
			Collections.sort(varNamesSorted, comparator);
		} else {
			Debug.println(getClass(), "No var name comparator was offered...");
		}

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
			
			
			// we'll write table to a file;
			PotentialTable tableToWrite = potentialTable;
			
			// reorder variables so that it fits specification;
			if (varNamesSorted != null) {
				tableToWrite = getJointTable(variableMap, varNamesSorted);
				if (tableToWrite.tableSize() != potentialTable.tableSize()) {
					throw new RuntimeException("Failed to create a table with sorted variables. Sorted table has size " 
							+ tableToWrite.tableSize() + ", table read from CSV has size " + potentialTable.tableSize());
				}
				// setting reorderedTable to zero and then adding potentialTable will copy values of potentialTable in proper cells of reorderedTable
				tableToWrite.fillTable(0f);
				tableToWrite.opTab(potentialTable, potentialTable.PLUS_OPERATOR);
				tableToWrite.normalize();
			} else {
				Debug.println(getClass(), "Not sorting table because no var name comparator was provided...");
			}
			
			// create a new file to print table
			File output = new File(outputDirectory, prefix + numColumnsRead + ".csv");

			// prepare the stream to print results to
			PrintStream printer = new PrintStream(new FileOutputStream(output, false));	// do not append if file exists
			
			for (int tableIndex = 0; tableIndex < tableToWrite.tableSize(); tableIndex++) {
				printer.println(tableToWrite.getValue(tableIndex));
			}
			
			printer.close();
		}
		
		csvReader.close();
		
	}

	/**
	 * @return the varNameComparator
	 */
	public Comparator<? super String> getVarNameComparator() {
		return varNameComparator;
	}

	/**
	 * @param varNameComparator the varNameComparator to set
	 */
	public void setVarNameComparator(Comparator<? super String> varNameComparator) {
		this.varNameComparator = varNameComparator;
	}

}
