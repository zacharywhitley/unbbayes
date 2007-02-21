package unbbayes.io.mebn.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.io.mebn package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: UnB</p>
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0
 * @since 11/10/2006
 */

public class IoMebnResources extends ListResourceBundle {

    /**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/*TODO Traduzir */
	
	/**
	 * The resources
	 */
	static final Object[][] contents =
	{		
		{"MTheoryNotExist", "Nao existe nenhuma MTheory definida no arquivo!"}, 
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
	};
}
