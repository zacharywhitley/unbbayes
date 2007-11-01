package unbbayes.datamining.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import unbbayes.controller.IProgress;

/** This class defines abstracs methods for open a file building an InstanceSet object
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public abstract class Loader implements IProgress {
	/** Database created from a file */
	protected InstanceSet instanceSet;

    /** Stores the name of the counter attribute */
	protected String counterAttributeName = "Total";

	protected int counterIndex = -1;
	
	protected int initialInstances = 0;
	
	protected StreamTokenizer tokenizer;

	protected boolean compacted;

	protected boolean checkedCompacted = false;

	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	protected byte[] attributeType;

	/** Constant set for numeric attributes. */
	protected final static byte NUMERIC = InstanceSet.NUMERIC;

	/** Constant set for nominal attributes. */
	protected final static byte NOMINAL = InstanceSet.NOMINAL;

	/** Constant set for cyclic numeric attributes. */
	protected final static byte CYCLIC = InstanceSet.CYCLIC;

	/** Number of attributes */
	protected int numAttributes;
	
	public File file;

	/**
	 * Used to ease the construction of an instance.
	 */
	protected boolean[] attributeIsString;

	protected String[] attributeName;

	protected int likelyCounterIndex = -1;

	private int classIndex;
	
	public void setAttributeType(byte[] attributeType) {
		this.attributeType = attributeType;
	}
	
	public void setAttributeIsString(boolean[] attributeIsString) {
		this.attributeIsString = attributeIsString;
	}
	
	public void setNumAttributes(int numAttributes) {
		this.numAttributes = numAttributes;
	}
	
	public void setCounterAttribute(int counterIndex) {
		this.counterIndex = counterIndex;
	}

	/** 
	 * Returns instance set generated from reader
	 * @return The instance set
	 */
	public InstanceSet getInstanceSet() {
		instanceSet.setFinal();
		
		/* Check if the instanceSet is compacted or not */
		if (!checkedCompacted) {
			instanceSet.setCompacted(false);
			float weight;
			for (int inst = 0; inst < instanceSet.numInstances; inst++) {
				weight = instanceSet
					.instances[inst].data[instanceSet.counterIndex];
				if (weight > 1) {
					instanceSet.setCompacted(true);
					break;
				}
			}
			checkedCompacted = true;
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
	 * Temporarily constructs the header of a txt file. Used as a preprocessor 
	 * step in the construction of the file's header. 
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	public abstract void buildHeader() throws IOException;

	/**
	 * Reads and stores header of a file.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	public abstract void readHeader() throws IOException;

	/**
	 * Reads and return the names of all attributes in an ARFF file.
	 * @return An arraylist containing the following results: An arraylist with
	 * all attributes' names, the likely counter attribute's name and the 
	 * relation's name.
	 * @exception IOException if the information is not read successfully.
	 */
	public abstract ArrayList<Object> getHeaderInfo() throws IOException;

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

	protected void countInstancesFromFile(File file, int num, boolean txt)
	throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inReader = new InputStreamReader(fileIn);
		BufferedReader in = new BufferedReader(inReader);
	
		/* Count lines of the file */
		int count = 0;
		String line;
		
		boolean startCount = false;

		/* Check if its a txt file */
		if (txt) {
			startCount = true;
		}
		
		while ((line = in.readLine()) != null) {
			if (startCount && (!line.startsWith("%"))) {
				count++;
			}
			if ((line.toLowerCase()).equals("@data")) {
				/* Finish reading header */
				startCount = true;
			}
			if (num != -1 && count >= num) {
				break;
			}
		}
		initialInstances = count;
		fileIn.close();
	}

	/**
	 * Reads a single instance using the tokenizer and appends it
	 * to the dataset. Automatically expands the dataset if it
	 * is not large enough to hold the instance.
	 *
	 * @return False if end of file has been reached
	 * @exception IOException if the information is not read
	 * successfully
	 */
	protected abstract boolean getInstanceAux() throws IOException;

	public int getNumAttributes() {
		return numAttributes;
	}

	public int getLikelyCounterIndex() {
		return likelyCounterIndex;
	}

	public void setAttributeName(String[] attributeName) {
		this.attributeName = attributeName;
	}

	public void setCounterAttributeName(String counterAttributeName) {
		this.counterAttributeName = counterAttributeName;
	}
	
	public void setCompacted(boolean compacted) {
		this.compacted = compacted;
	}

	public int getnumInitialInstances() {
		return initialInstances;
	}

	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}

}