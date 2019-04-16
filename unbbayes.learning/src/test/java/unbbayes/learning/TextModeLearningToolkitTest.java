package unbbayes.learning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;

/**
 * @author Shou Matsumoto
 *
 */
public class TextModeLearningToolkitTest {

	
	private static final String PARADIGMALGORITHMMETRICPARAMSGREEDY[] = {
			AlgorithmController.PARADIGMS.Ponctuation.name(),	// paradigm of algorithm
			AlgorithmController.SCORING_ALGORITHMS.K2.name(),	// algorithm in paradigm to use
			AlgorithmController.METRICS.GH.name(),				// metric
			"-1.5"												// special parameter used in algorithm
	};
	
	// IC cannot handle missing values...
//	private static final String PARADIGMALGORITHMMETRICPARAMSCI[] = {
//		AlgorithmController.PARADIGMS.IC.name(),			// paradigm of algorithm
//		AlgorithmController.INDEPENDENCE_ALGORITHM_CBLA,	// algorithm in paradigm to use
//		"",													// metric is not required
//		"0.01"												// special parameter used in algorithm
//	};

	public TextModeLearningToolkitTest() {}

	/**
	 * Test method for {@link unbbayes.learning.LearningToolkit}
	 * with arcs added programatically before parameter learning.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InvalidParentException 
	 */
	@Test
	public final void testLearnNetAddArc() throws URISyntaxException, IOException, InvalidParentException {
		
		URL url = getClass().getResource("data_freq50.txt");
//		url = getClass().getResource("Chest_Clinic_3state.txt");
		
		assertNotNull(url);
		File inputFile = new File(url.toURI());
		
		File outputFile = new File("learnedNetWithExtraArcsDataFreq50.net");
		
		String prefixNodeFrom = "w1";
		String prefixNodeTo = "w2";
//		prefixNodeFrom = "Asia";
//		prefixNodeTo = "Bronquite";
		
		
		assertTrue(inputFile.exists());
		
		// intantiate controller and learning toolkit
		TextModeConstructionController controller = new TextModeConstructionController(inputFile, null);
		TextModeLearningToolkit learningKit = new TextModeLearningToolkit(null, controller.getVariables());
		
		// initialize data in learning toolkit
		learningKit.setData(controller.getVariables(), controller.getMatrix(), controller.getVector(), controller.getCaseNumber(), controller.isCompacted());
		
		// set structure learning parameter
		learningKit.setParadigmAlgorithmMetricParam(PARADIGMALGORITHMMETRICPARAMSGREEDY);
		
		// perform structure learning
		learningKit.buildNet(true);
		
		// retrieve the learned model
		ProbabilisticNetwork learnedNet = learningKit.getLearnedNet();
		
		// add extra arcs
		Set<Edge> newEdges = new HashSet<Edge>();
		for (Node node : learnedNet.getNodes()) {
			if (node.getName().startsWith(prefixNodeFrom)) {
				// obtain respective node which differs only by prefix
				String targetNodeName = node.getName().replaceFirst(prefixNodeFrom, prefixNodeTo);
				Node targetNode = learnedNet.getNode(targetNodeName);
				assertNotNull(targetNodeName, targetNode);
				Edge edge = new Edge(node, targetNode);
				learnedNet.addEdge(edge);
				newEdges.add(edge);
			}
		}
		
		// perform parameter learning
		learningKit.runParameterLearning(controller.getVariables(), controller.getMatrix(),
				controller.getVector(), controller.getCaseNumber());
		
		// retrieve the learned model again
		learnedNet = learningKit.getLearnedNet();
		assertNotNull(learnedNet);
		assertTrue(learnedNet.getNodeCount() > 0);		// make sure nodes are present
		assertTrue(learnedNet.getEdges().size() >= newEdges.size());
		
		for (Edge edge : newEdges) {
			Node originInNet = learnedNet.getNode(edge.getOriginNode().getName());
			Node destinationInNet = learnedNet.getNode(edge.getDestinationNode().getName());
			
			assertTrue(edge.toString(), learnedNet.hasEdge(originInNet, destinationInNet) > 0);
			
		}
		
		
		// save network 
		outputFile.delete();	// delete old file
		new CountCompatibleNetIO().save(outputFile, learnedNet);
		assertTrue(outputFile.exists());
	}
	
