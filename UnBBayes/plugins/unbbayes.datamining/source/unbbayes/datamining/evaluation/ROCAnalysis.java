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
package unbbayes.datamining.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 18/01/2007
 */
public class ROCAnalysis {
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
		int[] probsIndex = Utils.sortDescending(probs);
		
		float[] distribution = testData.getClassDistribution(false);
		float fp = 0;
		float tp = 0;
		float n = distribution[negativeClass];
		float p = distribution[positiveClass];
		float[][] rocPoints = new float[numInstances + 2][2];
		int classIndex = testData.classIndex;
		int counterIndex = testData.counterIndex;
		Instance instance;
		
		/* Create new array of probs that will match the rocPoints */
		float[] newProbs = new float[numInstances + 2];

		float lastProb = Float.MIN_VALUE;
		int counter = 0;
		for (int inst, i = 0; i < numInstances; i++) {
			inst = probsIndex[i];
			
			/* Check if new roc point should be added or not */
			if (probs[inst] != lastProb) {
				/* Add new roc point */
				addRocPoint(fp, n, tp, p, rocPoints, counter);
				
				/* Add prob of this roc point */
				newProbs[counter] = probs[inst];
				
				/* Update counter */
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

		/* Add prob of this roc point */
		newProbs[counter] = 0;
		
		/* Update counter */
		++counter;
		
		/* Create rocPoints and probs arrays without null positions */
		float[][] rocPointsProbs = new float[counter][3];
		for (int i = 0; i < counter; i++) {
			rocPointsProbs[i][0] = rocPoints[i][0];
			rocPointsProbs[i][1] = rocPoints[i][1];
			rocPointsProbs[i][2] = newProbs[i];
		}
		
		return rocPointsProbs;
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
		int[] probsIndex = Utils.sortDescending(probs);
		
		float[] distribution = testData.getClassDistribution(false);
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
			inst = probsIndex[i];
			
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
		
		if (Double.isNaN(auc)) {
			return 0;
		}
		
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
	public static ArrayList<float[]> computeConvexHull(
			ArrayList<float[]> rocPoints) {
		/* Sort ascending by FP and TP */
		sort(rocPoints);
		
		/* Remove repeated points */
		removeRepeated(rocPoints);
		
		/* Remove those points under the diagonal ((0,0),(1,1)) */
		removePointsUnderDiagonal(rocPoints);
		
		int numPoints = rocPoints.size();

		/* Build array with points ABC */
		float[][] pointsABC = new float[3][2];
		
		/* Get point A */
		pointsABC[0] = rocPoints.remove(0);
		--numPoints;
		
		/* Get point B */
		pointsABC[1] = rocPoints.remove(numPoints - 1);
		--numPoints;
		
		/* Create the resulting arraylist wich will retain the hull points */
		ArrayList<float[]> result = new ArrayList<float[]>();
		
		/* Add points A and B to the resulting array list */
		result.add(pointsABC[0]);
		result.add(pointsABC[1]);

		convexHull(pointsABC, rocPoints, result);
		
		return result;
	}

	public static void removeRepeated(ArrayList<float[]> rocPoints) {
		int numPoints = rocPoints.size();
		
		/* Remove repeated points */
		float[] last = rocPoints.get(0);
		float[] current;
		for (int i = 1; i < numPoints; i++) {
			current = rocPoints.get(i);
			if (equal(last, current)) {
				rocPoints.remove(i);
				--i;
				--numPoints;
				continue;
			}
			last = current;
		}
		rocPoints.trimToSize();
	}

	private static boolean equal(float[] p1, float[] p2) {
		int num = p1.length;
		
		for (int i = 0; i < num; i++) {
//			if (Math.abs(p1[i] - p2[i]) > Utils.SMALL) {
			if (Math.abs(p1[i] - p2[i]) > 0) {
				return false;
			}
		}
		
		return true;
	}

	public static void convexHull(float[][] pointsABC,
			ArrayList<float[]> rocPoints, ArrayList<float[]> result) {
		/* Check if there is any roc point left */
		if (rocPoints.size() == 0) {
			/* No point between A and B. Just return */
			return;
		}
		
		float[] pointA = pointsABC[0];
		float[] pointB = pointsABC[1];

		/* 
		 * Get point C (farthest point from line AB), remove it from the 
		 * available rocPoints and insert it in the resulting arraylist
		 */
		float[] pointC = getPointC(pointsABC, rocPoints, result);
		pointsABC[2] = pointC;
		
		/* Exclude those points inside ABC from rocPoints */
		removePointsInsideABC(pointsABC, rocPoints);
		
		/* Arraylist of points above the line AC */
		ArrayList<float[]> s1 = new ArrayList<float[]>();
		
		/* Arraylist of points above the line CB */
		ArrayList<float[]> s2 = new ArrayList<float[]>();
		
		/* Separate the points outside the triangle ABC in s1 and s2 */
		buildS1S2(pointsABC, rocPoints, s1, s2);

		/* Apply convexhull to the s1 (points above line AC) */
		pointsABC[0] = pointA;
		pointsABC[1] = pointC;
		convexHull(pointsABC, s1, result);

		/* Apply convexhull to the s2 (points above line CB) */
		pointsABC[0] = pointC;
		pointsABC[1] = pointB;
		convexHull(pointsABC, s2, result);
	}

	public static void buildS1S2(float[][] pointsABC,
			ArrayList<float[]> rocPoints, ArrayList<float[]> s1,
			ArrayList<float[]> s2) {
		int numPoints = rocPoints.size();
		float[] pointA = pointsABC[0];
		float[] pointC = pointsABC[2];
		float[] pointP;
		
		double dCA = distance(pointC, pointA);
		double dPA;
		
		for (int i = 0; i < numPoints; i++) {
			pointP = rocPoints.remove(i);
			dPA = distance(pointP, pointA);
			
			/* Check wich group (s1 or s2) this point should go to */
			if (dPA < dCA) {
				/* This point pertains to s1 */
				s1.add(pointP);
			} else if (dPA > dCA) {
				/* This point pertains to s2 */
				s2.add(pointP);
			} else {
				/* Should never happens */
				@SuppressWarnings("unused")
				boolean fudeu = true;
			}
			--i;
			--numPoints;
		}
		
		/* Resize the arraylists to their real size */
		rocPoints.clear();
		s1.trimToSize();
		s2.trimToSize();
	}

	private static double distance(float[] point1, float[] point2) {
		double dist;
		double result = 0;
		int numAxis = point1.length;

		for (int axis = 0; axis < numAxis; axis++) {
				dist = point1[axis] - point2[axis];
				result += dist * dist;
		}
		
		return Math.sqrt(result);
	}

	public static void removePointsInsideABC(float[][] pointsABC,
			ArrayList<float[]> rocPoints) {
		float[] pointA = pointsABC[0];
		float[] pointB = pointsABC[1];
		float[] pointC = pointsABC[2];
		
		float[] point;
		
		double dAB;
		double dAC;
		double dBC;
		
		int numPoints = rocPoints.size();
		int counter = 0;

		/* Remove points inside the triangle ABC */
		for (int i = 0; i < numPoints; i++) {
			/* Get distances from the point to the faces of the triangle ABC */
			point = rocPoints.get(i);
			dAB = distancePointToLine(point, pointA, pointB);
			dAC = distancePointToLine(point, pointA, pointC);
			dBC = distancePointToLine(point, pointB, pointC);
			
			/* Exclude this point if it's inside the triangle ABC */
//			if (dAB <= Utils.SMALL &&
//					dAC >= -Utils.SMALL &&
//					dBC >= -Utils.SMALL) {
			if (dAB <= 0 &&
					dAC >= 0 &&
					dBC >= 0) {
				rocPoints.remove(i);
				--i;
				--numPoints;
			}
			++counter;
		}
		
		/* Resize the rocPoint arraylist to its real size */
		rocPoints.trimToSize();
	}
	
	private static float[] getPointC(float[][] pointsABC,
			ArrayList<float[]> rocPoints, ArrayList<float[]> result) {
		float[] pointA = pointsABC[0];
		float[] pointB = pointsABC[1];
		int numPoints = rocPoints.size();

		/* Loop through all points between A and B and pick the farthest one */
		int maxPointCindex = -1;
		double maxDistance = Double.MIN_VALUE;
		double d;
		float[] pointC;
		for (int i = 0; i < numPoints; i++) {
			pointC = rocPoints.get(i);
			d = -distancePointToLine(pointC, pointA, pointB);
			if (d > maxDistance) {
				maxDistance = d;
				maxPointCindex = i;
			}
		}
		
		pointC = rocPoints.remove(maxPointCindex);
		result.add(pointC);
		
		return pointC;
	}

	public static void sort(ArrayList<float[]> rocPoints) {
		Object[] aux = rocPoints.toArray();
		Arrays.sort(aux, new Comparator<Object>() {
			public int compare(final Object arg0, final Object arg1) {
				float[] p1 = (float[]) arg0;
				float[] p2 = (float[]) arg1;
				float x;
				
				for (int i = 0; i < p1.length; i++) {
					x = p1[i] - p2[i];
					if (x < 0) {
						return -1;
					} else if (x > 0) {
						return 1;
					}
				}
				
				return 0;
			}
			
		});
		
		int num = aux.length;
		rocPoints.clear();
		for (int i = 0; i < num ; i++) {
			rocPoints.add((float[]) aux[i]);
		}
	}

	private static void removePointsUnderDiagonal(
			ArrayList<float[]> rocPoints) {
		int numPoints = rocPoints.size();
		double d;
		
		for (int point = 1; point < numPoints - 1; point++) {
			d = rocPoints.get(point)[0] - rocPoints.get(point)[1];
			
			/* Remove if point is under diagonal */
//			if (d >= -Utils.SMALL) {
			if (d >= 0) {
				rocPoints.remove(point);
				--point;
				--numPoints;
			}
		}
		rocPoints.trimToSize();
	}

	private static double distancePointToLine(float[] point, float[] pointA,
			float[] pointB) {
		/*
		 * Compute the parameters a, b and c of the line AB:
		 * 
		 * 		ax + by + c = 0;
		 * 
		 * 		y - y1 = m(x - x1);
		 * 		y - mx + (mx1 - y1) = 0;
		 * 		mx + (-y) + (y1 - mx1) = 0;
		 * 		
		 * 		a = m;
		 * 		b = -1;
		 * 		c = y1 - mx1;
		 * 
		 * 		m = (y2 - y1) / (x2 - x1);
		 */
		if (pointB[0] == pointA[0]) {
			return point[1] - pointB[1];
		}
		float m = (pointB[1] - pointA[1]) / (pointB[0] - pointA[0]);
		float a = m;
		float b = -1;
		float c = pointB[1] - m * pointB[0];
		
		/*
		 * Compute the distance of the point C to the line AB:
		 * 
		 *     ( ax1 + by1 + c )
		 * d = (---------------)
		 *     (sqrt(a * a + b * b))
		 *     
		 * As all lines AB have slope greater than or equal to 0, all
		 * points above the diagonal are at a negative distance from it.
		 */
		double d = (a * point[0] + b * point[1] + c);
		d /= Math.sqrt(a * a + b * b);
		
		return d;
	}

	public static float[][][] averageROCPoints(float[][][] rocPoints) {
		int nRocs = rocPoints.length;
		
		/* Count the number of rocPoints in all roc curves */
		int numT = 0;
		int[] nPts = new int[nRocs];
		for (int roc = 0; roc < nRocs; roc++) {
			if (rocPoints[roc] == null) {
				@SuppressWarnings("unused")
				boolean fudeu = true;
			}
			nPts[roc] = rocPoints[roc].length;
			numT += nPts[roc];
		}
		
		/* Create array with all probs from all folds */
		float[] t = new float[numT];
		int count = 0;
		for (int roc = 0; roc < nRocs; roc++) {
			for (int i = 0; i < nPts[roc]; i ++) {
				t[count] = rocPoints[roc][i][2];
				++count;
			}
		}
		
		/* Sort t */
		Arrays.sort(t);
		
		/* Output rocPoints */
		ArrayList<float[]> avg = new ArrayList<float[]>();
		ArrayList<float[]> stdDev = new ArrayList<float[]>();
		                          
		int samples = numT / nRocs;
		int p;
		double[] fprsumTemp = new double[nRocs];
		double[] tprsumTemp = new double[nRocs];
		float[] rocPointAvg;
		float[] rocPointStdDev;
		double[] rocPointAvgStdDev;
		for (int tIdx = 0; tIdx < numT; tIdx += numT / samples) {
			for (int roc = 0; roc < nRocs; roc++) {
				p = rocPointAtThreshold(rocPoints[roc], t[tIdx]);
				fprsumTemp[roc] = rocPoints[roc][p][0];
				tprsumTemp[roc] = rocPoints[roc][p][1];
			}
			
			/* Prepare for roc points */
			rocPointAvg = new float[2];
			rocPointStdDev = new float[2];
			
			/* Average and Standard Deviation for fpr */
			rocPointAvgStdDev = Utils.computeMeanStdDev(fprsumTemp);
			rocPointAvg[0] = (float) rocPointAvgStdDev[0];
			rocPointStdDev[0] = (float) rocPointAvgStdDev[1];
			
			/* Average and Standard Deviation for tpr */
			rocPointAvgStdDev = Utils.computeMeanStdDev(tprsumTemp);
			rocPointAvg[1] = (float) rocPointAvgStdDev[0];
			rocPointStdDev[1] = (float) rocPointAvgStdDev[1];
			
			/* Add average and standard deviation of roc points */
			avg.add(rocPointAvg);
			stdDev.add(rocPointStdDev);
		}
		
		/* Check if last roc point is (0,0) */
		int numPoints = avg.size();
		if (avg.get(numPoints - 1)[0] != 0
				|| avg.get(numPoints - 1)[1] != 0) {
			float[] lastRocPointAvg = {0, 0};
			float[] lastRocPointStdDev = {0, 0};
			avg.add(lastRocPointAvg);
			stdDev.add(lastRocPointStdDev);
			++numPoints;
		}
		
		/* Build new rocPoints */
		float[][][] newRocPoints = new float[2][numPoints][];
		for (int i = 0; i < numPoints; i++) {
			newRocPoints[0][i] = avg.get(i);
			newRocPoints[1][i] = stdDev.get(i);
		}
		
		return newRocPoints;
	}

	private static int rocPointAtThreshold(float[][] rocPoints, float threshold) {
		int numPoints = rocPoints.length;
		int i = 0;
		
		while (i < numPoints && rocPoints[i][2] > threshold) {
			++i;
		}

		return i;
	}
	
}