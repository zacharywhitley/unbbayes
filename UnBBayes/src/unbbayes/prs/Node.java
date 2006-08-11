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

package unbbayes.prs;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawElement;
import unbbayes.gui.draw.DrawText;
import unbbayes.gui.draw.IDrawable;
import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.util.ArrayMap;
import unbbayes.util.NodeList;
import unbbayes.util.SerializablePoint2D;

/**
 *  Classe que representa um nó genérico.
 *
 *@author     Michael e Rommel
 */
public abstract class Node implements Serializable, IDrawable {

	private String description;
	protected String name;
	protected SerializablePoint2D position;
	protected static SerializablePoint2D size = new SerializablePoint2D();
	protected NodeList parents;
	private NodeList children;
	protected List<String> states;
	private NodeList adjacents;
	private boolean bSelected;
	private String explanationDescription;
	private ArrayMap<String,ExplanationPhrase> phrasesMap;
	private int informationType;

	public static final int PROBABILISTIC_NODE_TYPE = 0;
	public static final int UTILITY_NODE_TYPE = 1;
	public static final int DECISION_NODE_TYPE = 2;

	public static final int DESCRIPTION_TYPE = 3;
	public static final int EXPLANATION_TYPE = 4;
	
	protected DrawElement drawElement;

	/**
	 *  Constrói um novo nó e faz as devidas inicializações.
	 */
	public Node() {
		name = "";
		description = "";
		explanationDescription = "";
		adjacents = new NodeList();
		parents = new NodeList();
		children = new NodeList();
		states = new ArrayList<String>();
		// width
		size.x = 35;
		// height
		size.y = 35;
		position = new SerializablePoint2D();
		bSelected = false;
		drawElement = new DrawText(name, position);
		drawElement.setFillColor(Color.black);
		phrasesMap = new ArrayMap<String,ExplanationPhrase>();
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
		ExplanationPhrase ep = phrasesMap.get(node);
		if (ep == null) {
			throw new Exception("Nó não encontrado.");
		} else {
			return (ExplanationPhrase) ep;
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
	 *  Set the node's name.
	 *
	 *@param  name Node's name.
	 */
	public void setName(String name) {
		this.name = name;
		// It is necessary to update the name to be drawn by the DrawText class.
		((DrawText)drawElement).setText(name);
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
	 *  Modifica a position do nó.
	 *
	 *@param  x  Posição x do nó.
	 *@param  y  Posição y do nó.
	 */
	public void setPosition(double x, double y) {
		position.setLocation(x, y);
	}

	/**
	 *  Set the node's width.
	 *
	 *@param  width  Node's width.
	 */
	public static void setWidth(int width) {
		size.x = width;
	}

	/**
	 *  Set the node's height.
	 *
	 *@param  heigth  The node's height.
	 */
	public static void setHeight(int height) {
		size.y = height;
	}

	/**
	 *  Modifica o status de seleção do nó.
	 *
	 *@param  b  Status de seleção.
	 */
	public void setSelected(boolean b) {
		bSelected = b;
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
	public ArrayMap<String,ExplanationPhrase> setPhrasesMap(ArrayMap<String,ExplanationPhrase> phrasesMap) {
		ArrayMap<String,ExplanationPhrase> old = this.phrasesMap;
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
	public Point2D.Double getPosition() {
		return position;
	}

	/**
	 *  Get the node's width.
	 *
	 *@return Node's width.
	 */
	public static int getWidth() {
		return (int)size.x;
	}

	/**
	 *  Get the node's height.
	 *
	 *@return The node's height.
	 */
	public static int getHeight() {
		return (int)size.y;
	}

	/**
	 *  Retorna o status de seleção do nó.
	 *
	 *@return    Status de seleção do nó.
	 */

	public boolean isSelected() {
		return bSelected;
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
	public ArrayMap<String,ExplanationPhrase> getPhrasesMap() {
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
	public void makeAdjacents() {
		adjacents.addAll(parents);
		adjacents.addAll(children);
	}

	/**
	 *  Desmonta a lista de nós adjacentes.
	 */
	public void clearAdjacents() {
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
	public void setStates(List<String> states) {
		this.states = states;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		Node node = (Node) obj;
		return (node.name.equals(this.name));		
	}
	
	public void paint(Graphics2D graphics) {
		drawElement.paint(graphics);
	}

	public static SerializablePoint2D getSize() {
		return size;
	}

}
