package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Link {
	private SubNetwork n1, n2;
	private NodeList nodes;
	private JunctionTree tree;
	
	public Link(SubNetwork n1, SubNetwork n2) {
		this.n1 = n1;
		this.n2 = n2;
		n1.addAdjacent(n2);
		n2.setParent(n1);
		nodes = SetToolkit.intersection(n1.getNos(), n2.getNos());
	}
	
	/**
	 * Must be called after the Junction Tree creation
	 */
	protected void makeLinkageTree() {
		tree = (JunctionTree) n1.getJunctionTree().clone();
		boolean retirou = true;
		while (retirou) {
			retirou = false;
			for (int i = tree.getCliques().size()-1; i>=0; i--) {
				Clique c = (Clique) tree.getCliques().get(i);
				if (c.getChildrenSize() == 0) {
					NodeList inter = SetToolkit.intersection(c.getNos(), nodes);
										
				}		
			}			
		}
	}
}
