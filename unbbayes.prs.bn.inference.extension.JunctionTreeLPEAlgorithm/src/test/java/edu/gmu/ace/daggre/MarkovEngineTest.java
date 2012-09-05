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
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.inference.extension.ZeroAssetsException;
import unbbayes.prs.exception.InvalidParentException;
import edu.gmu.ace.daggre.MarkovEngineImpl.AddTradeNetworkAction;
import edu.gmu.ace.daggre.MarkovEngineImpl.BalanceTradeNetworkAction;
import edu.gmu.ace.daggre.MarkovEngineImpl.InexistingQuestionException;
import edu.gmu.ace.daggre.ScoreSummary.SummaryContribution;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineTest extends TestCase {
	
	private static final int THREAD_NUM = 1;//75;	// quantity of threads to use in order to test multi-thread behavior

	public static final int MAX_NETWIDTH = 3;
	public static final int MAX_STATES = 5;
	public static final int MIN_STATES = 2;
	
	/** Error margin used when comparing 2 probability values */
	public static final float PROB_ERROR_MARGIN = 0.005f;

	/** Error margin used when comparing 2 asset (score) values */
	public static final float ASSET_ERROR_MARGIN = .5f;
	
	private MarkovEngineImpl engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance((float)Math.E, (float)(10.0/Math.log(100)), 0);

	private boolean isToUseQValues = false;

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
		engine.setToUseQValues(isToUseQValues());
		engine.setCurrentLogBase((float) Math.E);
		engine.setCurrentCurrencyConstant((float) (10/Math.log(100)));
		engine.setDefaultInitialAssetTableValue(0f);
		engine.setToReturnEVComponentsAsScoreSummary(false);
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
		
		// check consistency of marginal probabilities
		Map<Long, List<Float>> probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
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
		
		// check consistency of marginal probabilities
		probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
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
				List<Long> newNodes = new ArrayList<Long>();
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
								newNodes.add(nodeID);
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
										// origin should always be smaller than anything in destination
										try {
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
										} catch (InexistingQuestionException e) {
											i--;
											continue;
//											e.printStackTrace();
										}
									}
								}
							}
						}
				}
				}
				if (isToCommitTransaction) {
					// transactionKey was null -> several threads + several transactions case
					try {
						engine.commitNetworkActions(transactionKey);
					} catch (Exception e) {
						generatedNodes.removeAll(newNodes);
						e.printStackTrace();
					}
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
		
		// check consistency of marginal probabilities
		Map<Long, List<Float>> probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
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
		
		// check consistency of marginal probabilities
		probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
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
		
		// check consistency of marginal probabilities
		probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
		}
		
		
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
		
		// check consistency of marginal probabilities
		probLists = engine.getProbLists(null, null, null);
		assertNotNull(probLists);
		assertEquals(engine.getProbabilisticNetwork().getNodeCount(), probLists.size());
		for (Long questionId : probLists.keySet()) {
			// check consistency of marginal prob value
			List<Float> prob = probLists.get(questionId);
			assertNotNull("Question " + questionId, prob);
			assertFalse("Question " + questionId + " = " + prob,prob.isEmpty());
			float sum = 0.0f;
			for (Float value : prob) {
				assertTrue("Question " + questionId + " = " + prob, value >= 0.0f);
				assertTrue("Question " + questionId + " = " + prob, value <= 1.0f);
				sum += value;
			}
			assertEquals("Question " + questionId + " = " + prob, 1.0f, sum, PROB_ERROR_MARGIN);
		}
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
		
		float INITIAL_ASSETS = 1000.0f;

		MarkovEngineImpl me = (MarkovEngineImpl) MarkovEngineImpl.getInstance();
		((MarkovEngineImpl)me).setCurrentLogBase(2);
		((MarkovEngineImpl)me).setCurrentCurrencyConstant(100);
		((MarkovEngineImpl)me).setDefaultInitialAssetTableValue((float) ((MarkovEngineImpl)me).getQValuesFromScore(INITIAL_ASSETS));
		
		transactionKey = me.startNetworkActions();
		me.addQuestion(transactionKey, new Date(), 1L, 2, null);
		me.commitNetworkActions(transactionKey);
		
		assertFalse(Double.isInfinite(me.getCash(2L, null, null)));
		
		// TODO test disconnected network cases
		
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
	 *	Trade-1: Tom would like to make a bet on E=e1, that has current probability as 0.5. First of all, we need to calculate Tom’s edit limit (in this case, there is no assumption):	<br/> 
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
	 *	Tom’s min-q is 90, at the following 4 min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     1     2     1	<br/>
	 *	     1     2     2	<br/>
	 *	     2     2     1	<br/>
	 *	     2     2     2	<br/>
	 *	<br/>	
	 *	<br/>	
	 *	Trade-2: Now Tom would like to make another conditional bet on E=e1 given D=d1 (current P(E=e1|D=d1) = 0.55). Again, let us calculate his edit limits first (in this case, we have assumed variable D=d1). And note that Tom’s asset tables are not the initial ones any more, but updated from last trade he did, now:	<br/> 
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
	 *	Tom’s min-q is 20, at the following two min-states (found by min-asset-propagation):	<br/> 
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
	 *	Joe’s min-q is 72.72727272727..., at the following two min-states (found by min-asset-propagation):	<br/> 
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
	 *	Amy’s min-q is 60, at the following two min-states (found by min-asset-propagation):	<br/> 
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
	 *	Joe’s min-q is 14.54545454546, at the following unique min-states (found by min-asset-propagation):	<br/> 
	 *	     D    E    F	<br/>
	 *	     2     1    1	<br/>
	 *	<br/>	
	 *	At this point, the model DEF reaches the status that has the same CPTs as the starting CPTs for the experimental model DEF we used in our AAAI 2012 paper.	<br/> 
	 *	<br/>	
	 *	From now on, we run test cases described in the paper.	<br/> 
	 *	<br/>	
	 *	Trade-6: Eric would like to trade on P(E=e1), which is currently 0.65.	<br/> 
	 *	To decide long or short, S(E=e1) = 10, S(E~=e1)=10, no difference because this will be Eric’s first trade.	<br/>
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
	 *	Eric’s expected score is S=10.1177.	<br/>
	 *	Eric’s min-q is 57.142857, at the following two min-states (found by min-asset-propagation):	<br/> 
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
	 *	Eric’s expected score is now 10.31615.	<br/> 
	 *	Eric’s min-q is 35.7393, at the following unique min-states (found by min-asset-propagation):	<br/> 
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
		assertEquals(0, engine.getCash(userNameToIDMap.get("Tom"), null, null), ASSET_ERROR_MARGIN);
		assertEquals(1, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
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
		
		// check that no one has made any trade on any question yet.
		for (String user : userNameToIDMap.keySet()) {
			assertEquals(user, 0, engine.getTradedQuestions(userNameToIDMap.get(user)).size());
		}
		
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
		
		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(1, engine.getTradedQuestions(userNameToIDMap.get("Tom")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(0).longValue());
		
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
		assertEquals((engine.getScoreFromQValues(90f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		
		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(1, engine.getTradedQuestions(userNameToIDMap.get("Tom")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(0).longValue());
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(1, engine.getTradedQuestions(userNameToIDMap.get("Joe")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Joe")).get(0).longValue());
		
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
		assertEquals((engine.getScoreFromQValues(72.727272f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(1, engine.getTradedQuestions(userNameToIDMap.get("Amy")).size());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Amy")).get(0).longValue());
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Joe")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Joe")).get(0).longValue());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Joe")).get(1).longValue());
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(1, engine.getTradedQuestions(userNameToIDMap.get("Eric")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(0).longValue());
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		
		assertEquals(10.1177, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(57.142857f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(57.142857f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Eric")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(0).longValue());
		assertEquals(0x0Dl, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(1).longValue());
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "To fail", Long.MAX_VALUE, (long)0x0D, newValues, null, null, false);
			fail("Should not allow to use commited transaction");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}

		assertEquals(10.31615, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		

		// check that the questions that can be retrieved from getTradedQuestions are still the same.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Eric")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(0).longValue());
		assertEquals(0x0Dl, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(1).longValue());

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// cannot reuse same transaction key
		try {
			newValues = new ArrayList<Float>(2);
			newValues.add(.9f);	newValues.add(.1f);
			engine.addTrade(transactionKey, new Date(), "OK", Long.MIN_VALUE, (long)0x0D, newValues, null, null, false);
			fail("It's not be supposed to reuse a commited transaction.");
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// add question disconnected question C
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), (long)0x0C, 2, null);
		engine.commitNetworkActions(transactionKey);
		

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Amy")).size());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Amy")).get(0).longValue());
		assertEquals(0x0Cl, engine.getTradedQuestions(userNameToIDMap.get("Amy")).get(1).longValue());
		
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
		
		// check that min-q is 6...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals((engine.getScoreFromQValues(6f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(6f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that new LPE of Amy is independent of E
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(0)), ASSET_ERROR_MARGIN);
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(1)), ASSET_ERROR_MARGIN);
		
		// check that LPE is d1 c1 f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0C);		// 2nd node is C; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, c1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// c1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, c1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, c2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, c2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, c1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check incomplete condition of LPE: c1
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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Eric")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(0).longValue());
		assertEquals(0x0Dl, engine.getTradedQuestions(userNameToIDMap.get("Eric")).get(1).longValue());
		
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
	
		Map<Long, List<Float>> probListsBeforeTrade = engine.getProbLists(null, null, null);
		
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
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Amy")).size());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Amy")).get(0).longValue());
		assertEquals(0x0Cl, engine.getTradedQuestions(userNameToIDMap.get("Amy")).get(1).longValue());
		
		// probability of nodes present before this transaction must remain unchanged
		Map<Long, List<Float>> probListsAfterTrade = engine.getProbLists(null, null, null);
		for (Long id : probListsBeforeTrade.keySet()) {
			assertEquals("question = " + id , probListsBeforeTrade.get(id).size(), probListsAfterTrade.get(id).size());
			for (int i = 0; i < probListsBeforeTrade.get(id).size(); i++) {
				assertEquals("Question = " + id , probListsBeforeTrade.get(id).get(i), probListsAfterTrade.get(id).get(i), PROB_ERROR_MARGIN);
			}
		}
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		assumedStates = new ArrayList<Integer>();
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0DL); assumptionIds.add(0x0EL); assumptionIds.add(0x0FL); 
		
		// check combination d1, e1, f1 (not min)
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
		
		try {
			List<Long> questionIds = new ArrayList<Long>();
			questionIds.add(13L); questionIds.add(14L); questionIds.add(15L); 
			questionIds.add(12L); questionIds.add(10L);
			List<Integer> states = new ArrayList<Integer>();
			states.add(0); states.add(0); states.add(0); 
			states.add(0); states.add(0);
			for (int i = 0; i < 32; i++) {
				states.set(0, i%2); states.set(1,(i/2)%2); states.set(2,(i/4)%2); 
				states.set(3,(i/16)%2); states.set(4,(i/32)%2);
//				System.out.println(engine.getJointProbability(questionIds, states));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// test invalid assumptions
		transactionKey = engine.startNetworkActions();
		
		// add a in which the assumptions will be ignored
		newValues = new ArrayList<Float>();
		newValues.add(.5f); newValues.add(.5f); 
		assertFalse( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(F|E = e2) = [.5,.5]", 
				userNameToIDMap.get("Tom"), 
				0x0FL, 
				newValues, 
				Collections.singletonList(0x0EL), 
				Collections.singletonList(1), 
				false
			).isEmpty());
		engine.commitNetworkActions(transactionKey);
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(2, engine.getTradedQuestions(userNameToIDMap.get("Tom")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(0).longValue());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(1).longValue());
		
		// check that marginal of F is [.5,.5] (i.e. condition E was ignored)
		probList = engine.getProbList(0x0FL, null, null);
		assertEquals(2, probList.size());
		assertEquals(.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that in the history, the assumption E was ignored
		questionHistory = engine.getQuestionHistory(0x0FL, null, null);
		assertNotNull(questionHistory);
		assertFalse(questionHistory.isEmpty());
		AddTradeNetworkAction action = (AddTradeNetworkAction) questionHistory.get(questionHistory.size()-1);
		assertEquals((long)0x0F, (long)action.getQuestionId());
		assertTrue("Assumptions = " + action.getTradeId(), action.getAssumptionIds().isEmpty());
		
		List<Float> editLimits = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0DL, 0, null, null);
		assertEquals(2, editLimits.size());
		assertTrue(editLimits.toString() , editLimits.get(0) > 0);
		assertTrue(editLimits.toString() , editLimits.get(0) < 1);
		assertTrue(editLimits.toString() , editLimits.get(1) > 0);
		assertTrue(editLimits.toString() , editLimits.get(1) < 1);
		assertTrue(editLimits.toString() , editLimits.get(0) < 0.5 && 0.5 < editLimits.get(1));
	
		// test disconnected assumptions
		transactionKey = engine.startNetworkActions();
		
		// add a in which the assumptions will be ignored
		newValues = new ArrayList<Float>();
		newValues.add(.5f); newValues.add(.5f); 
		assertFalse( engine.addTrade(
				transactionKey, 
				new Date(), 
				"Tom bets P(D|A = a1) = [.5,.5]", 
				userNameToIDMap.get("Tom"), 
				0x0DL, 
				newValues, 
				Collections.singletonList(0x0AL), 
				Collections.singletonList(0), 
				false
			).isEmpty());
		engine.commitNetworkActions(transactionKey);
		

		// check that the question can be retrieved from getTradedQuestions.
		assertEquals(3, engine.getTradedQuestions(userNameToIDMap.get("Tom")).size());
		assertEquals(0x0El, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(0).longValue());
		assertEquals(0x0Fl, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(1).longValue());
		assertEquals(0x0Dl, engine.getTradedQuestions(userNameToIDMap.get("Tom")).get(2).longValue());
		
		// check that marginal of D is [.5,.5] (i.e. condition A was ignored)
		probList = engine.getProbList(0x0DL, null, null);
		assertEquals(2, probList.size());
		assertEquals(.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that in the history, the assumption A was ignored
		questionHistory = engine.getQuestionHistory(0x0DL, null, null);
		assertNotNull(questionHistory);
		assertFalse(questionHistory.isEmpty());
		action = (AddTradeNetworkAction) questionHistory.get(questionHistory.size()-1);
		assertEquals((long)0x0D, (long)action.getQuestionId());
		assertTrue("Assumptions = " + action.getTradeId(), action.getAssumptionIds().isEmpty());
		
		assertNotNull(engine.getScoreSummaryObject(userNameToIDMap.get("Tom"), null, null, null));
		assertFalse(engine.getQuestionHistory(null, null, null).isEmpty());
		assertFalse(engine.previewBalancingTrade(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(Float.isNaN(engine.getCash(userNameToIDMap.get("Tom"), null, null)));
		assertNotNull(engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0AL, 2, null, null));
		assertEquals(0,engine.getMaximumValidAssumptionsSublists(0x0AL, null, 1).get(0).size());
		assertTrue(engine.getPossibleQuestionAssumptions(0x0A, null).isEmpty());
		assertNotNull(engine.getScoreDetails(userNameToIDMap.get("Tom"), 0x0AL, null, null));
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)}.
	 * This method performs the same test of {@link #testAddTradeInOneTransaction()},
	 * but q-values are set to a very high values.
	 * but it executes everything in a single transaction.
	 */
	public final void testAddTradeInOneTransactionWithHighAssets () {
		engine.setCurrentCurrencyConstant(100);
		engine.setCurrentLogBase(2);
		
		double initialQ = engine.getQValuesFromScore(5000.0f);
		if (engine.getDefaultInferenceAlgorithm().isToUseQValues()) {
			engine.setDefaultInitialAssetTableValue((float) initialQ);
		} else {
			// we are not using q-values anymore
			engine.setDefaultInitialAssetTableValue(engine.getScoreFromQValues(initialQ));
		}
		
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
		assertEquals((engine.getScoreFromQValues(20*initialQ/100)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20*initialQ/100, engine.getQValuesFromScore(minCash), ASSET_ERROR_MARGIN*initialQ/100);
		
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
		assertEquals((engine.getScoreFromQValues(60*initialQ/100)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60*initialQ/100, engine.getQValuesFromScore(minCash), ASSET_ERROR_MARGIN*initialQ/100);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545*initialQ/100)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545*initialQ/100, engine.getQValuesFromScore(minCash), ASSET_ERROR_MARGIN*initialQ/100);
		
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
		assertEquals(engine.getScoreFromQValues(35.7393*initialQ/100), minCash, ASSET_ERROR_MARGIN);
		assertEquals(35.7393*initialQ/100, engine.getQValuesFromScore(minCash), ASSET_ERROR_MARGIN*initialQ/100);
		
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
		
		List<List<Long>> questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertTrue(questionAssumptionGroups.toString(), questionAssumptionGroups.isEmpty());
		
		// generate node E
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.commitNetworkActions(transactionKey);
		
		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 1, questionAssumptionGroups.size());
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(90f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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


		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 1, questionAssumptionGroups.size());
		assertEquals(questionAssumptionGroups.toString(), 1, questionAssumptionGroups.get(0).size());
		
		// create node D and edge D->E
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		engine.commitNetworkActions(transactionKey);

		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 1, questionAssumptionGroups.size());
		assertEquals(questionAssumptionGroups.toString(), 2, questionAssumptionGroups.get(0).size());
		
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
		assertEquals((engine.getScoreFromQValues(90f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

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
		assertEquals((engine.getScoreFromQValues(72.727272f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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


		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 1, questionAssumptionGroups.size());
		assertEquals(questionAssumptionGroups.toString(), 2, questionAssumptionGroups.get(0).size());
		
		// create node F and edge D->F
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		engine.commitNetworkActions(transactionKey);
		

		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 2, questionAssumptionGroups.size());
		for (List<Long> group : questionAssumptionGroups) {
			assertEquals(questionAssumptionGroups.toString(), 2, group.size());
		}
		
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
		assertEquals((engine.getScoreFromQValues(72.727272f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
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
		assertEquals((engine.getScoreFromQValues(57.142857f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(57.142857f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		
		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 2, questionAssumptionGroups.size());
		for (List<Long> group : questionAssumptionGroups) {
			assertEquals(questionAssumptionGroups.toString(), 2, group.size());
		}
		
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#addTrade(long, Date, String, long, long, List, List, List, boolean)},
	 * {@link MarkovEngineImpl#addQuestion(long, Date, long, int, List)},
	 * {@link MarkovEngineImpl#addQuestionAssumption(long, Date, long, List, List)}.
	 * This method performs the same test of {@link #testAddQuestionAfterTrade()},
	 * but it executes everything in a single transaction.
	 */
	public final void testAddQuestionAfterTradeInOneTransaction() {
		
		List<List<Long>> questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertEquals(questionAssumptionGroups.toString(), 0, questionAssumptionGroups.size());
		
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
		
		// if true, the quantity of cliques has changed
		
		// add new node C and random edge from or to C
		engine.addQuestion(transactionKey, new Date(), 0x0C, 2, null);
		if (Math.random() < .5) {
			// edge from C
			if (Math.random() < .5) {
//				System.out.println("Edge C->D");
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0D, Collections.singletonList((long)0x0C), null);
			}
			if (Math.random() < .5) {
//				System.out.println("Edge C->E");
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long)0x0C), null);
			}
			if (Math.random() < .5) {
//				System.out.println("Edge C->F");
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
//			System.out.println("Edge " + parentQuestionIds + " -> C");
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0C, parentQuestionIds, null);
		}
//		List<Long> parentQuestionIds = new ArrayList<Long>();
//		parentQuestionIds.add(0x0EL); parentQuestionIds.add(0x0FL);
//		engine.addQuestionAssumption(transactionKey, new Date(), 0x0C, parentQuestionIds, null);
		
		// commit all trades (including the creation of network and user)
		engine.commitNetworkActions(transactionKey);
		
		questionAssumptionGroups = engine.getQuestionAssumptionGroups();
		assertTrue(questionAssumptionGroups.toString(), questionAssumptionGroups.size() >= 1);
		
		// check that final marginal of C is [.5,.5], E is [0.8509, 0.1491], F is  [0.2165, 0.7835], and D is [0.7232, 0.2768]
		Map<Long,List<Float>> probsList = engine.getProbLists(null, null, null);
		assertEquals(0.5f, probsList.get(0x0CL).get(0), PROB_ERROR_MARGIN);
		assertEquals(0.5f, probsList.get(0x0CL).get(1), PROB_ERROR_MARGIN);
		assertEquals(0.7232f, probsList.get(0x0DL).get(0), PROB_ERROR_MARGIN);
		assertEquals(0.2768f, probsList.get(0x0DL).get(1), PROB_ERROR_MARGIN);
		assertEquals(0.8509f, probsList.get(0x0EL).get(0), PROB_ERROR_MARGIN);
		assertEquals(0.1491f, probsList.get(0x0EL).get(1), PROB_ERROR_MARGIN);
		assertEquals(0.2165f, probsList.get(0x0FL).get(0), PROB_ERROR_MARGIN);
		assertEquals(0.7835f, probsList.get(0x0FL).get(1), PROB_ERROR_MARGIN);
		
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
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		if (Float.isNaN(cash)) {
			for (Node node : engine.getProbabilisticNetwork().getNodes()) {
				System.out.println(node.getParents() + "->" + node);
			}
			fail();
		}
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		
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
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getPossibleQuestionAssumptions(long, java.util.List)}
	 * and {@link edu.gmu.ace.daggre.MarkovEngineImpl#getQuestionAssumptionGroups()}.
	 */
	public final void testGetPossibleQuestionAssumptionsAndGetQuestionAssumptionGroups() {
		
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
		
		assertTrue(engine.getQuestionAssumptionGroups().isEmpty());
		
		// Case 1: 0
		
		engine.initialize();
		long transactionKey = engine.startNetworkActions();
		// create question 0
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+(Math.random()*3)), null);
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
		

		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		assertEquals(1, engine.getQuestionAssumptionGroups().size());
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(0L));
		
		// Case 2: 1->0
		
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+(Math.random()*3)), null);
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
		
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		assertEquals(1, engine.getQuestionAssumptionGroups().size());
		assertEquals(2, engine.getQuestionAssumptionGroups().get(0).size());
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(0L));
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(1L));
		
		// Case 3: 1->0<-2
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1,2
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 2, (int)(2+(Math.random()*3)), null);
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
		

		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		assertEquals(1, engine.getQuestionAssumptionGroups().size());
		assertEquals(3, engine.getQuestionAssumptionGroups().get(0).size());
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(0L));
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(1L));
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(2L));
		
		// case 4: 1<-0->2
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1,2
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 2, (int)(2+(Math.random()*3)), null);
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
		

		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		assertEquals(2, engine.getQuestionAssumptionGroups().size());
		assertEquals(2, engine.getQuestionAssumptionGroups().get(0).size());
		assertEquals(2, engine.getQuestionAssumptionGroups().get(1).size());
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(0L));
		assertTrue(engine.getQuestionAssumptionGroups().get(1).contains(0L));
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(1L) || engine.getQuestionAssumptionGroups().get(1).contains(1L));
		assertTrue(engine.getQuestionAssumptionGroups().get(0).contains(2L) || engine.getQuestionAssumptionGroups().get(1).contains(2L));
		
		// disconnected case: 0, 1->2, 3->4<-5, 6<-7->8
		engine.initialize();
		transactionKey = engine.startNetworkActions();
		// create questions 0,1,2
		engine.addQuestion(transactionKey, new Date(), 0, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 1, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 2, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 3, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 4, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 5, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 6, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 7, (int)(2+(Math.random()*3)), null);
		engine.addQuestion(transactionKey, new Date(), 8, (int)(2+(Math.random()*3)), null);
		// create edges 1->2,  3->4<-5, 7->6, and 7->8
		engine.addQuestionAssumption(transactionKey, new Date(), 2, Collections.singletonList((long)1), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 4, Collections.singletonList((long)3), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 4, Collections.singletonList((long)5), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 6, Collections.singletonList((long)7), null);
		engine.addQuestionAssumption(transactionKey, new Date(), 8, Collections.singletonList((long)7), null);
		engine.commitNetworkActions(transactionKey);
		for (long i = 0; i < 9; i++) {
			try {
				// assumptions contains main node
				engine.getPossibleQuestionAssumptions(i, Collections.singletonList(i));
				fail("Question cannot be assumption of itself: " + i);
			}catch (IllegalArgumentException e) {
				assertNotNull(e); // OK. 
			}
			try {
				// assumptions contains main node
				assumptionIds = new ArrayList<Long>();
				assumptionIds.add(i);
				do {
					long assumptionToAdd = (long)((Math.random()*9));
					if (assumptionToAdd == i) {
						continue;
					}
					assumptionIds.add(assumptionToAdd);
				} while (Math.random() < .5);
				engine.getPossibleQuestionAssumptions(i, assumptionIds);
				fail("Question cannot be assumption of itself: " + i + ", assumptions: " + assumptionIds);
			}catch (IllegalArgumentException e) {
				assertNotNull(e); // OK. 
			}
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
		// 0,
		assertEquals(0,  engine.getPossibleQuestionAssumptions(0, null).size());
		assertEquals(0,  engine.getPossibleQuestionAssumptions(0, (List)Collections.emptyList()).size());
		for (long i = 1; i < 9; i++) {
			assertEquals(0,  engine.getPossibleQuestionAssumptions(0, Collections.singletonList(i)).size());
		}
		// 1->2,
		assumptions = engine.getPossibleQuestionAssumptions(1, null);
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(2L));
		assumptions = engine.getPossibleQuestionAssumptions(2, null);
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(1L));
		assumptions = engine.getPossibleQuestionAssumptions(1, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(2L));
		assumptions = engine.getPossibleQuestionAssumptions(2, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(1L));
		assumptions = engine.getPossibleQuestionAssumptions(1, Collections.singletonList(2L));
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(2L));
		assumptions = engine.getPossibleQuestionAssumptions(2, Collections.singletonList(1L));
		assertEquals(1, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(1L));
		// invalid conditions
		assertTrue(engine.getPossibleQuestionAssumptions(1, Collections.singletonList(0L)).isEmpty());
		assertTrue(engine.getPossibleQuestionAssumptions(2, Collections.singletonList(0L)).isEmpty());
		for (long i = 3; i < 8; i++) {
			assertTrue(engine.getPossibleQuestionAssumptions(1, Collections.singletonList(i)).isEmpty());
			assertTrue(engine.getPossibleQuestionAssumptions(2, Collections.singletonList(i)).isEmpty());
		}
		// 3->4<-5,
		assumptions = engine.getPossibleQuestionAssumptions(3, null);
		assertEquals(2, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(4L));
		assertTrue("Assumptions = " + assumptions, assumptions.contains(5L));
		assumptions = engine.getPossibleQuestionAssumptions(4, null);
		assertEquals(2, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(3L));
		assertTrue("Assumptions = " + assumptions, assumptions.contains(5L));
		assumptions = engine.getPossibleQuestionAssumptions(5, null);
		assertEquals(2, assumptions.size());
		assertTrue("Assumptions = " + assumptions, assumptions.contains(3L));
		assertTrue("Assumptions = " + assumptions, assumptions.contains(4L));
		for (long i = 0; i < 8; i++) {
			if (i >= 3 && i <= 5) {
				i = 6;
			}
			assertTrue(engine.getPossibleQuestionAssumptions(3, Collections.singletonList(i)).isEmpty());
			assertTrue(engine.getPossibleQuestionAssumptions(4, Collections.singletonList(i)).isEmpty());
			assertTrue(engine.getPossibleQuestionAssumptions(5, Collections.singletonList(i)).isEmpty());
		}
		// 6<-7->8
		assumptions = engine.getPossibleQuestionAssumptions(7, null);
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)6));
		assertTrue(assumptions.contains((long)8));
		assumptions = engine.getPossibleQuestionAssumptions(7, (List)Collections.emptyList());
		assertEquals(2, assumptions.size());
		assertTrue(assumptions.contains((long)6));
		assertTrue(assumptions.contains((long)8));
		assumptions = engine.getPossibleQuestionAssumptions(7, Collections.singletonList((long)6));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)6));
		assumptions = engine.getPossibleQuestionAssumptions(7, Collections.singletonList((long)8));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)8));
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)6); assumptionIds.add((long)8);
		assumptions = engine.getPossibleQuestionAssumptions(7, assumptionIds);
		assertEquals(0, assumptions.size());
		assumptions = engine.getPossibleQuestionAssumptions(6, null);
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(6, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(6, Collections.singletonList((long)7));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(6, Collections.singletonList((long)8));
		assertEquals(0, assumptions.size());
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)7); assumptionIds.add((long)8);
		assumptions = engine.getPossibleQuestionAssumptions(6, assumptionIds);
		assertEquals(0, assumptions.size());
		assumptions = engine.getPossibleQuestionAssumptions(8, null);
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(8, (List)Collections.emptyList());
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(8, Collections.singletonList((long)7));
		assertEquals(1, assumptions.size());
		assertTrue(assumptions.contains((long)7));
		assumptions = engine.getPossibleQuestionAssumptions(8, Collections.singletonList((long)6));
		assertEquals(0, assumptions.size());
		assumptionIds = new ArrayList<Long>(2);
		assumptionIds.add((long)7); assumptionIds.add((long)6);
		assumptions = engine.getPossibleQuestionAssumptions(8, assumptionIds);
		assertEquals(0, assumptions.size());
		// disconnected nodes as assumptions
		for (long i = 0; i < 6; i++) {
			assumptions = engine.getPossibleQuestionAssumptions((Math.random()<.34)?7:(Math.random()<.34)?6:8, Collections.singletonList(i));
			assertEquals(0, assumptions.size());
		}
		
		// test getQuestionAssumptionGroups for disconnected net: 0, 1->2, 3->4<-5, 6<-7->8
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		assertEquals(5, engine.getQuestionAssumptionGroups().size());
		for (List<Long> clique : engine.getQuestionAssumptionGroups()) {
			assertTrue("Clique = " + clique, clique.size() >= 1 && clique.size() <= 3);
			if (clique.size() == 1) {	// 0
				assertTrue("Clique = " + clique, clique.contains(0L));
			} else if (clique.size() == 2) {	// 1->2 or 6<-7->8
				assertTrue("Clique = " + clique, ( clique.contains(1L) && clique.contains(2L) ) 
						|| ( clique.contains(6L) && clique.contains(7L) ) 
						|| ( clique.contains(8L) && clique.contains(7L) ) );
			} else {	// 3->4<-5
				assertTrue("Clique = " + clique, clique.contains(3L));
				assertTrue("Clique = " + clique, clique.contains(4L));
				assertTrue("Clique = " + clique, clique.contains(5L));
			}
		}
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
		

		// test invalid assumptions
		transactionKey = engine.startNetworkActions();
		
		// add a in which the assumptions will be ignored
		assertTrue( engine.doBalanceTrade(
				transactionKey, 
				new Date(), 
				"Tom balances P(E|F = f1)", 
				userNameToIDMap.get("Tom"), 
				0x0EL, 
				Collections.singletonList(0x0FL), 
				Collections.singletonList(1) 
			));
		engine.commitNetworkActions(transactionKey);
		
		// check that Tom has exited E (i.e. condition F was ignored)
		List<Float> assetIfStates = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0EL, null, null);
		assertEquals(2, assetIfStates.size());
		assertEquals(assetIfStates.get(0), assetIfStates.get(1), ASSET_ERROR_MARGIN);
		
		// check that in the history, the assumption F was ignored
		List<QuestionEvent> questionHistory = engine.getQuestionHistory(0x0EL, null, null);
		assertNotNull(questionHistory);
		assertFalse(questionHistory.isEmpty());
		BalanceTradeNetworkAction action = (BalanceTradeNetworkAction) questionHistory.get(questionHistory.size()-1);
		assertEquals((long)0x0E, (long)action.getQuestionId());
		assertTrue("Assumptions = " + action.getTradeId(), action.getAssumptionIds().isEmpty());
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
				fail("Should throw exeption indicating that assets == inconsistent (or 0)");
