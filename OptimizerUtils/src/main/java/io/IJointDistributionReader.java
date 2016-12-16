/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import unbbayes.prs.bn.PotentialTable;

/**
 * Reads some joint distribution (of probabilities or counts) from a file
 * @author Shou Matsumoto
 *
 */
public interface IJointDistributionReader {
	
	/**
	 * 
	 * @param table : updates content of this table by using data from input
	 * @param input : input stream to read
	 * @param isToNormalize : if true, table will be normalized to 1
	 * @return : true if success, false otherwise
	 * @throws IOException
	 */
	public boolean fillJointDist(PotentialTable table, InputStream input, boolean isToNormalize) throws IOException;
	
	/**
	 * @param input
	 * @return Max value per column (variable name) in input stream.
	 * @throws IOException
	 */
	public Map<String, Integer> getMaxValue(InputStream input) throws IOException;
	
	/**
	 * Translates a variable or column name to a valid variable name used in this file format.
	 * @param name
	 * @return
	 */
	public String convertName(String name);

	/**
	 * @param inputStream : stream to read
	 * @param alertDaysThreshold : if any detector's alert days for an user was larger than this value, then user has a system-level alert.
	 * @return how many users had system-level alerts.
	 */
	public int getNumUserSystemAlert(InputStream inputStream, int alertDaysThreshold) throws IOException;

}