	/**
	 * Test method for {@link unbbayes.learning.LearningToolkit}
	 * with arcs added programatically before parameter learning.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InvalidParentException 
	 */
	@Test
	public final void testLearnNetAddArc2Greedy() throws URISyntaxException, IOException, InvalidParentException {
		
		URL url = getClass().getResource("data_freq50_2.txt");
		
		assertNotNull(url);
		File inputFile = new File(url.toURI());
		
//		File tempFile = new File("tempNetFreq50_2_Greedy.net");
		File outputFile = new File("learnedDBNFreq50_2_Greedy.net");
//		File extentedFile = new File("learnedDBNExtraArcsFreq50_2_Greedy.net");
		
		String prefixNodeFrom = "wPrev_";
		String prefixNodeTo = "wNext_";
		
		// map of regex of names of nodes to add arc. Key is regex of node of origins, values are regex of destination nodes
//		Map<String, Collection<String>> mapExtraArcsRegex = new HashMap<String, Collection<String>>();
//		// include mapping from static nodes to dynamic nodes
//		List<String> destinationRegexList = new ArrayList<String>(2);
//		destinationRegexList.add("wPrev\\_.+");
//		destinationRegexList.add("wNext\\_.+");
//		mapExtraArcsRegex.put("Division", destinationRegexList);
//		mapExtraArcsRegex.put("Age", destinationRegexList);
//		mapExtraArcsRegex.put("Gender", destinationRegexList);
//		mapExtraArcsRegex.put("impul", destinationRegexList);
//		mapExtraArcsRegex.put("consc", destinationRegexList);
//		mapExtraArcsRegex.put("emo", destinationRegexList);
//		mapExtraArcsRegex.put("agree", destinationRegexList);
//		mapExtraArcsRegex.put("stress", destinationRegexList);
//		mapExtraArcsRegex.put("checklink", destinationRegexList);
//		mapExtraArcsRegex.put("privacysetting", destinationRegexList);
//		mapExtraArcsRegex.put("checkhttps", destinationRegexList);
//		mapExtraArcsRegex.put("clickwocheck", destinationRegexList);
//		mapExtraArcsRegex.put("phishbefore", destinationRegexList);
//		mapExtraArcsRegex.put("phishinlast", destinationRegexList);
//		mapExtraArcsRegex.put("loseinfo", destinationRegexList);
//		mapExtraArcsRegex.put("dlmalware", destinationRegexList);
																

		
		assertTrue(inputFile.exists());
		
		// intantiate controller and learning toolkit
		TextModeConstructionController controller = new TextModeConstructionController(inputFile, null);
		TextModeLearningToolkit learningKit = new TextModeLearningToolkit(null, controller.getVariables());
		
		// initialize data in learning toolkit
		learningKit.setData(controller.getVariables(), controller.getMatrix(), controller.getVector(), controller.getCaseNumber(), controller.isCompacted());
		
		// set structure learning parameter
		learningKit.setParadigmAlgorithmMetricParam(PARADIGMALGORITHMMETRICPARAMSGREEDY);
		
		// perform structure learning
		learningKit.buildNet(true);
		
		// retrieve the learned model
		ProbabilisticNetwork learnedNet = learningKit.getLearnedNet();
		
		assertTrue(learnedNet.getEdges().size() > 0);
		System.out.println("Number of learned arcs: " + learnedNet.getEdges().size());
		

		// save network 
//		tempFile.delete();	// delete old file
//		new CountCompatibleNetIO().save(tempFile, learnedNet);
//		assertTrue(tempFile.exists());
		
		// add DBN arcs
		Set<Edge> newEdges = new HashSet<Edge>();
		for (Node node : learnedNet.getNodes()) {
			if (node.getName().startsWith(prefixNodeFrom)) {
				// obtain respective node which differs only by prefix
				String targetNodeName = node.getName().replaceFirst(prefixNodeFrom, prefixNodeTo);
				Node targetNode = learnedNet.getNode(targetNodeName);
				assertNotNull(targetNodeName, targetNode);
				Edge edge = new Edge(node, targetNode);
				learnedNet.addEdge(edge);
				newEdges.add(edge);
			}
		}
		
		System.out.println("Number of arcs added as DBN: " + newEdges.size());
		
		// add arcs from static nodes to dynamic nodes
		// I'm assuming the learning supposedly assumed some ordering of nodes in which 
		// static nodes come before dynamic nodes (so that adding arcs from static to dynamic won't cause cycles).
//		for (Entry<String, Collection<String>> entry : mapExtraArcsRegex.entrySet()) {
//			for (int i = 0; i < learnedNet.getNodeCount(); i++) {
//				// find origin node matching with regex of key
//				Node nodeFrom = learnedNet.getNodeAt(i);
//				if (!nodeFrom.getName().matches(entry.getKey())) {
//					continue;
//				}
//				// find destination nodes matching with regex of values
//				for (String destinationRegex : entry.getValue()) {
//					for (int j = 0; j < learnedNet.getNodeCount(); j++) {
//						Node nodeTo = learnedNet.getNodeAt(j);
//						if (!nodeTo.getName().matches(destinationRegex)) {
//							continue;
//						}
//						
//						if (nodeTo.getParentNodes().contains(nodeFrom)
//								|| nodeFrom.getParentNodes().contains(nodeTo)) {
//							// nodes are connected already. Do not connect again
//							continue;
//						}
//						
//						System.out.println("Number of parents of " + nodeTo + " before adding " + nodeFrom + " as parent: " + nodeTo.getParentNodes().size());
//						
//						Edge edge = new Edge(nodeFrom, nodeTo);
//						learnedNet.addEdge(edge);
//						newEdges.add(edge);
//						
//					}
//				}
//			}
//		}
//
//		System.out.println("Number of arcs added after static to dynamic node connection: " + newEdges.size());
		
		// perform parameter learning
		learningKit.runParameterLearning(controller.getVariables(), controller.getMatrix(),
				controller.getVector(), controller.getCaseNumber());
		
		// retrieve the learned model again
		learnedNet = learningKit.getLearnedNet();
		assertNotNull(learnedNet);
		assertTrue(learnedNet.getNodeCount() > 0);		// make sure nodes are present
		assertTrue(learnedNet.getEdges().size() >= newEdges.size());
		
		for (Edge edge : newEdges) {
			Node originInNet = learnedNet.getNode(edge.getOriginNode().getName());
			Node destinationInNet = learnedNet.getNode(edge.getDestinationNode().getName());
			
			assertTrue(edge.toString(), learnedNet.hasEdge(originInNet, destinationInNet) > 0);
			
		}
		
		
		// save network 
		outputFile.delete();	// delete old file
		new CountCompatibleNetIO().save(outputFile, learnedNet);
		assertTrue(outputFile.exists());
	}
	
	
	/**
	 * Test method for {@link unbbayes.learning.LearningToolkit}
	 * with arcs added programatically before parameter learning.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InvalidParentException 
	 */
	@Test
	public final void testLearnNetAddArcRange() throws URISyntaxException, IOException, InvalidParentException {
		
		URL url = getClass().getResource("data_range50.txt");
		
		assertNotNull(url);
		File inputFile = new File(url.toURI());
		
		File outputFile = new File("learnedNetWithExtraArcsDataRange50.net");

		String prefixNodeFrom = "w1";
		String prefixNodeTo = "w2";
		
		
		assertTrue(inputFile.exists());
		
		// intantiate controller and learning toolkit
		TextModeConstructionController controller = new TextModeConstructionController(inputFile, null);
		TextModeLearningToolkit learningKit = new TextModeLearningToolkit(null, controller.getVariables());
		
		// initialize data in learning toolkit
		learningKit.setData(controller.getVariables(), controller.getMatrix(), controller.getVector(), controller.getCaseNumber(), controller.isCompacted());
		
		// set structure learning parameter
		learningKit.setParadigmAlgorithmMetricParam(PARADIGMALGORITHMMETRICPARAMSGREEDY);
		
		// perform structure learning
		learningKit.buildNet(true);
		
		// retrieve the learned model
		ProbabilisticNetwork learnedNet = learningKit.getLearnedNet();
		
		// add extra arcs
		Set<Edge> newEdges = new HashSet<Edge>();
		for (Node node : learnedNet.getNodes()) {
			if (node.getName().startsWith(prefixNodeFrom)) {
				// obtain respective node which differs only by prefix
				String targetNodeName = node.getName().replaceFirst(prefixNodeFrom, prefixNodeTo);
				Node targetNode = learnedNet.getNode(targetNodeName);
				assertNotNull(targetNodeName, targetNode);
				Edge edge = new Edge(node, targetNode);
				learnedNet.addEdge(edge);
				newEdges.add(edge);
			}
		}

		// perform parameter learning
		learningKit.runParameterLearning(controller.getVariables(), controller.getMatrix(),
				controller.getVector(), controller.getCaseNumber());
		
		// retrieve the learned model again
		learnedNet = learningKit.getLearnedNet();
		assertNotNull(learnedNet);
		assertTrue(learnedNet.getNodeCount() > 0);		// make sure nodes are present
		assertTrue(learnedNet.getEdges().size() >= newEdges.size());
		
		for (Edge edge : newEdges) {
			Node originInNet = learnedNet.getNode(edge.getOriginNode().getName());
			Node destinationInNet = learnedNet.getNode(edge.getDestinationNode().getName());
			
			assertTrue(edge.toString(), learnedNet.hasEdge(originInNet, destinationInNet) > 0);
			
		}
		
		
		// save network 
		outputFile.delete();	// delete old file
		new CountCompatibleNetIO().save(outputFile, learnedNet);
		assertTrue(outputFile.exists());
	}
	
