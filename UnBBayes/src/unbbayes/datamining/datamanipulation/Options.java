package unbbayes.datamining.datamanipulation;

public class Options
{	/** Uma inst�ncia deste objeto */
	private static Options singleton;
	
	private int numberStatesAllowed;
	private boolean compactedFile = false;
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
	
	/*public void setCounterAttribute(int counterAttribute)
	{	this.counterAttribute = counterAttribute;
		compactedFile = true;	
	}
	
	public int getCounterAttribute()
	{	return counterAttribute;
	}*/	
}