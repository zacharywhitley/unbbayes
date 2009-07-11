/**
 * 
 */
package unbbayes.prs.oobn.compiler;

import unbbayes.prs.Network;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;

/**
 * @author Shou Matsumoto
 *
 */
public interface IOOBNCompiler {

	/**
	 * Converts an OOBN to some other network format
	 * @param oobn: the oobn project as whole
	 * @param mainClass: the class to start compiling
	 * @return: compiled/converted network
	 */
	public Network compile(IObjectOrientedBayesianNetwork oobn, IOOBNClass mainClass);
	
}
