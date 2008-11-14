/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.util.SetToolkit;

/**
 * @author michael
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class AbstractMSBN extends Network {
	protected List<SubNetwork> nets;
	protected List<Linkage> links;
	protected String id;
	
	/**
	 * Creates a new multi-sectioned network with the specified id.
	 * @param id	the id of this multi-sectioned network
	 */
	public AbstractMSBN(String id) {
		super(id);
		this.id = id;
		nets = new ArrayList<SubNetwork>();
		links = new ArrayList<Linkage>();				
	}
	
	/**
	 * Returns the number of subnetworks.
	 * @return the number of subnetworks.
	 */
	public int getNetCount() {
		return nets.size();				
	}
	
	/**
	 * Gets the subnetwork of the specified index.
	 * @param index 		the index of subnetwork to get
	 * @return SubNetwork	the subnetwork of the specified index.
	 */
	public SubNetwork getNetAt(int index) {
		return nets.get(index);		
	}
	
	/**
	 * Adds the specified subnetwork to this multi-sectioned network.
	 * @param net	the subnetwork to add.
	 */
	public void addNetwork(SubNetwork net) {
		nets.add(net);
	}
	
	/**
	 * Removes the subnetwork in the specified index.
	 * @param index	the index of the subnetwork to remove.
	 */
	public void remove(int index) {
		nets.remove(index);		
	}
	
	/**
	 * Compile this multi-sectioned network. 
	 * The active Network is the root of the hyper-tree.
	 * @throws Exception
	 */
	public void compile() throws Exception {
		compile(nets.get(0));		
	}
	

	/**
	 * Compile this multi-sectioned network. The active Network is
	 * specified by the user.
	 * @param activeNet the active network after compilation
	 * @throws Exception	if a compilation error occurs.
	 */
	public void compile(SubNetwork activeNet) throws Exception {		
		links.clear();
	
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = nets.get(i);
			net.adjacents.clear();
			net.parent = null;
			
			net.verifyConsistency();		
		}
		
		hyperTree();
		
		verifyCycles();
		
		distributedMoralization();
				
		cooperativeTriangulation();
		
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = nets.get(i);
			net.compileJunctionTree();
		}
		
		for (int i = links.size()-1; i>=0; i--) {
			Linkage link = links.get(i);
			link.makeLinkageTree();			
		}
		
		initBeliefs();
	}
	
	protected abstract void initBeliefs() throws Exception;
	
	protected void coletBeliefs(SubNetwork net) throws Exception {
		for (int i = net.adjacents.size()-1; i>=0; i--) {
			SubNetwork netAdj = (SubNetwork) net.adjacents.get(i);
			if (netAdj.getAdjacentsSize() > 0) { 
				coletBeliefs(netAdj);
			}			
			updateBelief(net, netAdj);
		}
	}
	
	protected void distributeBelief(SubNetwork net) throws Exception {
		for (int i = net.adjacents.size()-1; i>=0; i--) {
			SubNetwork netAdj = (SubNetwork) net.adjacents.get(i);
			updateBelief(netAdj, net);
			if (netAdj.getAdjacentsSize() > 0) { 
				distributeBelief(netAdj);
			}
		}
	}
	

	protected void updateBelief(SubNetwork net1, SubNetwork net2) throws Exception {				
		for (int i = links.size()-1; i>=0; i--) {
			Linkage l = links.get(i);
			if (l.getNet1() == net1 && l.getNet2() == net2) {
				l.absorb(true);
				return;											
			}
			
			if (l.getNet2() == net1 && l.getNet1() == net2) {
				l.absorb(false);
				return;
			}
		}
//		assert false : "N�o podia chegar aqui";
	}
	
	/**
	 * Moralizes this multi-sectioned network.
	 */
	protected void distributedMoralization() {		
		for (int i = nets.size()-1; i >= 0; i--) {
			SubNetwork net = nets.get(i);			
			net.localMoralize();			
		}
		
		SubNetwork raiz = nets.get(0);
		raiz.distributeArcs();
	}
	
	/**
	 * Makes a hypertree structure from this multi-sectioned network.
	 * @throws Exception	if this multi-sectioned network can't construct a hypertree structure.
	 */	
	protected void hyperTree() throws Exception {
		int netsSize = nets.size();
		
		if (netsSize < 2) {
			return;			
		}
		
		ArrayList<Node> inters[][] = makeIntersection();
				
		boolean naArvore[] = new boolean[netsSize];	
		naArvore[0] = true;		
		for (int i = 0; i < netsSize-1; i++) {
			insertLink(inters, naArvore);						
		}
	}
	
	private ArrayList<Node>[][] makeIntersection() {
		int netsSize = nets.size();
		ArrayList<Node> interseccoes[][] = new ArrayList[netsSize][netsSize];			
		
		for (int i = 0; i < netsSize - 1; i++) {
			SubNetwork n1 = nets.get(i);
			for (int j = i+1; j < netsSize; j++) {
				SubNetwork n2 = nets.get(j);
				ArrayList<Node> inter = SetToolkit.intersection(n1.getNodes(), n2.getNodes());				
				interseccoes[i][j] = interseccoes[j][i] = inter;				
			}
		}
		return interseccoes;
	}
	
	private void insertLink(ArrayList<Node> inters[][], boolean naArvore[]) throws Exception {
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
			throw new Exception("N�o forma Hiper�rvore");			
		}
		
	 	for (int j = 0; j < netsSize; j++) {
			if (naArvore[j] && ! isDSepSet(j, kMax, inters[j][kMax])) {
				throw new Exception("Erro na contru��o da Hyper�rvore");					
			}
		}
		
		SubNetwork ni = nets.get(iMax);
		SubNetwork nk = nets.get(kMax);
		naArvore[kMax] = true;
		links.add(new Linkage(ni,nk));
	}
	
	private boolean isDSepSet(int j, int k, ArrayList<Node> inter) {
		SubNetwork nj = nets.get(j);
		SubNetwork nk = nets.get(k);
		for (int i = 0; i < inter.size(); i++) {
			ArrayList<Node> pais = inter.get(i).getParents();			
			if (! nj.getNodes().containsAll(pais) &&
				! nk.getNodes().containsAll(pais)) {
				
				return false;
			}
		}
		return true;
	}
	
	
	
	/**
     *  Verify if this multi-sectioned network has cycle.
     *
     *@throws Exception If this multi-sectioned network has a cycle.
     */
    protected final void verifyCycles() throws Exception {
    	
    	for (int i = nets.size()-1; i>=0; i--) {
    		SubNetwork net = nets.get(i);    		
    		net.initVisited();	
    	}
    	
    	for (int i = nets.size()-1; i>=0; i--) {
    		SubNetwork net = nets.get(i);    		
    		net.distributedCycle();    		
    	}
    }
	
	/**
	 * Triangulates this multi-sectioned network.
	 * Must be called after the distributedMoralization method. 
	 */
	protected void cooperativeTriangulation() {
		initTriangulation();			
		coTriag();
		
		boolean inseriu = false;
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = nets.get(i);
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
		
		SubNetwork a1 = nets.get(0);
		a1.elimineProfundidade(null);		
		a1.distributeArcs();
	}
	
	private void initTriangulation() {
		for (int i = nets.size()-1; i>=0; i--) {
			SubNetwork net = nets.get(i);
			net.initTriangulation();
		}
	}
	
	/**
	 * Returns the id of this multi-sectioned network.
	 * @return the id of this multi-sectioned network.
	 */
	public String getId() {
		return id;
	}
}