//				assertTrue("Cash = " + cash, Float.isInfinite(cash));	// 0 of q-value means -infinite assets
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail();
		} catch (ZeroAssetsException e) {
			// OK. Impossible state yields zero 
			assertNotNull(e);
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
			fail("D should not be resolvable anymore: " + balancingTrade);
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
		}
		try {
			Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			List<Float> balancingTrade = engine.previewBalancingTrade(userId, (long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(1));
			fail("D should not be resolvable anymore: " + balancingTrade);
		} catch (IllegalArgumentException e) {
			assertNotNull(e);
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
		Long userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
		try {
			engine.scoreUserQuestionEv(userId, (long)0x0D, null, null);
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (ZeroAssetsException e) {
			e.printStackTrace();
			fail(e.getMessage() + ", userId = " + userId);
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			engine.scoreUserQuestionEv(userId, (long)((Math.random() < .5)?0x0E:0x0F), Collections.singletonList((long)0x0D), Collections.singletonList(((Math.random()<.5)?0:1)));
			if (engine.isToDeleteResolvedNode()) {
				fail("D should not be present anymore");
			}
		} catch (ZeroAssetsException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			if (engine.isToDeleteResolvedNode()) {
				assertNotNull(e);
			} else {
				e.printStackTrace();
				fail(e.getMessage());
			}
		}
		try {
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
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
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
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
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
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
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
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
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
			transactionKey = engine.startNetworkActions();
			engine.doBalanceTrade(transactionKey, new Date(), "To fail", userId, (long)0x0D, null, null );
			fail("D should not be present anymore");
		} catch (IllegalArgumentException e) {
			engine.commitNetworkActions(transactionKey);
			assertNotNull(e);
		}
		try {
			userId = userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy");
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
		try {
			assertNotNull(engine.getScoreSummary(userNameToIDMap.get((Math.random() < .25)?"Joe":(Math.random() < .25)?"Eric":(Math.random() < .25)?"Tom":"Amy"), (long)0x0D, null, null));
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
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
			if (!engine.isToDeleteResolvedNode()) {
				assumedStates.set(2, 0);	// d1
			}
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
					try {
						cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
						fail("[" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2 + ", cash = " + cash);
					} catch (ZeroAssetsException e) {
						// It is expected to throw this exception, because we tried to calculate asset of impossible state
						assertNotNull(e);
					}
				}
			} 
		}
	}
	
	public final void testResolveAllQuestion() {

		engine.setToDeleteResolvedNode(false);
		engine.setCurrentCurrencyConstant(100);
		engine.setCurrentLogBase(2);
		float initAsssets = 12050.81f;
		if (engine.getDefaultInferenceAlgorithm().isToUseQValues()) {
			initAsssets = (float) engine.getQValuesFromScore(initAsssets);
		}
		engine.setDefaultInitialAssetTableValue(initAsssets);
		
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		long transactionKey = engine.startNetworkActions();
		engine.resolveQuestion(transactionKey, new Date(), 0x0DL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0EL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0FL, 0);
		engine.commitNetworkActions(transactionKey);

		// after all nodes are resolved, cash and expected scores are equal
		Map<String, Float> cashAndScoreMap = new HashMap<String, Float>();
		
		for (String user : userNameToIDMap.keySet()) {
			float cash = engine.getCash(userNameToIDMap.get(user), null, null);
			assertFalse("Cash of " + user + " = " + cash,Float.isInfinite(cash) || Float.isNaN(cash));
			float score = engine.scoreUserEv(userNameToIDMap.get(user), null, null);
			assertFalse("Score of " + user + " = " + score,Float.isInfinite(score) || Float.isNaN(score));
			assertEquals(cash, score, ASSET_ERROR_MARGIN);
			assertTrue(cash > 0 && score > 0);
			cashAndScoreMap.put(user, cash);
		}
		
		engine.setToDeleteResolvedNode(true);
		engine.initialize();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		transactionKey = engine.startNetworkActions();
		engine.resolveQuestion(transactionKey, new Date(), 0x0DL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0EL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0FL, 0);
		engine.commitNetworkActions(transactionKey);
		for (String user : userNameToIDMap.keySet()) {
			float cash = engine.getCash(userNameToIDMap.get(user), null, null);
			assertFalse("Cash of " + user + " = " + cash,Float.isInfinite(cash) || Float.isNaN(cash));
			float score = engine.scoreUserEv(userNameToIDMap.get(user), null, null);
			assertFalse("Score of " + user + " = " + score,Float.isInfinite(score) || Float.isNaN(score));
			assertEquals("User=" + user, cash, score, ASSET_ERROR_MARGIN);
			assertEquals("User=" + user, cashAndScoreMap.get(user), cash, ASSET_ERROR_MARGIN);
			assertEquals("User=" + user, cashAndScoreMap.get(user), score, ASSET_ERROR_MARGIN);
		}
		
		engine.initialize();
		engine.setCurrentCurrencyConstant((float) (1000/(Math.log(10)/Math.log(2))));
		engine.setCurrentLogBase(2);
		engine.setDefaultInitialAssetTableValue(10);
		userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		transactionKey = engine.startNetworkActions();
		engine.resolveQuestion(transactionKey, new Date(), 0x0DL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0EL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0FL, 0);
		engine.commitNetworkActions(transactionKey);
		
		Map<String, Float> cashMap2 = new HashMap<String, Float>();
		Map<String, Float> scoreMap2 = new HashMap<String, Float>();
		
		for (String user : userNameToIDMap.keySet()) {
			float cash = engine.getCash(userNameToIDMap.get(user), null, null);
			assertFalse("Cash of " + user + " = " + cash,Float.isInfinite(cash) || Float.isNaN(cash));
			float score = engine.scoreUserEv(userNameToIDMap.get(user), null, null);
			assertFalse("Score of " + user + " = " + score,Float.isInfinite(score) || Float.isNaN(score));
			assertTrue(cash > 0 && score > 0);
			assertEquals(cash, score, ASSET_ERROR_MARGIN);
			cashMap2.put(user, cash);
			scoreMap2.put(user, score);
		}
		
		engine.initialize();
		engine.setCurrentCurrencyConstant(1000);
		engine.setCurrentLogBase(2);
		if (engine.getDefaultInferenceAlgorithm().isToUseQValues()) {
			engine.setDefaultInitialAssetTableValue((float) engine.getQValuesFromScore(12050.81f));
		} else {
			engine.setDefaultInitialAssetTableValue(12050.81f);
		}
		userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		transactionKey = engine.startNetworkActions();
		engine.resolveQuestion(transactionKey, new Date(), 0x0DL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0EL, 0);
		engine.resolveQuestion(transactionKey, new Date(), 0x0FL, 0);
		engine.commitNetworkActions(transactionKey);
		
		
		for (String user : userNameToIDMap.keySet()) {
			float cash = engine.getCash(userNameToIDMap.get(user), null, null);
			assertFalse("Cash of " + user + " = " + cash,Float.isInfinite(cash) || Float.isNaN(cash));
			float score = engine.scoreUserEv(userNameToIDMap.get(user), null, null);
			assertFalse("Score of " + user + " = " + score,Float.isInfinite(score) || Float.isNaN(score));
			assertEquals("User = " + user, cash, score, ASSET_ERROR_MARGIN);
			assertTrue("User = " + user, cash > 0 && score > 0);
			assertFalse("User = " + user, engine.getScoreDetails(userNameToIDMap.get(user), null, null, null).isEmpty());
			assertNotNull("User = " + user, engine.getScoreSummaryObject(userNameToIDMap.get(user), null, null, null));
			assertEquals("User = " + user, engine.getScoreSummaryObject(userNameToIDMap.get(user), null, null, null).getCash(), cash);
			assertEquals("User = " + user, engine.getScoreSummaryObject(userNameToIDMap.get(user), null, null, null).getScoreEV(), score);
		}
		
		if (engine.isToDeleteResolvedNode()) {
			// questions were resolved, but they are still in the system
			assertTrue(engine.getQuestionAssumptionGroups().isEmpty());
		} else {
			// all questions were supposedly removed from system
			assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		}
		
		// check new user
		assertEquals(12050.81f, engine.getCash(Long.MAX_VALUE, null, null), ASSET_ERROR_MARGIN);
		assertEquals(12050.81f, engine.scoreUserEv(Long.MIN_VALUE, null, null), ASSET_ERROR_MARGIN);
		assertEquals(12050.81f, engine.getCash(Long.MIN_VALUE, null, null), ASSET_ERROR_MARGIN);
		assertEquals(12050.81f, engine.scoreUserEv(Long.MAX_VALUE, null, null), ASSET_ERROR_MARGIN);
		assertFalse(engine.getScoreDetails(Long.MAX_VALUE, null, null, null).isEmpty());
		assertFalse(engine.getScoreDetails(Long.MIN_VALUE, null, null, null).isEmpty());
		assertNotNull(engine.getScoreSummaryObject(Long.MAX_VALUE, null, null, null));
		assertNotNull(engine.getScoreSummaryObject(Long.MIN_VALUE, null, null, null));
		assertEquals(engine.getScoreSummaryObject(Long.MAX_VALUE, null, null, null).getCash(), 12050.81f);
		assertEquals(engine.getScoreSummaryObject(Long.MAX_VALUE, null, null, null).getScoreEV(), 12050.81f);
		assertEquals(engine.getScoreSummaryObject(Long.MIN_VALUE, null, null, null).getCash(), 12050.81f);
		assertEquals(engine.getScoreSummaryObject(Long.MIN_VALUE, null, null, null).getScoreEV(), 12050.81f);
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
		
		// nothing to revert yet
		assertFalse(engine.revertTrade(null, new Date(), new Date(0), null));
		
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
//				System.out.println("C->D");
			}
			if (Math.random() < .5) {
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0EL, Collections.singletonList(0x0CL), null);
//				System.out.println("C->E");
			}
			if (Math.random() < .5) {
				engine.addQuestionAssumption(transactionKey, new Date(), 0x0FL, Collections.singletonList(0x0CL), null);
//				System.out.println("C->F");
			}
		} else {
			// from some node to 0
			List<Long> assumptions = new ArrayList<Long>();
			if (Math.random() < .5) {
				assumptions.add(0x0DL);
//				System.out.println("D->C");
			}
			if (Math.random() < .5) {
				assumptions.add(0x0EL);
//				System.out.println("E->C");
			}
			if (Math.random() < .5) {
				assumptions.add(0x0FL);
//				System.out.println("F->C");
			}
			engine.addQuestionAssumption(transactionKey, new Date(), 0x0CL, assumptions, null);
		}
