package unbbs.persistence.model;
/**
 * Key class for Entity Bean: Domain
 */
public class DomainKey implements java.io.Serializable {
	static final long serialVersionUID = 3206093459760846163L;
	/**
	 * Implementation field for persistent attribute: id
	 */
	public int id;
	/**
	 * Creates an empty key for Entity Bean: Domain
	 */
	public DomainKey() {
	}
	/**
	 * Creates a key for Entity Bean: Domain
	 */
	public DomainKey(int id) {
		this.id = id;
	}
	/**
	 * Returns true if both keys are equal.
	 */
	public boolean equals(java.lang.Object otherKey) {
		if (otherKey instanceof unbbs.persistence.model.DomainKey) {
			unbbs.persistence.model.DomainKey o =
				(unbbs.persistence.model.DomainKey) otherKey;
			return ((this.id == o.id));
		}
		return false;
	}
	/**
	 * Returns the hash code for the key.
	 */
	public int hashCode() {
		return ((new java.lang.Integer(id).hashCode()));
	}
	/**
	 * Get accessor for persistent attribute: id
	 */
	public int getId() {
		return id;
	}
	/**
	 * Set accessor for persistent attribute: id
	 */
	public void setId(int newId) {
		id = newId;
	}
}
