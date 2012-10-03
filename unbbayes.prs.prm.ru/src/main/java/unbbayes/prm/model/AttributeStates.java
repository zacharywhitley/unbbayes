package unbbayes.prm.model;

import java.io.Serializable;

/**
 * Possible states of an attribute.
 * 
 * @author David Saldaña.
 * 
 */
public class AttributeStates implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6167755106543461918L;
	
	
	private Attribute attribute;
	private String[] states;

	public AttributeStates(Attribute attribute, String states[]) {
		this.attribute = attribute;
		this.states = states;
	}

	public String[] getStates() {
		return states;
	}

	public void setStates(String[] states) {
		this.states = states;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

}
