/**
 * 
 */
package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;
import utils.UniformNameConverter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Reads a joint distribution (counts or probabilities) from a CSV file.
 * @author Shou Matsumoto
 *
 */
public class CSVJointDistributionReader implements IJointDistributionReader {
	
	private int idColumn = 0;
	private boolean isToAdd1ToCounts = false;
	private boolean isToSortStates = false;
	
	private UniformNameConverter converter = null;
	

	/**
	 * Default constructor
	 */
	public CSVJointDistributionReader() {}

	
	/**
	 * @param cell
	 * @return
	 */
	public String convertName(String cell) {
		return this.getConverter().convertToName(cell);
//		// remove white spaces
//		cell = cell.replaceAll("\\s", "");
//		
//		if (cell.matches("det[0-9]+")) {
//			return cell.replaceAll("det", "Detector");
//		} else if (cell.matches("ADD[0-9]+")) {
//			return cell.replaceAll("ADD", "Detector");
//		}
//		
//		return cell.replaceAll("AlertDaysDetector", "Detector");
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IJointDistributionReader#fillJointDist(unbbayes.prs.bn.PotentialTable, java.io.InputStream, boolean)
	 */
	public boolean fillJointDist(PotentialTable table, InputStream input, boolean isToNormalize) throws IOException {
		
		// read the csv rows.
		CSVReader reader = new CSVReader(new InputStreamReader(input));
		List<String[]> allRows = reader.readAll();
		reader.close();
		
		// this array will be filled with which column in csv file is associated with the i-th variable in table. 
		int[] columns = new int[table.variableCount()];
		Arrays.fill(columns, -1);	// init with invalid column value
		
		// read the 1st line of csv (name of the columns)
		String[] csvLine = allRows.get(0);
		// iterate on columns in order to read variable names
		for (int columnInCSV = 0; columnInCSV < csvLine.length; columnInCSV++) {
			if (columnInCSV == getIdColumn()) {
				continue;
			}
			String nameInFile = csvLine[columnInCSV];
			if (nameInFile == null || nameInFile.trim().isEmpty()) {
				continue;
			}
			
			// check if current column in csv represents some variable in table
			for (int varIndex = 0; varIndex < table.getVariablesSize(); varIndex++) {
				if (convertName(table.getVariableAt(varIndex).getName()).equalsIgnoreCase(convertName(nameInFile))) {
					columns[varIndex] = columnInCSV;
				}
			}
		}
		
		// check if all columns were present
		for (int columnInCsv : columns) {
			if (columnInCsv < 0) {
				return false;
			}
		}
		
		// init table
		if (isToAdd1ToCounts()) {
			table.fillTable(1f);	// add count 1 to everyone
		} else {
			table.fillTable(0f);	// initialize counts with zeros
		}
		
		// read the remaining file and fill joint table with counts
		for (int currentRowIndex = 1; currentRowIndex < allRows.size(); currentRowIndex++) { // just read all rows except first row
			csvLine = allRows.get(currentRowIndex);
			if (csvLine.length <= 0) {
				Debug.println(getClass(), "Empty row: " + currentRowIndex);
				continue;	// ignore empty lines
			}
			
			// each line in csv file is something like 4,2,0,1 (this means 1st var is 4, second var is 2, 3rd var is 0, and 4th var is 1).
			
			// figure out which cell in joint table represents the value in current line
			int[] coord = table.getMultidimensionalCoord(0);
			for (int varIndex = 0; varIndex < coord.length; varIndex++) {
				int state = Integer.parseInt(csvLine[columns[varIndex]]);
				
				if (state >= table.getVariableAt(varIndex).getStatesSize()) {
					throw new IllegalArgumentException("Found state " + state + " at row " + (currentRowIndex+1) + " for variable " + table.getVariableAt(varIndex) + " with number of states " + table.getVariableAt(varIndex).getStatesSize());
				}
				
				coord[varIndex] = state;
			}
			
			// increment current cell in table
			int cell = table.getLinearCoord(coord);			// convert to linear index now, because it's faster to convert it just once, rather than multiple times
			table.setValue(cell, table.getValue(cell)+1);
		}
		
		
		if (isToNormalize) {
			// normalize joint table, so that joint table becomes a table of proportions
			table.normalize();
		}
		
		return true;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IJointDistributionReader#getMaxValue(java.io.InputStream)
	 */
	public Map<String, Integer> getMaxValue(InputStream input) throws IOException{
		
		// read the csv rows.
		CSVReader reader = new CSVReader(new InputStreamReader(input));
		List<String[]> allRows = reader.readAll();
		reader.close();
		
		Map<String, Integer> ret = new HashMap<String, Integer>();
		List<String> columnNames = new ArrayList<String>();
		
		// read the 1st line of csv (name of the columns)
		String[] csvLine = allRows.get(0);
		// iterate on columns in order to read variable names
		for (int columnInCSV = 0; columnInCSV < csvLine.length; columnInCSV++) {
			if (columnInCSV == getIdColumn()) {
				columnNames.add("ID");
				continue;
			}
			String nameInFile = csvLine[columnInCSV];
			if (nameInFile == null || nameInFile.trim().isEmpty()) {
				continue;
			}
			
			nameInFile = convertName(nameInFile);
			ret.put(nameInFile, -1);
			columnNames.add(nameInFile);
		}
		
		
		// read the remaining file and fill joint table with counts
		for (int currentRowIndex = 1; currentRowIndex < allRows.size(); currentRowIndex++) { // just read all rows except first row
			csvLine = allRows.get(currentRowIndex);
			if (csvLine.length <= 0) {
				Debug.println(getClass(), "Empty row: " + currentRowIndex);
				continue;	// ignore empty lines
			}
			
			for (int columnInCSV = 0; columnInCSV < csvLine.length; columnInCSV++) {
				if (columnInCSV == getIdColumn()) {
					continue;
				}
				
				String columnName = columnNames.get(columnInCSV);
				Integer max = ret.get(columnName);
				
				int value = Math.round(Float.parseFloat(csvLine[columnInCSV]));
				if (value > max) {
					ret.put(columnName, value);
				}
				
			}
			
		}
		
		
		return ret;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IJointDistributionReader#getNumUserSystemAlert(java.io.InputStream, int, java.lang.String)
	 */
	public Map<String, Integer> getNumUserSystemAlert(InputStream input, int alertDaysThreshold, String totalCountKey) throws IOException {

		// read the csv rows.
		CSVReader reader = new CSVReader(new InputStreamReader(input));
		
		// read the 1st line of csv (name of the columns)
		String[] csvLine = reader.readNext();
		if (csvLine == null) {
			reader.close();
			throw new IOException("No row found");
		}
		
		Map<String, Integer> alertCounters = new HashMap<String, Integer>();	// var to return (number of users with system alerts for each detector)
		
		// read which columns are detectors (i.e. columns to check for alert days)
		List<Integer> columnsToConsider = new ArrayList<Integer>();
		List<String> namesColumnsToConsider = new ArrayList<String>();	// respective names of columns to be considered
		for (int columnInCSV = 0; columnInCSV < csvLine.length; columnInCSV++) {	// iterate on columns in order to read variable names
			if (columnInCSV == getIdColumn()) {
				continue;	// user ids are definitely not detectors
			}
			String nameInFile = csvLine[columnInCSV];
			if (nameInFile == null || nameInFile.trim().isEmpty()) {
				continue;	// ignore empty names
			}
			
			// convert name to a format we know
			nameInFile = convertName(nameInFile);
			
			// if the name (in the format we know) contains the string "detector", then it should be considered as a detector
			if (nameInFile.toLowerCase().contains("detector")) {
				columnsToConsider.add(columnInCSV);	// mark this column as a detector column
				namesColumnsToConsider.add(nameInFile);
				alertCounters.put(nameInFile, 0);	// initialize counter (allocate space in map)
			}
		}
		
		int totalAlerts = 0;	// total number of alerts (avoid double-counting users with multiple alerts)
		
		// read the remaining file and count how many users had system alerts
		for (csvLine = reader.readNext(); csvLine != null; csvLine = reader.readNext()) { 
			if (csvLine.length <= 0) {
				Debug.println(getClass(), "Empty row found... ");
				continue;	// ignore empty lines
			}
			
			// iterate on columns of current row/line
			boolean hasCounted = false;	// this will become true when at least 1 detector had system alert for current user/row
			for (int indexInList = 0; indexInList < columnsToConsider.size(); indexInList++) {
				// only read columns that are considered (detectors)
				Integer columnInCSV = columnsToConsider.get(indexInList);
				// if we found at least one column with a value greater or equal to threshold, then current user has alert
				int value = Math.round(Float.parseFloat(csvLine[columnInCSV]));
				if (value >= alertDaysThreshold) {
					// increment number of users with alert on current detector.
					String detectorName = namesColumnsToConsider.get(indexInList);	// extract the name of current detector
					Integer count = alertCounters.get(detectorName);	// the counter of alerts in current detector
					count++;	
					alertCounters.put(detectorName, count);
					// increment total system alert only if we did not count current user yet
					if (!hasCounted) {
						totalAlerts++;
					}
					hasCounted = true;
				}
				
			}
			
		}
		reader.close();
		
		// append total count of system alerts to map
		if (totalCountKey != null && !totalCountKey.isEmpty()) {
			alertCounters.put(totalCountKey, totalAlerts);
		}
		
		return alertCounters;
	}

	/**
	 * @return the idColumn : the column to be considered an identifier column in csv file. This column will not be read. Set it to negative in order to read all columns
	 */
	public int getIdColumn() {
		return idColumn;
	}

	/**
	 * @param idColumn : the column to be considered an identifier column in csv file. This column will not be read. Set it to negative in order to read all columns
	 */
	public void setIdColumn(int idColumn) {
		this.idColumn = idColumn;
	}


	/**
	 * @return the isToAdd1ToCounts
	 */
	public boolean isToAdd1ToCounts() {
		return isToAdd1ToCounts;
	}


	/**
	 * @param isToAdd1ToCounts the isToAdd1ToCounts to set
	 */
	public void setToAdd1ToCounts(boolean isToAdd1ToCounts) {
		this.isToAdd1ToCounts = isToAdd1ToCounts;
	}
	
	/**
	 * @return the isToSortStates
	 */
	public boolean isToSortStates() {
		return isToSortStates;
	}


	/**
	 * @param isToSortStates the isToSortStates to set
	 */
	public void setToSortStates(boolean isToSortStates) {
		this.isToSortStates = isToSortStates;
	}


	/**
	 * @return the converter
	 */
	public UniformNameConverter getConverter() {
		if (converter == null) {
			converter = new UniformNameConverter();
		}
		return converter;
	}


	/**
	 * @param converter the converter to set
	 */
	public void setConverter(UniformNameConverter converter) {
		this.converter = converter;
	}


	/**
	 * This is just an example of how to use the methods in {@link CSVJointDistributionReader} class.
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {

		IJointDistributionReader io = new CSVJointDistributionReader();
		
		String fileName = "R/ADD.Test1.csv";	// default place to look for file
		if (args != null && args.length > 0) {
			fileName = args[0];	// extract name from argument
		}
		System.out.println("Loading file: " + fileName);
		
		System.out.println("**************************************************************************************");
		for (int detector1 = 1; detector1 <= 23; detector1++) {
			for (int detector2 = detector1 + 1; detector2 <= 24; detector2++) {
				
				PotentialTable table = new ProbabilisticTable();
				
				ProbabilisticNode node = new ProbabilisticNode();
				node.setName("Detector" + detector2);
				for (int i = 0; i < 6; i++) {
					node.appendState(""+i);
				}
				table.addVariable(node);
				node = new ProbabilisticNode();
				node.setName("Detector" + detector1);
				for (int i = 0; i < 6; i++) {
					node.appendState(""+i);
				}
				table.addVariable(node);
				
				io.fillJointDist(table, new FileInputStream(new File(fileName)), false);
				
				System.out.println("Vars (" + table.variableCount() + "):");
				for (int varIndex = 0; varIndex < table.variableCount(); varIndex++) {
					System.out.println("\t" + table.getVariableAt(varIndex) + " (" + table.getVariableAt(varIndex).getStatesSize() + ")");
				}
				System.out.println("Values (size = " + table.tableSize() + ", sum = " + table.getSum() + "):");
				for (int cellIndex = 0; cellIndex < table.tableSize(); cellIndex++) {
					System.out.print("\t" + table.getValue(cellIndex));
				}
				System.out.println("\n**************************************************************************************");
			}
		}
	}



}
