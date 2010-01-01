package unbbayes.prs.extension.impl;

import unbbayes.prs.Node;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.extension.IPluginNode;
/**
 * This is just a stub to test plugin nodes
 * @author Shou Matsumoto
 *
 */
public class ProbabilisticNodePluginStub extends ProbabilisticNode implements
		IPluginNode {

	public ProbabilisticNodePluginStub() {
		super();
		this.appendState("Stub state");
		PotentialTable table = this.getProbabilityFunction();
		table.addVariable(this);
		table.setValue(0, 1);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.extension.IPluginNode#getNode()
	 */
	public Node getNode() {
		return this;
	}

}
