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
	{	{"fileDirectoryType","Directory"},
		{"fileARFFType","Arquivo Arff"},
		{"fileTXTType","Text File TXT"},
		{"fileNETType","Baysian Netwotk File NET"},
		{"fileGenericType","Generic File"},
		{"unbbayesTitle","UnBBayes"},
		{"newToolTip","New net"},
		{"openToolTip","Open net"},
		{"saveToolTip","Save net"},
		{"learningToolTip","Learning mode"},
		{"metalToolTip","Metal Look And Feel"},
		{"motifToolTip","Motif Look And Feel"},
		{"windowsToolTip","Windows Look And Feel"},
		{"tileToolTip","Organize windows in tile"},
		{"cascadeToolTip","Organize windows in cascade"},
		{"netFileFilter","Net (.net)"},
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
		{"statusReadyLabel","Ready"},
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
		{"descriptionLabel","Description:"},
		{"compileToolTip","Compile junction tree"},
		{"moreToolTip","Add state"},
		{"lessToolTip","Remove state"},
		{"arcToolTip","Insert arc"},
		{"probabilisticNodeInsertToolTip","Insert probabilistic variable"},
		{"decisionNodeInsertToolTip","Insert decision variable"},
		{"utilityNodeInsertToolTip","Insert utility variable"},
		{"selectToolTip","Select various nodes and arcs"},
		{"printTableToolTip","Print table"},
		{"previewTableToolTip","Print table preview"},
		{"saveTableImageToolTip","Save table as a gif image"},
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
        {"newItem","New..."},
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
        {"aboutItemMn","A"}
	};
}