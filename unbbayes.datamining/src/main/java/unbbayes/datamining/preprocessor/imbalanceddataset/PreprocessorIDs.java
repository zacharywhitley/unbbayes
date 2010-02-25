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
package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Hashtable;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 20/09/2007
 */
public class PreprocessorIDs {

	public static final int ORIGINAL = 0;
	public static final String ORIGINAL_NAME = "Original";

	public static final int OVERSAMPLING = 1;
	public static final String OVERSAMPLING_NAME = "Oversampling";

	public static final int SIMPLESAMPLING = 2;
	public static final String SIMPLESAMPLING_NAME = "Simplesampling";

	public static final int SMOTE = 3;
	public static final String SMOTE_NAME = "Smote";

	public static final int CLUSTER_BASED_OVERSAMPLING = 4;
	public static final String CLUSTER_BASED_OVERSAMPLING_NAME = "Cluster Based Oversampling";

	public static final int CLUSTER_BASED_SMOTE = 5;
	public static final String CLUSTER_BASED_SMOTE_NAME = "Cluster Based Smote";

	public static final int CCLEAR = 6;
	public static final String CCLEAR_NAME = "Cclear";

	public static final int BASELINE = 7;
	public static final String BASELINE_NAME = "Baseline";
	
	private static Hashtable<Integer, String> preprocessorIDToName;
	private static Hashtable<String, Integer> preprocessorNameToID;
	
	private static String[] preprocessorNames;
	
	private static void initiate() {
		preprocessorIDToName = new Hashtable<Integer, String>();
		preprocessorIDToName.put(ORIGINAL, ORIGINAL_NAME);
		preprocessorIDToName.put(OVERSAMPLING, OVERSAMPLING_NAME);
		preprocessorIDToName.put(SIMPLESAMPLING, SIMPLESAMPLING_NAME);
		preprocessorIDToName.put(SMOTE, SMOTE_NAME);
		preprocessorIDToName.put(CLUSTER_BASED_OVERSAMPLING, CLUSTER_BASED_OVERSAMPLING_NAME);
		preprocessorIDToName.put(CLUSTER_BASED_SMOTE, CLUSTER_BASED_SMOTE_NAME);
		preprocessorIDToName.put(CCLEAR, CCLEAR_NAME);
		preprocessorIDToName.put(BASELINE, BASELINE_NAME);
		
		preprocessorNameToID = new Hashtable<String, Integer>();
		preprocessorNameToID.put(ORIGINAL_NAME, ORIGINAL);
		preprocessorNameToID.put(OVERSAMPLING_NAME, OVERSAMPLING);
		preprocessorNameToID.put(SIMPLESAMPLING_NAME, SIMPLESAMPLING);
		preprocessorNameToID.put(SMOTE_NAME, SMOTE);
		preprocessorNameToID.put(CLUSTER_BASED_OVERSAMPLING_NAME, CLUSTER_BASED_OVERSAMPLING);
		preprocessorNameToID.put(CLUSTER_BASED_SMOTE_NAME, CLUSTER_BASED_SMOTE);
		preprocessorNameToID.put(CCLEAR_NAME, CCLEAR);
		preprocessorNameToID.put(BASELINE_NAME, BASELINE);
		
	}

	public static String[] getPreprocessorNames() {
		if (preprocessorIDToName == null) {
			initiate();
		}
		if (preprocessorNames == null) {
			int numPreprocessors = preprocessorIDToName.size();
			preprocessorNames = new String[numPreprocessors];
			for (int i = 0; i < numPreprocessors; i++) {
				preprocessorNames[i] = preprocessorIDToName.get(i);
			}
		}
		
		return preprocessorNames.clone();		
	}
	
	public static String getPreprocessorName(int preprocessorID) {
		if (preprocessorIDToName == null) {
			initiate();
		}
		
		return preprocessorIDToName.get(preprocessorID);		
	}
	
	public int getNumPreprocessors() {
		return preprocessorIDToName.size();
	}

	public static int getPreprocessorID(String preprocessorName) {
		if (preprocessorNameToID == null) {
			initiate();
		}
		
		return preprocessorNameToID.get(preprocessorName);		
	}
	
}

