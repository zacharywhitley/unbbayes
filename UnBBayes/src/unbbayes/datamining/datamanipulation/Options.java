package unbbayes.datamining.datamanipulation;

public class Options
{	/** Uma inst�ncia deste objeto */
	private static Options singleton;
	
	private int numberStatesAllowed;
	private int confidenceLimit;
	private boolean compactedFile = false;
	//id3 options
	private boolean usingGainRatio = false;
	private int verbosityLevel = 1;		
	//private int counterAttribute = -1;
	
	/** Construtor padr�o. S� pode ser instanciado pelo m�todo getInstance. */
	protected Options() {}

	/** Retorna uma inst�ncia deste objeto. Se o objeto j� estiver instanciado retorna o
		objeto atual, sen�o retorna uma nova inst�ncia do objeto.
		@return Um objeto Options
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
  	
  	public void setIfUsingGainRatio(boolean usingGainRatio)
  	{
  		this.usingGainRatio = usingGainRatio;
  	}
  	
  	public boolean getIfUsingGainRatio()
  	{
  		return usingGainRatio;  
  	}
  	
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
  	
	public int getVerbosityLevel()
	{
		return verbosityLevel;  
	}
	
	/*public void setCounterAttribute(int counterAttribute)
	{	this.counterAttribute = counterAttribute;
		compactedFile = true;	
	}
	
	public int getCounterAttribute()
	{	return counterAttribute;
	}*/	
}
