package unbbayes.prs.mebn;

public class FindingMFrag extends MFrag {
 
	private FindingInputNode findingInputNode;
	 
	private FindingResidentNode findingResidentNode;
	
	public FindingMFrag(String name, MultiEntityBayesianNetwork mebn) {
		super(name, mebn);
		mebn.addFindingMFrag(this);
	}
}
 
