/**
 * 
 */
package unbbayes.prs.mebn.ssbn.pruner.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.util.Debug;
import unbbayes.util.dseparation.impl.MSeparationUtility;

/**
 * @author Shou Matsumoto
 * Prunes a node using barren node concept.
 * A barren node is a node which has no query or finding node as its descendant.
 * It also removes all isolated findings (findings that has no parents or children)
 */
public class BarrenNodePruner implements IPruner {

	/**
	 * default constructor is made protected in order to make it easier to extend
	 */
	protected BarrenNodePruner() {
		super();
	}
	
	/**
	 * Default construction method
	 * @return
	 */
	public static BarrenNodePruner newInstance() {
		return new BarrenNodePruner();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.pruner.IPruner#prune(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void prune(SSBN ssbn) {
		MSeparationUtility utility = MSeparationUtility.newInstance();
		
		// Set which will contain findings and their ancestors (parents and parents of parents and so on)
		Set<INode> ancestors = new HashSet<INode>();
		
		// extract findings, but ignore "isolated" findings (findings with no parents or children)
		for (INode finding : ssbn.getFindingList()) {
			if (finding.getParentNodes().size() > 0
					|| finding.getChildNodes().size() > 0) {
				ancestors.add(finding);
			}
		}
		
		// add all ancestors of the findings
		ancestors.addAll(utility.getAllAncestors(ancestors));

		// extract queries' ancestors and queries themselves
		Set<INode> queries = new HashSet<INode>();
		for (Query query : ssbn.getQueryList()) {
			queries.add(query.getSSBNNode());
		}
		ancestors.addAll(queries);
		ancestors.addAll(utility.getAllAncestors(queries));
		
		Collection<INode> nodesToRemove = new HashSet<INode>();
		
		for (INode node : ssbn.getSimpleSsbnNodeList()) {
			if (!ancestors.contains(node)) {
				Debug.println(this.getClass(), "Removing node: " + node.toString());
				nodesToRemove.add(node);
			}
		}
		
		ssbn.removeAll(nodesToRemove);
	}

}
