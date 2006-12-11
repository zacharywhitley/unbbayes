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
				likelycounterIndex = counter;
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