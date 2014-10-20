/**
 * 
 */
package edu.gmu.ace.scicast;

import java.io.File;
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

	private static PrintStream printStream = null;
	
	private int numThreads = 30;

	/** {@link #connectNodes()} will attempt to increase largest clique with this prob */
	private float probPickNodeInLargestClique = 1f/2f;
	

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
		if (engine == null) {
			engine = (MarkovEngineImpl) MarkovEngineImpl.getInstance();
			engine.initialize();
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
			
			printStream = new PrintStream(new File(System.currentTimeMillis() + ".log"));
			System.setOut(printStream);
		} else {
//			this.connectNodesInLargeCliques();
		}
	}

	private synchronized void connectNodes() {
		synchronized (engine) {
			
			// this will be used as upper bound (open/exclusive) when randomly picking nodes in net
			int numNodes = engine.getProbabilisticNetwork().getNodes().size();
			
			// these nodes will be connected
			Node nodeInLargestClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes)); ;
			Node nodeIn2ndLargestClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes));
			
			if (rand.nextFloat() < probPickNodeInLargestClique ) {
				// find 2 nodes that belongs to 2 different large cliques
				int largestSize = 0;
				for (Clique clique : engine.getProbabilisticNetwork().getJunctionTree().getCliques()) {
					if (clique.getProbabilityFunction().tableSize() > largestSize) {
						nodeIn2ndLargestClique = nodeInLargestClique;
						// pick any node in largest clique
						nodeInLargestClique = clique.getNodesList().get(rand.nextInt(clique.getNodesList().size()));
						largestSize = clique.getProbabilityFunction().tableSize();
					}
				}
			}
			
			// randomly discard nodeIn2ndLargestClique, so that we will pick another node randomly
			if (rand.nextFloat() > probPickNodeInLargestClique) {
				nodeIn2ndLargestClique = null;
			}
			
			// make sure nodeIn2ndLargestClique is not null and not equal nor connected to the other node
			for (int i = 0; 
					nodeIn2ndLargestClique == null 										// make sure we have a node
							|| nodeInLargestClique.equals(nodeIn2ndLargestClique)			// make sure the node is not equal
							|| nodeInLargestClique.isChildOf(nodeIn2ndLargestClique)		// make sure the nodes are not connected already
							|| nodeInLargestClique.isParentOf(nodeIn2ndLargestClique); 		// make sure the nodes are not connected already
					i++) {
				
				// pick a node randomly
				nodeIn2ndLargestClique = engine.getProbabilisticNetwork().getNodes().get(rand.nextInt(numNodes)); 
				
				// do not loop forever
				if (i >= numNodes*3) { 
					System.err.println("Unable to find a pair for node " + nodeInLargestClique);
					return;	
				}
			}
			
			// check direction of arcs by checking if there is already a directed path
			MSeparationUtility util = MSeparationUtility.newInstance();
			// add new edge between 2 nodes large cliques
			if (!util.getRoutes(nodeInLargestClique, nodeIn2ndLargestClique).isEmpty()) {
				// there is a path from nodeInLargestClique to nodeIn2ndLargestClique, so we must add arc in same direction nodeInLargestClique->nodeIn2ndLargestClique
				engine.addQuestionAssumption(null, new Date(), Long.parseLong(nodeIn2ndLargestClique.getName()), Collections.singletonList(Long.parseLong(nodeInLargestClique.getName())), null);
			} else {
				// either there is no path or there is a path in opposite direction.
				// in both cases, we can add arc nodeIn2ndLargestClique->nodeInLargestClique
				engine.addQuestionAssumption(null, new Date(), Long.parseLong(nodeInLargestClique.getName()), Collections.singletonList(Long.parseLong(nodeIn2ndLargestClique.getName())), null);
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
	 * Create and runs a {@link ComplexityFactorRunnable} and then runs {@link #connectNodes()}
	 * @throws InterruptedException 
	 */
	public final void testGetComplexityFactorMultiThread() throws InterruptedException {
		for (int i = 1; i <= numThreads; i++) {
			Thread t = new Thread(new ComplexityFactorRunnable(i));
//			t.setDaemon(true);
			t.start();
			Thread.sleep(1000*i);
			this.connectNodes();
		}
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
