package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MSNetwork {
	protected List nets;	
	
	public MSNetwork() {
		nets = new ArrayList();				
	}
	
	public void addNetwork(ProbabilisticNetwork net) {
		nets.add(net);
	}
	
	public void compile() {
		verifyCycle();				
	}
	
	public void verifyCycle() {
				
	}
	
	private void distributedMoralization() {
		ProbabilisticNetwork net = null;
		for (int i = nets.size() - 1; i >= 0; i--) {
			net = (ProbabilisticNetwork) nets.get(i);
			net.moraliza();
		}
		
		// assumed to be the root hyper-node
//		net.distributeArc();
	}	
}
