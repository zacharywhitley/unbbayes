package utils;
import io.ICliqueStructureLoader;
import io.JSONCliqueStructureLoader;

import java.io.File;
import java.io.FileInputStream;
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

import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
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
	private String cliquesFileName = null;
	private boolean isToPrintAlert = true;
	private String conditionalProbabilityFileName = "conditionals.net";
	private int alertStateThreshold = 1;
	
	
	private boolean isToUseDirichletMultinomial = true;
	private boolean isToReadAlert = false;
	private ICliqueStructureLoader cliqueStructureLoader;
	
	private BaseIO conditionalProbabilityLoader;
	private Long seed = null;
	

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
			Boolean alertState = null;	// keep track if we have alert variable in prob dist, and what is its state in current cell
			for (int varIndex = states.length-1; varIndex >= 0 ; varIndex--) {
				String state = table.getVariableAt(varIndex).getStateAt(states[varIndex]).trim();
				if (parseBoolean(state) == true) {
					csvLine += ("1");
					if (alertVars.contains(table.getVariableAt(varIndex).getName())) {
						countAlert++;
					} 
					if (table.getVariableAt(varIndex).getName().equalsIgnoreCase(getAlertName())) {
						alertState = true;	// if this is the alert variable, keep track of its state
					}
				} else {
					csvLine += ("0");
					if (table.getVariableAt(varIndex).getName().equalsIgnoreCase(getAlertName())) {
						alertState = false;	// if this is the alert variable, keep track of its state
					}
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
			} else if (isToReadAlert()) {
				// if we are reading alerts from the prob dist, we need to make sure the alert is only triggered if countAlert >= alertScore
				if (alertState == null) {	// just checking that we found a alert variable in probability distribution
					printer.close();
					throw new IllegalArgumentException("Alert variable " + getAlertName() 
							+ " is supposed to be read from probability distribution, but no state for such variable was found.");
				}
				// check that alert is true if and only if the number of active detectors are greater or equal to the specified score
				if ( ( countAlert >= alertScore ) != alertState) {
					tentativeAlpha[tableIndex] = 0;	// force this state to be impossible in Dirichlet distribution
				}
			}
			if (tentativeAlpha[tableIndex] > 0) {
				jointStates.add(csvLine);
			}
		}
		if (jointStates.size() <= 0) {
			printer.close();
			throw new IllegalArgumentException("No positive probability found.");
		}
		Alphabet dictionary = new Alphabet(jointStates.toArray(new Object[jointStates.size()]));
		
		// remove zeros from tentative alpha
		double[] alpha = new double[dictionary.size()];
		for (int indexTentative = 0, indexAlpha = 0; indexTentative < tentativeAlpha.length; indexTentative++) {
			if (tentativeAlpha[indexTentative] > 0) {
				alpha[indexAlpha] = tentativeAlpha[indexTentative];
				indexAlpha++;
			}
		}
		
		double[] distribution = null;
		if (isToUseDirichletMultinomial()) {
			
			// instantiate a dirichlet sampler
			Dirichlet dirichlet = new Dirichlet(alpha, dictionary);
			
			// sample dirichlet distribution
			distribution = dirichlet.nextDistribution();
		} else {
			// sample directly from original distribution
			distribution = new double[alpha.length];
			for (int i = 0; i < alpha.length; i++) {
				distribution[i] = alpha[i]/((double)numSimulation);
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
	 * Performs the same of {@link #runSingleSimulation(List, File, int, boolean, int)}, but the joint probability
	 * is represented as a junction tree instead of a table.
	 * @param junctionTree
	 * @param conditionals
	 * @param output
	 * @param numSimulation
	 * @param isToPrintAlert
	 * @param alertScore
	 * @throws IOException
	 */
	public void runSingleSimulation(IJunctionTree junctionTree, List<PotentialTable> conditionals, File output, int numSimulation, boolean isToPrintAlert, int alertScore, List<String> variableNames) throws IOException {
		
		// include threat
		int indexOfThreat = variableNames.indexOf(getThreatName());
		if (indexOfThreat < 0) {
			indexOfThreat = 0;
			variableNames.add(indexOfThreat, getThreatName());
		} 
		
		// include unspecified indicators into list of variables
		for (int i = getIndicatorNames().length - 1; i >= 0; i--) {
			String indicatorName = getIndicatorNames()[i];
			if (!variableNames.contains(indicatorName)) {
				variableNames.add(indexOfThreat + 1, indicatorName);
			}
		}
		
		// make sure alert is also in the names of variables
		if (isToPrintAlert && !variableNames.contains(getAlertName())) {
			variableNames.add(getAlertName());	// add at the end
		}
		
		// prepare the stream to print results to
		PrintStream printer = new PrintStream(new FileOutputStream(output, true));	// append if file exists
		
		List<Integer> sample = new ArrayList<Integer>(variableNames.size());
		if (output.length() <= 0) {
			// this is a new file, so start with the header
			for (int i = 0; i < variableNames.size(); i++) {
				String name = variableNames.get(i);
				printer.print(name);
				sample.add(-1);	// initialize with invalid value
				if (i > 0) {
					printer.print(",");
				}
			}
			printer.println();
		}
		
		
		// obtain the root clique
		Clique root = junctionTree.getCliques().get(0);
		while(root.getParent() != null) {
			root = root.getParent();
		}
		
		
		// backup clique potentials in junction tree before starting sampling
		Map<String, INode> nameToNodeMap = new HashMap<String, INode>(); // also create a mapping of all nodes in the junction tree
		for (Clique clique : junctionTree.getCliques()) {
			// we'll overwrite clique tables when sampling, so we need this backup in order to restore to original probability distribution
			clique.getProbabilityFunction().copyData();
			for (Node node : clique.getNodesList()) {
				nameToNodeMap.put(node.getName(), node);
			}
		}
		
		Map<PotentialTable, Dirichlet> samplerCache = new HashMap<PotentialTable, Dirichlet>();	// initialize map to store cache of dirichlet multinomial sampler
		// generate and print samples
		for (int i = 0; i < numSimulation; i++) {
			Collections.fill(sample, -1);	// reset sample list
			
			// sample from clique potentials (i.e. convert clique potentials to 0% and 100%) recursively, from root to leaf cliques
			sampleJunctionTreeRecursive(junctionTree, root, samplerCache, numSimulation);
			
			// fill the sample list from sampled junction tree
			fillSampleFromJunctionTree(sample, variableNames, junctionTree);
			
			// reset the clique potentials before next iteration (and before exiting the loop)
			for (Clique clique : junctionTree.getCliques()) {
				clique.getProbabilityFunction().restoreData();
			}
			
			// fill alert variable
			fillAlert(sample, alertScore, getAlertStateThreshold(), variableNames, nameToNodeMap);
			
			// fill indicators and threat from conditional probabilities;
			fillSampleConditionals(sample, variableNames, conditionals, samplerCache, numSimulation);
			
			// print sample
			for (int sampleIndex = 0; sampleIndex < sample.size(); sampleIndex++) {
				Integer value = sample.get(sampleIndex);
				if (sampleIndex > 0) {
					printer.print(",");
				}
				printer.print(value);
			}
			printer.println();
			
		}
		
		printer.close();
		
	}
	


	/**
	 * 
	 * @param junctionTree
	 * @param root
	 * @param samplerCache
	 * @param expectedCounts 
	 */
	protected void sampleJunctionTreeRecursive(IJunctionTree junctionTree, Clique root, Map<PotentialTable, Dirichlet> samplerCache, float expectedCounts) {
	
		// extract the clique table to sample at current recursive step
		PotentialTable table = root.getProbabilityFunction();
		
		// build a dictionary object (a subset of joint states containing only the joint states that have probabilities higher than 0%)
		// the dictionary will be used as a mapping between sampled state (subset) and actual state (original).
		Alphabet dictionary = null;
		double[] alpha = null;	// dirichlet parameter alpha, not containing zeros
		if (!isToUseDirichletMultinomial() || !samplerCache.containsKey(table)) {
			// instantiate the dictionary from scratch.
			
			// calculate dirichlet parameters as expectations from current probability.
			// we'll also use it to filter out states that are impossible to happen (for optimization and also to avoid dirichlet with alpha = 0)
			double[] fullSpaceAlpha = new double[table.tableSize()];	// this is the alpha containig zeros
			for (int i = 0; i < fullSpaceAlpha.length; i++) {
				fullSpaceAlpha[i] = expectedCounts * table.getValue(i);
			}
			
			// initialize dictionary of states of sampler
			List<Integer> jointStates = new ArrayList<Integer>();
			for (int tableIndex = 0; tableIndex < table.tableSize(); tableIndex++) {
				if (fullSpaceAlpha[tableIndex] > 0f) {
					jointStates.add(tableIndex);
				}
			}
			if (jointStates.size() <= 0) {
				throw new IllegalArgumentException("No positive probability found.");
			}
			dictionary = new Alphabet(jointStates.toArray(new Object[jointStates.size()]));
			
			// remove zeros from alpha
			alpha = new double[dictionary.size()];
			for (int indexTentative = 0, indexAlpha = 0; indexTentative < fullSpaceAlpha.length; indexTentative++) {
				if (fullSpaceAlpha[indexTentative] > 0) {
					alpha[indexAlpha] = fullSpaceAlpha[indexTentative];
					indexAlpha++;
				}
			}
			fullSpaceAlpha = null;	// we don't need alpha with zeros anymore
		} // we dont need dictionary and alpha to be initialized if we'll use dirichlet but it's cached
		
		double[] distribution = null;	// the distribution to sample from. It will be initialized in the if-clause below
		if (isToUseDirichletMultinomial()) {	// need to sample from dirichlet distribution
			// check cache
			Dirichlet dirichlet = samplerCache.get(table);
			if (dirichlet == null) {	// not cached. Instantiate dirichlet object and fill cache
				
				// instantiate a dirichlet sampler (which only considers possible states -- i.e. excludes states with 0% probability)
				dirichlet = new Dirichlet(alpha, dictionary);
				
				// add it to cache
				samplerCache.put(table, dirichlet);
				
			} else {
				// obtain dictionary from cached dirichlet object
				dictionary = dirichlet.getAlphabet();
			}
			
			// sample dirichlet distribution
			distribution = dirichlet.nextDistribution();
			
		} else {
			
			// sample directly from original distribution
			distribution = new double[alpha.length];
			for (int i = 0; i < alpha.length; i++) {
				distribution[i] = alpha[i]/expectedCounts;
			}
			
		}
		
		// prepare the object which will be used to sample an individual from some distribution (e.g. uniform, or dirichlet)
		Randoms random = new Randoms(getSeed().intValue());	
		
		// get a sample from the distribution. Use the dictionary to translate from reduced space (no zeros) to full space (with zeros)
		int jointState = (Integer)dictionary.lookupObject(random.nextDiscrete(distribution));
		
		// update root clique potential (i.e. fill the joint state with 100% and other states with 0%);
		table.fillTable(0f);
		table.setValue(jointState, 1f);
		
		// propagate to neighbors (only to children, because we are assuming that the sampling starts from root and goes to leaves);
		for (Clique child : root.getChildren()) {
			
			// Propagate to child table (i.e. make sure that only the joint states corresponding to parent's sampled state will be sampled in child).
			propagate(root, child);
			
			sampleJunctionTreeRecursive(junctionTree, child, samplerCache, expectedCounts);
		}
		
	}
	

	/**
	 * Propagates the probabilities from a clique to another clique.
	 * @param to
	 * @param from : it's assumed to have only values 0 or 1
	 */
	protected void propagate(Clique from, Clique to) {
		
		// we don't need to perform a complete propagation, 
		// because we are assuming that the "from" clique contains only 0 and 1.
		// The "from" table has 1 to sampled joint state and 0 to all other states.
		// so multiplying it to the "to" table will ensure states incompatible with sample will be 0%.
		
		// But we must only consider (propagate) common variables
		PotentialTable commonTable = from.getProbabilityFunction().getTemporaryClone();
		
		// remove variables not in common
		List<Node> varsToRemove = new ArrayList<Node>(from.getNodesList());
		varsToRemove.removeAll(to.getNodesList());
		if (varsToRemove.size() == from.getNodesList().size()) {
			// we don't have variables in common, so no need for propagation
			Debug.println(getClass(), "No intersection between cliques: " + from + ", " + to);
			return;
		}
		for (Node varToRemove : varsToRemove) {
			commonTable.removeVariable(varToRemove);
		}
		
		to.getProbabilityFunction().opTab(commonTable, PotentialTable.PRODUCT_OPERATOR);
		to.getProbabilityFunction().normalize();	// make sure sum of cells in child table is still 1
		
    }
	
	/**
	 * 
	 * @param sample
	 * @param junctionTree. It's assumed that all cliques in this junction tree has 1 joint state with 100% probability
	 */
	protected void fillSampleFromJunctionTree(List<Integer> sample, List<String> variableNames, IJunctionTree junctionTree) {
		
		if (junctionTree == null || junctionTree.getCliques() == null || junctionTree.getCliques().isEmpty() 
				|| sample == null || sample.isEmpty()) {
			return;	// there is nothing to sample
		}
		
		// get samples from cliques, one by one
		for (Clique clique : junctionTree.getCliques()) {
			PotentialTable table = clique.getProbabilityFunction();
			
			// extract the joint state which was sampled
			int[] coord = null;
			for (int cell = 0; cell < table.tableSize(); cell++) {
				if (table.getValue(cell) >= 0.99995f) {	// if current value is 1 (with enough error margin for 4 digits) then choose this state
					coord = table.getMultidimensionalCoord(cell);
					break;
				}
			}
			if (coord == null) {
				throw new IllegalArgumentException("No state with 100% probability was found in clique " + clique + ". It must be sampled before filling the final list of samples.");
			}
			
			// fill the sample by using this coord. 
			// Note: this will overwrite samples from previous cliques, but we are assuming that cliques were sampled in a total order, given samples from previous cliques
			// so all samples for common variables must be the same
			
			for (int varIndex = 0; varIndex < coord.length; varIndex++) {
				INode var = table.getVariableAt(varIndex);
				// extract the index of current variable in the list of samples
				int sampleIndex = variableNames.indexOf(var.getName());
				if (sampleIndex >= 0) {
					sample.set(sampleIndex, coord[varIndex]);
				} else {
					Debug.println(getClass(), "Unable to find variable " + var + " in list to fill samples.");
				}
			}
			
		}
		
	}
	

	/**
	 * 
	 * @param sample
	 * @param conditionals
	 * @param variableNames
	 * @param samplerCache 
	 * @param expectedCounts 
	 */
	protected void fillSampleConditionals(List<Integer> sample, List<String> variableNames, List<PotentialTable> conditionals, Map<PotentialTable, Dirichlet> samplerCache, float expectedCounts) {
		
		if (sample == null || sample.isEmpty()) {
			return;	// there is nothing to fill
		}


		
		// iterate on each conditionals and sample from dirichlet;
		for (PotentialTable table : conditionals) {

			// extract the variable to sample (it's always the 1st node)
			INode varToSample = table.getVariableAt(0);
			
			// build a dictionary object (a subset of joint states containing only the joint states that have probabilities higher than 0%)
			// the dictionary will be used as a mapping between sampled state (subset) and actual state (original).
			Alphabet dictionary = null;
			double[] alpha = null;	// dirichlet parameter alpha, not containing zeros
			if (!isToUseDirichletMultinomial() || !samplerCache.containsKey(table)) {
				// instantiate the dictionary from scratch.
				

				// extract the states of parents of variable to sample
				int[] coord = table.getMultidimensionalCoord(0);	// this will be filled with the 1st cell in table corresponding with states of parents
				for (int varInTable = 1; varInTable < table.getVariablesSize(); varInTable++) {
					INode parent = table.getVariableAt(varInTable);
					
					// extract the index of current parent in the sample list
					int indexInSample = variableNames.indexOf(parent.getName());
					if (indexInSample < 0) {
						throw new IllegalArgumentException("Node " + parent + " not found in list of variables to sample: " + variableNames);
					}
					
					coord[varInTable] = sample.get(indexInSample);
				}
				
				// calculate dirichlet parameters as expectations from current probability.
				// we'll also use it to filter out states that are impossible to happen (for optimization and also to avoid dirichlet with alpha = 0)
				double[] fullSpaceAlpha = new double[varToSample.getStatesSize()];	// this is the alpha containig zeros
				for (int state = 0; state < fullSpaceAlpha.length; state++) {
					coord[0] = state;	// index 0 is for the state of variable to sample. Other indexes were already filled with proper states of parent nodes
					fullSpaceAlpha[state] = table.getValue(coord) * expectedCounts;
				}
				
				// initialize dictionary of states of sampler

				// just fill entries in alphabet (dictionary) with indexes of states (starting from 0).
				Integer[] entries = new Integer[varToSample.getStatesSize()];
				for (int i = 0; i < entries.length; i++) {
					entries[i] = i;
				}
				
				dictionary = new Alphabet(entries);
				
				
				// remove zeros from alpha
				alpha = new double[dictionary.size()];
				for (int indexTentative = 0, indexAlpha = 0; indexTentative < fullSpaceAlpha.length; indexTentative++) {
					if (fullSpaceAlpha[indexTentative] > 0) {
						alpha[indexAlpha] = fullSpaceAlpha[indexTentative];
						indexAlpha++;
					}
				}
				fullSpaceAlpha = null;	// we don't need alpha with zeros anymore
			} // we dont need dictionary and alpha to be initialized if we'll use dirichlet but it's cached
			
			double[] distribution = null;	// the distribution to sample from. It will be initialized in the if-clause below
			if (isToUseDirichletMultinomial()) {	// need to sample from dirichlet distribution
				// check cache
				Dirichlet dirichlet = samplerCache.get(table);
				if (dirichlet == null) {	// not cached. Instantiate dirichlet object and fill cache
					
					// instantiate a dirichlet sampler (which only considers possible states -- i.e. excludes states with 0% probability)
					dirichlet = new Dirichlet(alpha, dictionary);
					
					// add it to cache
					samplerCache.put(table, dirichlet);
					
				} else {
					// obtain dictionary from cached dirichlet object
					dictionary = dirichlet.getAlphabet();
				}
				
				// sample dirichlet distribution
				distribution = dirichlet.nextDistribution();
				
			} else {
				
				// sample directly from original distribution
				distribution = new double[alpha.length];
				for (int i = 0; i < alpha.length; i++) {
					distribution[i] = alpha[i]/expectedCounts;
				}
				
			}
			
			// prepare the object which will be used to sample an individual from some distribution (e.g. uniform, or dirichlet)
			Randoms random = new Randoms(getSeed().intValue());	
			
			// get a sample from the distribution. Use the dictionary to translate from reduced space (no zeros) to full space (with zeros)
			int sampleState = (Integer)dictionary.lookupObject(random.nextDiscrete(distribution));
			
			// get the index of variable in sample list
			int indexOfVarInSample = variableNames.indexOf(varToSample.getName());
			if (indexOfVarInSample < 0) {
				throw new IllegalArgumentException("Variable " + varToSample + " not found in list of variables to sample: " + variableNames);
			}
			
			// overwrite sample list
			sample.set(indexOfVarInSample, sampleState);
			
		}	// end of for each conditional table
		
	}

	/**
	 * This method overwrites an entry in sample which represents the alert variable (i.e. variable with name {@link #getAlertName()}).
	 * It uses the states of other variables in sample to decide the value to write.
	 * TODO implement a more customizable system alert calculation when there are more than
	 * 2 states for each variable.
	 * @param sample
	 * @param alertScore : if this ammount of alert score is accumulated, then alert variable is true.
	 * @param variableNames
	 * @param nameToNodeMap 
	 * @param triggerStateThreshold : if the state of sample is greater than or equal to this value, 1 point will be added to alert score.
	 */
	protected void fillAlert(List<Integer> sample, int alertScore, int triggerStateThreshold, List<String> variableNames, Map<String, INode> nameToNodeMap) {
		
		if (nameToNodeMap == null || nameToNodeMap.isEmpty()) {
			throw new IllegalArgumentException("Current version cannot handle system alerts when no random variable is specified.");
		}
		if (sample == null || sample.isEmpty()) {
			return;	// there is nothing to fill
		}
		
		// extract in advance the index to be overwritten by this method
		int alertIndex = variableNames.indexOf(getAlertName());
		if (alertIndex < 0) {
			throw new IllegalArgumentException("Unable to find alert variable " + getAlertName() + " in a sample of variables " + variableNames);
		}
		
		// extract which state in alert variable represents the "true"
		Integer trueIndex = null;
		INode alertVar = nameToNodeMap.get(getAlertName());
		if (alertVar == null) {
			throw new IllegalArgumentException("Unable to find alert variable " + getAlertName() + " in name mapping " + nameToNodeMap);
		}
		for (int stateIndex = 0; stateIndex < alertVar.getStatesSize(); stateIndex++) {
			Boolean isTrue = this.parseBoolean(alertVar.getStateAt(stateIndex));
			if (isTrue == null) {
				throw new IllegalArgumentException("Unknown alert state: " + alertVar.getStateAt(stateIndex));
			} else if (isTrue) {
				trueIndex = stateIndex;
				break;
			}
		}
		
		// iterate on names of variables to be considered to calculate alert value
		int numTriggers = 0;	// how many variables triggered alert. If this value is higher than alertScore, then alert score must be triggered.
		for (String alertTriggerName : getNameList(getDetectorNames())) {
			// extract the index of current trigger in the sample
			int indexOfTrigger = variableNames.indexOf(alertTriggerName);
			if (indexOfTrigger < 0) {
				System.err.println("Alert trigger not found in sample. Trigger = " + alertTriggerName + ", variables of sample = " + variableNames);
				continue;
			}
			
			// get what is the state of the current trigger in the sample
			int triggerState = sample.get(indexOfTrigger);
			
			// if state is greater or equal to threshold, then increment number of triggers we detected
			if (triggerState >= triggerStateThreshold) {
				numTriggers++;
			}
		}
		
		if (numTriggers >= alertScore) {
			// system alert is true
			sample.set(alertIndex, trueIndex);
		} else {
			// system alert is false
			sample.set(alertIndex, 1-trueIndex);
		}
		
	}

	/**
	 * Wrapper for methods in this class.
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @see #setNumUsers(int)
	 * @see #setNumOrganization(int)
	 * @see #getInput()
	 * @see #getOutput()
	 * @see #getJointProbabilityFromFile(File)
	 * @see #runSingleSimulation(List, File, int)
	 */
	public void run() throws IOException, InstantiationException, IllegalAccessException {
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
		
		// load junction tree (structure only) if configuration was specified
		IJunctionTree junctionTree = null;
		List<String> variableNames = null;
		List<PotentialTable> conditionals = new ArrayList<PotentialTable>();
		if (getCliquesFileName() != null && !getCliquesFileName().isEmpty()) {
			ICliqueStructureLoader loader = getCliqueStructureLoader();
			loader.load(new FileInputStream(getCliquesFileName()));
			junctionTree = loader.getJunctionTree();
			variableNames = loader.getVariableNames();
			if (getConditionalProbabilityFileName() != null && !getConditionalProbabilityFileName().trim().isEmpty()) {
				conditionals = loadConditionals(getConditionalProbabilityFileName());
			}
		}
		
		
		// read input probabilities
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
				PotentialTable tableInFile = this.getJointProbabilityFromFile(internalFile, isToReadAlert());
				if (junctionTree != null) {
					// read clique ID from file name;
					int cliqueID = getCliqueIDFromFileName(internalFile.getName());	// TODO use a better way to pass clique names
					
					// find table of clique with respective ID
					PotentialTable tableInClique = null;
					for (Clique clique : junctionTree.getCliques()) {
						if (clique.getInternalIdentificator() == cliqueID) {
							tableInClique = clique.getProbabilityFunction();
							break;
						}
					}
					
					// fill current clique table;
					fillCliquePotential(tableInClique, tableInFile);
				} else {
					// add to list of joint probabilities to consider
					jointProbabilities.add(tableInFile);
				}
			}
		} else if (input.isFile() 
				&& (getCliquesFileName() == null || getCliquesFileName().isEmpty())) {	// there is no clique to read
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
				if (junctionTree == null) {
					this.runSingleSimulation(jointProbabilities, file, getNumUsers(), isToPrintAlert(), getCountAlert() );
				} else {
					// simulate from cliques instead of set of joint probabilities
					this.runSingleSimulation(junctionTree, conditionals, file, getNumUsers(), isToPrintAlert(), getCountAlert(), variableNames);
				}
			}
		} else if (output.isFile()) {
			// just run multiple times and append results to same output
			int numOrganization = getNumOrganization();
			for (int i = 0; i < numOrganization; i++) {
				if (junctionTree == null) {
					this.runSingleSimulation(jointProbabilities, output, getNumUsers(), isToPrintAlert(), getCountAlert());
				} else {
					// simulate from cliques instead of set of joint probabilities
					this.runSingleSimulation(junctionTree, conditionals, output, getNumUsers(), isToPrintAlert(), getCountAlert(), variableNames);
				}
			}
		} else {
			throw new IllegalArgumentException(output.getName() + "  is not a valid accessible file/directory.");
		}
		
		
	}
	
	/**
	 * @param conditionalProbabilityFileName : Hugin net file to load conditional probabilities from.
	 * Only non-root nodes will be read.
	 * @return conditional probabilities of non-root nodes.
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public List<PotentialTable> loadConditionals(String conditionalProbabilityFileName) throws LoadException, IOException {
		// load the file
		Graph net = getConditionalProbabilityLoader().load(new File(conditionalProbabilityFileName));
		
		List<PotentialTable> ret = new ArrayList<PotentialTable>(net.getNodeCount());	// list to return
		
		// extract non-root nodes
		for (Node node : net.getNodes()) {
			if (node.getParentNodes() != null && !node.getParentNodes().isEmpty()
					&& (node instanceof ProbabilisticNode)) {
				// this node has parents, so it's not a root node. It is a random variable, so it should have a probability function
				ret.add(((ProbabilisticNode)node).getProbabilityFunction());
			}
		}
		
		return ret;
	}

	/**
	 * Reads values from one table and overwrites another table.
	 * @param tableToFill : table to be filled
	 * @param tableToRead : table to be read
	 * @see PotentialTable#getValue(int)
	 * @see PotentialTable#setValue(int, float)
	 */
	protected void fillCliquePotential(PotentialTable tableToFill, PotentialTable tableToRead) {
		
		// create a copy of tableInFile which uses variables from tableInClique (to enforce the variables to be the same)
		// but before that, keep a mapping between names and variable objects (must use objects from tableToFill)
		Map<String, INode> nameToVarMap = new HashMap<String, INode>();
		for (int i = 0; i < tableToFill.variableCount(); i++) {
			nameToVarMap.put(tableToFill.getVariableAt(i).getName(), tableToFill.getVariableAt(i));
		}
		
		// now, create the copy (but replacing variables with variables in tableToFill--with same names)
		ProbabilisticTable copy = new ProbabilisticTable();
		for (int i = 0; i < tableToRead.variableCount(); i++) {
			INode variableToRead = tableToRead.getVariableAt(i);				// extract the current variable from tableToRead
			INode variableToFill = nameToVarMap.get(variableToRead.getName());	// extract equivalent (but not same) variable from tableToFill
			if (variableToFill == null) {
				throw new IllegalArgumentException("Variable " + variableToRead + " not found in table to fill.");
			}
			copy.addVariable(variableToFill);
		}
		copy.setValues(tableToRead.getValues());	// also copy the content (cells of table)
		
		// initialize tableInClique with 1 (null value of multiplication)
		tableToFill.fillTable(1f);
		
		// multiply tableInClique and tableInFile
		tableToFill.opTab(copy, tableToFill.PRODUCT_OPERATOR);
		
	}

	/**
	 * @param fileName
	 * @return fileName converted to an ID that can be used for {@link IRandomVariable#getInternalIdentificator()}.
	 */
	protected int getCliqueIDFromFileName(String fileName) {
		// obtain what's before underscore _
		fileName = fileName.trim().split("_")[0];
		// remove all non-numeric characters
		fileName = fileName.replaceAll("[^0-9]", " ").trim();	// keep non-leading and non-trailing white spaces, so that we can have errors when there are non-numeric characters between numbers
		return Integer.parseInt(fileName);
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

	/**
	 * @return the cliquesFileName
	 */
	public String getCliquesFileName() {
		return cliquesFileName;
	}

	/**
	 * @param cliquesFileName the cliquesFileName to set
	 */
	public void setCliquesFileName(String cliquesFileName) {
		this.cliquesFileName = cliquesFileName;
	}



	/**
	 * @return the cliqueStructureLoader
	 */
	public ICliqueStructureLoader getCliqueStructureLoader() {
		if (cliqueStructureLoader == null) {
			cliqueStructureLoader = new JSONCliqueStructureLoader();
		}
		return cliqueStructureLoader;
	}

	/**
	 * @param cliqueStructureLoader the cliqueStructureLoader to set
	 */
	public void setCliqueStructureLoader(ICliqueStructureLoader cliqueStructureLoader) {
		this.cliqueStructureLoader = cliqueStructureLoader;
	}

	

	/**
	 * @return the conditionalProbabilityFileName
	 */
	public String getConditionalProbabilityFileName() {
		return conditionalProbabilityFileName;
	}

	/**
	 * @param conditionalProbabilityFileName the conditionalProbabilityFileName to set
	 */
	public void setConditionalProbabilityFileName(
			String conditionalProbabilityFileName) {
		this.conditionalProbabilityFileName = conditionalProbabilityFileName;
	}


	/**
	 * @return the conditionalProbabilityLoader : this will be used to load file containing conditional probabilities
	 * @see #loadConditionals(String)
	 */
	public BaseIO getConditionalProbabilityLoader() {
		if (conditionalProbabilityLoader == null) {
			conditionalProbabilityLoader = new NetIO();
		}
		return conditionalProbabilityLoader;
	}

	/**
	 * @param conditionalProbabilityLoader the conditionalProbabilityLoader to set : this will be used to load file containing conditional probabilities
	 * @see #loadConditionals(String)
	 */
	public void setConditionalProbabilityLoader(
			BaseIO conditionalProbabilityLoader) {
		this.conditionalProbabilityLoader = conditionalProbabilityLoader;
	}

	/**
	 * @return the seed. If {@link #setSeed(Long)} is set to null, then this method will return {@link System#currentTimeMillis()}
	 */
	public Long getSeed() {
		if (seed == null) {
			return System.currentTimeMillis();
		}
		return seed;
	}

	/**
	 * @param seed the seed to set. Set it to null in order to make {@link #getSeed()} to return {@link System#currentTimeMillis()}
	 */
	public void setSeed(Long seed) {
		this.seed = seed;
	}


	/**
	 * @return the alertStateThreshold : if states of variables that contributes to alert (i.e. {@link #getDetectorNames()})
	 * are greater than or equal to this value, then {@link #fillAlert(List, int, int, List, Map)} will
	 * consider this variable in the count to trigger alert. If such count is greater than or equal to {@link #getCountAlert()},
	 * then system alert will be triggered.
	 * @see #getAlertName()
	 * @see #getCountAlert()
	 */
	public int getAlertStateThreshold() {
		return alertStateThreshold;
	}

	/**
	 * @param alertStateThreshold the alertStateThreshold to set : if states of variables that contributes to alert (i.e. {@link #getDetectorNames()})
	 * are greater than or equal to this value, then {@link #fillAlert(List, int, int, List, Map)} will
	 * consider this variable in the count to trigger alert. If such count is greater than or equal to {@link #getCountAlert()},
	 * then system alert will be triggered.
	 * @see #getAlertName()
	 * @see #getCountAlert()
	 */
	public void setAlertStateThreshold(int alertStateThreshold) {
		this.alertStateThreshold = alertStateThreshold;
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
		options.addOption("threshold","alert-threshold", true, "If states of detectors are greater than or equal to this value, then it will contribute to counts to trigger system alert.");
		options.addOption("s","short", false, "Short version (does not consider detectors).");
		options.addOption("h","help", false, "Help.");
		options.addOption("q","quick", false, "Quick sampling (does not use dirichlet multinomial sampling).");
		options.addOption("numI","number-indicators", true, "Number of indicators to consider.");
		options.addOption("numD","number-detectors", true, "Number of detectors to consider.");
		options.addOption("ra","read-alert", false, "Whether to read aleart from probability file.");
		options.addOption("threat","threat-name", true, "Name of threat variable to consider.");
		options.addOption("cliques","clique-structure-file-name", true, "Name of file (JSON format) to load clique structrue."
				+ " If this is provided, then the input file directory (specified with -i) must contain files with names specifying"
				+ " the clique name in the format <CLIQUE_NAME>_<NUMBER_TO_AVOID_DUPLICATE>.csv. For instance, C0_1.csv");
		options.addOption("cond","conditional-prob-file-name", true, "Name of file (hugin .net file) containing conditional probabilities.");
		
		
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
		if (cmd.hasOption("cond")) {
			sim.setConditionalProbabilityFileName(cmd.getOptionValue("cond"));
		}
		if (cmd.hasOption("threshold")) {
			sim.setAlertStateThreshold(Integer.parseInt(cmd.getOptionValue("threshold")));
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
		
		if (cmd.hasOption("threat")) {
			sim.setThreatName(cmd.getOptionValue("threat"));
		}
		
		if (cmd.hasOption("cliques")) {
			sim.setCliquesFileName(cmd.getOptionValue("cliques"));
		}
		
		try {
			sim.run();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}


}
