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
package unbbayes.prs.mebn.compiler.resources;

import java.util.ListResourceBundle;

/**
 * @author Laecio Lima dos Santos
 * @version 1.0
 * @since 12/04/2006
 */

public class Resources_pt extends ListResourceBundle {

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
	{	{"NoDefaultDistributionDeclared","Uma distribuicao default deve ser declarada (uma clausula else)."}, 
		{"InvalidConditionantFound","Um condicionante invalido foi encontrado na declaracao da tabela."},
		{"InvalidProbabilityRange","A distribuicao de probabilidades e invalida (a soma deve ser 1)."},
		{"SomeStateUndeclared","Todos os estados deste no deve ter uma probabilidade associada."},
		{"UnexpectedTokenFound","Um termo inesperado foi encontrado."},
		{"TableUndeclared","A tabela não está declarada."},
		{"FatalError","Erro Fatal: O compilador do pseudocódigo da CPT foi mal implementada."},
		{"SSBNInstanceFailure","O gerador de SSBN falhou na instanciação de nós."}
	};
}
