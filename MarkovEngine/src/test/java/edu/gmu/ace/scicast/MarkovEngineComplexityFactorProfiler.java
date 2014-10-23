/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.util.dseparation.impl.MSeparationUtility;

/**
 * @author Shou Matsumoto
 *
 */
public class MarkovEngineComplexityFactorProfiler extends TestCase {

	private static MarkovEngineImpl engine = null;
	
	private static long seed = System.currentTimeMillis();
	private Random rand = new Random(seed);
	
	/** How many threads to create in {@link #testGetComplexityFactorMultiThread()} and {@link #testGetComplexityFactorMultiThreadScicastNet()} */
	private int numThreads = 27;
	
	/** How many nodes to create in {@link #testGetComplexityFactorMultiThread()} */
	private int numNodesToCreate = 200;
	/** How many arcs to create in {@link #testGetComplexityFactorMultiThread()} before starting threads for profiling. */
	private int numArcsToCreate = 10;
	
	/** Max number of states of nodes to create in {@link #testGetComplexityFactorMultiThread()} */
	private int maxNumStates = 10;

	/** {@link #connectNodes()} will attempt to increase largest clique with this prob */
	private float probPickNodeInLargestClique = 1f/5f;

	

	/**
	 * @param name
	 */
	public MarkovEngineComplexityFactorProfiler(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance();
		engine.initialize();
	}

