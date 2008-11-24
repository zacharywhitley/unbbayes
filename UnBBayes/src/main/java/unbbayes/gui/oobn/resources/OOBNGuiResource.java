/**
 * 
 */
package unbbayes.gui.oobn.resources;

import java.util.ListResourceBundle;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.oobn package. Localization = english.</p>
 * <p>Copyleft: LGPL 2008</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 12/11/2008
 */
public class OOBNGuiResource extends ListResourceBundle {

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
		{"OOBNPopupMenuMessage","Change node type to:"},
		{"changeNodeToPrivate","Change to Private Node"},
		{"changeNodeToOutput","Change to Output Node"},
		{"changeNodeToInput","Change to Input Node"},
		{"OOBNPopupMenuTooltipMessage","Select one node type"},
		{"openClassFromFile","Open a new class from file"},
		
		{"ErrorLoadingClass","There was an error loading a class"},
		
		
		{"editionToolTip","Go to edition mode"},
		{"removeToolTip","Delete selected class from project"},
		{"newToolTip","Add new class to project"},
		{"newFromFileToolTip","Add new class from file"},
		{"status","Status:"},
		{"newOOBNClass","NewOOBNClass"},
		{"renameClass", "Renaming the oobn class"},
		{"oobnFileFilter","Net (.net), Net for OOBN (.oobn)"},
		{"NoClassSelected","No OOBN class was selected"},
		{"compilationError" , "Compilation Error"},
		{"DuplicatedClassName","Class name is duplicated"},
		
		{"CannotDragNDrop", "Could not drag and drop a class"},
		{"dragNDropToAddInstance", "Drag and drop a class from here to add an instance"},
		
		{"compileToolTip","Compile OOBN using selected class"},
		{"statusReadyLabel", "Ready"},
		
		{"classNavigationPanelLabel", "Classes List"},
		
		{"leftClickToChangeNodeType", "Left click to change node's type"},
		
		{"changeNodeType", "Change selected node type"},
		

		{"netFileFilterSaveOOBN","Net 3rd specification (.oobn)"},
		{"saveTitle", "Save currently active class"},
		
	};
}
