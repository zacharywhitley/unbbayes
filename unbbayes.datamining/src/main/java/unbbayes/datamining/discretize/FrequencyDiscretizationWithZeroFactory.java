package unbbayes.datamining.discretize;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Instantiates {@link FrequencyDiscretizationWithZero}
 * @author Shou Matsumoto
 */
public class FrequencyDiscretizationWithZeroFactory implements
		IDiscretizationFactory {

	public FrequencyDiscretizationWithZeroFactory() {}

	/* (non-Javadoc)
	 * @see unbbayes.datamining.discretize.IDiscretizationFactory#buildInstance(unbbayes.datamining.datamanipulation.InstanceSet)
	 */
	public IDiscretization buildInstance(InstanceSet instanceSet) {
		return new FrequencyDiscretizationWithZero(instanceSet);
	}

}
