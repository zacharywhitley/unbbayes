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
package unbbayes.io.mebn.resources;

import java.util.ListResourceBundle;

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
		{"DomainResidentNotExistsInDomainMFrag", "Nodo domain Resident n�o existe na DomainMFrag"}, 
		{"GenerativeInputNodeNotExistsInDomainMFrag", "Nodo generative Input nao existe na MTheory"}, 
		{"CategoricalStateNotFoundException", "Estado n�o encontrado"}, 		
		{"FileNotFoundException","N�o foi poss�vel abrir o arquivo!"},	
		{"ErrorReadingFile", "Ocorreu erro ao tentar ler o arquivo"}, 
		{"BuiltInDontImplemented", "Built-In n�o � implementada no UnBBayes"},	
		{"NoOVariableContextIdentifier", "No Ordinary Variable scope identifier was found in its name"}	
	};
}