package unbbayes.datamining.classifiers.decisiontree;

import java.util.*;

/**
 * @author Mário Henrique
 */
public class SplitObject {

	private ArrayList instances;
	private Integer[] attributes;
	
	public SplitObject(ArrayList instances, Integer[] attributes)
	{
		this.instances = instances;
		this.attributes = attributes;
	}
	
	public Integer[] getAttributes() {
		return attributes;
	}

	public ArrayList getInstances() {
		return instances;
	}

	public void setAttributes(Integer[] integers) {
		attributes = integers;
	}

	public void setInstances(ArrayList list) {
		instances = list;
	}
}
