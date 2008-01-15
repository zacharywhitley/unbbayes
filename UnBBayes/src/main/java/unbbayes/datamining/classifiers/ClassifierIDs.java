package unbbayes.datamining.classifiers;

import java.util.Hashtable;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 20/09/2007
 */
public class ClassifierIDs {

	public static final int C45 = 0;
	public static final String C45_NAME = "C4.5";

	public static final int NAIVE_BAYES = 1;
	public static final String NAIVE_BAYES_NAME = "Naive Bayes";
	
	private static Hashtable<Integer, String> classifierIDToName;
	private static Hashtable<String, Integer> classifierNameToID;
	
	private static String[] classifierNames;
	
	private static void initiate() {
		classifierIDToName = new Hashtable<Integer, String>();
		classifierIDToName.put(C45, C45_NAME);
		classifierIDToName.put(NAIVE_BAYES, NAIVE_BAYES_NAME);
		
		classifierNameToID = new Hashtable<String, Integer>();
		classifierNameToID.put(C45_NAME, C45);
		classifierNameToID.put(NAIVE_BAYES_NAME, NAIVE_BAYES);
		
	}

	public static String[] getClassifierNames() {
		if (classifierIDToName == null) {
			initiate();
		}
		if (classifierNames == null) {
			int numClassifiers = classifierIDToName.size();
			classifierNames = new String[numClassifiers];
			for (int i = 0; i < numClassifiers; i++) {
				classifierNames[i] = classifierIDToName.get(i);
			}
		}
		
		return classifierNames.clone();		
	}
	
	public static String getClassifierName(int classifierID) {
		if (classifierIDToName == null) {
			initiate();
		}
		
		return classifierIDToName.get(classifierID);		
	}
	
	public int getNumClassifiers() {
		return classifierIDToName.size();
	}
	
}

