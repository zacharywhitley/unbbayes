/**
 * 
 */
package unbbayes.prs.oobn.compiler.impl;

import unbbayes.prs.Network;
import unbbayes.prs.msbn.AbstractMSBN;
import unbbayes.prs.msbn.SingleAgentMSBN;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IObjectOrientedBayesianNetwork;
import unbbayes.prs.oobn.compiler.IOOBNCompiler;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNToSingleAgentMSBNCompiler implements IOOBNCompiler {

	/**
	 * 
	 */
	private OOBNToSingleAgentMSBNCompiler() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static OOBNToSingleAgentMSBNCompiler newInstance() {
		return new OOBNToSingleAgentMSBNCompiler();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.compiler.IOOBNCompiler#compile(unbbayes.prs.oobn.IObjectOrientedBayesianNetwork, unbbayes.prs.oobn.IOOBNClass)
	 */
	public Network compile(IObjectOrientedBayesianNetwork oobn,
			IOOBNClass mainClass) {
		// TODO Auto-generated method stub
		Debug.println(this.getClass(), "Compiler not yet implemented");
		AbstractMSBN msbn = new SingleAgentMSBN(oobn.getTitle());
		msbn.addNetwork(new SubNetwork("Stub1"));
		msbn.addNetwork(new SubNetwork("Stub2"));
		
		return msbn;
	}
	
	

}
