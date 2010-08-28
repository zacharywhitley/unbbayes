package unbbayes.prs.prm.cpt;

import java.util.List;

import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IDependencyChain;

/**
 * This interface represents an aggregate function
 * (e.g. mode, mean, min, max, median, etc.) to be
 * applied to a set of parents of a node in order
 * to calculate its CPT.
 * @author Shou Matsumoto
 *
 */
public interface IAggregateFunction {
	
	/**
	 * Obtains the dependency chain where this aggregate
	 * function resides.
	 * @return
	 */
	public IDependencyChain getDependencyChain();
	
	/**
	 * Sets the dependency chain where this aggregate
	 * function resides.
	 * @param dependencyChain
	 */
	public void setDependencyChain(IDependencyChain dependencyChain);
	
	/**
	 * Obtains the result of this aggregation function.
	 * That is, if this aggregate function is a mode,
	 * then it returns one of the most repeating parents' value.
	 * Note that this method may return a sample value, so
	 * it may or may not return one of the parents.
	 * Please, note that {@link IAttributeValue} 
	 * uses {@link IAttributeValue#getValue()} as its
	 * actual content.
	 * @param parents
	 * @return
	 */
	public IAttributeValue evaluate(List<IAttributeValue> parents);
	
	/**
	 * The name of this aggregation function.
	 * @return
	 */
	public String getName();
	
	/**
	 * The name of this aggregation function.
	 * @param name
	 */
	public void setName(String name);
}