//		List<Long> assumptions = new ArrayList<Long>();
//		assumptions.add(0x0FL);
//		engine.addQuestionAssumption(transactionKey, new Date(), 0x0CL, assumptions, null);
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
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		mapOfConditionalCash.put("Tom", new ArrayList<Float>());
		
		
		// check that final LPE of Tom contains d1, e2 and any value F
		
		// check combination d1, e1, f1 (not min)
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// e1
		assumedStates.set(2, 0);	// f1
		float cash = engine.getCash(userNameToIDMap.get("Tom"), assumptionIds, assumedStates);
		if (Float.isNaN(cash)) {
			for (Node node : engine.getProbabilisticNetwork().getNodes()) {
				System.out.println(node.getParents() + "->" + node);
			}
			fail();
		}
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
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
		
		// check that assets of question C is equal for everyone
		for (String userName : userNameToIDMap.keySet()) {
			List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0CL, null, null);
			assertEquals(userName, 2, assetsIfStates.size());
			assertFalse(Float.isInfinite(assetsIfStates.get(0)));
			assertFalse(Float.isInfinite(assetsIfStates.get(1)));
			assertEquals("User who made edit on C = " + userWhoMadeTradeOnC + ", current = " + userName + "(" + userNameToIDMap.get(userName)+ ") " + assetsIfStates, 
					engine.getQValuesFromScore(assetsIfStates.get(0)), 
					engine.getQValuesFromScore(assetsIfStates.get(1)),
					ASSET_ERROR_MARGIN
				);
		}
		
		// resolve to d1. Resolutions are not reverted (i.e. resolutions are supposedly re-done)
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.resolveQuestion(transactionKey, new Date(), (long)0x0D, 0));
		assertTrue(engine.commitNetworkActions(transactionKey));
		
		// check that assets of question C is equal for everyone
		for (String userName : userNameToIDMap.keySet()) {
			List<Float> assetsIfStates = engine.getAssetsIfStates(userNameToIDMap.get(userName), 0x0CL, null, null);
			assertEquals(userName, 2, assetsIfStates.size());
			try {
				assertEquals("User who made edit on C = " + userWhoMadeTradeOnC + ", current = " + userName + "(" + userNameToIDMap.get(userName)+ ") " + assetsIfStates, 
						engine.getQValuesFromScore(assetsIfStates.get(0)), 
						engine.getQValuesFromScore(assetsIfStates.get(1)),
						ASSET_ERROR_MARGIN
				);
			} catch (ZeroAssetsException e) {
				e.printStackTrace();
				for (Node node : engine.getProbabilisticNetwork().getNodes()) {
					System.out.println(node.getParents() + " -> " + node);
				}
				fail(userName + " fail.");
			}
		}
		
		
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
		for (int repeat = 0; repeat < 3; repeat++) {
			for (String userName : mapOfConditionalCash.keySet()) {
				if (!engine.isToDeleteResolvedNode()) {
					assumedStates.set(2, 0);	// d1
				}
				for (int i = 0; i < 4; i++) {	// combinations [e1f1,e1f2,e2f1,e2f2]
					assumedStates.set(0, (int)i/2);	// e
					assumedStates.set(1, (int)i%2);	// f
					cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
					if (Float.isNaN(cash)) {
						for (Node node : engine.getProbabilisticNetwork().getNodes()) {
							System.out.println(node.getParents() + "->" + node);
						}
						fail();
					}
					assertEquals("[" + repeat + "-" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2, mapOfConditionalCash.get(userName).get(i), cash);
				}
				if (!engine.isToDeleteResolvedNode()) {
					assumedStates.set(2, 1);	// d2
					for (int i = 0; i < 4; i++) {	// combinations [e1f1d2,e1f2d2,e2f1d2,e2f2d2]
						assumedStates.set(0, (int)i/2);	// e
						assumedStates.set(1, (int)i%2);	// f
						try {
							cash = engine.getCash(userNameToIDMap.get(userName), assumptionIds, assumedStates);
							fail("[" + repeat + "-" + i + "] user = " + userName + ", e = " + (int)i/2 + ", f = " + (int)i%2 + ", cash = " + cash);
						} catch (ZeroAssetsException e) {
							// This is the expected behavior
							assertNotNull(e);
						}
					}
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
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
		List<Float> currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0E, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}

		
		// Let's create user Joe, ID = 1.
		userNameToIDMap.put("Joe", (long) 1);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

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
		currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0E, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}

		// Let's create user Amy, ID = 2.
		userNameToIDMap.put("Amy", (long) 2);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

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
		currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Amy"), 0x0F, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}
		
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
		currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Joe"), 0x0F, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}

		// create new user Eric
		userNameToIDMap.put("Eric", (long) 3);
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		transactionKey = engine.startNetworkActions();
		assertTrue(engine.addCash(transactionKey, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		engine.commitNetworkActions(transactionKey);
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
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
		currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0E, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}
		
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
		currentAsssetsIf = engine.getAssetsIfStates(userNameToIDMap.get("Eric"), 0x0D, assumptionIds, assumedStates);
		assertNotNull(currentAsssetsIf);
		assertEquals(preview.size(), currentAsssetsIf.size());
		for (int i = 0; i < currentAsssetsIf.size(); i++) {
			assertEquals(currentAsssetsIf.get(i), preview.get(i), ASSET_ERROR_MARGIN);
		}

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
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#scoreUserEV(java.util.List, java.util.List, java.util.List)}.
	 */
	public final void testScoreUserEV() {
		// generate DEF net
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		engine.setDefaultInitialAssetTableValue(1000);
		// most basic assertion
		assertEquals(10.31615, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		List<Float> scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(10L, 0x0DL, Collections.singletonList(0x0EL), Collections.singletonList(1));
		assertEquals(2, scoreUserQuestionEvStates.size());
		assertEquals(1000f, scoreUserQuestionEvStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(1000f, scoreUserQuestionEvStates.get(1), ASSET_ERROR_MARGIN);
		
		for (String user : userNameToIDMap.keySet()) {
			boolean hasAtLeastOneCombinationWithDifferentValues = false;
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, null, null);
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
				|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
				|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
				
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, null, null);
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, null, null);
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, Collections.singletonList(0x0EL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, Collections.singletonList(0x0EL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, Collections.singletonList(0x0FL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, Collections.singletonList(0x0FL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, Collections.singletonList(0x0DL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, Collections.singletonList(0x0DL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, Collections.singletonList(0x0FL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, Collections.singletonList(0x0FL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, Collections.singletonList(0x0DL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, Collections.singletonList(0x0DL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, Collections.singletonList(0x0EL), Collections.singletonList(0));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, Collections.singletonList(0x0EL), Collections.singletonList(1));
			assertEquals(2, scoreUserQuestionEvStates.size());
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
			assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
			hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
			|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
			|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			
			
			List<Long> assumptionIds = new ArrayList<Long>();
			assumptionIds.add(0x0EL); assumptionIds.add(0x0FL);
			for (int i = 0; i < 4; i++) {
				List<Integer> assumedStates = new ArrayList<Integer>();
				assumedStates.add(i%2); assumedStates.add((int) ((i/2.0)%2.0));
				scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0DL, assumptionIds, assumedStates);
				assertEquals(2, scoreUserQuestionEvStates.size());
				assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
				assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
				hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
					|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
					|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			}

			assumptionIds.set(0,0x0DL); assumptionIds.set(1,0x0FL);
			for (int i = 0; i < 4; i++) {
				List<Integer> assumedStates = new ArrayList<Integer>();
				assumedStates.add(i%2); assumedStates.add((int) ((i/2.0)%2.0));
				scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0EL, assumptionIds, assumedStates);
				assertEquals(2, scoreUserQuestionEvStates.size());
				assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > 0);
				assertTrue(user + "," + scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(1) > 0);
				hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
				|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
				|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			}
			
			assumptionIds.set(0,0x0DL); assumptionIds.set(1,0x0EL);
			for (int i = 0; i < 4; i++) {
				List<Integer> assumedStates = new ArrayList<Integer>();
				assumedStates.add(i%2); assumedStates.add((int) ((i/2.0)%2.0));
				scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(userNameToIDMap.get(user), 0x0FL, assumptionIds, assumedStates);
				assertEquals(2, scoreUserQuestionEvStates.size());
				hasAtLeastOneCombinationWithDifferentValues = hasAtLeastOneCombinationWithDifferentValues
				|| scoreUserQuestionEvStates.get(0) - ASSET_ERROR_MARGIN > scoreUserQuestionEvStates.get(1) 
				|| scoreUserQuestionEvStates.get(0) < scoreUserQuestionEvStates.get(1) - ASSET_ERROR_MARGIN;
			}
			assertTrue(user, hasAtLeastOneCombinationWithDifferentValues);
		}
		
		// TODO implement more test cases
		
		engine.initialize();
		engine.setCurrentCurrencyConstant(100);
		engine.setCurrentLogBase(2);
		if (engine.getDefaultInferenceAlgorithm().isToUseQValues()) {
			engine.setDefaultInitialAssetTableValue((float) engine.getQValuesFromScore(1000));
		} else {
			engine.setDefaultInitialAssetTableValue(1000);
		}
		
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 1L, 2, null);
		engine.commitNetworkActions(transactionKey);

		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 2L, 2, null);
		engine.addQuestionAssumption(transactionKey, new Date(), 2L, Collections.singletonList(1L), null);
		engine.commitNetworkActions(transactionKey);
		
		assertEquals(1000f, engine.getCash(1L, null, null), ASSET_ERROR_MARGIN);
		assertEquals(1000f, engine.scoreUserEv(1L, null, null), ASSET_ERROR_MARGIN);
		scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(2L, 2L, Collections.singletonList(1L), Collections.singletonList(1));
		assertEquals(2, scoreUserQuestionEvStates.size());
		assertEquals(1000f, scoreUserQuestionEvStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(1000f, scoreUserQuestionEvStates.get(1), ASSET_ERROR_MARGIN);
		
		
		transactionKey = engine.startNetworkActions();
		List<Float> newValues = new ArrayList<Float>();
		newValues.add(.8f); newValues.add(.2f);
		engine.addTrade(transactionKey, new Date(), "", 1L, 1L, newValues , null, null, false);
		engine.commitNetworkActions(transactionKey);
		
		scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(1L, 1L, Collections.singletonList(2L), Collections.singletonList(0));
		assertEquals(2, scoreUserQuestionEvStates.size());
		assertTrue(scoreUserQuestionEvStates.toString(), scoreUserQuestionEvStates.get(0) > scoreUserQuestionEvStates.get(1));
		assertEquals(1067.8073f, scoreUserQuestionEvStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(867.8072f, scoreUserQuestionEvStates.get(1), ASSET_ERROR_MARGIN);
		
		transactionKey = engine.startNetworkActions();
		engine.resolveQuestion(transactionKey, new Date(), 1L, 0);
		engine.commitNetworkActions(transactionKey);
		
		assertFalse(Float.isNaN(engine.scoreUserEv(1L, null, null)));
		assertEquals(1067.8073, engine.scoreUserEv(1L, null, null), ASSET_ERROR_MARGIN);

		scoreUserQuestionEvStates = engine.scoreUserQuestionEvStates(1L, 2L, null, null);
		assertEquals(2, scoreUserQuestionEvStates.size());
		assertEquals(1067.8073f, scoreUserQuestionEvStates.get(0), ASSET_ERROR_MARGIN);
		assertEquals(1067.8073f, scoreUserQuestionEvStates.get(1), ASSET_ERROR_MARGIN);
		
		assertEquals(0f, engine.scoreUserQuestionEvStates(1L, 2L, Collections.singletonList(2L), Collections.singletonList(0)).get(1), PROB_ERROR_MARGIN);
		assertEquals(0f, engine.scoreUserQuestionEvStates(1L, 2L, Collections.singletonList(2L), Collections.singletonList(1)).get(0), PROB_ERROR_MARGIN);
		assertEquals(engine.scoreUserEv(1L, Collections.singletonList(2L), Collections.singletonList(0)), 
				engine.scoreUserQuestionEvStates(1L, 2L, Collections.singletonList(2L), Collections.singletonList(0)).get(0), ASSET_ERROR_MARGIN);
		assertEquals(engine.scoreUserEv(1L, Collections.singletonList(2L), Collections.singletonList(1)), 
				engine.scoreUserQuestionEvStates(1L, 2L, Collections.singletonList(2L), Collections.singletonList(1)).get(1), ASSET_ERROR_MARGIN);
		
		
		// simple disconnected network cases
		engine.initialize();
		engine.setCurrentCurrencyConstant(100);
		engine.setCurrentLogBase(2);
		if (engine.getDefaultInferenceAlgorithm().isToUseQValues()) {
			engine.setDefaultInitialAssetTableValue((float) engine.getQValuesFromScore(1000));
		} else {
			engine.setDefaultInitialAssetTableValue(1000);
		}
		transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 1L, 2, null);
		engine.addQuestion(transactionKey, new Date(), 2L, 2, null);
		engine.commitNetworkActions(transactionKey);
		
		assertEquals(1000f, engine.scoreUserEv(1L, null, null), ASSET_ERROR_MARGIN);
		
		transactionKey = engine.startNetworkActions();
		newValues = new ArrayList<Float>();
		newValues.add(.2f); newValues.add(.8f);
		engine.addTrade(transactionKey, new Date(), "", 1L, 1L, newValues, null, null, false);
		engine.resolveQuestion(transactionKey, new Date(), 1L, 1);
		engine.commitNetworkActions(transactionKey);
		
		assertEquals(1638.4f, engine.getQValuesFromScore(engine.scoreUserEv(1L, null, null)), ASSET_ERROR_MARGIN);
		assertEquals(engine.getScoreFromQValues(1638.4f), engine.scoreUserEv(1L, null, null), ASSET_ERROR_MARGIN);
		
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getScoreSummary(long, List, List)}
	 */
	public final void testGetScoreSummary() {
		engine.setToReturnEVComponentsAsScoreSummary(true);
		
		// generate DEF net
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		// extract summary
		ScoreSummary summary = engine.getScoreSummaryObject(userNameToIDMap.get("Eric"), null, null, null);
		
		// most basic assertions
		assertEquals(engine.getCash(userNameToIDMap.get("Eric"), null, null), summary.getCash(), ASSET_ERROR_MARGIN);
		assertEquals(10.31615, summary.getScoreEV(), PROB_ERROR_MARGIN);
		assertEquals(8,summary.getScoreComponents().size());
		assertEquals(2,summary.getIntersectionScoreComponents().size());
		
		
		// assert that the aggregation of contributions will result in the total expected score
		float sum = 0f;
		for (SummaryContribution positiveContribution : summary.getScoreComponents()) {
			sum += positiveContribution.getContributionToScoreEV();
		}
		for (SummaryContribution negativeContribution : summary.getIntersectionScoreComponents()) {
			sum -= negativeContribution.getContributionToScoreEV();
		}
		assertEquals(summary.getScoreEV(), sum, PROB_ERROR_MARGIN);
		
		// the separator D must be present at all the components
		for (SummaryContribution contribution : summary.getIntersectionScoreComponents()) {
			assertTrue(
					"Questions = " + contribution.getQuestions()
					+ ", states = " + contribution.getStates()
					+ ", value = " + contribution.getContributionToScoreEV(), 
					summary.getIntersectionScoreComponents().get(0).getQuestions().contains(0x0DL)
				);
		}
		for (SummaryContribution contribution : summary.getScoreComponents()) {
			assertTrue(
					"Questions = " + contribution.getQuestions()
					+ ", states = " + contribution.getStates()
					+ ", value = " + contribution.getContributionToScoreEV(), 
					summary.getIntersectionScoreComponents().get(0).getQuestions().contains(0x0DL)
				);
		}
		
		assertNotNull(engine.getScoreSummaryObject(userNameToIDMap.get("Tom"), 0x0DL, Collections.singletonList(0x0EL), Collections.singletonList(1)));
		
		// test the case in which the score summary contains expected score given states
		engine.setToReturnEVComponentsAsScoreSummary(false);
		
		// basic test: value of eric's score summary is unchanged
		assertEquals(10.31615, engine.getScoreSummaryObject(userNameToIDMap.get("Eric"), null, null, null).getScoreEV(), PROB_ERROR_MARGIN);
		
		// extract marginal probability of each question so that we can use them later for consistency check
		Map<Long, List<Float>> marginals = engine.getProbLists(null, null, null);
		assertEquals(marginals.toString(), 3, marginals.keySet().size());	// must be marginals of 3 questions
		for (Long key : marginals.keySet()) {
			assertEquals(marginals.toString(), 2, marginals.get(key).size());	// must be nods with 2 states
		}
		
		// add user who did not do any trade in userNameToIDMap in order to test boundary condition (users with no trades at all)
		userNameToIDMap.put("User with no trade " + Long.MIN_VALUE, Long.MIN_VALUE);
		
		for (String user : userNameToIDMap.keySet()) {
			// extract new summary
			summary = engine.getScoreSummaryObject(userNameToIDMap.get(user), null, null, null);
			
			// most basic assertions
			assertEquals(user, engine.getCash(userNameToIDMap.get(user), null, null), summary.getCash(), ASSET_ERROR_MARGIN);
			assertEquals(user, engine.scoreUserEv(userNameToIDMap.get(user), null, null), summary.getScoreEV(), ASSET_ERROR_MARGIN);
			
			
			// check that getQuestions of summary.getScoreComponents retains the same ordering of engine.getTradedQuestions
			List<Long> tradedQuestions = engine.getTradedQuestions(userNameToIDMap.get(user));
//			assertFalse(user, tradedQuestions.isEmpty());	// users did actually trade in the system, so its not empty
			assertEquals(user, tradedQuestions.size(), summary.getScoreComponents().size()/2);	// Note: I'm assuming each question has 2 states
			for (int questionIndex = 0; questionIndex < tradedQuestions.size(); questionIndex++) {
				
				sum = 0f;	// prepare to calculate the sum of (<Expected score given state> * <marginal of state>)
				
				for (int stateIndex = 0; stateIndex < marginals.get(tradedQuestions.get(questionIndex)).size(); stateIndex++) {
					
					// Note: I'm assuming that all nodes have 2 states
					int scoreComponentIndex = questionIndex*2 + stateIndex;	
					
					// assert that getScoreComponents is related to current question
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) + ", state = " + stateIndex, 
							1, summary.getScoreComponents().get(scoreComponentIndex).getQuestions().size());
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) + ", state = " + stateIndex, 
							tradedQuestions.get(questionIndex),
							summary.getScoreComponents().get(scoreComponentIndex).getQuestions().get(0)
					);
					
					// assert that getScoreComponents is related to current state
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) + ", state = " + stateIndex, 
							1, summary.getScoreComponents().get(scoreComponentIndex).getStates().size() );
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) + ", state = " + stateIndex, 
							stateIndex,
							summary.getScoreComponents().get(scoreComponentIndex).getStates().get(0).intValue()
					);
					
					// multiply marginal (of this state of this question) and expected score of this state of this question
					sum += marginals.get(tradedQuestions.get(questionIndex)).get(stateIndex) // marginal
						* summary.getScoreComponents().get(scoreComponentIndex).getContributionToScoreEV();	 // expected
				}
				
				// assert that, for each question, the sum of expected score per state multiplied by its marginal will result in the total expected score
				// i.e. scoreUserEV = Expected(D=d1)*P(D=d1) + Expected(D=d2)*P(D=d2) = Expected(E=e1)*P(E=e1) + Expected(E=e2)*P(E=e2) = Expected(F=f1)*P(F=f1) + Expected(F=f2)*P(F=f2)
				assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex), summary.getScoreEV(), sum, PROB_ERROR_MARGIN);
			}
		}
		
		// make the same test, but for a random single node
		Long selectedNode = (Math.random()<.3?0x0Fl:(Math.random()<.3?0x0El:0x0Dl));
		
		// extract marginal probability of each question so that we can use them later for consistency check
		marginals = engine.getProbLists(null, null, null);
		assertEquals(marginals.toString(), 3, marginals.keySet().size());	// must be marginals of 1 question (the selected one)
		for (Long key : marginals.keySet()) {
			assertEquals(marginals.toString(), 2, marginals.get(key).size());	// must be nods with 2 states
		}
		
		// add user who did not do any trade in userNameToIDMap in order to test boundary condition (users with no trades at all)
		userNameToIDMap.put("User with no trade " + Long.MAX_VALUE, Long.MAX_VALUE);
		
		for (String user : userNameToIDMap.keySet()) {
			// extract new summary
			summary = engine.getScoreSummaryObject(userNameToIDMap.get(user), selectedNode, null, null);
			
			// most basic assertions
			assertEquals(user, engine.getCash(userNameToIDMap.get(user), null, null), summary.getCash(), ASSET_ERROR_MARGIN);
			assertEquals(user, engine.scoreUserEv(userNameToIDMap.get(user), null, null), summary.getScoreEV(), ASSET_ERROR_MARGIN);
			
			
			// check that getQuestions of summary.getScoreComponents retains the same ordering of engine.getTradedQuestions
//			List<Long> tradedQuestions = engine.getTradedQuestions(userNameToIDMap.get(user));
//			assertFalse(user, tradedQuestions.isEmpty());	// users did actually trade in the system, so its not empty
			assertEquals(user, 1, summary.getScoreComponents().size()/2);	// This time I'm considering only 1 question. Note: again, I'm assuming each question has 2 states
			
			sum = 0f;	// prepare to calculate the sum of (<Expected score given state> * <marginal of state>)
			
			for (int stateIndex = 0; stateIndex < marginals.get(selectedNode).size(); stateIndex++) {
				
				// assert that getScoreComponents is related to current question
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex, 
						1, summary.getScoreComponents().get(stateIndex).getQuestions().size());
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex, 
						selectedNode, summary.getScoreComponents().get(stateIndex).getQuestions().get(0) );
				
				// assert that getScoreComponents is related to current state
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex, 
						1, summary.getScoreComponents().get(stateIndex).getStates().size() );
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex, 
						stateIndex, summary.getScoreComponents().get(stateIndex).getStates().get(0).intValue() );
				
				// multiply marginal (of this state of this question) and expected score of this state of this question
				sum += marginals.get(selectedNode).get(stateIndex) // marginal
				* summary.getScoreComponents().get(stateIndex).getContributionToScoreEV();	 // expected
			}
			
			// assert that, for each question, the sum of expected score per state multiplied by its marginal will result in the total expected score
			// i.e. scoreUserEV = Expected(D=d1)*P(D=d1) + Expected(D=d2)*P(D=d2) = Expected(E=e1)*P(E=e1) + Expected(E=e2)*P(E=e2) = Expected(F=f1)*P(F=f1) + Expected(F=f2)*P(F=f2)
			assertEquals("user = " + user + ", question = " + selectedNode, summary.getScoreEV(), sum, PROB_ERROR_MARGIN);
		}
		
		// repeat the same test, but now using assumptions
		List<Long> assumptionIds = new ArrayList<Long>();
		List<Integer> assumedStates = new ArrayList<Integer>();
		if (Math.random() < .25) {
			assumptionIds.add(0x0Dl);
			assumedStates.add((Math.random()<.5)?0:1);
		}
		if (Math.random() < .25) {
			assumptionIds.add(0x0El);
			assumedStates.add((Math.random()<.5)?0:1);
		}
		if (Math.random() < .25) {
			assumptionIds.add(0x0Fl);
			assumedStates.add((Math.random()<.5)?0:1);
		}
		
		// extract marginal probability of each question so that we can use them later for consistency check
		marginals = engine.getProbLists(null, assumptionIds, assumedStates);
		assertEquals(marginals.toString()+ ", assumptions = " + assumptionIds + assumedStates, 3, marginals.keySet().size());	// must be marginals of 3 questions
		for (Long key : marginals.keySet()) {
			assertEquals(marginals.toString() + ", assumptions = " + assumptionIds + assumedStates, 2, marginals.get(key).size());	// must be nods with 2 states
		}
		
		// add user who did not do any trade in userNameToIDMap in order to test boundary condition (users with no trades at all)
		userNameToIDMap.put("User with no trade " + (Long.MAX_VALUE-1), Long.MAX_VALUE-1);
		
		for (String user : userNameToIDMap.keySet()) {
			// extract new summary
			try {
				summary = engine.getScoreSummaryObject(userNameToIDMap.get(user), null, assumptionIds, assumedStates);
			} catch (IllegalStateException e) {
				e.printStackTrace();
				fail("["+user+"]"+selectedNode+" : "+assumptionIds+"="+assumedStates);
			}
			
			// most basic assertions
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, engine.getCash(userNameToIDMap.get(user), assumptionIds, assumedStates), summary.getCash(), ASSET_ERROR_MARGIN);
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, engine.scoreUserEv(userNameToIDMap.get(user), assumptionIds, assumedStates), summary.getScoreEV(), ASSET_ERROR_MARGIN);
			
			
			// check that getQuestions of summary.getScoreComponents retains the same ordering of engine.getTradedQuestions
			List<Long> tradedQuestions = engine.getTradedQuestions(userNameToIDMap.get(user));
