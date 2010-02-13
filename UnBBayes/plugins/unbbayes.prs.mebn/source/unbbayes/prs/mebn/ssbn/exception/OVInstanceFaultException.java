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

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * The evaluation is aborted because a ov instance for one ordinary variable
 * don't was found. 
 * 
 * @author Laecio Santos (laecio@gmail.com)
 */
public class OVInstanceFaultException extends Exception{

	private List<OrdinaryVariable> ovFaultList; 

	public OVInstanceFaultException(OrdinaryVariable ovFault){
		super(); 
		this.ovFaultList = new ArrayList<OrdinaryVariable>();
		ovFaultList.add(ovFault); 
	}
	
	public OVInstanceFaultException(List<OrdinaryVariable> ovFaultList){
		super(); 
		this.ovFaultList = ovFaultList; 
	}
	
    public OVInstanceFaultException(String msg, List<OrdinaryVariable> ovFaultList){
		super(msg); 
		this.ovFaultList = ovFaultList; 
	}

	public List<OrdinaryVariable> getOvFaultList() {
		return ovFaultList;
	}
	
}
