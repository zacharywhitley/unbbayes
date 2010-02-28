/**
 * 
 */
package unbbayes.io.oobn.resources;


import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.io.oobn package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 11/23/2008
 */

public class OOBNIOResources extends ListResourceBundle {

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
	static final Object[][] contents =	{	
		{"NoClassNameDefinition", "No class name was defined at class file."},
		{"netFileFilterSaveOOBN","Net 3rd specification (.oobn)"},
	};
}
