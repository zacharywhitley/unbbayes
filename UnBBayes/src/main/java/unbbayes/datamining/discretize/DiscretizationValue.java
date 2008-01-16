package unbbayes.datamining.discretize;

import java.util.ArrayList;
import java.util.List;

import unbbayes.datamining.datamanipulation.Utils;

public class DiscretizationValue implements Comparable {

	private float value;
	private List<Byte> classValue = new ArrayList<Byte>();;
	
	
	
	public List<Byte> getClassValue() {
		return classValue;
	}



	public void addClassValue(byte value) {
		classValue.add(value);
	}

	public int compareTo(Object obj) {
		if (obj instanceof DiscretizationValue) {
			DiscretizationValue other = (DiscretizationValue)obj;
			float otherValue = other.getValue();
			if (Utils.eq(value,otherValue)) {
				return 0;
			} else if (value > otherValue) {
				return 1;
			} else {
				return -1;
			}
		}
		return 0;
	}
	


	public float getValue() {
		return value;
	}



	public void setValue(float value) {
		this.value = value;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
