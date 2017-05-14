/**
 * 
 */
package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;
import au.com.bytecode.opencsv.CSVReader;
import cc.mallet.types.Dirichlet;
import cc.mallet.util.Randoms;

/**
 * @author Shou Matsumoto
 *
 */
public class ContingencyMatrixUserActivitySimulator {
	
	/** Key in {@link #readContinuousDetectorCSVFile(String, Integer, float)} to access the bin's lower bound value.*/
	public static final String BIN_UPPER_KEY = "upper";
	/** Key in {@link #readContinuousDetectorCSVFile(String, Integer, float)} to access the bin's upper bound value.*/
	public static final String BIN_LOWER_KEY = "lower";
	/** Key in {@link #readContinuousDetectorCSVFile(String, Integer, float)} to access the bin's counts (number of users in that bin).*/
	public static final String BIN_COUNTS_KEY = "counts";

	private Randoms random = null;
	private Long seed = null;
	
	private int numUsers = 2860;
	private int numOrganizations = 100;
	
	private String fileNamePrefix = "RCP11";
	private String fileNameSuffix = ".csv";
	private String vsLabel = "vs";
	private String targetLabelSeparator = "_";
	private float initialTableCount = 1f;
	private String outputFolder = "output";
	private String inputFolder = "input";
	private String headerColumnKeyword = "lower";
	private boolean isToSampleTargetExact = true;
	
	private double[] inputMonthDistribution = {.5,.5};
	
	private List<String> targetStateLabels = new ArrayList<String>();
	{
		getTargetStateLabels().add("False");
		getTargetStateLabels().add("True");
	}
	
	private List<String> discreteDetectorsLabels = new ArrayList<String>();
	{
		discreteDetectorsLabels.add("1");
		discreteDetectorsLabels.add("2");
	}
	
	private List<String> continuousDetectorsLabels = new ArrayList<String>();
	{
		continuousDetectorsLabels.add("3a");
		continuousDetectorsLabels.add("3b");
		continuousDetectorsLabels.add("3c");
		continuousDetectorsLabels.add("4a");
		continuousDetectorsLabels.add("4b");
		continuousDetectorsLabels.add("4c");
		continuousDetectorsLabels.add("5a");
		continuousDetectorsLabels.add("6a");
		continuousDetectorsLabels.add("6b");
	}
	
	
	private List<String> inputMonthLabel = new ArrayList<String>();
	{
		inputMonthLabel.add("JAN");
		inputMonthLabel.add("FEB");
	}
	
	private List<String> outputMonthLabels = new ArrayList<String>();
	{
		outputMonthLabels.add("JAN");
		outputMonthLabels.add("FEB");
		outputMonthLabels.add("MAR");
		outputMonthLabels.add("APR");
		outputMonthLabels.add("MAY");
		outputMonthLabels.add("JUN");
		outputMonthLabels.add("JUL");
		outputMonthLabels.add("AUG");
		outputMonthLabels.add("SEP");
	}
	
	private List<Float> monthlyVirtualCounts = new ArrayList<Float>();
	{
		monthlyVirtualCounts.add(-1f);
		monthlyVirtualCounts.add(-1f);
		monthlyVirtualCounts.add(2860f/2f);
		monthlyVirtualCounts.add(2860f/4f);
		monthlyVirtualCounts.add(2860f/8f);
		monthlyVirtualCounts.add(2860f/16f);
		monthlyVirtualCounts.add(2860f/32f);
		monthlyVirtualCounts.add(2860f/64f);
		monthlyVirtualCounts.add(2860f/128f);
	}

	private Map<String, INode> nameToVariableCache;
	private Map<String, PotentialTable> tableCache;
	private Map<String, Map<String,List<Double>>> continuousDetectorFileCache;
	private Map<String, Dirichlet> dirichletCache;


	public ContingencyMatrixUserActivitySimulator() {}
	
	/**
	 * Runs {@link #runSingleMonth(String)} for each month in {@link #getOutputMonthLabels()}
	 * @throws IOException
	 */
	public void runAll() throws IOException {
		for (String month : getOutputMonthLabels()) {
			runSingleMonth(month);
		}
		
	}

