package unbbayes.datamining.gui.evaluation;

import java.util.Hashtable;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 20/09/2007
 */
public class EvaluationIDs {

	public static final int AUC = 0;
	public static final String AUC_NAME = "AUC";

	public static final int ROC_POINTS = 1;
	public static final String ROC_POINTS_NAME = "ROC points";
	
	private static Hashtable<Integer, String> evaluationIDToName;
	private static Hashtable<String, Integer> evaluationNameToID;
	
	private static String[] evaluationNames;
	
	private static void initiate() {
		evaluationIDToName = new Hashtable<Integer, String>();
		evaluationIDToName.put(AUC, AUC_NAME);
		evaluationIDToName.put(ROC_POINTS, ROC_POINTS_NAME);
		
		evaluationNameToID = new Hashtable<String, Integer>();
		evaluationNameToID.put(AUC_NAME, AUC);
		evaluationNameToID.put(ROC_POINTS_NAME, ROC_POINTS);
		
	}

	public static String[] getEvaluationNames() {
		if (evaluationIDToName == null) {
			initiate();
		}
		if (evaluationNames == null) {
			int numEvaluations = evaluationIDToName.size();
			evaluationNames = new String[numEvaluations];
			for (int i = 0; i < numEvaluations; i++) {
				evaluationNames[i] = evaluationIDToName.get(i);
			}
		}
		
		return evaluationNames.clone();		
	}
	
	public static String getEvaluationName(int evaluationID) {
		if (evaluationIDToName == null) {
			initiate();
		}
		
		return evaluationIDToName.get(evaluationID);		
	}
	
	public static int getEvaluationID(String evaluationName) {
		return evaluationNameToID.get(evaluationName);
	}
	
	public int getNumEvaluations() {
		return evaluationIDToName.size();
	}

}

