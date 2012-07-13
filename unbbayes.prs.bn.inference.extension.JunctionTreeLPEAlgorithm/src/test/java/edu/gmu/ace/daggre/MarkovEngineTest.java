/**
 * 
 */
package edu.gmu.ace.daggre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;
import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import edu.gmu.ace.daggre.MarkovEngineImpl.AddTradeNetworkAction;
import edu.gmu.ace.daggre.MarkovEngineImpl.BalanceTradeNetworkAction;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineTest extends TestCase {
	
	private static final int THREAD_NUM = 5;//75;	// quantity of threads to use in order to test multi-thread behavior

	public static final int MAX_NETWIDTH = 3;
	public static final int MAX_STATES = 5;
	public static final int MIN_STATES = 2;
	
	/** Error margin used when comparing 2 probability values */
	public static final float PROB_ERROR_MARGIN = 0.0005f;

	/** Error margin used when comparing 2 asset (score) values */
	public static final float ASSET_ERROR_MARGIN = 1f;
	
	private MarkovEngineImpl engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance();

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
					Math.max(MIN_STATES, Math.min(1 + (int)(MAX_STATES*Math.random()), MAX_STATES)), // between MIN_STATES and MAX_STATES 
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
		assertNotNull(engine.getProbabilisticNetwork());
		
		// no nodes in network.
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
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
		
		// cannot reuse same transaction key
		try {
			engine.addQuestion(transactionKey, new Date(), Long.MAX_VALUE, 2, null);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// check if network contains THREAD_NUM nodes 
		assertEquals(THREAD_NUM, engine.getProbabilisticNetwork().getNodeCount());
		
		// check if network contains nodes with ID from 0 to THREAD_NUM-1
		for (int i = 0; i < THREAD_NUM; i++) {
			assertNotNull(engine.getProbabilisticNetwork().getNode(Integer.toString(i)));
		}
		
		// reset engine
		engine.initialize();
		assertNotNull(engine.getProbabilisticNetwork());
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
		
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
		assertEquals(THREAD_NUM, engine.getProbabilisticNetwork().getNodeCount());
		
		// check if network contains nodes with ID from 0 to THREAD_NUM-1
		for (int i = 0; i < THREAD_NUM; i++) {
			assertNotNull(engine.getProbabilisticNetwork().getNode(Integer.toString(i)));
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
										Math.max(MIN_STATES, Math.min(1 + (int)(MAX_STATES*Math.random()), MAX_STATES)), // between MIN_STATES and MAX_STATES
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
										if (parentNumCounters.get(child) + parents.size() > MAX_NETWIDTH) {
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
		assertNotNull(engine.getProbabilisticNetwork());
		
		// no nodes in network.
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
		// no edges in network
		assertEquals(0, engine.getProbabilisticNetwork().getEdges().size());
		
		
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
		
		// cannot reuse same transaction key
		try {
			engine.addQuestionAssumption(transactionKey, new Date(), (long)0, Collections.singletonList((long)1), null);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// check if data structures are synchronized
		assertEquals(parentNumCounters.keySet().size(), generatedNodes.size());
		int parentNumCounterValuesSum = 0;
		for (Integer numParents : parentNumCounters.values()) {
			parentNumCounterValuesSum += numParents;
		}
		assertEquals(parentNumCounterValuesSum, generatedEdges.size());
		
		// check if network contains all nodes and edges
		assertEquals(generatedNodes.size(), engine.getProbabilisticNetwork().getNodeCount());
		assertEquals(generatedEdges.size(), engine.getProbabilisticNetwork().getEdges().size());
		
		for (Long nodeID : generatedNodes) {
			assertEquals(nodeID + " is not present in " + engine.getProbabilisticNetwork(),
					Long.toString(nodeID),
					engine.getProbabilisticNetwork().getNode(Long.toString(nodeID)).getName());
		}
		for (QuestionPair pair : generatedEdges) {
			Node node1 = engine.getProbabilisticNetwork().getNode(Long.toString(pair.left));
			Node node2 = engine.getProbabilisticNetwork().getNode(Long.toString(pair.right));
			assertNotNull(pair.left + " is null", node1);
			assertNotNull(pair.right + " is null", node2);
			assertFalse(pair.left + ".equals(" + pair.right+")", node1.equals(node2));
			assertFalse(pair.left + "->" + pair.right + " is not present in " + engine.getProbabilisticNetwork(),
					engine.getProbabilisticNetwork().hasEdge(node1, node2) < 0);
		}
		
		// reset engine
		engine.initialize();
		assertNotNull(engine.getProbabilisticNetwork());
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
		
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
		assertEquals(generatedNodes.size(), engine.getProbabilisticNetwork().getNodeCount());
		assertEquals(generatedEdges.size(), engine.getProbabilisticNetwork().getEdges().size());
		for (Long nodeID : generatedNodes) {
			assertEquals(nodeID + " is not present in " + engine.getProbabilisticNetwork(),
					Long.toString(nodeID),
					engine.getProbabilisticNetwork().getNode(Long.toString(nodeID)).getName());
		}
		for (QuestionPair pair : generatedEdges) {
			Node node1 = engine.getProbabilisticNetwork().getNode(Long.toString(pair.left));
			Node node2 = engine.getProbabilisticNetwork().getNode(Long.toString(pair.right));
			assertNotNull(pair.left + " is null", node1);
			assertNotNull(pair.right + " is null", node2);
			assertFalse(pair.left + ".equals(" + pair.right+")", node1.equals(node2));
			assertFalse(pair.left + "->" + pair.right + " is not present in " + engine.getProbabilisticNetwork(),
					engine.getProbabilisticNetwork().hasEdge(node1, node2) < 0);
		}
		
		// case 3 : edges being substituted
		
		// reset engine
		engine.initialize();
		assertNotNull(engine.getProbabilisticNetwork());
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
		
		/*
		 * Create following net
		 *  0<-1
		 *  ^
		 *  |
		 *  2
		 */
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0, 2, null);
		engine.addQuestion(transactionKey, new Date(), 1, 2, null);
		engine.addQuestion(transactionKey, new Date(), 2, 2, null);
		List<Long> assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 1);
		assumptiveQuestionIds.add((long) 2);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, assumptiveQuestionIds, null);
		engine.commitNetworkActions(transactionKey);
		// check network structure consistency
		assertEquals(3, engine.getProbabilisticNetwork().getNodeCount());
		assertNotNull(engine.getProbabilisticNetwork().getNode("0"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("1"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("2"));
		assertEquals(2,engine.getProbabilisticNetwork().getNode("0").getParents().size());
		assertEquals(0,engine.getProbabilisticNetwork().getNode("1").getParents().size());
		assertEquals(0,engine.getProbabilisticNetwork().getNode("2").getParents().size());
		assertEquals(2, engine.getProbabilisticNetwork().getEdges().size());
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("1"), 
				engine.getProbabilisticNetwork().getNode("0"))
			);
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("2"), 
				engine.getProbabilisticNetwork().getNode("0"))
			);
		// check cpt
		for (Node node : engine.getProbabilisticNetwork().getNodes()) {
			PotentialTable cpt = ((ProbabilisticNode)node).getProbabilityFunction();
			for (int i = 0; i < cpt.tableSize(); i++) {
				assertEquals("Node " + node + ", index " + i, .5, cpt.getValue(i), PROB_ERROR_MARGIN);
			}
		}
		
		/*
		 * Modify to following net
		 *  0<-1
		 *  |
		 *  V
		 *  2
		 */
		transactionKey = engine.startNetworkActions();
		// delete edge 2->0 (leave only 1 -> 0)
		assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 1);
		List<Float> cpd = new ArrayList<Float>();
		cpd.add(.8f);
		cpd.add(.2f);
		cpd.add(.1f);
		cpd.add(.9f);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, assumptiveQuestionIds, cpd);	// let 1->0 substitute the old edges
		
		// add edge 0 -> 2
		assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 0);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, assumptiveQuestionIds, cpd);
		
		engine.commitNetworkActions(transactionKey);
		// check network structure consistency
		assertEquals(3, engine.getProbabilisticNetwork().getNodeCount());
		assertNotNull(engine.getProbabilisticNetwork().getNode("0"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("1"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("2"));
		assertEquals(1,engine.getProbabilisticNetwork().getNode("0").getParents().size());
		assertEquals(0,engine.getProbabilisticNetwork().getNode("1").getParents().size());
		assertEquals(1,engine.getProbabilisticNetwork().getNode("2").getParents().size());
		assertEquals(2, engine.getProbabilisticNetwork().getEdges().size());
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("1"), 
				engine.getProbabilisticNetwork().getNode("0"))
			);
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("0"), 
				engine.getProbabilisticNetwork().getNode("2"))
			);
		// check cpts of each node
		ProbabilisticNode nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("0");
		PotentialTable cpt = nodeToTest.getProbabilityFunction();
		assertEquals(4, cpt.tableSize());
		assertEquals(.8f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.2f, cpt.getValue(1), PROB_ERROR_MARGIN);
		assertEquals(.1f, cpt.getValue(2), PROB_ERROR_MARGIN);
		assertEquals(.9f, cpt.getValue(3), PROB_ERROR_MARGIN);
		nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("1");
		cpt = nodeToTest.getProbabilityFunction();
		assertEquals(2, cpt.tableSize());
		assertEquals(.5f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, cpt.getValue(1), PROB_ERROR_MARGIN);
		nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("2");
		cpt = nodeToTest.getProbabilityFunction();
		assertEquals(4, cpt.tableSize());
		assertEquals(.8f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.2f, cpt.getValue(1), PROB_ERROR_MARGIN);
		assertEquals(.1f, cpt.getValue(2), PROB_ERROR_MARGIN);
		assertEquals(.9f, cpt.getValue(3), PROB_ERROR_MARGIN);
		
		
		
		// case 3 : edges being substituted in same transaction
		
		engine.initialize();
		assertNotNull(engine.getProbabilisticNetwork());
		assertEquals(0, engine.getProbabilisticNetwork().getNodeCount());
		
		transactionKey = engine.startNetworkActions();
		
		/*
		 * Create following net
		 *  0<-1
		 *  ^
		 *  |
		 *  2
		 */
		engine.addQuestion(transactionKey, new Date(), 0, 2, null);
		engine.addQuestion(transactionKey, new Date(), 1, 2, null);
		engine.addQuestion(transactionKey, new Date(), 2, 2, null);
		assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 1);
		assumptiveQuestionIds.add((long) 2);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, assumptiveQuestionIds, null);
		
		/*
		 * Modify to following net
		 *  0<-1
		 *  |
		 *  V
		 *  2
		 */
		// delete edge 2->0 (leave only 1 -> 0)
		assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 1);
		cpd = new ArrayList<Float>();
		cpd.add(.8f);
		cpd.add(.2f);
		cpd.add(.1f);
		cpd.add(.9f);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, assumptiveQuestionIds, cpd);	// let 1->0 substitute the old edges
		
		// add edge 0 -> 2
		assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 0);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, assumptiveQuestionIds, cpd);
		
		engine.commitNetworkActions(transactionKey);
		
		
		// check network structure consistency
		assertEquals(3, engine.getProbabilisticNetwork().getNodeCount());
		assertNotNull(engine.getProbabilisticNetwork().getNode("0"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("1"));
		assertNotNull(engine.getProbabilisticNetwork().getNode("2"));
		assertEquals(1,engine.getProbabilisticNetwork().getNode("0").getParents().size());
		assertEquals(0,engine.getProbabilisticNetwork().getNode("1").getParents().size());
		assertEquals(1,engine.getProbabilisticNetwork().getNode("2").getParents().size());
		assertEquals(2, engine.getProbabilisticNetwork().getEdges().size());
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("1"), 
				engine.getProbabilisticNetwork().getNode("0"))
			);
		assertNotNull(engine.getProbabilisticNetwork().hasEdge(
				engine.getProbabilisticNetwork().getNode("0"), 
				engine.getProbabilisticNetwork().getNode("2"))
			);
		// check cpts of each node
		nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("0");
		cpt = nodeToTest.getProbabilityFunction();
		assertEquals(4, cpt.tableSize());
		assertEquals(.8f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.2f, cpt.getValue(1), PROB_ERROR_MARGIN);
		assertEquals(.1f, cpt.getValue(2), PROB_ERROR_MARGIN);
		assertEquals(.9f, cpt.getValue(3), PROB_ERROR_MARGIN);
		nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("1");
		cpt = nodeToTest.getProbabilityFunction();
		assertEquals(2, cpt.tableSize());
		assertEquals(.5f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, cpt.getValue(1), PROB_ERROR_MARGIN);
		nodeToTest = (ProbabilisticNode) engine.getProbabilisticNetwork().getNode("2");
		cpt = nodeToTest.getProbabilityFunction();
		assertEquals(4, cpt.tableSize());
		assertEquals(.8f, cpt.getValue(0), PROB_ERROR_MARGIN);
		assertEquals(.2f, cpt.getValue(1), PROB_ERROR_MARGIN);
		assertEquals(.1f, cpt.getValue(2), PROB_ERROR_MARGIN);
		assertEquals(.9f, cpt.getValue(3), PROB_ERROR_MARGIN);
		
	}

	/**
	 * Test method for 
	 * {@link edu.gmu.ace.daggre.MarkovEngineImpl#addCash(long, java.util.Date, long, float, java.lang.String)},
	 * {@link edu.gmu.ace.daggre.MarkovEngineImpl#getCash(long, List, List)},
	 * {@link edu.gmu.ace.daggre.MarkovEngineImpl#getAssetsIfStates(long, long, List, List)}.
	 */
	public final void testCashAndAssets() {
		/*
		 * Create following net
		 *  0<-1
		 *  ^
		 *  |
		 *  2
		 */
		Long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0, 2, null);
		engine.addQuestion(transactionKey, new Date(), 1, 2, null);
		engine.addQuestion(transactionKey, new Date(), 2, 2, null);
		List<Long> assumptiveQuestionIds = new ArrayList<Long>();
		assumptiveQuestionIds.add((long) 1);
		assumptiveQuestionIds.add((long) 2);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, assumptiveQuestionIds, null);
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			engine.addCash(transactionKey, new Date(), Long.MAX_VALUE, 10, "To fail");
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// global cash should be 0 initially (i.e. q-values are initialized as 1)
		assertEquals(0f, engine.getCash(1, null, null), ASSET_ERROR_MARGIN);
		List<Float> assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, null, null);
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// test some conditional cash as well
		// non-null conditions
		List<Long> assumptionIds = new ArrayList<Long>();
		List<Integer> assumedStates = new ArrayList<Integer>();
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, Collections.singletonList((long)1), Collections.singletonList(0));
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, Collections.singletonList((long)1), Collections.singletonList(0));
			fail("Should throw exception, because question = assumption");
		} catch (IllegalArgumentException e) {
			// OK, because question == 1 && assumption == 1 is invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, Collections.singletonList((long)1), Collections.singletonList(0));
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);

		// 1 = 0, 0 = null, 2 = null (equivalent to only 1 = 0)
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(null);	// node 0, state null
		assumptionIds.add((long) 1); assumedStates.add(0);		// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(null);	// node 2, state null
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assumptionIds.remove(0); assumedStates.remove(0);	// remove node 0 from assumption
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(4, assetsIfStates.size());	// will return table of assets, due to 2 = null
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);
		assumptionIds.set(1, (long)0); assumedStates.set(1, null);	// remove 2 from assumptions and add 0=null
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(4, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);
		
		// 1 = 0, 0 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assumptionIds.remove(0); assumedStates.remove(0);	// remove node 1 from assumption
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.add((long)1); assumedStates.add(0);	// re-add 1 = 0
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);

		// 2 = 1, 0 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assumptionIds.add((long) 0); assumedStates.add(1);	// node 0, state 1
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.remove(0); assumedStates.remove(0);	// remove 2 = 1
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// 1 = 1, 2 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(1);	// node 1, state 1
		assumptionIds.add((long) 2); assumedStates.add(0);	// node 2, state 0
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.remove(1); assumedStates.remove(1);	// remove 2 = 0
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(0f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(0f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// 0 = 0, 1 = 0, 2 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		try {
			engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		
		// add 100 cash
		transactionKey = engine.startNetworkActions();
		engine.addCash(transactionKey, new Date(), 1, 100f, "Just to test");
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			engine.addCash(transactionKey, new Date(), Long.MAX_VALUE-1, 10, "To fail");
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// test assumptions == null
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, null, null);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, null, null);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, null, null);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		

		// global cash should be 100 now
		assertEquals(100f, engine.getCash(1, null, null), ASSET_ERROR_MARGIN);
		
		
		// test some conditional cash as well
		// non-null conditions
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// 1 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);

		// 1 = 0, 0 = null, 2 = null (equivalent to only 1 = 0)
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(null);	// node 0, state null
		assumptionIds.add((long) 1); assumedStates.add(0);		// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(null);	// node 2, state null
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assumptionIds.remove(0); assumedStates.remove(0);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(4, assetsIfStates.size());	
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);
		assumptionIds.set(1, (long)0); assumedStates.set(1, null);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(4, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);
		
		// 1 = 0, 0 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.remove(1); assumedStates.remove(1);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);

		// 2 = 1, 0 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assumptionIds.add((long) 0); assumedStates.add(1);	// node 0, state 1
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.remove(0); assumedStates.remove(0);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// 1 = 1, 2 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(1);	// node 1, state 1
		assumptionIds.add((long) 2); assumedStates.add(0);	// node 2, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		try {
			engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
			fail("Should throw exception, because assumption contains question");
		} catch (IllegalArgumentException e) {
			// OK, because if assumptions contain the question, then  it is an invalid argument
			assertNotNull(e);
		}
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());	
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.remove(1); assumedStates.remove(1);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// 0 = 0, 1 = 0, 2 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		assumptionIds.remove(0); assumedStates.remove(0);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.set(0, (long)0); assumedStates.set(0, 0);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assumptionIds.set(1, (long)1); assumedStates.set(1, 0);
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, assumptionIds, assumedStates);
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);	
		
		// do some extra test regarding the structure 1 -> 0 <- 2
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, Collections.singletonList((long)1), Collections.singletonList(1));
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);	
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)0, Collections.singletonList((long)2), Collections.singletonList(1));
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);	
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, Collections.singletonList((long)0), Collections.singletonList(1));
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);	
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, Collections.singletonList((long)1), Collections.singletonList(1));
		assertEquals(2, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);	
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)2, Collections.singletonList((long)1), null);
		assertEquals(4, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);	
		assetsIfStates = engine.getAssetsIfStates((long)1, (long)1, Collections.singletonList((long)2), (List)Collections.emptyList());
		assertEquals(4, assetsIfStates.size());
		assertEquals(100f, assetsIfStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(1), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(2), ASSET_ERROR_MARGIN);
		assertEquals(100f, assetsIfStates.get(3), ASSET_ERROR_MARGIN);			
	}

	/**
	 * Test method for 
	 * {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)},
	 * {@link MarkovEngineImpl#getCash(long, List, List)}, 
	 * {@link MarkovEngineImpl#getEditLimits(long, long, int, List, List)},
	 * {@link MarkovEngineImpl#getProbList(long, List, List)},
	 * {@link MarkovEngineImpl#getAssetsIfStates(long, long, List, List)}.
	 * The tested data is equivalent to the one in 
	 * https://docs.google.com/document/d/179XTjD5Edj8xDBvfrAP7aDtvgDJlXbSDQQR-TlM5stw/edit.
	 * <br/>
	 * The DEF net looks like the following.		<br/>
	 * Note: uniform distribution for all nodes.	<br/>
	 * D--->F										<br/>
	 * | 											<br/>
	 * V  											<br/>
	 * E											<br/>
	 * 
	 * The sequence is:<br/>
	 * <br/>
	 * There are two cliques {D, E}, and {D, F}, initial asset tables have q-value as 100 in every cell. <br/>
	 *	Current marginal probabilities are:	<br/>
	 *	Variables    D             E              F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.5 0.5]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	Trade-1: Tom would like to make a bet on E=e1, that has current probability as 0.5. First of all, we need to calculate TomÅfs edit limit (in this case, there is no assumption):	<br/> 
	 *	Given E=e1, min-q1 = 100	<br/>
	 *	Given E~=e1, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995].	<br/> 
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55 (current)	<br/>
	 *	<br/>	
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.55 0.45]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	TomÅfs min-q is 90, at the following 4 min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2     1	<br/>
	 *	     1     2     2	<br/>
	 *	     2     2     1	<br/>
	 *	     2     2     2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-2: Now Tom would like to make another conditional bet on E=e1 given D=d1 (current P(E=e1|D=d1) = 0.55). Again, let us calculate his edit limits first (in this case, we have assumed variable D=d1). And note that TomÅfs asset tables are not the initial ones any more, but updated from last trade he did, now:	<br/> 
	 *	Given E=e1, and D=d1, min-q1 = 110	<br/>
	 *	Given E~=e1, and D=d1, min-q2 = 90	<br/>
	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9 (current)	<br/>
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.725 0.275]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	TomÅfs min-q is 20, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2    1	<br/>
	 *	     1     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-3: Joe came and intended to make a bet on E=e1 given D=d2 (current P(E=e1|D=d2) is 0.55). This will be his first edit, so he has initial asset tables before his trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given E=e1, and D=d2, min-q1 = 100	<br/>
	 *	Given E~=e1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0055, 0.9955]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4 (current)	<br/> 
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.5 0.5]	<br/>
	 *	<br/>	
	 *	JoeÅfs min-q is 72.72727272727..., at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	     2     1    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-4: Now Amy is interested in changing P(F=f1|D=d1), which is currently 0.5. It will be her first edit, so she also has initial asset tables before the trade.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d1, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d1, min-q2 = 100	<br/>
 	 *	From Equation (1), edit interval is [0.005, 0.995]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.4 0.6]	<br/>
	 *	<br/>	
	 *	AmyÅfs min-q is 60, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     1    1	<br/>
	 *	     1     2    1	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-5: Joe would like to trade again on P(F=f1|D=d2), which is currently 0.5.	<br/> 
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 72.727272727	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 72.727272727	<br/>
	 *	From Equation (1), edit interval is [0.006875, 0.993125]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5 0.5]   [0.65 0.35]   [0.2 0.8]	<br/>
	 *	<br/>	
	 *	JoeÅfs min-q is 14.54545454546, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	<br/>	
	 *	At this point, the model DEF reaches the status that has the same CPTs as the starting CPTs for the experimental model DEF we used in our AAAI 2012 paper.	<br/> 
	 *	<br/>	
	 *	From now on, we run test cases described in the paper.	<br/> 
	 *	<br/>	
	 *	Trade-6: Eric would like to trade on P(E=e1), which is currently 0.65.	<br/> 
	 *	To decide long or short, S(E=e1) = 10, S(E~=e1)=10, no difference because this will be EricÅfs first trade.	<br/>
	 *	Edit limit:	<br/>
	 *	Given F=f1, and D=d2, min-q1 = 100	<br/>
	 *	Given F~=f1, and D=d2, min-q2 = 100	<br/>
	 *	From Equation (1), edit interval is [0.0065, 0.9965]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D              E                   F	<br/> 
	 *	Marginals   [0.5824, 0.4176]   [0.8, 0.2]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	EricÅfs expected score is S=10.1177.	<br/>
	 *	EricÅfs min-q is 57.142857, at the following two min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    1	<br/>
	 *	     2     2    2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-7: Eric would like to make another edit. This time, he is interested on changing P(D=d1|F=f2), which is currently 0.52.	<br/> 
	 *	To decide long or short, S(D=d1, F=f2) = 10.36915, S(D~=d1, F=f2)=9.7669.	<br/>
	 *	Edit limit:	<br/>
	 *	Given D=d1, and F=f2 , min-q1 = 57.142857	<br/>
	 *	Given D~=d1, and F=f2, min-q2 = 57.142857	<br/>
	 *	From Equation (1), edit interval is [0.0091059, 0.9916058]	<br/>
	 *	- Verification successful: after substituting the limits into edit, we do get min-q to be 1.	<br/> 
	 *	<br/>	
	 *	Trading sequence:	<br/> 
	 *	Tom1:	P(E=e1) = 0.5  to 0.55	<br/>
	 *	Tom2:	P(E=e1|D=d1) = 0.55 to 0.9	<br/> 
	 *	Joe1:	P(E=e1|D=d2) = 0.55 to 0.4	<br/> 
	 *	Amy1:	P(F=f1|D=d1) = 0.5 to 0.3	<br/> 
	 *	Joe2:	P(F=f1|D=d2) = 0.5 to 0.1	<br/>
	 *	Eric1:	P(E=e1) = 0.65 to 0.8	<br/> 
	 *	Eric2:	P(D=d1|F=f2) = 0.52 to 0.7 (current)	<br/>
	 *	<br/>	  
	 *	Variables   D                            E                             F	<br/> 
	 *	Marginals   [0.7232, 0.2768]   [0.8509, 0.1491]   [0.2165, 0.7835]	<br/>
	 *	<br/>	
	 *	EricÅfs expected score is now 10.31615.	<br/> 
	 *	EricÅfs min-q is 35.7393, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     2    2	<br/>
	 *	<br/> 
	 *  The transaction is committed at each trade (trades are not committed at once).
	 */	
	public final void testAddTrade() {
		
		// crate transaction for generating the DEF network
		long transactionKey = engine.startNetworkActions();
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// commit changes
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			List<Float> newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// check whether probability prior to edit is really 0.5
		List<Float> probList = engine.getProbList(0x0E, null, null);
		assertEquals(2 , probList.size());
		assertEquals(0.5f , probList.get(0) , PROB_ERROR_MARGIN);
		assertEquals(0.5f , probList.get(1) , PROB_ERROR_MARGIN);
		
		// edit interval of P(E=e1) should be [0.005, 0.995]
		List<Float> editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, null, null);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0), PROB_ERROR_MARGIN );
		assertEquals(0.995f, editInterval.get(1), PROB_ERROR_MARGIN );
		
		// obtain conditional probabilities and assets of the edited clique, prior to edit, so that we can use it to check assets after edit
		List<Float> cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		List<Float> cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// do edit
		transactionKey = engine.startNetworkActions();
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		List<Float> cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		List<Float> cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		
		// check that new marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.55f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.45f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that Tom's min-q is 90 (and the cash is supposedly the log value of 90)
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);	 // null means unconditional cash, which is supposedly the global minimum
		assertEquals(Math.round(engine.getScoreFromQValues(90f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains e2 and any values for D and F, by asserting that cash conditioned to such states are equals to the min
		// d, e, f are always going to be the assumption nodes in this test
		List<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1 (not min)
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check minimal condition of LPE: e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(1));
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(0));
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.55, .45, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.55f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
		assertEquals(0.45f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
		assertEquals(0.55f , probList.get(2),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
		assertEquals(0.45f , probList.get(3),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(E=e1|D=d1) should be [0.005, 0.995]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, assumptionIds, assumedStates);	// (node == 0x0E && state == 0) == e1
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, prior to edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());

		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.725 0.275] (this is expected value), and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.725f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.275f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d1, e2 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1 (not min)
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check minimal condition of LPE: d1, e2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// TODO assert getAssetsIf

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.9f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
		assertEquals(0.1f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
		assertEquals(0.55f , probList.get(2),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
		assertEquals(0.45f , probList.get(3),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(E=e1|D=d2) should be [0.0055, 0.9955]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0E, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0055f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9955f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 72.727272...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(72.727272f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d2, e1 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check minimal condition of LPE: d2, e1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(1);
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// TODO assert getAssetsIf
		
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		probList = engine.getProbList(0x0F, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(F=f1|D=d1) should be [0.005, 0.995]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Amy"), 0x0F, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7  
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.4 .6], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.4f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.6f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d1, f1 and any value E
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check minimal condition of LPE: d1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0F);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0F);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// TODO assert getAssetsIf
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		probList = engine.getProbList(0x0F, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.3f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.7f , probList.get(1),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(F=f1|D=d2) should be [0.006875, 0.993125]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0F, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.006875f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.993125, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.2 .8], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.8f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check probabilities given assumptions outside the same clique
		probList = engine.getProbList(0x0E, Collections.singletonList((long)0x0F), Collections.singletonList(0));
		assertEquals(2, probList.size());
		assertEquals(0.775f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.225f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, Collections.singletonList((long)0x0F), Collections.singletonList(1));
		assertEquals(2, probList.size());
		assertEquals(0.6188f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.3813f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, Collections.singletonList((long)0x0E), Collections.singletonList(0));
		assertEquals(2, probList.size());
		assertEquals(0.2385f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7615f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, Collections.singletonList((long)0x0E), Collections.singletonList(1));
		assertEquals(2, probList.size());
		assertEquals(0.1286f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.8714f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d2, e1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// TODO assert getAssetsIf
		
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
		// Eric bets P(E=e1) = .65 -> .8
		
		// check whether probability prior to edit is really = [.65, .35]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(2, probList.size());
		assertEquals(0.65f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.35f , probList.get(1),PROB_ERROR_MARGIN );
		
		
		// edit interval of P(E=e1) should be [0.0065, 0.9965]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0E, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0065f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9965f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.8 0.2] (this is expected value), F is [0.2165, 0.7835], and D is [0.5824, 0.4176]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5824f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.4176f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 57.142857...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(57.142857f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(57.142857f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains e2 and any D or F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		
		// check whether probability prior to edit is really [d1f2, d2f2] = [.52, .48]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		probList = engine.getProbList(0x0D, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.52f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.48f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(1);	// set f2 as assumed state
		
		// edit interval of P(D=d1|F=f2) should be [0.0091059, 0.9916058]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
		).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		
		// check that min-q is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE is d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		

		// Eric makes a bet which makes his assets-q to go below 1, but the algorithm does not allow it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// get history before transaction, so that we can make sure new transaction is not added into history
		List<QuestionEvent> questionHistory = engine.getQuestionHistory(0x0DL, null, null);
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		assertNull(engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false	// do not allow negative assets
			));
		// this is supposedly going to commit empty transaction
		engine.commitNetworkActions(transactionKey);
		// make sure history was not changed
		assertEquals(questionHistory, engine.getQuestionHistory(0x0DL, null, null));

		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "OK", Long.MIN_VALUE, (long)0x0D, newValues, null, null, false);
			fail("It's not be supposed to reuse a commited transaction.");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		// check that assets and conditional probs did not change
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals( "Index = " + i, cliqueAssetsBeforeTrade.get(i), cliqueAssetsAfterTrade.get(i), ASSET_ERROR_MARGIN );
			assertEquals( "Index = " + i, cliqueProbsBeforeTrade.get(i), cliqueProbsAfterTrade.get(i), PROB_ERROR_MARGIN );
		}
		
		
		// check that marginals have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		
		// check that min-q has not changed
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE has not changed - still d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		
		// add question disconnected question C
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), (long)0x0C, 2, null);
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// check that probabilities and assets related to old node did not change
		
		// check that final marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		// init list of states of the assumptions
		assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		
		// check that min-q of Amy is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		// check that min-q of Joe is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check that final min-q of Eric is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		
		
		// Amy bets P(C=c1) = .5 -> .05
		
		// check whether probability of C prior to edit is really [.5, .5] no matter what combination of other nodes
		probList = engine.getProbList(0x0C, null, null);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		probList = engine.getProbList(0x0C, null, null);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		probList = engine.getProbList(0x0C, null, null);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assumptionIds = new ArrayList<Long>(3);	assumedStates = new ArrayList<Integer>(3);
		assumptionIds.add((long)0x0D);	assumptionIds.add((long)0x0E);
		assumedStates.add((Math.random() < .5)?1:0); assumedStates.add((Math.random() < .5)?1:0);
		probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assumptionIds.set(0, (long)0x0D);	assumptionIds.set(1, (long)0x0F);
		assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0);
		probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assumptionIds.set(0, (long)0x0E);	assumptionIds.set(1, (long)0x0F);
		assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0);
		probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assumptionIds.set(0, (long)0x0E);	assumptionIds.set(1, (long)0x0F); assumptionIds.add((long)0x0D); 
		assumedStates.set(0, (Math.random() < .5)?1:0); assumedStates.set(1, (Math.random() < .5)?1:0); assumedStates.add((Math.random() < .5)?1:0);
		probList = engine.getProbList(0x0C, assumptionIds, assumedStates);
		assertEquals(2, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		
		// edit interval of P(C=c1) should be [0.005, 0.995]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Amy"), 0x0C, 0, null, null);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0083f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916667f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// set P(C=c1) = 0.05 and   P(C=c1) = 0.95
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.05f);
		newValues.add(.95f);
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(C=c1) = 0.05", 
				userNameToIDMap.get("Amy"), 
				0x0C, 
				newValues, 
				null, 
				null, 
				false
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// check assets
		List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), (long)0x0C, null, null);
		assertEquals(2, assetsIfStates.size());
		assertEquals( 10,  engine.getQValuesFromScore(assetsIfStates.get(0)), ASSET_ERROR_MARGIN );
		assertEquals( 190,  engine.getQValuesFromScore(assetsIfStates.get(1)), ASSET_ERROR_MARGIN );
		
		// check that marginals of C is [.05,.95], and others have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0C, null, null);
		assertEquals(0.05f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.95f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 10...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(10f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(10f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE is independent from D,E,F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check minimal condition of LPE: c1
		cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(0));
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(1));
		assertTrue("Obtained cash = " + cash, minCash < cash);
		

		// Eric makes a bet which makes his assets-q to go below 1, and the algorithm allows it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		assertEquals(2, engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				true	// allow negative assets
			).size());
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that cash is smaller or equal to 0
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertTrue("Obtained unexpected cash = " + minCash, minCash <= 0);
	
		
		// test the case in which the trade will make the assets to go negative, but it cannot be previewed (it will throw exception only on commit)
		transactionKey = engine.startNetworkActions();
		
		// add a new question in the same transaction, so that we guarantee that trade cannot be previewed
		List<Float> initProbs = new ArrayList<Float>();
		initProbs.add(.9f); initProbs.add(.0999f); initProbs.add(.0001f);
		engine.addQuestion(transactionKey, new Date(), 0x0AL, 3, initProbs);
		
		// add a trade which will make user asset to go below zero and cannot be previewed
		newValues = new ArrayList<Float>();
		newValues.add(.0001f); newValues.add(.0999f);  newValues.add(.9f); 
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(A = [.0001, .0999, .9])", 
				userNameToIDMap.get("Amy"), 
				0x0AL, 
				newValues, 
				null, 
				null, 
				false
			).isEmpty());
		try {
			engine.commitNetworkActions(transactionKey);
			fail("This is expected to throw ZeroAssetsException");
		} catch (ZeroAssetsException e) {
			assertNotNull(e);
		}
	}
	

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * This method performs the same test of {@link #testAddTrade()},
	 * but it executes everything in a single transaction.
	 */
	public final void testAddTradeInOneTransaction () {
		
		// crate transaction
		long transactionKey = engine.startNetworkActions();
		
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro

		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Tom"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			).isEmpty());
		
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			).isEmpty());
		

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Joe"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			).isEmpty());
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Amy"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			).isEmpty());
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			).isEmpty());
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Eric"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Eric bets P(E=e1) = .65 -> .8
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				(List)Collections.emptyList(), 
				(List)Collections.emptyList(), 
				false
			).isEmpty());
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		assertTrue( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				Collections.singletonList((long)0x0F), 
				Collections.singletonList(1), 
				false
			).isEmpty());
		
		// commit all trades (including the creation of network and user)
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// check that final marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		List<Float> probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		ArrayList<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		// init list of states of the assumptions
		ArrayList<Integer> assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// check that final min-q of Tom is 20
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		
		// check that min-q of Amy is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		// check that min-q of Joe is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check that final min-q of Eric is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		
	}
	
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)},
	 * {@link MarkovEngineImpl#addQuestion(long, Date, long, int, List)},
	 * {@link MarkovEngineImpl#addQuestionAssumption(long, Date, long, List, List)}.
	 * This method performs the trades in {@link #testAddTrade()}, but it creates nodes and edges
	 * after some trade was performed.
	 */
	public final void testAddQuestionAfterTrade() {
		
		// generate node E
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.commitNetworkActions(transactionKey);
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// check whether probability prior to edit is really 0.5
		List<Float> probList = engine.getProbList(0x0E, null, null);
		assertEquals(2 , probList.size());
		assertEquals(0.5f , probList.get(0) , PROB_ERROR_MARGIN);
		assertEquals(0.5f , probList.get(1) , PROB_ERROR_MARGIN);
		
		// edit interval of P(E=e1) should be [0.005, 0.995]
		List<Float> editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, null, null);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0), PROB_ERROR_MARGIN );
		assertEquals(0.995f, editInterval.get(1), PROB_ERROR_MARGIN );
		
		// obtain conditional probabilities and assets of the edited clique, prior to edit, so that we can use it to check assets after edit
		List<Float> cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, null, null, false);
		List<Float> cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, null, null);
		assertEquals(2, cliqueProbsBeforeTrade.size());
		assertEquals(2, cliqueAssetsBeforeTrade.size());
		
		// do edit
		transactionKey = engine.startNetworkActions();
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		List<Float> cliqueProbsAfterTrade = engine.getProbList((long)0x0E, null, null, false);
		List<Float> cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E,null, null);
		assertEquals(2, cliqueProbsAfterTrade.size());
		assertEquals(2, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		try {
			engine.getProbList(0x0D, null, null);
			fail("Node was not created yet.");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);	// this is supposed to happen
		}
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.55f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.45f, probList.get(1), PROB_ERROR_MARGIN);
		try {
			engine.getProbList(0x0F, null, null);
			fail("Node was not created yet.");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);	// this is supposed to happen
		}
		
		// check that Tom's min-q is 90 (and the cash is supposedly the log value of 90)
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);	 // null means unconditional cash, which is supposedly the global minimum
		assertEquals(Math.round(engine.getScoreFromQValues(90f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains e2 
		// all nodes are always going to be the assumption nodes in this test
		List<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0E);
		
		// check e1
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// e1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check e2
		assumedStates.set(0, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		
		// create node D and edge D->E
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		engine.commitNetworkActions(transactionKey);
		
		// check that probs and assets did not change
		
		// check that marginal of E is [0.55 0.45], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.55f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.45f, probList.get(1), PROB_ERROR_MARGIN);
		try {
			engine.getProbList(0x0F, null, null);
			fail("Node does not exist");
		} catch (IllegalArgumentException e) {
			assertNotNull(e); // this is supposed to happen
		}
		
		// check that Tom's min-q is 90 (and the cash is supposedly the log value of 90)
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);	 // null means unconditional cash, which is supposedly the global minimum
		assertEquals(Math.round(engine.getScoreFromQValues(90f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains e2 and any values for D, by asserting that cash conditioned to such states are equals to the min
		// d, e are always going to be the assumption nodes in this test
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		
		// check combination d1, e1 (not min)
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check minimal condition of LPE: e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(1));
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		cash = engine.getCash(userNameToIDMap.get("Tom"), Collections.singletonList((long)0x0E), Collections.singletonList(0));
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.55, .45, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.55f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
		assertEquals(0.45f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
		assertEquals(0.55f , probList.get(2),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
		assertEquals(0.45f , probList.get(3),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(E=e1|D=d1) should be [0.005, 0.995]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0E, 0, assumptionIds, assumedStates);	// (node == 0x0E && state == 0) == e1
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, prior to edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());

		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.725 0.275] (this is expected value), and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.725f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.275f, probList.get(1), PROB_ERROR_MARGIN);
		try {
			engine.getProbList(0x0F, null, null);
			fail("Node was not created yet");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);	// this is expected
		}
		
		// check that min-q is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d1, e2 
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		
		// check combination d1, e1 (not min)
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.9f , probList.get(0),PROB_ERROR_MARGIN );	// P(E=e1|D=d1)
		assertEquals(0.1f , probList.get(1),PROB_ERROR_MARGIN );	// P(E=e2|D=d1)
		assertEquals(0.55f , probList.get(2),PROB_ERROR_MARGIN );	// P(E=e1|D=d2)
		assertEquals(0.45f , probList.get(3),PROB_ERROR_MARGIN );	// P(E=e2|D=d2)
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(E=e1|D=d2) should be [0.0055, 0.9955]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0E, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0055f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9955f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		try {
			engine.getProbList(0x0F, null, null);
			fail();
		} catch (Exception e) {
			assertNotNull(e);
		}
		
		// check that min-q is 72.727272...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(72.727272f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d2, e1 
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		
		// check combination d1, e1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		
		// create node F and edge D->F
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		engine.commitNetworkActions(transactionKey);
		
		// check that probs and assets did not change
		
		// check that marginal of E is still [0.65 0.35] (this is expected value), and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q of Joe is 72.727272...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(72.727272f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Joe contains d2, e1 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check minimal condition of LPE: d2, e1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(1);
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check that min-q of Tom is still 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Tom still contains d1, e2 and any value F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1 (not min)
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check minimal condition of LPE: d1, e2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0E);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0E);
		assumedStates.clear();
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		probList = engine.getProbList(0x0F, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.5f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(1),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(0);	// set d1 as assumed state
		
		// edit interval of P(F=f1|D=d1) should be [0.005, 0.995]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Amy"), 0x0F, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.005f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.995f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7  
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.4 .6], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.4f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.6f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d1, f1 and any value E
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check minimal condition of LPE: d1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0D);
		assumptionIds.add((long)0x0F);
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);
		assumedStates.add(0);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		// check conditions that do not match LPE
		assumedStates.set(0,1);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumedStates.set(0,0);
		assumedStates.set(1,1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumedStates.set(0,1);
		assumedStates.set(1,0);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0D);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		assumptionIds.clear();
		assumptionIds.add((long)0x0F);
		assumedStates.clear();
		assumedStates.add(1);
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// TODO assert getAssetsIf
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		probList = engine.getProbList(0x0F, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.3f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.7f , probList.get(1),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.5f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(F=f1|D=d2) should be [0.006875, 0.993125]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Joe"), 0x0F, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.006875f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.993125, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is still [0.65 0.35] (this is expected value), F is [.2 .8], and the others have not changed (remains 50%)
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.65f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.35f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.8f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains d2, e1, f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// TODO assert getAssetsIf
		
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
		// Eric bets P(E=e1) = .65 -> .8
		
		// check whether probability prior to edit is really = [.65, .35]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(2, probList.size());
		assertEquals(0.65f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.35f , probList.get(1),PROB_ERROR_MARGIN );
		
		
		// edit interval of P(E=e1) should be [0.0065, 0.9965]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0E, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0065f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9965f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0E, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.8 0.2] (this is expected value), F is [0.2165, 0.7835], and D is [0.5824, 0.4176]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.5824f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.4176f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that min-q is 57.142857...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(57.142857f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(57.142857f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE contains e2 and any D or F
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		
		// check whether probability prior to edit is really [d1f2, d2f2] = [.52, .48]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		probList = engine.getProbList(0x0D, assumptionIds, null);
		assertEquals(4, probList.size());
		assertEquals(0.52f , probList.get(2),PROB_ERROR_MARGIN );
		assertEquals(0.48f , probList.get(3),PROB_ERROR_MARGIN );
		
		assumedStates.add(1);	// set f2 as assumed state
		
		// edit interval of P(D=d1|F=f2) should be [0.0091059, 0.9916058]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
		);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that new marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		
		// check that min-q is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE is d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		

		// Eric makes a bet which makes his assets-q to go below 1, but the algorithm does not allow it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// get history before transaction, so that we can make sure new transaction is not added into history
		List<QuestionEvent> questionHistory = engine.getQuestionHistory(0x0DL, null, null);
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		assertNull(
				engine.addTrade(
					transactionKey, 
					new Date(), 
					"Eric bets P(D=d1|F=f2) = 0.7", 
					userNameToIDMap.get("Eric"), 
					0x0D, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false	// do not allow negative assets
				)
			);
		// this is supposedly going to commit empty transaction
		engine.commitNetworkActions(transactionKey);
		// make sure history was not changed
		assertEquals(questionHistory, engine.getQuestionHistory(0x0DL, null, null));
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		// check that assets and conditional probs did not change
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals( "Index = " + i, cliqueAssetsBeforeTrade.get(i), cliqueAssetsAfterTrade.get(i), ASSET_ERROR_MARGIN );
			assertEquals( "Index = " + i, cliqueProbsBeforeTrade.get(i), cliqueProbsAfterTrade.get(i), PROB_ERROR_MARGIN );
		}
		
		
		// check that marginals have not changed: E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		
		// check that min-q has not changed
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE has not changed - still d2, e2 and f2
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, e1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		
		
		// Eric makes a bet which makes his assets-q to go below 1, and the algorithm allows it
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// obtain conditional probabilities and assets of the edited clique, before the edit, so that we can use it to check assets
		cliqueProbsBeforeTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsBeforeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsBeforeTrade.size());
		assertEquals(4, cliqueAssetsBeforeTrade.size());
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				true	// allow negative assets
			);
		engine.commitNetworkActions(transactionKey);
		
		// obtain conditional probabilities and assets of the edited clique, after the edit, so that we can use it to check assets
		cliqueProbsAfterTrade = engine.getProbList((long)0x0F, Collections.singletonList((long)0x0D), null, false);
		cliqueAssetsAfterTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), (long)0x0F, Collections.singletonList((long)0x0D), null);
		assertEquals(4, cliqueProbsAfterTrade.size());
		assertEquals(4, cliqueAssetsAfterTrade.size());
		for (int i = 0; i < cliqueAssetsAfterTrade.size(); i++) {
			assertEquals(
					"Index = " + i, 
					cliqueProbsAfterTrade.get(i)/cliqueProbsBeforeTrade.get(i) * engine.getQValuesFromScore(cliqueAssetsBeforeTrade.get(i)), 
					engine.getQValuesFromScore(cliqueAssetsAfterTrade.get(i)), 
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that cash is smaller or equal to 0
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertTrue("Obtained unexpected cash = " + minCash, minCash <= 0);
		
		
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)},
	 * {@link MarkovEngineImpl#addQuestion(long, Date, long, int, List)},
	 * {@link MarkovEngineImpl#addQuestionAssumption(long, Date, long, List, List)}.
	 * This method performs the same test of {@link #testAddQuestionAfterTrade()},
	 * but it executes everything in a single transaction.
	 */
	public final void testAddQuestionAfterTradeInOneTransaction() {
		
		// crate transaction
		long transactionKey = engine.startNetworkActions();
		
		// create nodes D, E
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro

		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Tom"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			);
		
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			);
		

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Joe"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Amy"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// create node F and  edge D->F
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			);
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Eric"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Eric bets P(E=e1) = .65 -> .8
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				(List)Collections.emptyList(), 
				(List)Collections.emptyList(), 
				false
			);
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				Collections.singletonList((long)0x0F), 
				Collections.singletonList(1), 
				false
		);
		
		// add new node C and random edge from or to C
		engine.addQuestion(transactionKey, new Date(), 0x0C, 2, null);
		if (Math.random() < .5) {
			// edge from C
			if (Math.random() < .5) {
				System.out.println("Edge C->D");
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0D, Collections.singletonList((long)0x0C), null);
			}
			if (Math.random() < .5) {
				System.out.println("Edge C->E");
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long)0x0C), null);
			}
			if (Math.random() < .5) {
				System.out.println("Edge C->F");
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long)0x0C), null);
			}
		} else {
			// edge to C
			List<Long> parentQuestionIds = new ArrayList<Long>();
			if (Math.random() < .5) {
				parentQuestionIds.add((long)0x0D);
			}
			if (Math.random() < .5) {
				parentQuestionIds.add((long)0x0E);
			}
			if (Math.random() < .5) {
				parentQuestionIds.add((long)0x0F);
			}
			System.out.println("Edge " + parentQuestionIds + " -> C");
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0C, parentQuestionIds, null);
		}
		
		
		// commit all trades (including the creation of network and user)
		engine.commitNetworkActions(transactionKey);
		
		// check that final marginal of C is [.5,.5], E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		List<Float> probList = engine.getProbList(0x0C, null, null);
		assertEquals(0.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		ArrayList<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// init list of states of the assumptions
		ArrayList<Integer> assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// check that final min-q of Tom is 20
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		
		// check that min-q of Amy is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		// check that min-q of Joe is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check that final min-q of Eric is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		
	}
	
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getPossibleQuestionAssumptions(long, java.util.List)}.
	 */
	public final void testGetPossibleQuestionAssumptions() {
		
		// Case 0: no network
		
		do {
			try {
				engine.getPossibleQuestionAssumptions((long)(Math.random()*Integer.MAX_VALUE), ((Math.random()<.5)?null:((List)Collections.emptyList())));
				fail("It is impossible to obtain questions from a Bayes Net which was not created yet...");
			}catch (IllegalArgumentException e) {
				// OK. 
				assertNotNull(e);
			}
		} while (Math.random() < .5);
		
		// Case 1: 0
		
		engine.initialize();
		long transactionKey = engine.startNetworkActions();
		// create question 0
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+Math.round(Math.random()*3)), null);
		engine.commitNetworkActions(transactionKey);
		do {
			try {
				engine.getPossibleQuestionAssumptions((long)(1 + Math.random()*Integer.MAX_VALUE), ((Math.random()<.5)?null:((List)Collections.emptyList())));
				fail("Only question 0 is present");
			}catch (IllegalArgumentException e) {
				assertNotNull(e); // OK. 
			}
		} while (Math.random() < .5);
		try {
			engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)0));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		List<Long> assumptions = engine.getPossibleQuestionAssumptions(0, null);
		assertTrue("There is only 1 question in the network...",assumptions.isEmpty());
		assumptions = engine.getPossibleQuestionAssumptions(0, (List)Collections.emptyList());
		assertTrue("There is only 1 question in the network...",assumptions.isEmpty());
		
		// Case 2: 1->0
		
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+Math.round(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+Math.round(Math.random()*3)), null);
		// create edge 1->0 
		engine.addQuestionAssumption(transactionKey, new Date(), 0, Collections.singletonList((long)1), null);
		engine.commitNetworkActions(transactionKey);
		do {
			try {
				engine.getPossibleQuestionAssumptions((long)(Math.random()*Integer.MIN_VALUE), ((Math.random()<.5)?null:((List)Collections.emptyList())));
				fail("These questions are not present");
			}catch (IllegalArgumentException e) {
				assertNotNull(e); // OK. 
			}
		} while (Math.random() < .5);
		try {
			engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)0));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)1));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		assumptions = engine.getPossibleQuestionAssumptions(0, null);
		assertEquals(1, assumptions.size());
		assertEquals(1,assumptions.get(0).longValue());
		assumptions = engine.getPossibleQuestionAssumptions(0, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertEquals(1,assumptions.get(0).longValue());
		assumptions = engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)1));
		assertEquals(1, assumptions.size());
		assertEquals(1,assumptions.get(0).longValue());
		assumptions = engine.getPossibleQuestionAssumptions(1, null);
		assertEquals(1, assumptions.size());
		assertEquals(0,assumptions.get(0).longValue());
		assumptions = engine.getPossibleQuestionAssumptions(1, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertEquals(0,assumptions.get(0).longValue());
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)0));
		assertEquals(1, assumptions.size());
		assertEquals(0,assumptions.get(0).longValue());
		
		
		// Case 3: 1->0<-2
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1,2
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+Math.round(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+Math.round(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 2, (int)(2+Math.round(Math.random()*3)), null);
		// create edges 1->0 and 2->0
		ArrayList<Long> parentQuestionIds = new ArrayList<Long>(2);
		parentQuestionIds.add((long)1); parentQuestionIds.add((long)2);
		engine.addQuestionAssumption(transactionKey, new Date(), 0, parentQuestionIds, null);
		engine.commitNetworkActions(transactionKey);
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)0));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)1));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)2));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		List<Long> assumptionIds;	// list to hold more than 2 assumptions
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)0);
			assumptionIds.add((long)((Math.random()<.5)?1:2));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)1)?2:1));
			}
			engine.getPossibleQuestionAssumptions(0, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)1);
			assumptionIds.add((long)((Math.random()<.5)?0:2));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)0)?2:0));
			}
			engine.getPossibleQuestionAssumptions(1, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)2);
			assumptionIds.add((long)((Math.random()<.5)?0:1));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)0)?1:0));
			}
			engine.getPossibleQuestionAssumptions(2, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// test inexistent assumptions
			assumptionIds = new ArrayList<Long>();
			do {
				assumptionIds.add((long)(Math.random()*Integer.MIN_VALUE) - 1);
			} while (Math.random() < .5);
			engine.getPossibleQuestionAssumptions(0, assumptionIds);
			fail("Assumptions must exist in the Bayes Net");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		assumptions = engine.getPossibleQuestionAssumptions(0, null);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(0, (List)Collections.emptyList());
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)1));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)2));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)1); assumptionIds.add((long)2);
		assumptions = engine.getPossibleQuestionAssumptions(0, assumptionIds);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(1, null);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(1, (List)Collections.emptyList());
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)0));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)2));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)2));
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)0); assumptionIds.add((long)2);
		assumptions = engine.getPossibleQuestionAssumptions(1, assumptionIds);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(2, null);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)1));
		assumptions = engine.getPossibleQuestionAssumptions(2, (List)Collections.emptyList());
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)1));
		assumptions = engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)0));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)1));
		assumptions = engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)1));
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)1));
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)0); assumptionIds.add((long)1);
		assumptions = engine.getPossibleQuestionAssumptions(2, assumptionIds);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assertTrue(assumptions.contains((long)1));
		
		
		// case 4: 1<-0->2
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1,2
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+Math.round(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+Math.round(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 2, (int)(2+Math.round(Math.random()*3)), null);
		// create edges 1<-0 and 2<-0
		engine.addQuestionAssumption(transactionKey, new Date(), 1, Collections.singletonList((long)0), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList((long)0), null);
		engine.commitNetworkActions(transactionKey);
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)0));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)1));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)2));
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)0);
			assumptionIds.add((long)((Math.random()<.5)?1:2));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)1)?2:1));
			}
			engine.getPossibleQuestionAssumptions(0, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)1);
			assumptionIds.add((long)((Math.random()<.5)?0:2));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)0)?2:0));
			}
			engine.getPossibleQuestionAssumptions(1, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// assumptions contains main node
			assumptionIds = new ArrayList<Long>();
			assumptionIds.add((long)2);
			assumptionIds.add((long)((Math.random()<.5)?0:1));
			if (Math.random() < .5) {
				assumptionIds.add((long)(assumptionIds.contains((long)0)?1:0));
			}
			engine.getPossibleQuestionAssumptions(2, assumptionIds);
			fail("Question cannot be assumption of itself");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		try {
			// test inexistent assumptions
			assumptionIds = new ArrayList<Long>();
			do {
				assumptionIds.add((long)(Math.random()*Integer.MIN_VALUE) - 1);
			} while (Math.random() < .5);
			engine.getPossibleQuestionAssumptions(0, assumptionIds);
			fail("Assumptions must exist in the Bayes Net");
		}catch (IllegalArgumentException e) {
			assertNotNull(e); // OK. 
		}
		assumptions = engine.getPossibleQuestionAssumptions(0, null);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(0, (List)Collections.emptyList());
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assertTrue(assumptions.contains((long)2));
		assumptions = engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)1));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)1));
		assumptions = engine.getPossibleQuestionAssumptions(0, Collections.singletonList((long)2));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)2));
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)1); assumptionIds.add((long)2);
		assumptions = engine.getPossibleQuestionAssumptions(0, assumptionIds);
		assertEquals(0, assumptions.size());
		assumptions = engine.getPossibleQuestionAssumptions(1, null);
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(1, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)0));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList((long)2));
		assertEquals(0, assumptions.size());
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)0); assumptionIds.add((long)2);
		assumptions = engine.getPossibleQuestionAssumptions(1, assumptionIds);
		assertEquals(0, assumptions.size());
		assumptions = engine.getPossibleQuestionAssumptions(2, null);
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(2, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)0));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)0));
		assumptions = engine.getPossibleQuestionAssumptions(2, Collections.singletonList((long)1));
		assertEquals(0, assumptions.size());
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)0); assumptionIds.add((long)1);
		assumptions = engine.getPossibleQuestionAssumptions(2, assumptionIds);
		assertEquals(0, assumptions.size());
		
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#previewBalancingTrade(long, long, java.util.List, java.util.List)},
	 * {@link MarkovEngineImpl#doBalanceTrade(long, Date, String, long, long, List, List)}.
	 * Note: this method will not test {@link MarkovEngineImpl#previewBalancingTrade(long, long, List, List)}
	 * directly, because {@link MarkovEngineImpl#doBalanceTrade(long, Date, String, long, long, List, List)}
	 * calls {@link MarkovEngineImpl#previewBalancingTrade(long, long, List, List)} internally.
	 */
	public final void testBalanceTrade() {
		
		// perform the same sequence of trades of testAddTradeInOneTransaction
		long transactionKey = engine.startNetworkActions();
		
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro

		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Tom"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			);
		
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			);
		

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Joe"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Amy"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			);
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Eric"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Eric bets P(E=e1) = .65 -> .8
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				(List)Collections.emptyList(), 
				(List)Collections.emptyList(), 
				false
			);
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				Collections.singletonList((long)0x0F), 
				Collections.singletonList(1), 
				false
		);
		
		// commit all trades (including the creation of network and user)
		engine.commitNetworkActions(transactionKey);
		
		// cannot reuse same transaction key
		try {
			engine.doBalanceTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, null, null);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// test invalid questions and assumptions
		for (String key : userNameToIDMap.keySet()) {
			// invalid question
			do {
				try {
					long questionId = (long) (Math.random()*Integer.MIN_VALUE);
					engine.previewBalancingTrade(
							userNameToIDMap.get(key), 
							questionId, 
							(List)((Math.random() < .5)?null:Collections.emptyList()), 
							(List)((Math.random() < .5)?null:Collections.emptyList())
						);
					fail(questionId + " should be an invalid question.");
				} catch (IllegalArgumentException e) {
					assertNotNull(e);	// ok
				}
			} while (Math.random() < .5);
			// invalid assumption
			do {
				try {
					// choose question 0x0D, 0x0E, or 0x0F randomly
					long questionId = (Math.random() < .33)?0x0D:((Math.random() < .5)?0x0E:0x0F);
					List<Long> assumptionIds = new ArrayList<Long>();
					// fill assumptionIds with invalid assumptions
					// assumption is invalid if assumptionIds contain questionId
					if (Math.random() < .5) {
						assumptionIds.add(questionId);
					}
					// assumption is invalid if it contains inexistent node
					do {
						assumptionIds.add((long)(Math.random()*Integer.MIN_VALUE));
					} while (Math.random() < .5);
					// fill valid states (the nodes have only 2 states - 0 or 1)
					List<Integer> assumedStates = new ArrayList<Integer>();
					for (int i = 0; i < assumptionIds.size(); i++) {
						assumedStates.add((Math.random() < .5)?0:1);
					}
					engine.previewBalancingTrade(
							userNameToIDMap.get(key), 
							questionId, 
							assumptionIds, 
							assumedStates
						);
					fail("Questions " + assumptionIds + " and states " + assumedStates + " are invalid assumptions of question " +  questionId);
				} catch (IllegalArgumentException e) {
					assertNotNull(e);	// ok
				}
			} while (Math.random() < .5);
		}
		
		// test valid balance trades
		
		
		
		// prepare the transaction which will make users to exit (balance) from question given assumptions
		transactionKey = engine.startNetworkActions();
		
		// Available users are Joe, Tom, Amy, Eric. For each user, randomly generate question|assumptions to balance
		Map<String, Long> userToQuestionToBalanceMap = new HashMap<String, Long>();					// map to store the question user has chosen
		Map<String, List<Long>> userToAssumptionMap = new HashMap<String, List<Long>>();			// map to store user's assumptions
		Map<String, List<Integer>> userToAssumedStatesMap = new HashMap<String, List<Integer>>();	// map to store user's assumed states
		
		// generate random balance requests for each user
		for (String user : userNameToIDMap.keySet()) {
			// choose question randomly from [0x0D, 0x0E, 0x0F]
			long question = ((Math.random() < .33)?0x0D:((Math.random() < .5)?0x0E:0x0F));
			userToQuestionToBalanceMap.put(user, question);
			
			// setup assumption randomly
			List<Long> assumptions = new ArrayList<Long>();
			if (question > 0x0D) {	
				// 0x0E and 0x0F can only have 1 assumption, which is 0x0D
				if (Math.random() < .5) {
					assumptions.add((long)0x0D);
				}
			} else {
				// 0x0D can have either 0x0E or 0x0F as assumptions, but never both simultaneously.
				if (Math.random() < .5) {
					/*
					 * Assume question + 1 (but choose 0x0D if question is 0x0F).
					 * E.g. if question == 0x0D, then assumption == 0x0E;
					 * if question == 0x0E, then assumption == 0x0F;
					 * if question == 0x0F, then assumption == 0x0D;
					 */
					assumptions.add(((question == 0x0F)?0x0D:(question+1)));
				} else if (Math.random() < .5) {	
					/*
					 * Assume question - 1 (but choose 0x0F if question is 0x0D).
					 * E.g. if question == 0x0D, then assumption == 0x0F;
					 * if question == 0x0E, then assumption == 0x0D;
					 * if question == 0x0F, then assumption == 0x0E;
					 */
					assumptions.add(((question == 0x0D)?0x0F:(question-1)));
				}
			}
			userToAssumptionMap.put(user, assumptions);
			
			// setup states of the assumptions correctly regarding assumptions
			List<Integer> states = new ArrayList<Integer>();
			for (int i = 0; i < assumptions.size(); i++) {
				states.add((Math.random() < .5)?0:1);	// nodes have supposedly 2 states: 0,1
			}
			userToAssumedStatesMap.put(user, states);
			
			// add trade into transaction
			engine.doBalanceTrade(transactionKey, new Date(), user + " balances question " + question, userNameToIDMap.get(user), question, assumptions, states);
		}
		
		// execute trade which will balance the assets of the users
		engine.commitNetworkActions(transactionKey);
		
		// check if users have quit the questions correctly
		for (String user : userNameToIDMap.keySet()) {
			// extract the user's choices from the maps
			long questionId = userToQuestionToBalanceMap.get(user);
			List<Long> assumptionIds = userToAssumptionMap.get(user);
			List<Integer> assumedStates = userToAssumedStatesMap.get(user);
			// The condition to exit (balance) from a trade is to make the assets to become the same for each states of a node.
			List<Float> assets = engine.getAssetsIfStates(userNameToIDMap.get(user), questionId, assumptionIds, assumedStates);
			// since assumedStates has the same size of assumptionIds, size of assets must be equal to quantity of states (i.e. 2)
			assertEquals("User = " + user + ", question = " + questionId + ", assumptions = " + assumptionIds + ", states = " + assumedStates, 2, assets.size() );
			// the asset values must be the same for all states if we did exit (balanced) correctly
			assertEquals(
					"User = " + user + ", question = " + questionId + ", assumptions = " + assumptionIds + ", states = " + assumedStates,
					assets.get(0), assets.get(1), ASSET_ERROR_MARGIN
				);
		}
		
		// check from the history that the balancing trades were successfully created
		HashSet<BalanceTradeNetworkAction> balanceTradesInHistory = new HashSet<MarkovEngineImpl.BalanceTradeNetworkAction>();
		for (Long questionId : new HashSet<Long>(userToQuestionToBalanceMap.values())) {
			for (QuestionEvent event : engine.getQuestionHistory(questionId, null, null)) {
				if (event instanceof BalanceTradeNetworkAction) {
					balanceTradesInHistory.add((BalanceTradeNetworkAction) event);
				}
			}
		}
		
		// we have created 1 trade per user
		assertEquals(userNameToIDMap.keySet().size(), balanceTradesInHistory.size());
		for (String user : userNameToIDMap.keySet()) {
			// check that trades of all users were actually treated
			boolean found = false;
			for (BalanceTradeNetworkAction action : balanceTradesInHistory) {
				if (action.getUserId().equals(userNameToIDMap.get(user))) {
					found = true;
					break;
				}
			}
			if (!found) {
				fail("Did not find balancing trade of user " + user);
			}
		}
		
		// 
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#resolveQuestion(long, java.util.Date, long, int)}.
	 */
	public final void testResolveQuestion() {
		
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		
		// check probs and assets before resolution D = d1
		
		// check that final marginal of E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		List<Float> probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		ArrayList<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		// init list of states of the assumptions
		ArrayList<Integer> assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// this map will store the conditional cashes for posterior comparison. 
		// E.g. Tom -> [Cash(d1,e1,f1),Cash(d1,e1,f2),Cash(d1,e2,f1),Cash(d1,e2,f2),Cash(d2,e1,f1),Cash(d2,e1,f2),Cash(d2,e2,f1),Cash(d2,e2,f2)]
		Map<String, List<Float>> mapOfConditionalCash = new HashMap<String, List<Float>>();
		
		// check that final min-q of Tom is 20
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Tom", new ArrayList<Float>());
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		
		// check that min-q of Amy is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Amy", new ArrayList<Float>());
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		
		// check that min-q of Joe is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Joe",new ArrayList<Float>());
		
		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check that final min-q of Eric is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Eric",new ArrayList<Float>());
		
		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// resolve to d1
		long transactionKey = engine.startNetworkActions();
		assertTrue(engine.resolveQuestion(transactionKey, new Date(), (long)0x0D, 0));
		assertTrue(engine.commitNetworkActions(transactionKey));
		
		// cannot reuse same transaction key
		try {
			engine.resolveQuestion(transactionKey, new Date(), (long)0x0E, 0);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		// assert that D is not accessible anymore
		try {
			probList = engine.getProbList((long)0x0D, null, null);
			// marginals of resolved questions can still be retrieved if engine is set to retrieve such values from history.
			if (!engine.isToObtainProbabilityOfResolvedQuestions()) {
				fail("D should not be present anymore");
			} else {
				// check that final marginal of D is [1, 0]
				assertEquals(2, probList.size());
				assertEquals(1f, probList.get(0), PROB_ERROR_MARGIN);
				assertEquals(0f, probList.get(1), PROB_ERROR_MARGIN);
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToObtainProbabilityOfResolvedQuestions()) {
				// marginals of resolved questions can still be retrieved if engine is set to retrieve such values from history.
				e.printStackTrace();
				fail(e.getMessage());
			} else {
				assertNotNull(e);
			}
		}
		try {
			probList = engine.getProbList((long)0x0E, Collections.singletonList((long)0x0D), null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// check that final marginal of E is [0.9509, 0.0491]
				assertEquals(4, probList.size());
				assertEquals(0.9509f, probList.get(0), PROB_ERROR_MARGIN);
				assertEquals(0.0491f, probList.get(1), PROB_ERROR_MARGIN);
				assertEquals(0f, probList.get(2), PROB_ERROR_MARGIN);
				assertEquals(0f, probList.get(3), PROB_ERROR_MARGIN);
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> assetsIfStates = engine.getAssetsIfStates(userId, (long)0x0D, null, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// make sure the impossible state has assets == 0
				assertEquals(2, assetsIfStates.size());
				assertTrue(Float.isInfinite(assetsIfStates.get(1)));	// 0 of q-value means -infinite assets
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> assetsIfStates = engine.getAssetsIfStates(userId, (long)0x0E, Collections.singletonList((long)0x0D), null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// make sure the impossible state has assets == 0
				assertEquals(4, assetsIfStates.size());
				assertTrue(Float.isInfinite(assetsIfStates.get(2)));	// 0 of q-value means -infinite assets
				assertTrue(Float.isInfinite(assetsIfStates.get(3)));	// 0 of q-value means -infinite assets
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			cash = engine.getCash(userId, Collections.singletonList((long)0x0D), Collections.singletonList(1));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// make sure the impossible state has assets == 0
				assertTrue("Cash = " + cash, Float.isInfinite(cash));	// 0 of q-value means -infinite assets
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> balancingTrade = engine.previewBalancingTrade(userId, (long)0x0D, null, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// make sure the impossible state has assets == 0
				assertNull("" + balancingTrade, balancingTrade);
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> balancingTrade = engine.previewBalancingTrade(userId, (long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(1));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				// make sure the impossible state has assets == 0
				assertNull("" + balancingTrade, balancingTrade);
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> newValues = new ArrayList<Float>();
			newValues.add((float) Math.random());	newValues.add(1-newValues.get(0));
			engine.previewTrade(userId, (long)0x0D, newValues, null, null);
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> newValues = new ArrayList<Float>();
			newValues.add((float) Math.random());	newValues.add(1-newValues.get(0));
			engine.previewTrade(userId, (long)((Math.random() < .5)?0x0E:0x0F), newValues, Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.getEditLimits(userId, (long)0x0D, (Math.random() < .5)?0:1, null, null);
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.getEditLimits(userId, (long)((Math.random() < .5)?0x0E:0x0F), (Math.random() < .5)?0:1, Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		try {
			List<Long> assumptions = engine.getPossibleQuestionAssumptions((long)0x0D, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				assertNotNull(assumptions);
				assertEquals(2, assumptions.size());
				assertTrue(assumptions.contains((long)0x0E));
				assertTrue(assumptions.contains((long)0x0F));
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			List<Long> assumptions = engine.getPossibleQuestionAssumptions((long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			} else {
				assertNotNull(assumptions);
				assertEquals(1, assumptions.size());
				assertTrue(assumptions.contains((long)0x0D));
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.scoreUserQuestionEv(userId, (long)0x0D, null, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.scoreUserQuestionEv(userId, (long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.scoreUserQuestionEvStates(userId, (long)0x0D, null, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.scoreUserQuestionEvStates(userId, (long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		
		// assert that D is not accessible in transactional methods as well
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			transactionKey = engine.startNetworkActions();
			List<Float> newValues = new ArrayList<Float>();
			newValues.add((float) Math.random());	newValues.add(1-newValues.get(0));
			engine.addTrade(transactionKey, new Date(), "To fail", userId, (long)0x0D, newValues, null, null, false);
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			engine.commitNetworkActions(transactionKey);
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			transactionKey = engine.startNetworkActions();
			List<Float> newValues = new ArrayList<Float>();
			newValues.add((float) Math.random());	newValues.add(1-newValues.get(0));
			engine.addTrade(transactionKey, new Date(), "To fail", userId, (long)((Math.random()<.5)?0x0E:0x0F), newValues, Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)), false);
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			engine.commitNetworkActions(transactionKey);
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			transactionKey = engine.startNetworkActions();
			engine.doBalanceTrade(transactionKey, new Date(), "To fail", userId, (long)0x0D, null, null );
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			engine.commitNetworkActions(transactionKey);
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			transactionKey = engine.startNetworkActions();
			engine.doBalanceTrade(transactionKey, new Date(), "To fail", userId, (long)((Math.random()<.5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			engine.commitNetworkActions(transactionKey);
			assertNotNull(e);
		}
		
		
		// history and score detail/summary can be accessed normally
		assertFalse(engine.getQuestionHistory((long) 0x0D, null, null).isEmpty());
		assertFalse(engine.getScoreDetails(userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy"), (long)0x0D, null, null).isEmpty());
		assertNotNull(engine.getScoreSummary(userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy"), (long)0x0D, null, null));
		
		// revert trade should be OK
		transactionKey = engine.startNetworkActions();
		engine.revertTrade(transactionKey, new Date(), new Date(new Date().getTime() + 1), (long)0x0D);	// use future date, so that nothing is actually reverted
		engine.commitNetworkActions(transactionKey);
		
		
		// check that final marginal of E is [0.9509, 0.0491], F is  [0.2416, 0.7584]
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.9509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.0491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2416f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7584f, probList.get(1), PROB_ERROR_MARGIN);
		if (engine.isToObtainProbabilityOfResolvedQuestions()) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(1f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0f, probList.get(1), PROB_ERROR_MARGIN);
		} else {
			engine.setToObtainProbabilityOfResolvedQuestions(true);
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(1f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0f, probList.get(1), PROB_ERROR_MARGIN);
			engine.setToObtainProbabilityOfResolvedQuestions(false);
		}
		
		// test that conditional cash matches the values before resolution
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0E);	assumptionIds.add((long)0x0F);
		if (!engine.isToDeleteResolvedNode()) {
			assumptionIds.add((long)0x0D);
		}
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0); assumedStates.add(0); 
		if (!engine.isToDeleteResolvedNode()) {
			assumedStates.add(0);
		}
		for (String userName : mapOfConditionalCash.keySet()) {
			assumedStates.set(2, 0);	// d1
			for (int i = 0; i < 4; i++) {	// combinations [e1f1,e1f2,e2f1,e2f2]
				assumedStates.set(0, (int)i/2);	// e
				assumedStates.set(1, (int)i%2);	// f
				cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
				assertEquals("[" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2, mapOfConditionalCash.get(userName).get(i), cash);
			}
			if (!engine.isToDeleteResolvedNode()) {
				assumedStates.set(2, 1);	// d2
				for (int i = 0; i < 4; i++) {	// combinations [e1f1d2,e1f2d2,e2f1d2,e2f2d2]
					assumedStates.set(0, (int)i/2);	// e
					assumedStates.set(1, (int)i%2);	// f
					cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
					assertTrue("[" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2 + ", cash = " + cash, Float.isInfinite(cash));
				}
			} 
		}
	}

	private List<AddTradeNetworkAction> createDEFNetIn1Transaction(Map<String, Long> userNameToIDMap) {
		// crate transaction
		long transactionKey = engine.startNetworkActions();
		
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro

		// Let's use ID = 0 for the user Tom 
//		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Tom"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		
		// Tom bets  P(E=e1) = 0.5  to 0.55 and P(E=e1|D=d1) = .55 -> .9 simultaneously
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		List<Float> newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		newValues.add(.55f);
		newValues.add(.45f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.55, and P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				null, 
				false
			);
		

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Joe"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Amy"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(0), 
				false
			);
		

		// Joe bets P(F=f1|D=d2) = .5 -> .1
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				Collections.singletonList((long)0x0D), 
				Collections.singletonList(1), 
				false
			);
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		
		try {
			// Cannot obtain cash from user before network was initialized (Note: we did not commit the transaction yet, so the network is empty now)
			engine.getCash(userNameToIDMap.get("Eric"), null, null);
			fail("Engine should not allow us to access any data without initializing Bayesian network properly.");
		} catch (IllegalStateException e) {
			// OK. This is the expected
			assertNotNull(e);
		}
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		
		// Eric bets P(E=e1) = .65 -> .8
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				(List)Collections.emptyList(), 
				(List)Collections.emptyList(), 
				false
			);
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				Collections.singletonList((long)0x0F), 
				Collections.singletonList(1), 
				false
		);
		
		// commit all trades (including the creation of network and user)
		engine.commitNetworkActions(transactionKey);
		
//		return userNameToIDMap;
		Set<QuestionEvent> questionHistory = new HashSet<QuestionEvent>(engine.getQuestionHistory((long)0x0D, null, null));
		questionHistory.addAll(engine.getQuestionHistory((long)0x0E, null, null));
		questionHistory.addAll(engine.getQuestionHistory((long)0x0F, null, null));
		List<AddTradeNetworkAction> ret = new ArrayList<MarkovEngineImpl.AddTradeNetworkAction>();
		for (QuestionEvent questionEvent : questionHistory) {
			if (questionEvent instanceof AddTradeNetworkAction) {
				ret.add((AddTradeNetworkAction) questionEvent);
			}
		}
		return ret;
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#revertTrade(long, java.util.Date, java.lang.Long, java.lang.Long)}.
	 */
	public final void testRevertTrade() {
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		List<AddTradeNetworkAction> trades = this.createDEFNetIn1Transaction(userNameToIDMap );
		assertEquals(6, trades.size());
		
		// add a dummy node and dummy trade, just to add trade to a node which does not change probs of other nodes.
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0CL, 2,null);
		List<Float> newValues = new ArrayList<Float>();
		newValues.add(0.55f); newValues.add(0.45f);
		
		// store important info about dummy trade
		Date tradesStartingWhen = new Date();
		Long userWhoMadeTradeOnC = userNameToIDMap.get((Math.random() < .25)?"Tom":(Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":"Amy");
		assertTrue("User id = " + userWhoMadeTradeOnC, userNameToIDMap.containsValue(userWhoMadeTradeOnC));
		
		// do trade
		engine.addTrade(transactionKey, tradesStartingWhen, "", userWhoMadeTradeOnC, 0x0CL, newValues , null, null, true);
		
		// add edge after trade
		if (Math.random() < .5) {
			// from 0 to some node
			if (Math.random() < .5) {
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0DL, Collections.singletonList(0x0CL), null);
				System.out.println("C->D");
			}
			if (Math.random() < .5) {
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0EL, Collections.singletonList(0x0CL), null);
				System.out.println("C->E");
			}
			if (Math.random() < .5) {
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0FL, Collections.singletonList(0x0CL), null);
				System.out.println("C->F");
			}
		} else {
			// from some node to 0
			List<Long> assumptions = new ArrayList<Long>();
			if (Math.random() < .5) {
				assumptions.add(0x0DL);
				System.out.println("D->C");
			}
			if (Math.random() < .5) {
				assumptions.add(0x0EL);
				System.out.println("E->C");
			}
			if (Math.random() < .5) {
				assumptions.add(0x0FL);
				System.out.println("F->C");
			}
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0CL, assumptions, null);
		}
		engine.commitNetworkActions(transactionKey);
		
		// revert the last trade (actually, the only trade).
		transactionKey = engine.startNetworkActions();
		engine.revertTrade(transactionKey, new Date(), new Date(0), 0x0CL);	// search by question
		engine.commitNetworkActions(transactionKey);
		
		// check that the trade was reverted succesfully.
		
		// check that assets of question C is equal for everyone
		for (String userName : userNameToIDMap.keySet()) {
			List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0CL, null, null);
			assertEquals(userName, 2, assetsIfStates.size());
			assertEquals("User who made edit on C = " + userWhoMadeTradeOnC + ", current = " + userName + "(" + userNameToIDMap.get(userName)+ ") " + assetsIfStates, 
					engine.getQValuesFromScore(assetsIfStates.get(0)), 
					engine.getQValuesFromScore(assetsIfStates.get(1)),
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that final marginal of C is [0.55, 0.45] E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		List<Float> probList = engine.getProbList(0x0C, null, null);
		assertEquals(0.55f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.45f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0D, null, null);
		assertEquals(0.7232f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.8509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2165f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probList.get(1), PROB_ERROR_MARGIN);
		
		// set assumptions to D,E,F, so that we can use it to calculate conditional min-q (in order to test consistency of LPE)
		ArrayList<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0E);		// 2nd node is E; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		// init list of states of the assumptions
		ArrayList<Integer> assumedStates = new ArrayList<Integer>();	
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// e1
		assumedStates.add(0);	// f1
		
		// this map will store the conditional cashes for posterior comparison. 
		// E.g. Tom -> [Cash(d1,e1,f1),Cash(d1,e1,f2),Cash(d1,e2,f1),Cash(d1,e2,f2),Cash(d2,e1,f1),Cash(d2,e1,f2),Cash(d2,e2,f1),Cash(d2,e2,f2)]
		Map<String, List<Float>> mapOfConditionalCash = new HashMap<String, List<Float>>();
		
		
		// check that final min-q of Tom is 20
		float minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(20f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Tom", new ArrayList<Float>());
		
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d1, e1, f2 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e1, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);
		
		// check combination d2, e1, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e2, f1 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		// check combination d2, e2, f2 (not min)
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Tom").add(cash);

		
		// check that min-q of Amy is 60...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(60f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Amy", new ArrayList<Float>());
		
		// check that LPE of Amy contains d1, f1 and any value E
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Amy").add(cash);
		
		
		// check that min-q of Joe is 14.5454545...
		minCash = engine.getCash(userNameToIDMap.get("Joe"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(14.5454545f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Joe",new ArrayList<Float>());

		// check that LPE of Joe contains d2, e1, f1
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);

		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Joe"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Joe").add(cash);
		
		// check that final min-q of Eric is 35.7393...
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertEquals(Math.round(engine.getScoreFromQValues(35.7393f)), Math.round(minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, Math.round(engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Eric",new ArrayList<Float>());

		// check that final LPE of Eric is d2, e2 and f2
		
		// check combination d1, e1, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d1, e2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// check combination d2, e2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// e2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Eric"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		mapOfConditionalCash.get("Eric").add(cash);
		
		// resolve to d1. Resolutions are not reverted (i.e. resolutions are supposedly re-done)
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.resolveQuestion(transactionKey, new Date(), (long)0x0D, 0));
		assertTrue(engine.commitNetworkActions(transactionKey));
		
		
		// store assetIfs in order to check them after next revert
		Map<String, List<Float>> mapOfConditionalAssetsE = new HashMap<String, List<Float>>();
		Map<String, List<Float>> mapOfConditionalAssetsF = new HashMap<String, List<Float>>();
		
		// fill mapOfConditionalAssets for Tom
		mapOfConditionalAssetsE.put("Tom", engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0EL, null, null));
		mapOfConditionalAssetsF.put("Tom", engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0FL, null, null));
		// fill mapOfConditionalAssets for Amy
		mapOfConditionalAssetsE.put("Amy", engine.getAssetsIfStates(userNameToIDMap.get("Amy"), 0x0EL, null, null));
		mapOfConditionalAssetsF.put("Amy", engine.getAssetsIfStates(userNameToIDMap.get("Amy"), 0x0FL, null, null));
		// fill mapOfConditionalAssets for Amy
		mapOfConditionalAssetsE.put("Joe", engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0EL, null, null));
		mapOfConditionalAssetsF.put("Joe", engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0FL, null, null));
		// fill mapOfConditionalAssets for Eric
		mapOfConditionalAssetsE.put("Eric", engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0EL, null, null));
		mapOfConditionalAssetsF.put("Eric", engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0FL, null, null));
		
		// revert the last trade of C again (actually, the only trade). This time, only specifying the date
		transactionKey = engine.startNetworkActions();
		engine.revertTrade(transactionKey, new Date(), tradesStartingWhen, null);	// search by date
		engine.commitNetworkActions(transactionKey);
		
		// make sure assets and probs are the same of the reverted one, and probabilities are the same after resolution.
		
		// check that final marginal of E is [0.9509, 0.0491], F is  [0.2416, 0.7584]
		probList = engine.getProbList(0x0E, null, null);
		assertEquals(0.9509f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.0491f, probList.get(1), PROB_ERROR_MARGIN);
		probList = engine.getProbList(0x0F, null, null);
		assertEquals(0.2416f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7584f, probList.get(1), PROB_ERROR_MARGIN);
		if (engine.isToObtainProbabilityOfResolvedQuestions()) {
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(1f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0f, probList.get(1), PROB_ERROR_MARGIN);
		} else {
			engine.setToObtainProbabilityOfResolvedQuestions(true);
			probList = engine.getProbList(0x0D, null, null);
			assertEquals(1f, probList.get(0), PROB_ERROR_MARGIN);
			assertEquals(0f, probList.get(1), PROB_ERROR_MARGIN);
			engine.setToObtainProbabilityOfResolvedQuestions(false);
		}
		
		// test that conditional cash matches the values before resolution
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long)0x0E);	assumptionIds.add((long)0x0F);
		if (!engine.isToDeleteResolvedNode()) {
			assumptionIds.add((long)0x0D);
		}
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0); assumedStates.add(0); 
		if (!engine.isToDeleteResolvedNode()) {
			assumedStates.add(0);
		}
		for (String userName : mapOfConditionalCash.keySet()) {
			assumedStates.set(2, 0);	// d1
			for (int i = 0; i < 4; i++) {	// combinations [e1f1,e1f2,e2f1,e2f2]
				assumedStates.set(0, (int)i/2);	// e
				assumedStates.set(1, (int)i%2);	// f
				cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
				assertEquals("[" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2, mapOfConditionalCash.get(userName).get(i), cash);
			}
			if (!engine.isToDeleteResolvedNode()) {
				assumedStates.set(2, 1);	// d2
				for (int i = 0; i < 4; i++) {	// combinations [e1f1d2,e1f2d2,e2f1d2,e2f2d2]
					assumedStates.set(0, (int)i/2);	// e
					assumedStates.set(1, (int)i%2);	// f
					cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
					assertTrue("[" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2 + ", cash = " + cash, Float.isInfinite(cash));
				}
			} 
		}
		
		// check that assets of question C is equal for everyone
		for (String userName : userNameToIDMap.keySet()) {
			List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0CL, null, null);
			assertEquals(userName, 2, assetsIfStates.size());
			assertEquals("User who made edit on C = " + userWhoMadeTradeOnC + ", current = " + userName + "(" + userNameToIDMap.get(userName)+ ") " + assetsIfStates, 
					engine.getQValuesFromScore(assetsIfStates.get(0)), 
					engine.getQValuesFromScore(assetsIfStates.get(1)),
					ASSET_ERROR_MARGIN
				);
		}
		
		// check that assetsIfStates matches the ones before resolution
		for (String userName : userNameToIDMap.keySet()) {
			assertEquals(userName ,mapOfConditionalAssetsE.get(userName), engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0EL, null, null));
			assertEquals(userName ,mapOfConditionalAssetsF.get(userName), engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0FL, null, null));
		}
	}

	


	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#previewTrade(long, long, java.util.List, java.util.List, java.util.List, java.util.List)}.
	 * It just tests whether {@link MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}
	 * and then {@link MarkovEngineImpl#getAssetsIfStates(long, long, List, List)}
	 * is equivalent to {@link MarkovEngineImpl#previewTrade(long, long, List, List, List)}.
	 */
	public final void testPreviewTrade() {
		
		// crate transaction for generating the DEF network
		long transactionKey = engine.startNetworkActions();
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// commit changes
		engine.commitNetworkActions(transactionKey);
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
		// Tom bets P(E=e1) = 0.5  to 0.55 (unconditional soft evidence in E)
		
		// check whether probability prior to edit is really 0.5
		List<Float> probList = engine.getProbList(0x0E, null, null);
		assertEquals(2 , probList.size());
		assertEquals(0.5f , probList.get(0) , PROB_ERROR_MARGIN);
		assertEquals(0.5f , probList.get(1) , PROB_ERROR_MARGIN);
		
		
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		
		// get preview
		List<Float> preview = engine.previewTrade(userNameToIDMap.get("Tom"), 0x0E, newValues, null, null);
		assertEquals(2, preview.size());
		
		// do edit
		transactionKey = engine.startNetworkActions();
		List<Float> returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0E, null, null), preview);
		
		// Tom bets P(E=e1|D=d1) = .55 -> .9
		
		List<Long> assumptionIds = new ArrayList<Long>();		
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		assumedStates.add(0);	// set d1 as assumed state
		
		
		// set P(E=e1|D=d1) = 0.9 and P(E=e2|D=d1) = 0.1
		
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		
		preview = engine.previewTrade(userNameToIDMap.get("Tom"), 0x0E, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0E, assumptionIds, assumedStates), preview);

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

		// Joe bets P(E=e1|D=d2) = .55 -> .4
		
		// check whether probability prior to edit is really [e1d1, e2d1, e1d2, e2d2] = [.9, .1, .55, .45]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	// assumption = D
		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(E=e1|D=d2) should be [0.0055, 0.9955]
		
		// set P(E=e1|D=d2) = 0.4 and P(E=e2|D=d2) = 0.6
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		
		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Joe"), 0x0E, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0E, assumptionIds, assumedStates), preview);
		

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

		// Amy bets P(F=f1|D=d1) = .5 -> .3
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.5, .5, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		assumedStates.add(0);	// set d1 as assumed state
		
		
		// set P(F=f1|D=d1) = 0.3 and P(F=f2|D=d1) = 0.7  
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		

		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Amy"), 0x0F, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Amy"), 0x0F, assumptionIds, assumedStates), preview);
		
		// Joe bets P(F=f1|D=d2) = .5 -> .1
		
		// check whether probability prior to edit is really [f1d1, f2d1, f1d2, f2d2] = [.3, .7, .5, .5]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0D);	
		assumedStates.add(1);	// set d2 as assumed state
		
		
		// set P(F=f1|D=d2) = 0.1 and P(F=f2|D=d2) = 0.9
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		

		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Joe"), 0x0F, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0F, assumptionIds, assumedStates), preview);
		

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, Math.round(engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals(Math.round(engine.getScoreFromQValues(100)), Math.round(engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
		// Eric bets P(E=e1) = .65 -> .8
		
		// check whether probability prior to edit is really = [.65, .35]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		
		// set P(E=e1) = 0.8 and P(E=e2) = 0.2
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		

		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Eric"), 0x0E, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0E, assumptionIds, assumedStates), preview);
		
		
		// Eric bets  P(D=d1|F=f2) = 0.52 -> 0.7
		
		// check whether probability prior to edit is really [d1f2, d2f2] = [.52, .48]
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		
		
		// set P(D=d1|F=f2) = 0.7 and P(D=d2|F=f2) = 0.3
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		

		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Eric"), 0x0D, newValues, assumptionIds, assumedStates);		
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
		);
		engine.commitNetworkActions(transactionKey);
		
		// check if preview and value returned from trade matches
		assertEquals(returnFromTrade.size(), preview.size());
		assertEquals(returnFromTrade, preview);
		// check if preview matches current getAssetsIf
		assertEquals(engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0D, assumptionIds, assumedStates), preview);
		

		// Eric makes a bet which makes his assets-q to go below 1, but the algorithm does not allow it
		// this is a case in which preview != actual (because the actual trade will not be executed)
		
		// extract allowed interval of P(D=d1|F=f2), so that we can an edit incompatible with such interval
		assumptionIds = new ArrayList<Long>();		
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0x0F);	
		assumedStates.add(1);	// set f2 as assumed state
		List<Float> editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0D, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0091059f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9916058f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		

		// obtain preview
		preview = engine.previewTrade(userNameToIDMap.get("Eric"), 0x0D, newValues, assumptionIds, assumedStates);
		// check that some of the preview has 0 or negative assets
		try {
			boolean found = false;
			for (Float previewedAsset : preview) {
				if (previewedAsset <= 0) {
					found = true;
					break;
				}
			}
			assertTrue("Previewed asset is " + preview, found);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		List<Float> assetsIfBeforeNegativeTrade = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0D, assumptionIds, assumedStates);
		
		transactionKey = engine.startNetworkActions();
		returnFromTrade = engine.addTrade(
				transactionKey, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false	// do not allow negative assets
			);
		
		// engine.addTrade must return nothing when previewed value is negative and it is not allowed to use negative assets
		assertNull(returnFromTrade);
		
		// by committing an empty transaction, we can remove transaction from memory
		engine.commitNetworkActions(transactionKey);
		
		// check if preview does not match current getAssetsIf (which is )
		List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0D, assumptionIds, assumedStates);
		assertEquals(assetsIfBeforeNegativeTrade, assetsIfStates);
		assertFalse("Preview = " + preview + ", returned = " + assetsIfStates, assetsIfStates.equals(preview));

	}


	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)}.
	 */
	public final void testGetQuestionHistory() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#scoreUserEV(java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testScoreUserEV() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getScoreSummary(long, List, List)}.
	 */
	public final void testGetScoreSummary() {
		fail("Not yet implemented"); // TODO
	}

	// not needed for the 1st release
//	/**
//	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getScoreDetails(long, java.util.List, java.util.List)}.
//	 */
//	public final void testGetScoreDetails() {
//		fail("Not yet implemented"); // TODO
//	}
	


//	/**
//	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#scoreUserQuestionEv(long, java.lang.Long, java.util.List, java.util.List)}.
//	 */
//	public final void testScoreUserQuestionEv() {
//		fail("Not yet implemented"); // TODO
//	}


}
