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
package unbbayes.datamining.classifiers;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *  Abstract classifier. All schemes for numeric or nominal prediction extends this class.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public abstract class Classifier {
	/**
	 * Build a classifier.
	 *
	 * @param data The training set
	 * @exception Exception If classifier can't be build sucessfully.
	 */
  	public abstract void buildClassifier(InstanceSet data) throws Exception;

  	/**
  	 * Classifies a given test instance.
  	 *
  	 * @param instance the instance to be classified
  	 * @return the classification
	 * @exception Exception if an error occurred during the prediction
  	 */
  	public abstract int classifyInstance(Instance instance) throws Exception;

	public abstract float[] distributionForInstance(Instance instance)
	throws Exception;
  	
}