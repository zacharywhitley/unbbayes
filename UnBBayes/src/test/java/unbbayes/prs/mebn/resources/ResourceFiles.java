package unbbayes.prs.mebn.resources;

import java.util.ListResourceBundle;

/**
 * Contains the url's of the files used at the tests of MEBN. All change in the 
 * names of this files or in the site should be mapped here. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ResourceFiles extends ListResourceBundle {

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
	{	{"UnBBayesUBF","examples/mebn/StarTrek/StarTrek.ubf"}, 
		{"StarTrekKB_Situation1", "examples/mebn/StarTrek/KnowledgeBase_Situation1.plm"}, 
		
		{"StarTrek_PATHTests","examples/mebn/Tests/StarTrekTestSet"}, 
		{"StarTrek_LogFileName","StarTrekTestSet.log"},
	};
}
