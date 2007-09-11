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
		{"UnknownException","Ocorreu um erro desconhecido. Sugerimos que armazene seu projeto e feche o programa"}
	};
}
