/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import unbbayes.prs.exception.InvalidParentException;
import au.com.bytecode.opencsv.CSVReader;
import edu.gmu.ace.scicast.MarkovEngineImpl;
import edu.gmu.ace.scicast.MarkovEngineImpl.InexistingQuestionException;
import edu.gmu.ace.scicast.io.DAGGREQuestionReader;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineCSVTest extends TestCase {

	public static final int CONFIG_SIZE = 6;

//	private static final File csvFile = new File("examples/DAGGRE.csv");
	
	/**How many times to iterate test on the same file, in order to avoid background process to impact on time*/
	private int maxIterations = 2;//5;
	
	/** If two probability values are within an interval of + or - this value, then it is considered to be equal�@*/
	private float probPrecisionError = 0.01f;

	private boolean isToTestProbValues = false;

	private boolean isToPropagateEvidence = true;
	
	
	private boolean isToCreateUserAssetNets = true;

	private boolean isToPrintProbabilityValues = true;
	
	/** Probability to read a line in CSV. set to 1 if you want to read all lines of CSV file */
	private double probabilityToPickCSVLine = 0.00055d;
	
	
	private File csvFile = new File("examples/DAGGRE.csv");;

//	private File[] netFilesToTest = new File[] {new File(csvFile.getParent(),"DAGGRE1.net"), new File(csvFile.getParent(),"DAGGRE5p5.net")  };
	private File[] netFilesToTest = new File[] { new File(csvFile.getParent(),"DAGGRE5p5.net"),new File(csvFile.getParent(),"DAGGRE1.net")  };
	
	/**
	 * config of values of isToTestProbValues, isToPropagateEvidence, isToUpdateAssets, isToCreateUserAssetNets, isToPrintProbabilityValues, isToObtainCliqueSizes respectively;
	 * must match the size {@link #CONFIG_SIZE}
	 */
	private boolean[][] configs = new boolean[][] {{false, true, true, true, false, false}, {false, true, true, true, false, false}};

	private MarkovEngineImpl engine =  (MarkovEngineImpl)MarkovEngineImpl.getInstance(2, 100, 1000, false);;
	
//	private boolean[][] configs = new boolean[][] {{false, false, false, false, false, false}, {false, true, false, false, false, true}};



	

//	private File netFile = new File(csvFile.getParent(),"DAGGRE1.net");
//
//	private static final File NETFILE_5BY5 = new File(csvFile.getParent(),"DAGGRE5p5.net");
	

	/**
	 * @param name
	 */
	public MarkovEngineCSVTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		engine.initialize();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public class MarkovEngineQuestionReader extends DAGGREQuestionReader {

		/* (non-Javadoc)
		 * @see edu.gmu.ace.scicast.io.DAGGREQuestionReader#load(java.io.File, unbbayes.prs.bn.ProbabilisticNetwork, unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm, java.util.Map)
		 */
		@Override
		public long load(File csv, ProbabilisticNetwork net, AssetAwareInferenceAlgorithm algorithm, Map<Integer, AssetNetwork> usersMap) throws IOException, InvalidParentException {

			// initial assertion
			if (csv == null || !csv.exists() || net == null || net.getNodeCount() == 0) {
				return 0;
			}
			if (usersMap == null) {
				// userMap is not going to be an output parameter, but let's at least reuse the map's name
				usersMap = new HashMap<Integer, AssetNetwork>();
			}

			long transactionKey = Long.MIN_VALUE;
			if (!isToPropagate()) {
				// 1 transaction for all trades
				transactionKey = engine.startNetworkActions();
			}
			
			// iterate on csv
			CSVReader reader = new CSVReader(new FileReader(csv));	// classes of open csv
			String [] nextLine;	// the line read
			long lineCounter = 0;	// how many lines were read
			for (lineCounter = 0; (nextLine = reader.readNext()) != null ; lineCounter++) {
				if (Math.random() > probabilityToPickCSVLine) {
					continue;
				}
				long timestamp = System.currentTimeMillis();
		        
				if (isToPropagate()) {
					// 1 transaction for each trade
					transactionKey = engine.startNetworkActions();
				}
				
				// nextLine[] is an array of values from the line
				// extract columns of interest
		    	Integer id = Integer.valueOf(nextLine[0]);		// question ID
		        Integer userID = Integer.valueOf(nextLine[2]);	// user id
		        float probTrue = Float.valueOf(nextLine[3]);	// edit (i.e. probability of the question to be true)
		        
		        // check consistency of probTrue
		        if (probTrue <= 0 || probTrue >= 1) {
		        	throw new IllegalStateException("User " + userID + " bet " + probTrue + " on question " + id);
		        }
		        
		        List<Float> newValues = new ArrayList<Float>();
		        newValues.add(probTrue); newValues.add(1-probTrue);
				try {
					engine.addTrade(transactionKey, new Date(), "User " + userID + " bet " + probTrue + " on question " + id, userID, id, newValues , null, null, false);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
		        
		        if (isToPropagate()) {
					// 1 transaction for each trade
					try {
						engine.commitNetworkActions(transactionKey);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
		        
		        System.out.println("Time to process line " + lineCounter + " of " + csv.getName() +": " + (System.currentTimeMillis() - timestamp) + "ms.");
		        System.out.println("User " + userID + ", question " + id +", prob = " + probTrue + ", Mem(MB) = " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576);
		    }
			
			if (!isToPropagate()) {
				// 1 transaction for all trades
				try {
					engine.commitNetworkActions(transactionKey);
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return lineCounter;
		}
		
	}
	
	/**
	 * Requirements (from Dr. Charles Twardy's e-mail ctwardy@c4i.gmu.edu)
	 * I have no idea how (if?) this file represents edits to multiple-choice questions.  Just assume everything is binary for a first test.
	 * The columns are: QuestionID, Timestamp, UserID, new Probability.  All probabilities started at uniform. 
	 * Output should be a set of probabilities for each question, the number of edits processed, and a runtime.  
	 * 
	 * The first test should use only disconnected nodes.  So 100+ nodes and no arcs.
	 * The second test should arbitrarily divide them into 20ish groups of five.
	 * 
	 * To control for background procesess, do 3-5 runs each and take the minimum time.
	 * 
	 * The file contains at least 196 nodes and 3148 users.
	 * 
	 */
	public void testCSVReading() {

		assertNotNull(csvFile);
		assertTrue(csvFile.exists());
		
		
		for (int currentFileIndex = 0; currentFileIndex < netFilesToTest.length; currentFileIndex++) {
			File netFile = netFilesToTest[currentFileIndex];
			if (configs.length > 0) {
				// change config
				isToTestProbValues = (configs[0].length > 0)?configs[currentFileIndex][0]:false;
				isToPropagateEvidence = (configs[0].length > 1)?configs[currentFileIndex][1]:false;
				isToCreateUserAssetNets = (configs[0].length > 3)?configs[currentFileIndex][3]:false;
				isToPrintProbabilityValues = (configs[0].length > 4)?configs[currentFileIndex][4]:false;
			}
			
			// assert existence of files
			assertNotNull(netFile);
			assertTrue(netFile.exists());
			
			// iterate maxIterations time, because background process can change execution time.
			for (int currentIteration = 0; currentIteration < maxIterations; currentIteration++) {
				
				long startTime = System.currentTimeMillis();	// for counting the time to load network file
				
				// instantiate this here, so that we use a clean reader at each maxIterations
				MarkovEngineQuestionReader daggreQuestionReader = new MarkovEngineQuestionReader();
				daggreQuestionReader.setToPropagate(isToPropagateEvidence);
				daggreQuestionReader.setToCreateUserAssetNet(isToCreateUserAssetNets);
				
				
				// load base network (disconnected network)
				ProbabilisticNetwork net = null;
				try {
					net = (ProbabilisticNetwork) new NetIO().load(netFile);
				} catch (Exception e) {
					e.printStackTrace();
					fail (e.getMessage());
				}
				System.out.println("Iteration " + currentIteration  + " of " + maxIterations
						+ ", loaded network = " 
						+ netFile.getName() 
						+ ", time to load network = " + (System.currentTimeMillis() - startTime) + " ms, size of network = "
						+ net.getNodeCount());
				assertNotNull(net);
				assertTrue(net.getNodeCount() > 0);
				
				
				startTime = System.currentTimeMillis();	// for counting the time to init network
				
				// reset engine
				engine.initialize();
				long transactionKey = engine.startNetworkActions();
				
				// generate nodes
				for (Node node : net.getNodes()) {
					float sum = (((TreeVariable)node).getMarginalAt(0) + ((TreeVariable)node).getMarginalAt(1));	// sum must be 1 (with some margin for imprecision)
					assertTrue(node.getName() + ", sum = " + sum, ((0 - probPrecisionError) < sum) && (sum < (0 + probPrecisionError)) );
					engine.addQuestion(transactionKey, new Date(), Long.parseLong(node.getName()), node.getStatesSize(), null);
					System.out.println("Added node " + Long.parseLong(node.getName()));
				}
				
				// generate edges
				for (Node node : net.getNodes()) {
					List<Long> parentQuestionIds = new ArrayList<Long>(node.getParents().size());
					for (Node parent : node.getParents()) {
						parentQuestionIds.add(Long.parseLong(parent.getName()));
					}
					if (parentQuestionIds.isEmpty()) {
						continue;
					}
					System.out.println("Adding edge " + parentQuestionIds + " -> " + Long.parseLong(node.getName()));
					try {
						engine.addQuestionAssumption(transactionKey, new Date(), Long.parseLong(node.getName()), parentQuestionIds , null);
					} catch (InexistingQuestionException e) {
						e.printStackTrace();
						throw e;
					}
				}
				
				engine.commitNetworkActions(transactionKey);
				
				
				System.out.println("Iteration " + currentIteration 
						+ ", time to create network = " + (System.currentTimeMillis() - startTime) + " ms.");
				
				if (isToTestProbValues) {
					for (Node node : net.getNodes()) {
						List<Float> probList = engine.getProbList(Long.valueOf(node.getName()), null, null);
						assertTrue("State 0 = " + probList.get(0), probList.get(0) >= 0 && probList.get(0) <= 1);
						assertTrue("State 1 = " + probList.get(1), probList.get(1) >= 0 && probList.get(1) <= 1);
						float sum = probList.get(0) + probList.get(1);	// sum must be 1 (with some margin for imprecision)
						assertTrue(node.getName() + ", sum = " + sum, ((1 - probPrecisionError) < sum) && (sum < (1 + probPrecisionError)) );
					}
				}
				
				startTime = System.currentTimeMillis();	// for counting the time to load csv and edit net
				
				long processedEdits = 0;	// quantity of processed edits
				// load csv and process edits
				try {
					processedEdits = daggreQuestionReader.load(
							csvFile, // file containing edits
							net, 	// edits will be performed on this net
							null,	// algorithm for processing edit
							null	// users
					);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				assertTrue(processedEdits > 0);
				
				System.out.println("Iteration " + currentIteration 
						+ ", loaded csv = " + csvFile.getName() 
						+ ", processed edits = " + processedEdits
						+ ", time to process = " + (System.currentTimeMillis() - startTime) + " ms, number of users = "
						+ engine.getUserToAssetAwareAlgorithmMap().keySet().size()
						+ ", used memory = " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes.");
				
				if(isToTestProbValues) {
					
					if (isToPrintProbabilityValues) {
						System.out.println("\n**** Probabilities, file = " + netFile.getName() + ", maxIterations "+ currentIteration +" ****");
					}
					
					// assuming that the name of the nodes are the question IDs (and ids are integers), convert the name of nodes (string) to integers
					List<Long> sortedQuestionIDList = new ArrayList<Long>();
					for (String key : net.getNodeIndexes().keySet()) {
						sortedQuestionIDList.add(Long.valueOf(key));
					}
					
					// print probability of each network, in order
					Collections.sort(sortedQuestionIDList);
					for (Long id : sortedQuestionIDList) {
						assertTrue(id.toString(),id >= 0);
						
						// extract node from question id, still assuming that name of nodes are ids
						TreeVariable node = (TreeVariable)net.getNode(id.toString());	
						List<Float> probList = engine.getProbList(id, null, null);
						
						// print prob
						if (isToPrintProbabilityValues) {
							System.out.println(node.getName() + " [false,true] = [" + probList.get(0) + " , "+ probList.get(1) + "]");
						}
						
						// prob value assertions, assuming that questions are boolean variables whose states are [false, true]
						if (isToTestProbValues){
							assertTrue(id + ", state 0 = " + probList.get(0), probList.get(0) >= 0);
							assertTrue(id + ", state 1 = " + probList.get(1), probList.get(1) >= 0);
							float sum = (probList.get(0) + probList.get(1));	// sum must be 1 (with some margin for imprecision)
							assertTrue(id +", prob = [" + probList.get(0)+ ", " + probList.get(1)  +  "], sum = " + sum, ((1 - probPrecisionError) < sum) && (sum < (1 + probPrecisionError)) );
						}
						
					}
					if (isToPrintProbabilityValues) {
						System.out.println("\n***********************\n\n");
					}
					
				}
				// TODO check if we should print the q-tables as well
			}
		}
		
	}
	
	

	/**
	 * Execute this method only for printing the results without calling JUnit's assertions.
	 * @param args:<br/> 
	 * {{isToTestProbValues, isToPropagateEvidence, isToUpdateAssets, isToCreateUserAssetNets, isToPrintProbabilityValues} , <br/>
	 * {isToTestProbValues, isToPropagateEvidence, isToUpdateAssets, isToCreateUserAssetNets, isToPrintProbabilityValues, isToObtainCliqueSizes}}
	 */
	public static void main(String[] args) {
		MarkovEngineCSVTest test = new MarkovEngineCSVTest("testCSVReading");
		
		if (args.length == (test.getNetFilesToTests().length * 5) ) {	// 10 by default
			boolean[][] configs = new boolean[2][CONFIG_SIZE];
			for (int i = 0; i < args.length; i++) {
				configs[i/CONFIG_SIZE][i%CONFIG_SIZE] = Boolean.parseBoolean(args[i]);
			}
			test.setConfigs(configs);
		}
		
		TestResult result = test.run();
		if (!result.wasSuccessful()) {
			System.err.println("Error on test: " + test);
			while (result.failures().hasMoreElements()) {
				TestFailure failure = (TestFailure) result.failures().nextElement();
				System.err.println(failure.toString());
				failure.thrownException().printStackTrace();
			}
			while (result.errors().hasMoreElements()) {
				TestFailure failure = (TestFailure) result.errors().nextElement();
				System.err.println(failure.toString());
				failure.thrownException().printStackTrace();
			}
		} else {
			System.out.println("Success.");
		}
	}

	/**
	 * @return the netFilesToTest
	 */
	public File[] getNetFilesToTests() {
		return netFilesToTest;
	}

	/**
	 * @param netFilesToTest the netFilesToTest to set
	 */
	public void setNetFilesToTests(File[] netFilesToTests) {
		this.netFilesToTest = netFilesToTests;
	}

	/**
	 * @return the configs
	 */
	public boolean[][] getConfigs() {
		return configs;
	}

	/**
	 * @param configs the configs to set
	 */
	public void setConfigs(boolean[][] configs) {
		this.configs = configs;
	}
	
}
