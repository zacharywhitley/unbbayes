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
		{"InconsistencyUnderflowException","Encontrado erro numérico ou inconsistência."}
	};
}