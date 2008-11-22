/**
 * 
 */
package unbbayes.io.oobn;

import java.io.File;
import java.io.IOException;

import unbbayes.io.BaseIO;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public interface IObjectOrientedBayesianNetworkIO extends BaseIO {

	public static String fileExtension = "oobn";
	
	
	public IObjectOrientedBayesianNetwork loadOOBN(File classFile)  throws IOException;
}
