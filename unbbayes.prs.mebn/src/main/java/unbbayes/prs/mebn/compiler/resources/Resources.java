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
package unbbayes.prs.mebn.compiler.resources;

import java.util.ListResourceBundle;

/**
 * @author Laecio Lima dos Santos
 * @version 1.0
 * @since 12/04/2006
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
	{	{"NoDefaultDistributionDeclared","A default distribution (else clause) must be declared within table."}, 
		{"InvalidConditionantFound","An invalid conditionant was found within the table declaration."},
		{"InvalidProbabilityRange","The probability distribution is invalid (the sum must be 1)."},
		{"SomeStateUndeclared","All possible states of this node must have an associated probability."},
		{"UnexpectedTokenFound","Unexpected token found."},
		{"TableUndeclared","A table was not declared."},
		{"FatalError","Fatal Error: CPT Pseudocode Compiler was badly implemented."},
		{"SSBNInstanceFailure","SSBN generation failed to properly create nodes."},
		{"NonUserDefinedVariablesFoundBeforeIfClause","Only assignments to user-defined variables (instead of states of nodes) are permitted before a nested if-clause."},
		{"NonDeclaredVarStateAssignment","Variables or states must be used in right-side of assignments only after properly declared/initialized."},
	};
}
