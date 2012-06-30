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
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addCash(long, java.util.Date, long, float, java.lang.String)}.
	 */
	public final void testAddCash() {
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
		
		// global cash should be 0 initially (TODO check if this is really true)
		assertEquals(0f, engine.getCash(1, null, null), ASSET_ERROR_MARGIN);
		
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

		// 1 = 0, 0 = null, 2 = null (equivalent to only 1 = 0)
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(null);	// node 0, state null
		assumptionIds.add((long) 1); assumedStates.add(0);		// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(null);	// node 2, state null
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 0, 0 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);

		// 2 = 1, 0 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assumptionIds.add((long) 0); assumedStates.add(1);	// node 0, state 1
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 1, 2 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(1);	// node 1, state 1
		assumptionIds.add((long) 2); assumedStates.add(0);	// node 2, state 0
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 0 = 0, 1 = 0, 2 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assertEquals(0f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// add 100 cash
		transactionKey = engine.startNetworkActions();
		engine.addCash(transactionKey, new Date(), 1, 100f, "Just to test");
		engine.commitNetworkActions(transactionKey);
		

		// global cash should be 100 now
		assertEquals(100f, engine.getCash(1, null, null), ASSET_ERROR_MARGIN);
		
		// test some conditional cash as well
		// non-null conditions
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);

		// 1 = 0, 0 = null, 2 = null (equivalent to only 1 = 0)
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(null);	// node 0, state null
		assumptionIds.add((long) 1); assumedStates.add(0);		// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(null);	// node 2, state null
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 0, 0 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);

		// 2 = 1, 0 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assumptionIds.add((long) 0); assumedStates.add(1);	// node 0, state 1
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 1 = 1, 2 = 0
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 1); assumedStates.add(1);	// node 1, state 1
		assumptionIds.add((long) 2); assumedStates.add(0);	// node 2, state 0
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// 0 = 0, 1 = 0, 2 = 1
		assumptionIds = new ArrayList<Long>();
		assumedStates = new ArrayList<Integer>();
		assumptionIds.add((long) 0); assumedStates.add(0);	// node 0, state 0
		assumptionIds.add((long) 1); assumedStates.add(0);	// node 1, state 0
		assumptionIds.add((long) 2); assumedStates.add(1);	// node 2, state 1
		assertEquals(100f, engine.getCash(1, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		
		// TODO test cash after trades
	}

	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, java.util.Date, long, long, long, java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Boolean)}.
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
//		assumptionIds.add((long) 0x0D);	
		probList = engine.getProbList(0x0E, assumptionIds, null);
		assertEquals(2, probList.size());
		assertEquals(0.65f , probList.get(0),PROB_ERROR_MARGIN );
		assertEquals(0.35f , probList.get(1),PROB_ERROR_MARGIN );
		
//		assumedStates.add(1);	// set d2 as assumed state
		
		// edit interval of P(E=e1) should be [0.0065, 0.9965]
		editInterval = engine.getEditLimits(userNameToIDMap.get("Eric"), 0x0E, 0, assumptionIds, assumedStates);
		assertNotNull(editInterval);
		assertEquals(2, editInterval.size());
		assertEquals(0.0065f, editInterval.get(0) ,PROB_ERROR_MARGIN);
		assertEquals(0.9965f, editInterval.get(1) ,PROB_ERROR_MARGIN);
		
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
				false	// do not allow negative assets
			);
		try {
			engine.commitNetworkActions(transactionKey);
			fail("Should have thrown ZeroAssetsException, because assets went to negative");
		} catch (ZeroAssetsException e) {
			// ok
			assertNotNull(e);
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
		
		// check that cash is smaller or equal to 0
		minCash = engine.getCash(userNameToIDMap.get("Eric"), null, null);
		assertTrue("Obtained unexpected cash = " + minCash, minCash <= 0);
		
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
