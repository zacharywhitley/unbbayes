/**
 * 
 */
package unbbayes.prs;

import java.io.File;
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
import unbbayes.io.NetIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.util.dseparation.impl.MSeparationUtility;

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
