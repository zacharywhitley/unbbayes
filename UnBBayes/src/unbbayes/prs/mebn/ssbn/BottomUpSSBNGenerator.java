/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.kb.KBFacade;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomFacade;

/**
 * @author Shou Matsumoto
 *
 */
public class BottomUpSSBNGenerator implements ISSBNGenerator {

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
	
	private void contextNodeEvaluation (SSBNNode queryNode) {
		if (queryNode == null) {
			return;
		}
		DomainMFrag mfrag = queryNode.getResident().getMFrag();
		Collection<ContextNode> contexts = mfrag.getContextByOV( queryNode.getOVs());
		ContextNodeAvaliator evaluator = new ContextNodeAvaliator();
		for (ContextNode node : contexts) {
			evaluator.evaluate(node, this.getContextOVInstance(node, new ArrayList<OVInstance>(queryNode.getArguments())));
		}
	}
	
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen) {
		//		 TODO Auto-generated stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) {
		
		
		// some data extraction
		
		SSBNNode querynode = query.getQueryNode();
		KBFacade kb = query.getKb();

		// initialization
		
		ProbabilisticNetwork net = new ProbabilisticNetwork(query.getMebn().getName());
		
		
		// evaluates querynode's mfrag's context nodes
		
		contextNodeEvaluation(querynode);
		
		
		// check if querynode has a known value or it should be a probabilistic node
		List<String> findingArgList = new ArrayList<String>();
		for (OVInstance ovInstance : querynode.getArguments()) {
			findingArgList.add(ovInstance.getEntity().getInstanceName());
		}
		String findingValue = kb.searchFinding(querynode.getResident().getName(), findingArgList );
		
		if (!findingValue.toUpperCase().contains("NO SOLUTION")) {
			// there were an exact match
			// TODO treat exact match on knowledge base
			return net;
		}
		
		//TODO recursive calling
		
		
		// TODO finish this method
		return net;
	}

}
