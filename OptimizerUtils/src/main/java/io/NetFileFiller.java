/**
 * 
 */
package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.inference.TestUtils;

import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.NoisyMaxCPTConverter;
import utils.ChiSqureTestWithZero;

/**
 * Reads a csv file and fills a hugin net file.
 * @author Shou Matsumoto
 *
 */
public class NetFileFiller extends CSVJointDistributionReader implements INetFileFiller {

	private NetIO netIO;
	private boolean isToTreatRootNodes = false;
	private boolean isToGenerateNoisyOr = true;
	private int numIteration = 1000;
	private int numTrials = 50;
	private float numVirtualCount = 144f;

	/**
	 * Default constructor
	 */
	public NetFileFiller() {
		this.setIncrementCounts(1f);
		this.setIdColumn(Integer.MIN_VALUE);
	}

	/*
	 * (non-Javadoc)
	 * @see io.INetFileFiller#fillNetFile(java.io.File, java.io.File)
	 */
	public void fillNetFile(File input, File netFile) throws IOException {
		
		// extract the object to be used to read/write network file
		NetIO netIO = this.getNetIO();
		
		// load the network structure from net file
		ProbabilisticNetwork net = (ProbabilisticNetwork) netIO.load(netFile);
		
		// this will be used to normalize conditional probabilities
		NormalizeTableFunction conditionalNormalizer = new NormalizeTableFunction();
		
		
		// read nodes and get conditional probability tables to fill;
		for (Node node : net.getNodes()) {
			if (node instanceof ProbabilisticNode) {
				PotentialTable table = ((ProbabilisticNode) node).getProbabilityFunction();
				
				// if we should consider root nodes, then treat all nodes. If not, then only treat nodes with parents
				if (isToTreatRootNodes() || table.getVariablesSize() > 1) {	// tables with more than 1 variable mean they have parents
					// fill table with data;
					if (this.fillJointDist(table, new FileInputStream(input), false)) {	// we don't need to normalize at this point
						// Data is joint distribution/counts. Convert joint counts to conditional probabilities;
						conditionalNormalizer.applyFunction((ProbabilisticTable) table);
					} else {
						throw new IOException("Unable to fill table " + table);
					}
				}
			}
		}
		
		// overwrite network file
		netIO.save(netFile, net);
		
		if (!isToGenerateNoisyOr()) {
			return;	// finish if we don't need to generate noisy-or distribution
		}
		
		// reload network
		net = (ProbabilisticNetwork) netIO.load(netFile);
		ProbabilisticNetwork noisyOrNet = (ProbabilisticNetwork) netIO.load(netFile);
		
		
		
//		Map<String, List<Integer>> nodeNameToNoCountTableIndexMap = new HashMap<String, List<Integer>>();
//		Map<String, List<Integer>> nodeNameToCountTableIndexMap = new HashMap<String, List<Integer>>();
		
		float backup = this.getIncrementCounts();
		try {
			this.setIncrementCounts(0.00001f);	// disable increment, but use small value to avoid 0
			Debug.println(getClass(), "Increment changed from " + backup + " to " + getIncrementCounts());
			
			
			// read nodes and get conditional probability tables to fill;
			for (Node node : noisyOrNet.getNodes()) {
				if (node instanceof ProbabilisticNode) {
					PotentialTable noisyOrTable = ((ProbabilisticNode) node).getProbabilityFunction();
					
					// we should not consider root nodes in this case
					if (noisyOrTable.getVariablesSize() > 1) {	// tables with more than 1 variable mean they have parents
						
						if (noisyOrTable.getVariableAt(0).getStatesSize() != 2) {
							throw new RuntimeException("Noisy-OR cannot be estimated for a node with size " + noisyOrTable.getVariableAt(0).getStatesSize());
						}
						
						PotentialTable tableToRead = ((ProbabilisticNode)net.getNode(node.getName())).getProbabilityFunction();
						tableToRead.copyData();	// backup data with original increment of data
						if (!this.fillJointDist(tableToRead, new FileInputStream(input), false)) {	// re-calculate data with small increment
							throw new IOException("Unable to fill table " + tableToRead);
						}
						
						// obtain which indexes we didn't find counts in data
						List<Integer> noCountIndexes = new ArrayList<Integer>(tableToRead.tableSize());
						for (int cell = 0; cell < tableToRead.tableSize(); cell += 2) {
							if (tableToRead.getValue(cell) <= getIncrementCounts()
									&& tableToRead.getValue(cell+1) <= getIncrementCounts()) {	
								// if all entry in column is lower than the increment we specified, then there was no data for that entry
								noCountIndexes.add(cell);
								noCountIndexes.add(cell+1);
							}
						}
						
						// Data is joint distribution/counts. Convert joint counts to conditional probabilities;
						conditionalNormalizer.applyFunction((ProbabilisticTable) tableToRead);
						
						// start adjusting noisy-or table to approximate with data.
						
						// estimate best noisy-or parameters if not specified by data
						optimizeNoisyOrParameter(noisyOrTable, tableToRead, noCountIndexes);
						
						// print the chi-square test statistics of how noisy-or approximates known entries in data table
						System.out.println(node.getName() + ": chi-square p-value = " + getPValueNoisyOr(noisyOrTable, tableToRead, noCountIndexes));
						
						
						// adjust noisy-or table by overwriting known entries (only unknown entries are filled with noisy-or values).
						tableToRead.restoreData();	// use the original data to fill gaps in noisy-or table
						overwriteKnownEntries(noisyOrTable, tableToRead, noCountIndexes);
					}
				}
			}
			
			// save noisy-or graph as another file
			netIO.save(new File(netFile.getParentFile(), netFile.getName() + ".noisyOr.net"), noisyOrNet);
			
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			// restore backup
			this.setIncrementCounts(backup);
		}
		
	}
	


