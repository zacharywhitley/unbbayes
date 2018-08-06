package unbbayes.prs.bn;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import unbbayes.io.BaseIO;
import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;

/**
 * Test methods for {@link JeffreyRuleLikelihoodExtractor} and Jeffrey's rule soft evidences in {@link JunctionTreeAlgorithm}.
 * @author Shou Matsumoto
 */
public class JeffreyRuleLikelihoodExtractorTest extends TestCase {

	/** Error margin to be used when comparing probabilities */
	private static final float PROB_PRECISION_ERROR = 0.00005f;
	

	/**
	 * Default constructor for this JUnit test case.
	 */
	public JeffreyRuleLikelihoodExtractorTest(String name) {
		super(name);
	}

	
	/**
	 * Test method for {@link unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor#extractLikelihoodRatio(unbbayes.prs.Graph, unbbayes.prs.INode)},
	 * and {@link unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor#extractLikelihoodParents(unbbayes.prs.Graph, unbbayes.prs.INode)}.
	 * <br/>
	 * <br/>
	 * This loads network and compile it using junction tree algorithm. The network is: <br/>
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
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws LoadException 
	 */
	public final void testExtractLikelihoodRatio() throws LoadException, IOException, URISyntaxException {
		

		// load network from file
		BaseIO io = new NetIO();
		ProbabilisticNetwork net = (ProbabilisticNetwork) io.load(new File(this.getClass().getClassLoader().getResource("testCases/testmodel_defg.net").toURI()));
		// start up algorithm for compiling net
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		// compile net
		junctionTreeAlgorithm.run();
		InCliqueConditionalProbabilityExtractor arbitraryCPTExtractor = (InCliqueConditionalProbabilityExtractor) InCliqueConditionalProbabilityExtractor.newInstance();
		ILikelihoodExtractor likelihoddExtractor = JeffreyRuleLikelihoodExtractor.newInstance();
		
		TreeVariable nodeF = (TreeVariable) net.getNode("F");
		Node nodeD = net.getNode("D");
		Node nodeG = net.getNode("G");
		
		List<INode> conditions = new ArrayList<INode>();
		conditions.add(nodeD);
		conditions.add(nodeG);
		
		// fill likelihood based on current probability distribution
		PotentialTable cpt = (PotentialTable) arbitraryCPTExtractor.buildCondicionalProbability(nodeF, conditions, net, junctionTreeAlgorithm);
		
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
		nodeF.addLikeliHood(likelihood, conditions);
		
		// test extractLikelihoodParents
		List<INode> likelihoodParents = likelihoddExtractor.extractLikelihoodParents(net, nodeF);
		assertNotNull(likelihoodParents);
		assertEquals(conditions, likelihoodParents);
		
		// test extractLikelihoodRatio
		float[] ratio = likelihoddExtractor.extractLikelihoodRatio(net, nodeF);
		
		assertNotNull(ratio);
		assertEquals(27, ratio.length);
		
		float[] expectedRatio = new float[]{0.00438917f, 0.01536211f, 0.24579371f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.03072421f, 0.07022677f, 0.01024140f, 0.00877835f};
		
		
		for (int i = 0; i < expectedRatio.length; i++) {
			assertEquals("On index " + i + ", expected = " + expectedRatio[i] + "; obtained = " + ratio[i], 
					expectedRatio[i], ratio[i], 0.00005);
		}
		
	}


