package unbbs.persistence.model;

import java.io.Serializable;

/**
 * @author Rommel Carvalho
 *
 */
public class ModelValue implements Serializable {

	private int id;
	private String name;
	private String description;
	private String model;
	private DomainValue domain;

	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the id.
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the model.
	 * @return String
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the description.
	 * @param description The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the id.
	 * @param id The id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the model.
	 * @param model The model to set
	 */
	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the domain.
	 * @return DomainLocal
	 */
	public DomainValue getDomain() {
		return domain;
	}

	/**
	 * Sets the domain.
	 * @param domain The domain to set
	 */
	public void setDomain(DomainValue domain) {
		this.domain = domain;
	}

}
