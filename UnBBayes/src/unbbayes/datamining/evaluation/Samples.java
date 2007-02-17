package unbbayes.datamining.evaluation;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUndersampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUtils;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class Samples {
	
	private static int numSamples = 16;
	
	public static void sample(InstanceSet train, int sampleID, int i,
			float[] originalDist, int positiveClass, boolean simplesampling,
			int k, Smote smote, ClusterBasedSmote cbs,
			ClusterBasedUndersampling cbu)
	throws Exception {
		int negativeClass = Math.abs(1 - positiveClass);
		
		/* Get current class distribution  - Two class problem */
		float proportion = originalDist[negativeClass] * (float) i;
		proportion = proportion / (originalDist[positiveClass] * (float) (10 - i));
		
		float positiveRate;
		if (proportion < 1 && i != -1) {
			return;
		}
		
		switch (sampleID) {
			case 0:
				return;
				
			case 1:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(train, (float) (1 / proportion),
							negativeClass, true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(train, (float) (1 / proportion),
							negativeClass, true);
				}

				break;
			case 2:
				/* Samples the data up */
				if (simplesampling) {
					/* Simplesampling over */
					Sampling.simplesampling(train, proportion,
							positiveClass, false);
				} else {
					/* Random oversampling */
					Sampling.oversampling(train, proportion, positiveClass);
				}
				
				break;
//			case 3:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							Math.sqrt((float) (1 / proportion)), negativeClass,
//							true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							Math.sqrt((float) (1 / proportion)), negativeClass, true);
//				}
//
//				/* Samples the data up */
//				if (simplesampling) {
//					/* Simplesampling over */
//					Sampling.simplesampling(train,
//							Math.sqrt(proportion), positiveClass, true);
//				} else {
//					/* Random oversampling */
//					Sampling.oversampling(train, Math.sqrt(proportion),
//							positiveClass);
//				}
//
//				break;

			case 3:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							Math.sqrt((float) (1 / proportion)), negativeClass,
//							true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							Math.sqrt((float) (1 / proportion)), negativeClass,
//							true);
//				}

				/* SMOTE */
//				smote.run(positiveClass, Math.sqrt(proportion));
				smote.run(train, positiveClass, proportion);

				break;

			case 4:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				}

				/* Cluster-Based Oversampling (flattens clusters' distribution) */
				cbs.run(train, false, true);

				break;
				
			case 5:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				}

				/* Cluster-Based SMOTE (flattens clusters' distribution) */
				cbs.run(train, false, false);

				break;
				
			case 6:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				}

				/* Cluster-Based Oversampling modified (proportional to its clusters) */
//				cbs.runUndersampling(Math.sqrt((float) (1 / proportion)),
//						simplesampling);
//				cbs.runOversampling(Math.sqrt(proportion), false);
				cbs.runOversampling(train, proportion, false);

				break;
				
			case 7:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(train,
//							(float) Math.sqrt((float) (1 / proportion)),
//							negativeClass, true);
//				}

				/* Cluster-Based SMOTE modified (proportional to its clusters) */
//				cbs.runUndersampling(Math.sqrt((float) (1 / proportion)),
//						simplesampling);
//				cbs.runOversampling(Math.sqrt(proportion));
				cbs.runOversampling(train, proportion, true);

				break;
				
			case 8:
				/* over */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, false, false, false, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)
				break;
				
			case 9:
				/* Clean only */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, false, true, false, false);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 10:
				/* over and clean */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, false, true, false, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 11:
				/* under and over */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, false, false, true, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 12:
				/* under, over and clean */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, false, true, true, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 13:
				/* smote */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, true, false, false, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 14:
				/* smote and clean */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, true, true, false, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
				
			case 15:
				/* under, smote and clean */
				positiveRate = originalDist[positiveClass];
				positiveRate /= train.numWeightedInstances;
				cbu.run(train, positiveRate, true, true, true, true);
//				run(float positiveRate, boolean doSmote, boolean clean,
//				boolean under, boolean over, ClusterBasedUtils clustersInfo)

				break;
		}
		
		return;
	}
	
	public static String getSampleName(int sampleID) {
		String result = null;
		
		/* Sampling strategy */
		if (sampleID == 0) {
			result = "Original";
		} else if (sampleID == 1) {
			result = "Undersampling";
		} else if (sampleID == 2) {
			result = "Oversampling";
//		} else if (sampleID == 3) {
//			result = "Undersampling with Oversampling";
		} else if (sampleID == 3) {
			result = "SMOTE";
		} else if (sampleID == 4) {
			result = "Cluster-Based Oversampling (flattens clusters' distribution)";
		} else if (sampleID == 5) {
			result = "Cluster-Based SMOTE (flattens clusters' distribution)";
		} else if (sampleID == 6) {
			result = "Cluster-Based Oversampling modified (proportional to its clusters)";
		} else if (sampleID == 7) {
			result = "Cluster-Based SMOTE modified (proportional to its clusters)";
		} else if (sampleID == 8) {
			result = "Cluster-Based Undersampling (over)";
		} else if (sampleID == 9) {
			result = "Cluster-Based Undersampling (clean)";
		} else if (sampleID == 10) {
			result = "Cluster-Based Undersampling (over & clean)";
		} else if (sampleID == 11) {
			result = "Cluster-Based Undersampling (over & under)";
		} else if (sampleID == 12) {
			result = "Cluster-Based Undersampling (over, under & clean)";
		} else if (sampleID == 13) {
			result = "Cluster-Based Undersampling (smote)";
		} else if (sampleID == 14) {
			result = "Cluster-Based Undersampling (smote & clean)";
		} else if (sampleID == 15) {
			result = "Cluster-Based Undersampling (smote, under & over)";
		}
		
		return result;
	}

	public static int getNumSamples() {
		return numSamples;
	}
	
	/** 
	 * Stores the type of this instanceSet:
	 * 
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Mixed
	 * (-1) - Any
	 */
	public static int getSampleType(int sampleID) {
		switch (sampleID) {
			case 0:
				return InstanceSet.NUMERIC;
			case 1:
				return InstanceSet.NOMINAL;
			case 2:
				return InstanceSet.MIXED;
		}
		return -1;
	}
}