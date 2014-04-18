/**
 * 
 */
package unbbayes.prs.bn;

import java.awt.Color;

import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.extension.IPluginNode;

/**
 * A node specially tuned for noisy-max functions.
 * @author Shou Matsumoto
 *
 */
public class NoisyMaxNode extends ProbabilisticNode implements IPluginNode, ITemporalFactorizationNode {

	
	private static final long serialVersionUID = -2335262035711706344L;

	/**
	 * Default constructor must be public so that the plugin infrastructure
	 * can instantiate it easily.
	 */
	public NoisyMaxNode() {
		super();
		this.setColor(Color.ORANGE);
		this.appendState("State 0");
		this.appendState("State 1");
		this.getProbabilityFunction().addVariable(this);
		this.getProbabilityFunction().setValue(0, 1f);
		this.getProbabilityFunction().setValue(1, 0f);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.extension.IPluginNode#getNode()
	 */
	public Node getNode() {
		return this;
	}

	

}
