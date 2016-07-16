package io;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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

}