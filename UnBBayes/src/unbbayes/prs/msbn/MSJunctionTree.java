package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;

/**
 * @author michael
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSJunctionTree extends JunctionTree {
	protected void distribuaEvidencia(Clique c) {
		super.distribuaEvidencia(c);
	}
	
	protected void unificaCrencas() throws Exception {
		consistencia();		
	}
}
