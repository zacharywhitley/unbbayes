/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiRowTabSeparatedDataToMultiCSVConverter extends
		MultiColumnCSVToMultiFileConverterImpl {

	/**
	 * 
	 */
	public MultiRowTabSeparatedDataToMultiCSVConverter() {
		setThreatName("");
		setAlertName("");
		setIndicatorNames(null);
		setDetectorNames(null);
	}
	
	

	/* (non-Javadoc)
	 * @see io.MultiColumnCSVToMultiFileConverterImpl#convert(java.io.File, java.io.File, java.lang.String)
	 */
	public void convert(File input, File outputDirectory, String prefix) throws IOException {
		if (input == null || !input.exists()) {
			throw new IllegalArgumentException("Invalid input file: " + input);
		}
		
		// if it is a directory, read all files in the directory
		if (input.isDirectory()) {
			List<File> files = new ArrayList<File>(Arrays.asList(input.listFiles()));
			for (File internalFile : files) {
				this.convert(internalFile, outputDirectory, input.getName() + "." + prefix);
			}
			return;
		}
		
		
		if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
			throw new IOException("Unable to create directory " + outputDirectory);
		}
		
		if (!outputDirectory.isDirectory()) {
			throw new IOException(outputDirectory + " is not a valid directory.");
		}
		

		// set up file tokenizer
		BufferedReader reader = new BufferedReader(new FileReader(input));
		StreamTokenizer st = new StreamTokenizer(reader);
		st.resetSyntax();
		// java.io.StreamTokenizer fails to parse scientific notation like "1.09E-008", so I'm parsing such numbers as words.
		st.wordChars('0', '9'); 
		st.wordChars('e', 'e');
		st.wordChars('E', 'E'); 
		st.wordChars('.', '.'); 
		st.wordChars('+', '+'); 
		st.wordChars('-', '-'); 
		st.whitespaceChars('\r','\r');
		st.whitespaceChars(' ',' ');
		st.whitespaceChars('\t','\t');
		st.whitespaceChars(',', ',');	// ignore commas
		st.whitespaceChars(';', ';');	// ignore semicollons
		st.quoteChar('\'');		
		st.quoteChar('"');
		st.eolIsSignificant(true);	// declaration must be in same line
		

		List<String> indicatorNameList = this.getNameList(getIndicatorNames());
		List<String> detectorNameList = this.getNameList(getDetectorNames());
		
		List<String> allVariableList = new ArrayList<String>();
		if (getThreatName() != null && !getThreatName().trim().isEmpty()) {
			allVariableList.add(getThreatName());
		}
		allVariableList.addAll(indicatorNameList);
		allVariableList.addAll(detectorNameList);
		if (getAlertName() != null && !getAlertName().trim().isEmpty()) {
			allVariableList.add(getAlertName());
		}
		
		PotentialTable potentialTable = getJointTable(null, allVariableList);
		
		// read row of file and write to output
		for (int numJointProbsRead = 0; numJointProbsRead < Integer.MAX_VALUE; numJointProbsRead++) {
			
			// fill potentialTable with values read from file
			boolean isToWriteFile = true;
			for (int i = 0; i < potentialTable.tableSize(); i++) {
				st.nextToken();
				if (st.ttype == st.TT_EOL || st.ttype == st.TT_EOF) {
					if (i <= 0) {
						isToWriteFile = false;
						break;	// it was an empty line
					}
					throw new IOException("Joint probability should have size " + potentialTable.tableSize()
							+ ", but joint probability in file had size " + i);
				}
				if (st.ttype == st.TT_WORD) {
					try {
						potentialTable.setValue(i, Float.parseFloat(st.sval));
					} catch (RuntimeException e) {
						throw new IOException("Failed to parse token. Token code: " + (st.ttype) + ", string = " + st.sval, e);
					}
				} else {
					throw new IOException("Non-number token found. Token code: " + (st.ttype) + ", string = " + st.sval);
				}
			}
			
			st.nextToken();
			
			// next token should be EOL or EOF
			if (st.ttype != st.TT_EOF && st.ttype != st.TT_EOL) {
				throw new IOException("End of line/file expected at line " + st.lineno() + " after " + potentialTable.tableSize() + "  values.");
			}
			
			if (isToWriteFile) {
				// create a new file to print table
				File output = new File(outputDirectory, prefix + numJointProbsRead + ".csv");
				
				// prepare the stream to print results to
				PrintStream printer = new PrintStream(new FileOutputStream(output, false));	// do not append if file exists
				
				for (int tableIndex = 0; tableIndex < potentialTable.tableSize(); tableIndex++) {
					printer.println(potentialTable.getValue(tableIndex));
				}
				
				printer.close();
			}
			
			
			if (st.ttype == st.TT_EOF) {
				break;
			}
		}
		
		reader.close();
		
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("inames","indicator-names", true, "Comma-separated names of indicators.");
		options.addOption("dnames","detector-names", true, "Comma-separated names of detectors.");
		options.addOption("threat","threat-name", true, "Name of threat variable.");
		options.addOption("alert","alert-name", true, "Name of alert variable.");
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
		
		MultiRowTabSeparatedDataToMultiCSVConverter converter = new MultiRowTabSeparatedDataToMultiCSVConverter();
		if (cmd.hasOption("i")) {
			converter.setDefaultJointProbabilityInputFileName(cmd.getOptionValue("i"));
		}
		if (cmd.hasOption("o")) {
			// use these attributes as directory where to output tables
			converter.setPrimaryTableDirectoryName(cmd.getOptionValue("o"));
		}
		if (cmd.hasOption("id")) {
			converter.setProblemID(cmd.getOptionValue("id"));
		}
		

		if (cmd.hasOption("inames")) {
			converter.setIndicatorNames(cmd.getOptionValue("inames").split("[,:]"));
		}
		if (cmd.hasOption("dnames")) {
			converter.setDetectorNames(cmd.getOptionValue("dnames").split("[,:]"));
		}
		if (cmd.hasOption("threat")) {
			converter.setThreatName(cmd.getOptionValue("threat"));
		}
		if (cmd.hasOption("alert")) {
			converter.setAlertName(cmd.getOptionValue("alert"));
		}
		
		
		try {
			converter.convert(new File(converter.getDefaultJointProbabilityInputFileName()), 
					new File(converter.getPrimaryTableDirectoryName()), 
					converter.getProblemID());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
