package unbbayes.prs.mebn;

import java.util.List;

public class InputNode extends MultiEntityNode {

	private MFrag mFrag;

	private List<ResidentNode> residentNodeChildList;

	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The oposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	private BuiltInRV inputInstanceOfRV;
	private ResidentNode inputInstanceOfNode;

}
