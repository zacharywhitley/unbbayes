package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

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
		hyperTree();
		verifyCycle();				
	}
	
	private void hyperTree() {
		NodeList interseccoes[][] = new NodeList[nets.size()][nets.size()];
				
		int netsSize = nets.size();
		for (int i = 0; i < netsSize - 1; i++) {
			SubNetwork n1 = (SubNetwork) nets.get(i);
			for (int j = i+1; j < netsSize; j++) {
				SubNetwork n2 = (SubNetwork) nets.get(j);
				NodeList inter = SetToolkit.intersection(n1.getNos(), n2.getNos());
				if (inter.size() > 0) {
					interseccoes[i][j] = inter;					
				}
			}						
		}
	}
	
	private void verifyCycle() {
				
	}	
}