//			assertFalse(user+ ", assumptions = " + assumptionIds + assumedStates, tradedQuestions.isEmpty());	// users did actually trade in the system, so its not empty
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, tradedQuestions.size(), summary.getScoreComponents().size()/2);	// Note: I'm assuming each question has 2 states
			for (int questionIndex = 0; questionIndex < tradedQuestions.size(); questionIndex++) {
				
				sum = 0f;	// prepare to calculate the sum of (<Expected score given state> * <marginal of state>)
				
				for (int stateIndex = 0; stateIndex < marginals.get(tradedQuestions.get(questionIndex)).size(); stateIndex++) {
					
					// Note: I'm assuming that all nodes have 2 states
					int scoreComponentIndex = questionIndex*2 + stateIndex;	
					
					// assert that getScoreComponents is related to current question
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) + ", state = " + stateIndex
							+ ", assumptions = " + assumptionIds + assumedStates, 
							1, summary.getScoreComponents().get(scoreComponentIndex).getQuestions().size());
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) 
							+ ", state = " + stateIndex+ ", assumptions = " + assumptionIds + assumedStates, 
							tradedQuestions.get(questionIndex),
							summary.getScoreComponents().get(scoreComponentIndex).getQuestions().get(0)
					);
					
					// assert that getScoreComponents is related to current state
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) 
							+ ", state = " + stateIndex+ ", assumptions = " + assumptionIds + assumedStates, 
							1, summary.getScoreComponents().get(scoreComponentIndex).getStates().size() );
					assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex) 
							+ ", state = " + stateIndex+ ", assumptions = " + assumptionIds + assumedStates, 
							stateIndex,
							summary.getScoreComponents().get(scoreComponentIndex).getStates().get(0).intValue()
					);
					
					// multiply marginal (of this state of this question) and expected score of this state of this question
					sum += marginals.get(tradedQuestions.get(questionIndex)).get(stateIndex) // marginal
						* summary.getScoreComponents().get(scoreComponentIndex).getContributionToScoreEV();	 // expected
				}
				
				// assert that, for each question, the sum of expected score per state multiplied by its marginal will result in the total expected score
				// i.e. scoreUserEV = Expected(D=d1)*P(D=d1) + Expected(D=d2)*P(D=d2) = Expected(E=e1)*P(E=e1) + Expected(E=e2)*P(E=e2) = Expected(F=f1)*P(F=f1) + Expected(F=f2)*P(F=f2)
				assertEquals("user = " + user + ", question = " + tradedQuestions.get(questionIndex)
						+ ", assumptions = " + assumptionIds + assumedStates, 
						summary.getScoreEV(), sum, PROB_ERROR_MARGIN);
			}
			
		}
		
		// make the same test, but for a random single node and assumptions
		selectedNode = (Math.random()<.3?0x0Fl:(Math.random()<.3?0x0El:0x0Dl));
		
		// extract marginal probability of each question so that we can use them later for consistency check
		marginals = engine.getProbLists(null, assumptionIds, assumedStates);
		assertEquals(marginals.toString()+ ", assumptions = " + assumptionIds + assumedStates, 3, marginals.keySet().size());	// must be marginals of 1 question (the selected one)
		for (Long key : marginals.keySet()) {
			assertEquals(marginals.toString()+ ", assumptions = " + assumptionIds + assumedStates, 2, marginals.get(key).size());	// must be nods with 2 states
		}
		
		// add user who did not do any trade in userNameToIDMap in order to test boundary condition (users with no trades at all)
		userNameToIDMap.put("User with no trade " + (Long.MIN_VALUE+1), Long.MIN_VALUE + 1);
		
		for (String user : userNameToIDMap.keySet()) {
			// extract new summary
			summary = engine.getScoreSummaryObject(userNameToIDMap.get(user), selectedNode, assumptionIds, assumedStates);
			
			// most basic assertions
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, engine.getCash(userNameToIDMap.get(user), assumptionIds, assumedStates), summary.getCash(), ASSET_ERROR_MARGIN);
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, engine.scoreUserEv(userNameToIDMap.get(user), assumptionIds, assumedStates), summary.getScoreEV(), ASSET_ERROR_MARGIN);
			
			
			// check that getQuestions of summary.getScoreComponents retains the same ordering of engine.getTradedQuestions
