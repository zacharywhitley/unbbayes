package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.Separator;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * Linkage tree separating two adjacents sub-networks.
 * @author Michael S. Onishi
 */
public class Linkage {
	private SubNetwork n1, n2;
	private NodeList nodes;
	
	private JunctionTree jt;
	private ArrayList linkList;
	
	/**
	 * Creates a new Linkage separating the two subnetworks.
	 * 
	 * @param n1 The parent sub-network.
	 * @param n2 The child sub-network
	 */
	public Linkage(SubNetwork n1, SubNetwork n2) {
		this.n1 = n1;
		this.n2 = n2;
		linkList = new ArrayList();
		n1.addAdjacent(n2);
		n2.setParent(n1);
		nodes = SetToolkit.intersection(n1.getNos(), n2.getNos());		
	}
	
	/**
	 * Must be called after the Junction Tree creation
	 */
	protected void makeLinkageTree() throws Exception {
		linkList.clear();
		jt = new JunctionTree();
		Clique raiz = (Clique) n1.getJunctionTree().getCliques().get(0);
		makeCliqueList(raiz);
		
		remove1stPass();
		
		remove2ndPass();
		
		raiz = (Clique) jt.getCliques().get(0); 
		
		assert raiz.getParent() == null;		
		
		assignV1();	
			
		
		// DEBUG-------------------
		for (int i = linkList.size()-1; i >=0; i--) {
			System.out.println("-------");			
			System.out.println(linkList.get(i));
			System.out.println("-------");			
		}
		System.out.println();
		// DEBUG-------------------
		
		initTables();
		System.out.println();
	}
	
	
	private void remove1stPass() {
		boolean retirou = true;
		while (retirou) {
			retirou = false;
			for (int i = linkList.size()-1; i>=0; i--) {
				Link link = (Link) linkList.get(i);
				Clique c = link.getClique();
				if (c.getChildrenSize() == 0) {
					// c is tree leaf					
					if (c.getNos().size() == 0) {
						removeLink(link);
						retirou = true;
					} else {
						if (c.getParent() != null && c.getParent().getNos().containsAll(c.getNos())) {
							removeLink(link);
							retirou = true;
						}	
					}
				}
			}
		}
	}
	
	private void remove2ndPass() {
		for (int i = linkList.size()-1; i >=0; i--) {
			Link link = (Link) linkList.get(i);
			Clique c = link.getClique();
			if (c.getParent() != null && c.getParent().getNos().containsAll(c.getNos())) {
				removeLink(link);
				continue;																
			}
			
			for (int j = c.getChildrenSize()-1; j>=0; j--) {
				Clique c2 = c.getChildAt(j);
				if (c2.getNos().containsAll(c.getNos())) {
					removeLink(link);
					break;					
				}					
			}
		}
	}
	
	private void assignV1() {
		List cliquesN2 = n2.getJunctionTree().getCliques();
		for (int i = linkList.size()-1; i >=0; i--) {
			Link link = (Link) linkList.get(i);
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
	
	private void initTables() throws Exception {
		insertSeparators();
				
		for (int i = linkList.size()-1; i >=0; i--) {
			Link l = (Link) linkList.get(i);
			Clique c = l.getClique();
			PotentialTable tab = c.getPotentialTable();
			for (int j = 0; j < c.getNos().size(); j++) {
				tab.addVariable(c.getNos().get(j));
			}

			for (int j = tab.tableSize()-1; j>=0; j--) {
				tab.setValue(j, 1);				
			}
		}

		
		for (int k = jt.getSeparatorsSize() - 1; k >= 0; k--) {
			Separator auxSep = (Separator) jt.getSeparatorAt(k);
			PotentialTable tab = auxSep.getPotentialTable();
			for (int c = 0; c < auxSep.getNos().size(); c++) {
				tab.addVariable(auxSep.getNos().get(c));
			}
			
			for (int j = tab.tableSize()-1; j>=0; j--) {
				tab.setValue(j, 1);				
			}
		}
	}
	
	private void insertSeparators() {
		for (int i = jt.getCliques().size()-1; i>=0; i--) {
			Clique c = (Clique) jt.getCliques().get(i);
			for (int j = c.getChildrenSize()-1; j>=0; j--) {
				//c.getChildAt(j).getNos().removeAll(c.getNos());
				Separator sep = new Separator(c, c.getChildAt(j), false);
				sep.setNos(SetToolkit.intersection(c.getNos(), c.getChildAt(j).getNos()));
				jt.addSeparator(sep);
			}
		}
	}
	
	private void removeLink(Link l) {
		linkList.remove(l);
		Clique c = l.getClique();
		jt.getCliques().remove(c);
				
		if (c.getParent() != null) {
			c.getParent().removeChild(c);						
			for (int i = c.getChildrenSize()-1; i>=0; i--) {
				Clique child = c.getChildAt(i);				
				c.getParent().addChild(child);
				child.setParent(c.getParent());
			}
		}
	}
	
	
	/**
	 * Makes the tree with DFS.
	 */ 
	private Clique makeCliqueList(Clique c) {
		Clique cliqueClone = new Clique();
		cliqueClone.getNos().addAll(SetToolkit.intersection(c.getNos(), nodes));
		Link l = new Link(cliqueClone);
		l.setV0(c);
		linkList.add(l);
		jt.getCliques().add(cliqueClone);
		for (int i = c.getChildrenSize()-1; i>=0; i--) {
			Clique c2 = makeCliqueList(c.getChildAt(i));
			c2.setParent(cliqueClone);
			cliqueClone.addChild(c2);
		}
		return cliqueClone;
	}
	
	
	/**
	 * Returns the parent subnetwork of this Linkage.
	 * @return the parent subnetwork of this Linkage.
	 */
	public SubNetwork getN1() {
		return n1;
	}

	/**
	 * Returns the child subnetwork of this Linkage.
	 * @return the child subnetwork of this Linkage.
	 */
	public SubNetwork getN2() {
		return n2;
	}
	
	
	protected void absorve(boolean naOrdem) throws Exception {
		int treeSize = linkList.size();
		for (int i = 0; i < treeSize; i++) {		
			Link l = (Link) linkList.get(i);
			l.absorveIn(naOrdem);
		}
		
		jt.consistencia();
		
		for (int i = 0; i < treeSize; i++) {
			Link l = (Link) linkList.get(i);
			l.absorveOut(naOrdem);
		}

		if (naOrdem) {
			n1.getJunctionTree().consistencia();
			n1.updateMarginais();
		} else {
			n2.getJunctionTree().consistencia();
			n2.updateMarginais();
		}
	}
}
