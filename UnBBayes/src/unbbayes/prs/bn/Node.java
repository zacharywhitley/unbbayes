/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.prs.bn;

import java.awt.Color;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import unbbayes.util.*;

/**
 *  Classe que representa um nó genérico.
 *
 *@author     Michael e Rommel
 */
public abstract class Node implements java.io.Serializable {

	public class SerializablePoint2D
		extends Point2D.Double
		implements java.io.Serializable {
		private void writeObject(java.io.ObjectOutputStream out)
			throws java.io.IOException {
			out.writeDouble(x);
			out.writeDouble(y);
		}

		private void readObject(java.io.ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException {
			x = in.readDouble();
			y = in.readDouble();
		}
	}

	private String description = "";
	protected String name;
	private SerializablePoint2D posicao;
	protected NodeList parents;
	private NodeList children;
	protected List states;
	private NodeList adjacents;
	private boolean selecionado;
	private static int altura;
	private static int largura;
	private String explanationDescription = "";
	private ArrayMap phrasesMap = new ArrayMap();
	private int informationType;

	public static final int PROBABILISTIC_NODE_TYPE = 0;
	public static final int UTILITY_NODE_TYPE = 1;
	public static final int DECISION_NODE_TYPE = 2;

	public static final int DESCRIPTION_TYPE = 3;
	public static final int EXPLANATION_TYPE = 4;

	/**
	 *  Constrói um novo nó e faz as devidas inicializações.
	 */
	public Node() {
		adjacents = new NodeList();
		parents = new NodeList();
		children = new NodeList();
		states = new ArrayList();
		altura = 35;
		largura = 35;
		posicao = new SerializablePoint2D();
		selecionado = false;
		informationType = DESCRIPTION_TYPE;
	}

	public abstract int getType();

	/** Retorna o tipo de informação do nó.
	 *  @return Tipo de informação do nó.
	 */
	public int getInformationType() {
		return informationType;
	}

	/** Altera o tipo de informação do nó.
	 *  Os tipos de informação podem ser:
	 *  -   DESCRIPTION_TYPE : nó de descrição
	 *  -   EXPLANATION_TYPE : nó de explicação
	 *  @param informationType Tipo de informação
	 *  @throws Exception se o tipo de informação for inválida
	 */
	public void setInformationType(int informationType) /*throws Exception*/ {
		if ((informationType > 2) && (informationType < 5))
			this.informationType = informationType;
		/*else
		{   throw new Exception("Valor de infromação inválido");
		}*/
	}

	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		phrasesMap.put(explanationPhrase.getNode(), explanationPhrase);
	}

	public ExplanationPhrase getExplanationPhrase(String node)
		throws Exception {
		Object obj = phrasesMap.get(node);
		if (obj == null) {
			throw new Exception("Nó não encontrado.");
		} else {
			return (ExplanationPhrase) obj;
		}
	}

	/**
	 *  Modifica o nome do nó.
	 *
	 *@param  texto  descrição do nó.
	 */
	public void setDescription(String texto) {
		this.description = texto;
	}

	/**
	 *  Modifica a sigla do nó.
	 *
	 *@param  sigla sigla do nó.
	 */
	public void setName(String sigla) {
		this.name = sigla;
	}

	/**
	 *  Insere nova lista de filhos.
	 *
	 *@param  filhos  List de nós que representam os filhos.
	 */
	public void setChildren(NodeList filhos) {
		this.children = filhos;
	}

	/**
	 *  Insere nova lista de pais.
	 *
	 *@param  pais  List de nós que representam os pais.
	 */
	public void setParents(NodeList pais) {
		this.parents = pais;
	}	

	/**
	 *  Modifica a posicao do nó.
	 *
	 *@param  x  Posição x do nó.
	 *@param  y  Posição y do nó.
	 */
	public void setPosicao(double x, double y) {
		posicao.setLocation(x, y);
	}

	/**
	 *  Modifica a largura do nó.
	 *
	 *@param  l  Nova largura do nó.
	 */
	public static void setLargura(int l) {
		largura = l;
	}

	/**
	 *  Modifica a altura do nó.
	 *
	 *@param  a  A nova altura do nó.
	 */
	public static void setAltura(int a) {
		altura = a;
	}

	/**
	 *  Modifica o status de seleção do nó.
	 *
	 *@param  b  Status de seleção.
	 */
	public void setSelected(boolean b) {
		selecionado = b;
	}

	/**
	 *  Modifica a descrição da explanação do nó.
	 *
	 *@param  texto  descrição da explanação do nó.
	 */
	public void setExplanationDescription(String texto) {
		this.explanationDescription = texto;
	}

	/**
	 *  Modifica o ArrayMap com as frases.
	 *
	 *@param phrasesMap novo ArrayMap a ser setado
	 *@return	phrasesMap	anterior.
	 */
	public ArrayMap setPhrasesMap(ArrayMap phrasesMap) {
		ArrayMap old = this.phrasesMap;
		this.phrasesMap = phrasesMap;
		return old;
	}

