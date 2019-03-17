/**
 * 
 */
package unbbayes.simulation.montecarlo.sampling;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import unbbayes.gui.LongTaskProgressBar;
import unbbayes.io.CountCompatibleNetIO;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.simulation.montecarlo.io.MonteCarloIO;

/**
 * @author Shou Matsumoto
 *
 */
public class IterativeSecondOrderJunctionTreeSamplerTest {

	public IterativeSecondOrderJunctionTreeSamplerTest() {
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
				
				sampler.start(net, 1000);
				
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
