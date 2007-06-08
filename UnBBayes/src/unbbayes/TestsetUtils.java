package unbbayes;

import java.io.File;
import java.io.IOException;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.classifiers.decisiontree.C45;
import unbbayes.datamining.datamanipulation.ArffLoader;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Loader;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.evaluation.Evaluation;
import unbbayes.datamining.evaluation.Misclassify;
import unbbayes.datamining.evaluation.Samplings;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.Cclear;
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
	private boolean simplesampling;
	
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

	/** Number of clusters desired for the numeric clusterization */ 
	private int k;
	
	/** 
	 * The minimum accepted % of change in each iteration in the numeric
	 * clusterization.
	 */
	private double kError = 1.001f;
	
	private float learningRate;
	
	private float momentum;
	private float similarityThreshold;
	private boolean useClusterDistribution;
	private float thresholdFactor;
	private Misclassify misclassify;
	private float maxUnderLimit;
	private float cleanByDistThreshold;
	private int instanceSetType;
	private int ratioStart;
	private int ratioEnd;
	private int kStart;
	private int kEnd;
	
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

	public void printHeader(int sampleID) {
//		header = Samplings.getSamplingName(sampleID);

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
		
//		results = sensitivity + "\t";
//		results += specificity + "\t";
//		results += SE + "\t";
		
		results = eval.correct() + "\t";
		results += eval.incorrect() + "\t";
		results += 0 + "\t";
		
		return results;
	}


	
	/****************** Auxiliary methods **********************************/

	public static InstanceSet openFile(String fileName, int counterIndex, int classIndex) throws IOException {
		File file = new File(fileName);
		Loader loader = null;
		
		if (fileName.regionMatches(true, fileName.length() - 5, ".arff", 0, 5)) {
        	loader = new ArffLoader(file, -1);
        } else if (fileName.regionMatches(true, fileName.length() - 4, ".txt", 0, 4)) {
        	loader = new TxtLoader(file, -1);
        }

		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		loader.setClassIndex(classIndex);

		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstanceSet();

			return instanceSet;
		}
		
		return null;
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
	public void setPositiveClass(int positiveClass) {
		this.positiveClass = positiveClass;
		negativeClass = Math.abs(1 - positiveClass);
	}

	public void setConfidenceLevel(float value) {
		Options.getInstance().setConfidenceLevel(value);
	}

	public void setIfUsingPrunning(boolean value) {
		Options.getInstance().setIfUsingPrunning(value);
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getPositiveClass() {
		return positiveClass;
	}

	public int getNegativeClass() {
		return negativeClass;
	}

	public Smote getSmote() {
		return smote;
	}

	public int getK() {
		return k;
	}

	public float getLearningRate() {
		return learningRate;
	}

	public float getMomentum() {
		return momentum;
	}

	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}

	public void setMomentum(float momentum) {
		this.momentum = momentum;
	}

	/**
	 * @param simplesampling the simplesampling to set
	 */
	public void setSimplesampling(boolean simplesampling) {
		this.simplesampling = simplesampling;
	}

	/**
	 * @return the simplesampling
	 */
	public boolean isSimplesampling() {
		return simplesampling;
	}

	/**
	 * @param kError the kError to set
	 */
	public void setKError(double kError) {
		this.kError = kError;
	}

	/**
	 * @return the kError
	 */
	public double getKError() {
		return kError;
	}

	public float getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(float similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

	public boolean useClusterDistribution() {
		return useClusterDistribution;
	}

	public void setUseClusterDistribution(boolean useClusterDistribution) {
		this.useClusterDistribution = useClusterDistribution;
	}

	public float getThresholdFactor() {
		return thresholdFactor;
	}

	public void setThresholdFactor(float thresholdFactor) {
		this.thresholdFactor = thresholdFactor;
	}

	public void setMisclassify(Misclassify misclassify) {
		this.misclassify = misclassify;
	}

	public Misclassify getMisclassify() {
		return misclassify;
	}

	public float maxUnderLimit() {
		return maxUnderLimit;
	}

	public void setMaxUnderLimit(float maxUnderLimit) {
		this.maxUnderLimit = maxUnderLimit;
	}

	public float getCleanByDistThreshold() {
		return cleanByDistThreshold;
	}

	public void setCleanByDistThreshold(float cleanByDistThreshold) {
		this.cleanByDistThreshold = cleanByDistThreshold;
	}

	public int getInstanceSetType() {
		return instanceSetType;
	}

	public void setInstanceSetType(int instanceSetType) {
		this.instanceSetType = instanceSetType;
	}

	public int getRatioStart() {
		return ratioStart;
	}

	public void setRatioStart(int ratioStart) {
		this.ratioStart = ratioStart;
	}

	public int getRatioEnd() {
		return ratioEnd;
	}

	public void setRatioEnd(int ratioEnd) {
		this.ratioEnd = ratioEnd;
	}

	public int getKstart() {
		return kStart;
	}

	public void setKstart(int kStart) {
		this.kStart = kStart;
	}

	public int getKend() {
		return kEnd;
	}

	public void setKend(int kEnd) {
		this.kEnd = kEnd;
	}

}