	/**
	 * Runs simulation for a single month.
	 * @param monthLabel : month label. It is expected that it's one of {@link #getOutputMonthLabels()}.
	 * This will be used to pick correct place to look for files.
	 * @throws IOException
	 * @see #getMonthlyVirtualCounts()
	 * @see #getInputMonthLabels()
	 * @see #getDiscreteDetectorsLabels()
	 * @see #getFileNamePrefix()
	 * @see #getFileNameSuffix()
	 * @see #getTargetFalseLabel()
	 * @see #getTargetTrueLabel()
	 * @see #getVsLabel()
	 * @see #getOutputFolder()
	 */
	public void runSingleMonth(String monthLabel) throws IOException {
		if (getMonthlyVirtualCounts().size() != getOutputMonthLabels().size()) {
			throw new IllegalArgumentException("Output months had size " + getOutputMonthLabels().size() 
					+ ", while monthly virtual counts had size " + getMonthlyVirtualCounts().size());
		}
		
		// randomly pick 2 discrete detectors
		String discreteDetector1 = "";
		String discreteDetector2 = "";
		boolean isToUseDiscreteDetectors = (getDiscreteDetectorsLabels().size() >= 2);
//		if (getDiscreteDetectorsLabels().size() < 2) {
//			throw new IllegalArgumentException("There must be at least 2 discrete detectors.");
//		} 
		
		if (isToUseDiscreteDetectors) {
			List<String> listWithoutSubstitution = new ArrayList<String>(getDiscreteDetectorsLabels());
			discreteDetector1 = listWithoutSubstitution.remove(getRandom().nextInt(listWithoutSubstitution.size()));
			discreteDetector2 = listWithoutSubstitution.remove(getRandom().nextInt(listWithoutSubstitution.size()));
			
			// make sure detector1 is smaller than detector2 in the index of original list
			if (getDiscreteDetectorsLabels().indexOf(discreteDetector1) > getDiscreteDetectorsLabels().indexOf(discreteDetector2)) {
				// swap them, so that detector1 has smaller index than detector2
				String aux = discreteDetector2;
				discreteDetector2 = discreteDetector1;
				discreteDetector1 = aux;
			}
		}
		
		File monthFolder = null;
		if (getOutputFolder() != null && !getOutputFolder().isEmpty()) {
			monthFolder = new File(getOutputFolder(),monthLabel);
			Files.createDirectories(monthFolder.toPath());
		}
		for (int organization = 0; organization < getNumOrganizations(); organization++) {
			int indexOfMonthInInput = getInputMonthLabels().indexOf(monthLabel);
			if (indexOfMonthInInput < 0) {
				// randomly pick input data month
				double[] distribution = normalize(getInputMonthDistribution());
				if (distribution.length > getInputMonthLabels().size()) {
					throw new IllegalArgumentException("Input month distribution had size " + distribution.length
							+ ", while number of labels of input months provided was " + getInputMonthLabels().size());
				}
//				indexOfMonthInInput = getRandom().nextInt(getInputMonthLabels().size());
				indexOfMonthInInput = getRandom().nextDiscrete(distribution);
			}
			String dataMonth = getInputMonthLabels().get(indexOfMonthInInput);
			
			
			
			// read csv files in order to get number of users for each state of target/class variable
			// if we are using discrete detectors, instantiate a list that will store their contingency tables. Keep it null if we are not using such discrete detectors
			List<PotentialTable> discreteDetectorTablesByTargetState = isToUseDiscreteDetectors?new ArrayList<PotentialTable>(getTargetStateLabels().size()):null;
			List<Double> targetCounts = new ArrayList<Double>(getTargetStateLabels().size());	// how many users there are for each possible state of target/class variable
			for (String targetStateLabel : getTargetStateLabels()) {
				if (isToUseDiscreteDetectors) {
					// read from discrete (binary) detectors file
					// csv file of discrete variables has this format
					String fileName = getFileNamePrefix() + dataMonth + discreteDetector1 + getVsLabel() + discreteDetector2 + getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix();
					PotentialTable table = null;
					try {
						table = readTableFromCSV(fileName , discreteDetector1, discreteDetector2, getInitialTableCount());
					} catch (IOException e) {
						// try without dataMonth
						fileName = getFileNamePrefix() + discreteDetector1 + getVsLabel() + discreteDetector2 + getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix();
						try {
							table = readTableFromCSV(fileName , discreteDetector1, discreteDetector2, getInitialTableCount());
						} catch (IOException e2) {
							e.printStackTrace();
							throw e2;
						}
					}
					// keep table in memory
					discreteDetectorTablesByTargetState.add(table);
					// obtain target distribution from discrete detector data
					targetCounts.add(table.getSum() - (getInitialTableCount()*table.tableSize()));	// this subtraction compensates the counts that were artificially added by getInitialTableCount() in all cells
				} else {
					// read target var counts from csv of any continuous detector of current month (use 1st detector)
					// csv file has this format
					String fileName = getFileNamePrefix() + dataMonth + discreteDetector1 + getVsLabel() + getContinuousDetectorsLabels().get(0) 
							+ getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix(); 
					targetCounts.add(readTotalCountContinuous(fileName));
				}
				
			}
		
			
			// it's 1 file per organization. Prepare a printer for such file
			String numbering = "";
			if (getNumOrganizations() < 1000) {
				numbering = String.format("%1$03d", organization);
			} else if (getNumOrganizations() < 10000) {
				numbering = String.format("%1$04d", organization);
			} else if (getNumOrganizations() < 100000)  {
				numbering = String.format("%1$05d", organization);
			}
			PrintStream printer = System.out;
			if (monthFolder != null) {
				printer = new PrintStream(new FileOutputStream(new File(monthFolder, getFileNamePrefix() + "_" + monthLabel + "_" + numbering + getFileNameSuffix()), false));
			}
			
			// print the header
			printer.print("user,target");
			for (String detector : getDiscreteDetectorsLabels()) {
				printer.print("," + detector);
			}
			for (String detector : getContinuousDetectorsLabels()) {
				printer.print("," + detector);
			}
			printer.println();
			
			// obtain what is the virtual count we should use when generating Dirichlet samples from current month
			float virtualCount = getMonthlyVirtualCounts().get(getOutputMonthLabels().indexOf(monthLabel));
			
			
			for (int user = 0; user < getNumUsers(); user++) {
				printer.print(""+user);
				
				// sample target. 
//				int targetState = getRandom().nextDiscrete(dirichlet.nextDistribution());
				int targetState = sampleTargetState(virtualCount, targetCounts);
				
				String targetLabel = getTargetStateLabels().get(targetState);	// prepare in advance the label of target state, to be used to access respective files
				
				if (getTargetStateLabels().size() == 2) {
					// binary target. Use 0 or 1
					printer.print("," + targetState);
				} else {
					// multinomial target. Use label directly
					printer.print("," + targetLabel);
				}
				
				
				Map<String, Integer> discreteDetectorNameToValueMap = new HashMap<String, Integer>();	// keep track of sampled value
				if (isToUseDiscreteDetectors) {
					// sample discrete detectors given target
					PotentialTable tableToSample = discreteDetectorTablesByTargetState.get(targetState);	// for binary case, sample from tableTrue if target == true. Or else, sample from tableFalse
					
					// get a sample of all variables in the table
					int[] sample = tableToSample.getMultidimensionalCoord(getSampleIndex(tableToSample, virtualCount));
					// extract values of each variable
					for (String detectorName : getDiscreteDetectorsLabels()) {
						// get variable from name
						Node detectorVar = (Node) getNameToVariableCache().get(detectorName);
						// store and print the value of current variable
						String value = detectorVar.getStateAt(sample[tableToSample.getVariableIndex(detectorVar)]);
						discreteDetectorNameToValueMap.put(detectorName, Integer.parseInt(value));
						printer.print("," + value);
					}
				}
				
				
				// sample continuous detectors;
				for (String continuousDetectorName : getContinuousDetectorsLabels()) {
					// randomly pick a discrete detector to be used as condition for other continuous detectors;
					String conditionVariableName = discreteDetector1;
					if (getRandom().nextBoolean()) {
						conditionVariableName = discreteDetector2;
					}
					
					String fileName = getFileNamePrefix() + dataMonth + conditionVariableName + getVsLabel() + continuousDetectorName + getTargetLabelSeparator() + targetLabel  + getFileNameSuffix(); // csv file has this format
					double sampleValue = Float.NaN;
					try {
						sampleValue = getSampleContinuous(fileName, virtualCount, discreteDetectorNameToValueMap.get(conditionVariableName), getInitialTableCount());
					} catch (IOException e) {
						// try without dataMonth
						fileName = getFileNamePrefix() + conditionVariableName + getVsLabel() + continuousDetectorName + getTargetLabelSeparator() + targetLabel  + getFileNameSuffix(); // csv file has this format
						try {
							sampleValue = getSampleContinuous(fileName, virtualCount, discreteDetectorNameToValueMap.get(conditionVariableName), getInitialTableCount());
						} catch (IOException e2) {
							e.printStackTrace();
							throw e2;
						}
					}
					printer.print(","+sampleValue);
				}
				
				printer.println();
			}
			
			if (printer != System.out) {
				printer.close();
			}
		}
		
	}

