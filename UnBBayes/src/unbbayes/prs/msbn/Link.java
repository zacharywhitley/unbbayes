package unbbayes.prs.msbn;

import unbbayes.prs.bn.Clique;

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
