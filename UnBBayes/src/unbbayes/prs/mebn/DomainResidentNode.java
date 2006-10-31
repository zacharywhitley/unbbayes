package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.draw.DrawEllipse;
import unbbayes.gui.draw.DrawRectangle;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	
	private DrawRectangle drawRectangle; 
	
	public DomainResidentNode(String name, DomainMFrag mFrag){
		
		super(); 
		
		this.name = name; 
		this.mFrag = mFrag; 
		
		inputInstanceFromList = new ArrayList<GenerativeInputNode>(); 
		inputNodeFatherList = new ArrayList<GenerativeInputNode>();
		residentNodeFatherList = new ArrayList<DomainResidentNode>();	
		residentNodeChildList = new ArrayList<DomainResidentNode>();	
	
    	size.x = 100;
    	size.y = 20; 
    	drawRectangle = new DrawRectangle(position, size);
        drawElement.add(drawRectangle);
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
 
