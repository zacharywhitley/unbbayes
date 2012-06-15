/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineTest extends TestCase {
	
	private static final int THREAD_NUM = 100;	// quantity of threads to use in order to test multi-thread behavior

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
		
        // assert actions were added in order of date
        List<NetworkAction> actions = engine.getNetworkActionsMap().get(transactionKey);
        for (int i = 1; i < actions.size(); i++) {
        	// it is inconsistent if previous action was created afer next action.
			assertFalse(actions.get(i-1).getWhenCreated().after(actions.get(i).getWhenCreated()));
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

	/**created just to represent a BN edge using only IDs*/
	private class QuestionPair{
		public final Long right;
		public final Long left;
		public QuestionPair (Long left, Long right) {
			this.left = left;
			this.right = right;
			
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return new Integer(right.hashCode() + left.hashCode()).hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj instanceof QuestionPair) {
				QuestionPair pair = (QuestionPair) obj;
				return this.left.equals(pair.left) && this.right.equals(pair.right);
			}
			return false;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return "(" + this.left + "," + this.right + ")";
		}
		
	}
	
	/** This was created just to run addQuestionAssumption in multiple threads*/
	private class AddQuestionAssumptionThread extends Thread{
		private Long transactionKey;
		private final Collection<QuestionPair> generatedEdges;
		private final List<Long> generatedNodes;
		private final Map<Long, Integer> parentNumCounters;
		public AddQuestionAssumptionThread(Long transactionKey, List<Long> generatedNodes, Collection<QuestionPair> generatedEdges, Map<Long,Integer> parentNumCounters) {
			super();
			this.transactionKey = transactionKey;
			this.generatedNodes = generatedNodes;
			this.generatedEdges = generatedEdges;
			this.parentNumCounters = parentNumCounters;
		}
		public void run() {
			boolean isToCommitTransaction = false;
			synchronized (generatedNodes) {
				if (transactionKey == null) {
					// if no transaction key was provided, 
					// we shall test several threads in several transactions
					transactionKey = engine.startNetworkActions();
					isToCommitTransaction = true;
				}
				synchronized (parentNumCounters) {
					int overallIterationNum = (int)(5*Math.random() + 1);	// do at least 1 operation
					for (int j = 0; j < overallIterationNum; j++) {
						if (Math.random() < .6) {
							int nodeIterationNum = (int)(5*Math.random());
							for (int i = 0; i < nodeIterationNum; i++) {
								Long nodeID = (long) generatedNodes.size();
								engine.addQuestion(
										transactionKey, 
										new Date(), 
										nodeID, 
										2 + (int)(5*Math.random()), 
										null
								);
								generatedNodes.add(nodeID);
								parentNumCounters.put(nodeID, 0);
							}
						}
						if (generatedNodes.size() >= 2) {
							if (Math.random() < .5) {
								int edgeIterationNum = (int)(5*Math.random());
								synchronized (generatedEdges) {
									for (int i = 0; i < edgeIterationNum; i++) {
										// randomly pick destination of edge
										long child = (long) (generatedNodes.size() * Math.random());
										// randomly pick origins of edges
										List<Long> parents = new ArrayList<Long>();
										do {
											long destination = (long) (generatedNodes.size() * Math.random());
											if (destination < child) {
												// make sure destination  > origin always, so that loop becomes impossible
												long aux = destination;
												destination = child;
												child = aux;
											} else if (destination == child) {
												continue;	// pick random again
											}
											parents.add(destination);
										} while (parents.isEmpty() || (Math.random() < .7));
										// limit the max quantity of parents to 5
										if (parentNumCounters.get(child) + parents.size() > 5) {
											continue;
										}
										// origin should will always be smaller than anything in destination
										engine.addQuestionAssumption(
												transactionKey, 
												new Date(), 
												child, 
												parents, 
												((Math.random()<.5)?null:new ArrayList<Float>())
										);
										int actuallyAddedParentsCounter = 0;
										for (Long parent : parents) {
											if (generatedEdges.add(new QuestionPair(parent, child))) {
												actuallyAddedParentsCounter++;
											}
										}
										parentNumCounters.put(child, parentNumCounters.get(child) + actuallyAddedParentsCounter);
									}
								}
							}
						}
				}
				}
				if (isToCommitTransaction) {
					// transactionKey was null -> several threads + several transactions case
					engine.commitNetworkActions(transactionKey);
				}
			}
		}
	}
	
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addQuestionAssumption(long, java.util.Date, long, long, java.util.List)}.
	 */
	public final void testAddQuestionAssumption() {
		// initial assertion
		assertNotNull(engine.getInferenceAlgorithm().getNetwork());
		
		// no nodes in network.
		assertEquals(0, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		// no edges in network
		assertEquals(0, engine.getInferenceAlgorithm().getNetwork().getEdges().size());
		
		
		// case 1 : several threads in 1 transaction
		

		// start transaction
		long transactionKey = engine.startNetworkActions();
		
		
		// run addQuestion in THREAD_NUM threads
		AddQuestionAssumptionThread[] threads = new AddQuestionAssumptionThread[THREAD_NUM];
		List<Long> generatedNodes = Collections.synchronizedList(new ArrayList<Long>());
		Set<QuestionPair> generatedEdges = Collections.synchronizedSet(new HashSet<QuestionPair>());
		Map<Long, Integer> parentNumCounters = new ConcurrentHashMap<Long, Integer>();
        for (int i = 0; i < THREAD_NUM; i++) {
            threads[i] = new AddQuestionAssumptionThread(transactionKey, generatedNodes, generatedEdges,parentNumCounters);
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
		
		// check if data structures are synchronized
		assertEquals(parentNumCounters.keySet().size(), generatedNodes.size());
		int parentNumCounterValuesSum = 0;
		for (Integer numParents : parentNumCounters.values()) {
			parentNumCounterValuesSum += numParents;
		}
		assertEquals(parentNumCounterValuesSum, generatedEdges.size());
		
		// check if network contains all nodes and edges
		assertEquals(generatedNodes.size(), engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		assertEquals(generatedEdges.size(), engine.getInferenceAlgorithm().getNetwork().getEdges().size());
		
		for (Long nodeID : generatedNodes) {
			assertEquals(nodeID + " is not present in " + engine.getInferenceAlgorithm().getNetwork(),
					Long.toString(nodeID),
					((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(nodeID)).getName());
		}
		for (QuestionPair pair : generatedEdges) {
			Node node1 = ((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(pair.left));
			Node node2 = ((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(pair.right));
			assertNotNull(pair.left + " is null", node1);
			assertNotNull(pair.right + " is null", node2);
			assertFalse(pair.left + ".equals(" + pair.right+")", node1.equals(node2));
			assertFalse(pair.left + "->" + pair.right + " is not present in " + engine.getInferenceAlgorithm().getNetwork(),
					((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).hasEdge(node1, node2) < 0);
		}
		
		// reset engine
		engine.initialize();
		assertNotNull(engine.getInferenceAlgorithm().getNetwork());
		assertEquals(0, engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		
		
		// case 2 : several transactions, several threads.
		
		// run addQuestion in THREAD_NUM threads
		threads = new AddQuestionAssumptionThread[THREAD_NUM];
		generatedNodes.clear();
		generatedEdges.clear();
        for (int i = 0; i < THREAD_NUM; i++) {
        	// by passing null as transactionKey, AddQuestionThread will call startNetworkActions and commitNetworkActions for each thread
            threads[i] = new AddQuestionAssumptionThread(null, generatedNodes, generatedEdges, parentNumCounters);
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
        
     // check if network contains all nodes and edges
		assertEquals(generatedNodes.size(), engine.getInferenceAlgorithm().getNetwork().getNodeCount());
		assertEquals(generatedEdges.size(), engine.getInferenceAlgorithm().getNetwork().getEdges().size());
		for (Long nodeID : generatedNodes) {
			assertEquals(nodeID + " is not present in " + engine.getInferenceAlgorithm().getNetwork(),
					Long.toString(nodeID),
					((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(nodeID)).getName());
		}
		for (QuestionPair pair : generatedEdges) {
			Node node1 = ((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(pair.left));
			Node node2 = ((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).getNode(Long.toString(pair.right));
			assertNotNull(pair.left + " is null", node1);
			assertNotNull(pair.right + " is null", node2);
			assertFalse(pair.left + ".equals(" + pair.right+")", node1.equals(node2));
			assertFalse(pair.left + "->" + pair.right + " is not present in " + engine.getInferenceAlgorithm().getNetwork(),
					((ProbabilisticNetwork)engine.getInferenceAlgorithm().getNetwork()).hasEdge(node1, node2) < 0);
		}
		
		// case 3 : edges being substituted
		
		
		
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
