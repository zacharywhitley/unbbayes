/**
 * 
 */
package utils;

import io.IModelCenterWrapperIO;
import io.ModelCenterWrapperIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class UserActivitySimulatorWrapper {


	public static String[] convertWrapperFileToMainArgs(File input, File output) throws IOException {
		IModelCenterWrapperIO io = ModelCenterWrapperIO.getInstance();
		io.readWrapperFile(input);
		
		List<String> args = new ArrayList<String>();
		
		String property = io.getProperty("activity_histogram_file");
		if (property != null) {
			args.add("-activity_histogram_file");
			args.add(property);
		}
		property = io.getProperty("raw_user_activity_output");
		if (property != null) {
			args.add("-raw_user_activity_output");
			args.add(property);
		}
		property = io.getProperty("transformed_detectors_output");
		if (property != null) {
			args.add("-transformed_detectors_output");
			args.add(property);
		}
		property = io.getProperty("correlation_data_file_folder");
		if (property != null) {
			args.add("-correlation_data_file_folder");
			args.add(property);
		}
		property = io.getProperty("rscript_program_name");
		if (property != null) {
			args.add("-rscript_program_name");
			args.add(property);
		}
		property = io.getProperty("rscript");
		if (property != null) {
			args.add("-rscript");
			args.add(property);
		}
		if (output != null) {
			args.add("-distance_metric_output_file");
			args.add("\""+output.getAbsolutePath() + "\"");
		} else {
			property = io.getProperty("distance_metric_output_file");
			if (property != null) {
				args.add("-distance_metric_output_file");
				args.add(property);
			}
		}
		property = io.getProperty("peer_group_size_file");
		if (property != null) {
			args.add("-peer_group_size_file");
			args.add(property);
		}
		property = io.getProperty("random_seed");
		if (property != null) {
			args.add("-random_seed");
			args.add(property);
		}
		property = io.getProperty("block_cutoff_percent");
		if (property != null) {
			args.add("-block_cutoff_percent");
			args.add(property);
		}
		property = io.getProperty("days_redraw_user_attitude");
		if (property != null) {
			args.add("-days_redraw_user_attitude");
			args.add(property);
		}
		property = io.getProperty("block1_tokens_per_detector_good_user");
		if (property != null) {
			args.add("-block1_tokens_per_detector_good_user");
			args.add(property);
		}
		property = io.getProperty("block2_tokens_per_detector_good_user");
		if (property != null) {
			args.add("-block2_tokens_per_detector_good_user");
			args.add(property);
		}
		property = io.getProperty("block1_tokens_per_detector_bad_user");
		if (property != null) {
			args.add("-block1_tokens_per_detector_bad_user");
			args.add(property);
		}
		property = io.getProperty("block2_tokens_per_detector_bad_user");
		if (property != null) {
			args.add("-block2_tokens_per_detector_bad_user");
			args.add(property);
		}
		property = io.getProperty("bad_user_prob_by_group_size");
		if (property != null) {
			args.add("-bad_user_prob_by_group_size");
			args.add(property);
		}
		property = io.getProperty("total_users");
		if (property != null) {
			args.add("-total_users");
			args.add(property);
		}
		property = io.getProperty("total_days");
		if (property != null) {
			args.add("-total_days");
			args.add(property);
		}
		property = io.getProperty("first_day");
		if (property != null) {
			args.add("-first_day");
			args.add(property);
		}
		property = io.getProperty("test_days_threshold");
		if (property != null) {
			args.add("-test_days_threshold");
			args.add(property);
		}
		property = io.getProperty("number_timeblocks");
		if (property != null) {
			args.add("-number_timeblocks");
			args.add(property);
		}
		property = io.getProperty("total_number_detectors");
		if (property != null) {
			args.add("-total_number_detectors");
			args.add(property);
		}
		
		if (Debug.isDebugMode()) {
			args.add("-d");
		}
				
		return args.toArray(new String[args.size()]);
	}
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("i","input", true, "File to read. If not specified, UserActivitySimulator.in (in same directory of the program) will be used.");
		options.addOption("o","output", true, "File to write. If not specified, UserActivitySimulator.out (in same directory of the program) will be used.");
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
		

		if (cmd.hasOption("h")) {
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		Debug.setDebug(cmd.hasOption("d"));
		
		File input = null;
		if (cmd.hasOption("i")) {
			input = new File(cmd.getOptionValue("i"));
		} else {
			input = new File("UserActivitySimulator.in");
		}
		File output = null;
		if (cmd.hasOption("o")) {
			output = new File(cmd.getOptionValue("o"));
		}
		
		
		UserActivitySimulator.main(convertWrapperFileToMainArgs(input, output));
		
		
	}


}
