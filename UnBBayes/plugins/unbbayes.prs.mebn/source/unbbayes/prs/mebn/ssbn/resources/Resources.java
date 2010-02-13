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
package unbbayes.prs.mebn.ssbn.resources;

import java.util.*;

/**
 * @author Shou Matsumoto
 * @author Laecio Lima dos Santos
 * 
 * @version 1.0
 * @since September, 11, 2007
 */

public class Resources extends ListResourceBundle {

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
	{	{"ArgumentTypeMismatch","Resident node was not expecting this type as argument. Check argument order."}, 
		{"PossibleValueMismatch","The possible value expected was not matching."},
		{"CycleFound","A cycle was found when creating SSBN."},
		{"UnknownException","An unknown error has occurred. We suggest you to store your work and close the program"},
		{"NoNetworkDefined","No Probabilistic Network was defined. Create it first."},
		{"DefaultNetworkName","AutomaticallyCreatedNet"},
		{"IncompatibleNetworks","Parents and childs belong to different networks"},
		{"RecursiveLimit","Recursivity limit has been overlapped"}, 
		{"OrdVariableProblemLimit","For this implementation only one ord. variable search is possible"}, 
		{"ContextNodeSearchDontFound","Search context node dont found"}, 
		{"MoreThanOneOrdereableVariable","More than one ordinary variable was found for the recursive resident node. This implementation only works with one recursive variable."},
		{"MoreThanOneContextNodeFather", "A node can not have more than one context node as father!"},
		{"NoContextNodeFather", "Context node necessary for search not found!"},
		{"RVNotRecursive","The resident node is not recursive because it does not have ordereable ordinary variable. Cycle found!"}, 	
		{"InvalidContextNodeFormula","Invalid Context node Formula"}, 
		{"FindingNotHaveParent","An finding don't should have a parent"}, 
		{"IncompleteInformation","Impossible find variables that validate the context nodes"},
		{"InternalError","Internal Error"},
		{"InconsistencyError","Inconsistenty found"},
		{"TwoContextFathersError", "One node couldn't have two context nodes fathers!"}, 
		{"OnlyOneFreeVariableRestriction", "The search context node can not have more than one free variable!"}, 
		{"NotNodeInSSBN", "Not have nodes at the SSBN. "},
		{"MaxNodeLimit", "Number max of nodes created in ssbn generation"},
				
		//ImplementationRestrictionException
		{"OnlyOneOVInstanceForOV", "Should have only one instance for each ordinary variable."},
		{"MoreThanOneContextNodeSearh","More then one context node search found for the ord. variable. This implementation treat only the trival case of one node"}, 
		{"MoreThanOneValueForOVSearchFormula","More than one value for ordinary variable used in a search formula."}, 
		
		//SSBNWarning
		{"OVProblem", "Entities for variables ordinaries don't should be determinated"}, 
		{"contextInputNodeProblem", "Evaluation of context nodes fail because don't found all entities that match the ordinary variables."}, 
		{"OVProblemResidentChild", "Entity instance fault for evaluation the resident node child"}, 
		
		{"_", "End"} 
		
		
	};
}
