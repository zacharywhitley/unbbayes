package unbbayes.datamining.discretize.sample;

import unbbayes.datamining.discretize.IDiscretization;
import unbbayes.util.IStateIntervalParser;

/**
 * Common interface of classes that will undo discretization
 * @author Shou Matsumoto
 */
public interface ISampler extends IDiscretization {
	
	/**
	 * @return
	 * Object to parse a state label into 2 numbers
	 * (interval).
	 */
	public IStateIntervalParser getStateIntervalParser();
	
	/**
	 * @param parser
	 * Object to parse a state label into 2 numbers
	 * (interval).
	 */
	public void setStateIntervalParser(IStateIntervalParser parser);
	
}
