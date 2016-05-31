/**
 * 
 */
package unbbayes.prs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.io.DneIO;
import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;

/**
 * @author Shou Matsumoto
 *
 */
public class JointProbabilityTest extends TestCase {

	private static final float PROB_ERROR_MARGIN = 0.00005f;
	

	/**
	 * @param name
	 */
	public JointProbabilityTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public final void testMarginalProbability() {
	
		// open a .net file
	   ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new NetIO().load(new File(getClass().getResource("/toyNetwork.net").toURI()));
		} catch (LoadException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	   // prepare the algorithm to compile network
	   JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
	   alg.setNetwork(net);
	   alg.run();
	   
	   // store marginals before joint prob
	   Map<ProbabilisticNode,List<Float>> marginals = new HashMap<ProbabilisticNode, List<Float>>();
	   for (Node node : net.getNodes()) {
		   List<Float> marginal = new ArrayList<Float>(node.getStatesSize());
		   for (int i = 0; i < node.getStatesSize(); i++) {
			   marginal.add(((ProbabilisticNode)node).getMarginalAt(i));
		   }
		   marginals.put((ProbabilisticNode) node, marginal);
	   }
	   
	   
	   //specifying the nodes and states for inquiring about the joint distribution
	   ProbabilisticNode node13 = (ProbabilisticNode)net.getNode("Node13");
	   int node13State = 0;
	   ProbabilisticNode node15 = (ProbabilisticNode)net.getNode("Node15");
	   int node15State = 1;
	   Map<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
	   map.put(node13, node13State);
	   map.put(node15, node15State);		   

	   //calling the joint probability method
	   float jointProb1315 = alg.getJointProbability(map);
	   
	   // also, calculate joint probability of 11,13,15, to see if it matches joint 13,15 after evidence on 11
	   Map<ProbabilisticNode, Integer> anotherMap = new HashMap<ProbabilisticNode, Integer>(map);
	   anotherMap.put((ProbabilisticNode) net.getNode("Node11"), 0);
	   float jointToCompare = alg.getJointProbability(anotherMap);
	   
	   // check that marginals did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			}
	   }
	   
	   // insert evidence (finding) for node 11 (connected to nodes 13 and 15)
	   ProbabilisticNode node11 = (ProbabilisticNode)net.getNode("Node11");
	   System.out.println("Setting evidence for node " + node11.getDescription());
	   System.out.println();
	   node11.addFinding(0); // the 1st state is now 100%
	   
	   // propagate evidence
	    try {
		   net.updateEvidences();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	   
	    // check that marginals of disconnected nodes did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
		   if (!isConnectedOrEqual(entry.getKey(), node11,null)) {
			   for (int i = 0; i < entry.getValue().size(); i++) {
				   assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			   }
		   }
	   }
	    
	   // store marginals before joint prob
	   marginals = new HashMap<ProbabilisticNode, List<Float>>();
	   for (Node node : net.getNodes()) {
		   List<Float> marginal = new ArrayList<Float>(node.getStatesSize());
		   for (int i = 0; i < node.getStatesSize(); i++) {
			   marginal.add(((ProbabilisticNode)node).getMarginalAt(i));
		   }
		   marginals.put((ProbabilisticNode) node, marginal);
	   }
	   
	   //calling the joint probaility method
	   float jointProb = alg.getJointProbability(map);
	   assertEquals(jointToCompare, jointProb, PROB_ERROR_MARGIN);
	   
