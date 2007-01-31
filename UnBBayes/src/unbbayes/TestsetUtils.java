package unbbayes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.evaluation.CrossValidation;
import unbbayes.datamining.evaluation.Evaluation;
import unbbayes.datamining.evaluation.ROCAnalysis;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 17/01/2007
 */
public class TestsetUtils {
	float maxSE = 0;
	String maxSEHeader;
	String header = "";
	Smote smote;
	boolean simplesampling = false;
	
	/**
	 * Set it <code>true</code> to optionDiscretize the synthetic value created for
	 * the new instance. 
	 */
	private boolean optionDiscretize = false;
	
	/**
	 * Used in SMOTE
	 * 0: copy nominal attributes from the current instance
	 * 1: copy from the nearest neighbors
	 */
	private byte optionNominal = 0;
	
	/**
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
	 */
	boolean optionFixedGap = false;
	
	/**
	 * Distance function
	 * 0: Hamming
	 * 1: HVDM
	 */
	byte optionDistanceFunction = 1;
	
	/**
	 * The maximum order of combinations for CNM  
	 */
	int maxOrderCNM = 3;
	
	private int positiveClass;
	
	private int negativeClass;
	
	public TestsetUtils() {
		smote = new Smote(null);
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
	}
	
	public String classifyEvaluate(InstanceSet trainData, InstanceSet testData,
			float[] newDist) throws Exception {
		Classifier classifier;
		boolean relativeProb;
		String results;
		
		/************** Naive Bayes **************/
		/* Build model */
		classifier = new NaiveBayes();
		relativeProb = false;
		classifier.buildClassifier(trainData);
		
		/* Evaluate model */
		relativeProb = false;
		results = evaluate(classifier, testData, relativeProb, newDist);
		
		/************** Naive Bayes Relative Prob **************/
		/* Build model */
		classifier = new NaiveBayes();
		relativeProb = true;
		classifier.buildClassifier(trainData);
		
		/* Evaluate model */
		relativeProb = true;
		results += evaluate(classifier, testData, relativeProb, newDist);
		

		/************** C4.5 **************/
		/* Build model */
		classifier = new C45();
		classifier.buildClassifier(trainData);
		
		/* Evaluate model */
		results += evaluate(classifier, testData, relativeProb, newDist);
		

//		/************** CNM **************/
//		/* Build model */
//		classifier = new CombinatorialNeuralModel(maxOrderCNM);
//		classifier.buildClassifier(trainData);
//		
//		/* Evaluate model */
//		results += evaluate(classifier, testData, relativeProb, i, newDist);
		
		return results;
	}

	public InstanceSet sample(InstanceSet trainData, int sampleID, int i,
			float[] originalDist)
	throws Exception {
		/* Get current class distribution  - Two class problem */
		float proportion = originalDist[negativeClass] * (float) i;
		proportion = proportion / (originalDist[positiveClass] * (float) (10 - i));
		ClusterBasedSmote cbs;
		if (proportion < 1 && i != -1) {
			return null;
		}
		
		switch (sampleID) {
			case 0:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(trainData, (float) (1 / proportion),
							negativeClass, true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(trainData, (float) (1 / proportion),
							negativeClass, true);
				}

				break;
			case 1:
				/* Samples the data up */
				if (simplesampling) {
					/* Simplesampling over */
					Sampling.simplesampling(trainData, proportion,
							positiveClass, false);
				} else {
					/* Random oversampling */
					Sampling.oversampling(trainData, proportion, positiveClass);
				}
				
				break;
			case 2:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(trainData,
							Math.sqrt((float) (1 / proportion)), negativeClass,
							true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(trainData,
							Math.sqrt((float) (1 / proportion)), negativeClass, true);
				}

				/* Samples the data up */
				if (simplesampling) {
					/* Simplesampling over */
					Sampling.simplesampling(trainData,
							Math.sqrt(proportion), positiveClass, true);
				} else {
					/* Random oversampling */
					Sampling.oversampling(trainData, Math.sqrt(proportion),
							positiveClass);
				}

				break;
			case 3:
//				/* Samples the data down */
//				if (simplesampling) {
//					/* Simplesampling down */
//					Sampling.simplesampling(trainData,
//							Math.sqrt((float) (1 / proportion)), negativeClass,
//							true);
//				} else {
//					/* Random undersampling */
//					Sampling.undersampling(trainData,
//							Math.sqrt((float) (1 / proportion)), negativeClass,
//							true);
//				}

				/* Smote */
				smote.setInstanceSet(trainData);
				smote.buildNN(5, positiveClass);
				smote.run(positiveClass, (float) Math.sqrt(proportion));

				break;
			case 4:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				}

				/* Cluster-Based SMOTE */
				cbs = new ClusterBasedSmote(trainData);
				cbs.setOptionDiscretize(false);
				cbs.setOptionDistanceFunction((byte) 1);
				cbs.setOptionFixedGap(false);
				cbs.setOptionNominal((byte) 0);
//				cbs.runUndersampling(negativeClass,
//						Math.sqrt((float) (1 / proportion)), simplesampling);
				cbs.runOversampling(positiveClass, Math.sqrt(proportion));

				break;
			case 5:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				}

