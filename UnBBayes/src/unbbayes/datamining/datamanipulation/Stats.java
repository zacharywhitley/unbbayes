package unbbayes.datamining.datamanipulation;

/**
 * A class to store simple statistics
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class Stats 
{ /** The number of values seen */
  private double count = 0;

  /** The sum of values seen */
  private double sum = 0;

  /** The sum of values squared seen */
  private double sumSq = 0;

  /** The standard deviation of values at the last calculateDerived() call */    
  private double stdDev = Double.NaN;

  /** The mean of values at the last calculateDerived() call */    
  private double mean = Double.NaN;

  /** The minimum value seen, or Double.NaN if no values seen */
  private double min = Double.NaN;

  /** The maximum value seen, or Double.NaN if no values seen */
  private double max = Double.NaN;
  
  /** Returns the number of values seen
  	@return Number of values seen
  */	
  public double getCount()
  {	return count;
  }
  
  /** Returns the sum of values seen
  	@return Sum
	*/
  public double getSum()
  {	return sum;
  }
  
  /** Returns the sum of values squared seen
  	@return Sum of values squared
	*/
  public double getSumSq()
  {	return sumSq;
  }
  
  /** Return the standard deviation of values at the last calculateDerived() call
  	@return Standard deviation of values
	*/
  public double getStdDev()
  {	return stdDev;
  }
  
  /** Returns the mean of values at the last calculateDerived() call
  	@return Mean of values
	*/
  public double getMean()
  {	return mean;
  }
  
  /** Returns the minimum value seen, or Double.NaN if no values seen 
  	@return Minimum value seen
  */
  public double getMin()
  {	return min;
  }
  
  /** Returns the maximum value seen, or Double.NaN if no values seen 
  	@return Maximum value seen
  */
  public double getMax()
  {	return max;
  }
  
  /**
   * Adds a value to the observed values
   *
   * @param value the observed value
   */
  public void add(double value) 
  {	add(value, 1);
  }

  /**
   * Adds a value that has been seen n times to the observed values
   *
   * @param value the observed value
   * @param n the number of times to add value
   */
  public void add(double value, double n) 
  {	sum += value * n;
    sumSq += value * value * n;
    count += n;
    if (Double.isNaN(min)) 
	{	min = max = value;
    } 
	else if (value < min) 
	{	min = value;
    } 
	else if (value > max) 
	{	max = value;
    }
  }

  /**
   * Removes a value to the observed values (no checking is done
   * that the value being removed was actually added). 
   *
   * @param value the observed value
   */
  public void subtract(double value) 
  { sum -= value;
    sumSq -= value * value;
    count --;
  }

  /**
   * Tells the object to calculate any statistics that don't have their
   * values automatically updated during add. Currently updates the mean
   * and standard deviation.
   */
  public void calculateDerived() 
  { mean = Double.NaN;
    stdDev = Double.NaN;
    if (count > 0) 
	{	mean = sum / count;
      	stdDev = Double.POSITIVE_INFINITY;
      	if (count > 1) 
		{	stdDev = sumSq - (sum * sum) / count;
			stdDev /= (count - 1);
        	if (stdDev < 0) 
			{	stdDev = 0;
        	}
			stdDev = Math.sqrt(stdDev);
      	}
    }
  }
    
  /**
   * Returns a string summarising the stats so far.
   *
   * @return The summary string
   */
  public String toString() 
  {	calculateDerived();
    return
      "Count   " + Utils.doubleToString(count, 8) + '\n'
      + "Min     " + Utils.doubleToString(min, 8) + '\n'
      + "Max     " + Utils.doubleToString(max, 8) + '\n'
      + "Sum     " + Utils.doubleToString(sum, 8) + '\n'
      + "SumSq   " + Utils.doubleToString(sumSq, 8) + '\n'
      + "Mean    " + Utils.doubleToString(mean, 8) + '\n'
      + "StdDev  " + Utils.doubleToString(stdDev, 8) + '\n';
  }

} 

