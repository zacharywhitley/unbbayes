package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import unbbayes.util.Debug;
import utils.JavaSimulatorWrapper;
import au.com.bytecode.opencsv.CSVReader;

/**
 * This is a class for reading or writing text files which specifies keys and values (like standard property files),
 * with comments starting from "#".
 * <br/>
 * <br/>
 * The following is an example of an input supported by this class:
 * <pre>
 * # 
 * # JavaSimulatorWrapper input file
 * #
 * 
 * Number_of_Indicators:	1
 * Number_of_Detectors:	0
 * Has_Alert_In_Prob: 0
 * Number_of_Users:	2419
 * Type_of_Fusion:	1
 * Probabilities1:	1.0
 * Probabilities1:	0.0
 * Probabilities2:	0.0
 * Probabilities2:	1.0
 * Probabilities3:	.5
 * Probabilities3:	.5
 * </pre>
 * The following is an example of output supported by this class:
 * <pre>
 * # JavaSimulatorWrapper Results
 * # Tue Jul 12 11:47:42 EDT 2016
 * #
 * # calculated parameters
 * Probability=0.54347825,0.7352941,0.002134218,0.11557789,0.45454544,0.096330285,0.07339449,0.6764706,0.14705881,0.61764705,0.4705882
 * </pre>
 * @author Shou Matsumoto
 */
public class ModelCenterWrapperIO implements IModelCenterWrapperIO {
	
	public static final String PROBABILITY_PROPERTY_NAME = "Probability";
	private String probabilityPropertyName = PROBABILITY_PROPERTY_NAME;
	
	private Map<String, String> properties = new HashMap<String, String>();

	/**
	 * @see #getInstance(JavaSimulatorWrapper)
	 */
	protected ModelCenterWrapperIO() {}
	
