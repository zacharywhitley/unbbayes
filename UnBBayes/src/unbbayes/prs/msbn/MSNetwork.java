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
	protected List links;
	
	
	public MSNetwork() {
		nets = new ArrayList();
		links = new ArrayList();				
	}
	
	public void addNetwork(ProbabilisticNetwork net) {
		nets.add(net);
	}
	
	public void compile() throws Exception {
		hyperTree();
		verifyCycles();
		
		for (int i = nets.size()-1; i >= 0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			net.moralize();			
		}
		
		SubNetwork raiz = (SubNetwork) nets.get(0);
		raiz.distributeArcs();
		
		cooperativeTriangulation();						
	}
	
	private void hyperTree() throws Exception {
		int netsSize = nets.size();
		
		if (netsSize < 2) {
			return;			
		}
		
		NodeList inters[][] = makeIntersection();
				
		boolean naArvore[] = new boolean[netsSize];	
		naArvore[0] = true;		
		for (int i = 0; i < netsSize-1; i++) {
			insertLink(inters, naArvore);						
		}
	}
	
	private NodeList[][] makeIntersection() {
		int netsSize = nets.size();
		NodeList interseccoes[][] = new NodeList[netsSize][netsSize];				
		
		for (int i = 0; i < netsSize - 1; i++) {
			SubNetwork n1 = (SubNetwork) nets.get(i);
			for (int j = i+1; j < netsSize; j++) {
				SubNetwork n2 = (SubNetwork) nets.get(j);
				NodeList inter = SetToolkit.intersection(n1.getNos(), n2.getNos());				
				interseccoes[i][j] = interseccoes[j][i] = inter;				
			}
		}
		return interseccoes;
	}
	
	private void insertLink(NodeList inters[][], boolean naArvore[]) throws Exception {
		int netsSize = nets.size();
		int iMax = 0, kMax = 1;
		
		for (int i = 0; i < netsSize; i++) {
			if (! naArvore[i]) {
				continue;					
			}			
			for (int k = 1; k < netsSize; k++) {
				if (naArvore[k]) {
					continue;						
				}
				
				if (inters[i][k].size() > inters[iMax][kMax].size()) {
					iMax = i;
					kMax = k;												
				}
			}						
		}
		
		for (int j = 0; j < netsSize; j++) {
			if (naArvore[j] && ! isDSepSet(j, kMax, inters[j][kMax])) {
				throw new Exception("Erro na contru��o da Hyper�rvore");					
			}
		}
		
		SubNetwork ni = (SubNetwork) nets.get(iMax);
		SubNetwork nk = (SubNetwork) nets.get(kMax);
		naArvore[kMax] = true;
		links.add(new Link(ni,nk));
	}
	
	private boolean isDSepSet(int j, int k, NodeList inter) {
		SubNetwork nj = (SubNetwork) nets.get(j);
		SubNetwork nk = (SubNetwork) nets.get(k);
		for (int i = 0; i < inter.size(); i++) {
			NodeList pais = inter.get(i).getParents();			
			if (! nj.getNos().containsAll(pais) &&
				! nk.getNos().containsAll(pais)) {
				
				return false;
			}
		}
		return true;
	}
	
	
	
	/**
     *  Verify if this network has cycle.
     *
     *@throws Exception If this network has a cycle.
     */
    public final void verifyCycles() throws Exception {
    	for (int i = nets.size(); i>=0; i--) {
    		SubNetwork net = (SubNetwork) nets.get(i);    		
    		net.initVisited();    		
    	}
    	
    	for (int i = nets.size(); i>=0; i--) {
    		SubNetwork net = (SubNetwork) nets.get(i);    		
    		net.distributedCycle();    		
    	}
    }
	
	
	private void cooperativeTriangulation() {
		coTriag();
		
		List arcos = new ArrayList();
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			for (int j = net.getAdjacentsSize()-1; j>=0; j--) {
				arcos.addAll(net.elimine(j));		
			}
		}
		
		if (arcos.size() > 0) {
			coTriag();						
		}
	}
	
	private void coTriag() {
		SubNetwork a1 = (SubNetwork) nets.get(0);
		a1.elimineProfundidade();
		a1.distributeArcs();
	}
}
