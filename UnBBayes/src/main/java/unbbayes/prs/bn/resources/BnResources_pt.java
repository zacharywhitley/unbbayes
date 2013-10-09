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
package unbbayes.prs.bn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.prs.bn. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho
 * @author Michael Onishi
 * @version 1.0
 * @since 02/05/2002
 */

public class BnResources_pt extends ListResourceBundle {

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
	 * Os recursos
	 */
	static final Object[][] contents =
	{	{"CicleNetException","Rede com ciclo:"},
		{"DisconectedNetException","Rede desconexa"},
		{"TableSizeException","Tamanho das tabelas diferem"},
		{"OperatorException","Operador desconhecido"},
		{"moralizeLabel","Moralizados com os arcos:\n"},
		{"triangulateLabel","\nOrdem de Eliminação e Triangulação (ligações):\n"},
		{"EmptyNetException","A rede está vazia!"},		
		{"DecisionOrderException","Não existe ordenação das variáveis de decisão"},
		{"variableName","Variável "},
		{"hasChildName"," contém filho(s)"},
		{"linkedName"," ligado a "},
		{"copyName","Cópia do "},
		{"variableTableName","Tabela da variável "},
		{"inconsistencyName"," inconsistente -> "},
		{"utilityName","Utilidade"},
		{"InconsistencyUnderflowException","Encontrado erro numérico ou inconsistência."},
		{"mandatoryNodeName","O nome é obrigatório"},
		{"duplicateNodeName","Esse nome já existe"},
	};
}