/**
 * 
 */
package utils;

import io.IMultiColumnCSVToMultiFileConverter;
import io.MultiColumnCSVToMultiFileConverterImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.util.Debug;

/**
 * This is an utility class which converts a single csv with multiple columns (rows represent joint state and columns are the probabilities)
 * to multiple files with single column.
 * @author Shou Matsumoto
 *
 */
public class MultiColumnToMultiFileProbCSVConverter extends ExpectationPrinter {

	private String indicatorPrefix = "I";
	private String detectorPrefix = "D";
	private IMultiColumnCSVToMultiFileConverter io;
	
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
		
		getIO().convert(input, new File(directoryName), getProblemID() + "_" + directoryIndex + "_" + fileIndex + "_");
		
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

	/**
	 * @return the io
	 */
	public IMultiColumnCSVToMultiFileConverter getIO() {
		if (io == null) {
			io = new MultiColumnCSVToMultiFileConverterImpl(new Comparator<String>() {
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
		}
		return io;
	}

	/**
	 * @param io the io to set
	 */
	public void setIO(IMultiColumnCSVToMultiFileConverter io) {
		this.io = io;
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

}
