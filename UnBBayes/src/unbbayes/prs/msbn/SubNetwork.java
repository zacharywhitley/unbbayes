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
	
	
	public SubNetwork(String id) {
		super(id);
		adjacents = new ArrayList();		
	}
	
	public int getAdjacentsSize() {
		return adjacents.size();		
	}
	
	public SubNetwork getAdjacentAt(int index) {
		return (SubNetwork) adjacents.get(index);		
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
		super.compilaAJ(new MSJunctionTree());
	}
	
	protected MSJunctionTree getJunctionTree() {
		return (MSJunctionTree) junctionTree;
	}
	
	protected void initTriangulation() {
		copiaNos = SetToolkit.clone(nos);
		oe = new NodeList(copiaNos.size());
	}
	
	protected void verifyConsistency() throws Exception {
		super.verifyConsistency();		
	}
	
	protected List makePath(SubNetwork net) {
		List path = new ArrayList();
		makePath(net, path);
		return path;			
	}
	
	private boolean makePath(SubNetwork net, List path) {
		if (path.contains(this)) {
			return false;
		}
		
		path.add(this);
		
		if (this.equals(net)) {
			return true;
		}
		
		for (int i = adjacents.size()-1; i>=0; i--) {
			SubNetwork netAux = (SubNetwork) adjacents.get(i);
			if (netAux.makePath(net, path)) {
				return true;				
			}			
		}
		
		if (parent.makePath(net, path)) {
			return true;			
		}	
		
		path.remove(net);
		return false;
	}
	
	/**
	 * 
	 * Triangulacao com peso minimo primeiro só os não d-sepnodes e depois só os d-sep-nodes;	 * 
	 *
	 * @param adj		  Rede adjacente a esta para basear a ordem de eliminação. 		 
	 * @return boolean   true se inseriu algum arco, false caso contrário.
	 */	
	protected boolean elimine(SubNetwork adj) {
		System.out.println("Elimine");
		
		oe.clear();
		NodeList inter = SetToolkit.intersection(copiaNos, adj.nos);				
		NodeList auxNos = SetToolkit.clone(copiaNos);
		
		int sizeAnt = auxNos.size();
				
		auxNos.removeAll(inter);
		
		assert inter.size() + auxNos.size() == sizeAnt;
		
		boolean inseriu = false;		
		while (pesoMinimo(auxNos)) {
			inseriu = true;		
		}
		
		while (pesoMinimo(inter)) {
			inseriu = true;			
		}
		
		makeAdjacents();
		
		return inseriu;
	}
	
	//---------------------------- DEBUG
	public void teste() {
		for (int i = 0; i < nos.size(); i++) {
			Node a = nos.get(i);
			for (int j = 0; j < a.getAdjacents().size(); j++) {
				Node b = a.getAdjacents().get(j);
				if (! b.getAdjacents().contains(a)) {
					System.out.println("erro - " + a + " nao adj de " + b);
				}								
			}			
		}		
	}	
	//---------------------------- DEBUG
	
	protected void elimineProfundidade(SubNetwork caller) {
		System.out.println("Elimine profundidade");
		
		for (int i = adjacents.size()-1; i >= 0; i--) {
			SubNetwork ai = (SubNetwork) adjacents.get(i);			
			if (elimine(ai)) {
				updateArcs(this, ai);
			}
			ai.elimineProfundidade(this);
		}
		
		if (caller != null) {			
			if (elimine(caller)) {				
				updateArcs(this, caller);
			}
		}	
	}
	
	protected void distributeArcs() {
		System.out.println("DistributeArcs");
		
		for (int i = adjacents.size()-1; i>=0; i--) {
			SubNetwork net = (SubNetwork) adjacents.get(i);
			updateArcs(net);
			net.distributeArcs();
		}
	}
	
	private static void updateArcs(SubNetwork net1, SubNetwork net2) {
		NodeList dsepset = SetToolkit.intersection(net1.nos, net2.nos);
		for (int i = net1.arcosMarkov.size()-1; i>=0; i--) {
			Edge e = (Edge) net1.arcosMarkov.get(i);
			if (dsepset.contains(e.getOriginNode()) 
				&& dsepset.contains(e.getDestinationNode())) {
				
				Node a = net2.getNode(e.getOriginNode().getName());
				Node b = net2.getNode(e.getDestinationNode().getName()); 
				
				assert(a != null && b != null);
				if (! a.getAdjacents().contains(b) && ! b.getAdjacents().contains(a)) {
					a.getAdjacents().add(b);
					b.getAdjacents().add(a);
					Edge newEdge = new Edge(a,b);
					net2.arcosMarkov.add(newEdge);

					System.out.println(newEdge);
				}
			}
		}
	}
	
	private void updateArcs(SubNetwork net) {
		System.out.println("updateArcs");
		updateArcs(this, net);
		updateArcs(net, this);		
	}
	
	protected void initVisited() {
		visited = new char[nos.size()];	
	}
	
	protected void distributedCycle() throws Exception {
		for (int i = nos.size()-1; i>=0; i--) {
			dfsCycle(i, null);
		}
	}
	
	/**
     * Depth first search to verify cycle.
     */
    private void dfsCycle(int nodeIndex, Node caller) throws Exception {
    	if (visited[nodeIndex] != 0) {
    		if (visited[nodeIndex] == 1) {
    			// Back edge. Has cycle!
                throw new Exception("CicleNetException");
    		}
    		return;    		
    	}
    	
    	Node node = nos.get(nodeIndex);    	
    	visited[nodeIndex] = 1;
    	
//    	System.out.println(node);
    	
    	for (int i = node.getChildren().size()-1; i >= 0; i--) {
    		int newIndex = getNodeIndex(node.getChildren().get(i).getName());
    		dfsCycle(newIndex, node);
    	}
    	
    	if (parent != null) {    		
	    	int index = parent.getNodeIndex(node.getName());
	    	if (index != -1) {
	    		Node next = parent.getNodeAt(index);
	    		if (caller == null || (caller != null && ! next.equals(caller))) {
	    			parent.dfsCycle(index, node);
	    		}
	    	}
    	}
    	
    	for (int i = adjacents.size()-1; i>=0; i--) {
    		SubNetwork net = (SubNetwork) adjacents.get(i);
    		int index = net.getNodeIndex(node.getName());
    		if (index != -1) {
    			Node next = net.getNodeAt(index);
	    		if (caller == null || (caller != null && ! next.equals(caller))) {
    				net.dfsCycle(index, node);
	    		}
    		}
    	}
    	
    	visited[nodeIndex] = 2;
    }
}
