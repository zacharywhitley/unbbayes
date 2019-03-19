package unbbayes.learning;

import static org.junit.Assert.*;

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

	
	private String paradigmAlgorithmMetricParams[] = {
			AlgorithmController.PARADIGMS.Ponctuation.name(),	// paradigm of algorithm
			AlgorithmController.SCORING_ALGORITHMS.K2.name(),	// algorithm in paradigm to use
			AlgorithmController.METRICS.MDL.name(),				// metric
			"1"													// special parameter used in algorithm
	};

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
		learningKit.setParadigmAlgorithmMetricParam(paradigmAlgorithmMetricParams);
		
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
	public final void testLearnNetAddArc2() throws URISyntaxException, IOException, InvalidParentException {
		
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
		learningKit.setParadigmAlgorithmMetricParam(paradigmAlgorithmMetricParams);
		
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
		learningKit.setParadigmAlgorithmMetricParam(paradigmAlgorithmMetricParams);
		
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
