/*
 * Created on 24/05/2003
 *
 */
package unbbayes.datamining.classifiers;

import java.util.*;

/**
 * @author Mário Henrique
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SplitObject {

	private ArrayList instances;
	private Integer[] attributes;
	/**
	 * @return
	 */
	public Integer[] getAttributes() {
		return attributes;
	}

	/**
	 * @return
	 */
	public ArrayList getInstances() {
		return instances;
	}

	/**
	 * @param integers
	 */
	public void setAttributes(Integer[] integers) {
		attributes = integers;
	}

	/**
	 * @param list
	 */
	public void setInstances(ArrayList list) {
		instances = list;
	}

}
