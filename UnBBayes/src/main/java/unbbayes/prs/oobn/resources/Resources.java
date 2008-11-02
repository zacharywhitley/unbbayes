/**
 * 
 */
package unbbayes.prs.oobn.resources;

import java.util.ListResourceBundle;

/**
 * @author Shou Matsumoto
 *
 */
public class Resources extends ListResourceBundle {

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
	{	{"OOBNExceptionMessage","General OOBN exception"},
		//{"compileToolTip","Compile OOBN using current class"},
		

		{"DuplicateOOBNClassExceptionMessage","Duplicate OOBN class found"},
	};
	
}