	/**
	 * 
	 * @param noisyOrTable
	 * @param tableToCompare
	 * @param noCountIndexes : indexes not to be considered as data.
	 */
	protected void optimizeNoisyOrParameter(PotentialTable noisyOrTable, PotentialTable tableToCompare, List<Integer> noCountIndexes) {
		long seed = System.currentTimeMillis();
		Debug.println(getClass(), "Table = " + noisyOrTable + ", Seed = " + seed);
		Random rand = new Random(seed);
		
		
		if (noisyOrTable == null || tableToCompare == null) {
			Debug.println(getClass(), "Null table found: " + noisyOrTable + ", " + tableToCompare);
			return;
		}
		if (tableToCompare.getVariablesSize() != noisyOrTable.getVariablesSize()) {
			throw new IllegalArgumentException("Tables differ in number of variables: " 
					+ noisyOrTable + " = " + noisyOrTable.getVariablesSize() + ", " 
					+ tableToCompare + " = " + tableToCompare.getVariablesSize());
		}
		if (tableToCompare.tableSize() != noisyOrTable.tableSize()) {
			throw new IllegalArgumentException("Tables differ in sizes: " 
					+ noisyOrTable + " = " + noisyOrTable.tableSize() + ", " 
					+ tableToCompare + " = " + tableToCompare.tableSize());
		}
		if (Math.abs(noisyOrTable.tableSize() - Math.pow(2, noisyOrTable.getVariablesSize())) > 0.5) {
			throw new IllegalArgumentException("Only noisy-or (with boolean variables) supported.");
		}
		
		if (noCountIndexes == null) {
			noCountIndexes = Collections.EMPTY_LIST;
		}
		
		
		// this will be used to calculate noisy-or table
		NoisyMaxCPTConverter noisyOr = new NoisyMaxCPTConverter();
		
		// initialize with noisy-or table using default parameters
		noisyOr.forceCPTToIndependenceCausalInfluence(noisyOrTable);
		// backup initial noisy-or table
		noisyOrTable.copyData();
		
		// prepare space for respective joint probability table
//		PotentialTable noisyOrTableJoint = (PotentialTable) noisyOrTable.clone();
		
		// overwrite some cells so that they are not used in comparison (make sure they are equal to noisy or values, so that they such cells don't cause differences)
		tableToCompare = (PotentialTable) tableToCompare.clone();	// use a clone so that we don't change the original 
		tableToCompare.setValue(0, 1f);	// set 1st cell to 100% because noisy or has such value (and we don't want to consider it in comparison)
		tableToCompare.setValue(1, 0f); // set 2nd cell to 0% because noisy or has such value (and we don't want to consider it in comparison)
		
		// obtain a table of joint probabilities, assuming each parent is uniform
//		PotentialTable tableToCompareJoint = (PotentialTable) tableToCompare.clone();
		
		// this list will help in accessing parents in random order
		List<Integer> parentIndexes = new ArrayList<Integer>(noisyOrTable.getVariablesSize()-1);
		for (int varIndex = 1; varIndex < noisyOrTable.getVariablesSize(); varIndex++) {	// do not consider variable 0, because we only want to consider parents
			parentIndexes.add(varIndex);
		}
		
		// we will try to find table which minimizes KL divergence
		double minDivergenceGlobal = Float.MAX_VALUE;	// stores minimum divergence found so far. Initialize with huge number
		PotentialTable globalBestTable = null;
		for (int trial = 0; trial < getNumTrials(); trial++) {
			
			double minDivergence = Float.MAX_VALUE;	// stores minimum divergence of current iteration. Initialize with huge number
			
			// optimize each free variable (in noisy or, there is a free variable for each parent)
			// iterate on parent variables in random order
			List<Integer> parentRandomIterator = new ArrayList<Integer>(parentIndexes);
			while (!parentRandomIterator.isEmpty()) {
				int parentIndex = parentRandomIterator.remove(rand.nextInt(parentRandomIterator.size()));

				// prepare a multi-dimensional index, so that we can access a cell where only 1 parent has value 1
				int[] coord = noisyOrTable.getMultidimensionalCoord(0);
				coord[parentIndex] = 1;
				
				
				// convert it back to single dimension index
				int cell = noisyOrTable.getLinearCoord(coord);
				
				if (!noCountIndexes.contains(cell)) {
//					Debug.println(getClass(), "Ignoring cell " + cell + ". Parent " + parentIndex + " of " + parentIndexes);
					continue;	// do not consider cells that are marked as "there was no data in this cell"
				}
				
				Debug.println(getClass(), "Trial " + trial + ", node " + noisyOrTable.getVariableAt(0).getName() + ", optimizing parent " + parentIndex);
				
				// try multiple values from 0 to 1 and check which value minimizes kl divergence for current parameter
				for (int i = 0; i < getNumIteration(); i++) {
					
					// randomly choose a value to put in current free parameter
					float prob = rand.nextFloat();
					noisyOrTable.setValue(cell, prob);		// note: cell should be pointing to cell where state of node is 0 for all nodes except current parent
					noisyOrTable.setValue(cell + 1, 1f - prob);	// we are assuming noisy or (boolean), so fill the complement with 1-prob
					
					// recalculate noisy-or table with current parameter
					noisyOr.forceCPTToIndependenceCausalInfluence(noisyOrTable);
					

					// overwrite the cells in noCountIndexes with value equal to noisy or table, so that they are not considered when computing difference
					for (Integer index : noCountIndexes) {
						tableToCompare.setValue(index, noisyOrTable.getValue(index));
					}
					// get respective joint probability
//					tableToCompareJoint.setValues(tableToCompare.getValues());
//					convertToJoint(tableToCompareJoint);
					
					// calculate the divergence
					// recalculate to joint table first
//					noisyOrTableJoint.setValues(noisyOrTable.getValues());
//					convertToJoint(noisyOrTableJoint);
//					double currentDivergence = tableToCompareJoint.getKLDivergence(noisyOrTableJoint);
					double currentDivergence = getDivergence(tableToCompare, noisyOrTable);
					if (currentDivergence >= minDivergenceGlobal) {
						Debug.println(getClass(), "Current divergence " + currentDivergence + " is not better than global " + minDivergenceGlobal + ". Skip...");
						continue;	// if it's already worse than global, then go to next iteration immediately
					}
					if (currentDivergence <= minDivergence) {
						boolean isToSubstitute = currentDivergence < minDivergence;
						if (!isToSubstitute) {
							// randomly substitute if currentDivergence == minDivergence
							isToSubstitute = rand.nextBoolean();
						}
						if (isToSubstitute) {
							minDivergence = currentDivergence;
							// copy the optimal value to backup space
							noisyOrTable.copyData();
							Debug.println(getClass(), "Found best divergence " + currentDivergence + " when optimizing node " + noisyOrTable.getVariableAt(0).getName() + ", parent " + parentIndex);
						}
					} else {
						Debug.println(getClass(), "Current divergence " + currentDivergence + " is not better than previous " + minDivergence + ". Skip...");
					}
				}
				
				// at this point, the backup has the best value
				noisyOrTable.restoreData();
				
			}	// end of random iteration on parents
			
			
			// check global minimum
			if (minDivergence <= minDivergenceGlobal) {
				boolean isToSubstitute = minDivergence < minDivergenceGlobal;
				if (!isToSubstitute) {
					// randomly substitute if currentDivergence == minDivergence
					isToSubstitute = rand.nextBoolean();
				}
				if (isToSubstitute) {
					minDivergenceGlobal = minDivergence;
					globalBestTable = (PotentialTable) noisyOrTable.clone();
					Debug.println(getClass(), "Found best divergence " + minDivergenceGlobal + " when optimizing node " + globalBestTable.getVariableAt(0).getName());
				}
			}
			
		}	// end of for iteration
		
		if (globalBestTable == null) {
			Debug.println(getClass(), "No global best table found.");
		} else {
			// copy the best value
			noisyOrTable.setValues(globalBestTable.getValues());
		}
		
	}
	
