package unbbayes.prs.mebn.ssbn;

import java.util.List;
import java.util.ResourceBundle;

import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;


public class SSBNWarning {

	private SSBNNode nodeCause; 
	private Exception exception; 
	
	private Object[] arguments; 
	private int code; 
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");
	
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
		
		explanation.append("SSBNNode:" + nodeCause.getName() + "\n");
		
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
				explanation.append("Input Node in MFrag:" + ((ResidentNode)arguments[0]).getMFrag() + "\n");
				explanation.append("Resident Node Child:" + ((ResidentNode)arguments[0]));
			}
			break; 		
			
		default: 
			break; 
		}
		
		return explanation.toString(); 
	}
	
}
