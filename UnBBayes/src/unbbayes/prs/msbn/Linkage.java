package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
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
public class Linkage {
	private SubNetwork n1, n2;
	private NodeList nodes;
	private List tree;
	
	public Linkage(SubNetwork n1, SubNetwork n2) {
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
		
		remove1stPass();
		
		remove2ndPass();
		
		assignV1();
		
		initTables();		
		
		
		// DEBUG-------------------
		for (int i = tree.size()-1; i >=0; i--) {
			Link link = (Link) tree.get(i);
			Clique c = link.getClique();
			for (int j = c.getNos().size()-1; j>=0;j--) {
				System.out.print(c.getNos().get(j) + " ");				
			}
			System.out.println();
		}
		System.out.println();
		// DEBUG-------------------	
	}
	
	
	private void remove1stPass() {
		boolean retirou = true;
		while (retirou) {
			retirou = false;
			for (int i = tree.size()-1; i>=0; i--) {
				Link link = (Link) tree.get(i);
				Clique c = link.getClique();
				if (c.getChildrenSize() == 0) {
					// tree leaf
					NodeList inter = SetToolkit.intersection(c.getNos(), nodes);
					if (inter.size() == 0) {
						removeLink(link);
						retirou = true;
					} else {
						for (int j = tree.size()-1; j>=0; j--) {
							Link link2 = (Link) tree.get(j);
							Clique c2 = link2.getClique();
							if (i != j && c2.getNos().containsAll(inter)) {
								removeLink(link);
								retirou = true;
								break;								
							}
						}		
					}
				}
			}
		}		
	}
	
	private void remove2ndPass() {
		for (int i = tree.size()-1; i >=0; i--) {
			Link link = (Link) tree.get(i);
			Clique c = link.getClique();
			c.getNos().retainAll(nodes);
			for (int j = tree.size()-1; j>=0; j--) {
				if (i != j) {
					Link link2 = (Link) tree.get(j);
					Clique c2 = link2.getClique();
					if (c2.getNos().containsAll(c.getNos())) {
						removeLink(link);
						break;					
					}					
				}				
			}
		}	
	}
	
	private void assignV1() {
		List cliquesN2 = n2.getJunctionTree().getCliques();
		for (int i = tree.size()-1; i >=0; i--) {
			Link link = (Link) tree.get(i);
			Clique c = link.getClique();
			for (int j = cliquesN2.size()-1; j>=0; j--) {
				Clique c2 = (Clique) cliquesN2.get(j);
				if (c2.getNos().containsAll(c.getNos())) {
					link.setV1(c2);
					break;
				}
			}
		}
	}
	
	private void initTables() {
		for (int i = tree.size()-1; i >=0; i--) {
			Link link = (Link) tree.get(i);
			Clique c = link.getClique();
			PotentialTable tab = c.getPotentialTable();
			for (int j = c.getNos().size()-1; j>=0; j--) {
				tab.addVariable(c.getNos().get(j));
			}
			
			for (int j = tab.tableSize()-1; j>=0; j--) {
				tab.setValue(j, 1);
			}
		}
	}
	
	private void removeLink(Link l) {
		tree.remove(l);
		Clique c = l.getClique();
		if (c.getParent() != null) {
			c.getParent().removeChild(c);
			c.setParent(null);
		}
	}
	
	
	/**
	 * Makes the tree with DFS.
	 */ 
	private Clique makeCliqueList(Clique c) {
		Clique cliqueClone = new Clique();
		cliqueClone.getNos().addAll(c.getNos());
		Link l = new Link(cliqueClone);
		l.setV0(c);
		tree.add(l);
		for (int i = c.getChildrenSize()-1; i>=0; i--) {
			Clique c2 = makeCliqueList(c.getChildAt(i));
			c2.setParent(cliqueClone);
			cliqueClone.addChild(c2);
		}
		return cliqueClone;
	}
	
	
	/**
	 * Returns the n1.
	 * @return SubNetwork
	 */
	public SubNetwork getN1() {
		return n1;
	}

	/**
	 * Returns the n2.
	 * @return SubNetwork
	 */
	public SubNetwork getN2() {
		return n2;
	}
	
	
	protected void absorve(boolean naOrdem) {
		int treeSize = tree.size();
		for (int i = 0; i < treeSize; i++) {
			Link l = (Link) tree.get(i);
			if (naOrdem) {
				absorve(l.getV0(), l.getClique(), l.getV1());				
				n1.getJunctionTree().distribuaEvidencia(l.getV0());
			} else {				
				absorve(l.getV1(), l.getClique(), l.getV0());
				n2.getJunctionTree().distribuaEvidencia(l.getV1());
			}
		}
	}
	
	protected static void absorve(Clique c1, Clique l, Clique c2) {
		NodeList toDie = SetToolkit.clone(c2.getNos());
		toDie.removeAll(l.getNos());
		
		PotentialTable tB =
			(PotentialTable) c2.getPotentialTable().clone();
			
		for (int i = 0; i < toDie.size(); i++) {
			tB.removeVariable(toDie.get(i));
		}	

		toDie = SetToolkit.clone(c1.getNos());
		toDie.removeAll(l.getNos());

		PotentialTable tA = (PotentialTable) c1.getPotentialTable().clone();
		
		for (int i = 0; i < toDie.size(); i++) {
			tA.removeVariable(toDie.get(i));
		}
		
		tB.opTab(tA, PotentialTable.DIVISION_OPERATOR);
		
		c1.getPotentialTable().opTab(tB, PotentialTable.PRODUCT_OPERATOR);
	}
}
