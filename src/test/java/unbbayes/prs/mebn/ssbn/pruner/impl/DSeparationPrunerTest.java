/**
 * 
 */
package unbbayes.prs.mebn.ssbn.pruner.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;

/**
 * Test suite for DSeparationPruner
 * @author Shou Matsumoto
 *
 */
public class DSeparationPrunerTest extends TestCase {

	private IPruneStructure pruneStructure = null;
	
	private SSBN ssbn = null;
	private Set<SimpleSSBNNode> nodesNotToPrune;
	private Set<SimpleSSBNNode> nodesToPrune;
	
	
	private SSBN ssbn2 = null;
	private Set<SimpleSSBNNode> nodesNotToPrune2;
	private Set<SimpleSSBNNode> nodesToPrune2;
	
	/**
	 * @param name
	 */
	public DSeparationPrunerTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		List<IPruner> uniqueElementList = new ArrayList<IPruner>(1);
		uniqueElementList.add(DSeparationPruner.newInstance());
		
		this.pruneStructure = PruneStructureImpl.newInstance(uniqueElementList);
		
		this.nodesNotToPrune = new HashSet<SimpleSSBNNode>();
		this.nodesToPrune = new HashSet<SimpleSSBNNode>();
		
		this.nodesNotToPrune2 = new HashSet<SimpleSSBNNode>();
		this.nodesToPrune2 = new HashSet<SimpleSSBNNode>();
		
		// creating ssbn for test
		
		/*
		 * node1-->node2--->query1--->node4--->node6
		 *    \                             
		 *     ----node3--->query2--->node5--->finding1.
		 *            |
		 *            v
		 *           node7
		 *     
		 *    Nodes 4, 6 and 7 are to be pruned
		 */
		
		MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork("mebn");
		MFrag mfrag = new MFrag("mfrag",mebn);
		
		this.ssbn = new SSBN();
		
		ResidentNode resident = new ResidentNode("node1",mfrag);
		SimpleSSBNNode node1 = SimpleSSBNNode.getInstance(resident);
		this.ssbn.addSSBNNode(node1);
		this.nodesNotToPrune.add(node1);
		
		SimpleSSBNNode parent = node1;
		resident = new ResidentNode("node2",mfrag);
		SimpleSSBNNode node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesNotToPrune.add(node);
		
		parent = node;
		resident = new ResidentNode("query1",mfrag);
		Query query = new Query(resident,new ArrayList<OVInstance>());
		this.ssbn.getQueryList().add(query);
		node = query.getSSBNNode();
		node.addParent(parent);
		this.ssbn.addSSBNNodeIfItDontAdded(node);
		this.nodesNotToPrune.add(node);
		
