/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 *
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
	private KBFacade kb = null;
	
	private MFrag mfrag = null;
	
	/**
	 * 
	 */
	public BottomUpSSBNGenerator() {
		// TODO Auto-generated constructor stub
	}


	/*
	 * Extracts OVInstances refereed as context node's arguments
	 */
	private OVInstance[] getContextOVInstance(ContextNode context, List<OVInstance> ovinstances) {
		Collection<OVInstance> ret = new ArrayList<OVInstance>();
	
		if ( ( context == null ) || ( ovinstances == null ) ) {
			return ret.toArray(new OVInstance[ret.size()]);
		}
		
		List<Argument> arglist = context.getArgumentList();
		for (Argument arg : arglist) {
			for (OVInstance instance : ovinstances) {
				if (instance.getOv() == arg.getOVariable()) {
					ret.add(instance);
				}
			}
		}
		return ret.toArray(new OVInstance[ret.size()]);
	}
	
	/*
	 * Calls ContextNodeEvaluator's method to check context node's validation.
	 */
	private void contextNodeEvaluation (SSBNNode queryNode) {
		if (queryNode == null) {
			return;
		}
		DomainMFrag mfrag = queryNode.getResident().getMFrag();
		
		// We assume if MFrag is already set to use Default, then some context has failed previously and there's no need to evaluate again.		
		if (mfrag.isUsingDefaultCPT()) {
			return;
		}
		
		Collection<ContextNode> contexts = mfrag.getContextByOV( queryNode.getOVs());
		ContextNodeEvaluator evaluator = new ContextNodeEvaluator();
		for (ContextNode node : contexts) {
			evaluator.evaluate(node, this.getContextOVInstance(node, new ArrayList<OVInstance>(queryNode.getArguments())));
		}
	}
	
	/*
	 * The recursive part of SSBN bottom-up generation algorithm
	 * currentNode is the node currently analized
	 * seen is all nodes analized previously (doesnt contain currentNode)
	 * net is the resulting ProbabilisticNetwork
	 */
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen, ProbabilisticNetwork net) throws SSBNNodeGeneralException {
		
		// check for cycle
		if (seen.contains(currentNode)) {
			throw new SSBNNodeGeneralException(this.resource.getString("CycleFound"));
		}
		
		// check if querynode has a known value or it should be a probabilistic node (query the kb)
		
		// Extract arguments
		List<String> findingArgList = new ArrayList<String>();
		for (OVInstance ovInstance : currentNode.getArguments()) {
			findingArgList.add(ovInstance.getEntity().getInstanceName());
		}
		// Execute query on kb
		String findingValue = kb.searchFinding(currentNode.getResident().getName(), findingArgList );
		
		// Treat returned value
		if (!findingValue.toUpperCase().contains("NO SOLUTION")) {
			// there were an exact match
			StateLink exactValue = currentNode.getResident().getPossibleValueByName(findingValue);
			if (exactValue == null) {
				throw new SSBNNodeGeneralException(resource.getString("PossibleValueMismatch"));
			}
			currentNode.setNodeAsFinding(exactValue.getState());
			return currentNode;
		}
		
		// if the program reached this line, the node doesn't have a finding
		seen.add(currentNode);	// mark this as already seen  (treated) node
		
		
		// evaluates querynode's mfrag's context nodes (if not OK, sets MFrag's flag to use default CPT)	
		contextNodeEvaluation(currentNode);
		
		
		// extract parents to treat them recursively
		List<DomainResidentNode> parents = new ArrayList<DomainResidentNode>();
		
		// Extract resident nodes
		for (DomainResidentNode parent : currentNode.getResident().getResidentNodeFatherList()) {
			parents.add(parent);
		}
		
		// Extract input nodes
		for (GenerativeInputNode input : currentNode.getResident().getInputNodeFatherList()) {
			parents.add((DomainResidentNode)input.getResidentNodePointer().getResidentNode());
		}
		
		
		// recursive calling for parents
		for (DomainResidentNode resident : parents) {
			SSBNNode ssbnnode = SSBNNode.getInstance(resident, new ProbabilisticNode());
			
			this.generateRecursive(ssbnnode, seen, net);	// algorithm's core
			
			currentNode.addParent(ssbnnode, true);
			if (ssbnnode.getProbNode() != null) {	// if this parent is not a finding
				net.addEdge(new Edge(currentNode.getProbNode(),ssbnnode.getProbNode()));
			}
			
		}
		
		
		// TODO finish this method

		return currentNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) throws SSBNNodeGeneralException {
		
		// As the query starts, let's clear the flags used by the previous query
		query.getMebn().clearMFragsIsUsingDefaultCPTFlag();
		
		// some data extraction
		
		SSBNNode querynode = query.getQueryNode();
		this.kb = query.getKb();

		// initialization
		
		ProbabilisticNetwork net = new ProbabilisticNetwork(query.getMebn().getName());
		
		
		// call recursive
		
		this.generateRecursive(querynode, new SSBNNodeList(), net);
		
		return net;
	}

}
