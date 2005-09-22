package unbbayes.datamining.discretize;

import java.util.*;

public class DiscretizationValue {

	private float value;
	private List<Integer> classValue = new ArrayList<Integer>();;
	
	
	
	public List<Integer> getClassValue() {
		return classValue;
	}



	public void addClassValue(int value) {
		classValue.add(value);
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
