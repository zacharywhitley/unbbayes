package unbbayes.datamining.datamanipulation;

/**
 * Class used to keep UnBMiner's global options 
 */
public class Options
{	/** The only instance of this class */
	private static Options singleton;
	
	//global options
	private int numberStatesAllowed;
	private int confidenceLimit;
	private boolean compactedFile = false;
	
	//c4.5 options
	/** defines if gain ratio is used to build the tree or not */
	private boolean usingGainRatio = false;
	/** defines if prunning is used to build the tree or not */
	private boolean usingPrunning = false;
	/** verbosity level about the tree construction */
	private int verbosityLevel = 1;
	/** confidence level of prunning */
	private float confidenceLevel = 0.01f;		
	
		
	/** Default constructor. Can only be instanced by method getInstance. */
	protected Options(){}

	/** 
	 * Returns an instance of this class. If the class was already instanced, this
	 * object is returned, otherwise a new instance is returned
	 *
	 *	@return the only instance of this class 
	 */
	public static Options getInstance() 
	{	if (singleton == null)
		{	singleton = new Options();
		}	
		return singleton;
	}
	
	public void setNumberStatesAllowed(int numberStatesAllowed)
	{	this.numberStatesAllowed = numberStatesAllowed;
	}	
	
	public int getNumberStatesAllowed()
	{	return numberStatesAllowed;
	}
	
	public void setCompactedFile(boolean compactedFile)
	{	this.compactedFile = compactedFile;
	}	
	
	public boolean getCompactedFile()
	{	return compactedFile;
	}	
	
	public void setConfidenceLimit(int confidenceLimit)
  	{	this.confidenceLimit = confidenceLimit;
  	}
  
  	public int getConfidenceLimit()
  	{	return confidenceLimit;
  	}
  	
  	/**
	 * Sets if gain ratio is used or not
	 * 
	 * @param usingGainRatio true if gain ratio is used, false otherwise
	 */
  	public void setIfUsingGainRatio(boolean usingGainRatio)
  	{
  		this.usingGainRatio = usingGainRatio;
  	}
  	
	/**
	 * Tests if gain ratio is used or not
	 * 
	 * @return true if gain ratio is used, false otherwise
	 */
  	public boolean getIfUsingGainRatio()
  	{
  		return usingGainRatio;  
  	}
  	
	/**
	 * Sets if prunning is used or not
	 * 
	 * @param usingPrunning true if prunning is used, false otherwise
	 */
	public void setIfUsingPrunning(boolean usingPrunning)
	{
		this.usingPrunning = usingPrunning;
	}
  	
	/**
	 * Tests if prunning is used or not
	 * 
	 * @return true if prunning is used, false otherwise
	 */
	public boolean getIfUsingPrunning()
	{
		return usingPrunning;  
	}
  	
	/**
	 * Sets the verbosity level
	 * 
	 * @param verbosityLevel verbosity level
	 */
	public void setVerbosityLevel(int verbosityLevel)
	{
		if (verbosityLevel<1)
		{
			this.verbosityLevel = 1;
		}
		else if (verbosityLevel>4)
		{
			this.verbosityLevel = 4;
		}
		else
		{
			this.verbosityLevel = verbosityLevel;
		}
	}
  	
	/**
	 * Return the verbosity level
	 * 
	 * @return verbosity level
	 */
	public int getVerbosityLevel()
	{
		return verbosityLevel;  
	}
	
	/**
	 * Sets the confidence level
	 * 
	 * @param confidenceLevel confidence level
	 */
	public void setConfidenceLevel(float confidenceLevel)
	{
		if (confidenceLevel<0)
		{
			this.confidenceLevel = 0;
		}
		else if (confidenceLevel>1)
		{
			this.confidenceLevel = 1;
		}
		else
		{
			this.confidenceLevel = confidenceLevel;
		}
	}
  	
  	/**
  	 * Return the confidence level of prunning
  	 * 
  	 * @return confidence level of prunning
  	 */
  	public float getConfidenceLevel()
	{
		return confidenceLevel;  
	}
}