	/**
	 * 
	 * @param expected
	 * @param approximation
	 * @return the divergence metric
	 */
	protected double getDivergence(PotentialTable expected, PotentialTable approximation) {
		return expected.getKLDivergence(approximation);
		
		// return sum of square errors with virtual counts
//		double sum = 0f;
//		for (int i = 0; i < expected.tableSize(); i++) {
//			double prod = (expected.getValue(i) - approximation.getValue(i)) * getNumVirtualCount();
//			sum += (prod * prod);
//		}
//		return sum;
	}

	/**
	 * @param table : conditonal probabilities will be converted to joint probabilities, assuming uniform distribution of parents.
	 */
	protected void convertToJoint(PotentialTable table) {
		for (int cell = 0; cell < table.tableSize(); cell++) {
			table.setValue(cell, table.getValue(cell) / (table.tableSize()/2f));
		}
		if (Math.abs(1d - table.getSum()) > 0.00005) {
			throw new RuntimeException("Failed to normalize table. Please, report bug.");
		}
		
	}

	/**
	 * @param noisyOrTable
	 * @param tableToRead
	 * @param noCountIndexes
	 * @return p-value of goodness of fit test (e.g. chi-square test or g-test)
	 */
	protected double getPValueNoisyOr(PotentialTable noisyOrTable, PotentialTable tableToRead, List<Integer> noCountIndexes) {

		if (noisyOrTable == null || tableToRead == null) {
			throw new NullPointerException("Null table found: " + noisyOrTable + ", " + tableToRead);
		}
		if (tableToRead.getVariablesSize() != noisyOrTable.getVariablesSize()) {
			throw new IllegalArgumentException("Tables differ in number of variables: " 
					+ noisyOrTable + " = " + noisyOrTable.getVariablesSize() + ", " 
					+ tableToRead + " = " + tableToRead.getVariablesSize());
		}
		if (tableToRead.tableSize() != noisyOrTable.tableSize()) {
			throw new IllegalArgumentException("Tables differ in sizes: " 
					+ noisyOrTable + " = " + noisyOrTable.tableSize() + ", " 
					+ tableToRead + " = " + tableToRead.tableSize());
		}
		if (Math.abs(noisyOrTable.tableSize() - Math.pow(2, noisyOrTable.getVariablesSize())) > 0.5) {
			throw new IllegalArgumentException("Only noisy-or (with boolean variables) supported.");
		}
		
		// virtual counts we have in noisy or table
		int largestIndex = 0;
//		double[] expected = new double[noisyOrTable.tableSize()];
		double[] expected = new double[noisyOrTable.tableSize() - 2 - noCountIndexes.size()];
		// convert table to joint distribution
		noisyOrTable = (PotentialTable) noisyOrTable.clone();	// use a clone in order to keep original
		convertToJoint(noisyOrTable);
		// do not consider the first column (i.e. when all parents are 0)
		for (int cell = /*0*/2, arrayIndex = 0; cell < noisyOrTable.tableSize(); cell++) {	
			if (noCountIndexes.contains(cell)) {
				continue;
			}
			expected[arrayIndex] = noisyOrTable.getValue(cell)*getNumVirtualCount();
			if (expected[arrayIndex] > expected[largestIndex]) {
				largestIndex = arrayIndex;
			}
			arrayIndex++;
		}
		
		// virtual counts we have in data (re-estimated). TODO use actual counts.
//		long[] observed = new long[tableToRead.tableSize()];
		long[] observed = new long[tableToRead.tableSize() - 2 - noCountIndexes.size()];
		// convert table to joint distribution
		tableToRead = (PotentialTable) tableToRead.clone();	// use a clone in order to keep original
		convertToJoint(tableToRead);
		// do not consider the first column (i.e. when all parents are 0)
		for (int cell = /*0*/2, arrayIndex = 0; cell < tableToRead.tableSize(); cell++) {	
			if (noCountIndexes.contains(cell)) {
				continue;
			}
			observed[arrayIndex] = Math.round(tableToRead.getValue(cell)*getNumVirtualCount());
			arrayIndex++;
		}
		

		// perform chi-square independence test and return p-value.
		return new ChiSqureTestWithZero().chiSquareTest(expected, observed);
//		return TestUtils.gTest(expected, observed);
	}
	
