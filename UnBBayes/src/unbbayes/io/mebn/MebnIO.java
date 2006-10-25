package unbbayes.io.mebn;

import java.io.IOException;

import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Interface of IO for Mebn files.
 * @author Laecio Lima dos Santos
 * @version 1.0
 */

public interface MebnIO {

	public MultiEntityBayesianNetwork loadMebn(String nameFile) throws IOException, IOMebnException;
	
	public void saveMebn(String nameFile, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException; 
	
}