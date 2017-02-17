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
public interface ICPTFrameFactory {
	
	CPTFrame buildCPTFrame(IMEBNMediator mediator, ResidentNode resident);
}
