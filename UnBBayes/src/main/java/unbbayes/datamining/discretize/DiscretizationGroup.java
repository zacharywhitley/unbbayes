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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscretizationGroup {

	private Map<Float,DiscretizationValue> map = new HashMap<Float,DiscretizationValue>();

	public void addValue(DiscretizationValue dv) {
		if (!map.containsKey(dv.getValue())) {
			map.put(dv.getValue(),dv);			
		} else {
			map.get(dv.getValue()).getClassValue().add(dv.getClassValue().get(0));
		}
	}
	
	public List<Float> removeValue(float value) {
		Set<Float> set = map.keySet();
		List<Float> dumpList = new ArrayList<Float>();
		for (Float f : set) {
			if (f > value) {
				dumpList.add(f);
			}
		}
		for (float f : dumpList) {
			map.remove(f);
		}
		return dumpList;
	}
	
	public Collection<DiscretizationValue> getCollection() {
		return map.values();
	}
	
	public List<DiscretizationValue> getList() {
		List<DiscretizationValue> list = new ArrayList<DiscretizationValue>();
		for (DiscretizationValue value : getCollection()) {
			list.add(value);
		}
		return list;
	}
	
	/**
	 * Insertion sort incremental on a list of DiscretizationValue.
	 *
	 */
	public List<DiscretizationValue> sortValuesAsc() {
		List<DiscretizationValue> list = getList();
		int i;
		DiscretizationValue key;
		for(int j=1; j<list.size(); j++)
		{	key = list.get(j);
			//Insert a[j] into the sorted sequence a[1 .. j-1]
			i = j - 1;
			while ((i > -1) && (list.get(i).getValue() > key.getValue()))
			{	
				list.set(i + 1,list.get(i));
				i--;
			}
			list.set(i+1,key);
		}
		return list;
	}
	
	public List<Float> computeInfoPoints(List<DiscretizationValue> list)  {
		List<Float> points = new ArrayList<Float>();
		int size = list.size();
		for (int i=1; i<size;i++) {
			DiscretizationValue valueBefore = list.get(i-1);
			DiscretizationValue value = list.get(i);
			boolean diferentClass = false;
			start : for (int vb : valueBefore.getClassValue()) {
				for (int v : value.getClassValue()) {
					if (vb!=v) {
						diferentClass=true;
						break start;
					}
				}
			}
			if (diferentClass) {
				points.add((valueBefore.getValue()+value.getValue())/2);
			}
		}
		return points;
	}
	
	public float[] countClassesBefore(float infoPoint,List<DiscretizationValue> list, int numClasses) {
		float[] result = new float[numClasses];
		for (DiscretizationValue value : list) {
			if (value.getValue()<infoPoint) {
				for (int classValue : value.getClassValue()) {
					result[classValue]++;
				}
			}
		}
		return result;
	}
	
	public float[] countClassesAfter(float infoPoint,List<DiscretizationValue> list, int numClasses) {
		float[] result = new float[numClasses];
		for (DiscretizationValue value : list) {
			if (value.getValue()>infoPoint) {
				for (int classValue : value.getClassValue()) {
					result[classValue]++;
				}
			}
		}
		return result;
	}

	/*private static void insertionSortInc(float[] a)
	{	int i;
		float key;
		for(int j=1; j<a.length; j++)
		{	key = a[j];
			//Insert a[j] into the sorted sequence a[1 .. j-1]
			i = j - 1;
			while ((i > -1) && (a[i] > key))
			{	a[i + 1] = a[i];
				i--;
			}
			a[i + 1] = key;
		}
	}
	
  	/**
	 * @param args
	 */
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		float[] f = {6,5,8,9,2,1};
		DiscretizationGroup.insertionSortInc(f);
		for (float ff : f)
		System.out.println(ff);
	}*/

}
