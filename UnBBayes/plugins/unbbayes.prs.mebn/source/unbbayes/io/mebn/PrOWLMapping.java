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

package unbbayes.io.mebn;

import java.util.ArrayList;
import java.util.List;

/**
 * This class mapping the PrOwl model to the java source. It should be 
 * sincronized with the current pr-owl version utilized by the code.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class PrOWLMapping {

	//names of the classes in PR_OWL definition FIle, which are used inside UnBBayes IO:
	
	public static final String ARGUMENT_RELATIONSHIP = "ArgRelationship";
	public static final String BUILTIN_RV = "BuiltInRV";
	public static final String BOOLEAN_STATE = "BooleanRVState";
	public static final String CATEGORICAL_STATE = "CategoricalRVState"; 
	public static final String CONTEXT_NODE = "Context";
	public static final String DECLARATIVE_PROBABILITY_DISTRIBUTION = "DeclarativeDist";		
	public static final String DOMAIN_MFRAG = "Domain_MFrag";
	public static final String DOMAIN_RESIDENT = "Domain_Res";
	public static final String GENERATIVE_INPUT = "Generative_input";
	public static final String META_ENTITY = "MetaEntity"; 
	public static final String MTHEORY = "MTheory";	
	public static final String OBJECT_ENTITY = "ObjectEntity"; 	
	public static final String ORDINARY_VARIABLE = "OVariable";
	public static final String SIMPLE_ARGUMENT_RELATIONSHIP = "SimpleArgRelationship";
	
	
	// Names of meta entities' individuals native to pr-owl definition
	public static final String BOOLEAN_LABEL = "Boolean";
	public static final String TYPE_LABEL = "TypeLabel";
	public static final String CATEGORY_LABEL = "CategoryLabel";
	
	public static List<String> getReservedWords(){
		
		List<String> lst = new ArrayList<String>();
		
		lst.add(ARGUMENT_RELATIONSHIP); 
		lst.add(BUILTIN_RV); 
		lst.add(BOOLEAN_STATE); 
		lst.add(CATEGORICAL_STATE); 
		lst.add(CONTEXT_NODE); 
		lst.add(DECLARATIVE_PROBABILITY_DISTRIBUTION); 
		lst.add(DOMAIN_MFRAG); 
		lst.add(DOMAIN_RESIDENT); 
		lst.add(GENERATIVE_INPUT); 
		lst.add(META_ENTITY); 
		lst.add(MTHEORY); 
		lst.add(OBJECT_ENTITY); 
		lst.add(ORDINARY_VARIABLE); 
		lst.add(SIMPLE_ARGUMENT_RELATIONSHIP); 
		
		return lst; 
		
	}
	
}
