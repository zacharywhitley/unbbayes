package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.Network;
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
public class SubNetwork extends Network {
	
	private char[] visited;
	
	protected List adjacents;
	
	protected SubNetwork parent;
	
	
	public SubNetwork() {
		super();
		adjacents = new ArrayList();		
	}
	
	public int getAdjacentsSize() {
		return adjacents.size();		
	}
	
	protected void addAdjacent(SubNetwork net) {
		adjacents.add(net);			
	}
	
	protected void setParent(SubNetwork parent) {
		this.parent = parent;		
	}
	
	protected void moralize() {
		super.moraliza();		
	}
	
	protected void compilaAJ() throws Exception {
		super.compilaAJ();
	}
	
	protected JunctionTree getJunctionTree() {
		return junctionTree;
	}
	
	
	protected void initTriangulation() {
		copiaNos = SetToolkit.clone(nos);
		oe = new NodeList(copiaNos.size());
	}
	
	protected void verifyConsistency() throws Exception {
		super.verifyConsistency();		
	}
	
	/**
	 * 
	 * Triangulacao com peso minimo primeiro s� os n�o d-sepnodes e depois s� os d-sep-nodes;	 * 
	 *
	 * @param adj		  Rede adjacente a esta para basear a ordem de elimina��o. 		 
	 * @return boolean   true se inseriu algum arco, false caso contr�rio.
	 */	
	protected boolean elimine(SubNetwork adj) {
		oe.clear();
		NodeList inter = SetToolkit.intersection(nos, adj.nos);				
		NodeList auxNos = SetToolkit.clone(nos);		
		auxNos.removeAll(inter);
		boolean inseriu = false;		
		while (pesoMinimo(auxNos)) {
			inseriu = true;			
		}
		
		while (pesoMinimo(inter)) {
			inseriu = true;			
		}
		
		return inseriu;
	}
	
	protected void elimineProfundidade(SubNetwork caller) {
		for (int i = adjacents.size()-1; i >= 0; i--) {
			SubNetwork ai = (SubNetwork) adjacents.get(i);			
			if (elimine(ai)) {
				updateArcs(ai);
			}
			ai.elimineProfundidade(this);
		}
		
		if (caller != null) {
			if (elimine(caller)) {
				updateArcs(caller);
			}						
		}
	}
	
	protected void distributeArcs() {
		for (int i = adjacents.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) adjacents.get(i);
			updateArcs(net);
			net.distributeArcs();
		}				
	}
	
	
	private void updateArcs(SubNetwork net) {
		NodeList dsepset = SetToolkit.intersection(nos, net.nos);
		for (int i = arcosMarkov.size()-1; i>=0; i--) {
			Edge e = (Edge) arcosMarkov.get(i);
			if (dsepset.contains(e.getOriginNode()) 
				&& dsepset.contains(e.getDestinationNode())) {
				
				Node a,b;
				a = b = null;
				for (int j = net.getNodeCount()-1; j >= 0; j--) {
					if (net.getNodeAt(j).equals(e.getOriginNode())) {
						a = net.getNodeAt(j);
					} else if (net.getNodeAt(j).equals(e.getDestinationNode())) {
						b = net.getNodeAt(j);
					}			
				}
				assert(a != null && b != null);
				if (! a.getAdjacents().contains(b) && ! b.getAdjacents().contains(a)) {				
					a.getAdjacents().add(b);
					b.getAdjacents().add(a);
					net.arcosMarkov.add(new Edge(a,b));
				}					
			}
		}
		
		for (int i = net.arcosMarkov.size()-1; i>=0; i--) {
			Edge e = (Edge) net.arcosMarkov.get(i);
			if (dsepset.contains(e.getOriginNode()) 
				&& dsepset.contains(e.getDestinationNode())) {
				
				Node a,b;
				a = b = null;
				for (int j = getNodeCount()-1; j >= 0; j--) {
					if (getNodeAt(j).equals(e.getOriginNode())) {
						a = net.getNodeAt(j);
					} else if (getNodeAt(j).equals(e.getDestinationNode())) {
						b = net.getNodeAt(j);
					}							
				}
				assert(a != null && b != null);
				
				if (! a.getAdjacents().contains(b) && ! b.getAdjacents().contains(a)) {				
					a.getAdjacents().add(b);
					b.getAdjacents().add(a);
					arcosMarkov.add(new Edge(a,b));
				}
			}
		}
	}
	
	protected void initVisited() {
		visited = new char[nos.size()];	
	}
	
	protected void distributedCycle() throws Exception {
		for (int i = nos.size()-1; i>=0; i--) {
			dfsCycle(i);
		}		
	}
	
	/**
     * Depth first search to verify cycle.
     */
    private void dfsCycle(int nodeIndex) throws Exception {
    	if (visited[nodeIndex] != 0) { 			
 			// Back edge. Has cycle!
    		if (visited[nodeIndex] == 1) {
                throw new Exception("CicleNetException");
    		}
    		return;    		
    	}
    	
    	visited[nodeIndex] = 1;
    	Node node = nos.get(nodeIndex);
    	for (int i = node.getChildren().size()-1; i >= 0; i--) {
    		int newIndex = getNodeIndex(node.getChildren().get(i).getName());
    		dfsCycle(newIndex);
    	}
    	
    	int index = parent.getNodeIndex(node.getName());    	
    	if (index != -1) {
    		parent.dfsCycle(index);    		    		
    	}
    	
    	for (int i = adjacents.size(); i>=0; i--) {
    		SubNetwork net = (SubNetwork) adjacents.get(i);
    		index = net.getNodeIndex(node.getName());
    		if (index != -1) {
    			net.dfsCycle(index);    		    		
    		}    		
    	}
    	
    	visited[nodeIndex] = 2;
    }
}
