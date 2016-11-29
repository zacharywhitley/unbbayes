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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import unbbayes.io.NetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.Debug;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class CSVTableMarginalConsistencyChecker extends TestCase {
	
	/** Default name of alert variable */
	private String defaultAlertName = "Alert";
	
	/** How many detectors we expect to read from csv files (this will be used to check consistency) */
	private int expectedNumDetectors = 24;
	
	/** Expected total counts per table (this will be checked agains what's read from csv files) */
	private int expectedNumCounts = 3816;
	
	/** 
	 * Default place to look for pais of RCP data. 
	 * Content of this folder must be folders. Each folder in it must have a pair of csv files: 
	 * one for correlation data and one for detector x alert data 
	 */
	private String defaultDataFileFolder = "RCPData";

	/** If true, {@link #testCSVFileConsistency()} will print information about dependency between variables */
	private boolean isToPrintDependency = true;

	/** Name of the output file to print statistics, like mutual information and chi-square tests' p-values */
	private String outputFileName = "DependencyStatistics.csv";

	private boolean isToPrintCorrelation = true;
	
	/** The alpha value of p-value test. If p-value is smaller than this, then we can reject null hypothesis (of independence) */
	private float alpha = 0.01f;
	
	/** Name of Bayesian/Markov net file to generate. If null or empty, no such file will be generated*/
	private String networkFileName = "network.net";

	/** If true, {@link #generateNetFile(List, List)} will consider tables containing alert. */
	private boolean isToUseAlertTables = false;
	
	/** {@link #generateNet(Network, List, List)} will graphically separate nodes into blocks of this size */
	private int numNodeSet = 6;

	private boolean isToUseDecisionNode = true;

	private boolean isToReduceStateSpace = false;

	/**
	 * @param name
	 */
	public CSVTableMarginalConsistencyChecker(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	
	/**
	 * Read csv files and fill tables.
	 * The csv files must be in the following formats.
	 * <br/>
	 * <br/>
	 * Format 1 (correlation tables):
	 * <pre>
	 * [a blank line]
	 * Alert Days Detector 1,Alert Days Detector 2,Count
	 * 0,0,2646
	 * 0,1,152
	 * 0,2,41
	 * 0,3,14
	 * 1,0,2646
	 * 1,1,152
	 * 1,2,41
	 * 1,3,14
	 * 2,0,2646
	 * 2,1,152
	 * 2,2,41
	 * 2,3,14
	 * [a blank line]
	 * Alert Days Detector 1,Alert Days Detector 3,Count
	 * 0,0,2646
	 * 0,1,152
	 * 0,2,41
	 * 0,3,14
	 * 1,0,2646
	 * 1,1,152
	 * 1,2,41
	 * 1,3,14
	 * 2,0,2646
	 * 2,1,152
	 * 2,2,41
	 * 2,3,14
	 * </pre>
	 * 
	 * Format 2 (alert tables):
	 * <pre>
	 * Detector 1
	 * Num Alerts,No System Alert,System Alert
	 * 0,2833,37
	 * 1,439,15
	 * 2,212,18
	 * 3,101,15
	 * 4,60,13
	 * 5,17,8
	 * 6,12,5
	 * 7,2,6
	 * 8,0,8
	 * 9,0,3
	 * 10,0,6
	 * 11,0,1
	 * 12,0,4
	 * 13,0,1
	 * [a blank line]
	 * Detector 2
	 * Num Alerts,No System Alert,System Alert
	 * 0,3015,51
	 * 1,390,20
	 * 2,148,10
	 * 3,63,15
	 * 4,40,14
	 * 5,13,6
	 * 6,6,5
	 * 7,1,3
	 * 8,0,5
	 * 9,0,4
	 * 10,0,4
	 * 11,0,1
	 * 12,0,1
	 * 13,0,0
	 * 14,0,0
	 * 15,0,0
	 * 16,0,1
	 * </pre>
	 * 
	 * @param file : a csv file in the specified formats.
	 * @param sharedVariables : variables with same name should be related to same instances of {@link INode}, so this map
	 * keeps track of what names/nodes were instantiated already. This is an input/output argument (i.e. if it is
	 * filled already, values in it will be reused; and new values will be added to the mapping when read from csv).
	 * @param checkConsistency 
	 * @return tables read from csv files
	 * @throws IOException
	 */
	public List<PotentialTable> readTablesFromCSVFile(File file, Map<String, INode> sharedVariables) throws IOException {
		// basic assertions
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		
		CSVReader reader = new CSVReader(new FileReader(file));
		
		// read only the 1st entry just to check which type of csv file this is
		// skip blank lines
		String[] line = reader.readNext();
		while (line != null && isBlankCSVLine(line)){
			line = reader.readNext();
		}
		assertNotNull("End of file reached before reading a single table. File = " + file.getName(), line);
		
		// reset the reader
		reader.close();
		reader = new CSVReader(new FileReader(file));
		
		List<PotentialTable> ret = null; // the variable to return
		if (line.length <= 1) {
			// if first non-empty line had only 1 element, then it is a file containing detector x alert tables
			try {
				ret = readDetectorAlertTables(reader, sharedVariables);
			} catch (Exception e) {
				reader.close();
				throw e;
			}
		} else {
			// this is a file containing detector correlation tables
			try {
				ret = readDetectorCorrelationTables(reader, sharedVariables);
			} catch (Exception e) {
				reader.close();
				throw e;
			}
		}
		
		reader.close();
		return ret;
	}
	

	/**
	 * @param var : variable to check
	 * @param state : state to check (case insensitive)
	 * @return true if variable has the specified state. False otherwise
	 * @see INode#getStateAt(int)
	 */
	public boolean hasState(INode var, String state) {
		assertNotNull(var);
		assertNotNull(state);
		assertFalse(var + ": " +  state, state.trim().isEmpty());
		
		for (int i = 0; i < var.getStatesSize(); i++) {
			if (state.equalsIgnoreCase(var.getStateAt(i))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param cell
	 * @return
	 */
	public String convertToName(String cell) {
		// remove white spaces
		cell = cell.replaceAll("\\s", "");
		
		if (cell.matches("det[0-9]+")) {
			return cell.replaceAll("det", "Detector");
		} else if (cell.matches("ADD[0-9]+")) {
			return cell.replaceAll("ADD", "Detector");
		}
		
		return cell.replaceAll("Alert Days Detector", "Detector");
	}
	
	/**
	 * Reads the csv file assuming that csv contains correlation tables in following format:
	 * <pre>
	 * [a blank line]
	 * Alert Days Detector 1,Alert Days Detector 2,Count
	 * 0,0,2646
	 * 0,1,152
	 * 0,2,41
	 * 0,3,14
	 * 1,0,2646
	 * 1,1,152
	 * 1,2,41
	 * 1,3,14
	 * 2,0,2646
	 * 2,1,152
	 * 2,2,41
	 * 2,3,14
	 * [a blank line]
	 * Alert Days Detector 1,Alert Days Detector 3,Count
	 * 0,0,2646
	 * 0,1,152
	 * 0,2,41
	 * 0,3,14
	 * 1,0,2646
	 * 1,1,152
	 * 1,2,41
	 * 1,3,14
	 * 2,0,2646
	 * 2,1,152
	 * 2,2,41
	 * 2,3,14
	 * </pre>
	 * @param reader : reader of csv. It is assumed that it read the initial non-blank lines already
	 * @param sharedVariables : variables (i.e. nodes) in here must be reused across method invocations.
	 * This is an input & output argument, so if this is filled, entries will be reused. New entries read from csv
	 * will be pushed to this map as well.
	 * @return list of tables read from csv.
	 * @throws IOException 
	 * @see #readTablesFromCSVFile(File, Map)
	 */
	protected List<PotentialTable> readDetectorCorrelationTables(CSVReader reader, Map<String, INode> sharedVariables) throws IOException {
		// basic assertions
		assertNotNull(reader);
		assertNotNull(sharedVariables);
		
		// read the entire csv file
		List<String[]> allRows = reader.readAll();
		assertNotNull("No rows found. " + reader, allRows);
		assertFalse("Empty csv file. " + reader, allRows.isEmpty());
		assertTrue("File must contain at least the initial blank row, header row, and a data row. " + reader, allRows.size() >= 3);
		
		// variable to return. Follows same order of tables in CSV
		List<PotentialTable> ret = new ArrayList<PotentialTable>();
		
		Map<String, INode> variablesInCSV = new HashMap<String, INode>();	// will store variables in this CSV (need to check if consistent with shared vars)
		
		// read the rows 2 times: 1st time to read the variables & states, and 2nd time to read the values in tables
		for (int rowIndex = 0; rowIndex < allRows.size(); rowIndex++) {	// 1st time (read variables and states)
			
			// skip initial blank lines
			String[] row = allRows.get(rowIndex);
			while (rowIndex+1 < allRows.size() && isBlankCSVLine(row)){
				rowIndex++;
				row = allRows.get(rowIndex);
			}
			if (isBlankCSVLine(row)) {
				Debug.println(getClass(), "Found multiple blank lines at end of file");
				break;
			}
			
			// variables read in current table (a csv contains multiple tables). Indexes are columns in csv
			List<INode> currentVars = new ArrayList<INode>(2);	// optimize size for tables with 2 vars
			
			// read the header
			for (int i = row.length-1, countCellIndex = -1; i >= 0; i--) {	// countCellIndex will point to the index of the "count" column
				String cell = row[i];
				if (cell == null || cell.trim().isEmpty()) {
					continue;	// ignore empty cells
				}
				if (countCellIndex < 0) {
					// consider the "count" column is the last no-empty column
					countCellIndex = i;
					continue;
				}
				// all other cells that are not the count column, so model as new variables
				INode var = new ProbabilisticNode();
				var.setName(convertToName(cell));
				currentVars.add(0, var);
			}	// end of read header
			
			// read states;
			for (rowIndex++; rowIndex < allRows.size() ; rowIndex++) {
				row = allRows.get(rowIndex);
				if (isBlankCSVLine(row)) {
					// read next table (next iteration) if blank line was found
					break;
				}
				
				assertFalse("Number of variables in row " + rowIndex + " is expected to be " + currentVars.size()
						+ ", but number of columns was " + row.length, 
						row.length < currentVars.size());
				
				// read states of each var, assuming that initial columns of CSV are states of variables
				for (int column = 0; column < currentVars.size(); column++) {
					INode var = currentVars.get(column);
					String state = row[column].trim();
					if (!hasState(var,state)) {
						var.appendState(state);
					}
				}
				
			}	// end of read states
			
			// check if variables read in this iteration matches with variables in csv (compare names and states);
			for (INode currentVar : currentVars) {
				INode varInCSV =  variablesInCSV.get(currentVar.getName());
				if (varInCSV == null) {
					// add current vars to variablesin csv if not present already;
					variablesInCSV.put(currentVar.getName(), currentVar);
				} else {
					// check variable consistency
					assertEquals(varInCSV.getName(), currentVar.getName());									// double check name consistency
					assertEquals(varInCSV.getName(), varInCSV.getStatesSize(), currentVar.getStatesSize());	// check consistency of number of states
					// check consistency of order and names of states
					for (int i = 0; i < currentVar.getStatesSize(); i++) {
						assertEquals(varInCSV.getName(), varInCSV.getStateAt(i), currentVar.getStateAt(i));
					}
				}
			}
			
			// create table containing current vars (the table will be filled later when we read csv for 2nd time)
			ProbabilisticTable table = new ProbabilisticTable();
			for (int i = currentVars.size() - 1; i >= 0 ; i--) {	
				// last var has states iterating quickly in csv, 
				// var that iterates quickly needs to be the 1st vars to be included in table 
				// (so i starts at last var and is decremented)
				INode varToAdd = sharedVariables.get(currentVars.get(i).getName());
				if (varToAdd == null) {
					varToAdd = variablesInCSV.get(currentVars.get(i).getName());
				}
				if (varToAdd == null) {
					varToAdd = currentVars.get(i);
				}
				table.addVariable(varToAdd);
			}
			ret.add(table);
			
		}	// end of read variables & states
		
		// check if names/states of variable in this csv matches with shared variable;
		for (Entry<String, INode> entry : variablesInCSV.entrySet()) {
			INode sharedVar =  sharedVariables.get(entry.getKey());
			if (sharedVar == null) {
				// add current var to shared vars if not present already;
				sharedVariables.put(entry.getKey(), entry.getValue());
			} else {
				// check variable consistency
				assertEquals(sharedVar.getName(), entry.getValue().getName());									// double check name consistency
				assertEquals(sharedVar.getName(), sharedVar.getStatesSize(), entry.getValue().getStatesSize());	// check consistency of number of states
				// check consistency of order and names of states
				for (int i = 0; i < sharedVar.getStatesSize(); i++) {
					assertEquals(sharedVar.getName(), sharedVar.getStateAt(i), entry.getValue().getStateAt(i));
				}
			}
		}
		
		// read the data (values in table);
		for (int rowIndex = 0, tableIndex = 0; rowIndex < allRows.size(); rowIndex++) {	// tableIndex keeps track of which entry in ret shall be filled
			
			// skip initial blank lines
			String[] row = allRows.get(rowIndex);
			while (rowIndex+1 < allRows.size() && isBlankCSVLine(row)){
				rowIndex++;
				row = allRows.get(rowIndex);
			}
			if (isBlankCSVLine(row)) {
				Debug.println(getClass(), "Found multiple blank lines at end of file");
				break;
			}
			
			// extract current table to be filled
			PotentialTable table = ret.get(tableIndex);
			tableIndex++;	// use next table in next iteration
			
			// note: we are skipping the header
			
			// read counts;
			int cellIndex = 0;	// cell in table
			for (rowIndex++; rowIndex < allRows.size(); rowIndex++, cellIndex++) {
				row = allRows.get(rowIndex);
				if (isBlankCSVLine(row)) {
					// read next table (next iteration) if blank line was found
					break;
				}
				
				// row needs to contain state for each variable and count, so number of columns needs to be number of vars + 1
				assertFalse("Number of columns in row " + rowIndex + " is expected to be " + (table.getVariablesSize() + 1)
						+ ", but was " + row.length, 
						row.length < table.getVariablesSize() + 1);
				
				// read count
				int count = Integer.parseInt(row[table.getVariablesSize()]);
				table.setValue(cellIndex, count);
			}	// end of read states
			
			assertEquals(table.toString(), cellIndex, table.tableSize());
			
		}
		
		return ret;
	}

	

	/**
	 * Reads the csv file assuming that csv contains alert tables in following format:
	 * <pre>
	 * Detector 1
	 * Num Alerts,No System Alert,System Alert
	 * 0,2833,37
	 * 1,439,15
	 * 2,212,18
	 * 3,101,15
	 * 4,60,13
	 * 5,17,8
	 * 6,12,5
	 * 7,2,6
	 * 8,0,8
	 * 9,0,3
	 * 10,0,6
	 * 11,0,1
	 * 12,0,4
	 * 13,0,1
	 * [a blank line]
	 * Detector 2
	 * Num Alerts,No System Alert,System Alert
	 * 0,3015,51
	 * 1,390,20
	 * 2,148,10
	 * 3,63,15
	 * 4,40,14
	 * 5,13,6
	 * 6,6,5
	 * 7,1,3
	 * 8,0,5
	 * 9,0,4
	 * 10,0,4
	 * 11,0,1
	 * 12,0,1
	 * 13,0,0
	 * 14,0,0
	 * 15,0,0
	 * 16,0,1
	 * </pre>
	 * @param reader : reader of csv. It is assumed that it read the initial non-blank lines already
	 * @param sharedVariables : variables (i.e. nodes) in here must be reused across method invocations.
	 * This is an input & output argument, so if this is filled, entries will be reused. New entries read from csv
	 * will be pushed to this map as well.
	 * @return list of tables read from csv.
	 * @throws IOException 
	 * @see #readTablesFromCSVFile(File, Map)
	 */
	protected List<PotentialTable> readDetectorAlertTables(CSVReader reader, Map<String, INode> sharedVariables) throws IOException {
		// basic assertions
		assertNotNull(reader);
		assertNotNull(sharedVariables);
		
		// read the entire csv file
		List<String[]> allRows = reader.readAll();
		assertNotNull("No rows found. " + reader, allRows);
		assertFalse("Empty csv file. " + reader, allRows.isEmpty());
		assertTrue("File must contain at least the initial blank row, header row, and a data row. " + reader, allRows.size() >= 3);
		
		// variable to return. Follows same order of tables in CSV
		List<PotentialTable> ret = new ArrayList<PotentialTable>();
		
		Map<String, INode> variablesInCSV = new HashMap<String, INode>();	// will store variables in this CSV (need to check if consistent with shared vars)
		
		// read the rows 2 times: 1st time to read the variables & states, and 2nd time to read the values in tables
		for (int rowIndex = 0; rowIndex < allRows.size(); rowIndex++) {	// 1st time (read variables and states)
			
			// skip initial blank lines
			String[] row = allRows.get(rowIndex);
			while (rowIndex+1 < allRows.size() && isBlankCSVLine(row)){
				rowIndex++;
				row = allRows.get(rowIndex);
			}
			if (isBlankCSVLine(row)) {
				Debug.println(getClass(), "Found multiple blank lines at end of file");
				break;
			}
			
			// prepare alert var
			INode alertVar = new ProbabilisticNode();
			alertVar.setName(getDefaultAlertName());
			
			// read the 1st header (Detector X)
			assertTrue(row.length >= 1);
			INode currentVar = new ProbabilisticNode();
			currentVar.setName(convertToName(row[0]));
			
			// read the 2nd header to read states of alert (Num Alerts,No System Alert,System Alert)
			rowIndex++;
			assertTrue("row = " + rowIndex, rowIndex < allRows.size());
			row = allRows.get(rowIndex);
			for (int i = 1 ; i < row.length; i++) {	// skip 1st row, because it is not a state of alert var
				String cell = row[i];
				if (cell == null || cell.trim().isEmpty()) {
					continue;	// ignore empty cells
				}
				alertVar.appendState(cell.trim());
			}	// end of read 2nd header
			
			// check alert var read in this iteration matches with variables in csv (compare names and states);
			INode alertInCSV =  variablesInCSV.get(alertVar.getName());
			if (alertInCSV == null) {
				// add current vars to variablesin csv if not present already;
				alertInCSV = alertVar;
				variablesInCSV.put(alertInCSV.getName(), alertInCSV);
			} else {
				// check variable consistency
				assertEquals(alertInCSV.getName(), alertVar.getName());									// double check name consistency
				assertEquals(alertInCSV.getName(), alertInCSV.getStatesSize(), alertVar.getStatesSize());	// check consistency of number of states
				// check consistency of order and names of states
				for (int i = 0; i < alertVar.getStatesSize(); i++) {
					assertEquals(alertInCSV.getName(), alertInCSV.getStateAt(i), alertVar.getStateAt(i));
				}
			}
			
			// read states;
			for (rowIndex++; rowIndex < allRows.size() ; rowIndex++) {
				row = allRows.get(rowIndex);
				if (isBlankCSVLine(row)) {
					// read next table (next iteration) if blank line was found
					break;
				}
				
				assertFalse("Number of columns in row " + rowIndex + " is " + row.length, row.length < 1);
				
				// read states of each var, assuming that initial columns of CSV are states of variables
				String state = row[0].trim();
				if (!hasState(currentVar,state)) {
					currentVar.appendState(state);
				}
				
			}	// end of read states
			
			// check if variable read in this iteration matches with variables in csv (compare names and states);
			INode varInCSV =  variablesInCSV.get(currentVar.getName());
			if (varInCSV == null) {
				// add current vars to variablesin csv if not present already;
				variablesInCSV.put(currentVar.getName(), currentVar);
				varInCSV = currentVar;
			} else {
				// check variable consistency
				assertEquals(varInCSV.getName(), currentVar.getName());									// double check name consistency
				assertEquals(varInCSV.getName(), varInCSV.getStatesSize(), currentVar.getStatesSize());	// check consistency of number of states
				// check consistency of order and names of states
				for (int i = 0; i < currentVar.getStatesSize(); i++) {
					assertEquals(varInCSV.getName(), varInCSV.getStateAt(i), currentVar.getStateAt(i));
				}
			}
			
			// create table containing current vars (the table will be filled later when we read csv for 2nd time)
			ProbabilisticTable table = new ProbabilisticTable();
			// alert has states iterating quickly in csv, 
			// var that iterates quickly needs to be the 1st vars to be included in table 
			// so add alert first
			INode varToAdd = sharedVariables.get(alertVar.getName());
			if (varToAdd == null) {
				varToAdd = alertInCSV;
			}
			table.addVariable(varToAdd);
			varToAdd = sharedVariables.get(currentVar.getName());
			if (varToAdd == null) {
				varToAdd = varInCSV;
			}
			table.addVariable(varToAdd);
			ret.add(table);
			
		}	// end of read variables & states
		
		// check if names/states of variable in this csv matches with shared variable;
		for (Entry<String, INode> entry : variablesInCSV.entrySet()) {
			INode sharedVar =  sharedVariables.get(entry.getKey());
			if (sharedVar == null) {
				// add current var to shared vars if not present already;
				sharedVariables.put(entry.getKey(), entry.getValue());
			} else {
				// check variable consistency
				assertEquals(sharedVar.getName(), entry.getValue().getName());									// double check name consistency
				assertEquals(sharedVar.getName(), sharedVar.getStatesSize(), entry.getValue().getStatesSize());	// check consistency of number of states
				// check consistency of order and names of states
				for (int i = 0; i < sharedVar.getStatesSize(); i++) {
					assertEquals(sharedVar.getName(), sharedVar.getStateAt(i), entry.getValue().getStateAt(i));
				}
			}
		}
		
		// prepare alert variable
//		INode sharedAlertVar = sharedVariables.get(getDefaultAlertName());
//		assertNotNull(sharedAlertVar);
		
		// read the data (values in table);
		// tableIndex keeps track of which entry in ret shall be filled
		for (int rowIndex = 0, tableIndex = 0; rowIndex < allRows.size(); rowIndex++) {	// 1st time (read variables and states)
			
			// skip initial blank lines
			String[] row = allRows.get(rowIndex);
			while (rowIndex+1 < allRows.size() && isBlankCSVLine(row)){
				rowIndex++;
				row = allRows.get(rowIndex);
			}
			if (isBlankCSVLine(row)) {
				Debug.println(getClass(), "Found multiple blank lines at end of file");
				break;
			}
			
			// skip the 1st header (Detector X)
			// 2nd header will also be skipped in next for loop
			rowIndex++;
			
			
			// extract current table to be filled
			PotentialTable table = ret.get(tableIndex);
			assertEquals(getDefaultAlertName(), table.getVariableAt(0).getName());
			assertEquals(sharedVariables.get(getDefaultAlertName()), table.getVariableAt(0));
			
			
			tableIndex++;	// use next table in next iteration
			
			// read counts;
			int cellIndex = 0;	// cell in table
			for (rowIndex++; rowIndex < allRows.size(); rowIndex++) {
				row = allRows.get(rowIndex);
				if (isBlankCSVLine(row)) {
					// read next table (next iteration) if blank line was found
					break;
				}
				
				// row needs to contain counts for each state of alert, so number of columns needs to be number of states of alert + 1 column for state of detector
				assertFalse("Number of columns in row " + rowIndex + " is expected to be " + (table.getVariableAt(0).getStatesSize() + 1)
						+ ", but was " + row.length, 
						row.length < table.getVariableAt(0).getStatesSize() + 1);
				
				// read counts
				for (int alertState = 0; alertState < table.getVariableAt(0).getStatesSize(); alertState++) {
					int count = Integer.parseInt(row[alertState + 1]);
					table.setValue(cellIndex, count);
					cellIndex++;
				}
			}	// end of read states
			
			assertEquals(table.toString(), cellIndex, table.tableSize());
		
			
		}
		
		return ret;
	
		
	}

	/**
	 * @param line : array of string to test
	 * @return true if line represents a blank line (either null, empty, or all values are blank)
	 */
	public boolean isBlankCSVLine(String[] line) {
		if (line == null || line.length <= 0) {
			return true;
		}
		// check if all content are blank
		for (int i = 0; i < line.length; i++) {
			if (!line[i].trim().isEmpty()) {
				return false;	// found at least one non-empty content
			}
		}
		return true;	// all entries were white space
	}

	/**
	 * @throws IOException 
	 * @throws InvalidParentException 
	 */
	public void testCSVFileConsistency() throws IOException, InvalidParentException {
		
		// calculate how many correlation tables we expect
		int numDetectors = getExpectedNumDetectors();		// how many detectors we expect
		// binomial coefficient (i.e. from numDetectors, choose 2) = fact(numDetectors) / ( (fact(numDetectors) - 2)*2 )
		int numCorrelations = (numDetectors * (numDetectors - 1)) / 2;
		
		// extract the folder where folders with pairs of csv files are placed
		File folder = new File(getDefaultDataFileFolder());
		assertNotNull(folder);
		assertTrue(folder.exists());
		assertTrue(folder.isDirectory());
		
		File[] innerFolders = folder.listFiles();
		assertNotNull(innerFolders);
		assertTrue(innerFolders.length > 0);
		
		
		Network net = null;	// network generated from all files in inner folder
		String netFileName = getNetworkFileName();	// file to save net
		for (File innerFolder : innerFolders) {
			
			assertNotNull(innerFolder);
			assertTrue(innerFolder.getName(), innerFolder.exists());
			assertTrue(innerFolder.getName(), innerFolder.isDirectory());
			
			// extract the pair of files to read
			File[] dataFiles = innerFolder.listFiles();
			assertNotNull(innerFolder.getName(), dataFiles);
			assertEquals(innerFolder.getName(), 2, dataFiles.length);
			
			
			
			Map<String, INode> sharedVariables = new HashMap<String, INode>();	// we must use same instances of variables in all tables
			
			// prepare 2 separate lists: one for correlation tables (read from one of the csv files), another for alert tables (read from the other csv file)
			List<PotentialTable> correlationTables = null;
			List<PotentialTable> alertTables = null;
			
			// read the two csv files and check which one is a correlation table file, and which one is a alert table file
			// read the 1st file
			List<PotentialTable> tables = readTablesFromCSVFile(dataFiles[0], sharedVariables);
			if (tables.size() == numCorrelations) {
				// this is a correlation table
				correlationTables = tables;
			} else if (tables.size() == numDetectors) {
				// this is an alert table
				alertTables = tables;
			} else {
				fail("Unexpected number of tables read from " + dataFiles[0].getAbsolutePath() + ": " + tables.size());
			}
			// read the second file
			tables = readTablesFromCSVFile(dataFiles[1], sharedVariables);
			if (tables.size() == numCorrelations) {
				// this is a correlation table
				assertNull(correlationTables);	// make sure the previous file was not specifying correlation tables
				correlationTables = tables;
			} else if (tables.size() == numDetectors) {
				// this is an alert table
				assertNull(alertTables);		// make sure the previous file was not specifying alert tables
				alertTables = tables;
			} else {
				fail("Unexpected number of tables read from " + dataFiles[1].getAbsolutePath() + ": " + tables.size());
			}
			
			assertNotNull(alertTables);
			assertNotNull(correlationTables);
			
			// make sure number of variables matches with expected (24 detectors + 1 alert)
			assertEquals(sharedVariables.toString(), numDetectors + 1, sharedVariables.size());
			
			// make sure all tables are using the shared variable instances and the total counts is expected
			for (PotentialTable table : correlationTables) {
				for (int i = 0; i < table.getVariablesSize(); i++) {
					INode tableVar = table.getVariableAt(i);
					INode sharedVar = sharedVariables.get(tableVar.getName());
					assertNotNull(table.toString(), sharedVar);
					assertTrue(table.toString(), sharedVar == tableVar);
				}
				// check total counts in this table
				int sum = 0;
				for (int i = 0; i < table.tableSize(); i++) {
					sum += ((int)table.getValue(i));
				}
				assertEquals(table.toString(), getExpectedNumCounts(), sum);
			}
			for (PotentialTable table : alertTables) {
				for (int i = 0; i < table.getVariablesSize(); i++) {
					INode tableVar = table.getVariableAt(i);
					INode sharedVar = sharedVariables.get(tableVar.getName());
					assertNotNull(table.toString(), sharedVar);
					assertTrue(table.toString(), sharedVar == tableVar);
				}
				// check total counts in this table
				int sum = 0;
				for (int i = 0; i < table.tableSize(); i++) {
					sum += ((int)table.getValue(i));
				}
				assertEquals(table.toString(), getExpectedNumCounts(), sum);
			}
			
			// compare correlation tables pair-wise;
			for (int i = 0; i < correlationTables.size()-1; i++) {
				PotentialTable table1 = correlationTables.get(i);
				boolean hasMatch = false;
				for (int j = i+1; j < correlationTables.size(); j++) {
					PotentialTable table2 = correlationTables.get(j);
					// extract variables in common. 
					Collection<INode> commonVars = this.getCommonVars(table1, table2);
					if (commonVars == null || commonVars.isEmpty()) { 	
						continue;	// Skip if they do not have variables in common;
					}
					// marginalize out table 1
					PotentialTable table1Marginal = retainVars(table1, commonVars);
					// marginalize out table 2
					PotentialTable table2Marginal = retainVars(table2, commonVars);
					
					assertTrue("Tables did not match. " + table1 + " ; " + table2 + "; common vars = " + commonVars, matches(table1Marginal, table2Marginal));
					
					hasMatch = true;
				}
				assertTrue("Table did not have a match. " + table1, hasMatch);
			}
			
			
			// compare alert tables pair-wise;
			for (int i = 0; i < alertTables.size()-1; i++) {
				PotentialTable table1 = alertTables.get(i);
				for (int j = i+1; j < alertTables.size(); j++) {
					PotentialTable table2 = alertTables.get(j);
					// extract variables in common. 
					Collection<INode> commonVars = this.getCommonVars(table1, table2);
					// alert tables must have only the alert var in common;
					assertNotNull("Tables did not have a common var. " + table1 + " ; " + table2, commonVars);
					assertEquals("Alert tables must have only the alert var in common. " + table1 + " ; " + table2 + "; common = " + commonVars, 1,  commonVars.size());
					// marginalize out table 1
					PotentialTable table1Marginal = retainVars(table1, commonVars);
					// marginalize out table 2
					PotentialTable table2Marginal = retainVars(table2, commonVars);
					
					assertTrue("Tables did not match. " + table1 + " ; " + table2 + "; common vars = " + commonVars, matches(table1Marginal, table2Marginal));
				}
			}
			
			// compare correlation tables to alert tables;
			for (int correlationTableIndex = 0; correlationTableIndex < correlationTables.size(); correlationTableIndex++) {
				PotentialTable correlationTable = correlationTables.get(correlationTableIndex);
				int numMatches = 0;	// each var in correlation table must have 1 match with alert table
				for (int alertTableIndex = 0; alertTableIndex < alertTables.size(); alertTableIndex++) {
					PotentialTable alertTable = alertTables.get(alertTableIndex);
					// extract variables in common. 
					Collection<INode> commonVars = this.getCommonVars(correlationTable, alertTable);
					if (commonVars == null || commonVars.isEmpty()) { 	
						continue;	// Skip if they do not have variables in common;
					}
					// marginalize out correlation table
					PotentialTable correlationMarginal = retainVars(correlationTable, commonVars);
					// marginalize out alert table
					PotentialTable alertMarginal = retainVars(alertTable, commonVars);
					
					assertTrue("Tables did not match. " + correlationMarginal + " ; " + alertMarginal + "; common vars = " + commonVars, matches(correlationMarginal, alertMarginal));
					
					numMatches++;
				}
				assertEquals("Table did not have matches. " + correlationTable, correlationTable.variableCount(), numMatches);
			}
			
			// print the results of chi-square test of independence. 
			// results are p-values, so smaller values (e.g. less than 0.05) mean dependence.
			
			this.printDependency(innerFolder.getName() + "_" + getOutputFileName(), correlationTables, alertTables);
			
			// reuse net from previous iteration (if it's null, then a new net is instantiated)
			if (netFileName != null && !netFileName.trim().isEmpty()) {
				net = this.generateNet(net, correlationTables, alertTables);
			}
			
		}
		
		// save the net
		if (netFileName != null && !netFileName.trim().isEmpty()) {
			assertFalse(((ProbabilisticNetwork)net).hasCycle());
			new NetIO().save(new File(netFileName.trim()), net);
		}
		
	}
	
	/**
	 * 
	 * @param correlationTables
	 * @param alertTables
	 * @throws InvalidParentException 
	 * @see #getAlpha()
	 */
	protected Network generateNet(Network net, List<PotentialTable> correlationTables, List<PotentialTable> alertTables) throws InvalidParentException {
		
		
		// prepare list of tables to consider
		List<PotentialTable> tables = new ArrayList<PotentialTable>(correlationTables);
		if (isToUseAlertTables()) {
			tables.addAll(alertTables);
		}
		
		// instantiate the network to save
		if (net == null) {
			net = new ProbabilisticNetwork("StubNetwork");
		}
		
		// include the nodes to network
		for (PotentialTable table : tables) {
			// iterate on each node (variable) in table
			for (int varIndex = 0; varIndex < table.variableCount(); varIndex++) {
				INode node = table.getVariableAt(varIndex);
				String name = node.getName().replaceAll("\\s", "_");
				// if network does not already have this node, then include it
				if (net.getNodeIndex(name) < 0) {
					// use decision nodes instead, because they don't need conditional probability tables
					Node newNode = null;
					if (isToUseDecisionNode()) {
						newNode = new DecisionNode();
					} else {
						newNode = new ProbabilisticNode();
					}
					newNode.setName(name);
					for (int i = 0; i < node.getStatesSize(); i++) {
						newNode.appendState(node.getStateAt(i));
					}
					
					newNode.setPosition(100 + (net.getNodeCount() % getNumNodeSet())*200, 100 + (net.getNodeCount() / getNumNodeSet())*200);
					
					net.addNode(newNode);
				} else {
					// node already exists. Append state if necessary
					Node nodeInNet = net.getNode(name);
					if (node.getStatesSize() > nodeInNet.getStatesSize()) {
						nodeInNet.removeStates();
						for (int i = 0; i < node.getStatesSize(); i++) {
							nodeInNet.appendState(node.getStateAt(i));
						}
					}
				}
			}
		}
		
		
		// reduce state space. Node with smallest state will have 1 state, and others will be adjusted.
		if (isToReduceStateSpace()) {
			// this will be a list of number of states found in network
			List<Integer> stateSizeList = new ArrayList<Integer>(net.getNodeCount());
			for (Node node : net.getNodes()) {
				Integer statesSize = node.getStatesSize();
				if (!stateSizeList.contains(statesSize)) {
					stateSizeList.add(statesSize);
				}
			}
			
			// order the list so that smallest sizes will be the 1st in list
			Collections.sort(stateSizeList);
			
			// adjust the number of states of each nodes
			for (Node node : net.getNodes()) {
				Integer originalSize = node.getStatesSize();
				int newSize = stateSizeList.indexOf(originalSize) + 1;	// smallest size will be 1, and larger sizes will be 1 + position in list
				node.removeStates();
				for (int i = 0; i < newSize; i++) {
					node.appendState("state"+i);
				}
			}
			
		}
		
		// prepare a chi-square tester which will be used to calculate p-value for rejecting null hypothesis of independence.
		// the dependence calculated by this object will be used to generate arcs
		ChiSqureTestWithZero chiSquareTester = new ChiSqureTestWithZero();
		
		// prepare an object that will be used to check if there is a directed path between two nodes
		// this will be used to avoid cycles when inserting new arc
		MSeparationUtility pathFinder = MSeparationUtility.newInstance();	
		
		// create the arcs for each pair of nodes in table
		for (PotentialTable table : tables) {
			// extract the p-value to be used to check if we should add an arc between nodes in this table
			double pValue = chiSquareTester.chiSquareTest(getContingencyMatrix(table));	
			// remove arc if test failed (p-value was greater than the threshold alpha means we cannot reject null hypothesis of independence)
			// add otherwise.
			boolean isToAdd = pValue <= getAlpha();
			
			// include the arcs for each pair of nodes in table (table supposedly has only 2 nodes, though)
			for (int i = 0; i < table.getVariablesSize()-1; i++) {
				
				// extract the nodes
				String name1 = table.getVariableAt(i).getName().replaceAll("\\s", "_");
				Node node1 = net.getNode(name1);
				assertNotNull(name1,node1);
				
				for (int j = i+1; j < table.getVariablesSize(); j++) {
					
					String name2 = table.getVariableAt(j).getName().replaceAll("\\s", "_");
					Node node2 = net.getNode(name2);
					assertNotNull(name2,node2);
					
					// do not connect same nodes twice
					if (net.getEdge(node1, node2) != null) {
						if (isToAdd) {
							continue;
						} else {
							// remove existing edge
							net.removeEdge(net.getEdge(node1, node2));
						}
					}
					
					if (isToAdd) {
						// include the arc if it will not cause a cycle
						if (pathFinder.getRoutes(node2, node1, null, null, 1).isEmpty()) {
							// there is no route from node2 to node1, so we can create node1->node2 without generating cycle
							net.addEdge(new Edge(node1, node2));
						} else {
							// include the arc in opposite direction won't cause a cycle
							net.addEdge(new Edge(node2, node1));
						}
					}
					
				}
			}
			
		}
		
		return net;
	}

	/**
	 * Creates a csv file with dependency information (e.g. mutual information, chi-square test's p-value).
	 * @param fileName : name of the csv file
	 * @param correlationTables : contingency tables with correlation data
	 * @param alertTables : contingency table containing the alert variable
	 * @throws IOException
	 * @see {@link #testCSVFileConsistency()}
	 */
	protected void printDependency(String fileName, List<PotentialTable> correlationTables, List<PotentialTable> alertTables) throws IOException {
		
		// do nothing if boolean attribute is false
		if (!isToPrintDependency()) {
			return;
		}
		
		// this will be used to test chi-square independence test's p-value.
		ChiSqureTestWithZero chiSquareTester = new ChiSqureTestWithZero();
		
		// print statistics to a separate file
		PrintStream printer = new PrintStream(new FileOutputStream(new File(fileName), false));
//		System.out.println("=====================================================");
//		System.out.println("Dependence statistics");
//		System.out.println("=====================================================");
		if (isToPrintCorrelation()) {
			printer.println("\"Var1\",\"Var2\",\"Correlation\",\"Mutual information\",\"Chi-Square p-value\"");
		} else {
			printer.println("\"Var1\",\"Var2\",\"Mutual information\",\"Chi-Square p-value\"");
		}
		List<PotentialTable> tables = new ArrayList<PotentialTable>(correlationTables);
		tables.addAll(alertTables);
		for (PotentialTable table : tables) {
//			System.out.println("-------------------------------------------------------");
			if (table.variableCount() != 2) {
				Debug.println(getClass(), "Skipping table without 2 variables: " + table);
				continue;
			}
			
			// print the variables
			for (int i = 0; i < table.variableCount(); i++) {
				printer.print("\"" + table.getVariableAt(i).getName() + "\"");
				printer.print(",");
			}
			
			// print correlation
			if (isToPrintCorrelation()) {
				List<double[]> countArrays = getCountArrays(table);	// convert table to arrays of counts
				assertEquals(2, countArrays.size());
				printer.print(new PearsonsCorrelation().correlation(countArrays.get(0), countArrays.get(1)));
				printer.print(",");
			}
			
			// print mutual information
			printer.print("\"" + table.getMutualInformation(0, 1) + "\"");	// we know that there are only 2 vars in table
			printer.print(",");
			
			// calculate and print p-value of chi-square test
			long[][] counts = getContingencyMatrix(table);	// convert table to matrix of contingency
			printer.print("\"" + chiSquareTester.chiSquareTest(counts) + "\"");	// values lower than 0.05 strongly suggest dependence
			
			// end of line
			printer.println();
//			System.out.println("-------------------------------------------------------");
		}
//		System.out.println("=====================================================");
		printer.close();
	
		
	}

	/**
	 * Converts a contingency table to arrays of counts.
	 * @param table : contingency table
	 * @return : arrays of counts for each variable in {@link PotentialTable#getVariableAt(int)}
	 */
	public List<double[]> getCountArrays(PotentialTable table) {
		if (table == null) {
			return Collections.EMPTY_LIST;
		}
		
		// calculate how many datasets we need to create
		int numDatasets = (int) table.getSum();		// TODO check overflow
		
		List<double[]> ret = new ArrayList<double[]>(table.getVariablesSize());
		
		// create 1 entry in ret for each variable in the table
		for (int varIndex = 0; varIndex < table.getVariablesSize(); varIndex++) {
			
			double[] data = new double[numDatasets];	// entry in ret
			
			// read the table
			int dataIndex = 0;
			for (int cell = 0; cell < table.tableSize(); cell++) {
				
				int state = table.getMultidimensionalCoord(cell)[varIndex];
				int count = (int) table.getValue(cell);
				for (int i = 0; i < count; i++) {
					if (dataIndex >= numDatasets) {
						Debug.println(null, "Expected number of datasets is " + numDatasets + ", but attempted to add entry to index " + dataIndex + " for variable " + table.getVariableAt(varIndex));
					} else {
						data[dataIndex]	= state;
					}
					dataIndex++;
				}
			}
			
			if (dataIndex != numDatasets) {
				throw new RuntimeException("Estimated sum of cells in contingency table was " + numDatasets + ", but found " + dataIndex + " for variable " + table.getVariableAt(varIndex));
			}
			
			ret.add(data);
		}
		
		return ret;
	}

	/**
	 * Simply converts the table to an equivalent matrix of long.
	 * @param table : table of counts of 2 variables
	 * @return equivalent representation as a matrix of long.
	 * States of the variable at index 1 of {@link PotentialTable#getVariableAt(int)} will be the rows of the matrix,
	 * and states of the variable at index 0 of {@link PotentialTable#getVariableAt(int)} will be the columns of the matrix.
	 */
	public long[][] getContingencyMatrix(PotentialTable table) {
		// basic assertion
		assertNotNull(table);
		assertEquals(2, table.getVariablesSize());
		
		// prepare the matrix to return
		long[][] ret = new long[table.getVariableAt(1).getStatesSize()][table.getVariableAt(0).getStatesSize()];
		
		for (int row = 0, tableIndex = 0; row < ret.length; row++) {
			for (int column = 0; column < ret[row].length; column++, tableIndex++) {
				ret[row][column] = (long) table.getValue(tableIndex);
			}
		}
		
		return ret;
	}

	/**
	 * Compares the content of two tables with same size and same variables (but variables may be in different ordering).
	 * @param table1
	 * @param table2
	 * @return true if values in the 2 tables matches. False otherwise.
	 */
	public boolean matches(PotentialTable table1, PotentialTable table2) {
		
		// basic assertions
		assertNotNull(table1);
		assertNotNull(table2);
		assertEquals(table1 + " ; " + table2 , table1.tableSize(), table2.tableSize());
		
		// make sure both tables uses same variables
		for (int i = 0; i < table1.variableCount(); i++) {
			INode var1 = table1.getVariableAt(i);
			boolean found = false;
			for (int j = 0; j < table2.variableCount(); j++) {
				INode var2 = table2.getVariableAt(j);
				if (var1.equals(var2)) {
					found = true;
				}
			}
			assertTrue("Did not find node " + var1 + " of table " + table1 + " in table " + table2, found);
		}
		
		// iterate on cells of table 1
		for (int table1Cell = 0; table1Cell < table1.tableSize(); table1Cell++) {
			
			// get the states associated with current cell
			int[] table1States = table1.getMultidimensionalCoord(table1Cell);
			
			// get the index of table2 associated with the states of curent cell
			int[] table2States = table2.getMultidimensionalCoord(0);	// use arbitrary argument, because I'll fill this array later
			
			// fill table2States with values in table1States
			for (int table1VarIndex = 0; table1VarIndex < table1States.length; table1VarIndex++) {
				table2States[table2.indexOfVariable((Node)table1.getVariableAt(table1VarIndex))] = table1States[table1VarIndex];
			}
			
			// get the cell of table 2 associated with current cell in table 1
			int table2Cell = table2.getLinearCoord(table2States);
			
			// if we found any diverging cell, then immediately return false
			if ( ((int)table1.getValue(table1Cell)) != ((int)table2.getValue(table2Cell))) {
				return false;
			}
			
		}
		
		// at this point, all cells matched
		return true;
		
	}

	/**
	 * Marginalizes out a table retaining all specified vars/nodes.
	 * @param table : table to marginalize out
	 * @param varsToRetain : variables to keep in table.
	 * @return table with all nodes except those specified in commonVars removed (and content of tables will be summed up when variable is marginalized out).
	 * @see Collection#contains(Object)
	 */
	public PotentialTable retainVars(PotentialTable table, Collection<INode> varsToRetain) {
		// basic assertions
		assertNotNull(table);
		assertNotNull(varsToRetain);
		
		// use a different instance, so that the input table is not changed
		PotentialTable ret = (PotentialTable) table.clone();	
		
		// retrieve which variables we need to remove
		Collection<INode> varsToMarginalizeOut = new ArrayList<INode>(ret.variableCount());
		for (int i = 0; i < ret.variableCount(); i++) {
			INode varInTable = ret.getVariableAt(i);
			// avoid removing vars in varsToRetain
			if (!varsToRetain.contains(varInTable)) {
				varsToMarginalizeOut.add(varInTable);
			}
		}
		
		// remove all vars we retrieved
		for (INode varToRemove : varsToMarginalizeOut) {
			ret.removeVariable(varToRemove, false);		// do not normalize table
		}
		
		return ret;
	}

	/**
	 * @param table1 : instances from this table will be used to fill the collection to be returned.
	 * @param table2 : instances from this table will be used just for comparison ({@link Object#equals(Object)}).
	 * @return variables (nodes) in common between table1 and table2.
	 * @see PotentialTable#getVariableAt(int)
	 */
	public Collection<INode> getCommonVars(PotentialTable table1, PotentialTable table2) {
		
		// prepare the object to return
		Collection<INode> ret =  new HashSet<INode>(table1.variableCount());
		
		// simply iterate on nodes of both tables
		for (int table1VarIndex = 0; table1VarIndex < table1.variableCount(); table1VarIndex++) {
			INode var1 = table1.getVariableAt(table1VarIndex);
			for (int table2VarIndex = 0; table2VarIndex < table2.variableCount(); table2VarIndex++) {
				INode var2 = table2.getVariableAt(table2VarIndex);
				if (var1.equals(var2)) {
					ret.add(var1);
				}
			}
		}
		
		return ret;
	}


	/**
	 * @return How many detectors we expect to read from csv files (this will be used to check consistency)
	 */
	public int getExpectedNumDetectors() {
		return expectedNumDetectors;
	}

	/**
	 * @param expectedNumDetectors : How many detectors we expect to read from csv files (this will be used to check consistency)
	 */
	public void setExpectedNumDetectors(int expectedNumDetectors) {
		this.expectedNumDetectors = expectedNumDetectors;
	}

	/**
	 * @return : Default name of alert variable
	 */
	public String getDefaultAlertName() {
		return defaultAlertName;
	}

	/**
	 * @param defaultAlertName : Default name of alert variable
	 */
	public void setDefaultAlertName(String defaultAlertName) {
		this.defaultAlertName = defaultAlertName;
	}

	/**
	 * @return Default place to look for pais of RCP data. 
	 * Content of this folder must be folders. Each folder in it must have a pair of csv files: 
	 * one for correlation data and one for detector x alert data 
	 */
	public String getDefaultDataFileFolder() {
		return defaultDataFileFolder;
	}

	/**
	 * @param defaultDataFileFolder : Default place to look for pais of RCP data. 
	 * Content of this folder must be folders. Each folder in it must have a pair of csv files: 
	 * one for correlation data and one for detector x alert data 
	 */
	public void setDefaultDataFileFolder(String defaultDataFileFolder) {
		this.defaultDataFileFolder = defaultDataFileFolder;
	}

	/**
	 * @return Expected total counts per table (this will be checked agains what's read from csv files)
	 */
	public int getExpectedNumCounts() {
		return expectedNumCounts;
	}

	/**
	 * @param expectedNumCounts : Expected total counts per table (this will be checked agains what's read from csv files)
	 */
	public void setExpectedNumCounts(int expectedNumCounts) {
		this.expectedNumCounts = expectedNumCounts;
	}

	/**
	 * @return : If true, {@link #testCSVFileConsistency()} will print information about dependency between variables
	 */
	public boolean isToPrintDependency() {
		return isToPrintDependency;
	}

	/**
	 * @param isToPrintDependency : If true, {@link #testCSVFileConsistency()} will print information about dependency between variables
	 */
	public void setToPrintDependency(boolean isToPrintDependency) {
		this.isToPrintDependency = isToPrintDependency;
	}
	

	/**
	 * @return the outputFileName : Name of the output file to print statistics, like mutual information and chi-square tests' p-values
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * @param outputFileName the outputFileName to set : Name of the output file to print statistics, like mutual information and chi-square tests' p-values
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * @return the isToPrintCorrelation
	 */
	public boolean isToPrintCorrelation() {
		return isToPrintCorrelation;
	}

	/**
	 * @param isToPrintCorrelation the isToPrintCorrelation to set
	 */
	public void setToPrintCorrelation(boolean isToPrintCorrelation) {
		this.isToPrintCorrelation = isToPrintCorrelation;
	}
	

	/**
	 * @return The alpha value of p-value test. If p-value is smaller than this, then we can reject null hypothesis (of independence).
	 * @see #testCSVFileConsistency()
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha : The alpha value of p-value test. If p-value is smaller than this, then we can reject null hypothesis (of independence)
	 * @see #testCSVFileConsistency()
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}


	/**
	 * @return name of Bayesian/Markov net file to generate. If null or empty, no such file will be generated
	 * @see #generateNetFile(String, List, List)
	 */
	public String getNetworkFileName() {
		return networkFileName;
	}

	/**
	 * @param networkFileName : name of Bayesian/Markov net file to generate. If null or empty, no such file will be generated
	 * @see #generateNetFile(String, List, List)
	 */
	public void setNetworkFileName(String networkFileName) {
		this.networkFileName = networkFileName;
	}
	


	/**
	 * @return the isToUseAlertTables : if true, {@link #generateNetFile(List, List)} will consider tables containing alert.
	 */
	public boolean isToUseAlertTables() {
		return isToUseAlertTables;
	}

	/**
	 * @param isToUseAlertTables : if true, {@link #generateNetFile(List, List)} will consider tables containing alert.
	 */
	public void setToUseAlertTables(boolean isToUseAlertTables) {
		this.isToUseAlertTables = isToUseAlertTables;
	}



	/**
	 * @return the numNodeSet
	 */
	public int getNumNodeSet() {
		return numNodeSet;
	}

	/**
	 * @param numNodeSet the numNodeSet to set
	 */
	public void setNumNodeSet(int numNodeSet) {
		this.numNodeSet = numNodeSet;
	}

	/**
	 * @return the isToUseDecisionNode : if true, {@link #generateNet(Network, List, List)} will generate decision nodes
	 * in order to save space of conditional probabilities.
	 */
	public boolean isToUseDecisionNode() {
		return isToUseDecisionNode;
	}

	/**
	 * @param isToUseDecisionNode : if true, {@link #generateNet(Network, List, List)} will generate decision nodes
	 * in order to save space of conditional probabilities
	 */
	public void setToUseDecisionNode(boolean isToUseDecisionNode) {
		this.isToUseDecisionNode = isToUseDecisionNode;
	}

	/**
	 * @return the isToReduceStateSpace : if true, {@link #generateNet(Network, List, List)} will consider
	 * that variables with smaller size has 1 state, and other variables will be adjusted.
	 */
	public boolean isToReduceStateSpace() {
		return isToReduceStateSpace;
	}

	/**
	 * @param isToReduceStateSpace the isToReduceStateSpace to set : if true, {@link #generateNet(Network, List, List)} will consider
	 * that variables with smaller size has 1 state, and other variables will be adjusted.
	 */
	public void setToReduceStateSpace(boolean isToReduceStateSpace) {
		this.isToReduceStateSpace = isToReduceStateSpace;
	}

	/**
	 * Execute this main function/method if you don't have a driver program (i.e. an invoker) of JUnit tests.
	 * @param args 
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("i","input", true, "Directory to get folders with pairs of csv files.");
		options.addOption("o","output", true, "Name of the resulting csv files which will be written to each input directory.");
		options.addOption("n","num-detectors", true, "Expected number of detectors.");
		options.addOption("c","num-counts", true, "Expected number of counts (users) per table.");
		options.addOption("net","net-file", true, "Name of network file to generate. If empty, no network file will be generated.");
		options.addOption("alpha","alpha-p-value", true, "The alpha value (between 0 and 1) to be considered as the "
				+ "cut-off for rejecting null hypothesis in p-value. "
				+ "Smaller values indicates that more pairs of variables will be considered as independent.");
		options.addOption("corr","print-correlation", false, "Prints Pearson's correlation to result.");
		options.addOption("alert","use-alert-tables", false, "Uses alert tables when generating network file.");
		options.addOption("decision","use-decision-nodes", false, "Decision nodes will be used when generating network file in order to save space.");
		options.addOption("reduced","reduce-state-space", false, "Variables with smaller states will be converted to variables with 1 state only, when generating network file.");
		options.addOption("d","debug", false, "Enables debug mode.");
		
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
		
		CSVTableMarginalConsistencyChecker test = new CSVTableMarginalConsistencyChecker("testCSVFileConsistency");
		
		if (cmd.hasOption("i")) { // if this param is not set, then it will look at a default place
			test.setDefaultDataFileFolder(cmd.getOptionValue("i"));
		}
		if (cmd.hasOption("o")) { // if this param is not set, then it will look at a default place
			test.setOutputFileName(cmd.getOptionValue("o"));
		}
		if (cmd.hasOption("net")) { // if this param is not set, then it will look at a default place
			test.setNetworkFileName(cmd.getOptionValue("net"));
		}
		if (cmd.hasOption("n")) { // if this param is not set, then it will look at a default place
			test.setExpectedNumDetectors(Integer.parseInt(cmd.getOptionValue("n")));
		}
		if (cmd.hasOption("c")) { // if this param is not set, then it will look at a default place
			test.setExpectedNumCounts(Integer.parseInt(cmd.getOptionValue("c")));
		}
		if (cmd.hasOption("alpha")) { // if this param is not set, then it will look at a default place
			test.setAlpha(Float.parseFloat(cmd.getOptionValue("alpha")));
		}
		test.setToPrintCorrelation(cmd.hasOption("corr"));
		test.setToUseAlertTables(cmd.hasOption("alert"));
		test.setToUseDecisionNode(cmd.hasOption("decision"));
		test.setToReduceStateSpace(cmd.hasOption("reduced"));
		
		
		try {
			PublicResultPrinter printer = new PublicResultPrinter(System.out);
			TestResult result = new TestResult();
			result.addListener(printer);
			long startTime= System.currentTimeMillis();
			test.run(result);
			printer.printAll(result , System.currentTimeMillis() - startTime);
			
			if (!result.wasSuccessful()) 
				System.exit(TestRunner.FAILURE_EXIT);
			System.exit(TestRunner.SUCCESS_EXIT);
		} catch(Throwable e) {
			System.err.println(e.getMessage());
			System.exit(TestRunner.EXCEPTION_EXIT);
		}
	}
	




	/**
	 * This is just an extension of {@link ResultPrinter} with
	 * a public wrapper of methods {@link #printHeader(long)},
	 * {@link #printErrors(TestResult)}, {@link #printFailures(TestResult)},
	 * and {@link #printFooter(TestResult)}.
	 * @author Shou Matsumoto
	 *
	 */
	public static class PublicResultPrinter extends ResultPrinter {
		public PublicResultPrinter(PrintStream writer) {
			super(writer);
		}
		public synchronized void printAll(TestResult result, long runTime) {
			printHeader(runTime);
		    printErrors(result);
		    printFailures(result);
		    printFooter(result);
		}
	}

}
