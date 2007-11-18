package unbbayes.prs.mebn.ssbn.resources;

import java.util.*;

import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;

/**
 * @author Shou Matsumoto
 * @version 1.0
 * @since September, 11, 2007
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
	{	{"ArgumentTypeMismatch","Resident node was not expecting this type as argument. Check argument order."}, 
		{"PossibleValueMismatch","The possible value expected was not matching."},
		{"CycleFound","A cycle was found when creating SSBN."},
		{"UnknownException","An unknown error has occurred. We suggest you to store your work and close the program"},
		{"NoNetworkDefined","No Probabilistic Network was defined. Create it first."},
		{"DefaultNetworkName","AutomaticallyCreatedNet"},
		{"IncompatibleNetworks","Parents and childs belong to different networks"},
		{"RecursiveLimit","Recursivity limit has been overlapped"}, 
		{"OrdVariableProblemLimit","For this implementation only one ord. variable search is possible"}, 
		{"MoreThanOneContextNodeSearh","More then one context node search found for the ord. variable. This implementation treat only the trival case of one node"}, 
		{"ContextNodeSearchDontFound","Search context node dont found"}, 
		{"InvalidContextNodeFormula","Invalid Context node Formula"}
	};
}
