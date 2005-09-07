package unbbayes.datamining.datamanipulation;

/**
 * A Utility class that contains summary information on an
 * the values that appear in a dataset for a particular attribute.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class AttributeStats 
{   
  /** The number of missing values */
  private int missingCount = 0;
  private int missingCountWeighted = 0;
  
  /** The number of distinct values */
  private int distinctCount = 0;
  
  /** Stats on numeric value distributions */
  private Stats numericStats;
  
  /** Counts of each nominal value */
  private int[] nominalCounts;
  private int[] nominalCountsWeighted;
  
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
  {	
  	nominalCounts = new int [numValues];
	nominalCountsWeighted = new int [numValues];
	if (attributeType == NUMERIC)
	{	numericStats = new Stats();
	}
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
  
  /** Set the number of missing values 
	@param missingCount Number of missing values
	*/
  public void setMissingCountWeighted(int missingCountWeighted)
  { this.missingCountWeighted = missingCountWeighted;
  }
  
  /** Get the number of missing values 
	@return Number of missing values
	*/
  public int getMissingCountWeighted()
  {	return missingCountWeighted;
  }
  
  /** Return the number of counts for each nominal value. If Attribute is numeric returns
  	null
	@return Counts for each nominal value
	*/
  public int[] getNominalCounts()
  {	return nominalCounts;
  }
  
  /** Return the number of counts for each nominal value. If Attribute is numeric returns
	null
	@return Counts for each nominal value
	*/
  public int[] getNominalCountsWeighted()
  {	return nominalCountsWeighted;
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
  protected void addDistinct(int value, int count, int countWeighted) 
  {	if (count > 0) 
  	{	
  		nominalCounts[value] = count;
		nominalCountsWeighted[value] = countWeighted;
    	if (numericStats != null) 
		{	numericStats.add(value, count);
	  		numericStats.calculateDerived();
    	}
		distinctCount++;
    }		
  }
  protected void addDistinct(float value, int internalValue,int count, int countWeighted) 
  {	if (count > 0) 
	{	
		nominalCounts[internalValue] = count;
		nominalCountsWeighted[internalValue] = countWeighted;
		if (numericStats != null) 
		{	numericStats.add(value, count);
			numericStats.calculateDerived();
		}
		distinctCount++;
	}		
  }
  
  /**
   * Returns a string summarising the stats so far.
   *
   * @return The summary string
   */
  public String toString() 
  {	
	  StringBuilder result = new StringBuilder();
    result.append("Missing Count " + missingCount + '\n');
	result.append("Missing Count Weighted " + missingCountWeighted + '\n');
	result.append("Distinct Count " + distinctCount + '\n');
	result.append("Counts ");
	for (int i=0;i<nominalCounts.length;i++)
	{
		result.append(nominalCounts[i]+" ");
	}
	result.append("\n");
	result.append("Counts Weighted");
	for (int i=0;i<nominalCountsWeighted.length;i++)
	{
		result.append(nominalCountsWeighted[i]+" ");
	}
	result.append("\n");
    return result.toString();
  }

}
