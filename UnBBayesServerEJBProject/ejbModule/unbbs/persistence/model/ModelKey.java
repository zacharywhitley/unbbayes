package unbbs.persistence.model;
/**
 * Key class for Entity Bean: Model
 */
public class ModelKey implements java.io.Serializable {
	static final long serialVersionUID = 3206093459760846163L;
	/**
	 * Implementation field for persistent attribute: id
	 */
	public int id;
	/**
	 * Creates an empty key for Entity Bean: Model
	 */
	public ModelKey() {
	}
	/**
	 * Creates a key for Entity Bean: Model
	 */
	public ModelKey(int id) {
		this.id = id;
	}
	/**
	 * Returns true if both keys are equal.
	 */
	public boolean equals(java.lang.Object otherKey) {
		if (otherKey instanceof unbbs.persistence.model.ModelKey) {
			unbbs.persistence.model.ModelKey o =
				(unbbs.persistence.model.ModelKey) otherKey;
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
