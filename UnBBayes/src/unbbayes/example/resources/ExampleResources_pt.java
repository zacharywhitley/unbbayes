
package unbbayes.example.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Arquivo de recurso para o pacote unbbayes.example. Localization = portuguese.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 02/05/2002
 */

public class ExampleResources_pt extends ListResourceBundle {

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
	{	{"exampleTitle","Exemplo de Uso da API"},
		{"fileName","Nome do Arquivo"},
		{"compileTree","Compila Árvore"},
		{"nodeName1","K"},
		{"nodeName2","A"},
		{"nodeDescription","Variável de Teste"},
		{"stateName0","Estado 0"},
		{"stateName1","Estado 1"}
	};
}