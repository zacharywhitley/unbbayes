/**
 * 
 */
package unbbayes.prs.mebn.compiler.extension;

import java.util.List;

import unbbayes.prs.mebn.compiler.Compiler.INestedIfElseClauseContainer;
import unbbayes.prs.mebn.compiler.Compiler.IExpressionValue;

/**
 * Classes implementing this interface builds an instance of {@link IUserDefinedFunction}
 * given some parameters.
 * @author Shou Matsumoto
 */
public interface IUserDefinedFunctionBuilder {
	
	/**
	 * @param args : arguments that the script is passing to the function.
	 */
	public void setArguments(List<IExpressionValue> args);
	
	
	/**
	 * @param ifBlock : represents the if-clause block that this function belongs to.
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTable
	 * @see unbbayes.prs.mebn.compiler.Compiler.TempTableHeaderCell
	 */
	public void setIfBlockHeader(INestedIfElseClauseContainer ifBlock);
	
	/**
	 * @return an instance of {@link IUserDefinedFunction}.
	 * @see #setArguments(List)
	 * @see #setIfBlockHeader(INestedIfElseClauseContainer)
	 */
	public IUserDefinedFunction buildUserDefinedFunction();
	
	/**
	 * @return : the name of the function this builder is building.
	 * This should match with what the LPD script is invoking.
	 */
	public String getFunctionName();

}
