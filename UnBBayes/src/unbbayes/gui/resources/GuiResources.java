package unbbayes.gui.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 06/04/2002
 */

public class GuiResources extends ListResourceBundle {

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
		{"fileDirectoryType","Directory"},
		{"fileARFFType","Arquivo Arff"},
		{"fileTXTType","Text File TXT"},
		{"fileNETType","Baysian Netwotk File NET"},
		{"fileGenericType","Generic File"},
		
		{"unbbayesTitle","UnBBayes"},
		
		//main toll bar tips 
		{"newToolTip","New net"},
		{"newMsbnToolTip", "New MSBN"}, 
		{"newMebnToolTip", "New MEBN"}, 
		{"openToolTip","Open net"},
		{"saveToolTip","Save net"},
		{"learningToolTip","Learning mode"},
		{"metalToolTip","Metal Look And Feel"},
		{"motifToolTip","Motif Look And Feel"},
		{"windowsToolTip","Windows Look And Feel"},
		{"tileToolTip","Organize windows in tile"},
		{"cascadeToolTip","Organize windows in cascade"},
		
		{"netFileFilter","Net (.net), XMLBIF (.xml), PR-OWL (.owl), UnBBayes file (.ubf)"},
		{"netFileFilterSave","Net (.net), XMLBIF (.xml), UnBBayes file (.ubf)"},
		
		{"xmlBIFFileFilter", "XMLBIF (.xml)"},
		{"textFileFilter","Text (.txt)"},
		{"fileUntitled","Untitled.txt"},
		
		{"globalOptionTitle","Global Option"},
		{"hierarchyToolTip","Hierarchy definition"},
        
		{"usaName","USA"},
		{"chinaName","China"},
		{"japanName","Japan"},
		{"ukName","UK"},
		{"koreaName","Korea"},
		{"italyName","Italy"},
		{"canadaName","Canada"},
		{"brazilName","Brazil"},
		
		{"nodeName","Node: "},
		{"radiusLabel","Radius:"},
		{"radiusToolTip","Node radius"},
		{"netLabel","Net"},
		{"netToolTip","Net size"},
		{"probabilisticDescriptionNodeColorLabel","Description"},
		{"probabilisticDescriptionNodeColorToolTip","Select probabilistic description node color"},
		{"probabilisticExplanationNodeColorLabel","Explanation"},
		{"probabilisticExplanationNodeColorToolTip","Select probabilistic explanation node color"},
		{"decisionNodeColorLabel","Decision"},
		{"decisionNodeColorToolTip","Select decision node color"},
		{"utilityNodeColorLabel","Utility"},
		{"utilityNodeColorToolTip","Select utility node color"},
		{"nodeColorLabel","Node Color"},
                {"arcColor","Arc Color"},
                {"selectionColor","Selection Color"},
                {"backGroundColor","Background Color"},
                {"arcColorLabel","Arc"},
		{"arcColorToolTip","Select arc color"},
		{"selectionColorLabel","Selection"},
		{"selectionColorToolTip","Select selection color"},
		{"backgroundColorLabel","Background"},
		{"backgroundColorToolTip","Select background color"},
		
		{"confirmLabel","Confirm"},
		{"confirmToolTip","Corfim modifications"},
		{"cancelLabel","Cancel"},
		{"cancelToolTip","Cancel modifications"},
		{"resetLabel","Reset"},
		{"resetToolTip","Reset default values"},
		{"decimalPatternTab","Decimal Pattern"},
		{"colorControllerTab","Color Controller"},
		{"sizeControllerTab","Size Controller"},
		{"logTab","Log"},
		{"createLogLabel","Create Log"},
		{"nodeGraphName","Node"},
		
		{"LookAndFeelUnsupportedException","It does not support this LookAndFeel: "},
		{"LookAndFeelClassNotFoundException","This LookAndFeel class was not found: "},
		{"LookAndFeelInstantiationException","It was not possible to load this LookAndFeel: "},
		{"LookAndFeelIllegalAccessException","This LookAndFeel can not be used: "},
		{"nameError","Nome don't acept"},	
		{"nameAlreadyExists","Already exists a object with this name"},	
		{"internalError","Internal Error..."},	
		
