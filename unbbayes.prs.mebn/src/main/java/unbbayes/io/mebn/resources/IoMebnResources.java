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
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: UnB</p>
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0
 * @since 11/10/2006
 */

public class IoMebnResources extends ListResourceBundle {

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
		{"MTheoryNotExist", "This file does not contain a MTheory."}, 
		{"ModelCreationError", "Could not read the PR-OWL file correctly."}, 
		{"DomainMFragNotExistsInMTheory", "This MTheory does not contain such MFrag."}, 
		{"ContextNodeNotExistsInMTheory", "This MTheory does not contain such context node."}, 
		{"GenerativeInputNodeNotExistsInMTheory", "This MTheory does not contain such generative input node."}, 		
		{"BuiltInRVNotExistsInMTheory", "This MTheory does not contain such Built-In RV."}, 		
		{"OVariableNotExistsInMTheory", "This MTheory does not contain such Ordinary Variable."}, 		
		{"NodeNotFound", "The Node was not found."}, 		
		{"ArgumentNotFound", "The Argument was not found."}, 		
		{"isOVariableInError", "Inconsistent isOVariableIn property."}, 			
		{"isArgumentOfError", "Inconsistent isArgumentOf property."}, 					
		{"ArgumentTermInError", "Conflicting Argument Term."}, 			
		{"ContextNodeNotExistsInMFrag", "The MFrag does not contain such ContextNode."}, 
		{"DomainResidentNotExistsInMTheory", "This MTheory does not contain such Domain Resident Node."}, 
		{"DomainResidentNotExistsInDomainMFrag", "This DomainMFrag does not contain such Domain Resident Node."}, 
		{"GenerativeInputNodeNotExistsInDomainMFrag", "This MFrag does not contain such Generative Input Node."}, 
		{"CategoricalStateNotFoundException", "The State was not found."}, 		
		{"FileNotFoundException","Could not access the file."},	
		{"ErrorReadingFile", "There was an error reading the file."}, 
		{"BuiltInDontImplemented", "UnBBayes cannot handle such Built-In node."},	
		{"NoOVariableContextIdentifier", "No Ordinary Variable scope identifier was found in its name"}	
	};
}