//			List<Long> tradedQuestions = engine.getTradedQuestions(userNameToIDMap.get(user));
//			assertFalse(user+ ", assumptions = " + assumptionIds + assumedStates, tradedQuestions.isEmpty());	// users did actually trade in the system, so its not empty
			assertEquals(user+ ", assumptions = " + assumptionIds + assumedStates, 1, summary.getScoreComponents().size()/2);	// This time I'm considering only 1 question. Note: again, I'm assuming each question has 2 states
			
			sum = 0f;	// prepare to calculate the sum of (<Expected score given state> * <marginal of state>)
			
			for (int stateIndex = 0; stateIndex < marginals.get(selectedNode).size(); stateIndex++) {
				
				// assert that getScoreComponents is related to current question
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex
						+ ", assumptions = " + assumptionIds + assumedStates, 
						1, summary.getScoreComponents().get(stateIndex).getQuestions().size());
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex
						+ ", assumptions = " + assumptionIds + assumedStates, 
						selectedNode, summary.getScoreComponents().get(stateIndex).getQuestions().get(0) );
				
				// assert that getScoreComponents is related to current state
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex
						+ ", assumptions = " + assumptionIds + assumedStates, 
						1, summary.getScoreComponents().get(stateIndex).getStates().size() );
				assertEquals("user = " + user + ", question = " + selectedNode + ", state = " + stateIndex
						+ ", assumptions = " + assumptionIds + assumedStates, 
						stateIndex, summary.getScoreComponents().get(stateIndex).getStates().get(0).intValue() );
				
				// multiply marginal (of this state of this question) and expected score of this state of this question
				sum += marginals.get(selectedNode).get(stateIndex) // marginal
				* summary.getScoreComponents().get(stateIndex).getContributionToScoreEV();	 // expected
			}
			
			// assert that, for each question, the sum of expected score per state multiplied by its marginal will result in the total expected score
			// i.e. scoreUserEV = Expected(D=d1)*P(D=d1) + Expected(D=d2)*P(D=d2) = Expected(E=e1)*P(E=e1) + Expected(E=e2)*P(E=e2) = Expected(F=f1)*P(F=f1) + Expected(F=f2)*P(F=f2)
			assertEquals("user = " + user + ", question = " + selectedNode
					+ ", assumptions = " + assumptionIds + assumedStates, 
					summary.getScoreEV(), sum, PROB_ERROR_MARGIN);
		}
		
		
		// TODO implement more test cases
	}
	
	/**
	 * Test method for {@link edu.gmu.ace.daggre.MarkovEngineImpl#getQuestionHistory(java.lang.Long, java.util.List, java.util.List)}.
	 */
	public final void testGetQuestionHistory() {
		assertEquals(0, engine.getQuestionHistory(null, null, null).size());
		
		// generate DEF net
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		// check history of actions not related to any question
		int sum = engine.getQuestionHistory(null, null, null).size();
		// there were 4 addCash
		assertEquals(4, engine.getQuestionHistory(null, null, null).size());
		
		// check history of actions related each question
		sum += engine.getQuestionHistory(0x0DL, null, null).size();
		// create node and 1 trade
		assertEquals(2, engine.getQuestionHistory(0x0DL, null, null).size());
		sum += engine.getQuestionHistory(0x0EL, null, null).size();
		// create node and 4 trades
		assertEquals(5, engine.getQuestionHistory(0x0EL, null, null).size());
		sum += engine.getQuestionHistory(0x0FL, null, null).size();
		// create node and 3 trades
		assertEquals(4, engine.getQuestionHistory(0x0FL, null, null).size());
		
		assertEquals(engine.getExecutedActions().size(), sum);
	}
	
	/**
	 * Test method for {@link MarkovEngineImpl#getJointProbability(List, List)}
	 */
	public final void testGetJointProbability() {
		// initialize network
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		
		List<Float> jointProbabilityNoOptimization = new ArrayList<Float>();	// joint prob calculation w/o optimization
		List<Float> jointProbabilityDelegated = new ArrayList<Float>();			// joint prob calculation delegated to class unbbayes.prs.bn.JunctionTree
		List<Float> jointProbabilityLocalOptimization = new ArrayList<Float>(); // joint prob calculation will not propagate evidences if all nodes are in same clique
		List<Float> jointProbabilityManual = new ArrayList<Float>(); 		// joint prob obtained from manually calculating values returned by getProbList 
		
		// check null or empty arguments
		for (int i = 0; i < 100; i++) {
			// random config
			((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(Math.random() < .5);
			((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(Math.random() < .5);
			try {
				// tested cases of questionIds := null, empty, 1 element, 2 elements, 3 elements
				List<Long> questionIds = null;
				if (Math.random() < .2) {
					// questionIds = null;
				} else if (Math.random() < .25) {
					questionIds = Collections.emptyList();
				} else if (Math.random() < .33334) {
					questionIds = Collections.singletonList((Math.random() < .34)?0x0DL:((Math.random() < .5)?0x0EL:0x0FL));
				} else if (Math.random() < .5) {
					questionIds = new ArrayList<Long>();
					questionIds.add(0x0DL);
					questionIds.add((Math.random() < .5)?0x0EL:0x0FL);
				} else {
					questionIds = new ArrayList<Long>();
					questionIds.add(0x0DL);
					questionIds.add(0x0EL);
					questionIds.add(0x0FL);
				}
				// tested cases of states := null, empty, out of range
				List<Integer> states = null;
				if (Math.random() < .34) {
					// states = null;
				} else if (Math.random() < .5) {
					states = Collections.emptyList();
				} else {
					// add at least 1 invalid
					states = new ArrayList<Integer>();
					if (Math.random() < .5) {
						states.add((int) (((Math.random()<.5)?-1:1) * Math.random()*5));
					}
					if (Math.random() < .5) {
						states.add((int) (((Math.random()<.5)?-1:1) * Math.random()*5));
					}
					if (Math.random() < .5) {
						states.add((int) (((Math.random()<.5)?-1:1) * Math.random()*5));
					}
					if (states.isEmpty()) {
						states.add((int) (((Math.random()<.5)?-1:1) * 5));
					} else if (questionIds != null) {
						// if all states are within interval [-3,2], we must force add an invalid state
						boolean hasInvalid = false;
						for (int j = 0; j < Math.min(questionIds.size(),states.size()) ; j++) {
							int state = states.get(j);
							if (state < 0) {
								state = Math.abs(state + 1);
							}
							if (state > 2) {
								hasInvalid = true;
								break;
							}
						}
						if (!hasInvalid) {
							// random generator did not add invalid state. Force to add
							states.set(0, (int) (((Math.random()<.5)?-1:1) * 5));
						}
					}
				}
				float ret = engine.getJointProbability(questionIds, states);
				fail("[" + i + "] should fail on invalid args: questions = " + questionIds + ", states = " + states + ", returned " + ret);
			} catch (IllegalArgumentException e) {
				assertNotNull(e);
			}
		}
		
		// check marginals equals to the ones returned by engine.getProbList(). Tested sequence: {d1, d2, e1, e2 , f1, f2}
		assertEquals(engine.getProbList(0x0DL, null, null).get(0), engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(engine.getProbList(0x0DL, null, null).get(1), engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(engine.getProbList(0x0EL, null, null).get(0), engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(engine.getProbList(0x0EL, null, null).get(1), engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(engine.getProbList(0x0FL, null, null).get(0), engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(engine.getProbList(0x0FL, null, null).get(1), engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0DL))).getMarginalAt(0), 
				engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0DL))).getMarginalAt(1), 
				engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0EL))).getMarginalAt(0), 
				engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0EL))).getMarginalAt(1), 
				engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0FL))).getMarginalAt(0), 
				engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(((ProbabilisticNode)engine.getDefaultInferenceAlgorithm().getRelatedProbabilisticNetwork().getNode(Long.toString(0x0FL))).getMarginalAt(1), 
				engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		
		// test combinations of different combinations of configuration of algorithm, questions, and states
		
		// combo: d1e1
		List<Long> questionIds = new ArrayList<Long>(); List<Integer> states = new ArrayList<Integer>();
		questionIds.add(0x0DL);	states.add(0);  questionIds.add(0x0EL); states.add(0);
		
		// force internal algorithm not to use optimization at all
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(false);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityNoOptimization.add(engine.getJointProbability(questionIds, states));
		
		// Force internal algorithm to delegate calculation of joint probability to JunctionTree class
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityDelegated.add(engine.getJointProbability(questionIds, states));

		// Force internal algorithm to optimize calculation if all nodes are in same clique
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(true);
		jointProbabilityLocalOptimization.add(engine.getJointProbability(questionIds, states));
		
		// man calc := P(e1) * P(d1|e1)
		float manuallyCalculatedValue = engine.getProbList(0x0EL, null, null).get(0);
		manuallyCalculatedValue *= engine.getProbList(0x0DL, Collections.singletonList(0x0EL), Collections.singletonList(0)).get(0);
		jointProbabilityManual.add(manuallyCalculatedValue);
		
		// combo: d2f2
		questionIds = new ArrayList<Long>(); states = new ArrayList<Integer>();
		questionIds.add(0x0DL);	states.add(1);  questionIds.add(0x0FL); states.add(1);
		
		// force internal algorithm not to use optimization at all
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(false);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityNoOptimization.add(engine.getJointProbability(questionIds, states));
		
		// Force internal algorithm to delegate calculation of joint probability to JunctionTree class
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityDelegated.add(engine.getJointProbability(questionIds, states));

		// Force internal algorithm to optimize calculation if all nodes are in same clique
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(true);
		jointProbabilityLocalOptimization.add(engine.getJointProbability(questionIds, states));
		
		// man calc := P(f2) * P(d2|f2)
		manuallyCalculatedValue = engine.getProbList(0x0FL, null, null).get(1);
		manuallyCalculatedValue *= engine.getProbList(0x0DL, Collections.singletonList(0x0FL), Collections.singletonList(1)).get(1);
		jointProbabilityManual.add(manuallyCalculatedValue);
		
		// combo: d1e2f1
		questionIds = new ArrayList<Long>(); states = new ArrayList<Integer>();
		questionIds.add(0x0DL);	states.add(0);  questionIds.add(0x0EL); states.add(1);  questionIds.add(0x0FL); states.add(0);
		
		// force internal algorithm not to use optimization at all
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(false);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityNoOptimization.add(engine.getJointProbability(questionIds, states));
		
		// Force internal algorithm to delegate calculation of joint probability to JunctionTree class
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityDelegated.add(engine.getJointProbability(questionIds, states));

		// Force internal algorithm to optimize calculation if all nodes are in same clique
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(true);
		jointProbabilityLocalOptimization.add(engine.getJointProbability(questionIds, states));

		// man calc := P(f1)*P(d1|f1)*P(e2|f1,d1)
		manuallyCalculatedValue = engine.getProbList(0x0FL, null, null).get(0);
		manuallyCalculatedValue *= engine.getProbList(0x0DL, Collections.singletonList(0x0FL), Collections.singletonList(0)).get(0);
		List<Long> manualAssumptions = new ArrayList<Long>(); 
		List<Integer> manualAssumptionStates = new ArrayList<Integer>();
		manualAssumptions.add(0x0FL); manualAssumptionStates.add(0);
		manualAssumptions.add(0x0DL); manualAssumptionStates.add(0);
		manuallyCalculatedValue *= engine.getProbList(0x0EL, manualAssumptions, manualAssumptionStates).get(1);
		jointProbabilityManual.add(manuallyCalculatedValue);
		
		// combo: f2d2e2
		questionIds = new ArrayList<Long>(); states = new ArrayList<Integer>();
		questionIds.add(0x0FL);	states.add(1);  questionIds.add(0x0DL); states.add(1);  questionIds.add(0x0EL); states.add(1);
		
		// force internal algorithm not to use optimization at all
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(false);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityNoOptimization.add(engine.getJointProbability(questionIds, states));
		
		// Force internal algorithm to delegate calculation of joint probability to JunctionTree class
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(false);
		jointProbabilityDelegated.add(engine.getJointProbability(questionIds, states));

		// Force internal algorithm to optimize calculation if all nodes are in same clique
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToUseEstimatedTotalProbability(true);
		((JunctionTreeAlgorithm)engine.getDefaultInferenceAlgorithm().getProbabilityPropagationDelegator()).setToCalculateJointProbabilityLocally(true);
		jointProbabilityLocalOptimization.add(engine.getJointProbability(questionIds, states));

		// man calc := Prod(clique potential) / Prod (separator potential)
		Clique cliqueDE = (Clique) ((ProbabilisticNode)engine.getProbabilisticNetwork().getNode(Long.toString(0x0EL))).getAssociatedClique();
		Clique cliqueDF = (Clique) ((ProbabilisticNode)engine.getProbabilisticNetwork().getNode(Long.toString(0x0FL))).getAssociatedClique();
		Separator sepD = (Separator) ((ProbabilisticNode)engine.getProbabilisticNetwork().getNode(Long.toString(0x0DL))).getAssociatedClique();
		int coordinate[] = {1,1};
		jointProbabilityManual.add(
				cliqueDE.getProbabilityFunction().getValue(coordinate) 
				* cliqueDF.getProbabilityFunction().getValue(coordinate)
				/ sepD.getProbabilityFunction().getValue(1)
			);
		
		// check that all probabilities are the same
		assertEquals(jointProbabilityDelegated.size(), jointProbabilityLocalOptimization.size());
		assertEquals(jointProbabilityDelegated.size(), jointProbabilityNoOptimization.size());
		for (int i = 0; i < jointProbabilityDelegated.size(); i++) {
			assertEquals("["+i+"]",jointProbabilityManual.get(i), jointProbabilityLocalOptimization.get(i), PROB_ERROR_MARGIN);
			assertEquals("["+i+"]",jointProbabilityManual.get(i), jointProbabilityNoOptimization.get(i), PROB_ERROR_MARGIN);
			assertEquals("["+i+"]",jointProbabilityManual.get(i), jointProbabilityDelegated.get(i), PROB_ERROR_MARGIN);
		}
		
		
		// test joint probability of disconnected network
		// D = [.5,.5]; E = [.8,.2]; F = [.1,.9].
		engine.initialize();
		long transactionKey = engine.startNetworkActions();
		List<Float> initProbs = new ArrayList<Float>();
		initProbs.add(.5f); initProbs.add(.5f);
		engine.addQuestion(transactionKey, new Date(), 0x0DL, 2, initProbs );
		initProbs = new ArrayList<Float>();
		initProbs.add(.8f); initProbs.add(.2f);
		engine.addQuestion(transactionKey, new Date(), 0x0EL, 2, initProbs );
		initProbs = new ArrayList<Float>();
		initProbs.add(.1f); initProbs.add(.9f);
		engine.addQuestion(transactionKey, new Date(), 0x0FL, 2, initProbs );
		engine.commitNetworkActions(transactionKey);
		
		// test marginal probs
		assertEquals(.5f, engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(.5f, engine.getJointProbability(Collections.singletonList(0x0DL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(.8f, engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(.2f, engine.getJointProbability(Collections.singletonList(0x0EL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		assertEquals(.1f, engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(0)), PROB_ERROR_MARGIN);
		assertEquals(.9f, engine.getJointProbability(Collections.singletonList(0x0FL), Collections.singletonList(1)), PROB_ERROR_MARGIN);
		
		// test 2-by-2
		
		// D and E
		questionIds = new ArrayList<Long>();
		questionIds.add(0x0DL);	questionIds.add(0x0EL);
		manualAssumptionStates = new ArrayList<Integer>();
		manualAssumptionStates.add(0); manualAssumptionStates.add(0);
		assertEquals(.4f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,0); manualAssumptionStates.set(1,1);
		assertEquals(.1f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,0);
		assertEquals(.4f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,1);
		assertEquals(.1f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);

		// D and F
		questionIds = new ArrayList<Long>();
		questionIds.add(0x0DL);	questionIds.add(0x0FL);
		manualAssumptionStates = new ArrayList<Integer>();
		manualAssumptionStates.add(0); manualAssumptionStates.add(0);
		assertEquals(.05f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,0); manualAssumptionStates.set(1,1);
		assertEquals(.45f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,0);
		assertEquals(.05f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,1);
		assertEquals(.45f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		
		// E and F
		questionIds = new ArrayList<Long>();
		questionIds.add(0x0EL);	questionIds.add(0x0FL);
		manualAssumptionStates = new ArrayList<Integer>();
		manualAssumptionStates.add(0); manualAssumptionStates.add(0);
		assertEquals(.08f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,0); manualAssumptionStates.set(1,1);
		assertEquals(.72f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,0);
		assertEquals(.02f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.set(0,1); manualAssumptionStates.set(1,1);
		assertEquals(.18f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		
		// P (D,E,F)
		questionIds = new ArrayList<Long>();
		questionIds.add(0x0DL); questionIds.add(0x0EL);	questionIds.add(0x0FL);
		manualAssumptionStates = new ArrayList<Integer>();
		manualAssumptionStates.add(0); manualAssumptionStates.add(0); manualAssumptionStates.add(0);
		assertEquals(.04f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,0); manualAssumptionStates.add(1,0); manualAssumptionStates.set(2,1);
		assertEquals(.36f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,0); manualAssumptionStates.add(1,1); manualAssumptionStates.set(2,0);
		assertEquals(.01f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,0); manualAssumptionStates.add(1,1); manualAssumptionStates.set(2,1);
		assertEquals(.09f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,1); manualAssumptionStates.add(1,0); manualAssumptionStates.set(2,0);
		assertEquals(.04f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,1); manualAssumptionStates.add(1,0); manualAssumptionStates.set(2,1);
		assertEquals(.36f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,1); manualAssumptionStates.add(1,1); manualAssumptionStates.set(2,0);
		assertEquals(.01f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
		manualAssumptionStates.add(0,1); manualAssumptionStates.add(1,1); manualAssumptionStates.set(2,1);
		assertEquals(.09f, engine.getJointProbability(questionIds, manualAssumptionStates), PROB_ERROR_MARGIN);
	}
	
	/**
	 * Test method for {@link MarkovEngineImpl#getMaximumValidAssumptionsSublists(long, List, int)}
	 */
	public final void testGetMaximumValidAssumptionsSublists() {
		this.createDEFNetIn1Transaction(new HashMap<String, Long>());
		
		// check assumptions of D in collection {E,F}
		List<Long> assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0EL); assumptionIds.add(0x0FL);
		List<List<Long>> validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , Integer.MAX_VALUE);
		assertEquals(2, validAssumptions.size());
		for (List<Long> assumption : validAssumptions) {
			assertEquals(1, assumption.size());
			assertTrue("Assumption = " + assumption, assumption.contains(0x0EL) || assumption.contains(0x0FL));
		}
		// search for 0 or negative sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , (int)(Math.random()*Integer.MIN_VALUE));
		assertEquals(0, validAssumptions.size());
		// search for 1 sublist
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , 1);
		assertEquals(1, validAssumptions.size());
		for (List<Long> assumption : validAssumptions) {
			assertEquals(1, assumption.size());
			assertTrue("Assumption = " + assumption, assumption.contains(0x0EL) || assumption.contains(0x0FL));
		}
		// search for 2 sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , 2);
		assertEquals(2, validAssumptions.size());
		for (List<Long> assumption : validAssumptions) {
			assertEquals(1, assumption.size());
			assertTrue("Assumption = " + assumption, assumption.contains(0x0EL) || assumption.contains(0x0FL));
		}
		// search for 3 or more sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , (int)(3 + (Math.random() * (Integer.MAX_VALUE - 3))));
		assertEquals(2, validAssumptions.size());
		for (List<Long> assumption : validAssumptions) {
			assertEquals(1, assumption.size());
			assertTrue("Assumption = " + assumption, assumption.contains(0x0EL) || assumption.contains(0x0FL));
		}
		
		// check assumptions of D in collection {E}
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0EL);
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , Integer.MAX_VALUE);
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0EL));
		// search for 0 or negative sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , (int)(Math.random()*Integer.MIN_VALUE));
		assertEquals(0, validAssumptions.size());
		// search for 1 sublist
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , 1);
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0EL));
		// search for 2 or more sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0DL, assumptionIds , (int)(2 + (Math.random() * (Integer.MAX_VALUE - 2))));
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0EL));
		
		// check assumptions of F in collection {D, E}
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0DL); assumptionIds.add(0x0EL);
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , Integer.MAX_VALUE);
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0DL));
		// search for 0 or negative sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , (int)(Math.random()*Integer.MIN_VALUE));
		assertEquals(0, validAssumptions.size());
		// search for 1 sublist
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , 1);
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0DL));
		// search for 2 or more sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , (int)(2 + (Math.random() * (Integer.MAX_VALUE - 2))));
		assertEquals(1, validAssumptions.size());
		assertEquals(1, validAssumptions.get(0).size());
		assertTrue("Assumption = " + validAssumptions, validAssumptions.get(0).contains(0x0DL));
		

		// check assumptions of E in collection {F}
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0FL);
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0EL, assumptionIds , Integer.MAX_VALUE);
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
		// search for 0 or negative sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0EL, assumptionIds , (int)(Math.random()*Integer.MIN_VALUE));
		assertEquals(0, validAssumptions.size());
		// search for 1 sublist
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0EL, assumptionIds , 1);
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
		// search for 2 or more sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0EL, assumptionIds , (int)(2 + (Math.random() * (Integer.MAX_VALUE - 2))));
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
		
		// disconnected net case
		engine.initialize();
		long transactionKey = engine.startNetworkActions();
		engine.addQuestion(transactionKey, new Date(), 0x0DL, 3, null);
		engine.addQuestion(transactionKey, new Date(), 0x0EL, 3, null);
		engine.addQuestion(transactionKey, new Date(), 0x0FL, 3, null);
		engine.commitNetworkActions(transactionKey);
		
		// check assumptions of F in collection {D, E}
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0DL); assumptionIds.add(0x0EL);
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , Integer.MAX_VALUE);
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
		// search for 0 or negative sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , (int)(Math.random()*Integer.MIN_VALUE));
		assertEquals(0, validAssumptions.size());
		// search for 1 sublist
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , 1);
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
		// search for 2 or more sublists
		validAssumptions = engine.getMaximumValidAssumptionsSublists(0x0FL, assumptionIds , (int)(2 + (Math.random() * (Integer.MAX_VALUE - 2))));
		assertEquals(1, validAssumptions.size());
		assertEquals(0, validAssumptions.get(0).size());
	}
	
