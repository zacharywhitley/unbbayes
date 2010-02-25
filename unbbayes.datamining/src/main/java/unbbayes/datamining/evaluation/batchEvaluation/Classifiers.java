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
package unbbayes.datamining.evaluation.batchEvaluation;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class Classifiers {

	private static String[] classifierNames = new String[20];
	
	private static int numClassifiers = 1;
	
	public static Classifier newClassifier(int classifierID)
	throws Exception {
		Classifier classifier = null;

	    switch (classifierID) {
			case 1:
				classifier = new NaiveBayes();
				classifierNames[classifierID] = "naive";
				break;
				
			case 0:
				classifier = new C45();
				classifierNames[classifierID] = "c45";
				break;
				
//			case 1:
//				classifier = new NaiveScaleBayes();
//				classifierNames[classifierID] = "naiveScale";
//				break;
		}

		return classifier;
	}

	public static void buildClassifier(InstanceSet train,
			Classifier classifier, float[] distribution,
			int positiveClass)
	throws Exception {
		if (classifier instanceof DecisionTreeLearning) {
			((DecisionTreeLearning) classifier).setPositiveClass(positiveClass);
		} else if (classifier instanceof DistributionClassifier) {
			((DistributionClassifier) classifier).setOriginalDistribution(
			distribution);
		}
		classifier.buildClassifier(train);
	}

	public static String getClassifierName(int classifierID) {
		return classifierNames[classifierID];
	}
	
	public static int getNumClassifiers() {
		return numClassifiers;
	}
	
}