	/**
	 * 
	 * @param noisyOrTable
	 * @param tableToRead
	 * @param noCountIndexes
	 */
	protected void overwriteKnownEntries(PotentialTable noisyOrTable, PotentialTable tableToRead, List<Integer> noCountIndexes) {

		if (noisyOrTable == null || tableToRead == null) {
			throw new NullPointerException("Null table found: " + noisyOrTable + ", " + tableToRead);
		}
		if (tableToRead.getVariablesSize() != noisyOrTable.getVariablesSize()) {
			throw new IllegalArgumentException("Tables differ in number of variables: " 
					+ noisyOrTable + " = " + noisyOrTable.getVariablesSize() + ", " 
					+ tableToRead + " = " + tableToRead.getVariablesSize());
		}
		if (tableToRead.tableSize() != noisyOrTable.tableSize()) {
			throw new IllegalArgumentException("Tables differ in sizes: " 
					+ noisyOrTable + " = " + noisyOrTable.tableSize() + ", " 
					+ tableToRead + " = " + tableToRead.tableSize());
		}
		
		for (int cell = 0; cell < tableToRead.tableSize(); cell++) {
			if (noCountIndexes.contains(cell)) {
//				Debug.println(getClass(), "Ignoring cell " + cell);
				continue;
			}
			noisyOrTable.setValue(cell, tableToRead.getValue(cell));
		}
		
		
	}

