package unbbayes.datamining.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.ResourceBundle;

/** This class opens a txt file building an InstanceSet object
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class TxtLoader extends Loader {
	/** The filename extension that should be used for txt files */
	public static final String FILE_EXTENSION = ".txt";

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");

	/**
	 * Reads a TXT file from a reader.
	 *
	 * @param reader Reader
	 * @exception IOException if the TXT file is not read
	 * successfully
	 */
	public TxtLoader(File file) throws IOException {
		this.file = file;
		
		// Count instanceSet
		countInstancesFromFile(file);
		
		//Memory initialization
		Reader reader = new BufferedReader(new FileReader(file));
		tokenizer = new StreamTokenizer(reader);
		initTokenizer();
	}

	/**
	 * Initializes the StreamTokenizer used for reading the TXT file.
	 *
	 * @param tokenizer Stream tokenizer
	 */
	protected void initTokenizer() {
		tokenizer.resetSyntax();
		tokenizer.wordChars(' ' + 1, '\u00FF');
		tokenizer.wordChars('_', '_');
		tokenizer.wordChars('-', '-');
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.whitespaceChars('\t','\t');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.eolIsSignificant(true);
		tokenizer.parseNumbers();
	}

	/**
	 * Temporarily constructs the header of a txt file. Used as a preprocessor 
	 * step in the construction of the file's header. 
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	public void buildHeader() throws IOException {
		/* Create am arraylist for the attributes */
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		/* Insert attributes in the new dataset */
		getNextToken();
		int attIndex = 0;
		String attName;
		
		/* Update initialInstances excluding the header */
		--initialInstances;
		
		while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
			attName = tokenizer.sval;

			/* Check if the header is ok */
			if (attName == null) {
				attName = String.valueOf(tokenizer.nval);
			}
			if (attName == null) {
				errms(resource.getString("Invalid header"));
			}
			
			/* Check if the current attribute is the counter */
			if (attIndex == counterIndex) {
				/* Get counter's name and skip to the next attribute */
				counterAttributeName = attName;
				tokenizer.nextToken();
				continue;
			}
			
			/* Build attribute */
			attributes.add(new Attribute(attName,(byte) 1, true,
					initialInstances, attIndex));
			tokenizer.nextToken();
			++attIndex;
		}
		
		int size = attributes.size();
		Attribute[] attributesArray = new Attribute[size];
		
		for (int i = 0; i < size; i++) {
			attributesArray[i] = (Attribute)attributes.get(i);
		}
		
		/* Create the instanceSet */
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setCounterAttributeName(counterAttributeName);
		
		/* Set the current number of attributes */
		numAttributes = attIndex;
	}



	/**
	 * Reads and stores header of a TXT file.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	public void readHeader() throws IOException {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		ema();
		
		/* Insert attributes in the new dataset */
		getNextToken();
		int attIndex = 0;
		String attName;
		
		/* Update initialInstances excluding the header */
		--initialInstances;
		
		while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
			attName = tokenizer.sval;

			/* Check if the header is ok */
			if (attName == null) {
				attName = String.valueOf(tokenizer.nval);
			}
			if (attName == null) {
				errms(resource.getString("Invalid header"));
			}
			
			/* Check if the current attribute is the counter */
			if (attIndex == counterIndex) {
				/* Get counter's name and skip to the next attribute */
				counterAttributeName = attName;
				tokenizer.nextToken();
				continue;
			}
			
			/* Build attribute */
			attributes.add(new Attribute(attName,
										attributeType[attIndex],
										attributeIsString[attIndex],
										initialInstances,
										attIndex
										)
							);
			tokenizer.nextToken();
			++attIndex;
		}
		
		int size = attributes.size();
		Attribute[] attributesArray = new Attribute[size];
		
		for (int i = 0; i < size; i++) {
			attributesArray[i] = (Attribute)attributes.get(i);
		}
		
		/* Create the instanceSet */
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setCounterAttributeName(counterAttributeName);
		
		/* Set the current number of attributes */
		numAttributes = attIndex;
	}

	/**
	 * Reads a single instance using the tokenizer and appends it
	 * to the dataset. Automatically expands the dataset if it
	 * is not large enough to hold the instance.
	 *
	 * @param tokenizer Tokenizer to be used
	 * @return False if end of file has been reached
	 * @exception IOException if the information is not read successfully
	 */
	public boolean getInstance() throws IOException {
		/* Check if the header has already been read */
		if (instanceSet == null) {
			readHeader();
		}
		
		/* Check if any attributes have been declared */
		if (instanceSet.numAttributes() == 0) {
			errms(resource.getString("getInstanceTXT"));
		}

		/* Check if end of file reached */
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			return false;
		}

		/* Read the next instance from the file */
		return getInstanceAux();
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
	private boolean getInstanceAux() throws IOException {
		/* Alocate space for the attributes and the counter variable */
		float[] instance = new float[numAttributes + 1];
		
		/* Default value for the weight of an instance */
		float instanceWeight = 1;
		
		int attIndex = 0;
		String stringValue;
		float numberValue;
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
			
			/* Check the type of the token */
			if (tokenizer.sval != null) {
				/* The token is a String */
				stringValue = tokenizer.sval;
				
				/* Check if value is missing */ 
				if (stringValue.equals("?")) {
					instance[attIndex] = Instance.MISSING_VALUE;
				}

				/* Map the current String value to an internal value */ 
				Attribute attribute = instanceSet.getAttribute(i);
				instance[attIndex] = attribute.addValue(stringValue);
			} else {
				/* The token is a number. Check if the attribute type was set
				 * to nominal.
				 */
				if (attributeType[attIndex] == NOMINAL) {
					/* Map the current nominal value to an internal value */
					numberValue = (float) tokenizer.nval;
					Attribute attribute = instanceSet.getAttribute(i);
					instance[attIndex] = attribute.addValue(numberValue);
				} else {
					instance[attIndex] = (float) tokenizer.nval;
				}
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

	/**
	 * Gets next token, skipping empty lines.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if reading the next token fails
	 */
	protected void getFirstToken() throws IOException {
		while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			/* Loop until the next token is different of EOL (end of line) */
		}
	}

	/**
	 * Gets next token, checking for a premature end of line.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if it finds a premature end of line
	 */
	protected void getNextToken() throws IOException {
		if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			errms(resource.getString("getNextTokenException1"));
		}
		if (tokenizer.ttype == StreamTokenizer.TT_EOF)  {
			errms(resource.getString("getNextTokenException2"));
		}
	}

