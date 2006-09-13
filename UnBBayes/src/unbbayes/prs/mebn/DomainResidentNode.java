package unbbayes.prs.mebn;

import java.util.List;

public class DomainResidentNode extends ResidentNode {
 
	private List<GenerativeInputNode> inputInstanceFromList;
	
	private List<GenerativeInputNode> inputNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeFatherList;
	 
	private List<DomainResidentNode> residentNodeChildList;
	 
	private DomainMFrag mFrag;
	 
}
 
