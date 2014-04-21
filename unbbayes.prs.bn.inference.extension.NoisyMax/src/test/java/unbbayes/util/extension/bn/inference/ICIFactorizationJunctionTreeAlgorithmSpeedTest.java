package unbbayes.util.extension.bn.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.cpt.impl.UniformTableFunction;
import unbbayes.prs.exception.InvalidParentException;

public class ICIFactorizationJunctionTreeAlgorithmSpeedTest extends TestCase {

	public ICIFactorizationJunctionTreeAlgorithmSpeedTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Tests and compares the junction tree build time and evidence propagation time
	 * of {@link JunctionTreeAlgorithm} and {@link ICIFactorizationJunctionTreeAlgorithm}
	 * for a 2-level network like the following:
	 * <pre>
	 * P1 P2 P3 ... Pk
	 *  |  |  | ... |
	 *  V  V  V     V
	 *  ( Noisy_Max )
	 * </pre>
	 * Nodes P1,...,Pk are common parent of Noisy_Max, which has noisy-max distribution.
	 * <br/>
	 * The junction tree compilation time and evidence propagation time will be calculated for
	 * different values of k
	 */
	public final void testPropagateTime() {
		int maxK = 20;	// the max number of K
		int stepSize = 1;	// how much to increment k
		int numParentStates = 3;	// how many states P1,...,Pk will have
		int numChildStates = 4;		// how many states N1,...,Nk will have
		long seed = System.currentTimeMillis();
		Random random = new Random(seed);
		
		System.out.println("***************************************");
		System.out.println("Seed = " + seed);
		System.out.println("***************************************");
		
		// just create the net. Use 2 nets, so that one won't interfere the other
		ProbabilisticNetwork netJT = new ProbabilisticNetwork("JT");
		ProbabilisticNetwork netICI = new ProbabilisticNetwork("ICI");
		List<ProbabilisticNode> parentsJT = new ArrayList<ProbabilisticNode>(maxK);	// list of P1,...,Pk
		List<ProbabilisticNode> parentsICI = new ArrayList<ProbabilisticNode>(maxK);	// list of P1,...,Pk
		
		
		// the printing format will be:
		// <K> , <time to compile JT> , <time to compile ICI> , <mean time to propagate evidence JT> , <mean time to propagate evidence ICI>
		
		// create common children
		ProbabilisticNode childJT = new ProbabilisticNode();
		ProbabilisticNode childICI = new ProbabilisticNode();
		childJT.setName("NoisyMax");
		childICI.setName("NoisyMax");
		childJT.setDescription(childJT.getName());
		childICI.setDescription(childICI.getName());
		for (int j = 0; j < numChildStates; j++) {
			childJT.appendState(""+j);
			childICI.appendState(""+j);
		}
		childJT.getProbabilityFunction().addVariable(childJT);
		childICI.getProbabilityFunction().addVariable(childICI);
		netJT.addNode(childJT);
		netICI.addNode(childICI);
		
		// start iteration
		for (int k = stepSize; k <= maxK; k += stepSize) {
			
			// create parents P1,...,Pk for this iteration
			for (int i = 0; i < stepSize; i++) {
				ProbabilisticNode nodeJT = new ProbabilisticNode();
				ProbabilisticNode nodeICI = new ProbabilisticNode();
				nodeJT.setName("P"+(k-stepSize+i));
				nodeICI.setName("P"+(k-stepSize+i));
				nodeJT.setDescription(nodeJT.getName());
				nodeICI.setDescription(nodeICI.getName());
				for (int j = 0; j < numParentStates; j++) {
					nodeJT.appendState(""+j);
					nodeICI.appendState(""+j);
				}
				nodeJT.getProbabilityFunction().addVariable(nodeJT);
				nodeICI.getProbabilityFunction().addVariable(nodeICI);
				netJT.addNode(nodeJT);
				netICI.addNode(nodeICI);
				
				// connect to child
				try {
					netJT.addEdge(new Edge(nodeJT, childJT));
					netICI.addEdge(new Edge(nodeICI, childICI));
				} catch (InvalidParentException e) {
					throw new RuntimeException(e);
				}
				
				parentsJT.add(nodeJT);
				parentsICI.add(nodeICI);
			}
			
			
			// fill CPT of all nodes with uniform distribution
			UniformTableFunction uniformFunction = new UniformTableFunction();
			NoisyMaxCPTConverter noisyMax = new NoisyMaxCPTConverter();
			for (Node node : netJT.getNodes()) {
				uniformFunction.applyFunction((ProbabilisticTable) ((ProbabilisticNode)node).getProbabilityFunction());
				// convert CPT of children to uniform noisy-max distribution
				if (!node.getParentNodes().isEmpty()) {
					noisyMax.forceCPTToIndependenceCausalInfluence(((ProbabilisticNode)node).getProbabilityFunction());
				}
			}
			for (Node node : netICI.getNodes()) {
				uniformFunction.applyFunction((ProbabilisticTable) ((ProbabilisticNode)node).getProbabilityFunction());
				// convert CPT of children to uniform noisy-max distribution
				if (!node.getParentNodes().isEmpty()) {
					noisyMax.forceCPTToIndependenceCausalInfluence(((ProbabilisticNode)node).getProbabilityFunction());
				}
			}
			
			
			// prepare the algorithms
			JunctionTreeAlgorithm jt = new JunctionTreeAlgorithm();
			jt.setNet(netJT);
			ICIFactorizationJunctionTreeAlgorithm ici = new ICIFactorizationJunctionTreeAlgorithm();
			ici.setNet(netICI);
			
			System.out.print(k + " , ");
			
			// check the time we need to compile JT
			long prevTimestamp = System.currentTimeMillis();
			jt.run();
			System.out.print((System.currentTimeMillis() - prevTimestamp) + " , ");
			// make sure the junction tree was created
			assertNotNull(netJT.getJunctionTree());
			assertFalse(netJT.getJunctionTree().getCliques().isEmpty());
			// get the size of largest clique
			int sizeLargestCliqueJT = 0;
			for (Clique clique : netJT.getJunctionTree().getCliques()) {
				if (clique.getProbabilityFunction().tableSize() > sizeLargestCliqueJT) {
					sizeLargestCliqueJT = clique.getProbabilityFunction().tableSize();
				}
			}
			
			// check the time we need to compile JT with ICI
			prevTimestamp = System.currentTimeMillis();
			ici.run();
			System.out.print((System.currentTimeMillis() - prevTimestamp) + " , ");
			// make sure the junction tree was created
			assertNotNull(netICI.getJunctionTree());
			assertFalse(netICI.getJunctionTree().getCliques().isEmpty());
			// get the size of largest clique
			int sizeLargestCliqueICI = 0;
			for (Clique clique : netICI.getJunctionTree().getCliques()) {
				if (clique.getProbabilityFunction().tableSize() > sizeLargestCliqueICI) {
					sizeLargestCliqueICI = clique.getProbabilityFunction().tableSize();
				}
			}
			// make sure the ICI did the optimization
			if (k > 2) {
				assertTrue(sizeLargestCliqueICI < sizeLargestCliqueJT);
			}
			
			// set evidence to random parent, and get propagation time of jt
			ProbabilisticNode findingNode = parentsJT.get(random.nextInt(parentsJT.size()));
			prevTimestamp = System.currentTimeMillis();
			findingNode.addFinding(random.nextInt(findingNode.getStatesSize()));
			jt.propagate();
			System.out.print(((System.currentTimeMillis() - prevTimestamp)) + " , ");
			jt.reset();		// reset evidences for next iteration
			
			// do the same for ICI
			findingNode = parentsICI.get(random.nextInt(parentsICI.size()));
			prevTimestamp = System.currentTimeMillis();
			findingNode.addFinding(random.nextInt(findingNode.getStatesSize()));
			ici.propagate();
			System.out.print(((System.currentTimeMillis() - prevTimestamp)) + " , ");
			ici.reset();	// reset evidences for next iteration
			ici.defactorize(); // delete all temporary nodes, for next iteration
			
			System.out.println();
		}
		
	}
	
