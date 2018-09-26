/**
 * 
 */
package unbbayes.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.io.BaseIO;
import unbbayes.io.DneIO;
import unbbayes.io.EvidenceIO;
import unbbayes.io.FileExtensionIODelegator;
import unbbayes.io.IEvidenceIO;
import unbbayes.io.NetIO;
import unbbayes.io.XMLBIFIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This program runs UnBBayes in command-line mode
 * @author Shou Matsumoto
 */
public class TextModeRunner {
	
	
	/** Name of command line argument for specifying net file. The command line argument should be written like <pre>-n FILENAME </pre>*/
	public static final String NET_FILE_ARGUMENT = "n";
	/** Name of command line argument for specifying evidence (input) file. The command line argument should be written like <pre>-i FILENAME </pre>*/
	public static final String INPUT_ARGUMENT = "i";
	/** Name of command line argument for specifying output (posterior probabilities) file. The command line argument should be written like <pre>-o FILENAME </pre>*/
	public static final String OUT_ARGUMENT = "o";
	/** Name of command line argument for specifying queried node to be included in output file (all nodes will be included if not specified). 
	 * The command line argument should be written like <pre>-q NODENAME </pre>
	 * Multiple entries are allowed by indicating the above pair multiple times.
	 * The value in {@link #getConfiguration()} will be a string which is a comma-separated list of names. */
	public static final String QUERY_ARGUMENT = "q";
	/** Name of command line argument for specifying states of queried nodes (see {@link #QUERY_ARGUMENT}) to be printed. 
	 * The command line argument should be written like <pre>-s INDEX </pre> INDEX is an integer greater than or equal to 1.*/
	public static final String QUERY_STATE_ARGUMENT = "s";
	
	/** Abbreviated name of command line argument that indicates that the output file must contain a preamble. */
	public static final String PREAMBLE_ARGUMENT = "p";
	/** Abbreviated name of command line argument that indicates that program must print a help. */
	public static final String HELP_ARGUMENT = "h";
	
  	
	private Map<String, String> configuration = Collections.EMPTY_MAP;
	
	private BaseIO io;
	private IEvidenceIO evidenceIO;
	
	
	public static void main(String[] args)  {
		TextModeRunner runner = null;
		try {
			runner = TextModeRunner.newInstance(args);
			if (runner.getConfiguration().containsKey(HELP_ARGUMENT)) {
				runner.printHelp();
				return;
			}
			runner.run();
		} catch (Throwable t) {
			Debug.println(TextModeRunner.class, "Error running in test mode", t);
			if (runner != null) {
				runner.printHelp();
			}
		}
	}

