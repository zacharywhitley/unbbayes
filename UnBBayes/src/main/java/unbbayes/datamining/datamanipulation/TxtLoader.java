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
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class TxtLoader extends Loader {
	/** The filename extension that should be used for txt files */
	public static final String FILE_EXTENSION = ".txt";

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");

	/**
	 * Reads a TXT file from a reader.
	 * @param file
	 * @param numLines Desired number of lines to count
	 *
	 * @exception IOException if the TXT file is not read
	 * successfully
	 */
	public TxtLoader(File file, int numLines) throws IOException {
		this.file = file;
		
		// Count instanceSet
		countInstancesFromFile(file, numLines, true);
		
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
		tokenizer.whitespaceChars(',',',');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.eolIsSignificant(true);
//		tokenizer.parseNumbers(); // not working with floating (e.g. 1.23E8)
	}

	public void buildHeader() throws IOException {
		/* Create am arraylist for the attributes */
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		
		/* Update initialInstances excluding the header */
		--initialInstances;
		
		/* Build attributes */
		int counter = 0;
		String attName;
		getNextToken();
		while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
			attName = tokenizer.sval;
			
			/* Check if there is a counter attribute */
			if (attName.equalsIgnoreCase(counterAttributeName)) {
				likelyCounterIndex = counter;
			}

			/* Check if the header is ok */
			if (attName == null) {
				errms(resource.getString("Invalid header"));
			}
			
			/* Build attribute */
			attributes.add(new Attribute(attName,
										(byte) 1,
										true,
										initialInstances,
										counter));
			tokenizer.nextToken();
			++counter;
		}
		
		numAttributes = counter;
		attributeType = new byte[numAttributes];
		attributeIsString = new boolean[numAttributes];
		Attribute[] attributesArray = new Attribute[numAttributes];
		
		for (int att = 0; att < numAttributes; att++) {
			attributesArray[att] = (Attribute)attributes.get(att);
			attributeType[att] = InstanceSet.NOMINAL;
			attributeIsString[att] = true;
		}
		
		/* Create the instanceSet */
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setCounterAttributeName(counterAttributeName);
	}

	public ArrayList<Object> getHeaderInfo() throws IOException {
		/* Create am arraylist for the attributes' names */
		ArrayList<String> attributesName = new ArrayList<String>();
		String likelyCounterName = null;
		String relationName = null;
		
		/* Build attributes */
		getNextToken();
		while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
			attributesName.add(tokenizer.sval);
			
			/* Check if there is a counter attribute */
			if (tokenizer.sval.equalsIgnoreCase(counterAttributeName)) {
				likelyCounterName = tokenizer.sval;
			}

			/* Check if the header is ok */
			if (tokenizer.sval == null) {
				errms(resource.getString("Invalid header"));
			}
			
			tokenizer.nextToken();
		}
		
		/* Build the result arraylist */
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(attributesName);
		result.add(likelyCounterName);
		result.add(relationName);
		
		return result;
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
		
		/* Insert attributes in the new dataset */
		getNextToken();
		int attIndex = 0;
		String attName;
		
		/* Update initialInstances excluding the header */
		--initialInstances;
		
		compacted = false;
		while (tokenizer.ttype != StreamTokenizer.TT_EOL) {
			attName = tokenizer.sval;

			/* Check if the header is ok */
			if (attName == null) {
				errms(resource.getString("Invalid header"));
			}
			
			/* Check if the current attribute is the counter */
			if (attIndex == counterIndex) {
				/* Get counter's name and skip to the next attribute */
				compacted = true;
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
		
		/* Set the current number of attributes */
		numAttributes = attributes.size();
		
		Attribute[] attributesArray = new Attribute[numAttributes];
		
		for (int i = 0; i < numAttributes; i++) {
			attributesArray[i] = (Attribute)attributes.get(i);
		}
		
		/* Create the instanceSet */
		instanceSet = new InstanceSet(initialInstances, attributesArray);
		instanceSet.setCounterAttributeName(counterAttributeName);
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
	protected boolean getInstanceAux() throws IOException {
		/* Alocate space for the attributes and the counter variable */
		float[] instance = new float[numAttributes + 1];
		
		/* Default value for the weight of an instance */
		float instanceWeight = 1;
		
		int attIndex = 0;
		String stringValue;
		int columns = numAttributes;
		
		/* Check if the instanceSet file has a counter attribute */
		if (compacted) {
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
					float numValue = (float) Double.parseDouble(tokenizer.sval);
					instanceWeight = numValue;
					continue;
				} catch (NumberFormatException nfe) {
					errms("Atributo de contagem inv�lido");
				}
			}
			
			if (attributeType[attIndex] == NOMINAL) {
				stringValue = tokenizer.sval;
				Attribute attribute = instanceSet.getAttribute(attIndex);
				
				/* Check if the attribute is made of String values */
				if (attributeIsString[attIndex]) {
					/* The token is a String */
					stringValue = tokenizer.sval;
					
					/* Check if value is missing */ 
					if (stringValue.equals("?")) {
						instance[attIndex] = Instance.MISSING_VALUE;
					}
	
					/* Map the current String value to an internal value */ 
					instance[attIndex] = attribute.addValue(stringValue);
				} else {
					/* Map the current String value to an internal value */
					float numValue = (float) Double.parseDouble(tokenizer.sval);
					instance[attIndex] = attribute.addValue(numValue);
				}
			} else {
				/*
				 * The attribute is not nominal thus only numbers are allowed
				 * here.
				 */
				float numValue = (float) Double.parseDouble(tokenizer.sval);
				instance[attIndex] = numValue;
			}
			++attIndex;
			tokenizer.nextToken();
		}
		
		/* Set the weight of this instance */
		instance[attIndex] = instanceWeight;
		
		/* Add the current instance to the instanceSet */
		instanceSet.insertInstance(instance);

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

}