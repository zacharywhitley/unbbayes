
package unbbayes.example.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.example package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 02/05/2002
 */

public class ExampleResources extends ListResourceBundle {

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
	{	{"exampleTitle","Use of API Example"},
		{"fileName","File Name"},
		{"compileTree","Compile Tree"},
		{"nodeName1","K"},
		{"nodeName2","A"},
		{"nodeDescription","Test Variable"},
		{"stateName0","State 0"},
		{"stateName1","State 1"}		
	};
}