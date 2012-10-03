package unbbayes.prm.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Parent relationship.
 * 
 * @author David Saldaña.
 * 
 */
public class ParentRel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6521692027180658334L;
	
	/**
	 * Child of the relationship.
	 */
	private Attribute child;
	/**
	 * Parent of the relationship.
	 */
	private Attribute parent;

	/**
	 * This attribute represents the path that the parent requires to get
	 * connected to the child.
	 */
	private Attribute[] path;
	
	
	/**
	 * Aggregate function.
	 */
	private AggregateFunctionName aggregateFunction;

	public ParentRel(Attribute parent, Attribute children) {
		this.parent = parent;
		this.child = children;
	}

	public Attribute getChild() {
		return child;
	}

	public void setChild(Attribute children) {
		this.child = children;
	}

	public Attribute getParent() {
		return parent;
	}

	public void setParent(Attribute parent) {
		this.parent = parent;
	}

	public Attribute[] getPath() {
		return path;
	}

	public void setPath(Attribute[] path) {
		this.path = path;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		
		ParentRel rhs = (ParentRel) obj;
		
		return new EqualsBuilder().appendSuper(super.equals(obj))
				.append(child, rhs.child).append(parent, rhs.parent)
				.append(path, rhs.path).isEquals();
	}

	public AggregateFunctionName getAggregateFunction() {
		return aggregateFunction;
	}

	public void setAggregateFunction(AggregateFunctionName aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

}
