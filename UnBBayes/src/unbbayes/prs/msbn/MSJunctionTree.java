package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;

/**
 * A JunctionTree created to get visibility to protected methods in this package.
 * @author michael
 */
public class MSJunctionTree extends JunctionTree {
	protected void distribuaEvidencia(Clique c) {
		super.distribuaEvidencia(c);
	}
	
	protected void unificaCrencas() throws Exception {
		consistencia();		
	}
}
