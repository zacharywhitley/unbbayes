/**
 * 
 */
package unbbayes.prs.mebn.compiler.extension;

import java.util.List;

import unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer;
import unbbayes.prs.mebn.compiler.Compiler.IExpressionValue;

/**
 * @author Shou Matsumoto
 *
 */
public interface IUserDefinedFunction {

	/**
	 * @param args : arguments that the script is passing to the function.
	 */
	public void setArguments(List<IExpressionValue> args);
	
	/**
	 * @return  arguments that the script is passing to the function.
	 */
	public List<IExpressionValue> getArguments();
	
	/**
	 * @param ifBlock : represents the if-clause block that this function belongs to.
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTable
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell
	 */
	public void setIfBlockHeader(INestedIfElseClauseContainer ifBlock);
	
	/**
	 * @return the if-clause block that this function belongs to.
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTable
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell
	 */
	public INestedIfElseClauseContainer getIfBlockHeader();
	
	/**
	 * @return computed result of the function.
	 * May return null if function cannot be computed at this moment
	 * (e.g. when script is being statically compiled, without actually generating SSBN).
	 */
	public String getResult();
	
	/**
	 * @return : the name of the function this builder is building.
	 * This should match with what the LPD script is invoking.
	 */
	public String getFunctionName();
}
