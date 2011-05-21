/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege.resources;

import java.util.ArrayList;

/**
 * @author Shou Matsumoto
 *
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
		{"OWLProperties" , "OWL Property"},
		{"OWLPropertiesToolTip" , "Show OWL Properties"},
		{"NoOWLModelFound" , "This MEBN project is not bound to a PR-OWL ontology. Please, save it as a UBF/PR-OWL file and then load it again."},

		{"DnDOWLProperty" , "Drag and drop an OWL property"},
		{"AddProperty" , "Add OWL property"},
		{"RemoveProperty" , "Remove OWL property"},
		
		{"SelectedPropertyError" , "The selected property is erroneous. Please, check OWL model consistency."},
		{"NoSelectedProperty" , "Please, select one property"},
		{"NoSelectedPropertyTitle" , "No selection"},
		
		{"EnterNameOfNewProperty" , "Please, specify a name for the new property."},
		
		{"PropertyRemovalMessage" , "Removing a property usually results in an inconsistent ontology. Are you sure?"},
		{"PropertyRemovalTitle" , "Removing selected property"},
		
		{"CannotDragNDrop" , "Could not drop to target."},
		

		{"ChooseOWLPropertyDomain" , "Choose the domain"},
		{"ChooseOWLPropertyRange" , "Choose the range"},
		
		{"CouldNotLoadProtegeOWLWidget" , "Could not load Protégé-OWL widget to render this panel. Check the JAR version."},
		
		{"DefinesUncertaintyOfToolTip" , "Specify the OWL property that the resident node is defining its uncertainty."},
		{"DefineUncertaintyOfOWLProperty" , "Create a link from a Resident Node to an OWL Property"},
		{"DefineUncertaintyOfOWLPropertyToolTip" , "A Resident Node defines the uncertainty of an OWL Property. You can specify it here."},
		{"DefineUncertaintyOfToolBarToolTip" , "Select another OWL property or just unselect it."},
		{"SelectOWLPropertyToolTip" , "Select another OWL property."},
		{"ClearSelectionToolTip" , "Clear the selection."},

		{"ArgumentMapping" , "Arguments"},
		{"EditArgumentMapping" , "Map the arguments"},
		{"EditArgumentMappingToolTip" , "Map the arguments to domains and ranges of OWL properties."},
		{"NoMappings" , "No mappings"},
		{"isSubjectOf" , " < SUBJECT in > "},
		{"isObjectIn" , " < OBJECT in > "},
		{"chooseTypeOfMapping" , "Choose the type of association"},
		
		{"invalidSelectedNode" , "The selected node seems to be invalid"},
		
	};

}
