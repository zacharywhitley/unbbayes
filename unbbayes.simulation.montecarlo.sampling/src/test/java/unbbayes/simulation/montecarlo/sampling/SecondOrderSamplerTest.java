/**
 * 
 */
package unbbayes.simulation.montecarlo.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.gui.LongTaskProgressBar;
import unbbayes.io.CSVMultiEvidenceIO;
import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.simulation.montecarlo.io.MonteCarloIO;

/**
 * Test case for {@link IterativeSecondOrderJunctionTreeSampler}
 * and {@link SecondOrderMonteCarloSampling}
 * @author Shou Matsumoto
 *
 */
public class SecondOrderSamplerTest {

	public SecondOrderSamplerTest() {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	

	/**
	 * This is to make sure that exceptions in AWT thread are properly caught
	 * and asserted as a failure in JUnit.
	 * 
	 * @author Shou Matsumoto
	 */
	class AWTExceptionHandlerTester implements Thread.UncaughtExceptionHandler {

		private Thread mainThread = null;

		public AWTExceptionHandlerTester(Thread currentThread) {
			this.mainThread = currentThread;
		}

		public void uncaughtException(Thread awtThread, Throwable e) {
			try {
				e.printStackTrace();
				// force main thread to throw interrupt exception
				mainThread.interrupt();
				// just to make sure JUnit is also notified about the error...
				fail(e.getMessage());
			} catch (Throwable tt) {
				// Don't throw another exception. Will cause infinite loop.
				tt.printStackTrace();
			}
		}

	}
	
	/**
	 * Test method for {@link unbbayes.simulation.montecarlo.sampling.IterativeSecondOrderJunctionTreeSampler#start(unbbayes.prs.bn.ProbabilisticNetwork, int)}.
	 * Only one sample will be generated for each evidence read from a file.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public final void testJTStartWithFileEvidence() throws IOException, InterruptedException, URISyntaxException {
		
		// files to read
		URL netURL = getClass().getResource("./learnedNetWithExtraArcsDataFreq50.net");
		assertNotNull(netURL);
		URL evidenceURL = getClass().getResource("./testEvidence.txt");
		assertNotNull(evidenceURL);
		
		File outFile = new File("samples_Freq50_DBN_" + System.currentTimeMillis() + ".txt");
		outFile.delete();
		
		// load net
		final ProbabilisticNetwork net = (ProbabilisticNetwork) 
				new CountCompatibleNetIO().load(new File(netURL.toURI()));
		assertNotNull(net);
		assertNotNull(net.getNodes());
		assertEquals(48, net.getNodes().size());
		
		// the sampler to test
		final IterativeSecondOrderJunctionTreeSampler sampler = new IterativeSecondOrderJunctionTreeSampler();
		assertNotNull(sampler.getAlgorithm());
		sampler.getAlgorithm().setNet(net);
		
		// read evidence file;
		CSVMultiEvidenceIO evidenceIO = new CSVMultiEvidenceIO();
        evidenceIO.loadEvidences(new File(evidenceURL.toURI()), net);
        List<Map<String, Integer>> allEvidences = evidenceIO.getLastEvidencesRead();
        assertNotNull(allEvidences);
        
        // iterate on evidences in order to generate sample for each evidence
        byte[][] samplesToSave = new byte[allEvidences.size()][net.getNodeCount()];
        for (int evidenceIndex = 0; evidenceIndex < samplesToSave.length; evidenceIndex++) {
        	Map<String, Integer> evidences  = allEvidences.get(evidenceIndex);
        	// each record is a data entry
        	// Accessing Values by name of the node
        	Map<String, Integer> evidenceBackup = new HashMap<String,Integer>();
        	for (String evidenceNodeName : evidences.keySet()) {
        		
        		// check consistency
        		Node node = net.getNode(evidenceNodeName);
        		assertNotNull(evidenceNodeName + ", row " + evidenceIndex, node);
        		
        		// extract the state of the evidence
        		Integer evidenceState = evidences.get(evidenceNodeName);
        		assertNotNull(evidenceNodeName + ", row " + evidenceIndex, evidenceState);
        		assertTrue(evidenceNodeName + ", row " + evidenceIndex, evidenceState >= 0);
        		
        		// insert the evidence
        		assertTrue(evidenceNodeName + ", row " + evidenceIndex, node instanceof ProbabilisticNode);
        		((ProbabilisticNode)node).initMarginalList();
        		((ProbabilisticNode)node).addFinding(evidenceState);
        		evidenceBackup.put(node.getName(), evidenceState);
        		
        		// make sure evidence was inserted
        		assertTrue(evidenceNodeName + ", row " + evidenceIndex, ((ProbabilisticNode)node).hasEvidence());
        		assertEquals(evidenceNodeName + ", row " + evidenceIndex, evidenceState.intValue(), ((ProbabilisticNode)node).getEvidence());
        		
			}
        	
        	// sample 1 row with current evidence
        	sampler.start(net, 1);
        	
        	// extract the generated single sample
        	byte[][] singleSample = sampler.getSampledStatesMatrix();
        	assertNotNull("row = " + evidenceIndex, singleSample);
        	assertEquals("row = " + evidenceIndex, 1, singleSample.length);
        	assertEquals("row = " + evidenceIndex, net.getNodeCount(), singleSample[0].length);
        	
        	// store the current sample
        	for (int nodeIndex = 0; nodeIndex < singleSample[0].length; nodeIndex++) {
        		samplesToSave[evidenceIndex][nodeIndex] = singleSample[0][nodeIndex];
			}

    		// make sure marginals add up to 1
    		for (Node node : net.getNodes()) {
    			if (!(node instanceof ProbabilisticNode)) {
    				continue;
    			}
    			
    			float sum = 0;
    			for (int i = 0; i < node.getStatesSize(); i++) {
    				sum += ((ProbabilisticNode)node).getMarginalAt(i);
    			}
    			
    			assertEquals(node.toString(), 1f, sum, 0.00005);
    			
    		}
        	
        	// make sure all evidence nodes have evidences;
    		for (Entry<String, Integer> entry : evidenceBackup.entrySet()) {
				
    			Node evidenceNode = net.getNode(entry.getKey());
				
				assertTrue(evidenceNode.toString(), evidenceNode instanceof ProbabilisticNode);
    			assertTrue(((ProbabilisticNode)evidenceNode).hasEvidence());
    			assertEquals(evidenceNode.toString(), entry.getValue().intValue(), ((ProbabilisticNode)evidenceNode).getEvidence());
			}
        	
        	
        	// don't use same evidence in next iteration
        	sampler.getAlgorithm().resetEvidences(net);
        
        }	// end of loop for CSV
        
		
		// store all generated samples
        MonteCarloIO io = new MonteCarloIO(samplesToSave);
        io.setFile(outFile);
        io.makeFile(sampler.getSamplingNodeOrderQueue());
        assertTrue(outFile.exists());
		
	}
	
	/**
	 * Test method for {@link SecondOrderMonteCarloSampling#start(ProbabilisticNetwork, int, long)}.
	 * Only one sample will be generated for each evidence read from a file.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@SuppressWarnings("deprecation")
	@Test
	public final void testMCStartWithFileEvidence2Greedy() throws IOException, InterruptedException, URISyntaxException {
		
		// number of outputs to generate
		int numResults = 10;
		
		// files to read
		URL netURL = getClass().getResource("./learnedDBNFreq50_2_K2[-1.5].net");
		assertNotNull(netURL);
		URL evidenceURL = getClass().getResource("./testEvidence2.txt");
		assertNotNull(evidenceURL);
		
		
		// load net
		final ProbabilisticNetwork net = (ProbabilisticNetwork) 
				new CountCompatibleNetIO().load(new File(netURL.toURI()));
		assertNotNull(net);
		assertNotNull(net.getNodes());
		assertEquals(54, net.getNodes().size());	// 55 vars minus the uid
		
		// the sampler to test
		SecondOrderMonteCarloSampling sampler = new SecondOrderMonteCarloSampling();
		
		// generate multiple results
		for (int iteration = 0; iteration < numResults; iteration++) {
			File outFile = new File("[" + System.currentTimeMillis() + "]samples_Freq50_DBN_Greedy_" + iteration + ".txt");
			outFile.delete();
			
			// read evidence file;
			CSVMultiEvidenceIO evidenceIO = new CSVMultiEvidenceIO();
			evidenceIO.loadEvidences(new File(evidenceURL.toURI()), net);
			List<Map<String, Integer>> allEvidences = evidenceIO.getLastEvidencesRead();
			assertNotNull(allEvidences);
			
			// iterate on evidences in order to generate sample for each evidence
			byte[][] samplesToSave = new byte[allEvidences.size()][net.getNodeCount()];
			for (int evidenceIndex = 0; evidenceIndex < samplesToSave.length; evidenceIndex++) {
				Map<String, Integer> evidences  = allEvidences.get(evidenceIndex);
				// each record is a data entry
				// Accessing Values by name of the node
				Map<String, Integer> evidenceBackup = new HashMap<String,Integer>();
				for (String evidenceNodeName : evidences.keySet()) {
					
					// check consistency
					Node node = net.getNode(evidenceNodeName);
					assertNotNull(evidenceNodeName + ", row " + evidenceIndex, node);
					
					// extract the state of the evidence
					Integer evidenceState = evidences.get(evidenceNodeName);
					assertNotNull(evidenceNodeName + ", row " + evidenceIndex, evidenceState);
					assertTrue(evidenceNodeName + ", row " + evidenceIndex, evidenceState >= 0);
					
					// insert the evidence
					assertTrue(evidenceNodeName + ", row " + evidenceIndex, node instanceof ProbabilisticNode);
					((ProbabilisticNode)node).initMarginalList();
					((ProbabilisticNode)node).addFinding(evidenceState);
					evidenceBackup.put(node.getName(), evidenceState);
					
					// make sure evidence was inserted
					assertTrue(evidenceNodeName + ", row " + evidenceIndex, ((ProbabilisticNode)node).hasEvidence());
					assertEquals(evidenceNodeName + ", row " + evidenceIndex, evidenceState.intValue(), ((ProbabilisticNode)node).getEvidence());
					
				}
				
				// sample 1 row with current evidence
				sampler.start(net, 1, Long.MAX_VALUE);
				
				// extract the generated single sample
				byte[][] singleSample = sampler.getSampledStatesMatrix();
				assertNotNull("row = " + evidenceIndex, singleSample);
				assertEquals("row = " + evidenceIndex, 1, singleSample.length);
				assertEquals("row = " + evidenceIndex, net.getNodeCount(), singleSample[0].length);
				
				// store the current sample
				for (int nodeIndex = 0; nodeIndex < singleSample[0].length; nodeIndex++) {
					samplesToSave[evidenceIndex][nodeIndex] = singleSample[0][nodeIndex];
				}
				
				
				// make sure all evidence nodes have evidences;
				for (Entry<String, Integer> entry : evidenceBackup.entrySet()) {
					
					Node evidenceNode = net.getNode(entry.getKey());
					
					assertTrue(evidenceNode.toString(), evidenceNode instanceof ProbabilisticNode);
					assertTrue(((ProbabilisticNode)evidenceNode).hasEvidence());
					assertEquals(evidenceNode.toString(), entry.getValue().intValue(), ((ProbabilisticNode)evidenceNode).getEvidence());
				}
				
				
				// don't use same evidence in next iteration
				net.resetEvidences();
				for (Node node : net.getNodes()) {
					assertTrue(node.toString(), !((TreeVariable)node).hasEvidence());
				}
				
			}	// end of loop for CSV
			
			
			// store all generated samples
			MonteCarloIO io = new MonteCarloIO(samplesToSave);
			io.setFile(outFile);
			io.makeFile(sampler.getSamplingNodeOrderQueue());
			assertTrue(outFile.exists());
			
		}	// end of iteration for each output file
		
		
	}
	
	
	
	/**
	 * Test method for {@link unbbayes.simulation.montecarlo.sampling.IterativeSecondOrderJunctionTreeSampler#start(unbbayes.prs.bn.ProbabilisticNetwork, int)}.
	 * Only one sample will be generated for each evidence read from a file.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public final void testJTStartWithFileEvidenceRange() throws IOException, InterruptedException, URISyntaxException {
		
		// files to read
		URL netURL = getClass().getResource("./learnedNetWithExtraArcsDataRange50.net");
		assertNotNull(netURL);
		URL evidenceURL = getClass().getResource("./testEvidence.txt");
		assertNotNull(evidenceURL);
		
		File outFile = new File("samples_Range50_DBN_" + System.currentTimeMillis() + ".txt");
		outFile.delete();
		
		// load net
		final ProbabilisticNetwork net = (ProbabilisticNetwork) 
				new CountCompatibleNetIO().load(new File(netURL.toURI()));
		assertNotNull(net);
		assertNotNull(net.getNodes());
		assertEquals(48, net.getNodes().size());
		
		// the sampler to test
		final IterativeSecondOrderJunctionTreeSampler sampler = new IterativeSecondOrderJunctionTreeSampler();
		assertNotNull(sampler.getAlgorithm());
		sampler.getAlgorithm().setNet(net);
		
		// read evidence file;
		CSVMultiEvidenceIO evidenceIO = new CSVMultiEvidenceIO();
		evidenceIO.loadEvidences(new File(evidenceURL.toURI()), net);
		List<Map<String, Integer>> allEvidences = evidenceIO.getLastEvidencesRead();
		assertNotNull(allEvidences);
		
		// iterate on evidences in order to generate sample for each evidence
		byte[][] samplesToSave = new byte[allEvidences.size()][net.getNodeCount()];
		for (int evidenceIndex = 0; evidenceIndex < samplesToSave.length; evidenceIndex++) {
			Map<String, Integer> evidences  = allEvidences.get(evidenceIndex);
			// each record is a data entry
			// Accessing Values by name of the node
			Map<String, Integer> evidenceBackup = new HashMap<String,Integer>();
			for (String evidenceNodeName : evidences.keySet()) {
				
				// check consistency
				Node node = net.getNode(evidenceNodeName);
				assertNotNull(evidenceNodeName + ", row " + evidenceIndex, node);
				
				// extract the state of the evidence
				Integer evidenceState = evidences.get(evidenceNodeName);
				assertNotNull(evidenceNodeName + ", row " + evidenceIndex, evidenceState);
				assertTrue(evidenceNodeName + ", row " + evidenceIndex, evidenceState >= 0);
				
				// insert the evidence
				assertTrue(evidenceNodeName + ", row " + evidenceIndex, node instanceof ProbabilisticNode);
				((ProbabilisticNode)node).initMarginalList();
				((ProbabilisticNode)node).addFinding(evidenceState);
				evidenceBackup.put(node.getName(), evidenceState);
				
				// make sure evidence was inserted
				assertTrue(evidenceNodeName + ", row " + evidenceIndex, ((ProbabilisticNode)node).hasEvidence());
				assertEquals(evidenceNodeName + ", row " + evidenceIndex, evidenceState.intValue(), ((ProbabilisticNode)node).getEvidence());
				
			}
			
			// sample 1 row with current evidence
			sampler.start(net, 1);
			
			// extract the generated single sample
			byte[][] singleSample = sampler.getSampledStatesMatrix();
			assertNotNull("row = " + evidenceIndex, singleSample);
			assertEquals("row = " + evidenceIndex, 1, singleSample.length);
			assertEquals("row = " + evidenceIndex, net.getNodeCount(), singleSample[0].length);
			
			// store the current sample
			for (int nodeIndex = 0; nodeIndex < singleSample[0].length; nodeIndex++) {
				samplesToSave[evidenceIndex][nodeIndex] = singleSample[0][nodeIndex];
			}
			
			// make sure marginals add up to 1
			for (Node node : net.getNodes()) {
				if (!(node instanceof ProbabilisticNode)) {
					continue;
				}
				
				float sum = 0;
				for (int i = 0; i < node.getStatesSize(); i++) {
					sum += ((ProbabilisticNode)node).getMarginalAt(i);
				}
				
				assertEquals(node.toString(), 1f, sum, 0.00005);
				
			}
			
			// make sure all evidence nodes have evidences;
			for (Entry<String, Integer> entry : evidenceBackup.entrySet()) {
				
				Node evidenceNode = net.getNode(entry.getKey());
				
				assertTrue(evidenceNode.toString(), evidenceNode instanceof ProbabilisticNode);
				assertTrue(((ProbabilisticNode)evidenceNode).hasEvidence());
				assertEquals(evidenceNode.toString(), entry.getValue().intValue(), ((ProbabilisticNode)evidenceNode).getEvidence());
			}
			
			
			// don't use same evidence in next iteration
			sampler.getAlgorithm().resetEvidences(net);
			
		}	// end of loop for CSV
		
		
		// store all generated samples
		MonteCarloIO io = new MonteCarloIO(samplesToSave);
		io.setFile(outFile);
		io.makeFile(sampler.getSamplingNodeOrderQueue());
		assertTrue(outFile.exists());
		
	}

	/**
	 * Test method for {@link unbbayes.simulation.montecarlo.sampling.IterativeSecondOrderJunctionTreeSampler#start(unbbayes.prs.bn.ProbabilisticNetwork, int)}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public final void testStartWithBaseEvidence() throws IOException, InterruptedException, URISyntaxException {

		boolean isToSaveSamples = false;
		if (System.getenv("eclipse_IDE") != null
				&& "true".equalsIgnoreCase(System.getenv("eclipse_IDE").trim())) {
	        isToSaveSamples = true;
	        new File("samples.txt").delete();
	    }
		
		// file to read
		URL resourceURL = getClass().getResource("./Chest_Clinic_Learned_Dirichlet.net");
		assertNotNull(resourceURL);
		
		// load net
		final ProbabilisticNetwork net = (ProbabilisticNetwork) 
				new CountCompatibleNetIO().load(new File(resourceURL.toURI()));
		assertNotNull(net);
		assertNotNull(net.getNodes());
		assertEquals(8, net.getNodes().size());
		
		// the sampler to test
		final IterativeSecondOrderJunctionTreeSampler sampler = new IterativeSecondOrderJunctionTreeSampler();
		assertNotNull(sampler.getAlgorithm());
		sampler.getAlgorithm().setNet(net);
		
		ProbabilisticNode evidenceNode = (ProbabilisticNode) net.getNode("Asia");
		assertNotNull(evidenceNode);
		int evidenceState = 1;
		evidenceNode.initMarginalList();
		evidenceNode.addFinding(evidenceState);	// evidence: Visit
		assertTrue(evidenceNode.hasEvidence());
		assertEquals(evidenceState, evidenceNode.getEvidence());
		
		int numSamples = 10;
		sampler.start(net, numSamples );
		
		// make sure marginals add up to 1
		for (Node node : net.getNodes()) {
			if (!(node instanceof ProbabilisticNode)) {
				continue;
			}
			
			float sum = 0;
			for (int i = 0; i < node.getStatesSize(); i++) {
				sum += ((ProbabilisticNode)node).getMarginalAt(i);
			}
			
			assertEquals(node.toString(), 1f, sum, 0.00005);
			
		}
		
		assertTrue(evidenceNode.hasEvidence());
		assertEquals(evidenceState, evidenceNode.getEvidence());
		
		int evidenceNodeIndex = sampler.getSamplingNodeOrderQueue().indexOf(evidenceNode);
		assertEquals(net.getNodeCount(), sampler.getSamplingNodeOrderQueue().size());
		assertEquals(net.getNodes().indexOf(evidenceNode), evidenceNodeIndex);
		
		byte[][] samples = sampler.getSampledStatesMatrix();
		assertEquals(numSamples, samples.length);
		assertEquals(net.getNodeCount(), samples[0].length);
		
		for (int row = 0; row < samples.length; row++) {
			assertEquals("Row = " + row, evidenceState, samples[row][evidenceNodeIndex]);
		}
		
		// store samples
		if (isToSaveSamples) {
			try {
				MonteCarloIO io = new MonteCarloIO(sampler.getSampledStatesMatrix());
				io.setFile(new File("samples.txt"));
				io.makeFile(sampler.getSamplingNodeOrderQueue());
				assertTrue(new File("samples.txt").exists());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	
	/**
	 * Test method for 
	 * {@link unbbayes.simulation.montecarlo.sampling.IterativeSecondOrderJunctionTreeSampler#start(unbbayes.prs.bn.ProbabilisticNetwork, int)}
	 * running in a thread.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public final void testThread() throws IOException, InterruptedException, URISyntaxException {
		

		final boolean isToShowProgress = (System.getenv("eclipse_IDE") != null
				&& "true".equalsIgnoreCase(System.getenv("eclipse_IDE").trim()));
		
		new File("testSamplesThread.txt").delete();
		
		// file to read
		URL resourceURL = getClass().getResource("./Chest_Clinic_Learned_Dirichlet.net");
		assertNotNull(resourceURL);
		
		// load net
		final ProbabilisticNetwork net = (ProbabilisticNetwork) 
				new CountCompatibleNetIO().load(new File(resourceURL.toURI()));
		assertNotNull(net);
		assertNotNull(net.getNodes());
		assertEquals(8, net.getNodes().size());
		
		// the sampler to test
		final IterativeSecondOrderJunctionTreeSampler sampler = new IterativeSecondOrderJunctionTreeSampler();
		assertNotNull(sampler.getAlgorithm());
		sampler.getAlgorithm().setNet(net);

		// Start progress bar
    	final LongTaskProgressBar progressBar = new LongTaskProgressBar("Sampling Test", true);
    	
    	// Register progress bar as observer of the long task mc
    	sampler.registerObserver(progressBar);
    	

		// make sure AWT exceptions are caught in JUnit
		Thread.setDefaultUncaughtExceptionHandler(new AWTExceptionHandlerTester(
				Thread.currentThread()));
		System.setProperty("sun.awt.exception.handler",
				AWTExceptionHandlerTester.class.getName());
    	
		// Add thread to progress bar to allow canceling the operation
    	Thread thread = new Thread(new Runnable() {
			public void run() {
				if (isToShowProgress) {
					progressBar.showProgressBar();
				} else {
					progressBar.hideProgressBar();
				}
				
				sampler.start(net, 200);
				
				// Hides the frame of the progress bar
				progressBar.hideProgressBar();
				// store samples
				try {
					MonteCarloIO io = new MonteCarloIO(sampler.getSampledStatesMatrix());
					io.setFile(new File("testSamplesThread.txt"));
					io.makeFile(sampler.getSamplingNodeOrderQueue());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
    	
    	progressBar.setThread(thread);
    	if (isToShowProgress) {
    		progressBar.showProgressBar();
    	} else {
    		progressBar.hideProgressBar();
    	}
    	
    	thread.start();
		thread.join();
		
		assertTrue(new File("testSamplesThread.txt").exists());
	}

}
