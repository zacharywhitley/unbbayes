package unbbayes.prs.msbn;

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
	
	public Link(SubNetwork n1, SubNetwork n2) {
		this.n1 = n1;
		this.n2 = n2;
		n1.addAdjacent(n2);
		n2.setParent(n1);	
	}

}
