package unbbayes.datamining.gui.datamanipulation.resources;

import java.util.*;

/** Resources file for datamanipulation package. Localization = english.
 *
 *  @author Emerson Lopes Machado (emersoft@conectanet.com.br)
 *  @version $1.0 $ (2006/11/24)
 */
public class AttributeTypeChooserResource extends ListResourceBundle {
	/** 
	 * Override getContents and provide an array, where each item in the array
	 * is a pair of objects. The first element of each pair is a String key,
	 * and the second is the value associated with that key.
	 */
	public Object[][] getContents() {
		return contents;
	}

	/** The resources */
	static final Object[][] contents = {
		{"windowTitle","Attribute Type Chooser"},
		{"attributeType","Choose the attribute type:"},
		{"isString","Check if String values:"},
		{"counter","Choose the counter:"},
	};
}