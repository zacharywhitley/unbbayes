/**
 * 
 */
package utils;

import io.ActivityHistCSVIO;
import io.CSVJointDistributionReader;
import io.IActivityHistIO;
import io.IJointDistributionReader;
import io.IModelCenterWrapperIO;
import io.IPeerGroupReader;
import io.ModelCenterMatrixStyleWrapperIO;
import io.PeerGroupSizeCSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import unbbayes.prs.INode;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class UserActivitySimulator {

	private String activityHistFileName = "RCP8_ActivityHist.csv";
	private String peerGroupSizeFile = "rcp7_group_sizes_withTRUE.csv";
	private String rawDataOutput = "userActivity.csv";
	private String transformedDataOutput = "detectorsDays.csv";
	private float block1CutoffPercent = .8f;
	private int totalUsers = 100;//3816;
	private int totalDays = 60;//163;
	private int testDayThreshold = 36;//144;
	private int numTimeBlocks = 6;
	private int numDetectors = 24;
	private String rScriptName = "RCP8_ComputeAlertDays_Driver.r";
	private String correlationDataFileFolder = "CorrelationData";
	private String rscriptProgramName = "RScript";
	private String distanceMetricFileName = "UserActivitySimulator.out";
	private int daysToRedrawUserAttitude = 20;
	private boolean isToAdd1ToCounts = true;
	private int firstDay = 0;
	
	private IJointDistributionReader transformedDataReader = new CSVJointDistributionReader();
	private IModelCenterWrapperIO wrapperIO = null;
	private Long seed = null;
	private IActivityHistIO io = null;
	private List<Integer> block1TokensGood = null;
	private List<Integer> block2TokensGood = null;
	private List<Integer> block1TokensBad = null;
	private List<Integer> block2TokensBad = null;
	private Map<String, Float> badUserProbs = null;
	
	/**
	 * Default constructor
	 */
	public UserActivitySimulator() {}
	
	
	/**
	 * 
	 * @param dist
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Float> getCumulativeDist(List<Number> dist) {
		
		if (dist == null || dist.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		
		
		List<Float> ret = new ArrayList<Float>(dist.size());	// the list to return
		
		// convert to cumulative dist
		float sum = 0f;
		for (Number number : dist) {
			sum += number.floatValue();
			ret.add(sum);
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param cumulative
	 * @param percentile
	 * @return
	 */
	public int getPercentileIndex(List<Float> cumulative, float percentile) {
		
		if (percentile < 0 || percentile > 1) {
			throw new IllegalArgumentException("Percentiles must be between 0 and 1: " + percentile);
		}
		
		// check at which index is the percentile
		for (int i = 0; i < cumulative.size(); i++) {
			if (cumulative.get(i) > percentile) {
				return i;
			}
		}
		
//		throw new IllegalArgumentException("Unable to find percentile " + percentile + " in list " + cumulative);
		System.err.println("Unable to find percentile " + percentile + " in list " + cumulative);
		return 0;	// return the 1st element by default
	}

	/**
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void simulateAll() throws IOException, InterruptedException {
		
		// make sure the output files are deleted prior to execution
		new File(getTransformedDataOutput()).delete();
		new File(getRawDataOutput()).delete();
		new File(getDistanceMetricFileName()).delete();
		
		// generate a csv file with raw data
		simulateRawData();
		
		// call PCA to generate csv file with transformed (PCA) data 
		generateTransformedData();
		
		// compute the distance of transformed correlation (expected VS actual)
		computeDistance();
		
	}

	


	/**
	 * @throws IOException 
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void simulateRawData() throws IOException {

		// prepare random number generator
		Random rand = new Random(getSeed());
		
		// load to cache the user activity histogram
		IActivityHistIO io = getIO();
		io.load(new FileInputStream(new File(getActivityHistFileName())));
		
		// prepare and load peer group sizes
		IPeerGroupReader groupIO = new PeerGroupSizeCSVReader();
		groupIO.load(new FileInputStream(new File(getPeerGroupSizeFile())));
		List<String> peerGroupSizes = groupIO.getPeerGroupSizes();
		List<Integer> peerGroupCounts = new ArrayList<Integer>(groupIO.getPeerGroupCounts());
		
		// extract the first day to be considered test day (all days prior to this are training days)
		int firstTestDay = getTestDayThreshold();
		if (firstTestDay < 0 || firstTestDay > getTotalDays()) {
			throw new IllegalArgumentException("1st test day was set to " + firstTestDay + ", but total number of days is " + getTotalDays());
		}
		
		// prepare output file to write simulated users
		PrintStream printer = new PrintStream(new FileOutputStream(rawDataOutput, false));
		
		// write the 1st row: 
		// type,timeid,grpid,userid,dayid,det1,...,det12
		printer.print("\"type\",\"timeid\",\"grpid\",\"userid\",\"dayid\"");
		for (int detector = 1; detector <= getNumDetectors()/2; detector++) {	// we only support half of the detectors for raw data
			printer.print(",\"det" + detector + "\"");
		}
		
		printer.println();
		

		// reset/initialize tokens of this user (set of block1 and block2 tokens for each detector)
		List<Integer> block1TokenGood = new ArrayList<Integer>(getBlock1TokensGood());	
		List<Integer> block2TokenGood = new ArrayList<Integer>(getBlock2TokensGood());
		List<Integer> block1TokenBad = new ArrayList<Integer>(getBlock1TokensBad());	
		List<Integer> block2TokenBad = new ArrayList<Integer>(getBlock2TokensBad());
		
		// iterate on users (userid). Start from 1
		for (int userid = 1; userid <= getTotalUsers(); userid++) {
			
			// Assign peer group ids (grpid) to user.
			
			int peerGroup = pickPeerGroup(rand, peerGroupCounts);
					
			String groupSize = peerGroupSizes.get(peerGroup);
			
			// extract probability of a user to be bad given size of peer group
			Float badUserProb = getBadUserProbs().get(groupSize);
			
			// randomly choose if this user is a bad user
			boolean isBadUser = (rand.nextFloat() < badUserProb);
			
			// iterate on day (dayid). 
			for (int day = 0; day < getTotalDays(); day++) {

				
				// check if data of this day is a testing data (type = 2) or training data (type = 1)
				int type = (day < firstTestDay)?1:2;
				
				// iterate on time (timeid), starting
				for (int time = 0; time < getNumTimeBlocks(); time++) {
					
					
					// peerGroup, time and day must start from 1 (so add 1). User id is already starting from 1.
					// write the beginning of the current row: type,time,grpid,userid,day
					printer.print(type + "," + (time+1) + "," + (peerGroup+1) + "," + userid + "," + (day+1));
					
					// iterate on detectors, but we only need to simulate raw data for half of them (the other half is for peer group outlier)
					for (int detector = 0; detector < getNumDetectors() / 2; detector++) {	
						
						// pick a token
						boolean isLowBlockToken = false;
						if (isBadUser) {
							isLowBlockToken = pickToken(block1TokenBad, block2TokenBad, detector, rand);
						} else {
							isLowBlockToken = pickToken(block1TokenGood, block2TokenGood, detector, rand);
						}
						
						// choose a distribution to sample. If token was block 1, pick lower than cutoff. If token was block 2, pick higher than cutoff
						List<Float> probDist = normalize((List)io.getNumUsers(detector, time, day + getFirstDay()));
						List<Float> cumulative = getCumulativeDist((List)probDist);	
						int block1CutoffIndex = getPercentileIndex(cumulative, getBlock1CutoffPercent());
						
						// transform the distribution accordingly to the token we picked
						if (isLowBlockToken) {
							// if picked token of lower distribution, then let higher distribution to be zero and normalize.
							for (int i = block1CutoffIndex + 1; i < cumulative.size(); i++) {
								probDist.set(i, 0f);
							}
						} else {
							// if picked token of higher distribution, then let lower distribution to be zero and normalize.
							for (int i = 0; i <= block1CutoffIndex; i++) {
								probDist.set(i, 0f);
							}
						}
						probDist = normalize((List)probDist);
						cumulative = getCumulativeDist((List)probDist);	// update the cumulative dist
						
						Integer rawDetectorSample = getRawDetectorSample(rand, cumulative, io, detector, time);
						
						// print the rest of the current row: ,det1,...,det12;
						printer.print("," + rawDetectorSample);
						
					}	// end of for detector
					
					// prepare to print next row
					printer.println();
					
				}	// end of for time
				
				if (getDaysToRedrawUserAttitude() > 0) {
					if (day+1 % getDaysToRedrawUserAttitude() == 0) {
						// redraw the type of user for next iteration
						isBadUser = (rand.nextFloat() < badUserProb);
					}
				}
				
			}	// end of for day
		}	// end of for userid
		
		printer.close();
	}
	
	/**
	 * @param rand
	 * @param peerGroupCounts : this is input/output argument (so values will be changed after invoking this method)
	 * @return an index in peerGroupCounts that was randomly chosen given its content. A count at this index will be decremented from the list.
	 */
	protected int pickPeerGroup(Random rand, List<Integer> peerGroupCounts) {
		
		// pick a choice from all available counts
		int sum = 0;
		for (Integer count : peerGroupCounts) {
			sum += count;
		}
		int choice = rand.nextInt(sum);
		
		// retrieve the index where the choice was from
		sum = 0;
		for (int group = 0; group < peerGroupCounts.size(); group++) {
			if (peerGroupCounts.get(group) <= 0) {
				continue;
			}
			sum += peerGroupCounts.get(group);
			if (sum > choice) {
				// reduce 1 count
				peerGroupCounts.set(group, peerGroupCounts.get(group) - 1);
				// return the group id 
				return group;
			}
		}
		
		// if there is no more peer groups, then pick one with uniform dist
		return rand.nextInt(peerGroupCounts.size());
	}


	/**
	 * 
	 * @param lowerBlockTokens : block 1 (low activity) tokens left for a detector. The index of list represents the detector.
	 * This is an input/output argument, so its contents will be altered when this method is invoked.
	 * @param higherBlockTokens : block 2 (high activity) tokens left for a detector. The index of list represents the detector.
	 * This is an input/output argument, so its contents will be altered when this method is invoked.
	 * @param detector 
	 * @param rand : random number generator
	 * @return true if low activity token was picked. False if high activity token was picked.
	 */
	protected boolean pickToken(List<Integer> lowerBlockTokens, List<Integer> higherBlockTokens, int detector, Random rand) {
		
		Integer numTokens1 = lowerBlockTokens.get(detector);	// number of tokens left for block1 (lower activity distribution)
		Integer numTokens2 = higherBlockTokens.get(detector);	// number of tokens left for block2 (higher activity distribution)
		
		if (numTokens1 == 0 && numTokens2 == 0) {
			Debug.println("No more tokens left for detector " + (detector + 1));
			// by default, pick a token with uniform distribution
			return rand.nextBoolean();
		}
		
		if (numTokens1 == 0) {
			// pick token 2, because there is no more token 1 left
			// decrement token 2
			higherBlockTokens.set(detector, numTokens2 - 1);
			return false;	// false indicates that token 2 was chosen
		}
		
		if (numTokens2 == 0) {
			// pick token 1, because there is no more token 2 left
			// decrement token 1
			lowerBlockTokens.set(detector, numTokens1 - 1);
			return true;	// true indicates that token 1 was chosen
		}
		
		// pick a token randomly
		
		int totalTokens = numTokens1 + numTokens2;
		
		if (rand.nextInt(totalTokens) < numTokens1) {
			// token 1 was chosen 
			// decrement token 1
			lowerBlockTokens.set(detector, numTokens1 - 1);
			return true;	// true indicates that token 1 was chosen
		} else {
			// token 2 was chosen
			// decrement token 2
			higherBlockTokens.set(detector, numTokens2 - 1);
			return false;	// false indicates that token 2 was chosen
		}
		
	}


	/**
	 * @param distro
	 */
	protected List<Float> normalize(List<Number> distro) {
		float sum = 0f;
		
		for (Number prob : distro) {
			sum += prob.floatValue();
		}
		
		List<Float> ret = new ArrayList<Float>(distro.size());
		
		for (Number prob : distro) {
			if (sum == 0f) {
				ret.add(prob.floatValue());
			} else {
				ret.add(prob.floatValue() / sum);
			}
		}
		
		return ret;
	}
	
	/**
	 * Executes the R script of {@link #getRScriptName()} passing command line arguments
	 * {@link #getRawDataOutput()} and {@link #getTransformedDataOutput()}.
	 * 
	 * It assumes that RScript (specified with {@link #getRscriptProgramName()}) is installed and invokable from command line.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void generateTransformedData() throws IOException, InterruptedException {
		
		if (!new File(getRawDataOutput()).exists()) {
			throw new IOException("File does not exist: " + getRawDataOutput());
		}
		
		Runtime rt = Runtime.getRuntime();
		
		Process pr = rt.exec(getRscriptProgramName() + " " + getRScriptName() + " " + getRawDataOutput() + " " + getTransformedDataOutput());
		
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
        	throw new IOException("Exited R script with exit code " + exitVal);
        }
	}
	
	
	/**
	 * This will compute the distance of correlation tables in {@link #getCorrelationDataFileFolder()}
	 * with the detector alert day data in {@link #getTransformedDataOutput()}
	 * @throws IOException 
	 */
	public void computeDistance() throws IOException {
		// prepare an object which will read the file of simulated detector alert days (i.e. the data transformed from raw user activity data)
		IJointDistributionReader transformedDataReader = getTransformedDataReader();
		File transformedDataFile = new File(getTransformedDataOutput());
		
		// obtain the maximum detector state in simulated data. Make sure we use support the largest of simulated and actual data
		Map<String, Integer> maxDetectorValues = transformedDataReader.getMaxValue(new FileInputStream(transformedDataFile));
		
		// read the detector correlation files

		// extract the folder where folders with pairs of csv files are placed
		File folder = new File(getCorrelationDataFileFolder());
		File[] innerFiles = folder.listFiles();
		
		// I'll use a method in this class to read correlation tables
		CSVTableMarginalConsistencyChecker checker = new CSVTableMarginalConsistencyChecker("");
		
		
		// calculate how many correlation tables we expect for each csv file
		// binomial coefficient (i.e. from numDetectors, choose 2) = fact(numDetectors) / ( (fact(numDetectors) - 2)*2 )
		int numCorrelations = (getNumDetectors() * (getNumDetectors() - 1)) / 2;
		
		// prepare a list of correlation tables (read from csv files)
		List<PotentialTable> correlationTables = new ArrayList<PotentialTable>();
		// read the files
		for (File innerFile : innerFiles) {
			Map<String, INode> sharedVariables = new HashMap<String, INode>();	// we must use same instances of variables in all tables in each file
			List<PotentialTable> tables = checker.readTablesFromCSVFile(innerFile, sharedVariables);
			
			// check that the simulated maximum detector size is supported
			tables = ensureCapacities(tables, maxDetectorValues);
			
			if (tables.size() != numCorrelations) {
				throw new IOException("Unexpected number of tables read from " + innerFile.getAbsolutePath() + ": " + tables.size());
			}
			
			correlationTables.addAll(tables);
		}
		if (correlationTables.isEmpty()) {
			throw new IOException("Unable to read correlation tables from folder " + folder.getAbsolutePath());
		}
		
		
		// for each correlation table, read the transformed detector alert days file (for comparison)
		float sum = 0;		// We'll use sum of kl divergence as the metric
		for (PotentialTable correlationTable : correlationTables) {
			
			if (isToAdd1ToCounts()) {
				// increment 1 to counts (to avoid 0)
				for (int i = 0; i < correlationTable.tableSize(); i++) {
					correlationTable.setValue(i, correlationTable.getValue(i) + 1);
				}
			}
			
			// make sure this table represents proportions between 0 and 1 (kl divergence is a distance of probability/proportions)
			correlationTable.normalize();	
			
			// create a clone of correlation table that will be filled by the csv file of transformed data
			PotentialTable transformedDataTable = (PotentialTable) correlationTable.clone();
			transformedDataTable.fillTable(-1f);	// initialize with invalid values
			
			// fill the table
			transformedDataReader.fillJointDist(transformedDataTable, new FileInputStream(transformedDataFile), false);
			
			if (isToAdd1ToCounts()) {
				// increment 1 to counts (to avoid 0)
				for (int i = 0; i < transformedDataTable.tableSize(); i++) {
					transformedDataTable.setValue(i, transformedDataTable.getValue(i) + 1);
				}
			}
			
			transformedDataTable.normalize();
			
			// calculate the distance metric (kl divergence). 
			sum += getKLDistance(correlationTable, transformedDataTable); // kl divergence is non-negative, so minimizing sum will minimize overall distance
		}
		
		// TODO calculate the distance metric regarding peer groups.
		
		// save the distance metric(s) to file (sum of kl distance)
		
		getWrapperIO().writeWrapperFile(Collections.singletonMap("Distance", ""+sum), new File(getDistanceMetricFileName()));
		
	}
	
	

	/**
	 * Ensures that tables will have a capacity that can handle the maximum state values specified by max >= node.getStatesSize().
	 * The variables used in tables may be modified or replaced.
	 * @param tables
	 * @param maxDetectorValues
	 */
	protected List<PotentialTable> ensureCapacities(List<PotentialTable> tables, Map<String, Integer> maxDetectorValues) {
		for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
			PotentialTable table = tables.get(tableIndex);
			for (int i = 0; i < table.getVariablesSize(); i++) {
				INode node = table.getVariableAt(i);
				String name = getTransformedDataReader().convertName(node.getName());
				Integer max = maxDetectorValues.get(name);
				PotentialTable newTable = ensureCapacity(table, node, max+1);	// size must be max value + 1
				tables.set(tableIndex, newTable);
				table = newTable;
			}
		}
		return tables;
	}

	/**
	 * Ensures that tables will have a capacity that can handle the size of a variable.
	 * The variables used in tables may be modified or replaced
	 * @param oldTable
	 * @param oldNode
	 * @param nodeSize
	 */
	protected PotentialTable ensureCapacity(PotentialTable oldTable, INode oldNode, Integer nodeSize) {
		if (nodeSize <= oldNode.getStatesSize()) {
			return oldTable;	// no need for changes
		}
		
		// copy the node and append states to it to ensure node size
		INode newNode = (INode) ((ProbabilisticNode)oldNode).clone();
		while (nodeSize > newNode.getStatesSize()) {
			newNode.appendState(""+(newNode.getStatesSize() + (nodeSize - newNode.getStatesSize()) -1) );
		}
		
		// copy the table into another table, but substitute the node with a larger one
		PotentialTable newTable = new ProbabilisticTable();
		for (int varIndex = 0; varIndex < oldTable.getVariablesSize(); varIndex++) {
			INode var = oldTable.getVariableAt(varIndex);
			if (var.equals(oldNode)) {
				newTable.addVariable(newNode);
			} else {
				newTable.addVariable(var);
			}
		}
		
		if (newTable.tableSize() <= oldTable.tableSize()) {
			throw new RuntimeException("Failed to increase table size. This is probably caused by a bug in older APIs.");
		}
		
		// make sure we initialize new table with zeros
		newTable.fillTable(0f);
		
		// copy the content of old table to new table
		for (int cell = 0; cell < oldTable.tableSize(); cell++) {
			// new table and old table do not share same linear index, but they can use same multi-dimensional indexes, because variables were included in same order.
			int[] coord = oldTable.getMultidimensionalCoord(cell);
			newTable.setValue(coord, oldTable.getValue(cell));
		}
		
		return newTable;
	}


	protected boolean containsVar(PotentialTable table, INode node) {
		for (int i = 0; i < table.getVariablesSize(); i++) {
			if (table.getVariableAt(i).equals(node)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * @param expected : expected distribution
	 * @param actual : approximate distribution
	 * @return The Kullback–Leibler divergence D(expected||actual)
	 */
	public float getKLDistance(PotentialTable expected , PotentialTable actual) {
		// basic assertions
		if ((expected == null && actual == null) || (expected.tableSize() == 0 && expected.tableSize() == 0)) {
			// by default, if both are null/empty, consider them equal distribution
			return 0f;	// 0 means no divergence
		}
		if (expected == null || actual == null || (expected.tableSize() != actual.tableSize())) {
			// no way to calculate kl distance if they differ in size
			return Float.POSITIVE_INFINITY;
		}
		
		// calculate kl distance := sum of expected log-divergence
		float sum = 0f;
		
		// at this point, p.size() == q.size()
		for (int i = 0; i < expected.tableSize(); i++) {
			if (expected.getValue(i) > 0) {	// this if is just to consider 0*ln(0) = 0
				sum += expected.getValue(i) * Math.log(expected.getValue(i)/actual.getValue(i));	// log-divergence is ln(p/q). Multiply by p to get its expectation.
			}
		}
		
		return sum;
	}



	/**
	 * Sample a bin from cumulative distribution, and then sample a value from bin (because bin is an interval of values).
	 * @param rand
	 * @param cumulative : indexes represent bin values
	 * @param day 
	 * @param timeBlock 
	 * @param detector 
	 * @param io 
	 * @return 
	 * @see IActivityHistIO#getBinValueLower(int, int, int)
	 * @see IActivityHistIO#getBinValueUpper(int, int, int)
	 */
	protected Integer getRawDetectorSample(Random rand, List<Float> cumulative, IActivityHistIO io, int detector, int timeBlock) {
		
		// sample a bin value from cumulative dist
		int bin = 0;	// use zero by default (default will happen, for example, when cumulative is zero for all bins)
		float randValue = rand.nextFloat();
		for (int i = 0; i < cumulative.size(); i++) {
			// pick the 1st bin that exceeds randValue
			if (cumulative.get(i) > randValue) {
				bin = i;
				break;
			}
		}
		
		// get the lower and upper values of the bin
		float lower = io.getBinValueLower(detector, timeBlock, bin);
		float upper = io.getBinValueUpper(detector, timeBlock, bin);
		
		// uniformly sample a value between the lower and upper value of bin
		return Math.round(lower + (rand.nextFloat() * (upper - lower)));
	}


	/**
	 * @return the io
	 */
	public IActivityHistIO getIO() {
		if (io == null) {
			io = new ActivityHistCSVIO();
		}
		return io;
	}

	/**
	 * @param io the io to set
	 */
	public void setIO(IActivityHistIO io) {
		this.io = io;
	}

	/**
	 * @return the activityHistFileName
	 */
	public String getActivityHistFileName() {
		return activityHistFileName;
	}


	/**
	 * @param activityHistFileName the activityHistFileName to set
	 */
	public void setActivityHistFileName(String activityHistFileName) {
		this.activityHistFileName = activityHistFileName;
	}


	/**
	 * @return the block1CutoffPercent
	 */
	public float getBlock1CutoffPercent() {
		return block1CutoffPercent;
	}


	/**
	 * @param block1CutoffPercent the block1CutoffPercent to set
	 */
	public void setBlock1CutoffPercent(float block1CutoffPercent) {
		if (block1CutoffPercent < 0 || block1CutoffPercent > 1) {
			throw new IllegalArgumentException("Cut-off percentile must be between 0 and 1: " + block1CutoffPercent);
		}
		this.block1CutoffPercent = block1CutoffPercent;
	}


	/**
	 * @return the seed
	 */
	public Long getSeed() {
		if (seed == null) {
			seed = System.currentTimeMillis();
		}
		return seed;
	}


	/**
	 * @param seed the seed to set
	 */
	public void setSeed(Long seed) {
		this.seed = seed;
	}


	/**
	 * @return the block1Tokens
	 */
	public List<Integer> getBlock1TokensGood() {
		if (block1TokensGood == null) {
			String string = "15000";
			for (int i = 1; i < getNumDetectors()/2; i++) {
				string += ",15000";
			};
			block1TokensGood = parseTokens(string );
		}
		return block1TokensGood;
	}


	/**
	 * @param block1Tokens the block1Tokens to set
	 */
	public void setBlock1TokensGood(List<Integer> block1Tokens) {
		this.block1TokensGood = block1Tokens;
	}




	/**
	 * @return the totalUsers
	 */
	public int getTotalUsers() {
		return totalUsers;
	}


	/**
	 * @param totalUsers the totalUsers to set
	 */
	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}
	
	/**
	 * @return the block2Tokens
	 */
	public List<Integer> getBlock2TokensGood() {
		if (block2TokensGood == null) {
			String string = "3000";
			for (int i = 1; i < getNumDetectors()/2; i++) {
				string += ",3000";
			};
			block2TokensGood = parseTokens(string );
		}
		return block2TokensGood;
	}


	/**
	 * @param block2Tokens the block2Tokens to set
	 */
	public void setBlock2TokensGood(List<Integer> block2Tokens) {
		this.block2TokensGood = block2Tokens;
	}


	/**
	 * @return the block1TokensBad
	 */
	public List<Integer> getBlock1TokensBad() {
		if (block1TokensBad == null) {
			String string = "4000";
			for (int i = 1; i < getNumDetectors()/2; i++) {
				string += ",4000";
			};
			block1TokensBad = parseTokens(string );
		}
		
		return block1TokensBad;
	}


	/**
	 * @param block1TokensBad the block1TokensBad to set
	 */
	public void setBlock1TokensBad(List<Integer> block1TokensBad) {
		this.block1TokensBad = block1TokensBad;
	}


	/**
	 * @return the block2TokensBad
	 */
	public List<Integer> getBlock2TokensBad() {
		if (block2TokensBad == null) {
			String string = "10000";
			for (int i = 1; i < getNumDetectors()/2; i++) {
				string += ",10000";
			};
			block2TokensBad = parseTokens(string );
		}
		return block2TokensBad;
	}


	/**
	 * @param block2TokensBad the block2TokensBad to set
	 */
	public void setBlock2TokensBad(List<Integer> block2TokensBad) {
		this.block2TokensBad = block2TokensBad;
	}


	/**
	 * 
	 * @param hasOption
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> parseTokens(String commaSeparatedInt) {
		if (commaSeparatedInt == null || commaSeparatedInt.trim().isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		
		// remove spaces
		commaSeparatedInt = commaSeparatedInt.replaceAll("\\s", "");
		
		List<Integer> ret = new ArrayList<Integer>();
		for (String token : commaSeparatedInt.split(",")) {
			ret.add(Integer.parseInt(token));
		}
		
		return ret;
	}

	/**
	 * @return the peerGroupSizeFile
	 */
	public String getPeerGroupSizeFile() {
		return peerGroupSizeFile;
	}


	/**
	 * @param peerGroupSizeFile the peerGroupSizeFile to set
	 */
	public void setPeerGroupSizeFile(String peerGroupSizeFile) {
		this.peerGroupSizeFile = peerGroupSizeFile;
	}


	/**
	 * @return the rawDataOutput
	 */
	public String getRawDataOutput() {
		return rawDataOutput;
	}


	/**
	 * @param rawDataOutput the rawDataOutput to set
	 */
	public void setRawDataOutput(String rawDataOutput) {
		this.rawDataOutput = rawDataOutput;
	}


	/**
	 * @return the badUserProbs : probability of a user to be bad given the peer group it belongs
	 */
	public Map<String, Float> getBadUserProbs() {
		if (badUserProbs == null) {
			badUserProbs = parseBadUserProb("0.1");
		}
		return badUserProbs;
	}


	/**
	 * @param badUserProbs the badUserProbs to set
	 */
	public void setBadUserProbs(Map<String, Float> badUserProbs) {
		this.badUserProbs = badUserProbs;
	}
	
	/**
	 * 
	 * @param optionValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Float> parseBadUserProb(String optionValue) {
		if (optionValue == null) {
			return Collections.EMPTY_MAP;
		}
		
		// remove white space
		optionValue = optionValue.replaceAll("\\s", "");
		
		Map<String, Float> ret = new HashMap<String, Float>();
		
		if (!optionValue.contains(":") && !optionValue.contains(",")) {
			// return a map which always returns the same value
			final Float prob = Float.parseFloat(optionValue);
			return new Map<String, Float>() {
				public int size() {return 1;}
				public boolean isEmpty() {return false;}
				public boolean containsKey(Object key) {return true;}
				public boolean containsValue(Object value) {return value.equals(prob);}
				public Float get(Object key) {return prob;}
				public Float put(String key, Float value) {throw new UnsupportedOperationException("Not supported");}
				public Float remove(Object key) {throw new UnsupportedOperationException("Not supported");}
				public void putAll(Map<? extends String, ? extends Float> m) {throw new UnsupportedOperationException("Not supported");}
				public void clear() {throw new UnsupportedOperationException("Not supported");}
				public Set<String> keySet() {return Collections.singleton("prob");}
				public Collection<Float> values() {return Collections.singletonList(prob);}
				public Set<java.util.Map.Entry<String, Float>> entrySet() {return Collections.singletonMap("prob", prob).entrySet();}
			};
		} else {
			// iterate on each comma-separated declaration
			for (String sizeProbPair : optionValue.split(",")) {
				if (sizeProbPair == null || sizeProbPair.isEmpty()) {
					continue;
				}
				String[] split = sizeProbPair.split(":");
				String size = null;
				if (split.length >= 1) {
					size = split[0];
				}
				Float prob = -1f;
				if (split.length >= 2) {
					prob = Float.parseFloat(split[1]);
				}
				if (size != null) {
					ret.put(size, prob);
				}
			}
		}
		
		
		return ret;
	}
	

	/**
	 * @return the totalDays
	 */
	public int getTotalDays() {
		return totalDays;
	}


	/**
	 * @param totalDays the totalDays to set
	 */
	public void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}


	/**
	 * @return the testDayThreshold : days after this day (inclusive) will be considered as test dates instead of
	 * training dates.
	 * @see #getTotalDays()
	 */
	public int getTestDayThreshold() {
		return testDayThreshold;
	}


	/**
	 * @param testDayThreshold : days after this day (inclusive) will be considered as test dates instead of
	 * training dates.
	 * @see #getTotalDays()
	 */
	public void setTestDayThreshold(int testDayThreshold) {
		this.testDayThreshold = testDayThreshold;
	}


	/**
	 * @return the numTimeBlocks
	 */
	public int getNumTimeBlocks() {
		return numTimeBlocks;
	}


	/**
	 * @param numTimeBlocks the numTimeBlocks to set
	 */
	public void setNumTimeBlocks(int numTimeBlocks) {
		this.numTimeBlocks = numTimeBlocks;
	}


	/**
	 * @return the numDetectors
	 */
	public int getNumDetectors() {
		return numDetectors;
	}


	/**
	 * @param numDetectors the numDetectors to set
	 */
	public void setNumDetectors(int numDetectors) {
		this.numDetectors = numDetectors;
	}

	/**
	 * @return the rDriverScript
	 */
	public String getRScriptName() {
		return rScriptName;
	}


	/**
	 * @param rDriverScript the rDriverScript to set
	 */
	public void setRScriptName(String rDriverScript) {
		this.rScriptName = rDriverScript;
	}


	/**
	 * @return the transformedDataOutput
	 */
	public String getTransformedDataOutput() {
		return transformedDataOutput;
	}


	/**
	 * @param transformedDataOutput the transformedDataOutput to set
	 */
	public void setTransformedDataOutput(String transformedDataOutput) {
		this.transformedDataOutput = transformedDataOutput;
	}


	/**
	 * @return the rscriptProgramName
	 */
	public String getRscriptProgramName() {
		return rscriptProgramName;
	}


	/**
	 * @param rscriptProgramName the rscriptProgramName to set
	 */
	public void setRscriptProgramName(String rscriptProgramName) {
		this.rscriptProgramName = rscriptProgramName;
	}


	/**
	 * @return the transformedDataIO
	 */
	public IJointDistributionReader getTransformedDataReader() {
		return transformedDataReader;
	}


	/**
	 * @param transformedDataIO the transformedDataIO to set
	 */
	public void setTransformedDataReader(IJointDistributionReader transformedDataIO) {
		this.transformedDataReader = transformedDataIO;
	}


	/**
	 * @return the correlationDataFileFolder
	 */
	public String getCorrelationDataFileFolder() {
		return correlationDataFileFolder;
	}


	/**
	 * @param correlationDataFileFolder the correlationDataFileFolder to set
	 */
	public void setCorrelationDataFileFolder(String correlationDataFileFolder) {
		this.correlationDataFileFolder = correlationDataFileFolder;
	}


	/**
	 * @return the wrapperIO
	 */
	public IModelCenterWrapperIO getWrapperIO() {
		if (wrapperIO == null) {
			wrapperIO = ModelCenterMatrixStyleWrapperIO.getInstance();
			wrapperIO.setProgramName("UserActivitySimulator");
		}
		return wrapperIO;
	}


	/**
	 * @param wrapperIO the wrapperIO to set
	 */
	public void setWrapperIO(IModelCenterWrapperIO wrapperIO) {
		this.wrapperIO = wrapperIO;
	}


	/**
	 * @return the distanceMetricFileName
	 */
	public String getDistanceMetricFileName() {
		return distanceMetricFileName;
	}


	/**
	 * @param distanceMetricFileName the distanceMetricFileName to set
	 */
	public void setDistanceMetricFileName(String distanceMetricFileName) {
		this.distanceMetricFileName = distanceMetricFileName;
	}


	/**
	 * @return the daysToRedrawBadUser : how many days to wait until randomly redrawing whether a user is bad or good.
	 */
	public int getDaysToRedrawUserAttitude() {
		return daysToRedrawUserAttitude;
	}


	/**
	 * @param daysToRedrawUserAttitude : how many days to wait until randomly redrawing whether a user is bad or good.
	 */
	public void setDaysToRedrawUserAttitude(int daysToRedrawUserAttitude) {
		this.daysToRedrawUserAttitude = daysToRedrawUserAttitude;
	}


	/**
	 * @return the isToAdd1ToCounts
	 */
	public boolean isToAdd1ToCounts() {
		return isToAdd1ToCounts;
	}


	/**
	 * @param isToAdd1ToCounts the isToAdd1ToCounts to set
	 */
	public void setToAdd1ToCounts(boolean isToAdd1ToCounts) {
		this.isToAdd1ToCounts = isToAdd1ToCounts;
	}


	/**
	 * @return the firstDay
	 */
	public int getFirstDay() {
		return firstDay;
	}


	/**
	 * @param firstDay the firstDay to set
	 */
	public void setFirstDay(int firstDay) {
		this.firstDay = firstDay;
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption("hist","activity_histogram_file", true, "CSV file with user activity histogram.");
		options.addOption("activity","raw_user_activity_output", true, "Name of CSV file with simulated user activity.");
		options.addOption("output","transformed_detectors_output", true, "Name of CSV file with simulated detectors alert days (i.e. transformed data).");
		options.addOption("correlation","correlation_data_file_folder", true, "Name of folder containing CSV files with detectors correlation data (of alert days).");
		options.addOption("rProg","rscript_program_name", true, "Name of program that will execute R script. It's usually \"RScript\"");
		options.addOption("rscript","rscript", true, "Name of R script file to be invoked in order to generate the output file from activity file.");
		options.addOption("metricFile","distance_metric_output_file", true, "Name of output file (which contains a metric of distance between the correlation data and simulated detectors alert days data) to be generated.");
		options.addOption("block","block_cutoff_percent", true, "This proportion of users in the histogram will be considered to be in block 1 (histogram of low activity users).");
		options.addOption("redrawAttitude","days_redraw_user_attitude", true, "How many days to wait until redrawing the bad/good attitude of user. Set to negative in order to make an user immutable.");
		options.addOption("seed","random_seed", true, "Seed to be used in random number generator.");
		options.addOption("noIncrement","no_increment_counts", false, "Do not add 1 to counts before calculating proportions/probabilities.");
		options.addOption("goodTokens1","block1_tokens_per_detector_good_user", true, "Comma separated integer list of how many tokens for block 1 a good user has for each detector. "
				+ "An example for 6 detectors would be: \"5,70,15,10,90,40\"");
		options.addOption("goodTokens2","block2_tokens_per_detector_good_user", true, "Comma separated integer list of how many tokens for block 2 a good user has for each detector. "
				+ "An example for 6 detectors would be: \"5,70,15,10,90,40\"");
		options.addOption("badTokens1","block1_tokens_per_detector_bad_user", true, "Comma separated integer list of how many tokens for block 1 a bad user has for each detector. "
				+ "An example for 6 detectors would be: \"5,70,15,10,90,40\"");
		options.addOption("badTokens2","block2_tokens_per_detector_bad_user", true, "Comma separated integer list of how many tokens for block 2 a bad user has for each detector. "
				+ "An example for 6 detectors would be: \"5,70,15,10,90,40\"");
		options.addOption("badUserProb","bad_user_prob_by_group_size", true, "Probability of a user to be a bad user, given each peer group size."
				+ " The format is <PeerGroupSizeName>:<prob>[,<PeerGroupSizeName>:<prob>]*."
				+ " For example, large:0.2,small:0.04,medium:0.0001");
		options.addOption("groupSizeFile","peer_group_size_file", true, "Path to csv file containing peer group sizes.");
		options.addOption("users","total_users", true, "Total number of users to simulate.");
		options.addOption("days","total_days", true, "Total number of days to simulate.");
		options.addOption("firstDay","first_day", true, "First day to simulate. This is actually which date in histogram file (see parameter hist) to consider to be the 1st date. Default is 0, but it's suggested to use last day in histogram - total days to simulate.");
		options.addOption("testDays","test_days_threshold", true, "Number of days for training.");
		options.addOption("numTimeblocks","number_timeblocks", true, "Total number of time blocks to simulate.");
		options.addOption("totalDetectors","total_number_detectors", true, "Total number of detectors.");
		options.addOption("d","debug", false, "Enables debug mode.");
		options.addOption("h","help", false, "Prints this help.");
		
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
		
		UserActivitySimulator simulator = new UserActivitySimulator();
		
		if (cmd.hasOption("hist")) {
			simulator.setActivityHistFileName(cmd.getOptionValue("hist"));
		}
		if (cmd.hasOption("activity")) {
			simulator.setRawDataOutput(cmd.getOptionValue("activity"));
		}
		if (cmd.hasOption("output")) {
			simulator.setTransformedDataOutput(cmd.getOptionValue("output"));
		}
		if (cmd.hasOption("correlation")) {
			simulator.setCorrelationDataFileFolder(cmd.getOptionValue("correlation"));
		}
		if (cmd.hasOption("rscript")) {
			simulator.setRScriptName(cmd.getOptionValue("rscript"));
		}
		if (cmd.hasOption("rProg")) {
			simulator.setRscriptProgramName(cmd.getOptionValue("rProg"));
		}
		if (cmd.hasOption("groupSizeFile")) {
			simulator.setPeerGroupSizeFile(cmd.getOptionValue("groupSizeFile"));
		}
		if (cmd.hasOption("metricFile")) {
			simulator.setDistanceMetricFileName(cmd.getOptionValue("metricFile"));
		}
		if (cmd.hasOption("block")) {
			simulator.setBlock1CutoffPercent(Float.parseFloat(cmd.getOptionValue("block")));
		}
		if (cmd.hasOption("redrawAttitude")) {
			simulator.setDaysToRedrawUserAttitude(Integer.parseInt(cmd.getOptionValue("redrawAttitude")));
		}
		if (cmd.hasOption("seed")) {
			simulator.setSeed(Long.parseLong(cmd.getOptionValue("seed")));
		}
		if (cmd.hasOption("users")) {
			simulator.setTotalUsers(Integer.parseInt(cmd.getOptionValue("users")));
		}
		if (cmd.hasOption("days")) {
			simulator.setTotalDays(Integer.parseInt(cmd.getOptionValue("days")));
		}
		if (cmd.hasOption("firstDay")) {
			simulator.setFirstDay(Integer.parseInt(cmd.getOptionValue("firstDay")));
		}
		if (cmd.hasOption("testDays")) {
			simulator.setTestDayThreshold(Integer.parseInt(cmd.getOptionValue("testDays")));
		}
		if (cmd.hasOption("numTimeblocks")) {
			simulator.setNumTimeBlocks(Integer.parseInt(cmd.getOptionValue("numTimeblocks")));
		}
		if (cmd.hasOption("totalDetectors")) {
			simulator.setNumDetectors(Integer.parseInt(cmd.getOptionValue("totalDetectors")));
		}
		if (cmd.hasOption("goodTokens1")) {
			simulator.setBlock1TokensGood(simulator.parseTokens(cmd.getOptionValue("goodTokens1")));
		}
		if (cmd.hasOption("goodTokens2")) {
			simulator.setBlock2TokensGood(simulator.parseTokens(cmd.getOptionValue("goodTokens2")));
		}
		if (cmd.hasOption("badTokens1")) {
			simulator.setBlock1TokensBad(simulator.parseTokens(cmd.getOptionValue("badTokens1")));
		}
		if (cmd.hasOption("badTokens2")) {
			simulator.setBlock2TokensBad(simulator.parseTokens(cmd.getOptionValue("badTokens2")));
		}
		if (cmd.hasOption("badUserProb")) {
			simulator.setBadUserProbs(simulator.parseBadUserProb(cmd.getOptionValue("badUserProb")));
		}
		
		simulator.setToAdd1ToCounts(!cmd.hasOption("noIncrement"));
		
		simulator.simulateAll();
		System.exit(0);
	}




}
