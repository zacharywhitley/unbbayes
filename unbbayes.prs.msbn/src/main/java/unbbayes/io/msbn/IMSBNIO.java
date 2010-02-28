/**
 * 
 */
package unbbayes.io.msbn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import unbbayes.io.BaseIO;
import unbbayes.io.exception.LoadException;
import unbbayes.io.exception.SaveException;
import unbbayes.prs.msbn.SingleAgentMSBN;

/**
 * I/O methods for MSBN
 * @author Shou Matsumoto
 *
 */
public interface IMSBNIO extends BaseIO {
	 /**
     * Loads a new MSBN from the input DIRECTORY
     * @param input	Input directory for the MSBN
     * @return The loaded MSBN
     * @throws LoadException If the directory doesn't describes a MSBN.
     * @throws IOException	 If an IO error occurs
     */
    public SingleAgentMSBN loadMSBN(File input) throws LoadException, IOException, JAXBException;
    
    
    /**
     * Saves a MSBN to the output directory.
     * @param output The output file to save
     * @param net		The MSBN to save.
     * @throws SaveException If the output is not a directory.
     */
    public void saveMSBN(File output, SingleAgentMSBN net) throws FileNotFoundException,IOException,  JAXBException, SaveException;
}
