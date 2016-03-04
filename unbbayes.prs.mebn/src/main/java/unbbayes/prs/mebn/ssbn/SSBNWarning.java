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

import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * A warning is a situation that don't stop the evaluation of the SSBN algorithm. 
 * The process of generation of algorithm should have some warnings (situations
 * that don't is a error but is important communicate to the user). 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class SSBNWarning {

	private SSBNNode nodeCause; 
	private Exception exception; 
	
	private Object[] arguments; 
	private int code; 
	
	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
	//List of codes
	
	/**
	 * Arguments: 
	 * - List<OrdinaryVariable>
	 */
	public final static int ENTYTY_FAULT = 0x0001; 
	
	/**
	 * Arguments:
	 * - InputNode 
	 */
     public final static int OV_FAULT_EVALUATION_OF_CONTEXT_FOR_INPUT_INSTANCE = 0x0002; 
	
 	/**
 	 * Arguments:
 	 * - ResidentNode 
 	 */
      public final static int OV_FAULT_RESIDENT_CHILD = 0x0003; 
	
	public SSBNWarning(int code, Exception e, SSBNNode nodeCause, Object... args){
		this.code = code; 
		this.exception = e; 
		this.nodeCause = nodeCause;
		this.arguments = args; 
	}
	
	public SSBNNode getNodeCause() {
		return nodeCause;
	}
	
	public String getExplanation(){
		
		switch(code){
		
		case ENTYTY_FAULT:
			return resource.getString("OVProblem");
		
		case OV_FAULT_EVALUATION_OF_CONTEXT_FOR_INPUT_INSTANCE:
			return resource.getString("contextInputNodeProblem");
		
		case OV_FAULT_RESIDENT_CHILD:
			return resource.getString("contextInputNodeProblem");
			
		default: 
				return ""; 
		
		}
	}
	
	public String getDetalhedExplanation(){
		
		StringBuilder explanation = new StringBuilder();
		
		switch(code){

		case ENTYTY_FAULT:
			if(arguments.length > 0){
				explanation.append("Ordinary Variables:" + (List<OrdinaryVariable>)arguments[0]);
			}
			break; 
		
		case OV_FAULT_EVALUATION_OF_CONTEXT_FOR_INPUT_INSTANCE:
			if(arguments.length > 0){
				explanation.append("Input Node in MFrag:" + ((InputNode)arguments[0]).getMFrag());
			}
			break; 			
			
		case OV_FAULT_RESIDENT_CHILD:
			if(arguments.length > 0){
				explanation.append("Input Node in MFrag:" + ((IResidentNode)arguments[0]).getMFrag() + "\n");
				explanation.append("Resident Node Child:" + ((IResidentNode)arguments[0]));
			}
			break; 		
			
		default: 
			break; 
		}
		
		return explanation.toString(); 
	}
	
}
