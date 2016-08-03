package utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.Debug;
import cc.mallet.types.Alphabet;
import cc.mallet.types.Dirichlet;
import cc.mallet.util.Randoms;


/**
 * Description from Dr. Kathryn Laskey (klaskey@gmu.edu)
 * <pre>
We seek a method to simulate Threat x Indicator tables that makes use of both sets of data and represents 
uncertainty in the probability distribution due to sampling variation in the data. 
Here is an approach I suggest. Comments are welcome.

We will simulate 100 hypothetical organizations, with 4263 users per organization. 
For each user, we will simulate whether or not the user matches the threat type and the values of all indicators for the user.

Notation:  We use p = (p1, …, pn) to denote the vector of probabilities 
for n = 2#Indicators + 1 combinations of threat x indicators.  
We let 0 < w < 100 denote the weight in the objective function for the threat x indicator tables, 
with 100-w being the weight on the indicator x indicator tables.

The simulation for a hypothetical organization works as follows:

Choose w from the values 20, 50, 80 with probability ⅓ each.
Let pW be the optimal probability vector for w.
Let a = 4263 pW.  This is the “virtual count” parameter for the Dirichlet distribution we will use to simulate p.
Simulate p from a Dirichlet distribution with parameter a.
Simulate the threat and indicator values for 4263 users randomly from the distribution p.

We will do the above simulation 100 times to get 100 threat x indicator combinations.
 * </pre>
 * @author Shou Matsumoto
 */
public class DirichletUserSimulator extends ExpectationPrinter {

	
	private int numUsers = 4263;
	private int numOrganization = 1000;
	private File input = new File("input");
	private File output = new File("output");
	private boolean isToPrintAlert = true;
	private int countAlert = 2;
	
	private boolean isToUseDirichletMultinomial = true;
	private boolean isToReadAlert = false;
	

	/**
	 * Default constructor is kept protected to avoid instantiation, but to allow inheritance.
	 */
	protected DirichletUserSimulator() {
	}
	
	/**
	 * Constructor method initializing fields
	 * @param numUsers
	 * @param numOrganization
	 */
	public static DirichletUserSimulator getInstance(int numUsers, int numOrganization) {
		DirichletUserSimulator ret = new DirichletUserSimulator();
		ret.setNumUsers(numUsers);
		ret.setNumOrganization(numOrganization);
		return ret;
	}
	/**
	 */
	public static DirichletUserSimulator getInstance() {
		return new DirichletUserSimulator();
	}
	
