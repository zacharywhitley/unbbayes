/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm;

/**
 * @author Shou Matsumoto
 *
 */
public class DAGGREQuestionsReaderTest extends TestCase {

//	private static final File csvFile = new File("examples/DAGGRE.csv");
	
	/**How many times to iterate test on the same file, in order to avoid background process to impact on time*/
	private static int ITERATION = 5;
	
	/** If two probability values are within an interval of + or - this value, then it is considered to be equalÅ@*/
	private static final float PROB_PRECISION_ERROR = 0.0005f;
	

//	private File netFile = new File(csvFile.getParent(),"DAGGRE1.net");
//
//	private static final File NETFILE_5BY5 = new File(csvFile.getParent(),"DAGGRE5p5.net");
	

	/**
	 * @param name
	 */
	public DAGGREQuestionsReaderTest(String name) {
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
		File[] netFilesToTest = {new File(csvFile.getParent(),"DAGGRE1.net"), new File(csvFile.getParent(),"DAGGRE5p5.net") };
		for (File netFile : netFilesToTest) {
			
			// assert existence of files
			assertNotNull(netFile);
			assertTrue(netFile.exists());
			
			// iterate ITERATION time, because background process can change execution time.
			for (int i = 0; i < ITERATION; i++) {
				
				// instantiate this here, so that we use a clean reader at each iteration
				DAGGREQuestionReader daggreQuestionReader = new DAGGREQuestionReader();
				daggreQuestionReader.setToPropagate(false);
				
				long startTime = System.currentTimeMillis();	// for counting the time to load network file
				
				// load base network (disconnected network)
				ProbabilisticNetwork net = null;
				try {
					net = (ProbabilisticNetwork) new NetIO().load(netFile);
				} catch (Exception e) {
					e.printStackTrace();
					fail (e.getMessage());
				}
				System.out.println("Iteration " + i 
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
				
				startTime = System.currentTimeMillis();	// for counting the time to compile network
				
				// compile network
				algorithm.run();
				
				System.out.println("Iteration " + i 
						+ ", time to compile network = " + (System.currentTimeMillis() - startTime) + " ms.");
				
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
				assertFalse(userMap.isEmpty());
				
				System.out.println("Iteration " + i 
						+ ", loaded csv = " + csvFile.getName() 
						+ ", processed edits = " + processedEdits
						+ ", time to process = " + (System.currentTimeMillis() - startTime) + " ms, number of users = "
						+ userMap.size());
				
				
				System.out.println("\n**** Probabilities, file = " + netFile.getName() + ", iteration "+ i +" ****");
				
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
					
					// prob value assertions, assuming that questions are boolean variables whose states are [false, true]
					assertEquals("false", node.getStateAt(0));
					assertEquals("true", node.getStateAt(1));
					assertTrue(id + ", false = " + node.getMarginalAt(0), node.getMarginalAt(0) >= 0);
					assertTrue(id + ", true = " + node.getMarginalAt(1), node.getMarginalAt(1) >= 0);
					float sum = (node.getMarginalAt(0) + node.getMarginalAt(1));	// sum must be 1 (with some margin for imprecision)
					assertTrue(id + ", sum = " + sum, ((1 - PROB_PRECISION_ERROR) < sum) && (sum < (1 + PROB_PRECISION_ERROR)) );
					
					// print prob
					System.out.println(node.getName() + " [false,true] = [" + node.getMarginalAt(0) + " , "+ node.getMarginalAt(1) + "]");
				}
				System.out.println("\n***********************\n\n");
				
				// TODO check if we should print the q-tables as well
			}
		}
		
	}
	
	

//	/**
//	 * Execute this method only for printing the results without calling JUnit's assertions.
//	 * @param args
//	 */
//	public static void main(String[] args) {
//	}
	
}
