/**
 * 
 */
package edu.gmu.ace.daggre.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestResult;
import unbbayes.io.NetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;
import au.com.bytecode.opencsv.CSVReader;

/**
 * @author Shou Matsumoto
 *
 */
public class DAGGREQuestionsReaderDriver extends TestCase {

//	private static final File csvFile = new File("examples/DAGGRE.csv");
	
	/**How many times to iterate test on the same file, in order to avoid background process to impact on time*/
	private int maxIterations = 5;//5;
	
	/** If two probability values are within an interval of + or - this value, then it is considered to be equalÅ@*/
	private float probPrecisionError = 0.0005f;

	private boolean isToTestProbValues = true;

	private boolean isToPropagateEvidence = false;
	
	private boolean isToUpdateAssets = false;
	
	private boolean isToCreateUserAssetNets = false;

	private boolean isToPrintProbabilityValues = false;
	

//	private File netFile = new File(csvFile.getParent(),"DAGGRE1.net");
//
//	private static final File NETFILE_5BY5 = new File(csvFile.getParent(),"DAGGRE5p5.net");
	

	/**
	 * @param name
	 */
	public DAGGREQuestionsReaderDriver(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
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

		File csvFile = new File("examples/DAGGRE.csv");
		assertNotNull(csvFile);
		assertTrue(csvFile.exists());
		
		// do the test for these files
//		File[] netFilesToTest = {new File(csvFile.getParent(),"DAGGRE1.net"), new File(csvFile.getParent(),"DAGGRE5p5.net") };
		File[] netFilesToTest = {new File(csvFile.getParent(),"DAGGRE5p5.net"), new File(csvFile.getParent(),"DAGGRE1.net")  };
		
		// config of values of isToTestProbValues, isToPropagateEvidence, isToUpdateAssets, isToCreateUserAssetNets, isToPrintProbabilityValues respectively;
		boolean[][]	config = {{true, true, true, true, false}, {true, false, false, false, false}};
		
		
		for (int currentFileIndex = 0; currentFileIndex < netFilesToTest.length; currentFileIndex++) {
			File netFile = netFilesToTest[currentFileIndex];
			// change config
			isToTestProbValues = config[currentFileIndex][0];
			isToPropagateEvidence = config[currentFileIndex][1];
			isToUpdateAssets = config[currentFileIndex][2];
			isToCreateUserAssetNets = config[currentFileIndex][3];
			isToPrintProbabilityValues = config[currentFileIndex][4];
			
			// assert existence of files
			assertNotNull(netFile);
			assertTrue(netFile.exists());
			
			// iterate maxIterations time, because background process can change execution time.
			for (int currentIteration = 0; currentIteration < maxIterations; currentIteration++) {
				
				// instantiate this here, so that we use a clean reader at each maxIterations
				DAGGREQuestionReader daggreQuestionReader = new DAGGREQuestionReader();
				daggreQuestionReader.setToPropagate(isToPropagateEvidence);
				daggreQuestionReader.setToCreateUserAssetNet(isToCreateUserAssetNets);
				
				System.gc();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				long startTime = System.currentTimeMillis();	// for counting the time to load network file
				
				// load base network (disconnected network)
				ProbabilisticNetwork net = null;
				try {
					net = (ProbabilisticNetwork) new NetIO().load(netFile);
				} catch (Exception e) {
					e.printStackTrace();
					fail (e.getMessage());
				}
				System.out.println("Iteration " + currentIteration 
						+ ", loaded network = " 
						+ netFile.getName() 
						+ ", time to load network = " + (System.currentTimeMillis() - startTime) + " ms, size of network = "
						+ net.getNodeCount());
				assertNotNull(net);
				assertTrue(net.getNodeCount() > 0);
				
				
				// algorithm to propagate probabilities
				JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(net);
				assertNotNull(junctionTreeAlgorithm);
				
				// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
//			junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
				
				// main algorithm
				AssetAwareInferenceAlgorithm algorithm = (AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm);
				assertNotNull(algorithm);
				algorithm.setToPropagateForGlobalConsistency(false);	// do not calculate LPE
				algorithm.setToUpdateAssets(isToUpdateAssets);
				
				for (Node node : algorithm.getProbabilityPropagationDelegator().getNetwork().getNodes()) {
					float sum = (((TreeVariable)node).getMarginalAt(0) + ((TreeVariable)node).getMarginalAt(1));	// sum must be 1 (with some margin for imprecision)
					assertTrue(node.getName() + ", sum = " + sum, ((0 - probPrecisionError) < sum) && (sum < (0 + probPrecisionError)) );
				}
				
				startTime = System.currentTimeMillis();	// for counting the time to compile network
				
				
				// compile network
				if (isToPropagateEvidence) {
					algorithm.run();
				} else {
					// force nodes to start with a marginal list.
					for (Node n : net.getNodes()) {
						TreeVariable tv = (TreeVariable) n;
						tv.initMarginalList();
						tv.setMarginalAt(0, .5f);
						tv.setMarginalAt(1, .5f);
					}
				}
				
				System.out.println("Iteration " + currentIteration 
						+ ", time to compile network = " + (System.currentTimeMillis() - startTime) + " ms.");
				if (isToTestProbValues) {
					for (Node node : algorithm.getProbabilityPropagationDelegator().getNetwork().getNodes()) {
						float sum = (((TreeVariable)node).getMarginalAt(0) + ((TreeVariable)node).getMarginalAt(1));	// sum must be 1 (with some margin for imprecision)
						assertTrue(node.getName() + ", sum = " + sum, ((1 - probPrecisionError) < sum) && (sum < (1 + probPrecisionError)) );
					}
				}
				
				startTime = System.currentTimeMillis();	// for counting the time to load csv and edit net
				
				long processedEdits = 0;	// quantity of processed edits
				HashMap<Integer, AssetNetwork> userMap = new HashMap<Integer, AssetNetwork>();	// users making edits (starts from 0)
				// load csv and process edits
				try {
					processedEdits = daggreQuestionReader.load(
							csvFile, // file containing edits
							net, 	// edits will be performed on this net
							algorithm,	// algorithm for processing edit
							userMap	// users
					);
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				assertTrue(processedEdits > 0);
				if (isToCreateUserAssetNets) {
					assertFalse(userMap.isEmpty());
				}
				
				System.out.println("Iteration " + currentIteration 
						+ ", loaded csv = " + csvFile.getName() 
						+ ", processed edits = " + processedEdits
						+ ", time to process = " + (System.currentTimeMillis() - startTime) + " ms, number of users = "
						+ userMap.size()
						+ ", used memory = " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes.");
				
				if(isToTestProbValues) {
					// collect last edits
					CSVReader reader = null;
					try {
						reader = new CSVReader(new FileReader(csvFile)); // classes of open csv
					} catch (FileNotFoundException e) {
						fail(e.getMessage());
						e.printStackTrace();
					}	
					String [] nextLine;	// the line read
					Map<Integer, Float> lastEditMap = new HashMap<Integer, Float>();
					try {
						while ((nextLine = reader.readNext()) != null) {
							// csv is assumed to be ordered by time, so the last edit comes last
							lastEditMap.put(Integer.valueOf(
									nextLine[0]), 				//1st element is the question ID
									Float.valueOf(nextLine[3])	//3rd element is the prob value
								);
						}
					} catch (IOException e) {
						e.printStackTrace();
						fail(e.getMessage());
					}
					
					if (isToPrintProbabilityValues) {
						System.out.println("\n**** Probabilities, file = " + netFile.getName() + ", maxIterations "+ currentIteration +" ****");
					}
					
					// assuming that the name of the nodes are the question IDs (and ids are integers), convert the name of nodes (string) to integers
					List<Integer> sortedQuestionIDList = new ArrayList<Integer>();
					for (String key : net.getNodeIndexes().keySet()) {
						sortedQuestionIDList.add(Integer.valueOf(key));
					}
					
					// print probability of each network, in order
					Collections.sort(sortedQuestionIDList);
					for (Integer id : sortedQuestionIDList) {
						assertTrue(id.toString(),id >= 0);
						
						// extract node from question id, still assuming that name of nodes are ids
						TreeVariable node = (TreeVariable)net.getNode(id.toString());	
						
						// print prob
						if (isToPrintProbabilityValues) {
							System.out.println(node.getName() + " [false,true] = [" + node.getMarginalAt(0) + " , "+ node.getMarginalAt(1) + "]");
						}
						
						// prob value assertions, assuming that questions are boolean variables whose states are [false, true]
						if (isToTestProbValues){
							assertEquals("false", node.getStateAt(0));
							assertEquals("true", node.getStateAt(1));
							assertTrue(id + ", false = " + node.getMarginalAt(0), node.getMarginalAt(0) >= 0);
							assertTrue(id + ", true = " + node.getMarginalAt(1), node.getMarginalAt(1) >= 0);
							assertTrue(id + ", false = " + node.getMarginalAt(0), (((1-lastEditMap.get(id)) - probPrecisionError) < node.getMarginalAt(0)) && (node.getMarginalAt(0) < ((1-lastEditMap.get(id)) + probPrecisionError)));
							assertTrue(id + ", true = " + node.getMarginalAt(1), ((lastEditMap.get(id) - probPrecisionError) < node.getMarginalAt(1)) && (node.getMarginalAt(1) < (lastEditMap.get(id) + probPrecisionError)));
							float sum = (node.getMarginalAt(0) + node.getMarginalAt(1));	// sum must be 1 (with some margin for imprecision)
							assertTrue(id + ", sum = " + sum, ((1 - probPrecisionError) < sum) && (sum < (1 + probPrecisionError)) );
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
	 * @param args
	 */
	public static void main(String[] args) {
		DAGGREQuestionsReaderDriver test = new DAGGREQuestionsReaderDriver("testCSVReading");
		TestResult result = test.run();
		if (!result.wasSuccessful()) {
			System.err.println("Error on test: " + test);
//			while (result.failures().hasMoreElements()) {
//				TestFailure failure = (TestFailure) result.failures().nextElement();
//				System.err.println(failure.exceptionMessage());
//			}
		} else {
			System.out.println("Success.");
		}
	}
	
}
