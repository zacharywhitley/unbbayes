/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;

/**
 * 
 * This is just a common interface for inference algorithms that are able to include an evidence
 * permanently in a node (e.g. to set a finding, and then absorb the node).
 * @author Shou Matsumoto
 *
 */
public interface IPermanentEvidenceInferenceAlgorithm extends IInferenceAlgorithm {

	/**
	 * This method shall make best effort in order to mark
	 * the probability of a given node to a value permanently.
	 * For example, implementations may remove the node from
	 * the network after inserting an evidence.
	 * @param evidences : node to add permanent hard evidence and their probability distribution.
	 * For example, if a list [.7,.1,.2] is provided, then the 1st state will be set
	 * to 70%, the second to 10% and the third to 20% (and the node may eventually be absorbed).
	 * If set to [1,0,0,...,0], then a hard evidence will be inserted.
	 * If invalid values (e.g. values above 1, below 0, Infinite, NaN or null) are present,
	 * and the other values in the remaining indexes shall be either 0 or 1, and it will
	 * be considered as a hard evidence. For example, providing [0,null,null] will be
	 * a "negative" hard evidence  setting the 1st state to 0% and adjusting the probabilities
	 * of the other states consistently. If [1,null,null] is provided, then it shall be equivalent
	 * to [1,0,0] 
	 * @param isToDeleteNode : if true, node will be deleted after setting as permanent node.
	 */
	public void setAsPermanentEvidence(Map<INode, List<Float>> evidences, boolean isToDeleteNode);
}
