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
	
	/** Key in {@link #readContinuousVarCSVFile(String, Integer, float)} to access the bin's lower bound value.*/
	public static final String BIN_UPPER_KEY = "upper";
	/** Key in {@link #readContinuousVarCSVFile(String, Integer, float)} to access the bin's upper bound value.*/
	public static final String BIN_LOWER_KEY = "lower";
	/** Key in {@link #readContinuousVarCSVFile(String, Integer, float)} to access the bin's counts (number of users in that bin).*/
	public static final String BIN_COUNTS_KEY = "counts";

	private Randoms random = null;
	private Long seed = null;
	
	private int numUsers = -1;
	private int numOrganizations = 100;
	
	private int totalCounts = -1;
	
	private String fileNamePrefix = "";
	private String fileNameSuffix = ".csv";
	private String vsLabel = "vs";
	private String targetLabelSeparator = "_";
	private String timeSliceVarSeparator = "_";
	private float initialTableCount = 1f;
	private String outputFolder = "output";
	private String inputFolder = "input";
	private String headerColumnKeyword = "lower";
	
	private boolean isToSampleTargetExact = false;
	
	private double[] inputTimeSliceDistribution = {.5,.5};
	
	private List<String> targetStateLabels = new ArrayList<String>();
	{
		getTargetStateLabels().add("False");
		getTargetStateLabels().add("True");
	}
	
	private List<String> discreteVarsLabels = new ArrayList<String>();
	
	private List<String> continuousVarsLabels = new ArrayList<String>();
	
	
	private List<String> inputTimeSliceLabel = new ArrayList<String>();
	
	private List<String> outputTimeSliceLabels = new ArrayList<String>();
	
	private List<Float> timeSliceVirtualCountCoefficients = new ArrayList<Float>();

	private Map<String, INode> nameToVariableCache;
	private Map<String, PotentialTable> tableCache;
	private Map<String, Map<String,List<Double>>> continuousVarFileCache;
	private Map<String, Dirichlet> dirichletCache;

	private boolean is1stVarInRow = true;

	public ContingencyMatrixUserActivitySimulator() {}
	
	/**
	 * Runs {@link #runSingleTimeSlice(String)} for each Time Slice in {@link #getOutputTimeSliceLabels()}
	 * @throws IOException
	 */
	public void runAll() throws IOException {
		for (String timeSlice : getOutputTimeSliceLabels()) {
			runSingleTimeSlice(timeSlice);
		}
		
	}

	/**
	 * Runs simulation for a single timeSlice.
	 * @param timeSliceLabel : timeSlice label. It is expected that it's one of {@link #getOutputTimeSliceLabels()}.
	 * This will be used to pick correct place to look for files.
	 * @throws IOException
	 * @see #getTimeSliceVirtualCountCoefficients()
	 * @see #getInputTimeSliceLabels()
	 * @see #getDiscreteVarsLabels()
	 * @see #getFileNamePrefix()
	 * @see #getFileNameSuffix()
	 * @see #getTargetFalseLabel()
	 * @see #getTargetTrueLabel()
	 * @see #getVsLabel()
	 * @see #getOutputFolder()
	 */
	public void runSingleTimeSlice(String timeSliceLabel) throws IOException {
		if (getTimeSliceVirtualCountCoefficients().size() != getOutputTimeSliceLabels().size()) {
			throw new IllegalArgumentException("Output timeSlices had size " + getOutputTimeSliceLabels().size() 
					+ ", while timeSlicely virtual counts had size " + getTimeSliceVirtualCountCoefficients().size());
		}
		
		// randomly pick 2 discrete vars
		String discreteVar1 = "";
		String discreteVar2 = "";
		boolean isToUseDiscreteVars = (getDiscreteVarsLabels().size() >= 2);
//		if (getDiscreteVarsLabels().size() < 2) {
//			throw new IllegalArgumentException("There must be at least 2 discrete vars.");
//		} 
		
		if (isToUseDiscreteVars) {
			List<String> listWithoutSubstitution = new ArrayList<String>(getDiscreteVarsLabels());
			discreteVar1 = listWithoutSubstitution.remove(getRandom().nextInt(listWithoutSubstitution.size()));
			discreteVar2 = listWithoutSubstitution.remove(getRandom().nextInt(listWithoutSubstitution.size()));
			
			// make sure var1 is smaller than var2 in the index of original list
			if (getDiscreteVarsLabels().indexOf(discreteVar1) > getDiscreteVarsLabels().indexOf(discreteVar2)) {
				// swap them, so that var1 has smaller index than var2
				String aux = discreteVar2;
				discreteVar2 = discreteVar1;
				discreteVar1 = aux;
			}
		}
		
		// obtain what is the virtual count we should use when generating Dirichlet samples from current timeSlice
		float virtualCountCoef = getTimeSliceVirtualCountCoefficients().get(getOutputTimeSliceLabels().indexOf(timeSliceLabel));
		
		File timeSliceFolder = null;
		if (getOutputFolder() != null && !getOutputFolder().isEmpty()) {
			timeSliceFolder = new File(getOutputFolder(),timeSliceLabel);
			Files.createDirectories(timeSliceFolder.toPath());
		}
		for (int organization = 0; organization < getNumOrganizations(); organization++) {
			int indexOfTimeSliceInInput = getInputTimeSliceLabels().indexOf(timeSliceLabel);
			if (indexOfTimeSliceInInput < 0) {
				// randomly pick input data timeSlice, because we don't have data to simulate current time slice (i.e. use random time slice to simulate current one)
				double[] distribution = normalize(getInputTimeSliceDistribution());
				if (distribution.length > getInputTimeSliceLabels().size()) {
					throw new IllegalArgumentException("Input timeSlice distribution had size " + distribution.length
							+ ", while number of labels of input timeSlices provided was " + getInputTimeSliceLabels().size());
				}
//				indexOfTimeSliceInInput = getRandom().nextInt(getInputTimeSliceLabels().size());
				indexOfTimeSliceInInput = getRandom().nextDiscrete(distribution);
			}
			String dataTimeSlice = getInputTimeSliceLabels().get(indexOfTimeSliceInInput);	// label of time slice picked for simulating current month
			
			
			// read csv files in order to get number of users for each state of target/class variable
			// if we are using discrete vars, instantiate a list that will store their contingency tables. Keep it null if we are not using such discrete vars
			List<PotentialTable> discreteVarTablesByTargetState = isToUseDiscreteVars?new ArrayList<PotentialTable>(getTargetStateLabels().size()):null;
			List<Double> targetCounts = new ArrayList<Double>(getTargetStateLabels().size());	// how many users there are for each possible state of target/class variable
			List<Integer> hasNoDataTimeSliceSpecification = new ArrayList<Integer>(getTargetStateLabels().size());	// Indexes of which data/file didn't have dataTimeSlice
			for (int targetIndex = 0; targetIndex < getTargetStateLabels().size(); targetIndex++) {
				String targetStateLabel = getTargetStateLabels().get(targetIndex);
				if (isToUseDiscreteVars) {
					// read from discrete (binary) vars file
					// csv file of discrete variables has this format
					String fileName = getFileNamePrefix() + dataTimeSlice + getTimeSliceVarSeparator() + discreteVar1 + getVsLabel() + discreteVar2 + getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix();
					PotentialTable table = null;
					if (new File(getInputFolder(),fileName).exists()) {
						table = readTableFromCSV(fileName , discreteVar1, discreteVar2, getInitialTableCount(), !is1stVarInRow());
						// obtain target distribution from discrete var data
						targetCounts.add(table.getSum() - (getInitialTableCount()*table.tableSize()));	// this subtraction compensates the counts that were artificially added by getInitialTableCount() in all cells
					} else {
						// there is no data timeSlice specified. Try without dataTimeSlice
						fileName = getFileNamePrefix() + discreteVar1 + getVsLabel() + discreteVar2 + getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix();
						table = readTableFromCSV(fileName , discreteVar1, discreteVar2, getInitialTableCount(), !is1stVarInRow());
						hasNoDataTimeSliceSpecification.add(targetIndex);	// mark this table as "no dataTimeSlice specified"
						// fill count with default count (to be calculated later). Use zero, so that we can use getSum to obtain the counts we know so far.
						targetCounts.add(0d);	
					}
					// keep table in memory
					discreteVarTablesByTargetState.add(table);
				} else {
					// read target var counts from csv of any continuous var of current timeSlice (use 1st var)
					// csv file has this format
					String fileName = getFileNamePrefix() + dataTimeSlice + getTimeSliceVarSeparator() + discreteVar1 + getVsLabel() + getContinuousVarsLabels().get(0) 
							+ getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix(); 
					if (new File(getInputFolder(),fileName).exists()) {
						targetCounts.add(readTotalCountContinuous(fileName));
					} else {
						// there is no data timeSlice specified. Try without dataTimeSlice
						fileName = getFileNamePrefix() + discreteVar1 + getVsLabel() + getContinuousVarsLabels().get(0) 
								+ getTargetLabelSeparator() + targetStateLabel + getFileNameSuffix(); 
						// fill count with default count (to be calculated later). Use zero, so that we can use getSum to obtain the counts we know so far.
						targetCounts.add(0d);	
						hasNoDataTimeSliceSpecification.add(targetIndex);	// mark this table as "no dataTimeSlice specified"
					}
				}
				
			}
			
			if (!hasNoDataTimeSliceSpecification.isEmpty()) {
				// calculate how many users there are in data with time slices (the data without time slices are 0, so it's ignored in sum)
				int sum = getSum((List)targetCounts).intValue();
				// calculate target counts of data with no time slice as (total count - sum);
				for (Integer index : hasNoDataTimeSliceSpecification) {
					if (targetCounts.get(index) != 0d) {
						throw new RuntimeException("Failed to treat data file with no specification of time slice.");
					}
					int totalCounts = getTotalCounts();
					if (totalCounts <= 0) {
						// if not specified, use number of users to simulate as total counts by default
						totalCounts = getNumUsers();
					}
					double count = (totalCounts - sum) / hasNoDataTimeSliceSpecification.size();	// if there are multiple data with no time slice specified, uniformly distribute counts
					if (count < 0) {
						// this may happen if data has more counts/users than expected
						Debug.println(getClass(), "Unexpected counts. Total specified in arguments = " + totalCounts + "; total in data = " + sum);
//						if (virtualCountCoef >= 0) {
//							count = 1;	// if we are using dirichlet-multinomial, make sure no state is impossible
//						} else {
							count = 0;	// if we are not using dirichlet-multinomial, this state can be impossible
//						}
					}
					targetCounts.set(index.intValue(), count);
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
			if (timeSliceFolder != null) {
				printer = new PrintStream(new FileOutputStream(new File(timeSliceFolder, getFileNamePrefix() + (getFileNamePrefix().isEmpty()?"":"_") 
						+ timeSliceLabel + "_" + numbering + getFileNameSuffix()), false));
			}
			
			// print the header
			printer.print("user,target");
			for (String var : getDiscreteVarsLabels()) {
				printer.print("," + var);
			}
			for (String var : getContinuousVarsLabels()) {
				printer.print("," + var);
			}
			printer.println();
			
			
			
			for (int user = 0; user < getNumUsers(); user++) {
				printer.print(""+user);
				
				// sample target. 
//				int targetState = getRandom().nextDiscrete(dirichlet.nextDistribution());
				int targetState = sampleTargetState(virtualCountCoef, targetCounts);
				
				
				String targetLabel = getTargetStateLabels().get(targetState);	// prepare in advance the label of target state, to be used to access respective files
				
				if (getTargetStateLabels().size() == 2) {
					// binary target. Use 0 or 1
					printer.print("," + targetState);
				} else {
					// multinomial target. Use label directly
					printer.print("," + targetLabel);
				}
				
				
				Map<String, Integer> discreteVarNameToValueMap = new HashMap<String, Integer>();	// keep track of sampled value
				if (isToUseDiscreteVars) {
					// sample discrete vars given target
					PotentialTable tableToSample = discreteVarTablesByTargetState.get(targetState);	// for binary case, sample from tableTrue if target == true. Or else, sample from tableFalse
					
					// get a sample of all variables in the table
					int[] sample = tableToSample.getMultidimensionalCoord(getSampleIndex(tableToSample, virtualCountCoef));
					// extract values of each variable
					for (String varName : getDiscreteVarsLabels()) {
						// get variable from name
						Node node = (Node) getNameToVariableCache().get(varName);
						// store and print the value of current variable
						String value = node.getStateAt(sample[tableToSample.getVariableIndex(node)]);
						discreteVarNameToValueMap.put(varName, Integer.parseInt(value));
						printer.print("," + value);
					}
				}
				
				
				// sample continuous vars;
				for (String continuousVarName : getContinuousVarsLabels()) {
					// randomly pick a discrete var to be used as condition for other continuous vars;
					String conditionVariableName = discreteVar1;
					if (getRandom().nextBoolean()) {
						conditionVariableName = discreteVar2;
					}
					
					String fileName = getFileNamePrefix() + dataTimeSlice + getTimeSliceVarSeparator() + conditionVariableName + getVsLabel() + continuousVarName + getTargetLabelSeparator() + targetLabel  + getFileNameSuffix(); // csv file has this format
					double sampleValue = Float.NaN;
					try {
						sampleValue = getSampleContinuous(fileName, virtualCountCoef, discreteVarNameToValueMap.get(conditionVariableName), getInitialTableCount());
					} catch (IOException e) {
						// try without dataTimeSlice
						fileName = getFileNamePrefix() + conditionVariableName + getVsLabel() + continuousVarName + getTargetLabelSeparator() + targetLabel  + getFileNameSuffix(); // csv file has this format
						try {
							sampleValue = getSampleContinuous(fileName, virtualCountCoef, discreteVarNameToValueMap.get(conditionVariableName), getInitialTableCount());
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
	 * @param virtualCountCoef : if this is negative or {@link #isToSampleTargetExact()} is false, then 
	 * sampling will be probabilistic by using a {@link Dirichlet} distribution.
	 * This will be multiplied to counts (lower counts result in higher variance).
	 * @param targetCounts : counts of target/class variable.
	 * If virtualCount is negative or {@link #isToSampleTargetExact()}, then values in this array will be decremented
	 * as samples are generated (i.e. sampling without substitution).
	 * @return sample of target variable.
	 */
	protected int sampleTargetState(float virtualCountCoef, List<Double> targetCounts) {
		
		if (isToSampleTargetExact() || virtualCountCoef < 0) {
			
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
			Dirichlet dirichlet = getDirichletCache().get(targetCounts + "," + virtualCountCoef);
			if (dirichlet == null) {
				// we may need to normalize targetCounts if virtual counts is positive, so calculate sum (to be used later for normalization)
//				double sum = 0;
//				if (virtualCountCoef > 0) {
//					for (double count : targetCounts) {
//						sum += count;
//					}
//				}
				// calculate parameters of dirichlet
				double[] alpha = new double[targetCounts.size()];
				Arrays.fill(alpha, 0.0);
				for (int i = 0; i < targetCounts.size(); i++) {
					alpha[i] = targetCounts.get(i) + getInitialTableCount(); //initialTableCount = 1 is used avoid zeros (common practice in bayesian learning)
					if (virtualCountCoef > 0) {
//						alpha[i] *= virtualCount/sum; // this is equivalent to normalizing targetCounts to 1 and then multiplying virtual counts
						alpha[i] *= virtualCountCoef; 
					}
				}
				dirichlet = new Dirichlet(alpha);
				getDirichletCache().put(targetCounts + "," + virtualCountCoef, dirichlet);
			}
			
			return  getRandom().nextDiscrete(dirichlet.nextDistribution());
		}

	}

	/**
	 * Generate a sample from dirichlet-multinomial distribution
	 * @param table
	 * @param virtualCountCoef : this coefficient will be multiplied to sample counts before generating dirichlet multinomial samples.
	 * Use values between 0(exclusive) and 1(inclusive) to adjust virtual counts. Value 0 will be considered as 1. 
	 * Values higher than 1 will make virtual counts higher than observed counts.
	 * @param normalize : if true, table will be normalized
	 * @return
	 */
	public int getSampleIndex(PotentialTable table, float virtualCountCoef) {
		
		// check cache first
		Dirichlet dirichlet = getDirichletCache().get(table.toString() + "_" + virtualCountCoef);
		double[] alpha = null;	// the alpha parameter of dirichlet (which can be used as prob of multinomial if we don't want to use dirichlet)
		
		if (dirichlet == null) {
			
			float coefficientToUse = virtualCountCoef;	// don't overwrite original coefficient
			
			PotentialTable origTable = table;
			if (virtualCountCoef <= 0) {
				// do not normalize table, and use the counts in table directly
				coefficientToUse = 1;
//			} else {
//				// normalize table and use virtual counts
//				table = table.getTemporaryClone();	// use a clone, so that we don't modify original table
//				table.normalize();
			}
			
			alpha = new double[table.tableSize()];
			for (int i = 0; i < table.tableSize(); i++) {
				alpha[i] = table.getValue(i) * coefficientToUse;
			}
			dirichlet = new Dirichlet(alpha);
			
			getDirichletCache().put(origTable.toString() + "_" + virtualCountCoef, dirichlet);
		}
		
		if (virtualCountCoef < 0) {
			// if negative, just use multinomial instead of dirichlet-multinomial
			// re-extract alpha from cached dirichlet instance
			alpha = new double[dirichlet.size()];
			for (int i = 0; i < dirichlet.size(); i++) {
				alpha[i] = dirichlet.alpha(i) - getInitialTableCount();	// compensate the value we used to fill tables initially (to avoid zeros) by subtracting initialTableCount
			}
			return getRandom().nextDiscrete(normalize(alpha));
		}
		return getRandom().nextDiscrete(dirichlet.nextDistribution());
		
	}
	
	
	/**
	 * 
	 * @param fileName : file to read histogram of continuous variable
	 * @param virtualCountCoef : this coefficient will be multiplied to sample counts before generating dirichlet multinomial samples.
	 * Use values between 0(exclusive) and 1(inclusive) to adjust virtual counts. Value 0 will be considered as 1. 
	 * Values higher than 1 will make virtual counts higher than observed counts.
	 * @param conditionState : if non-null, it is assumed that file is conditioned to another discrete variable and this is the state of such discrete var.
	 * @param initialCounts  : this value will be added to all entries initially (use 0 to void this parameter).
	 * @return a sample (point value) of continuous variable
	 * @throws IOException 
	 */
	public double getSampleContinuous(String fileName, float virtualCountCoef, Integer conditionState, float initialCounts) throws IOException {
		
		// read csv file
		Map<String, List<Double>> map = readContinuousVarCSVFile(fileName, conditionState, initialCounts);
		
		Dirichlet dirichlet = getDirichletCache().get(fileName + "_" + virtualCountCoef + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts);
		double[] alpha = null;	// alpha parameters of dirichlet. This can also be used as prob of multinomial if we don't want to use dirichlet
		
		if (dirichlet == null) {
			
			float coefficientToUse = virtualCountCoef;	// do not overwrite initial virtual count
			
			List<Double> counts = map.get(BIN_COUNTS_KEY);
			if (virtualCountCoef <= 0) {
				// do not normalize list, and use the counts in list directly
				coefficientToUse = 1;
//			} else {
//				// normalize list and use virtual counts
//				double sum = 0;
//				for (Double count : counts) {
//					sum += count;
//				}
//				for (int i = 0; i < counts.size(); i++) {
//					counts.set(i, counts.get(i) / sum);
//				}
			}
			
			// use dirichlet-multinomial sampler to sample a bin
			alpha = new double[counts.size()];
			for (int i = 0; i < counts.size(); i++) {
				alpha[i] = counts.get(i) * coefficientToUse;
			}
			dirichlet = new Dirichlet(alpha);
			
			getDirichletCache().put(fileName + "_" + virtualCountCoef + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts, dirichlet);
		}
		
		int bin = -1;

		if (virtualCountCoef < 0) {
			// if negative, just use multinomial instead of dirichlet-multinomial to sample a bin
			// extract alpha from cached dirichlet instance
			alpha = new double[dirichlet.size()];
			for (int i = 0; i < dirichlet.size(); i++) {
				alpha[i] = dirichlet.alpha(i) - initialCounts;	// compensate the value we used to fill tables initially (to avoid zeros) by subtracting initialCounts
			}
			bin = getRandom().nextDiscrete(normalize(alpha));
		} else {
			// sample bin from dirichlet multinomial
			bin = getRandom().nextDiscrete(dirichlet.nextDistribution());
		}
		
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
		Map<String, List<Double>> map = readContinuousVarCSVFile(fileName, null, 0f);	// add 0 to counts, because we want actual counts here
		
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
	public Map<String,List<Double>> readContinuousVarCSVFile(String fileName, Integer conditionState, float initialCounts) throws IOException {
		
		// check cache
		Map<String,List<Double>> ret = getContinuousVarFileCache().get(fileName + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts);
		if (ret != null) {
			return ret;
		}
		
		// read csv file
		CSVReader reader = new CSVReader(new FileReader(new File(getInputFolder(),fileName)));
		
		// prepare keyword which will indicate beginning of data block
		String dataBlockKeyword = getHeaderColumnKeyword().toLowerCase();
		
		// read rows until we find the block we want (which matches with condition state)
		int countColumn = -1;	// this will be filled with which column has the data given conditionState
		String[] row = null;
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			if (row[0].toLowerCase().contains(dataBlockKeyword) && row.length >= 3) {
				// check if condition matched
				if (conditionState == null) {
					// no condition was specified, so we can stop at any block (so just use the 1st block we found)
					countColumn = 2;	// we can use any column. Use smallest (column 0 and 1 are "Lower bound" and "Upper bound", so these aren't included).
					break;
				} else {
					// find column that matches with conditionState
					for (int column = 2; column < row.length; column++) {	// column 0 and 1 are "Lower bound" and "Upper bound", so ignore them.
						if (row[column].matches(".*=.*")) {
							String[] split = row[column].trim().split("=");
							if (conditionState.intValue() == Integer.parseInt(split[1])) {
								countColumn = column;
								break;	// found a matching condition
							}
						}
					}
					break;
				}
			}
		}
		if (row == null || countColumn < 0) {
			reader.close();
			throw new IllegalArgumentException("Var value " + conditionState + " not found in file " + fileName);
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
			if (row[0].toLowerCase().contains(dataBlockKeyword)) {
				break;	// this is the next block, so stop here
			}
			ret.get(BIN_LOWER_KEY).add(Double.parseDouble(row[0]));
			ret.get(BIN_UPPER_KEY).add(Double.parseDouble(row[1]));
			ret.get(BIN_COUNTS_KEY).add(Double.parseDouble(row[countColumn]) + initialCounts);
		}
		
		reader.close();
		
		getContinuousVarFileCache().put(fileName + "_" + ((conditionState != null)?conditionState:"") + "_" + initialCounts, ret);
		return ret;
	}

	/**
	 * Read a table of discrete variables from csv file
	 * @param fileName : name of file to read
	 * @param var1 : 1st variable (it will be added as 2nd variable in table -- states will iterate less often -- row in csv)
	 * @param var2 : 2nd variable (it will be added as 1st variable in table -- states will iterate most often -- column in csv)
	 * @param targetStateLabel : label indicating whether target variable is true or false. This will be used for file name.
	 * @param initialTableValue : this value will be added to all entries initially (use 0 to void this parameter).
	 * @param transpose : if false, then var1 is the row of table (and var2 is the column). 
	 * If true, then var1 is the column (and var2 is the row).
	 * @return the table read from csv file
	 * @throws IOException
	 */
	public PotentialTable readTableFromCSV(String fileName,String var1, String var2, float initialTableValue, boolean transpose) throws IOException {

		// check cache
		PotentialTable ret = getTableCache().get(fileName + "_" + var1 + "_" + var2 + "_" + initialTableValue);
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
		INode det2Node = getNameToVariableCache().get(var2);	// var 2 iterates on each column, so add it first
		if (det2Node == null) {
			det2Node = new ProbabilisticNode();
			det2Node.setName(var2);
			det2Node.appendState("0");
			det2Node.appendState("1");
			getNameToVariableCache().put(var2, det2Node);
		}
		ret.addVariable(det2Node);
		INode det1Node = getNameToVariableCache().get(var1);
		if (det1Node == null) {
			det1Node = new ProbabilisticNode();
			det1Node.setName(var1);
			det1Node.appendState("0");
			det1Node.appendState("1");
			getNameToVariableCache().put(var1, det1Node);
		}
		ret.addVariable(det1Node);
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
		
		if (transpose) {
			// backup data
			ret.copyData();
			int[] coordWrite = ret.getMultidimensionalCoord(0);			// initialize (allocate space for) index of transposed index
			for (int cellIndex = 0; cellIndex < ret.tableSize(); cellIndex++) {
				// calculate the coordinate of original table corresponding to current cell
				int[] coordRead = ret.getMultidimensionalCoord(cellIndex);	// non-transposed indexes
				// calculate cell in transposed table (i.e. just reverse the order)
				for (int readIndex = 0, writeIndex = coordWrite.length-1; readIndex < coordWrite.length; readIndex++, writeIndex--) {
					coordWrite[writeIndex] = coordRead[readIndex];
				}
				// copy from backup to table value
				ret.setValue(coordWrite, ret.getCopiedValue(cellIndex));
			}
			
		}
		
		getTableCache().put(fileName + "_" + var1 + "_" + var2 + "_" + initialTableValue, ret);
		
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
	 * @return the continuousVarsLabel
	 */
	public List<String> getContinuousVarsLabels() {
		return this.continuousVarsLabels;
	}

	/**
	 * @param continuousVarsLabels the continuousVarsLabel to set
	 */
	public void setContinuousVarsLabels(List<String> continuousVarsLabels) {
		this.continuousVarsLabels = continuousVarsLabels;
	}

	/**
	 * @return the discreteVarsLabel
	 */
	public List<String> getDiscreteVarsLabels() {
		return this.discreteVarsLabels;
	}

	/**
	 * @param discreteVarsLabels the discreteVarsLabel to set
	 */
	public void setDiscreteVarsLabels(List<String> discreteVarsLabels) {
		this.discreteVarsLabels = discreteVarsLabels;
	}

	/**
	 * @return the inputTimeSliceLabel
	 */
	public List<String> getInputTimeSliceLabels() {
		return this.inputTimeSliceLabel;
	}

	/**
	 * @param inputTimeSliceLabel the inputTimeSliceLabel to set
	 */
	public void setInputTimeSliceLabels(List<String> inputTimeSliceLabel) {
		this.inputTimeSliceLabel = inputTimeSliceLabel;
	}

	/**
	 * @return the outputTimeSliceLabel
	 */
	public List<String> getOutputTimeSliceLabels() {
		return this.outputTimeSliceLabels;
	}

	/**
	 * @param outputTimeSliceLabel the outputTimeSliceLabel to set
	 */
	public void setOutputTimeSliceLabels(List<String> outputTimeSliceLabel) {
		this.outputTimeSliceLabels = outputTimeSliceLabel;
	}

	/**
	 * @return the timeSliceVirtualCounts
	 */
	public List<Float> getTimeSliceVirtualCountCoefficients() {
		return this.timeSliceVirtualCountCoefficients;
	}

	/**
	 * @param timeSlicelyVirtualCounts the timeSliceVirtualCounts to set
	 */
	public void setTimeSliceVirtualCountCoefficients(List<Float> timeSliceVirtualCounts) {
		this.timeSliceVirtualCountCoefficients = timeSliceVirtualCounts;
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
	 * @return the continuousVarFileCache
	 */
	protected Map<String, Map<String,List<Double>>> getContinuousVarFileCache() {
		if (continuousVarFileCache == null) {
			continuousVarFileCache = new HashMap<String, Map<String,List<Double>>>();
		}
		return continuousVarFileCache;
	}

	/**
	 * @param continuousVarFileCache the continuousVarFileCache to set
	 */
	protected void setContinuousVarFileCache(
			Map<String, Map<String,List<Double>>> continuousVarFileCache) {
		this.continuousVarFileCache = continuousVarFileCache;
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
	 * @return the timeSliceVarSeparator
	 */
	public String getTimeSliceVarSeparator() {
		return timeSliceVarSeparator;
	}

	/**
	 * @param timeSliceVarSeparator the timeSliceVarSeparator to set
	 */
	public void setTimeSliceVarSeparator(String timeSliceVarSeparator) {
		this.timeSliceVarSeparator = timeSliceVarSeparator;
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
	 * @return the multinomial probability distribution of a timeSlice in {@link #getInputTimeSliceLabels()}
	 * to be randomly picked as input data when generating samples of timeSlices when we don't have respective input data
	 * (i.e. timeSlices in {@link #getOutputTimeSliceLabels()} which is not in {@link #getInputTimeSliceLabels()}).
	 */
	public double[] getInputTimeSliceDistribution() {
		return inputTimeSliceDistribution;
	}

	/**
	 * @param inputTimeSliceDistribution : the multinomial probability distribution of a timeSlice in {@link #getInputTimeSliceLabels()}
	 * to be randomly picked as input data when generating samples of timeSlices when we don't have respective input data
	 * (i.e. timeSlices in {@link #getOutputTimeSliceLabels()} which is not in {@link #getInputTimeSliceLabels()}).
	 */
	public void setInputTimeSliceDistribution(double[] inputTimeSliceDistribution) {
		this.inputTimeSliceDistribution = inputTimeSliceDistribution;
	}

	/**
	 * @return the is1stVarInRow : if true, contingency tables of discrete vars in csv files will be considered to have
	 * 1st variable in rows, and 2nd variable in columns.
	 */
	public boolean is1stVarInRow() {
		return is1stVarInRow;
	}

	/**
	 * @param is1stVarInRow the is1stVarInRow to set : if true, contingency tables of discrete vars in csv files will be considered to have
	 * 1st variable in rows, and 2nd variable in columns.
	 */
	public void set1stVarInRow(boolean is1stVarInRow) {
		this.is1stVarInRow = is1stVarInRow;
	}


	/**
	 * @return the totalCounts: total (sum) of counts expected in input histograms. This should be usually the same of {@link #getNumUsers()},
	 * but it is separated because we may want to simulate n users while the data contains m users.
	 */
	public int getTotalCounts() {
		return totalCounts;
	}

	/**
	 * @param totalCounts the totalCounts to set : : total (sum) of counts expected in input histograms. This should be usually the same of {@link #getNumUsers()},
	 * but it is separated because we may want to simulate n users while the data contains m users.
	 */
	public void setTotalCounts(int totalCounts) {
		this.totalCounts = totalCounts;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("usr","num-users", true, "Number of users (samples or individuals) to simulate.");
		options.addOption("total","total-counts-num-users", true, "Number of users (samples or individuals) expected in data. If negative or 0, then this will be the same of specified in -usr.");
		options.addOption("org","num-organizations", true, "Number of organizations (group of users) to simulate. This is number of replications.");
		options.addOption("prefix","file-name-prefix", true, "Common prefixes of csv file names in the input folder (e.g. \"RCP11\", default is empty).");
		options.addOption("suffix","file-name-suffix-extension", true, "Common suffixes or extensions of file names in the input folder (default \".csv\").");
		options.addOption("varSep","separator-var-vs-label", true, "Suffixes in input csv file names that separates names of vars (default \"vs\").");
		options.addOption("targetSep","target-label-separator", true, "Suffixes in input csv file names that separates target state from other names (default \"_\").");
		options.addOption("timeSliceSep","timeSlice-var-label-separator", true, "Suffixes in input csv file names that separates time slice labels from variable names (default \"_\").");
		options.addOption("target","target-labels", true, "Comma separated list of names of states of target variable. This will also be used as label/suffix in csv file that indicates that the data is for target = false (default \"False,True\")."
				+ " This must match with true/false values that appear in names of csv files.");
		options.addOption("initCount","initial-table-count", true, "This value will be added to counts for dirichlet-multinomial sampling (real numbers are also allowed).");
		options.addOption("o","output", true, "Folder to write. If not specified, \"output\" will be used.");
		options.addOption("i","input", true, "Input folder to read. If not specified, \"input\" will be used.");
		options.addOption("discrete","discrete-var-labels", true, "Comma-separated list of variables that are discrete (e.g. \"1,2\", default is empty)."
				+ " This must match with vars that appear in names of csv files.");
		options.addOption("continuous","continuous-var-labels", true, "Comma-separated list of var numbers that are continuous in this RCP "
				+ "(default \"3a,3b,3c,4a,4b,4c,5a,6a,6b\"). This must match with vars that appear in names of csv files.");
		options.addOption("inputTimeSlices","input-timeSlice-labels", true, "Comma-separated list of timeSlice labels that we have input data (e.g. \"JAN,FEB\", default is empty)."
				+ " This must match with timeSlices that appear in names of csv files.");
		options.addOption("allTimeSlices","all-output-timeSlice-labels", true, "Comma-separated list of all timeSlice labels to output (e.g. \"JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP\" , default is empty)."
				+ " This must match with timeSlices that appear in names of csv files.");
		options.addOption("virtCountCoefs","timeSlice-virtual-count-coefficients", true, "Comma-separated list of numbers (virtual counts) to be multiplied to number of users of each timeSlice when sampling from dirichlet-multinomial"
				+ "(e.g. \"-1,-1,.5,.25,.125,.06,.03,.015,.008\", default is empty). Zero or 1 can be used to consider actual counts instead (if the timeSlice has actual data). "
				+ "Negative values can be used to disable dirichlet-multinomial sampling.");
//		options.addOption("numStates","num-states-class-variable", true, "Number of states of the class or target variable (default is binary, which is 2).");
		options.addOption("targetExact","sample-target-class-variable-exactly", true, "If set, target/class variable will be sampled without dirichlet-multinomial distribution.");
		options.addOption("seed","random-seed", true, "Number to be used as random seed (default is system's time).");
		options.addOption("inputDist","input-timeSlice-distribution", true, "Comma-separated list of probabilities (default is \".5,.5\"). "
				+ "This is used to randomly pick an input timeSlice when simulating timeSlices with no input data. "
				+ "The length of this list must match with list provided in -inputTimeSlices.");
		options.addOption("transpose","transpose-discrete-vars-table", true, "If this argument is specified, csv files of contingency tables of discrete variables will be consided to have 1st var as column and 2nd var as rows."
				+ "If unspecified, then 1st variable will be rows, and 2nd variable will be column.");
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
		if (cmd.hasOption("total")) {
			sim.setTotalCounts(Integer.parseInt(cmd.getOptionValue("total")));
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
		if (cmd.hasOption("varSep")) {
			sim.setVsLabel(cmd.getOptionValue("varSep"));
		}
		if (cmd.hasOption("targetSep")) {
			sim.setTargetLabelSeparator(cmd.getOptionValue("targetSep"));
		}
		if (cmd.hasOption("timeSliceSep")) {
			sim.setTimeSliceVarSeparator(cmd.getOptionValue("timeSliceSep"));
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
			sim.setDiscreteVarsLabels(parseListString(cmd.getOptionValue("discrete")));
		}
		if (cmd.hasOption("continuous")) {
			sim.setContinuousVarsLabels(parseListString(cmd.getOptionValue("continuous")));
		}
		if (cmd.hasOption("inputTimeSlices")) {
			sim.setInputTimeSliceLabels(parseListString(cmd.getOptionValue("inputTimeSlices")));
		}
		if (cmd.hasOption("allTimeSlices")) {
			sim.setOutputTimeSliceLabels(parseListString(cmd.getOptionValue("allTimeSlices")));
		}
		if (cmd.hasOption("virtCountCoefs")) {
			sim.setTimeSliceVirtualCountCoefficients(parseListFloat(cmd.getOptionValue("virtCountCoefs")));
		}
		if (cmd.hasOption("seed")) {
			sim.setSeed(Long.parseLong(cmd.getOptionValue("seed")));
		}
		if (cmd.hasOption("inputDist")) {
			sim.setInputTimeSliceDistribution(parseArrayDouble(cmd.getOptionValue("inputDist")));
		}
		
		if (cmd.hasOption("transpose")) {
			sim.set1stVarInRow(!Boolean.parseBoolean(cmd.getOptionValue("transpose")));
		}
		
		if (cmd.hasOption("targetExact")) {
			sim.setToSampleTargetExact(Boolean.parseBoolean(cmd.getOptionValue("targetExact")));
		}
		
		sim.runAll();
	}


}
