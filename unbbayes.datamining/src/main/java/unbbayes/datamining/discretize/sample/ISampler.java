/**
 * 
 */
package unbbayes.datamining.discretize.sample;

import unbbayes.datamining.discretize.IDiscretization;

/**
 * Common interface of classes that will undo discretization
 * @author Shou Matsumoto
 */
public interface ISampler extends IDiscretization {
	
	public String getPrefix();
	public void setPrefix(String prefix);
	
	public String getSplitter();
	public void setSplitter(String splitter);
	
	public String getSuffix();
	public void setSuffix(String suffix);
	
}
