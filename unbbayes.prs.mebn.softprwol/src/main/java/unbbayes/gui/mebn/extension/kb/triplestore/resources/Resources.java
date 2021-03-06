package unbbayes.gui.mebn.extension.kb.triplestore.resources;

import java.util.ArrayList;

/**
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class Resources extends unbbayes.gui.mebn.resources.Resources {

	
	/** 
	 *  Override getContents and provide an array, where each item in the array is a pair
	 *	of objects. The first element of each pair is a String key,
	 *	and the second is the value associated with that key.
	 *
	 * @return The resources' contents
	 */
	public Object[][] getContents() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		for (Object[] objects : super.getContents()) {
			list.add(objects);
		}
		for (Object[] objects2 : this.contents) {
			list.add(objects2);
		}
		return list.toArray(new Object[0][0]);
	}
	
	/**
	 * The particular resources for this class
	 */
	static final Object[][] contents =
	{	
		{"Message" , "Message"},
		{"Error" , "Error"},
		
		{"ConnectionError" , "Error trying connect to triplestore."},
		{"ConnectionSuccessfull" , "Connection sucessfull."},
		
	};

}
