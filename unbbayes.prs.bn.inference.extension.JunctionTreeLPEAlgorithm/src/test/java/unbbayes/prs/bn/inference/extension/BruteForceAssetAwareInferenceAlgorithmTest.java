/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import unbbayes.io.NetIO;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;

/**
 * This is a JUnit test case for the asset propagation algorithms
 * @author Shou Matsumoto
 *
 */
public class BruteForceAssetAwareInferenceAlgorithmTest extends TestCase {
	
	/** If two probability values are within an interval of + or - this value, then it is considered to be equal�@*/
	private static final float PROB_PRECISION_ERROR = 0.0005f;

	/** If two asset q values are within an interval of + or - this value, then it is considered to be equal�@*/
	private static final float ASSET_PRECISION_ERROR = 0.05f;

	private BruteForceAssetAwareInferenceAlgorithm assetQAlgorithm;

	private ProbabilisticNetwork network;

	private JunctionTreeAlgorithm junctionTreeAlgorithm;

	/**
	 * @param name
	 */
	public BruteForceAssetAwareInferenceAlgorithmTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		// load DEF network
		File file = null;
		try {
			file = new File(this.getClass().getClassLoader().getResource("DEF_flat.net").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(file);
		
		NetIO netio = new NetIO();
		try {
			network = (ProbabilisticNetwork) netio.load(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(network);
		
		// algorithm to propagate probabilities
		junctionTreeAlgorithm = new JunctionTreeAlgorithm(network);
		assertNotNull(junctionTreeAlgorithm);
		
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		
		// algorithm to propagate q values (assets) given that probabilities were propagated
		assetQAlgorithm = (BruteForceAssetAwareInferenceAlgorithm) BruteForceAssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm);
		assertNotNull(assetQAlgorithm);
		assetQAlgorithm.setToUseQValues(true);
		
//		assetQAlgorithm.setToPropagateForGlobalConsistency(false);
//		assetQAlgorithm.setToUpdateSeparators(false);
		
		// set the default asset q quantity of all clique cells to 100
		if (assetQAlgorithm.isToUseQValues()) {
			assetQAlgorithm.setDefaultInitialAssetTableValue(100);
		} else {
			// algorithm will use asset space instead of q. Initialize with a value of assets equivalent of using q = 100
			assetQAlgorithm.setDefaultInitialAssetTableValue(assetQAlgorithm.getqToAssetConverter().getScoreFromQValues(100));
		}
		// force it to use min-propagation automatically even when we dont call doMinPropagation
		assetQAlgorithm.setToPropagateForGlobalConsistency(true);	
		
		try {
			// compile network
			assetQAlgorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for the methods: 
	 * {@link unbbayes.prs.bn.JunctionTreeAlgorithm#reset()};
	 * {@link unbbayes.prs.bn.JunctionTreeAlgorithm#run()};
	 * {@link unbbayes.prs.bn.inference.extension.AssetAwareInferenceAlgorithm#propagate()}.
	 * The tested data is equivalent to the one in 
	 * https://docs.google.com/document/d/179XTjD5Edj8xDBvfrAP7aDtvgDJlXbSDQQR-TlM5stw/edit.
	 * <br/>
	 * The DEF net looks like the following.		<br/>
	 * Note: uniform distribution for all nodes.	<br/>
	 * D--->F										<br/>
	 * | 											<br/>
	 * V  											<br/>
	 * E											<br/>
	 * 
	 * The sequence is:<br/>
	 * <br/>
	 * There are two cliques {D, E}, and {D, F}, initial asset tables have q-value as 100 in every cell. <br/>
	 *	Current marginal probabilities are:	<br/>
	 *	Variables    D             E              F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.5 0.5]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Trade-1: Tom would like to make a bet on E=e1, that has current probability as 0.5. First of all, we need to calculate Tom�fs edit limit (in this case, there is no assumption):	<br/> 
	 *	Given E=e1, min-q1 = 100	<br/>
	 *	Given E~=e1, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995].	<br/> 
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55 (current)	<br/>
	 *	<br/>	
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.55 0.45]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Tom�fs min-q is 90, at the following 4 min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2     1	<br/>
	 *	     1     2     2	<br/>
	 *	     2     2     1	<br/>
	 *	     2     2     2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-2: Now Tom would like to make another conditional bet on E=e1 given D=d1 (current P(E=e1|D=d1) = 0.55). Again, let us calculate his edit limits first (in this case, we have assumed variable D=d1). And note that Tom�fs asset tables are not the initial ones any more, but updated from last trade he did, now:	<br/> 
	 *	Given E=e1, and D=d1, min-q1 = 110	<br/>
	 *	Given E~=e1, and D=d1, min-q2 = 90	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 (current)	<br/>
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.725 0.275]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Tom�fs min-q is 20, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2    1	<br/>
	 *	     1     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-3: Joe came and intended to make a bet on E=e1 given D=d2 (current P(E=e1|D=d2) is 0.55). This will be his first edit, so he has initial asset tables before his trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given E=e1, and D=d2, min-q1 = 100	<br/>
	 *	Given E~=e1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0055, 0.9955]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 (current)	<br/> 
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Joe�fs min-q is 72.72727272727..., at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	     2     1    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-4: Now Amy is interested in changing P(F=f1|D=d1), which is currently 0.5. It will be her first edit, so she also has initial asset tables before the trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d1, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d1, min-q2 = 100	<br/>
 	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.4 0.6]	<br/>
	 *	<br/>	
	 *	Amy�fs min-q is 60, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     1    1	<br/>
	 *	     1     2    1	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-5: Joe would like to trade again on P(F=f1|D=d2), which is currently 0.5.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 72.727272727	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 72.727272727	<br/>
	 *	From Equation (1), edit interval is [0.006875, 0.993125]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.2 0.8]	<br/>
	 *	<br/>	
	 *	Joe�fs min-q is 14.54545454546, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	<br/>	
	 *	At this point, the model DEF reaches the status that has the same CPTs as the starting CPTs for the experimental model DEF we used in our AAAI 2012 paper.	<br/> 
	 *	<br/>	
	 *	From now on, we run test cases described in the paper.	<br/> 
	 *	<br/>	
	 *	Trade-6: Eric would like to trade on P(E=e1), which is currently 0.65.	<br/> 
	 *	To decide long or short, S(E=e1) = 10, S(E~=e1)=10, no difference because this will be Eric�fs first trade.	<br/>
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0065, 0.9965]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5824, 0.4176]   [0.8, 0.2]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	Eric�fs expected score is S=10.1177.	<br/>
	 *	Eric�fs min-q is 57.142857, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    1	<br/>
	 *	     2     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-7: Eric would like to make another edit. This time, he is interested on changing P(D=d1|F=f2), which is currently 0.52.	<br/> 
	 *	To decide long or short, S(D=d1, F=f2) = 10.36915, S(D~=d1, F=f2)=9.7669.	<br/>
	 *	Edit limit:	<br/>
	 *	Given D=d1, and F=f2 , min-q1 = 57.142857	<br/>
	 *	Given D~=d1, and F=f2, min-q2 = 57.142857	<br/>
	 *	From Equation (1), edit interval is [0.0091059, 0.9916058]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8	<br/> 
	 *	Eric2:	P(D=d1|F=f2) = 0.52 to 0.7 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D                            E                             F	<br/> 
	 *	Marginals   [0.7232, 0.2768]   [0.8509, 0.1491]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	Eric�fs expected score is now 10.31615.	<br/> 
	 *	Eric�fs min-q is 35.7393, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    2	<br/>
	 *	<br/> 
	 */	
	public final void testDEFNet() {
		
		
		
		// create new user Tom and his asset net. A new asset net represents a new user.
		AssetNetwork assetNetTom = null;
		try {
			assetNetTom = assetQAlgorithm.createAssetNetFromProbabilisticNet(network);
			assetNetTom.setName("Tom");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(assetNetTom);
		
		// set Tom as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetTom);
		assertEquals(assetNetTom, assetQAlgorithm.getAssetNetwork());
		
		
		
		// A soft evidence is represented as a list of conditions (assumed nodes) and an array of float representing new conditional probability.
		// Non-edited probabilities must remain in the values before the edit. Thus, we must extract what are the current probability values.
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();	
		assertNotNull(conditionalProbabilityExtractor);
		
		/*
		 * The idea of conditional soft evidence in UnBBayes is to start from a CPT (generated on-the-fly depending on what nodes are assumed)
		 * containing current probabilities, and only change the cells we want to.
		 * E.g. If we want to set P(D=d1 | E=e1) = .9, then initially we have:
		 *     _______________
		 * 	   |  e1  |  e2  |
		 *    ---------------
		 * d1  | .5   |  .5  |
		 * d2  | .5   |  .5  |
		 *     ---------------
		 *     
		 * And we want to change it to:
		 *     _______________
		 * 	   |  e1  |  e2  |
		 *    ---------------
		 * d1  | .9   |  .5  |
		 * d2  | .1   |  .5  |
		 *     ---------------
		 *     
		 * This is how soft evidence is inserted into UnBBayes.
		 */
		
		// Let's bet P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// extract E
		TreeVariable betNode = (TreeVariable) network.getNode("E");
		assertNotNull(betNode);
		
		// conditions (nodes) assumed in the bet (currently, none)
		List<INode> betConditions =  new ArrayList<INode>();
		
		// first, extract what is P(E) prior to edit (this is the "CPT" generated on-the-fly with no nodes assumed)
		PotentialTable potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(2, potential.tableSize());
		
		// check whether probability prior to edit is really 0.5
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.5f + PROB_PRECISION_ERROR)) );
		
		// edit interval of P(E=e1) should be [0.005, 0.995]
		float editInterval[] = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 0);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.005f - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.005f + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.995f - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.995f + PROB_PRECISION_ERROR)) );
		
		// set P(E=e1) = 0.55 and P(E=e2) = 0.45 (we are changing only the cells we want)
		potential.setValue(0, 0.55f);
		potential.setValue(1, 0.45f);
		
		// we are doing soft evidence, but the JeffreyRuleLikelihoodExtractor can convert soft evidence to likelihood evidence
		// thus, we are adding the values of soft evidence in the likelihood evidence's vector 
		// (JeffreyRuleLikelihoodExtractor will then use Jeffrey rule to change its contents again)
		// The likelihood evidence vector is the user input CPT converted to a uni-dimensional array
		float likelihood[] = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		TreeVariable nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue(((0.55f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.55f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.45f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.45f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		
		// check that LPE contains e2 and any values for D and F
		
		// prepare argument, which is input and output at the same moment
		List<Map<INode, Integer>> inOutArgLPE = new ArrayList<Map<INode,Integer>>();
		float minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		Map<INode, Integer> lpes = inOutArgLPE.get(0);
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	// index 1 has state e2
		
		// check that min-q is 90
		assertTrue("Obtained min q = " + minQ,((90f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (90f + ASSET_PRECISION_ERROR)) );
		
		
		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// test conditional LPE = 110 if we assume E = e1. 
		((AssetNode)assetQAlgorithm.getAssetNetwork().getNode("E")).addFinding(0);	// set evidence E = e1
		assetQAlgorithm.runMinPropagation(null);
		// obtain LPE and minQ when E = e1
		inOutArgLPE = new ArrayList<Map<INode,Integer>>();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		assertFalse(inOutArgLPE.isEmpty());
		// make sure the returned LPE has E = e1
		lpes = inOutArgLPE.get(0);
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	// index 0 has state e1
		// make sure returned minQ is 110
		assertTrue("Obtained min q = " + minQ,((110f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (110f + ASSET_PRECISION_ERROR)) );
		
		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// bet node is still E
		assertEquals(network.getNode("E"), betNode);
		
		// add D to bet condition
		Node assumedNode = network.getNode("D");
		assertNotNull(assumedNode);
		betConditions.add(assumedNode);
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(assumedNode));
		
		// extract CPT of E given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.55, .45, .55, .45]
		assertTrue(((0.55f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.55f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.45f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.45f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.55f - PROB_PRECISION_ERROR) < potential.getValue(2)) && (potential.getValue(2) < (0.55f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.45f - PROB_PRECISION_ERROR) < potential.getValue(3)) && (potential.getValue(3) < (0.45f + PROB_PRECISION_ERROR)) );
		
		
		// edit interval of P(E=e1|D=d1) should be [0.005, 0.995]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 0);	// 0 is the index of e1,d1
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.005f - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.005f + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.995f - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.995f + PROB_PRECISION_ERROR)) );
		
		
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.9f);
		potential.setValue(1, 0.1f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is [0.725 0.275] (this is expected value), and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue("Obtained marginal is " + nodeToTest.getMarginalAt(0),((0.725f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.725f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.275f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.275f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		
		
		// check that LPE contains d1, e2 and any value F
		
		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	// index 0 has state d1
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	// index 1 has state e2
		
		// check that min-q is 20
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		}
		assertTrue("Obtained min q = " + minQ,((20f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (20f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// test conditional LPE = 90 if we assume D = d2. 
		((AssetNode)assetQAlgorithm.getAssetNetwork().getNode("D")).addFinding(1);	// set evidence D = d2
		assetQAlgorithm.runMinPropagation(null);
		// obtain LPE and minQ when D = d2
		inOutArgLPE = new ArrayList<Map<INode,Integer>>();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		assertFalse(inOutArgLPE.isEmpty());
		// check that LPE contains d2, e2 and any value F
		lpes = inOutArgLPE.get(0);
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	// index 1 has state d2
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	// index 1 has state e2
		// make sure returned minQ is 90
		assertEquals("Obtained min q = " + minQ, 90f, minQ, ASSET_PRECISION_ERROR);
		
		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// create new user Joe
		AssetNetwork assetNetJoe = null;
		try {
			assetNetJoe = assetQAlgorithm.createAssetNetFromProbabilisticNet(network);
			assetNetJoe.setName("Joe");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(assetNetJoe);
		
		// set Joe as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetJoe);
		assertEquals(assetNetJoe, assetQAlgorithm.getAssetNetwork());
		
		// Joe bets P(E=e1|D=d2) = .55 -> .4
		
		// bet node is still E
		assertEquals(network.getNode("E"), betNode);
		
		// D is still the bet condition. 
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(network.getNode("D")));
		
		// extract CPT of E given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assertTrue(((0.9f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.9f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.1f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.1f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.55f - PROB_PRECISION_ERROR) < potential.getValue(2)) && (potential.getValue(2) < (0.55f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.45f - PROB_PRECISION_ERROR) < potential.getValue(3)) && (potential.getValue(3) < (0.45f + PROB_PRECISION_ERROR)) );
		

		// edit interval of P(E=e1|D=d2) should be [0.0055, 0.9955]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 2);	// 2 is the index of e1,d2
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.0055f - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.0055f + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.9955f - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.9955f + PROB_PRECISION_ERROR)) );
		
		
		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.4f);
		potential.setValue(3, 0.6f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
//			assetQAlgorithm.setToPropagateForGlobalConsistency(true);
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue("Obtained marginal is " + nodeToTest.getMarginalAt(0),((0.65f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.65f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.35f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.35f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		
		

		// check that LPE contains d2, e1 and any value F

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());
		
		// check that min-q is 72.727272...
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		}
		assertTrue("Obtained min q = " + minQ,((72.727272727f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (72.727272727f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// create new user Amy
		AssetNetwork assetNetAmy = null;
		try {
			assetNetAmy = assetQAlgorithm.createAssetNetFromProbabilisticNet(network);
			assetNetAmy.setName("Amy");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(assetNetAmy);
		
		// set Amy as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetAmy);
		assertEquals(assetNetAmy, assetQAlgorithm.getAssetNetwork());
		
		// Amy bets P(F=f1|D=d1) = .5 -> .3
		
		// bet node is F
		betNode = (TreeVariable) network.getNode("F");
		assertEquals(network.getNode("F"), betNode);
		
		// D is still the bet condition. 
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(network.getNode("D")));
		
		// extract CPT of F given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(2)) && (potential.getValue(2) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(3)) && (potential.getValue(3) < (0.5f + PROB_PRECISION_ERROR)) );
		

		// edit interval of P(F=f1|D=d1) should be [0.005, 0.995]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 0);	// 0 is the index of f1,d1
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.005f - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.005f + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.995f - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.995f + PROB_PRECISION_ERROR)) );
		
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.3f);
		potential.setValue(1, 0.7f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.4 .6], and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue("Obtained marginal is " + nodeToTest.getMarginalAt(0),((0.65f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.65f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.35f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.35f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.4f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.4f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.6f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.6f + PROB_PRECISION_ERROR)) );
		
		

		// check that LPE contains d1, f1 and any value E

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("F")).intValue());
		
		// check that min-q is 60...
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		}
		assertTrue("Obtained min q = " + minQ,((60f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (60f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		
		// set Joe as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetJoe);
		assertEquals(assetNetJoe, assetQAlgorithm.getAssetNetwork());
		
		// Joe bets P(F=f1|D=d2) = .5 -> .1
		
		// bet node is still F
		assertEquals(network.getNode("F"), betNode);
		
		// D is still the bet condition. 
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(network.getNode("D")));
		
		// extract CPT of F given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assertTrue(((0.3f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.3f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.7f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.7f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(2)) && (potential.getValue(2) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < potential.getValue(3)) && (potential.getValue(3) < (0.5f + PROB_PRECISION_ERROR)) );
		

		// edit interval of P(F=f1|D=d2) should be [0.006875, 0.993125]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 2);	// 2 is the index of f1,d2
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.006875f - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.006875f + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.993125f - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.993125f + PROB_PRECISION_ERROR)) );
		
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.1f);
		potential.setValue(3, 0.9f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
//			assetQAlgorithm.setToPropagateForGlobalConsistency(false);
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.2 .8], and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue("Obtained marginal is " + nodeToTest.getMarginalAt(0),((0.65f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.65f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.35f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.35f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.5f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.5f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.2f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.2f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.8f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.8f + PROB_PRECISION_ERROR)) );
		
		

		// check that LPE contains d2, e1, f1

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	
		assertEquals(0, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("F")).intValue());
		
		// check that min-q is 14.5454545...
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		} 
		assertTrue("Obtained min q = " + minQ,((14.5454545f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (14.5454545f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		
		// create new user Eric
		AssetNetwork assetNetEric = null;
		try {
			assetNetEric = assetQAlgorithm.createAssetNetFromProbabilisticNet(network);
			assetNetEric.setName("Eric");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(assetNetEric);
		
		// set Eric as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetEric);
		assertEquals(assetNetEric, assetQAlgorithm.getAssetNetwork());
		
		// Eric bets P(E=e1) = .65 -> .8
		
		// bet node is E
		betNode = (TreeVariable) network.getNode("E");
		assertEquals(network.getNode("E"), betNode);
		
		// no bet condition. 
		betConditions.clear();
		assertEquals(0, betConditions.size());
		
		// extract CPT of E
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(2,potential.tableSize());	

		// check whether probability prior to edit is really = [.65, .35]
		assertTrue(((0.65f - PROB_PRECISION_ERROR) < potential.getValue(0)) && (potential.getValue(0) < (0.65f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.35f - PROB_PRECISION_ERROR) < potential.getValue(1)) && (potential.getValue(1) < (0.35f + PROB_PRECISION_ERROR)) );
		

		// edit interval of P(E=e1) should be [0.0065, 0.9965]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 0);	// 0 is the index of e1
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.0065 - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.0065 + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.9965 - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.9965 + PROB_PRECISION_ERROR)) );
		
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.8f);
		potential.setValue(1, 0.2f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// Eric�fs expected score is S=10.1177
		assertEquals(10.1177, assetQAlgorithm.calculateExpectedAssets(), 0.0001);
		
		// check expected score given e2,d1,f1 == b*log(57.142857)
		try {
			AssetAwareInferenceAlgorithm clonedAlgorithm = (AssetAwareInferenceAlgorithm) assetQAlgorithm.clone(false);
			clonedAlgorithm.setToUpdateAssets(false);
			
			// the asset network is not going to change, so we cal use the original
			clonedAlgorithm.setAssetNetwork(assetQAlgorithm.getAssetNetwork());
			
			// propagate findings e2,d1,f1
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("E")).addFinding(1);
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("D")).addFinding(0);
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("F")).addFinding(0);
			clonedAlgorithm.propagate();
			assertEquals(clonedAlgorithm.getqToAssetConverter().getScoreFromQValues(57.142857), clonedAlgorithm.calculateExpectedAssets(), 0.0001);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that new marginal of E is [0.8 0.2] (this is expected value), F is [0.2165, 0.7835], and D is [0.5824, 0.4176]
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue("Obtained marginal is " + nodeToTest.getMarginalAt(0),((0.8f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.8f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.2f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.2f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.5824f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.5824f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.4176f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.4176f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.2165f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.2165f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.7835f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.7835f + PROB_PRECISION_ERROR)) );
		
		

		// check that LPE contains e2 and any D or F


		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		// TODO the test case indicates that d1 is not LPE... Check if it is true.
