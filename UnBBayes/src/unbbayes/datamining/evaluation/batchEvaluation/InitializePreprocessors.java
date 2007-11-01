package unbbayes.datamining.evaluation.batchEvaluation;

import java.util.ArrayList;
import java.util.Date;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.preprocessor.imbalanceddataset.Baseline;
import unbbayes.datamining.preprocessor.imbalanceddataset.Batch;
import unbbayes.datamining.preprocessor.imbalanceddataset.Cclear;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedOversampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUtils;
import unbbayes.datamining.preprocessor.imbalanceddataset.Original;
import unbbayes.datamining.preprocessor.imbalanceddataset.PreprocessorIDs;
import unbbayes.datamining.preprocessor.imbalanceddataset.RandomOversampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Simplesampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class InitializePreprocessors {
	
	private int totalNumBatchIterations;
	private int numPreprocessors;
	private ArrayList<Integer> preprocessorOrder;
	private ArrayList<Batch> preprocessorList;
	private Original original;
	private Simplesampling simplesampling;
	private RandomOversampling oversampling;
	private Smote smote;
	private ClusterBasedOversampling cbo;
	private ClusterBasedSmote cbs;
	private Cclear cClear;
	private Baseline baseline;

	private ClusterBasedUtils clusterBasedUtils;
	private ArrayList<Integer> numBatchIterations;
	private int currentPreprocessorID;
	private int currentBatchIterationID;

	public InitializePreprocessors(InstanceSet instanceSet,
			PreprocessorParameters[] preprocessors)
	throws Exception {
		currentPreprocessorID = 0;
		currentBatchIterationID = -1;

		instantiate(instanceSet, preprocessors);
		computeNumBatchIterations();
	}
	
	public void instantiate(InstanceSet instanceSet, PreprocessorParameters[] preprocessors)
	throws Exception {
		numPreprocessors = preprocessors.length;
		numBatchIterations = new ArrayList<Integer>(numPreprocessors);
		preprocessorList = new ArrayList<Batch>(numPreprocessors);
		preprocessorOrder = new ArrayList<Integer>();
		
		int preprocessorID;
		for (int i = 0; i < numPreprocessors; i++) {
			preprocessorID = preprocessors[i].getPreprocessorID();
			preprocessorOrder.add(preprocessorID);
			instantiateAux(instanceSet, preprocessors[i]);
		}
	}
	
	private void instantiateAux(InstanceSet instanceSet,
			PreprocessorParameters parameters)
	throws Exception {
		/* Initialize cluster utils needed */
		clusterBasedUtils = new ClusterBasedUtils(instanceSet);
		int preprocessorID = parameters.getPreprocessorID();
		
		switch (preprocessorID) {
			/* Original */
			case PreprocessorIDs.ORIGINAL:
				original = new Original(instanceSet, parameters);
				numBatchIterations.add(original.getNumBatchIterations());
				preprocessorList.add(original);
				break;
	
			/* Oversampling */
			case PreprocessorIDs.OVERSAMPLING:
				oversampling = new RandomOversampling(instanceSet, parameters);
				numBatchIterations.add(oversampling.getNumBatchIterations());
				preprocessorList.add(oversampling);
				break;
			
			/* Simplesampling */
			case PreprocessorIDs.SIMPLESAMPLING:
				simplesampling = new Simplesampling(instanceSet, parameters);
				numBatchIterations.add(simplesampling.getNumBatchIterations());
				preprocessorList.add(simplesampling);
				break;
			
			/* Smote */
			case PreprocessorIDs.SMOTE:
				smote = new Smote(instanceSet, parameters);
				numBatchIterations.add(smote.getNumBatchIterations());
				preprocessorList.add(smote);
				break;
			
			/* Cluster Based Oversampling */
			case PreprocessorIDs.CLUSTER_BASED_OVERSAMPLING:
				cbo = new ClusterBasedOversampling(clusterBasedUtils,
						instanceSet, parameters);
				numBatchIterations.add(cbo.getNumBatchIterations());
				preprocessorList.add(cbo);
				break;
			
			/* Cluster Based Smote */
			case PreprocessorIDs.CLUSTER_BASED_SMOTE:
				cbs = new ClusterBasedSmote(clusterBasedUtils, instanceSet,
						parameters);
				numBatchIterations.add(cbs.getNumBatchIterations());
				preprocessorList.add(cbs);
				break;
			
			/* Cclear */
			case PreprocessorIDs.CCLEAR:
				cClear = new Cclear(clusterBasedUtils, instanceSet, parameters);
				numBatchIterations.add(cClear.getNumBatchIterations());
				preprocessorList.add(cClear);
				break;
			
			/* Baseline */
			case PreprocessorIDs.BASELINE:
				baseline = new Baseline(instanceSet, parameters);
				numBatchIterations.add(baseline.getNumBatchIterations());
				preprocessorList.add(baseline);
				break;
		}
	}

	private void computeNumBatchIterations() {
		totalNumBatchIterations = 0;
		for (int i = 0; i < numBatchIterations.size(); i++) {
			totalNumBatchIterations += numBatchIterations.get(i);
		}
	}

	public int[] getNextBatchID() throws Exception {
		++currentBatchIterationID;

		int maxBatchIterations = numBatchIterations.get(currentPreprocessorID);
		if (currentBatchIterationID >= maxBatchIterations) {
			currentBatchIterationID = 0;
			++currentPreprocessorID;
		}
		int[] batchID = {currentPreprocessorID, currentBatchIterationID};
		
		return batchID;
	}
		
	public void applyPreprocessor(InstanceSet instanceSet, int[] batchID)
	throws Exception {
		if (batchID == null) {
			batchID = getNextBatchID();
		}
		this.currentPreprocessorID = batchID[0];
		this.currentBatchIterationID = batchID[1];

		if (currentBatchIterationID == 0) {
			System.out.print((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));
			System.out.println(": preprocessorID: " + currentPreprocessorID);
		}
		
		Batch preprocessor = preprocessorList.get(currentPreprocessorID);
		preprocessor.getNextBatchIteration(instanceSet);
	}

	public String getPreprocessorName() {
		return getPreprocessorName(currentPreprocessorID);
	}

	public String getPreprocessorName(int preprocessorID) {
		Batch currentPreprocessor = preprocessorList.get(preprocessorID);
		return currentPreprocessor.getPreprocessorName();
	}

	public String getPreprocessorStringParameters() {
		Batch currentPreprocessor = preprocessorList.get(currentPreprocessorID);
		return currentPreprocessor.parametersToString(currentBatchIterationID);
	}

	public int getTotalNumBatchIterations() {
		return totalNumBatchIterations;
	}

	public int getNumPreprocessors() {
		return preprocessorOrder.size();
	}

	public int[] getCurrentBatchID() {
		int[] preprocessorID = {currentPreprocessorID, currentBatchIterationID};
		return preprocessorID;
	}

	public int getPreprocessorID() {
		return currentPreprocessorID;
	}

}
