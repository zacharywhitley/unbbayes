package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * Link representing one Clique in the linkage tree.
 * It has references to the host cliques on the two adjacents
 * Sub-Networks.
 * Used by the Linkage class.
 * 
 * @author michael
 */
public class Link {	
	private Clique clique, v0, v1;
	
	/**
	 * Constructs a new Link with the parameter clique as the 
	 * clique of this new link.
	 * 
	 * @param clique clique of this new link.	
	 */
	public Link(Clique clique) {
		this.clique = clique;
	}
	
	protected void absorve(boolean naOrdem) {
		Clique c1, c2;
		
		if (naOrdem) {
			c1 = v0;
			c2 = v1;
		} else {
			c1 = v1;
			c2 = v0;						
		}

		NodeList toDie = SetToolkit.clone(c2.getNos());
		toDie.removeAll(clique.getNos());
		/*
		PotentialTable originalLinkTable = (PotentialTable) clique.getPotentialTable().clone();
		*/	
		
		PotentialTable tB =
			(PotentialTable) c2.getPotentialTable().clone();
			
		for (int i = 0; i < toDie.size(); i++) {
			tB.removeVariable(toDie.get(i));
		}
		
		/*
		for (int i = clique.getPotentialTable().tableSize() - 1; i >= 0; i--) {
			clique.getPotentialTable().setValue(i, tB.getValue(i));
		}
		tB = (PotentialTable) clique.getPotentialTable().clone();
		tB.directOpTab(originalLinkTable, PotentialTable.DIVISION_OPERATOR);
		*/		
		
		toDie = SetToolkit.clone(c1.getNos());
		toDie.removeAll(clique.getNos());

		PotentialTable tA = (PotentialTable) c1.getPotentialTable().clone();
		
		for (int i = 0; i < toDie.size(); i++) {
			tA.removeVariable(toDie.get(i));
		}
		
		tB.opTab(tA, PotentialTable.DIVISION_OPERATOR);		
		
		
		c1.getPotentialTable().opTab(tB, PotentialTable.PRODUCT_OPERATOR);
	}
	
	
	/**
	 * Sets the host clique on the first.sub-network.
	 * @param v0 The host clique on the first.sub-network
	 */
	public void setV0(Clique v0) {
		this.v0 = v0;
	}

	/**
	 * Sets the host clique on the second.sub-network.
	 * @param v1 the host clique on the second.sub-network.
	 */
	public void setV1(Clique v1) {
		this.v1 = v1;
	}

	/**
	 * Returns the clique of this Link.
	 * @return Clique the clique of this Link.
	 */
	public Clique getClique() {
		return clique;
	}

	/**
	 * Returns the host clique on the first.sub-network.
	 * @return Clique the host clique on the first.sub-network.
	 */
	public Clique getV0() {
		return v0;
	}

	/**
	 * Returns the host clique on the second.sub-network.
	 * @return Clique the host clique on the second.sub-network.
	 */
	public Clique getV1() {
		return v1;
	}

}
