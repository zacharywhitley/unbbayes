/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.prm.builders.AttributeDescriptorBuilder;
import unbbayes.prs.prm.builders.ForeignKeyBuilder;
import unbbayes.prs.prm.builders.IAttributeDescriptorBuilder;
import unbbayes.prs.prm.builders.IForeignKeyBuilder;

/**
 * This is the default implementation of {@link IPRMClass}.
 * Inheritance is not implemented.
 * @author Shou Matsumoto
 *
 */
public class PRMClass implements IPRMClass {

	private String name;
	private List<IAttributeDescriptor> attributeDescriptors;
	
	private List<String> states;

	private IAttributeDescriptorBuilder attributeDescriptorBuilder;
	
	private Collection<IForeignKey> incomingForeignKeys;
	private List<IForeignKey> outgoingForeignKeys;
	
	private String primaryKeyName;
	
	private IForeignKeyBuilder foreignKeyBuilder;

	private IPRM prm;
	private List<IPRMObject> prmObjects;
	private String description;
	private IPRMClass superClass;
	
	/**
	 * At least one constructor is made visible for subclasses
	 * in order to allow inheritance
	 */
	protected PRMClass() {
		this.attributeDescriptors = new ArrayList<IAttributeDescriptor>();
		this.attributeDescriptorBuilder = new AttributeDescriptorBuilder();
		this.states = new ArrayList<String>();
		this.incomingForeignKeys = new ArrayList<IForeignKey>();
		this.outgoingForeignKeys = new ArrayList<IForeignKey>();
		this.foreignKeyBuilder = new ForeignKeyBuilder();
		this.prmObjects = new ArrayList<IPRMObject>();
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static PRMClass newInstance(IPRM prm, String name) {
		PRMClass ret = new PRMClass ();
		ret.prm = prm;
		ret.name = name;
		ret.primaryKeyName = "PK_" + name;
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getAttributeDescriptors()
	 */
	public List<IAttributeDescriptor> getAttributeDescriptors() {
		return this.attributeDescriptors;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getForeignKeys()
	 */
	public List<IForeignKey> getForeignKeys() {
		return this.outgoingForeignKeys;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setForeignKeys(java.util.Collection)
	 */
	public void setForeignKeys(List<IForeignKey> foreignKeys) {
		this.outgoingForeignKeys = foreignKeys;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getIncomingForeignKeys()
	 */
	public Collection<IForeignKey> getIncomingForeignKeys() {
		return this.incomingForeignKeys;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getPRM()
	 */
	public IPRM getPRM() {
		return this.prm;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getPRMObjects()
	 */
	public List<IPRMObject> getPRMObjects() {
		return this.prmObjects;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getPrimaryKeys()
	 */
	public Collection<IAttributeDescriptor> getPrimaryKeys() {
		List<IAttributeDescriptor> keys = new ArrayList<IAttributeDescriptor>();
		for (IAttributeDescriptor attribute : this.getAttributeDescriptors()) {
			if (attribute.isPrimaryKey()) {
				keys.add(attribute);
			}
		}
		return keys;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setAttributeDescriptors(java.util.List)
	 */
	public void setAttributeDescriptors(
			List<IAttributeDescriptor> attributeDescriptors) {
		for (IAttributeDescriptor attribute : this.attributeDescriptors ) {
			attribute.setPRMClass(null);
		}
		this.attributeDescriptors = attributeDescriptors;
		for (IAttributeDescriptor attribute : this.attributeDescriptors ) {
			attribute.setPRMClass(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setIncomingForeignKeys(java.util.Collection)
	 */
	public void setIncomingForeignKeys(Collection<IForeignKey> foreignKeys) {
		this.incomingForeignKeys = foreignKeys;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setPRM(unbbayes.prs.prm.IPRM)
	 */
	public void setPRM(IPRM prm) {
		this.prm = prm;
		if (this.prm != null && !this.prm.getIPRMClasses().contains(this)) {
			this.prm.addPRMClass(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setPRMObjects(java.util.List)
	 */
	public void setPRMObjects(List<IPRMObject> objects) {
		this.prmObjects = objects;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode arg0) throws InvalidParentException {
		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode arg0) throws InvalidParentException {
		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#appendState(java.lang.String)
	 */
	public void appendState(String state) {
		this.updateCPT();
		states.add(state);
	}

	/**
	 * This is called when a state information of {@link #getStates()}
	 * is modified by {@link #appendState(String)}, {@link #removeStateAt(int)}...
	 */
	protected void updateCPT() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		List<INode> ret = new ArrayList<INode>(this.getParentNodes());
		ret.addAll(this.getChildNodes());
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getChildNodes()
	 */
	public List<INode> getChildNodes() {
		List<INode> ret = new ArrayList<INode>();
		for (IForeignKey fk : this.getForeignKeys()) {
			ret.add(fk.getClassTo());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		List<INode> ret = new ArrayList<INode>();
		for (IForeignKey fk : this.getIncomingForeignKeys()) {
			ret.add(fk.getClassFrom());
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStateAt(int)
	 */
	public String getStateAt(int arg0) {
		return states.get(arg0);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		return states.size();
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
	public void removeChildNode(INode arg0) {
		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeLastState()
	 */
	public void removeLastState() {
		if (states.size() > 1) {
			updateCPT();
			states.remove(states.size() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode arg0) {
		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeStateAt(int)
	 */
	public void removeStateAt(int arg0) {
		states.remove(arg0);
		this.updateCPT();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> arg0) {
		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setDescription(java.lang.String)
	 */
	public void setDescription(String arg0) {
		this.description = arg0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setName(java.lang.String)
	 */
	public void setName(String arg0) {
		this.name = arg0;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> arg0) {

		throw new UnsupportedOperationException("ALPHA version of this class not allow this operation. Use methods of IAttributeDescriptor or IForeignKey.");
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String arg0, int arg1) {
		states.set(arg1, arg0);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStates(java.util.List)
	 */
	public void setStates(List<String> arg0) {
		this.states = arg0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(IPRMClass obj) {
		// instance or name comparision
		if (this.getName() != null) {
			return super.equals(obj) || this.getName().equals(obj.getName());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#getPRMSuperClass()
	 */
	public IPRMClass getPRMSuperClass() {
		return this.superClass;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#setPRMSuperClass(unbbayes.prs.prm.IPRMClass)
	 */
	public void setPRMSuperClass(IPRMClass superClass) {
		this.superClass = superClass;
	}

	/**
	 * @return the attributeDescriptorBuilder
	 */
	public IAttributeDescriptorBuilder getAttributeDescriptorBuilder() {
		return attributeDescriptorBuilder;
	}

	/**
	 * @param attributeDescriptorBuilder the attributeDescriptorBuilder to set
	 */
	public void setAttributeDescriptorBuilder(
			IAttributeDescriptorBuilder attributeDescriptorBuilder) {
		this.attributeDescriptorBuilder = attributeDescriptorBuilder;
	}

	/**
	 * @return the states
	 */
	public List<String> getStates() {
		return states;
	}

	/**
	 * @return the primaryKeyName
	 */
	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	/**
	 * @param primaryKeyName the primaryKeyName to set
	 */
	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}

	/**
	 * @return the foreignKeyBuilder
	 */
	public IForeignKeyBuilder getForeignKeyBuilder() {
		return foreignKeyBuilder;
	}

	/**
	 * @param foreignKeyBuilder the foreignKeyBuilder to set
	 */
	public void setForeignKeyBuilder(IForeignKeyBuilder foreignKeyBuilder) {
		this.foreignKeyBuilder = foreignKeyBuilder;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IPRMClass#findAttributeDescriptorByName(java.lang.String)
	 */
	public IAttributeDescriptor findAttributeDescriptorByName(String name) {
		// TODO optimize search
		for (IAttributeDescriptor attribute : this.getAttributeDescriptors()) {
			if (attribute.getName() == null ) {
				if (name == null) {
					return attribute;
				}
			} else if (attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}
	
	

}
