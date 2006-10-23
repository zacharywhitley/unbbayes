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
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	private byte[] attType;

	/** Constant set for numeric attributes. */
	private final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	private final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	private final static byte CYCLIC = 2;

    /** Stores the name of the counter attribute */
	private String counterAttributeName;

	/** Number of attributes */
	protected int numAttributes;

	/**
	 * Used to ease the construction of an instance.
	 */
	private boolean[] attIsString;
	
	/**
	 * Reads a TXT file from a reader.
	 *
	 * @param reader Reader
	 * @exception IOException if the TXT file is not read
	 * successfully
	 */
	public TxtLoader(File file) throws IOException {
		// Count instanceSet
		countInstancesFromFile(file);
		
		//Memory initialization
		Reader reader = new BufferedReader(new FileReader(file));
		tokenizer = new StreamTokenizer(reader);
		initTokenizer();
		readHeader();
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
	 * Reads and stores header of a TXT file.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	protected void readHeader() throws IOException {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		/* Ask the user for the types of each attribute */
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
				errms(resource.getString("Invalid header"));
			}
			
			/* Check if the current attribute is the counter */
			if (attIndex == counterAttribute) {
				/* Get counter's name and skip to the next attribute */
				counterAttributeName = attName;
				tokenizer.nextToken();
				continue;
			}
			
			/* Build attribute */
			attributes.add(new Attribute(attName,
										attType[attIndex],
										attIsString[attIndex],
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
		if (counterAttribute != -1) {
			/* Read the counter attribute */
			++columns;
		}
		
		/* 
		 * Create instance. Iterate over all attributes and the counter
		 * variable
		 */
		for (int i = 0; i < columns; i++) {
			/* Check if the current attribute is the counter attribute */
			if (i == counterAttribute) {
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
				if (attType[attIndex] == NOMINAL) {
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
//		attType = new byte[11];
//		attIsString = new boolean[11];
//		attType[0] = NOMINAL;
//		attType[1] = NOMINAL;
//		attType[2] = NOMINAL;
//		attType[3] = NOMINAL;
//		attType[4] = NOMINAL;
//		attType[5] = NOMINAL;
//		attType[6] = NOMINAL;
//		attType[7] = NOMINAL;
//		attType[8] = NOMINAL;
//		attType[9] = CYCLIC;
////		attType[9] = NOMINAL;
//		attType[10] = NOMINAL;
//		attIsString[0] = false;
//		attIsString[1] = false;
//		attIsString[2] = false;
//		attIsString[3] = false;
//		attIsString[4] = false;
//		attIsString[5] = false;
//		attIsString[6] = false;
//		attIsString[7] = false;
//		attIsString[8] = false;
//		attIsString[9] = false;
//		attIsString[10] = false;
////		counterAttribute = 11;
//	}

	private void ema() {
		attType = new byte[11];
		attIsString = new boolean[11];
		attType[0] = NOMINAL;
		attType[1] = NOMINAL;
		attType[2] = NOMINAL;
		attType[3] = NOMINAL;
		attType[4] = NOMINAL;
		attType[5] = NOMINAL;
		attType[6] = NOMINAL;
		attType[7] = NOMINAL;
		attType[8] = NOMINAL;
		attType[9] = CYCLIC;
//		attType[9] = NOMINAL;
		attType[10] = NOMINAL;
		attIsString[0] = false;
		attIsString[1] = false;
		attIsString[2] = false;
		attIsString[3] = false;
		attIsString[4] = false;
		attIsString[5] = false;
		attIsString[6] = false;
		attIsString[7] = false;
		attIsString[8] = false;
		attIsString[9] = false;
		attIsString[10] = false;
		counterAttribute = 11;
	}

}
