/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.datamanipulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * This class opens a arff file building an InstanceSet object
 * 
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (16/02/2002)
 */
public class ArffLoader extends Loader {
	/** The filename extension that should be used for arff files */
	public static String FILE_EXTENSION = ".arff";

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.datamanipulation.resources.DataManipulationResource.class.getName());

	/**
	 * Reads a ARFF file from a reader.
	 * 
	 * @param file
	 * @param numLines
	 *            Desired number of lines to count
	 * 
	 * @exception IOException
	 *                if the ARFF file is not read successfully
	 */
	public ArffLoader(File file, int numLines) throws IOException {
		this.file = file;

		// Count instanceSet
		countInstancesFromFile(file, numLines, false);

		// Memory initialization
		Reader reader = new BufferedReader(new FileReader(file));

		tokenizer = new StreamTokenizer(reader);
		initTokenizer();
	}

	protected void initTokenizer() {
		tokenizer.resetSyntax();
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.wordChars(' ' + 1, '\u00FF');
		tokenizer.whitespaceChars(',', ',');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.quoteChar('\'');
		tokenizer.ordinaryChar('{');
		tokenizer.ordinaryChar('}');
		tokenizer.eolIsSignificant(true);
//		tokenizer.parseNumbers(); // not working with floating (e.g. 1.23E8)
	}

	public void buildHeader() throws IOException {
		readHeader();
	}

	public ArrayList<Object> getHeaderInfo() throws IOException {
		ArrayList<String> attributesName = new ArrayList<String>();
		String likelycounterIndexName = null;
		String relationName = null;

		/* Get name of relation */
		getFirstToken();
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("readHeaderException1"));
		}
		if (tokenizer.sval.equalsIgnoreCase("@relation")) {
			getNextToken();
			relationName = tokenizer.sval;
			if (relationName.equalsIgnoreCase("null")) {
				relationName = null;
			}
			getLastToken(false);
		} else {
			errms(resource.getString("readHeaderException2"));
		}

		/* Get attribute declarations */
		getFirstToken();
		String attributesNameAux;
		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			errms(resource.getString("readHeaderException1"));
		}
		while (tokenizer.sval.equalsIgnoreCase("@attribute")) {
			/* Get attribute name */
			getNextToken();
			attributesNameAux = tokenizer.sval;
			attributesName.add(attributesNameAux);
			getNextToken();

			/* Get likely counter attribute index */
			if (attributesNameAux != null && 
					attributesNameAux.equalsIgnoreCase(counterAttributeName)) {
				likelycounterIndexName = new String(attributesNameAux);
			}

			readTillEOL();
			getFirstToken();
			if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
				errms(resource.getString("readHeaderException1"));
			}
		}

		/* Set the current number of attributes */
		numAttributes = attributesName.size();

		// Check if data part follows. We can't easily check for EOL.
		if (!tokenizer.sval.equalsIgnoreCase("@data")) {
			errms(resource.getString("readHeaderException7"));
		}

		// Check if any attributes have been declared.
		if (numAttributes == 0) {
			errms(resource.getString("readHeaderException8"));
		}
		
		/* Build the result arraylist */
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(attributesName);
		result.add(likelycounterIndexName);
		result.add(relationName);
		
		return result;
	}

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
		compacted = false;
		while (tokenizer.sval.equalsIgnoreCase("@attribute")) {
			// Get attribute name.
			getNextToken();
			attributeName = tokenizer.sval;
			getNextToken();

			/* Get likely counter attribute index */
			if (attributeName.equalsIgnoreCase(counterAttributeName)) {
				likelyCounterIndex = counter;
			}

			/* Check if the attribute is the counter attribute */
			if (counter == counterIndex) {
				compacted = true;
				counterAttributeName = attributeName;
				readTillEOL();
				getLastToken(false);
				getFirstToken();
				if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
					errms(resource.getString("readHeaderException1"));
				}
				++counter;
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
					try {
						float numValue;
						numValue = (float) Double.parseDouble(tokenizer.sval);

						/* Number value */
						numberValuesAux.add(numValue);
					} catch (NumberFormatException nfe) {
						/* String value */
						stringValuesAux.add(tokenizer.sval);
						isString = true;
					}
				}
				if (stringValuesAux.size() == 0 && numberValuesAux.size() == 0) {
					errms(resource.getString("readHeaderException6"));
				}

				/* Check if the nominal attribute values are string values */
				if (isString) {
					/* The attribute has string values */
					int numStringValues = stringValuesAux.size();
					int numNumericValues = numberValuesAux.size();
					int totalSize = numStringValues + numNumericValues;
					String[] stringValues = new String[totalSize];

					/* Get string values */
					for (int i = 0; i < numStringValues; i++) {
						stringValues[i] = stringValuesAux.remove(0);
					}

					/* Get numeric values (if exists) */
					for (int i = numStringValues; i < totalSize; i++) {
						stringValues[i] = numberValuesAux.remove(0).toString();
					}

					/* Create attribute and insert it in the attribute array */
					attributes.add(new Attribute(attributeName, stringValues,
							attributes.size()));
				} else {
					int sizeValues = numberValuesAux.size();

					/* All attribute values are number values */
					float[] numberValues = new float[sizeValues];
					for (int i = 0; i < sizeValues; i++) {
						numberValues[i] = numberValuesAux.remove(0);
					}

					/* Create attribute and insert it in the attribute array */
					attributes.add(new Attribute(attributeName, numberValues,
							attributes.size()));
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
		numAttributes = attributes.size();
		attributeIsString = new boolean[numAttributes];

		Attribute[] attributesArray = new Attribute[numAttributes];
		Attribute attribute;
		for (int att = 0; att < numAttributes; att++) {
			attribute = (Attribute) attributes.get(att);
			attributesArray[att] = attribute;
			attributeIsString[att] = attribute.isString();
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
	 * @param tokenizer
	 *            Stream tokenizer
	 * @exception IOException
	 *                if reading the next token fails
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
	 * @param tokenizer
	 *            Stream tokenizer
	 * @param endOfFileOk
	 *            Checks if it's end of line
	 * @exception IOException
	 *                if it doesn't find an end of line
	 */
	protected void getLastToken(boolean endOfFileOk) throws IOException {
		if (tokenizer.nextToken() != StreamTokenizer.TT_EOL
				&& (tokenizer.nextToken() != StreamTokenizer.TT_EOF || !endOfFileOk)) {
			errms(resource.getString("getLastTokenException1"));
		}
	}

	/**
	 * Gets next token, checking for a premature end of line.
	 * 
	 * @param tokenizer
	 *            Stream tokenizer
	 * @exception IOException
	 *                if it finds a premature end of line
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
	 * @param tokenizer
	 *            Stream tokenizer
	 * @throws IOException
	 *             EOF not found
	 */
	private void readTillEOL() throws IOException {
		while (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
			/* Just skip token */
		}
		tokenizer.pushBack();
	}

	/**
	 * Reads a single instance using the tokenizer and appends it to the
	 * dataset. Automatically expands the dataset if it is not large enough to
	 * hold the instance.
	 * 
	 * @return False if end of file has been reached
	 * @exception IOException
	 *                if the information is not read successfully
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
	 * Reads a single instance using the tokenizer and appends it to the
	 * dataset. Automatically expands the dataset if it is not large enough to
	 * hold the instance.
	 * 
	 * @return False if end of file has been reached
	 * @exception IOException
	 *                if the information is not read successfully
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
		 * Create instance. Iterate over all attributes and the counter variable
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
				try {
				double numValue = Double.parseDouble(tokenizer.sval);
				instance[attIndex] = (float) numValue;
				} catch (Exception e) {
					boolean pause = true;
				}
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
	 * Build the attributes.
	 * 
	 * @throws IOException
	 */
	public void buildAttributes(boolean[] buildNominalFromHeader,
			Attribute[] attributesAux) throws IOException {
		Attribute[] attributes = new Attribute[numAttributes];
		int numColumns = numAttributes;
		int attIndex = 0;

		if (compacted) {
			++numColumns;
		}

		for (int att = 0; att < numColumns; att++) {
			if (att == counterIndex) {
				/* Counter attribute. Just skip it */
				continue;
			}

			/* Build attribute */
			if (buildNominalFromHeader[attIndex]) {
				attributes[attIndex] = attributesAux[att];
			} else {
				attributes[attIndex] = new Attribute(attributeName[attIndex],
						attributeType[attIndex], attributeIsString[attIndex],
						initialInstances, attIndex);
			}

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