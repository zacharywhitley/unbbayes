package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Encapsulates data relative to splitted instance set
 *  
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class SplitObject implements Serializable
{
	/** indexes for instances of an instance set */
	private ArrayList instances;
	/** indexes for attributes of an instance set */
	private Integer[] attributes;
	
	//-----------------------------CONSTRUCTORS---------------------------//

	/**
	 * Default constructor
	 * 
	 * @param instances indexes for instances of an instance set
	 * @param attributes indexes for attributes of an instance set
	 */
	public SplitObject(ArrayList instances, Integer[] attributes)
	{
		this.instances = instances;
		this.attributes = attributes;
	}
	
	//---------------------------------GETS-------------------------------//

	/**
	 * Returns the indexes for attributes of an instance set
	 * @return indexes for attributes of an instance set
	 */
	public Integer[] getAttributes() {
		return attributes;
	}

	/**
	 * Returns the indexes for instances of an instance set
	 * @return indexes for instances of an instance set
	 */
	public ArrayList getInstances() {
		return instances;
	}
	
	//---------------------------------SETS-------------------------------//	

	/**
	 * Sets the indexes for attributes of an instance set
	 * @param attributes indexes for attributes of an instance set
	 */
	public void setAttributes(Integer[] attributes) {
		this.attributes = attributes;
	}

	/**
	 * Sets the indexes for instances of an instance set
	 * @param instances indexes for instances of an instance set
	 */
	public void setInstances(ArrayList instances) {
		this.instances = instances;
	}
}
