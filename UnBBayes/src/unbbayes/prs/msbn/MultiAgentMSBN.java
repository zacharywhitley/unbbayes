package unbbayes.prs.msbn;

/**
 * @author michael
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MultiAgentMSBN extends AbstractMSBN {
	public MultiAgentMSBN(String id) {
		super(id);
	}
	
	protected void initBeliefs() {
		//@TODO terminar
		
		for (int i = 0; i < nets.size(); i++) {
			
		}
	}
}
