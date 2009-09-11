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
package unbbayes.datamining.evaluation;

import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 14/04/2007
 */
public class Folds {
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.classifiers.resources.ClassifiersResource");

	int numFolds;
	int currentFold;
	InstanceSet instanceSet;
	InstanceSet train;
	InstanceSet temp;
	InstanceSet test;
	InstanceSet eval;
	
	public Folds(InstanceSet instanceSet, int numFolds)
	throws Exception {
		this.numFolds = numFolds;
		this.instanceSet = instanceSet;
		
		/* Check if the instanceSet is compacted */
		if (instanceSet.isCompacted()) {
			throw new IllegalArgumentException("cross validation only " +
					"works with non compacted instanceSet!");
		}
		
		if (instanceSet.getClassAttribute().isNominal()) {
			instanceSet.stratify(numFolds);
		} else {
			instanceSet.randomize(new Random(new Date().getTime()));
		}
	}

	public InstanceSet getTrain(int foldID) {
		return trainCV(instanceSet, numFolds, foldID);
	}
	
	public InstanceSet getTest(int foldID) {
		return testCV(instanceSet, numFolds, foldID);
	}
	
	/**
	 * Creates the training set for one fold of a cross-validation
	 * on the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param currentFold 0 for the first fold, 1 for the second, ...
	 * @return the training set for a fold
	 * @exception IllegalArgumentException if the number of folds is less than
	 * 2 or greater than the number of instances.
	 */
	public static InstanceSet trainCV(InstanceSet instanceSet, int numFolds,
			int currentFold) {
		int numInstPerFold, first, offset;
		int numInstances = instanceSet.numInstances();
		InstanceSet train;
	
		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
		}
		
	    numInstPerFold = numInstances / numFolds;
		if (currentFold < numInstances % numFolds) {
			numInstPerFold++;
			offset = currentFold;
		} else {
			offset = numInstances % numFolds;
		}
		
		int trainSize = numInstances - numInstPerFold;
		train = new InstanceSet(instanceSet, trainSize);
		
	    first = currentFold * (numInstances / numFolds) + offset;
		
		instanceSet.copyInstancesTo(train, 0, first);
		instanceSet.copyInstancesTo(train, first + numInstPerFold,
				numInstances - first - numInstPerFold);
	
		return train;
	}

	/**
	 * Creates the test set for one fold of a cross-validation on
	 * the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param currentFold 0 for the first fold, 1 for the second, ...
	 * @return the test set of instances
	 * @exception IllegalArgumentException if the number of folds is less than
	 * 2 or greater than the number of instances.
	 */
	public static InstanceSet testCV(InstanceSet instanceSet, int numFolds,
			int currentFold) {
		int numInstPerFold, first, offset;
		int numInstances = instanceSet.numInstances();
		InstanceSet test;
	
		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
		}
		
		numInstPerFold = numInstances / numFolds;
		if (currentFold < numInstances % numFolds) {
			numInstPerFold++;
			offset = currentFold;
		} else {
			offset = numInstances % numFolds;
		}
		
		test = new InstanceSet(instanceSet, numInstPerFold);
		
		first = currentFold * (numInstances / numFolds) + offset;
		instanceSet.copyInstancesTo(test, first, numInstPerFold);
		
		return test;
	}

}