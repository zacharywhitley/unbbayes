/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;

import unbbayes.prs.bn.PotentialTable;

/**
 * Reads some joint distribution (of probabilities or counts) from a file
 * @author Shou Matsumoto
 *
 */
public interface IJointDistributionReader {
	
	public void fillJointDist(PotentialTable table, InputStream input, boolean isToNormalize) throws IOException;

}
