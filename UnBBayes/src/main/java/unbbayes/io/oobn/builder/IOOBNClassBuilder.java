/**
 * 
 */
package unbbayes.io.oobn.builder;

import unbbayes.io.builder.IProbabilisticNetworkBuilder;

/**
 * Builder for classes and nodes of OOBN module,
 * used by I/O classes to generate desired instances of nodes/classes
 * @author Shou Matsumoto
 *
 */
public interface IOOBNClassBuilder extends IProbabilisticNetworkBuilder {
	
	public IOOBNInstanceNodeBuilder getInstanceNodeBuilder();
	
	public void setInstanceNodeBuilder(IOOBNInstanceNodeBuilder builder);
}
