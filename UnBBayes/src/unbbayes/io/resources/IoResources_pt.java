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
	{	{"logHeader","Essa descri��o � feita no processo de compila��o da rede.\n" +
                     "Ela disp�e de informa��es de como a �rvore de jun��o subjacente foi\n" +
                     "criada baseada na t�cnica de �rvore de jun��o com uso da heur�stica do\n" +
                     "peso m�nimo.\n\n"},
		{"cliqueHeader","******************* Cliques ******************\n"},
		{"cliqueName","Clique "},
		{"cliqueLabel"," Clique: "},
		{"potentialTableName","\nTabela de Potencial\n"},
		{"utilityTableName","\nTabela de Utilidade\n"},
		{"separatorHeader","**************** Separators *****************\n"},
		{"separatorName","Separador "},
		{"betweenName","entre "},
		{"andName"," e "},
		{"nodeName","N�(s): "},		
		{"potentialAssociatedHeader","************ Potenciais associados aos cliques **************\n"},
		{"errorNet","Esse arquivo n�o est� de acordo com a especifica��o do NET."},
		{"LoadException"," Falta 'net'"},
		{"LoadException2",": Senten�a inv�lida: "},
		{"LoadException3",": Falta '{'"},
		{"LoadException4",": Vari�vel de decis�o n�o pode ter tabela"},
		{"LoadException5",": Falta 'data'"},
		{"FileNotFoundException","N�o foi poss�vel abrir o arquivo!"},
		{"IsNotDirectoryException", "O caminho especificado tem que ser um diret�rio"}
		
	};
}