	/**
	 * @param distribution : array to be normalized to 1. The returned value will be the same instance.
	 * @return normalized (sum is 1) distribution. No new array is allocated.
	 */
	public double[] normalize(double[] distribution) {
		if (distribution == null ) {
			return null;
		}
		if (distribution.length <= 0) {
			return distribution;
		}
		if (distribution.length == 1) {
			distribution[0] = 1;
			return distribution;
		}
		
		// correct negative values
		double smallest = 0;
		for (double val : distribution) {
			if (val < smallest) {
				smallest = val;
			}
		}
		
		// at this point, smallest is 0 or negative
		
		// calculate sum for normalization
		double sum = 0;
		for (int i = 0; i < distribution.length; i++) {
			distribution[i] -= smallest;	// correct negative values
			sum += distribution[i];
		}
		if (sum <= 0) {
			throw new IllegalArgumentException("Sum of distribution was zero.");
		}
		
		// normalize
		for (int i = 0; i < distribution.length; i++) {
			distribution[i] /= sum;	// distribution[i] = distribution[i] / sum
		}
		
		return distribution;
	}

	/**
	 * @param numList
	 * @return
	 * Simply gets the sum of a content of a list
	 */
	private static Number getSum(List<Number> numList) {
		double sum = 0;
		for (Number number : numList) {
			sum += number.doubleValue();
		}
		return sum;
	}
	
