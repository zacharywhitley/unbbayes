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
public interface IMultiColumnCSVToMultiFileConverter {

	/**
	 * @param input : file to read
	 * @param outputDirectory : directory to output multiple files
	 * @param prefix : prefix of files to be created in output directory
	 */
	public void convert(File input, File outputDirectory, String prefix)  throws IOException;
	
}

