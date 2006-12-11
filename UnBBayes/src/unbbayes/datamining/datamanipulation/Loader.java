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

    /** Stores the name of the counter attribute */
	protected String counterAttributeName = "Total";

	protected int counterIndex = -1;
	
	protected int initialInstances = 0;
	
	protected StreamTokenizer tokenizer;

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

	protected int likelycounterIndex = -1;
	
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

	/**
	 * Reads a single instance using the tokenizer and appends it
	 * to the dataset. Automatically expands the dataset if it
	 * is not large enough to hold the instance.
	 *
	 * @return False if end of file has been reached
	 * @exception IOException if the information is not read
	 * successfully
	 */
	protected boolean getInstanceAux() throws IOException {
		/* Alocate space for the attributes and the counter variable */
		float[] instance = new float[numAttributes + 1];
		
		/* Default value for the weight of an instance */
		float instanceWeight = 1;
		
		int attIndex = 0;
		String stringValue;
		int columns = numAttributes;
		
		/* Check if the instanceSet file has a counter attribute */
		if (counterIndex != -1) {
			/* Read the counter attribute */
			++columns;
		}
		
		/* 
		 * Create instance. Iterate over all attributes and the counter
		 * variable
		 */
		for (int i = 0; i < columns; i++) {
			/* Check if the current attribute is the counter attribute */
			if (i == counterIndex) {
				try {
					instanceWeight = (float) tokenizer.nval;
					continue;
				} catch (NumberFormatException nfe) {
					errms("Atributo de contagem inválido");
				}
			}
			
			if (attributeType[attIndex] == NOMINAL) {
				stringValue = tokenizer.sval;
				Attribute attribute = instanceSet.getAttribute(attIndex);
				
				/* Check if the attribute is made of String values */
				if (attributeIsString[attIndex]) {
					if (tokenizer.sval == null) {
						/* The token is a number */
						stringValue = String.valueOf(tokenizer.nval);
					} else {
						/* The token is a String */
						stringValue = tokenizer.sval;
					}
					/* Check if value is missing */ 
					if (stringValue.equals("?")) {
						instance[attIndex] = Instance.MISSING_VALUE;
					}
	
					/* Map the current String value to an internal value */ 
					instance[attIndex] = attribute.addValue(stringValue);
				} else {
					/* Map the current String value to an internal value */
					float value = (float) tokenizer.nval;
					instance[attIndex] = attribute.addValue(value);
				}
			} else {
				/* 
				 * The attribute is not nominal thus only numbers are allowed
				 * here.
				 */
				instance[attIndex] = (float) tokenizer.nval;
			}
			++attIndex;
			tokenizer.nextToken();
		}
		
		/* Set the weight of this instance */
		instance[attIndex] = instanceWeight;
		
		/* Add the current instance to the instanceSet */
		instanceSet.insertInstance(new Instance(instance));

		return true;
	}

	public int getNumAttributes() {
		return numAttributes;
	}

	public int getLikelyCounterIndex() {
		return likelycounterIndex;
	}

	public void setAttributeName(String[] attributeName) {
		this.attributeName = attributeName;
	}

	public void setCounterAttributeName(String counterAttributeName) {
		this.counterAttributeName = counterAttributeName;
	}

}