	/**
	 * Notation:  We use p = (p1, …, pn) to denote the vector of joint probabilities.
	 * 
	 * The simulation for a hypothetical organization works as follows:
	 * 
	 * <pre>
	 * Choose p from list of possible joint probabilities.
	 * 
	 * Let a = numUsers*p.  This is the "virtual count" parameter for the Dirichlet distribution we will use to simulate p'.
	 * 
	 * Simulate p' from a Dirichlet distribution with parameter a.
	 * 
	 * Simulate the values for a given number of samples randomly from the distribution p'.
	 * </pre>
	 * @param jointProbabilities
	 * @param output : file where output will be appended.
	 * The column headings will be the variable names as in the first joint table. 
	 * The entry in row i, column j will be 0 if sample i has a false value for the column and 1 if sample i has a true value for the column.
	 * @param numSimulation : number of samples to generate
	 * @throws IOException 
	 */
	public void runSingleSimulation(List<PotentialTable> jointProbabilities, File output, int numSimulation, boolean isToPrintAlert, int alertScore) throws IOException {
		
		// assert that all joint probabilities have same ordering of variables
		for (int i = 1; i < jointProbabilities.size(); i++) {
			this.reorderJointProbabilityTable(jointProbabilities.get(0), jointProbabilities.get(i));
		}
		
		// prepare the stream to print results to
		PrintStream printer = new PrintStream(new FileOutputStream(output, true));	// append if file exists
		
		if (output.length() <= 0) {
			// this is a new file, so start with the header
			// read the variables from 1st table
			PotentialTable referenceTable = jointProbabilities.get(0);
			for (int i = referenceTable.getVariablesSize()-1; i >= 0; i--) {
				printer.print(referenceTable.getVariableAt(i).getName());
				if (i > 0) {
					printer.print(",");
				}
			}
			if (isToPrintAlert) {
				printer.print(",Alert");
			}
			printer.println();
		}
		
		// prepare the object which will be used to sample an individual from some distribution (e.g. uniform, or dirichlet)
		Randoms random = new Randoms();	
		
		// randomly choose a joint prob distribution (assuming uniform distribution)
		PotentialTable table = jointProbabilities.get(random.nextInt(jointProbabilities.size()));
		
		// extract name of variables to be used to calculate the value of the alert variable
		List<String> alertVars = getNameList(getDetectorNames());
		if (alertVars.isEmpty()) {
			alertVars = getNameList(getIndicatorNames());
		}
		
		// calculate dirichlet parameters as expectations from current joint probability.
		// we'll also use it to filter out states that are impossible to happen (for optimization and also to avoid dirichlet with alpha = 0)
		double[] tentativeAlpha = new double[table.tableSize()];
		for (int i = 0; i < tentativeAlpha.length; i++) {
			tentativeAlpha[i] = numSimulation * table.getValue(i);
		}
		
		// initialize dictionary of states of sampler
		List<Object> jointStates = new ArrayList<Object>();
		for (int tableIndex = 0; tableIndex < table.tableSize(); tableIndex++) {
			int[] states = table.getMultidimensionalCoord(tableIndex);
			String csvLine = "";
			int countAlert = 0;
			for (int varIndex = states.length-1; varIndex >= 0 ; varIndex--) {
				String state = table.getVariableAt(varIndex).getStateAt(states[varIndex]).trim();
				if (state.equalsIgnoreCase("Yes") || state.equalsIgnoreCase("true")) {
					csvLine += ("1");
					if (alertVars.contains(table.getVariableAt(varIndex).getName())) {
						countAlert++;
					} 
				} else {
					csvLine += ("0");
				}
				if (varIndex > 0) {
					csvLine += (",");
				}
			}
			if (isToPrintAlert) {
				if (countAlert >= alertScore) {
					csvLine += ",1";
				} else {
					csvLine += ",0";
				}
			}
			if (tentativeAlpha[tableIndex] > 0) {
				jointStates.add(csvLine);
			}
		}
		Alphabet dictionary = new Alphabet(jointStates.toArray(new Object[jointStates.size()]));
		
		double[] distribution = null;
		if (isToUseDirichletMultinomial()) {
			// remove zeros from tentative alpha
			double[] alpha = new double[dictionary.size()];
			for (int indexTentative = 0, indexAlpha = 0; indexTentative < tentativeAlpha.length; indexTentative++) {
				if (tentativeAlpha[indexTentative] > 0) {
					alpha[indexAlpha] = tentativeAlpha[indexTentative];
					indexAlpha++;
				}
			}
			
			// instantiate a dirichlet sampler
			Dirichlet dirichlet = new Dirichlet(alpha, dictionary);
			
			// sample dirichlet distribution
			distribution = dirichlet.nextDistribution();
		} else {
			// sample directly from original distribution
			distribution = new double[table.tableSize()];
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] = table.getValue(i);
			}
		}
		
		// generate and print samples
		for (int i = 0; i < numSimulation; i++) {
			// generate sample, use alphabet (dictionary) to translate it to joint state, and then print joint state
			printer.println(dictionary.lookupObject(random.nextDiscrete(distribution)));
		}
		
		printer.close();
		
	}
	
	/**
	 * Wrapper for methods in this class.
	 * @throws IOException 
	 * @see #setNumUsers(int)
	 * @see #setNumOrganization(int)
	 * @see #getInput()
	 * @see #getOutput()
	 * @see #getJointProbabilityFromFile(File)
	 * @see #runSingleSimulation(List, File, int)
	 */
	public void run() throws IOException {
		File input = this.getInput();
		if (!input.exists()) {
			throw new IOException("File or folder " + input.getName() + " does not exist.");
		}
		
		
		File output = this.getOutput();
		if (!output.exists()) {
			output.mkdirs();
		}
		if (!output.exists()) {
			throw new IOException("Folder " + output.getName() + " could not be created.");
		}
		
		// read input joint probabilities
		List<PotentialTable> jointProbabilities = new ArrayList<PotentialTable>();
		if (input.isDirectory()) {
			// read all files in the directory
			List<File> files = new ArrayList<File>(Arrays.asList(input.listFiles()));
			Collections.sort(files, new Comparator<File>() {
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (File internalFile : files) {
				jointProbabilities.add(this.getJointProbabilityFromFile(internalFile, isToReadAlert()));
			}
		} else if (input.isFile()) {
			// read a single file
			jointProbabilities.add(this.getJointProbabilityFromFile(input, isToReadAlert()));
		} else {
			throw new IllegalArgumentException(input.getName() + " is not a valid accessible file/directory.");
		}
		
		
		if (output.isDirectory()) { // create multiple files in directory
			
			// prefix of filename to create as output
			String prefix = getProblemID();
			if (prefix == null) {
				prefix = "";
			}
			if (!prefix.isEmpty()) {
				prefix += "_";
			}
			
			// how many iterations to run
			int numOrganization = getNumOrganization();
			
			
			for (int i = 1; i <= numOrganization; i++) {
				String numbering = ""+i;
				if (numOrganization < 1000) {
					numbering = String.format("%1$03d", i);
				} else if (numOrganization < 10000) {
					numbering = String.format("%1$04d", i);
				} else if (numOrganization < 100000)  {
					numbering = String.format("%1$05d", i);
				}
				File file = new File(output, prefix + numbering  + ".csv");
				this.runSingleSimulation(jointProbabilities, file, getNumUsers(), isToPrintAlert(), getCountAlert() );
			}
		} else if (output.isFile()) {
			// just run multiple times and append results to same output
			int numOrganization = getNumOrganization();
			for (int i = 0; i < numOrganization; i++) {
				this.runSingleSimulation(jointProbabilities, output, getNumUsers(), isToPrintAlert(), getCountAlert());
			}
		} else {
			throw new IllegalArgumentException(output.getName() + "  is not a valid accessible file/directory.");
		}
		
		
	}
	
	/**
	 * Makes sure the second table has the variables in the same order of the first table
	 * @param reference : read only argument
	 * @param toReorder : read/write argument. May have changes after execution of this method.
	 */
	public void reorderJointProbabilityTable( PotentialTable reference, PotentialTable toReorder) {
		if (reference.getVariablesSize() != toReorder.getVariablesSize()) {
			throw new IllegalArgumentException("Tables have different sizes: " + reference.getVariablesSize() + " != " + toReorder.getVariablesSize());
		}
		
		// check if both tables have same variables
		for (int i = 0; i < reference.getVariablesSize(); i++) {
			boolean found = false;
			for (int j = 0; j < toReorder.getVariablesSize(); j++) {
				if (reference.getVariableAt(i).getName().equals(toReorder.getVariableAt(j).getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException(reference.getVariableAt(i) + " not found.");
			}
		}
		
		// check if order of variables in table are the same
		boolean hasSameOrder = true;
		for (int i = 0; i < reference.getVariablesSize(); i++) {
			if (!reference.getVariableAt(i).getName().equals(toReorder.getVariableAt(i).getName())) {
				hasSameOrder = false;
				break;
			}
		}
		
		if (hasSameOrder) {
			// they are already in same ordering, so nothing needs to be done
			return;
		}
		
		// we need to reorder the variables in second table
		
		// create a mapping from variable name to instance, because we want to use same java object instance from original table
		Map<String, INode> varMap = new HashMap<String, INode>();
		for (int i = 0; i < toReorder.getVariablesSize(); i++) {
			varMap.put(toReorder.getVariableAt(i).getName(), toReorder.getVariableAt(i));
		}
		
		// make a backup, because the original one will be changed
		PotentialTable backup = toReorder.getTemporaryClone();
		
		// remove all the variables and add again from reference table
		while( toReorder.variableCount() > 0 ) {
			toReorder.removeVariable(toReorder.getVariableAt(0), false);
		}
		for (int i = 0; i < reference.getVariablesSize(); i++) {
			// add same java object instances from original table, instead of adding the instances from reference table
			toReorder.addVariable(varMap.get(reference.getVariableAt(i).getName()));
		}
		
		// fill probabilities from backup
		for (int cell = 0; cell < toReorder.tableSize(); cell++) {
			// convert i to corresponding index in backup table, by using coordinates (states of each var at current cell)
			int[] currentCellCoord = toReorder.getMultidimensionalCoord(cell);
			int[] backupCoord = backup.getMultidimensionalCoord(0);
			if (currentCellCoord.length != backupCoord.length) {
				throw new RuntimeException();
			}
			// fill backupCoord with states related to current cell in toReorder
			for (int state = 0; state < backupCoord.length; state++) {
				backupCoord[state] = currentCellCoord[toReorder.getVariableIndex((Node) backup.getVariableAt(state))];
			}
			
			toReorder.setValue(cell, backup.getValue(backupCoord));
		}
		
		// re-check if order of variables in table are the same
		for (int i = 0; i < reference.getVariablesSize(); i++) {
			if (!reference.getVariableAt(i).getName().equals(toReorder.getVariableAt(i).getName())) {
				throw new RuntimeException();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("i","input", true, "File or directory to get joint probabilities from.");
		options.addOption("u","users", true, "Number of users.");
		options.addOption("n","number-repetition", true, "Number of organizations (repetitions of sampled users).");
		options.addOption("o","output", true, "File or directory to place output files.");
		options.addOption("id","problem-id", true, "Name or identification of the current problem (this will be used as suffixes of output file names).");
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("a","alert", true, "Whether to print alert and how many detectors to consider in alert.");
		options.addOption("s","short", false, "Short version (does not consider detectors).");
		options.addOption("h","help", false, "Help.");
		options.addOption("q","quick", false, "Quick sampling (does not use dirichlet multinomial sampling).");
		options.addOption("numI","number-indicators", true, "Number of indicators to consider.");
		options.addOption("numD","number-detectors", true, "Number of detectors to consider.");
		options.addOption("ra","read-alert", false, "Whether to read aleart from probability file.");
		
		
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
		
		DirichletUserSimulator sim = DirichletUserSimulator.getInstance();
		sim.setToConsiderDetectors(!cmd.hasOption("s"));
		sim.setToUseDirichletMultinomial(!cmd.hasOption("q"));
		if (cmd.hasOption("u")) {
			sim.setNumUsers(Integer.parseInt(cmd.getOptionValue("u")));
		}
		if (cmd.hasOption("n")) {
			sim.setNumOrganization(Integer.parseInt(cmd.getOptionValue("n")));
		}
		if (cmd.hasOption("i")) {
			sim.setInput(new File(cmd.getOptionValue("i")));
		}
		if (cmd.hasOption("o")) {
			sim.setOutput(new File(cmd.getOptionValue("o")));
		}
		if (cmd.hasOption("id")) {
			sim.setProblemID(cmd.getOptionValue("id"));
		}
		if (cmd.hasOption("a")) {
			sim.setToPrintAlert(true);
			sim.setCountAlert(Integer.parseInt(cmd.getOptionValue("a")));
		} else {
			sim.setToPrintAlert(false);
		}
		if (cmd.hasOption("ra")) {
			sim.setToReadAlert(true);
			sim.setToPrintAlert(false);
		}

		// generate default names of indicators
		if (cmd.hasOption("numI")) {
			int num = Integer.parseInt(cmd.getOptionValue("numI"));
			String[] names = new String[num];
			for (int i = 1; i <= num; i++) {
				names[i-1] = "I"+i;
			}
			sim.setIndicatorNames(names);
		}
		// generate default names of detectors
		if (cmd.hasOption("numD")) {
			int num = Integer.parseInt(cmd.getOptionValue("numD"));
			String[] names = new String[num];
			for (int i = 1; i <= num; i++) {
				names[i-1] = "D"+i;
			}
			sim.setDetectorNames(names);
		}
		
		try {
			sim.run();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}


	/**
	 * @return the numUsers
	 */
	public int getNumUsers() {
		return numUsers;
	}

	/**
	 * @param numUsers the numUsers to set
	 */
	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
	}

	/**
	 * @return the numOrganization
	 */
	public int getNumOrganization() {
		return numOrganization;
	}

	/**
	 * @param numOrganization the numOrganization to set
	 */
	public void setNumOrganization(int numOrganization) {
		this.numOrganization = numOrganization;
	}


	/**
	 * @return the input
	 */
	public File getInput() {
		return input;
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(File input) {
		this.input = input;
	}

	/**
	 * @return the output
	 */
	public File getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(File output) {
		this.output = output;
	}

	/**
	 * @return the isToPrintAlert
	 */
	public boolean isToPrintAlert() {
		return isToPrintAlert;
	}

	/**
	 * @param isToPrintAlert the isToPrintAlert to set
	 */
	public void setToPrintAlert(boolean isToPrintAlert) {
		this.isToPrintAlert = isToPrintAlert;
	}

	/**
	 * @return the countAlert
	 */
	public int getCountAlert() {
		return countAlert;
	}

	/**
	 * @param countAlert the countAlert to set
	 */
	public void setCountAlert(int countAlert) {
		this.countAlert = countAlert;
	}

	/**
	 * @return the isToUseDirichletMultinomial
	 */
	public boolean isToUseDirichletMultinomial() {
		return isToUseDirichletMultinomial;
	}

	/**
	 * @param isToUseDirichletMultinomial the isToUseDirichletMultinomial to set
	 */
	public void setToUseDirichletMultinomial(boolean isToUseDirichletMultinomial) {
		this.isToUseDirichletMultinomial = isToUseDirichletMultinomial;
		Debug.println("Dirichlet multinomial sampling mode: " + (isToUseDirichletMultinomial?"on":"off"));
	}

	/**
	 * @return the isToReadAlert
	 */
	public boolean isToReadAlert() {
		return isToReadAlert;
	}

	/**
	 * @param isToReadAlert the isToReadAlert to set
	 */
	public void setToReadAlert(boolean isToReadAlert) {
		this.isToReadAlert = isToReadAlert;
	}



}
