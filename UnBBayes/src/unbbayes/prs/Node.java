/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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


import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ExplanationPhrase;
import unbbayes.prs.bn.ITabledVariable;
import unbbayes.util.*;

/**
 *  Classe que representa um n� gen�rico.
 *
 *@author     Michael e Rommel
 */
public abstract class Node implements java.io.Serializable {

	public class SerializablePoint2D
		extends Point2D.Double
		implements java.io.Serializable {
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;

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
	private boolean numerico=true;
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
	public int infoestados[];

	public static final int PROBABILISTIC_NODE_TYPE = 0;
	public static final int UTILITY_NODE_TYPE = 1;
	public static final int DECISION_NODE_TYPE = 2;

	public static final int DESCRIPTION_TYPE = 3;
	public static final int EXPLANATION_TYPE = 4;

	/**
	 *  Constr�i um novo n� e faz as devidas inicializa��es.
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

	/** Retorna o tipo de informa��o do n�.
	 *  @return Tipo de informa��o do n�.
	 */
	public int getInformationType() {
		return informationType;
	}

	/** Altera o tipo de informa��o do n�.
	 *  Os tipos de informa��o podem ser:
	 *  -   DESCRIPTION_TYPE : n� de descri��o
	 *  -   EXPLANATION_TYPE : n� de explica��o
	 *  @param informationType Tipo de informa��o
	 *  @throws Exception se o tipo de informa��o for inv�lida
	 */
	public void setInformationType(int informationType) /*throws Exception*/ {
		if ((informationType > 2) && (informationType < 5))
			this.informationType = informationType;
		/*else
		{   throw new Exception("Valor de infroma��o inv�lido");
		}*/
	}

	public void addExplanationPhrase(ExplanationPhrase explanationPhrase) {
		phrasesMap.put(explanationPhrase.getNode(), explanationPhrase);
	}

	public ExplanationPhrase getExplanationPhrase(String node)
		throws Exception {
		Object obj = phrasesMap.get(node);
		if (obj == null) {
			throw new Exception("N� n�o encontrado.");
		} else {
			return (ExplanationPhrase) obj;
		}
	}

	/**
	 *  Modifica o nome do n�.
	 *
	 *@param  texto  descri��o do n�.
	 */
	public void setDescription(String texto) {
		this.description = texto;
	}

	/**
	 *  Modifica a sigla do n�.
	 *
	 *@param  sigla sigla do n�.
	 */
	public void setName(String sigla) {
		this.name = sigla;
	}

	/**
	 *  Insere nova lista de filhos.
	 *
	 *@param  filhos  List de n�s que representam os filhos.
	 */
	public void setChildren(NodeList filhos) {
		this.children = filhos;
	}
	public void AddChild(Node filho){
		this.children.add(filho);
	}
	public void AddParent(Node parent){
		this.parents.add(parent);
	}
	public boolean isParentOf(Node child){
		//boolean result=children.contains(child);
		boolean result=false;
		int j=children.size();
		try{
		for(int i=0;i<j;i++){
			result=((result)||((child.getName())==(children.get(i).getName())));
		}
		}
		catch (Exception ee){
			int debug=0;
			int debug2=debug;
		}
		return result;
		}
	public boolean isChildOf(Node parent){
		//boolean result=parents.contains(parent);
		boolean result=false;
		int j=parents.size();
		for(int i=0;i<j;i++){
			result=((result)||((parent.getName())==(parents.get(i).getName())));
		}
		return result;
	}

	/**
	 *  Insere nova lista de pais.
	 *
	 *@param  pais  List de n�s que representam os pais.
	 */
	public void setParents(NodeList pais) {
		this.parents = pais;
	}	

	/**
	 *  Modifica a posicao do n�.
	 *
	 *@param  x  Posi��o x do n�.
	 *@param  y  Posi��o y do n�.
	 */
	public void setPosition(double x, double y) {
		posicao.setLocation(x, y);
	}

	/**
	 *  Modifica a largura do n�.
	 *
	 *@param  l  Nova largura do n�.
	 */
	public static void setWidth(int l) {
		largura = l;
	}

	/**
	 *  Modifica a altura do n�.
	 *
	 *@param  a  A nova altura do n�.
	 */
	public static void setHeight(int a) {
		altura = a;
	}

	/**
	 *  Modifica o status de sele��o do n�.
	 *
	 *@param  b  Status de sele��o.
	 */
	public void setSelected(boolean b) {
		selecionado = b;
	}

	/**
	 *  Modifica a descri��o da explana��o do n�.
	 *
	 *@param  texto  descri��o da explana��o do n�.
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
	 *  Retorna o nome do n�.
	 *
	 *@return    descri��o do n�.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *  Retorna a lista de adjacentes.
	 *
	 *@return    Refer�ncia para os adjacentes do n�.
	 */
	public NodeList getAdjacents() {
		return adjacents;
	}

	/**
	 *  Retorna a sigla do n�.
	 *
	 *@return    Sigla do n�.
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
	public final void removeChildren() {
		children.clear();
	}
	public final void removeChild(int child) {
		children.remove2(child);
	}

	/**
	 *  Retorna a lista de pais.
	 *
	 *@return    Lista de Pais.
	 */
	public final NodeList getParents() {
		return parents;
	}
	public final void removeParents() {
		parents.clear();
	}
	public final void removeParent(int parent) {
		parents.remove2(parent);
	}

	/**
	 *  Retorna a posi��o gr�fica do n�.
	 *
	 *@return    Posi��o do n�.
	 */
	public Point2D.Double getPosition() {
		return posicao;
	}

	/**
	 *  Retorna a largura do n�.
	 *
	 *@return    largura do n�.
	 */
	public static int getWidth() {
		return largura;
	}

	/**
	 *  Retorna a altura do n�.
	 *
	 *@return    Altura do n�.
	 */
	public static int getHeight() {
		return altura;
	}

	/**
	 *  Retorna o status de sele��o do n�.
	 *
	 *@return    Status de sele��o do n�.
	 */

	public boolean isSelected() {
		return selecionado;
	}
	/**
	 *  Retorna a descri��o de explana��o do n�.
	 *
	 *@return    descri��o de explana��o do n�.
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

	public void atualizatamanhoinfoestados(){
		int i=states.size();
		infoestados=new int[i];
		for(int j=0;j<i;j++)infoestados[j]=0;
		}

	
	/**
	 *  Insere um estado com o nome especificado no final da lista.
	 *
	 *@param  estado  Nome do estado a ser inserido.
	 */
	public void appendState(String estado) {
		updateTables();
		states.add(estado);
		this.atualizatamanhoinfoestados();
		}
	
	 public boolean existeEstado(String nomeEstado){
	        int tamanho = states.size();
	        for(int tamanhoEstado = 0; tamanhoEstado < tamanho; tamanhoEstado++){
	            if(states.get(tamanhoEstado).equals(nomeEstado)){
	                return true;
	            }
	        }
	        return false;
	    }
	 
	 public int addEstado(String estado){
		 int posf=states.size();
		 if(this.getNumerico()){
	    	int tamanho = states.size();
	        List states2=new ArrayList();
	        int i;
	        posf=0;
	        String b1,b2=estado;
	        //if(tamanho==1)posf
	for(i = 0; i < tamanho; i++){
		b1=(String)states.get(i);
	if(Double.parseDouble(b1)<Double.parseDouble(b2)){
	                posf++;
	            }
	            
	        }
	        for(i=0;i<posf;i++){
	        	states2.add(states.get(i));	
	        }
	        states2.add(estado);
	        for(i=posf;i<tamanho;i++){
	        	states2.add(states.get(i));	
	        }
	        states=states2;        
	        }		 
	        return posf;
	        
	}
	 
	
	
	/**
	 *  Retira o estado criado mais recentemente.
	 *  Isto �, o �ltimo estado da lista.
	 */
	public void removeLastState() {
		if (states.size() > 1) {
			updateTables();
			states.remove(states.size() - 1);
		}
		this.atualizatamanhoinfoestados();
	}
	public void removestate(int num){
		states.remove(num);
		this.atualizatamanhoinfoestados();
	}

	/**
	 *  Substitui o estado da posi��o especificada pelo estado especificado.
	 *
	 *@param  estado  Nome do estado atualizado.
	 *@param  index   �ndice em que deseja-se modificar, come�ando do 0.
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
	 *  Retorna o n�mero de estados do n�.
	 *
	 *@return    Retorna o n�mero de estados do n�.
	 */
	public final int getStatesSize() {
		return states.size();
	}

	/**
	 *  Retorna o estado da posi��o <code>index</code>
	 *
	 *@param  index  �ndice do estado a ser lido.
	 *@return        Nome do estado da posi��o <code>index</code>
	 */
	public final String getStateAt(int index) {
		return (String) (states.get(index));
	}

	/**
	 *  Imprime a descri��o do n� no formato: "descri��o (sigla)" (sem aspas)
	     *  � utilizado no JTree da Interface quando a rede � compilada.
	 *
	 *@return    descri��o do n� formatado.
	 */
	public String toString() {
		return description + " (" + name + ")";
	}

	/**
	 *  Monta lista de n�s adjacentes.
	 */
	public void makeAdjacents() {
		adjacents.addAll(parents);
		adjacents.addAll(children);
	}

	/**
	 *  Desmonta a lista de n�s adjacentes.
	 */
	public void clearAdjacents() {
		adjacents.clear();
	}


	/**
	 * Utilizado para notificar as tabelas de que esta vari�vel faz parte de que houve uma
	 * modifica��o na estrutura desta vari�vel.
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
	public void setNumerico(boolean sn){
		numerico=sn;
	}
	public boolean getNumerico(){
		return numerico;
	}

}
