/**
 * 
 */
package unbbayes.util;

import java.util.Map;

/**
 * Parses a state label
 * as if it represents an interval of numbers.
 * @author Shou Matsumoto
 */
public interface IStateIntervalParser {

	/**
	 * @return
	 * prefix of label to be ignored
	 */
	public String getPrefix();
	public void setPrefix(String prefix);
	
	public String getSplitter();
	public void setSplitter(String splitter);
	
	public String getSuffix();
	public void setSuffix(String suffix);
	

	/**
	 * It will use {@link #getPrefix()}, {@link #getSplitter()},
	 * and {@link #getSuffix()} to parse a state label and
	 * obtain lower and upper numeric values to sample from.
	 * @param state : state label
	 * @return : {@link Entry} in which {@link Entry#getKey()} is the lower
	 * bound and {@link Entry#getValue()} is the upper bound.
	 */
	public Map.Entry<Float, Float> parseLowerUpperBin(String state);
}
