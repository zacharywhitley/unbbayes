package unbbayes.datamining.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

import unbbayes.controller.IProgress;

/** This class defines abstracs methods for open a file building an InstanceSet object
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public abstract class Loader implements IProgress {
	/** Database created from a file */
	protected InstanceSet instanceSet;

	protected int counterAttribute = -1;
	
	protected int initialInstances = 0;
	
	protected StreamTokenizer tokenizer;

	public void setCounterAttribute(int counterAttribute) {
		if (counterAttribute >= 0) {
			instanceSet.removeAttribute(counterAttribute);
			this.counterAttribute = counterAttribute;
		}
	}

	/** 
	 * Returns instance set generated from reader
	 * @return The instance set
	 */
	public InstanceSet getInstances() {
		if (this instanceof TxtLoader) {
			instanceSet.setFinal();
		}
		return instanceSet;
	}

	/**
	 * Initializes the StreamTokenizer.
	 *
	 * @param tokenizer Stream tokenizer
	 */
	protected abstract void initTokenizer();

	/**
	 * Reads and stores header of a file.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	protected abstract void readHeader() throws IOException;

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
	public abstract boolean getInstance() throws IOException;

	/**
	 * Gets next token, skipping empty lines.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if reading the next token fails
	 */
	protected abstract void getFirstToken() throws IOException;

	/**
	 * Gets next token, checking for a premature end of line.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if it finds a premature end of line
	 */
	protected abstract void getNextToken() throws IOException;

	/**
	* Throws error message with line number and last token read.
	*
	* @param theMsg Error message to be thrown
	* @param tokenizer Stream tokenizer
	* @throws IOException containing the error message
	*/
	protected void errms(String theMsg) throws IOException {
		throw new IOException(theMsg + ", read " + tokenizer.toString());
	}

	public int getInitialInstancesCount() {
		return initialInstances;
	}

	public boolean next() {
		boolean result = false;
		
		try {
			result = getInstance();
		} catch(IOException ioe) {
			result = false;
		}
		
		return result;
	}

	public void cancel() {
		instanceSet = null;
	}
	
	public int maxCount() {
		return initialInstances;
	}

	protected void countInstancesFromFile(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inReader = new InputStreamReader(fileIn);
		BufferedReader in = new BufferedReader(inReader);
	
		/* Count lines of the file */
		int count = 0;
		String line;
		while ((line = in.readLine()) != null && !line.startsWith("%")) {
			count++;
		}
		initialInstances = count;
		fileIn.close();
	}

}