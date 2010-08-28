/**
 * 
 */
package unbbayes.prs.prm.cpt;

import java.util.List;
import java.util.Map;

import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IPRMDependency;
import unbbayes.prs.prm.cpt.compiler.IPRMCPTCompiler;

/**
 * This interface represents a CPT in a PRM class.
 * @author Shou Matsumoto
 *
 */
public interface IPRMCPT {
	
	/**
	 * Obtains a compiler to be used by this CPT.
	 * This class has a reference to its compiler because
	 * a future release may want to use different compilers
	 * for different types of CPT (e.g. a CPT in a script-like
	 * fassion).
	 * @return
	 */
	public IPRMCPTCompiler getCPTCompiler();
	
	/**
	 * Sets a compiler to be used by this CPT.
	 * This class has a reference to its compiler because
	 * a future release may want to use different compilers
	 * for different types of CPT (e.g. a CPT in a script-like
	 * fassion).
	 * @param cptCompiler
	 */
	public void setCPTCompiler(IPRMCPTCompiler cptCompiler);
	
	/**
	 * This represents the PRM node hosting this CPT.
	 * We do not reference {@link IAttributeDescriptor} directly,
	 * since all probabilistic informations must be centered to
	 * {@link IPRMDependency}.
	 * @return
	 */
	public IPRMDependency getPRMDependency();
	

	/**
	 * This represents the PRM node hosting this CPT.
	 * We do not reference {@link IAttributeDescriptor} directly,
	 * since all probabilistic informations must be centered to
	 * {@link IPRMDependency}.
	 * @param hostNode
	 */
	public void setPRMDependency(IPRMDependency hostNode);
	
//	/**
//	 * This is a list of parents of the {@link #getPRMDependency()}.
//	 * That is, this is a list of conditionants of this CPT.
//	 * You must sincronize these values to the ones obtainable
//	 * from {@link IPRMDependency#getDependencyChains()}
//	 * @return
//	 */
//	public List<IDependencyChain> getDependencyChains();
	
	/**
	 * Content of the table.
	 * Usually, this is a linearized table by column.
	 * The following table usually becomes {0.90, 0.10, 0.80, 0.20, 0.30, 0.70, 0.40, 0.60}.
	 * <code><br />
	 *_____________________________<br />
	 *|Parent1|val1|val1|val2|val2|<br />
	 *|Parent2|val1|val2|val1|val2|<br />
	 *|===========================|<br />
	 *|State01|0.90|0.80|0.30|0.40|<br />
	 *|State02|0.10|0.20|0.70|0.60|<br />
	 *------------------------------<br />
	 * </code>
	 * @return the tableValues
	 */
	public List<Float> getTableValues() ;

	/**
	 * Content of the table.
	 * Usually, this is a linearized table by column.
	 * The following table usually becomes {0.90, 0.10, 0.80, 0.20, 0.30, 0.70, 0.40, 0.60}.
	 * <code><br />
	 *_____________________________<br />
	 *|Parent1|val1|val1|val2|val2|<br />
	 *|Parent2|val1|val2|val1|val2|<br />
	 *|===========================|<br />
	 *|State01|0.90|0.80|0.30|0.40|<br />
	 *|State02|0.10|0.20|0.70|0.60|<br />
	 *------------------------------<br />
	 * </code>
	 * @param tableValues the tableValues to set
	 */
	public void setTableValues(List<Float> tableValues);
	
	/**
	 * Extracts a list representing values of a column in a cpt, given
	 * the states of its parents. That is, if CPT is the following table:
	 * 
	 * <code><br /><br />
	 *_____________________________<br />
	 *|Parent1|val1|val1|val2|val2|<br />
	 *|Parent2|val1|val2|val1|val2|<br />
	 *|===========================|<br />
	 *|State01|0.90|0.80|0.30|0.40|<br />
	 *|State02|0.10|0.20|0.70|0.60|<br />
	 *------------------------------<br />
	 * <br /></code>
	 * 
	 * ...And if the argument (i.e. parentStateMap) is {(Parent1 -> val2) , (Parent2 -> val1)},
	 * then the 3rd column (4th, if we also count the column containing states) is the selected
	 * column, and this method will return {0.30, 0.70}.
	 * 
	 * @param cpt
	 * @param parentStateMap : if parentStateMap == null || parentStateMap.isEmpty() == true, this method
	 * returns the 1st column. This is useful when a node has no parent (in such case, the 1st column
	 * would be the table itself). It must contain all the parents of this CPT's owner.
	 * @return : values of the selected column. This is not a reference, so, changes on this list will
	 * not affect {@link #getTableValues()}.
	 */
	public List<Float> getTableValuesByColumn(Map<IPRMDependency, String> parentStateMap);
		
}
