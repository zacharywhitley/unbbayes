package unbbayes.prm.model;

/**
 * Possible states of an attribute.
 * 
 * @author David Saldaña.
 * 
 */
public class AttributeStates {
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
