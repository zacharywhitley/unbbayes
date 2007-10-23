/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MFrag;

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
	
	private OVInstance[] getContextOVInstance() {
		// TODO
		return null;
	}
	
	private SSBNNode generateRecursive(SSBNNode currentNode , SSBNNodeList seen) {
		//		 TODO Auto-generated stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNGenerator#generateSSBN(unbbayes.prs.mebn.ssbn.Query)
	 */
	public ProbabilisticNetwork generateSSBN(Query query) {
		
		SSBNNode querynode = query.getQueryNode();
		
		DomainMFrag mfrag = querynode.getResident().getMFrag();
		
		Collection<ContextNode> contexts = mfrag.getContextByOV( querynode.getOVs());
		ContextNodeAvaliator evaluator = new ContextNodeAvaliator();
		/*
		OVInstance ovinsts[] = new OVInstance[querynode.getArguments().size()];
		ovinsts = querynode.getArguments().toArray(ovinsts);
		*/
		for (ContextNode node : contexts) {
			//evaluator.evaluate(node, ovinsts);
		}
		// TODO Auto-generated method stub
		return null;
	}

}
