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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private Randoms random = null;
	private Long seed = null;
	
	private int numUsers = 2860;
	private int numOrganizations = 100;
	
	private String fileNamePrefix = "RCP11";
	private String fileNameSuffix = ".csv";
	private String vsLabel = "vs";
	private String targetTrueLabel = "_True";
	private String targetFalseLabel = "_False";
	private float initialTableCount = 1f;
	private String outputFolder = "output";
	private String inputFolder = "input";
	
	
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
		String discreteDetector1 = null;
		String discreteDetector2 = null;
		if (getDiscreteDetectorsLabels().size() < 2) {
			throw new IllegalArgumentException("There must be at least 2 discrete detectors.");
		} else {
			
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
		if (getOutputFolder() != null) {
			monthFolder = new File(getOutputFolder(),monthLabel);
			Files.createDirectories(monthFolder.toPath());
		}
		for (int organization = 0; organization < getNumOrganizations(); organization++) {
			int indexOfMonthInInput = getInputMonthLabels().indexOf(monthLabel);
			if (indexOfMonthInInput < 0) {
				// randomly pick input data month
				indexOfMonthInInput = getRandom().nextInt(getInputMonthLabels().size());
			}
			String dataMonth = getInputMonthLabels().get(indexOfMonthInInput);
			
			
			// read discrete detectors data (contingency tables)
			String fileName = getFileNamePrefix() + dataMonth + discreteDetector1 + getVsLabel() + discreteDetector2 + getTargetTrueLabel() + getFileNameSuffix(); // csv file of discrete variables has this format
			PotentialTable tableTrue = readTableFromCSV(fileName , discreteDetector1, discreteDetector2, getInitialTableCount());
			fileName = getFileNamePrefix() + dataMonth + discreteDetector1 + getVsLabel() + discreteDetector2 + getTargetFalseLabel() + getFileNameSuffix(); // csv file of discrete variables has this format
			PotentialTable tableFalse = readTableFromCSV(fileName, discreteDetector1, discreteDetector2, getInitialTableCount());
			
			// obtain target distribution from discrete detector data
			double totalTrue = tableTrue.getSum();
			double totalFalse = tableFalse.getSum();
		
			
			// it's 1 file per organization
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
			
			
			for (int user = 0; user < getNumUsers(); user++) {
				printer.print(""+user);
				
				// sample target. If rand.nextDouble <= probability of true, then sample true. Sample false otherwise
				boolean target = (getRandom().nextDouble() <= (totalTrue / (totalTrue + totalFalse)));
				String targetLabel = target?getTargetTrueLabel():getTargetFalseLabel();	// prepare in advance the label of target state, to be used to access respective files
				
				printer.print("," + (target?1:0));
				
				// sample discrete detectors given target
				PotentialTable tableToSample = target?tableTrue:tableFalse;	// sample from tableTrue if target == true. Or else, sample from tableFalse
				
				// obtain what is the virtual count we should use when generating sample from current month
				float virtualCount = getMonthlyVirtualCounts().get(getOutputMonthLabels().indexOf(monthLabel));
				
				// get a sample of all variables in the table
				int[] sample = tableToSample.getMultidimensionalCoord(getSampleIndex(tableToSample, virtualCount));
				Map<String, Integer> discreteDetectorNameToValueMap = new HashMap<String, Integer>();	// keep track of sampled value
				// extract values of each variable
				for (String detectorName : getDiscreteDetectorsLabels()) {
					// get variable from name
					Node detectorVar = (Node) getNameToVariableCache().get(detectorName);
					// store and print the value of current variable
					String value = detectorVar.getStateAt(sample[tableToSample.getVariableIndex(detectorVar)]);
					discreteDetectorNameToValueMap.put(detectorName, Integer.parseInt(value));
					printer.print("," + value);
				}
				
				
				// sample continuous detectors;
				for (String continuousDetectorName : getContinuousDetectorsLabels()) {
					// randomly pick a discrete detector to be used as condition for other continuous detectors;
					String conditionVariableName = discreteDetector1;
					if (getRandom().nextBoolean()) {
						conditionVariableName = discreteDetector2;
					}
					
					fileName = getFileNamePrefix() + dataMonth + conditionVariableName + getVsLabel() + continuousDetectorName + targetLabel  + getFileNameSuffix(); // csv file has this format
					double sampleValue = getSampleContinuous(fileName, virtualCount, discreteDetectorNameToValueMap.get(conditionVariableName), getInitialTableCount());
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
	 * Generate a sample from dirichlet-multinomial distribution
	 * @param table
	 * @param virtualCount
	 * @param normalize : if true, table will be normalized
	 * @return
	 */
	public int getSampleIndex(PotentialTable table, float virtualCount) {
		table = table.getTemporaryClone();	// use a clone, so that we don't modify original table
		if (virtualCount <= 0) {
			// do not normalize table, and use the counts in table directly
			virtualCount = 1;
		} else {
			// normalize table and use virtual counts
			table.normalize();
		}
		double[] alpha = new double[table.tableSize()];
		for (int i = 0; i < table.tableSize(); i++) {
			alpha[i] = table.getValue(i) * virtualCount;
		}
		Dirichlet dirichlet = new Dirichlet(alpha);
		
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
		List<Double> counts = map.get("counts");
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
		int bin = getRandom().nextDiscrete(new Dirichlet(alpha).nextDistribution());
		
		// use uniform distribution to sample a single value inside the sampled bin
		return getRandom().nextUniform(map.get("lower").get(bin), map.get("upper").get(bin));
	}

	/**
	 * 
	 * @param fileName
	 * @param conditionState 
	 * @param initialCounts  : this value will be added to all entries initially (use 0 to void this parameter).
	 * @return mapping from attributes to values.
	 * The i-th element in the list is the value of i-th bin.
	 * The labels (keys) will be "lower", "upper", and "counts".
	 * @throws IOException 
	 * @see {@link #getOutputFolder()}
	 */
	public Map<String,List<Double>> readContinuousDetectorCSVFile(String fileName, Integer conditionState, float initialCounts) throws IOException {
		
		// read csv file
		CSVReader reader = new CSVReader(new FileReader(new File(getInputFolder(),fileName)));
		
		// read rows until we find the block we want (which matches with condition state)
		String[] row = null;
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			if (row[0].toLowerCase().contains("lower") && row.length >= 3) {
				// check if condition matched
				String[] split = row[2].trim().split("=");
				if (conditionState.intValue() == Integer.parseInt(split[1])) {
					break;	// found a matching condition
				}
			}
		}
		if (row == null) {
			reader.close();
			throw new IllegalArgumentException("Detector value " + conditionState + " not found in file " + fileName);
		}
		
		// The labels (keys) will be "lower", "upper", and "counts".
		Map<String,List<Double>> ret = new HashMap<String, List<Double>>();
		ret.put("lower", new ArrayList<Double>());
		ret.put("upper", new ArrayList<Double>());
		ret.put("counts", new ArrayList<Double>());
		
		// at this point, cursor is at the header. Start reading the block
		for (row = reader.readNext(); row != null; row = reader.readNext()) {
			if (row.length <= 0) {
				continue;	// ignore empty rows
			}
			if (row[0].toLowerCase().contains("lower")) {
				break;	// this is the next block, so stop here
			}
			ret.get("lower").add(Double.parseDouble(row[0]));
			ret.get("upper").add(Double.parseDouble(row[1]));
			ret.get("counts").add(Double.parseDouble(row[2]) + initialCounts);
		}
		
		reader.close();
		
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

		
		// prepare table to return
		PotentialTable ret = new ProbabilisticTable();
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
		CSVReader reader = new CSVReader(new FileReader(new File(getInputFolder(),fileName)));
		
		// ignore 1st row (header Det2)
		reader.readNext();
		
		// read from 2nd row
		for (int tableIndex = 0; tableIndex < ret.tableSize();) {
			String[] row = reader.readNext();
			if (row == null) {
				Debug.println(getClass(), "Premature EOF found at index " + tableIndex + " of file " + fileName);
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
	 * @return the targetTrueLabel
	 */
	public String getTargetTrueLabel() {
		return this.targetTrueLabel;
	}

	/**
	 * @param targetTrueLabel the targetTrueLabel to set
	 */
	public void setTargetTrueLabel(String targetTrueLabel) {
		this.targetTrueLabel = targetTrueLabel;
	}

	/**
	 * @return the targetFalseLabel
	 */
	public String getTargetFalseLabel() {
		return this.targetFalseLabel;
	}

	/**
	 * @param targetFalseLabel the targetFalseLabel to set
	 */
	public void setTargetFalseLabel(String targetFalseLabel) {
		this.targetFalseLabel = targetFalseLabel;
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
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ContingencyMatrixUserActivitySimulator sim = new ContingencyMatrixUserActivitySimulator();
		// TODO read command line args and fill attributes from it
		sim.runAll();
	}

}
