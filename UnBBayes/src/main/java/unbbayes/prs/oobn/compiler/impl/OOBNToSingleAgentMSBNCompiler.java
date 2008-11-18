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
import unbbayes.prs.oobn.compiler.IDisconnectedNetworkToMultipleSubnetworkConverter;
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
		
		Debug.println(this.getClass(), "OOBNToSingleAgentMSBNCompiler is not implemented yet. Returning stub...");
		
		// obtain network fragmenter
		IDisconnectedNetworkToMultipleSubnetworkConverter fragmenter = DisconnectedNetworkToMultipleSubnetworkConverterImpl.newInstance();
		
		try {
			// add each class to MSBN (TODO treat only those referenced by mainClass)
			for (IOOBNClass oobnClass : oobn.getOOBNClassList()) {
				// if network is disconnected, create multiple sub-networks in order to make them connected
				for (SubNetwork subnetwork : fragmenter.generateSubnetworks(oobnClass.getNetwork())) {
					msbn.addNetwork(subnetwork);
				}			
			}
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not convert " + oobn.getTitle() 
										 + " using " + mainClass.getNetwork().getName()
										 + " as main class", e);
		}
		
		
		
		return msbn;
	}
	
	

}
