package unbbayes.datamining.datamanipulation;

import java.awt.Component;
import java.io.*;
import java.util.*;

/** This class opens a arff file building an InstanceSet object
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class ArffLoader extends Loader
{	/** The filename extension that should be used for arff files */
  	public static String FILE_EXTENSION = ".arff";
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");
  
  	/**
   	* Reads an ARFF file from a reader.
   	*
   	* @param reader Reader
	* @exception IOException if the ARFF file is not read 
   	* successfully
   	*/
  	public ArffLoader(Reader reader) throws IOException 
  	{	StreamTokenizer tokenizer = new StreamTokenizer(reader);
		instances = new InstanceSet();
		initTokenizer(tokenizer);
    	readHeader(tokenizer);
		while (getInstance(tokenizer)) {};
		instances.compactify();
	}
	
	public ArffLoader(Reader reader,Component component) throws IOException 
  	{	StreamTokenizer tokenizer = new StreamTokenizer(reader);
		instances = new InstanceSet();
		initTokenizer(tokenizer);
    	readHeader(tokenizer);
		new CompactFileDialog(this,component);
    	while (getInstance(tokenizer)) {};
		if (counterAttribute >= 0)
		{	instances.removeAttribute(counterAttribute);
		}
		/*maximumStatesAllowed = Options.getInstance().getNumberStatesAllowed();		
		checkNumericAttributes();*/
		instances.compactify();
	}

	
  	/**
   	* Initializes the StreamTokenizer used for reading the ARFF file.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	protected void initTokenizer(StreamTokenizer tokenizer)
	{	tokenizer.resetSyntax();         
    	tokenizer.whitespaceChars(0, ' ');    
    	tokenizer.wordChars(' '+1,'\u00FF');
    	tokenizer.whitespaceChars(',',',');
    	tokenizer.commentChar('%');
    	tokenizer.quoteChar('"');
    	tokenizer.quoteChar('\'');
    	tokenizer.ordinaryChar('{');
    	tokenizer.ordinaryChar('}');
    	tokenizer.eolIsSignificant(true);
  	}
  
  	/**
   	* Reads and stores header of an ARFF file.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if the information is not read 
   	* successfully
   	*/ 
  	protected void readHeader(StreamTokenizer tokenizer) throws IOException
	{	String attributeName;
    	ArrayList attributeValues;
    	int i;

    	// Get name of relation.
    	getFirstToken(tokenizer);
    	if (tokenizer.ttype == StreamTokenizer.TT_EOF) 
			errms(tokenizer,resource.getString("readHeaderException1"));
    	if (tokenizer.sval.equalsIgnoreCase("@relation"))
		{	getNextToken(tokenizer);
      		instances.setRelationName(tokenizer.sval);
      		getLastToken(tokenizer,false);
    	} 
		else 
		{	errms(tokenizer,resource.getString("readHeaderException2"));
    	}

    	// Get attribute declarations.
    	getFirstToken(tokenizer);
    	if (tokenizer.ttype == StreamTokenizer.TT_EOF) 
		{	errms(tokenizer,resource.getString("readHeaderException1"));
    	}
    	while (tokenizer.sval.equalsIgnoreCase("@attribute")) 
		{	// Get attribute name.
      		getNextToken(tokenizer);
      		attributeName = tokenizer.sval;
      		getNextToken(tokenizer);

      		// Check if attribute is nominal.
      		if (tokenizer.ttype == StreamTokenizer.TT_WORD) 
			{	// Attribute is real, or integer.
				if (tokenizer.sval.equalsIgnoreCase("real") || tokenizer.sval.equalsIgnoreCase("integer") || tokenizer.sval.equalsIgnoreCase("numeric")) 
				{	instances.insertAttribute(new Attribute(attributeName,null,Attribute.NUMERIC,instances.numAttributes()));
	  				readTillEOL(tokenizer);
				} 
				else 
				{	errms(tokenizer,resource.getString("readHeaderException3"));
				}
      		} 
			else 
			{	// Attribute is nominal.
				attributeValues = new ArrayList();
				tokenizer.pushBack();
	
				// Get values for nominal attribute.
				if (tokenizer.nextToken() != '{') 
				{	errms(tokenizer,resource.getString("readHeaderException4"));
				}
				while (tokenizer.nextToken() != '}') 
				{	if (tokenizer.ttype == StreamTokenizer.TT_EOL) 
					{	errms(tokenizer,resource.getString("readHeaderException5"));
	  				} 
					else 
					{	attributeValues.add(tokenizer.sval);
	  				}
				}
				if (attributeValues.size() == 0) 
				{	errms(tokenizer,resource.getString("readHeaderException6"));
				}
				instances.insertAttribute(new Attribute(attributeName, attributeValues, Attribute.NOMINAL,instances.numAttributes()));
      		}
      		getLastToken(tokenizer,false);
      		getFirstToken(tokenizer);
      		if (tokenizer.ttype == StreamTokenizer.TT_EOF)
				errms(tokenizer,resource.getString("readHeaderException1"));
    	}

    	// Check if data part follows. We can't easily check for EOL.
    	if (!tokenizer.sval.equalsIgnoreCase("@data")) 
		{	errms(tokenizer,resource.getString("readHeaderException7"));
    	}
    
    	// Check if any attributes have been declared.
    	if (instances.numAttributes() == 0) 
		{	errms(tokenizer,resource.getString("readHeaderException8"));
    	}
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
    	if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) 
		{	tokenizer.ttype = StreamTokenizer.TT_WORD;
    	} 
		else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) && (tokenizer.sval.equals("?")))
		{	tokenizer.ttype = '?';
    	}
  	}
  
  	/**
   	* Gets token and checks if its end of line.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if it doesn't find an end of line
   	*/
  	protected void getLastToken(StreamTokenizer tokenizer, boolean endOfFileOk) throws IOException
	{	if ((tokenizer.nextToken() != StreamTokenizer.TT_EOL) && ((tokenizer.nextToken() != StreamTokenizer.TT_EOF) || !endOfFileOk)) 
			errms(tokenizer,resource.getString("getLastTokenException1"));
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
		else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) 
		{	tokenizer.ttype = StreamTokenizer.TT_WORD;
    	} 
		else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) && (tokenizer.sval.equals("?")))
		{	tokenizer.ttype = '?';
    	}
  	}
  
  	/**
   	* Reads and skips all tokens before next end of line token.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	private void readTillEOL(StreamTokenizer tokenizer) throws IOException
  	{	while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) 
		{};
    	tokenizer.pushBack();
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
		{	errms(tokenizer,resource.getString("getInstanceException1"));
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
		int index;
		int instanceWeight = 1;
    	
		// Get values for all attributes.
    	for (int i = 0; i < instances.numAttributes(); i++)
		{	// Get next token
      		if (i > 0) 
			{	getNextToken(tokenizer);
      		}
            
			if (counterAttribute == i)
			{	try
				{	instanceWeight = Integer.valueOf(tokenizer.sval).intValue();
				}
				catch(NumberFormatException nfe)
				{	errms(tokenizer,"Atributo de contagem inválido");
				}
			}
			else			
			{	// Check if value is missing.
      			if  (tokenizer.ttype == '?') 
				{	instance[i] = Instance.missingValue();
      			} 
				else 
				{	// Check if token is valid.
					if (tokenizer.ttype != StreamTokenizer.TT_WORD) 
					{	errms(tokenizer,resource.getString("getInstanceFullException1"));
					}
					if (instances.getAttribute(i).isNominal()) 
					{	// Check if value appears in header.
	  					index = instances.getAttribute(i).indexOfValue(tokenizer.sval);
	  					if (index == -1) 
						{	errms(tokenizer,resource.getString("getInstanceFullException2"));
	  					}
	  					instance[i] = (short)index;
					} 
					else if (instances.getAttribute(i).isNumeric()) 
					{	// Check if value is really a number.
	  					try
						{	Attribute att = instances.getAttribute(i);
							float newValue = Float.valueOf(tokenizer.sval).floatValue();
							String nomeEstado = newValue + "";
							if (att.numValues()==0 || att.indexOfValue(nomeEstado) == -1)
							{	att.addValue(nomeEstado);
							}
				
							// Check if value appears in header.
	  						index = att.indexOfValue(nomeEstado);
	  						if (index == -1) 
							{	errms(tokenizer,resource.getString("getInstanceFullException2"));
	  						}
	  						instance[i] = (short)index;						
	  					} 
						catch (NumberFormatException e) 
						{	errms(tokenizer,resource.getString("getInstanceFullException3"));
	  					}
					} 
      			}
			}
    	}
    	getLastToken(tokenizer,true);
    	
    	// Add instance to dataset
    	add(new Instance(instanceWeight,instance));
		return true;
  	}
  
}

     