//		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());
		
		// check that min-q is 57.142857...
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		}
		assertTrue("Obtained min q = " + minQ,((57.142857f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (57.142857f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		
		// bet node is D
		betNode = (TreeVariable) network.getNode("D");
		assertEquals(network.getNode("D"), betNode);
		
		// bet condition is F. 
		assumedNode = network.getNode("F");
		betConditions.add(assumedNode);
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(assumedNode));
		
		// extract CPT of D given F
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [d1f2, d2f2] = [.52, .48]
		assertTrue(((0.52f - PROB_PRECISION_ERROR) < potential.getValue(2)) && (potential.getValue(2) < (0.52f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.48f - PROB_PRECISION_ERROR) < potential.getValue(3)) && (potential.getValue(3) < (0.48f + PROB_PRECISION_ERROR)) );
		

		// edit interval of P(D=d1|F=f2) should be [0.0091059, 0.9916058]
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 2);	// 2 is the index of d1 f2
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		assertTrue("Lower: " + editInterval[0], ((0.0091059 - PROB_PRECISION_ERROR) < editInterval[0]) && (editInterval[0] < (0.0091059 + PROB_PRECISION_ERROR)) );
		assertTrue("Upper: " + editInterval[1], ((0.9916058 - PROB_PRECISION_ERROR) < editInterval[1]) && (editInterval[1] < (0.9916058 + PROB_PRECISION_ERROR)) );
		
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.7f);
		potential.setValue(3, 0.3f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// Eric�fs expected score is now 10.31615.
		assertEquals(10.31615, assetQAlgorithm.calculateExpectedAssets(), 0.0001);
		
		// check expected score given d2, e2, f2 == b*log(35.7393f)
		try {
			AssetAwareInferenceAlgorithm clonedAlgorithm = (AssetAwareInferenceAlgorithm) assetQAlgorithm.clone(false);
			clonedAlgorithm.setToUpdateAssets(false);
			
			// the asset network is not going to change, so we cal use the original
			clonedAlgorithm.setAssetNetwork(assetQAlgorithm.getAssetNetwork());
			
			// propagate findings d2, e2, f2
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("D")).addFinding(1);
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("E")).addFinding(1);
			((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("F")).addFinding(1);
			clonedAlgorithm.propagate();
			assertEquals(clonedAlgorithm.getqToAssetConverter().getScoreFromQValues(35.7393), clonedAlgorithm.calculateExpectedAssets(), 0.0001);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
		// check that new marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue(((0.8509f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.8509f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.1491f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.1491f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.7232f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.7232f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.2768f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.2768f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.2165f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.2165f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.7835f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.7835f + PROB_PRECISION_ERROR)) );
		
		
		// check that LPE is d2, e2 and f2

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertTrue(lpes.size() == 3);
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("F")).intValue());	
		
		// check that min-q is 35.7393...
