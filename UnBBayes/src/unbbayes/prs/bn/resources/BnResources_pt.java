package unbbayes.prs.bn.resources;

import java.util.*;

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
	 *  Sobrescreve getContents e retorna um array, onde cada item no array �
	 *	um par de objetos. O primeiro elemento do par � uma String chave, e o
	 *	segundo � o valor associado a essa chave.
	 *
	 * @return O conte�do dos recursos
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
		{"triangulateLabel","\nOrdem de Elimina��o e Triangula��o (liga��es):\n"},
		{"EmptyNetException","A rede est� vazia!"},		
		{"DecisionOrderException","N�o existe ordena��o das vari�veis de decis�o"},
		{"variableName","Vari�vel "},
		{"hasChildName"," cont�m filho(s)"},
		{"linkedName"," ligado a "},
		{"copyName","C�pia do "},
		{"variableTableName","Tabela da vari�vel "},
		{"inconsistencyName"," inconsistente -> "},
		{"utilityName","Utilidade"},
		{"InconsistencyUnderflowException","Encontrado erro num�rico ou inconsist�ncia."}
	};
}