		{"helpToolTip","UnBBayes help"},
		{"propagateToolTip","Propagate evidences"},
		{"expandToolTip","Expand evidences tree"},
		{"collapseToolTip","Collapse evidences tree"},
		{"editToolTip","Return to edit mode"},
		{"logToolTip","Information about Compilation (Log)"},
		{"resetCrencesToolTip","Reset crences"},
		{"printNetToolTip","Print graph"},
		{"previewNetToolTip","Print graph preview"},
		{"saveNetImageToolTip","Save graph as a gif image"},
		{"siglaLabel","Sigla:"},
		{"nameLabel", "Name:"}, 
		{"typeLabel", "Type:"}, 
		{"descriptionLabel","Description"},
		{"compileToolTip","Compile junction tree"},
		{"moreToolTip","Add state"},
		{"lessToolTip","Remove state"},
		{"arcToolTip","Insert arc"},
		{"probabilisticNodeInsertToolTip","Insert probabilistic variable"},
		{"decisionNodeInsertToolTip","Insert decision variable"},
		{"utilityNodeInsertToolTip","Insert utility variable"},
		{"contextNodeInsertToolTip","Insert context variable"},
		{"inputNodeInsertToolTip","Insert input variable"},
		{"residentNodeInsertToolTip","Insert resident variable"},
		{"mFragInsertToolTip","Insert MFrag"},
		{"inputActiveToolTip","Input Node Selected"},  
		{"mFragActiveToolTip","MFrag Selected"}, 		
		{"contextActiveToolTip","Context Node Selected"}, 
		{"residentActiveToolTip","Resident Node Selected"}, 		
		{"addArgumentToolTip","Add argumment"}, 
		{"editFormulaToolTip","Edit formula"},		
		{"selectToolTip","Select various nodes and arcs"},
		{"printTableToolTip","Print table"},
		{"previewTableToolTip","Print table preview"},
		{"saveTableImageToolTip","Save table as a gif image"},
		{"newEntityToolTip","Create new entity"},		
		{"delEntityToolTip","Delete entity"},
		{"newOVariableToolTip","Create new ord. variable"},
		{"delOVariableToolTip", "Delete ord. variable"}, 	
		{"newArgumentToolTip","Add new ord. variable to argument list"},
		{"delArgumentToolTip", "Remove ord. variable from argument list"}, 	
		{"downArgumentToolTip", "Add to argument list ord. variable selected"}, 
		{"mTheoryEditionTip", "Edit atributes of the MTheory"}, 		
		
		{"showMTheoryToolTip","Show MTheory tree"},	
		{"showEntitiesToolTip","Show entities of the MTheory"},
		{"showOVariablesToolTip","Show ovariables of the MFrag"},			
		
		{"formula","Formula:"},	
		{"inputOf","Input of:"},	
		{"arguments", "Args: "}, 
		{"statusReadyLabel","Ready"},
		
		{"andToolTip", "'and' operator"}, 
		{"orToolTip", "'or' operator"},
		{"notToolTip", "'not' operator"},
		{"equalToToolTip", "'equal to' operator"},
		{"impliesToolTip", "'implies' operator"},
		{"iffToolTip", "'iff' operator"},
		{"forallToolTip", "'for all' quantifier"},
		{"existsToolTip", "'exists' quantifier"},		
		
		//Menus MEBN
		{"menuDelete", "Delete"}, 
		{"menuAddContext", "Add Context"}, 
		{"menuAddInput", "Add Input"},
		{"menuAddResident", "Add Resident"}, 
		{"menuAddDomainMFrag", "Add Domain MFrag"}, 
		{"menuAddFindingMFrag", "Add Finding MFrag"}, 
		
		//Titles for tab panel
		{"ResidentTabTitle", "Resident Node"}, 
		{"InputTabTitle", "Input Node"}, 
		{"ContextTabTitle", "Context Node"}, 
		{"MTheoryTreeTitle", "MTheory Tree"}, 
		{"EntityTitle", "Entity"}, 
		{"OVariableTitle", "Ord. Variable"}, 
		{"ArgumentTitle", "Arguments"}, 
		{"StatesTitle", "States"}, 	
		{"FathersTitle", "Fathers Nodes"}, 		
		{"AddFinding", "Finding"}, 	
		
		//Label for buttons of tab selection
		/* Don't use names with more than fifteen letters */
		{"MTheoryButton", "MTheory"}, 
		{"ResidentButton", "Resident"}, 
		{"InputButton", "Input"}, 
		{"ContextButton", "Context"}, 
		{"MFragButton", "MFrag"}, 	
		{"ArgumentsButton", "Arguments"}, 			
		{"OrdVariableButton", "Variable"}, 
		
