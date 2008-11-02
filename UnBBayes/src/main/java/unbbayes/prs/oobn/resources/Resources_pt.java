/**
 * 
 */
package unbbayes.prs.oobn.resources;

import java.util.ListResourceBundle;

/**
 * @author Shou Matsumoto
 *
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
	{	{"OOBNExceptionMessage","Exceção geral de OOBN"},
		//{"compileToolTip","Compila a OOBN usando a classe atual"},
		
		{"DuplicateOOBNClassExceptionMessage","Foi encontrado uma classe duplicada"},
	};
}
