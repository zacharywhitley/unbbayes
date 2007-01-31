package unbbayes.datamining.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import unbbayes.TestsetUtils;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 18/01/2007
 */
public class CopyOfROCAnalysis {
	private static Random rnd;

	public static float[][] computeROCPoints(float[] probs, InstanceSet testData,
			int positiveClass) {	
		int numInstances = testData.numInstances();

		if (!testData.classIsNominal()) {
			/* Class is numeric */
			System.out.println("numeric class");
			return null;
		}
		
		int negativeClass = 1 - positiveClass;
		
		/* 
		 * Sort in descending way the probabilistic classifier's estimate
		 * that instance i is positive.
		 */
		int[] rocProbsIndexAux = Utils.sort(probs);
		int[] rocProbsIndex = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			rocProbsIndex[inst] = rocProbsIndexAux[numInstances - inst - 1];
		}
		
		float[] distribution = TestsetUtils.distribution(testData);
		float fp = 0;
		float tp = 0;
		float n = distribution[negativeClass];
		float p = distribution[positiveClass];
		float[][] rocPoints = new float[numInstances + 2][2];
		int classIndex = testData.classIndex;
		int counterIndex = testData.counterIndex;
		Instance instance;
		
		float lastProb = Float.MIN_VALUE;
		int counter = 0;
		for (int inst, i = 0; i < numInstances; i++) {
			inst = rocProbsIndex[i];
			
			/* Check if new roc point should be added or not */
			if (probs[inst] != lastProb) {
				/* Add new roc point */
				addRocPoint(fp, n, tp, p, rocPoints, counter);
				++counter;
				
				/* Update last probability read */
				lastProb = probs[inst];
			}
			/* 
			 * Increase TP or FP according to the class of the
			 * corresponding instance.
			 */ 
			instance = testData.instances[inst];
			if (instance.data[classIndex] == positiveClass) {
				tp += instance.data[counterIndex];
			} else {
				fp += instance.data[counterIndex];
			}
		}
		/* Add last roc point */
		addRocPoint(fp, n, tp, p, rocPoints, counter);
		++counter;
		
		float[][] rocPointsAux = new float[counter][2];
		for (int i = 0; i < counter; i++) {
			rocPointsAux[i] = rocPoints[i];
		}
		rocPoints = rocPointsAux;
		
