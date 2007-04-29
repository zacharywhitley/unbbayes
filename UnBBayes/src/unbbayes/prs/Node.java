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
import unbbayes.gui.draw.IOnePositionDrawable;
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
public abstract class Node implements Serializable, IOnePositionDrawable {

	private String description;
	protected String name;
	protected String label; 
	private boolean nameIsLabel = true; 
	
	protected SerializablePoint2D position;
	protected static SerializablePoint2D size = new SerializablePoint2D();
	
	protected SerializablePoint2D sizeVariable = new SerializablePoint2D(); 
	protected boolean sizeIsVariable = false;  
	
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
	 * Holds the mean of the values for each class if this is a numeric
	 * attribute node
	 */
	protected double[] mean;

	/**
	 * Holds the standard deviation of the values for each class if this is a
	 * numeric attribute node.
	 */		
	protected double[] standardDeviation;

	/**
	 *  Constrói um novo nó e faz as devidas inicializações.
	 */
	public Node() {
		name = "";
		label= ""; //o texto dentro do nó
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
		if(nameIsLabel == true){
		   ((DrawText)drawElement).setText(name);
		}
	}

	/**
	 *  Set the node's label (text of the node).
	 *
	 *@param  label Node's label.
	 */
	public void setLabel(String label) {
		this.label = label;
		nameIsLabel = false; 
		((DrawText)drawElement).setText(label);
	}	
	
	/**
	 *  Return the node's label (text of the node).
	 *
	 */
	public String getLabel() {
		return label; 
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
	 *  Imprime a descrição do nó no formato: "descrição (sigla)" (sem aspas)
	 *  É utilizado no JTree da Interface quando a rede é compilada.
	 *
	 *@return    descrição do nó formatado.
	 */
	public String toString() {
		return description + " (" + name + ")";
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		
		if (obj == this) {
			return true;
		}
		
		if(obj != null){
		   Node node = (Node) obj;
		   return (node.name.equals(this.name));
		}
		
		return false; //obj == null && this != null 
		
	}
	
	public void paint(Graphics2D graphics) {
		drawElement.paint(graphics);
	}
	
	public boolean isPointInDrawableArea(int x, int y) {
		double x1 = position.x;
        double y1 = position.y;
        double width = size.x/2;
        double height = size.y/2;

        if ((x >= x1 - width) && (x <= x1 + width) && (y >= y1 - height) && (y <= y1 + height)) {
            return true;
        }
        
		return false;
	}

	public boolean isSelected() {
		return bSelected;
	}

	public void setSelected(boolean b) {
		bSelected = b;
	}
	
	public Point2D.Double getPosition() {
		return position;
	}
	
	public void setPosition(double x, double y) {
		position.setLocation(x, y);
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
	 * Returns the node's size (x,y) where x = width and y = height. 
	 * @return The node's size.
	 */
	public static Point2D.Double getSize() {
      
	      return size;
	      

	}
	
	/**
	 *  Set the node's size.
	 *
	 *@param  width  The node's width.
	 *@param  height The node's height.
	 */
	public static void setSize(double width, double height) {
		size.setLocation(width, height);
	}
	
	public void setSizeVariable(double width, double height){
		sizeVariable.setLocation(width, height); 
	}
	
	public void setSizeIsVariable(boolean is){
	   sizeIsVariable = is; 	
	}
	
	/**
	 * Set the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public void setMean(double[] mean) {
		this.mean = mean;
	}
	
	/**
	 * Set the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public void setStandardDeviation(double[] standardDeviation) {
		this.standardDeviation = standardDeviation;
	}
	
	/**
	 * Get the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public double[] getMean() {
		return mean;
	}
	
	/**
	 * Get the mean of the values if this is a numeric attribute node.
	 * 
	 * @param mean
	 */
	public double[] getStandardDeviation() {
		return standardDeviation;
	}
	
}