	   // check that marginals did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			}
	   }
	   
	   //resetting evidence 
	   net.resetEvidences();
	   

	   // store marginals before joint prob
	   marginals = new HashMap<ProbabilisticNode, List<Float>>();
	   for (Node node : net.getNodes()) {
		   List<Float> marginal = new ArrayList<Float>(node.getStatesSize());
		   for (int i = 0; i < node.getStatesSize(); i++) {
			   marginal.add(((ProbabilisticNode)node).getMarginalAt(i));
		   }
		   marginals.put((ProbabilisticNode) node, marginal);
	   }
	   
	   
	   //calling the joint probability method
	   System.out.println("Joint probability for state " + node13State + " of node " 
	   + node13.getDescription() + " and state " + node15State + " of node " + node15.getDescription());
	   System.out.println("No evidence specified");
	   jointProb = alg.getJointProbability(map);
	   System.out.println(jointProb);		
	   System.out.println();	
	   

	   // check that marginals did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			}
	   }
	   
	   
	   // insert evidence (finding) for node 5 (not connected to nodes 13 and 15)
	   ProbabilisticNode node5 = (ProbabilisticNode)net.getNode("Node5");
	   
	   anotherMap = new HashMap<ProbabilisticNode, Integer>(map);
	   anotherMap.put(node5, 0);
	   float joint51315 = alg.getJointProbability(anotherMap);
	   
	   System.out.println("Setting evidence for node " + node5.getDescription());
	   System.out.println();
	   node5.addFinding(0); // the 1st state is now 100%
	   
	   
	   // propagate evidence
		try {
			net.updateEvidences();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		

	    // check that marginals of disconnected nodes did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
		   if (!isConnectedOrEqual(entry.getKey(), node5, null)) {
			   for (int i = 0; i < entry.getValue().size(); i++) {
				   assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			   }
		   }
	   }
	   
	   Map<ProbabilisticNode, List<Float>>  marginalsBeforeEvidence = marginals;
	   // store marginals before change
	   marginals = new HashMap<ProbabilisticNode, List<Float>>();
	   for (Node node : net.getNodes()) {
		   List<Float> marginal = new ArrayList<Float>(node.getStatesSize());
		   for (int i = 0; i < node.getStatesSize(); i++) {
			   marginal.add(((ProbabilisticNode)node).getMarginalAt(i));
		   }
		   marginals.put((ProbabilisticNode) node, marginal);
	   }
	   
	   // absorb node5
	   net.removeNode(node5, true);
	   
	   // check that marginals did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
		   if (entry.getKey().equals(node5)) {
			   continue;	// ignore absorbed node
		   }
			for (int i = 0; i < entry.getValue().size(); i++) {
				assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			}
	   }
	   
	   
	   //calling the joint probaility method
	   jointProb = alg.getJointProbability(map);
	   if (net.getNode("Node5") != null) {
		   assertEquals(joint51315, jointProb,PROB_ERROR_MARGIN);		   
	   } else {
		   assertEquals(jointProb1315, jointProb,PROB_ERROR_MARGIN);		   
	   }
		   

	   // check that marginals did not change
	   for (Entry<ProbabilisticNode, List<Float>> entry : marginals.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				assertEquals(entry.getValue().get(i), entry.getKey().getMarginalAt(i), PROB_ERROR_MARGIN);
			}
	   }
	   
	}
	
	public final void testJointProbability () {
		// open a .net file
        ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new NetIO().load(new File(getClass().getResource("/toyNetwork.net").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

       // prepare the algorithm to compile network
       JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
       alg.setNetwork(net);
       alg.run();

       //specifying the nodes and states for inquiring about the joint distribution
       ProbabilisticNode node13 = (ProbabilisticNode)net.getNode("Node13");
       int node13State = 0;
       ProbabilisticNode node15 = (ProbabilisticNode)net.getNode("Node15");
       int node15State = 1;
       HashMap<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
       map.put(node13, node13State);
       map.put(node15, node15State);

       //calling the joint probability method
       System.out.println("No evidence specified");
       System.out.println("Joint probability for state " + node13State + " of node " + node13.getDescription() + " and state " + node15State + 
    		   	" of node " + node15.getDescription());
       float jointProb = alg.getJointProbability(map);
       System.out.println(jointProb);
       System.out.println("Probability for state " + node13State + " of node " + node13.getDescription());
       System.out.println(node13.getMarginalAt(node13State));
       System.out.println("Probability for state " + node15State + " of node " + node15.getDescription());
       System.out.println(node13.getMarginalAt(node15State));
       System.out.println();

       // insert evidence (finding) for node 10 (connected to nodes 13 and 15)
       ProbabilisticNode nodeTest= (ProbabilisticNode)net.getNode("Node10");
       System.out.println("Setting evidence for node " + nodeTest.getDescription());
       System.out.println();
       nodeTest.addFinding(1); // the 1st state is now 100%

       // propagate evidence
       try {
		net.updateEvidences();
	} catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	}

       //calling the joint probaility method
       jointProb = alg.getJointProbability(map);
       System.out.println("After specifying evidence for node11");
       System.out.println("Joint probability for state " + node13State 
			+ " of node " + node13.getDescription() + " and state " + node15State + 
			" of node " + node15.getDescription());
       System.out.println(jointProb);
       System.out.println("Probability for state " + node13State + " of node " + node13.getDescription());
       System.out.println(node13.getMarginalAt(node13State));
       System.out.println("Probability for state " + node15State + " of node " + node15.getDescription());
       System.out.println(node13.getMarginalAt(node15State));
       System.out.println();

       //resetting evidence
       net.resetEvidences();
       alg.run();

       //calling the joint probability method
       System.out.println("Evidence resetted");
       System.out.println("Joint probability for state " + node13State 
			+ " of node " + node13.getDescription() + " and state " + node15State + 
			" of node " + node15.getDescription());
       jointProb = alg.getJointProbability(map);
       System.out.println(jointProb);
       System.out.println("Probability for state " + node13State + " of node " + node13.getDescription());
       System.out.println(node13.getMarginalAt(node13State));
       System.out.println("Probability for state " + node15State + " of node " + node15.getDescription());
       System.out.println(node13.getMarginalAt(node15State));
       System.out.println();

       // insert evidence (finding) for node 5 (not connected to nodes 13 and 15)
       ProbabilisticNode node5 = (ProbabilisticNode)net.getNode("Node5");
       System.out.println("Setting evidence for node " + node5.getDescription());
       System.out.println();
       node5.addFinding(0); // the 1st state is now 100%

       // propagate evidence
       try {
    	   net.updateEvidences();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

       //calling the joint probaility method
       System.out.println("After specifying evidence for node5. Node5 is not connected to node 11 and 13");
       jointProb = alg.getJointProbability(map);
       System.out.println("Joint probability for state " + node13State 
			+ " of node " + node13.getDescription() + " and state " + node15State + 
			" of node " + node15.getDescription());
       System.out.println(jointProb);
       System.out.println("Probability for state " + node13State + " of node " + node13.getDescription());
       System.out.println(node13.getMarginalAt(node13State));
       System.out.println("Probability for state " + node15State + " of node " + node15.getDescription());
       System.out.println(node13.getMarginalAt(node15State));
       System.out.println();
	}
	
	
	public final void testJointProbability2 () {
		// open a .net file
		ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new DneIO().load(new File(getClass().getResource("/Challenge_1.dne").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// prepare the algorithm to compile network
		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
		alg.setNetwork(net);
		alg.run();
		
		
		for (int node1Index = 0; node1Index < net.getNodeCount()-1; node1Index++) {
			for (int node2Index = node1Index+1; node2Index < net.getNodeCount(); node2Index++) {
				
				//specifying the nodes and states for inquiring about the joint distribution
				ProbabilisticNode node1 = (ProbabilisticNode)net.getNodeAt(node1Index);
				ProbabilisticNode node2 = (ProbabilisticNode)net.getNodeAt(node2Index);
				
				for (int i = 0; i <node1.getStatesSize(); i++) {
					for (int j = 0; j < node2.getStatesSize(); j++) {
						HashMap<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
						map.put(node1, i);
						map.put(node2, j);
						
						//calling the joint probability method
						float jointProb = alg.getJointProbability(map);
						System.out.println("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
								+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb);
						assertTrue("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
									+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb, 
								(jointProb > 0 && jointProb < 1));
					}
				}
			}
		}
		
		
		
	}
	

	public final void testJointProbability3 () {
		// open a .net file
		ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new DneIO().load(new File(getClass().getResource("/Challenge_3.dne").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// prepare the algorithm to compile network
		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
		alg.setNetwork(net);
		alg.run();
		
		
		for (int node1Index = 0; node1Index < net.getNodeCount()-1; node1Index++) {
			for (int node2Index = node1Index+1; node2Index < net.getNodeCount(); node2Index++) {
				
				//specifying the nodes and states for inquiring about the joint distribution
				ProbabilisticNode node1 = (ProbabilisticNode)net.getNodeAt(node1Index);
				ProbabilisticNode node2 = (ProbabilisticNode)net.getNodeAt(node2Index);
				
				for (int i = 0; i <node1.getStatesSize(); i++) {
					for (int j = 0; j < node2.getStatesSize(); j++) {
						HashMap<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
						map.put(node1, i);
						map.put(node2, j);
						
						//calling the joint probability method
						float jointProb = alg.getJointProbability(map);
						System.out.println("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
								+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb);
						assertTrue("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
								+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb, 
								(jointProb > 0 && jointProb < 1));
					}
				}
			}
		}
		
		
		
	}

	
	public final void testJointProbability4 () {
		// open a .net file
		ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new DneIO().load(new File(getClass().getResource("/Challenge_2v5.dne").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// prepare the algorithm to compile network
		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
		alg.setNetwork(net);
		alg.run();
		
		
		for (int node1Index = 0; node1Index < net.getNodeCount()-1; node1Index++) {
			for (int node2Index = node1Index+1; node2Index < net.getNodeCount(); node2Index++) {
				
				//specifying the nodes and states for inquiring about the joint distribution
				ProbabilisticNode node2 = (ProbabilisticNode)net.getNodeAt(node2Index);
				ProbabilisticNode node1 = (ProbabilisticNode)net.getNodeAt(node1Index);
				
				assertEquals(2, node1.getStatesSize());
				assertEquals(2, node2.getStatesSize());
				
				System.out.println();
				System.out.println(" \t \t" + node2.getName() + "\t ");
				System.out.println(" \t \t" + node2.getStateAt(0) + "\t" + node2.getStateAt(1));
				float sum = 0f;
				for (int i = 0; i <node1.getStatesSize(); i++) {
					System.out.print(((i==0)?node1.getName():" ") + "\t" + node1.getStateAt(i));
					for (int j = 0; j < node2.getStatesSize(); j++) {
						HashMap<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
						map.put(node1, i);
						map.put(node2, j);
						
						//calling the joint probability method
						float jointProb = alg.getJointProbability(map);
						System.out.print("\t" + jointProb);
						sum += jointProb;
						
						assertTrue("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
								+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb, 
								(jointProb > 0 && jointProb < 1));
					}
					System.out.println();
				}
				assertEquals(node1.getName() + "," + node2.getName() ,1f, sum, 0.00005);
			}
		}
	}
	public final void testJointProbability5 () {
		// open a .net file
		ProbabilisticNetwork net = null;
		try {
			net = (ProbabilisticNetwork)new DneIO().load(new File(getClass().getResource("/Challenge_1v3.dne").toURI()));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// prepare the algorithm to compile network
		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
		alg.setNetwork(net);
		alg.run();
		
		
		for (int node1Index = 0; node1Index < net.getNodeCount()-1; node1Index++) {
			for (int node2Index = node1Index+1; node2Index < net.getNodeCount(); node2Index++) {
				
				//specifying the nodes and states for inquiring about the joint distribution
				ProbabilisticNode node2 = (ProbabilisticNode)net.getNodeAt(node2Index);
				ProbabilisticNode node1 = (ProbabilisticNode)net.getNodeAt(node1Index);
				
				assertEquals(2, node1.getStatesSize());
				assertEquals(2, node2.getStatesSize());
				
				System.out.println();
				System.out.println(" \t \t" + node2.getName() + "\t ");
				System.out.println(" \t \t" + node2.getStateAt(0) + "\t" + node2.getStateAt(1));
				float sum = 0f;
				for (int i = 0; i <node1.getStatesSize(); i++) {
					System.out.print(((i==0)?node1.getName():" ") + "\t" + node1.getStateAt(i));
					for (int j = 0; j < node2.getStatesSize(); j++) {
						HashMap<ProbabilisticNode, Integer> map = new HashMap<ProbabilisticNode, Integer>();
						map.put(node1, i);
						map.put(node2, j);
						
						//calling the joint probability method
						float jointProb = alg.getJointProbability(map);
						System.out.print("\t" + jointProb);
						sum += jointProb;
						
						assertTrue("P ( " + node1.getName() + " = " + node1.getStateAt(i) 
								+ " , " + node2.getName() + " = " + node1.getStateAt(j) + " ) : " + jointProb, 
							(jointProb > 0 && jointProb < 1));
					}
					System.out.println();
				}
				assertEquals(node1.getName() + "," + node2.getName() ,1f, sum, 0.00005);
			}
		}
	}
	
//	public final void testInvertArc() throws FileNotFoundException, URISyntaxException {
//		// open a .net file
//		ProbabilisticNetwork inputNet = null;
//		try {
//			inputNet = (ProbabilisticNetwork)new NetIO().load(new File(getClass().getResource("/Challenge_1.net").toURI()));
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		ProbabilisticNetwork outputNet = null;
//		try {
//			outputNet = (ProbabilisticNetwork)new NetIO().load(new File(getClass().getResource("/Challenge_1_inverted.net").toURI()));
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getMessage());
//		}
//		
//		// prepare the algorithm to compile network
//		JunctionTreeAlgorithm alg = new JunctionTreeAlgorithm();
//		alg.setNetwork(inputNet);
//		alg.run();
//		
//		ProbabilisticNode nodeInOutputNet = (ProbabilisticNode) outputNet.getNode("Investigation");
//		PotentialTable outputTable = nodeInOutputNet.getProbabilityFunction();
//		
//		List<INode> nodesInInputNet = new ArrayList<INode>(nodeInOutputNet.getParentNodes().size());
//		for (int i = 1; i < outputTable.variableCount(); i++) {
//			nodesInInputNet.add(inputNet.getNode(outputTable.getVariableAt(i).getName()));
//		}
//		
//		InCliqueConditionalProbabilityExtractor condProbExtractor = (InCliqueConditionalProbabilityExtractor) InCliqueConditionalProbabilityExtractor.newInstance(true);
//		PotentialTable inputTable = (PotentialTable) condProbExtractor.buildCondicionalProbability(inputNet.getNode("Investigation"), nodesInInputNet, inputNet, alg);
//		assertEquals(64, inputTable.tableSize());
//		
//		
//		assertEquals(outputTable.getVariablesSize(), inputTable.getVariablesSize());
//		assertEquals(inputTable.tableSize(), outputTable.tableSize());
//		
//		for (int i = 0; i < outputTable.getVariablesSize(); i++) {
//			assertEquals(outputTable.getVariableAt(i), inputTable.getVariableAt(i));
//		}
//		
//		outputTable.setValues(inputTable.getValues());
//		outputTable.copyData();
//		
//		new NetIO().save(new File("Challenge_1_inverted.net"), outputNet);
//	}
	
	
	private boolean isConnectedOrEqual(ProbabilisticNode from, ProbabilisticNode to, Set<ProbabilisticNode> handled) {
		if (handled == null) {
			handled = new HashSet<ProbabilisticNode>();
		}
		handled.add(from);
		if (from.equals(to)) {
			return true;
		}
		for (INode parent : from.getParentNodes()) {
			if (handled.contains(parent)) {
				continue;
			}
			if (isConnectedOrEqual((ProbabilisticNode) parent, to, handled)) {
				return true;
			}
		}
		for (INode child : from.getChildNodes()) {
			if (handled.contains(child)) {
				continue;
			}
			if (isConnectedOrEqual((ProbabilisticNode) child, to, handled)) {
				return true;
			}
		}
		return false;
	}

}
