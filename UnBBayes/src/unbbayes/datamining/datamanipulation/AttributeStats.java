package unbbayes.datamining.datamanipulation;

/**
 * A Utility class that contains summary information on an
 * the values that appear in a dataset for a particular attribute.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class AttributeStats 
{ /** The number of int-like values */
  private int intCount = 0;
  
  /** The number of real-like values (i.e. have a fractional part) */
  private int realCount = 0;
  
  /** The number of missing values */
  private int missingCount = 0;
  
  /** The number of distinct values */
  private int distinctCount = 0;
  
  /** The number of instances */
  private int totalCount = 0;
  
  /** Stats on numeric value distributions */
  private Stats numericStats;
  
  /** Counts of each nominal value */
  private int[] nominalCounts;
  
  /** Constant set for numeric attributes. */
  public final static int NUMERIC = 0;

  /** Constant set for nominal attributes. */
  public final static int NOMINAL = 1;

  /** Constructor that defines the type of Attribute will be manipulated and the number
  	of values associated with this Attribute. If Attribute is numeric numValues will not 
	be considerated.
	@param attributeType Type of Attribute (Nominal or Numeric)
	@param numValues Number of values associated with an Attribute
	*/
  public AttributeStats(int attributeType,int numValues)
  {	if (attributeType == NOMINAL)
  	{	nominalCounts = new int [numValues];
	}
	else if (attributeType == NUMERIC)
	{	numericStats = new Stats();
	}
  }
  
  /** Set number of instances
  	@param totalCount Number of instances
	*/
  public void setTotalCount(int totalCount)
  {	this.totalCount = totalCount;
  }
  
  /** Get number of instances
  	@return Number of instances
	*/
  public int getTotalCount()
  {	return totalCount;
  }
  
  /** Set the number of distinct values 
  	@param distinctCount Number of distinct values	
  */
  public void setDistinctCount(int distinctCount)
  {	this.distinctCount = distinctCount;
  }
  
  /** Get the number of distinct values 
  	@return Number of distinct values	
  */
  public int getDistinctCount()
  {	return distinctCount;
  }
  
  /** Set the number of missing values 
  	@param missingCount Number of missing values
	*/
  public void setMissingCount(int missingCount)
  { this.missingCount = missingCount;
  }
  
  /** Get the number of missing values 
  	@return Number of missing values
	*/
  public int getMissingCount()
  {	return missingCount;
  }
  
  /** Return the number of counts for each nominal value. If Attribute is numeric returns
  	null
	@return Counts for each nominal value
	*/
  public int[] getNominalCounts()
  {	return nominalCounts;
  }
  
  /** Return a Stats object with some simple statics for a numeric Attribute. If attribute
  	is nominal returns null
  	@return Simple statistics
	*/
  public Stats getNumericStats()
  {	return numericStats;
  }  
    
  /**
   * Updates the counters for one more observed distinct value.
   *
   * @param value the value that has just been seen
   * @param count the number of times the value appeared
   */
  protected void addDistinct(double value, int count) 
  {	if (count > 0) 
  	{	if (Utils.eq(value, (double)((int)value))) 
  		{	intCount += count;
    	} 
		else 
		{	realCount += count;
    	}
    	if (nominalCounts != null) 
		{	nominalCounts[(int)value] = count;
    	}
    	else if (numericStats != null) 
		{	numericStats.add(value, count);
	  		numericStats.calculateDerived();
    	}
    }
	distinctCount++;	
  }
  
  /**
   * Returns a string summarising the stats so far.
   *
   * @return The summary string
   */
  public String toString() 
  {	return
      "Int Count " + intCount + '\n'
      + "Real Count " + realCount + '\n'
      + "Missing Count " + missingCount + '\n'
      + "Distinct Count " + distinctCount + '\n'
      + "Total Count " + totalCount + '\n';
  }

}
