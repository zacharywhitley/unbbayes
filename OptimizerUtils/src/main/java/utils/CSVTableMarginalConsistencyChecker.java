/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;
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
	
	/** 
	 * Default place to look for pais of RCP data. 
	 * Content of this folder must be folders. Each folder in it must have a pair of csv files: 
	 * one for correlation data and one for detector x alert data 
	 */
	private String defaultDataFileFolder = "RCPData";

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
	protected String convertToName(String cell) {
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
	 */
	public void testCSVFileConsistency() throws IOException {
		
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
			
			// make sure all tables are using the shared variable instances
			for (PotentialTable table : correlationTables) {
				for (int i = 0; i < table.getVariablesSize(); i++) {
					INode tableVar = table.getVariableAt(i);
					INode sharedVar = sharedVariables.get(tableVar.getName());
					assertNotNull(table.toString(), sharedVar);
					assertTrue(table.toString(), sharedVar == tableVar);
				}
			}
			for (PotentialTable table : alertTables) {
				for (int i = 0; i < table.getVariablesSize(); i++) {
					INode tableVar = table.getVariableAt(i);
					INode sharedVar = sharedVariables.get(tableVar.getName());
					assertNotNull(table.toString(), sharedVar);
					assertTrue(table.toString(), sharedVar == tableVar);
				}
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
		}
		
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
	 * Execute this main function/method if you don't have a driver program (i.e. an invoker) of JUnit tests.
	 * @param args 
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("i","input", true, "Directory to get folders with pairs of csv files.");
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
		
		CSVTableMarginalConsistencyChecker test = new CSVTableMarginalConsistencyChecker(CSVTableMarginalConsistencyChecker.class.getName());
		
		if (cmd.hasOption("i")) { // if this param is not set, then it will look at a default place
			test.setDefaultDataFileFolder(cmd.getOptionValue("i"));
		}
		
		TestRunner runner = new TestRunner(System.out);
		try {
			TestResult result = runner.doRun(test);
			if (!result.wasSuccessful()) 
				System.exit(TestRunner.FAILURE_EXIT);
			System.exit(TestRunner.SUCCESS_EXIT);
		} catch(Throwable e) {
			System.err.println(e.getMessage());
			System.exit(TestRunner.EXCEPTION_EXIT);
		}
	}

}
