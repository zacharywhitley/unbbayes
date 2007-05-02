package unbbayes.io.mebn.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.io.mebn package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto (cardialfly@[yahoo|gmail].com)
 * @version 0.1
 * @since 01/05/2007
 * @see unbbayes.io.mebn.UbfIO.java
 */

public class IoUbfResources extends ListResourceBundle {

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

	/*TODO Traduzir */
	
	/**
	 * The resources
	 */
	static final Object[][] contents =
	{		
		{"UBFFileHeader", " UBF file header"},
		{"UBFMTheory", " UBF MTheory declaration"},
		{"UBFMFragsNodes", " UBF MFrags and Nodes declaration"},
		{"UBFMFrags", " MFrag"},
		{"UBFResidentNodes", " Resident nodes"},
		{"UBFInputNodes", " Input nodes"},
		{"UBFContextNodes", " Context nodes"},
		{"UBFOrdinalVars", " Ordinary Variables"},
		
		{"InvalidSyntax", "Invalid syntax for this version"},
		{"NoProwlFound", "No pr-owl file found"}, 		
		{"IncompatibleVersion", "File version is incompatible"},
		{"MTheoryConfigError", "Some MTheory data where not loaded - using default"},
		{"MFragConfigError", "Some MFrag/Node data where not loaded - using default"},
		{"MFragTypeException", "System has tried to store a unexpected MFrag type"},
	};
}
