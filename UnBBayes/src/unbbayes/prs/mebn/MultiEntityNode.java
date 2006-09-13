package unbbayes.prs.mebn;

import java.util.List;

import unbbayes.prs.Node;

public class MultiEntityNode extends Node {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = -5435895970322752281L;

	private List<Argument> argumentList;
	 
	private List<MultiEntityNode> innerTermOfList;
	 
	private List<MultiEntityNode> innerTermFromList;

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	 
}
 
