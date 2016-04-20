/**
 * 
 */
package unbbayes.io;

import java.io.File;
import java.io.IOException;

import unbbayes.prs.Graph;

/**
 * Generic interface for I/O classes which loads evidences.
 * @author Shou Matsumoto
 *
 */
public interface IEvidenceIO {

	/**
	 * @param file : file from where the evidences will be loaded.
	 * @param net : the network where the evidences will be inserted into.
	 */
	public void loadEvidences(File file, Graph net) throws IOException;
	

    /**
     * Returns true if the file is supported by this IO class. It may be implemented as simple extension check.
     * False otherwise.
     * @param file : the file to analyze extension.
	 * @param isLoadOnly : if set to true, it should consider file extensions for file loading (input).
	 * If set to false, it should consider both saving and loading. Note
	 * that not every I/O class can implement both loading and saving, and this parameter may separate such
	 * special behaviors.
	 * @return
     */
    public boolean supports(File file, boolean isLoadOnly);
    
    /**
	 * Obtains an array of file extensions supported by this network window.
	 * The file extensions should come without the dot
	 * @param isLoadOnly :  if set to true, it should consider file extensions for file loading (input).
	 * If set to false, it should consider both saving and loading. Note
	 * that not every module/plugin can implement both loading and saving, and this parameter may separate such
	 * special behaviors.
	 * @return
	 */
	public String[] getSupportedFileExtensions(boolean isLoadOnly);
	
	/**
	 * Gets a description of supported file extensions,
	 * which may be shown to the user through file chooser's file filter to explain what
	 * file format are supported.
	 * E.g. "File Wrapper (.fileWrapper), Input (.in)"
	 * @param isLoadOnly :  if set to true, it should consider file extensions for file loading (input).
	 * If set to false, it should consider both saving and loading. Note
	 * that not every module/plugin can implement both loading and saving, and this parameter may separate such
	 * special behaviors.
	 */
	public String getSupportedFilesDescription(boolean isLoadOnly);
	
	
	/**
	 * Gets the name of this I/O component.
	 * This name may be displayed to a user when there is a need to choose a
	 * specific I/O class to use.
	 * @return a name
	 */
	public String getName();
	
}
