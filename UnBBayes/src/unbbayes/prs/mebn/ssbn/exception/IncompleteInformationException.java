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
