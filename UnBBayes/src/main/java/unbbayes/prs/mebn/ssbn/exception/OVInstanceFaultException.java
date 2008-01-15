package unbbayes.prs.mebn.ssbn.exception;

import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;

public class OVInstanceFaultException extends Exception{

	private List<OrdinaryVariable> ovFaultList; 
	
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
