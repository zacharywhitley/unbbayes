/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.io.NetIO;
import unbbayes.prs.INode;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;

/**
 * This is a JUnit test case for the asset propagation algorithms
 * @author Shou Matsumoto
 *
 */
public class AssetAwareInferenceAlgorithmTest extends TestCase {
	
	/** If two probability values are within an interval of + or - this value, then it is considered to be equalÅ@*/
	private static final float PROB_PRECISION_ERROR = 0.005f;

	/** If two asset q values are within an interval of + or - this value, then it is considered to be equalÅ@*/
	private static final float ASSET_PRECISION_ERROR = 0.5f;

	/**
	 * @param name
	 */
	public AssetAwareInferenceAlgorithmTest(String name) {
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
	 * The sequence is:
	 * 
	 * There are two cliques {D, E}, and {D, F}, initial asset tables have q-value as 100 in every cell. 
Current marginal probabilities are:
Variables    D             E              F 
Marginals   [0.5 0.5]   [0.5 0.5]   [0.5 0.5]

Trade-1: Tom would like to make a bet on E=e1, that has current probability as 0.5. First of all, we need to calculate TomÅfs edit limit (in this case, there is no assumption): 
Given E=e1, min-q1 = 100
Given E~=e1, min-q2 = 100
From Equation (1), edit interval is [0.005, 0.995]. 
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55 (current)

Variables   D              E                   F 
Marginals   [0.5 0.5]   [0.55 0.45]   [0.5 0.5]

TomÅfs min-q is 90, at the following 4 min-states (found by min-asset-propagation): 
     D    E    F
     1     2     1
     1     2     2
     2     2     1
     2     2     2


Trade-2: Now Tom would like to make another conditional bet on E=e1 given D=d1 (current P(E=e1|D=d1) = 0.55). Again, let us calculate his edit limits first (in this case, we have assumed variable D=d1). And note that TomÅfs asset tables are not the initial ones any more, but updated from last trade he did, now: 
Given E=e1, and D=d1, min-q1 = 110
Given E~=e1, and D=d1, min-q2 = 90
From Equation (1), edit interval is [0.005, 0.995]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 (current)
Variables   D              E                   F 
Marginals   [0.5 0.5]   [0.725 0.275]   [0.5 0.5]

TomÅfs min-q is 20, at the following two min-states (found by min-asset-propagation): 
     D    E    F
     1     2    1
     1     2    2


Trade-3: Joe came and intended to make a bet on E=e1 given D=d2 (current P(E=e1|D=d2) is 0.55). This will be his first edit, so he has initial asset tables before his trade. 
Edit limit:
Given E=e1, and D=d2, min-q1 = 100
Given E~=e1, and D=d2, min-q2 = 100
From Equation (1), edit interval is [0.0055, 0.9955]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 
Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 (current) 
  
Variables   D              E                   F 
Marginals   [0.5 0.5]   [0.65 0.35]   [0.5 0.5]

JoeÅfs min-q is 72.72727272727..., at the following two min-states (found by min-asset-propagation): 
     D    E    F
     2     1    1
     2     1    2


Trade-4: Now Amy is interested in changing P(F=f1|D=d1), which is currently 0.5. It will be her first edit, so she also has initial asset tables before the trade. 
Edit limit:
Given F=f1, and D=d1, min-q1 = 100
Given F~=f1, and D=d1, min-q2 = 100
From Equation (1), edit interval is [0.005, 0.995]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 
Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 
Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 (current)
  
Variables   D              E                   F 
Marginals   [0.5 0.5]   [0.65 0.35]   [0.4 0.6]

AmyÅfs min-q is 60, at the following two min-states (found by min-asset-propagation): 
     D    E    F
     1     1    1
     1     2    1


Trade-5: Joe would like to trade again on P(F=f1|D=d2), which is currently 0.5. 
Edit limit:
Given F=f1, and D=d2, min-q1 = 72.727272727
Given F~=f1, and D=d2, min-q2 = 72.727272727
From Equation (1), edit interval is [0.006875, 0.993125]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 
Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 
Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 
Joe2:	P(F=f1|D=d2) = 0.5 to 0.1
  
Variables   D              E                   F 
Marginals   [0.5 0.5]   [0.65 0.35]   [0.2 0.8]

AmyÅfs min-q is 14.54545454546, at the following unique min-states (found by min-asset-propagation): 
     D    E    F
     2     1    1

At this point, the model DEF reaches the status that has the same CPTs as the starting CPTs for the experimental model DEF we used in our AAAI 2012 paper. 

From now on, we run test cases described in the paper. 

Trade-6: Eric would like to trade on P(E=e1), which is currently 0.65. 
To decide long or short, S(E=e1) = 10, S(E~=e1)=10, no difference because this will be EricÅfs first trade.
Edit limit:
Given F=f1, and D=d2, min-q1 = 100
Given F~=f1, and D=d2, min-q2 = 100
From Equation (1), edit interval is [0.0065, 0.9965]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 
Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 
Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 
Joe2:	P(F=f1|D=d2) = 0.5 to 0.1
Eric1:	P(E=e1) = 0.65 to 0.8 (current)
  
Variables   D              E                   F 
Marginals   [0.5824, 0.4176]   [0.8, 0.2]   [0.2165, 0.7835]

EricÅfs expected score is S=10.1177.
EricÅfs min-q is 57.142857, at the following two min-states (found by min-asset-propagation): 
     D    E    F
     2     2    1
     2     2    2


Trade-7: Eric would like to make another edit. This time, he is interested on changing P(D=d1|F=f2), which is currently 0.52. 
To decide long or short, S(D=d1, F=f2) = 10.36915, S(D~=d1, F=f2)=9.7669.
Edit limit:
Given D=d1, and F=f2 , min-q1 = 57.142857
Given D~=d1, and F=f2, min-q2 = 57.142857
From Equation (1), edit interval is [0.0091059, 0.9916058]
- Verification successful: after substituting the limits into edit, we do get min-q to be 1. 

Trading sequence: 
Tom1:	P(E=e1) = 0.5  to 0.55
Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 
Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 
Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 
Joe2:	P(F=f1|D=d2) = 0.5 to 0.1
Eric1:	P(E=e1) = 0.65 to 0.8 
Eric2:	P(D=d1|F=f2) = 0.52 to 0.7 (current)
  
Variables   D                            E                             F 
Marginals   [0.7232, 0.2768]   [0.8509, 0.1491]   [0.2165, 0.7835]

EricÅfs expected score is now 10.31615. 
EricÅfs min-q is 35.7393, at the following unique min-states (found by min-asset-propagation): 
     D    E    F
     2     2    2


	 * 
	 */
	public final void testDEFNet() {
		
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
		ProbabilisticNetwork network = null;
		try {
			network = (ProbabilisticNetwork) netio.load(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(network);
		
		// algorithm to propagate probabilities
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm(network);
		assertNotNull(junctionTreeAlgorithm);
		
		// enable soft evidence by using jeffrey rule in likelihood evidence w/ virtual nodes.
		junctionTreeAlgorithm.setLikelihoodExtractor(JeffreyRuleLikelihoodExtractor.newInstance() );
		
		// algorithm to propagate q values (assets) given that probabilities were propagated
		AssetAwareInferenceAlgorithm assetQAlgorithm = (AssetAwareInferenceAlgorithm) AssetAwareInferenceAlgorithm.getInstance(junctionTreeAlgorithm);
		assertNotNull(assetQAlgorithm);
		
		// set the default asset q quantity of all clique cells to 100
		assetQAlgorithm.setDefaultInitialAssetQuantity(100);
		
		try {
			// compile network
			assetQAlgorithm.run();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// create new user Tom and his asset net
		AssetNetwork assetNetTom = null;
		try {
			assetNetTom = assetQAlgorithm.createAssetNetFromProbabilisticNet(network);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertNotNull(assetNetTom);
		
		// set tom as the current network user
		assetQAlgorithm.setAssetNetwork(assetNetTom);
		assertEquals(assetNetTom, assetQAlgorithm.getAssetNetwork());
		
		// A soft evidence is represented as a list of conditions (assumed nodes) and an array of float representing new conditional probability.
		// Non-edited probabilities must remain in the values before the edit. Thus, we must extract what are the current probability values.
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();	
		assertNotNull(conditionalProbabilityExtractor);
		
		// Let's bet P(E=e1) = 0.5  to 0.55 
		
		// extract E
		TreeVariable betNode = (TreeVariable) network.getNode("E");
		assertNotNull(betNode);
		
		// conditions (nodes) assumed in the bet (currently, none)
		List<INode> betConditions =  new ArrayList<INode>();
		
		// first, extract what is P(E) prior to edit
		PotentialTable potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		
		// set P(E=e1) = 0.55 and P(E=e2) = 0.45
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
		
		// check that min-q is 90
		float minQ = Float.MAX_VALUE;	
		for (Clique qClique : assetQAlgorithm.getAssetNetwork().getJunctionTree().getCliques()) {
			for (int i = 0; i < qClique.getProbabilityFunction().tableSize(); i++) {
				if (qClique.getProbabilityFunction().getValue(i) < minQ) {
					minQ = qClique.getProbabilityFunction().getValue(i);
				}
			}
		}
		assertTrue(((90f - ASSET_PRECISION_ERROR) < minQ) && (minQ < (90f + ASSET_PRECISION_ERROR)) );
		
		// TODO other tests
	}

}
