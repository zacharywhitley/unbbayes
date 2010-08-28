/**
 * 
 */
package unbbayes.controller.prm;

import java.util.Collection;

import unbbayes.prs.Graph;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.compiler.IPRMCompiler;

/**
 * This is the main controller for PRM project.
 * This is basically a facade or a mediator.
 * @author Shou Matsumoto
 *
 */
public interface IPRMController {
	
	/**
	 * Obtains the database controller used by this prm project
	 * @return
	 */
	public IDatabaseController getDatabaseController();
	

	/**
	 * Sets the database controller used by this prm project
	 * @param dbController
	 */
	public void setDatabaseController(IDatabaseController dbController);
	
	/**
	 * This is the compiler to be used by this controller to convert
	 * a PRM to another format.
	 * @return
	 */
	public IPRMCompiler getPRMCompiler();
	
	
	/**
	 * This is the compiler to be used by this controller to convert
	 * a PRM to another format.
	 * @param prmCompiler
	 */
	public void setPRMCompiler(IPRMCompiler prmCompiler);
	
	
	/**
	 * Converts a prm into a Graph format, using query-driven generation.
	 * @param prm : the prm to be compiled. Every known {@link IAttributeValue} will
	 * be considered as an evidence (finding). 
	 * @param query : a set of queries to be used as a starting point. If set to null, this compilation
	 * algorithm may start from any point or may perform a full data-driven generation.
	 * @return a generated Graph (usually, an instance of {@link unbbayes.prs.bn.ProbabilisticNetwork})
	 */
	public Graph compilePRM(IPRM prm, Collection<IAttributeValue> query);
	
	
}
