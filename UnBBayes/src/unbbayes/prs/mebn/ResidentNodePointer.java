package unbbayes.prs.mebn;

import java.util.Vector;

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
		
		numberArguments = residentNode.getOrdinaryVariableList().size(); 
		typesOfOrdinaryVariableList = new Type[numberArguments]; 
	    ordinaryVariableList = new OrdinaryVariable[numberArguments]; 
	    
		buildTypeOfOVList(); 
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
	 * Add a ordinary variable in the list of arguments of this input node.  
	 * 
	 * @throws OVDontIsOfTypeExpected The type don't is equal to the type of argument expected in the
	 *                                node father of this input node. 
	 */
	
	public void addOrdinaryVariable(OrdinaryVariable ov, int index)  throws OVDontIsOfTypeExpected{
		
		if(typesOfOrdinaryVariableList[index].equals(ov.getValueType())){
			throw new OVDontIsOfTypeExpected(typesOfOrdinaryVariableList[index].toString()); 
		}
		else{
			ordinaryVariableList[index] = ov; 
		}
		
	}
	
	/**
	 * Remove the ordinary variable present in the index choice. The
	 * position of this ordinary variable will be fill with null
	 * 
	 * @param index
	 */
	
	public void removeOrdinaryVariable(int index){
		
		ordinaryVariableList[index] = null; 
	
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

	/*
	public void setOrdinaryVariableList(
			Vector<OrdinaryVariable> ordinaryVariableList) {
		this.ordinaryVariableList = ordinaryVariableList;
	}*/

	public Type getTypeOfArgument(int index){
		return typesOfOrdinaryVariableList[index]; 
	}

	public Vector<Type> getTypesOfOrdinaryVariableList() {
        
		Vector<Type> vetor = new Vector<Type>(); 
		
		for(int i= 0; i < typesOfOrdinaryVariableList.length; i++){
			vetor.add(i, typesOfOrdinaryVariableList[i]); 
		}
		
		return vetor;
	}
/*
	public void setTypesOfOrdinaryVariableList(
			Vector<String> typesOfOrdinaryVariableList) {
		this.typesOfOrdinaryVariableList = typesOfOrdinaryVariableList;
	}
*/
	public void setResidentNode(ResidentNode residentNode) {
		this.residentNode = residentNode;
	}
    	
}