	/**
	 * Tests and compares the junction tree build time and evidence propagation time
	 * of {@link JunctionTreeAlgorithm} and {@link ICIFactorizationJunctionTreeAlgorithm}
	 * for a 2-level network like the following:
	 * <pre>
	 * P1   P2   P3   ...  Pk
	 * |\___|___/|__/_..._/|
	 * V    V    V         V
	 * N1   N2   N3   ...  Nk
	 * </pre>
	 * ...That is , all nodes P1,...,Pk are not connected each other, but they are fully connected
	 * with nodes N1,...,N3. In other words, P1,...,Pk are parents of N1,...,Nk.
	 * <br/>
	 * P1,...,Pk starts with uniform distribution, and N1,...,Nk are nodes with noisy-max distribution.
	 * <br/>
	 * The junction tree compilation time and evidence propagation time will be calculated for
	 * different values of k
	 */
	public final void testPropagateTimeLoopy() {
		int maxK = 13;	// the max number of K
		int stepSize = 1;	// how much to increment k
		int numParentStates = 3;	// how many states P1,...,Pk will have
		int numChildStates = 4;		// how many states N1,...,Nk will have
		long seed = System.currentTimeMillis();
		Random random = new Random(seed);
		
		System.out.println("***************************************");
		System.out.println("Seed = " + seed);
		System.out.println("***************************************");
		
		// just create the net. Use 2 nets, so that one won't interfere the other
		ProbabilisticNetwork netJT = new ProbabilisticNetwork("JT");
		ProbabilisticNetwork netICI = new ProbabilisticNetwork("ICI");
		List<ProbabilisticNode> parentsJT = new ArrayList<ProbabilisticNode>(maxK);	// list of P1,...,Pk
		List<ProbabilisticNode> parentsICI = new ArrayList<ProbabilisticNode>(maxK);	// list of P1,...,Pk
		
		
		// the printing format will be:
		// <K> , <time to compile JT> , <time to compile ICI> , <mean time to propagate evidence JT> , <mean time to propagate evidence ICI>
		
		// start iteration
		for (int k = stepSize; k <= maxK; k += stepSize) {
			
			// create parents P1,...,Pk for this iteration
			for (int i = 0; i < stepSize; i++) {
				ProbabilisticNode nodeJT = new ProbabilisticNode();
				ProbabilisticNode nodeICI = new ProbabilisticNode();
				nodeJT.setName("P"+(k-stepSize+i));
				nodeICI.setName("P"+(k-stepSize+i));
				nodeJT.setDescription(nodeJT.getName());
				nodeICI.setDescription(nodeICI.getName());
				for (int j = 0; j < numParentStates; j++) {
					nodeJT.appendState(""+j);
					nodeICI.appendState(""+j);
				}
				nodeJT.getProbabilityFunction().addVariable(nodeJT);
				nodeICI.getProbabilityFunction().addVariable(nodeICI);
				netJT.addNode(nodeJT);
				netICI.addNode(nodeICI);
				
				// connect to all existing children
				for (Node node : netJT.getNodes()) {
					if (!node.getParentNodes().isEmpty()) {
						try {
							netJT.addEdge(new Edge(nodeJT, node));
						} catch (InvalidParentException e) {
							throw new RuntimeException(e);
						}
					}
				}
				for (Node node : netICI.getNodes()) {
					if (!node.getParentNodes().isEmpty()) {
						try {
							netICI.addEdge(new Edge(nodeICI, node));
						} catch (InvalidParentException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
				parentsJT.add(nodeJT);
				parentsICI.add(nodeICI);
			}
			
			// create children N1,...,Nk
			for (int i = 0; i < stepSize; i++) {
				ProbabilisticNode nodeJT = new ProbabilisticNode();
				ProbabilisticNode nodeICI = new ProbabilisticNode();
				nodeJT.setName("N"+(k-stepSize+i));
				nodeICI.setName("N"+(k-stepSize+i));
				nodeJT.setDescription(nodeJT.getName());
				nodeICI.setDescription(nodeICI.getName());
				for (int j = 0; j < numChildStates; j++) {
					nodeJT.appendState(""+j);
					nodeICI.appendState(""+j);
				}
				nodeJT.getProbabilityFunction().addVariable(nodeJT);
				nodeICI.getProbabilityFunction().addVariable(nodeICI);
				netJT.addNode(nodeJT);
				netICI.addNode(nodeICI);
				
				// connect to all parents
				for (ProbabilisticNode parent : parentsJT) {
					try {
						netJT.addEdge(new Edge(parent, nodeJT));
					} catch (InvalidParentException e) {
						throw new RuntimeException(e);
					}
				}
				for (ProbabilisticNode parent : parentsICI) {
					try {
						netICI.addEdge(new Edge(parent, nodeICI));
					} catch (InvalidParentException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			// fill CPT of all nodes with uniform distribution
			UniformTableFunction uniformFunction = new UniformTableFunction();
			NoisyMaxCPTConverter noisyMax = new NoisyMaxCPTConverter();
			for (Node node : netJT.getNodes()) {
				uniformFunction.applyFunction((ProbabilisticTable) ((ProbabilisticNode)node).getProbabilityFunction());
				// convert CPT of children to uniform noisy-max distribution
				if (!node.getParentNodes().isEmpty()) {
					noisyMax.forceCPTToIndependenceCausalInfluence(((ProbabilisticNode)node).getProbabilityFunction());
				}
			}
			for (Node node : netICI.getNodes()) {
				uniformFunction.applyFunction((ProbabilisticTable) ((ProbabilisticNode)node).getProbabilityFunction());
				// convert CPT of children to uniform noisy-max distribution
				if (!node.getParentNodes().isEmpty()) {
					noisyMax.forceCPTToIndependenceCausalInfluence(((ProbabilisticNode)node).getProbabilityFunction());
				}
			}
			
			
			
			// prepare the algorithms
			JunctionTreeAlgorithm jt = new JunctionTreeAlgorithm();
			jt.setNet(netJT);
			ICIFactorizationJunctionTreeAlgorithm ici = new ICIFactorizationJunctionTreeAlgorithm();
			ici.setNet(netICI);
			
			System.out.print(k + " , ");
			 
			// check the time we need to compile JT
			long prevTimestamp = System.currentTimeMillis();
			jt.run();
			System.out.print((System.currentTimeMillis() - prevTimestamp) + " , ");
			// make sure the junction tree was created
			assertNotNull(netJT.getJunctionTree());
			assertFalse(netJT.getJunctionTree().getCliques().isEmpty());
			
			// check the time we need to compile JT with ICI
			prevTimestamp = System.currentTimeMillis();
			ici.run();
			System.out.print((System.currentTimeMillis() - prevTimestamp) + " , ");
			// make sure the junction tree was created
			assertNotNull(netICI.getJunctionTree());
			assertFalse(netICI.getJunctionTree().getCliques().isEmpty());
			
			// set evidence to random parent, and get propagation time of jt
			ProbabilisticNode findingNode = parentsJT.get(random.nextInt(parentsJT.size()));
			prevTimestamp = System.currentTimeMillis();
			findingNode.addFinding(random.nextInt(findingNode.getStatesSize()));
			jt.propagate();
			System.out.print(((System.currentTimeMillis() - prevTimestamp)) + " , ");
			jt.reset();		// reset evidences for next iteration
			
			// do the same for ICI
			findingNode = parentsICI.get(random.nextInt(parentsICI.size()));
			prevTimestamp = System.currentTimeMillis();
			findingNode.addFinding(random.nextInt(findingNode.getStatesSize()));
			ici.propagate();
			System.out.print(((System.currentTimeMillis() - prevTimestamp)) + " , ");
			ici.reset();	// reset evidences for next iteration
			ici.defactorize(); // delete all temporary nodes, for next iteration
			
			System.out.println();
		}
		
	}

}
