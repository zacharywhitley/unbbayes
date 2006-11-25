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
public class ArffLoader extends Loader {
	/** The filename extension that should be used for arff files */
	public static String FILE_EXTENSION = ".arff";

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.datamanipulation.resources." +
			"DataManipulationResource");

	/**
	 * Reads a ARFF file from a reader.
	 *
	 * @param reader Reader
	 * @exception IOException if the ARFF file is not read successfully
	 */
	public ArffLoader(File file) throws IOException {
		this.file = file;

		// Count instanceSet
        countInstancesFromFile(file);
        
        //Memory initialization
        Reader reader = new BufferedReader(new FileReader(file));
        
        tokenizer = new StreamTokenizer(reader);
        initTokenizer();
	}

	/**
	 * Initializes the StreamTokenizer used for reading the ARFF file.
	 *
	 * @param tokenizer Stream tokenizer
	 */
	protected void initTokenizer() {
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
		tokenizer.parseNumbers();
	}

	protected void countInstancesFromFile(File file) throws IOException {
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inReader = new InputStreamReader(fileIn);
		BufferedReader in = new BufferedReader(inReader);
		
		// read file and count lines
		int count = 0;
		boolean startCount = false;
		String line;
		while ((line = in.readLine()) != null) {
			if (startCount && (!line.startsWith("%"))) {
				count++;
			}
			if ((line.toLowerCase()).equals("@data")) {
				/* Finish reading header */
				startCount = true;
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
	public void readHeader() throws IOException {
        String attributeName;
		String relationName = "";
		ArrayList<String> stringValuesAux;
		ArrayList<Float> numberValuesAux;
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// Get name of relation.
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("readHeaderException1"));
		}
		if (tokenizer.sval.equalsIgnoreCase("@relation")) {
			getNextToken();
			relationName=(tokenizer.sval);
			getLastToken(false);
		} else {
			errms(resource.getString("readHeaderException2"));
		}

		// Get attribute declarations.
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("readHeaderException1"));
		}

		while (tokenizer.sval.equalsIgnoreCase("@attribute")) {
			// Get attribute name.
			getNextToken();
			attributeName = tokenizer.sval;
			getNextToken();

			// Check if attribute is nominal.
			if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
				// Attribute is real, or integer.
				if (tokenizer.sval.equalsIgnoreCase("real")
						|| tokenizer.sval.equalsIgnoreCase("integer")
						|| tokenizer.sval.equalsIgnoreCase("numeric")) {
					attributes.add(new Attribute(attributeName,
							Attribute.NUMERIC, false, initialInstances,
							attributes.size()));
					readTillEOL();
				} else {
					errms(resource.getString("readHeaderException3"));
				}
			} else {
				// Attribute is nominal.
				stringValuesAux = new ArrayList<String>();
				numberValuesAux = new ArrayList<Float>();
				tokenizer.pushBack();

				// Get values for nominal attribute.
				if (tokenizer.nextToken() != '{') {
					errms(resource.getString("readHeaderException4"));
				}
				boolean isString = false;
				while (tokenizer.nextToken() != '}') {
					if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
						errms(resource.getString("readHeaderException5"));
					}
					if (tokenizer.sval != null) {
						/* String value */
						stringValuesAux.add(tokenizer.sval);
						isString = true;
					} else {
						/* Number value */
						numberValuesAux.add((float) tokenizer.nval);
					}
				}
				if (stringValuesAux.size() == 0) {
					errms(resource.getString("readHeaderException6"));
				}
				int sizeValues = stringValuesAux.size();
				
				/* Check if the nominal attribute values are string values */ 
				if (isString) {
					/* The attribute has string values */
					String[] stringValues = new String[sizeValues];
					for (int i = 0; i < sizeValues; i++) {
						stringValues[i] = (String) stringValuesAux.get(i);
					}
					attributes.add(new Attribute(attributeName,
							stringValues, attributes.size()));
				} else {
					/* All attribute values are number values */
					float[] numberValues = new float[sizeValues];
					for (int i = 0; i < sizeValues; i++) {
						numberValues[i] = numberValuesAux.get(i);
					}
					attributes.add(new Attribute(attributeName,
							numberValues, attributes.size()));
				}
			}
			getLastToken(false);
			getFirstToken();
			if (tokenizer.ttype == StreamTokenizer.TT_EOF)
				errms(resource.getString("readHeaderException1"));
		}

		int size = attributes.size();
		Attribute[] attributesArray = new Attribute[size];
		for (int i = 0; i < size; i++) {
			attributesArray[i] = (Attribute)attributes.get(i);
		}
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setRelationName(relationName);

		// Check if data part follows. We can't easily check for EOL.

		if (!tokenizer.sval.equalsIgnoreCase("@data")) {
			errms(resource.getString("readHeaderException7"));
		}

		// Check if any attributes have been declared.
		if (instanceSet.numAttributes() == 0) {
			errms(resource.getString("readHeaderException8"));
		}
	}

	/**
	 * Gets next token, skipping empty lines.
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if reading the next token fails
	 */
	protected void getFirstToken() throws IOException {
		while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			/* Just skip token */
		}
		if (tokenizer.ttype == '\'' || tokenizer.ttype == '"') {
			tokenizer.ttype = StreamTokenizer.TT_WORD;
		} else if (tokenizer.ttype == StreamTokenizer.TT_WORD
				&& tokenizer.sval.equals("?")) {
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
	protected void getLastToken(boolean endOfFileOk) throws IOException {
		if (tokenizer.nextToken() != StreamTokenizer.TT_EOL 
				&& (tokenizer.nextToken() != StreamTokenizer.TT_EOF 
						|| !endOfFileOk)) {
			errms(resource.getString("getLastTokenException1"));
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
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("getNextTokenException2"));
		} else if ((tokenizer.ttype == '\'') || (tokenizer.ttype == '"')) {
			tokenizer.ttype = StreamTokenizer.TT_WORD;
		} else if ((tokenizer.ttype == StreamTokenizer.TT_WORD)
				&& (tokenizer.sval.equals("?"))) {
			tokenizer.ttype = '?';
		}
	}

	/**
	 * Reads and skips all tokens before next end of line token.
	 *
	 * @param tokenizer Stream tokenizer
        * @throws IOException EOF not found
	 */
	private void readTillEOL() throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL)	{
			/* Just skip token */
		}
		tokenizer.pushBack();
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
	public boolean getInstance() throws IOException {
		/* Check if the header has already been read */
		if (instanceSet == null) {
			readHeader();
		}

		// Check if any attributes have been declared.
		if (instanceSet.numAttributes() == 0) {
			errms(resource.getString("getInstanceException1"));
		}

		// Check if end of file reached.
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			return false;
		}

		/* Get the instance */
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
		int numAttributes = instanceSet.numAttributes();
		Attribute attribute;
		
		/* Alocate space for the attributes and the counter variable */
		float[] instance = new float[numAttributes + 1];
		
		/* Default value for the weight of an instance */
		float instanceWeight = 1;
		
		int attIndex = 0;
		String stringValue;
		int numColumns = numAttributes;
		int index;
		
		/* Check if the instanceSet file has a counter attribute */
		if (counterIndex != -1) {
			/* Read the counter attribute */
			++numColumns;
		}
		
		/* 
		 * Create instance. Iterate over all attributes and the counter
		 * variable
		 */
		for (int i = 0; i < numColumns; i++) {
			/* Check if the current attribute is the counter attribute */
			if (i == counterIndex) {
				try {
					instanceWeight = (float) tokenizer.nval;
					continue;
				} catch (NumberFormatException nfe) {
					errms("Atributo de contagem inválido");
				}
			}
			
			/* Get the attribute */
			attribute = instanceSet.getAttribute(i);

			/* Check the type of the token */
			if (tokenizer.sval != null) {
				/* The token is a String */
				stringValue = tokenizer.sval;
				
				/* Check if value is missing */ 
				if (stringValue.equals("?")) {
					instance[attIndex] = Instance.MISSING_VALUE;
				}

				/* Check if value appears in header */
				index = attribute.indexOfValue(stringValue);
				if (index == -1) {
					errms(resource.getString("getInstanceFullException2"));
				}
				instance[attIndex] = index;
			} else {
				/* 
				 * The token is a number. Check if the attribute type was set
				 * to nominal.
				 */
				if (attribute.isNominal()) {
					/* 
					 * The attribute is nominal. Check if value appears in
					 * header. 
					 */
					index = attribute.indexOfValue((float) tokenizer.nval);
					if (index == -1) {
						errms(resource.getString("getInstanceFullException2"));
					}
					instance[attIndex] = index;
				} else {
					/* The attribute is numeric */
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
}