	/**
	 * @return the netIO
	 */
	public NetIO getNetIO() {
		if (netIO == null) {
			netIO = new NetIO();
		}
		return netIO;
	}

	/**
	 * @param netIO the netIO to set
	 */
	public void setNetIO(NetIO netIO) {
		this.netIO = netIO;
	}

	/**
	 * @return the isToTreatRootNodes
	 */
	public boolean isToTreatRootNodes() {
		return isToTreatRootNodes;
	}

	/**
	 * @param isToTreatRootNodes the isToTreatRootNodes to set
	 */
	public void setToTreatRootNodes(boolean isToTreatRootNodes) {
		this.isToTreatRootNodes = isToTreatRootNodes;
	}

	/**
	 * @return the isToGenerateNoisyOr
	 */
	public boolean isToGenerateNoisyOr() {
		return isToGenerateNoisyOr;
	}

	/**
	 * @param isToGenerateNoisyOr the isToGenerateNoisyOr to set
	 */
	public void setToGenerateNoisyOr(boolean isToGenerateNoisyOr) {
		this.isToGenerateNoisyOr = isToGenerateNoisyOr;
	}

	/**
	 * @return the numIteration : how many iterations to attempt in optimizing a single dimension in {@link #optimizeNoisyOrParameter(PotentialTable, PotentialTable, List)}
	 */
	public int getNumIteration() {
		return numIteration;
	}

