/**
 * 
 */
package unbbayes.prs.medg;

import unbbayes.prs.mebn.IMEBNElementFactory;
import unbbayes.prs.mebn.MFrag;

/**
 * @author Shou Matsumoto
 *
 */
public interface IMEDGElementFactory extends IMEBNElementFactory {

	public MultiEntityDecisionNode createDecisionNode(String name, MFrag mfrag);
	public MultiEntityUtilityNode createUtilityNode(String name, MFrag mfrag);
	
}
