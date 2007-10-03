package unbbayes.io.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.io. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 02/05/2002
 */

public class IoResources_pt extends ListResourceBundle {

    /**
	 *  Sobrescreve getContents e retorna um array, onde cada item no array e
	 *	um par de objetos. O primeiro elemento do par e uma String chave, e o
	 *	segundo e o valor associado a essa chave.
	 *
	 * @return O conteudo dos recursos
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * Os recursos
	 */
	static final Object[][] contents =
	{	{"logHeader","Essa descrição é feita no processo de compilação da rede.\n" +
                     "Ela dispõe de informações de como a árvore de junção subjacente foi\n" +
                     "criada baseada na técnica de árvore de junção com uso da heurística do\n" +
                     "peso mínimo.\n\n"},
		{"cliqueHeader","******************* Cliques ******************\n"},
		{"cliqueName","Clique "},
		{"cliqueLabel"," Clique: "},
		{"potentialTableName","\nTabela de Potencial\n"},
		{"utilityTableName","\nTabela de Utilidade\n"},
		{"separatorHeader","**************** Separators *****************\n"},
		{"separatorName","Separador "},
		{"betweenName","entre "},
		{"andName"," e "},
		{"nodeName","Nó(s): "},		
		{"potentialAssociatedHeader","************ Potenciais associados aos cliques **************\n"},
		{"errorNet","Esse arquivo não está de acordo com a especificação do NET."},
		{"LoadException"," Falta 'net'"},
		{"LoadException2",": Sentença inválida: "},
		{"LoadException3",": Falta '{'"},
		{"LoadException4",": Variável de decisão não pode ter tabela"},
		{"LoadException5",": Falta 'data'"},
		{"FileNotFoundException","Não foi possível abrir o arquivo!"},
		{"IsNotDirectoryException", "O caminho especificado tem que ser um diretório"}
		
	};
}