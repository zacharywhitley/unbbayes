package unbbayes.prs.mebn;

import java.util.Vector;

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
	
	private Vector<OrdinaryVariable> ordinaryVariableList; 	
	
	private Vector<String> typesOfOrdinaryVariableList; 
	
	private int numberArguments; 
	
	/**
	 * 
	 * Nota: o numero de argumentos do no residente bem como o tipo
	 * de cada argumento indice i n�o pode ser alterado. Caso o seja, 
	 * outro objeto dever� ser criado para estar de acordo com a nova
	 * configura��o. 
	 * @param _residentNode
	 * 
	 */
	
	public ResidentNodePointer(ResidentNode _residentNode){
		residentNode = _residentNode; 
		buildTypeOfOVList(); 
	}
	
	/*
	 * Cria uma lista de variaveis ordinarias do mesmo tamanho que a lista
	 * presente no n� do qual este n� input � instancia. 
	 * Atualiza a lista de tipos para permitir a checagem das entidades
	 * que o usuario utilizar para preencher os argumentos. 
	 */
	
	private void buildTypeOfOVList(){
		
		        numberArguments = residentNode.getOrdinaryVariableList().size(); 
				typesOfOrdinaryVariableList = new Vector<String>(numberArguments); 
			    ordinaryVariableList = new Vector<OrdinaryVariable>(numberArguments); 
			    
				int index = 0; 
			    for (OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
				   typesOfOrdinaryVariableList.add(index, ov.getValueType()); 
				   index++; 
			    }
		
	}
	
	/**
	 * Add a ordinary variable in the list of arguments of this input node.  
	 * 
	 * @throws OVDontIsOfTypeExpected The type don't is equal to the type of argument expected in the
	 *                                node father of this input node. 
	 */
	
	public void addOrdinaryVariable(OrdinaryVariable ov, int index)  throws OVDontIsOfTypeExpected{
		
		if(typesOfOrdinaryVariableList.get(index).compareTo(ov.getValueType()) != 0){
			throw new OVDontIsOfTypeExpected(typesOfOrdinaryVariableList.get(index)); 
		}
		else{
			ordinaryVariableList.add(index, ov); 
		}
		
	}
	
	/**
	 * Remove the ordinary variable present in the index choice. The
	 * position of this ordinary variable will be fill with null
	 * 
	 * @param index
	 */
	
	public void removeOrdinaryVariable(int index){
		
		ordinaryVariableList.remove(index);
		ordinaryVariableList.add(index, null); 
	
	}	
	
	public ResidentNode getResidentNode(){
		return residentNode; 
	}

	public int getNumberArguments() {
		return numberArguments;
	}

	public Vector<OrdinaryVariable> getOrdinaryVariableList() {
		return ordinaryVariableList;
	}

	public void setOrdinaryVariableList(
			Vector<OrdinaryVariable> ordinaryVariableList) {
		this.ordinaryVariableList = ordinaryVariableList;
	}

	public String getTypeOfArgument(int index){
		return typesOfOrdinaryVariableList.get(index); 
	}

	public Vector<String> getTypesOfOrdinaryVariableList() {
		return typesOfOrdinaryVariableList;
	}

	public void setTypesOfOrdinaryVariableList(
			Vector<String> typesOfOrdinaryVariableList) {
		this.typesOfOrdinaryVariableList = typesOfOrdinaryVariableList;
	}

	public void setResidentNode(ResidentNode residentNode) {
		this.residentNode = residentNode;
	}
    	
}
