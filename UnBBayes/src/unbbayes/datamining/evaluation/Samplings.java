package unbbayes.datamining.evaluation;

import unbbayes.TestsetUtils;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.preprocessor.imbalanceddataset.Cclear;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class Samplings {
	
	private int numSamplings;
	
	private float[] originalDist;
	
	private int positiveClass;

	private int negativeClass;

	private boolean simplesampling;

	private ClusterBasedSmote cbs;

	private Cclear cClear;
	
	private Smote smote;

	private int samplingID;

	private String[] samplingName;

	private int[] samplingOrder;
	
	
	/****** Sampling IDs ******/
	private int original = 0;
	private int oversamplingOnly = 1;
	private int smoteOnly = 2;
	private int clusterBasedOversampling = 3;
	private int clusterBasedSmote = 4;
	private int cPlusClear = 5;
	private int cMinusClear = 6;
	private int baseline = 7;
	/****************************************/
	
	private String[] samplingStrategiesNames;

	private TestsetUtils testsetUtils;


	public Samplings(InstanceSet instanceSet, float[] originalDist,
			TestsetUtils testsetUtils)
	throws Exception {
		this.originalDist = originalDist;
		this.testsetUtils = testsetUtils;
		smote = testsetUtils.getSmote();
		simplesampling = testsetUtils.isSimplesampling();
		positiveClass = testsetUtils.getPositiveClass();
		negativeClass = testsetUtils.getNegativeClass();

		smote.setInstanceSet(instanceSet);
		
		cClear = new Cclear(instanceSet, testsetUtils);

		cbs = new ClusterBasedSmote(instanceSet, testsetUtils);
		cbs.setOptionDiscretize(false);
		cbs.setOptionDistanceFunction((byte) 1);
		cbs.setOptionFixedGap(true);
		cbs.setOptionNominal((byte) 0);

		subscribeSamplingStrategies();
		samplingID = 0;
	}
	
	private void subscribeSamplingStrategies() {
		int[] samplingOrder = {
//				original,
//				oversamplingOnly,
				smoteOnly,
//				clusterBasedOversampling,
//				clusterBasedSmote,
//				cPlusClear,
//				cMinusClear,
//				baseline,
		};
		this.samplingOrder = samplingOrder;
		
		/* Sampling names */
		String[] samplingStrategiesNames = {
				"Original",
				"Oversampling",
				"Smote",
				"Cluster Based Oversampling",
				"Cluster Based Smote",
				"C+clear",
				"C-clear",
				"Baseline",
		};
		
		this.samplingStrategiesNames = samplingStrategiesNames;
		
		numSamplings = samplingOrder.length;
		samplingName = new String[numSamplings];
	}
	
	public int buildSample(InstanceSet instanceSet, int samplingID, int ratio)
	throws Exception {
		this.samplingID = samplingID;
		
		/*
		 * 0: no cluster and no ratio;
		 * 1: no cluster;
		 * 2: cluster and ratio
		 */
		int samplingType = 0;
		
		/* Get current class distribution  - Two class problem */
		float proportion = originalDist[negativeClass] * (float) ratio;
		proportion /= originalDist[positiveClass] * (float) (10 - ratio);

		
		/* Original */
		if (querySampling(original)) {
			/* No sampling required */
			samplingType = 0;
		} else 
		
			
		/* Oversampling */
		if (querySampling(oversamplingOnly)) {
			if (simplesampling) {
				Sampling.simplesampling(instanceSet, proportion, positiveClass, false);
			} else {
				Sampling.oversampling(instanceSet, proportion, positiveClass);
			}
			samplingType = 1;
		} else

			
		/* Smote */
		if (querySampling(smoteOnly)) {
//			buildNN();
			smote.setInstanceSet(instanceSet);
//			smote.run(instanceSet, positiveClass, proportion);
			
//			smote.buildNN(5, 1);
//			smote.run(instanceSet, 1, 1.3);
			
//			smote.buildNN(5, 3);
//			smote.run(instanceSet, 3, 5);
//			
//			smote.buildNN(5, 4);
//			smote.run(instanceSet, 4, 3);
			
//			Sampling.undersampling(instanceSet, 0.2, 0, true);
			
//			testsetUtils.setNegativeClass(0);
//			cbs.runUndersampling(instanceSet, 0.2, false);
			
//			Sampling.undersampling(instanceSet, 0.8, 1, true);
			
//			Sampling.undersampling(instanceSet, 0.9, 2, true);
			
			
			samplingType = 1;
		} else
		
			
		/* Cluster-Based Oversampling */
		if (querySampling(clusterBasedOversampling)) {
			boolean overMajority = true;
			boolean cbo = true;
			cbs.run(instanceSet, overMajority, cbo, ratio);
			samplingType = 2;
		} else

		
		/* Cluster-Based Smote */
		if (querySampling(clusterBasedSmote)) {
			buildNN();
			boolean overMajority = true;
			boolean cbo = false;
			cbs.run(instanceSet, overMajority, cbo, ratio);
			samplingType = 2;
		} else

		
		/* C+clear */
		if (querySampling(cPlusClear)) {
			buildNN();
			boolean clean = false;
			cClear.run(instanceSet, ratio, clean);
			samplingType = 2;
		} else


		/* C-clear */
		if (querySampling(cMinusClear)) {
			buildNN();
			boolean clean = true;
			cClear.run(instanceSet, ratio, clean);
			samplingType = 2;
		}
		
		/* Baseline */
		if (querySampling(baseline)) {
			int limit = testsetUtils.getBaselineLimit();
			Sampling.limitWeight(instanceSet, limit);
			samplingType = 0;
		}
		
		return samplingType;
	}
	
	private boolean querySampling(int samplingStrategyID) {
		/* Choose sampling technique */
		int pos = -1;
		for (int i = 0; i < numSamplings; i++) {
			if (samplingStrategyID == samplingOrder[i]) {
				pos = i;
			}
		}
		if (samplingID == pos) {
			samplingName[samplingID] = 
				samplingStrategiesNames[samplingStrategyID];
			return true;
		}
			
		return false;
	}
	
	public String getSamplingName(int samplingID) {
		return samplingName[samplingID];
	}

	/** 
	 * Stores the type of this instanceSet:
	 * 
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Mixed
	 * (-1) - Any
	 */
	public int getSamplingType(int samplingID) {
		switch (samplingID) {
			case 0:
				return InstanceSet.NUMERIC;
			case 1:
				return InstanceSet.NOMINAL;
			case 2:
				return InstanceSet.MIXED;
		}
		return -1;
	}

	public void setNumSamplings(int numSamplings) {
		this.numSamplings = numSamplings;
	}

	public int getNumSamplings() {
		return numSamplings;
	}

	private void buildNN() throws Exception {
		if (!smote.isNearestNeighborsIDsBuilt()) {
			smote.buildNN(5, positiveClass);
		}
	}
	
//	private void clusterizeByClass() throws Exception {
//		if (!cbs.isNearestNeighborsIDsBuilt()) {
//			cbs.clusterizeByClass();
//		}
//	}
	
	}