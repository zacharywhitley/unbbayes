package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super(); 
		
		this.name = name; 
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
	}
	
	public void addResidentNodeFather(DomainResidentNode father){
		residentNodeFatherList.add(father); 
	}
	
	public void addInputNodeFather(GenerativeInputNode father){
		inputNodeFatherList.add(father); 
	}	 
	
	public void addInputInstanceFromList(GenerativeInputNode instance){
		inputInstanceFromList.add(instance); 
	}
	
	
	public DomainMFrag getMFrag(){
		return mFrag; 
	}
	
	public List<DomainResidentNode> getResidentNodeFatherList(){
		return this.residentNodeFatherList; 
	}
	
	public List<GenerativeInputNode> getInputNodeFatherList(){
		return this.inputNodeFatherList; 
	}	
	
	public List<DomainResidentNode> getResidentNodeChildList(){
		return this.residentNodeChildList; 
	}
	
	public List<GenerativeInputNode> getInputInstanceFromList(){
		return this.inputInstanceFromList; 
	}	
	
	
	
	
}
 
