package unbbayes.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * This class is a {@link IReaderBuilder} which
 * builds a {@link Reader} which simply
 * reads from string.
 * For such purpose, it builds instances of {@link StringReader}.
 * This is useful to be used in {@link unbbayes.io.NetIO#load(File)}
 * to read from the provided string, instead from the file.
 * @author Shou Matsumoto
 * @see StringPrintStreamBuilder
 * @see unbbayes.io.NetIO
 */
public class StringReaderBuilder implements IReaderBuilder {

	private String stringToRead = null;
	
	/**
	 * Default constructor is kept protected to facilitate inheritance
	 */
	protected StringReaderBuilder() { }
	
	/**
	 * This constructor initializes a string 
	 * to be passed passed to a {@link StringReader}
	 * when a {@link StreamTokenizer} is instantiated in
	 * {@link #getReaderFromFile(File)}
	 * @param stringToRead
	 * @see #setStringToRead(String)
	 */
	public StringReaderBuilder(String stringToRead) { 
		this.setStringToRead(stringToRead);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.IReaderBuilder#getReaderFromFile(java.io.File)
	 */
	public Reader getReaderFromFile(File file)  throws FileNotFoundException{
		if (getStringToRead() != null) {
			// read from the string
			return (new StringReader(this.getStringToRead()));
		} else {
			// consider reading from the file, by default
			return (new BufferedReader(new FileReader(file)));
		}
	}

	/**
	 * This string is passed to a {@link StringReader}
	 * when a {@link StreamTokenizer} is instantiated in
	 * {@link #getReaderFromFile(File)}
	 * @return the stringToRead
	 */
	public String getStringToRead() {
		return stringToRead;
	}

	/**
	 * This string is passed to a {@link StringReader}
	 * when a {@link StreamTokenizer} is instantiated in
	 * {@link #getReaderFromFile(File)}
	 * @param stringToRead the stringToRead to set
	 */
	public void setStringToRead(String stringToRead) {
		this.stringToRead = stringToRead;
	}

}
