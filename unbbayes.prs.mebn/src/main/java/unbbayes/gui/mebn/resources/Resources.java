/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.gui.mebn.resources;

import java.util.ArrayList;

import unbbayes.gui.resources.GuiResources;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.gui.mebn package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Shou Matsumoto
 * @version 1.0
 * @since 02/13/2010
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031) Added turnToMTheoryViewModeToolTip
 * 
 * TODO gradually move mebn-specific resources from {@link GuiResources} to here.
 */

public class Resources extends GuiResources {
 
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
		{"defaultMEBNEditor" , "MTheory"},
		{"defaultMEBNEditorTip" , "Default MTheory Editor"},
		{"MEBNModuleName" , "Multi Entity Bayesian Network"},
		{"newMebnToolTip", "New MEBN"}, 
        {"newMEBN","New MEBN"},
        {"newMEBNMn","E"},

		/* Exceptions MEBN */
		{"withoutMFrag", "No MFrag Found"},
		
		// Entity warnings
		{"warning","Warning"},
		{"selectEntityFirst","First select a Entity that you want to add a Child"},
		{"removingEntityWarning","Removing this node will also remove all its descendants. Continue?"},
		{"removeRootWarning","Root node cannot be removed!"},
		
		//Menus MEBN
		{"menuDelete", "Delete"}, 
		{"menuAddContext", "Add Context"}, 
		{"menuAddInput", "Add Input"},
		{"menuAddResident", "Add Resident"}, 
		{"menuAddDomainMFrag", "Add MFrag"}, 
		
		// option dialog
		{"openMEBNOptions", "Open MEBN Options"}, 
		{"mebnOptionTitle", "MEBN Options"}, 
		{"kbTab", "Knowledge Base"}, 
		{"kbParameters", "Parameters for the Knowledge Base"}, 
		{"availableKB", "Available Knowledge Bases"},  
		{"defaultKB", "Default"},  
		{"availableSSBN", "SSBN generation algorithms"},  
		{"ssbnParameters", "Parameters for Situation Specific Bayesian Network generation"},
		{"ssbnTab", "SSBN Algorithm"},
		{"defaultSSBN", "Default Algorithm"}, 

		// SSBN option panel
		{"initializationCheckBoxLabel" , " Execute initialization phase "},
		{"buildCheckBoxLabel" , " Execute build phase "},
		{"pruneCheckBoxLabel" , " Execute graph pruning phase "},
		{"cptGenerationCheckBoxLabel" , " Execute CPT generation phase "},
		{"userInteractionCheckBoxLabel" , " Enable interactive mode "},
		{"pruneConfigurationBorderTitle" , " Pruning options "},
		{"barrenNodePrunerCheckBoxLabel" , " Prune barren nodes "},
		{"dseparatedNodePrunerCheckBoxLabel" , " Prune nodes d-separated from queries "},
		{"mainPanelBorderTitle" , "Choose the functionalities to be enabled"},
		{"recursiveLimitBorderTitle" , "Recursivity depth limit"},
		
		/* PLM file manager */
		/* TODO transfer it to IO package? */
		{"FileSaveOK" , "Knowledge Base was successfully saved"},
		{"FileLoadOK" , "Knowledge Base was successfully loaded"},
		{"NoSSBN" , "No previously generated SSBN found."},
		{"KBClean" , "Knowledge base successfully cleared"},
		{"NotImplemented" , "Not implemented yet."}, 
		{"loadedWithErrors" , "File loaded, but some errors may have occurred"},
		
		// MTheory view mode
		{"turnToMTheoryViewModeToolTip" , "See MTheory"},
		{"showTitleBorder" , "Show Title Border"},
		{"showBodyBorder" , "Show Body Border"},
		{"showRoundBorder" , "Show Round Border"},
		
		
		// miscellaneous messages
		{"selectOnlyOneEntry" , "Please, select only one entry"},
		
		{"enableSoftEvidence" , "Enable soft/likelihood evidence"},
		{"softEvidence" , "Soft Evidence (Jeffrey's Rule)"},
		{"likelihoodEvidence" , "Likelihood Evidence"},
		{"csvSoftEvidence" , "Please, provide the evidence (probabilities/likelihoods separated by comma)"},
		{"nonNegativeError" , "Only non-negative numbers are allowed"},

		
	};
	
}