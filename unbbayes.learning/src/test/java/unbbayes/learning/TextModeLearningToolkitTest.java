package unbbayes.learning;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class TextModeLearningToolkitTest {

	private File outputFile = new File("learnedNet.net");
	private File inputFile = new File("examples/Chest_Clinic.txt");
//	{
//		URL url = getClass().getResource("data_freq50.txt");
//		assertNotNull(url);
//		try {
//			inputFile = new File(url.toURI());
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//	}
	
	private String paradigmAlgorithmMetricParams[] = {
			AlgorithmController.PARADIGMS.Ponctuation.name(),	// paradigm of algorithm
			AlgorithmController.SCORING_ALGORITHMS.K2.name(),	// algorithm in paradigm to use
			AlgorithmController.METRICS.MDL.name(),				// metric
			"1"													// special parameter used in algorithm
	};

	public TextModeLearningToolkitTest() {}

	/**
	 * Test method for {@link unbbayes.learning.LearningToolkit}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	public final void testLearnNet() throws URISyntaxException, IOException {
		
		
		assertTrue(getInputFile().exists());
		
		// intantiate controller and learning toolkit
		TextModeConstructionController controller = new TextModeConstructionController(getInputFile(), null);
		TextModeLearningToolkit learningKit = new TextModeLearningToolkit(null, controller.getVariables());
		
		// initialize data in learning toolkit
		learningKit.setData(controller.getVariables(), controller.getMatrix(), controller.getVector(), controller.getCaseNumber(), controller.isCompacted());
		
		// set structure learning parameter
		learningKit.setParadigmAlgorithmMetricParam(getParadigmAlgorithmMetricParams());
		
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
		getOutputFile().delete();	// delete old file
		new CountCompatibleNetIO().save(getOutputFile(), learnedNet);
		
		assertTrue(getOutputFile().exists());
	}

	/**
	 * @return the outputFile
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * @param outputFile the outputFile to set
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * @return the inputFile
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * @return the paradigmAlgorithmMetricParams
	 * @see TextModeLearningToolkit#getParadigmAlgorithmMetricParam()
	 */
	public String[] getParadigmAlgorithmMetricParams() {
		return paradigmAlgorithmMetricParams;
	}

	/**
	 * @param paradigmAlgorithmMetricParams the paradigmAlgorithmMetricParams to set
	 * @see TextModeLearningToolkit#setParadigmAlgorithmMetricParam(String[])
	 */
	public void setParadigmAlgorithmMetricParams(
			String paradigmAlgorithmMetricParams[]) {
		this.paradigmAlgorithmMetricParams = paradigmAlgorithmMetricParams;
	}

}
