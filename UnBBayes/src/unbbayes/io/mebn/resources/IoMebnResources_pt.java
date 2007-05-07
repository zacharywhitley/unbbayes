package unbbayes.io.mebn.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.io.mebn Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: UnB</p>
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0
 * @since 11/10/2006
 */

public class IoMebnResources_pt extends ListResourceBundle {

    /**
	 *  Sobrescreve getContents e retorna um array, onde cada item no array é
	 *	um par de objetos. O primeiro elemento do par é uma String chave, e o
	 *	segundo é o valor associado a essa chave.
	 *
	 * @return O conteúdo dos recursos
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * resources
	 */
	static final Object[][] contents =
	{	{"MTheoryNotExist", "Nao existe nenhuma MTheory definida no arquivo!"}, 
		{"ModelCreationError", "Nao foi possivel ler corretamente o arquivo Pr-Owl!"}, 
		{"DomainMFragNotExistsInMTheory", "MFrag nao existe na MTheory!"}, 
		{"ContextNodeNotExistsInMTheory", "Context Node nao existe na MTheory!"}, 
		{"GenerativeInputNodeNotExistsInMTheory", "Generative Input Node nao existe na MTheory!"}, 		
		{"BuiltInRVNotExistsInMTheory", "Built-In RV nao existe na MTheory"}, 		
		{"OVariableNotExistsInMTheory", "Ordinary Variable nao existe na MTheory"}, 		
		{"NodeNotFound", "Node nao encontrado!"}, 		
		{"ArgumentNotFound", "Argumento nao encontrado!"}, 		
		{"isOVariableInError", "Propriedade isOVariableIn nao confere!"}, 			
		{"isArgumentOfError", "Propriedade isArgumentOf nao confere!"}, 					
		{"ArgumentTermInError", "Conflito na propriedade Argument Term!"}, 			
		{"ContextNodeNotExistsInMFrag", "ContextNode nao existe na MFrag!"}, 
		{"DomainResidentNotExistsInMTheory", "Nodo de dominio residente nao existe na MTheory!"}, 
		{"DomainResidentNotExistsInDomainMFrag", "Nodo domain Resident não existe na DomainMFrag"}, 
		{"GenerativeInputNodeNotExistsInDomainMFrag", "Nodo generative Input nao existe na MTheory"}, 		
        {"CategoricalStateNotFoundException", "Estado não encontrado"}, 
		{"FileNotFoundException","Não foi possível abrir o arquivo!"},	  
		{"ErrorReadingFile", "Ocorreu erro ao tentar ler o arquivo"}, 
		{"BuiltInDontImplemented", "Built-In não é implementada no UnBBayes"}
	};
}
