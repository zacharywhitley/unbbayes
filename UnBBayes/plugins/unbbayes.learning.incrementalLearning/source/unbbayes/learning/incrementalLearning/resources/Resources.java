/**
 * 
 */
package unbbayes.learning.incrementalLearning.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.learning.incrementalLearning package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 10/02/2010
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
	{	
		{"title","Incremental Learning"},
		{"selectFile","Select a network file to start incremental learning"},
		{"openFile","Open File"},
		{"chooseNetworkFile","Choose the network file"},
		{"chooseFrontierSet","Choose the frontier set"},
		{"chooseTrainingSet","Choose the training set"},
		{"saveNetworkFile","Save the network file"},
		{"saveFrontierSet","Save the frontier set"},

	};

}
