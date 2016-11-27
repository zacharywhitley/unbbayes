/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This is a common interface for reading activity histogram from files.
 * An activity histogram is composed with days, time block, detector, number of users, and the bin (block of the histogram).
 * @author Shou Matsumoto
 */
public interface IActivityHistIO {
	
	public void load(InputStream input) throws IOException;
	
	public int getTotalNumDays();
	
	public int getTotalNumDetectors();
	
	public int getTotalNumTimeBlocks(int detector);
	
	public int getNumBins(int detector, int timeBlock);
	
	public float getBinValueLower(int detector, int timeBlock, int bin);
	
	public float getBinValueUpper(int detector, int timeBlock, int bin);
	
	public List<Integer> getNumUsers(int detector, int timeBlock, int day);
	
	
}