		return rocPoints;
	}

	private static void addRocPoint(float fp, float n, float tp, float p,
			float[][] rocPoints, int pos) {
		/* Check if FP == N */
		if (fp == n) {
			rocPoints[pos][0] = 1;
		} else {
			rocPoints[pos][0] = fp / n;
		}
		
		/* Check if TP == P */
		if (tp == p) {
			rocPoints[pos][1] = 1;
		} else {
			rocPoints[pos][1] = tp / p;
		}
	}

	public static double computeAUC(float[] probs, InstanceSet testData,
			int positiveClass) {	
		int numInstances = testData.numInstances();

		if (!testData.classIsNominal()) {
			/* Class is numeric */
			System.out.println("numeric class");
			return 0;
		}
		
		int negativeClass = 1 - positiveClass;
		
		/* 
		 * Sort in descending way the probabilistic classifier's estimate
		 * that instance i is positive.
		 */
		int[] rocProbsIndexAux = Utils.sort(probs);
		int[] rocProbsIndex = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			rocProbsIndex[inst] = rocProbsIndexAux[numInstances - inst - 1];
		}
		
		float[] distribution = TestsetUtils.distribution(testData);
		float fp = 0;
		float tp = 0;
		float fpLast = 0;
		float tpLast = 0;
		float n = distribution[negativeClass];
		float p = distribution[positiveClass];
		int classIndex = testData.classIndex;
		int counterIndex = testData.counterIndex;
		Instance instance;
		
		double auc = 0;
		float lastProb = Float.MIN_VALUE;
		int counter = 0;
		for (int inst, i = 0; i < numInstances; i++) {
			inst = rocProbsIndex[i];
			
			/* Check if new roc point should be added or not */
			if (probs[inst] != lastProb) {
				/* Add new trapezoid area */
				auc += trapezoidArea(fp, fpLast, tp, tpLast);
				++counter;
				
				/* Update last probability read */
				lastProb = probs[inst];
				
				fpLast = fp;
				tpLast = tp;
			}
			
			/* 
			 * Increase TP or FP according to the class of the
			 * corresponding instance.
			 */ 
			instance = testData.instances[inst];
			if (instance.data[classIndex] == positiveClass) {
				tp += instance.data[counterIndex];
			} else {
				fp += instance.data[counterIndex];
			}
		}
		
		/* Add last trapezoid area */
		auc += trapezoidArea(fp, fpLast, tp, tpLast);
		++counter;
		
		/* Scale the AUC to the unit square */
		auc /= p * n;
		
		return auc;
	}

	private static double trapezoidArea(float fp, float fpLast, float tp,
			float tpLast) {
		double base = (double) (Math.abs(fp - fpLast));
		double height = (double) ((tp + tpLast) / 2);
		
		return base * height;
	}

	/**
	 * QuickHull
	 * 
	 * @param rocPoints
	 * @return
	 */
	public static float[][] computeConvexHull(float[][] rocPointsInput) {	
		int pointA;
		int pointB;
		
		/* Remove repeated points */
		rocPointsInput = removeRepeated(rocPointsInput);
		
		/* Get distances from each point to the diagonal ((0,0),(1,1)) */
		float[] rocPointsDistances = distanceFromDiagonal(rocPointsInput);
		
		/* Remove those points under the diagonal */
		ArrayList<Object> results;
		results = removePointsUnderDiagonal(rocPointsInput, rocPointsDistances);
		rocPointsInput = (float[][]) results.get(0);
		rocPointsDistances = (float[]) results.get(1);
		
		/* Sort roc points by their distance from the diagonal ((0,0),(1,1)) */
		sort(rocPointsInput, rocPointsDistances);
		
		/* Create arraylist with rocPoints */
		int numPoints = rocPointsInput.length;
		ArrayList<float[]> rocPoints = new ArrayList<float[]>(numPoints);
		for (int i = 0; i < numPoints; i++) {
			rocPoints.add(rocPointsInput[i]);
		}
		
		/* Get point A */
		pointA = 0;
		
		/* Get point B */
		pointB = numPoints - 1;
		
		int[] pointsABC = new int[3];
		pointsABC[0] = pointA;
		pointsABC[1] = pointB;
		
		convexHull(pointsABC, rocPoints);

		numPoints = rocPoints.size();
		float[][] rocPointsOutput = new float[numPoints][2];
		for (int i = 0; i < numPoints; i++) {
			rocPointsOutput[i] = rocPoints.get(i);
		}

		return rocPointsOutput;
	}

	private static ArrayList<Object> removePointsUnderDiagonal(float[][] rocPointsInput, float[] rocPointsDistances) {
		// TODO Auto-generated method stub
		return null;
	}

	private static float[][] removeRepeated(float[][] rocPointsInput) {
		int numPoints = rocPointsInput.length;
		
		/* Sort ascending by axis (x, y, z, ...) */
		sort(rocPointsInput);
		
		/* Count the unique points */
		float[] last = rocPointsInput[0];
		float[] current;
		int uniqueCount = 1;
		for (int i = 1; i < numPoints; i++) {
			current = rocPointsInput[i];
			if (!equal(last, current)) {
				last = current;
				++uniqueCount;
			}
		}
		
		/* Build array with unique points */
		float[][] rocPoints = new float[uniqueCount][];
		rocPoints[0] = rocPointsInput[0];
		last = rocPointsInput[0];
		uniqueCount = 1;
		for (int i = 1; i < numPoints; i++) {
			current = rocPointsInput[i];
			if (!equal(last, current)) {
				rocPoints[uniqueCount] = current;
				++uniqueCount;
				last = current;
			}
		}
		
		return rocPoints;
	}

	private static boolean equal(float[] p1, float[] p2) {
		int num = p1.length;
		
		for (int i = 0; i < num; i++) {
			if (p1[i] != p2[i]) {
				return false;
			}
		}
		
		return true;
	}

	public static void convexHull(int[] pointsABC,
			ArrayList<float[]> rocPoints) {
		int pointA = pointsABC[0];
		int pointB = pointsABC[1];
		int pointC;
		int gap;
		
		/* Check if an point can occur between the points A and B */
		gap = pointB - pointA;
		if (gap > 1) {
			/* No point between A and B. Just return */
			return;
		}
		
		/* Get point C and exclude those points inside ABC from rocPoints */
		getPointC(pointsABC, rocPoints);
		
		/* Check if any point can occur between the points A and C */
		pointA = pointsABC[0];
		pointC = pointsABC[2];
		gap = pointC - pointA;
		if (gap > 1) {
			pointsABC[0] = pointA;
			pointsABC[1] = pointC;
			convexHull(pointsABC, rocPoints);
		}
		
		/* Check if any point can occur between the points C and B */
		pointC = pointsABC[2];
		pointB = pointsABC[1];
		gap = pointB - pointC;
		if (gap > 1) {
			pointsABC[0] = pointC;
			pointsABC[1] = pointB;
			convexHull(pointsABC, rocPoints);
		}
	}

	/**
	 * Search pointC by binary search and exclude from rocPoints those points
	 * inside ABC. The pointC is the farthest point from pointA and pointB.
	 * 
	 * @param pointsABC
	 * @param pointsABC
	 * @param rocPoints
	 * @return index of the farthest point from pointA and pointB
	 */
	private static void getPointC(int[] pointsABC, ArrayList<float[]> rocPoints) {
		int pointAindex = pointsABC[0];
		int pointBindex = pointsABC[1];
		int pointCindex = searchPointC(pointsABC, rocPoints);
		int pointBindexNew = pointBindex;
		int pointCindexNew = pointCindex;
		
//		/* Check if pointC really exist */
//		if (pointCindex == -1) {
//			for (int i = pointAindex + 1; i < pointBindex; i++) {
//		}
		
		float[] pointA = rocPoints.get(pointAindex);
		float[] pointB = rocPoints.get(pointBindex);
		float[] pointC = rocPoints.get(pointCindex);
		
		float[] point;
		float slope;
		
		int numPoints = pointBindex - pointAindex + 1;
		
		boolean[] delete = new boolean[numPoints];
		Arrays.fill(delete, false);
		
		/* 
		 * Test all points between pointA and pointC for their slope relative
		 * to the pointA.
		 */
		float slopeAC;
		slopeAC = pointC[1] - pointA[1];
		slopeAC /= pointC[0] - pointA[0];
		for (int i = pointAindex + 1; i < pointCindexNew; i++) {
			/* Get the slope of point relative to pointA */
			point = rocPoints.get(i);
			slope = point[1] - pointA[1];
			slope /= point[0] - pointA[0];
			
			/* Exclude this point if its slope is less than the slopeAC */
			if (slope < slopeAC) {
				rocPoints.remove(i);
				--i;
				--pointCindexNew;
				--pointBindexNew;
			} 
		}
			
		/* 
		 * Test all points between pointC and pointB for their slope relative
		 * to the pointA.
		 */
		float slopeCB;
		slopeCB = pointB[1] - pointC[1];
		slopeCB /= pointB[0] - pointC[0];
		for (int i = pointCindexNew + 1; i < pointBindexNew; i++) {
			/* Get the slope of point relative to pointA */
			point = rocPoints.get(i);
			slope = point[1] - pointC[1];
			slope /= point[0] - pointC[0];
			
			/* Exclude this point if its slope is less than the slopeAC */
			if (slope < slopeCB) {
				rocPoints.remove(i);
				--i;
				--pointBindexNew;
			}
		}
		
		pointsABC[1] = pointBindexNew;
		pointsABC[2] = pointCindexNew;
	}

	private static int searchPointC(int[] pointsABC, ArrayList<float[]> rocPoints) {
		int pointAindex = pointsABC[0];
		int pointBindex = pointsABC[1];
		float[] pointA = rocPoints.get(pointAindex);
		float[] pointB = rocPoints.get(pointBindex);

		/*
		 * Compute the parameters of the line AB:
		 * 
		 * 		Ax + By + C = 0;
		 * 
		 * 		y - y1 = m(x - x1);
		 * 		y - mx + (mx1 - y1) = 0;
		 * 		mx + (-y) + (y1 - mx1) = 0;
		 * 		
		 * 		A = m;
		 * 		B = -1;
		 * 		C = y1 - mx1;
		 * 
		 * 		m = (x2 - x1) / (y2 - y1);
		 */
		float m = (pointB[1] - pointA[1]) / (pointB[0] - pointA[0]);
		float A = m;
		float B = -1;
		float C = pointB[1] - m * pointB[0];
		
		/* Loop through all points between A and B and pick the farthest one */
		int maxPointCindex = -1;
		float maxDistance = 0;
		float d;
		float[] pointC;
		for (int i = pointAindex + 1; i < pointBindex; i++) {
			pointC = rocPoints.get(i);
			
			/*
			 * Compute the distance of the point C to the line AB:
			 * 
			 *     ( Ax1 + By1 + C )
			 * d = (---------------)
			 *     (sqrt(A*A + B*B))
			 *     
 			 * As all lines AB have slope greater than or equal to 0, all
 			 * points above the diagonal are at a negative distance from it.
 			 * So, we compare the negative of the distance.
 			 */
			d = -(A * pointC[0] + B * pointC[1] + C);
			d /= Math.sqrt(A * A + B * B);
			
			if (d > maxDistance) {
				maxDistance = d;
				maxPointCindex = i;
			}
		}
		
		return maxPointCindex;
	}

	private static void sort(float[][] rocPoints, float[] rocPointsDistances) {
		rnd = new Random(new Date().getTime());
		qsort(rocPoints, 0, rocPoints.length - 1, rocPointsDistances);
	}

	private static void qsort(float[][] rocPoints, int begin,
			int end, float[] rocPointsDistances) {
		if (end > begin) {
			int pos;
			pos = partition(rocPoints, begin, end, rocPointsDistances);
			
			qsort(rocPoints, begin, pos - 1, rocPointsDistances);
			qsort(rocPoints, pos + 1,  end, rocPointsDistances);
		}
	}
	
	private static int partition(float[][] rocPoints, int begin, int end,
			float[] rocPointsDistances) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		float pivot = rocPointsDistances[pos];
		
		swap(rocPoints, rocPointsDistances, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (rocPointsDistances[i] <= pivot) {
				swap(rocPoints, rocPointsDistances, pos++, i);
			}
		}
		swap(rocPoints, rocPointsDistances, pos, end);
		
		return pos;
	}
	
	private static void swap(float[][] rocPoints, float[] rocPointsDistances,
			int i, int j) {
		float[] tmp = rocPoints[i];
		rocPoints[i] = rocPoints[j];
		rocPoints[j] = tmp;

		float tmp2 = rocPointsDistances[i];
		rocPointsDistances[i] = rocPointsDistances[j];
		rocPointsDistances[j] = tmp2;
	}
	
	private static void sort(float[][] rocPoints) {
		rnd = new Random(new Date().getTime());
		qsort(rocPoints, 0, rocPoints.length - 1);
	}

	private static void qsort(float[][] rocPoints, int begin, int end) {
		if (end > begin) {
			int pos;
			pos = partition(rocPoints, begin, end);
			
			qsort(rocPoints, begin, pos - 1);
			qsort(rocPoints, pos + 1,  end);
		}
	}
	
	private static int partition(float[][] rocPoints, int begin, int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		float[] pivot = rocPoints[pos];
		
		swap(rocPoints, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (lessThanOrEqual(rocPoints[i], pivot)) {
				swap(rocPoints, pos++, i);
			}
		}
		swap(rocPoints, pos, end);
		
		return pos;
	}
	
	private static boolean lessThanOrEqual(float[] p1, float[] p2) {
		int num = p1.length;
		
		for (int i = 0; i < num; i++) {
			if (p1[i] > p2[i]) {
				return false;
			}
		}
		
		return true;
	}

	private static void swap(float[][] rocPoints, int i, int j) {
		float[] tmp = rocPoints[i];
		rocPoints[i] = rocPoints[j];
		rocPoints[j] = tmp;
	}
	
	private static float[] distanceFromDiagonal(float[][] rocPoints) {
		int numPoints = rocPoints.length;
		float[] distances = new float[numPoints];
		double root2 = Math.sqrt(2);
		
		Arrays.fill(distances, 0);
		
		for (int point = 0; point < numPoints; point++) {
			distances[point] = -(rocPoints[point][0] - rocPoints[point][1]);
			distances[point] /= root2;
		}

		return distances;
	}
	
}