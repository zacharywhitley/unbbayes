/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
