package edu.gmu.ace.scicast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import unbbayes.util.Debug;

public class MarkovEngineMinorPerformanceTest extends TestCase {

	private MarkovEngineImpl engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance();


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		Debug.setDebug(true);
		engine.initialize();
	}
	
	public MarkovEngineMinorPerformanceTest(String name) {
		super(name);
	}
	
	/**
	 * Reads a state file and checks for consistency
	 * @throws IOException
	 * @throws URISyntaxException
	 */
//	public final void test1500NodeStateLoading() throws IOException, URISyntaxException  {
//		
//		// read the zipped base64 network state file
//		File stateFile = new File(getClass().getResource("/unbbayes.state").toURI());
//		BufferedReader reader = new BufferedReader(new FileReader(stateFile));
//		String netStringZipBase64 = "";
//		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//			netStringZipBase64 += line;
//		}
//		reader.close();
//		assertFalse(netStringZipBase64.isEmpty());
//		
//		
//		// This stream will read a file input stream, and then unzip it
//		ZipInputStream zipInputStream = new ZipInputStream(new Base64InputStream(new FileInputStream(stateFile)));
//		// move cursor to 1st zipped entry  
//		zipInputStream.getNextEntry();
//		// Convert stream to reader, and wraps with buffer
//		reader = new BufferedReader(new InputStreamReader(zipInputStream));
//		
//		File netFile = File.createTempFile("Temp_", ".net", stateFile.getParentFile());
//		netFile.deleteOnExit();
//		
//		PrintStream writer = new PrintStream(netFile);
//		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//			writer.println(line);
//		}
//		writer.close();
//		reader.close();
//		
//		assertTrue(netFile.exists());
//		
//		// import the network to the engine
//		engine.importState(netStringZipBase64);
//		
//		// check if statistics match with expected
//		NetStatistics statistics = engine.getNetStatistics();
//		assertNotNull(statistics);
//		
//		Map<Integer, Integer> numStatesMap = statistics.getNumberOfStatesToNumberOfNodesMap();
//		assertEquals(6, numStatesMap.size());
//		assertEquals(1458, numStatesMap.get(2).intValue());
//		assertEquals(8, numStatesMap.get(4).intValue());
//		assertEquals(1, numStatesMap.get(6).intValue());
//		assertEquals(3, numStatesMap.get(7).intValue());
//		assertEquals(1, numStatesMap.get(12).intValue());
//		assertEquals(1, numStatesMap.get(15).intValue());
//		
//		assertEquals(1971110, statistics.getSumOfCliqueTableSizes());
//		assertEquals(2356670, statistics.getSumOfCliqueAndSeparatorTableSizes());
//		assertEquals(1971110, statistics.getSumOfCliqueTableSizesWithoutResolvedCliques());
//		assertEquals(2356670, statistics.getSumOfCliqueAndSeparatorTableSizesWithoutResolvedCliques());
//		assertEquals(1585318, statistics.getDegreeOfFreedom());
//		assertEquals(1466, statistics.getNumberOfCliques());
//		assertEquals(1465, statistics.getNumberOfSeparators());
//		assertEquals(493920, statistics.getMaxCliqueTableSize());
//		assertEquals(1466, statistics.getNumberOfNonEmptyCliques());
//		assertEquals(6, statistics.getMaxNumParents());
//		assertEquals(2490, statistics.getNumArcs());
//		
//		// 'maxNumParents': 6, 'numArcs': 2490, 'isRunningApproximation': False
//		
//		// 2 * 15 * 12 * 7 * 7 * 7 * 4
//		// 2 * 15 * (6*2) * 7 * 7 * 7 * (2*2)
////		int maxCliqueSize = 493920;	
//		
//		
//	}

	/**
	 * Check that performance of JT propagation is OK in completely disconnected but sufficiently large network.
	 */
	public final void testDisconnectedNetTradePerformance()  {
		
		engine.initialize();
		
		long seed = System.currentTimeMillis();
		
		// build 1400 binary nodes with new (2-5) parents (try to limit joint state to 168 states at most)
		long transactionKey = engine.startNetworkActions();
		assertEquals("Seed = " + seed, 1L, transactionKey);
		
		
		// create the 1400 binary nodes
		for (int questionId = 1; questionId <= 1400; questionId++) {
			assertTrue("Seed = " + seed + ", questionId = " + questionId, engine.addQuestion(transactionKey, new Date(), questionId, 2, null));
		}
		
		// create extra 5 non-binary nodes
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-3, 3, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-5, 5, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-7, 7, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-11, 11, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-13, 13, null));
		
		// commit questions
		assertTrue("Seed = " + seed, engine.commitNetworkActions(transactionKey));
		
		
		// do 1400 trades (1 in each binary node)
		transactionKey = engine.startNetworkActions();
		assertEquals("Seed = " + seed, 2L, transactionKey);

		// create the 1400 binary nodes
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.9f);
		newValues.add(0.1f);
		for (int questionId = 1; questionId <= 1400; questionId++) {
			List<Float> ret = engine.addTrade(transactionKey, new Date(), questionId, null, null, newValues, null, null);
			assertNotNull("Seed = " + seed + ", questionId = " + questionId, ret);
			assertEquals("Seed = " + seed + ", questionId = " + questionId, 2, ret.size());
		}
		
		// commit trades
		assertTrue("Seed = " + seed, engine.commitNetworkActions(transactionKey));
		
		// check marginals
		for (int questionId = 1; questionId <= 1400; questionId++) {
			List<Float> ret = engine.getProbList(questionId, null, null);
			assertNotNull("Seed = " + seed + ", questionId = " + questionId, ret);
			assertEquals("Seed = " + seed + ", questionId = " + questionId, newValues.size(), ret.size());
			for (int i = 0; i < ret.size(); i++) {
				assertEquals("Seed = " + seed + ", questionId = " + questionId, newValues.get(i), ret.get(i), 0.00005f);
			}
		}
		
		// check network & clique status
		NetStatistics statistics = engine.getNetStatistics();
