package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;

/**
 * Make load/save in pr-owl.
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 2006/10/25
 */

public class PrOwlIO implements MebnIO {
	
	public static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 
	
	/* only a fa�ade for the class that realy do the work */
	
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException{
 		LoaderPrOwlIO loader = new LoaderPrOwlIO(); 
 		return loader.loadMebn(file); 
	}
	
	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */
	
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException{
	   SaverPrOwlIO saver = new SaverPrOwlIO();
	   saver.saveMebn(file, mebn); 
	}
	
}