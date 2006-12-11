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
	 * Temporarily constructs the header of a txt file. Used as a preprocessor 
	 * step in the construction of the file's header. 
	 *
	 * @param tokenizer Stream tokenizer
	 * @exception IOException if the information is not read
	 * successfully
	 */
	public void buildHeader() throws IOException {
		readHeader();
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
			relationName = tokenizer.sval;
			getLastToken(false);
		} else {
			errms(resource.getString("readHeaderException2"));
		}

		// Get attribute declarations.
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("readHeaderException1"));
		}

		int counter = 0;
		while (tokenizer.sval.equalsIgnoreCase("@attribute")) {
			// Get attribute name.
			getNextToken();
			attributeName = tokenizer.sval;
			getNextToken();

			/* Get likely counter attribute index */
			if (attributeName.equalsIgnoreCase(counterAttributeName)) {
				likelycounterIndex = counter;
			}
			
			/* Check if the attribute is the counter attribute */
			if (counter == counterIndex) {
				counterAttributeName = attributeName;
				readTillEOL();
				getLastToken(false);
				getFirstToken();
				if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
					errms(resource.getString("readHeaderException1"));
				}
				continue;
			}
			
			if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
				// Attribute is real, or integer.
				if (tokenizer.sval.equalsIgnoreCase("real")
						|| tokenizer.sval.equalsIgnoreCase("integer")
						|| tokenizer.sval.equalsIgnoreCase("numeric")) {
					attributes.add(new Attribute(attributeName,
							Attribute.NUMERIC, false, initialInstances,
							attributes.size()));
					readTillEOL();
				} else if (tokenizer.sval.equalsIgnoreCase("cyclic")) {
					attributes.add(new Attribute(attributeName,
							Attribute.CYCLIC, false, initialInstances,
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
				if (stringValuesAux.size() == 0 && numberValuesAux.size() == 0) {
					errms(resource.getString("readHeaderException6"));
				}
				
				/* Check if the nominal attribute values are string values */ 
				if (isString) {
					int sizeValues = stringValuesAux.size();
					
					/* The attribute has string values */
					String[] stringValues = new String[sizeValues];
					for (int i = 0; i < sizeValues; i++) {
						stringValues[i] = (String) stringValuesAux.get(i);
					}
					attributes.add(new Attribute(attributeName,
							stringValues, attributes.size()));
				} else {
					int sizeValues = numberValuesAux.size();

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
			if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
				errms(resource.getString("readHeaderException1"));
			}
			++counter;
		}

		/* Set the current number of attributes */
		numAttributes = counter;
		attributeIsString = new boolean[numAttributes];
		
		Attribute[] attributesArray = new Attribute[numAttributes];
		int attIndex = 0;
		Attribute attribute;
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the counter attribute */
			if (att != counterIndex) {
				attribute = (Attribute)attributes.get(att);
				attributesArray[attIndex] = attribute;
				attributeIsString[attIndex] = attribute.isString();
				++attIndex;
			}
		}
		
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setRelationName(relationName);
		instanceSet.setCounterAttributeName(counterAttributeName);
		
		attributeType = instanceSet.attributeType;
		
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
	 * Build the attributes.
	 * @throws IOException 
	 */
	public void buildAttributes() throws IOException {
		Attribute[] attributes = new Attribute[numAttributes];
		int numColumns = numAttributes;
		int attIndex = 0;
		
		if (counterIndex != -1) {
			++numColumns;
		}
		
		for (int att = 0; att < numColumns; att++) {
			if (att == counterIndex) {
				/* Counter attribute. Just skip it */
				continue;
			}
			
			/* Build attribute */
			attributes[attIndex] = new Attribute(attributeName[attIndex],
												 attributeType[attIndex],
												 attributeIsString[attIndex],
												 initialInstances,
												 attIndex
												);
							
			++attIndex;
		}
		
		/* Create the instanceSet */
		instanceSet = new InstanceSet(initialInstances, attributes);
		instanceSet.setCounterAttributeName(counterAttributeName);
		
		/* Skip the header of the aaaaaaaaarff file */
		getFirstToken();
		while (!tokenizer.sval.equalsIgnoreCase("@data")) {
			readTillEOL();
			getLastToken(false);
			getFirstToken();
		}
		
		attIndex = 0;
	}


}