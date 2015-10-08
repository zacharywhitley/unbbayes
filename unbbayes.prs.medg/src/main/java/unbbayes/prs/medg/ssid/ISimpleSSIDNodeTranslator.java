/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNode;
import unbbayes.prs.mebn.ssbn.SimpleSSBNNodeUtils;
import unbbayes.prs.mebn.ssbn.exception.ImplementationRestrictionException;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * This interface should be used instead of the static methods in {@link SimpleSSBNNodeUtils}
 * @author Shou Matsumoto
 *
 */
public interface ISimpleSSIDNodeTranslator {


	/**
	 * Translate the SimpleSSBNNode's for the SSBNNode. The SimpleSSBNNode was 
	 * created for economize memory in the step of build the network. For posterior
	 * steps, how generation of the CPTs, is necessary more informations that the
	 * information offer by the SimpleSSBNNode. The SSBNNode contain all the 
	 * information necessary. This method translate the simpleSSBNNodes to the 
	 * correspondent SSBNNode and add the informations about parents (context node 
	 * parents too). 
	 * 
	 * @param simpleSSBNNodeList
	 * @param pn The probabilisticNetwork where will be created the ProbabilisticNodes 
	 *           (referenced by the SSBNNodes)
	 *           
	 * @return The list contain the SSBNNodes correspondent to the simpleSSBNNodeList.
	 *         In this list don't are returned the ContextFatherSSBNNode's correspondent 
	 *         to the SimpleContextNodeFatherSSBNNode's. This is set as parent
	 *         at the SSBNNode that originated it. 
	 * 
	 * @throws SSBNNodeGeneralException
	 * @throws ImplementationRestrictionException
	 */
	public  List<SSBNNode> translateSimpleSSBNNodeListToSSBNNodeList( 
			List<SimpleSSBNNode> simpleSSBNNodeList, ProbabilisticNetwork pn) throws 
			                SSBNNodeGeneralException,  ImplementationRestrictionException;
	
	

	/**
	 * The objective of this method is separate disconnected networks in singles
	 * networks. The argument is a list of nodes that have its relations by the 
	 * parents attributes. From this relations, the nodes are separated in singles 
	 * networks where if two nodes have a edge between its, its are in the 
	 * same single network. 
	 * 
	 * @param simpleSSBNNodeList List contain the SimpleSSBNNodes 
	 * @return A list of lists of ssbn nodes, where each of this list are a 
	 *         single connected network. 
	 */
	public List<SimpleSSBNNode>[] individualizeDisconnectedNetworks(
			List<SimpleSSBNNode> simpleSSBNNodeList);
	
	/**
	 * This object can create new instances of {@link SSBNNode} given {@link SimpleSSBNNode}.
	 * It should be used by {@link #translateSimpleSSBNNodeListToSSBNNodeList(List, ProbabilisticNetwork)} in
	 * order to create new instances of {@link SSBNNode}
	 * @return
	 */
	public INodeTranslator getNodeTranslator();
	
	/**
	 * This object can create new instances of {@link SSBNNode} given {@link SimpleSSBNNode}.
	 * It should be used by {@link #translateSimpleSSBNNodeListToSSBNNodeList(List, ProbabilisticNetwork)} in
	 * order to create new instances of {@link SSBNNode}
	 * @param nodeTranslator
	 */
	public void setNodeTranslator(INodeTranslator nodeTranslator);

}