	/**
	 * Starts summing up the values in a list from 0-th index to last index, stops when reached the 
	 * value in second argument, and return the index.
	 * @param countList : list to iterate in
	 * @param largerThan : iteration stops when sum reaches this value
	 * @return the index 
	 */
	private static int getIndexOfSample(List<Number> countList, Number largerThan)  {
		double sum = 0;
		for (int i = 0; i < countList.size(); i++) {
			sum += countList.get(i).doubleValue();
			if (sum > largerThan.doubleValue()) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @param virtualCount : if {@link #isToSampleTargetExact()} is false, then 
	 * sampling will be probabilistic by using a {@link Dirichlet} distribution.
	 * This will be used as virtual counts (lower counts result in higher variance).
	 * @param targetCounts : counts of target/class variable.
	 * If {@link #isToSampleTargetExact()}, then values in this array will be decremented
	 * as samples are generated (i.e. sampling without substitution).
	 * @return
	 */
	protected int sampleTargetState(float virtualCount, List<Double> targetCounts) {
		
		if (isToSampleTargetExact()) {
			
			// randomly pick a target, with substitution
			Number numPopulation = getSum((List)targetCounts);			// size of population
			int sample = getRandom().nextInt(numPopulation.intValue());	// a sample from the population
			int state = getIndexOfSample((List)targetCounts, sample);	// index of sample in the list
			
			if (state < 0) {
				// pick uniformly if counts are zero
				state = getRandom().nextInt(targetCounts.size());
				Debug.println(getClass(), "No more counts in target/class variable to pick. Picking uniformly: " + state);
			} else {
				targetCounts.set(state, targetCounts.get(state) - 1);		// decrease count of that index (equivalent to removing the sample)
			}
			
			return state;
		} else {
			// sample randomly
			
			// prepare a dirichlet-multinomial sampler to sample a state from counts of target variable
			// look for cache first
			Dirichlet dirichlet = getDirichletCache().get(targetCounts + "," + virtualCount);
			if (dirichlet == null) {
				// we may need to normalize targetCounts if virtual counts is positive, so calculate sum (to be used later for normalization)
				double sum = 0;
				if (virtualCount > 0) {
					for (double count : targetCounts) {
						sum += count;
					}
				}
				// calculate parameters of dirichlet
				double[] alpha = new double[targetCounts.size()];
				Arrays.fill(alpha, 0.0);
				for (int i = 0; i < targetCounts.size(); i++) {
					alpha[i] = targetCounts.get(i) + getInitialTableCount(); //initialTableCount = 1 is used avoid zeros (common practice in bayesian learning)
					if (virtualCount > 0) {
						alpha[i] *= virtualCount/sum; // this is equivalent to normalizing targetCounts to 1 and then multiplying virtual counts
					}
				}
				dirichlet = new Dirichlet(alpha);
				getDirichletCache().put(targetCounts + "," + virtualCount, dirichlet);
			}
			
			return  getRandom().nextDiscrete(dirichlet.nextDistribution());
		}

	}

	/**
	 * Generate a sample from dirichlet-multinomial distribution
	 * @param table
	 * @param virtualCount
	 * @param normalize : if true, table will be normalized
	 * @return
	 */
	public int getSampleIndex(PotentialTable table, float virtualCount) {
		
		// check cache first
		Dirichlet dirichlet = getDirichletCache().get(table.toString() + "_" + virtualCount);
		
		if (dirichlet == null) {
			PotentialTable origTable = table;
			if (virtualCount <= 0) {
				// do not normalize table, and use the counts in table directly
				virtualCount = 1;
			} else {
				// normalize table and use virtual counts
				table = table.getTemporaryClone();	// use a clone, so that we don't modify original table
				table.normalize();
			}
			double[] alpha = new double[table.tableSize()];
			for (int i = 0; i < table.tableSize(); i++) {
				alpha[i] = table.getValue(i) * virtualCount;
			}
			dirichlet = new Dirichlet(alpha);
			
			getDirichletCache().put(origTable.toString() + "_" + virtualCount, dirichlet);
		}
		
		
		return getRandom().nextDiscrete(dirichlet.nextDistribution());
		
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @param virtualCount
	 * @param conditionState 
	 * @param initialCounts  : this value will be added to all entries initially (use 0 to void this parameter).
	 * @return
	 * @throws IOException 
	 */
	public double getSampleContinuous(String fileName, float virtualCount, Integer conditionState, float initialCounts) throws IOException {
		
		// read csv file
		Map<String, List<Double>> map = readContinuousDetectorCSVFile(fileName, conditionState, initialCounts);
		
		Dirichlet dirichlet = getDirichletCache().get(fileName + "_" + virtualCount + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts);
		if (dirichlet == null) {
			List<Double> counts = map.get(BIN_COUNTS_KEY);
			if (virtualCount <= 0) {
				// do not normalize list, and use the counts in list directly
				virtualCount = 1;
			} else {
				// normalize list and use virtual counts
				double sum = 0;
				for (Double count : counts) {
					sum += count;
				}
				for (int i = 0; i < counts.size(); i++) {
					counts.set(i, counts.get(i) / sum);
				}
			}
			
			// use dirichlet-multinomial sampler to sample a bin
			double[] alpha = new double[counts.size()];
			for (int i = 0; i < counts.size(); i++) {
				alpha[i] = counts.get(i) * virtualCount;
			}
			dirichlet = new Dirichlet(alpha);
			
			getDirichletCache().put(fileName + "_" + virtualCount + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts, dirichlet);
		}
		
		int bin = getRandom().nextDiscrete(dirichlet.nextDistribution());
		
		// use uniform distribution to sample a single value inside the sampled bin
		return getRandom().nextUniform(map.get(BIN_LOWER_KEY).get(bin), map.get(BIN_UPPER_KEY).get(bin));
	}
	

	/**
	 * @param fileName
	 * @return sum of counts in the csv file specified by fileName
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public double readTotalCountContinuous(String fileName) throws NumberFormatException, IOException {
		
		// read csv file without condition (i.e. read only the 1st block found if csv has multiple headers)
		Map<String, List<Double>> map = readContinuousDetectorCSVFile(fileName, null, 0f);	// add 0 to counts, because we want actual counts here
		
		// simply calculate the sum of counts
		double ret = 0;
		for (Double count : map.get(BIN_COUNTS_KEY)) {
			ret += count;
		}
		
		return ret;
	}

	/**
	 * 
	 * @param fileName
	 * @param conditionState 
	 * @param initialCounts  : this value will be added to all entries initially (use 0 to void this parameter).
	 * @return mapping from attributes to values.
	 * The i-th element in the list is the value of i-th bin.
	 * The labels (keys) will be {@link #BIN_LOWER_KEY}, {@link #BIN_UPPER_KEY}, and {@link #BIN_COUNTS_KEY}.
	 * @throws IOException 
	 * @see {@link #getOutputFolder()}
	 */
	public Map<String,List<Double>> readContinuousDetectorCSVFile(String fileName, Integer conditionState, float initialCounts) throws IOException {
		
		// check cache
		Map<String,List<Double>> ret = getContinuousDetectorFileCache().get(fileName + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts);
		if (ret != null) {
			return ret;
		}
		
		// read csv file
		CSVReader reader = new CSVReader(new FileReader(new File(getInputFolder(),fileName)));
		
		// read rows until we find the block we want (which matches with condition state)
		String[] row = null;
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			if (row[0].toLowerCase().contains(getHeaderColumnKeyword()) && row.length >= 3) {
				// check if condition matched
				if (conditionState == null) {
					// no condition was specified, so we can stop at any block (so just use the 1st block we found)
					break;
				} else if (row[2].matches(".*=.*")) {
					String[] split = row[2].trim().split("=");
					if (conditionState.intValue() == Integer.parseInt(split[1])) {
						break;	// found a matching condition
					}
				} else {
					break;
				}
			}
		}
		if (row == null) {
			reader.close();
			throw new IllegalArgumentException("Detector value " + conditionState + " not found in file " + fileName);
		}
		
		// The labels (keys) will be "lower", "upper", and "counts".
		ret = new HashMap<String, List<Double>>();
		ret.put(BIN_LOWER_KEY, new ArrayList<Double>());
		ret.put(BIN_UPPER_KEY, new ArrayList<Double>());
		ret.put(BIN_COUNTS_KEY, new ArrayList<Double>());
		
		// at this point, cursor is at the header. Start reading the block
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			if (row[0].toLowerCase().contains(getHeaderColumnKeyword())) {
				break;	// this is the next block, so stop here
			}
			ret.get(BIN_LOWER_KEY).add(Double.parseDouble(row[0]));
			ret.get(BIN_UPPER_KEY).add(Double.parseDouble(row[1]));
			ret.get(BIN_COUNTS_KEY).add(Double.parseDouble(row[2]) + initialCounts);
		}
		
		reader.close();
		
		getContinuousDetectorFileCache().put(fileName + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts, ret);
		return ret;
	}

	/**
	 * Read a table of discrete variables from csv file
	 * @param fileName : name of file to read
	 * @param detector1 : 1st variable (it will be added as 2nd variable in table -- states will iterate less often -- row in csv)
	 * @param detector2 : 2nd variable (it will be added as 1st variable in table -- states will iterate most often -- column in csv)
	 * @param targetStateLabel : label indicating whether target variable is true or false. This will be used for file name.
	 * @param initialTableValue : this value will be added to all entries initially (use 0 to void this parameter).
	 * @return the table read from csv file
	 * @throws IOException
	 */
	public PotentialTable readTableFromCSV(String fileName,String detector1, String detector2, float initialTableValue) throws IOException {

		// check cache
		PotentialTable ret = getTableCache().get(fileName + "_" + detector1 + "_" + detector2 + "_" + initialTableValue);
		if (ret != null) {
			return ret;
		}
		
		File file = new File(getInputFolder(),fileName);
		if (!file.exists()) {
			throw new IOException(file.getAbsolutePath() + " does not exist.");
		}
		if (!file.isFile()) {
			throw new IOException(file.getAbsolutePath() + " is not a file.");
		}
		
		// prepare table to return
		ret = new ProbabilisticTable();
		INode node = getNameToVariableCache().get(detector2);	// detector 2 iterates on each column, so add it first
		if (node == null) {
			node = new ProbabilisticNode();
			node.setName(detector2);
			node.appendState("0");
			node.appendState("1");
			getNameToVariableCache().put(detector2, node);
		}
		ret.addVariable(node);
		node = getNameToVariableCache().get(detector1);
		if (node == null) {
			node = new ProbabilisticNode();
			node.setName(detector1);
			node.appendState("0");
			node.appendState("1");
			getNameToVariableCache().put(detector1, node);
		}
		ret.addVariable(node);
		ret.fillTable(initialTableValue);
		
		// read csv file
		CSVReader reader = new CSVReader(new FileReader(file));
		
		// ignore 1st row (header Det2)
		reader.readNext();
		
		// read from 2nd row
		for (int tableIndex = 0; tableIndex < ret.tableSize();) {
			String[] row = reader.readNext();
			if (row == null) {
				Debug.println(getClass(), "Premature EOF found at index " + tableIndex + " of file " + file.getAbsolutePath());
				break;
			}
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			// ignore 1st column (header Det1)
			for (int column = 1; column < row.length; column++, tableIndex++) {
				ret.setValue(tableIndex, ret.getValue(tableIndex) + Float.parseFloat(row[column]));
			}
		}
		
		reader.close();
		
		getTableCache().put(fileName + "_" + detector1 + "_" + detector2 + "_" + initialTableValue, ret);
		
		return ret;
	}

	/**
	 * @return the numUsers
	 */
	public int getNumUsers() {
		return this.numUsers;
	}

	/**
	 * @param numUsers the numUsers to set
	 */
	public void setNumUsers(int numUsers) {
		this.numUsers = numUsers;
	}

	/**
	 * @return the numOrganizations
	 */
	public int getNumOrganizations() {
		return this.numOrganizations;
	}

	/**
	 * @param numOrganizations the numOrganizations to set
	 */
	public void setNumOrganizations(int numOrganizations) {
		this.numOrganizations = numOrganizations;
	}

	/**
	 * @return the fileNamePrefix
	 */
	public String getFileNamePrefix() {
		return this.fileNamePrefix;
	}

	/**
	 * @param fileNamePrefix the fileNamePrefix to set
	 */
	public void setFileNamePrefix(String fileNamePrefix) {
		this.fileNamePrefix = fileNamePrefix;
	}

	/**
	 * @return the fileNameSuffix
	 */
	public String getFileNameSuffix() {
		return this.fileNameSuffix;
	}

	/**
	 * @param fileNameSuffix the fileNameSuffix to set
	 */
	public void setFileNameSuffix(String fileNameSuffix) {
		this.fileNameSuffix = fileNameSuffix;
	}

	/**
	 * @return the vsLabel
	 */
	public String getVsLabel() {
		return this.vsLabel;
	}

	/**
	 * @param vsLabel the vsLabel to set
	 */
	public void setVsLabel(String vsLabel) {
		this.vsLabel = vsLabel;
	}


	/**
	 * @return the continuousDetectorsLabel
	 */
	public List<String> getContinuousDetectorsLabels() {
		return this.continuousDetectorsLabels;
	}

	/**
	 * @param continuousDetectorsLabels the continuousDetectorsLabel to set
	 */
	public void setContinuousDetectorsLabels(List<String> continuousDetectorsLabels) {
		this.continuousDetectorsLabels = continuousDetectorsLabels;
	}

	/**
	 * @return the discreteDetectorsLabel
	 */
	public List<String> getDiscreteDetectorsLabels() {
		return this.discreteDetectorsLabels;
	}

	/**
	 * @param discreteDetectorsLabels the discreteDetectorsLabel to set
	 */
	public void setDiscreteDetectorsLabels(List<String> discreteDetectorsLabels) {
		this.discreteDetectorsLabels = discreteDetectorsLabels;
	}

	/**
	 * @return the inputMonthLabel
	 */
	public List<String> getInputMonthLabels() {
		return this.inputMonthLabel;
	}

	/**
	 * @param inputMonthLabel the inputMonthLabel to set
	 */
	public void setInputMonthLabels(List<String> inputMonthLabel) {
		this.inputMonthLabel = inputMonthLabel;
	}

	/**
	 * @return the outputMonthLabel
	 */
	public List<String> getOutputMonthLabels() {
		return this.outputMonthLabels;
	}

	/**
	 * @param outputMonthLabel the outputMonthLabel to set
	 */
	public void setOutputMonthLabels(List<String> outputMonthLabel) {
		this.outputMonthLabels = outputMonthLabel;
	}

	/**
	 * @return the monthlyVirtualCounts
	 */
	public List<Float> getMonthlyVirtualCounts() {
		return this.monthlyVirtualCounts;
	}

	/**
	 * @param monthlyVirtualCounts the monthlyVirtualCounts to set
	 */
	public void setMonthlyVirtualCounts(List<Float> monthlyVirtualCounts) {
		this.monthlyVirtualCounts = monthlyVirtualCounts;
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
	 * @return the random number generator
	 */
	protected Randoms getRandom() {
		if (random == null) {
			random = new Randoms(getSeed().intValue());
		}
		return random;
	}

	/**
	 * @param random : the random number generator
	 */
	protected void setRandom(Randoms random) {
		this.random = random;
	}


	/**
	 * @return the nameToVariableCache
	 */
	protected Map<String, INode> getNameToVariableCache() {
		if (nameToVariableCache == null) {
			nameToVariableCache = new HashMap<String, INode>();
		}
		return nameToVariableCache;
	}

	/**
	 * @param nameToVariableCache the nameToVariableCache to set
	 */
	protected void setNameToVariableCache(Map<String, INode> nameToVariableCache) {
		if (this.nameToVariableCache == null) {
			this.nameToVariableCache = new HashMap<String, INode>();
		}
		this.nameToVariableCache = nameToVariableCache;
	}

	/**
	 * @return the initialTableCount
	 */
	public float getInitialTableCount() {
		return initialTableCount;
	}

	/**
	 * @param initialTableCount the initialTableCount to set
	 */
	public void setInitialTableCount(float initialTableCount) {
		this.initialTableCount = initialTableCount;
	}

	/**
	 * @return the outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/**
	 * @param outputFolder the outputFolder to set
	 */
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	/**
	 * @return the inputFolder
	 */
	public String getInputFolder() {
		return inputFolder;
	}

	/**
	 * @param inputFolder the inputFolder to set
	 */
	public void setInputFolder(String inputFolder) {
		this.inputFolder = inputFolder;
	}
	
	/**
	 * @return the tableCache
	 */
	protected Map<String, PotentialTable> getTableCache() {
		if (tableCache == null) {
			tableCache = new HashMap<String, PotentialTable>();
		}
		return tableCache;
	}

	/**
	 * @param tableCache the tableCache to set
	 */
	protected void setTableCache(Map<String, PotentialTable> tableCache) {
		this.tableCache = tableCache;
	}

	/**
	 * @return the continuousDetectorFileCache
	 */
	protected Map<String, Map<String,List<Double>>> getContinuousDetectorFileCache() {
		if (continuousDetectorFileCache == null) {
			continuousDetectorFileCache = new HashMap<String, Map<String,List<Double>>>();
		}
		return continuousDetectorFileCache;
	}

	/**
	 * @param continuousDetectorFileCache the continuousDetectorFileCache to set
	 */
	protected void setContinuousDetectorFileCache(
			Map<String, Map<String,List<Double>>> continuousDetectorFileCache) {
		this.continuousDetectorFileCache = continuousDetectorFileCache;
	}

	/**
	 * @return the dirichletCache
	 */
	protected Map<String, Dirichlet> getDirichletCache() {
		if (dirichletCache == null) {
			dirichletCache = new HashMap<String, Dirichlet>();
		}
		return dirichletCache;
	}

	/**
	 * @param dirichletCache the dirichletCache to set
	 */
	protected void setDirichletCache(Map<String, Dirichlet> dirichletCache) {
		this.dirichletCache = dirichletCache;
	}

	/**
	 * @return the headerColumnKeyword : a keyword belonging to 1st column of header of csv files (this is used to detect a header if csv has multiple headers).
	 */
	public String getHeaderColumnKeyword() {
		return headerColumnKeyword;
	}

	/**
	 * @param headerColumnKeyword the headerColumnKeyword to set  : a keyword belonging to 1st column of header of csv files (this is used to detect a header if csv has multiple headers).
	 */
	public void setHeaderColumnKeyword(String headerColumnKeyword) {
		this.headerColumnKeyword = headerColumnKeyword;
	}


	/**
	 * @return the targetLabelSeparator
	 */
	public String getTargetLabelSeparator() {
		return targetLabelSeparator;
	}

	/**
	 * @param targetLabelSeparator the targetLabelSeparator to set
	 */
	public void setTargetLabelSeparator(String targetLabelSeparator) {
		this.targetLabelSeparator = targetLabelSeparator;
	}

	/**
	 * @return the targetLabels
	 */
	public List<String> getTargetStateLabels() {
		return targetStateLabels;
	}

	/**
	 * @param targetLabels the targetLabels to set
	 */
	public void setTargetStateLabels(List<String> targetLabels) {
		this.targetStateLabels = targetLabels;
	}

	/**
	 * @return the isToSampleTargetExact : if true then {@link #sampleTargetState(float, double[])} will
	 * pick values from target counts instead of sampling from dirichlet-multinomial.
	 */
	public boolean isToSampleTargetExact() {
		return isToSampleTargetExact;
	}

	/**
	 * @param isToSampleTargetExact the isToSampleTargetExact to set : if true then {@link #sampleTargetState(float, double[])} will
	 * pick values from target counts instead of sampling from dirichlet-multinomial.
	 */
	public void setToSampleTargetExact(boolean isToSampleTargetExact) {
		this.isToSampleTargetExact = isToSampleTargetExact;
	}

	/**
	 * @param commaSeparatedList
	 * @return comma separated list (string) parsed to a {@link List} of {@link String} 
	 */
	public static List<String> parseListString(String commaSeparatedList) {
		if (commaSeparatedList == null || commaSeparatedList.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
//		commaSeparatedList = commaSeparatedList.replaceAll("\\s", "");	// remove whitespaces
		List<String> ret = new ArrayList<String>();
		for (String string : commaSeparatedList.split(",")) {
			if (string == null || string.isEmpty()) {
				continue;	// ignore null/empty entries
			}
			ret.add(string);
		}
		return ret;
	}
	
	/**
	 * @param commaSeparatedList
	 * @return comma separated list (string) parsed to a {@link List} of {@link Float} 
	 */
	public static List<Float> parseListFloat(String commaSeparatedList) {
		if (commaSeparatedList == null || commaSeparatedList.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		commaSeparatedList = commaSeparatedList.replaceAll("\\s", "");	// remove whitespaces
		List<Float> ret = new ArrayList<Float>();
		for (String value : commaSeparatedList.split(",")) {
			if (value == null || value.isEmpty()) {
				continue;	// ignore null/empty entries
			}
			ret.add(Float.parseFloat(value));
		}
		return ret;
	}
	
	/**
	 * @param commaSeparatedList
	 * @return comma separated list (string) parsed to a {@link List} of {@link Float} 
	 */
	public static double[] parseArrayDouble(String commaSeparatedList) {
		if (commaSeparatedList == null || commaSeparatedList.isEmpty()) {
			return new double[0];
		}
		
		// read the argument
		commaSeparatedList = commaSeparatedList.replaceAll("\\s", "");	// remove whitespaces
		List<Float> list = new ArrayList<Float>();	// using a list, because we don't know the size (given that there may be invalid entries)
		for (String value : commaSeparatedList.split(",")) {
			if (value == null || value.isEmpty()) {
				continue;	// ignore null/empty entries
			}
			list.add(Float.parseFloat(value));
		}
		
		// convert to array of double
		double[] ret = new double[list.size()];
		for (int i = 0; i < list.size(); i++) {
			ret[i] = list.get(i);
		}
		return ret;
	}

	/**
	 * @return the multinomial probability distribution of a month in {@link #getInputMonthLabels()}
	 * to be randomly picked as input data when generating samples of months when we don't have respective input data
	 * (i.e. months in {@link #getOutputMonthLabels()} which is not in {@link #getInputMonthLabels()}).
	 */
	public double[] getInputMonthDistribution() {
		return inputMonthDistribution;
	}

	/**
	 * @param inputMonthDistribution : the multinomial probability distribution of a month in {@link #getInputMonthLabels()}
	 * to be randomly picked as input data when generating samples of months when we don't have respective input data
	 * (i.e. months in {@link #getOutputMonthLabels()} which is not in {@link #getInputMonthLabels()}).
	 */
	public void setInputMonthDistribution(double[] inputMonthDistribution) {
		this.inputMonthDistribution = inputMonthDistribution;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("usr","num-users", true, "Number of users to simulate.");
		options.addOption("org","num-organizations", true, "Number of organizations to simulate.");
		options.addOption("prefix","file-name-prefix", true, "Common prefixes of csv file names in the input folder (default \"RCP11\").");
		options.addOption("suffix","file-name-suffix-extension", true, "Common suffixes or extensions of file names in the input folder (default \".csv\").");
		options.addOption("detectorSep","separator-detector-vs-label", true, "Suffixes in input csv file names that separates names of detectors (default \"vs\").");
		options.addOption("targetSep","target-label-separator", true, "Suffixes in input csv file names that separates target state from other names (default \"_\")."
				+ " This must match with true/false values that appear in names of csv files.");
		options.addOption("target","target-labels", true, "Comma separated list of names of states of target variable. This will also be used as label/suffix in csv file that indicates that the data is for target = false (default \"False,True\")."
				+ " This must match with true/false values that appear in names of csv files.");
		options.addOption("initCount","initial-table-count", true, "This value will be added to counts for dirichlet-multinomial sampling (real numbers are also allowed).");
		options.addOption("o","output", true, "Folder to write. If not specified, \"output\" will be used.");
		options.addOption("i","input", true, "Input folder to read. If not specified, \"input\" will be used.");
		options.addOption("discrete","discrete-detector-labels", true, "Comma-separated list of detector numbers that are discrete in this RCP (default \"1,2\")."
				+ " This must match with detectors that appear in names of csv files.");
		options.addOption("continuous","continuous-detector-labels", true, "Comma-separated list of detector numbers that are continuous in this RCP "
				+ "(default \"3a,3b,3c,4a,4b,4c,5a,6a,6b\"). This must match with detectors that appear in names of csv files.");
		options.addOption("inputMonths","input-month-labels", true, "Comma-separated list of month labels that we have input data (default \"JAN,FEB\")."
				+ " This must match with months that appear in names of csv files.");
		options.addOption("allMonths","all-output-month-labels", true, "Comma-separated list of all month labels to output (default \"JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP\")."
				+ " This must match with months that appear in names of csv files.");
		options.addOption("virtCounts","monthly-virtual-counts", true, "Comma-separated list of numbers (virtual counts) to be used for sampling users of each month"
				+ "(default \"-1,-1,1430,715,357.5,178.75,89.375,44.6875,22.34375\"). Zero or negative values can be used to consider actual counts instead (if the month has actual data).");
		options.addOption("numStates","num-states-class-variable", true, "Number of states of the class or target variable (default is binary, which is 2).");
		options.addOption("targetExact","sample-target-class-variable-exactly", false, "If set, target/class variable will be sampled without dirichlet-multinomial distribution.");
		options.addOption("seed","random-seed", true, "Number to be used as random seed (default is system's time).");
		options.addOption("inputDist","input-month-distribution", true, "Comma-separated list of probabilities (default is \".5,.5\"). "
				+ "This is used to randomly pick an input month when simulating months with no input data. "
				+ "The length of this list must match with list provided in -inputMonths.");
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
		
		ContingencyMatrixUserActivitySimulator sim = new ContingencyMatrixUserActivitySimulator();
		
		// fill attributes with arguments
		if (cmd.hasOption("usr")) {
			sim.setNumUsers(Integer.parseInt(cmd.getOptionValue("usr")));
		}
		if (cmd.hasOption("org")) {
			sim.setNumOrganizations(Integer.parseInt(cmd.getOptionValue("org")));
		}
		if (cmd.hasOption("prefix")) {
			sim.setFileNamePrefix(cmd.getOptionValue("prefix"));
		}
		if (cmd.hasOption("suffix")) {
			sim.setFileNameSuffix(cmd.getOptionValue("suffix"));
		}
		if (cmd.hasOption("detectorSep")) {
			sim.setVsLabel(cmd.getOptionValue("detectorSep"));
		}
		if (cmd.hasOption("targetSep")) {
			sim.setTargetLabelSeparator(cmd.getOptionValue("targetSep"));
		}
		if (cmd.hasOption("target")) {
			sim.setTargetStateLabels(parseListString(cmd.getOptionValue("target")));
		}
		if (cmd.hasOption("initCount")) {
			sim.setInitialTableCount(Float.parseFloat(cmd.getOptionValue("initCount")));
		}
		if (cmd.hasOption("o")) {
			sim.setOutputFolder(cmd.getOptionValue("o"));
		}
		if (cmd.hasOption("i")) {
			sim.setInputFolder(cmd.getOptionValue("i"));
		}
		if (cmd.hasOption("discrete")) {
			sim.setDiscreteDetectorsLabels(parseListString(cmd.getOptionValue("discrete")));
		}
		if (cmd.hasOption("continuous")) {
			sim.setContinuousDetectorsLabels(parseListString(cmd.getOptionValue("continuous")));
		}
		if (cmd.hasOption("inputMonths")) {
			sim.setInputMonthLabels(parseListString(cmd.getOptionValue("inputMonths")));
		}
		if (cmd.hasOption("allMonths")) {
			sim.setOutputMonthLabels(parseListString(cmd.getOptionValue("allMonths")));
		}
		if (cmd.hasOption("virtCounts")) {
			sim.setMonthlyVirtualCounts(parseListFloat(cmd.getOptionValue("virtCounts")));
		}
		if (cmd.hasOption("seed")) {
			sim.setSeed(Long.parseLong(cmd.getOptionValue("seed")));
		}
		if (cmd.hasOption("inputDist")) {
			sim.setInputMonthDistribution(parseArrayDouble(cmd.getOptionValue("inputDist")));
		}
		
		sim.setToSampleTargetExact(cmd.hasOption("targetExact"));
		
		sim.runAll();
	}


}
