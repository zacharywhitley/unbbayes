package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Interface of IO for Mebn files.
 * @author Laecio Lima dos Santos
 * @version 1.0 2006/10/25
 */

public interface MebnIO {

	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException;
	
	public void saveMebn(String nameFile, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException; 
	
}