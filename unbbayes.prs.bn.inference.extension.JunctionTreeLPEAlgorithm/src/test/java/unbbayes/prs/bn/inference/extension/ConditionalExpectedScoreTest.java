/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import unbbayes.io.NetIO;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import junit.framework.TestCase;

/**
 * @author gch
 *
 */
public class ConditionalExpectedScoreTest extends TestCase {

	private double b = 10/(Math.log(100)) ;;

	/**
	 * @param name
	 */
	public ConditionalExpectedScoreTest(String name) {
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

//	public final void testExpectedScore() {
//		
//	}
	
	public final void testConditionalExpectedScore()  {
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
		
		// enable soft evidence by using Jeffrey rule in likelihood evidence w/ virtual nodes.
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
		
		IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();	
		assertNotNull(conditionalProbabilityExtractor);
		
		TreeVariable betNode = (TreeVariable) network.getNode("E");
		assertNotNull(betNode);
		
		// conditions (nodes) assumed in the bet (currently, none)
		List<INode> betConditions =  new ArrayList<INode>();
		
		// first, extract what is P(E) prior to edit (this is the "CPT" generated on-the-fly with no nodes assumed)
		PotentialTable potential = (PotentialTable) conditionalProbabilityExtractor.buildCondicionalProbability(betNode, betConditions, network, junctionTreeAlgorithm);
		assertNotNull(potential);
		assertEquals(2, potential.tableSize());
				
		// set P(E=e1) = 0.55 and P(E=e2) = 0.45 (we are changing only the cells we want)
		potential.setValue(0, 0.6f);
		potential.setValue(1, 0.4f);
		
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
		
		// unconditional expected score
		Iterator<Clique> iterator_clq = assetNetTom.getJunctionTree().getCliques().iterator();
		Iterator<Separator> iterator_sep = assetNetTom.getJunctionTree().getSeparators().iterator(); 
		 
//		foreach + Tab
		double bar_score = 0 ;		
		double curr_q, curr_p, curr_score ;
		
		for (int k = 0; k < assetNetTom.getJunctionTree().getCliques().size(); k++) {
			Clique assetClique = assetNetTom.getJunctionTree().getCliques().get(k) ;
			Clique probClique = network.getJunctionTree().getCliques().get(k) ;			
			for (int i = 0; i < assetClique.getProbabilityFunction().tableSize(); i++) {
				System.out.println("assets in clique {" +assetClique + "} is " + 
						assetClique.getProbabilityFunction().getValue(i));
				curr_q = assetClique.getProbabilityFunction().getValue(i) ;
				curr_p = probClique.getProbabilityFunction().getValue(i) ;
				curr_score = b*Math.log(curr_q) ;
				bar_score += curr_p * curr_score ;
			}			
		}
		
		while (iterator_sep.hasNext()) {
			Separator assetSeparator = (Separator) iterator_sep.next();			
			List<Separator> listOfSeparator = new ArrayList<Separator>( network.getJunctionTree().getSeparators() );
			Separator probSeparator = listOfSeparator.get(listOfSeparator.indexOf(assetSeparator));
			for (int i = 0; i < assetSeparator.getProbabilityFunction().tableSize(); i++) {				
				curr_q = assetSeparator.getProbabilityFunction().getValue(i) ;
				curr_p = probSeparator.getProbabilityFunction().getValue(i) ;
				curr_score = b*Math.log(curr_q) ;
				bar_score -= curr_p * curr_score ;
			}
		}
		System.out.println("Unconditional expected score is " + bar_score ) ;
		
		// conditional expected score
		AssetAwareInferenceAlgorithm clonedAlgorithm = null;
		try {
			clonedAlgorithm = (AssetAwareInferenceAlgorithm)assetQAlgorithm.clone();
			clonedAlgorithm.setToUpdateAssets(false);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ProbabilisticNetwork clonedNetwork = (ProbabilisticNetwork) clonedAlgorithm.getNetwork();
		
		ProbabilisticNode nodeE =(ProbabilisticNode) clonedNetwork .getNode("E");
		nodeE.addFinding(0);
		clonedAlgorithm.propagate() ;
		
		bar_score = 0 ;
		for (int k = 0; k < assetNetTom.getJunctionTree().getCliques().size(); k++) {
			Clique assetClique = assetNetTom.getJunctionTree().getCliques().get(k) ;
			Clique probClique = clonedNetwork.getJunctionTree().getCliques().get(k) ;			
			for (int i = 0; i < assetClique.getProbabilityFunction().tableSize(); i++) {
				System.out.println("probability in clique {" +assetClique + "} is " + 
						probClique.getProbabilityFunction().getValue(i));
				curr_q = assetClique.getProbabilityFunction().getValue(i) ;
				curr_p = probClique.getProbabilityFunction().getValue(i) ;
				curr_score = b*Math.log(curr_q) ;
				bar_score += curr_p * curr_score ;
			}			
		}
		
		iterator_sep = assetNetTom.getJunctionTree().getSeparators().iterator();
		while (iterator_sep.hasNext()) {
			Separator assetSeparator = (Separator) iterator_sep.next();			
			List<Separator> listOfSeparator = new ArrayList<Separator>( clonedNetwork.getJunctionTree().getSeparators() );
			Separator probSeparator = listOfSeparator.get(listOfSeparator.indexOf(assetSeparator));
			for (int i = 0; i < assetSeparator.getProbabilityFunction().tableSize(); i++) {				
				curr_q = assetSeparator.getProbabilityFunction().getValue(i) ;
				curr_p = probSeparator.getProbabilityFunction().getValue(i) ;
				curr_score = b*Math.log(curr_q) ;
				bar_score -= curr_p * curr_score ;
			}
		}
		System.out.println("Conditional expected score give E=1 is " + bar_score ) ;		
		
		
		
//		while(iterator_clq.hasNext()) {
//			Clique assetClique = iterator_clq.next();
//			for (int i = 0; i < assetClique.getProbabilityFunction().tableSize(); i++) {
//				System.out.println("assets in clique {" +assetClique + "} is " + 
//						assetClique.getProbabilityFunction().getValue(i));
//				curr_q = assetClique.getProbabilityFunction().getValue(i) ;
//				curr_p = network.getJunctionTree().getCliques().
//				curr_score = b*Math.log(curr_q) ;
//				curr_score = prob
//				assertEquals(100, assetClique.getProbabilityFunction().getValue(i), 0.001);
//			}
//		}
		
//		for (iterable_type iterable_element : iterable) {
//			
//		}
//		
//		for (Clique assetClique : assetNetTom.getJunctionTree().getCliques()) {
//			for (int i = 0; i < assetClique.getProbabilityFunction().tableSize(); i++) {
//				System.out.println("assets in clique {" +assetClique + "} is " + 
//						assetClique.getProbabilityFunction().getValue(i));
//				assertEquals(100, assetClique.getProbabilityFunction().getValue(i), 0.001);
//			}
//		}
		
		
//		for (Clique probClique : network.getJunctionTree().getCliques()) {
//			for (int i = 0; i < probClique.getProbabilityFunction().tableSize(); i++) {
//				assertTrue(0<= probClique.getProbabilityFunction().getValue(i)
//						&& probClique.getProbabilityFunction().getValue(i) <= 1);
//				PotentialTable potentialTable = probClique.getProbabilityFunction();
//				INode node0 = potentialTable.getVariableAt(0);
//				potentialTable.indexOfVariable(node0);				
//			}
//		}
		
	}
}
