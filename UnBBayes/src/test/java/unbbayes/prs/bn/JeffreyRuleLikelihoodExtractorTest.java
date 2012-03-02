/**
 * 
 */
package unbbayes.prs.bn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;

/**
 * @author Shou Matsumoto
 *
 */
public class JeffreyRuleLikelihoodExtractorTest extends TestCase {

	private ProbabilisticNetwork net;
	private JunctionTreeAlgorithm algorithm;
	private InCliqueConditionalProbabilityExtractor extractor;
	private ILikelihoodExtractor objectUnderTest;

	/**
	 * @param name
	 */
	public JeffreyRuleLikelihoodExtractorTest(String name) {
		super(name);
	}

	/**
	 * Load network and compile it using junction tree algorithm. The network is: <br/>
	 * D -> F <- G 	<br/>
	 * |			<br/>
	 * v			<br/>
	 * E			<br/>
	 * <br/>
	 * The cliques/separators are:  (DFG) - [D] - (DE) <br/>
	 * The conditional probability for F given D and G is: <br/>
	 * -----------------------------------------------------------	<br/>
	 * | _ __ g 1 _ _ _ _ | _ __ g 2 _ _ _ _ | _ __ g 3 _ _ _ __ |	<br/>
	 * -----------------------------------------------------------	<br/>
	 * | d1 | d 2_ | d 3_ | d1 | d 2_ | d 3_ | d 1 | d 2_  | d3  |	<br/>
	 * -----------------------------------------------------------	<br/>
	 * |0.7 | 0.05 | 0.10 | 0.1 | 0.05 | 0.60 | 0.4 | 0.05 | 0.35|	<br/>
	 * |0.2 | 0.90 | 0.30 | 0.2 | 0.90 | 0.30 | 0.2 | 0.90 | 0.30|	<br/>
	 * |0.1 | 0.05 | 0.60 | 0.7 | 0.05 | 0.10 | 0.4 | 0.05 | 0.35|	<br/>
	 * @see junit.framework.TestCase#setUp()
	 * 
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// load network from file
		BaseIO io = new NetIO();
		net = (ProbabilisticNetwork) io.load(new File(this.getClass().getClassLoader().getResource("testCases/testmodel_defg.net").toURI()));
		// start up algorithm for compiling net
		algorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		// compile net
		algorithm.run();
		extractor = (InCliqueConditionalProbabilityExtractor) InCliqueConditionalProbabilityExtractor.newInstance();
		objectUnderTest = JeffreyRuleLikelihoodExtractor.newInstance();
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test method for {@link unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor#extractLikelihoodRatio(unbbayes.prs.Graph, unbbayes.prs.INode)},
	 * and {@link unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor#extractLikelihoodParents(unbbayes.prs.Graph, unbbayes.prs.INode)}.
	 */
	public final void testExtractLikelihoodRatio() {
		TreeVariable nodeF = (TreeVariable) net.getNode("F");
		Node nodeD = net.getNode("D");
		Node nodeG = net.getNode("G");
		
		List<INode> expected = new ArrayList<INode>();
		expected.add(nodeD);
		expected.add(nodeG);
		
		// fill likelihood based on current probability distribution
		PotentialTable cpt = (PotentialTable) extractor.buildCondicionalProbability(nodeF, expected, net, algorithm);
		
		// instantiate likelihood
		float[] likelihood = new float[cpt.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = cpt.getValue(i);
		}
		
		// change P(F|D=d1,G=g1) from {0.7, 0.2, 0.1} to {0.1, 0.1, 0.8}
		likelihood[0] = .1f;
		likelihood[1] = .1f;
		likelihood[2] = .8f;
		
		// change P(F|D=d3,G=g3) from {0.35, 0.3, 0.35} to {0.8, 0.1, 0.1}
		likelihood[likelihood.length - 3] = .8f;
		likelihood[likelihood.length - 2] = .1f;
		likelihood[likelihood.length - 1] = .1f;
			
		// fill likelihood
		nodeF.addLikeliHood(likelihood, expected);
		
		// test extractLikelihoodParents
		List<INode> likelihoodParents = objectUnderTest.extractLikelihoodParents(net, nodeF);
		assertNotNull(likelihoodParents);
		assertEquals(expected, likelihoodParents);
		
		// test extractLikelihoodRatio
		float[] ratio = objectUnderTest.extractLikelihoodRatio(net, nodeF);
		
		assertNotNull(ratio);
		assertEquals(27, ratio.length);
		
		float[] expectedRatio = new float[]{0.01652893f, 0.05785124f, 0.92561983f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.33333333f, 0.78688525f, 0.11475410f, 0.09836066f};
		
		for (int i = 0; i < expectedRatio.length; i++) {
//			assertEquals("On index " + i + ", expected = " + expectedRatio[i] + "; obtained = " + ratio[i], 
//					expectedRatio[i] , ratio[i]);
			
			assertTrue("On index " + i + ", expected = " + expectedRatio[i] + "; obtained = " + ratio[i], 
					expectedRatio[i]-0.00005 < ratio[i] && ratio[i] < expectedRatio[i]+0.00005);
		}
		
	}

	


}
