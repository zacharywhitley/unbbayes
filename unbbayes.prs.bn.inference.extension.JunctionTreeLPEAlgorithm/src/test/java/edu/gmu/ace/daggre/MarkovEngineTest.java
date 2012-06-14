/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.Date;

import junit.framework.TestCase;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineTest extends TestCase {
	
	private static final int THREAD_NUM = 1000;	// quantity of threads to use in order to test multi-thread behavior

	private MarkovEngineImpl engine = new MarkovEngineImpl();

	/**
	 * @param name
	 */
	public MarkovEngineTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		engine.initialize();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/** This was created just to run addQuestion in multiple threads and same transaction*/
	private class AddQuestionThread extends Thread{
		private final long questionID;
		private Long transactionKey;
		public AddQuestionThread(Long transactionKey, long questionID) {
			super();
			this.questionID = questionID;
			this.transactionKey = transactionKey;
		}
		public void run() {
			boolean isToCommitTransaction = false;
			if (transactionKey == null) {
				// if no transaction key was provided, 
				// we shall test several threads in several transactions
				transactionKey = engine.startNetworkActions();
				isToCommitTransaction = true;
			}
			engine.addQuestion(
					transactionKey, 
					new Date(), 
					questionID, 
					2 + (int)(5*Math.random()), 
					null
				);
			if (isToCommitTransaction) {
				// transactionKey was null -> several threads + several transactions case
				engine.commitNetworkActions(transactionKey);
			}
		}
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addQuestion(long, java.util.Date, long, int, java.util.List)}.
	 */
	public final void testAddQuestion() {
		// initial assertion
		assertNotNull(engine.getInferenceAlgorithm().getNetwork());
		
		// no nodes in network.
		assertEquals(0, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		// case 1 : several threads in 1 transaction
		
		// start transaction
		long transactionKey = engine.startNetworkActions();
		
		// run addQuestion in THREAD_NUM threads
		Thread[] threads = new AddQuestionThread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            threads[i] = new AddQuestionThread(transactionKey, i);
            threads[i].start();
        }
        
        // wait until the threads are finished
        for (int i = 0; i < THREAD_NUM; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            	e.printStackTrace();
                fail(e.getMessage());
            }
        }
		
        // commit transaction
		engine.commitNetworkActions(transactionKey);
		
		// check if network contains THREAD_NUM nodes 
		assertEquals(THREAD_NUM, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		// check if network contains nodes with ID from 0 to THREAD_NUM-1
		for (int i = 0; i < THREAD_NUM; i++) {
			assertNotNull(((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Integer.toString(i)));
		}
		
		// reset engine
		engine.initialize();
		assertNotNull(engine.getInferenceAlgorithm().getNetwork());
		assertEquals(0, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		
		// case 2 : several transactions, several threads.
		// run addQuestion in THREAD_NUM threads
		threads = new AddQuestionThread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
        	// by passing null as transactionKey, AddQuestionThread will call startNetworkActions and commitNetworkActions for each thread
            threads[i] = new AddQuestionThread(null, i);
            threads[i].start();
        }
        
        // wait until the threads are finished
        for (int i = 0; i < THREAD_NUM; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            	e.printStackTrace();
                fail(e.getMessage());
            }
        }
        
     	// check if network contains THREAD_NUM nodes 
		assertEquals(THREAD_NUM, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		// check if network contains nodes with ID from 0 to THREAD_NUM-1
		for (int i = 0; i < THREAD_NUM; i++) {
			assertNotNull(((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Integer.toString(i)));
		}
		
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)}.
	 */
	public final void testAddQuestionAssumption() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addCash(long, java.util.Date, long, float, java.lang.String)}.
	 */
	public final void testAddCash() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)}.
	 */
	public final void testAddTrade() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#resolveQuestion(long, java.util.Date, long, int)}.
	 */
	public final void testResolveQuestion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#revertTrade(long, java.util.Date, java.lang.Long, java.lang.Long)}.
	 */
	public final void testRevertTrade() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getProbList(long, java.util.List, java.util.List)}.
	 */
	public final void testGetProbList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getProbsList(java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testGetProbsList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getPossibleQuestionAssumptions(long, java.util.List)}.
	 */
	public final void testGetPossibleQuestionAssumptions() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getAssetsIfStates(long, long, java.util.List, java.util.List)}.
	 */
	public final void testGetAssetsIfStates() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getEditLimits(long, long, int, java.util.List, java.util.List)}.
	 */
	public final void testGetEditLimits() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getCash(long, java.util.List, java.util.List)}.
	 */
	public final void testGetCash() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#assetsCommittedByUserQuestion(long, long, java.util.List, java.util.List)}.
	 */
	public final void testAssetsCommittedByUserQuestion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#assetsCommittedByUserQuestions(long, java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testAssetsCommittedByUserQuestions() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)}.
	 */
	public final void testScoreUserQuestionEv() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#scoreUser(java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testScoreUser() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#previewTrade(long, long, java.util.List, java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testPreviewTrade() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#determineBalancingTrade(long, long, java.util.List, java.util.List)}.
	 */
	public final void testDetermineBalancingTrade() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)}.
	 */
	public final void testGetQuestionHistory() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getScoreDetails(long, java.util.List, java.util.List)}.
	 */
	public final void testGetScoreDetails() {
		fail("Not yet implemented"); // TODO
	}

}
