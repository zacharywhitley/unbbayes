/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasília
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
	private PotentialTable originalLinkTable;
	private PotentialTable newLinkTable;
	
	/**
	 * Constructs a new Link with the parameter clique as the 
	 * clique of this new link.
	 * 
	 * @param clique clique of this new link.	
	 */
	public Link(Clique clique) {
		this.clique = clique;
	}
	
	/**
	 * First pass to absorb
	 * @param naOrdem
	 */
	protected void absorveIn(boolean naOrdem) {
		Clique c2 = (naOrdem) ? v1 : v0;
		
		originalLinkTable = (PotentialTable) clique.getPotentialTable().clone();
				
		NodeList toDie = SetToolkit.clone(c2.getNos());
		toDie.removeAll(clique.getNos());
		newLinkTable = 
			(PotentialTable) c2.getPotentialTable().clone();
			
		for (int i = toDie.size()-1; i >= 0; i--) {
			newLinkTable.removeVariable(toDie.get(i));
		}
		
		for (int i = clique.getPotentialTable().tableSize() - 1; i >= 0; i--) {
			clique.getPotentialTable().setValue(i, newLinkTable.getValue(i));
		}
	}
	
	protected void removeRedundancy(PotentialTable newRedTab, PotentialTable oldRedTab) {
		newLinkTable.opTab(newRedTab, PotentialTable.DIVISION_OPERATOR);
		originalLinkTable.opTab(oldRedTab, PotentialTable.DIVISION_OPERATOR);
	}
	
	/**
	 * Second pass to absorb.
	 * @param naOrdem
	 */
	protected void absorveOut(boolean naOrdem) {
		Clique c1 = (naOrdem) ? v0 : v1;
				
		newLinkTable.directOpTab(originalLinkTable, PotentialTable.DIVISION_OPERATOR);
		
		c1.getPotentialTable().opTab(newLinkTable, PotentialTable.PRODUCT_OPERATOR);		
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (v0 + "-> " + clique + "<- " + v1);
	}

}
