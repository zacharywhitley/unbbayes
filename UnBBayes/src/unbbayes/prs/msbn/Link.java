package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

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
	private List tree;
	
	public Link(SubNetwork n1, SubNetwork n2) {
		this.n1 = n1;
		this.n2 = n2;
		n1.addAdjacent(n2);
		n2.setParent(n1);
		nodes = SetToolkit.intersection(n1.getNos(), n2.getNos());
		tree = new ArrayList();
	}
	
	/**
	 * Must be called after the Junction Tree creation
	 */
	protected void makeLinkageTree() {
		tree.clear();		
			
		Clique raiz = (Clique) n1.getJunctionTree().getCliques().get(0);
		makeCliqueList(raiz);	
		
		boolean retirou = true;
		while (retirou) {
			retirou = false;
			for (int i = tree.size()-1; i>=0; i--) {
				Clique c = (Clique) tree.get(i);
				if (c.getChildrenSize() == 0) {
					NodeList inter = SetToolkit.intersection(c.getNos(), nodes);
					if (inter.size() == 0) {						 	
						tree.remove(i);
						retirou = true;
					} else {
						for (int j = tree.size()-1; j>=0; j--) {
							Clique c2 = (Clique) tree.get(j);
							if (i != j && c2.getNos().contains(inter)) {
								tree.remove(i);
								retirou = true;
								break;								
							}
						}						
					}			
				}
			}
		}
		
		for (int i = tree.size()-1; i >=0; i--) {
			Clique c = (Clique) tree.get(i);
			c.getNos().removeAll(nodes);
			for (int j = tree.size()-1; j>=0; j--) {
				if (i != j) {
					Clique c2 = (Clique) tree.get(j);
					if (c2.getNos().contains(c.getNos())) {
						tree.remove(i);						
					}					
				}				
			}
		}
	}
	
	
	/**
	 * Makes the tree with DFS.
	 */ 
	private void makeCliqueList(Clique c) {
		tree.add(c.clone());
		for (int i = c.getChildrenSize()-1; i>=0; i--) {
			makeCliqueList(c.getChildAt(i));			
		}		
	}
}
