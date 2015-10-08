/**
 * 
 */
package unbbayes.prs.medg;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.ResidentNode;

/**
 * Classes implementing this interface represent nodes specific to MEDG
 * (e.g. decision nodes and utility nodes)
 * @author Shou Matsumoto
 *
 */
public interface IMEDGNode extends IMultiEntityNode {
	
	
	/**
	 * @return obtains an equivalent representation of this node as an instance of {@link ResidentNode}
	 */
	ResidentNode asResidentNode();
	
	IMEBNMediator getMediator();
}
