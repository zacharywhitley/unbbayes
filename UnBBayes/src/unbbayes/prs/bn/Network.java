package unbbayes.prs.bn;

import java.util.List;
import java.util.Map;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.util.NodeList;

public class Network {

	protected String id;
	protected String name;
	/**
	 *  Lista de nós que compõem o grafo.
	 */
	protected NodeList nodeList;
	/**
	 *  Lista de edgeList que compõem o grafo.
	 */
	protected List<Edge> edgeList;
	protected Map<String,Integer> nodeIndexes;

	public Network() {
		super();
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
	 *  Retorna os nós do grafo.
	 *
	 *@return    nós do grafo.
	 * 
	 * @todo Eliminar esse metodo! eh utilizado na classe NetWindow
	 */
	public NodeList getNodes() {
	    return this.nodeList;
	}

	/**
	 *  Returna o número de variáveis da rede.
	 *
	 *@return    número de variáveis da rede.
	 */
	public int getNodeCount() {
		return nodeList.size();
	}

	/**
	 *  Retorna o nó do grafo com o respectivo índice.
	 *
	 *@param  index  índice do nó.
	 *@return	nó com respectivo índice no List.
	 */
	public Node getNodeAt(int index) {
	    return nodeList.get(index);
	}

	/**
	 *  Retorna o nó do grafo com a respectiva sigla.
	 *
	 *@param  name  nome do nó.
	 *@return       nó com a respectiva sigla.
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
	 *  Adiciona novo nó ao grafo.
	 *
	 *@param  no  nó a ser inserido.
	 */
	public void addNode(Node no) {
	    nodeList.add(no);
	    nodeIndexes.put(no.getName(), new Integer(nodeList.size()-1));
	}

	/**
	 *  Adiciona o arco à rede.
	 *
	 *@param  arco  arco a ser inserido.
	 */
	public void addEdge(Edge arco) {
	    arco.getOriginNode().getChildren().add(arco.getDestinationNode());
	    arco.getDestinationNode().getParents().add(arco.getOriginNode());
	    edgeList.add(arco);
	    if (arco.getDestinationNode() instanceof ITabledVariable) {
			ITabledVariable v2 = (ITabledVariable) arco.getDestinationNode();
			PotentialTable auxTab = v2.getPotentialTable();
			auxTab.addVariable(arco.getOriginNode());
		}
	}

	/**
	 *  Remove nó do grafo.
	 *
	 *@param  elemento  no a ser removido.
	 */
	public void removeNode(Node elemento) {
	    int c;
	    Node auxNo;
	    Edge auxArco;
	    
	    nodeList.remove(elemento);
	    
	    nodeIndexes.remove(elemento.getName());
	    for (c = 0; c < nodeList.size(); c++) {
	        auxNo = nodeList.get(c);
	        auxNo.getParents().remove(elemento);
	        auxNo.getChildren().remove(elemento);
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
	 *  Limpa a lista de nós.
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
	 *  Verifica existência de determinado arco.
	 *
	 *@param  no1  nó origem.
	 *@param  no2  nó destino.
	 *@return      posição do arco no vetor ou -1 caso não exista tal arco.
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