//		minQ = Float.MAX_VALUE;	
//		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
//			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
//				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
//					minQ = qClique.getProbabilityFunction().getValue(i);
//				}
//			}
//		}
		assertTrue("Obtained min q = " + minQ,(( 35.7393f - ASSET_PRECISION_ERROR) < minQ) && (minQ < ( 35.7393f + ASSET_PRECISION_ERROR)) );

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		
		// Eric makes a bet which makes his assets-q to go below 1, but the algorithm does not allow it
		
		// let the algorithm not to allow asset-q smaller than 1
		assetQAlgorithm.setToAllowZeroAssets(false);
		
		// bet node is D
		betNode = (TreeVariable) network.getNode("D");
		assertEquals(network.getNode("D"), betNode);
		
		// bet condition is F. 
		betConditions.clear();
		assumedNode = network.getNode("F");
		betConditions.add(assumedNode);
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(assumedNode));
		
		// extract CPT of D given F
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 2);	// 2 is the index of d1 f2
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		potential.setValue(2, editInterval[0]/10);
		potential.setValue(3, 1-(editInterval[0]/10));
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			fail("Algorithm should not allow this edit");
		} catch (ZeroAssetsException e) {
			// OK
			System.out.println(network.getLog());
			network.getLogManager().clear();
			assertNotNull(e);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that marginals have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		nodeToTest = (TreeVariable) network.getNode("E");
		assertTrue(((0.8509f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.8509f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.1491f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.1491f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("D");
		assertTrue(((0.7232f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.7232f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.2768f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.2768f + PROB_PRECISION_ERROR)) );
		nodeToTest = (TreeVariable) network.getNode("F");
		assertTrue(((0.2165f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(0)) && (nodeToTest.getMarginalAt(0) < (0.2165f + PROB_PRECISION_ERROR)) );
		assertTrue(((0.7835f - PROB_PRECISION_ERROR) < nodeToTest.getMarginalAt(1)) && (nodeToTest.getMarginalAt(1) < (0.7835f + PROB_PRECISION_ERROR)) );
		
		
		// check that LPE has not changed - still d2, e2 and f2

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		lpes = inOutArgLPE.get(0);	// current implementation only returns 1 LPE
		assertNotNull(lpes);
		assertEquals(lpes.toString()+", min = "+  minQ , 3,lpes.size());
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("D")).intValue());	
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("E")).intValue());	
		assertEquals(1, lpes.get(assetQAlgorithm.getAssetNetwork().getNode("F")).intValue());	
		
		assertTrue("Obtained min q = " + minQ,(( 35.7393f - ASSET_PRECISION_ERROR) < minQ) && (minQ < ( 35.7393f + ASSET_PRECISION_ERROR)) );
		
		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// Eric makes a bet which makes his assets-q to go below 1, and the algorithm allows it
		
		// let the algorithm allow asset-q smaller than 1
		assetQAlgorithm.setToAllowZeroAssets(true);
		
		// bet node is D
		betNode = (TreeVariable) network.getNode("D");
		assertEquals(network.getNode("D"), betNode);
		
		// bet condition is F. 
		assumedNode = network.getNode("F");
		betConditions.clear();
		betConditions.add(assumedNode);
		assertEquals(1, betConditions.size());
		assertTrue(betConditions.contains(assumedNode));
		
		// extract CPT of D given F
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		editInterval = assetQAlgorithm.calculateIntervalOfAllowedEdit(potential, 2);	// 2 is the index of d1 f2
		assertNotNull(editInterval);
		assertEquals(2, editInterval.length);
		
		// set P(D=d1|F=f2) to a value lower (half) than the lower bound of edit interval
		potential.setValue(2, editInterval[0]/2);
		potential.setValue(3, 1-(editInterval[0]/2));
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		betNode.addLikeliHood(likelihood, betConditions);
		
		try {
			// propagate soft evidence
			assetQAlgorithm.propagate();
			System.out.println(network.getLog());
			network.getLogManager().clear();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// check that LPE is 1 or below 

		// prepare argument, which is input and output at the same moment
		inOutArgLPE.clear();
		minQ = assetQAlgorithm.calculateExplanation(inOutArgLPE);		// it obtains both min-q value and states.
		if (!assetQAlgorithm.isToUseQValues()) {
			// minQ contains assets instead of q-values. Convert to q-values
			minQ = (float) assetQAlgorithm.getqToAssetConverter().getQValuesFromScore(minQ);
		}
		
		assertNotNull(lpes);
		assertFalse(lpes.isEmpty());
		assertTrue("Obtained min q = " + minQ, minQ <= 1);

		// undo only the min propagation (we do not need the min q values anymore, and next q-calculations must use q values prior to min propagation)
		assetQAlgorithm.undoMinPropagation();
		
		// make sure propagating evidences in cloned algorithm will give same result of propagating original network
		AssetAwareInferenceAlgorithm clonedAlgorithm = null;
		try {
			clonedAlgorithm = (AssetAwareInferenceAlgorithm) assetQAlgorithm.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(clonedAlgorithm);
		assertEquals(assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size(), clonedAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size());
		assertEquals(assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators().size(), clonedAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators().size());
		assertTrue(assetQAlgorithm.getRelatedProbabilisticNetwork() != clonedAlgorithm.getRelatedProbabilisticNetwork());
		assertTrue(assetQAlgorithm.getAssetNetwork() != clonedAlgorithm.getAssetNetwork());
		assertEquals(assetQAlgorithm.calculateExpectedAssets(), clonedAlgorithm.calculateExpectedAssets(), ASSET_PRECISION_ERROR);
		
		// make sure cloned and originals got same values before propagation
		for (int i = 0; i < assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size(); i++) {
			Clique probOrig =  assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique probClone =  clonedAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique assetOrig =  assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			Clique assetClone =  clonedAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			for (int j = 0; j < probOrig.getProbabilityFunction().tableSize(); j++) {
				assertEquals(probOrig.getProbabilityFunction().getValue(j), probClone.getProbabilityFunction().getValue(j), PROB_PRECISION_ERROR);
				assertEquals((assetOrig.getProbabilityFunction()).getValue(j), (assetClone.getProbabilityFunction()).getValue(j), ASSET_PRECISION_ERROR);
			}
		}

		// set D = d2 as finding in both original and cloned networks
		((ProbabilisticNode)assetQAlgorithm.getRelatedProbabilisticNetwork().getNode("D")).addFinding(1);
		((ProbabilisticNode)clonedAlgorithm.getRelatedProbabilisticNetwork().getNode("D")).addFinding(1);
		
		assetQAlgorithm.setToUpdateAssets(false);
		clonedAlgorithm.setToUpdateAssets(false);
		assetQAlgorithm.propagate();
		clonedAlgorithm.propagate();
		assetQAlgorithm.setToUpdateAssets(true);
		clonedAlgorithm.setToUpdateAssets(true);
		assertEquals(assetQAlgorithm.calculateExpectedAssets(), clonedAlgorithm.calculateExpectedAssets(), ASSET_PRECISION_ERROR);
		
		// make sure cloned and originals got same results
		for (int i = 0; i < assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size(); i++) {
			Clique probOrig =  assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique probClone =  clonedAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique assetOrig =  assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			Clique assetClone =  clonedAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			for (int j = 0; j < probOrig.getProbabilityFunction().tableSize(); j++) {
				assertEquals(probOrig.getProbabilityFunction().getValue(j), probClone.getProbabilityFunction().getValue(j), PROB_PRECISION_ERROR);
				assertEquals((assetOrig.getProbabilityFunction()).getValue(j), (assetClone.getProbabilityFunction()).getValue(j), ASSET_PRECISION_ERROR);
			}
		}
		
		// this time, propagation should not change anything
		boolean backup = assetQAlgorithm.isToPropagateForGlobalConsistency();
		assetQAlgorithm.setToPropagateForGlobalConsistency(false);
		assetQAlgorithm.propagate();
		assetQAlgorithm.setToPropagateForGlobalConsistency(backup);
		
		// make sure cloned and originals got same results
		for (int i = 0; i < assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size(); i++) {
			Clique probOrig =  assetQAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique probClone =  clonedAlgorithm.getRelatedProbabilisticNetwork().getJunctionTree().getCliques().get(i);
			Clique assetOrig =  assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			Clique assetClone =  clonedAlgorithm.getAssetNetwork().getJunctionTree().getCliques().get(i);
			for (int j = 0; j < probOrig.getProbabilityFunction().tableSize(); j++) {
				assertEquals(probOrig.getProbabilityFunction().getValue(j), probClone.getProbabilityFunction().getValue(j), PROB_PRECISION_ERROR);
				assertEquals((assetOrig.getProbabilityFunction()).getValue(j), (assetClone.getProbabilityFunction()).getValue(j), ASSET_PRECISION_ERROR);
			}
		}
		
		assertEquals(assetQAlgorithm.calculateExpectedAssets(), clonedAlgorithm.calculateExpectedAssets(), ASSET_PRECISION_ERROR);
		
	}

}
