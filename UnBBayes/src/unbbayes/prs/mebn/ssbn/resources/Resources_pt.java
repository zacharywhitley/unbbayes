package unbbayes.prs.mebn.ssbn.resources;

import java.util.*;

/**
 * @author Shou Matsumoto
 * @version 1.0
 * @since September, 11, 2007
 */

public class Resources_pt extends ListResourceBundle {

    /**
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		return contents;
	}

	/**
	 * The resources
	 */
	static final Object[][] contents =
	{	{"ArgumentTypeMismatch","O nó residente não estava esperando por esse tipo de argumento. Verifique se você a inseriu na ordem correta."}, 
		{"PossibleValueMismatch","Houve incompatibilidade com o valor esperado para o nó."},
		{"CycleFound","Um ciclo foi encontrado ao gerar SSBN."},
		{"UnknownException","Ocorreu um erro desconhecido. Sugerimos que armazene seu projeto e feche o programa"},
		{"NoNetworkDefined","Nenhuma rede probabilística foi definida. Crie primeiro essa rede."},
		{"DefaultNetworkName","RedeCriadaAutomaticamente"},
		{"IncompatibleNetworks","Nós pais e filhos estão em redes diferentes"},
		{"RecursiveLimit","Limite de recursividade atingida"}, 
		{"OrdVariableProblemLimit","Esta implementação permite que apenas uma var. ordinária seja de busca"}, 
		{"MoreThanOneContextNodeSearh","Mais de um nó de contexto de busca foi achado para a var. ordinária. Esta implementação trata apenas o caso de apenas um nó"},
		{"ContextNodeSearchDontFound","Nó de contexto de busca não encontrado"}, 
		{"MoreThanOneOrdereableVariable","Foram encontradas mais de uma variável ordinárea ordenável para o nó residente recursivo. Esta implementação trata apenas o caso trivial de uma variável recursiva."}, 
		{"RVNotRecursive","O nó residente não é recursivo pois não possui variáveis ordinárias ordenáveis. Ciclo Encontrado"}, 		
		{"InvalidContextNodeFormula","Formula do nó de contexto inválida"}
		
	};
}