	/**
	 * Test method for Jeffrey's rule soft evidences simulated with Bayes rule and virtual nodes.
	 * <br/>
	 * <br/>
	 * The DEF net looks like the following.		<br/>
	 * Note: uniform distribution for all nodes.	<br/>
	 * D--->F										<br/>
	 * | 											<br/>
	 * V  											<br/>
	 * E											<br/>
	 * <br/>
	 * The idea of conditional soft evidence in UnBBayes is to start from a CPT (generated on-the-fly depending on what nodes are assumed)
	 * containing current probabilities, and only change the cells we want to.
	 * E.g. If we want to set P(D=d1 | E=e1) = .9, then initially we have:
	 * <pre>
	 *     _______________
	 *     |  e1  |  e2  |
	 *    ---------------
	 * d1  | .5   |  .5  |
	 * d2  | .5   |  .5  |
	 *     ---------------
	 * </pre>
	 * And we want to change it to:
	 * <pre>
	 *     _______________
	 *     |  e1  |  e2  |
	 *    ---------------
	 * d1  | .9   |  .5  |
	 * d2  | .1   |  .5  |
	 *     ---------------
	 * </pre>
	 *     
	 * This is how soft evidence is inserted into UnBBayes (please, notice that P(D|E=e2) did not change--I didn't want to change it).
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws LoadException 
	 */	
	public final void testSoftEvidence() throws LoadException, IOException, URISyntaxException {

		// load network from file
		BaseIO io = new NetIO();
		ProbabilisticNetwork net = (ProbabilisticNetwork) io.load(new File(this.getClass().getClassLoader().getResource("testCases/DEF_flat.net").toURI()));
		// start up algorithm for compiling net
		JunctionTreeAlgorithm junctionTreeAlgorithm = new JunctionTreeAlgorithm((ProbabilisticNetwork) net);
		// compile net
		junctionTreeAlgorithm.run();
		
		// enable soft evidence by using Jeffrey's rule w/ virtual nodes.
		// By doing this, soft evidence is translated to equivalent likelihood evidence which will set probability to desired value.
		ILikelihoodExtractor likelihoddExtractor = JeffreyRuleLikelihoodExtractor.newInstance();
		junctionTreeAlgorithm.setLikelihoodExtractor(likelihoddExtractor);
		
		// A (conditional) soft evidence is represented as a list of conditions (assumed nodes) and an array of float representing new conditional probability.
		// Non-edited probabilities must remain in the values before the edit. Thus, we must extract what are the current probability values.
		// this object extracts conditional probability of any nodes in same clique (it assumes prob network was compiled using junction tree algorithm)
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();	
		assertNotNull(conditionalProbabilityExtractor);
		
		
		// Let's set P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// extract E
		TreeVariable nodeToAddSoftEvidence = (TreeVariable) net.getNode("E");
		assertNotNull(nodeToAddSoftEvidence);
		
		// conditions (nodes) assumed in the evidence (currently, none)
		List<INode> conditions =  new ArrayList<INode>();
		
		// first, extract what is P(E) prior to edit (this is the "CPT" generated on-the-fly with no nodes assumed)
		PotentialTable potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(2, potential.tableSize());
		
		// check whether probability prior to edit is really 0.5
		assertEquals(.5f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(1), PROB_PRECISION_ERROR);
		
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
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		// check that new marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		TreeVariable nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.55f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.45f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Set P(E=e1|D=d1) = .55 -> .9
		
		// node is still E
		assertEquals(net.getNode("E"), nodeToAddSoftEvidence);
		
		// add D to condition
		Node assumedNode = net.getNode("D");
		assertNotNull(assumedNode);
		conditions.add(assumedNode);
		assertEquals(1, conditions.size());
		assertTrue(conditions.contains(assumedNode));
		
		// extract CPT of E given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.55, .45, .55, .45]
		assertEquals(.55f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.45f, potential.getValue(1), PROB_PRECISION_ERROR);
		assertEquals(.55f, potential.getValue(2), PROB_PRECISION_ERROR);
		assertEquals(.45f, potential.getValue(3), PROB_PRECISION_ERROR);
		
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.9f);
		potential.setValue(1, 0.1f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given parents (conditions assumed in the bet - not empty anymore)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		// check that new marginal of E is [0.725 0.275] (this is expected value), and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.725f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.275f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Change P(E=e1|D=d2) = .55 -> .4
		
		// node is still E
		assertEquals(net.getNode("E"), nodeToAddSoftEvidence);
		
		// D is still the condition. 
		assertEquals(1, conditions.size());
		assertTrue(conditions.contains(net.getNode("D")));
		
		// extract CPT of E given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assertEquals(.9f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.1f, potential.getValue(1), PROB_PRECISION_ERROR);
		assertEquals(.55f, potential.getValue(2), PROB_PRECISION_ERROR);
		assertEquals(.45f, potential.getValue(3), PROB_PRECISION_ERROR);
		

		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.4f);
		potential.setValue(3, 0.6f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given parents (conditions assumed in the bet - not empty now)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		// check that new marginal of E is [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.65f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.35f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Change P(F=f1|D=d1) = .5 -> .3
		
		// node is F
		nodeToAddSoftEvidence = (TreeVariable) net.getNode("F");
		assertEquals(net.getNode("F"), nodeToAddSoftEvidence);
		
		// D is still the condition. 
		assertEquals(1, conditions.size());
		assertTrue(conditions.contains(net.getNode("D")));
		
		// extract CPT of F given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assertEquals(.5f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(1), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(2), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(3), PROB_PRECISION_ERROR);
		
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.3f);
		potential.setValue(1, 0.7f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given parents (conditions assumed in the bet - not empty now)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.4 .6], and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.65f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.35f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.4f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.6f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Change P(F=f1|D=d2) = .5 -> .1
		
