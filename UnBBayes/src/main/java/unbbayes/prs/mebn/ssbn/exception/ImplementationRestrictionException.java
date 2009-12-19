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
package unbbayes.prs.mebn.ssbn.exception;

import java.util.ResourceBundle;

/**
 * The situation don't is treat for this implementation of the algorithm  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ImplementationRestrictionException extends Exception{

	private static ResourceBundle resource = 
		ResourceBundle.getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
	//Possible Restrictions
	public final static String ONLY_ONE_OVINSTANCE_FOR_OV = resource.getString("OnlyOneOVInstanceForOV"); 
	public final static String MORE_THAN_ONE_CTXT_NODE_SEARCH = resource.getString("MoreThanOneContextNodeSearh"); 
	public final static String NO_CONTEXT_NODE_FATHER = resource.getString("NoContextNodeFather"); 
	public final static String INVALID_CTXT_NODE_FORMULA = resource.getString("InvalidContextNodeFormula"); 
	public final static String ONLY_ONE_OV_FAULT_LIMIT = resource.getString("OrdVariableProblemLimit"); 
	public final static String MORE_THAN_ONE_ORDEREABLE_VARIABLE = resource.getString("MoreThanOneOrdereableVariable"); 
	public final static String RV_NOT_RECURSIVE = resource.getString("RVNotRecursive"); 
	public final static String MORE_THAN_ONE_VALUE_FOR_OV_OF_SEARCH_FORMULA = resource.getString("MoreThanOneValueForOVSearchFormula"); 
	
	public ImplementationRestrictionException(){
		super();
	}
	
	public ImplementationRestrictionException(String msg){
		super(msg); 
	}
	
}
