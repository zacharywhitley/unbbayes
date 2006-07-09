package unbbayes.datamining.classifiers.resources;

import java.io.Serializable;
import java.util.ListResourceBundle;

/** Resources file for classifiers package. Localization = english.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public class ClassifiersResource extends ListResourceBundle implements Serializable
{	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** Override getContents and provide an array, where each item in the array is a pair
		of objects. The first element of each pair is a String key,
		and the second is the value associated with that key.
	*/
	public Object[][] getContents()
	{	return contents;
	}

	/** The resources */
	static final Object[][] contents =
	{	// Naive Bayes
		{"nullPrediction","Null distribution predicted"},
		{"exception1","Class is numeric!"},
		{"attribute","attribute"},
		{"exception2","less than two values for class"},
		{"exception3","standard deviation is 0 for class"},
		{"exception4","No model built yet."},
		{"class","Class"},
		{"mean","Mean: "},
		{"stdev","Standard Deviation: "},
		{"naiveBayesError","Can't print Naive Bayes classifier!"},
		// Evaluation
		{"summary","=== Summary ===\n"},
		{"correctly","Correctly Classified Instances   "},
		{"incorrectly","Incorrectly Classified Instances "},
		{"unclassified","UnClassified Instances      "},
		{"totalNumber","Total Number of Instances        "},
		{"unknownInstances","Ignored Class Unknown Instances   "},
		{"folds2","Number of folds must be at least 2!"},
		{"moreFolds","Can't have more folds than instances!"},
		{"folds1","Number of folds must be greater than 1"},
		{"classNegative","Class index is negative (not set)!"},
		{"noMatrix","Evaluation: No confusion matrix possible!"},
		{"accuracy","=== Detailed Accuracy By Class ===\n"},
		{"matrix","=== Confusion Matrix ===\n"},
		// Id3
		{ "exception1","Decision tree: Nominal class, please."	},
		{ "exception2","Decision tree: Only nominal attributes, please."	},
		{ "exception3","Decision tree: No missing values, please."	},
		{ "toStringException1","Decision tree: No model built yet. Try again!"	},
		{ "id3ToString","DECISION TREE:"	},
		{ "null","null"	},
		{ "NULL","NULL"	},
	};
}
