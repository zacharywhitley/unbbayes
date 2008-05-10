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
package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Encapsulates data relative to splitted instance set
 *  
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class SplitObject implements Serializable
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
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
