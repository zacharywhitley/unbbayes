package unbbayes.io.oobn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.io.oobn package. Localization = japanese.</p>
 * <p>Copyright: Copyright (c) 2008</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 12/20/2009
 */
public class OOBNIOResources_ja extends ListResourceBundle {

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
		{"NoClassNameDefinition", "クラス名の定義が発見出来ませんでした"},
		{"netFileFilterSaveOOBN","第三世代Net定義(.oobn)"},
	};

}
