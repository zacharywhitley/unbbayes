/**
 * 
 */
package io;

import java.io.File;
import java.io.IOException;

/**
 * @author Shou Matsumoto
 *
 */
public interface INetFileFiller {
	
	/**
	 * Reads data and fill network output stream.
	 * @param dataInput : data input to read (e.g. csv file)
	 * @param netFile : file (e.g. .net file) to read network structure and overwrite conditional probabilities based on input data.
	 * @throws IOException 
	 */
	public void fillNetFile(File dataInput, File netFile) throws IOException;

}
