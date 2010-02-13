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
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto (cardialfly@[yahoo|gmail].com)
 * @version 0.1
 * @since 01/05/2007
 * @see unbbayes.io.mebn.UbfIO.java
 */

public class IoUbfResources_pt extends ListResourceBundle {

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

	
	/**
	 * The resources
	 */
	static final Object[][] contents =
	{		
		{"UBFFileHeader", " Cabeçalho UBF"},
		{"UBFMTheory", " Declaração de MTheory - UBF"},
		{"UBFMFragsNodes", " Declaração de MFrags e nós UBF"},
		{"UBFMFrags", " MFrag"},
		{"UBFResidentNodes", " Nós Residentes"},
		{"UBFInputNodes", " nós Input"},
		{"UBFContextNodes", " Nós de Contexto"},
		{"UBFOrdinalVars", " Variáveis Ordinárias"},
		{"UBFObjectEntityInstances", " Instâncias de Entidades Objetos"},
		
		{"InvalidSyntax", "Sintaxe inválida para essa versão"},
		{"NoProwlFound", "Não foi encontrado um arquivo pr-owl"}, 	
		{"InvalidProwlScheme", "Arquivo contém declarações inválidas do pr-owl"}, 	
		{"InvalidUbfScheme", "Arquivo contém construção errônea. Alguns elementos serão ignorados"}, 
		{"IncompatibleVersion", "Versão incompatível de arquivo"},
		{"MTheoryConfigError", "Algumas MTheories não foram carregadas - usando default"},
		{"MFragConfigError", "Algumas MFrags/Nós não foram carregadas - usando default"},
		{"MFragTypeException", "O sistema executou uma tentativa de armazenamento de uma MFrag inválida"},
		{"UnknownPrOWLError", "Erro ao manipular pr-owl file (.owl)"}
	};
}