	/**
	 * @param numIteration : how many iterations to attempt in optimizing a single dimension in {@link #optimizeNoisyOrParameter(PotentialTable, PotentialTable, List)}
	 */
	public void setNumIteration(int numIteration) {
		this.numIteration = numIteration;
	}

	/**
	 * @return the numTrials : how many trials to run in {@link #optimizeNoisyOrParameter(PotentialTable, PotentialTable, List)}
	 * in order to optimize parameters globally.
	 */
	public int getNumTrials() {
		return numTrials;
	}

	/**
	 * @param numTrials : how many trials to run in {@link #optimizeNoisyOrParameter(PotentialTable, PotentialTable, List)}
	 * in order to optimize parameters globally.
	 */
	public void setNumTrials(int numTrials) {
		this.numTrials = numTrials;
	}

	/**
	 * @return how many total entries to expect in data. This is used in {@link #getPValueNoisyOr(PotentialTable, PotentialTable, List)} 
	 * in order to obtain virtual counts from a joint probability distribution.
	 */
	public float getNumVirtualCount() {
		return numVirtualCount;
	}

	/**
	 * @param numVirtualCount : how many total entries to expect in data. This is used in {@link #getPValueNoisyOr(PotentialTable, PotentialTable, List)} 
	 * in order to obtain virtual counts from a joint probability distribution.
	 */
	public void setNumVirtualCount(float numVirtualCount) {
		this.numVirtualCount = numVirtualCount;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("i","input", true, "CSV file to get joint distribution from.");
		options.addOption("o","output", true, "Network file (.net extension) to overwrite with conditional probabilities.");
		options.addOption("inc","increment", true, "Increment this value to counts in order to avoid 0 counts.");
		options.addOption("d","debug", false, "Enables debug mode.");
		
		
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
		
		if (cmd.hasOption("h")) {;
			for (Option option : options.getOptions()) {
				System.out.println("-" + option.getOpt() + (option.hasArg()?(" <" + option.getLongOpt() +">"):"") + " : " + option.getDescription());
			}
			return;
		}
		
		Debug.setDebug(cmd.hasOption("d"));
		
		
		File dataInput = null;
		if (cmd.hasOption("i")) {
			dataInput = (new File(cmd.getOptionValue("i")));
		} else {
			dataInput = (new File("selectedUserDetectorIndicatorThreat.csv"));
		}
		
		File netFile = null;
		if (cmd.hasOption("o")) {
			netFile = new File(cmd.getOptionValue("o"));
		} else {
			netFile = new File("conditionals.net");
		}
		
		
		NetFileFiller filler = new NetFileFiller();
		if (cmd.hasOption("inc")) {
			filler.setIncrementCounts(Float.parseFloat(cmd.getOptionValue("inc")));
		}
		
		filler.fillNetFile(dataInput, netFile);
		
	}

}
