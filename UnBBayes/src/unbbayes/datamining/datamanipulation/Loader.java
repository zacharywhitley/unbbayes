package unbbayes.datamining.datamanipulation;

import java.io.*;

/** This class defines abstracs methods for open a file building an InstanceSet object
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public abstract class Loader
{	/** Database created from a file */
	protected InstanceSet instances;
	
	protected int counterAttribute = -1;
	
	public void setCounterAttribute(int counterAttribute)
	{	this.counterAttribute = counterAttribute;
	}
	
	/** Returns instance set generated from reader 
		@return The instance set
		*/
	public InstanceSet getInstances()
	{	return instances;
	}
	
	/**
   	* Initializes the StreamTokenizer.
   	*
   	* @param tokenizer Stream tokenizer
   	*/
  	protected abstract void initTokenizer(StreamTokenizer tokenizer);
	
	/**
   	* Reads and stores header of a file.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if the information is not read 
   	* successfully
   	*/
	protected abstract void readHeader(StreamTokenizer tokenizer) throws IOException;
	
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
	protected abstract boolean getInstance(StreamTokenizer tokenizer) throws IOException;
	
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
	protected abstract boolean getInstanceFull(StreamTokenizer tokenizer) throws IOException;
	
	/**
	* Adds one instance to the end of the set. 
	* Increases the size of the dataset if it is not large enough. Does not
	* check if the instance is compatible with the dataset.
	*
	* @param instance Instance to be added
	*/
	public void add(Instance newInstance)
	{	newInstance.setDataset(instances);
    	instances.insertInstance(newInstance);
  	}
	
	/**
   	* Gets next token, skipping empty lines.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if reading the next token fails
   	*/
	protected abstract void getFirstToken(StreamTokenizer tokenizer) throws IOException;

	/**
   	* Gets next token, checking for a premature end of line.
   	*
   	* @param tokenizer Stream tokenizer
   	* @exception IOException if it finds a premature end of line
   	*/
	protected abstract void getNextToken(StreamTokenizer tokenizer) throws IOException;
	
	/**
	* Throws error message with line number and last token read.
	*
	* @param theMsg Error message to be thrown
	* @param tokenizer Stream tokenizer
	* @throws IOException containing the error message
	*/
	protected void errms(StreamTokenizer tokenizer, String theMsg) throws IOException
	{	throw new IOException(theMsg + ", read " + tokenizer.toString());
  	}  
}