/**
 * 
 */
package unbbayes.prs.mebn.ssbn.pruner.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.INode;
import unbbayes.prs.mebn.ssbn.Query;
import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.pruner.IPruner;
import unbbayes.util.dseparation.IDSeparationUtility;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.dseparation.impl.MSeparationUtilityIncludingSeparators;

/**
 * @author Shou Matsumoto
 * Prunes a SSBN using d-separation concept.
 * A node will be removed if it is d-separated to the query node
 * given evidence nodes as separators.
 * The behavior of this class can be customized by using different 
 * d-separation utility.
 * @see {@link IDSeparationUtility}}
 */
public class DSeparationPruner implements IPruner {

	private IDSeparationUtility dSeparationUtility = null;
	
	/**
	 * default constructor is made protected in order to make it easy to extend
	 */
	protected DSeparationPruner() {
		super();
	}
	
	/**
	 * Construction method.
	 * Sets a default d-separation utility as {@link MSeparationUtility}
	 * @return new instance
	 * @see {@link MSeparationUtility}
	 */
	public static DSeparationPruner newInstance() {
		DSeparationPruner pruner = new DSeparationPruner();
		pruner.setDSeparationUtility(MSeparationUtilityIncludingSeparators.newInstance());
		return pruner;
	}
	
	/**
	 * Construction method initializing a d-separation utility to be used.
	 * @param dSeparationUtility
	 * @return new instance
	 */
	public static DSeparationPruner newInstance(IDSeparationUtility dSeparationUtility) {
		DSeparationPruner pruner = new DSeparationPruner();
		pruner.setDSeparationUtility(dSeparationUtility);
		return pruner;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.pruner.IPruner#prune(unbbayes.prs.mebn.ssbn.SSBN)
	 */
	public void prune(SSBN ssbn) {
		// converts a list of findings into a list of INodes
		List<INode> findingNodes = new ArrayList<INode>(ssbn.getFindingList());
		Set<INode> findingSet = new HashSet<INode>(findingNodes);
		
		// converts a list of queries into a list of INodes
		List<INode> queryNodes = new ArrayList<INode>();
		for (Query query : ssbn.getQueryList()) {
			queryNodes.add(query.getSSBNNode());
		}
		Set<INode> querySet =  new HashSet<INode>(queryNodes);
		
		// stores the nodes to be removed and find d-separated nodes
		Set<INode> nodesToPrune = this.getDSeparationUtility().getAllDSeparatedNodes(new HashSet<INode>(ssbn.getSimpleSsbnNodeList()), querySet, findingSet);
		
		
		// remove d-separated nodes from ssbn
		// TODO check if it is OK to retain the edges
		ssbn.removeAll(nodesToPrune);
	}

	/**
	 *  The behavior of this class can be customized by using different 
	 * d-separation utility.
	 * @see {@link IDSeparationUtility}}
	 * @return the dSeparationUtility
	 */
	public IDSeparationUtility getDSeparationUtility() {
		return dSeparationUtility;
	}

	/**
	 *  The behavior of this class can be customized by using different 
	 * d-separation utility.
	 * @see {@link IDSeparationUtility}}
	 * @param separationUtility the dSeparationUtility to set
	 */
	public void setDSeparationUtility(IDSeparationUtility separationUtility) {
		dSeparationUtility = separationUtility;
	}

}
