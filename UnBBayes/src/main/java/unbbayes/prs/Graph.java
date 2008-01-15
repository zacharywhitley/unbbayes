package unbbayes.prs;

import java.util.List;
import unbbayes.util.NodeList;

/** 
 * Interface for a graph building of Node's and Edge's
 */

public interface Graph {

		/**
		 *  Retorna os edgeList do grafo.
		 *
		 *@return    edgeList do grafo.
		 */
		public List<Edge> getEdges();

		/**
		 *  Retorna os nós do grafo.
		 *
		 *@return    nós do grafo.
		 * 
		 * @todo Eliminar esse metodo! eh utilizado na classe NetWindow
		 */
		public NodeList getNodes();

		/**
		 *  Returna o número de variáveis da rede.
		 *
		 *@return    número de variáveis da rede.
		 */
		public int getNodeCount();


		/**
		 *  Retira do grafo o arco especificado.
		 *
		 *@param  arco  arco a ser retirado.
		 */
		public void removeEdge(Edge arco) ;

		/**
		 *  Adiciona novo nó ao grafo.
		 *
		 *@param  no  nó a ser inserido.
		 */
		public void addNode(Node no);

		/**
		 *  Adiciona o arco à rede.
		 *
		 *@param  arco  arco a ser inserido.
		 */
		public void addEdge(Edge arco) throws Exception;

		/**
		 *  Remove nó do grafo.
		 *
		 *@param  elemento  no a ser removido.
		 */
		public void removeNode(Node elemento);

		/**
		 *  Verifica existência de determinado arco.
		 *
		 *@param  no1  nó origem.
		 *@param  no2  nó destino.
		 *@return      posição do arco no vetor ou -1 caso não exista tal arco.
		 */
		public int hasEdge(Node no1, Node no2);

}
