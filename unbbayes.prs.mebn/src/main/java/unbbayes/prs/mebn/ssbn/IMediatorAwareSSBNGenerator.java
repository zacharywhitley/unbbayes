/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.controller.INetworkMediator;

/**
 * @author Shou Matsumoto
 *
 */
public interface IMediatorAwareSSBNGenerator extends ISSBNGenerator {
	/**
	 * Sets the mediator of this ssbn generator.
	 * You may use mediator if you have to access elements
	 * of GUI or controller.
	 * @param mediator : this is a reference to a controller
	 */
	public void setMediator(INetworkMediator mediator);
}