	/**
	 * @param args : arguments to check
	 * @return : if there is any argument that can be handled by this class, then returns true. False otherwise.
	 */
	public static boolean hasTextModeCommandLineArgument(String[] args) {
		if (args == null) {
			return false;
		}
		for (String arg : args) {
			if (arg == null) {
				continue;	// ignore null entries
			}
			if (arg.equals("-"+HELP_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+NET_FILE_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+INPUT_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+OUT_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+PREAMBLE_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+QUERY_ARGUMENT)) {
				return true;
			} else if (arg.equals("-"+QUERY_STATE_ARGUMENT)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Default constructor is kept protected to allow extension.
	 * @deprecated use {@link #newInstance(String[])} instead
	 */
	protected TextModeRunner() {}
	
	/**
	 * Constructor method with command line arguments
	 * @param args : command line arguments
	 * @return an instance of {@link TextModeRunner}
	 */
	public static TextModeRunner newInstance(String[] args) {
		TextModeRunner ret = new TextModeRunner();
		
		// prepare an I/O object which delegates to NetIO or DneIO or XMLBIFIO accordingly to file extension
		FileExtensionIODelegator io = FileExtensionIODelegator.newInstance();
		io.getDelegators().add(new NetIO());
		io.getDelegators().add(new DneIO());
		io.getDelegators().add(new XMLBIFIO());
		ret.setIO(io);
		
		ret.setEvidenceIO(new EvidenceIO());
		
		// convert command line arguments to map of arguments
		ret.setConfiguration(ret.extractArguments(args));
		
		return ret;
	}

	/**
	 * Uses {@link #getConfiguration()} in order to load a network from file, compile the network, insert and propagate evidences and print the output.
	 * @throws IOException 
	 * @throws LoadException 
	 * @see #extractArguments(String[])
	 * @see #setConfiguration(Map)
	 * @see #loadNetFromFile()
	 * @see #compileNet(ProbabilisticNetwork)
	 * @see #insertEvidences(ProbabilisticNetwork, IInferenceAlgorithm)
	 * @see #printResults(ProbabilisticNetwork, IInferenceAlgorithm)
	 */
	public void run() throws LoadException, IOException {

		// the network to be used
		ProbabilisticNetwork net = this.loadNetFromFile();
		if (net == null) {
			throw new IllegalArgumentException("Could not load network");
		}


		// prepare the algorithm to compile network
		IInferenceAlgorithm algorithm = this.compileNet(net);
		
		
		// insert and propagate evidence (finding)
		this.insertEvidences(net, algorithm);
		
		
        // print results
		this.printResults(net, algorithm);
	
	}

	/**
	 * Loads a network from a file specified in {@link #getConfiguration()} at key {@link #NET_FILE_ARGUMENT}
	 * by using a {@link BaseIO} object at {@link #getIO()}.
	 * If no file was specified, then "bn.net" will be loaded from program's folder
	 * @return the network loaded from file
	 * @throws LoadException
	 * @throws IOException
	 * @see #run()
	 */
	protected ProbabilisticNetwork loadNetFromFile() throws LoadException, IOException {

		// extract the network file to load BN from.
		String netFileName = getConfiguration().get(NET_FILE_ARGUMENT);
		if (netFileName == null) {
			netFileName = "bn.net";	// default input file if nothing was specified
		}
		return (ProbabilisticNetwork)getIO().load(new File(netFileName));
	}

	/**
	 * Prints the help message
	 */
	public void printHelp() {
		System.out.println("Expected arguments: [-n NETWORK_FILE] [-i EVIDENCE_FILE] [-o OUTPUT_FILE] [-q NODE_TO_QUERY]* [-p] [-s INDEX] [-h]");
		System.out.println("\t -n NETWORK_FILE : the bayesian network to load. Can be a .net (Hugin NET) file, an .xml (XMLBIF) file, or .dne (Netica DNE) file."
				+ " If not spefcified, then bn.net will be loaded from program's root folder.");
		System.out.println("\t -i EVIDENCE_FILE : text file to load evidences from. Comments in this file starts with \"#\". "
				+ "Each line must contain \"NODE_NAME: STATE_NAME\" or \"NODE_NAME: STATE_INDEX\" (without quotes), whose STATE_INDEX is a integer number greater or equal to 1.");
		System.out.println("\t -o OUTPUT_FILE : text file to print results. If not specified, results will be printed to console.");
		System.out.println("\t -q NODE_TO_QUERY : name of nodes to be included in OUTPUT_FILE. If not specified, all nodes will be included. "
				+ "Multiple nodes can be queried by offering multiple entries of the pair -q NODE_TO_QUERY.");
		System.out.println("\t -p : if present, a preamble will be included to output.");
		System.out.println("\t -s INDEX : if present, only the states at spefied index will be printed to output. INDEX is an integer greater or equal to 1.");
		System.out.println("\t -h : if present, this message will be printed to console.");
	}

	/**
	 * Prints the result of compilation and evidence propagation to console or file, accordingly to configuration in {@link #getConfiguration()}
	 * key {@link #OUT_ARGUMENT}, key {@link #QUERY_ARGUMENT}, key {@link #QUERY_STATE_ARGUMENT}, and key {@link #PREAMBLE_ARGUMENT}.
	 * @param net : network to get nodes from. {@link unbbayes.prs.bn.TreeVariable#getMarginalAt(int)} will be printed.
	 * @param algorithm : created by {@link #compileNet(ProbabilisticNetwork)} and used in {@link #insertEvidences(ProbabilisticNetwork, IInferenceAlgorithm)}
	 * for obtaining prior and posterior probabilities.
	 * @see #run()
	 */
	protected void printResults(ProbabilisticNetwork net, IInferenceAlgorithm algorithm) {
		// assertions
		if (net == null || net.getNodes() == null) {
			throw new IllegalArgumentException(net + " is an invalid network.");
		}
		
		// check if output file was specified
		String outputFileName = getConfiguration().get(OUT_ARGUMENT);
		if (outputFileName != null) {
			// redirect system.out to specified output file
			try {
				System.setOut(new PrintStream(new File(outputFileName)));
			} catch (FileNotFoundException e) {
				Debug.println(getClass(), "Error redirecting system.out", e);
			}
		}
		
		if (getConfiguration().containsKey(PREAMBLE_ARGUMENT)) {
			// extract the input file name, because we will print it in the preamble
			String inputFileName = getConfiguration().get(INPUT_ARGUMENT);
			if (inputFileName == null) {
				inputFileName = net.getName();
			}
			
			// print the preamble
			System.out.println("# " + net.getName() + " Results");
			System.out.println("# " + new Date().toString());
			System.out.println("#");
			// TODO check what all these inputs are for
			System.out.println("#INPUT: #");
			System.out.println("#INPUT: # " + inputFileName +" input file");
			System.out.println("#INPUT: #");
			System.out.println("#INPUT:");
			System.out.println("#INPUT: ");
			System.out.println("#INPUT: ");
			System.out.println("#INPUT: ");
			System.out.println("#INPUT: ");
			System.out.println("#INPUT:");
			System.out.println("#INPUT:");
			System.out.println();
			System.out.println("# calculated parameters");
		}
		
		// get the nodes to print probabilities
		List<TreeVariable> nodesToPrint = new ArrayList<TreeVariable>();;
		String queries = getConfiguration().get(QUERY_ARGUMENT);
		if (queries == null || queries.trim().isEmpty()) {
			// consider all nodes
			for (Node node : net.getNodes()) {
				if (node instanceof TreeVariable) {
					nodesToPrint.add((TreeVariable) node);
				}
			}
		} else {
			// this is supposedly a string with comma-separated names of nodes
			for (String nodeName : queries.split(",")) {
				// extract the node with specified name
				Node node = net.getNode(nodeName);
				if ((node != null) && (node instanceof TreeVariable)) {
					nodesToPrint.add((TreeVariable) node);
				} else {
					System.err.println(nodeName + " is an invalid node name.");
				}
			}
		}
		
		// check if we should print all states or just 1 state
		int stateToPrint = -1;
		String queryStateArgument = getConfiguration().get(QUERY_STATE_ARGUMENT);
		if (queryStateArgument != null) {
			try {
				stateToPrint = Integer.parseInt(queryStateArgument);
			} catch (NumberFormatException e) {
				System.err.println("Invalid value for argument -" + QUERY_STATE_ARGUMENT + ": " + queryStateArgument);
				stateToPrint = -1;
			}
		}
		
		// print the probabilities of each node
		for (TreeVariable node : nodesToPrint) {
			if (node.getStatesSize() <= 0) {
				System.err.println(node + " has no states.");
				continue;
			}
			if ((stateToPrint-1) >= node.getStatesSize()) {
				System.err.println(stateToPrint + " is not a valid state for node " + node);
				continue;
			}
			if (stateToPrint >= 0) {
				// print the specified state
				if (nodesToPrint.size() == 1) {
					System.out.print("Prob=");	// there is only 1 node being printed. Simply say "Prob" instead of name of node
				} else {
					System.out.print(node.getName() + "=");
				}
				System.out.print(node.getMarginalAt(stateToPrint==0?0:(stateToPrint-1)));	// if state 0 was specified, print 1st state.
			} else {
				// print all states
				System.out.print(node.getName() + "=" + node.getMarginalAt(0));
				for (int i = 1; i < node.getStatesSize(); i++) {
					System.out.print(","+node.getMarginalAt(i));
				}
			}
			if (nodesToPrint.size() > 1) {
				System.out.println();
			}
		}
	}

	/**
	 * Loads evidences from an input file specified in {@link #getConfiguration()} with key {@link #INPUT_ARGUMENT},
	 * and propagates them in order to get posterior probabilities
	 * @param net : network currently being evaluated
	 * @param algorithm : algorithm to be used in order to propagate evidences. It's usually the same algorithm used to compile
	 * network at {@link #compileNet(ProbabilisticNetwork)}
	 * @throws IOException 
	 * @see #compileNet(ProbabilisticNetwork)
	 * @see #run()
	 */
	protected void insertEvidences(ProbabilisticNetwork net, IInferenceAlgorithm algorithm) throws IOException {
		
		// initial assertions
		if (net == null || algorithm == null) {
			return;
		}
		
		// extract the file to load evidences from
		String inputFileName = getConfiguration().get(INPUT_ARGUMENT);
		if (inputFileName == null) {
			// there is nothing to load, because file was not specified
			return;
		}
		
		
		// instantiate the object responsible for loading evidences from file
		IEvidenceIO evidenceIO = getEvidenceIO();
		if (evidenceIO == null) {
			throw new IllegalStateException("I/O object for evidences was not set.");
		}
		
		// actually load evidences and insert them to net
		evidenceIO.loadEvidences(new File(inputFileName), net);
		
		// propagate the evidences
		algorithm.propagate();
	}

	/**
	 * Compiles a network in order to fill it with prior probabilities.
	 * Subclasses must overwrite this method in order to change the algorithm to be used.
	 * @param net : network to be compiled
	 * @return the inference algorithm used to compile the network.
	 * @see #insertEvidences(ProbabilisticNetwork, IInferenceAlgorithm)
	 * @see #run()
	 */
	protected IInferenceAlgorithm compileNet(ProbabilisticNetwork net) {

		// prepare the algorithm to compile network
		IInferenceAlgorithm algorithm = new JunctionTreeAlgorithm();
		algorithm.setNetwork(net);
		algorithm.run();
		
		return algorithm;
	}

	/**
	 * Converts command line arguments in the format like --name=value to a map with key=name and value=value.
	 * This can be used to convert command line arguments to a format expected by {@link #getConfiguration()}
	 * @param args : command line arguments containing arguments in the format like --name=value
	 * @return : a map with key=name and value=value.
	 * @see #getConfiguration()
	 * @see #setConfiguration(Map)
	 * @see #newInstance(String[])
	 */
	public Map<String, String> extractArguments(String[] args) {
		Map<String, String> ret = new HashMap<String, String>();
		
		if (args != null) {
			// Expected arguments: [-n NETWORK_FILE] [-i EVIDENCE_FILE] [-o OUTPUT_FILE] [-q NODE_TO_QUERY]* [-p] [-s INDEX] [-h]
			for (int i = 0; i < args.length; i++) {
				
				// extract the string at current index
				String arg = args[i];
				
				if (arg.equals("-"+HELP_ARGUMENT)
						|| arg.equals("-"+PREAMBLE_ARGUMENT)) {	// argument without value
					
					ret.put(arg.substring(1), "");	// just indicate that the argument was present, by including the key to the mapping
					
				} else if (arg.equals("-"+QUERY_ARGUMENT)) {	// special case: list of arguments (i.e. multiple occurrences of "-arg value")
					
					// get the string next to it
					i++;
					if (i >= args.length) {
						return ret;
					}
					String value = args[i];
					
					if (!value.startsWith("-")) { // make sure its not another argument
						
						// remove quotes
						if (value.startsWith("\"") && value.endsWith("\"")) {
							value = value.substring(1, value.length()-1);
						}
						
						// append new name to the comma-separated list of node names
						String nodeNames =  ret.get(QUERY_ARGUMENT);
						if (nodeNames == null) {
							nodeNames = value;	// it's the first time QUERY_ARGUMENT was used
						} else {
							nodeNames = nodeNames +  "," + value;	// append at the end (names are separated by commas)
						}
						ret.put(QUERY_ARGUMENT, nodeNames);
					} else {
						i--; // revert
						continue;
					}
					
				} else if ( arg.equals("-" + NET_FILE_ARGUMENT)
						|| arg.equals("-" + INPUT_ARGUMENT )
						|| arg.equals("-" + OUT_ARGUMENT )
						|| arg.equals("-" + QUERY_STATE_ARGUMENT ) ) {	
					// argument in the format "-name value"
					
					// get the string next to it
					i++;
					if (i >= args.length) {
						return ret;
					}
					String value = args[i];
					
					if (!value.startsWith("-")) { // make sure its not another argument
						// remove quotes
						if (value.startsWith("\"") && value.endsWith("\"")) {
							value = value.substring(1, value.length()-1);
						}
						ret.put(arg.substring(1), value);
					} else {
						i--; // revert
						continue;
					}
					
				} else {
					// ignore unknown argument
					continue;
				}
			}
		}
		
		return ret;
	}


	/**
	 * @return the configuration : arguments to be used in {@link #run()}
	 * @see #extractArguments(String[])
	 */
	public Map<String, String> getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration : arguments to be used in {@link #run()}
	 * @see #extractArguments(String[])
	 */
	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the io
	 */
	public BaseIO getIO() {
		return io;
	}

	/**
	 * @param io the io to set
	 */
	public void setIO(BaseIO io) {
		this.io = io;
	}

	/**
	 * @return the evidenceIO : the I/O object to be used to load evidences.
	 * @see #insertEvidences(ProbabilisticNetwork, IInferenceAlgorithm)
	 */
	public IEvidenceIO getEvidenceIO() {
		return evidenceIO;
	}

	/**
	 * @param evidenceIO the evidenceIO to set : the I/O object to be used to load evidences.
	 * @see #insertEvidences(ProbabilisticNetwork, IInferenceAlgorithm)
	 */
	public void setEvidenceIO(IEvidenceIO evidenceIO) {
		this.evidenceIO = evidenceIO;
	}
}
