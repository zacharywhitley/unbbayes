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

package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * This class is used for the management of the arguments of a ssbnnode 
 * build for a InputNode. This is necessary because the arguments of this
 * ssbn node need to be interchangeable (should refer to the ordinary
 * variables of the MFrag of the ssbn node when the evaluation is of the 
 * input how father of a resident node and to the ordinary variables of the 
 * MFrag where the node is residente if the evaluation is about the 
 * ssbnnode itself). 
 */
public class SSBNNodeJacket{
	
	private final Integer ARGUMENTS_OF_INPUT_MFRAG = 0; 
	private final Integer ARGUMENTS_OF_RESIDENT_MFRAG = 1; 
	
	private SSBNNode ssbnNode; 
	private List<OVInstance> ovInstancesOfInputMFrag; 
	private List<OVInstance> ovInstancesOfResidentMFrag; 
	private Integer typeAtualArguments; 
	
	public SSBNNodeJacket(SSBNNode ssbnNode){
		this.ssbnNode = ssbnNode;
		this.typeAtualArguments = -1; 
		ovInstancesOfInputMFrag = new ArrayList<OVInstance>();
		ovInstancesOfResidentMFrag = new ArrayList<OVInstance>(); 
	}

	public SSBNNode getSsbnNode() {
		return ssbnNode;
	}

	public void setSsbnNode(SSBNNode ssbnNode) {
		this.ssbnNode = ssbnNode;
	}

	public List<OVInstance> getInputMFragOvInstances() {
		return ovInstancesOfInputMFrag;
	}

	public void addOVInstanceOfInputMFrag(OVInstance ovInstance){
		this.ovInstancesOfInputMFrag.add(ovInstance);
	}
	
	public Collection<OVInstance> getResidentMFragOvInstances() {
		return ovInstancesOfResidentMFrag;
	}

	public void addOVInstanceOfResidentMFrag(OVInstance ovInstance){
		this.ovInstancesOfResidentMFrag.add(ovInstance);
	}		
	
	public void setInputMFragArguments(){
		if(typeAtualArguments != ARGUMENTS_OF_INPUT_MFRAG){
			ssbnNode.removeAllArguments(); 
			ssbnNode.setArguments(ovInstancesOfInputMFrag);
			typeAtualArguments = ARGUMENTS_OF_INPUT_MFRAG;
		}
	}
	
	public void setResidentMFragArguments(){
		if(typeAtualArguments != ARGUMENTS_OF_RESIDENT_MFRAG){
			ssbnNode.removeAllArguments(); 
			ssbnNode.setArguments(ovInstancesOfResidentMFrag);
			typeAtualArguments = ARGUMENTS_OF_RESIDENT_MFRAG;
		}
	}

	public Integer getTypeAtualArguments() {
		return typeAtualArguments;
	}

	public void setTypeAtualArguments(Integer typeAtualArguments) {
		this.typeAtualArguments = typeAtualArguments;
	}
	
	public String toString(){
		return ssbnNode.toString() +  
		       " Input["+ ovInstancesOfInputMFrag + "]" + 
		       " Resident[" + ovInstancesOfResidentMFrag + "]";
	}
	
	public void addArgument(IResidentNode residentNode, InputNode inputNode, 
			OVInstance ovInstanceResidentMFrag){
		
		OrdinaryVariable ovResidentMFrag = ovInstanceResidentMFrag.getOv(); 
		int index = residentNode.getOrdinaryVariableIndex(ovResidentMFrag); 
		
		if(index > -1){
			addOVInstanceOfResidentMFrag(ovInstanceResidentMFrag); 
			OrdinaryVariable ovInputMFrag = inputNode.getOrdinaryVariableByIndex(index); 
			addOVInstanceOfInputMFrag(OVInstance.getInstance(ovInputMFrag, ovInstanceResidentMFrag.getEntity())); 
		}
		
	}
	
	/** 
	 * @param inputNode Node that originate the SSBNNode
	 * @param ovInstanceInputMFrag OVInstance of the input MFrag (MFrag where exists the input node)
	 * 
	 * @throws SSBNNodeGeneralException
	 */
	public void addArgument(InputNode inputNode, OVInstance ovInstanceInputMFrag) 
	            throws SSBNNodeGeneralException {
		
		IResidentNode residentNode = inputNode.getResidentNodePointer().getResidentNode(); 
		OrdinaryVariable ovInputMFrag = ovInstanceInputMFrag.getOv(); 
		int index = inputNode.getResidentNodePointer().getOrdinaryVariableIndex(ovInputMFrag);
		if(index > -1){
			addOVInstanceOfInputMFrag(ovInstanceInputMFrag); 
			OrdinaryVariable ovResidentMFrag = residentNode.getOrdinaryVariableList().get(index);
			addOVInstanceOfResidentMFrag(OVInstance.getInstance(ovResidentMFrag, ovInstanceInputMFrag.getEntity()));
		}
	}
	
}