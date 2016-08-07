package io;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * This is a generic interface for files that can be used to communicate with 
 * model integration suites, like Phoenix Integration Model Center.
 * Actually, suites for model integration don't need to follow a particular file format
 * (i.e. the user can specify a common file format between two systems when integration between is needed),
 * so this interface is simply a set of common methods for reading or writing
 * files in general.
 * @author Shou Matsumoto
 * @see ModelCenterWrapperIO
 * @see ModelCenterMatrixStyleWrapperIO
 */
public interface IModelCenterWrapperIO {

	/**
	 * @param input : input file to be read.
	 * The content of this file can be accessed from {@link #getProperty(String)}
	 */
	public void readWrapperFile(File input) throws IOException;

	/**
	 * @param property : the key-value pairs to write to output file
	 * @param output : the output file
	 */
	public void writeWrapperFile(Map<String, String> property , File output)  throws IOException;
	
	/**
	 * Content of the wrapper file.
	 * @param key : key of the value to access.
	 * @return the value
	 */
	public String getProperty(String key);
	
	/**
	 * This is an utility method to convert some file to wrapper file.
	 * @param input : the file to read from.
	 * @param output : the file to write to.
	 * @throws IOException
	 */
	public void convertToWrapperOutput(File input, File output) throws IOException;

}