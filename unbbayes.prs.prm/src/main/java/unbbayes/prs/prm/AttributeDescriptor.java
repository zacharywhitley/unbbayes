/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;

/**
 * Default implementation of {@link IAttributeDescriptor}
 * @author Shou Matsumoto
 *
 */
public class AttributeDescriptor implements IAttributeDescriptor {
	
	private String name = "";
	
	private Boolean isPK = false;
	private Boolean isMandatory = false;

	private int typeCode = IAttributeDescriptor.STRING_TYPE;	// default is string

	private IPRMDependency prmDependency;

	private IForeignKey foreignKeyReference;
	
	private List<String> states;
	
	private IPRMClass prmClass;

	private Collection<IAttributeValue> attributeValues;

	/**
	 * At least one constructor must be visible for subclasses to
	 * allow inheritance
	 */
	protected AttributeDescriptor() {
		this.prmDependency = PRMDependency.newInstance(this);
		states = new ArrayList<String>();
		this.attributeValues = new HashSet<IAttributeValue>();
	}
	
	/**
	 * Default construction method
	 * @param name
	 * @param prmClass
	 * @return
	 */
	public static AttributeDescriptor newInstance(IPRMClass prmClass, String name) {
		AttributeDescriptor ret = new AttributeDescriptor();
		ret.setName(name);
		ret.setPRMClass(prmClass);
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#getAttributeValues()
	 */
	public Collection<IAttributeValue> getAttributeValues() {
		return this.attributeValues;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#getForeignKeyReference()
	 */
	public IForeignKey getForeignKeyReference() {
		return this.foreignKeyReference;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#getPRMClass()
	 */
	public IPRMClass getPRMClass() {
		return this.prmClass;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#getPRMDependency()
	 */
	public IPRMDependency getPRMDependency() {
		return this.prmDependency;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#isForeignKey()
	 */
	public Boolean isForeignKey() {
		return this.getForeignKeyReference() != null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#isMandatory()
	 */
	public Boolean isMandatory() {
		return this.isMandatory;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#isPrimaryKey()
	 */
	public Boolean isPrimaryKey() {
		return isPK;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setAttributeValues(java.util.Collection)
	 */
	public void setAttributeValues(Collection<IAttributeValue> attributeValues) {
		this.attributeValues = attributeValues;
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setForeignKeyReference(unbbayes.prs.prm.IForeignKey)
	 */
	public void setForeignKeyReference(IForeignKey foreignKey) {
		this.foreignKeyReference = foreignKey;

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setMandatory(java.lang.Boolean)
	 */
	public void setMandatory(Boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setPRMClass(unbbayes.prs.prm.IPRMClass)
	 */
	public void setPRMClass(IPRMClass prmClass) {
		this.prmClass = prmClass;
		if (this.prmClass != null && this.prmClass.getAttributeDescriptors() != null && !this.prmClass.getAttributeDescriptors().contains(this)) {
			this.prmClass.getAttributeDescriptors().add(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setPRMDependency(unbbayes.prs.prm.IPRMDependency)
	 */
	public void setPRMDependency(IPRMDependency prmDependency) {
		this.prmDependency = prmDependency;
		
		if (this.prmDependency != null && !this.equals(this.prmDependency.getAttributeDescriptor())) {
			this.prmDependency.setAttributeDescriptor(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setPrimaryKey(java.lang.Boolean)
	 */
	public void setPrimaryKey(Boolean isPK) {
		this.isPK = isPK;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IAttributeDescriptor#setType(int)
	 */
	public void setType(int typeCode) {
		this.typeCode = typeCode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode arg0) throws InvalidParentException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode arg0) throws InvalidParentException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#appendState(java.lang.String)
	 */
	public void appendState(String arg0) {
		this.onStateCountChange(true);
		this.getStates().add(arg0);
	}
	
	/**
     *  This method can be used to update the affected tables when inserting and removing
     *  new states.
     *
     *@param  state  state to be inserted / removed.
     *@param  isInsertion  true for insertion and false for remotion.
     */
    protected void onStateCountChange(boolean isInsertion) {
        int d = getStatesSize();
        if (d > 0) {
            while (d <=  getPRMDependency().getCPT().getTableValues().size()) {
                if (isInsertion) {
                	getPRMDependency().getCPT().getTableValues().add(d++, 0.0f);
                } else {
                	getPRMDependency().getCPT().getTableValues().remove(d);
                }
                d += getStatesSize();
            }
        }        
        
        // TODO update children
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getParentNodes()
	 */
	public List<INode> getParentNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStateAt(int)
	 */
	public String getStateAt(int arg0) {
		return this.getStates().get(arg0);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		return this.getStates().size();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#getType()
	 */
	public int getType() {
		return this.typeCode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeLastState()
	 */
	public void removeLastState() {
		this.removeStateAt(this.getStatesSize() - 1);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#removeStateAt(int)
	 */
	public void removeStateAt(int arg0) {
		this.getStates().remove(this.getStateAt(arg0));
		this.onStateCountChange(false);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
	 */
	public void setChildNodes(List<INode> arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setDescription(java.lang.String)
	 */
	public void setDescription(String arg0) {
		// TODO Auto-generated method stub

	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
	 */
	public void setStateAt(String arg0, int arg1) {
		this.getStates().set(arg1, arg0);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.INode#setStates(java.util.List)
	 */
	public void setStates(List<String> arg0) {
		this.states = arg0;
	}
	
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IAttributeDescriptor && this.getName() != null) {
			return super.equals(obj) 
			|| (this.getName().equals(((IAttributeDescriptor)obj).getName())
					&& this.getPRMClass().equals(((IAttributeDescriptor)obj).getPRMClass()));
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		/*
		 * Renders a string in the following format:
		 * <Name> : <Type> (<Possible_values>) ; <PK> ; <FK> ; <Mandatory>
		 */
		String states = this.getStatesAsString();
		return this.getName() + " : " 
							  + ((this.getTypeAsString() != null)?this.getTypeAsString():"Unknown") 
							  + ((states != null && (states.length() > 0) && (!this.isMandatory()))?( "(" + states + ")" ):"") 
							  + (this.isPrimaryKey()?" ; PK":"") 
							  + (this.isForeignKey()?" ; FK":"") 
							  + (this.isMandatory()?" ; Mandatory":"") ;
	}
	
	/**
	 * Converts {@link #getStateAt(int)} from 0 to {@link #getStatesSize()}
	 * as a comma separated string.
	 * @return
	 */
	public String getStatesAsString() {
		String ret = "";
		
		for (int i = 0; i < getStatesSize(); i++) {
			ret += getStateAt(i);
			if (i + 1 < getStatesSize() ) {
				ret += ",";
			}
		}
		
		return ret;
	}
	
	/**
	 * Converts {@link #getType()} to a string
	 * @return
	 */
	public String getTypeAsString() {
		switch (this.getType()) {
		case IAttributeDescriptor.STRING_TYPE:
			return "String";
		case IAttributeDescriptor.NUMERIC_TYPE:
			return "Number";
		case IAttributeDescriptor.DATE_TYPE:
			return "Date";
		}
		return null; // default
	}

	/**
	 * @return the states
	 */
	public List<String> getStates() {
		return states;
	}

}
