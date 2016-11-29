/**
 * 
 */
package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author Shou Matsumoto
 *
 */
public interface IPeerGroupReader {
	
	public void load(InputStream input) throws IOException;
	
	public List<String> getPeerGroupSizes();
	
	public List<Integer> getPeerGroupCounts();

}