				/* Cluster-Based SMOTE */
				cbs = new ClusterBasedSmote(trainData);
				cbs.setOptionDiscretize(false);
				cbs.setOptionDistanceFunction((byte) 1);
				cbs.setOptionFixedGap(true);
				cbs.setOptionNominal((byte) 0);
				cbs.run(negativeClass, false, false);

				break;
				
			case 6:
				/* Samples the data down */
				if (simplesampling) {
					/* Simplesampling down */
					Sampling.simplesampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				} else {
					/* Random undersampling */
					Sampling.undersampling(trainData,
							(float) Math.sqrt((float) (1 / proportion)),
							negativeClass, true);
				}

				/* Cluster-Based SMOTE */
				cbs = new ClusterBasedSmote(trainData);
				cbs.setOptionDiscretize(false);
				cbs.setOptionDistanceFunction((byte) 1);
				cbs.setOptionFixedGap(true);
				cbs.setOptionNominal((byte) 0);
				cbs.run(negativeClass, false, true);

				break;
				
			default:
				return trainData;
		}
		
		return trainData;
	}
	
	public String getSampleName(int sampleID) {
		String result = null;
		
		/* Sampling strategy */
		if (sampleID == 0) {
			result = "Undersampling";
		} else if (sampleID == 1) {
			result = "Oversampling";
		} else if (sampleID == 2) {
			result = "Undersampling with Oversampling";
		} else if (sampleID == 3) {
			result = "SMOTE with Undersampling";
		} else if (sampleID == 4) {
			result = "Cluster-Based SMOTE (modified) with Undersampling";
		} else if (sampleID == 5) {
			result = "Cluster-Based SMOTE with Undersampling";
		} else if (sampleID == 6) {
			result = "Cluster-Based Oversampling with Undersampling";
		} else {
			result = "Original";
		}
		
		return result;
	}
	
	public void printHeader(int sampleID) {
		header = getSampleName(sampleID);

		System.out.print("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println("");
		System.out.println(header);
		System.out.println("");
		System.out.println("			NB			NBRP			C4.5");
		System.out.println("% de fraude	S	E	SE	S	E	SE	S	E	SE");
	}

	public String evaluate(Classifier classifier, InstanceSet testData,
			boolean relativeProb, float[] dist) throws Exception {
		String results;
		
		float percentage = (float) dist[positiveClass] / (dist[0] + dist[1]);
		percentage = (100 * percentage);
		
		if (classifier instanceof DistributionClassifier) {
			((DistributionClassifier) classifier).setOriginalDistribution(dist);
			if (relativeProb) {
				((DistributionClassifier)classifier).setRelativeClassification();
			} else {
				((DistributionClassifier)classifier).setNormalClassification();
			}
		}

		/* Evaluate the model */
		Evaluation eval = new Evaluation(testData, classifier);
		eval.evaluateModel(classifier, testData);
		
		/* Print out the SE */
		float sensitivity = (float) eval.getSensitivity(1) * 1000;
		sensitivity = (int) sensitivity;
		sensitivity = sensitivity / 1000;
		float specificity = (float) eval.getSpecificity(1) * 1000;
		specificity = (int) specificity;
		specificity = specificity / 1000;
		float SE = sensitivity * specificity * 1000;
		SE = (int) SE;
		SE = SE / 1000;
		
		if (SE > maxSE) {
			maxSE = SE;
			maxSEHeader = header + "\nQuantity of fraud: "  + (int) percentage + "%";
		}
		
		results = sensitivity + "\t";
		results += specificity + "\t";
		results += SE + "\t";
		
		return results;
	}

	public void generateRocPoints(InstanceSet instanceSet,
			InstanceSet trainData, InstanceSet testData, float[] distribution,
			boolean cross, int sampleID, int i, int numFolds, String outputFileName,
			String ext)
	throws Exception {
		float percentage = (float) distribution[positiveClass];
		percentage /= (distribution[0] + distribution[1]);
		percentage *= 100;

		Classifier classifier;
		
		/************** Naive Bayes **************/
		/* Build model */
		classifier = new NaiveBayes();
		classifier.buildClassifier(trainData);
		
		/* Run roc */
		outputFileName = outputFileName + getSampleName(sampleID) +
			"NaiveBayes" + (int) percentage + ext;
		runROC(instanceSet, classifier, testData, distribution, outputFileName,
				numFolds, cross, sampleID, i);
		

//		/************** C4.5 **************/
//		/* Build model */
//		classifier = new C45();
//		classifier.buildClassifier(trainData);
//		
//		/* Run roc */
//		outputFileName = outputFileName + getSampleName(sampleID) +
//			"C4.5" + (int) percentage + ext;
//		runROC(instanceSet, classifier, testData, distribution, cross,
//				outputFileName, numFolds);
		

//		/************** CNM **************/
//		/* Build model */
//		classifier = new CombinatorialNeuralModel(maxOrderCNM);
//		classifier.buildClassifier(trainData);
//		
//		/* Run roc */
//		outputFileName = outputFileName + getSampleName(sampleID) +
//			"Cnm" + (int) percentage + ext;
//		runROC(instanceSet, classifier, testData, distribution, cross,
//				outputFileName, numFolds);
	}

	private void runROC(InstanceSet instanceSet, Classifier classifier,
			InstanceSet testData, float[] distribution, String outputFileName,
			int numFolds, boolean cross, int sampleID, int i)
	throws Exception {
		if (classifier instanceof DistributionClassifier) {
			((DistributionClassifier)classifier).setNormalClassification();
			((DistributionClassifier) classifier).setOriginalDistribution(
					distribution);
		}

		float[][] rocPoints;
		float[] probs;
		
		if (cross) {
			ArrayList<Object> aux;
			aux = CrossValidation.getEvaluatedProbabilities(classifier,
					instanceSet, positiveClass, numFolds, sampleID, i,
					this);
			probs = (float[]) aux.get(0);
			distribution = (float[]) aux.get(1);		
			testData = instanceSet;
		} else {
			probs = Evaluation.getEvaluatedProbabilities(classifier, testData,
					positiveClass);
		}
		
		rocPoints = ROCAnalysis.computeROCPoints(probs, testData, positiveClass);
		saveRocPoints(rocPoints, outputFileName);
	}
	
	public static void saveRocPoints(float[][] rocPoints,
			String rocPointsFileName) throws Exception {
		File output = new File(rocPointsFileName);
		PrintWriter writer = new PrintWriter(new FileWriter(output), true);
		int numRocPoints = rocPoints.length;
		writer.println("FP\tTP");
		for (int i = 0; i < numRocPoints; i++) {
			writer.println(rocPoints[i][0] + "\t" + rocPoints[i][1]);
		}
		writer.flush();
		writer.close();
	}
	
	public ArrayList<Object> generateAUCValues(InstanceSet trainData,
			InstanceSet testData, float[] distribution, int numFolds,
			int sampleID, int i, boolean cross)
	throws Exception {
		Classifier classifier;
		
		ArrayList<Object> allResults = new ArrayList<Object>();
		ArrayList<Object> rocResults = new ArrayList<Object>();
		
		double auc;
		
		String[][] aucResults = new String[1][2];
		
		/************** Naive Bayes **************/
		/* Build model */
		classifier = new NaiveBayes();
		if (!cross) {
			classifier.buildClassifier(trainData);
		}
		
		/* Run roc */
		ArrayList<Object> results;
		results = runAUC(trainData, testData, classifier, numFolds, sampleID,
				i, cross);
		auc = (Double) results.get(0);
		distribution = (float[]) results.get(2);
		
		if (distribution == null) {
			distribution = distribution(trainData);
		}
		
		if (auc == -1) {
			return null;
		}

		float percentage = (float) distribution[positiveClass];
		percentage /= (distribution[0] + distribution[1]);
		percentage *= 100;

		aucResults[0][0] = getSampleName(sampleID) + "NaiveBayes" + Math.round(percentage);
		aucResults[0][1] = "" + auc;
		
		/* Add roc results */
		rocResults.add(aucResults[0][0]);
		rocResults.add(results.get(1));
		
//		/************** C4.5 **************/
//		/* Build model */
//		classifier = new C45();
//		classifier.buildClassifier(trainData);
//		
//		/* Run roc */
//		outputFileName = outputFileName + getSampleName(sampleID) +
//			"C4.5" + (int) percentage + ext;
//		auc = runROC(instanceSet, classifier, testData, distribution, cross,
//				outputFileName, numFolds);
		

//		/************** CNM **************/
//		/* Build model */
//		classifier = new CombinatorialNeuralModel(maxOrderCNM);
//		classifier.buildClassifier(trainData);
//		
//		/* Run roc */
//		outputFileName = outputFileName + getSampleName(sampleID) +
//			"Cnm" + (int) percentage + ext;
//		auc = runROC(instanceSet, classifier, testData, distribution, cross,
//				outputFileName, numFolds);

		allResults.add(aucResults);
		allResults.add(rocResults);
		
		return allResults;
	}

	private ArrayList<Object> runAUC(InstanceSet instanceSet, InstanceSet testData,
			Classifier classifier, int numFolds,
			int sampleID, int i, boolean cross)
	throws Exception {
		float[] probs;
		float[] distribution = null;
		double auc;
		
		if (cross) {
			ArrayList<Object> aux;
			aux = CrossValidation.getEvaluatedProbabilities(classifier,
					instanceSet, positiveClass, numFolds, sampleID, i,
					this);
			probs = (float[]) aux.get(0);
			distribution = (float[]) aux.get(1);		
			if (probs == null) {
				return null;
			}
			testData = instanceSet;
		} else {
			if (classifier instanceof DistributionClassifier) {
				((DistributionClassifier)classifier).setNormalClassification();
				((DistributionClassifier) classifier).setOriginalDistribution(
						distribution(instanceSet));
			}
			probs = Evaluation.getEvaluatedProbabilities(classifier, testData,
					positiveClass);
		}
		
		float[][] rocPoints;
		rocPoints = ROCAnalysis.computeROCPoints(probs, testData, positiveClass);

		auc = ROCAnalysis.computeAUC(probs, testData, positiveClass);
		
		ArrayList<Object> results = new ArrayList<Object>(2);
		results.add(auc);
		results.add(rocPoints);
		results.add(distribution);
		
		return results;
	}
	
	
	/** Auxiliary methods ***********************************/
	
	public static InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		Loader loader = null;
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
        	loader = new ArffLoader(file, -1);
        } else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
        	loader = new TxtLoader(file, -1);
        }

		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstanceSet();

			return instanceSet;
		}
		
		return null;
	}

	public static float[] distribution(InstanceSet trainData) {
		int numInstances = trainData.numInstances();
		int numClasses = trainData.numClasses();
		int classIndex = trainData.classIndex;
		int counterIndex = trainData.counterIndex;
		float distribution[] = new float[numClasses];
		int classValue;
		
		for (int i = 0; i < numClasses; i++) {
			distribution[i] = 0;
		}
		
		for (int i = 0; i < numInstances; i++) {
			classValue = (int) trainData.instances[i].data[classIndex];
			distribution[classValue] += trainData.instances[i].data[counterIndex];
		}

		return distribution;
	}

	/**
	 * @param optionDiscretize the optionDiscretize to set
	 */
	public void setOptionDiscretize(boolean optionDiscretize) {
		this.optionDiscretize = optionDiscretize;
	}

	/**
	 * @return the optionDiscretize
	 */
	public boolean isOptionDiscretize() {
		return optionDiscretize;
	}

	/**
	 * @param optionNominal the optionNominal to set
	 */
	public void setOptionNominal(byte optionNominal) {
		this.optionNominal = optionNominal;
	}

	/**
	 * @return the optionNominal
	 */
	public byte getOptionNominal() {
		return optionNominal;
	}

	/**
	 * @param positiveClass the positiveClass to set
	 */
	public void setInterestingClass(int interestingClass) {
		positiveClass = interestingClass;
		negativeClass = 1 - positiveClass;
	}

}