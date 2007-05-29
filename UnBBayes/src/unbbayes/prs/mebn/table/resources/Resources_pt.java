package unbbayes.prs.mebn.table.resources;

import java.util.*;

/**
 * @author Laecio Lima dos Santos
 * @version 1.0
 * @since 12/04/2006
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
	{	{"NoDefaultDistributionDeclared","Uma distribuicao default deve ser declarada (uma clausula else)."}, 
		{"InvalidConditionantFound","Um condicionante invalido foi encontrado na declaracao da tabela."},
		{"InvalidProbabilityRange","A distribuicao de probabilidades e invalida (a soma deve ser 1)."},
		{"SomeStateUndeclared","Todos os estados deste no deve ter uma probabilidade associada."}
		
	};
}
