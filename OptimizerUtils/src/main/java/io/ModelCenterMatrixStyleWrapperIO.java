package io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import unbbayes.util.Debug;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This class is a customization of {@link ModelCenterWrapperIO} which outputs a matrix instead of 
 * a property file (in a format like name=value).
 * The header of the output and the input file should be the same.
 * @author Shou Matsumoto
 *
 */
public class ModelCenterMatrixStyleWrapperIO extends ModelCenterWrapperIO {

	public static final String DEFAULT_PROBABILITY_MATRIX_TITLE =  "Probability Matrix";
	
	private String probabilityMatrixTitle = DEFAULT_PROBABILITY_MATRIX_TITLE;

	/**
	 * Default constructor is kept protected to allow inheritance, but instances should be created by {@link #getInstance()}
	 */
	protected ModelCenterMatrixStyleWrapperIO() {}
	
	/**
	 * Default constructor method
	 * @return : an instance of {@link ModelCenterMatrixStyleWrapperIO}
	 */
	public static IModelCenterWrapperIO getInstance() {
		return new ModelCenterMatrixStyleWrapperIO();
	}
	
	
	/** 
	 * This method will print the property values as-is instead of printing them in the format name=value.
	 * Values will be alphabetically reordered by name, and each property value will be printer as 1 row.
	 * For example, let's assume the following property as input 
	 * (the left side of "=" is the key/name, and the right side is the value):
	 * <pre>
	 * "3" = "0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631"
	 * "1" = "Probability Matrix"
	 * "4" = "1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0"
	 * "2" = "Q1	Q2	Q3	Q4	Q5	Q6	Q7	Q8	Q9	Q10	Q11"
	 * </pre>
	 * This method should print something like the following:
	 * <pre>
	 * Probability Matrix
	 * Q1	Q2	Q3	Q4	Q5	Q6	Q7	Q8	Q9	Q10	Q11
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * 1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0
	 * </pre>
	 * @see io.ModelCenterWrapperIO#printBody(java.util.Map, java.io.PrintStream)
	 * @see String#compareTo(String)
	 */
	protected void printBody(Map<String, String> property, PrintStream printer) {
		// basic assertions
		if (property == null || property.isEmpty() || printer == null) {
			return;
		}
		
		// sort the property by name/key
		List<Entry<String, String>> sortedEntries = new ArrayList<Map.Entry<String,String>>(property.entrySet());
		Collections.sort(sortedEntries, new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getKey().compareTo(o2.getKey());	// compare keys
			}
		});
		
		// print the values line-by-line
		for (Entry<String, String> entry : sortedEntries) {
			printer.println(entry.getValue());	// only print value. The key (name) was only used for sorting.
		}
		
		// make sure we include additional line break at the end
		printer.println();
	}

	/**
	 * This method extends the superclass' method in order to output a file in the format specified in {@link #writeWrapperFile(Map, File)}.
	 * The input file is a csv file in the same format of the one specified in {@link ModelCenterWrapperIO#convertToWrapperOutput(File, File)},
	 * however it will have more than 1 probability row, like the following:
	 * <pre>
	 * Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Q9,Q10,Q11
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * </pre>
	 * The output will look like the following:
	 * <pre>
	 * # JavaSimulatorWrapper Results
	 * # Sun Aug 07 14:22:08 EDT 2016
	 * #
	 * # calculated parameters
	 * Probability Matrix
	 * Q1	Q2	Q3	Q4	Q5	Q6	Q7	Q8	Q9	Q10	Q11
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * </pre>
	 * @see io.ModelCenterWrapperIO#convertToWrapperOutput(java.io.File, java.io.File)
	 * @see #writeWrapperFile(Map, File)
	 */
	public void convertToWrapperOutput(File input, File output) throws IOException {
		// basic assertions
		if (input == null || !input.exists() || input.isDirectory()) {
			throw new IOException("Invalid input: " + input);
		}
		if (output == null || output.isDirectory()) {
			throw new IOException("Invalid output: " + output);
		}
		
		Debug.println(getClass(), "Converting CSV " + input.getAbsolutePath() + " to wrapper " + output.getAbsolutePath());
		
		// this will read csv files
		CSVReader reader = new CSVReader(new FileReader(input));
		
		// this will be the property to be passed to #writeWrapperFile(Map, File)
		Map<String, String> property = new HashMap<String, String>();
		int rowNumber = 0;	// this will be used as key of property.
		
		// make sure 1st row is "Probability Matrix"
		property.put(++rowNumber+"", getProbabilityMatrixTitle());
		
		// the csv file has 1st row describing names of questions, and remaining rows specifying their probabilities
		String[] csvRow = reader.readNext();	// read 1st row
		if (csvRow == null) {
			reader.close();
			throw new IOException("Row not found in " + input.getName());
		}
		
		// convert 1st row to a tab-separated string
		String rowToWrite = "";
		int numQuestionsRead = 0;
		for (int i = 0; i < csvRow.length; i++) {
			if (csvRow[i] == null || csvRow[i].trim().isEmpty()) {
				continue;	// ignore null or empty names
			}
			if ( numQuestionsRead > 0) {
				rowToWrite += "\t";
			}
			if (csvRow[i].matches("Q[0-9]+")) {
				// if questions are in the format like Q01, convert to Q1
				rowToWrite += "Q" + Integer.parseInt(csvRow[i].split("Q")[1]);
			} else {
				// or else, use name as-is
				rowToWrite += csvRow[i];
			}
			numQuestionsRead++;
		}
		
		property.put(++rowNumber+"", rowToWrite);	// increment row number and insert row to property
		
		
		// read remaining rows;
		for (csvRow = reader.readNext(); csvRow != null; csvRow = reader.readNext()) {
			
			// convert back the csvRow to a comma-separated string
			// TODO this is redundant
			rowToWrite = "";
			int numValidEntries = 0;	// counts how many values (in original csv) were non-null and non-empty
			for (int i = 0; i < csvRow.length; i++) {
				if (csvRow[i] == null || csvRow[i].trim().isEmpty()) {
					continue;	// skip such values.
				}
				if (numValidEntries > 0) {
					// if we read some valid entry before, we need to separate with comma.
					rowToWrite += ",";
				}
				rowToWrite += csvRow[i];
				numValidEntries++;
			}
			
			property.put(++rowNumber+"", rowToWrite);	// increment row number and insert row to property
		}
		
		// at this point, we can close the reader, because there is nothing more to read
		reader.close();
		
		// write to wrapper ouput file
		Debug.println(getClass(), "Writing wrapper " + output.getAbsolutePath());
		this.writeWrapperFile(property , output);
		
	}

	/**
	 * @return the probabilityMatrixTitle : title of probability matrix which will be written to 1st row of output file after header.
	 * @see #DEFAULT_PROBABILITY_MATRIX_TITLE 
	 */
	public String getProbabilityMatrixTitle() {
		return probabilityMatrixTitle;
	}

	/**
	 * @param probabilityMatrixTitle : title of probability matrix which will be written to 1st row of output file after header.
	 * @see #DEFAULT_PROBABILITY_MATRIX_TITLE
	 */
	public void setProbabilityMatrixTitle(String probabilityMatrixTitle) {
		this.probabilityMatrixTitle = probabilityMatrixTitle;
	}
	
}
