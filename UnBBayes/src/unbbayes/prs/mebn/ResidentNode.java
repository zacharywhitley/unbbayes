package unbbayes.prs.mebn;

import java.util.List;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.PotentialTable;

public class ResidentNode extends MultiEntityNode implements ITabledVariable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8497908054569004909L;

	private MFrag mFrag;
	 
	private List<InputNode> inputNodeFatherList;
	 
	private List<InputNode> inputInstanceFromList;
	 
	private ProbabilisticTable probabilisticTable;
	 
	private List<ResidentNode> residentNodeFatherList;
	 
	private List<ResidentNode> residentNodeChildList;
	 
	/**
	 *@see unbbayes.prs.bn.ITabledVariable#getPotentialTable()
	 */
	public PotentialTable getPotentialTable() {
		return null;
	}
	 
}
 
