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
package unbbayes.prs.mebn.exception;

import java.util.ResourceBundle;

/**
 * A cycle found in the partial order of the Resident Nodes. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 (05/27/2007)
 */

/** Load resource file from this package */

public class CycleFoundException extends Exception{

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.prs.mebn.resources.Resources");  		
	
	private CycleFoundException(String msg){
		super(msg); 
	}
	
	public CycleFoundException(){
		super(resource.getString("CycleFoundException")); 
	}
	
}
