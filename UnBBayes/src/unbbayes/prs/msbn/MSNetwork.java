package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

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
	protected SubNetwork activeNet;	
	
	public MSNetwork() {
		nets = new ArrayList();
		links = new ArrayList();				
	}
	
	public void addNetwork(SubNetwork net) {
		nets.add(net);
	}
	
	public void compile() throws Exception {
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			net.verifyConsistency();		
		}
		
		hyperTree();
		
		verifyCycles();
		
		distributedMoralization();	
				
		cooperativeTriangulation();
		
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			net.compilaAJ();
		}
		
		for (int i = links.size()-1; i>=0; i--) {
			Linkage link = (Linkage) links.get(i);
			link.makeLinkageTree();			
		}
		
		
		SubNetwork raiz = (SubNetwork) nets.get(0);
		distribuaCrencas(raiz);
	}
	
	protected void distribuaCrencas(SubNetwork net) {
		for (int i = net.adjacents.size()-1; i>=0; i--) {
			SubNetwork netAdj = (SubNetwork) net.adjacents.get(i);
			atualizaCrenca(netAdj, net);
			distribuaCrencas(netAdj);
		}
	}
	
	public void desviaAtencao(SubNetwork net) {
		List caminho = activeNet.makePath(net);		
		for (int i = 1; i < caminho.size(); i++) {
			SubNetwork netAux = (SubNetwork) caminho.get(i);
			atualizaCrenca(netAux, activeNet);
			activeNet = netAux;
		}
		
		assert activeNet == net;				
	}
	
	protected void atualizaCrenca(SubNetwork net1, SubNetwork net2) {
		for (int i = 0; i<links.size(); i++) {
			Linkage l = (Linkage) links.get(i);
			if (l.getN1() == net1 && l.getN2() == net2) {
				l.absorve(true);
				return;											
			}
			
			if (l.getN2() == net1 && l.getN1() == net2) {
				l.absorve(false);								
				return;
			}
		}
	}
	
	private void distributedMoralization() {
		for (int i = nets.size()-1; i >= 0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			net.moralize();			
		}
		
		SubNetwork raiz = (SubNetwork) nets.get(0);
		raiz.distributeArcs();
	}
	
	protected void hyperTree() throws Exception {
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
		int max = 0;
		int iMax, kMax;
		iMax = kMax = -1;
		
		for (int i = 0; i < netsSize; i++) {
			if (! naArvore[i]) {
				continue;					
			}			
			for (int k = 1; k < netsSize; k++) {
				if (naArvore[k]) {
					continue;						
				}
				
				if (inters[i][k].size() > max) {
					iMax = i;
					kMax = k;
					max = inters[iMax][kMax].size();
				}
			}		
		}
		
		if (max == 0) {
			throw new Exception("Não forma Hiperárvore");			
		}
		
	 	for (int j = 0; j < netsSize; j++) {
			if (naArvore[j] && ! isDSepSet(j, kMax, inters[j][kMax])) {
				throw new Exception("Erro na contrução da HyperÁrvore");					
			}
		}
		
		SubNetwork ni = (SubNetwork) nets.get(iMax);
		SubNetwork nk = (SubNetwork) nets.get(kMax);
		naArvore[kMax] = true;
		links.add(new Linkage(ni,nk));
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
    protected final void verifyCycles() throws Exception {
    	
    	for (int i = nets.size()-1; i>=0; i--) {
    		SubNetwork net = (SubNetwork) nets.get(i);    		
    		net.initVisited();	
    	}
    	
    	for (int i = nets.size()-1; i>=0; i--) {
    		SubNetwork net = (SubNetwork) nets.get(i);    		
    		net.distributedCycle();    		
    	}
    }
	
	
	protected void cooperativeTriangulation() {
		initTriangulation();			
		coTriag();
		
		boolean inseriu = false;
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			for (int j = net.getAdjacentsSize()-1; j>=0; j--) {
				SubNetwork net2 = (SubNetwork) net.adjacents.get(j);
				if (net.elimine(net2)) {
					inseriu = true;					
				}
			}
		}
		
		if (inseriu) {
			coTriag();				
		}
	}
	
	private void coTriag() {
		System.out.println("coTriag");
		
		SubNetwork a1 = (SubNetwork) nets.get(0);
		a1.elimineProfundidade(null);		
		a1.distributeArcs();
	}
	
	private void initTriangulation() {
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) nets.get(i);
			net.initTriangulation();
		}		
	}
}