	/**
	 * @param isRandom : if true, it will use some randomness to connect nodes. If false, 
	 * it will connect a node in smallest clique with a node in 2nd largest clique
	 * (so that the increase in max clique size is small)
	 */
	private synchronized void connectNodes(boolean isRandom) {
		synchronized (engine) {
			
			// this will be used as upper bound (open/exclusive) when randomly picking nodes in net
			int numNodes = engine.getProbabilisticNetwork().getNodes().size();
			
			// these nodes will be connected
			Node nodeInLargerClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes)); ;
			Node nodeInSmallerClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes));
			
			if (isRandom) {
				if (rand.nextFloat() < probPickNodeInLargestClique ) {
					// find 2 nodes that belongs to 2 different large cliques
					int largestSize = 0;
					for (Clique clique : engine.getProbabilisticNetwork().getJunctionTree().getCliques()) {
						if (clique.getProbabilityFunction().tableSize() > largestSize) {
							nodeInSmallerClique = nodeInLargerClique;
							// pick any node in largest clique
							nodeInLargerClique = clique.getNodesList().get(rand.nextInt(clique.getNodesList().size()));
							largestSize = clique.getProbabilityFunction().tableSize();
						}
					}
				}
				
				// randomly discard nodeInSmallerClique, so that we will pick another node randomly
				if (rand.nextFloat() > probPickNodeInLargestClique) {
					nodeInSmallerClique = null;
				}
			} else {	// we shall not randomly pick nodes
				
				// pick a node from 2nd largest clique, but for that we'll need another variable to point to a node in the largest clique
				Node nodeInLargestCliqueSoFar = nodeInLargerClique;	// this will point to a node in largest clique found so far.
				int largestSize = 0;
				
				// ...and also pick one node in smallest clique
				int smallestSize = Integer.MAX_VALUE;
				
				for (Clique clique : engine.getProbabilisticNetwork().getJunctionTree().getCliques()) {
					if (clique.getProbabilityFunction().tableSize() > largestSize) {
						// the node in 2nd largest clique known at this point becomes the node in previous largest clique found so far
						nodeInLargerClique = nodeInLargestCliqueSoFar;
						// pick any node in largest clique
						nodeInLargestCliqueSoFar = clique.getNodesList().get(rand.nextInt(clique.getNodesList().size()));
						largestSize = clique.getProbabilityFunction().tableSize();
					}
					if (clique.getProbabilityFunction().tableSize() < smallestSize) {
						// pick any node in smallest clique
						nodeInSmallerClique = clique.getNodesList().get(rand.nextInt(clique.getNodesList().size()));
						smallestSize = clique.getProbabilityFunction().tableSize();
					}
				}
				
			}
			
			
			// make sure nodeIn2ndLargestClique is not null and not equal nor connected to the other node
			for (int i = 0; 
					nodeInSmallerClique == null 										// make sure we have a node
							|| nodeInLargerClique.equals(nodeInSmallerClique)			// make sure the node is not equal
							|| nodeInLargerClique.isChildOf(nodeInSmallerClique)		// make sure the nodes are not connected already
							|| nodeInLargerClique.isParentOf(nodeInSmallerClique); 		// make sure the nodes are not connected already
					i++) {
				
				// pick a node randomly
				nodeInSmallerClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes)); 
				
				// do not loop forever
				if (i >= numNodes*3) { 
					System.err.println("Unable to find a pair for node " + nodeInLargerClique);
					return;	
				}
			}
			
			// check direction of arcs by checking if there is already a directed path
			MSeparationUtility util = MSeparationUtility.newInstance();
			// add new edge between 2 nodes large cliques
			if (!util.getRoutes(nodeInLargerClique, nodeInSmallerClique).isEmpty()) {
				// there is a path from nodeInLargestClique to nodeIn2ndLargestClique, so we must add arc in same direction nodeInLargestClique->nodeIn2ndLargestClique
				engine.addQuestionAssumption(null, new Date(), Long.parseLong(nodeInSmallerClique.getName()), Collections.singletonList(Long.parseLong(nodeInLargerClique.getName())), null);
			} else {
				// either there is no path or there is a path in opposite direction.
				// in both cases, we can add arc nodeIn2ndLargestClique->nodeInLargestClique
				engine.addQuestionAssumption(null, new Date(), Long.parseLong(nodeInLargerClique.getName()), Collections.singletonList(Long.parseLong(nodeInSmallerClique.getName())), null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
//		this.connectNodes();
		while (true) {
			System.err.println("Finished execution. Please, halt.");
			Thread.sleep(10000);
		}
	}
	
	private void makeTradeLargestClique() {
		// find a node in the largest clique
		Node node = null;
		int largestSize = 0;
		for (Clique clique : engine.getProbabilisticNetwork().getJunctionTree().getCliques()) {
			if (clique.getProbabilityFunction().tableSize() > largestSize) {
				node = clique.getNodesList().get(0);
				largestSize = clique.getProbabilityFunction().tableSize();
			}
		}
		// randomly fill new value
		List<Float> newValues = new ArrayList<Float>(node.getStatesSize());
		float sum = 0f;
		// for a node with n states, fill n-1 states (the free parameters) with random prob
		for (int i = 0; i < node.getStatesSize()-1; i++) {
			float value = rand.nextFloat()*(1f-sum);	// multiply with 1-sum to guarantee that sum will never exceed 1
			newValues.add(value);
			sum += value;
		}
		newValues.add(1f-sum);	// last state is a non-free parameter (to guarantee sum to be 1)
		
		engine.addTrade(null, new Date(), "", 0, Long.parseLong(node.getName()), newValues , null, null, true);
	
	}
	
	/**
	 * A thread that runs {@link MarkovEngineComplexityFactorProfiler#makeTradeLargestClique()}
	 * @author Shou Matsumoto
	 * @see MarkovEngineComplexityFactorProfiler#testGetComplexityFactorMultiThread()
	 */
	class ComplexityFactorRunnable implements Runnable {
		private long id = Long.MIN_VALUE;
		public ComplexityFactorRunnable(long threadID) {
			super();
			this.id = threadID;
		}
		public long getId() { return id; }
		public void run() {
			synchronized (engine) {
				makeTradeLargestClique();
				System.out.println(seed + ", " + Thread.currentThread().getName() + " , " + getId() + " , " + engine.getComplexityFactors(null));
			}
		}
	}
	
	/**
	 * Create and runs a {@link ComplexityFactorRunnable} and then runs {@link #connectNodes()}
	 * after loading the network "/140925.net", which is the scicast network on September 25 of 2014.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testGetComplexityFactorMultiThreadScicastNet() throws InterruptedException, IOException {
		String netString = "";
		// read file
		InputStream stream = getClass().getResourceAsStream("/140925.net");
		int content;
		while ((content = stream.read()) != -1) {
			// convert to char and display it
			netString += (char) content;
		}
		
		// import the network
		engine.importState(netString);
		
		System.setOut(new PrintStream(new File(System.currentTimeMillis() + ".log")));
		
		for (int i = 1; i <= numThreads; i++) {
			Thread t = new Thread(new ComplexityFactorRunnable(i));
//			t.setDaemon(true);
			t.start();
			Thread.sleep(2000);
			this.connectNodes(true);
		}
	}
	

	/**
	 * Create and runs a {@link ComplexityFactorRunnable} and then runs {@link #connectNodes()}
	 * with argument == true
	 * after creating a network with {@link #numNodesToCreate} nodes with 2 to {@link #maxNumStates} states 
	 * and {@link #numArcsToCreate} arcs.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testGetComplexityFactorMultiThread() throws InterruptedException, IOException {
		
		long transactionKey = engine.startNetworkActions();
		for (int i = 0; i < numNodesToCreate; i++) {
			// create nodes from 2 to maxNumStates states
			engine.addQuestion(transactionKey, new Date(), i, 2+rand.nextInt(maxNumStates-1), null);	
		}
		engine.commitNetworkActions(transactionKey);
		
		// create a few arcs before starting the profiling
		for (int i = 0; i < numArcsToCreate; i++) {
			this.connectNodes(true);
		}
		
		System.setOut(new PrintStream(new File(System.currentTimeMillis() + ".log")));
		for (int i = 1; i <= numThreads; i++) {
			Thread t = new Thread(new ComplexityFactorRunnable(i));
//			t.setDaemon(true);
			t.start();
			Thread.sleep(2000);
			this.connectNodes(true);
		}
	}
	
	/**
	 * Create and runs a {@link ComplexityFactorRunnable} and then {@link #connectNodes(boolean)}
	 * with argument == false.
	 * after creating a network with {@link #numNodesToCreate} nodes with 2 to {@link #maxNumStates} states.
	 * However, this will attempt to control the arcs to be created, in order to keep the maximum clique size
	 * increasing near linearly.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public final void testGetComplexityFactorMultiThreadControlled() throws InterruptedException, IOException {
		
		long transactionKey = engine.startNetworkActions();
		for (int i = 0; i < numNodesToCreate; i++) {
			// create nodes from 2 to maxNumStates states
			engine.addQuestion(transactionKey, new Date(), i, 2+rand.nextInt(maxNumStates-1), null);	
		}
		engine.commitNetworkActions(transactionKey);
		
		// create a few arcs before starting the profiling
		for (int i = 0; i < numArcsToCreate; i++) {
			this.connectNodes(false);
		}
		
		System.setOut(new PrintStream(new File(System.currentTimeMillis() + ".log")));
		for (int i = 1; i <= numThreads; i++) {
			Thread t = new Thread(new ComplexityFactorRunnable(i));
			t.start();
			while (t.isAlive()) {
				Thread.sleep(500);
			}
			this.connectNodes((rand.nextFloat()<probPickNodeInLargestClique)?true:false);
		}
	}

	
	
	
//	public final void testGetComplexityFactor1() {
//		System.out.println(seed + ", " + new Object(){}.getClass().getEnclosingMethod().getName() + " , " + engine.getComplexityFactors(null));
//		this.makeTradeLargestClique();
//	}
//	
//	public final void testGetComplexityFactor2() {
//		System.out.println(seed + ", " + new Object(){}.getClass().getEnclosingMethod().getName() + " , " + engine.getComplexityFactors(null));
//		this.makeTradeLargestClique();
//	}
//	public final void testGetComplexityFactor3() {
//		System.out.println(seed + ", " + new Object(){}.getClass().getEnclosingMethod().getName() + " , " + engine.getComplexityFactors(null));
//		this.makeTradeLargestClique();
//	}
//	public final void testGetComplexityFactor4() {
//		System.out.println(seed + ", " + new Object(){}.getClass().getEnclosingMethod().getName() + " , " + engine.getComplexityFactors(null));
//		this.makeTradeLargestClique();
//	}

}