	/**
	 * Test method for {@link unbbayes.learning.LearningToolkit}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	public final void testLearnNet() throws IOException {
		
		File inputFile = new File("examples/Chest_Clinic.txt");
		File outputFile = new File("learnedNet.net");
		
		assertTrue(inputFile.exists());
		
		// intantiate controller and learning toolkit
		TextModeConstructionController controller = new TextModeConstructionController(inputFile, null);
		TextModeLearningToolkit learningKit = new TextModeLearningToolkit(null, controller.getVariables());
		
		// initialize data in learning toolkit
		learningKit.setData(controller.getVariables(), controller.getMatrix(), controller.getVector(), controller.getCaseNumber(), controller.isCompacted());
		
		// set structure learning parameter
		learningKit.setParadigmAlgorithmMetricParam(PARADIGMALGORITHMMETRICPARAMSGREEDY);
		
		// perform structure learning
		learningKit.buildNet(true);

		// perform parameter learning
		learningKit.runParameterLearning(controller.getVariables(), controller.getMatrix(),
				controller.getVector(), controller.getCaseNumber());
		
		// retrieve the learned model
		ProbabilisticNetwork learnedNet = learningKit.getLearnedNet();
		assertNotNull(learnedNet);
		assertTrue(learnedNet.getNodeCount() > 0);		// make sure nodes are present
		assertFalse(learnedNet.getEdges().isEmpty());	// make sure at least 1 arc was created
		
		// save network 
		outputFile.delete();	// delete old file
		new CountCompatibleNetIO().save(outputFile, learnedNet);
		assertTrue(outputFile.exists());
		
	}

	

}
