package unbbayes.datamining.datamanipulation;

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
        public TxtLoader(File file) throws IOException
  	{	// Count instances
                countInstancesFromFile(file);
                //Memory initialization
                
                Reader reader = new BufferedReader(new FileReader(file));
                tokenizer = new StreamTokenizer(reader);
                initTokenizer();
                readHeader();
                maximumStatesAllowed = Options.getInstance().getNumberStatesAllowed();
	}

	/**
   	* Initializes the StreamTokenizer used for reading the TXT file.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	protected void initTokenizer()
	{   tokenizer.resetSyntax();
    	    tokenizer.wordChars(' '+1,'\u00FF');
    	    tokenizer.wordChars('_', '_');
            tokenizer.wordChars('-', '-');
            tokenizer.whitespaceChars(0, ' ');
    	    tokenizer.whitespaceChars('\t','\t');
    	    tokenizer.commentChar('%');
    	    tokenizer.quoteChar('"');
    	    tokenizer.eolIsSignificant(true);
  	}

	/**
   	* Reads and stores header of a TXT file.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if the information is not read
   	* successfully
   	*/
  	protected void readHeader() throws IOException
	{	String[] attributeValues = null;
		ArrayList attributes = new ArrayList();
		//Insert attributes in the new dataset
		getNextToken();
		while (tokenizer.ttype != StreamTokenizer.TT_EOL)
		{	if(tokenizer.sval != null)
			{	attributes.add(new Attribute(tokenizer.sval,attributeValues,Attribute.NOMINAL,attributes.size()));
                	}
			else
			{	attributes.add(new Attribute(String.valueOf(tokenizer.nval),attributeValues,Attribute.NUMERIC,attributes.size()));
                	}
			tokenizer.nextToken();
		}
		int size = attributes.size();
                Attribute[] attributesArray = new Attribute[size];
                for (int i=0;i<size;i++)
                {
                  attributesArray[i] = (Attribute)attributes.get(i);
                }
                initialInstances--;// Number of lines - header
                instances = new InstanceSet(initialInstances, attributesArray);
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
  	public boolean getInstance() throws IOException
	{
          // Check if any attributes have been declared.
    	  if (instances.numAttributes() == 0)
            {   errms(resource.getString("getInstanceTXT"));
    	    }

    	    // Check if end of file reached.
    	    getFirstToken();
    	    if (tokenizer.ttype == StreamTokenizer.TT_EOF)
            {   return false;
    	    }

    	    return getInstanceFull();
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
  	protected boolean getInstanceFull() throws IOException
	{
          int numAttributes = instances.numAttributes();
            int[] instance = new int[numAttributes];
            int instanceWeight = 1;
            int position = 0,index = 0;
            int attributeNumber = -1;
            String stateName = "";

            int instanceSize = 0;
            if (counterAttribute >= 0)
            {   instanceSize = numAttributes + 1;
            }
            else
            {   instanceSize = numAttributes;
            }
            //Create instances
            while(position < instanceSize)
            {   if (counterAttribute == position)
                {   try
                    {   instanceWeight = Integer.valueOf(tokenizer.sval).intValue();
                    }
                    catch(NumberFormatException nfe)
                    {   errms("Atributo de contagem inválido");
                    }
                }
                else
                {
                  attributeNumber++;
                    Attribute att = instances.getAttribute(attributeNumber);
                    //Insert new value for attribute if number of values is 0 or this value isn't inserted
                    if(tokenizer.sval != null)
                    {   stateName = tokenizer.sval;
                        // Check if value is missing.
                        if (stateName.equals("?"))
                        {   instance[attributeNumber] = Instance.missingValue();
                        }
                        else if (att.numValues()==0 || att.indexOfValue(stateName) == -1)
                        {   att.addValue(stateName);
                        }
                    }
                    else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
                    {	stateName = String.valueOf(tokenizer.nval);
                        if (att.numValues()==0 || att.indexOfValue(stateName) == -1)
                        {   att.addValue(stateName);
                        }
                    }
                    else
                    {}
                    if (tokenizer.ttype == StreamTokenizer.TT_WORD)
                    {	if (instances.getAttribute(attributeNumber).isNominal())
                        {   // Check if value appears in header.
                            index = att.indexOfValue(tokenizer.sval);
                            instance[attributeNumber] = index;
                        }
                    }
                    else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
                    {	if (instances.getAttribute(attributeNumber).isNominal())
                        {   // Check if value appears in header.
                            index = att.indexOfValue(tokenizer.nval+"");
                            instance[attributeNumber] = index;
                        }
                    }
                }
                position++;
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
  	protected void getFirstToken() throws IOException
	{	while (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
		{};
  	}

	/**
   	* Gets next token, checking for a premature end of line.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if it finds a premature end of line
   	*/
  	protected void getNextToken() throws IOException
	{	if (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
		{	errms(resource.getString("getNextTokenException1"));
    	}
    	if (tokenizer.ttype == StreamTokenizer.TT_EOF)
		{	errms(resource.getString("getNextTokenException2"));
    	}
  	}

	/** Changes all nominal attributes that can be parsed to numeric attributes
	*/
	public void checkNumericAttributes()
	{	int numAttributes = instances.numAttributes();
		for (int i = 0; i < numAttributes; i++)
		{	Attribute att = instances.getAttribute(i);
			if (att.numValues() > maximumStatesAllowed)
			{	boolean bool = checkNominal(att);
                                if (bool == false)
					att.setAttributeType(Attribute.NUMERIC);
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
			if (!value.equals("?"))
                        {   if (value.equals(""))
                                return true;
			    try
			    {	Float.parseFloat(value);
			    }
			    catch (NumberFormatException nfe)
			    {	return true;
			    }
                        }
		}
		return false;
	}

}
