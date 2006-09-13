package unbbayes.prs.mebn;

import java.util.List;

public class GenerativeInputNode extends InputNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7377146558744109802L;
	
	private List<DomainResidentNode> residentNodeChildList;
	
	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The oposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	//already inherit from superclass
	//private BuiltInRV inputInstanceOfRV; 
	//is it possible to have the same name as in the superclass?
	private DomainResidentNode inputInstanceOfNode; 

	private DomainMFrag mFrag;

}
