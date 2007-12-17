package unbbayes.prs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.NodeList;

public class Network implements Graph{

	protected String id;
	protected String name;
	
	/**
	 *  List of nodes that this network has.
	 */
	protected NodeList nodeList;
	/**
	 *  List of edges that this network has.
	 */
	protected List<Edge> edgeList;
	
	protected Map<String,Integer> nodeIndexes;

	public Network(String name) {
		this.id = this.name = name;
		nodeList = new NodeList();
        edgeList = new ArrayList<Edge>();
        nodeIndexes = new HashMap<String,Integer>();
	}

	/**
	 *  Retorna os edgeList do grafo.
	 *
	 *@return    edgeList do grafo.
	 */
	public List<Edge> getEdges() {
	    return this.edgeList;
	}

	/**
	 *  Retorna os n�s do grafo.
	 *
	 *@return    n�s do grafo.
	 * 
	 * @todo Eliminar esse metodo! eh utilizado na classe NetWindow
	 */
	public NodeList getNodes() {
	    return this.nodeList;
	}

	/**
	 *  Returna o n�mero de vari�veis da rede.
	 *
	 *@return    n�mero de vari�veis da rede.
	 */
	public int getNodeCount() {
		return nodeList.size();
	}

	/**
	 *  Retorna o n� do grafo com o respectivo �ndice.
	 *
	 *@param  index  �ndice do n�.
	 *@return	n� com respectivo �ndice no List.
	 */
	public Node getNodeAt(int index) {
	    return nodeList.get(index);
	}

	/**
	 *  Retorna o n� do grafo com a respectiva sigla.
	 *
	 *@param  name  nome do n�.
	 *@return       n� com a respectiva sigla.
	 */
	public Node getNode(String name) {
		int index = getNodeIndex(name);
		if (index == -1) return null;
		return nodeList.get(index);
	}

	/**
	 * @todo prever o caso de mudanca de nome de nodeList.
	 */
	public int getNodeIndex(String name) {
		Integer index = nodeIndexes.get(name);
		if (index == null) {
			return -1;    		
		}
		return index.intValue();
	}

	/**
	 *  Retira do grafo o arco especificado.
	 *
	 *@param  arco  arco a ser retirado.
	 */
	public void removeEdge(Edge arco) {
	    arco.getOriginNode().getChildren().remove(arco.getDestinationNode());
	    arco.getDestinationNode().getParents().remove(arco.getOriginNode());
	    removeArco(arco);
	}

	/**
	 *  Adiciona novo n� ao grafo.
	 *
	 *@param  no  n� a ser inserido.
	 */
	public void addNode(Node no) {
	    nodeList.add(no);
	    nodeIndexes.put(no.getName(), new Integer(nodeList.size()-1));
	}

	/**
	 *  Adds an edge into the net.
	 *  
	 *  - The table of the destination node will be updated with the new Variable
	 *
	 *@param  edge  An edge to be inserted.
	 */
	public void addEdge(Edge edge) {
		edge.getOriginNode().getChildren().add(edge.getDestinationNode());
		edge.getDestinationNode().getParents().add(edge.getOriginNode());
	    edgeList.add(edge);
	    if (edge.getDestinationNode() instanceof ITabledVariable) {
			ITabledVariable v2 = (ITabledVariable) edge.getDestinationNode();
			PotentialTable auxTab = v2.getPotentialTable();
			auxTab.addVariable(edge.getOriginNode());
		}
	}

	/**
	 *  Remove node of the graph
	 *
	 *@param  elemento  no a ser removido.
	 */
	public void removeNode(Node elemento) {
	    int c;
	    Node auxNo;
	    Edge auxArco;
	    
	    nodeList.remove(elemento);
	    
	    //nodeIndexes.remove(elemento.getName());
	    nodeIndexes.clear();
	    for (c = 0; c < nodeList.size(); c++) {
	        auxNo = nodeList.get(c);
	        auxNo.getParents().remove(elemento);
	        auxNo.getChildren().remove(elemento);
	        nodeIndexes.put(auxNo.getName(), new Integer(c));
	    }
	    if (!edgeList.isEmpty()) {
	        auxArco = edgeList.get(0);
	        c = 0;
	        while (auxArco != edgeList.get(edgeList.size() - 1)) {
	            if ((auxArco.getOriginNode() == elemento) || (auxArco.getDestinationNode() == elemento)) {
	                removeArco(auxArco);
	            }
	            else {
	                c++;
	            }
	            auxArco = edgeList.get(c);
	        }
	        if ((auxArco.getOriginNode() == elemento) || (auxArco.getDestinationNode() == elemento)) {
	            removeArco(auxArco);
	        }
	    }
	    
	    
	}

	/**
	 *  Remove arco do grafo.
	 *
	 *@param  elemento  arco a ser removido
	 */
	private void removeArco(Edge elemento) {
	    Node auxNo;
	    ITabledVariable auxVTab;
	    PotentialTable auxTP;
	
	    edgeList.remove(elemento);
	
	    auxNo = elemento.getDestinationNode();
	    if (auxNo instanceof ITabledVariable) {
	        auxVTab = (ITabledVariable)auxNo;
	        auxTP = auxVTab.getPotentialTable();
	        auxTP.removeVariable(elemento.getOriginNode());
	    }
	}

	/**
	 *  Limpa a lista de n�s.
	 */
	protected void clearNodes() {
	    nodeList.clear();
	}

	/**
	 *  Limpa a lista de edgeList.
	 */
	protected void clearEdges() {
	    edgeList.clear();
	}

	/**
	 *  Verifica exist�ncia de determinado arco.
	 *
	 *@param  no1  n� origem.
	 *@param  no2  n� destino.
	 *@return      posi��o do arco no vetor ou -1 caso n�o exista tal arco.
	 */
	public int hasEdge(Node no1, Node no2) {
		return hasEdge(no1, no2, edgeList);
	}

	protected int hasEdge(Node no1, Node no2, List<Edge> vetArcos) {
		if (no1 == no2) {
			return 1;
		}
	
		int sizeArcos = vetArcos.size();
		Edge auxA;
		for (int i = 0; i < sizeArcos; i++) {
			auxA = (Edge) vetArcos.get(i);
			if ((auxA.getOriginNode() == no1)
				&& (auxA.getDestinationNode() == no2)
				|| (auxA.getOriginNode() == no2)
				&& (auxA.getDestinationNode() == no1)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Seta o nome da rede.
	 *
	 * @param name nome da rede.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retorna o nome da rede.
	 *
	 * @return nome da rede.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the id.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return ((name != null) ? name : "") + '(' + id + ')';
	}

}