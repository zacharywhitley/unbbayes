package unbbayes.datamining.datamanipulation;

import java.lang.Math;
import java.util.*;

/**
 * Class implementing some simple utility methods.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public final class Utils 
{ /** The natural logarithm of 2. */
  public static double LOG2 = Math.log(2);

  /** The small deviation allowed in double comparisons */
  public static double SMALL = 1e-6;

  /** Load resource file from this package */
  private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");
  
  /**
   * Tests if a is equal to b.
   *
   * @param a A double
   * @param b A double
   */
  public static boolean eq(double a, double b)
  {	return (a - b < SMALL) && (b - a < SMALL); 
  }
  public static boolean eq(short a, short b)
  {	if (a == b)
  	{	return true;
	}
	else
	{	return false;
	}	
  }

  /**
   * Tests if a is smaller than b.
   *
   * @param a A double
   * @param b A double 
   */
  public static boolean gr(double a,double b) 
  {	return (a-b > SMALL);
  }

  /**
   * Returns the logarithm of a for base 2.
   *
   * @param a A double
   */
  public static double log2(double a) 
  {	return Math.log(a) / LOG2;
  }

  /**
   * Returns index of maximum element in a given
   * array of doubles. First maximum is returned.
   *
   * @param doubles The array of doubles
   * @return The index of the maximum element
   */
  public static int maxIndex(double [] doubles) 
  {	double maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < doubles.length; i++) 
	{	if ((i == 0) || (doubles[i] > maximum)) 
		{	maxIndex = i;
			maximum = doubles[i];
      	}
    }

    return maxIndex;
  }

  /**
   * Returns index of maximum element in a given
   * array of floats. First maximum is returned.
   *
   * @param floats The array of floats
   * @return The index of the maximum element
   */
  public static int maxIndex(float[] floats) 
  {	float maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < floats.length; i++) 
  	{	if ((i == 0) || (floats[i] > maximum)) 
  		{	maxIndex = i;
  			maximum = floats[i];
      	}
    }

    return maxIndex;
  }
  
  /**
   * Returns index of maximum element in a given
   * array of integers. First maximum is returned.
   *
   * @param ints The array of integers
   * @return The index of the maximum element
   */
  public static int maxIndex(int [] ints) 
  { int maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < ints.length; i++) 
	{	if ((i == 0) || (ints[i] > maximum)) 
		{	maxIndex = i;
			maximum = ints[i];
      	}
    }

    return maxIndex;
  }
  
  /**
   * Returns minimum element in a given array of doubles.
   *
   * @param doubles The array of doubles
   * @return Minimum element
   */
  public static double min(double [] doubles) 
  {	double minimum = Double.MAX_VALUE;
    
    for (int i = 0; i < doubles.length; i++) 
	{	if (doubles[i] < minimum) 
		{	minimum = doubles[i];
      	}
    }

    return minimum;
  }
  
  public static float min(float[] floats) 
  {	float minimum = Float.MAX_VALUE;
    
    for (int i = 0; i < floats.length; i++) 
	{	if (floats[i] < minimum) 
		{	minimum = floats[i];
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
  public static void normalize(double[] doubles) 
  { double sum = 0;
    for (int i = 0; i < doubles.length; i++) 
	{	sum += doubles[i];
    }
    normalize(doubles, sum);
  }

  /**
   * Normalizes the floats in the array by their sum.
   *
   * @param floats The array of float
   * @exception IllegalArgumentException if sum is Zero or NaN
   */
  public static void normalize(float[] floats) 
  { float sum = 0;
    for (int i = 0; i < floats.length; i++) 
	{	sum += floats[i];
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
  public static void normalize(double[] doubles, double sum) 
  { if (Double.isNaN(sum)) 
  	{	throw new IllegalArgumentException(resource.getString("normalizeException1"));
    }
    if (sum == 0) 
	{	throw new IllegalArgumentException(resource.getString("normalizeException2"));
    }
    for (int i = 0; i < doubles.length; i++) 
	{	doubles[i] /= sum;
    }
  }
  
  /**
   * Normalizes the floats in the array using the given value.
   *
   * @param floats The array of float
   * @param sum The value by which the floats are to be normalized
   * @exception IllegalArgumentException if sum is zero or NaN
   */
  public static void normalize(float[] floats, float sum) 
  { if (Float.isNaN(sum)) 
  	{	throw new IllegalArgumentException(resource.getString("normalizeException1"));
    }
    if (sum == 0) 
	{	throw new IllegalArgumentException(resource.getString("normalizeException2"));
    }
    for (int i = 0; i < floats.length; i++) 
	{	floats[i] /= sum;
    }
  }

  /**
   * Computes the sum of the elements of an array of doubles.
   *
   * @param doubles The array of double
   * @returns The sum of the elements
   */
  public static double sum(double[] doubles) 
  {	double sum = 0;

    for (int i = 0; i < doubles.length; i++) 
	{ sum += doubles[i];
    }
    return sum;
  }
  
  /**
   * Computes the sum of the elements of an array of floats.
   *
   * @param floats The array of float
   * @returns The sum of the elements
   */
  public static float sum(float[] floats) 
  {	float sum = 0;

    for (int i = 0; i < floats.length; i++) 
	{ sum += floats[i];
    }
    return sum;
  }
   
   /**
   * Computes the sum of the elements of an array of integers.
   *
   * @param ints The array of integers
   * @returns The sum of the elements
   */
  public static int sum(int[] ints) 
  { int sum = 0;

    for (int i = 0; i < ints.length; i++) 
	{ sum += ints[i];
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
  public static String doubleToString(double value, int afterDecimalPoint) 
  {	StringBuffer stringBuffer;
    double temp;
    int i,dotPosition;
    long precisionValue;
    
    temp = value * Math.pow(10.0, afterDecimalPoint);
    if (Math.abs(temp) < Long.MAX_VALUE) 
	{	precisionValue = (temp > 0) ? (long)(temp + 0.5) : -(long)(Math.abs(temp) + 0.5);
      	if (precisionValue == 0) 
		{	stringBuffer = new StringBuffer(String.valueOf(0));
      	} 
		else 
		{	stringBuffer = new StringBuffer(String.valueOf(precisionValue));
      	}
      	if (afterDecimalPoint == 0) 
		{	return stringBuffer.toString();
      	}
      	dotPosition = stringBuffer.length() - afterDecimalPoint;
      	while (((precisionValue < 0) && (dotPosition < 1)) || (dotPosition < 0)) 
		{	if (precisionValue < 0) 
			{	stringBuffer.insert(1, 0);
			} 
			else 
			{	stringBuffer.insert(0, 0);
			}
			dotPosition++;
      	}
      	stringBuffer.insert(dotPosition, '.');
      	if ((precisionValue < 0) && (stringBuffer.charAt(1) == '.')) 
		{	stringBuffer.insert(1, 0);
      	} 
		else if (stringBuffer.charAt(0) == '.') 
		{	stringBuffer.insert(0, 0);
      	}
      	int currentPos = stringBuffer.length() - 1;
      	while ((currentPos > dotPosition) && (stringBuffer.charAt(currentPos) == '0')) 
		{	stringBuffer.setCharAt(currentPos--, ' ');
      	}
      	if (stringBuffer.charAt(currentPos) == '.') 
		{	stringBuffer.setCharAt(currentPos, ' ');
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
  public static String doubleToString(double value, int width, int afterDecimalPoint) 
  {	String tempString = doubleToString(value, afterDecimalPoint);
    char[] result;
    int dotPosition;

    if ((afterDecimalPoint >= width) || (tempString.indexOf('E') != -1)) 
	{	return tempString;
    }

    // Initialize result
    result = new char[width];
    for (int i = 0; i < result.length; i++) 
	{	result[i] = ' ';
    }

    if (afterDecimalPoint > 0) 
	{	// Get position of decimal point and insert decimal point
      	dotPosition = tempString.indexOf('.');
      	if (dotPosition == -1) 
		{	dotPosition = tempString.length();
      	} 
		else 
		{	result[width - afterDecimalPoint - 1] = '.';
      	}
    } 
	else 
	{	dotPosition = tempString.length();
    }
    

    int offset = width - afterDecimalPoint - dotPosition;
    if (afterDecimalPoint > 0) 
	{	offset--;
    }

    // Not enough room to decimal align within the supplied width
    if (offset < 0) 
	{	return tempString;
    }

    // Copy characters before decimal point
    for (int i = 0; i < dotPosition; i++) 
	{	result[offset + i] = tempString.charAt(i);
    }

    // Copy characters after decimal point
    for (int i = dotPosition + 1; i < tempString.length(); i++) 
	{	result[offset + i] = tempString.charAt(i);
    }

    return new String(result);
  }
  
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
  public static int[] sort(double [] array) 
  {	int [] index = new int[array.length];
    array = (double [])array.clone();
    for (int i = 0; i < index.length; i++) 
	{	index[i] = i;
      	if (Double.isNaN(array[i])) 
		{	array[i] = Double.MAX_VALUE;
      	}
    }
    quickSort(array, index, 0, array.length - 1);
    return index;
  }
  
  /**
   * Sorts a given array of shorts in ascending order and returns an
   * array of integers with the positions of the elements of the
   * original array in the sorted array. It doesn't use safe floating-point
   * comparisons. 
   *
   * @param array This array is not changed by the method!
   * @return An array of integers with the positions in the sorted
   * array.  
   */  
  public static int[] sort(short[] array) 
  {	int [] index = new int[array.length];
    array = (short[])array.clone();
    for (int i = 0; i < index.length; i++) 
	{	index[i] = i;
    }
    quickSort(array, index, 0, array.length - 1);
    return index;
  }
  
  /**
   * Implements unsafe quicksort for an array of indices.
   *
   * @param array The array of doubles to be sorted
   * @param index The index which should contain the positions in the
   * sorted array
   * @param lo0 The first index of the subset to be sorted
   * @param hi0 The last index of the subset to be sorted
   */
  private static void quickSort(double [] array, int [] index, int lo0, int hi0) 
  { int lo = lo0;
    int hi = hi0;
    double mid;
    int help;
    
    if (hi0 > lo0) 
	{ // Arbitrarily establishing partition element as the midpoint of
      // the array.
      mid = array[index[(lo0 + hi0) / 2]];

      // loop through the array until indices cross
      while (lo <= hi) 
	  {	// find the first element that is greater than or equal to  
		// the partition element starting from the left Index.
		while ((array[index[lo]] < mid) && (lo < hi0)) 
		{	++lo;
		}
	
		// find an element that is smaller than or equal to 
		// the partition element starting from the right Index.
		while ((array[index[hi]] > mid) && (hi > lo0)) 
		{	--hi;
		}
	
		// if the indexes have not crossed, swap
		if (lo <= hi) 
		{	help = index[lo];
	  		index[lo] = index[hi];
	  		index[hi] = help;
	  		++lo;
	  		--hi;
		}
      }
      
      // If the right index has not reached the left side of array
      // must now sort the left partition.
      if (lo0 < hi) 
	  {	quickSort(array, index, lo0, hi);
      }
      
      // If the left index has not reached the right side of array
      // must now sort the right partition.
      if (lo < hi0) 
	  {	quickSort(array, index, lo, hi0);
      }
    }
  }

	/**
   * Implements unsafe quicksort for an array of indices.
   *
   * @param array The array of shorts to be sorted
   * @param index The index which should contain the positions in the
   * sorted array
   * @param lo0 The first index of the subset to be sorted
   * @param hi0 The last index of the subset to be sorted
   */  
private static void quickSort(short[] array, int [] index, int lo0, int hi0) 
{   int lo = lo0;
    int hi = hi0;
    short mid;
    int help;
    
    if (hi0 > lo0) 
	{ // Arbitrarily establishing partition element as the midpoint of
      // the array.
      mid = array[index[(lo0 + hi0) / 2]];

      // loop through the array until indices cross
      while (lo <= hi) 
	  {	// find the first element that is greater than or equal to  
		// the partition element starting from the left Index.
		while ((array[index[lo]] < mid) && (lo < hi0)) 
		{	++lo;
		}
	
		// find an element that is smaller than or equal to 
		// the partition element starting from the right Index.
		while ((array[index[hi]] > mid) && (hi > lo0)) 
		{	--hi;
		}
	
		// if the indexes have not crossed, swap
		if (lo <= hi) 
		{	help = index[lo];
	  		index[lo] = index[hi];
	  		index[hi] = help;
	  		++lo;
	  		--hi;
		}
      }
      
      // If the right index has not reached the left side of array
      // must now sort the left partition.
      if (lo0 < hi) 
	  {	quickSort(array, index, lo0, hi);
      }
      
      // If the left index has not reached the right side of array
      // must now sort the right partition.
      if (lo < hi0) 
	  {	quickSort(array, index, lo, hi0);
      }
    }
}
  
  /** Sort values in a array of doubles and returns an array of doubles
  	with the sum of equal values. Original array will be modified
	@param values The array of double
	@return An array with the sum of equal values from original array
	*/
  public static double[] getDistribution(double[] values)
  {   int size = values.length;
      int j=0;
      double currentValue = values[0];
      double[] resultArray = new double[size + 1];
      Arrays.sort(values);
      for (int i=0; i<size; i++)
      {   if (values[i]==currentValue)
          {   resultArray[j] += values[i];
          }
          else
          {   j++;
              resultArray[j] = values[i];
              currentValue = values[i];
          }
      }
      double[] resultArray2 = new double[j];
      System.arraycopy(resultArray,1,resultArray2,0,resultArray2.length);
      return resultArray2;
  }

  /** Sort values in a array of floats and returns an array of floats
  	with the sum of equal values. Original array will be modified
	@param values The array of float
	@return An array with the sum of equal values from original array
	*/
  public static float[] getDistribution(float[] values)
  {   int size = values.length;
      int j=0;
      float currentValue = values[0];
      float[] resultArray = new float[size + 1];
      Arrays.sort(values);
      for (int i=0; i<size; i++)
      {   if (values[i]==currentValue)
          {   resultArray[j] += values[i];
          }
          else
          {   j++;
              resultArray[j] = values[i];
              currentValue = values[i];
          }
      }
      float[] resultArray2 = new float[j];
      System.arraycopy(resultArray,1,resultArray2,0,resultArray2.length);
      return resultArray2;
  }

/** Sort values in a array of doubles and returns an array of doubles
  	with the frequency of values seen. Original array will be modified
	@param values The array of double
	@return An array with the frequency of values from original array
	*/
  public static double[] getFrequency(double[] values)
  {   int size = values.length;
      int j=0;
      double currentValue = values[0];
      double[] resultArray = new double[size];
      Arrays.sort(values);
      for (int i=0; i<size; i++)
      {   if (values[i]==currentValue)
          {   resultArray[j] ++;
          }
          else
          {   j++;
              resultArray[j] ++;
              currentValue = values[i];
          }
      }
      double[] resultArray2 = new double[j + 1];
      System.arraycopy(resultArray,0,resultArray2,0,resultArray2.length);
      return resultArray2;
  }
  
/** Sort values in a array of floats and returns an array of floats
  	with the frequency of values seen. Original array will be modified
	@param values The array of float
	@return An array with the frequency of values from original array
	*/
  public static float[] getFrequency(float[] values)
  {   int size = values.length;
      int j=0;
      float currentValue = values[0];
      float[] resultArray = new float[size];
      Arrays.sort(values);
      for (int i=0; i<size; i++)
      {   if (values[i]==currentValue)
          {   resultArray[j] ++;
          }
          else
          {   j++;
              resultArray[j] ++;
              currentValue = values[i];
          }
      }
      float[] resultArray2 = new float[j + 1];
      System.arraycopy(resultArray,0,resultArray2,0,resultArray2.length);
      return resultArray2;
  }
  
  /**
   * Computes information gain for an attribute.
   *
   * @param data Data for which info gain is to be computed
   * @param att Attribute
   * @return Information gain for the given attribute and data
   */
  public static double computeInfoGain(InstanceSet data, Attribute att) throws Exception 
  {	int numInstances = data.numWeightedInstances();
  	double infoGain = computeEntropy(data);   
	if (att.isNominal()) 
	{	InstanceSet[] splitData = splitData(data, att);
    	int numValues = att.numValues();
		for (int j = 0; j < numValues; j++) 
  		{	int numInstancesSplit = splitData[j].numWeightedInstances();
			if (numInstancesSplit > 0) 
  			{	infoGain -= ((double) numInstancesSplit / (double) numInstances) * computeEntropy(splitData[j]);
			}
    	}
	}
	else
	{	//Get values and classes from numeric attribute
		float[] values = new float[numInstances];
        short[] classes = new short[numInstances];
		Enumeration enumInst = data.enumerateInstances();
        int i=0,j=0;
        while (enumInst.hasMoreElements())
        {   Instance instance = (Instance)enumInst.nextElement();
           	values[i] = Float.parseFloat(instance.stringValue(att));
			classes[i] = instance.classValue();
       		i++;
        }
		//Sort values and classes
		insertionSortInc(values,classes);
		
		//Detect number of class changes 	
		int numChanged = 0;
		for(i=0; i<(values.length - 1); i++)
			if (classes[i] != classes[i + 1])
				numChanged++;
		
		//Detect which is the class change 	
		float[] changed = new float[numChanged];
		for(i=0; i<(values.length - 1); i++)
			if (classes[i] != classes[i + 1])
			{	changed[j] = (values[i] + values[i + 1])/2;
				j++;
			}	
		
		//Compute entropy
		double[] entropy = new double[numChanged];
		for (j = 0; j < numChanged; j++) 
  		{	InstanceSet splitDataG = new InstanceSet(data, numInstances);
			InstanceSet splitDataS = new InstanceSet(data, numInstances);
			Enumeration instEnum = data.enumerateInstances();
    		int numDataG=0,numDataS=0;
			while (instEnum.hasMoreElements()) 
  			{	Instance inst = (Instance) instEnum.nextElement();
	      		float instanceValue = Float.parseFloat(inst.stringValue(att));
				if (changed[j] > instanceValue)
				{	splitDataG.add(inst);
					numDataG++;	
				}	
				else
				{	splitDataS.add(inst);	
					numDataS++;	
				}	
    		}
			entropy[j] = (((double)numDataG / numInstances * computeEntropy(splitDataG)) + ((double)numDataS / numInstances * computeEntropy(splitDataS)));
    	}
		//Return original infoGain less minimum entropy
		infoGain -= min(entropy);    	
	}
    return infoGain;
  }
  
  public static double computeGainRatio(InstanceSet data, Attribute att) throws Exception
  {	// Compute split info
  	int numInstances = data.numWeightedInstances();
  	double splitInfo = 0;   
	if (att.isNominal()) 
	{	InstanceSet[] splitData = splitData(data, att);
    	int numValues = att.numValues();
		for (int j = 0; j < numValues; j++) 
  		{	int numInstancesSplit = splitData[j].numWeightedInstances();
			if (numInstancesSplit > 0) 
  			{	splitInfo += (-1 * ((double)numInstancesSplit/(double)numInstances) * log2((double)numInstancesSplit/(double)numInstances));
			}
    	}
	}
	// Compute gain ratio
	double gainRatio; 
	if (splitInfo != 0)
		gainRatio = computeInfoGain(data,att)/splitInfo;
	else
		gainRatio = computeInfoGain(data,att);
	return gainRatio;
  }
  
  /**
  * Computes the entropy of a dataset.
  * 
  * @param data Data for which entropy is to be computed
  * @return Entropy of the data's class distribution
  */
  public static double computeEntropy(InstanceSet data) throws Exception 
  {	double [] classCounts = new double[data.numClasses()];
    Enumeration instEnum = data.enumerateInstances();
    while (instEnum.hasMoreElements()) 
  	{	Instance inst = (Instance) instEnum.nextElement();
      	classCounts[(int) inst.classValue()] += inst.getWeight();
    }
    double entropy = 0;
  	int numClasses = data.numClasses();
    for (int j = 0; j < numClasses; j++) 
  	{	if (classCounts[j] > 0) 
  		{	entropy -= classCounts[j] * log2(classCounts[j]);
      	}
    }
    int numWeightedInstances = data.numWeightedInstances();
	entropy /= (double) numWeightedInstances;
	return entropy + log2(numWeightedInstances);
  }
  
  /**
   * Splits a dataset according to the values of an attribute.
   *
   * @param data Data which is to be split
   * @param att Attribute to be used for splitting
   * @return The sets of instances produced by the split
   */
  public static InstanceSet[] splitData(InstanceSet data, Attribute att) 
  {	int numInstances = data.numInstances();
	InstanceSet[] splitData;  
  	int numValues = att.numValues();
	splitData = new InstanceSet[numValues];
	for (int j = 0; j < numValues; j++) 
  	{	splitData[j] = new InstanceSet(data, numInstances);
  	}
    Enumeration instEnum = data.enumerateInstances();
    while (instEnum.hasMoreElements()) 
  	{	Instance inst = (Instance) instEnum.nextElement();
    	splitData[(int) inst.getValue(att)].add(inst);
    }    
	return splitData;
  }
  
  /** Insertion sort incremental of an array of float. An array of short
  	is sort in the same points that the array of float
	@param a An array of float
	@param b An array of short
  */  
  	private static void insertionSortInc(float[] a,short[] b)
	{	int i;
		float key;
		short temp;	
		for(int j=1; j<a.length; j++)
		{	key = a[j];
			temp = b[j];	
			//Insert a[j] into the sorted sequence a[1 .. j-1]
			i = j - 1;
			while ((i > -1) && (a[i] > key)) 
			{	a[i + 1] = a[i];
				b[i + 1] = b[i];	
				i--;
			}
			a[i + 1] = key;
			b[i + 1] = temp;
		}
	}
}
  
