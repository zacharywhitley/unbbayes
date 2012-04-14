
package unbbayes.prs.mebn.ssbn.pruner.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.pruner.IPruneStructure;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import junit.framework.TestCase;

/**
 * Test suite for BarrenNodePruner
 * @author Shou Matsumoto
 *
 */
public class BarrenNodePrunerTest extends TestCase {

	private IPruneStructure pruneStructure = null;
	
	private SSBN ssbn = null;
	
	private Set<SimpleSSBNNode> nodesNotToPrune;
	private Set<SimpleSSBNNode> nodesToPrune;
	
	/**
	 * @param name
	 */
	public BarrenNodePrunerTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		List<IPruner> uniqueElementList = new ArrayList<IPruner>(1);
		uniqueElementList.add(BarrenNodePruner.newInstance());
		
		this.pruneStructure = PruneStructureImpl.newInstance(uniqueElementList);
		
		this.nodesNotToPrune = new HashSet<SimpleSSBNNode>();
		this.nodesToPrune = new HashSet<SimpleSSBNNode>();
		
		// creating ssbn for test
		
		/*
		 * node1-->node2--->query1--->node4--->node6
		 *    \                             
		 *     ----node3--->query2--->node5--->finding1.
		 *            |
		 *            v
		 *           node7
		 *           
		 *    node8   finding2
		 *     
		 *    Nodes 4, 6, 7, 8 and finding2 are to be pruned
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
		this.nodesToPrune.add(node);
		
		
		parent = node;
		resident = new ResidentNode("node6",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		node.addParent(parent);
		this.ssbn.addSSBNNode(node);
		this.nodesToPrune.add(node);
		
		
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
		this.nodesToPrune.add(node);
		
		
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

		// isolated nodes
		
		resident = new ResidentNode("node8",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		this.ssbn.addSSBNNode(node);
		this.nodesToPrune.add(node);
		
		resident = new ResidentNode("finding2",mfrag);
		node = SimpleSSBNNode.getInstance(resident);
		this.ssbn.addSSBNNode(node);
		this.ssbn.getFindingList().add(node);
		this.nodesToPrune.add(node);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		
		this.pruneStructure = null;
		this.ssbn = null;
		
		System.gc();
	}

	/**
	 * Test method for {@link unbbayes.prs.mebn.ssbn.pruner.impl.BarrenNodePruner#prune(unbbayes.prs.mebn.ssbn.SSBN)}.
	 */
	public final void testPrune() {
		
		assertTrue("SSBN must contain all non-prunable nodes", this.ssbn.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune));
		assertTrue("SSBN must contain all prunable nodes", this.ssbn.getSimpleSsbnNodeList().containsAll(this.nodesToPrune));
		
		this.pruneStructure.pruneStructure(this.ssbn);
		
		assertTrue("SSBN must contain all non-prunable nodes after prune",this.ssbn.getSimpleSsbnNodeList().containsAll(this.nodesNotToPrune));
		
		for (SimpleSSBNNode prunedNode : this.nodesToPrune) {
			assertFalse("SSBN should not contain pruned nodes", this.ssbn.getSimpleSsbnNodeList().contains(prunedNode));
			for (SimpleSSBNNode node : this.ssbn.getSimpleSsbnNodeList()) {
				assertFalse("Remaining nodes should not contain pruned parents", node.getParents().contains(prunedNode));
				assertFalse("Remaining nodes should not contain pruned children", node.getChildNodes().contains(prunedNode));
			}
		}
		
	}

}
