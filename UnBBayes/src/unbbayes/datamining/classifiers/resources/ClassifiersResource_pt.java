package unbbayes.datamining.classifiers.resources;

import java.io.*;
import java.util.*;

/** Resources file for classifiers package. Localization = portuguese.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class ClassifiersResource_pt extends ListResourceBundle implements Serializable
{	/** Override getContents and provide an array, where each item in the array is a pair
		of objects. The first element of each pair is a String key,
		and the second is the value associated with that key.
	*/
	public Object[][] getContents()
	{	return contents;
	}

	/** The resources */
	static final Object[][] contents =
	{	// Naive Bayes
		{"nullPrediction","Distribui��o null"},
		{"exception1","N�o � aceito classe num�rica!"},
		{"attribute","Atributo"},
		{"exception2","menos do que duas classes"},
		{"exception3","o desvio padr�o � 0 para a classe"},
		{"exception4","o modelo ainda n�o foi constru�do"},
		{"class","Classe"},
		{"mean","M�dia"},
		{"stdev","Desvio padr�o:"},
		{"exception5","Erro ao imprimir o classificador Naive Bayes!"},
		// Evaluation
		{"summary","=== Resumo ===\n"},
		{"correctly",   "Inst�ncias Corretamente Classificadas   "},
		{"incorrectly", "Inst�ncias Incorretamente Classificadas "},
		{"unclassified","Inst�ncias N�o Classificadas            "},
		{"totalNumber", "N�mero Total de Inst�ncias              "},
		{"unknownInstances","Inst�ncias Ignoradas Com Classes Desconhecidas   "},
		{"folds2","N�mero de folds deve ser pelo menos 2!"},
		{"moreFolds","N�o pode haver mais folds do que inst�ncias!"},
		{"folds1","N�mero de folds deve ser maior que 1"},
		{"classNegative","�ndice da classe � negativo (n�o definido)!"},
		{"noMatrix","Avalia��o: A matriz confus�o n�o pode ser constru�da!"},
		{"accuracy","=== Acur�cia Detalhada por Classe ===\n"},
		{"matrix","=== Matriz Confus�o ===\n"},
		// Id3
		{ "exception1","�rvore de decis�o: N�o � aceito classe num�rica!"		},
		{ "exception2","�rvore de decis�o: N�o � aceito atributos num�ricos!"	},
		{ "exception3","�rvore de decis�o: N�o s�o aceitos missing values!" 	},
		{ "toStringException1","�rvore de decis�o: O modelo ainda n�o foi constru�do. Tente de novo!"	},
		{ "id3ToString","�RVORE DE DECIS�O:"	},
		{ "null","null"	},
		{ "NULL","NULL"	},
	};
}
