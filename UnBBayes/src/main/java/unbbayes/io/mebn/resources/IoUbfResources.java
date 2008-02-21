/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io.mebn.resources;

import java.util.ListResourceBundle;

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
		{"UBFObjectEntityInstances", " Object Entity Instances"},
		
		{"InvalidSyntax", "Invalid syntax for this version"},
		{"NoProwlFound", "No pr-owl file found"}, 	
		{"InvalidProwlScheme", "File contains invalid pr-owl format"}, 	
		{"InvalidUbfScheme", "File contains invalid or erroneous UBF format. Some elements were ignored"}, 
		{"IncompatibleVersion", "File version is incompatible"},
		{"MTheoryConfigError", "Some MTheory data where not loaded - using default"},
		{"MFragConfigError", "Some MFrag/Node data where not loaded - using default"},
		{"MFragTypeException", "System has tried to store a unexpected MFrag type"},
		{"UnknownPrOWLError", "Error manipulating pr-owl file (.owl)"}
	};
}
