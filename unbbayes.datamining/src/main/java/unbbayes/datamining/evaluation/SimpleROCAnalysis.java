package unbbayes.datamining.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class is responsible for reading a txt file with three columns and 
 * generating the ROC points from it. The first two are probability estimation 
 * of a positive classification (binary classifier) and the third one 
 * is the true class.
 * 
 * There will be two sets of ROC points, one for each of the first columns.
 * 
 * @author Rommel Novaes Carvalho (rommel.carvalho@gmail.com)
 *
 */
public class SimpleROCAnalysis {

	/**
	 * @param aFileName
	 *            full name of an existing, readable file.
	 */
	public SimpleROCAnalysis(String aFileName) {
		fFile = new File(aFileName);
	}

	/** Template method that calls {@link #processLine(String)}. */
	public final void processLineByLine() throws FileNotFoundException {
		Scanner scanner = new Scanner(fFile);
		try {
			// first use a Scanner to get each line
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
		} finally {
			// ensure the underlying stream is always closed
			scanner.close();
		}
	}

	/**
	 * 
	 */
	protected void processLine(String aLine) {
		// use a second Scanner to parse the content of each line
		Scanner scanner = new Scanner(aLine);
		scanner.useDelimiter("	");
		if (scanner.hasNext()) {
			probs1.add(Float.parseFloat(scanner.next()));
			probs2.add(Float.parseFloat(scanner.next()));
			groundTruth.add(Integer.parseInt(scanner.next()));
		} else {
			System.out.println("Empty or invalid line. Unable to process.");
		}
		// (no need for finally here, since String is source)
		scanner.close();
	}
	
	/**
	 * Assuming the probabilities loaded were in ascending order.
	 */
	public void computeROCPoints() {
		rocPoints1 = computeROCPoints(probs1, groundTruth);
		rocPoints2 = computeROCPoints(probs2, groundTruth);
		
		System.out.println("ROC Points 1");
		for (int i = 0; i < rocPoints1.length; i++) {
			System.out.println(rocPoints1[i][0] + ", " + rocPoints1[i][1]);
		}
		
		System.out.println();
		System.out.println();
		System.out.println();
		
		System.out.println("ROC Points 2");
		for (int i = 0; i < rocPoints2.length; i++) {
			System.out.println(rocPoints2[i][0] + ", " + rocPoints2[i][1]);
		}
	}
	
	protected float[][] computeROCPoints(List<Float> probs, List<Integer> groundTruth) {
		float[][] rocPoints = new float[groundTruth.size()][2];
		
		// tpr = tp / (tp + fn)
		float tpr = 0;
		int tp = 0;
		int fn = 0;
		// fpr = fp / (fp + tn)
		float fpr = 0;
		int fp = 0;
		int tn = 0;
		// tpr at 0 and fpr at 1
		float[] point = new float[2];
		// i represents the threshold to consider a positive class
		for (int i = 0; i < groundTruth.size(); i++) {
			tp = 0;
			fn = 0;
			fp = 0;
			tn = 0;
			point = new float[2];
			for (int j = 0; j < groundTruth.size(); j++) {
				// Classified as negative - prob at j < prob at i
				if (probs.get(j) < probs.get(i)) {
					if (groundTruth.get(j) == -1) {
						tn++;
					} else {
						fn++;
					}
				// Classified as positive - prob at j >= prob at i
				} else {
					if (groundTruth.get(j) == 1) {
						tp++;
					} else {
						fp++;
					}
				}
			}
			tpr = (float)tp / (tp + fn);
			fpr = (float)fp / (fp + tn);
			point[0] = tpr;
			point[1] = fpr;
			rocPoints[i] = point;
		}
		
		return rocPoints;
	}

	// PRIVATE //
	private final File fFile;
	private List<Float> probs1 = new ArrayList<Float>();
	private float[][] rocPoints1;
	private List<Float> probs2 = new ArrayList<Float>();
	private float[][] rocPoints2;
	private List<Integer> groundTruth = new ArrayList<Integer>();
	
	public static void main(String[] args) throws FileNotFoundException {
		SimpleROCAnalysis roc = new SimpleROCAnalysis("src/main/resources/pclass1.txt");
		roc.processLineByLine();
		roc.computeROCPoints();
	}

}
