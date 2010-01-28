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
package unbbayes.datamining.classifiers.resources;

import java.io.Serializable;
import java.util.ListResourceBundle;

/** Resources file for classifiers package. Localization = portuguese.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class ClassifiersResource_pt extends ListResourceBundle implements Serializable
{	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	/** Override getContents and provide an array, where each item in the array is a pair
		of objects. The first element of each pair is a String key,
		and the second is the value associated with that key.
	*/
	public Object[][] getContents()
	{	return contents;
	}

	/** The resources */
	static final Object[][] contents =
	{	// Naive Bayes
		{"nullPrediction","Distribuição null"},
		{"exception1","Não é aceito classe numérica!"},
		{"attribute","Atributo"},
		{"exception2","menos do que duas classes"},
		{"exception3","o desvio padrão é 0 para a classe"},
		{"exception4","o modelo ainda não foi construído"},
		{"class","Classe"},
		{"mean","Média"},
		{"stdev","Desvio padrão:"},
		{"exception5","Erro ao imprimir o classificador Naive Bayes!"},
		// Evaluation
		{"summary","=== Resumo ===\n"},
		{"correctly",   "Instâncias Corretamente Classificadas   "},
		{"incorrectly", "Instâncias Incorretamente Classificadas "},
		{"unclassified","Instâncias Não Classificadas            "},
		{"totalNumber", "Número Total de Instâncias              "},
		{"unknownInstances","Instâncias Ignoradas Com Classes Desconhecidas   "},
		{"folds2","Número de folds deve ser pelo menos 2!"},
		{"moreFolds","Não pode haver mais folds do que instâncias!"},
		{"folds1","Número de folds deve ser maior que 1"},
		{"classNegative","índice da classe é negativo (não definido)!"},
		{"noMatrix","Avaliação: A matriz confusão não pode ser construída!"},
		{"accuracy","=== Acurácia Detalhada por Classe ===\n"},
		{"matrix","=== Matriz Confusão ===\n"},
		// Id3
		{ "exception1","Árvore de decisão: Não é aceito classe numérica!"		},
		{ "exception2","Árvore de decisão: Não é aceito atributos numéricos!"	},
		{ "exception3","Árvore de decisão: Não são aceitos missing values!" 	},
		{ "toStringException1","Árvore de decisão: O modelo ainda não foi construído. Tente de novo!"	},
		{ "id3ToString","ÁRVORE DE DECISÃO:"	},
		{ "null","null"	},
		{ "NULL","NULL"	},
	};
}
