package unbbs.persistence.model;

import java.io.Serializable;

/**
 * @author Rommel Carvalho
 *
 */
public class DomainValue implements Serializable {
	
	private int id;
	private String name;

	/**
	 * Returns the id.
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

}
