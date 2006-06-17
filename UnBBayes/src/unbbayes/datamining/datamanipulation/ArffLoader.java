package unbbayes.datamining.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.ResourceBundle;

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
  	public ArffLoader(File file) throws IOException
  	{   // Count instances
            countInstancesFromFile(file);
            //Memory initialization
            Reader reader = new BufferedReader(new FileReader(file));
            tokenizer = new StreamTokenizer(reader);
            initTokenizer();
            readHeader();
	}

  	/**
   	* Initializes the StreamTokenizer used for reading the ARFF file.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	protected void initTokenizer()
	{
          tokenizer.resetSyntax();
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

          protected void countInstancesFromFile(File file) throws IOException
          {   FileInputStream fileIn = new FileInputStream(file);
              InputStreamReader inReader = new InputStreamReader(fileIn);
              BufferedReader in = new BufferedReader(inReader);

              // read file and count lines
              int count = 0;
              boolean startCount = false;
              String line;
              while ((line = in.readLine()) != null)
              {
                if (startCount && (!line.startsWith("%")))
                {
                  count++;
                }
                if ((line.toLowerCase()).equals("@data"))
                {
                  startCount = true;//Finish read header
                }
              }
              initialInstances = count;
              fileIn.close();
          }


  	/**
   	* Reads and stores header of an ARFF file.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if the information is not read
   	* successfully
   	*/
  	protected void readHeader() throws IOException
	{
          String attributeName;
          String relationName = "";
          ArrayList attributeValues;
          ArrayList attributes = new ArrayList();
          int i;

          // Get name of relation.
          getFirstToken();
          if (tokenizer.ttype == StreamTokenizer.TT_EOF)
            errms(resource.getString("readHeaderException1"));
          if (tokenizer.sval.equalsIgnoreCase("@relation"))
          {
            getNextToken();
            relationName=(tokenizer.sval);
            getLastToken(false);
          }
          else
          {
            errms(resource.getString("readHeaderException2"));
          }

          // Get attribute declarations.
          getFirstToken();
          if (tokenizer.ttype == StreamTokenizer.TT_EOF)
          {
            errms(resource.getString("readHeaderException1"));
          }

          while (tokenizer.sval.equalsIgnoreCase("@attribute"))
          {
            // Get attribute name.
            getNextToken();
            attributeName = tokenizer.sval;
            getNextToken();

            // Check if attribute is nominal.
            if (tokenizer.ttype == StreamTokenizer.TT_WORD)
            {
              // Attribute is real, or integer.
              if (tokenizer.sval.equalsIgnoreCase("real") || tokenizer.sval.equalsIgnoreCase("integer") || tokenizer.sval.equalsIgnoreCase("numeric"))
              {
                attributes.add(new Attribute(attributeName,null,Attribute.NUMERIC,attributes.size()));
                readTillEOL();
              }
              else
              {
                errms(resource.getString("readHeaderException3"));
              }
            }
            else
            {
              // Attribute is nominal.
              attributeValues = new ArrayList();
              tokenizer.pushBack();

              // Get values for nominal attribute.
              if (tokenizer.nextToken() != '{')
              {
                errms(resource.getString("readHeaderException4"));
              }
              while (tokenizer.nextToken() != '}')
              {
                if (tokenizer.ttype == StreamTokenizer.TT_EOL)
                {
                  errms(resource.getString("readHeaderException5"));
                }
                else
                {
                  attributeValues.add(tokenizer.sval);
                }
              }
              if (attributeValues.size() == 0)
              {
                errms(resource.getString("readHeaderException6"));
              }
              int sizeValues = attributeValues.size();
              String[] attributeValuesArray = new String[sizeValues];
              for (i=0;i<sizeValues;i++)
              {
                attributeValuesArray[i] = (String)attributeValues.get(i);
              }
              attributes.add(new Attribute(attributeName, attributeValuesArray, Attribute.NOMINAL,attributes.size()));
            }
            getLastToken(false);
            getFirstToken();
            if (tokenizer.ttype == StreamTokenizer.TT_EOF)
              errms(resource.getString("readHeaderException1"));
          }

          int size = attributes.size();
          Attribute[] attributesArray = new Attribute[size];
          for (i=0;i<size;i++)
          {
            attributesArray[i] = (Attribute)attributes.get(i);
          }
          instances = new InstanceSet(initialInstances, attributesArray);
          instances.setRelationName(relationName);

          // Check if data part follows. We can't easily check for EOL.

          if (!tokenizer.sval.equalsIgnoreCase("@data"))
          {
            errms(resource.getString("readHeaderException7"));
          }

          // Check if any attributes have been declared.
          if (instances.numAttributes() == 0)
          {
            errms(resource.getString("readHeaderException8"));
          }
        }

  	/**
   	* Gets next token, skipping empty lines.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if reading the next token fails
   	*/
  	protected void getFirstToken() throws IOException
	{
          while (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
          {};
          if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"'))
          {
            tokenizer.ttype = StreamTokenizer.TT_WORD;
          }
          else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) && (tokenizer.sval.equals("?")))
          {
            tokenizer.ttype = '?';
          }
  	}

  	/**
   	* Gets token and checks if its end of line.
   	*
   	* @param tokenizer Stream tokenizer
        * @param endOfFileOk Checks if it's end of line
   	* @exception IOException if it doesn't find an end of line
   	*/
  	protected void getLastToken(boolean endOfFileOk) throws IOException
	{	if ((tokenizer.nextToken() != StreamTokenizer.TT_EOL) && ((tokenizer.nextToken() != StreamTokenizer.TT_EOF) || !endOfFileOk))
			errms(resource.getString("getLastTokenException1"));
  	}

  	/**
   	* Gets next token, checking for a premature end of line.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if it finds a premature end of line
   	*/
  	protected void getNextToken() throws IOException
	{
          if (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
          {
            errms(resource.getString("getNextTokenException1"));
          }
          if (tokenizer.ttype == StreamTokenizer.TT_EOF)
          {
            errms(resource.getString("getNextTokenException2"));
          }
          else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"'))
          {
            tokenizer.ttype = StreamTokenizer.TT_WORD;
          }
          else if ((tokenizer.ttype == StreamTokenizer.TT_WORD) && (tokenizer.sval.equals("?")))
          {
            tokenizer.ttype = '?';
          }
  	}

  	/**
   	* Reads and skips all tokens before next end of line token.
   	*
   	* @param tokenizer Stream tokenizer
        * @throws IOException EOF not found
   	*/
  	private void readTillEOL() throws IOException
  	{
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOL)
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
        public boolean getInstance() throws IOException
        {
          // Check if any attributes have been declared.
          if (instances.numAttributes() == 0)
          {
            errms(resource.getString("getInstanceException1"));
          }

          // Check if end of file reached.
          getFirstToken();
          if (tokenizer.ttype == StreamTokenizer.TT_EOF)
          {
            return false;
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
          short[] instance = new short[numAttributes];
          int index;
          int instanceWeight = 1;

          // Get values for all attributes.
          int instanceSize = 0;
          if (counterAttribute >= 0)
          {
            instanceSize = numAttributes + 1;
          }
          else
          {
            instanceSize = numAttributes;
          }
          int attributeNumber = -1;
          for (int i = 0; i < instanceSize; i++)
          {
            // Get next token
            if (i > 0)
              getNextToken();
            if (counterAttribute == i)
            {
              try
              {
                instanceWeight = Integer.valueOf(tokenizer.sval).intValue();
              }
              catch(NumberFormatException nfe)
              {
                errms("Atributo de contagem inválido");
              }
            }
            else
            {
              attributeNumber++;
                    // Check if value is missing.
                    if  (tokenizer.ttype == '?')
                    {	instance[attributeNumber] = Instance.missingValue();
                    }
                    else
                    {	// Check if token is valid.
                        if (tokenizer.ttype != StreamTokenizer.TT_WORD)
                        {   errms(resource.getString("getInstanceFullException1"));
                        }
                        if (instances.getAttribute(attributeNumber).isNominal())
                        {   // Check if value appears in header.
                            index = instances.getAttribute(attributeNumber).indexOfValue(tokenizer.sval);
                            if (index == -1)
                            {   errms(resource.getString("getInstanceFullException2"));
                            }
                            instance[attributeNumber] = (byte)index;
                        }
                        else if (instances.getAttribute(attributeNumber).isNumeric())
                        {   // Check if value is really a number.
                            try
                            {   Attribute att = instances.getAttribute(attributeNumber);
                                float newValue = Float.valueOf(tokenizer.sval).floatValue();
                                String nomeEstado = newValue + "";
                                if (att.numValues()==0 || att.indexOfValue(nomeEstado) == -1)
                                {   att.addValue(nomeEstado);
                                }
                                // Check if value appears in header.
                                index = att.indexOfValue(nomeEstado);
                                if (index == -1)
                                {   errms(resource.getString("getInstanceFullException2"));
                                }
                                instance[attributeNumber] = (byte)index;
                            }
                            catch (NumberFormatException e)
                            {   errms(resource.getString("getInstanceFullException3"));
                            }
                        }
                    }
                }
    	    }
    	    getLastToken(true);
    	    // Add instance to dataset
    	    add(new Instance(instanceWeight,instance));
            return true;
        }

}



