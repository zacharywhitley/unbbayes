package unbbayes.datamining.evaluation.resources;

import java.util.*;

/** Resources file for datamanipulation package. Localization = portuguese.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class BatchEvaluationResource_pt extends ListResourceBundle {

	/**
	 * Override getContents and provide an array, where each item in the array
	 * is a pair of objects. The first element of each pair is a String key,
	 * and the second is the value associated with that key.
	 */
	public Object[][] getContents() {
		return contents;
	}

	/** The resources */
	static final Object[][] contents = {
		{"normalizeException1", "Array não pode ser normalizado. Soma é NaN."},
		{"normalizeException2", "Array não pode ser normalizado. Soma é zero."},
		{"normalizeException3", "Não é possível normalizar. Limites de normalização inválidos."},
		{"readHeaderException1", "Fim de linha prematuro."},
		{"readHeaderException2", "Palavra chave @relation esperada"},
		{"readHeaderException3", "Nenhum tipo de atributo válido ou enumeração inválida"},
		{"readHeaderException4", "{ esperado no início da enumeração "},
		{"readHeaderException5", "} esperado no fim da enumeração "},
		{"readHeaderException6", "Nenhum valor nominal encontrado"},
		{"readHeaderException7", "Palavra chave @data esperada"},
		{"readHeaderException8", "Nenhum atributo declarado"},
		{"getLastTokenException1", "fim de linha esperado"},
		{"getNextTokenException1", "Fim de linha prematuro."},
		{"getNextTokenException2", "Fim de arquivo prematuro."},
		{"getInstanceException1", "Nenhuma informação no cabeçalho disponível"},
		{"getInstanceAuxException1", "uma String foi lida quando se esperava um número"},
		{"getInstanceFullException1", "Valor inválido"},
		{"getInstanceFullException2", "Valor nominal não declarado no cabeçalho"},
		{"getInstanceFullException3", "Número esperado"},
		{"getInstanceTXT", "Nenhuma informação sobre o atributo disponível"},
		{"runtimeException1", "Instância não tem acesso à base de dados!"},
		{"runtimeException2", "Classe não está definida!"},
		{"illegalArgumentException1", "Índice da classe inválido: "},
		{"setAttributeAtException", "Índice não definido"},
		{"setClassIndexException", "Índice da classe inválido: "},
		{"outOfRange", "Parâmetro first e/ou toCopy não definidos"},
		{"setValueException", "Valor não pode ser inserido. Não consegue traduzir String para float."},
		{"emptyInstanceSet", "O instanceSet não contém instancias."},
		{"nominalAttribute", "O atributo para se calcular o desvio padrão deve ser numérico."},
		
		
		/**********************************************************************
		 * Datasets
		 *********************************************************************/
		{"activeTableHeader", "Ativo"},
		{"finishedTableHeader", "Finalizado"},
		{"datasetNameTableHeader", "Base de dados"},
		{"classTableHeader", "Classe"},
		{"counterTableHeader", "Contador"},
		{"fileTableHeader", "Arquivo"},
		
		
		/**********************************************************************
		 * InitializePreprocessors
		 *********************************************************************/
		{"preprocessorNameTableHeader", "Preprocessador"},
		{"configButtonTableHeader", ""},
		
		
		/**********************************************************************
		 * Classifiers
		 *********************************************************************/
		{"classifierNameTableHeader", "Classificador"},
		
		
		/**********************************************************************
		 * Evaluations
		 *********************************************************************/
		{"evaluationNameTableHeader", "Avaliação"},

	};
}