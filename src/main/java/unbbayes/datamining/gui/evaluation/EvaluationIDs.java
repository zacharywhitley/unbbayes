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

