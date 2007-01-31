package unbbayes.datamining.datamanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Class implementing some simple utility methods.
 *
 *	@author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *	@author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *	@version $1.0 $ (16/02/2002)
 */
public final class Utils { 
	/** The small deviation allowed in double comparisons */
	public static double SMALL = 1e-6;
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.datamanipulation.resources." +
			"DataManipulationResource");

	private static Random rnd;

	/**
	 * Tests if a is equal to b.
	 *
	 * @param a A double
	 * @param b A double
	 */
	public static boolean eq(double a, double b) {	
		return (a - b < SMALL) && (b - a < SMALL);
	}

	/**
	 * Tests if a is equal to b.
	 *
	 * @param a A byte
	 * @param b A byte
	 */
	public static boolean eq(byte a, byte b) {	
		if (a == b) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tests if a is smaller than b.
	 *
	 * @param a A double
	 * @param b A double
	 */
	public static boolean gr(double a, double b) {
		return (a-b > SMALL);
	}

	/**
	 * Returns index of maximum element in a given
	 * array of doubles. First maximum is returned.
	 *
	 * @param doubles The array of doubles not null
	 * @return the index of the maximum element
	 */
	public static int maxIndex(double[] doubles) {
		double maximum = doubles[0];
		int maxIndex = 0;

		for (int i = 0; i < doubles.length; i++) {
			if (doubles[i] > maximum) {
				maxIndex = i;
				maximum = doubles[i];
			}
		}

		return maxIndex;
	}

	/**
	 * Returns index of maximum element in a given
	 * array of floats. First maximum is returned.
	 *
	 * @param floats The array of floats not null
	 * @return the index of the maximum element
	 */
	public static int maxIndex(float[] floats) {
		float maximum = floats[0];
		int maxIndex = 0;
		int i;

		for (i = 0; i < floats.length; i++) {
			if (floats[i] > maximum) {
				maxIndex = i;
				maximum = floats[i];
			}
		}

		return maxIndex;
	}

	/**
	 * Returns index of maximum element in a given
	 * array of integers. First maximum is returned.
	 *
	 * @param ints The array of integers not null
	 * @return the index of the maximum element
	 */
	public static int maxIndex(int[] ints) {
		int maximum = ints[0];
		int maxIndex = 0;

		for (int i = 0; i < ints.length; i++) {
			if (ints[i] > maximum) {
				maxIndex = i;
				maximum = ints[i];
			}
		}

		return maxIndex;
	}

	/**
	 * Returns minimum element in a given array of doubles.
	 *
	 * @param doubles The array of doubles not null
	 * @return minimum element
	 */
	public static double min(double[] doubles) {
		double minimum = doubles[0];

		for (int i = 0; i < doubles.length; i++) {
			if (doubles[i] < minimum) {
				minimum = doubles[i];
			}
		}

		return minimum;
	}

	/**
	 * Returns minimum element in a given array of floats.
	 *
	 * @param doubles The array of floats not null
	 * @return minimum element
	 */
	public static float min(float[] floats) {
		float minimum = floats[0];

		for (int i = 0; i < floats.length; i++) {
			if (floats[i] < minimum) {
				minimum = floats[i];
			}
		}

		return minimum;
	}

	/**
	 * Normalizes the doubles in the array by their sum.
	 *
	 * @param doubles The array of double
	 * @exception IllegalArgumentException if sum is Zero or NaN
	 */
	public static void normalize(double[] doubles) {
		double sum = 0;
		
		for (int i = 0; i < doubles.length; i++) {
			sum += doubles[i];
		}
		normalize(doubles, sum);
	}

	/**
	 * Normalizes the floats in the array by their sum.
	 *
	 * @param floats The array of float
	 * @exception IllegalArgumentException if sum is Zero or NaN
	 */
	public static void normalize(float[] floats) {
		float sum = 0;
		
		for (int i = 0; i < floats.length; i++) {
			sum += floats[i];
		}
		normalize(floats, sum);
	}

	/**
	 * Normalizes the doubles in the array using the given value.
	 *
	 * @param doubles The array of double
	 * @param sum The value by which the doubles are to be normalized
	 * @exception IllegalArgumentException if sum is zero or NaN
	 */
	public static void normalize(double[] doubles, double sum) {
		if (Double.isNaN(sum)) {
			throw new IllegalArgumentException(resource.getString("normalizeException1"));
		}
		if (sum == 0) {
			throw new IllegalArgumentException(resource.getString("normalizeException2"));
		}
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] /= sum;
		}
	}

	/**
	 * Normalizes the floats in the array using the given value.
	 *
	 * @param floats The array of float
	 * @param sum The value by which the floats are to be normalized
	 * @exception IllegalArgumentException if sum is zero or NaN
	 */
	public static void normalize(float[] floats, float sum) {
		if (Float.isNaN(sum)) {
			throw new IllegalArgumentException(resource.getString("normalizeException1"));
		}
		if (sum == 0) {
			throw new IllegalArgumentException(resource.getString("normalizeException2"));
		}
		for (int i = 0; i < floats.length; i++) {
			floats[i] /= sum;
		}
	}


	/**
	 * Normalizes a float array to ensure that all elements of the input data
	 * vector are proportionally tranformed into an vector with predifined upper
	 * limit and lower limit.
	 *
	 * @param data The vetor to be normalized
	 * @param upperLimit The upper limit of the normalized vector
	 * @param lowerLimit The lower limit of the normalized vector
	 */
	public static void normalize(float[] data, float upperLimit,
			float lowerLimit){
		float hi = Float.MIN_VALUE;
		float lo = Float.MAX_VALUE;

		if (upperLimit <= lowerLimit){
			throw new IllegalArgumentException(resource.getString("" +
					"normalizeException3"));
		}

		for (int i = 0; i < data.length; i++){
			hi = Math.max(hi, data[i]);
			lo = Math.min(lo, data[i]);
		}

		float fact = (upperLimit - lowerLimit) / (hi-lo);

		for(int i = 0; i < data.length; i++){
			data[i] = ((data[i] - lo) * fact) + lowerLimit;
		}
	}

	/**
	 * Normalizes a float to ensure that it will be proportionally tranformed
	 * into a float with predifined upper limit and lower limit.
	 *
	 * @param data The data to normalize
	 * @param highestValue The highest value that data may assume
	 * @param smallestValue The smallest value that data may assume
	 * @param upperLimit The upper limit for normalization
	 * @param lowerLimit The lower limit for normalization
	 * @return The normalized data
	 */
	public static float normalize(float data, float highestValue,
			float smallestValue, float upperLimit, float lowerLimit){
		if(upperLimit <= lowerLimit){
			throw new IllegalArgumentException(resource.getString("" +
					"normalizeException3"));
		}

		data = ((data - smallestValue) * (upperLimit - lowerLimit) 
				/ (highestValue - smallestValue)) + lowerLimit;
		return data;
	}


	/**
	 * Computes the sum of the elements of an array of doubles.
	 *
	 * @param doubles The array of double
	 * @return The sum of the elements
	 */
	public static double sum(double[] doubles) {
		double sum = 0;

		for (int i = 0; i < doubles.length; i++) {
			sum += doubles[i];
		}
		
		return sum;
	}

	/**
	 * Computes the sum of the elements of an array of floats.
	 *
	 * @param floats The array of float
	 * @return The sum of the elements
	 */
	public static float sum(float[] floats) {
		float sum = 0;

		for (int i = 0; i < floats.length; i++) {
			sum += floats[i];
		}
		
		return sum;
	}

	 /**
	 * Computes the sum of the elements of an array of integers.
	 *
	 * @param ints The array of integers
	 * @return The sum of the elements
	 */
	public static int sum(int[] ints) {
		int sum = 0;

		for (int i = 0; i < ints.length; i++) {
			sum += ints[i];
		}
		
		return sum;
	}

	/**
	 * Rounds a double and converts it into String.
	 *
	 * @param value The double value
	 * @param afterDecimalPoint The (maximum) number of digits permitted
	 * after the decimal point
	 * @return The double as a formatted string
	 */
	public static String doubleToString(double value, int afterDecimalPoint) {
		StringBuffer stringBuffer;
		double temp;
		int dotPosition;
		long precisionValue;

		temp = value * Math.pow(10.0, afterDecimalPoint);
		if (Math.abs(temp) < Long.MAX_VALUE) {
			precisionValue = (temp > 0) ? 
					(long)(temp + 0.5):	-(long)(Math.abs(temp) + 0.5);
			if (precisionValue == 0) {
				stringBuffer = new StringBuffer(String.valueOf(0));
			} else {
				stringBuffer = new StringBuffer(String.valueOf(precisionValue));
			}
			if (afterDecimalPoint == 0) {
				return stringBuffer.toString();
			}
			dotPosition = stringBuffer.length() - afterDecimalPoint;
			while ((precisionValue < 0 && dotPosition < 1) || dotPosition < 0) {
				if (precisionValue < 0) {
					stringBuffer.insert(1, 0);
				} else {
					stringBuffer.insert(0, 0);
				}
				dotPosition++;
			}
			stringBuffer.insert(dotPosition, '.');
			if ((precisionValue < 0) && (stringBuffer.charAt(1) == '.')) {
				stringBuffer.insert(1, 0);
			} else if (stringBuffer.charAt(0) == '.') {
				stringBuffer.insert(0, 0);
			}
			int currentPos = stringBuffer.length() - 1;
			while (currentPos > dotPosition
					&& stringBuffer.charAt(currentPos) == '0') {
				stringBuffer.setCharAt(currentPos--, ' ');
			}
			if (stringBuffer.charAt(currentPos) == '.') {
				stringBuffer.setCharAt(currentPos, ' ');
			}
			return stringBuffer.toString().trim();
		}
		
		return new String("" + value);
	}

	/**
	 * Rounds a double and converts it into a formatted decimal-justified String.
	 * Trailing 0's are replaced with spaces.
	 *
	 * @param value The double value
	 * @param width The width of the string
	 * @param afterDecimalPoint The number of digits after the decimal point
	 * @return The double as a formatted string
	 */
	public static String doubleToString(double value, int width,
			int afterDecimalPoint)	{
		String tempString = doubleToString(value, afterDecimalPoint);
		char[] result;
		int dotPosition;

		if ((afterDecimalPoint >= width) || (tempString.indexOf('E') != -1)) {
			return tempString;
		}

		// Initialize result
		result = new char[width];
		for (int i = 0; i < result.length; i++) {
			result[i] = ' ';
		}

		if (afterDecimalPoint > 0) {
			// Get position of decimal point and insert decimal point
			dotPosition = tempString.indexOf('.');
			if (dotPosition == -1) {
				dotPosition = tempString.length();
			} else {
				result[width - afterDecimalPoint - 1] = '.';
			}
		} else {
			dotPosition = tempString.length();
		}

		int offset = width - afterDecimalPoint - dotPosition;
		if (afterDecimalPoint > 0) {
			offset--;
		}

		// Not enough room to decimal align within the supplied width
		if (offset < 0) {
			return tempString;
		}

		// Copy characters before decimal point
		for (int i = 0; i < dotPosition; i++) {
			result[offset + i] = tempString.charAt(i);
		}

		// Copy characters after decimal point
		for (int i = dotPosition + 1; i < tempString.length(); i++) {
			result[offset + i] = tempString.charAt(i);
		}

		return new String(result);
	}


	
	/*------------------- Generic Quicksort - start ---------------------*/
	
	/**
	 * Sorts a given array of objects in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array. 
	 * 
	 * @param array This array is not changed by the method!
	 * @param cmp A comparable object.
	 * @return An array of integers with the positions in the sorted array.
	 */
	public static int[] sort(Object[] array, Comparator<Object> cmp) {
		int[] index = new int[array.length];

		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		qsort(array, index, 0, array.length - 1, cmp);
		
		return index;
	}

	private static void qsort(Object[] array, int[] index, int begin, int end,
			Comparator<Object> cmp) {
		if (end > begin) {
			int pos = partition(array, index, begin, end, cmp);
			
			qsort(array, index, begin, pos - 1, cmp);
			qsort(array, index, pos + 1,  end,  cmp);
		}
	}
	
	private static int partition(Object[] array, int[] index, int begin,
			int end, Comparator<Object> cmp) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		Object pivot = array[index[pos]];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (cmp.compare(array[index[i]], pivot) <= 0) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}
	
	private static void swap(int[] index, int i, int j) {
		int tmp = index[i];
		index[i] = index[j];
		index[j] = tmp;
	}
	
	/*------------------- Generic Quicksort - end ---------------------*/

	
	
	/*------------------- double[] Quicksort - start ---------------------*/
	
	/**
	 * Sorts a given array of doubles in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array. It doesn't use safe floating-point
	 * comparisons. Occurrences of Double.NaN are treated as
	 * Double.MAX_VALUE
	 *
	 * @param array This array is not changed by the method!
	 * @return An array of integers with the positions in the sorted
	 * array.
	 */
	public static int[] sort(double[] array) {
		int [] index = new int[array.length];
		
		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
			if (Double.isNaN(array[i])) {
				array[i] = Double.MAX_VALUE;
			}
		}
		qsort(array, index, 0, array.length - 1);
		
		return index;
	}

	private static void qsort(double[] array, int[] index, int begin, int end) {
		if (end > begin) {
			int pos = partition(array, index, begin, end);
			
			qsort(array, index, begin, pos - 1);
			qsort(array, index, pos + 1,  end);
		}
	}
	
	private static int partition(double[] array, int[] index, int begin,
			int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		double pivot = array[index[pos]];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (array[index[i]] <= pivot) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}

	/*------------------- double[] Quicksort - end ---------------------*/

	
	
	/*------------------- float[] Quicksort - start ---------------------*/
	
	/**
	 * Sorts a given array of floats in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array. It doesn't use safe floating-point
	 * comparisons. Occurrences of Double.NaN are treated as
	 * Double.MAX_VALUE
	 *
	 * @param array This array is not changed by the method!
	 * @return An array of integers with the positions in the sorted
	 * array.
	 */
	public static int[] sort(float[] array) {
		int [] index = new int[array.length];
		
		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
			if (Double.isNaN(array[i])) {
				array[i] = Float.MAX_VALUE;
			}
		}
		qsort(array, index, 0, array.length - 1);
		
		return index;
	}

	private static void qsort(float[] array, int[] index, int begin, int end) {
		if (end > begin) {
			int pos = partition(array, index, begin, end);
			
			qsort(array, index, begin, pos - 1);
			qsort(array, index, pos + 1,  end);
		}
	}
	
	private static int partition(float[] array, int[] index, int begin,
			int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		float pivot = array[index[pos]];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (array[index[i]] <= pivot) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}

	/*------------------- double[] Quicksort - end ---------------------*/

	
	
	/*------------------- int[] Quicksort - start ---------------------*/

	/**
	 * Sorts a given array of integers in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array.
	 *
	 * @param array This array is not changed by the method!
	 * @return An array of integers with the positions in the sorted
	 * array.
	 */
	public static int[] sort(int[] array) {
		int[] index = new int[array.length];
		
		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		qsort(array, index, 0, array.length - 1);
		
		return array;
	}

	private static void qsort(int[] array, int[] index, int begin, int end) {
		if (end > begin) {
			int pos = partition(array, index, begin, end);
			
			qsort(array, index, begin, pos - 1);
			qsort(array, index, pos + 1,  end);
		}
	}
	
	private static int partition(int[] array, int[] index, int begin,
			int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		int pivot = array[index[pos]];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (array[index[i]] <= pivot) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}

	/*------------------- double[] Quicksort - end ---------------------*/

	
	
	/*------------------- byte[] Quicksort - start ---------------------*/

	/**
	 * Sorts a given array of bytes in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array. It doesn't use safe floating-point
	 * comparisons.
	 *
	 * @param array This array is not changed by the method!
	 * @return An array of integers with the positions in the sorted
	 * array.
	 */
	public static int[] sort(byte[] array) {
		int[] index = new int[array.length];
		
		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		qsort(array, index, 0, array.length - 1);
		
		return index;
	}

	private static void qsort(byte[] array, int[] index, int begin, int end) {
		if (end > begin) {
			int pos = partition(array, index, begin, end);
			
			qsort(array, index, begin, pos - 1);
			qsort(array, index, pos + 1,  end);
		}
	}
	
	private static int partition(byte[] array, int[] index, int begin,
			int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		byte pivot = array[index[pos]];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (array[index[i]] <= pivot) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}

	/*------------------- byte[] Quicksort - end ---------------------*/

	
	
	/** 
	 * Sort values in a array of doubles and returns an array of doubles
	 * with the sum of equal values. Original array will be modified
	 * 		
	 * @param values The array of double
	 * @return An array with the sum of equal values from original array
	 */
	public static double[] getDistribution(double[] values) {
		int size = values.length;
		int j=0;
		double currentValue = values[0];
		double[] resultArray = new double[size + 1];
		Arrays.sort(values);
		for (int i=0; i<size; i++) {
			if (values[i]==currentValue) {
				resultArray[j] += values[i];
			} else {
				j++;
				resultArray[j] = values[i];
				currentValue = values[i];
			}
		}
		double[] resultArray2 = new double[j];
		System.arraycopy(resultArray,1,resultArray2,0,resultArray2.length);
		
		return resultArray2;
	}

	/** 
	 * Sort values in a array of floats and returns an array of floats
	 * with the sum of equal values. Original array will be modified
	 * 
	 * @param values The array of float
	 * @return An array with the sum of equal values from original array
	 */
	public static float[] getDistribution(float[] values) {
		int size = values.length;
		int j=0;
		float currentValue = values[0];
		float[] resultArray = new float[size + 1];
		Arrays.sort(values);
		for (int i=0; i<size; i++) {
			if (values[i]==currentValue) {
				resultArray[j] += values[i];
			} else {
				j++;
				resultArray[j] = values[i];
				currentValue = values[i];
			}
		}
		float[] resultArray2 = new float[j];
		System.arraycopy(resultArray,1,resultArray2,0,resultArray2.length);
		
		return resultArray2;
	}

	/** 
	 * Sort values in a array of doubles and returns an array of doubles
	 * with the frequency of values seen. Original array will be modified
	 * 
	 * @param values The array of double
	 * @return An array with the frequency of values from original array
	 */
	public static double[] getFrequency(double[] values) {
		int size = values.length;
		int j=0;
		double currentValue = values[0];
		double[] resultArray = new double[size];
		Arrays.sort(values);
		for (int i=0; i<size; i++) {
			if (values[i]==currentValue) {
				resultArray[j] ++;
			} else {
				j++;
				resultArray[j] ++;
				currentValue = values[i];
			}
		}
		double[] resultArray2 = new double[j + 1];
		System.arraycopy(resultArray,0,resultArray2,0,resultArray2.length);
		
		return resultArray2;
	}

	/**
	 * Sort values in a array of floats and returns an array of floats
	 * with the frequency of values seen. Original array will be modified
	 * @param values The array of float
	 * @return An array with the frequency of values from original array
	 */
	public static float[] getFrequency(float[] values) {
		int size = values.length;
		int j=0;
		float currentValue = values[0];
		float[] resultArray = new float[size];
		Arrays.sort(values);
		for (int i=0; i<size; i++) {
			if (values[i]==currentValue) {
				resultArray[j] ++;
			} else {
				j++;
				resultArray[j] ++;
				currentValue = values[i];
			}
		}
		float[] resultArray2 = new float[j + 1];
		System.arraycopy(resultArray,0,resultArray2,0,resultArray2.length);
		
		return resultArray2;
	}

	/** applies a two arrays sum
	 * 	@param array1 one of the arrays to apply the sum
	 *	@param array2 one of the arrays to apply the sum
	 *	@return result of the sum, null if the arrays if of different sizes
	 */
	public static int[] arraysSum(int[] array1, int[] array2) {
		if (array1.length!=array2.length) {
			return null;
		}

		int[] newArray = new int[array1.length];
		for (int i=0;i<array1.length;i++) {
			newArray[i] = array1[i]+array2[i];
		}

		return newArray;
	}


	/** 
	 * Applies a two arrays sum
	 * 
	 * @param array1: one of the arrays to apply the sum
	 * @param array2: one of the arrays to apply the sum
	 * @return result of the sum, null if the arrays if of different sizes
	 */
	public static double[] arraysSum(double[] array1, double[] array2) {
		if (array1.length!=array2.length) {
			return null;
		}

		double[] newArray = new double[array1.length];
		for(int i=0;i<array1.length;i++) {
			newArray[i] = array1[i]+array2[i];
		}

		return newArray;
	}
	
	/** 
	 * Applies a two arrays sum
	 * @param array1: one of the arrays to apply the sum
	 * @param array2: one of the arrays to apply the sum
	 * @return result of the sum, null if the arrays if of different sizes
	 */
	 public static float[] arraysSum(float[] array1, float[] array2) {
		if (array1.length!=array2.length) {
			return null;
		}

		float[] newArray = new float[array1.length];
		for(int i=0;i<array1.length;i++) {
			newArray[i] = array1[i]+array2[i];
		}

		return newArray;
	}


	/**
	 * Calculates the standard deviation and the mean of an array of numbers
	 * per each class value of the instance set.
	 * 
	 * See Knuth's The Art Of Computer Programming Volume II: Seminumerical
	 * Algorithms.
	 * This algorithm is slower, but more resistant to error propagation.
	 * The input dataset must contain at least two values.
	 * M(1) = x(1), M(k) = M(k-1) + (x(k) - M(k-1) / k<br>
	 * S(1) = 0, S(k) = S(k-1) + (x(k) - M(k-1)) * (x(k) - M(k))<br>
	 * for 2 <= k <= n, then<br>
	 * sigma = sqrt(S(n) / (n - 1))
	 *
	 * @param dataset Sample to compute the standard deviation of.
	 * @param att The attribute's index of the sample.
	 * @return An array list with two values containing:
	 * <li> First: A vector of floats with the <b>standard deviation</b> 
	 * per class.
	 * <li> Second: A vector of floats with the <b>mean</b> per class.
	 * @throws Exception
	 */
	public static ArrayList<double[]> stdDevMeanPerClass(InstanceSet instanceSet,
			int att) throws Exception {
		int count = instanceSet.numInstances;

		if (count < 2) {
						throw new Exception(resource.getString("emptyInstanceSet"));
		}
		
		if(instanceSet.attributeType[att] == InstanceSet.NOMINAL){
				throw new Exception(resource.getString("nominalAttribute"));
		}

		double MISSING_VALUE = Instance.MISSING_VALUE;
		int counterIndex = instanceSet.counterIndex;
		int classIndex = instanceSet.classIndex;
		int numClasses = instanceSet.numClasses();
		double weight;
		double value;
		double temp;
		int w;
		int classValue;
		double[] sumPerClass = new double[numClasses];
		double[] avgPerClass = new double[numClasses];
		double[] newavgPerClass = new double[numClasses];
		double[] instPerClass = new double[numClasses];
		double[] meanPerClass = new double[numClasses];
		double[] stdDevPerClass = new double[numClasses];

		for (int k = 0; k < numClasses; k++) {
			sumPerClass[k] = 0;
			instPerClass[k] = 0;
			meanPerClass[k] = 0;
		}
		
		for (int i = 0; i < count; i++) {
			value = instanceSet.instances[i].data[att];
			classValue = (int) instanceSet.instances[i].data[classIndex];
			weight = instanceSet.instances[i].data[counterIndex];
			meanPerClass[classValue] += value * weight;
			
			/* Check if current value or class valus is a missing value */
			if (classValue == MISSING_VALUE || value == MISSING_VALUE) {
				/* Skip missing value and missing class value*/
				continue;
			}
			
			if (i == 0) {
				/* First loop. Initiate 'avgPerClass' with first value */
				avgPerClass[classValue] = value;
				++instPerClass[classValue];
				
				/* Skip first value */
				w = 1;
			} else {
				w = 0;
			}

			for (; w < weight; w++) {
				newavgPerClass[classValue] = value - avgPerClass[classValue];
				newavgPerClass[classValue] /= (instPerClass[classValue] + 1);
				newavgPerClass[classValue] += avgPerClass[classValue];
				temp = (value - avgPerClass[classValue]);
				temp *= (value - newavgPerClass[classValue]);
				sumPerClass[classValue] += temp;
				avgPerClass[classValue] = newavgPerClass[classValue];
				++instPerClass[classValue];
			}
		}
		for (int k = 0; k < numClasses; k++) {
			meanPerClass[k] = meanPerClass[k] / instPerClass[k];
			temp = sumPerClass[k] / (instPerClass[k] - 1);
			stdDevPerClass[k] = (double) Math.sqrt(temp);
		}
		
		ArrayList<double[]> list = new ArrayList<double[]>(2);
		list.add(0, stdDevPerClass);
		list.add(1, meanPerClass);
		return list;
	}

	/**
	 * Calculates the standard deviation of a specific attribute of an instance set given the attribute mean.
	 *
	 * @param instanceSet The instanceSet that contains the attribute witch the standard deviation is to be calculated
	 * @param attribute The attribute to calculate the standard deviation
	 * @param mean The mean of the desired standard deviation attribute
	 * @return the standard deviation of the desired attribute
	 * @throws Exception
	 */
	public static double standardDeviation(InstanceSet instanceSet, int attribute, double mean) throws Exception{
		Enumeration instancesEnum = instanceSet.enumerateInstances();
		Instance instance;
		Attribute att = instanceSet.getAttribute(attribute);
		double sigma = 0;
		double sqrSum = 0;
		double temp;
		int numOfInstances = instanceSet.numInstances();

		if(numOfInstances==0){
			throw new Exception(resource.getString("emptyInstanceSet"));
		}
		if(att.isNominal()){
			throw new Exception(resource.getString("nominalAttribute"));
		}

		while(instancesEnum.hasMoreElements()){
			instance = (Instance)instancesEnum.nextElement();
			temp = instance.data[attribute];
			temp = temp-mean;
			sqrSum = sqrSum+(temp*temp);
		}

		sigma = Math.sqrt(sqrSum/(numOfInstances-1));

		return sigma;
	}

	/**
	 * Calculate the mean of an specified attribute of an instanceSet
	 *
	 * @param instanceSet The instanceSet that contains the attribute to calculate the mean
	 * @param attribute The attribute to calculate mean
	 * @return The mean of the specified attribute
	 * @throws Exception
	 */
	public static double mean(InstanceSet instanceSet, int attribute) throws Exception{
		Enumeration instancesEnum = instanceSet.enumerateInstances();
		Instance instance;
		Attribute att = instanceSet.getAttribute(attribute);
		double sum = 0;
		int numOfInstances = instanceSet.numInstances();

		if(numOfInstances == 0){
			throw new Exception(resource.getString("emptyInstanceSet"));
		}
		if(att.isNominal()){
			throw new Exception(resource.getString("nominalAttribute"));
		}

		while(instancesEnum.hasMoreElements()){
			instance = (Instance)instancesEnum.nextElement();
			sum = sum + instance.data[attribute];
		}

		return sum / numOfInstances;
	}
				
	/** 
	 * Set a float number only to keep two digits after dot on a 
	 * string representation
	 * 
	 * @floatValue value desirable to transform
	 * @return string relative to floatValue with only two digits after dot
	 *	 
	 * */
	public static String keep2DigitsAfterDot(float floatValue) {		
		String stringValue = floatValue+"";		
		int index = stringValue.indexOf('.');
		if(stringValue.length()>index+3) {
			stringValue = stringValue.substring(0,index+3);
		}
		
		return stringValue;
	}

	/**
	 * Compute the necessary frequencies for a nominal attribute. 
	 */
	public static float[][] computeNominalDistribution(InstanceSet instanceSet,
			int att) {
		int numValues;
		int classValue;
		int instValue;
		float weight;
		int numInstances = instanceSet.numInstances;
		int counterIndex = instanceSet.counterIndex;
		int classIndex = instanceSet.classIndex;
		int numClasses = instanceSet.numClasses();
		float[][] distribution;

		numValues = instanceSet.attributes[att].numValues();
		distribution = new float[numValues][numClasses];
		
		/* Zero all distribution values */
		for (int i = 0; i < numValues; i++) {
			for (int j = 0; j < numClasses; j++) {
				distribution[i][j] = 0;
			}
		}
		
		/* Compute distribution values */
		for (int inst = 0; inst < numInstances; inst++) {
			instValue = (int) instanceSet.instances[inst].data[att];
			classValue = (int) instanceSet.instances[inst].data[classIndex];
			weight = instanceSet.instances[inst].data[counterIndex];
			distribution[instValue][classValue] += weight;
		}
			
		return distribution;
	}

	/**
	 * Compute the nominal distributions per class for all nominal attributes.
	 * 
	 * 
	 * @param instanceSet The input instanceSet
	 * @return A matrix with this positions: distribution[class][attribute][value]
	 */
	public static float[][][] computeNominalDistributions(InstanceSet instanceSet) 
	 throws Exception {
		int attIndex = 0;
		int numValues;
		int classValue;
		int instValue;
		float weight;
		int numInstances = instanceSet.numInstances;
		int counterIndex = instanceSet.counterIndex;
		int classIndex = instanceSet.classIndex;
		int numAttributes = instanceSet.numAttributes;
		byte[] attributeType = instanceSet.attributeType;
		int numNominalAttributes = instanceSet.numNominalAttributes;
		int numClasses = 0;
		if (classIndex != -1) {
			numClasses = instanceSet.numClasses();

			/* The class is a nominal attribute as well. Must be not counted */
			--numNominalAttributes;
		}
		
		float[][][] distribution = new float[numClasses][numNominalAttributes][];

		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (att == classIndex) {
				continue;
			}

			/* Check if the current attribute is nominal */
			if (attributeType[att] == InstanceSet.NOMINAL) {
				numValues = instanceSet.attributes[att].numValues();
				for (int k = 0; k < numClasses; k++) {
					distribution[k][attIndex] = new float[numValues];
				}
				
				/* Zero all distribution values */
				for (int k = 0; k < numClasses; k++) {
					for (int v = 0; v < numValues; v++) {
						distribution[k][attIndex][v] = 0;
					}
				}
				
				/* Compute distribution values */
				for (int inst = 0; inst < numInstances; inst++) {
					instValue = (int) instanceSet.instances[inst].data[att];
					classValue = (int) instanceSet.instances[inst].data[classIndex];
					weight = instanceSet.instances[inst].data[counterIndex];
					distribution[classValue][attIndex][instValue] += weight;
				}
				
				/* Next nominal attribute index */
				++attIndex;
			}
		}
		
		return distribution;
	}

	public static double normalDensityFunction(float value, float stdDev, float mean) {
		double prob;
		double e;
		
		prob = 1 / (float) Math.sqrt(2 * (float) Math.PI * stdDev);
		e = (value - mean) * (value - mean) / (2 * mean * mean);
		e = (float) Math.exp(-e);
		prob = prob * e;
		
		return prob;
	}
	
	public static void randomize(int[] index, Random random) {
		int inst;
		int size = index.length;
		int temp;
		
		for (int i = size - 1; i > 0; i--) {
			/* Randomly get an instance index */ 
			inst = (int) (random.nextDouble() * (double) i);
			
			/* swap index with the current instance */
			temp = index[i];
			index[i] = index[inst];
			index[inst] = temp;
		}
	}
	
}