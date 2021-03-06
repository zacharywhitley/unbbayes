/**
 * 
 */
package unbbayes.prs.medg.ssid;

import java.util.ArrayList;

import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticNodeWrapper;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ssbn.SSBNNode;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * @author Shou Matsumoto
 * @deprecated classes involving {@link unbbayes.prs.medg.ssid.SSID} and {@link unbbayes.prs.medg.ssid.SSIDNode} should be avoided and use {@link unbbayes.prs.medg.ssid.SSIDGenerator} to generate ID from SSBN directly
 */
public class SSIDNode extends SSBNNode {

	private INode nodeInstance;

	/**
	 * @param pnet
	 * @param resident
	 * @param nodeInstance
	 */
	protected SSIDNode(ProbabilisticNetwork pnet, ResidentNode resident, INode nodeInstance) { 
		super(pnet, resident, new ProbabilisticNodeWrapper(nodeInstance));
		this.nodeInstance = nodeInstance;
	}
	

	/**
	 * @return the nodeInstance
	 */
	public INode getNodeInstance() {
		return nodeInstance;
	}

	/**
	 * @param decisionNode the nodeInstance to set
	 */
	public void setNodeInstance(INode nodeInstance) {
		this.nodeInstance = nodeInstance;
		if (nodeInstance instanceof ProbabilisticNode) {
			this.setProbNode((ProbabilisticNode) nodeInstance);
		} else {
			nodeInstance.setName(getName());
			nodeInstance.setDescription(getDescription());
			this.setProbNode(new ProbabilisticNodeWrapper(nodeInstance));
		}
	}
	
	/**
	 * Default constructor method initializing fields.
	 * @param network
	 * @param resident
	 * @param nodeInstance
	 * @return a new instance.
	 */
	public static INode getInstance(ProbabilisticNetwork network, ResidentNode resident, INode nodeInstance) {
		return new SSIDNode(network, resident, nodeInstance);
	}
	
	/**
	 * @see SSBNNode#getInstance(ProbabilisticNetwork, ResidentNode, ProbabilisticNode)
	 */
	public static SSBNNode getInstance(ProbabilisticNetwork network, ResidentNode resident, ProbabilisticNode nodeInstance) {
		return new SSIDNode(network, resident, nodeInstance);
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNNode#addParent(unbbayes.prs.mebn.ssbn.SSBNNode, boolean)
	 */
	public void addParent(SSBNNode parent, boolean isCheckingParentResident) throws SSBNNodeGeneralException {
		if (parent instanceof SSIDNode) {
			this.addParent((SSIDNode)parent, isCheckingParentResident);
		} else {
			super.addParent(parent, isCheckingParentResident);
		}
	}
	
	/**
	 * @see unbbayes.prs.mebn.ssbn.SSBNNode#addParent(unbbayes.prs.mebn.ssbn.SSBNNode, boolean)
	 */
	public void addParent(SSIDNode parent, boolean isCheckingParentResident) throws SSBNNodeGeneralException {
		
		if(getParents().contains(parent)){
			return; //do nothing! already is parent. 
		}
		
		// initial check. 
		if ((parent.getResident() == null )) {
			throw new SSBNNodeGeneralException(unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName()).getString("InternalError"));
		}
		
		// perform consistency check
		if (isCheckingParentResident) {
			
			//verify if the parent is in the list of possible parents of the node
			//(resident/input nodes that have a edge to the node)
			ArrayList<Node> expectedParents = this.getResident().getParents();
			boolean isConsistent = false;
			InputNode input = null;
			for (int i = 0; i < expectedParents.size(); i++) {
				if (parent.getResident() == expectedParents.get(i)) {
					isConsistent = true;
					break;
				}
				if (expectedParents.get(i) instanceof InputNode) {
					input = (InputNode)expectedParents.get(i);
					if (input.getResidentNodePointer().getResidentNode() == parent.getResident()) {
						isConsistent = true;
						break;
					}
				}
			}
			if (!isConsistent) {
				throw new SSBNNodeGeneralException("InconsistencyError");
			}
			// check if both probNodes are in a same network
			if (this.getProbNode() != null) {
				if (parent.getProbNode() != null) {
					if (this.getProbabilisticNetwork() != parent.getProbabilisticNetwork()) {
						throw new SSBNNodeGeneralException(unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName()).getString("IncompatibleNetworks"));
					}
				}
			}
		}
		
		//consistency OK: add the node 
		this.getParents().add(parent);
		parent.getChildren().add(this); 
		
		if (this.getNodeInstance() != null && parent.getNodeInstance() != null) {
			Edge edge = new Edge((Node)parent.getNodeInstance(), (Node)this.getNodeInstance());
			if (this.getProbabilisticNetwork() != null) {
				try {
					this.getProbabilisticNetwork().addEdge(edge);
				} catch (InvalidParentException e) {
					throw new SSBNNodeGeneralException(e.getMessage());
				}
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNNode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		// TODO Auto-generated method stub
		super.addChildNode(child);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNNode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		// TODO Auto-generated method stub
		super.appendState(state);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ssbn.SSBNNode#setProbNode(unbbayes.prs.bn.ProbabilisticNode)
	 */
	public void setProbNode(ProbabilisticNode probNode) {
		super.setProbNode(probNode);
		if (probNode instanceof ProbabilisticNodeWrapper) {
			// don't add the wrapper. Add the wrapped node directly
			// this is necessary because the core frequently uses == (i.e. instance comparison) instead of using Object#equals(Object),
			// TODO change the core so that it uses Object#equals(Object) instead of ==.
			ProbabilisticNodeWrapper wrapper = (ProbabilisticNodeWrapper) probNode;
			this.getProbabilisticNetwork().removeNode(wrapper);
			this.getProbabilisticNetwork().addNode((Node) wrapper.getWrappedNode());
			
			// also, do not use the wrapper in the CPTs.
			// Again, this is necessary because the core (especially the methods which handles CPTs)
			// frequently uses == (i.e. instance comparison) instead of using Object#equals(Object),
			// TODO change the core so that it uses Object#equals(Object) instead of ==.
			if (wrapper.getProbabilityFunction() != null) {	// if this is null, then wrapper is wrapping a node with no table
				int index = wrapper.getProbabilityFunction().getVariableIndex(wrapper);
				if (index >= 0) {
					// substitute the wrapper with the wrapped node
					wrapper.getProbabilityFunction().setVariableAt(index, wrapper.getWrappedNode());
				}
			}
		}
	}
	

}
