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

import java.util.List;

/**
 * The Context node can't be evaluate because don't have all necessary
 * information about its arguments. 
 * 
 * @author Laecio
 *
 */
public class IncompleteInformationException extends Exception{

	private List<String> ovFaultList; 
	
	public IncompleteInformationException(List<String> ovFaultList){
		super(); 
		this.ovFaultList = ovFaultList; 
	}
	
    public IncompleteInformationException(String msg, List<String> ovFaultList){
		super(msg); 
		this.ovFaultList = ovFaultList; 
	}

	public List<String> getOvFaultList() {
		return ovFaultList;
	}
	
}
