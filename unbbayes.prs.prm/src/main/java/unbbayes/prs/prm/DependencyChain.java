/**
 * 
 */
package unbbayes.prs.prm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.prm.cpt.IAggregateFunction;

/**
 * @author Shou Matsumoto
 *
 */
public class DependencyChain implements IDependencyChain {

	private List<IForeignKey> foreignKeys;
	private IAggregateFunction aggregateFunction;
	private IPRMDependency dependencyTo;
	private IPRMDependency dependencyFrom;
	
	private Map<IForeignKey, Boolean> inverseFKMap = new HashMap<IForeignKey, Boolean>();

	protected DependencyChain() {
		this.foreignKeys = new ArrayList<IForeignKey>();
		// initialize chain as "no aggregate function is necessary"
//		this.aggregateFunction = AggregateFunctionMode.newInstance(this);
	}
	
	public static DependencyChain newInstance(){
		DependencyChain ret = new DependencyChain();
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#getAggregateFunction()
	 */
	public IAggregateFunction getAggregateFunction() {
		return this.aggregateFunction;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#getDependencyFrom()
	 */
	public IPRMDependency getDependencyFrom() {
		return this.dependencyFrom;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#getDependencyTo()
	 */
	public IPRMDependency getDependencyTo() {
		return this.dependencyTo;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#getForeignKeyChain()
	 */
	public List<IForeignKey> getForeignKeyChain() {
		return this.foreignKeys;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#setAggregateFunction(unbbayes.prs.prm.cpt.IAggregateFunction)
	 */
	public void setAggregateFunction(IAggregateFunction aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#setDependencyFrom(unbbayes.prs.prm.IPRMDependency)
	 */
	public void setDependencyFrom(IPRMDependency from) {
		this.dependencyFrom = from;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#setDependencyTo(unbbayes.prs.prm.IPRMDependency)
	 */
	public void setDependencyTo(IPRMDependency to) {
		this.dependencyTo = to;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#setForeignKeyChain(java.util.List)
	 */
	public void setForeignKeyChain(List<IForeignKey> foreignKeys) {
		this.foreignKeys = foreignKeys;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#isInverseForeignKey(unbbayes.prs.prm.IForeignKey)
	 */
	public boolean isInverseForeignKey(IForeignKey fk) {
		if (this.getForeignKeyChain().contains(fk)) {
			Boolean ret = this.getInverseFKMap().get(fk);
			return (ret != null)?ret:false;
		}
		throw new IllegalArgumentException(
				"The dependency chain \"" 
				+ this 
				+ "\" does not contain FK \"" 
				+ fk 
				+ "\" in its foreign key chain. Please, check dependency and FF consistency.");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.prm.IDependencyChain#markAsInverseForeignKey(unbbayes.prs.prm.IForeignKey, boolean)
	 */
	public void markAsInverseForeignKey(IForeignKey fk, boolean isInverse) {
		if (this.getForeignKeyChain().contains(fk)) {
			this.getInverseFKMap().put(fk, isInverse);
		}
	}

	/**
	 * @return the inverseFKMap
	 */
	public Map<IForeignKey, Boolean> getInverseFKMap() {
		return inverseFKMap;
	}

	/**
	 * @param inverseFKMap the inverseFKMap to set
	 */
	public void setInverseFKMap(Map<IForeignKey, Boolean> inverseFKMap) {
		this.inverseFKMap = inverseFKMap;
	}

}