//	private void ema() {
//		attributeType = new byte[6];
//		attributeIsString = new boolean[6];
//		attributeType[0] = NUMERIC;
//		attributeType[1] = NUMERIC;
//		attributeType[2] = NUMERIC;
//		attributeType[3] = NUMERIC;
//		attributeType[4] = NUMERIC;
//		attributeType[5] = NUMERIC;
//		attributeIsString[0] = false;
//		attributeIsString[1] = false;
//		attributeIsString[2] = false;
//		attributeIsString[3] = false;
//		attributeIsString[4] = false;
//		attributeIsString[5] = false;
//	}

	private void ema() {
		attributeType = new byte[11];
		attributeIsString = new boolean[11];
		attributeType[0] = NOMINAL;
		attributeType[1] = NOMINAL;
		attributeType[2] = NOMINAL;
		attributeType[3] = NOMINAL;
		attributeType[4] = NOMINAL;
		attributeType[5] = NOMINAL;
		attributeType[6] = NOMINAL;
		attributeType[7] = NOMINAL;
		attributeType[8] = NOMINAL;
		attributeType[9] = CYCLIC;
//		attributeType[9] = NOMINAL;
//		attributeType[9] = NUMERIC;
		attributeType[10] = NOMINAL;
		attributeIsString[0] = false;
		attributeIsString[1] = false;
		attributeIsString[2] = false;
		attributeIsString[3] = false;
		attributeIsString[4] = false;
		attributeIsString[5] = false;
		attributeIsString[6] = false;
		attributeIsString[7] = false;
		attributeIsString[8] = false;
		attributeIsString[9] = false;
		attributeIsString[10] = false;
	}

//	private void ema() {
//		// creditApproval
//		attributeType = new byte[16];
//		attributeIsString = new boolean[16];
//		attributeType[0] = NOMINAL;
//		attributeType[1] = NUMERIC;
//		attributeType[2] = NUMERIC;
//		attributeType[3] = NOMINAL;
//		attributeType[4] = NOMINAL;
//		attributeType[5] = NOMINAL;
//		attributeType[6] = NOMINAL;
//		attributeType[7] = NUMERIC;
//		attributeType[8] = NOMINAL;
//		attributeType[9] = NOMINAL;
//		attributeType[9] = NOMINAL;
//		attributeType[10] = NUMERIC;
//		attributeType[11] = NOMINAL;
//		attributeType[12] = NOMINAL;
//		attributeType[13] = NUMERIC;
//		attributeType[14] = NUMERIC;
//		attributeType[15] = NOMINAL;
//		attributeIsString[0] = true;
//		attributeIsString[1] = true;
//		attributeIsString[2] = true;
//		attributeIsString[3] = true;
//		attributeIsString[4] = true;
//		attributeIsString[5] = true;
//		attributeIsString[6] = true;
//		attributeIsString[7] = true;
//		attributeIsString[8] = true;
//		attributeIsString[9] = true;
//		attributeIsString[10] = true;
//		attributeIsString[11] = true;
//		attributeIsString[12] = true;
//		attributeIsString[13] = true;
//		attributeIsString[14] = true;
//		attributeIsString[15] = false;
//		counterIndex = -1;
//	}


//	private void ema() {
//		attributeType = new byte[5];
//		attributeIsString = new boolean[5];
//		attributeType[0] = NOMINAL;
//		attributeIsString[0] = true;
//		attributeType[1] = NUMERIC;
//		attributeIsString[1] = false;
//		attributeType[2] = NUMERIC;
//		attributeIsString[2] = false;
//		attributeType[3] = NOMINAL;
//		attributeIsString[3] = true;
//		attributeType[4] = NOMINAL;
//		attributeIsString[4] = true;
//	}

//	private void ema() {
//		attributeType = new byte[23];
//		attributeIsString = new boolean[23];
//		for (int i = 0; i < 23; i++) {
//			attributeType[i] = NOMINAL;
//			attributeIsString[i] = true;
//		}
//	}
}