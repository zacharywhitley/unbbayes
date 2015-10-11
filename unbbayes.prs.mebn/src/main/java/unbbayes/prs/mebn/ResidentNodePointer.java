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
package unbbayes.prs.mebn;

import java.util.Vector;

import unbbayes.prs.Node;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;

/** 
 * Esta classe funciona como um ponteiro para um n� residente, sendo
 * os argumentos alterados para outro conjunto de v�riaveis ordin�rias. 
 * 
 * @author Laecio Lima dos Santos
 *
 */

public class ResidentNodePointer {
	
	private ResidentNode residentNode; 

	/*
	 * O vetor de vari�veis ordin�rias deve possuir 
	 * uma correspond�ncia com a lista de vari�veis ordin�rias do n� 
	 * residente do qual ela � input. A correspondencia deve ser da 
	 * seguinte forma:
	 * -> Cada elemento com indice i do vetor do n� de input deve ser 
	 * do mesmo tipo que o elemento de indice i do vetor do n� residente
	 * -> O vetor do n� de input tem o mesmo tamanho da lita do n� residente. 
	 * 
	 * Para tal o vetor <typesOfOrdinaryVariableList> lista os tipos da lista
	 * de OV do n� residente, enquanto os elementos do vetor <ordinaryVariableList>
	 * devem sempre corresponder ao tipo do outro vetor.  
	 */
	
	private OrdinaryVariable ordinaryVariableList[]; 	
	
	private Type typesOfOrdinaryVariableList[]; 
	
	private final int numberArguments; 
	
	private Node node; 
	
	/**
	 * 
	 * Nota: o numero de argumentos do no residente bem como o tipo
	 * de cada argumento indice i n�o pode ser alterado. Caso o seja, 
	 * outro objeto dever� ser criado para estar de acordo com a nova
	 * configura��o. 
	 * @param _residentNode
	 * 
	 */
	
	public ResidentNodePointer(ResidentNode _residentNode, Node _node){
		residentNode = _residentNode; 
		node = _node; 
		residentNode.addResidentNodePointer(this); 
		
		numberArguments = residentNode.getOrdinaryVariableList().size(); 
		typesOfOrdinaryVariableList = new Type[numberArguments]; 
	    ordinaryVariableList = new OrdinaryVariable[numberArguments]; 
	    
		buildTypeOfOVList(); 
	}
	
	public void delete(){
		residentNode.removeResidentNodePointer(this); 
	}
	
	/*
	 * Cria uma lista de variaveis ordinarias do mesmo tamanho que a lista
	 * presente no n� do qual este n� input � instancia. 
	 * Atualiza a lista de tipos para permitir a checagem das entidades
	 * que o usuario utilizar para preencher os argumentos. 
	 */
	
	private void buildTypeOfOVList(){
		
		        int index = 0; 
			    for (OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
				   Type type = ov.getValueType(); 
			       typesOfOrdinaryVariableList[index] = type; 
				   type.addUserObject(this); 
			       
				   index++; 
			    }
		
	}
	
	/**
	 * Add an ordinary variable in the list of arguments of this input node.  
	 * 
	 * @throws OVDontIsOfTypeExpected The type isn't equal to the type of argument expected in the
	 *                                node father of this input node. 
	 */
	
	public void addOrdinaryVariable(OrdinaryVariable ov, int index)  throws OVDontIsOfTypeExpected{
		
		// FIXME: This if should also verify if the OV is a subclass of the expected class.
		// For now I will remove this condition to verify if the rest works as expected.
//		if(!typesOfOrdinaryVariableList[index].equals(ov.getValueType())){
//			throw new OVDontIsOfTypeExpected(typesOfOrdinaryVariableList[index].toString()); 
//		}
//		else{
			ordinaryVariableList[index] = ov; 
			ov.addIsArgumentOfList(this); 
//		}
		
	}
	
	/**
	 * Remove the ordinary variable present in the index choice. The
	 * position of this ordinary variable will be fill with null
	 * 
	 * @param index
	 */
	
	public void removeOrdinaryVariable(int index){
		
		if(ordinaryVariableList[index] != null){
			ordinaryVariableList[index].removeIsArgumentOfList(this); 
		}
		
		ordinaryVariableList[index] = null; 
		
	}	
	
	/**
	 * Return the index of a ordinary variable
	 * @param ov
	 * @return indice or -1 if the ov don't is an argument. 
	 */
	public int getOrdinaryVariableIndex(OrdinaryVariable ov){
		
		int result = -1; 
		
		for(int i= 0; i < ordinaryVariableList.length; i++){
			if(ordinaryVariableList[i].equals(ov)){
				result = i;
				break; 
			}
		}
		
		return result; 
		
	}
	
	/**
	 * Return the ordinary variable at the resident node that corresponds to 
	 * the ordinary variable at the input node
	 * 
	 * @param inputNodeOV
	 */
	public OrdinaryVariable getCorrespondentOrdinaryVariable(OrdinaryVariable inputNodeOV){
		
		int index; 
		
		index = getOrdinaryVariableIndex(inputNodeOV); 
		
		return this.residentNode.getOrdinaryVariableByIndex(index); 
		
	}
	
	/**
	 * Remove from the list of arguments all the references
	 * for the ordinary variable.
	 */
	
	public void removeOrdinaryVariable(OrdinaryVariable ov){
		
		for(int i = 0; i < ordinaryVariableList.length; i++){
			if(ordinaryVariableList[i] != null){
				if(ordinaryVariableList[i].equals(ov)){
					ordinaryVariableList[i] = null; 
				}
			}
		}
		
		if(node instanceof InputNode){
			((InputNode)node).updateLabel(); 
		}else{
			if(node instanceof ContextNode){
				((ContextNode)node).updateLabel(); 
			}
		}
		
	}
	
	public ResidentNode getResidentNode(){
		return residentNode; 
	}

	public int getNumberArguments() {
		return numberArguments;
	}

	public Vector<OrdinaryVariable> getOrdinaryVariableList() {
		Vector<OrdinaryVariable> vetor = new Vector<OrdinaryVariable>(); 
		
		for(int i= 0; i < ordinaryVariableList.length; i++){
			vetor.add(i, ordinaryVariableList[i]); 
		}
		
		return vetor;
	}
	
	public OrdinaryVariable[] getOrdinaryVariableArray(){
		
		return this.ordinaryVariableList; 
		
	}

	public Type getTypeOfArgument(int index){
		return typesOfOrdinaryVariableList[index]; 
	}
	
	public OrdinaryVariable getArgument(int index){
		return ordinaryVariableList[index]; 
	}

	public Vector<Type> getTypesOfOrdinaryVariableList() {
        
		Vector<Type> vetor = new Vector<Type>(); 
		
		for(int i= 0; i < typesOfOrdinaryVariableList.length; i++){
			vetor.add(i, typesOfOrdinaryVariableList[i]); 
		}
		
		return vetor;
	}

	public void setResidentNode(ResidentNode residentNode) {
		this.residentNode = residentNode;
	}
    	
}
