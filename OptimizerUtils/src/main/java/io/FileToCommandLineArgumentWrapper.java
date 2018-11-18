/**
 * 
 */
package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.util.Debug;

/**
 * Converts a {@link ModelCenterWrapperIO} property file to command-line arguments.
 * @author Shou Matsumoto
 *
 */
public class FileToCommandLineArgumentWrapper extends ModelCenterWrapperIO {
	
	private String inputFileName = "FileToCommandLineArgumentWrapper.in";

	public FileToCommandLineArgumentWrapper() {
		this.setProgramName("FileToCommandLineArgumentWrapper");
	}
	
	/**
	 * Executes a program specified in {@link #getProgramName()}
	 * with command line arguments read from {@link #getProperties()}
	 * @throws IOException
	 * @throws InterruptedException
	 * @see #readWrapperFile(File)
	 */
	public String runProgram() throws IOException, InterruptedException {
		String program = getProgramName();
		if ( program == null || program.isEmpty()) {
			throw new IOException("Invalid program: " + program);
		}
		
		// build command-line arguments
		String args = buildArgs();
		if (!args.startsWith(" ")) {
			// make sure there's whitespace between program and command line arg
			args = " " + args;
		}
		program += args;
		
		Debug.println(getClass(), "Executing: " + program);
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(program);
		
		int exitVal = pr.waitFor();
		
		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		for (String line = input.readLine(); line != null; line = input.readLine()) {
			System.out.println(line);
		}
		input.close();
		input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		for (String line = input.readLine(); line != null; line = input.readLine()) {
			System.out.println(line);
		}
		input.close();

        if (exitVal != 0) {
        	throw new IOException("Exited program with exit code " + exitVal);
        }
        
        return program;
	
	}

	/**
	 * @return the inputFileName
	 */
	public String getInputFileName() {
		return inputFileName;
	}

	/**
	 * @param inputFileName the inputFileName to set
	 */
	public void setInputFileName(String inputFileName) {
		this.inputFileName = inputFileName;
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("e","execute-program", true, "Program to be executed. This is a required argument.");
		options.addOption("i","input", true, "Input file to read. If not specified, \"FileToCommandLineArgumentWrapper.in\" will be used.");
		options.addOption("d","debug", false, "Enables debug mode.");
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
		

		if (cmd.hasOption("h") || !cmd.hasOption("e")) {
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		Debug.setDebug(cmd.hasOption("d"));
		
		FileToCommandLineArgumentWrapper io = new FileToCommandLineArgumentWrapper();
		io.setProgramName(cmd.getOptionValue("e"));
		
		if (cmd.hasOption("i")) {
			io.setInputFileName(cmd.getOptionValue("i"));
		}
		
		// reads the command line arguments from file
		io.readWrapperFile(new File(io.getInputFileName()));
		
		// execute program with command line arguments
		io.runProgram();
	}

	/**
	 * @return command line arguments for {@link #runProgram()}
	 */
	public String buildArgs() {
		String ret = "";
		Map<String, String> properties = getProperties();
		if (properties != null) {
			for (Entry<String, String> argValue : properties.entrySet()) {	
				// key is argument, value is its value. This will append something like: -i "someFile.txt"
				String arg = " -" + argValue.getKey();
				if (argValue.getValue() != null) {
					arg += " \"" + argValue.getValue() + "\"";
				}
				Debug.println(getClass(), "Handling argument: " + arg);
				ret += arg;
			}
		}
		return ret;
	}

}