//		System.out.println(statistics.toString());
		assertFalse("Seed = " + seed, statistics.isRunningApproximation());
		assertEquals("Seed = " + seed, 13, statistics.getMaxCliqueTableSize());
		assertEquals("Seed = " + seed, 0, statistics.getMaxNumParents());
		assertEquals("Seed = " + seed, 0, statistics.getNumArcs());
		assertEquals("Seed = " + seed, 1400 + 5, statistics.getNumberOfNonEmptyCliques());
		assertEquals("Seed = " + seed, 1400*2 + 3 + 5 + 7 + 11 + 13, statistics.getSumOfCliqueTableSizes());
		
		
		
	}
	
	/**
	 * This is similar to {@link #testDisconnectedNetTradePerformance()}, but some nodes will be fully connected,
	 * but trades will be made on disconnected nodes only.
	 */
	public final void testDisconnectedCliqueTradePerformance()  {
		
		engine.initialize();
		
		long seed = System.currentTimeMillis();
		
		// build 1400 binary nodes with new (2-5) parents (try to limit joint state to 168 states at most)
		long transactionKey = engine.startNetworkActions();
		assertEquals("Seed = " + seed, 1L, transactionKey);
		
		
		// create the 1400 binary nodes
		for (int questionId = 1; questionId <= 1400; questionId++) {
			assertTrue("Seed = " + seed + ", questionId = " + questionId, engine.addQuestion(transactionKey, new Date(), questionId, 2, null));
		}
		
		// create extra non-binary nodes
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-3, 3, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-5, 5, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-7, 7, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-11, 11, null));
		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-13, 13, null));
