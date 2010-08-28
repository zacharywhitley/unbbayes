/**
 * 
 */
package unbbayes.prs.prm;

import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;

/**
 * Default implementation of {@link IAttributeValue}
 * @author Shou Matsumoto
 *
 */
public class AttributeValue implements IAttributeValue {

	private String value;
	private IAttributeDescriptor attributeDescriptor;
	private IPRMObject containerObject;
	private IDependencyChainSolver dependencyChainSolver;
	
	/**
	 * Visible at least for subclasses to allow inheritance
	 */
	protected AttributeValue() {
		super();
		this.dependencyChainSolver = DependencyChainSolver.newInstance(null);
	}

	/**
	 * 
	 * Default constructor method initializing fields
	 * @param containerObject
	 * @param attributeDescriptor
	 * @return
	 */
	public static AttributeValue newInstance(IPRMObject containerObject, IAttributeDescriptor attributeDescriptor) {
		AttributeValue ret = new AttributeValue();
		ret.setAttributeDescriptor(attributeDescriptor);
		ret.setContainerObject(containerObject);	// this setter must be after ret.setAttributeDescriptor(attributeDescriptor);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#getAttributeDescriptor()
	 */
	public IAttributeDescriptor getAttributeDescriptor() {
		return attributeDescriptor;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#getContainerObject()
	 */
	public IPRMObject getContainerObject() {
		return containerObject;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#getDependencyChainSolver()
	 */
	public IDependencyChainSolver getDependencyChainSolver() {
		return this.dependencyChainSolver;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#getValue()
	 */
	public String getValue() {
		return this.value;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#setAttributeDescriptor(unbbayes.prs.prm.IAttributeDescriptor)
	 */
	public void setAttributeDescriptor(IAttributeDescriptor attributeDescriptor) {
		this.attributeDescriptor = attributeDescriptor;
		// add to new attribute
		if (attributeDescriptor != null) {
			if (!attributeDescriptor.getAttributeValues().contains(this)) {
				attributeDescriptor.getAttributeValues().add(this);
			}
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#setContainerObject(unbbayes.prs.prm.IPRMObject)
	 */
	public void setContainerObject(IPRMObject prmObject) {
		this.containerObject = prmObject;
		// add to new object
		if (prmObject != null && this.getAttributeDescriptor() != null) {
			prmObject.getAttributeValueMap().put(this.getAttributeDescriptor(), this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#setDependencyChainSolver(unbbayes.prs.prm.IDependencyChainSolver)
	 */
	public void setDependencyChainSolver(IDependencyChainSolver solver) {
		this.dependencyChainSolver = solver;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeValue#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode parent) throws InvalidParentException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getChildNodes()
	 */
	public List<INode> getChildNodes() {
		return (List)this.getDependencyChainSolver().solveChildren(this);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		return (List)this.getDependencyChainSolver().solveParents(this);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStateAt(int)
	 */
	public String getStateAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getType()
	 */
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode child) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeLastState()
	 */
	public void removeLastState() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode parent) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeStateAt(int)
	 */
	public void removeStateAt(int index) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> children) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setDescription(java.lang.String)
	 */
	public void setDescription(String text) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setName(java.lang.String)
	 */
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> parents) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String state, int index) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStates(java.util.List)
	 */
	public void setStates(List<String> states) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if ((obj instanceof IAttributeValue) && (this.getAttributeDescriptor() != null)) {
			if (this.getValue() != null) {
				return super.equals(obj) 
					|| (this.getAttributeDescriptor().equals(((IAttributeValue)obj).getAttributeDescriptor()) && this.getValue().equals(((IAttributeValue)obj).getValue()));
			} else {
				return super.equals(obj) 
				|| (this.getAttributeDescriptor().equals(((IAttributeValue)obj).getAttributeDescriptor()) && ((IAttributeValue)obj).getValue() == null);
			}
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getContainerObject() 
				+ "_"
				+ this.getAttributeDescriptor().getName() ;
	}
	
	

}
