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
package unbbayes.simulation.montecarlo.resources;

import java.util.ListResourceBundle;

/**
 * Resources for the monte carlo package.
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 */

public class MCResources_pt extends ListResourceBundle {

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
		{"netFileFilter","Net (.net), XMLBIF (.xml)"},
		{"textFileFilter","Text (.txt)"},
		
		{"mcTitle", "Simulação Monte Carlo"},
		{"sampleSizeLbl", "Número de casos :"},
		{"ok", "OK"},
		
		{"success", "Sucesso"},
		{"error", "Erro"},
		{"loadNetException", "Erro ao carregar rede"},
		{"sampleSizeException", "O número de casos deve ser um inteiro positivo"},
		{"saveException", "Erro ao salvar arquivo com amostragem dos estados"},
		{"saveSuccess", "Arquivo com amostragem dos estados salvo com sucesso"},

		{"selectFile", "Você deve selecionar um arquivo para amostragem "},
		{"openFile", "Abrir arquivo de rede para amostragem"},
		
	};
}