	/**
	 * @return a new instance
	 * @see ModelCenterMatrixStyleWrapperIO#getInstance()
	 */
	public static IModelCenterWrapperIO getInstance() {
		return ModelCenterMatrixStyleWrapperIO.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see io.IModelCenterWrapperIO#readWrapperFile(java.io.File)
	 */
	public void readWrapperFile(File input) throws IOException {
		
		// reset properties
		getProperties().clear();

		// set up file tokenizer
		StreamTokenizer st = new StreamTokenizer(new BufferedReader(new FileReader(input)));
		st.resetSyntax();
		st.wordChars('A', 'z');
		st.wordChars('0', '9');
		st.wordChars('_', '_');
		st.wordChars('-', '-');
		st.wordChars(',', ',');	// commas should be considered as part of the word
		st.wordChars('.', '.');	// dots should be considered as part of the word
		st.whitespaceChars(':',':');	// jumps separators
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.quoteChar('"');
		st.quoteChar('\'');		
		st.eolIsSignificant(true);	// declaration must be in same line
		st.commentChar('#');

		// read the file
		while (st.nextToken() != st.TT_EOF) {
			if (st.ttype != st.TT_WORD) {
				continue;
			}
			
			// read the left value (key of the property)
			String key = st.sval;
			if (key == null || key.trim().isEmpty()) {
				// go to next line
				while (st.nextToken() != st.TT_EOL){};
				continue;
			}
			
			// read the right value (value of the property)
			st.nextToken();
			String value = st.sval;
			if (st.ttype == st.TT_NUMBER) {
				value = String.valueOf(st.nval);
			}
			if (value == null || value.trim().isEmpty()) {
				// go to next line
				while (st.nextToken() != st.TT_EOL){};
				continue;
			}
			
			// check if a property with same name exists already.
			String currentProperty=getProperties().get(key);

            if (currentProperty==null || currentProperty.isEmpty()) {
            	getProperties().put(key, value);
            } else {
            	// if there is a property with same name already, append (separated by comma)
            	getProperties().put(key, currentProperty + "," + value);
            }

		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IModelCenterWrapperIO#writeWrapperFile(java.util.Map, java.io.File)
	 */
	public void writeWrapperFile(Map<String, String> property , File output) throws FileNotFoundException {

		if (property == null || output == null) {
			return;
		}
		
		// prepare the stream to print results to
		PrintStream printer = new PrintStream(new FileOutputStream(output, false));	// overwrite if file exists
		
		/*
		Print something like the following:
		# JavaSimulatorWrapper Results
		# Tue Jul 12 11:47:42 EDT 2016
		#
		# calculated parameters
		Probability=0.54347825,0.7352941,0.002134218,0.11557789,0.45454544,0.096330285,0.07339449,0.6764706,0.14705881,0.61764705,0.4705882
		*/
		
		// print header (i.e. comments starting with "#")
		this.printHeader(property, printer);
		
		this.printBody(property, printer);
		
		printer.close();
		
	}

	/**
	 * Print properties in the format name=value.
	 * The "value" can be any string, so the following is also valid.
	 * <pre>
	 * Probability=0.54347825,0.7352941,0.002134218,0.11557789,0.45454544,0.096330285,0.07339449,0.6764706,0.14705881,0.61764705,0.4705882
	 * </pre>
	 * @param property : properties to be printed
	 * @param printer : stream to be printed to.
	 * @see #printHeader(Map, PrintStream)
	 */
	protected void printBody(Map<String, String> property, PrintStream printer) {
		// assertions
		if (property == null || property.isEmpty() || printer == null) {
			return;
		}
		
		// just print all properties
		for (Entry<String, String> entry : property.entrySet()) {
			printer.println(entry.getKey() + "=" + entry.getValue());
		}
		
		// make sure we have an extra line break
		printer.println();
	}

	/**
	 * Prints the following header:
	 * <pre>
	 *# JavaSimulatorWrapper Results
	 *# Tue Jul 12 11:47:42 EDT 2016
	 *#
	 *# calculated parameters
	 * </pre>
	 * Subclasses may extend this method in order to print different headers.
	 * @param property : reference to the properties to be printed at {@link #printBody(Map, PrintStream)}
	 * @param printer : the print stream to print to.
	 */
	protected void printHeader(Map<String, String> property, PrintStream printer) {
		// basic assertions
		if (printer == null) {
			return;
		}
		// actually print the header
		printer.println("# JavaSimulatorWrapper Results");
		printer.println("# " + new Date().toString());
		printer.println("#");
		printer.println("# calculated parameters");
	}
	

	/**
	 * This is an utility method to convert csv file to wrapper file.
	 * The csv file should look like the following:
	 * <pre>
	 * Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Q9,Q10,Q11
	 * 0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631
	 * </pre>
	 * The method {@link #writeWrapperFile(Map, File)} will be used to write the output file. 
	 * The following property will be used as argument:
	 * <pre>
	 * {@value #PROBABILITY_PROPERTY_NAME} = "0.88372093,1.0,0.0,0.19075142,0.33333334,0.10552763,0.13142855,0.8684211,0.052631583,0.55263156,0.6052631"
	 * </pre>
	 * @param input : the input csv file
	 * @param output : a file in the same specification of {@link #writeWrapperFile(Map, File)}.
	 * @throws IOException
	 * @see io.IModelCenterWrapperIO#convertToWrapperOutput(java.io.File, java.io.File)
	 * @see #getProbabilityPropertyName()
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
		
		CSVReader reader = new CSVReader(new FileReader(input));
		
		// the csv file has 1st row describing names of questions, and remaining rows specifying their probabilities
		reader.readNext();	// ignore first row
		String[] line = reader.readNext();
		if (line == null) {
			reader.close();
			throw new IOException("2nd row not found in " + input.getName());
		}
		
		reader.close();
		
		// convert back the line to a comma-separated string
		// TODO this is redundant
		String commaSeparatedProb = "";
		int numValidEntries = 0;	// counts how many values (in original csv) were non-null and non-empty
		for (int i = 0; i < line.length; i++) {
			if (line[i] == null || line[i].trim().isEmpty()) {
				continue;	// skip such values
			}
			if (numValidEntries > 0) {
				// if we read some valid entry before, we need to separate with comma.
				commaSeparatedProb += ",";
			}
			commaSeparatedProb += line[i];
			numValidEntries++;
		}
		
		// write to wrapper ouput file
		Debug.println(getClass(), "Writing wrapper " + output.getAbsolutePath());
		Debug.println(getClass(), "Number of valid entries in the probability was " + numValidEntries);
		this.writeWrapperFile(Collections.singletonMap(getProbabilityPropertyName(), commaSeparatedProb), output);
		
	}



	/**
	 * @return the properties
	 */
	protected Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	protected void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	/*
	 * (non-Javadoc)
	 * @see io.IModelCenterWrapperIO#getProperty(java.lang.String)
	 */
	public String getProperty(String key) {
		return getProperties().get(key);
	}
	

	/**
	 * @return the probabilityPropertyName : the name of the property to be used
	 * in {@link #convertToWrapperOutput(File, File)} as the the probabilities.
	 */
	public String getProbabilityPropertyName() {
		return this.probabilityPropertyName;
	}

	/**
	 * @param probabilityPropertyName : the name of the property to be used
	 * in {@link #convertToWrapperOutput(File, File)} as the the probabilities.
	 */
	public void setProbabilityPropertyName(String probabilityPropertyName) {
		this.probabilityPropertyName = probabilityPropertyName;
	}

}
