package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import utils.JavaSimulatorWrapper;

/**
 * @author Shou Matsumoto
 *
 */
public class ModelCenterWrapperIO implements IModelCenterWrapperIO {
	
	private Map<String, String> properties = new HashMap<String, String>();
	

	/**
	 * @see #getInstance(JavaSimulatorWrapper)
	 */
	protected ModelCenterWrapperIO() {}
	
	/**
	 * @return a new instance
	 */
	public static IModelCenterWrapperIO getInstance() {
		return new ModelCenterWrapperIO();
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
		
		// header
		printer.println("# JavaSimulatorWrapper Results");
		printer.println("# " + new Date().toString());
		printer.println("#");
		printer.println("# calculated parameters");
		
		// properties
		for (Entry<String, String> entry : property.entrySet()) {
			printer.println(entry.getKey() + "=" + entry.getValue());
		}
		
		printer.println();
		
		printer.close();
		
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
	
	
	public String getProperty(String key) {
		return getProperties().get(key);
	}


}
