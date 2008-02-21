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
package unbbayes.controller.resources;

import java.util.*;

/**
 * <p>Title: UnBBayes</p>
 * <p>Description: Resources file for unbbayes.controller package. Localization = english.</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: UnB</p>
 * @author Rommel Novaes Carvalho, Michael Onishi
 * @version 1.0
 * @since 05/04/2002
 */

public class ControllerResources extends ListResourceBundle {

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
	{	{"imageFileFilter","Image (*.gif)"},
		{"likelihoodName","Likelihood"},
		{"likelihoodException","There are only zeros!"},
		{"statusEvidenceProbabilistic","Total Evidence Probabilistic: "},
		{"statusEvidenceException","Evidences are not consistancy or underflow"},
		{"statusError","Error!"},
		{"printLogToolTip","Print compilation log"},
		{"previewLogToolTip","Print preview"},
		{"okButtonLabel"," Ok "},
		{"statusTotalTime","Total Time: "},
		{"statusSeconds"," seconds"},
		{"stateProbabilisticName","State "},
		{"stateDecisionName","Action "},
		{"stateUtilityName","Utility "},
		{"firstStateProbabilisticName","State 0"},
		{"firstStateDecisionName","Action 0"},
		{"nodeName","Node: "},
		
		{"probabilisticNodeName","C"},
		{"decisionNodeName","D"},
		{"utilityNodeName","U"},
		{"contextNodeName","CX"},
		{"residentNodeName","RX"},
		{"inputNodeName","IX"},
		{"ordinaryVariableName", "OX"}, 
		{"entityName", "EX"}, 			
		
		{"domainMFragName","DMFrag"},	
		{"findingMFragName","FMFrag"},				
		
		{"potentialTableException","It is not a number!"},
		{"copiedNodeName","Copy of"},
		{"askTitle","Type a title for the net"},
		{"informationText","Information"},
		{"printException","Print Error: "},
		{"loadNetException","Error to load net file"},
		{"cancelOption","Cancel"},
		{"printerStatus","Printer Status"},
		{"initializingPrinter","Initializing printer..."},
		{"printingPage","Printing page "},
		{"previewButtonLabel","Preview"},
		{"nextButtonLabel","Next"},
		{"fitToPageButtonLabel","Fit to Page"},
		{"loading","Loading "},
		{"cancel","Cancel"},
		{"of"," of "},
		
		/* Exceptions MEBN */
		{"withoutMFrag", "Don't exists any MFrag"}, 
		{"edgeInvalid", "Edge Invalid"}, 

		{"JAXBExceptionFound", "Sintaxe error..."},
		
		/* Numeric attribute node */
		{"mean", "Mean"},
		{"stdDev", "Standard Dev"}, 
				
		/* load/save */
		{"saveSucess", "File save!"},
		{"mebnDontExists", "Opera��o falhou: N�o h� MEBN ativa"}, //TODO Translate
		{"bnDontExists", "Opera��o falhou: N�o h� Rede Bayesiana ativa"},
		{"mebnDontExists", "Opera��o falhou: N�o h� MSBN ativa"},
		{"sucess", "Sucess"}, 
		{"error", "Error"},
		{"withoutPosfixe", "Type of file don't informed!"}
		
	};
}
