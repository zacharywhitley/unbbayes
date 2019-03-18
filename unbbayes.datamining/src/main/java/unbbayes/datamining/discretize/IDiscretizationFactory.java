package unbbayes.datamining.discretize;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * Instantiates {@link IDiscretization} with some default
 * configuration
 * @author Shou Matsumoto
 */
public interface IDiscretizationFactory {

	/**
	 * @param instanceSet : data set
	 * @return
	 * Instance of {@link IDiscretization} with some default configuration
	 */
	public IDiscretization buildInstance(InstanceSet instanceSet);
	
}
