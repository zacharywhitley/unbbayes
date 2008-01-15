package unbbayes.prs.mebn;

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
	{	{"InvalidEdgeException","Arco Invalido!"}, 
		{"CycleFoundException","Ciclo encontrado! Não pode haver ciclos no grafo. "}
	};
}
