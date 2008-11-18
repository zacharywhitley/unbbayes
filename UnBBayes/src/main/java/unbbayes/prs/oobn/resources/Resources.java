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
		
		{"InputNodeHasNoParents", "Input node should have no parents"},
		{"InstanceOutputNodeHasNoParents", "Output node of an instance should have no parents"},
		{"InstanceInputNodeHasNoMultipleParents", "Input node of an instance should not have multiple parents"},
		{"NoNodeIsParentOf2InstanceInput","No node should be parent of 2 or more instance input node"},
		{"PleaseAddParentToInstanceInputNodes","Please, add an edge to an input node"},
		{"PleaseAddChildToInstanceOutputNodes", "Please, add an edge to an output node"},
		{"InstanceInputTypeCompatibilityFailed", "Node types are not compatible. Please, check number and name of states"},
		{"ClassCycleFound", "There is a cycle: class contains itself"},
	};
	
}
