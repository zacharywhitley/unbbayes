package unbbayes.prs.msbn;

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
	protected List adjacentes;
	protected SubNetwork pai;
	
	public void compile() {
		distributedMoralization();		
	}
	
	private void distributedMoralization() {
		moraliza();		
	}
}
