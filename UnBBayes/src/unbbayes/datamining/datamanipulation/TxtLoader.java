package unbbayes.datamining.datamanipulation;

import java.awt.Component;
import java.io.*;
import java.util.*;

/** This class opens a txt file building an InstanceSet object
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class TxtLoader extends Loader
{	/** The filename extension that should be used for arff files */
  	public static final String FILE_EXTENSION = ".txt";
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");
  	
	private int maximumStatesAllowed = 40;
	
  	/**
   	* Reads a TXT file from a reader.
   	*
   	* @param reader Reader
   	* @exception IOException if the TXT file is not read 
   	* successfully
   	*/  	
	public TxtLoader(Reader reader) throws IOException 
  	{	StreamTokenizer tokenizer = new StreamTokenizer(reader);
		instances = new InstanceSet();
		initTokenizer(tokenizer);
    	readHeader(tokenizer);
		while (getInstance(tokenizer)) {};
		maximumStatesAllowed = Options.getInstance().getNumberStatesAllowed();
		checkNumericAttributes();
	}
	
	public TxtLoader(Reader reader,Component component) throws IOException 
  	{	StreamTokenizer tokenizer = new StreamTokenizer(reader);
		instances = new InstanceSet();
		initTokenizer(tokenizer);
    	readHeader(tokenizer);
		new CompactFileDialog(this,component);
    	while (getInstance(tokenizer)) {};
		if (counterAttribute >= 0)
		{	instances.removeAttribute(counterAttribute);
		}
		maximumStatesAllowed = Options.getInstance().getNumberStatesAllowed();		
		checkNumericAttributes();
	}
	
	/**
   	* Initializes the StreamTokenizer used for reading the TXT file.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	protected void initTokenizer(StreamTokenizer tokenizer)
	{	tokenizer.wordChars('a', '}');
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('.', '.');
		tokenizer.wordChars('A', 'Z');
		tokenizer.commentChar('%');
    	tokenizer.quoteChar('\t');
    	tokenizer.eolIsSignificant(true);
  	}
	
	/**
   	* Reads and stores header of a TXT file.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if the information is not read 
   	* successfully
   	*/ 
  	protected void readHeader(StreamTokenizer tokenizer) throws IOException
	{	ArrayList attributeValues = null;
		//Insert attributes in the new dataset
		getNextToken(tokenizer);
		while (tokenizer.ttype != StreamTokenizer.TT_EOL)
		{	if(tokenizer.sval != null)
			{	instances.insertAttribute(new Attribute(tokenizer.sval,attributeValues,Attribute.NOMINAL,instances.numAttributes()));
	  		}
			else
			{	instances.insertAttribute(new Attribute(String.valueOf(tokenizer.nval),attributeValues,Attribute.NUMERIC,instances.numAttributes()));	
			}
			tokenizer.nextToken();
		}
	}  
	
	/**
   	* Reads a single instance using the tokenizer and appends it
   	* to the dataset. Automatically expands the dataset if it
   	* is not large enough to hold the instance.
   	*
   	* @param tokenizer Tokenizer to be used
   	* @return False if end of file has been reached
   	* @exception IOException if the information is not read 
   	* successfully
   	*/ 
  	protected boolean getInstance(StreamTokenizer tokenizer) throws IOException 
	{	// Check if any attributes have been declared.
    	if (instances.numAttributes() == 0) 
		{	errms(tokenizer,resource.getString("getInstanceTXT"));
    	}

    	// Check if end of file reached.
    	getFirstToken(tokenizer);
    	if (tokenizer.ttype == StreamTokenizer.TT_EOF) 
		{	return false;
    	}
    
    	return getInstanceFull(tokenizer);
  	}
	
	/**
   	* Reads a single instance using the tokenizer and appends it
   	* to the dataset. Automatically expands the dataset if it
   	* is not large enough to hold the instance.
   	*
   	* @param tokenizer Tokenizer to be used
   	* @return False if end of file has been reached
   	* @exception IOException if the information is not read 
   	* successfully
   	*/ 
  	protected boolean getInstanceFull(StreamTokenizer tokenizer) throws IOException 
	{	short[] instance;
		if (counterAttribute >= 0)
		{	instance = new short[instances.numAttributes() - 1];
		}
		else
		{	instance = new short[instances.numAttributes()];
		}
		int instanceWeight = 1;    	
		int posicao = 0,index = 0;
		String nomeEstado = "";
		
		//Create instances
  		while(posicao < instances.numAttributes())
		{	Attribute att = instances.getAttribute(posicao);
			//Insert new value for attribute if number of values is 0 or this value isn't inserted
			if(tokenizer.sval != null)
			{	nomeEstado = tokenizer.sval;
				if (att.numValues()==0 || att.indexOfValue(nomeEstado) == -1)
				{	att.addValue(nomeEstado);
				}
			}
			else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)			
			{	nomeEstado = String.valueOf(tokenizer.nval);
				if (att.numValues()==0 || att.indexOfValue(nomeEstado) == -1)
				{	att.addValue(nomeEstado);
				}
			}
			else
			{}
			if (counterAttribute == posicao)
			{	try
				{	instanceWeight = (int)tokenizer.nval;
				}
				catch(NumberFormatException nfe)
				{	errms(tokenizer,"Atributo de contagem inválido");
				}
				catch(Exception exc)
				{	errms(tokenizer,"erro "+exc.getMessage());
				}
			}
			else			
			{	if (tokenizer.ttype == StreamTokenizer.TT_WORD) 
				{	// Check if value is missing.
      				if (tokenizer.ttype == '?') 
					{	instance[posicao] = Instance.missingValue();
      				} 
					else
					{	if (instances.getAttribute(posicao).isNominal()) 
						{	// Check if value appears in header.
	  						index = att.indexOfValue(tokenizer.sval);
	  						instance[posicao] = (short)index;
						} 
					}
				}
				else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
				{	if (instances.getAttribute(posicao).isNominal()) 
					{	// Check if value appears in header.
	  					index = att.indexOfValue(tokenizer.nval+"");
	  					instance[posicao] = (short)index;
					}					
				}
			}
			posicao++;
			tokenizer.nextToken();
		}
		
		// Add instance to dataset
    	add(new Instance(instanceWeight,instance));
    	return true;		
	}
	
	/**
   	* Gets next token, skipping empty lines.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if reading the next token fails
   	*/
  	protected void getFirstToken(StreamTokenizer tokenizer) throws IOException
	{	while (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
		{};
  	}
  
	/**
   	* Gets next token, checking for a premature end of line.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if it finds a premature end of line
   	*/
  	protected void getNextToken(StreamTokenizer tokenizer) throws IOException
	{	if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) 
		{	errms(tokenizer,resource.getString("getNextTokenException1"));
    	}
    	if (tokenizer.ttype == StreamTokenizer.TT_EOF) 
		{	errms(tokenizer,resource.getString("getNextTokenException2"));
    	}
  	}
	
	/** Changes all nominal attributes that can be parsed to numeric attributes
	*/ 
	private void checkNumericAttributes()
	{	int numAttributes = instances.numAttributes();
		for (int i = 0; i < numAttributes; i++)
		{	Attribute att = instances.getAttribute(i);
			if (att.numValues() > maximumStatesAllowed)
			{	boolean bool = checkNominal(att);
				if (bool == false)
					att.setAttributeType(att.NUMERIC);
			}	
    	}
    	
	}
	
	/** If all values from a nominal attribute can be parsed to float returns false
		@param att An attribute
		@return False if nominal attribute can be parsed to float
	*/
	private boolean checkNominal(Attribute att)
	{	int numValues = att.numValues();
		for (int j = 0; j<numValues; j++)
		{	String value = att.value(j);
			if (value.equals(""))
				return true;
			try
			{	Float.parseFloat(value);
			}
			catch (NumberFormatException nfe)
			{	return true;
			}
		}
		return false;
	}
  
}