package unbbayes.prm.model;

/**
 * It defines a relationship between two instances.
 * 
 * @author David Saldaña
 * 
 */
public class InstanceRelationship {
	String idParent;
	Attribute parentAttribute;

	String idChild;
	Attribute childAttribute;

	/*
	 * Default constructor
	 */
	public InstanceRelationship(String idParent, Attribute parentAttribute,
			String idChild, Attribute childAttribute) {
		super();
		this.idParent = idParent;
		this.parentAttribute = parentAttribute;
		this.idChild = idChild;
		this.childAttribute = childAttribute;
	}

	public String getIdParent() {
		return idParent;
	}

	public void setIdParent(String idParent) {
		this.idParent = idParent;
	}

	public Attribute getParentAttribute() {
		return parentAttribute;
	}

	public void setParentAttribute(Attribute parentAttribute) {
		this.parentAttribute = parentAttribute;
	}

	public String getIdChild() {
		return idChild;
	}

	public void setIdChild(String idChild) {
		this.idChild = idChild;
	}

	public Attribute getChildAttribute() {
		return childAttribute;
	}

	public void setChildAttribute(Attribute childAttribute) {
		this.childAttribute = childAttribute;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InstanceRelationship)) {
			return false;
		}
		InstanceRelationship other = (InstanceRelationship) obj;

		// ids
		if (this.getIdParent().equals(other.getIdParent())
				&& this.getIdChild().equals(other.getIdChild())
				&& this.getParentAttribute().equals(other.getParentAttribute())
				&& this.getChildAttribute().equals(other.getChildAttribute())) {
			return true;
		}
		return false;
	}
}
