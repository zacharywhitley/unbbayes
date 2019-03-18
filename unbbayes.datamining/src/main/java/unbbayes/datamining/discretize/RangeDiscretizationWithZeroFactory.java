package unbbayes.datamining.discretize;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Instantiates {@link RangeDiscretizationWithZero}
 * @author Shou Matsumoto
 *
 */
public class RangeDiscretizationWithZeroFactory implements
		IDiscretizationFactory {

	public RangeDiscretizationWithZeroFactory() {}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretizationFactory#buildInstance(unbbayes.datamining.datamanipulation.InstanceSet)
	 */
	public IDiscretization buildInstance(InstanceSet instanceSet) {
		return new RangeDiscretizationWithZero(instanceSet);
	}

}
