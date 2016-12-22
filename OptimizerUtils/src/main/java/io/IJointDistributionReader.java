/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
	 * @param totalCountKey : this will be the key in returned map with the total number of users with system alerts 
	 * (without double-counting users with alerts in different detectors). Setting this to null or empty will make the returned map
	 * not to contain such total number of users with system alerts.
	 * @return how many users had system-level alerts, for each attribute (i.e. each detectors).
	 * Keys of this map is the name of attribute (detector), and values of this map is the number of users with system alert
	 * coming from that detector.
	 * The value mapped with the key specified in the totalCountKey argument will contain the total number of users with system alerts,
	 * without double-counting users with alerts from multiple detectors
	 */
	public Map<String, Integer> getNumUserSystemAlert(InputStream inputStream, int alertDaysThreshold, String totalCountKey) throws IOException;
	

}
