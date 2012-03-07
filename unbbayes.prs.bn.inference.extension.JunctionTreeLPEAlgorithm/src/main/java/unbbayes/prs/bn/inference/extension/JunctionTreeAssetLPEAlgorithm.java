/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * This class adapts {@link JunctionTreeLPEAlgorithm} in order
 * to calculate the states which yields smallest assets, instead
 * of smallest probability.
 * Currently, this is just a placeholder for future changes.
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeAssetLPEAlgorithm extends JunctionTreeLPEAlgorithm {

	/**
	 * Default constructor is public just to allow the plugin infrastructure to
	 * instantiate this class easily. However, use {@link #JunctionTreeLPEAlgorithm(ProbabilisticNetwork)}
	 * when possible.
	 */
	public JunctionTreeAssetLPEAlgorithm() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * Constructor initializing fields.
	 * @param net
	 */
	public JunctionTreeAssetLPEAlgorithm(AssetNetwork net) {
		this();
		
	}


}
