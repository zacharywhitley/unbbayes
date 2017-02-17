/**
 * 
 */
package unbbayes.gui.mebn.cpt;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.ResidentNode;

/**
 * @author Shou Matsumoto
 *
 */
public class CPTFrameFactory implements ICPTFrameFactory {

	/**
	 * 
	 */
	public CPTFrameFactory() {}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.cpt.ICPTFrameFactory#buildCPTFrame(unbbayes.controller.mebn.IMEBNMediator, unbbayes.prs.mebn.ResidentNode)
	 */
	public CPTFrame buildCPTFrame(IMEBNMediator mediator, ResidentNode resident) {
		return new CPTFrame((MEBNController) mediator, resident);
	}


}