//		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-17, 17, null));
//		assertTrue("Seed = " + seed, engine.addQuestion(transactionKey, new Date(), Integer.MAX_VALUE-19, 19, null));
		
		// connect the above 5 nodes
		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-5L), null));
		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-7L), null));
		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-11L), null));
		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-13L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-17L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-3, Collections.singletonList(Integer.MAX_VALUE-19L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-5, Collections.singletonList(Integer.MAX_VALUE-7L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-5, Collections.singletonList(Integer.MAX_VALUE-11L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-5, Collections.singletonList(Integer.MAX_VALUE-13L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-5, Collections.singletonList(Integer.MAX_VALUE-17L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-5, Collections.singletonList(Integer.MAX_VALUE-19L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-7, Collections.singletonList(Integer.MAX_VALUE-11L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-7, Collections.singletonList(Integer.MAX_VALUE-13L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-7, Collections.singletonList(Integer.MAX_VALUE-17L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-7, Collections.singletonList(Integer.MAX_VALUE-19L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-11, Collections.singletonList(Integer.MAX_VALUE-13L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-11, Collections.singletonList(Integer.MAX_VALUE-17L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-11, Collections.singletonList(Integer.MAX_VALUE-19L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-13, Collections.singletonList(Integer.MAX_VALUE-17L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-13, Collections.singletonList(Integer.MAX_VALUE-19L), null));
//		assertTrue("Seed = " + seed, engine.addQuestionAssumption(transactionKey, new Date(), Integer.MAX_VALUE-17, Collections.singletonList(Integer.MAX_VALUE-19L), null));
		
		// commit questions
		assertTrue("Seed = " + seed, engine.commitNetworkActions(transactionKey));
		
		
		// do 1400 trades (1 in each binary node)
		transactionKey = engine.startNetworkActions();
		assertEquals("Seed = " + seed, 2L, transactionKey);
		
		// trade on the connected node
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.9f);
		newValues.add(0.1f);
		newValues.add(0f);
		engine.addTrade(transactionKey, new Date(), Integer.MAX_VALUE-3, null, null, newValues, null, null);
		
		// trade on the 1400 binary nodes
		newValues = new ArrayList<Float>(3);
		newValues.add(0.9f);
		newValues.add(0.1f);
		for (int questionId = 1; questionId <= 1400; questionId++) {
			List<Float> ret = engine.addTrade(transactionKey, new Date(), questionId, null, null, newValues, null, null);
			assertNotNull("Seed = " + seed + ", questionId = " + questionId, ret);
			assertEquals("Seed = " + seed + ", questionId = " + questionId, 2, ret.size());
		}
		
		// commit trades
		assertTrue("Seed = " + seed, engine.commitNetworkActions(transactionKey));
		
		// check marginals
		for (int questionId = 1; questionId <= 1400; questionId++) {
			List<Float> ret = engine.getProbList(questionId, null, null);
			assertNotNull("Seed = " + seed + ", questionId = " + questionId, ret);
			assertEquals("Seed = " + seed + ", questionId = " + questionId, newValues.size(), ret.size());
			for (int i = 0; i < ret.size(); i++) {
				assertEquals("Seed = " + seed + ", questionId = " + questionId, newValues.get(i), ret.get(i), 0.00005f);
			}
		}
		
		
		// check network & clique status
		NetStatistics statistics = engine.getNetStatistics();
//		System.out.println(statistics.toString());
		assertFalse("Seed = " + seed, statistics.isRunningApproximation());
//		assertEquals("Seed = " + seed, 3*5*7*11*13*17*19, statistics.getMaxCliqueTableSize());
		assertEquals("Seed = " + seed, 3*5*7*11*13, statistics.getMaxCliqueTableSize());
//		assertEquals("Seed = " + seed, 6, statistics.getMaxNumParents());
		assertEquals("Seed = " + seed, 4, statistics.getMaxNumParents());
//		assertEquals("Seed = " + seed, 6+5+4+3+2+1, statistics.getNumArcs());
		assertEquals("Seed = " + seed, 4, statistics.getNumArcs());
		assertEquals("Seed = " + seed, 1400 + 1, statistics.getNumberOfNonEmptyCliques());
//		assertEquals("Seed = " + seed, 1400*2 + 3*5*7*11*13*17*19, statistics.getSumOfCliqueTableSizes());
		assertEquals("Seed = " + seed, 1400*2 + 3*5*7*11*13, statistics.getSumOfCliqueTableSizes());
		
		
		
	}

}