		parent = node;
		resident = new ResidentNode("node4",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesNotToPrune.add(node);
		
		
		parent = node;
		resident = new ResidentNode("node6",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesNotToPrune.add(node);
		
		
		parent = node1;
		resident = new ResidentNode("node3",mfrag);
		SimpleSSBNNode node3 = SimpleSSBNNode.getInstance(resident);
		node3.addParent(parent);
		this.ssbn.addSSBNNode(node3);
		this.nodesNotToPrune.add(node3);
		
		
		parent = node3;
		resident = new ResidentNode("node7",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesNotToPrune.add(node);
		
		
		parent = node3;
		resident = new ResidentNode("query2",mfrag);
		query = new Query(resident,new ArrayList<OVInstance>());
		this.ssbn.getQueryList().add(query);
		node = query.getSSBNNode();
		node.addParent(parent);
		this.ssbn.addSSBNNodeIfItDontAdded(node);
		this.nodesNotToPrune.add(node);
		

		parent = node;
		resident = new ResidentNode("node5",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesNotToPrune.add(node);
		
		parent = node;
		resident = new ResidentNode("finding1",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.ssbn.getFindingList().add(node);
		this.nodesNotToPrune.add(node);
		
		
		
		
		/*
		 * SSBN2:
		 * 
		 * (node0)
		 *   |
		 *   V
		 * (node1)------>(finding2)-------
		 *   \                            \
		 *    \                            V
		 *     ------->(finding3)-------->(node4)----->(query5)
		 * 
		 */		
		
		this.ssbn2 = new SSBN();
		
		// Node0 -> Node1
		resident = new ResidentNode("node0",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		this.ssbn2.addSSBNNode(node);
		this.nodesToPrune2.add(node);
		
		parent = node;
		resident = new ResidentNode("node1",mfrag);
		node1 = SimpleSSBNNode.getInstance(resident);
		node1.addParent(parent);
		this.ssbn2.addSSBNNode(node1);
		this.nodesToPrune2.add(node1);
		
		// Node1 -> (finding2,finding3)
		parent = node1;
		resident = new ResidentNode("finding2",mfrag);
		SimpleSSBNNode finding2 = SimpleSSBNNode.getInstance(resident);
		finding2.addParent(parent);
		this.ssbn2.addSSBNNode(finding2);
		this.ssbn2.getFindingList().add(finding2);
		this.nodesNotToPrune2.add(finding2);
		
		resident = new ResidentNode("finding3",mfrag);
		SimpleSSBNNode finding3  = SimpleSSBNNode.getInstance(resident);
		finding3.addParent(parent);
		this.ssbn2.addSSBNNode(finding3);
		this.ssbn2.getFindingList().add(finding3);
		this.nodesNotToPrune2.add(finding3);
		
		// finding2 -> Node4
		// finding3 -> Node4
		resident = new ResidentNode("node4",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(finding2);
		node.addParent(finding3);
		this.ssbn2.addSSBNNode(node);
		this.nodesNotToPrune2.add(node);		
		
		// Node 4 -> query5
		parent = node;
		resident = new ResidentNode("query5",mfrag);
		query = new Query(resident,new ArrayList<OVInstance>());
		this.ssbn2.getQueryList().add(query);
		node = query.getSSBNNode();
		node.addParent(parent);
		this.ssbn2.addSSBNNodeIfItDontAdded(node);
		this.nodesNotToPrune2.add(node);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		
		this.nodesNotToPrune = null;
		this.nodesNotToPrune2 = null;
		this.nodesToPrune = null;
		this.nodesToPrune2 = null;
		
		this.pruneStructure = null;
		this.ssbn = null;
		this.ssbn2 = null;
		
		System.gc();
	}
	
	
	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.pruner.impl.DSeparationPruner#prune(unbbayes.prs.mebn.ssbn.SSBN)}.
	 */
	public final void testPrune() {
		
		// testing ssbn 1
		
		assertTrue("SSBN must contain all non-prunable nodes", this.ssbn.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune));
		
		int nodeCount = this.ssbn.getSimpleSsbnNodeList().size();
		
		this.pruneStructure.pruneStructure(this.ssbn);
		
		assertEquals("No prune should be made", nodeCount, this.ssbn.getSimpleSsbnNodeList().size());
		
		assertTrue("SSBN must contain all non-prunable nodes after prune",this.ssbn.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune));
		
		
		// testing ssbn 2

		assertTrue("SSBN must contain all non-prunable nodes", this.ssbn2.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune2));
		assertTrue("SSBN must contain all prunable nodes", this.ssbn2.getSimpleSsbnNodeList().containsAll(this.nodesToPrune2));
		
		this.pruneStructure.pruneStructure(this.ssbn2);
		
		assertTrue("SSBN must contain all non-prunable nodes after prune",this.ssbn2.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune2));
		
		for (SimpleSSBNNode prunedNode : this.nodesToPrune2) {
			assertFalse("SSBN should not contain pruned nodes", this.ssbn2.getSimpleSsbnNodeList().contains(prunedNode));
			for (SimpleSSBNNode node : this.ssbn2.getSimpleSsbnNodeList()) {
				assertFalse("Remaining nodes should not contain pruned parents", node.getParents().contains(prunedNode));
				assertFalse("Remaining nodes should not contain pruned children", node.getChildNodes().contains(prunedNode));
			}
		}
		
	}

}
