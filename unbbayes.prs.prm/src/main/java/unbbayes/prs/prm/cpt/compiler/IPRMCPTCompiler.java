package unbbayes.prs.prm.cpt.compiler;

import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.prm.IAttributeValue;

/**
 * This class converts a CPT in a PRM format into
 * another format. This is usually called by
 * {@link unbbayes.prs.prm.compiler.IPRMCompiler} in order
 * to convert a PRM to another network.
 * @author Shou Matsumoto
 *
 */
public interface IPRMCPTCompiler {
	
	/**
	 * Converts a CPT in a PRM format to a CPT in another format.
	 * OBS. we usually assume prmNode and probabilityFunctionOwner has consistent parents 
	 * (i.e. each prmNode's parent maps 1-1 to each probabilityFunctionOwner's parent IN THE SAME ORDER)
	 * we also assume probabilityFunctionOwner's CPT to have sufficient size.
	 * 
	 * @param prmNode : a node from where a CPT in a PRM format will be extracted
	 * @param probabilityFunctionOwner : a node from where a CPT in a resulting format will
	 * be extracted and filled
	 * @param parentMap a mapping from probabilityFunctionOwner's parents to prmNode's parents
	 * @return : the filled IProbabilityFunction, which can be also extracted from probabilisticFunctionOwner.
	 */
	public IProbabilityFunction compileCPT(IAttributeValue prmNode, IRandomVariable probabilityFunctionOwner, 
			Map<INode, IAttributeValue> parentMap);
}
