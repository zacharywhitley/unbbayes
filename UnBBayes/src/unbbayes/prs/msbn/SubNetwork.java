package unbbayes.prs.msbn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Network;

/**
 * @author Michael S. Onishi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SubNetwork extends Network {
	protected List adjacents;
	protected SubNetwork parent;
	
	public SubNetwork() {
		super();
		adjacents = new ArrayList();
	}
		
	
	public void addAdjacent(SubNetwork net) {
		adjacents.add(net);			
	}
	
	public void setParent(SubNetwork parent) {
		this.parent = parent;		
	}	
	
	public void compile() {
		distributedMoralization();		
	}
	
	private void distributedMoralization() {
		moraliza();		
	}
}
