package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.util.NodeList;
import unbbayes.util.SetToolkit;

/**
 * @author michael
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Link {	
	private Clique clique, v0, v1;
	
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
	 * Sets the v0.
	 * @param v0 The v0 to set
	 */
	public void setV0(Clique v0) {
		this.v0 = v0;
	}

	/**
	 * Sets the v1.
	 * @param v1 The v1 to set
	 */
	public void setV1(Clique v1) {
		this.v1 = v1;
	}

	/**
	 * Returns the clique.
	 * @return Clique
	 */
	public Clique getClique() {
		return clique;
	}

	/**
	 * Returns the v0.
	 * @return Clique
	 */
	public Clique getV0() {
		return v0;
	}

	/**
	 * Returns the v1.
	 * @return Clique
	 */
	public Clique getV1() {
		return v1;
	}

}