//	/**
//	 * Check that if Gain(X) = Prob(X=true) * ExpectedGain(X=true) + Prob(X=false) * ExpectedGain(X=false),
//	 * then Sum(Gain(Xi)) for all nodes will result in global expected gain.
//	 * This will test the DEF net after the trades of {@link #testAddTrade()}.
//	 */
//	public final void testQuestionLevelScoreGain() {
//		// crate transaction
//		long transactionKey = engine.startNetworkActions();
//		// create nodes D, E, F
//		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
//		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
//		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
//		// create edge D->E 
//		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
//		// create edge D->F
//		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
//		engine.addCash(transactionKey, new Date(), Long.MAX_VALUE, engine.getScoreFromQValues(100), "adding 100 q");
//		engine.commitNetworkActions(transactionKey);
//		
//		// extract the marginal probabilities of all nodes
//		Map<Long, List<Float>> marginals = engine.getProbLists(null, null, null);
//		assertEquals(10.0,engine.scoreUserEv(Long.MAX_VALUE, null, null),ASSET_ERROR_MARGIN);
//		assertEquals(10.0,engine.getCash(Long.MAX_VALUE, null, null),ASSET_ERROR_MARGIN);
//		
//		// multiply with expected scores conditioned to states and compare the sum with engine.scoreUserEv
//		// extract base so that we can obtain the score = base + gain -> gain = score - base.
////			float base = engine.getCash(userNameToIDMap.get(name), null, null);
//		float base = engine.getScoreFromQValues(100f);
//		try {
//			engine.getAlgorithmAndAssetNetFromUserID(Long.MAX_VALUE).setExpectedAssetPivot(base);
//			base = 0;
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(Long.MAX_VALUE+"");
//		} 
//		double sum = 0;
//		double sumWithoutProb = 0;
//		for (Long questionId : marginals.keySet()) {
//			List<Float> scorePerState = engine.scoreUserQuestionEvStates(Long.MAX_VALUE, questionId, null, null);
//			// the quantity of states must match
//			assertEquals("user = " + Long.MAX_VALUE + ", question = " + questionId, marginals.get(questionId).size(), scorePerState.size());
//			for (int i = 0; i < scorePerState.size(); i++) {
//				// Prob(X=i) * ExpectedGain(X=i). Note: ExpectedGain(X=i) = scoreUserQuestionEvStates[i] - cash.
//				sum += marginals.get(questionId).get(i) * (scorePerState.get(i) - base);
//				sumWithoutProb += (scorePerState.get(i) - base);
//			}
//		}
//		assertEquals(Long.MAX_VALUE+"", engine.scoreUserEv(Long.MAX_VALUE, null, null)- base, sum, PROB_ERROR_MARGIN);
//		
//		engine.initialize();
//		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
//		this.createDEFNetIn1Transaction(userNameToIDMap );
//		
//		// extract the marginal probabilities of all nodes
//		marginals = engine.getProbLists(null, null, null);
//		
//		// multiply with expected scores conditioned to states and compare the sum with engine.scoreUserEv
//		for (String name : userNameToIDMap.keySet()) {
//			// extract base so that we can obtain the score = base + gain -> gain = score - base.
////			base = engine.getCash(userNameToIDMap.get(name), null, null);
//			base = engine.getScoreFromQValues(100f);
//			try {
////				engine.getAlgorithmAndAssetNetFromUserID(userNameToIDMap.get(name)).setExpectedAssetPivot(base);
//				base = 0;
//			} catch (Exception e) {
//				e.printStackTrace();
//				fail(name);
//			} 
//			sum = 0;
//			sumWithoutProb = 0;
//			for (Long questionId : marginals.keySet()) {
//				List<Float> scorePerState = engine.scoreUserQuestionEvStates(userNameToIDMap.get(name), questionId, null, null);
//				// the quantity of states must match
//				assertEquals("user = " + name + ", question = " + questionId, marginals.get(questionId).size(), scorePerState.size());
//				for (int i = 0; i < scorePerState.size(); i++) {
//					// Prob(X=i) * ExpectedGain(X=i). Note: ExpectedGain(X=i) = scoreUserQuestionEvStates[i] - cash.
//					sum += marginals.get(questionId).get(i) * (scorePerState.get(i) - base);
//					sumWithoutProb += (scorePerState.get(i) - base);
//				}
//			}
//			float globalGain = engine.scoreUserEv(userNameToIDMap.get(name), null, null)- base;
//			if (Math.abs(globalGain - sum) < Math.abs(globalGain - sumWithoutProb)) {
//				assertEquals(name, globalGain, sum, 0.01);
//			} else {
//				assertEquals(name, globalGain, sumWithoutProb, 0.01);
//			}
//		}
//	}

	/**
	 * @param isToUseQValues the isToUseQValues to set
	 */
	public void setToUseQValues(boolean isToUseQValues) {
		this.isToUseQValues = isToUseQValues;
	}

	/**
	 * @return the isToUseQValues
	 */
	public boolean isToUseQValues() {
		return isToUseQValues;
	}
	
	/**
	 * Performs the same of {@link #testAddTrade()}, but all transactions
	 * will use transactionKey == null
	 */
	public final void testAddTradeNullTransactionKey() {
		
		// create nodes D, E, F
		engine.addQuestion(null, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(null, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(null, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		// create edge D->E 
		engine.addQuestionAssumption(null, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F
		engine.addQuestionAssumption(null, new Date(), 0x0F, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// commit changes
		
		
		// Let's use ID = 0 for the user Tom 
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		userNameToIDMap.put("Tom", (long)0);
		
		// By default, cash is initialized as 0 (i.e. min-q = 1)
		assertEquals(0, engine.getCash(userNameToIDMap.get("Tom"), null, null), ASSET_ERROR_MARGIN);
		assertEquals(1, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(null, new Date(), userNameToIDMap.get("Tom"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		// check that user's min-q value was changed to the correct value
		assertEquals(100, engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Tom"), null, null)), ASSET_ERROR_MARGIN);
		
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
		List<Float> newValues = new ArrayList<Float>(2);
		newValues.add(0.55f);		// P(E=e1) = 0.55
		newValues.add(0.45f);		// P(E=e2) = 1 - P(E=e1) = 0.45
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Tom bets P(E=e1) = 0.5  to 0.55", 
				userNameToIDMap.get("Tom"), 
				0x0E, 	// question E
				newValues,
				null, 	// no assumptions
				null, 	// no states of the assumptions
				false	// do not allow negative
			).size());
		
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
		assertEquals((engine.getScoreFromQValues(90f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(90f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		newValues = new ArrayList<Float>();
		newValues.add(.9f);
		newValues.add(.1f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Tom bets P(E=e1|D=d1) = 0.9", 
				userNameToIDMap.get("Tom"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(null, new Date(), userNameToIDMap.get("Joe"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Joe"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Joe"), null, null)), ASSET_ERROR_MARGIN);

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
		newValues = new ArrayList<Float>();
		newValues.add(.4f);
		newValues.add(.6f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Joe bets P(E=e1|D=d2) = 0.4", 
				userNameToIDMap.get("Joe"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		
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
		assertEquals((engine.getScoreFromQValues(72.727272f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(72.727272f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(null, new Date(), userNameToIDMap.get("Amy"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Amy"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Amy"), null, null)), ASSET_ERROR_MARGIN);

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
		newValues = new ArrayList<Float>();
		newValues.add(.3f);
		newValues.add(.7f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Amy bets P(F=f1|D=d1) = 0.3", 
				userNameToIDMap.get("Amy"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		newValues = new ArrayList<Float>();
		newValues.add(.1f);
		newValues.add(.9f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Joe bets P(F=f1|D=d2) = 0.1", 
				userNameToIDMap.get("Joe"), 
				0x0F, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals(0, (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);
		assertEquals(1, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		
		// add 100 q-values to new users
		assertTrue(engine.addCash(null, new Date(), userNameToIDMap.get("Eric"), engine.getScoreFromQValues(100f), "Initialize User's asset to 100"));
		// check that user's min-q value was changed to the correct value
		assertEquals(100, (engine.getQValuesFromScore(engine.getCash(userNameToIDMap.get("Eric"), null, null))), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(100)), (engine.getCash(userNameToIDMap.get("Eric"), null, null)), ASSET_ERROR_MARGIN);

		
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
		newValues = new ArrayList<Float>();
		newValues.add(.8f);
		newValues.add(.2f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Eric bets P(E=e1) = 0.8", 
				userNameToIDMap.get("Eric"), 
				0x0E, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
			).size());
		
		
		assertEquals(10.1177, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(57.142857f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(57.142857f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		newValues = new ArrayList<Float>();
		newValues.add(.7f);
		newValues.add(.3f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				false
		).size());
		

		assertEquals(10.31615, engine.scoreUserEv(userNameToIDMap.get("Eric"), null, null), PROB_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// set P(D=d1|F=f2) to a value lower (1/10) than the lower bound of edit interval
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		try {
			assertNull(engine.addTrade(
					null, 
					new Date(), 
					"Eric bets P(D=d1|F=f2) = 0.7", 
					userNameToIDMap.get("Eric"), 
					0x0D, 
					newValues, 
					assumptionIds, 
					assumedStates, 
					false	// do not allow negative assets
			));
			fail("It should throw an exception indicating that the assets whent to zero or negative");
		} catch (ZeroAssetsException e) {
			assertNotNull(e);
		}
		// this is supposedly going to commit empty transaction
		// make sure history was not changed
		assertEquals(questionHistory, engine.getQuestionHistory(0x0DL, null, null));

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// add question disconnected question C
		engine.addQuestion(null, new Date(), (long)0x0C, 2, null);
		

		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		
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
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		newValues = new ArrayList<Float>();
		newValues.add(.05f);
		newValues.add(.95f);
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Amy bets P(C=c1) = 0.05", 
				userNameToIDMap.get("Amy"), 
				0x0C, 
				newValues, 
				null, 
				null, 
				false
			).size());
		
		
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
		
		// check that min-q is 6...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals((engine.getScoreFromQValues(6f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(6f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that new LPE of Amy is independent of E
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(0)), ASSET_ERROR_MARGIN);
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(1)), ASSET_ERROR_MARGIN);
		
		// check that LPE is d1 c1 f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0C);		// 2nd node is C; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, c1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// c1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, c1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, c2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, c2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, c1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check incomplete condition of LPE: c1
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
		newValues = new ArrayList<Float>();
		newValues.add(editInterval.get(0)/10);
		newValues.add(1-(editInterval.get(0)/10));
		assertEquals(2, engine.addTrade(
				null, 
				new Date(), 
				"Eric bets P(D=d1|F=f2) = 0.7", 
				userNameToIDMap.get("Eric"), 
				0x0D, 
				newValues, 
				assumptionIds, 
				assumedStates, 
				true	// allow negative assets
			).size());
		
		
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
	
		Map<Long, List<Float>> probListsBeforeTrade = engine.getProbLists(null, null, null);
		
		// test the case in which the trade will make the assets to go negative, but it cannot be previewed (it will throw exception only on commit)
		
		// add a new question in the same transaction, so that we guarantee that trade cannot be previewed
		List<Float> initProbs = new ArrayList<Float>();
		initProbs.add(.9f); initProbs.add(.0999f); initProbs.add(.0001f);
		engine.addQuestion(null, new Date(), 0x0AL, 3, initProbs);
		
		// add a trade which will make user asset to go below zero and cannot be previewed
		newValues = new ArrayList<Float>();
		newValues.add(.0001f); newValues.add(.0999f);  newValues.add(.9f); 
		try {
			assertTrue( engine.addTrade(
					null, 
					new Date(), 
					"Amy bets P(A = [.0001, .0999, .9])", 
					userNameToIDMap.get("Amy"), 
					0x0AL, 
					newValues, 
					null, 
					null, 
					false
			).isEmpty());
			fail("This is expected to throw ZeroAssetsException");
		} catch (ZeroAssetsException e) {
			assertNotNull(e);
		}
		
		// probability of nodes present before this transaction must remain unchanged
		Map<Long, List<Float>> probListsAfterTrade = engine.getProbLists(null, null, null);
		for (Long id : probListsBeforeTrade.keySet()) {
			assertEquals("question = " + id , probListsBeforeTrade.get(id).size(), probListsAfterTrade.get(id).size());
			for (int i = 0; i < probListsBeforeTrade.get(id).size(); i++) {
				assertEquals("Question = " + id , probListsBeforeTrade.get(id).get(i), probListsAfterTrade.get(id).get(i), PROB_ERROR_MARGIN);
			}
		}
		
		// check that final min-q of Tom is 20
		minCash = engine.getCash(userNameToIDMap.get("Tom"), null, null);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		
		// check that final LPE of Tom contains d1, e2 and any value F
		assumedStates = new ArrayList<Integer>();
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add(0x0DL); assumptionIds.add(0x0EL); assumptionIds.add(0x0FL); 
		
		// check combination d1, e1, f1 (not min)
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
		
		
		// test invalid assumptions
		
		// add a in which the assumptions will be ignored
		newValues = new ArrayList<Float>();
		newValues.add(.5f); newValues.add(.5f); 
		assertFalse( engine.addTrade(
				null, 
				new Date(), 
				"Tom bets P(F|E = e2) = [.5,.5]", 
				userNameToIDMap.get("Tom"), 
				0x0FL, 
				newValues, 
				Collections.singletonList(0x0EL), 
				Collections.singletonList(1), 
				false
			).isEmpty());
		
		// check that marginal of F is [.5,.5] (i.e. condition E was ignored)
		probList = engine.getProbList(0x0FL, null, null);
		assertEquals(2, probList.size());
		assertEquals(.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that in the history, the assumption E was ignored
		questionHistory = engine.getQuestionHistory(0x0FL, null, null);
		assertNotNull(questionHistory);
		assertFalse(questionHistory.isEmpty());
		AddTradeNetworkAction action = (AddTradeNetworkAction) questionHistory.get(questionHistory.size()-1);
		assertEquals((long)0x0F, (long)action.getQuestionId());
		assertTrue("Assumptions = " + action.getTradeId(), action.getAssumptionIds().isEmpty());
		
		List<Float> editLimits = engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0DL, 0, null, null);
		assertEquals(2, editLimits.size());
		assertTrue(editLimits.toString() , editLimits.get(0) > 0);
		assertTrue(editLimits.toString() , editLimits.get(0) < 1);
		assertTrue(editLimits.toString() , editLimits.get(1) > 0);
		assertTrue(editLimits.toString() , editLimits.get(1) < 1);
		assertTrue(editLimits.toString() , editLimits.get(0) < 0.5 && 0.5 < editLimits.get(1));
	
		// test disconnected assumptions
		
		// add a in which the assumptions will be ignored
		newValues = new ArrayList<Float>();
		newValues.add(.5f); newValues.add(.5f); 
		assertFalse( engine.addTrade(
				null, 
				new Date(), 
				"Tom bets P(D|A = a1) = [.5,.5]", 
				userNameToIDMap.get("Tom"), 
				0x0DL, 
				newValues, 
				Collections.singletonList(0x0AL), 
				Collections.singletonList(0), 
				false
			).isEmpty());
		
		// check that marginal of D is [.5,.5] (i.e. condition A was ignored)
		probList = engine.getProbList(0x0DL, null, null);
		assertEquals(2, probList.size());
		assertEquals(.5f, probList.get(0), PROB_ERROR_MARGIN);
		assertEquals(.5f, probList.get(1), PROB_ERROR_MARGIN);
		
		// check that in the history, the assumption A was ignored
		questionHistory = engine.getQuestionHistory(0x0DL, null, null);
		assertNotNull(questionHistory);
		assertFalse(questionHistory.isEmpty());
		action = (AddTradeNetworkAction) questionHistory.get(questionHistory.size()-1);
		assertEquals((long)0x0D, (long)action.getQuestionId());
		assertTrue("Assumptions = " + action.getTradeId(), action.getAssumptionIds().isEmpty());
		
		assertNotNull(engine.getScoreSummaryObject(userNameToIDMap.get("Tom"), null, null, null));
		assertFalse(engine.getQuestionHistory(null, null, null).isEmpty());
		assertFalse(engine.previewBalancingTrade(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(Float.isNaN(engine.getCash(userNameToIDMap.get("Tom"), null, null)));
		assertNotNull(engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0AL, 2, null, null));
		assertEquals(0,engine.getMaximumValidAssumptionsSublists(0x0AL, null, 1).get(0).size());
		assertTrue(engine.getPossibleQuestionAssumptions(0x0A, null).isEmpty());
		assertNotNull(engine.getScoreDetails(userNameToIDMap.get("Tom"), 0x0AL, null, null));
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		
		
		
		// test condition in which min is below 0 even though no entry in asset table is below 0
		
		// backup values before edit
		probListsBeforeTrade = engine.getProbLists(null, null, null);
		
		// assert that the min is positive
		assertTrue("Cash = " + engine.getCash(userNameToIDMap.get("Amy"), null, null), engine.getCash(userNameToIDMap.get("Amy"), null, null) > 0f);
		
		float emptySeparatorsDefaultContent = Float.NaN;
		try {
			emptySeparatorsDefaultContent = engine.getAlgorithmAndAssetNetFromUserID(userNameToIDMap.get("Amy")).getEmptySeparatorsDefaultContent();
			engine.getAlgorithmAndAssetNetFromUserID(userNameToIDMap.get("Amy")).setEmptySeparatorsDefaultContent(20);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidParentException e) {
			e.printStackTrace();
			fail();
		}
		
		// now the min should be negative
		assertFalse("Cash = " + engine.getCash(userNameToIDMap.get("Amy"), null, null), engine.getCash(userNameToIDMap.get("Amy"), null, null) > 0f);
		
		// do trade 
		newValues = new ArrayList<Float>();
		newValues.add(.3f); newValues.add(.3f); newValues.add(.4f);
		try {
			engine.addTrade(
					null, 
					new Date(), 
					"Amy makes a trade P(A) = [.3,.3,.4].", 
					userNameToIDMap.get("Amy"), 
					0X0AL, 
					newValues, 
					null, 
					null, 
					false
			);
			fail("Should throw exception indicating 0 or negative min");
		} catch (ZeroAssetsException e) {
			assertNotNull(e);
		}
		
		// probability of nodes present before this transaction must remain unchanged
		probListsAfterTrade = engine.getProbLists(null, null, null);
		for (Long id : probListsBeforeTrade.keySet()) {
			assertEquals("question = " + id , probListsBeforeTrade.get(id).size(), probListsAfterTrade.get(id).size());
			for (int i = 0; i < probListsBeforeTrade.get(id).size(); i++) {
				assertEquals("Question = " + id , probListsBeforeTrade.get(id).get(i), probListsAfterTrade.get(id).get(i), PROB_ERROR_MARGIN);
			}
		}
		
		
		try {
			engine.getAlgorithmAndAssetNetFromUserID(userNameToIDMap.get("Amy")).setEmptySeparatorsDefaultContent(emptySeparatorsDefaultContent);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidParentException e) {
			e.printStackTrace();
			fail();
		}
		
		// check that final min-q of Amy remains 6...
		minCash = engine.getCash(userNameToIDMap.get("Amy"), null, null);
		assertEquals((engine.getScoreFromQValues(6f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(6f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
		// check that new LPE of Amy is independent of E
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(0)), ASSET_ERROR_MARGIN);
		assertEquals(minCash, engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList(0x0EL), Collections.singletonList(1)), ASSET_ERROR_MARGIN);
		
		// check that LPE is d1 c1 f1
		assumptionIds = new ArrayList<Long>();
		assumptionIds.add((long) 0x0D);		// 1st node is D; assumedStates must follow this order
		assumptionIds.add((long) 0x0C);		// 2nd node is C; assumedStates must follow this order
		assumptionIds.add((long) 0x0F);		// 3rd node is F; assumedStates must follow this order
		
		// check combination d1, c1, f1
		assumedStates = new ArrayList<Integer>();
		assumedStates.add(0);	// d1
		assumedStates.add(0);	// c1
		assumedStates.add(0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		
		// check combination d1, c1, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d1, c2, f1
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d1, c2, f2
		assumedStates.set(0, 0);	// d1
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c1, f1 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check combination d2, c1, f2 
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 0);	// c1
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f1
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 0);	// f1
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);

		// check combination d2, c2, f2
		assumedStates.set(0, 1);	// d2
		assumedStates.set(1, 1);	// c2
		assumedStates.set(2, 1);	// f2
		cash = engine.getCash(userNameToIDMap.get("Amy"), assumptionIds, assumedStates);
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		// check incomplete condition of LPE: c1
		cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(0));
		assertEquals(minCash, cash, ASSET_ERROR_MARGIN);
		cash = engine.getCash(userNameToIDMap.get("Amy"), Collections.singletonList((long)0x0C), Collections.singletonList(1));
		assertTrue("Obtained cash = " + cash, minCash < cash);
		
		
		
		assertNotNull(engine.getScoreSummaryObject(userNameToIDMap.get("Tom"), null, null, null));
		assertFalse(engine.getQuestionHistory(null, null, null).isEmpty());
		assertFalse(engine.previewBalancingTrade(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(Float.isNaN(engine.getCash(userNameToIDMap.get("Tom"), null, null)));
		assertNotNull(engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0AL, 2, null, null));
		assertEquals(0,engine.getMaximumValidAssumptionsSublists(0x0AL, null, 1).get(0).size());
		assertTrue(engine.getPossibleQuestionAssumptions(0x0A, null).isEmpty());
		assertNotNull(engine.getScoreDetails(userNameToIDMap.get("Tom"), 0x0AL, null, null));
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		
		// now, the trade should be OK to do this trade, because the empty separators were fixed
		engine.addTrade(
				null, 
				new Date(), 
				"Amy makes a trade P(A) = [.3,.3,.4].", 
				userNameToIDMap.get("Amy"), 
				0X0AL, 
				newValues, 
				null, 
				null, 
				false
		);
		
		// marginal is [.3,.3,.4]
		probList = engine.getProbList(0x0AL, null, null);
		assertEquals(3, probList.size());
		assertEquals(probList.get(0), .3f, PROB_ERROR_MARGIN);
		assertEquals(probList.get(1), .3f, PROB_ERROR_MARGIN);
		assertEquals(probList.get(2), .4f, PROB_ERROR_MARGIN);
		
		// assert that the min is positive
		assertTrue("Cash = " + engine.getCash(userNameToIDMap.get("Amy"), null, null), engine.getCash(userNameToIDMap.get("Amy"), null, null) > 0f);
		
		
		assertNotNull(engine.getScoreSummaryObject(userNameToIDMap.get("Tom"), null, null, null));
		assertFalse(engine.getQuestionHistory(null, null, null).isEmpty());
		assertFalse(engine.previewBalancingTrade(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(engine.getAssetsIfStates(userNameToIDMap.get("Tom"), 0x0AL, null, null).isEmpty());
		assertFalse(Float.isNaN(engine.getCash(userNameToIDMap.get("Tom"), null, null)));
		assertNotNull(engine.getEditLimits(userNameToIDMap.get("Tom"), 0x0AL, 2, null, null));
		assertEquals(0,engine.getMaximumValidAssumptionsSublists(0x0AL, null, 1).get(0).size());
		assertTrue(engine.getPossibleQuestionAssumptions(0x0A, null).isEmpty());
		assertNotNull(engine.getScoreDetails(userNameToIDMap.get("Tom"), 0x0AL, null, null));
		assertFalse(engine.getQuestionAssumptionGroups().isEmpty());
		
	}
	
	
	/**
	 * Performs {@link #testAddTradeInOneTransaction()}, but with duplicated arcs (i.e. duplicate insertion of arcs)
	 */
	public final void testAddTradeInOneTransactionWithDuplicateArcs () {
		
		// crate transaction
		long transactionKey = engine.startNetworkActions();
		
		// create nodes D, E, F
		engine.addQuestion(transactionKey, new Date(), 0x0D, 2, null);	// question D has ID = hexadecimal D. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0E, 2, null);	// question E has ID = hexadecimal E. CPD == null -> linear distro
		engine.addQuestion(transactionKey, new Date(), 0x0F, 2, null);	// question F has ID = hexadecimal F. CPD == null -> linear distro
		
		List<Long> duplicateParents = new ArrayList<Long>();
		duplicateParents.add(0x0DL);
		duplicateParents.add(0x0DL);
		
		// create edge [D,D]->E 
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, duplicateParents , null);	// cpd == null -> linear distro
		// create edge [D,D]->F
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0F, duplicateParents, null);	// cpd == null -> linear distro
		// create edge D->E again
		engine.addQuestionAssumption(transactionKey, new Date(), 0x0E, Collections.singletonList((long) 0x0D), null);	// cpd == null -> linear distro
		// create edge D->F again
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
		
		// check that the DEF network has a correct structure
		assertEquals(1,engine.getProbabilisticNetwork().getNode(""+0x0EL).getParents().size());
		assertEquals(1,engine.getProbabilisticNetwork().getNode(""+0x0FL).getParents().size());
		assertEquals(2,engine.getProbabilisticNetwork().getJunctionTree().getCliques().size());
		assertEquals(1,engine.getProbabilisticNetwork().getJunctionTree().getSeparators().size());
		assertEquals(2,engine.getProbabilisticNetwork().getEdges().size());
		
		
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
		assertEquals((engine.getScoreFromQValues(20f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(20f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(60f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(60f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(14.5454545f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(14.5454545f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
		assertEquals((engine.getScoreFromQValues(35.7393f)), (minCash), ASSET_ERROR_MARGIN);
		assertEquals(35.7393f, (engine.getQValuesFromScore(minCash)), ASSET_ERROR_MARGIN);
		
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
	 * This method is used as a regression test of a bug which causes the
	 * {@link ProbabilisticNode#clone()}, called from {@link MarkovEngineImpl#scoreUserEv(long, List, List)} 
	 * not to copy empty cliques, causing inconsistency between probabilistic network and asset network.
	 */
	public final void testConditionalExpectedScoreWithEmptyCliques() {
		engine.setDefaultInitialAssetTableValue(100);
		engine.setToDeleteResolvedNode(true);
		engine.addQuestion(null, new Date(), 1L, 3, null);
		engine.addQuestion(null, new Date(), 2L, 3, null);
		engine.addQuestion(null, new Date(), 3L, 3, null);
		engine.resolveQuestion(null, new Date(), 1, 0);
		engine.resolveQuestion(null, new Date(), 2, 0);
		assertEquals(1f, engine.getProbList(1, null, null).get(0));
		assertEquals(1f, engine.getProbList(2, null, null).get(0));
		try {
			float scoreUserEv = engine.scoreUserEv(Long.MAX_VALUE, Collections.singletonList(3L), Collections.singletonList(0));
			assertTrue("score = " + scoreUserEv, scoreUserEv > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Tests conditional min assets when user has 0 or negative assets.
	 * This was created because the min propagation was failing to
	 * set findings when there were non positive values
	 * in the asset table.
	 */
	public final void testConditional0OrNegativeAssets() {
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		List<Long> assumptionIds = new ArrayList<Long>();
		List<Integer> assumedStates = new ArrayList<Integer>();
		assumptionIds.add(0x0DL); assumedStates.add(1);
		assumptionIds.add(0x0FL); assumedStates.add(0);
		assertEquals(0f,engine.getCash(Long.MAX_VALUE, assumptionIds, assumedStates), ASSET_ERROR_MARGIN);
		assertNotNull(engine.getScoreSummaryObject(Long.MIN_VALUE, null, assumptionIds, assumedStates));
		
	}
	
	/**
	 * Tests the following sequence of actions in a DEF network: <br/>
	 * 1 - Balance some question X<br/>
	 * 2 - Resolve question X<br/>
	 * 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})<br/>
	 * <br/><br/>
	 * Case 2: <br/>
	 * 1 - Balance some question X assuming Y<br/>
	 * 2 - Resolve question Y<br/>
	 * 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})<br/>
	 * <br/>
	 * This was created because engine was failing to redo trades when resolved questions are present.
	 */
	public final void testBalanceResolveRebuild() {
		// test with an engine which does not delete resolved nodes
		engine.setToDeleteResolvedNode(false);
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		// choose a random user
		String user = new ArrayList<String>(userNameToIDMap.keySet()).get((int) (Math.random()*userNameToIDMap.keySet().size()));
		
		// choose a random question from question which user has traded at least once
		List<Long> tradedQuestions = engine.getTradedQuestions(userNameToIDMap.get(user));
		Long questionId = tradedQuestions.get((int) (Math.random()*tradedQuestions.size()));
		
		// 0 - Resolve some question which is not the selected question
		Map<Long, List<Float>> probMap = engine.getProbLists(null, null, null);
		probMap.remove(questionId);
		engine.resolveQuestion(null, new Date(), probMap.keySet().iterator().next(), (Math.random()<.5)?0:1);
		
		// 1 - Balance question
		assertTrue(engine.doBalanceTrade(null, new Date(), user + " balances question " + questionId, userNameToIDMap.get(user), questionId, null, null));
		
		// 2 - Resolve question
		assertTrue(engine.resolveQuestion(null, new Date(), questionId, (Math.random()<.5)?0:1));
		
		// 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})
		assertTrue(engine.addQuestion(null, new Date(), Long.MIN_VALUE, 2, null));
		
		// case 2
		engine.initialize();
		userNameToIDMap.clear();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		
		List<Long> assumptionIds = Collections.singletonList(engine.getPossibleQuestionAssumptions(questionId, null).get(0));
		
		// 0 - Resolve some question which is not the selected questions
		probMap = engine.getProbLists(null, null, null);
		probMap.remove(questionId);
		probMap.remove(assumptionIds.get(0));
		assertFalse(probMap.isEmpty());
		engine.resolveQuestion(null, new Date(), probMap.keySet().iterator().next(), (Math.random()<.5)?0:1);
		
		// 1 - Balance question given assumptions
		assertTrue(engine.doBalanceTrade(null, new Date(), user + " balances question " + questionId, 
				userNameToIDMap.get(user), questionId, assumptionIds, Collections.singletonList((Math.random()<.5)?0:1)));
		
		// 2 - Resolve assumed question
		assertTrue(engine.resolveQuestion(null, new Date(), assumptionIds.get(0), (Math.random()<.5)?0:1));
		
		// 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})
		assertTrue(engine.addQuestion(null, new Date(), Long.MAX_VALUE, 2, null));
		
		// test with an engine which deletes resolved nodes
		engine.setToDeleteResolvedNode(true);
		engine.initialize();
		userNameToIDMap.clear();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		// 1 - Balance question
		assertTrue(engine.doBalanceTrade(null, new Date(), user + " balances question " + questionId, userNameToIDMap.get(user), questionId, null, null));
		
		// 2 - Resolve question
		assertTrue(engine.resolveQuestion(null, new Date(), questionId, (Math.random()<.5)?0:1));
		
		// 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})
		assertTrue(engine.addQuestion(null, new Date(), Long.MIN_VALUE, 2, null));
		
		// case 2
		engine.initialize();
		userNameToIDMap.clear();
		this.createDEFNetIn1Transaction(userNameToIDMap);
		
		assumptionIds = Collections.singletonList(engine.getPossibleQuestionAssumptions(questionId, null).get(0));
		
		// 1 - Balance question given assumptions
		assertTrue(engine.doBalanceTrade(null, new Date(), user + " balances question " + questionId, 
				userNameToIDMap.get(user), questionId, assumptionIds, Collections.singletonList((Math.random()<.5)?0:1)));
		
		// 2 - Resolve assumed question
		assertTrue(engine.resolveQuestion(null, new Date(), assumptionIds.get(0), (Math.random()<.5)?0:1));
		
		// 3 - Perform any action which rebuilds the DEF network (e.g. {@link MarkovEngineImpl#addQuestion(Long, Date, long, int, List)})
		assertTrue(engine.addQuestion(null, new Date(), Long.MAX_VALUE, 2, null));
	}
	
	/**
	 * Verify what happens when we revert a trade after resolving it
	 */
	public final void testResolveAndRevert() {
		
		Map<String, Long> userNameToIDMap = new HashMap<String, Long>();
		this.createDEFNetIn1Transaction(userNameToIDMap );
		
		Set<Long> resolved = new HashSet<Long>();
		
		if (Math.random() < .5) {
			assertTrue(engine.resolveQuestion(null, new Date(), 0x0DL, (Math.random()<.5)?0:1));
			resolved.add(0x0DL);
		}
		if (Math.random() < .5) {
			assertTrue(engine.resolveQuestion(null, new Date(), 0x0EL, (Math.random()<.5)?0:1));
			resolved.add(0x0EL);
		}
		if (Math.random() < .5) {
			assertTrue(engine.resolveQuestion(null, new Date(), 0x0FL, (Math.random()<.5)?0:1));
			resolved.add(0x0FL);
		}
		
		assertTrue(engine.revertTrade(null, new Date(), new Date(0), (Math.random()<.5)?null:((Math.random()<.5)?0x0DL:((Math.random()<.5)?0x0EL:0X0F))));
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