		{"whithotMFragActive","Don't have MFrag active"},			
		{"previewTitle","Preview"},
		{"filesText"," files"},
		{"aprendizagemTitle","Net Learning Edition"},
		{"calculateProbabilitiesFromLearningToEditMode","Rebuild net structure and return to edit mode"},
        {"fileMenu","File"},
        {"lafMenu","Look and Feel"},
        {"viewMenu","View"},
        {"tbMenu","Toolbars"},
        {"toolsMenu","Tools"},
        {"windowMenu","Window"},
        {"helpMenu","Help"},
        {"newMenu","New..."},
        {"newBN", "New BN"},
        {"newMSBN", "New MSBN"},
        {"newMEBN","New MEBN"},
        {"openItem","Open..."},
        {"saveItem","Save as..."},
        {"exitItem","Exit"},
        {"tbFile","File Toolbar"},
        {"tbView","View Toolbar"},
        {"tbTools","Tools Toolbar"},
        {"tbWindow","Window Toolbar"},
        {"tbHelp","Help Toolbar"},
        {"metalItem","Metal"},
        {"motifItem","Motif"},
        {"windowsItem","Windows"},
        {"learningItem","Learning"},
        {"cascadeItem","Cascade"},
        {"tileItem","Tile"},
        {"helpItem","Help"},
        {"aboutItem","About UnBBayes"},
        
        {"fileMenuMn","F"},
        {"lafMenuMn","L"},
        {"viewMenuMn","V"},
        {"tbMenuMn","T"},
        {"toolsMenuMn","T"},
        {"windowMenuMn","W"},
        {"helpMenuMn","H"},
        {"newItemMn","N"},
        {"openItemMn","O"},
        {"saveItemMn","S"},
        {"exitItemMn","X"},
        {"metalItemMn","M"},
        {"motifItemMn","O"},
        {"windowsItemMn","W"},
        {"learningItemMn","L"},
        {"cascadeItemMn","C"},
        {"tileItemMn","T"},
        {"helpItemMn","H"},
        {"aboutItemMn","A"},
 
        {"operationError","Operation Error"},        
        {"oVariableAlreadyIsArgumentError","Ord. Variable already is argument of this node!"},
        {"properties","Properties..."},
        {"nameException","Name Error"},
        {"nameDuplicated", "Name alredy exists..."}, 
        {"siglaError","The sigla must have only letters and numbers."},
        {"descriptionError","The description must have only letters and numbers."}, 
        
        /* FormulaTreeConstructionException */
        {"notOperator", "Operator don't acept in this position"},  
		
		{"sucess", "Sucess"}, 
		{"error", "Error"},
        
		/* Tips for buttons of the table edition */
		{"deleteTip", "Delet selected text"}, 
		{"anyTip", "Insert statement \"If any\""}, 
		{"allTip", "Insert statement \"If all\""}, 
		{"elseTip", "Insert \"else\""}, 
		{"equalTip", "Insert equal operator"}, 
		{"andTip", "Insert statement AND"}, 
		{"orTip", "Insert statement OR"}, 
		{"notTip", "Insert statement NOT"}, 
		{"cadinalityTip", "Insert statement CARDINALITY"}, 
		{"maxTip", "Insert statement MAX"}, 
		{"minTip", "Insert statement MIN"}, 
		{"saveTip", "Save the table"}, 
		{"statesTip", "Show states of the node"}, 
		{"fatherTip", "Show fathers of the node"}, 
		{"argTip", "Show arguments of the node"}, 
		{"exitTip", "Exit whithout save"}, 
		
		/* Exceptions MEBN */
		{"withoutMFrag", "Don't exists any MFrag"}, 
		{"edgeInvalid", "Edge Invalid"}, 
		
		/* Edition of states */
		{"insertBooleanStates", "Insert boolean states"}, 
		{"categoryStatesType", "Insert category states"}, 
		{"objectStatesType", "Insert object entity states"}, 
		{"booleanStatesType", "Insert boolean states"}, 
		{"addState", "Add state(s)"}, 
		{"removeState", "Remove state(s)"}, 
		{"confirmation", "Confirmation"}, 
		{"warningDeletStates", "Os estados anteriores serão removidos. Tem certeza que deseja realizar a operação?"}
		
		
	};
}