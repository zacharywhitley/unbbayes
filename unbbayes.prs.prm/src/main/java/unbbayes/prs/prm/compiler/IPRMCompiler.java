/**
 * 
 */
package unbbayes.prs.prm.compiler;

import java.util.Collection;

import unbbayes.controller.prm.IDatabaseController;
import unbbayes.prs.Graph;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRM;

/**
 * Classes implementing this interface must know how to deal with prm
 * and database (optional) to convert it to a graph format 
 * (e.g. {@link unbbayes.prs.bn.ProbabilisticNetwork})
 * @author Shou Matsumoto
 *
 */
public interface IPRMCompiler {
	
	/**
	 * Converts a prm into a Graph format, using query-driven generation.
	 * @param databaseController : a facade to database access methods
	 * @param prm : the prm to be compiled. Every known {@link IAttributeValue} will
	 * be considered as an evidence (finding). 
	 * @param query : a set of queries to be used as a starting point. If set to null, this compilation
	 * algorithm may start from any point or may perform a full data-driven generation.
	 * @return a generated Graph (usually, an instance of {@link unbbayes.prs.bn.ProbabilisticNetwork})
	 */
	public Graph compile(IDatabaseController databaseController, IPRM prm, Collection<IAttributeValue> query);
	
	

}
