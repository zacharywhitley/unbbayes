package unbbayes.datamining.datamanipulation.resources;

import java.util.*;

/** Resources file for datamanipulation package. Localization = portuguese.
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class DataManipulationResource_pt extends ListResourceBundle
{	/** Override getContents and provide an array, where each item in the array is a pair
		of objects. The first element of each pair is a String key,
		and the second is the value associated with that key.
	*/
	public Object[][] getContents()
	{	return contents;
	}

	/** The resources */
	static final Object[][] contents =
	{	{"normalizeException1","Array n�o pode ser normalizado. Soma � NaN."},
		{"normalizeException2","Array n�o pode ser normalizado. Soma � zero."},
		{"readHeaderException1","Fim de linha prematuro."},
		{"readHeaderException2","Palavra chave @relation esperada"},
		{"readHeaderException3","Nenhum tipo de atributo v�lido ou enumera��o inv�lida"},
		{"readHeaderException4","{ esperado no in�cio da enumera��o "},
		{"readHeaderException5","} esperado no fim da enumera��o "},
		{"readHeaderException6","Nenhum valor nominal encontrado"},
		{"readHeaderException7","Palavra chave @data esperada"},
		{"readHeaderException8","Nenhum atributo declarado"},
		{"getLastTokenException1","fim de linha esperado"},
		{"getNextTokenException1","Fim de linha prematuro."},
		{"getNextTokenException2","Fim de arquivo prematuro."},
		{"getInstanceException1","Nenhuma informa��o no cabe�alho dispon�vel"},
		{"getInstanceFullException1","Valor inv�lido"},
		{"getInstanceFullException2","Valor nominal n�o declarado no cabe�alho"},
		{"getInstanceFullException3","Numero esperado"},
		{"getInstanceTXT","Nenhuma informa��o sobre o atributo dispon�vel"},
		{"runtimeException1","Inst�ncia n�o tem acesso � base de dados!"},
		{"runtimeException2","Classe n�o est� definida!"},
		{"illegalArgumentException1","�ndice da classe inv�lido: "},
		{"setAttributeAtException","�ndice n�o definido"},
		{"setClassIndexException","�ndice da classe inv�lido: "},
		{"outOfRange","Par�metro first e/ou toCopy n�o definidos"},
		{"setValueException","Valor n�o pode ser inserido. N�o consegue traduzir String para float."},
	};
}