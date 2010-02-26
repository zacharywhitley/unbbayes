/**
 * 
 */
package unbbayes.learning.io;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.prs.Graph;

/**
 * This is just an adapter class in order
 * to enable Learning modules to communicate
 * with UnBBayes's core's default I/O infrastructure.
 * It does not actually load/save a network file.
 * It is used by UnBBayes'core in order to correctly
 * identify that TXT files must be delegated to the owner
 * of this I/O
 * @author Shou Matsumoto
 *
 */
public class LearningDataSetIO implements BaseIO {

	private String name = "Learning";
	
	private ResourceBundle resource = null; 
	
	public static final String SUPPORTED_EXTENSION = "txt";
	
	/**
	 * Default constructor.
	 */
	public LearningDataSetIO() {
		this.resource = unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.learning.resources.Resources.class.getName());
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFileExtensions(boolean)
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly) {
		String[] ret = {SUPPORTED_EXTENSION};
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#getSupportedFilesDescription(boolean)
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		return this.resource.getString("supportedFileDescription");
	}

	/**
	 * This is a stub method.
	 * @param input : this is simply ignored.
	 * @return null
	 */
	public Graph load(File input) throws LoadException, IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	public void save(File output, Graph net) throws IOException {
		throw new IOException(this.resource.getString("outputNotSupported"));
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.BaseIO#supports(java.io.File, boolean)
	 */
	public boolean supports(File file, boolean isLoadOnly) {
		// extracts file extension
		String fileExtension = null;
		try {
			int index = file.getName().lastIndexOf(".");
			if (index >= 0) {
				fileExtension = file.getName().substring(index + 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		//compare file extension with the supported one
		return SUPPORTED_EXTENSION.equalsIgnoreCase(fileExtension);
	}

}