		// node is still F
		assertEquals(net.getNode("F"), nodeToAddSoftEvidence);
		
		// D is still the condition. 
		assertEquals(1, conditions.size());
		assertTrue(conditions.contains(net.getNode("D")));
		
		// extract CPT of F given D
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assertEquals(.3f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.7f, potential.getValue(1), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(2), PROB_PRECISION_ERROR);
		assertEquals(.5f, potential.getValue(3), PROB_PRECISION_ERROR);
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.1f);
		potential.setValue(3, 0.9f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given parents (conditions assumed in the bet - not empty now)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.2 .8], and the others have not changed (remains 50%)
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.65f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.35f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.5f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.2f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.8f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Change P(E=e1) = .65 -> .8
		
		// node is E
		nodeToAddSoftEvidence = (TreeVariable) net.getNode("E");
		assertEquals(net.getNode("E"), nodeToAddSoftEvidence);
		
		// no condition. 
		conditions.clear();
		assertEquals(0, conditions.size());
		
		// extract CPT of E
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(2,potential.tableSize());	

		// check whether probability prior to edit is really = [.65, .35]
		assertEquals(.65f, potential.getValue(0), PROB_PRECISION_ERROR);
		assertEquals(.35f, potential.getValue(1), PROB_PRECISION_ERROR);
		
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2 (i.e. we are changing only the cells we want)
		potential.setValue(0, 0.8f);
		potential.setValue(1, 0.2f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given (empty) parents (conditions assumed in the bet - empty now)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		
		
		// check that new marginal of E is [0.8 0.2] (this is expected value), F is [0.2165, 0.7835], and D is [0.5824, 0.4176]
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(.8f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.2f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(.5824f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.4176f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(.2165f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(.7835f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
		// Change P(D=d1|F=f2) = 0.5203 -> 0.7
		
		// node is D
		nodeToAddSoftEvidence = (TreeVariable) net.getNode("D");
		assertEquals(net.getNode("D"), nodeToAddSoftEvidence);
		
		// condition is F. 
		assumedNode = net.getNode("F");
		conditions.add(assumedNode);
		assertEquals(1, conditions.size());
		assertTrue(conditions.contains(assumedNode));
		
		// extract CPT of D given F
		potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(nodeToAddSoftEvidence, conditions, net, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(4,potential.tableSize());	// CPT of a node with 2 states conditioned to a node with 2 states -> CPT with 2*2 cells.

		// check whether probability prior to edit is really [d1f2, d2f2] = [.5203, .4797]
		assertEquals(0.5203f, potential.getValue(2), PROB_PRECISION_ERROR);
		assertEquals(0.4797f, potential.getValue(3), PROB_PRECISION_ERROR);
		
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3 (i.e. we are changing only the cells we want)
		potential.setValue(2, 0.7f);
		potential.setValue(3, 0.3f);
		
		// fill array of likelihood with values in CPT
		likelihood = new float[potential.tableSize()];
		for (int i = 0; i < likelihood.length; i++) {
			likelihood[i] = potential.getValue(i);
		}
		
		// add likelihood ratio given parents (conditions assumed in the bet - not empty now)
		nodeToAddSoftEvidence.addLikeliHood(likelihood, conditions);
		
		// propagate soft evidence
		junctionTreeAlgorithm.propagate();
		System.out.println(net.getLog());
		net.getLogManager().clear();
		
		
		// check that new marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		nodeToTest = (TreeVariable) net.getNode("E");
		assertEquals(0.8509f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(0.1491f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("D");
		assertEquals(0.7232f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(0.2768f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		nodeToTest = (TreeVariable) net.getNode("F");
		assertEquals(0.2165f, nodeToTest.getMarginalAt(0), PROB_PRECISION_ERROR);
		assertEquals(0.7835f, nodeToTest.getMarginalAt(1), PROB_PRECISION_ERROR);
		
		
	}



}
