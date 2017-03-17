/**
 * 
 */
package unbbayes.gui.mebn.cpt;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.mebn.ResidentNode;

/**
 * @author Shou Matsumoto
 *
 */
public class RSyntaxFrameFactory extends CPTFrameFactory  {

	/**
	 * 
	 */
	public RSyntaxFrameFactory() {}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.cpt.CPTFrameFactory#buildCPTFrame(unbbayes.controller.mebn.IMEBNMediator, unbbayes.prs.mebn.ResidentNode)
	 */
	public CPTFrame buildCPTFrame(IMEBNMediator mediator, ResidentNode resident) {
		return new RSyntaxTextAreaCPTFrame(mediator, resident);
	}

	
}