	/**
	 *  Retorna o nome do nó.
	 *
	 *@return    descrição do nó.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *  Retorna a lista de adjacentes.
	 *
	 *@return    Referência para os adjacentes do nó.
	 */
	public NodeList getAdjacents() {
		return adjacents;
	}

	/**
	 *  Retorna a sigla do nó.
	 *
	 *@return    Sigla do nó.
	 */
	public String getName() {
		return name;
	}

	/**
	 *  Retorna a lista de filhos.
	 *
	 *@return    Lista de filhos.
	 */
	public final NodeList getChildren() {
		return children;
	}

	/**
	 *  Retorna a lista de pais.
	 *
	 *@return    Lista de Pais.
	 */
	public final NodeList getParents() {
		return parents;
	}

	/**
	 *  Retorna a posição gráfica do nó.
	 *
	 *@return    Posição do nó.
	 */
	public Point2D.Double getPosicao() {
		return posicao;
	}

	/**
	 *  Retorna a largura do nó.
	 *
	 *@return    largura do nó.
	 */
	public static int getLargura() {
		return largura;
	}

	/**
	 *  Retorna a altura do nó.
	 *
	 *@return    Altura do nó.
	 */
	public static int getAltura() {
		return altura;
	}

	/**
	 *  Retorna o status de seleção do nó.
	 *
	 *@return    Status de seleção do nó.
	 */

	public boolean isSelecionado() {
		return selecionado;
	}
	/**
	 *  Retorna a descrição de explanação do nó.
	 *
	 *@return    descrição de explanação do nó.
	 */
	public String getExplanationDescription() {
		return explanationDescription;
	}

	/**
	 *  Retorna o ArrayMap com as frases.
	 *
	 *@return    ArrayMap com as frases.
	 */
	public ArrayMap getPhrasesMap() {
		return this.phrasesMap;
	}

	/**
	 *  Insere um estado com o nome especificado no final da lista.
	 *
	 *@param  estado  Nome do estado a ser inserido.
	 */
	public void appendState(String estado) {
		updateTables();
		states.add(estado);
	}

	/**
	 *  Retira o estado criado mais recentemente.
	 *  Isto é, o último estado da lista.
	 */
	public void removeLastState() {
		if (states.size() > 1) {
			updateTables();
			states.remove(states.size() - 1);
		}
	}

	/**
	 *  Substitui o estado da posição especificada pelo estado especificado.
	 *
	 *@param  estado  Nome do estado atualizado.
	 *@param  index   Índice em que deseja-se modificar, começando do 0.
	 */
	public void setStateAt(String estado, int index) {
		states.set(index, estado);
	}

	/*
	public boolean existState(String state)
	{   int size = states.size();
	    for (int i=0; i<size; i++)
	    {   if (states.get(i).equals(state))
	            return true;
	    }
	    return false;
	}
	*/

	/**
	 *  Retorna o número de estados do nó.
	 *
	 *@return    Retorna o número de estados do nó.
	 */
	public final int getStatesSize() {
		return states.size();
	}

	/**
	 *  Retorna o estado da posição <code>index</code>
	 *
	 *@param  index  Índice do estado a ser lido.
	 *@return        Nome do estado da posição <code>index</code>
	 */
	public final String getStateAt(int index) {
		return (String) (states.get(index));
	}

	/**
	 *  Imprime a descrição do nó no formato: "descrição (sigla)" (sem aspas)
	     *  É utilizado no JTree da Interface quando a rede é compilada.
	 *
	 *@return    descrição do nó formatado.
	 */
	public String toString() {
		return description + " (" + name + ")";
	}

	/**
	 *  Monta lista de nós adjacentes.
	 */
	protected void montaAdjacentes() {
		adjacents.addAll(parents);
		adjacents.addAll(children);
	}

	/**
	 *  Desmonta a lista de nós adjacentes.
	 */
	protected void desmontaAdjacentes() {
		adjacents.clear();
	}


	/**
	 * Utilizado para notificar as tabelas de que esta variável faz parte de que houve uma
	 * modificação na estrutura desta variável.
	 */
	private void updateTables() {
		ITabledVariable aux;
		if (this instanceof ITabledVariable) {
			aux = (ITabledVariable) this;
			aux.getPotentialTable().variableModified();
		}

		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i) instanceof ITabledVariable) {
				aux = (ITabledVariable) children.get(i);
				aux.getPotentialTable().variableModified();
			}
		}
	}
	/**
	 * Sets the adjacents.
	 * @param adjacents The adjacents to set
	 */
	public void setAdjacents(NodeList adjacents) {
		this.adjacents = adjacents;
	}

	/**
	 * Sets the states.
	 * @param states The states to set
	 */
	public void setStates(List states) {
		this.states = states;
	}

}
